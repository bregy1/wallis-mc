package wallis_ultimate.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModEntities;

/** Checks for the regular zombie: ENEMY-001 (speed), ENEMY-002 (Sunburn), ENEMY-005 (no block damage), ENEMY-012. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZombieGameTests {
    private static final String PLATFORM = "platform";
    /** The speed tests change the time of day, so each gets its own batch (batches run one after another). */
    private static final String DAY = "zombie_day";
    private static final String NIGHT = "zombie_night";
    private static final double SPEED_TOLERANCE = 0.08;

    private ZombieGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void regularZombiesReplaceVanillaZombieSpawns(GameTestHelper helper) {
        List<MobSpawnSettings.SpawnerData> monsters = helper.getLevel().getBiome(helper.absolutePos(BlockPos.ZERO))
                .value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
        if (monsters.stream().noneMatch(spawner -> spawner.type == ModEntities.ZOMBIE.get())) {
            helper.fail("no regular zombie spawns here");
        }
        for (EntityType<?> vanilla : List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER)) {
            if (monsters.stream().anyMatch(spawner -> spawner.type == vanilla)) {
                helper.fail(EntityType.getKey(vanilla) + " still spawns naturally");
            }
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** About 15% get Sunburn; nobody is a baby, not even when asked to be. */
    @GameTest(template = PLATFORM)
    public static void spawnsAreAdultsAndAboutFifteenPercentHaveSunburn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        int samples = 400;
        int sunburned = 0;
        for (int i = 0; i < samples; i++) {
            RegularZombie zombie = ModEntities.ZOMBIE.get().create(level);
            zombie.moveTo(Vec3.atBottomCenterOf(pos));
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
            if (zombie.isBaby() || zombie.isPassenger()) {
                helper.fail("a spawn came out as a baby or a jockey");
            }
            if (zombie.burnsInSunlight()) {
                sunburned++;
            }
        }
        // Expected 60 (15%); more than four standard deviations off would be a real error.
        if (sunburned < 30 || sunburned > 90) {
            helper.fail(sunburned + " of " + samples + " have Sunburn, expected about 60");
        }
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(2, 2, 2));
        zombie.setBaby(true);
        if (zombie.isBaby()) {
            helper.fail("setBaby(true) made a baby");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** ENEMY-004, tier 1: about 10% get one hidden weak effect from the pool, never Speed (Q-143). */
    @GameTest(template = PLATFORM)
    public static void aboutTenPercentSpawnWithOneHiddenWeakEffect(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        int samples = 600;
        int withEffect = 0;
        for (int i = 0; i < samples; i++) {
            RegularZombie zombie = ModEntities.ZOMBIE.get().create(level);
            zombie.moveTo(Vec3.atBottomCenterOf(pos));
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
            List<MobEffectInstance> effects = zombie.getActiveEffects().stream()
                    .filter(effect -> effect.getEffect() != ModEffects.SUNBURN.get()).toList();
            if (effects.size() > 1 || effects.stream().anyMatch(effect -> !RegularZombie.TIER_1_SPAWN_EFFECTS.contains(effect.getEffect())
                    || effect.getAmplifier() != 0 || effect.isVisible())) {
                helper.fail("unexpected spawn effects: " + effects);
            }
            if (zombie.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                helper.fail("a tier 1 zombie spawned with Speed");
            }
            withEffect += effects.size();
        }
        // Expected 60 (10%); more than four standard deviations off would be a real error.
        if (withEffect < 31 || withEffect > 89) {
            helper.fail(withEffect + " of " + samples + " have a spawn effect, expected about 60");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** Only zombies with Sunburn burn, and nobody can see who has it. */
    @GameTest(template = PLATFORM)
    public static void onlySunburnedZombiesBurnAndItIsHidden(GameTestHelper helper) {
        RegularZombie plain = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(1, 2, 1));
        RegularZombie burning = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(5, 2, 5));
        plain.removeAllEffects();
        burning.giveSunburn();
        if (plain.burnsInSunlight() || !burning.burnsInSunlight()) {
            helper.fail("burnsInSunlight: plain " + plain.burnsInSunlight() + ", sunburned " + burning.burnsInSunlight());
        }
        MobEffectInstance sunburn = burning.getEffect(ModEffects.SUNBURN.get());
        if (sunburn.isVisible() || sunburn.showIcon() || !sunburn.isInfiniteDuration()) {
            helper.fail("Sunburn should be hidden and permanent: " + sunburn);
        }
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void zombiesDontBreakDoorsOrTurtleEggs(GameTestHelper helper) {
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(2, 2, 2));
        zombie.setCanBreakDoors(true);
        if (zombie.canBreakDoors()) {
            helper.fail("can break doors");
        }
        if (zombie.goalSelector.getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof RemoveBlockGoal)) {
            helper.fail("still has the goal that tramples turtle eggs");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** A 3-block fall onto farmland: a vanilla zombie turns it to dirt, ours doesn't. */
    @GameTest(template = PLATFORM, timeoutTicks = 60)
    public static void zombiesDontTrampleFarmland(GameTestHelper helper) {
        BlockPos ours = new BlockPos(2, 1, 2);
        BlockPos vanilla = new BlockPos(5, 1, 5);
        for (BlockPos pos : List.of(ours, vanilla)) {
            helper.setBlock(pos, Blocks.FARMLAND);
            // A crop keeps dry farmland from turning to dirt on its own.
            helper.setBlock(pos.above(), Blocks.WHEAT);
        }
        helper.spawn(ModEntities.ZOMBIE.get(), new Vec3(2.5, 5, 2.5));
        helper.spawn(EntityType.ZOMBIE, new Vec3(5.5, 5, 5.5));
        helper.runAfterDelay(40, () -> {
            helper.assertBlockPresent(Blocks.DIRT, vanilla);
            helper.assertBlockPresent(Blocks.FARMLAND, ours);
            helper.killAllEntities();
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = DAY, timeoutTicks = 60)
    public static void byDayZombiesAreSlowerThanWalking(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);
        measureSpeed(helper, 0.8);
    }

    @GameTest(template = PLATFORM, batch = NIGHT, timeoutTicks = 60)
    public static void atNightZombiesAreFasterThanWalkingButSlowerThanSprinting(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        measureSpeed(helper, 1.1);
    }

    /**
     * Walks a zombie diagonally across the platform with its AI off and compares its ground speed (after it got
     * going) with the given share of a walking player's speed.
     */
    private static void measureSpeed(GameTestHelper helper, double shareOfWalking) {
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new Vec3(0.5, 2, 0.5));
        zombie.goalSelector.removeAllGoals(goal -> true);
        zombie.targetSelector.removeAllGoals(goal -> true);
        Vec3 target = helper.absoluteVec(new Vec3(7.5, 2, 7.5));
        helper.onEachTick(() -> zombie.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0));
        Vec3[] start = new Vec3[1];
        helper.runAfterDelay(10, () -> start[0] = zombie.position());
        helper.runAfterDelay(30, () -> {
            double blocksPerTick = zombie.position().subtract(start[0]).horizontalDistance() / 20.0;
            double expected = shareOfWalking * RegularZombie.PLAYER_WALK_BLOCKS_PER_TICK;
            if (Math.abs(blocksPerTick - expected) > expected * SPEED_TOLERANCE) {
                helper.fail(String.format("%.2f m/s, expected %.2f m/s", blocksPerTick * 20, expected * 20));
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
