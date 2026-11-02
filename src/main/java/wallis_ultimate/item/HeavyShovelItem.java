package wallis_ultimate.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import wallis_ultimate.combat.WeaponEffects;

/** GEAR-007: a heavy shovel that digs like a stone one, but hits hard enough to knock back and briefly stun. */
public class HeavyShovelItem extends ShovelItem {
    /** With stone's attack bonus of 1 and a bare hand's 1 that makes 4 damage, at 1 attack per second. */
    public HeavyShovelItem() {
        super(Tiers.STONE, 2.0F, -3.0F, new Properties().durability(180));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WeaponEffects.heavySwing(target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }
}
