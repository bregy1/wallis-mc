package wallis_ultimate.combat;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModItems;

/**
 * CMBT-003: the Chapter 1 trinkets whose effects aren't plain attributes. Granny's Rabbit Foot weakens night enemies
 * (ENEMY-003), Worn Spurs and the Lucky Horseshoe improve the horse you ride (TRANS-002), and the Spare Clip adds
 * shots to the downed pistol (SURV-001). Starting values, tuned in playtesting.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Trinkets {
    public static final float RABBIT_FOOT_DAMAGE_DEALT = 1.1F;
    public static final float RABBIT_FOOT_DAMAGE_TAKEN = 0.9F;
    public static final double SPURS_SPEED = 0.15;
    public static final double HORSESHOE_JUMP = 0.20;
    public static final int SPARE_CLIP_SHOTS = 5;

    private static final AttributeModifier SPURS = new AttributeModifier(UUID.fromString("2f1a6d0e-52a7-4a0b-9f3e-7c1d5b8a9e10"),
            "Worn Spurs", SPURS_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier HORSESHOE = new AttributeModifier(UUID.fromString("8b4c2e71-0d36-4f58-a1c9-3e6f7a2b1d44"),
            "Lucky Horseshoe", HORSESHOE_JUMP, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private Trinkets() {
    }

    public static boolean isEquipped(LivingEntity entity, Item trinket) {
        return CuriosApi.getCuriosInventory(entity).map(curios -> curios.isEquipped(trinket)).orElse(false);
    }

    /** Extra shots on the downed pistol. */
    public static int pistolBonus(Player player) {
        return isEquipped(player, ModItems.SPARE_CLIP.get()) ? SPARE_CLIP_SHOTS : 0;
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!RegularZombie.isNight(victim.level())) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        Item foot = ModItems.GRANNYS_RABBIT_FOOT.get();
        if (attacker instanceof Player player && victim instanceof Enemy && isEquipped(player, foot)) {
            event.setAmount(event.getAmount() * RABBIT_FOOT_DAMAGE_DEALT);
        } else if (victim instanceof Player player && attacker instanceof Enemy && isEquipped(player, foot)) {
            event.setAmount(event.getAmount() * RABBIT_FOOT_DAMAGE_TAKEN);
        }
    }

    /** Horse bonuses follow the rider's trinkets, so taking one off mid-ride ends its bonus. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide() && player.getVehicle() instanceof AbstractHorse horse) {
            setBonus(horse, Attributes.MOVEMENT_SPEED, SPURS, isEquipped(player, ModItems.WORN_SPURS.get()));
            setBonus(horse, Attributes.JUMP_STRENGTH, HORSESHOE, isEquipped(player, ModItems.LUCKY_HORSESHOE.get()));
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.isDismounting() && event.getEntityBeingMounted() instanceof AbstractHorse horse && !horse.level().isClientSide()) {
            setBonus(horse, Attributes.MOVEMENT_SPEED, SPURS, false);
            setBonus(horse, Attributes.JUMP_STRENGTH, HORSESHOE, false);
        }
    }

    private static void setBonus(AbstractHorse horse, Attribute attribute, AttributeModifier bonus, boolean active) {
        AttributeInstance instance = horse.getAttribute(attribute);
        if (instance == null || instance.hasModifier(bonus) == active) {
            return;
        }
        if (active) {
            instance.addTransientModifier(bonus);
        } else {
            instance.removeModifier(bonus);
        }
    }
}
