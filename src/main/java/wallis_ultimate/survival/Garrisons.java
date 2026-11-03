package wallis_ultimate.survival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.Defender;
import wallis_ultimate.entity.Dropship;
import wallis_ultimate.entity.NpcUnstuck;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * SURV-011: what keeps each safe spot going, while players are around to load it. It has its guards at their posts,
 * calls in a dropship when it runs short, notices what got broken so guards can repair it, and remembers which players
 * are in trouble for mining there. Only runs where the chunks are loaded; an empty safe spot costs nothing (PACK-003).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Garrisons {
    /** How many guards each safe spot has (my proposals). */
    private static final Map<String, Integer> SIZES = Map.of("earls_salvage", 10, "gas_n_guns", 8, "camp_tarp", 4, "stranded_hauler", 3);
    private static final int DEFAULT_SIZE = 4;
    private static final int CHECK_TICKS = 40;
    /** After its chunks load, the entities in them take a moment to appear; counting right away would see nobody. */
    private static final int SETTLE_TICKS = 60;
    /** How long a safe spot waits for help after losing guards. */
    public static final int REINFORCE_DELAY_TICKS = 900;
    /** With a player this close, reinforcements come by air. Otherwise nobody sees it, and they're just there. */
    private static final double WITNESS_RANGE = 96.0;
    private static final int MAX_PER_DROPSHIP = 6;
    private static final int[] APPROACH_DISTANCES = {96, 72, 48, 32};
    /** How long a player who mined here stays wanted, unless they die first. */
    public static final int ANGRY_TICKS = 20 * 60 * 3;
    private static final int SCAN_PER_TICK = 6;
    /** One block every 3 seconds per safe spot, however many guards there are: "slowly patched up". */
    private static final int REPAIR_TICKS = 60;
    private static final int FALLBACK_POST_MIN_RADIUS = 3;
    private static final int FALLBACK_POST_SPREAD = 6;

    private static final Map<String, Garrison> GARRISONS = new HashMap<>();
    /** GameTests only: guards for the test zones. */
    private static final List<Garrison> TEST_GARRISONS = new ArrayList<>();
    private static int reinforceDelay = REINFORCE_DELAY_TICKS;

    private Garrisons() {
    }

    private static final class Garrison {
        final SafeSpot spot;
        final int size;
        @Nullable
        final Blueprint blueprint;
        final List<Map.Entry<BlockPos, BlockState>> toCheck = new ArrayList<>();
        final Set<BlockPos> broken = new LinkedHashSet<>();
        final Map<UUID, Long> wanted = new HashMap<>();
        final boolean isTest;
        BlockPos origin;
        int scanAt;
        int loadedTicks;
        long shortSince = -1;
        long nextRepairAt;
        @Nullable
        Dropship dropship;
        boolean filled;

        Garrison(SafeSpot spot, int size, @Nullable Blueprint blueprint, boolean isTest) {
            this.spot = spot;
            this.size = size;
            this.blueprint = blueprint;
            this.isTest = isTest;
            origin = spot.origin();
            if (blueprint != null) {
                if (origin == null) {
                    // Safe spots saved before they remembered this: it follows from how they're centered.
                    origin = spot.center().offset(-blueprint.sizeX() / 2, 0, -blueprint.sizeZ() / 2);
                }
                for (Map.Entry<BlockPos, BlockState> block : blueprint.blocks().entrySet()) {
                    // Nothing that's meant to be empty: players may build there. No loot chests: a new one would refill.
                    if (!block.getValue().isAir() && !blueprint.lootTables().containsKey(block.getKey())) {
                        toCheck.add(block);
                    }
                }
            }
        }

        boolean isBuilt(BlockPos pos) {
            if (blueprint == null) {
                return true;
            }
            BlockState planned = blueprint.blocks().get(pos.subtract(origin));
            return planned != null && !planned.isAir();
        }

        List<BlockPos> postList(ServerLevel level) {
            List<BlockPos> result = new ArrayList<>();
            if (blueprint != null) {
                blueprint.marks().entrySet().stream()
                        .filter(mark -> mark.getKey().startsWith(GUARD_MARK))
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(mark -> result.add(origin.offset(mark.getValue())));
            }
            if (result.isEmpty()) {
                // No posts marked in this build: around the signpost, wherever there's room.
                for (int i = 0; i < size; i++) {
                    double angle = i * Math.PI * 2.0 / size;
                    int distance = FALLBACK_POST_MIN_RADIUS + i % FALLBACK_POST_SPREAD;
                    int x = spot.signpost().getX() + (int) Math.round(Math.cos(angle) * distance);
                    int z = spot.signpost().getZ() + (int) Math.round(Math.sin(angle) * distance);
                    int y = Math.min(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), spot.signpost().getY() + 3);
                    result.add(new BlockPos(x, y, z));
                }
            }
            return result;
        }
    }

    /** Blueprints mark where guards stand with marks named guard1, guard2, and so on. */
    public static final String GUARD_MARK = "guard";

    private static Garrison garrison(SafeSpot spot) {
        return GARRISONS.computeIfAbsent(spot.id(), id -> new Garrison(spot, SIZES.getOrDefault(id, DEFAULT_SIZE), DistrictSites.blueprintOf(id), false));
    }

    @Nullable
    private static Garrison garrison(ServerLevel level, String spotId) {
        for (Garrison test : TEST_GARRISONS) {
            if (test.spot.id().equals(spotId)) {
                return test;
            }
        }
        return DistrictSites.get(level).spot(spotId).map(Garrisons::garrison).orElse(null);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ServerLevel overworld = event.getServer().overworld();
        for (SafeSpot spot : DistrictSites.get(overworld).spots()) {
            tick(overworld, garrison(spot));
        }
        for (Garrison test : TEST_GARRISONS) {
            tick(overworld, test);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        GARRISONS.clear();
        TEST_GARRISONS.clear();
        reinforceDelay = REINFORCE_DELAY_TICKS;
    }

    /** Everything that hunts players hunts defenders too, so a horde reaching a safe spot turns into a fight by itself. */
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Monster monster) {
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(monster, Defender.class, true));
        }
    }

    private static boolean isAllLoaded(ServerLevel level, SafeSpot spot) {
        BlockPos center = spot.center();
        int r = spot.radius();
        return level.isPositionEntityTicking(center) && level.isPositionEntityTicking(center.offset(r, 0, r))
                && level.isPositionEntityTicking(center.offset(-r, 0, r)) && level.isPositionEntityTicking(center.offset(r, 0, -r))
                && level.isPositionEntityTicking(center.offset(-r, 0, -r));
    }

    private static void tick(ServerLevel level, Garrison garrison) {
        if (!isAllLoaded(level, garrison.spot)) {
            garrison.loadedTicks = 0;
            return;
        }
        garrison.loadedTicks++;
        if (garrison.loadedTicks < SETTLE_TICKS) {
            return;
        }
        scanForDamage(level, garrison);
        if (garrison.loadedTicks % CHECK_TICKS == 0) {
            checkCount(level, garrison);
        }
    }

    /** A few blocks each tick, in turn, so a big safe spot never costs much at once. */
    private static void scanForDamage(ServerLevel level, Garrison garrison) {
        if (garrison.toCheck.isEmpty()) {
            return;
        }
        for (int i = 0; i < SCAN_PER_TICK; i++) {
            garrison.scanAt = (garrison.scanAt + 1) % garrison.toCheck.size();
            Map.Entry<BlockPos, BlockState> planned = garrison.toCheck.get(garrison.scanAt);
            BlockPos at = garrison.origin.offset(planned.getKey());
            if (level.hasChunkAt(at) && !isSameBlock(level.getBlockState(at), planned.getValue())) {
                garrison.broken.add(at);
            }
        }
    }

    /** Doors open, fires go out, and grass grows over dirt: none of that is damage. */
    private static boolean isSameBlock(BlockState now, BlockState planned) {
        return now.is(planned.getBlock()) || now.is(BlockTags.DIRT) && planned.is(BlockTags.DIRT);
    }

    private static void checkCount(ServerLevel level, Garrison garrison) {
        List<Defender> guards = level.getEntitiesOfClass(Defender.class, areaOf(garrison.spot),
                guard -> garrison.spot.id().equals(guard.spotId()) && !guard.isMainNpc());
        int missing = garrison.size - guards.size();
        if (garrison.dropship != null && !garrison.dropship.isRemoved()) {
            if (!garrison.dropship.isDone()) {
                return;
            }
            garrison.dropship = null;
        }
        if (missing <= 0) {
            garrison.shortSince = -1;
            garrison.filled = true;
            markFilled(level, garrison);
            return;
        }
        if (!isWitnessed(level, garrison.spot) || !hasBeenFilled(level, garrison)) {
            // Nobody sees it, or it's the first time: a player arriving at a new safe spot finds its guards there already.
            for (BlockPos post : freePosts(level, garrison, guards, missing)) {
                spawnGuard(level, garrison, post);
            }
            garrison.shortSince = -1;
            garrison.filled = true;
            markFilled(level, garrison);
            return;
        }
        long now = level.getGameTime();
        if (garrison.shortSince < 0) {
            garrison.shortSince = now;
        }
        if (now - garrison.shortSince >= reinforceDelay) {
            sendDropship(level, garrison, freePosts(level, garrison, guards, Math.min(missing, MAX_PER_DROPSHIP)));
            garrison.shortSince = now;
        }
    }

    /** Anyone looking, spectators too. */
    private static boolean isWitnessed(ServerLevel level, SafeSpot spot) {
        double rangeSqr = WITNESS_RANGE * WITNESS_RANGE;
        for (Player player : level.players()) {
            if (player.distanceToSqr(spot.center().getX(), spot.center().getY(), spot.center().getZ()) <= rangeSqr) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBeenFilled(ServerLevel level, Garrison garrison) {
        return garrison.isTest ? garrison.filled : DistrictSites.get(level).hasBeenGarrisoned(garrison.spot.id());
    }

    private static void markFilled(ServerLevel level, Garrison garrison) {
        if (!garrison.isTest) {
            DistrictSites.get(level).markGarrisoned(garrison.spot.id());
        }
    }

    private static AABB areaOf(SafeSpot spot) {
        double reach = spot.radius() + 32.0;
        return new AABB(spot.center()).inflate(reach, 64.0, reach);
    }

    /** The posts with the fewest guards at them, so losses get replaced where they happened. */
    private static List<BlockPos> freePosts(ServerLevel level, Garrison garrison, List<Defender> guards, int count) {
        List<BlockPos> posts = garrison.postList(level);
        Map<BlockPos, Integer> taken = new HashMap<>();
        for (Defender guard : guards) {
            if (guard.post() != null) {
                taken.merge(guard.post(), 1, Integer::sum);
            }
        }
        List<BlockPos> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BlockPos best = posts.get(0);
            int fewest = Integer.MAX_VALUE;
            for (BlockPos post : posts) {
                int here = taken.getOrDefault(post, 0);
                if (here < fewest) {
                    fewest = here;
                    best = post;
                }
            }
            taken.merge(best, 1, Integer::sum);
            result.add(best);
        }
        return result;
    }

    private static void spawnGuard(ServerLevel level, Garrison garrison, BlockPos post) {
        Defender guard = ModEntities.DEFENDER.get().create(level);
        BlockPos at = post;
        if (!level.getBlockState(post).getCollisionShape(level, post).isEmpty()) {
            BlockPos free = NpcUnstuck.freeSpotNear(level, post);
            at = free != null ? free : post;
        }
        guard.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        guard.assign(garrison.spot.id(), post);
        level.addFreshEntity(guard);
    }

    private static void sendDropship(ServerLevel level, Garrison garrison, List<BlockPos> posts) {
        BlockPos center = garrison.spot.center();
        for (int distance : APPROACH_DISTANCES) {
            BlockPos start = center.offset(distance, 0, 0);
            if (level.isPositionEntityTicking(start) && level.isPositionEntityTicking(center.offset(-distance, 0, 0))
                    && level.isPositionEntityTicking(center.offset(0, 0, distance)) && level.isPositionEntityTicking(center.offset(0, 0, -distance))) {
                garrison.dropship = Dropship.send(level, garrison.spot.id(), center, posts, distance);
                return;
            }
        }
    }

    /** The next broken block for this guard to fix, or null if there's none or it isn't time yet. */
    @Nullable
    public static BlockPos nextRepair(Defender guard) {
        if (!(guard.level() instanceof ServerLevel level) || guard.spotId().isEmpty()) {
            return null;
        }
        Garrison garrison = garrison(level, guard.spotId());
        if (garrison == null || garrison.broken.isEmpty() || level.getGameTime() < garrison.nextRepairAt) {
            return null;
        }
        BlockPos next = garrison.broken.iterator().next();
        garrison.broken.remove(next);
        garrison.nextRepairAt = level.getGameTime() + REPAIR_TICKS;
        return next;
    }

    /** Puts the planned block back, unless someone's standing in the way; the next scan notices it again then. */
    public static void repair(Defender guard, BlockPos pos) {
        if (!(guard.level() instanceof ServerLevel level)) {
            return;
        }
        Garrison garrison = garrison(level, guard.spotId());
        if (garrison == null || garrison.blueprint == null) {
            return;
        }
        BlockState planned = garrison.blueprint.blocks().get(pos.subtract(garrison.origin));
        if (planned == null || isSameBlock(level.getBlockState(pos), planned) || !level.isUnobstructed(planned, pos, CollisionContext.empty())) {
            return;
        }
        level.setBlock(pos, planned, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        BlockState connected = Block.updateFromNeighbourShapes(planned, level, pos);
        // A door or bed half whose other half isn't back yet would vanish; it comes back on its own next.
        if (!connected.isAir() && connected != planned) {
            level.setBlock(pos, connected, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
        guard.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, planned.getSoundType(level, pos, guard).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.01);
    }

    private static boolean isBuilder(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    /** Q-155: mining is allowed, but it's what turns the guards on you. Trees and ground around the build don't count. */
    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || isBuilder(event.getPlayer())) {
            return;
        }
        SafeSpot spot = SafeZones.spotAt(level, event.getPos().getX(), event.getPos().getZ());
        if (spot == null) {
            return;
        }
        Garrison garrison = garrison(level, spot.id());
        if (garrison != null && garrison.isBuilt(event.getPos())) {
            markWanted(level, garrison, event.getPlayer());
        }
    }

    /** Hitting a guard is as bad as mining. */
    public static void anger(Defender guard, Player player) {
        if (guard.level() instanceof ServerLevel level && !isBuilder(player)) {
            Garrison garrison = garrison(level, guard.spotId());
            if (garrison != null) {
                markWanted(level, garrison, player);
            }
        }
    }

    private static void markWanted(ServerLevel level, Garrison garrison, Player player) {
        Long before = garrison.wanted.put(player.getUUID(), level.getGameTime() + ANGRY_TICKS);
        if (before == null || before < level.getGameTime()) {
            player.sendSystemMessage(Component.translatable("message." + WallisUltimate.MOD_ID + ".safe_spot.angry").withStyle(ChatFormatting.RED));
        }
    }

    public static boolean isWantedBy(Defender guard, Player player) {
        if (!(guard.level() instanceof ServerLevel level) || isBuilder(player) || Downed.isDowned(player)) {
            return false;
        }
        Garrison garrison = garrison(level, guard.spotId());
        if (garrison == null) {
            return false;
        }
        Long until = garrison.wanted.get(player.getUUID());
        return until != null && level.getGameTime() < until;
    }

    /** Dying is enough: everyone calms down. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        forgive(event.getEntity());
    }

    public static void forgive(Player player) {
        GARRISONS.values().forEach(garrison -> garrison.wanted.remove(player.getUUID()));
        TEST_GARRISONS.forEach(garrison -> garrison.wanted.remove(player.getUUID()));
    }

    /** GameTests and the smoke test only: reinforcements arrive sooner, so tests don't wait 45 seconds. */
    public static void setReinforceDelayForTests(int ticks) {
        reinforceDelay = ticks;
    }

    /** GameTests only: guards for a test zone, with a blueprint to repair if there is one. */
    public static void addTestGarrison(SafeSpot zone, int size, @Nullable Blueprint blueprint) {
        TEST_GARRISONS.add(new Garrison(zone, size, blueprint, true));
    }

    /** GameTests only. */
    public static void removeTestGarrison(SafeSpot zone) {
        TEST_GARRISONS.removeIf(garrison -> garrison.spot == zone);
    }
}
