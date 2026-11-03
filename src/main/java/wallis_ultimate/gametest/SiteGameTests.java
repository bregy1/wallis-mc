package wallis_ultimate.gametest;

import java.util.List;
import java.util.function.LongFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.init.ModBlocks;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.CabinBuild;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.RuinBuilds;
import wallis_ultimate.worldgen.SafeSpotBuilds;

/** Checks for WORLD-005 and STORY-002: the hand-made places get built once, and players start at the cabin. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SiteGameTests {
    private static final String PLATFORM = "platform";
    private static final int RUIN_SEEDS = 8;
    /** A feature may write into the chunks right next to its own, and its middle can be anywhere in its chunk. */
    private static final int MOST_RUIN_REACH = 15;

    private SiteGameTests() {
    }

    /** Parses every block in the blueprints: a typo in one throws here instead of when a world is created. */
    @GameTest(template = PLATFORM)
    public static void blueprintsHaveNoTypos(GameTestHelper helper) {
        if (CabinBuild.blueprint().mark(CabinBuild.SPAWN) == null) {
            helper.fail("the cabin needs a spawn mark");
        }
        for (Blueprint blueprint : List.of(SafeSpotBuilds.earlsSalvage(), SafeSpotBuilds.gasNGuns(), SafeSpotBuilds.campTarp(), SafeSpotBuilds.strandedHauler())) {
            if (blueprint.mark("travel") == null || blueprint.mark("bed1") == null) {
                helper.fail("every safe spot needs a signpost and a bed");
            }
        }
        helper.succeed();
    }

    /** WORLD-014: every look parses, whatever its seed, and fits where a feature may write: one chunk around its middle. */
    @GameTest(template = PLATFORM)
    public static void ruinsFitInsideTheirChunks(GameTestHelper helper) {
        for (LongFunction<Blueprint> look : RuinBuilds.allLooks()) {
            for (long seed = 0; seed < RUIN_SEEDS; seed++) {
                Blueprint ruin = look.apply(seed);
                int halfX = ruin.sizeX() / 2;
                int halfZ = ruin.sizeZ() / 2;
                ruin.blocks().keySet().forEach(pos -> {
                    if (Math.abs(pos.getX() - halfX) > MOST_RUIN_REACH || Math.abs(pos.getZ() - halfZ) > MOST_RUIN_REACH) {
                        helper.fail("a ruin reaches too far: " + pos + " in a look " + ruin.sizeX() + " by " + ruin.sizeZ());
                    }
                });
            }
        }
        helper.succeed();
    }

    /** WORLD-014: it's in the worldgen data, and on land only. */
    @GameTest(template = PLATFORM)
    public static void ruinsGenerateOnLandOnly(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        PlacedFeature ruin = registries.registryOrThrow(Registries.PLACED_FEATURE).get(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "ruin"));
        if (ruin == null) {
            helper.fail("the ruin's placed feature is missing");
        }
        Registry<Biome> biomes = registries.registryOrThrow(Registries.BIOME);
        if (!biomes.getOrThrow(Biomes.PLAINS).getGenerationSettings().hasFeature(ruin)
                || !biomes.getOrThrow(Biomes.DESERT).getGenerationSettings().hasFeature(ruin)) {
            helper.fail("ruins should generate in plains and deserts");
        }
        if (biomes.getOrThrow(Biomes.OCEAN).getGenerationSettings().hasFeature(ruin) || biomes.getOrThrow(Biomes.RIVER).getGenerationSettings().hasFeature(ruin)) {
            helper.fail("ruins shouldn't generate in oceans and rivers");
        }
        helper.succeed();
    }

    /** SURV-011: guard posts and NPC spots have room to stand, and something to stand on. */
    @GameTest(template = PLATFORM)
    public static void guardPostsHaveRoomToStand(GameTestHelper helper) {
        for (Blueprint blueprint : List.of(SafeSpotBuilds.earlsSalvage(), SafeSpotBuilds.gasNGuns(), SafeSpotBuilds.campTarp(), SafeSpotBuilds.strandedHauler())) {
            if (blueprint.mark("guard1") == null || blueprint.mark("npc1") == null) {
                helper.fail("every safe spot needs guard posts and a place for a named NPC");
            }
            blueprint.marks().forEach((name, pos) -> {
                if (!name.startsWith("guard") && !name.startsWith("npc")) {
                    return;
                }
                BlockState feet = blueprint.blocks().get(pos);
                BlockState floor = blueprint.blocks().get(pos.below());
                if (feet != null && !feet.isAir()) {
                    helper.fail(name + " at " + pos + " is inside " + feet);
                }
                if (pos.getY() > 0 && (floor == null || floor.isAir())) {
                    helper.fail(name + " at " + pos + " has nothing to stand on");
                }
            });
        }
        helper.succeed();
    }

    /** WORLD-011: every chest in every blueprint is a personal one with a loot table that exists and isn't empty. */
    @GameTest(template = PLATFORM)
    public static void chestsHaveRealLootTables(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Blueprint blueprint : List.of(CabinBuild.blueprint(), SafeSpotBuilds.earlsSalvage(), SafeSpotBuilds.gasNGuns(), SafeSpotBuilds.campTarp(), SafeSpotBuilds.strandedHauler())) {
            if (blueprint.lootTables().isEmpty()) {
                helper.fail("every place should have a loot chest");
            }
            blueprint.lootTables().forEach((pos, table) -> {
                if (!blueprint.blocks().get(pos).is(ModBlocks.CHEST.get())) {
                    helper.fail("the chest at " + pos + " isn't a Lootr chest");
                }
                LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
                if (level.getServer().getLootData().getLootTable(table).getRandomItems(params).isEmpty()) {
                    helper.fail("the loot table " + table + " gives nothing");
                }
            });
        }
        helper.succeed();
    }

    /** A chest placed by a blueprint knows its table, ready for Lootr to roll per player. */
    @GameTest(template = PLATFORM)
    public static void placedChestsKnowTheirTable(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint(1).lootChest(1, 0, 1, "south", "supplies");
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        blueprint.place(helper.getLevel(), origin);
        if (!(helper.getLevel().getBlockEntity(origin.offset(1, 0, 1)) instanceof ILootBlockEntity chest)
                || !chest.getTable().equals(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "chests/supplies"))) {
            helper.fail("the chest doesn't know its loot table");
        }
        helper.succeed();
    }

    /** The server's own world has been through its first start by the time this runs. */
    @GameTest(template = PLATFORM)
    public static void playersStartAtTheCabin(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        DistrictSites sites = DistrictSites.get(overworld);
        if (sites.cabin() == null) {
            helper.fail("the cabin wasn't built");
        }
        if (!overworld.getSharedSpawnPos().equals(sites.spawn())) {
            helper.fail("spawn is at " + overworld.getSharedSpawnPos() + ", the cabin's is " + sites.spawn());
        }
        if (overworld.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) != 0) {
            helper.fail("players should appear right at the spawn");
        }
        // Whether the cabin really stands is for the smoke test: these tests get placed right on top of it.
        helper.succeed();
    }
}
