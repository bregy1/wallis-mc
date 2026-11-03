package wallis_ultimate.survival;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * ENEMY-006, WORLD-012: every 7th night a horde comes. Each player counts the nights they were online for. When a team
 * plays together, whoever is closest decides, and everyone present starts over. The horde goes for the bed most of the
 * team uses; with nobody home, it comes for the players wherever they are (Q-157). It can't be slept through (Q-158).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HordeNights {
    public static final int NIGHTS_PER_HORDE = 7;
    /** Proposals: three waves, a minute apart. The same in every horde of a district (PROG-007). */
    public static final int WAVES = 3;
    private static final int WAVE_GAP_TICKS = 60 * 20;
    private static final int WAVE_SIZE = 6;
    private static final int WAVE_SIZE_PER_EXTRA_PLAYER = 4;
    /** How close to the bed still counts as being home. */
    public static final int BASE_RADIUS = 64;
    /** Players this close together away from home share one fight. */
    private static final int TOGETHER_RADIUS = 32;
    private static final int RING_DISTANCE = 24;
    private static final int RING_WIDTH = 12;
    private static final int RING_ZONE_MARGIN = 4;
    private static final int SPOT_TRIES = 16;
    private static final int HEIGHT_REACH = 8;

    private static final long TICKS_PER_DAY = 24000L;
    /** SURV-007: Hourglass runs daytime's 13,000 ticks in 16 real minutes, so this is what a real minute is worth. */
    private static final double DAYTIME_TICKS_PER_MINUTE = 13000.0 / 16.0;
    public static final long FIRST_WARNING = Beds.NIGHTFALL - Math.round(2.0 * DAYTIME_TICKS_PER_MINUTE);
    public static final long SECOND_WARNING = Beds.NIGHTFALL - Math.round(DAYTIME_TICKS_PER_MINUTE);
    /** A bigger step than this is somebody's /time command, not time going by, so it doesn't count a night. */
    private static final long LARGEST_TIME_STEP = 100L;

    private static final String SAVE_KEY = WallisUltimate.MOD_ID + ":horde";
    private static final String NIGHTS = "Nights";
    private static final String HORDE_DAY = "HordeDay";
    private static final String MESSAGE = "message." + WallisUltimate.MOD_ID + ".horde.";

    private static final List<Horde> HORDES = new ArrayList<>();
    private static long lastDayTime = -1L;

    /** A horde under way, which sends its waves whether or not its players stay put. */
    private static final class Horde {
        final List<UUID> players;
        int wavesLeft = WAVES;
        int nextWaveTick;

        Horde(List<UUID> players, int nextWaveTick) {
            this.players = players;
            this.nextWaveTick = nextWaveTick;
        }
    }

    /** Where a wave of the horde goes, and who's there to fight it. */
    public record Fight(BlockPos center, List<Player> players) {
    }

    private HordeNights() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        long now = server.overworld().getDayTime();
        long before = lastDayTime;
        lastDayTime = now;
        if (before >= 0L && isTimeGoingBy(before, now)) {
            List<ServerPlayer> online = server.getPlayerList().getPlayers();
            if (crossed(before, now, FIRST_WARNING)) {
                warn(online, "uneasy");
            }
            if (crossed(before, now, SECOND_WARNING)) {
                warn(online, "worse");
            }
            if (crossed(before, now, Beds.NIGHTFALL)) {
                nightfall(online);
            }
        }
        HORDES.removeIf(horde -> sendWaveIfDue(server, horde));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HORDES.clear();
        lastDayTime = -1L;
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && isHordeNight(new ArrayList<Player>(teamOnline(player)), player.server.overworld().getDayTime())) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            player.sendSystemMessage(Component.translatable(MESSAGE + "no_sleep"));
        }
    }

    public static boolean isTimeGoingBy(long before, long now) {
        return now > before && now - before <= LARGEST_TIME_STEP;
    }

    /** Whether that time of day came around after {@code before}, up to and including {@code now}. */
    public static boolean crossed(long before, long now, long timeOfDay) {
        return before + Math.floorMod(timeOfDay - before - 1L, TICKS_PER_DAY) + 1L <= now;
    }

    public static int nightsCounted(Player player) {
        return saved(player).getInt(NIGHTS);
    }

    public static void setNightsCounted(Player player, int nights) {
        CompoundTag tag = saved(player);
        tag.putInt(NIGHTS, nights);
        save(player, tag);
    }

    public static void countNight(Player player) {
        setNightsCounted(player, nightsCounted(player) + 1);
    }

    /** The counter closest to its horde night decides for everyone present. */
    public static boolean isDue(List<? extends Player> present) {
        return present.stream().anyMatch(player -> nightsCounted(player) >= NIGHTS_PER_HORDE);
    }

    public static boolean isTomorrow(List<? extends Player> present) {
        return present.stream().anyMatch(player -> nightsCounted(player) >= NIGHTS_PER_HORDE - 1);
    }

    /** Everyone present starts over, and remembers that tonight is the night. */
    public static void markHorde(List<? extends Player> present, long day) {
        for (Player player : present) {
            CompoundTag tag = saved(player);
            tag.putInt(NIGHTS, 0);
            tag.putLong(HORDE_DAY, day);
            save(player, tag);
        }
    }

    /** True from nightfall to dawn if anyone present has a horde tonight, so nobody can sleep through it. */
    public static boolean isHordeNight(List<? extends Player> present, long dayTime) {
        long timeOfDay = dayTime % TICKS_PER_DAY;
        long day = dayTime / TICKS_PER_DAY;
        return timeOfDay >= Beds.NIGHTFALL && timeOfDay < Beds.DAWN && present.stream().anyMatch(player -> {
            CompoundTag tag = saved(player);
            return tag.contains(HORDE_DAY) && tag.getLong(HORDE_DAY) == day;
        });
    }

    /** The home that the most homes are close to, or null if nobody has one. Ties go to whoever comes first. */
    @Nullable
    public static BlockPos sharedBase(List<BlockPos> homes) {
        BlockPos best = null;
        long bestUsers = 0L;
        for (BlockPos home : homes) {
            long users = homes.stream().filter(other -> other.closerThan(home, BASE_RADIUS)).count();
            if (users > bestUsers) {
                best = home;
                bestUsers = users;
            }
        }
        return best;
    }

    /** At the base if anyone's home; otherwise around each bunch of players, wherever they are (Q-157). */
    public static List<Fight> fights(List<Player> team, @Nullable BlockPos base) {
        if (base != null) {
            List<Player> home = team.stream().filter(player -> player.blockPosition().closerThan(base, BASE_RADIUS)).toList();
            if (!home.isEmpty()) {
                return List.of(new Fight(base, home));
            }
        }
        List<Fight> fights = new ArrayList<>();
        for (Player player : team) {
            Fight together = fights.stream().filter(fight -> player.blockPosition().closerThan(fight.center(), TOGETHER_RADIUS)).findFirst().orElse(null);
            if (together != null) {
                together.players().add(player);
            } else {
                List<Player> players = new ArrayList<>();
                players.add(player);
                fights.add(new Fight(player.blockPosition(), players));
            }
        }
        return fights;
    }

    /** PROG-007: more players in the fight, more zombies; the same every time. */
    public static int waveSize(int players) {
        return WAVE_SIZE + WAVE_SIZE_PER_EXTRA_PLAYER * (players - 1);
    }

    /** Waves come from outside a safe spot, never from inside it (SURV-006), so the ring goes around the whole area. */
    public static int ringDistance(@Nullable SafeSpot zone) {
        return zone != null ? zone.radius() + RING_ZONE_MARGIN : RING_DISTANCE;
    }

    /** Every online player's night counts, then each team finds out whether its horde is due. */
    public static void nightfall(List<ServerPlayer> online) {
        online.forEach(HordeNights::countNight);
        teams(online).stream().filter(HordeNights::isDue).forEach(HordeNights::start);
    }

    private static void warn(List<ServerPlayer> online, String message) {
        teams(online).stream().filter(HordeNights::isTomorrow).forEach(team -> team.forEach(player -> {
            player.sendSystemMessage(Component.translatable(MESSAGE + message).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0F, 1.0F);
        }));
    }

    private static void start(List<ServerPlayer> team) {
        MinecraftServer server = team.get(0).server;
        markHorde(team, server.overworld().getDayTime() / TICKS_PER_DAY);
        for (ServerPlayer player : team) {
            player.sendSystemMessage(Component.translatable(MESSAGE + "started").withStyle(ChatFormatting.RED));
            player.playNotifySound(SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        HORDES.add(new Horde(team.stream().map(Player::getUUID).toList(), server.getTickCount()));
    }

    /** True when this horde has sent all its waves. */
    private static boolean sendWaveIfDue(MinecraftServer server, Horde horde) {
        if (server.getTickCount() < horde.nextWaveTick) {
            return false;
        }
        ServerLevel level = server.overworld();
        List<ServerPlayer> team = horde.players.stream().map(server.getPlayerList()::getPlayer).filter(Objects::nonNull)
                .filter(player -> player.isAlive() && player.level() == level).toList();
        // Peaceful would remove every zombie on its next tick anyway.
        if (!team.isEmpty() && level.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPos base = sharedBase(team.stream().map(HordeNights::home).filter(Objects::nonNull).toList());
            fights(new ArrayList<>(team), base).forEach(fight -> sendWave(level, fight));
        }
        horde.wavesLeft--;
        horde.nextWaveTick = server.getTickCount() + WAVE_GAP_TICKS;
        return horde.wavesLeft <= 0;
    }

    private static void sendWave(ServerLevel level, Fight fight) {
        SafeSpot zone = SafeZones.spotAt(level, fight.center().getX(), fight.center().getZ());
        BlockPos around = zone != null ? zone.center() : fight.center();
        int distance = ringDistance(zone);
        Consumer<BlockPos> spawn = spot -> {
            RegularZombie zombie = ModEntities.ZOMBIE.get().create(level);
            if (zombie != null) {
                zombie.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.EVENT, null, null);
                zombie.hearNoise(fight.center());
                fight.players().stream().min((a, b) -> Double.compare(zombie.distanceToSqr(a), zombie.distanceToSqr(b))).ifPresent(zombie::setTarget);
                level.addFreshEntity(zombie);
            }
        };
        int wanted = waveSize(fight.players().size());
        int spawned = 0;
        for (int i = 0; i < wanted; i++) {
            BlockPos spot = findSpot(level, around, distance);
            if (spot != null) {
                spawn.accept(spot);
                spawned++;
            }
        }
        WallisUltimate.LOGGER.info("Horde wave around {}: {} of {} zombies", around, spawned, wanted);
    }

    @Nullable
    private static BlockPos findSpot(ServerLevel level, BlockPos around, int distance) {
        for (int i = 0; i < SPOT_TRIES; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double reach = distance + level.random.nextDouble() * RING_WIDTH;
            int x = around.getX() + (int) (Math.cos(angle) * reach);
            int z = around.getZ() + (int) (Math.sin(angle) * reach);
            if (SafeZones.spotAt(level, x, z) == null) {
                BlockPos spot = standingPlace(level, x, around.getY(), z);
                if (spot != null) {
                    return spot;
                }
            }
        }
        return null;
    }

    /** As near the fight's height as possible, so a horde can reach someone in a cave; otherwise on the surface. */
    @Nullable
    private static BlockPos standingPlace(ServerLevel level, int x, int y, int z) {
        for (int offset = 0; offset <= HEIGHT_REACH; offset++) {
            BlockPos above = new BlockPos(x, y + offset, z);
            if (canStandAt(level, above)) {
                return above;
            }
            BlockPos below = new BlockPos(x, y - offset, z);
            if (canStandAt(level, below)) {
                return below;
            }
        }
        BlockPos surface = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        return canStandAt(level, surface) ? surface : null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos spot) {
        return level.isPositionEntityTicking(spot) && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, spot, ModEntities.ZOMBIE.get());
    }

    /** A player's own bed; or else wherever they last slept, which may be a safe spot's bed. */
    @Nullable
    private static BlockPos home(ServerPlayer player) {
        BlockPos bed = Beds.standingBed(player, player.server.overworld(), Beds.planetOf(Level.OVERWORLD));
        if (bed != null) {
            return bed;
        }
        return player.getRespawnDimension() == Level.OVERWORLD ? player.getRespawnPosition() : null;
    }

    private static List<ServerPlayer> teamOnline(ServerPlayer player) {
        String team = Teams.teamKey(player);
        return player.server.getPlayerList().getPlayers().stream().filter(other -> Teams.teamKey(other).equals(team)).toList();
    }

    private static List<List<ServerPlayer>> teams(List<ServerPlayer> online) {
        Map<String, List<ServerPlayer>> teams = new LinkedHashMap<>();
        online.forEach(player -> teams.computeIfAbsent(Teams.teamKey(player), key -> new ArrayList<>()).add(player));
        return List.copyOf(teams.values());
    }

    /** Forge only carries this part of a player's data over when they respawn, and the counter has to outlive a death. */
    private static CompoundTag saved(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(SAVE_KEY);
    }

    private static void save(Player player, CompoundTag tag) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(SAVE_KEY, tag);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
