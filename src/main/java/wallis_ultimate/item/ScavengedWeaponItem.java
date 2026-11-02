package wallis_ultimate.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

/**
 * GEAR-007: a melee weapon patched together from junk. It has its own damage, attack speed and reach, instead of
 * belonging to a tool tier, and may do something special on a hit.
 */
public class ScavengedWeaponItem extends Item {
    /** A player's bare hand: the modifiers below add to these. */
    private static final double BASE_DAMAGE = 1.0;
    private static final double BASE_ATTACKS_PER_SECOND = 4.0;
    private static final UUID REACH_MODIFIER = UUID.fromString("5c0d7f31-9a44-4e27-8b16-2f6a9e1c7d05");

    private final double damage;
    private final double attacksPerSecond;
    private final double extraReach;
    private final BiConsumer<LivingEntity, LivingEntity> onHit;
    /** Built on first use: Forge's reach attribute isn't registered yet when items are. */
    private Multimap<Attribute, AttributeModifier> modifiers;

    /** {@code onHit} gets the target and the attacker. */
    public ScavengedWeaponItem(int durability, double damage, double attacksPerSecond, double extraReach, BiConsumer<LivingEntity, LivingEntity> onHit) {
        super(new Item.Properties().durability(durability));
        this.damage = damage;
        this.attacksPerSecond = attacksPerSecond;
        this.extraReach = extraReach;
        this.onHit = onHit;
    }

    public double damage() {
        return damage;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        onHit.accept(target, attacker);
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        if (modifiers == null) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage - BASE_DAMAGE, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attacksPerSecond - BASE_ATTACKS_PER_SECOND, AttributeModifier.Operation.ADDITION));
            if (extraReach > 0) {
                builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_MODIFIER, "Weapon reach", extraReach, AttributeModifier.Operation.ADDITION));
            }
            modifiers = builder.build();
        }
        return modifiers;
    }
}
