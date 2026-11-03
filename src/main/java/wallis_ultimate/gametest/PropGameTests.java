package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.bananemdnsa.historystages.api.lock.ItemLocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.BarbedWireBlock;
import wallis_ultimate.block.OilDrumBlock;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.registry.ModKit;

/** Checks for WORLD-013: barbed wire works on everyone; props are locked, uncraftable and drop themselves. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PropGameTests {
    private static final String PLATFORM = "platform";

    private PropGameTests() {
    }

    private static List<Item> propItems() {
        List<Item> items = new ArrayList<>(List.of(ModItems.SANDBAGS.get(), ModItems.SANDBAG_SLAB.get(), ModItems.BARBED_WIRE.get(), ModItems.OIL_DRUM.get(),
                ModItems.GREEN_TARP.get(), ModItems.BLUE_TARP.get(), ModItems.GREEN_TARP_SLOPE.get(), ModItems.BLUE_TARP_SLOPE.get(),
                ModItems.SUPPLY_CRATE.get(), ModItems.WRECK_BODY.get(), ModItems.WRECK_PANEL.get(), ModItems.WRECK_WINDOW.get(),
                ModItems.WRECK_WHEEL.get(), ModItems.BEER_CANS.get(), ModItems.ANTLERS.get()));
        ModKit.items().forEach(item -> items.add(item.get()));
        return items;
    }

    /** Two zombies walk the same way, one through wire: it ends up behind and hurt, the other stays unharmed. */
    @GameTest(template = PLATFORM, timeoutTicks = 100)
    public static void barbedWireHurtsAndSlowsZombies(GameTestHelper helper) {
        for (int z = 2; z <= 5; z++) {
            helper.setBlock(new BlockPos(1, 2, z), ModBlocks.BARBED_WIRE.get());
        }
        RegularZombie wired = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(1.5, 2.0, 1.5));
        RegularZombie control = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(5.5, 2.0, 1.5));
        Vec3 wiredGoal = helper.absoluteVec(new Vec3(1.5, 2.0, 6.5));
        Vec3 controlGoal = helper.absoluteVec(new Vec3(5.5, 2.0, 6.5));
        // Not right away: a mob finds no path until it has landed.
        helper.runAfterDelay(3, () -> {
            wired.getNavigation().moveTo(wiredGoal.x, wiredGoal.y, wiredGoal.z, 1.0);
            control.getNavigation().moveTo(controlGoal.x, controlGoal.y, controlGoal.z, 1.0);
        });
        helper.runAfterDelay(33, () -> {
            if (control.getZ() - wired.getZ() < 1.0) {
                helper.fail("the zombie in the wire should fall behind: " + wired.getZ() + " vs " + control.getZ());
            }
            if (wired.getHealth() >= wired.getMaxHealth()) {
                helper.fail("the wire should hurt the zombie walking through it");
            }
            if (control.getHealth() < control.getMaxHealth()) {
                helper.fail("the zombie outside the wire got hurt");
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** Hurts players the same way, but only while they move. */
    @GameTest(template = PLATFORM)
    public static void barbedWireHurtsMovingPlayers(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.setBlock(new BlockPos(2, 2, 2), ModBlocks.BARBED_WIRE.get());
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), "wire_walker")) {
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
        BlockState wire = ModBlocks.BARBED_WIRE.get().defaultBlockState();
        player.xOld = player.getX();
        player.zOld = player.getZ();
        wire.entityInside(helper.getLevel(), pos, player);
        if (player.getHealth() < player.getMaxHealth()) {
            helper.fail("standing still in the wire shouldn't hurt");
        }
        player.xOld = player.getX() - 0.2;
        wire.entityInside(helper.getLevel(), pos, player);
        if (player.getMaxHealth() - player.getHealth() != BarbedWireBlock.DAMAGE) {
            helper.fail("walking through the wire should cost " + BarbedWireBlock.DAMAGE + ", health is " + player.getHealth());
        }
        helper.succeed();
    }

    /** Only lit drums are lights. */
    @GameTest(template = PLATFORM)
    public static void onlyLitDrumsGiveLight(GameTestHelper helper) {
        BlockState drum = ModBlocks.OIL_DRUM.get().defaultBlockState();
        if (drum.getLightEmission() != 0 || drum.setValue(OilDrumBlock.LIT, true).getLightEmission() != OilDrumBlock.LIGHT) {
            helper.fail("an unlit drum gives no light, a lit one " + OilDrumBlock.LIGHT);
        }
        helper.succeed();
    }

    /** Also proves every prop has its loot table; double slabs drop two. */
    @GameTest(template = PLATFORM)
    public static void propsDropThemselves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        for (Item item : propItems()) {
            Block block = Block.byItem(item);
            List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), level, pos, null);
            if (drops.size() != 1 || drops.get(0).getItem() != item) {
                helper.fail(item + " should drop itself, but drops " + drops);
            }
        }
        BlockState doubleSlab = ModBlocks.SANDBAG_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        if (Block.getDrops(doubleSlab, level, pos, null).get(0).getCount() != 2) {
            helper.fail("a double slab should drop two");
        }
        helper.succeed();
    }

    /** PROG-002: nobody can place or craft them until a stage unlocks them. */
    @GameTest(template = PLATFORM)
    public static void propsAreLockedAndUncraftable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID nobody = UUID.randomUUID();
        List<Item> items = propItems();
        for (Item item : items) {
            if (!ItemLocks.isGated(new ItemStack(item), nobody)) {
                helper.fail(item + " should be locked");
            }
        }
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            if (items.contains(recipe.getResultItem(level.registryAccess()).getItem())) {
                helper.fail("recipe " + recipe.getId() + " makes a prop");
            }
        }
        helper.succeed();
    }
}
