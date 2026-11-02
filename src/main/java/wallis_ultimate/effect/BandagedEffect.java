package wallis_ultimate.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** SURV-010: heals 1 HP every {@link #HEAL_INTERVAL_TICKS} while active. */
public class BandagedEffect extends MobEffect {
    public static final int HEAL_INTERVAL_TICKS = 16;

    public BandagedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8E0D0);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.heal(1.0F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % HEAL_INTERVAL_TICKS == 0;
    }
}
