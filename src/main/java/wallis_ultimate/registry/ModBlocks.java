package wallis_ultimate.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WallisUltimate.MOD_ID);

    /** WORLD-006: unminable blocks for protected structures. */
    public static final RegistryObject<Block> REINFORCED_CONCRETE = BLOCKS.register("reinforced_concrete",
            () -> new Block(unminable(MapColor.COLOR_LIGHT_GRAY, SoundType.STONE)));
    public static final RegistryObject<Block> RIVETED_STEEL = BLOCKS.register("riveted_steel",
            () -> new Block(unminable(MapColor.METAL, SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> ALIEN_ALLOY = BLOCKS.register("alien_alloy",
            () -> new Block(unminable(MapColor.COLOR_PURPLE, SoundType.AMETHYST)));

    private ModBlocks() {
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
