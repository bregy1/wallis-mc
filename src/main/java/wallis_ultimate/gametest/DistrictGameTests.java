package wallis_ultimate.gametest;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.worldgen.DistrictBorder;

/** Checks for District 1: WORLD-001, WORLD-005, WORLD-009, WORLD-010. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DistrictGameTests {
    private static final String PLATFORM = "platform";

    private static final List<String> REMOVED_ORES = List.of(
            "ore_gold", "ore_gold_lower", "ore_gold_extra", "ore_redstone", "ore_redstone_lower",
            "ore_diamond", "ore_diamond_large", "ore_diamond_buried", "ore_lapis", "ore_lapis_buried",
            "ore_emerald", "ore_copper", "ore_copper_large", "ore_iron_middle");
    private static final List<String> REMOVED_STRUCTURE_FEATURES = List.of(
            "monster_room", "monster_room_deep", "fossil_upper", "fossil_lower", "desert_well", "amethyst_geode");
    private static final long[] LAND_TEST_SEEDS = {1L, 42L, -7L, 20261103L, 123456789L, -987654321L};
    private static final int LAND_TEST_STEP = 200;
    /** Rivers and ponds stay, and are welcome: they take up about a fifth of a district, as in vanilla's inland. */
    private static final int LAND_TEST_MAX_FLOODED_PERCENT = 30;

    private DistrictGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void worldRunsFromZeroTo320(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        if (level.getMinBuildHeight() != 0 || level.getMaxBuildHeight() != 320) {
            helper.fail("build height is " + level.getMinBuildHeight() + ".." + level.getMaxBuildHeight());
        }
        helper.succeed();
    }

    /** WORLD-001: a 2,000-block district can't be mostly sea, whichever seed it gets. */
    @GameTest(template = PLATFORM)
    public static void district1IsLandOnEverySeed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        NoiseGeneratorSettings settings = ((NoiseBasedChunkGenerator) generator).generatorSettings().value();
        int seaLevel = generator.getSeaLevel();
        int half = (int) (DistrictBorder.DISTRICT_SIZE / 2);
        int worstPercent = 0;
        long worstSeed = 0;
        for (long seed : LAND_TEST_SEEDS) {
            RandomState random = RandomState.create(settings, level.registryAccess().lookupOrThrow(Registries.NOISE), seed);
            int sampled = 0;
            int flooded = 0;
            for (int x = -half; x <= half; x += LAND_TEST_STEP) {
                for (int z = -half; z <= half; z += LAND_TEST_STEP) {
                    Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(seaLevel), QuartPos.fromBlock(z), random.sampler());
                    if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) {
                        helper.fail("seed " + seed + " has an ocean at " + x + ", " + z);
                    }
                    sampled++;
                    if (generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random) < seaLevel) {
                        flooded++;
                    }
                }
            }
            WallisUltimate.LOGGER.info("Seed {}: {} of {} sampled places are under water", seed, flooded, sampled);
            if (flooded * 100 / sampled > worstPercent) {
                worstPercent = flooded * 100 / sampled;
                worstSeed = seed;
            }
        }
        if (worstPercent > LAND_TEST_MAX_FLOODED_PERCENT) {
            helper.fail("seed " + worstSeed + " is " + worstPercent + "% under water");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void district1HasOnlyCoalCopperIron(GameTestHelper helper) {
        HolderSet<PlacedFeature> ores = features(helper, Biomes.PLAINS).get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal());
        for (String id : REMOVED_ORES) {
            if (contains(ores, vanilla(id))) {
                helper.fail("plains still has " + id);
            }
        }
        for (ResourceKey<PlacedFeature> required : List.of(
                ours("district1_ore_iron_middle"), ours("district1_ore_copper"),
                vanilla("ore_coal_upper"), vanilla("ore_coal_lower"), vanilla("ore_iron_upper"), vanilla("ore_iron_small"))) {
            if (!contains(ores, required)) {
                helper.fail("plains is missing " + required.location());
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void noDungeonsFossilsWellsOrGeodes(GameTestHelper helper) {
        for (ResourceKey<Biome> biome : List.of(Biomes.PLAINS, Biomes.DESERT)) {
            for (HolderSet<PlacedFeature> step : features(helper, biome)) {
                for (String id : REMOVED_STRUCTURE_FEATURES) {
                    if (contains(step, vanilla(id))) {
                        helper.fail(biome.location() + " still has " + id);
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void villagesHaveNoBiomes(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        boolean anyVillageBiome = biomes.getTag(BiomeTags.HAS_VILLAGE_PLAINS).map(tag -> tag.size() > 0).orElse(false);
        if (anyVillageBiome) {
            helper.fail("has_structure/village_plains is not empty");
        }
        helper.succeed();
    }

    private static List<HolderSet<PlacedFeature>> features(GameTestHelper helper, ResourceKey<Biome> biome) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(biome).value().getGenerationSettings().features();
    }

    private static boolean contains(HolderSet<PlacedFeature> set, ResourceKey<PlacedFeature> key) {
        for (Holder<PlacedFeature> feature : set) {
            if (feature.is(key)) {
                return true;
            }
        }
        return false;
    }

    private static ResourceKey<PlacedFeature> vanilla(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.withDefaultNamespace(path));
    }

    private static ResourceKey<PlacedFeature> ours(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, path));
    }
}
