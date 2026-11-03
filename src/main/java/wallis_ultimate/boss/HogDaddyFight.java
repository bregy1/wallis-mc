package wallis_ultimate.boss;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.GroundMark;
import wallis_ultimate.entity.HogDaddy;
import wallis_ultimate.entity.ZombiePiglet;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.survival.Downed;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.HogFarmBuild;

/**
 * ENEMY-011: the fight around Hog Daddy. Brings the team to the cabin at the far end of the cornfield and closes the
 * gate; he bursts through the wall and chases them to the barn (phase 1); once everyone still standing is there he comes
 * over the roof into the pen and the piglets start coming (phases 2 and 3). It ends when he dies, or when nobody inside
 * the ring is left standing. Then the farm is put back as it was. Numbers are proposals (30-enemies.md).
 * Who takes part is simply who is inside the ring (PROG-007), so there's no list of participants to get out of date.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HogDaddyFight {
    public enum Stage { INTRO, CHASE, PEN, OVER }

    /** Long enough to arrive, look around, and hear him. */
    public static final int INTRO_TICKS = 80;
    /** Everyone down or gone for this long: the fight is lost. */
    public static final int WIPE_TICKS = 60;
    public static final int PIGLET_WAVE_TICKS = 200;
    private static final int HAY_TICKS = 35;
    private static final int HAY_HEIGHT = 10;
    private static final float HAY_RADIUS = 1.5F;
    /** A bale takes about this long to come down from that height. */
    private static final int HAY_FALL_TICKS = 22;
    private static final int HAY_DAMAGE = 3;
    private static final int HAY_LEAD_TICKS = 25;
    private static final int SCALE_CHECK_TICKS = 20;
    private static final int RESTORE_DELAY_TICKS = 40;
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final double[] ARRIVAL_ROWS = {0.0, 1.0, -1.0};

    @Nullable
    private static HogDaddyFight real;
    /** GameTests only: their own small farms, ticked like the real one. */
    private static final List<HogDaddyFight> TEST_FIGHTS = new ArrayList<>();

    private final ServerLevel level;
    private final HogFarm farm;
    private final String team;
    private final boolean test;
    private final List<Player> testPlayers = new ArrayList<>();
    private final List<ZombiePiglet> piglets = new ArrayList<>();
    private final HogDaddy boss;
    private Stage stage = Stage.INTRO;
    private int ticks;
    private int nobodyStandingTicks;
    private int restoreTicks = -1;
    private boolean won;

    private HogDaddyFight(ServerLevel level, HogFarm farm, String team, boolean test) {
        this.level = level;
        this.farm = farm;
        this.team = team;
        this.test = test;
        boss = ModEntities.HOG_DADDY.get().create(level);
        Vec3 start = farm.standingAt(HogFarmBuild.BOSS_START);
        // Nobody may be anywhere near the farm yet.
        level.getChunkAt(BlockPos.containing(start));
        // Facing the wall he's about to come through.
        boss.moveTo(start.x, start.y, start.z, 90.0F, 0.0F);
        boss.joinFight(this);
        boss.setPhase(HogDaddy.Phase.CHASE);
        boss.waitBehindWall();
        level.addFreshEntity(boss);
        setGate(ModBlocks.RIVETED_STEEL.get().defaultBlockState());
    }

    /** The fight running in this world, if any. */
    @Nullable
    public static HogDaddyFight current() {
        return real;
    }

    /**
     * /wallis_boss hog_daddy start, and the bell at the farm's gate: starts the fight for the player's team, all of them
     * online and on their feet. Returns whether it started; if not, the player has been told why.
     */
    public static boolean start(ServerPlayer starter) {
        ServerLevel overworld = starter.server.overworld();
        HogFarm farm = HogFarm.of(overworld);
        if (farm == null) {
            starter.sendSystemMessage(message("no_farm"));
            return false;
        }
        HogDaddyProgress progress = HogDaddyProgress.get(overworld);
        String team = Teams.teamKey(starter);
        if (progress.hasBeaten(team)) {
            starter.sendSystemMessage(message("already_beaten"));
            return false;
        }
        progress.call(team);
        if (real != null) {
            starter.sendSystemMessage(message(real.team.equals(team) ? "already_fighting" : "busy"));
            return false;
        }
        List<ServerPlayer> members = new ArrayList<>(Teams.onlineTeammates(starter));
        members.add(starter);
        members.removeIf(Downed::isDowned);
        if (members.isEmpty()) {
            starter.sendSystemMessage(message("nobody_standing"));
            return false;
        }
        real = new HogDaddyFight(overworld, farm, team, false);
        progress.setFightRunning(true);
        Vec3 arrival = farm.standingAt(HogFarmBuild.CHASE_START);
        for (int i = 0; i < members.size(); i++) {
            ServerPlayer member = members.get(i);
            member.stopRiding();
            // Facing the door, side by side, all of them lined up with its three blocks of width.
            member.teleportTo(overworld, arrival.x, arrival.y, arrival.z + ARRIVAL_ROWS[i % ARRIVAL_ROWS.length], 90.0F, 0.0F);
            member.sendSystemMessage(message("snorting"));
        }
        return true;
    }

    /** GameTests only: a fight on a small farm of the test's own, which doesn't touch the world's records. */
    public static HogDaddyFight startForTest(ServerLevel level, HogFarm farm, Player... players) {
        HogDaddyFight fight = new HogDaddyFight(level, farm, "test", true);
        fight.testPlayers.addAll(List.of(players));
        TEST_FIGHTS.add(fight);
        return fight;
    }

    /** GameTests only. */
    public void stopForTest() {
        piglets.forEach(ZombiePiglet::discard);
        boss.discard();
        TEST_FIGHTS.remove(this);
    }

    private static Component message(String key) {
        return Component.translatable("message." + WallisUltimate.MOD_ID + ".hog_daddy." + key);
    }

    public HogDaddy boss() {
        return boss;
    }

    public HogFarm farm() {
        return farm;
    }

    public Stage stage() {
        return stage;
    }

    public boolean won() {
        return won;
    }

    public List<ZombiePiglet> piglets() {
        return piglets;
    }

    /** Spec: one extra piglet per player. */
    public static int pigletsPerWave(int players) {
        return 1 + Math.max(players, 1);
    }

    /** They keep coming, but only ever twice a wave at a time. */
    public static int pigletLimit(int players) {
        return 2 * pigletsPerWave(players);
    }

    /** PROG-007: every real player inside the ring, standing or not. Creative and spectator players are only watching. */
    public List<Player> playersInside() {
        List<Player> inside = new ArrayList<>(testPlayers);
        if (!test) {
            for (ServerPlayer player : level.players()) {
                if (farm.contains(player.getX(), player.getZ()) && !player.isCreative() && !player.isSpectator() && player.isAlive()) {
                    inside.add(player);
                }
            }
        }
        return inside;
    }

    private List<Player> standing() {
        List<Player> standing = playersInside();
        standing.removeIf(Downed::isDowned);
        return standing;
    }

    private void tick() {
        ticks++;
        if (stage == Stage.OVER) {
            if (--restoreTicks == 0) {
                restoreFarm(level, farm);
                TEST_FIGHTS.remove(this);
                if (this == real) {
                    real = null;
                }
            }
            return;
        }
        if (boss.isRemoved() || !boss.isAlive() && !won) {
            finish(false);
            return;
        }
        List<Player> inside = playersInside();
        List<Player> standing = standing();
        if (ticks % SCALE_CHECK_TICKS == 0) {
            boss.setPlayerCount(inside.size());
        }
        nobodyStandingTicks = standing.isEmpty() ? nobodyStandingTicks + 1 : 0;
        if (nobodyStandingTicks >= WIPE_TICKS) {
            finish(false);
            return;
        }
        switch (stage) {
            case INTRO -> {
                if (ticks == INTRO_TICKS / 2) {
                    level.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.ZOGLIN_ANGRY, SoundSource.HOSTILE, 3.0F, 0.5F);
                }
                if (ticks >= INTRO_TICKS) {
                    boss.burstThrough(farm.at(HogFarmBuild.WALL_FROM), farm.at(HogFarmBuild.WALL_TO));
                    stage = Stage.CHASE;
                    tell(message("run").copy().withStyle(ChatFormatting.RED));
                }
            }
            case CHASE -> tickChase(standing);
            case PEN -> tickPen(inside.size());
            default -> {
            }
        }
    }

    private void tickChase(List<Player> standing) {
        if (!standing.isEmpty() && standing.stream().allMatch(player -> farm.reachedBarn(player.position()))) {
            stage = Stage.PEN;
            boss.leapInto(farm.standingAt(HogFarmBuild.PEN_LANDING));
            tell(message("pen"));
            return;
        }
        if (ticks % HAY_TICKS == 0 && !standing.isEmpty()) {
            dropHayNear(standing.get(level.getRandom().nextInt(standing.size())));
        }
    }

    /**
     * Spec: falling hay in the chase. A bale comes down ahead of a runner, marked by a red circle while it falls, and
     * stays there as an obstacle until he tramples it or the farm is put back.
     */
    private void dropHayNear(Player runner) {
        Vec3 moving = runner.position().subtract(runner.xo, runner.yo, runner.zo).multiply(1.0, 0.0, 1.0);
        Vec3 spot = runner.position().add(moving.scale(HAY_LEAD_TICKS));
        if (!farm.contains(spot.x, spot.z) || farm.reachedBarn(spot)) {
            return;
        }
        BlockPos top = BlockPos.containing(spot.x, runner.getY() + HAY_HEIGHT, spot.z);
        if (!level.getBlockState(top).isAir()) {
            return;
        }
        GroundMark.disc(level, spot, HAY_RADIUS, HAY_FALL_TICKS, HAY_FALL_TICKS + 2);
        FallingBlockEntity bale = FallingBlockEntity.fall(level, top, Blocks.HAY_BLOCK.defaultBlockState());
        bale.setHurtsEntities(1.0F, HAY_DAMAGE);
        bale.disableDrop();
    }

    private void tickPen(int players) {
        if (ticks % PIGLET_WAVE_TICKS == 0) {
            spawnPiglets(players);
        }
    }

    private void spawnPiglets(int players) {
        piglets.removeIf(piglet -> !piglet.isAlive());
        int room = Math.min(pigletsPerWave(players), pigletLimit(players) - piglets.size());
        int spawns = farm.pigletSpawns();
        for (int i = 0; i < room && spawns > 0; i++) {
            ZombiePiglet piglet = ModEntities.ZOMBIE_PIGLET.get().create(level);
            Vec3 at = Vec3.atBottomCenterOf(farm.pigletSpawn(i % spawns));
            piglet.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(piglet);
            piglets.add(piglet);
        }
    }

    public void onStunned() {
        tell(message("stunned"));
    }

    public void onEnraged() {
        tell(message("enraged").copy().withStyle(ChatFormatting.RED));
    }

    public void onBossDied() {
        won = true;
        finish(true);
    }

    private void finish(boolean victory) {
        if (stage == Stage.OVER) {
            return;
        }
        stage = Stage.OVER;
        restoreTicks = RESTORE_DELAY_TICKS;
        piglets.forEach(ZombiePiglet::discard);
        if (!boss.isDeadOrDying()) {
            boss.discard();
        }
        tell(message(victory ? "beaten" : "wiped").copy().withStyle(victory ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        // Right away, so nobody waits to leave. The rest comes back when the dust has settled.
        setGate(Blocks.AIR.defaultBlockState());
        if (!test) {
            HogDaddyProgress progress = HogDaddyProgress.get(level.getServer().overworld());
            progress.setFightRunning(false);
            if (victory) {
                progress.markBeaten(team);
            }
        }
    }

    private void tell(Component text) {
        for (Player player : playersInside()) {
            player.displayClientMessage(text, true);
        }
    }

    private void setGate(BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(farm.at(HogFarmBuild.GATE_FROM), farm.at(HogFarmBuild.GATE_TO))) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    /**
     * Puts back whatever the fight changed: the wall, the corn, the posts, and clears the bales. Only touches blocks that
     * are different, so it's cheap and sends little. Compares by block: fences and the like have changed their state to
     * fit their neighbours.
     */
    static void restoreFarm(ServerLevel level, HogFarm farm) {
        farm.blueprint().blocks().forEach((relative, state) -> {
            BlockPos at = farm.origin().offset(relative);
            if (!level.getBlockState(at).is(state.getBlock())) {
                level.setBlock(at, state, QUIET);
            }
        });
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < farm.blueprint().sizeX(); x++) {
            for (int z = 0; z < farm.blueprint().sizeZ(); z++) {
                for (int y = 0; y <= 2; y++) {
                    pos.set(farm.origin().getX() + x, farm.origin().getY() + y, farm.origin().getZ() + z);
                    if (level.getBlockState(pos).is(Blocks.HAY_BLOCK) && !farm.blueprint().blocks().containsKey(new BlockPos(x, y, z))) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), QUIET);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (real != null) {
            real.tick();
        }
        new ArrayList<>(TEST_FIGHTS).forEach(HogDaddyFight::tick);
    }

    /** A fight doesn't survive a restart (its mobs aren't saved), so if one was running the farm is put back. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        HogDaddyProgress progress = HogDaddyProgress.get(overworld);
        HogFarm farm = HogFarm.of(overworld);
        if (progress.fightRunning() && farm != null) {
            restoreFarm(overworld, farm);
            progress.setFightRunning(false);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        real = null;
        TEST_FIGHTS.clear();
    }
}
