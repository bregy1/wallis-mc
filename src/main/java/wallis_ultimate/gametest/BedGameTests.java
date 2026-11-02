package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.survival.Beds;

/** Checks for SURV-005: beds are rare, one placed bed per player and planet. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedGameTests {
    private static final String PLATFORM = "platform";

    private BedGameTests() {
    }

    /** No recipe makes a new bed. Recoloring a bed you already have (bed + dye) is still allowed. */
    @GameTest(template = PLATFORM)
    public static void bedsCantBeCrafted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            boolean makesBed = recipe.getResultItem(level.registryAccess()).getItem() instanceof BedItem;
            boolean usesBed = recipe.getIngredients().stream()
                    .anyMatch(ingredient -> Arrays.stream(ingredient.getItems()).anyMatch(stack -> stack.getItem() instanceof BedItem));
            if (makesBed && !usesBed) {
                helper.fail("recipe " + recipe.getId() + " makes a new bed");
            }
        }
        helper.succeed();
    }

    /** Q-144: one placed bed per planet. A second is refused while the first stands, allowed once it's gone. */
    @GameTest(template = PLATFORM)
    public static void aSecondBedCantBePlacedWhileTheFirstStands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos first = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos second = helper.absolutePos(new BlockPos(5, 2, 5));
        Player player = new Player(level, first, 0.0F, new GameProfile(UUID.randomUUID(), "bd_owner")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        if (placeBed(level, player, first)) {
            helper.fail("the first bed was refused");
        }
        if (!placeBed(level, player, second)) {
            helper.fail("a second bed was allowed while the first stands");
        }
        // The usual way in: right-clicking with a bed is refused before anything is placed.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_BED));
        PlayerInteractEvent.RightClickBlock click = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND,
                second.below(), new BlockHitResult(Vec3.atCenterOf(second.below()), Direction.UP, second.below(), false));
        MinecraftForge.EVENT_BUS.post(click);
        if (click.getUseItem() != Event.Result.DENY) {
            helper.fail("right-clicking with a second bed wasn't refused");
        }
        level.removeBlock(first, false);
        if (placeBed(level, player, second)) {
            helper.fail("a new bed was refused after the old one was picked up");
        }
        level.removeBlock(second, false);
        helper.succeed();
    }

    /** Puts a bed block down and fires the place event like a real placement; true if it was refused. */
    private static boolean placeBed(ServerLevel level, Player player, BlockPos pos) {
        level.setBlock(pos, Blocks.RED_BED.defaultBlockState(), 3);
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, pos),
                Blocks.STONE.defaultBlockState(), player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            level.removeBlock(pos, false);
        }
        return event.isCanceled();
    }

    @GameTest(template = PLATFORM)
    public static void onlySleepingThroughTheNightHeals(GameTestHelper helper) {
        if (!Beds.sleptThroughTheNight(false, false, 1000L) || !Beds.sleptThroughTheNight(false, false, 24000L * 3 + 50L)
                || !Beds.sleptThroughTheNight(false, false, 23900L)) {
            helper.fail("waking in the morning doesn't count");
        }
        if (Beds.sleptThroughTheNight(false, true, 1000L)) {
            helper.fail("getting out of bed counts");
        }
        if (Beds.sleptThroughTheNight(true, false, 1000L)) {
            helper.fail("being thrown out of bed counts");
        }
        if (Beds.sleptThroughTheNight(false, false, 18000L)) {
            helper.fail("waking at midnight counts");
        }

        Player player = helper.makeMockPlayer();
        player.setHealth(5.0F);
        MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, false, true));
        if (player.getHealth() != 5.0F) {
            helper.fail("getting out of bed healed");
        }
        MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, false, false));
        float expected = Beds.sleptThroughTheNight(false, false, helper.getLevel().getDayTime()) ? player.getMaxHealth() : 5.0F;
        if (player.getHealth() != expected) {
            helper.fail("health after waking: " + player.getHealth() + ", expected " + expected);
        }
        helper.succeed();
    }
}
