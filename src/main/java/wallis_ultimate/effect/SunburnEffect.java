package wallis_ultimate.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * ENEMY-002: marks a zombie that burns in daylight. {@link wallis_ultimate.entity.RegularZombie} checks for it and
 * burns like a vanilla zombie; on other mobs it does nothing yet. Given hidden (Q-134).
 */
public class SunburnEffect extends MobEffect {
    public SunburnEffect() {
        super(MobEffectCategory.HARMFUL, 0xF28C28);
    }
}
