package wallis_ultimate.worldgen;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * WORLD-005, STORY-002, SURV-006: district 1's hand-made places, each built once, when the world is first started,
 * before anyone joins. Remembers in the world's data that this is done, where everything ended up, and which safe spots
 * each team has found.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DistrictSites extends SavedData {
    private static final String DATA_NAME = WallisUltimate.MOD_ID + "_district_sites";
    /** Q-147: the cabin is in the woods, so it goes to the nearest forest. Close enough to the middle to leave room for the safe spots. */
    private static final int FOREST_SEARCH_RADIUS = 350;
    private static final int FOREST_SEARCH_STEP = 16;
    private static final int FOREST_SEARCH_HEIGHT = 70;
    private static final int FOREST_SEARCH_HEIGHT_STEP = 64;
    /** Around that place, it takes the flattest ground. */
    private static final int CABIN_SEARCH_RADIUS = 96;
    private static final int CABIN_SEARCH_STEP = 16;
    private static final int GROUND_MARGIN = 8;
    private static final int TREES = 40;
    private static final int TREE_RING = 22;
    private static final long TREE_SEED = 1002;
    /** Q-138: about this far from spawn, each in its own direction. */
    private static final int SPOT_NEAREST = 350;
    private static final int SPOT_FARTHEST = 600;
    private static final int SPOT_STEP = 25;
    private static final int SPOT_SIDEWAYS = 150;
    private static final int SPOT_SIDEWAYS_STEP = 50;
    /** How far a safe spot's area reaches beyond its build. */
    private static final int SPOT_AREA_MARGIN = 6;
    /** How far a site stays from the district's border. */
    private static final int BORDER_MARGIN = 48;

    private enum Site {
        EARLS_SALVAGE("earls_salvage", 0, -1, SafeSpotBuilds::earlsSalvage),
        GAS_N_GUNS("gas_n_guns", -1, 0, SafeSpotBuilds::gasNGuns),
        CAMP_TARP("camp_tarp", 1, 0, SafeSpotBuilds::campTarp),
        STRANDED_HAULER("stranded_hauler", 0, 1, SafeSpotBuilds::strandedHauler);

        final String id;
        final int directionX;
        final int directionZ;
        final Supplier<Blueprint> blueprint;

        Site(String id, int directionX, int directionZ, Supplier<Blueprint> blueprint) {
            this.id = id;
            this.directionX = directionX;
            this.directionZ = directionZ;
            this.blueprint = blueprint;
        }
    }

    private BlockPos cabin;
    private BlockPos spawn;
    private final List<SafeSpot> spots = new ArrayList<>();
    private final Map<String, Set<String>> discovered = new HashMap<>();
    private final Set<String> garrisoned = new HashSet<>();

    private DistrictSites() {
    }

    /** The plan a safe spot was built from, or null if it isn't one of ours. Building a plan isn't free: keep it. */
    public static Blueprint blueprintOf(String spotId) {
        for (Site site : Site.values()) {
            if (site.id.equals(spotId)) {
                return site.blueprint.get();
            }
        }
        return null;
    }

    public static DistrictSites get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(DistrictSites::load, DistrictSites::new, DATA_NAME);
    }

    private static DistrictSites load(CompoundTag tag) {
        DistrictSites sites = new DistrictSites();
        if (tag.contains("Cabin")) {
            sites.cabin = NbtUtils.readBlockPos(tag.getCompound("Cabin"));
            sites.spawn = NbtUtils.readBlockPos(tag.getCompound("Spawn"));
        }
        tag.getList("Spots", Tag.TAG_COMPOUND).forEach(spot -> sites.spots.add(SafeSpot.load((CompoundTag) spot)));
        CompoundTag teams = tag.getCompound("Discovered");
        for (String team : teams.getAllKeys()) {
            Set<String> found = new HashSet<>();
            teams.getList(team, Tag.TAG_STRING).forEach(id -> found.add(id.getAsString()));
            sites.discovered.put(team, found);
        }
        tag.getList("Garrisoned", Tag.TAG_STRING).forEach(id -> sites.garrisoned.add(id.getAsString()));
        return sites;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (cabin != null) {
            tag.put("Cabin", NbtUtils.writeBlockPos(cabin));
            tag.put("Spawn", NbtUtils.writeBlockPos(spawn));
        }
        ListTag spotTags = new ListTag();
        spots.forEach(spot -> spotTags.add(spot.save()));
        tag.put("Spots", spotTags);
        CompoundTag teams = new CompoundTag();
        discovered.forEach((team, found) -> {
            ListTag ids = new ListTag();
            found.forEach(id -> ids.add(StringTag.valueOf(id)));
            teams.put(team, ids);
        });
        tag.put("Discovered", teams);
        ListTag garrisonedTags = new ListTag();
        garrisoned.forEach(id -> garrisonedTags.add(StringTag.valueOf(id)));
        tag.put("Garrisoned", garrisonedTags);
        return tag;
    }

    /** Where the cabin's corner is, or null until it's built. */
    public BlockPos cabin() {
        return cabin;
    }

    public BlockPos spawn() {
        return spawn;
    }

    public List<SafeSpot> spots() {
        return spots;
    }

    public Optional<SafeSpot> spot(String id) {
        return spots.stream().filter(spot -> spot.id().equals(id)).findFirst();
    }

    public boolean hasDiscovered(String team, String spotId) {
        return discovered.getOrDefault(team, Set.of()).contains(spotId);
    }

    /** True if the team didn't know this one yet. */
    public boolean discover(String team, String spotId) {
        boolean isNew = discovered.computeIfAbsent(team, key -> new HashSet<>()).add(spotId);
        if (isNew) {
            setDirty();
        }
        return isNew;
    }

    /** SURV-011: whether this safe spot has had its first guards. After that, new ones come as reinforcements. */
    public boolean hasBeenGarrisoned(String spotId) {
        return garrisoned.contains(spotId);
    }

    public void markGarrisoned(String spotId) {
        if (garrisoned.add(spotId)) {
            setDirty();
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        DistrictSites sites = get(overworld);
        if (sites.cabin == null) {
            sites.buildCabin(server, overworld);
            sites.setDirty();
        }
        if (sites.spots.isEmpty()) {
            for (Site site : Site.values()) {
                sites.buildSafeSpot(overworld, site);
            }
            sites.setDirty();
        }
    }

    private void buildCabin(MinecraftServer server, ServerLevel level) {
        Blueprint blueprint = CabinBuild.blueprint();
        int sizeX = blueprint.sizeX();
        int sizeZ = blueprint.sizeZ();
        BlockPos around = forestNearTheMiddle(level);
        int centerX = around.getX() - sizeX / 2;
        int centerZ = around.getZ() - sizeZ / 2;
        int bestX = centerX;
        int bestZ = centerZ;
        int bestBadness = Integer.MAX_VALUE;
        for (int x = centerX - CABIN_SEARCH_RADIUS; x <= centerX + CABIN_SEARCH_RADIUS; x += CABIN_SEARCH_STEP) {
            for (int z = centerZ - CABIN_SEARCH_RADIUS; z <= centerZ + CABIN_SEARCH_RADIUS; z += CABIN_SEARCH_STEP) {
                int badness = SiteGround.badness(level, x, z, sizeX, sizeZ, DistrictSites::isWoods);
                if (badness < bestBadness) {
                    bestBadness = badness;
                    bestX = x;
                    bestZ = z;
                }
            }
        }
        int groundY = SiteGround.buildHeight(level, bestX, bestZ, sizeX, sizeZ);
        SiteGround.flatten(level, bestX, bestZ, sizeX, sizeZ, groundY, GROUND_MARGIN);
        cabin = new BlockPos(bestX, groundY + 1, bestZ);
        blueprint.place(level, cabin);
        spawn = cabin.offset(blueprint.mark(CabinBuild.SPAWN));
        plantTrees(level, bestX + sizeX / 2, bestZ + sizeZ / 2, Math.max(sizeX, sizeZ) / 2 + GROUND_MARGIN);
        // Players always appear on the spot, not somewhere within ten blocks of it, so nobody lands on the roof.
        level.setDefaultSpawnPos(spawn, 0.0F);
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
        WallisUltimate.LOGGER.info("Built the cabin at {}, spawn at {}", cabin, spawn);
    }

    /** The closest forest to the map's middle; if there is none around, where vanilla would have put the spawn. */
    private static BlockPos forestNearTheMiddle(ServerLevel level) {
        Pair<BlockPos, Holder<Biome>> forest = level.findClosestBiome3d(DistrictSites::isWoods,
                new BlockPos(0, FOREST_SEARCH_HEIGHT, 0), FOREST_SEARCH_RADIUS, FOREST_SEARCH_STEP, FOREST_SEARCH_HEIGHT_STEP);
        return forest != null ? forest.getFirst() : level.getSharedSpawnPos();
    }

    private static boolean isWoods(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA);
    }

    private static void plantTrees(ServerLevel level, int centerX, int centerZ, int innerRadius) {
        RandomSource random = RandomSource.create(TREE_SEED);
        Holder<ConfiguredFeature<?, ?>> spruce = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolderOrThrow(TreeFeatures.SPRUCE);
        Holder<ConfiguredFeature<?, ?>> pine = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolderOrThrow(TreeFeatures.PINE);
        int outerRadius = innerRadius + TREE_RING;
        SiteGround.loadChunks(level, centerX - outerRadius - 8, centerZ - outerRadius - 8, centerX + outerRadius + 8, centerZ + outerRadius + 8);
        for (int i = 0; i < TREES; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = innerRadius + random.nextDouble() * TREE_RING;
            int x = centerX + (int) (Math.cos(angle) * distance);
            int z = centerZ + (int) (Math.sin(angle) * distance);
            BlockPos ground = new BlockPos(x, SiteGround.naturalGround(level, x, z), z);
            if (level.getBlockState(ground).is(BlockTags.DIRT)) {
                (random.nextBoolean() ? spruce : pine).value().place(level, level.getChunkSource().getGenerator(), random, ground.above());
            }
        }
    }

    private void buildSafeSpot(ServerLevel level, Site site) {
        Blueprint blueprint = site.blueprint.get();
        int sizeX = blueprint.sizeX();
        int sizeZ = blueprint.sizeZ();
        int limit = (int) (DistrictBorder.DISTRICT_SIZE / 2) - BORDER_MARGIN - Math.max(sizeX, sizeZ);
        int bestX = spawn.getX() + site.directionX * SPOT_NEAREST - sizeX / 2;
        int bestZ = spawn.getZ() + site.directionZ * SPOT_NEAREST - sizeZ / 2;
        int bestBadness = Integer.MAX_VALUE;
        for (int distance = SPOT_NEAREST; distance <= SPOT_FARTHEST; distance += SPOT_STEP) {
            for (int sideways = -SPOT_SIDEWAYS; sideways <= SPOT_SIDEWAYS; sideways += SPOT_SIDEWAYS_STEP) {
                int x = spawn.getX() + site.directionX * distance + site.directionZ * sideways - sizeX / 2;
                int z = spawn.getZ() + site.directionZ * distance + site.directionX * sideways - sizeZ / 2;
                if (Math.abs(x) > limit || Math.abs(z) > limit) {
                    continue;
                }
                int badness = SiteGround.badness(level, x, z, sizeX, sizeZ, biome -> true);
                if (badness < bestBadness) {
                    bestBadness = badness;
                    bestX = x;
                    bestZ = z;
                }
            }
        }
        int groundY = SiteGround.buildHeight(level, bestX, bestZ, sizeX, sizeZ);
        SiteGround.flatten(level, bestX, bestZ, sizeX, sizeZ, groundY, GROUND_MARGIN);
        BlockPos origin = new BlockPos(bestX, groundY + 1, bestZ);
        blueprint.place(level, origin);
        List<BlockPos> beds = blueprint.marks().entrySet().stream()
                .filter(mark -> mark.getKey().startsWith(SafeSpotBuilds.BED))
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(mark -> origin.offset(mark.getValue()))
                .toList();
        int radius = Math.max(sizeX, sizeZ) / 2 + SPOT_AREA_MARGIN;
        SafeSpot spot = new SafeSpot(site.id, origin.offset(sizeX / 2, 0, sizeZ / 2), radius, origin.offset(blueprint.mark(SafeSpotBuilds.TRAVEL)), beds, origin);
        spots.add(spot);
        WallisUltimate.LOGGER.info("Built safe spot {} at {} (ground unevenness {})", site.id, spot.center(), bestBadness);
    }
}
