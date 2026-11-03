package wallis_ultimate.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.BillboardBlock;
import wallis_ultimate.block.LampPropBlock;
import wallis_ultimate.block.PillarPropBlock;
import wallis_ultimate.registry.ModKit;

/** WORLD-013: models for the apocalypse kit. Props that face a way are authored against the block's north side, looking south. */
public class ModKitModelProvider extends BlockStateProvider {
    public ModKitModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, WallisUltimate.MOD_ID, files);
    }

    @Override
    public String getName() {
        return "Apocalypse kit models";
    }

    @Override
    protected void registerStatesAndModels() {
        ground();
        walls();
        whatTheWorldWas();
        art();
        bits();
    }

    private ResourceLocation tex(String name) {
        return modLoc("block/" + name);
    }

    private void ground() {
        turningCube(ModKit.CRACKED_CONCRETE);
        slabWithItem(ModKit.CRACKED_CONCRETE_SLAB, "cracked_concrete");
        stairsWithItem(ModKit.CRACKED_CONCRETE_STAIRS, "cracked_concrete");
        wallWithItem(ModKit.CRACKED_CONCRETE_WALL, "cracked_concrete");
        turningCube(ModKit.ASPHALT);
        slabWithItem(ModKit.ASPHALT_SLAB, "asphalt");
        roadLine(ModKit.ASPHALT_LINE);
        roadLine(ModKit.ASPHALT_DASHED);
        rubblePile();
        simpleBlock(ModKit.EXPOSED_REBAR.get(), models().cross("exposed_rebar", tex("exposed_rebar")).renderType("cutout"));
        flatItem(ModKit.EXPOSED_REBAR, "exposed_rebar");
    }

    private void walls() {
        cube(ModKit.CORRUGATED_METAL);
        panel(ModKit.CORRUGATED_SHEET, "corrugated_sheet", 0, 0);
        columnCube(ModKit.SHIPPING_CONTAINER_BLUE, "shipping_container_top");
        columnCube(ModKit.SHIPPING_CONTAINER_ORANGE, "shipping_container_top");
        cube(ModKit.SCRAP_BLOCK);
        scrapPile();
        pane(ModKit.CHAIN_LINK_FENCE, "chain_link_fence", "chain_link_edge");
        jerseyBarrier();
        simpleBlockWithItem(ModKit.GABION_BARRIER.get(), models().cubeBottomTop("gabion_barrier", tex("gabion_side"), tex("gabion_top"), tex("gabion_top")));
        cube(ModKit.HAZARD_PLATE);
        girder();
        panel(ModKit.SIGN_KEEP_OUT, "sign_keep_out", 0, 0);
        panel(ModKit.SIGN_SAFE_ZONE, "sign_safe_zone", 0, 0);
        panel(ModKit.SIGN_SHOT_ON_SITE, "sign_shot_on_site", 0, 0);
    }

    private void whatTheWorldWas() {
        cube(ModKit.COMPOSITE_PANEL);
        cube(ModKit.COMPOSITE_PANEL_CRACKED);
        cube(ModKit.COMPOSITE_LIGHT_STRIP);
        pane(ModKit.SHATTERED_GLASS_PANE, "shattered_glass", "shattered_glass_edge");
        solarPanel();
        panel(ModKit.DEAD_SCREEN, "dead_screen", 0, 0);
        panel(ModKit.FLICKERING_SCREEN, "flickering_screen", 15, 15);
        billboard();
        serverRack();
        chargingStation();
        drone();
        deadRobot();
        robotScrap();
        cube(ModKit.EV_BODY);
        slabWithItem(ModKit.EV_PANEL, "ev_body");
        panel(ModKit.EV_WINDOW, "ev_window", 0, 0);
        cube(ModKit.EV_BATTERY);
        neon(ModKit.NEON_TUBE_PINK, "neon_tube_pink");
        neon(ModKit.NEON_TUBE_CYAN, "neon_tube_cyan");
        lampPost();
        streetLamp();
        floodlight();
        holoProjector();
    }

    private void art() {
        simpleBlockWithItem(ModKit.CHROME_BLOCK.get(), models().cubeAll("chrome_block", tex("chrome")));
        chromeCracked();
        chromeShard();
    }

    private void bits() {
        trafficCone();
        tireStack();
        decal(ModKit.BLOOD_DECAL, "blood_decal");
        decal(ModKit.RUBBLE_DECAL, "rubble_decal");
        decal(ModKit.SCORCH_DECAL, "scorch_decal");
    }

    private void cube(RegistryObject<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    /** Turned at random around Y, so a floor of them doesn't show a repeating pattern. */
    private void turningCube(RegistryObject<Block> block) {
        ModelFile model = cubeAll(block.get());
        simpleBlock(block.get(), ConfiguredModel.allYRotations(model, 0, false));
        simpleBlockItem(block.get(), model);
    }

    private void columnCube(RegistryObject<Block> block, String top) {
        simpleBlockWithItem(block.get(), models().cubeBottomTop(block.getId().getPath(), tex(block.getId().getPath()), tex(top), tex(top)));
    }

    private void slabWithItem(RegistryObject<SlabBlock> block, String texture) {
        slabBlock(block.get(), tex(texture), tex(texture));
        simpleBlockItem(block.get(), models().getExistingFile(block.getId().withPrefix("block/")));
    }

    private void stairsWithItem(RegistryObject<StairBlock> block, String texture) {
        stairsBlock(block.get(), tex(texture));
        simpleBlockItem(block.get(), models().getExistingFile(block.getId().withPrefix("block/")));
    }

    private void wallWithItem(RegistryObject<WallBlock> block, String texture) {
        wallBlock(block.get(), tex(texture));
        itemModels().wallInventory(block.getId().getPath(), tex(texture));
    }

    private void flatItem(RegistryObject<? extends Block> block, String texture) {
        itemModels().withExistingParent(block.getId().getPath(), mcLoc("item/generated")).texture("layer0", tex(texture));
    }

    private void pane(RegistryObject<IronBarsBlock> block, String pane, String edge) {
        paneBlockWithRenderType(block.get(), tex(pane), tex(edge), "cutout");
        flatItem(block, pane);
    }

    /** A sheet along the top of the block; directionalBlock turns "up" to the side FACING names. */
    private void panel(RegistryObject<Block> block, String texture, int blockLight, int skyLight) {
        BlockModelBuilder model = models().withExistingParent(block.getId().getPath(), mcLoc("block/block"))
                .texture("particle", tex(texture))
                .texture("all", tex(texture))
                .renderType("cutout");
        model.element().from(0.0F, 15.0F, 0.0F).to(16.0F, 16.0F, 16.0F).textureAll("#all").emissivity(blockLight, skyLight).end();
        directionalBlock(block.get(), model);
        flatItem(block, texture);
    }

    private BlockModelBuilder prop(String name, String particle) {
        return models().withExistingParent(name, mcLoc("block/block")).texture("particle", tex(particle));
    }

    private void facingWithItem(RegistryObject<Block> block, ModelFile model) {
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    /** A plain box with one texture and UVs taken from where it sits. Only for boxes that stay inside the block. */
    private void box(BlockModelBuilder model, float x1, float y1, float z1, float x2, float y2, float z2, String texture) {
        model.element().from(x1, y1, z1).to(x2, y2, z2).textureAll(texture).end();
    }

    /** A box with one area of the texture on all its faces. Boxes that stick out of the block need this. */
    private void boxUv(BlockModelBuilder model, float x1, float y1, float z1, float x2, float y2, float z2, String texture, float u1, float v1, float u2, float v2) {
        model.element().from(x1, y1, z1).to(x2, y2, z2)
                .allFaces((direction, face) -> face.texture(texture).uvs(u1, v1, u2, v2))
                .end();
    }

    private void roadLine(RegistryObject<Block> block) {
        String name = block.getId().getPath();
        ModelFile model = models().cubeBottomTop(name, tex("asphalt"), tex("asphalt"), tex(name));
        facingWithItem(block, model);
    }

    private void rubblePile() {
        BlockModelBuilder model = prop("rubble_pile", "rubble").texture("all", tex("rubble"));
        box(model, 0.0F, 0.0F, 0.0F, 16.0F, 3.0F, 16.0F, "#all");
        box(model, 2.0F, 3.0F, 1.0F, 12.0F, 6.0F, 13.0F, "#all");
        box(model, 5.0F, 6.0F, 3.0F, 10.0F, 8.0F, 8.0F, "#all");
        model.element().from(9.0F, 3.0F, 8.0F).to(15.0F, 7.0F, 14.0F)
                .rotation().origin(12.0F, 5.0F, 11.0F).axis(Direction.Axis.Y).angle(22.5F).end()
                .textureAll("#all").end();
        facingWithItem(ModKit.RUBBLE_PILE, model);
    }

    private void scrapPile() {
        BlockModelBuilder model = prop("scrap_pile", "scrap_block")
                .texture("all", tex("scrap_block"))
                .texture("sheet", tex("corrugated_metal"));
        box(model, 1.0F, 0.0F, 1.0F, 15.0F, 4.0F, 15.0F, "#all");
        box(model, 3.0F, 4.0F, 2.0F, 13.0F, 8.0F, 12.0F, "#all");
        box(model, 6.0F, 8.0F, 5.0F, 11.0F, 11.0F, 10.0F, "#all");
        model.element().from(1.0F, 4.0F, 6.0F).to(11.0F, 5.0F, 15.0F)
                .rotation().origin(6.0F, 4.0F, 10.0F).axis(Direction.Axis.Z).angle(22.5F).end()
                .textureAll("#sheet").end();
        facingWithItem(ModKit.SCRAP_PILE, model);
    }

    /** Runs along x when it faces north or south: three steps, wide at the foot, with a painted band on top. */
    private void jerseyBarrier() {
        BlockModelBuilder model = prop("jersey_barrier", "jersey_barrier").texture("all", tex("jersey_barrier"));
        boxUv(model, 0.0F, 0.0F, 2.0F, 16.0F, 5.0F, 14.0F, "#all", 0.0F, 9.0F, 16.0F, 14.0F);
        boxUv(model, 0.0F, 5.0F, 4.0F, 16.0F, 9.0F, 12.0F, "#all", 0.0F, 5.0F, 16.0F, 9.0F);
        boxUv(model, 0.0F, 9.0F, 5.0F, 16.0F, 14.0F, 11.0F, "#all", 0.0F, 0.0F, 16.0F, 5.0F);
        facingWithItem(ModKit.JERSEY_BARRIER, model);
    }

    private void axis(RegistryObject<Block> block, ModelFile model) {
        axisBlock((PillarPropBlock) block.get(), model, model);
        simpleBlockItem(block.get(), model);
    }

    /** An I-beam. */
    private void girder() {
        BlockModelBuilder model = prop("steel_girder", "steel_girder").texture("all", tex("steel_girder")).renderType("cutout");
        box(model, 2.0F, 0.0F, 2.0F, 14.0F, 16.0F, 4.0F, "#all");
        box(model, 2.0F, 0.0F, 12.0F, 14.0F, 16.0F, 14.0F, "#all");
        box(model, 7.0F, 0.0F, 4.0F, 9.0F, 16.0F, 12.0F, "#all");
        axis(ModKit.STEEL_GIRDER, model);
    }

    /** A cracked array on a post, tipped toward the sky. */
    private void solarPanel() {
        BlockModelBuilder model = prop("broken_solar_panel", "broken_solar_panel")
                .texture("panel", tex("broken_solar_panel"))
                .texture("post", tex("lamp_post"))
                .renderType("cutout");
        box(model, 7.0F, 0.0F, 7.0F, 9.0F, 7.0F, 9.0F, "#post");
        model.element().from(0.0F, 7.0F, 0.0F).to(16.0F, 8.0F, 16.0F)
                .rotation().origin(8.0F, 7.5F, 8.0F).axis(Direction.Axis.X).angle(22.5F).end()
                .textureAll("#panel").end();
        facingWithItem(ModKit.BROKEN_SOLAR_PANEL, model);
    }

    private void billboard() {
        ModelFile[] tiles = new ModelFile[BillboardBlock.COLUMNS * BillboardBlock.ROWS];
        for (int row = 0; row < BillboardBlock.ROWS; row++) {
            for (int column = 0; column < BillboardBlock.COLUMNS; column++) {
                float width = 16.0F / BillboardBlock.COLUMNS;
                float height = 16.0F / BillboardBlock.ROWS;
                float u1 = column * width;
                float v1 = row * height;
                BlockModelBuilder model = prop("billboard_" + (row * BillboardBlock.COLUMNS + column), "lamp_post")
                        .texture("ad", tex("billboard"))
                        .texture("back", tex("lamp_post"))
                        .renderType("cutout");
                model.element().from(0.0F, 0.0F, 1.0F).to(16.0F, 16.0F, 2.0F).emissivity(11, 11)
                        .face(Direction.SOUTH).texture("#ad").uvs(u1, v1, u1 + width, v1 + height).end()
                        .face(Direction.NORTH).texture("#ad").uvs(u1, v1, u1 + width, v1 + height).end()
                        .end();
                model.element().from(0.0F, 0.0F, 0.0F).to(16.0F, 16.0F, 1.0F)
                        .face(Direction.NORTH).texture("#back").end()
                        .face(Direction.EAST).texture("#back").end()
                        .face(Direction.WEST).texture("#back").end()
                        .face(Direction.UP).texture("#back").end()
                        .face(Direction.DOWN).texture("#back").end()
                        .end();
                tiles[row * BillboardBlock.COLUMNS + column] = model;
            }
        }
        horizontalBlock(ModKit.BILLBOARD.get(), state -> tiles[state.getValue(BillboardBlock.TILE)]);
        flatItem(ModKit.BILLBOARD, "dead_screen");
    }

    private void serverRack() {
        ResourceLocation side = tex("server_rack_side");
        ModelFile model = models().cube("server_rack", side, side, side, tex("server_rack_front"), side, side).texture("particle", side);
        facingWithItem(ModKit.SERVER_RACK, model);
    }

    /** A post with a screen and a cable hanging off it, taller than one block. */
    private void chargingStation() {
        BlockModelBuilder model = prop("charging_station", "charging_station")
                .texture("all", tex("charging_station"))
                .texture("cable", tex("lamp_post"));
        model.element().from(4.0F, 0.0F, 6.0F).to(12.0F, 24.0F, 10.0F)
                .allFaces((direction, face) -> {
                    if (direction == Direction.SOUTH) {
                        face.texture("#all").uvs(0.0F, 0.0F, 16.0F, 16.0F);
                    } else {
                        face.texture("#all").uvs(0.0F, 9.0F, 2.0F, 11.0F);
                    }
                })
                .end();
        boxUv(model, 12.0F, 10.0F, 7.0F, 13.0F, 20.0F, 9.0F, "#cable", 0.0F, 0.0F, 2.0F, 8.0F);
        boxUv(model, 12.0F, 8.0F, 6.0F, 15.0F, 11.0F, 10.0F, "#cable", 2.0F, 2.0F, 6.0F, 6.0F);
        boxUv(model, 3.0F, 0.0F, 5.0F, 13.0F, 1.0F, 11.0F, "#cable", 0.0F, 0.0F, 8.0F, 8.0F);
        facingWithItem(ModKit.CHARGING_STATION, model);
    }

    /** Crashed on the ground: one rotor still in place, one bent, the rest gone. */
    private void drone() {
        BlockModelBuilder model = prop("drone_wreck", "drone").texture("all", tex("drone"));
        boxUv(model, 5.0F, 0.0F, 5.0F, 11.0F, 3.0F, 11.0F, "#all", 0.0F, 0.0F, 8.0F, 8.0F);
        model.element().from(0.0F, 1.0F, 7.5F).to(16.0F, 2.0F, 8.5F)
                .rotation().origin(8.0F, 1.5F, 8.0F).axis(Direction.Axis.Y).angle(45.0F).rescale(true).end()
                .allFaces((direction, face) -> face.texture("#all").uvs(0.0F, 8.0F, 16.0F, 10.0F))
                .end();
        model.element().from(4.0F, 1.0F, 7.5F).to(16.0F, 2.0F, 8.5F)
                .rotation().origin(8.0F, 1.5F, 8.0F).axis(Direction.Axis.Y).angle(-45.0F).rescale(true).end()
                .allFaces((direction, face) -> face.texture("#all").uvs(0.0F, 8.0F, 16.0F, 10.0F))
                .end();
        boxUv(model, 0.0F, 2.0F, 0.0F, 5.0F, 2.5F, 5.0F, "#all", 8.0F, 0.0F, 14.0F, 6.0F);
        boxUv(model, 11.0F, 2.0F, 11.0F, 16.0F, 2.5F, 16.0F, "#all", 8.0F, 0.0F, 14.0F, 6.0F);
        model.element().from(9.0F, 0.5F, 0.0F).to(15.0F, 1.0F, 6.0F)
                .rotation().origin(12.0F, 0.75F, 3.0F).axis(Direction.Axis.X).angle(22.5F).end()
                .allFaces((direction, face) -> face.texture("#all").uvs(8.0F, 0.0F, 14.0F, 6.0F))
                .end();
        facingWithItem(ModKit.DRONE_WRECK, model);
    }

    /** Slumped against the block's north side with its legs out, one arm off. Its eye is still on. */
    private void deadRobot() {
        BlockModelBuilder model = prop("dead_robot", "robot")
                .texture("all", tex("robot"))
                .texture("eye", tex("robot_eye"))
                .renderType("cutout");
        boxUv(model, 4.0F, 3.0F, 2.0F, 12.0F, 13.0F, 7.0F, "#all", 6.0F, 0.0F, 14.0F, 7.0F);
        boxUv(model, 5.0F, 13.0F, 2.0F, 11.0F, 18.0F, 8.0F, "#all", 0.0F, 0.0F, 6.0F, 5.0F);
        boxUv(model, 4.0F, 0.0F, 5.0F, 7.0F, 3.0F, 15.0F, "#all", 0.0F, 8.0F, 7.0F, 11.0F);
        boxUv(model, 9.0F, 0.0F, 4.0F, 12.0F, 3.0F, 14.0F, "#all", 0.0F, 8.0F, 7.0F, 11.0F);
        boxUv(model, 1.0F, 2.0F, 3.0F, 4.0F, 12.0F, 6.0F, "#all", 0.0F, 8.0F, 3.0F, 15.0F);
        boxUv(model, 12.0F, 0.0F, 11.0F, 16.0F, 2.0F, 15.0F, "#all", 8.0F, 8.0F, 12.0F, 12.0F);
        model.element().from(6.0F, 15.0F, 8.0F).to(8.0F, 16.0F, 8.1F).shade(false).emissivity(15, 15).textureAll("#eye").end();
        facingWithItem(ModKit.DEAD_ROBOT, model);
    }

    private void robotScrap() {
        BlockModelBuilder model = prop("robot_scrap", "robot").texture("all", tex("robot"));
        boxUv(model, 2.0F, 0.0F, 2.0F, 8.0F, 5.0F, 8.0F, "#all", 0.0F, 0.0F, 6.0F, 5.0F);
        boxUv(model, 9.0F, 0.0F, 3.0F, 15.0F, 1.0F, 10.0F, "#all", 6.0F, 0.0F, 14.0F, 7.0F);
        model.element().from(3.0F, 0.0F, 10.0F).to(14.0F, 3.0F, 13.0F)
                .rotation().origin(8.0F, 1.5F, 11.0F).axis(Direction.Axis.Y).angle(22.5F).end()
                .allFaces((direction, face) -> face.texture("#all").uvs(0.0F, 8.0F, 7.0F, 11.0F))
                .end();
        facingWithItem(ModKit.ROBOT_SCRAP, model);
    }

    private void neon(RegistryObject<Block> block, String texture) {
        BlockModelBuilder model = prop(block.getId().getPath(), texture).texture("all", tex(texture));
        model.element().from(7.0F, 0.0F, 7.0F).to(9.0F, 16.0F, 9.0F).shade(false).emissivity(15, 15).textureAll("#all").end();
        axis(block, model);
    }

    private void lampPost() {
        BlockModelBuilder model = prop("lamp_post", "lamp_post").texture("all", tex("lamp_post"));
        box(model, 6.0F, 0.0F, 6.0F, 10.0F, 16.0F, 10.0F, "#all");
        axis(ModKit.LAMP_POST, model);
    }

    /** An arm reaching out of the block's north side, for a lamp post standing there. */
    private void streetLamp() {
        ModelFile on = streetLampModel("street_lamp_on", "lamp_glass", 15);
        ModelFile off = streetLampModel("street_lamp", "lamp_glass_off", 0);
        horizontalBlock(ModKit.STREET_LAMP.get(), state -> state.getValue(LampPropBlock.LIT) ? on : off);
        simpleBlockItem(ModKit.STREET_LAMP.get(), off);
    }

    private ModelFile streetLampModel(String name, String glass, int light) {
        BlockModelBuilder model = prop(name, "lamp_head")
                .texture("post", tex("lamp_post"))
                .texture("head", tex("lamp_head"))
                .texture("glass", tex(glass));
        box(model, 7.0F, 8.0F, 0.0F, 9.0F, 10.0F, 8.0F, "#post");
        box(model, 4.0F, 7.0F, 8.0F, 12.0F, 11.0F, 16.0F, "#head");
        model.element().from(5.0F, 6.5F, 9.0F).to(11.0F, 7.0F, 15.0F).shade(false).emissivity(light, light).textureAll("#glass").end();
        return model;
    }

    /** On a little stand, its head tipped down and looking south. */
    private void floodlight() {
        ModelFile on = floodlightModel("floodlight_on", "lamp_glass", 15);
        ModelFile off = floodlightModel("floodlight", "lamp_glass_off", 0);
        horizontalBlock(ModKit.FLOODLIGHT.get(), state -> state.getValue(LampPropBlock.LIT) ? on : off);
        simpleBlockItem(ModKit.FLOODLIGHT.get(), off);
    }

    private ModelFile floodlightModel(String name, String glass, int light) {
        BlockModelBuilder model = prop(name, "lamp_head")
                .texture("post", tex("lamp_post"))
                .texture("head", tex("lamp_head"))
                .texture("glass", tex(glass));
        box(model, 6.0F, 0.0F, 6.0F, 10.0F, 2.0F, 10.0F, "#post");
        box(model, 7.0F, 2.0F, 7.0F, 9.0F, 6.0F, 9.0F, "#post");
        model.element().from(3.0F, 6.0F, 4.0F).to(13.0F, 14.0F, 10.0F)
                .rotation().origin(8.0F, 8.0F, 8.0F).axis(Direction.Axis.X).angle(22.5F).end()
                .textureAll("#head").end();
        model.element().from(4.0F, 7.0F, 10.0F).to(12.0F, 13.0F, 10.2F).shade(false).emissivity(light, light)
                .rotation().origin(8.0F, 8.0F, 8.0F).axis(Direction.Axis.X).angle(22.5F).end()
                .textureAll("#glass").end();
        return model;
    }

    /** A small base throwing a glitching life-size figure into the air, two crossed sheets of light. */
    private void holoProjector() {
        BlockModelBuilder model = prop("holo_projector", "lamp_head")
                .texture("base", tex("lamp_head"))
                .texture("figure", tex("holo_figure"))
                .renderType("translucent");
        box(model, 4.0F, 0.0F, 4.0F, 12.0F, 2.0F, 12.0F, "#base");
        for (float angle : new float[] {45.0F, -45.0F}) {
            model.element().from(0.0F, 2.0F, 8.0F).to(16.0F, 32.0F, 8.0F).shade(false).emissivity(15, 15)
                    .rotation().origin(8.0F, 8.0F, 8.0F).axis(Direction.Axis.Y).angle(angle).end()
                    .face(Direction.NORTH).texture("#figure").uvs(0.0F, 0.0F, 16.0F, 16.0F).end()
                    .face(Direction.SOUTH).texture("#figure").uvs(0.0F, 0.0F, 16.0F, 16.0F).end()
                    .end();
        }
        horizontalBlock(ModKit.HOLO_PROJECTOR.get(), model);
        simpleBlockItem(ModKit.HOLO_PROJECTOR.get(), model);
    }

    /** Dented chrome with a second skin of glowing cracks over it. */
    private void chromeCracked() {
        BlockModelBuilder model = prop("chrome_cracked", "chrome_cracked")
                .texture("base", tex("chrome_cracked"))
                .texture("cracks", tex("chrome_cracks"))
                .renderType("cutout");
        model.element().from(0.0F, 0.0F, 0.0F).to(16.0F, 16.0F, 16.0F)
                .allFaces((direction, face) -> face.texture("#base").cullface(direction))
                .end();
        model.element().from(-0.02F, -0.02F, -0.02F).to(16.02F, 16.02F, 16.02F).shade(false).emissivity(15, 15)
                .allFaces((direction, face) -> face.texture("#cracks").cullface(direction))
                .end();
        simpleBlockWithItem(ModKit.CHROME_CRACKED.get(), model);
    }

    /** Chrome spikes sticking out of the ground, what's left of the art, with some glow where they broke. */
    private void chromeShard() {
        BlockModelBuilder model = prop("chrome_shard", "chrome")
                .texture("chrome", tex("chrome"))
                .texture("glow", tex("chrome_glow"));
        model.element().from(6.0F, 0.0F, 6.0F).to(10.0F, 15.0F, 10.0F)
                .rotation().origin(8.0F, 0.0F, 8.0F).axis(Direction.Axis.X).angle(22.5F).end()
                .textureAll("#chrome").end();
        model.element().from(2.0F, 0.0F, 7.0F).to(5.0F, 11.0F, 10.0F)
                .rotation().origin(3.5F, 0.0F, 8.5F).axis(Direction.Axis.Z).angle(-22.5F).end()
                .textureAll("#chrome").end();
        model.element().from(10.0F, 0.0F, 3.0F).to(13.0F, 12.0F, 6.0F)
                .rotation().origin(11.5F, 0.0F, 4.5F).axis(Direction.Axis.Z).angle(22.5F).end()
                .textureAll("#chrome").end();
        model.element().from(7.0F, 0.0F, 7.0F).to(9.0F, 6.0F, 12.0F).shade(false).emissivity(15, 15).textureAll("#glow").end();
        facingWithItem(ModKit.CHROME_SHARD, model);
    }

    private void trafficCone() {
        BlockModelBuilder model = prop("traffic_cone", "traffic_cone").texture("all", tex("traffic_cone"));
        boxUv(model, 3.0F, 0.0F, 3.0F, 13.0F, 1.0F, 13.0F, "#all", 0.0F, 14.0F, 16.0F, 16.0F);
        boxUv(model, 5.0F, 1.0F, 5.0F, 11.0F, 4.0F, 11.0F, "#all", 0.0F, 9.0F, 16.0F, 13.0F);
        boxUv(model, 6.0F, 4.0F, 6.0F, 10.0F, 8.0F, 10.0F, "#all", 0.0F, 5.0F, 16.0F, 9.0F);
        boxUv(model, 7.0F, 8.0F, 7.0F, 9.0F, 12.0F, 9.0F, "#all", 0.0F, 0.0F, 16.0F, 5.0F);
        facingWithItem(ModKit.TRAFFIC_CONE, model);
    }

    private void tireStack() {
        BlockModelBuilder model = prop("tire_stack", "wreck_tread")
                .texture("top", tex("tire_top"))
                .texture("tread", tex("wreck_tread"));
        tire(model, 2.0F, 0.0F, 2.0F);
        tire(model, 3.0F, 5.0F, 1.0F);
        tire(model, 2.0F, 10.0F, 3.0F);
        facingWithItem(ModKit.TIRE_STACK, model);
    }

    private void tire(BlockModelBuilder model, float x, float y, float z) {
        model.element().from(x, y, z).to(x + 12.0F, y + 5.0F, z + 12.0F)
                .allFaces((direction, face) -> {
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.texture("#top").uvs(0.0F, 0.0F, 16.0F, 16.0F);
                    } else {
                        face.texture("#tread").uvs(0.0F, 0.0F, 16.0F, 5.0F);
                    }
                })
                .end();
    }

    private void decal(RegistryObject<Block> block, String texture) {
        BlockModelBuilder model = prop(texture, texture).texture("all", tex(texture)).renderType("cutout");
        model.element().from(0.0F, 0.1F, 0.0F).to(16.0F, 0.1F, 16.0F).face(Direction.UP).texture("#all").end().end();
        simpleBlock(block.get(), ConfiguredModel.allYRotations(model, 0, false));
        flatItem(block, texture);
    }
}
