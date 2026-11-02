package wallis_ultimate.survival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEffects;

/**
 * SURV-008 and SURV-009. The diet suite (data/wallis_ultimate/diet/suites) gives these effects to players whose food
 * groups are high or low; here they do their work. Starting values, tuned in playtesting. Also weakens Farmer's
 * Delight's Nourishment so it only slows hunger drain (Q-103).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DietEffects {
    /** Natural healing with high vegetables (Hearty) and with low vegetables (Frail). */
    public static final float HEARTY_HEALING = 1.5F;
    public static final float FRAIL_HEALING = 0.5F;
    /** High grains (Filling): this much extra saturation on top of what a meal gives. */
    public static final float FILLING_BONUS = 0.25F;
    /** High sugars (Sugar Buzz): Speed I for this long after eating. */
    public static final int SUGAR_BUZZ_TICKS = 100;
    /** Nourishment lets through this share of the hunger drain it would otherwise cancel. */
    public static final float NOURISHED_DRAIN = 0.5F;

    private static final ResourceLocation NOURISHMENT = ResourceLocation.fromNamespaceAndPath("farmersdelight", "nourishment");
    private static final String NOURISHED_EXHAUSTION = WallisUltimate.MOD_ID + ":nourished_exhaustion";
    /** Vanilla's food exhaustion threshold: this much exhaustion costs one saturation point or food level. */
    private static final float EXHAUSTION_PER_POINT = 4.0F;

    private DietEffects() {
    }

    /** Natural healing only: potions and bandages (which arrive through effects) keep their own speed. */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.hasEffect(MobEffects.REGENERATION)
                || player.hasEffect(ModEffects.BANDAGED.get())) {
            return;
        }
        if (player.hasEffect(ModEffects.HEARTY.get())) {
            event.setAmount(event.getAmount() * HEARTY_HEALING);
        } else if (player.hasEffect(ModEffects.FRAIL.get())) {
            event.setAmount(event.getAmount() * FRAIL_HEALING);
        }
    }

    @SubscribeEvent
    public static void onEaten(LivingEntityUseItemEvent.Finish event) {
        ItemStack food = event.getItem();
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || !food.isEdible()) {
            return;
        }
        if (player.hasEffect(ModEffects.FILLING.get())) {
            addFillingSaturation(player, food.getFoodProperties(player));
        }
        if (player.hasEffect(ModEffects.SUGAR_BUZZ.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SUGAR_BUZZ_TICKS, 0));
        }
    }

    public static void addFillingSaturation(Player player, FoodProperties food) {
        if (food == null) {
            return;
        }
        FoodData data = player.getFoodData();
        float mealSaturation = food.getNutrition() * food.getSaturationModifier() * 2.0F;
        data.setSaturation(Math.min(data.getSaturationLevel() + mealSaturation * FILLING_BONUS, data.getFoodLevel()));
    }

    /**
     * Farmer's Delight zeroes the player's exhaustion on every effect tick while Nourishment is active. The start of
     * the player's tick comes before that, so what's there now is what piled up since the last tick; a share of it
     * still counts, like vanilla's own food tick would count it.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.START || player.level().isClientSide()) {
            return;
        }
        MobEffect nourishment = ForgeRegistries.MOB_EFFECTS.getValue(NOURISHMENT);
        if (nourishment == null || !player.hasEffect(nourishment)) {
            return;
        }
        FoodData data = player.getFoodData();
        CompoundTag saved = player.getPersistentData();
        float owed = saved.getFloat(NOURISHED_EXHAUSTION) + data.getExhaustionLevel() * NOURISHED_DRAIN;
        while (owed >= EXHAUSTION_PER_POINT) {
            owed -= EXHAUSTION_PER_POINT;
            if (data.getSaturationLevel() > 0.0F) {
                data.setSaturation(Math.max(data.getSaturationLevel() - 1.0F, 0.0F));
            } else {
                data.setFoodLevel(Math.max(data.getFoodLevel() - 1, 0));
            }
        }
        saved.putFloat(NOURISHED_EXHAUSTION, owed);
    }
}
