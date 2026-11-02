package wallis_ultimate.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** SURV-009: an effect the diet suite gives; {@link wallis_ultimate.survival.DietEffects} checks for it. */
public class DietEffect extends MobEffect {
    public DietEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
