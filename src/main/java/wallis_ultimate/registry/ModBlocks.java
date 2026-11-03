package wallis_ultimate.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.BarbedWireBlock;
import wallis_ultimate.block.FacingPropBlock;
import wallis_ultimate.block.GraveBlock;
import wallis_ultimate.block.OilDrumBlock;
import wallis_ultimate.block.PanelBlock;
import wallis_ultimate.block.TravelSignpostBlock;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WallisUltimate.MOD_ID);

    /** WORLD-006: unminable blocks for protected structures. */
    public static final RegistryObject<Block> REINFORCED_CONCRETE = BLOCKS.register("reinforced_concrete",
            () -> new Block(unminable(MapColor.COLOR_LIGHT_GRAY, SoundType.STONE)));
    public static final RegistryObject<Block> RIVETED_STEEL = BLOCKS.register("riveted_steel",
            () -> new Block(unminable(MapColor.METAL, SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> ALIEN_ALLOY = BLOCKS.register("alien_alloy",
            () -> new Block(unminable(MapColor.COLOR_PURPLE, SoundType.AMETHYST)));

    /** SURV-004: unbreakable too, so nobody but the owner gets at the items. No block item: only deaths make graves. */
    public static final RegistryObject<Block> GRAVE = BLOCKS.register("grave",
            () -> new GraveBlock(unminable(MapColor.STONE, SoundType.STONE)));

    /** WORLD-004: no block item either; every safe spot comes with one. */
    public static final RegistryObject<Block> TRAVEL_SIGNPOST = BLOCKS.register("travel_signpost",
            () -> new TravelSignpostBlock(unminable(MapColor.WOOD, SoundType.WOOD).noOcclusion()));

    // WORLD-013: apocalypse props for structures. All can be mined; the locks keep players from placing them (PROG-002).
    public static final RegistryObject<Block> SANDBAGS = BLOCKS.register("sandbags",
            () -> new Block(prop(MapColor.SAND, SoundType.SAND, 1.5F)));
    public static final RegistryObject<SlabBlock> SANDBAG_SLAB = BLOCKS.register("sandbag_slab",
            () -> new SlabBlock(prop(MapColor.SAND, SoundType.SAND, 1.5F)));
    public static final RegistryObject<Block> BARBED_WIRE = BLOCKS.register("barbed_wire",
            () -> new BarbedWireBlock(prop(MapColor.METAL, SoundType.CHAIN, 1.0F).noCollission().noOcclusion()));
    public static final RegistryObject<Block> OIL_DRUM = BLOCKS.register("oil_drum",
            () -> new OilDrumBlock(prop(MapColor.COLOR_BLACK, SoundType.METAL, 2.0F).noOcclusion()
                    .lightLevel(state -> state.getValue(OilDrumBlock.LIT) ? OilDrumBlock.LIGHT : 0)));
    public static final RegistryObject<Block> GREEN_TARP = BLOCKS.register("green_tarp",
            () -> new PanelBlock(prop(MapColor.COLOR_GREEN, SoundType.WOOL, 0.3F).noOcclusion()));
    public static final RegistryObject<Block> BLUE_TARP = BLOCKS.register("blue_tarp",
            () -> new PanelBlock(prop(MapColor.COLOR_BLUE, SoundType.WOOL, 0.3F).noOcclusion()));
    public static final RegistryObject<Block> GREEN_TARP_SLOPE = BLOCKS.register("green_tarp_slope",
            () -> new FacingPropBlock(prop(MapColor.COLOR_GREEN, SoundType.WOOL, 0.3F).noCollission().noOcclusion(), 0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    public static final RegistryObject<Block> BLUE_TARP_SLOPE = BLOCKS.register("blue_tarp_slope",
            () -> new FacingPropBlock(prop(MapColor.COLOR_BLUE, SoundType.WOOL, 0.3F).noCollission().noOcclusion(), 0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    public static final RegistryObject<Block> SUPPLY_CRATE = BLOCKS.register("supply_crate",
            () -> new Block(prop(MapColor.WOOD, SoundType.WOOD, 2.0F)));
    /** Wrecked cars are built from these four (body, panels for hood, roof and bed, windows, wheels). */
    public static final RegistryObject<Block> WRECK_BODY = BLOCKS.register("wreck_body",
            () -> new Block(prop(MapColor.COLOR_RED, SoundType.METAL, 3.0F)));
    public static final RegistryObject<SlabBlock> WRECK_PANEL = BLOCKS.register("wreck_panel",
            () -> new SlabBlock(prop(MapColor.COLOR_RED, SoundType.METAL, 3.0F)));
    public static final RegistryObject<Block> WRECK_WINDOW = BLOCKS.register("wreck_window",
            () -> new PanelBlock(prop(MapColor.NONE, SoundType.GLASS, 0.5F).noOcclusion()));
    public static final RegistryObject<Block> WRECK_WHEEL = BLOCKS.register("wreck_wheel",
            () -> new FacingPropBlock(prop(MapColor.COLOR_BLACK, SoundType.WOOL, 1.0F).noOcclusion(), 0.0, 0.0, 0.0, 16.0, 16.0, 6.0));
    /** For the rednecks' cabin (STORY-002). */
    public static final RegistryObject<Block> BEER_CANS = BLOCKS.register("beer_cans",
            () -> new FacingPropBlock(prop(MapColor.METAL, SoundType.CHAIN, 0.1F).noCollission().noOcclusion(), 2.0, 0.0, 2.0, 14.0, 5.0, 14.0));
    public static final RegistryObject<Block> ANTLERS = BLOCKS.register("antlers",
            () -> new FacingPropBlock(prop(MapColor.WOOD, SoundType.BONE_BLOCK, 0.5F).noCollission().noOcclusion(), 0.0, 2.0, 0.0, 16.0, 14.0, 3.0));

    // ENEMY-011: Hog Daddy's farm, placeholder art. No block items: only the farm has these.
    /** The ring around the farm: hay bales and crushed cars that can't be broken (WORLD-006). */
    public static final RegistryObject<Block> STRAPPED_HAY = BLOCKS.register("strapped_hay",
            () -> new Block(unminable(MapColor.COLOR_YELLOW, SoundType.GRASS)));
    public static final RegistryObject<Block> CRUSHED_WRECK = BLOCKS.register("crushed_wreck",
            () -> new Block(unminable(MapColor.COLOR_RED, SoundType.NETHERITE_BLOCK)));
    /** Slamming into one of these two stuns him. They only go away when he breaks them, and come back with the farm. */
    public static final RegistryObject<Block> PEN_POST = BLOCKS.register("pen_post",
            () -> new FacingPropBlock(unminable(MapColor.WOOD, SoundType.WOOD).noOcclusion(), 4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
    public static final RegistryObject<Block> FEEDING_TROUGH = BLOCKS.register("feeding_trough",
            () -> new FacingPropBlock(unminable(MapColor.WOOD, SoundType.WOOD).noOcclusion(), 0.0, 0.0, 2.0, 16.0, 8.0, 14.0));
    /** Two blocks tall, and he tramples it. Nobody can walk off with it: nothing can be mined on the farm anyway. */
    public static final RegistryObject<Block> CORN = BLOCKS.register("corn",
            () -> new DoublePlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.CROP)
                    .offsetType(BlockBehaviour.OffsetType.XZ).noLootTable().pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {
    }

    /** An ordinary block players can break by hand or with a tool, dropping itself. */
    private static BlockBehaviour.Properties prop(MapColor color, SoundType sound, float strength) {
        return BlockBehaviour.Properties.of().mapColor(color).sound(sound).strength(strength);
    }

    /** Like bedrock: can't be mined, survives any explosion, drops nothing, can't be pushed by pistons. */
    private static BlockBehaviour.Properties unminable(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .sound(sound)
                .strength(-1.0F, 3_600_000.0F)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK);
    }
}
