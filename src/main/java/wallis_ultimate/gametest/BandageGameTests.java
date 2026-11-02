package wallis_ultimate.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.item.BandageItem;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModItems;

/** Checks for SURV-010: the Bandage heals 3 hearts over 5 seconds. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BandageGameTests {
    private static final String PLATFORM = "platform";

    private BandageGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void bandageIsUsedOnlyWhenHurt(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BANDAGE.get(), 2));

        player.setHealth(player.getMaxHealth());
        ModItems.BANDAGE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (player.hasEffect(ModEffects.BANDAGED.get()) || player.getMainHandItem().getCount() != 2) {
            helper.fail("bandage was used at full health");
        }

        player.setHealth(10.0F);
        ModItems.BANDAGE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!player.hasEffect(ModEffects.BANDAGED.get())) {
            helper.fail("bandage did not apply the Bandaged effect");
        }
        if (player.getMainHandItem().getCount() != 1) {
            helper.fail("bandage was not consumed");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void bandagesAreCraftedFromClothAndPaperAndStackTo16(GameTestHelper helper) {
        if (ModItems.BANDAGE.get().getMaxStackSize() != 16) {
            helper.fail("bandages stack to " + ModItems.BANDAGE.get().getMaxStackSize());
        }
        Recipe<?> recipe = helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "bandage")).orElse(null);
        if (recipe == null) {
            helper.fail("no bandage recipe");
            return;
        }
        ItemStack result = recipe.getResultItem(helper.getLevel().registryAccess());
        if (!result.is(ModItems.BANDAGE.get()) || result.getCount() != 1) {
            helper.fail("recipe makes " + result);
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        long cloth = ingredients.stream().filter(i -> i.test(new ItemStack(Items.STRING)) && i.test(new ItemStack(Items.RED_WOOL))).count();
        long paper = ingredients.stream().filter(i -> i.test(new ItemStack(Items.PAPER))).count();
        if (ingredients.size() != 4 || cloth != 3 || paper != 1) {
            helper.fail("unexpected ingredients: " + ingredients.size() + " total, " + cloth + " cloth, " + paper + " paper");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200)
    public static void bandagedHealsSixHealthOverFiveSeconds(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(3, 2, 3));
        pig.setHealth(2.0F);
        pig.setInvulnerable(true); // only the effect may change its health
        pig.addEffect(new MobEffectInstance(ModEffects.BANDAGED.get(), BandageItem.HEAL_DURATION_TICKS, 0));
        helper.succeedWhen(() -> {
            if (pig.hasEffect(ModEffects.BANDAGED.get())) {
                helper.fail("still bandaged");
            }
            if (Math.abs(pig.getHealth() - 8.0F) > 0.01F) {
                helper.fail("expected 8 health after the effect, got " + pig.getHealth());
            }
        });
    }
}
