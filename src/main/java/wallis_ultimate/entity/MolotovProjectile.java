package wallis_ultimate.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;

/** GEAR-007: a thrown Moonshine Molotov. Wherever it lands, it leaves a {@link BurningPatch}. */
public class MolotovProjectile extends ThrowableItemProjectile {
    public MolotovProjectile(EntityType<? extends MolotovProjectile> type, Level level) {
        super(type, level);
    }

    public MolotovProjectile(Level level, LivingEntity thrower) {
        super(ModEntities.MOONSHINE_MOLOTOV.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOONSHINE_MOLOTOV.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            BurningPatch patch = ModEntities.BURNING_PATCH.get().create(level());
            patch.setPos(result.getLocation());
            level().addFreshEntity(patch);
            level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            discard();
        }
    }
}
