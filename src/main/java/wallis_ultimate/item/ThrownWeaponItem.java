package wallis_ultimate.item;

import java.util.function.BiFunction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** GEAR-007: right-click to throw, like a snowball but heavier. */
public class ThrownWeaponItem extends Item {
    private static final float VELOCITY = 1.0F;
    private static final float INACCURACY = 1.0F;
    private static final int COOLDOWN_TICKS = 20;

    private final BiFunction<Level, LivingEntity, ThrowableItemProjectile> projectile;

    public ThrownWeaponItem(BiFunction<Level, LivingEntity, ThrowableItemProjectile> projectile) {
        super(new Item.Properties().stacksTo(8));
        this.projectile = projectile;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.6F);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (!level.isClientSide()) {
            ThrowableItemProjectile thrown = projectile.apply(level, player);
            thrown.setItem(stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, VELOCITY, INACCURACY);
            level.addFreshEntity(thrown);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
