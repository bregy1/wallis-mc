package wallis_ultimate.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** GEAR-007: 1 damage per second while it lasts, from the Nail Bat. */
public class BleedingEffect extends MobEffect {
    public static final int DAMAGE_INTERVAL_TICKS = 20;

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8E1B1B);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), 1.0F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }
}
