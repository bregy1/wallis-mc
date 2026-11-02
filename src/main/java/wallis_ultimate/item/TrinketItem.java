package wallis_ultimate.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * CMBT-003: a trinket worn in a Curios slot; which slot comes from the item tags in datagen. It either grants an
 * attribute while equipped or has no stats of its own, and {@link wallis_ultimate.combat.Trinkets} checks for it.
 */
public class TrinketItem extends Item implements ICurioItem {
    @Nullable
    private final Attribute attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    /** A trinket whose effect lives in {@link wallis_ultimate.combat.Trinkets}. */
    public TrinketItem() {
        this(null, 0.0, AttributeModifier.Operation.ADDITION);
    }

    public TrinketItem(@Nullable Attribute attribute, double amount, AttributeModifier.Operation operation) {
        super(new Item.Properties().stacksTo(1));
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (attribute != null) {
            modifiers.put(attribute, new AttributeModifier(uuid, "Trinket", amount, operation));
        }
        return modifiers;
    }

    /** Right-clicking puts it into a free slot, like armor. */
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
