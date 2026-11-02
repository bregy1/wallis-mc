package wallis_ultimate.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.GraveBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WallisUltimate.MOD_ID);

    /** SURV-004. */
    public static final RegistryObject<BlockEntityType<GraveBlockEntity>> GRAVE = BLOCK_ENTITY_TYPES.register("grave",
            () -> BlockEntityType.Builder.of(GraveBlockEntity::new, ModBlocks.GRAVE.get()).build(null));

    private ModBlockEntities() {
    }
}
