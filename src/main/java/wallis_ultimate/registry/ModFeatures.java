package wallis_ultimate.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.worldgen.RuinFeature;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, WallisUltimate.MOD_ID);

    /** WORLD-014. */
    public static final RegistryObject<RuinFeature> RUIN = FEATURES.register("ruin", () -> new RuinFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
