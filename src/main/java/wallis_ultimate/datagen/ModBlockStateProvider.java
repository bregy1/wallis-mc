package wallis_ultimate.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.OilDrumBlock;
import wallis_ultimate.registry.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        cubeWithItem(ModBlocks.REINFORCED_CONCRETE);
        cubeWithItem(ModBlocks.RIVETED_STEEL);
        cubeWithItem(ModBlocks.ALIEN_ALLOY);
        simpleBlock(ModBlocks.GRAVE.get()); // no item: only deaths make graves
        signpost();
        props();
        hogFarm();
    }

    /** ENEMY-011: placeholders, like all our art. Only the farm has these, so they need no items. */
    private void hogFarm() {
        simpleBlock(ModBlocks.STRAPPED_HAY.get());
        simpleBlock(ModBlocks.CRUSHED_WRECK.get());
        BlockModelBuilder post = models().withExistingParent("pen_post", mcLoc("block/block"))
                .texture("particle", mcLoc("block/stripped_spruce_log"))
                .texture("side", mcLoc("block/stripped_spruce_log"))
                .texture("top", mcLoc("block/stripped_spruce_log_top"));
        post.element().from(4.0F, 0.0F, 4.0F).to(12.0F, 16.0F, 12.0F)
                .allFaces((direction, face) -> face.texture(direction.getAxis() == Direction.Axis.Y ? "#top" : "#side"))
                .end();
        horizontalBlock(ModBlocks.PEN_POST.get(), post);
        // Open at the top, with the slop inside.
        BlockModelBuilder trough = models().withExistingParent("feeding_trough", mcLoc("block/block"))
                .texture("particle", mcLoc("block/spruce_planks"))
                .texture("wood", mcLoc("block/spruce_planks"))
                .texture("slop", mcLoc("block/mud"));
        trough.element().from(0.0F, 0.0F, 2.0F).to(16.0F, 2.0F, 14.0F).textureAll("#wood").end();
        trough.element().from(0.0F, 2.0F, 2.0F).to(16.0F, 8.0F, 4.0F).textureAll("#wood").end();
        trough.element().from(0.0F, 2.0F, 12.0F).to(16.0F, 8.0F, 14.0F).textureAll("#wood").end();
        trough.element().from(0.0F, 2.0F, 4.0F).to(16.0F, 6.0F, 12.0F).textureAll("#slop").end();
        horizontalBlock(ModBlocks.FEEDING_TROUGH.get(), trough);
        getVariantBuilder(ModBlocks.CORN.get())
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)
                .modelForState().modelFile(models().cross("corn_lower", modLoc("block/corn_lower")).renderType("cutout")).addModel()
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
                .modelForState().modelFile(models().cross("corn_upper", modLoc("block/corn_upper")).renderType("cutout")).addModel();
    }

    /** A pole with two painted boards pointing different ways. */
    private void signpost() {
        BlockModelBuilder model = models().withExistingParent("travel_signpost", mcLoc("block/block"))
                .texture("particle", mcLoc("block/spruce_log"))
                .texture("post", mcLoc("block/spruce_log"))
                .texture("board", modLoc("block/travel_sign"));
        // Taller than a block, so the UVs can't be left to follow its height: they'd run off the texture.
        model.element().from(7.0F, 0.0F, 7.0F).to(9.0F, 26.0F, 9.0F)
                .allFaces((direction, face) -> face.texture("#post").uvs(0.0F, 0.0F, 3.0F, 16.0F))
                .end();
        model.element().from(1.0F, 18.0F, 9.0F).to(15.0F, 22.0F, 10.0F)
                .allFaces((direction, face) -> face.texture("#board").uvs(0.0F, 0.0F, 14.0F, 4.0F))
                .end();
        model.element().from(9.0F, 12.0F, 1.0F).to(10.0F, 16.0F, 15.0F)
                .allFaces((direction, face) -> face.texture("#board").uvs(0.0F, 4.0F, 14.0F, 8.0F))
                .end();
        simpleBlock(ModBlocks.TRAVEL_SIGNPOST.get(), model);
    }

    /** WORLD-013. Item models for the flat ones are in ModItemModelProvider. */
    private void props() {
        cubeWithItem(ModBlocks.SANDBAGS);
        slabWithItem(ModBlocks.SANDBAG_SLAB, "sandbags");
        simpleBlock(ModBlocks.BARBED_WIRE.get(), models().cross("barbed_wire", modLoc("block/barbed_wire")).renderType("cutout"));
        drum();
        panel(ModBlocks.GREEN_TARP, "green_tarp");
        panel(ModBlocks.BLUE_TARP, "blue_tarp");
        slope(ModBlocks.GREEN_TARP_SLOPE, "green_tarp");
        slope(ModBlocks.BLUE_TARP_SLOPE, "blue_tarp");
        cubeWithItem(ModBlocks.SUPPLY_CRATE);
        cubeWithItem(ModBlocks.WRECK_BODY);
        slabWithItem(ModBlocks.WRECK_PANEL, "wreck_body");
        panel(ModBlocks.WRECK_WINDOW, "wreck_window");
        wheel();
        beerCans();
        antlers();
        oldRadio();
    }

    private void cubeWithItem(RegistryObject<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    private void slabWithItem(RegistryObject<SlabBlock> block, String texture) {
        slabBlock(block.get(), modLoc("block/" + texture), modLoc("block/" + texture));
        simpleBlockItem(block.get(), models().getExistingFile(block.getId().withPrefix("block/")));
    }

    /** Rounded with crossing boxes. Lit ones get flames out of the top, which glow in the dark. */
    private void drum() {
        BlockModelBuilder unlit = drumBase("oil_drum", "oil_drum_top");
        BlockModelBuilder lit = drumBase("oil_drum_lit", "oil_drum_top_lit");
        lit.texture("fire", mcLoc("block/fire_0")).renderType("cutout");
        for (float angle : new float[] {45.0F, -45.0F}) {
            lit.element().from(2.0F, 12.0F, 8.0F).to(14.0F, 28.0F, 8.0F).shade(false).emissivity(15, 15)
                    .rotation().origin(8.0F, 8.0F, 8.0F).axis(Direction.Axis.Y).angle(angle).end()
                    .face(Direction.NORTH).texture("#fire").uvs(2.0F, 0.0F, 14.0F, 16.0F).end()
                    .face(Direction.SOUTH).texture("#fire").uvs(2.0F, 0.0F, 14.0F, 16.0F).end()
                    .end();
        }
        getVariantBuilder(ModBlocks.OIL_DRUM.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(OilDrumBlock.LIT) ? lit : unlit).build());
        simpleBlockItem(ModBlocks.OIL_DRUM.get(), unlit);
    }

    private BlockModelBuilder drumBase(String name, String top) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("particle", modLoc("block/oil_drum_side"))
                .texture("side", modLoc("block/oil_drum_side"))
                .texture("top", modLoc("block/" + top))
                .texture("bottom", modLoc("block/oil_drum_top"));
        drumBox(model, 3.0F, 13.0F, 2.0F, 14.0F);
        drumBox(model, 2.0F, 3.0F, 3.0F, 13.0F);
        drumBox(model, 13.0F, 14.0F, 3.0F, 13.0F);
        return model;
    }

    private void drumBox(BlockModelBuilder model, float fromX, float toX, float fromZ, float toZ) {
        model.element().from(fromX, 0.0F, fromZ).to(toX, 16.0F, toZ)
                .allFaces((direction, face) -> face.texture(direction == Direction.UP ? "#top" : direction == Direction.DOWN ? "#bottom" : "#side"))
                .end();
    }

    /** A sheet along the top of the block; directionalBlock turns "up" to the side FACING names. */
    private void panel(RegistryObject<Block> block, String texture) {
        BlockModelBuilder model = models().withExistingParent(block.getId().getPath(), mcLoc("block/block"))
                .texture("particle", modLoc("block/" + texture))
                .texture("all", modLoc("block/" + texture))
                .renderType("cutout");
        model.element().from(0.0F, 15.0F, 0.0F).to(16.0F, 16.0F, 16.0F).textureAll("#all").end();
        directionalBlock(block.get(), model);
    }

    /** A sheet running diagonally through the block, rising toward FACING. For tent roofs. */
    private void slope(RegistryObject<Block> block, String texture) {
        BlockModelBuilder model = models().withExistingParent(block.getId().getPath(), mcLoc("block/block"))
                .texture("particle", modLoc("block/" + texture))
                .texture("all", modLoc("block/" + texture));
        model.element().from(0.0F, 7.5F, 0.0F).to(16.0F, 8.5F, 16.0F)
                .rotation().origin(8.0F, 8.0F, 8.0F).axis(Direction.Axis.X).angle(45.0F).rescale(true).end()
                .textureAll("#all").end();
        horizontalBlock(block.get(), model);
    }

    /** A disc lying against the block's north side, rounded with columns of different height. */
    private void wheel() {
        BlockModelBuilder model = models().withExistingParent("wreck_wheel", mcLoc("block/block"))
                .texture("particle", modLoc("block/wreck_tread"))
                .texture("face", modLoc("block/wreck_wheel"))
                .texture("tread", modLoc("block/wreck_tread"));
        float[][] columns = {{0, 1, 5, 11}, {1, 2, 3, 13}, {2, 4, 1, 15}, {4, 12, 0, 16}, {12, 14, 1, 15}, {14, 15, 3, 13}, {15, 16, 5, 11}};
        for (float[] column : columns) {
            model.element().from(column[0], column[2], 0.0F).to(column[1], column[3], 5.0F)
                    .allFaces((direction, face) -> face.texture(direction.getAxis() == Direction.Axis.Z ? "#face" : "#tread"))
                    .end();
        }
        horizontalBlock(ModBlocks.WRECK_WHEEL.get(), model);
        simpleBlockItem(ModBlocks.WRECK_WHEEL.get(), model);
    }

    private void beerCans() {
        BlockModelBuilder model = models().withExistingParent("beer_cans", mcLoc("block/block"))
                .texture("particle", modLoc("block/beer_can"))
                .texture("all", modLoc("block/beer_can"));
        can(model, 3.0F, 0.0F, 3.0F, 6.0F, 5.0F, 6.0F);
        can(model, 9.0F, 0.0F, 5.0F, 12.0F, 5.0F, 8.0F);
        can(model, 5.0F, 0.0F, 10.0F, 10.0F, 3.0F, 13.0F); // this one fell over
        horizontalBlock(ModBlocks.BEER_CANS.get(), model);
        simpleBlockItem(ModBlocks.BEER_CANS.get(), model);
    }

    private void can(BlockModelBuilder model, float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        model.element().from(fromX, fromY, fromZ).to(toX, toY, toZ)
                .allFaces((direction, face) -> {
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.texture("#all").uvs(5.0F, 0.0F, 8.0F, 3.0F);
                    } else {
                        face.texture("#all").uvs(0.0F, 0.0F, 3.0F, 5.0F);
                    }
                })
                .end();
    }

    /** A wooden plaque with a flat cut-out rack in front of it. */
    private void antlers() {
        BlockModelBuilder model = models().withExistingParent("antlers", mcLoc("block/block"))
                .texture("particle", modLoc("block/antlers"))
                .texture("rack", modLoc("block/antlers"))
                .texture("plaque", mcLoc("block/dark_oak_planks"))
                .renderType("cutout");
        model.element().from(6.0F, 3.0F, 0.0F).to(10.0F, 9.0F, 2.0F).textureAll("#plaque").end();
        model.element().from(0.0F, 2.0F, 2.5F).to(16.0F, 18.0F, 2.5F).shade(false)
                .face(Direction.SOUTH).texture("#rack").uvs(0.0F, 0.0F, 16.0F, 16.0F).end()
                .face(Direction.NORTH).texture("#rack").uvs(0.0F, 0.0F, 16.0F, 16.0F).end()
                .end();
        horizontalBlock(ModBlocks.ANTLERS.get(), model);
    }

    /** A boxy old radio with a bent aerial. The texture's front is on the north face, like every prop here. */
    private void oldRadio() {
        BlockModelBuilder model = models().withExistingParent("old_radio", mcLoc("block/block"))
                .texture("particle", modLoc("block/old_radio"))
                .texture("all", modLoc("block/old_radio"))
                .texture("aerial", mcLoc("block/iron_block"));
        model.element().from(3.0F, 0.0F, 5.0F).to(13.0F, 7.0F, 11.0F)
                .allFaces((direction, face) -> {
                    if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                        face.texture("#all").uvs(3.0F, 0.0F, 13.0F, 7.0F);
                    } else {
                        face.texture("#all").uvs(0.0F, 8.0F, 6.0F, 15.0F);
                    }
                })
                .end();
        model.element().from(11.0F, 7.0F, 7.5F).to(11.5F, 14.0F, 8.0F).textureAll("#aerial").end();
        horizontalBlock(ModBlocks.OLD_RADIO.get(), model);
        simpleBlockItem(ModBlocks.OLD_RADIO.get(), model);
    }
}
