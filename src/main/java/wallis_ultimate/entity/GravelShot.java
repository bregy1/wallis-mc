package wallis_ultimate.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import wallis_ultimate.combat.Noise;
import wallis_ultimate.registry.ModEntities;

/**
 * GEAR-007: a piece of gravel from the Slingshot. Firing is silent, but where a shot lands against a block it makes
 * noise, which can draw zombies away (ENEMY-006).
 */
public class GravelShot extends ThrowableItemProjectile {
    public static final double NOISE_RADIUS = 12.0;

    private float damage;

    public GravelShot(EntityType<? extends GravelShot> type, Level level) {
        super(type, level);
    }

    public GravelShot(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.GRAVEL_SHOT.get(), shooter, level);
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GRAVEL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Noise.make(level(), result.getLocation(), NOISE_RADIUS);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            discard();
        }
    }
}
