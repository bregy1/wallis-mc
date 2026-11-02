package wallis_ultimate.item;

import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import wallis_ultimate.entity.GravelShot;

/** GEAR-007: draws like a bow, shoots gravel, and is silent. Where a shot lands makes noise instead. */
public class SlingshotItem extends BowItem {
    public static final float FULL_DRAW_DAMAGE = 3.0F;
    private static final float FULL_DRAW_VELOCITY = 2.0F;
    private static final float MIN_POWER = 0.1F;

    public SlingshotItem() {
        super(new Item.Properties().durability(180));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.is(Items.GRAVEL);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack ammo = player.getProjectile(stack);
        boolean free = player.getAbilities().instabuild;
        float power = getPowerForTime(getUseDuration(stack) - timeLeft);
        if ((ammo.isEmpty() && !free) || power < MIN_POWER) {
            return;
        }
        if (!level.isClientSide()) {
            GravelShot shot = new GravelShot(level, player, FULL_DRAW_DAMAGE * power);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, FULL_DRAW_VELOCITY * power, 1.0F);
            level.addFreshEntity(shot);
            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        if (!free) {
            ammo.shrink(1);
        }
    }
}
