package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.GraveBlockEntity;
import wallis_ultimate.block.GraveMenu;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.survival.Graves;

/** Checks for SURV-004: graves keep a dead player's items. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GraveGameTests {
    private static final String PLATFORM = "platform";

    private GraveGameTests() {
    }

    /** Relative y=1 is the platform's stone floor; stand at y=2. */
    private static Player mockPlayer(GameTestHelper helper, String name, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
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

    private static List<ItemStack> loot() {
        return new ArrayList<>(List.of(new ItemStack(Items.IRON_SWORD), new ItemStack(Items.BREAD, 12), new ItemStack(Items.COBBLESTONE, 64)));
    }

    private static GraveBlockEntity graveAt(GameTestHelper helper, BlockPos absolutePos) {
        if (!(helper.getLevel().getBlockEntity(absolutePos) instanceof GraveBlockEntity grave)) {
            throw new IllegalStateException("no grave at " + absolutePos);
        }
        return grave;
    }

    private static int countItems(GraveBlockEntity grave) {
        int count = 0;
        for (int i = 0; i < grave.items().getContainerSize(); i++) {
            count += grave.items().getItem(i).getCount();
        }
        return count;
    }

    @GameTest(template = PLATFORM)
    public static void deathDropsGoIntoAGraveOnlyTheOwnerCanOpen(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_owner", new BlockPos(2, 2, 2));
        Player stranger = mockPlayer(helper, "gv_stranger", new BlockPos(3, 2, 2));
        List<ItemEntity> drops = new ArrayList<>();
        for (ItemStack stack : loot()) {
            drops.add(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), stack));
        }
        LivingDropsEvent event = new LivingDropsEvent(owner, helper.getLevel().damageSources().generic(), drops, 0, false);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.getDrops().isEmpty()) {
            helper.fail("items still drop: " + event.getDrops().size());
        }
        helper.assertBlockPresent(ModBlocks.GRAVE.get(), new BlockPos(2, 2, 2));
        GraveBlockEntity grave = graveAt(helper, helper.absolutePos(new BlockPos(2, 2, 2)));
        if (countItems(grave) != 1 + 12 + 64) {
            helper.fail("grave holds " + countItems(grave) + " items");
        }
        if (!grave.isOwner(owner) || grave.isOwner(stranger)) {
            helper.fail("wrong owner");
        }
        if (!grave.items().stillValid(owner) || grave.items().stillValid(stranger)) {
            helper.fail("the wrong player can open the grave");
        }
        helper.getLevel().removeBlock(grave.getBlockPos(), false);
        helper.succeed();
    }

    /** Q-122: a chat message tells the dead player where their grave is. */
    @GameTest(template = PLATFORM)
    public static void theOwnerIsToldWhereTheGraveIs(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        List<Component> messages = new ArrayList<>();
        Player owner = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), "gv_told")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }

            @Override
            public void sendSystemMessage(Component message) {
                messages.add(message);
            }
        };
        owner.moveTo(Vec3.atBottomCenterOf(pos));
        List<ItemEntity> drops = new ArrayList<>(List.of(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), new ItemStack(Items.BREAD))));
        MinecraftForge.EVENT_BUS.post(new LivingDropsEvent(owner, helper.getLevel().damageSources().generic(), drops, 0, false));
        String expected = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        if (messages.stream().noneMatch(message -> message.getString().contains(expected))) {
            helper.fail("no message with " + expected + ": " + messages.stream().map(Component::getString).toList());
        }
        helper.getLevel().removeBlock(pos, false);
        helper.succeed();
    }

    /** Q-122: items can be taken out of a grave, but nothing can be put in, by any kind of click. */
    @GameTest(template = PLATFORM)
    public static void gravesAreTakeOnly(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_takeonly", new BlockPos(6, 2, 6));
        Graves.Result result = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), loot());
        GraveBlockEntity grave = graveAt(helper, result.graves().get(0));
        AbstractContainerMenu menu = grave.menuProvider().createMenu(1, owner.getInventory(), owner);
        if (!(menu instanceof GraveMenu)) {
            helper.fail("the grave opens " + menu);
        }
        int before = countItems(grave);
        int emptyGraveSlot = GraveBlockEntity.SIZE - 1;
        int firstInventorySlot = GraveBlockEntity.SIZE;
        owner.getInventory().setItem(9, new ItemStack(Items.DIRT, 10));
        // Shift-click from the inventory, clicking a carried stack in, and swapping in with a number key.
        menu.clicked(firstInventorySlot, 0, ClickType.QUICK_MOVE, owner);
        menu.setCarried(new ItemStack(Items.STONE, 5));
        menu.clicked(emptyGraveSlot, 0, ClickType.PICKUP, owner);
        owner.getInventory().setItem(0, new ItemStack(Items.GRAVEL, 3));
        menu.clicked(emptyGraveSlot, 0, ClickType.SWAP, owner);
        if (countItems(grave) != before || !grave.items().getItem(emptyGraveSlot).isEmpty()) {
            helper.fail("items went into the grave: " + countItems(grave) + " instead of " + before);
        }
        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, owner);
        if (countItems(grave) >= before) {
            helper.fail("taking an item out didn't work");
        }
        helper.getLevel().removeBlock(grave.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void lavaAndVoidGravesGoToTheLastSafeSpot(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_lava", new BlockPos(5, 2, 5));
        Graves.rememberSafeSpot(owner);
        owner.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))));
        Graves.Result lava = Graves.placeGraves(owner, helper.getLevel().damageSources().lava(), loot());
        if (!lava.graves().equals(List.of(helper.absolutePos(new BlockPos(5, 2, 5))))) {
            helper.fail("lava grave at " + lava.graves());
        }
        owner.moveTo(owner.getX(), helper.getLevel().getMinBuildHeight() - 10, owner.getZ());
        Graves.Result voidGrave = Graves.placeGraves(owner, helper.getLevel().damageSources().fellOutOfWorld(), loot());
        // The safe spot itself is taken by the lava grave, so the void grave goes right next to it.
        if (voidGrave.graves().size() != 1 || voidGrave.graves().get(0).distSqr(helper.absolutePos(new BlockPos(5, 2, 5))) > 2) {
            helper.fail("void grave at " + voidGrave.graves());
        }
        lava.graves().forEach(pos -> helper.getLevel().removeBlock(pos, false));
        voidGrave.graves().forEach(pos -> helper.getLevel().removeBlock(pos, false));
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void bigInventoriesGetSeveralGraves(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_hoarder", new BlockPos(3, 2, 3));
        List<ItemStack> swords = new ArrayList<>();
        for (int i = 0; i < GraveBlockEntity.SIZE + 6; i++) {
            swords.add(new ItemStack(Items.WOODEN_SWORD));
        }
        Graves.Result result = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), swords);
        if (result.graves().size() != 2 || !result.leftovers().isEmpty()) {
            helper.fail("expected 2 graves and nothing left over, got " + result.graves().size() + " and " + result.leftovers().size());
        }
        int total = result.graves().stream().mapToInt(pos -> countItems(graveAt(helper, pos))).sum();
        if (total != GraveBlockEntity.SIZE + 6) {
            helper.fail("graves hold " + total + " swords");
        }
        result.graves().forEach(pos -> helper.getLevel().removeBlock(pos, false));
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20)
    public static void emptyGravesDisappear(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_empty", new BlockPos(2, 2, 2));
        BlockPos pos = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), List.of(new ItemStack(Items.APPLE))).graves().get(0);
        graveAt(helper, pos).items().removeItemNoUpdate(0);
        helper.succeedWhen(() -> helper.assertBlockNotPresent(ModBlocks.GRAVE.get(), new BlockPos(2, 2, 2)));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20)
    public static void gravesVanishWithTheirItemsAfterAnHour(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_old", new BlockPos(2, 2, 2));
        BlockPos pos = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), loot()).graves().get(0);
        GraveBlockEntity grave = graveAt(helper, pos);
        grave.setCreatedAt(helper.getLevel().getGameTime() - GraveBlockEntity.LIFETIME_TICKS + 5);
        helper.runAfterDelay(1, () -> helper.assertBlockPresent(ModBlocks.GRAVE.get(), new BlockPos(2, 2, 2)));
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(ModBlocks.GRAVE.get(), new BlockPos(2, 2, 2));
            if (!helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3.0)).isEmpty()) {
                helper.fail("an expired grave dropped its items");
            }
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40)
    public static void hoppersCantEmptyAGrave(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        Player owner = mockPlayer(helper, "gv_hopper", new BlockPos(2, 3, 2));
        BlockPos pos = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), loot()).graves().get(0);
        if (!pos.equals(helper.absolutePos(new BlockPos(2, 3, 2)))) {
            helper.fail("grave not above the hopper: " + pos);
        }
        helper.runAfterDelay(30, () -> {
            if (countItems(graveAt(helper, pos)) != 1 + 12 + 64) {
                helper.fail("the hopper took items out of the grave");
            }
            if (helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(2, 2, 2))) instanceof HopperBlockEntity hopper && !hopper.isEmpty()) {
                helper.fail("the hopper holds items");
            }
            helper.getLevel().removeBlock(pos, false);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "explosions")
    public static void gravesSurviveExplosionsAndCantBeMined(GameTestHelper helper) {
        Player owner = mockPlayer(helper, "gv_boom", new BlockPos(3, 2, 3));
        BlockPos pos = Graves.placeGraves(owner, helper.getLevel().damageSources().generic(), loot()).graves().get(0);
        if (helper.getLevel().getBlockState(pos).getDestroySpeed(helper.getLevel(), pos) >= 0.0F) {
            helper.fail("graves can be mined");
        }
        BlockPos blast = helper.absolutePos(new BlockPos(4, 2, 3));
        helper.getLevel().explode(null, blast.getX() + 0.5, blast.getY() + 0.5, blast.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
        helper.assertBlockPresent(ModBlocks.GRAVE.get(), new BlockPos(3, 2, 3));
        if (countItems(graveAt(helper, pos)) != 1 + 12 + 64) {
            helper.fail("the explosion changed the grave's items");
        }
        helper.getLevel().removeBlock(pos, false);
        helper.succeed();
    }
}
