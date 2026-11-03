package wallis_ultimate.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.BillboardBlock;
import wallis_ultimate.block.DecalBlock;
import wallis_ultimate.block.FacingPropBlock;
import wallis_ultimate.block.LampPropBlock;
import wallis_ultimate.block.PanelBlock;
import wallis_ultimate.block.PillarPropBlock;
import wallis_ultimate.block.SparkingPropBlock;

/**
 * WORLD-013: the apocalypse kit, the blocks that make Earth look like the ruin of an advanced world (see World view in
 * the overview). Like the other props: for structures first, they can be mined and drop themselves, and the Settler stage
 * locks them for players because it names our whole mod. Each block carries its display name and fastest tool here, so
 * adding one takes one entry; its model goes in ModKitModelProvider.
 */
public final class ModKit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WallisUltimate.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WallisUltimate.MOD_ID);
    private static final Map<RegistryObject<? extends Block>, String> NAMES = new LinkedHashMap<>();
    private static final List<RegistryObject<? extends Block>> PICKAXE = new ArrayList<>();
    private static final List<RegistryObject<? extends Block>> AXE = new ArrayList<>();
    private static final List<RegistryObject<? extends Block>> SHOVEL = new ArrayList<>();
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

    private enum Tool { PICKAXE, AXE, SHOVEL }

    // Roads and concrete.
    public static final RegistryObject<Block> CRACKED_CONCRETE = block("cracked_concrete", "Cracked Concrete", Tool.PICKAXE,
            () -> new Block(props(MapColor.STONE, SoundType.STONE, 1.5F)));
    public static final RegistryObject<SlabBlock> CRACKED_CONCRETE_SLAB = block("cracked_concrete_slab", "Cracked Concrete Slab", Tool.PICKAXE,
            () -> new SlabBlock(props(MapColor.STONE, SoundType.STONE, 1.5F)));
    public static final RegistryObject<StairBlock> CRACKED_CONCRETE_STAIRS = block("cracked_concrete_stairs", "Cracked Concrete Stairs", Tool.PICKAXE,
            () -> new StairBlock(() -> CRACKED_CONCRETE.get().defaultBlockState(), props(MapColor.STONE, SoundType.STONE, 1.5F)));
    public static final RegistryObject<WallBlock> CRACKED_CONCRETE_WALL = block("cracked_concrete_wall", "Cracked Concrete Wall", Tool.PICKAXE,
            () -> new WallBlock(props(MapColor.STONE, SoundType.STONE, 1.5F)));
    public static final RegistryObject<Block> ASPHALT = block("asphalt", "Asphalt", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BLACK, SoundType.STONE, 1.5F)));
    public static final RegistryObject<SlabBlock> ASPHALT_SLAB = block("asphalt_slab", "Asphalt Slab", Tool.PICKAXE,
            () -> new SlabBlock(props(MapColor.COLOR_BLACK, SoundType.STONE, 1.5F)));
    public static final RegistryObject<Block> ASPHALT_LINE = block("asphalt_line", "Asphalt with Road Line", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_BLACK, SoundType.STONE, 1.5F), 0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    public static final RegistryObject<Block> ASPHALT_DASHED = block("asphalt_dashed", "Asphalt with Dashed Line", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_BLACK, SoundType.STONE, 1.5F), 0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    public static final RegistryObject<Block> RUBBLE_PILE = block("rubble_pile", "Rubble Pile", Tool.SHOVEL,
            () -> new FacingPropBlock(props(MapColor.STONE, SoundType.GRAVEL, 0.8F).noOcclusion(), 0.0, 0.0, 0.0, 16.0, 8.0, 16.0));
    public static final RegistryObject<Block> EXPOSED_REBAR = block("exposed_rebar", "Exposed Rebar", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BROWN, SoundType.CHAIN, 1.0F).noCollission().noOcclusion()));

    // Walls and barricades.
    public static final RegistryObject<Block> CORRUGATED_METAL = block("corrugated_metal", "Corrugated Metal", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BROWN, SoundType.METAL, 2.0F)));
    public static final RegistryObject<Block> CORRUGATED_SHEET = block("corrugated_sheet", "Corrugated Sheet", Tool.PICKAXE,
            () -> new PanelBlock(props(MapColor.COLOR_BROWN, SoundType.METAL, 1.0F).noOcclusion()));
    public static final RegistryObject<Block> SHIPPING_CONTAINER_BLUE = block("shipping_container_blue", "Blue Shipping Container", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BLUE, SoundType.METAL, 2.5F)));
    public static final RegistryObject<Block> SHIPPING_CONTAINER_ORANGE = block("shipping_container_orange", "Orange Shipping Container", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_ORANGE, SoundType.METAL, 2.5F)));
    public static final RegistryObject<Block> SCRAP_BLOCK = block("scrap_block", "Scrap Block", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BROWN, SoundType.METAL, 2.0F)));
    public static final RegistryObject<Block> SCRAP_PILE = block("scrap_pile", "Scrap Pile", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_BROWN, SoundType.METAL, 1.0F).noOcclusion(), 1.0, 0.0, 1.0, 15.0, 11.0, 15.0));
    public static final RegistryObject<IronBarsBlock> CHAIN_LINK_FENCE = block("chain_link_fence", "Chain-Link Fence", Tool.PICKAXE,
            () -> new IronBarsBlock(props(MapColor.METAL, SoundType.CHAIN, 1.5F).noOcclusion()));
    public static final RegistryObject<Block> JERSEY_BARRIER = block("jersey_barrier", "Jersey Barrier", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.STONE, SoundType.STONE, 3.0F).noOcclusion(), 0.0, 0.0, 2.0, 16.0, 14.0, 14.0));
    public static final RegistryObject<Block> GABION_BARRIER = block("gabion_barrier", "Gabion Barrier", Tool.SHOVEL,
            () -> new Block(props(MapColor.SAND, SoundType.SAND, 2.0F)));
    public static final RegistryObject<Block> HAZARD_PLATE = block("hazard_plate", "Hazard Plate", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_YELLOW, SoundType.METAL, 2.5F)));
    public static final RegistryObject<Block> STEEL_GIRDER = block("steel_girder", "Steel Girder", Tool.PICKAXE,
            () -> new PillarPropBlock(props(MapColor.METAL, SoundType.METAL, 2.5F).noOcclusion(), 6.0));
    public static final RegistryObject<Block> SIGN_KEEP_OUT = block("sign_keep_out", "Keep Out Sign", Tool.AXE,
            () -> new PanelBlock(props(MapColor.WOOD, SoundType.WOOD, 0.5F).noOcclusion()));
    public static final RegistryObject<Block> SIGN_SAFE_ZONE = block("sign_safe_zone", "Safe Zone Sign", Tool.AXE,
            () -> new PanelBlock(props(MapColor.WOOD, SoundType.WOOD, 0.5F).noOcclusion()));
    public static final RegistryObject<Block> SIGN_SHOT_ON_SITE = block("sign_shot_on_site", "Shot on Site Sign", Tool.AXE,
            () -> new PanelBlock(props(MapColor.WOOD, SoundType.WOOD, 0.5F).noOcclusion()));

    // What the world was: buildings, screens and machines.
    public static final RegistryObject<Block> COMPOSITE_PANEL = block("composite_panel", "Composite Panel", Tool.PICKAXE,
            () -> new Block(props(MapColor.SNOW, SoundType.NETHERITE_BLOCK, 2.0F)));
    public static final RegistryObject<Block> COMPOSITE_PANEL_CRACKED = block("composite_panel_cracked", "Cracked Composite Panel", Tool.PICKAXE,
            () -> new Block(props(MapColor.SNOW, SoundType.NETHERITE_BLOCK, 1.5F)));
    public static final RegistryObject<Block> COMPOSITE_LIGHT_STRIP = block("composite_light_strip", "Composite Light Strip", Tool.PICKAXE,
            () -> new Block(props(MapColor.SNOW, SoundType.NETHERITE_BLOCK, 2.0F).lightLevel(state -> 9)));
    public static final RegistryObject<IronBarsBlock> SHATTERED_GLASS_PANE = block("shattered_glass_pane", "Shattered Glass Pane", Tool.PICKAXE,
            () -> new IronBarsBlock(props(MapColor.NONE, SoundType.GLASS, 0.5F).noOcclusion()));
    public static final RegistryObject<Block> BROKEN_SOLAR_PANEL = block("broken_solar_panel", "Broken Solar Panel", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_BLUE, SoundType.GLASS, 1.5F).noOcclusion(), 0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
    public static final RegistryObject<Block> DEAD_SCREEN = block("dead_screen", "Dead Screen", Tool.PICKAXE,
            () -> new PanelBlock(props(MapColor.COLOR_BLACK, SoundType.GLASS, 1.0F).noOcclusion()));
    public static final RegistryObject<Block> FLICKERING_SCREEN = block("flickering_screen", "Flickering Screen", Tool.PICKAXE,
            () -> new PanelBlock(props(MapColor.COLOR_BLACK, SoundType.GLASS, 1.0F).noOcclusion().lightLevel(state -> 5)));
    public static final RegistryObject<Block> BILLBOARD = block("billboard", "Billboard", Tool.PICKAXE,
            () -> new BillboardBlock(props(MapColor.COLOR_PURPLE, SoundType.METAL, 2.0F).noOcclusion().lightLevel(state -> 6)));
    public static final RegistryObject<Block> SERVER_RACK = block("server_rack", "Server Rack", Tool.PICKAXE,
            () -> new SparkingPropBlock(props(MapColor.COLOR_BLACK, SoundType.METAL, 2.0F).lightLevel(state -> 3), 0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    public static final RegistryObject<Block> CHARGING_STATION = block("charging_station", "Charging Station", Tool.PICKAXE,
            () -> new SparkingPropBlock(props(MapColor.SNOW, SoundType.METAL, 2.0F).noOcclusion(), 3.0, 0.0, 5.0, 13.0, 16.0, 11.0));
    public static final RegistryObject<Block> DRONE_WRECK = block("drone_wreck", "Drone Wreck", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 1.0F).noCollission().noOcclusion(), 0.0, 0.0, 0.0, 16.0, 4.0, 16.0));
    public static final RegistryObject<Block> DEAD_ROBOT = block("dead_robot", "Dead Robot", Tool.PICKAXE,
            () -> new SparkingPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 2.0F).noOcclusion(), 1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
    public static final RegistryObject<Block> ROBOT_SCRAP = block("robot_scrap", "Robot Scrap", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 1.0F).noCollission().noOcclusion(), 1.0, 0.0, 1.0, 15.0, 5.0, 15.0));
    public static final RegistryObject<Block> EV_BODY = block("ev_body", "Electric Car Body", Tool.PICKAXE,
            () -> new Block(props(MapColor.SNOW, SoundType.METAL, 3.0F)));
    public static final RegistryObject<SlabBlock> EV_PANEL = block("ev_panel", "Electric Car Panel", Tool.PICKAXE,
            () -> new SlabBlock(props(MapColor.SNOW, SoundType.METAL, 3.0F)));
    public static final RegistryObject<Block> EV_WINDOW = block("ev_window", "Electric Car Window", Tool.PICKAXE,
            () -> new PanelBlock(props(MapColor.COLOR_BLACK, SoundType.GLASS, 0.5F).noOcclusion()));
    public static final RegistryObject<Block> EV_BATTERY = block("ev_battery", "Electric Car Battery", Tool.PICKAXE,
            () -> new Block(props(MapColor.COLOR_BLACK, SoundType.METAL, 3.0F).lightLevel(state -> 4)));
    public static final RegistryObject<Block> NEON_TUBE_PINK = block("neon_tube_pink", "Pink Neon Tube", Tool.PICKAXE,
            () -> new PillarPropBlock(props(MapColor.COLOR_PINK, SoundType.GLASS, 0.3F).noCollission().noOcclusion().lightLevel(state -> 9), 1.0));
    public static final RegistryObject<Block> NEON_TUBE_CYAN = block("neon_tube_cyan", "Cyan Neon Tube", Tool.PICKAXE,
            () -> new PillarPropBlock(props(MapColor.COLOR_CYAN, SoundType.GLASS, 0.3F).noCollission().noOcclusion().lightLevel(state -> 9), 1.0));
    public static final RegistryObject<Block> LAMP_POST = block("lamp_post", "Lamp Post", Tool.PICKAXE,
            () -> new PillarPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 2.0F).noOcclusion(), 2.0));
    public static final RegistryObject<Block> STREET_LAMP = block("street_lamp", "Street Lamp", Tool.PICKAXE,
            () -> new LampPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 1.0F).noCollission().noOcclusion()
                    .lightLevel(state -> state.getValue(LampPropBlock.LIT) ? 13 : 0), 3.0, 6.0, 0.0, 13.0, 11.0, 16.0));
    public static final RegistryObject<Block> FLOODLIGHT = block("floodlight", "Floodlight", Tool.PICKAXE,
            () -> new LampPropBlock(props(MapColor.COLOR_GRAY, SoundType.METAL, 1.5F).noOcclusion()
                    .lightLevel(state -> state.getValue(LampPropBlock.LIT) ? 15 : 0), 3.0, 0.0, 3.0, 13.0, 14.0, 13.0));
    public static final RegistryObject<Block> HOLO_PROJECTOR = block("holo_projector", "Hologram Projector", Tool.PICKAXE,
            () -> new SparkingPropBlock(props(MapColor.COLOR_CYAN, SoundType.METAL, 1.5F).noCollission().noOcclusion().lightLevel(state -> 7), 4.0, 0.0, 4.0, 12.0, 2.0, 12.0));

    // The futuristic art it left behind.
    public static final RegistryObject<Block> CHROME_BLOCK = block("chrome_block", "Sculpture Chrome", Tool.PICKAXE,
            () -> new Block(props(MapColor.METAL, SoundType.NETHERITE_BLOCK, 3.0F)));
    public static final RegistryObject<Block> CHROME_CRACKED = block("chrome_cracked", "Cracked Sculpture Chrome", Tool.PICKAXE,
            () -> new Block(props(MapColor.METAL, SoundType.NETHERITE_BLOCK, 3.0F).noOcclusion().lightLevel(state -> 7)));
    public static final RegistryObject<Block> CHROME_SHARD = block("chrome_shard", "Chrome Shard", Tool.PICKAXE,
            () -> new FacingPropBlock(props(MapColor.METAL, SoundType.AMETHYST, 1.0F).noOcclusion().lightLevel(state -> 5), 2.0, 0.0, 2.0, 14.0, 14.0, 14.0));

    // Bits and pieces.
    public static final RegistryObject<Block> TRAFFIC_CONE = block("traffic_cone", "Traffic Cone", Tool.AXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_ORANGE, SoundType.WOOL, 0.2F).noCollission().noOcclusion(), 3.0, 0.0, 3.0, 13.0, 12.0, 13.0));
    public static final RegistryObject<Block> TIRE_STACK = block("tire_stack", "Tire Stack", Tool.AXE,
            () -> new FacingPropBlock(props(MapColor.COLOR_BLACK, SoundType.WOOL, 1.0F).noOcclusion(), 1.0, 0.0, 1.0, 15.0, 15.0, 15.0));
    public static final RegistryObject<Block> BLOOD_DECAL = block("blood_decal", "Blood Stain", Tool.SHOVEL,
            () -> new DecalBlock(props(MapColor.COLOR_RED, SoundType.SLIME_BLOCK, 0.1F).noCollission().noOcclusion()));
    public static final RegistryObject<Block> RUBBLE_DECAL = block("rubble_decal", "Scattered Rubble", Tool.SHOVEL,
            () -> new DecalBlock(props(MapColor.STONE, SoundType.GRAVEL, 0.1F).noCollission().noOcclusion()));
    public static final RegistryObject<Block> SCORCH_DECAL = block("scorch_decal", "Scorch Mark", Tool.SHOVEL,
            () -> new DecalBlock(props(MapColor.COLOR_BLACK, SoundType.SAND, 0.1F).noCollission().noOcclusion()));

    private ModKit() {
    }

    private static BlockBehaviour.Properties props(MapColor color, SoundType sound, float strength) {
        return BlockBehaviour.Properties.of().mapColor(color).sound(sound).strength(strength);
    }

    private static <T extends Block> RegistryObject<T> block(String id, String name, Tool tool, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(id, block);
        NAMES.put(registered, name);
        (tool == Tool.PICKAXE ? PICKAXE : tool == Tool.AXE ? AXE : SHOVEL).add(registered);
        BLOCK_ITEMS.add(ITEMS.register(id, () -> new BlockItem(registered.get(), new Item.Properties())));
        return registered;
    }

    public static Map<RegistryObject<? extends Block>, String> names() {
        return NAMES;
    }

    public static List<RegistryObject<? extends Block>> pickaxeBlocks() {
        return PICKAXE;
    }

    public static List<RegistryObject<? extends Block>> axeBlocks() {
        return AXE;
    }

    public static List<RegistryObject<? extends Block>> shovelBlocks() {
        return SHOVEL;
    }

    public static List<RegistryObject<Item>> items() {
        return BLOCK_ITEMS;
    }

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            BLOCK_ITEMS.forEach(event::accept);
        }
    }
}
