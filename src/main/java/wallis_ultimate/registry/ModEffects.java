package wallis_ultimate.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.effect.BandagedEffect;
import wallis_ultimate.effect.BleedingEffect;
import wallis_ultimate.effect.DietEffect;
import wallis_ultimate.effect.SunburnEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, WallisUltimate.MOD_ID);

    public static final RegistryObject<MobEffect> BANDAGED = EFFECTS.register("bandaged", BandagedEffect::new);
    /** GEAR-007, from the Nail Bat. */
    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", BleedingEffect::new);
    /** ENEMY-002. */
    public static final RegistryObject<MobEffect> SUNBURN = EFFECTS.register("sunburn", SunburnEffect::new);
    /** SURV-009, given by the diet suite; DietEffects does the work. High vegetables: faster natural healing. */
    public static final RegistryObject<MobEffect> HEARTY = EFFECTS.register("hearty", () -> new DietEffect(MobEffectCategory.BENEFICIAL, 0x5BA33C));
    /** Low vegetables: slower natural healing. */
    public static final RegistryObject<MobEffect> FRAIL = EFFECTS.register("frail", () -> new DietEffect(MobEffectCategory.HARMFUL, 0x8A8A5C));
    /** High grains: more saturation from meals. */
    public static final RegistryObject<MobEffect> FILLING = EFFECTS.register("filling", () -> new DietEffect(MobEffectCategory.BENEFICIAL, 0xD4A31C));
    /** High sugars: a short speed boost after eating. */
    public static final RegistryObject<MobEffect> SUGAR_BUZZ = EFFECTS.register("sugar_buzz", () -> new DietEffect(MobEffectCategory.BENEFICIAL, 0xE86FB0));

    private ModEffects() {
    }
}
