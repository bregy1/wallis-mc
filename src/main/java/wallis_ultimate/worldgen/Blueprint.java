package wallis_ultimate.worldgen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import wallis_ultimate.WallisUltimate;

/**
 * WORLD-005: a hand-made structure, written down block by block. Coordinates are relative to one corner: x east, z
 * south, y = 0 is the first layer above the ground. Later calls overwrite earlier ones. A given seed always gives the
 * same structure, so its little variations (worn roof, missing boards) stay the same in every world.
 */
public final class Blueprint {
    /** Don't tell the neighbours while building: half a structure would make torches and doors pop off. */
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** Worldgen builds ruins on many threads at once. */
    private static final Map<String, BlockState> PARSED = new ConcurrentHashMap<>();

    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<String, BlockPos> marks = new HashMap<>();
    private final Map<BlockPos, ResourceLocation> lootTables = new HashMap<>();
    private final RandomSource random;
    private int maxX;
    private int maxZ;

    public Blueprint(long seed) {
        random = RandomSource.create(seed);
    }

    /** Like a command argument: "minecraft:spruce_stairs[facing=north]". A typo fails the GameTest that builds every blueprint. */
    public static BlockState state(String block) {
        return PARSED.computeIfAbsent(block, key -> {
            try {
                return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), key, false).blockState();
            } catch (CommandSyntaxException e) {
                throw new IllegalArgumentException("Bad block in a blueprint: " + key, e);
            }
        });
    }

    public Blueprint set(int x, int y, int z, String block) {
        blocks.put(new BlockPos(x, y, z), state(block));
        maxX = Math.max(maxX, x);
        maxZ = Math.max(maxZ, z);
        return this;
    }

    public Blueprint box(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, String block) {
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                for (int z = fromZ; z <= toZ; z++) {
                    set(x, y, z, block);
                }
            }
        }
        return this;
    }

    /** Fills the box, but each block is the other kind with the given chance. For wear and patches. */
    public Blueprint mottled(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, String block, String other, float chance) {
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                for (int z = fromZ; z <= toZ; z++) {
                    set(x, y, z, random.nextFloat() < chance ? other : block);
                }
            }
        }
        return this;
    }

    /** Changes blocks at one height with the given chance, whatever was set there. For a second kind of wear on a surface. */
    public Blueprint speckle(int fromX, int y, int fromZ, int toX, int toZ, String block, float chance) {
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                if (random.nextFloat() < chance) {
                    set(x, y, z, block);
                }
            }
        }
        return this;
    }

    /** True with the given chance, for choices that make one copy of a build differ from the next. */
    public boolean roll(float chance) {
        return random.nextFloat() < chance;
    }

    /** Puts the block here and there inside the area at one height, only where nothing has been set yet. So: last. */
    public Blueprint scatter(int fromX, int y, int fromZ, int toX, int toZ, String block, float chance) {
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                if (!blocks.containsKey(new BlockPos(x, y, z)) && random.nextFloat() < chance) {
                    set(x, y, z, block);
                }
            }
        }
        return this;
    }

    /** WORLD-011: a Lootr chest, which every player gets to loot once, with contents rolled for them. */
    public Blueprint lootChest(int x, int y, int z, String facing, String table) {
        set(x, y, z, "lootr:lootr_chest[facing=" + facing + "]");
        lootTables.put(new BlockPos(x, y, z), ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "chests/" + table));
        return this;
    }

    /** A named spot the code needs later: where players spawn, a bed, a travel point. Places nothing. */
    public Blueprint mark(String name, int x, int y, int z) {
        marks.put(name, new BlockPos(x, y, z));
        return this;
    }

    public BlockPos mark(String name) {
        return marks.get(name);
    }

    public Map<String, BlockPos> marks() {
        return marks;
    }

    public int sizeX() {
        return maxX + 1;
    }

    public int sizeZ() {
        return maxZ + 1;
    }

    public Map<BlockPos, BlockState> blocks() {
        return blocks;
    }

    /**
     * Places everything, then lets each block look at its neighbours once, so fences, sofas and panes connect. Blocks
     * that can't stay where they are (a torch without a wall) go away there, like vanilla's structures.
     */
    public void place(ServerLevel level, BlockPos origin) {
        blocks.forEach((pos, state) -> level.setBlock(origin.offset(pos), state, QUIET));
        blocks.forEach((pos, state) -> {
            BlockPos at = origin.offset(pos);
            BlockState connected = Block.updateFromNeighbourShapes(level.getBlockState(at), level, at);
            if (connected.isAir() && !state.isAir()) {
                WallisUltimate.LOGGER.error("Blueprint: {} can't stay at {}", state, pos);
            }
            if (connected != level.getBlockState(at)) {
                level.setBlock(at, connected, QUIET);
            }
        });
        lootTables.forEach((pos, table) -> {
            if (level.getBlockEntity(origin.offset(pos)) instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(table, random.nextLong());
            }
        });
    }

    /**
     * For worldgen, where the world is still being generated around it and blocks can't look far: places it around the
     * given middle, turned. No loot chests: ruins are decoration.
     */
    public void placeTurned(WorldGenLevel level, BlockPos middle, Rotation rotation) {
        BlockPos center = new BlockPos(sizeX() / 2, 0, sizeZ() / 2);
        blocks.forEach((pos, state) -> level.setBlock(middle.offset(pos.subtract(center).rotate(rotation)), state.rotate(rotation), QUIET));
        blocks.forEach((pos, state) -> {
            BlockPos at = middle.offset(pos.subtract(center).rotate(rotation));
            BlockState connected = Block.updateFromNeighbourShapes(level.getBlockState(at), level, at);
            if (connected != level.getBlockState(at) && !connected.isAir()) {
                level.setBlock(at, connected, QUIET);
            }
        });
    }

    public Map<BlockPos, ResourceLocation> lootTables() {
        return lootTables;
    }
}
