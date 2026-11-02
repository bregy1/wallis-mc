package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.survival.DietEffects;

/** Checks for SURV-008 and SURV-009: what the diet effects do, and Farmer's Delight's weakened Nourishment. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DietGameTests {
    private static final String PLATFORM = "platform";
    private static final ResourceLocation NOURISHMENT = ResourceLocation.fromNamespaceAndPath("farmersdelight", "nourishment");

    private DietGameTests() {
    }

    private static Player mockPlayer(GameTestHelper helper, String name) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(pos));
        return player;
    }

    private static void give(Player player, MobEffect effect) {
        player.addEffect(new MobEffectInstance(effect, 200));
    }

    /** Vegetables change natural healing; effects like Regeneration and the bandage keep their own speed. */
    @GameTest(template = PLATFORM)
    public static void vegetablesChangeNaturalHealing(GameTestHelper helper) {
        float heal = 4.0F;
        Player hearty = mockPlayer(helper, "dt_hearty");
        give(hearty, ModEffects.HEARTY.get());
        Player frail = mockPlayer(helper, "dt_frail");
        give(frail, ModEffects.FRAIL.get());
        Player bandaged = mockPlayer(helper, "dt_bandaged");
        give(bandaged, ModEffects.FRAIL.get());
        give(bandaged, ModEffects.BANDAGED.get());
        for (Player player : new Player[] {hearty, frail, bandaged}) {
            player.setHealth(10.0F);
        }
        hearty.heal(heal);
        frail.heal(heal);
        bandaged.heal(heal);
        if (hearty.getHealth() != 10.0F + heal * DietEffects.HEARTY_HEALING) {
            helper.fail("Hearty healed to " + hearty.getHealth());
        }
        if (frail.getHealth() != 10.0F + heal * DietEffects.FRAIL_HEALING) {
            helper.fail("Frail healed to " + frail.getHealth());
        }
        if (bandaged.getHealth() != 10.0F + heal) {
            helper.fail("Frail slowed a bandage: " + bandaged.getHealth());
        }
        helper.succeed();
    }

    /** High grains: a meal gives extra saturation. High sugars: a short speed boost after eating. */
    @GameTest(template = PLATFORM)
    public static void grainsAndSugarsWorkWhenEating(GameTestHelper helper) {
        Player plain = mockPlayer(helper, "dt_plain");
        Player player = mockPlayer(helper, "dt_filled");
        give(player, ModEffects.FILLING.get());
        give(player, ModEffects.SUGAR_BUZZ.get());
        ItemStack bread = new ItemStack(Items.BREAD);
        for (Player eater : new Player[] {plain, player}) {
            eater.getFoodData().setFoodLevel(10);
            eater.getFoodData().setSaturation(0.0F);
            eater.getFoodData().eat(Items.BREAD, bread);
            ForgeEventFactory.onItemUseFinish(eater, bread, 0, bread);
        }
        float mealSaturation = Items.BREAD.getFoodProperties().getNutrition() * Items.BREAD.getFoodProperties().getSaturationModifier() * 2.0F;
        float expected = plain.getFoodData().getSaturationLevel() + mealSaturation * DietEffects.FILLING_BONUS;
        if (Math.abs(player.getFoodData().getSaturationLevel() - expected) > 0.001F) {
            helper.fail("saturation " + player.getFoodData().getSaturationLevel() + ", expected " + expected);
        }
        if (plain.hasEffect(MobEffects.MOVEMENT_SPEED) || !player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            helper.fail("the speed boost went to the wrong player");
        }
        helper.succeed();
    }

    /** SURV-008: Nourishment only slows hunger drain, it never stops it (Q-103). */
    @GameTest(template = PLATFORM)
    public static void nourishmentOnlySlowsHungerDrain(GameTestHelper helper) {
        MobEffect nourishment = ForgeRegistries.MOB_EFFECTS.getValue(NOURISHMENT);
        if (nourishment == null) {
            helper.fail("Farmer's Delight's Nourishment isn't registered");
        }
        Player player = mockPlayer(helper, "dt_nourished");
        give(player, nourishment);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);
        // Exhaustion of 8 at half the drain is 4: exactly one saturation point.
        player.getFoodData().setExhaustion(8.0F);
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
        if (player.getFoodData().getSaturationLevel() != 4.0F) {
            helper.fail("saturation " + player.getFoodData().getSaturationLevel() + ", expected 4");
        }
        helper.succeed();
    }
}
