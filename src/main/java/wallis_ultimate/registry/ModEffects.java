package wallis_ultimate.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.effect.BandagedEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, WallisUltimate.MOD_ID);

    public static final RegistryObject<MobEffect> BANDAGED = EFFECTS.register("bandaged", BandagedEffect::new);

    private ModEffects() {
    }
}
