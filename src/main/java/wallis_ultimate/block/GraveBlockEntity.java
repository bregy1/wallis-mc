package wallis_ultimate.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlockEntities;

/**
 * SURV-004: holds a dead player's items. Only the owner can open it. It removes itself as soon as it's empty,
 * and after 60 minutes (world time) it disappears together with everything in it.
 * The block entity is deliberately not a Container and has no item capability, so hoppers and pipes can't empty it.
 */
public class GraveBlockEntity extends BlockEntity {
    public static final int SIZE = 54;
    public static final long LIFETIME_TICKS = 60L * 60L * 20L;

    private final SimpleContainer items = new SimpleContainer(SIZE) {
        @Override
        public boolean stillValid(Player player) {
            return isOwner(player) && Container.stillValidBlockEntity(GraveBlockEntity.this, player);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            GraveBlockEntity.this.setChanged();
        }
    };
    private UUID owner;
    private String ownerName = "";
    private long createdAt;

    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVE.get(), pos, state);
    }

    public void setOwner(Player player, long gameTime) {
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        createdAt = gameTime;
        setChanged();
    }

    public boolean isOwner(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    /** Moves the stacks into the grave and returns what didn't fit. */
    public List<ItemStack> addItems(List<ItemStack> stacks) {
        List<ItemStack> leftover = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack rest = items.addItem(stack);
            if (!rest.isEmpty()) {
                leftover.add(rest);
            }
        }
        return leftover;
    }

    public Container items() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public long createdAt() {
        return createdAt;
    }

    /** For GameTests: pretend the grave was made at another time. */
    public void setCreatedAt(long gameTime) {
        createdAt = gameTime;
        setChanged();
    }

    public MenuProvider menuProvider() {
        return new SimpleMenuProvider((id, inventory, player) -> ChestMenu.sixRows(id, inventory, items),
                Component.translatable("container." + WallisUltimate.MOD_ID + ".grave", ownerName));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GraveBlockEntity grave) {
        if (grave.isEmpty() || level.getGameTime() - grave.createdAt >= LIFETIME_TICKS) {
            grave.items.clearContent(); // an expired grave takes its items with it
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) {
            stacks.set(i, items.getItem(i));
        }
        ContainerHelper.saveAllItems(tag, stacks);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putString("OwnerName", ownerName);
        tag.putLong("CreatedAt", createdAt);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks);
        for (int i = 0; i < SIZE; i++) {
            items.setItem(i, stacks.get(i));
        }
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerName = tag.getString("OwnerName");
        createdAt = tag.getLong("CreatedAt");
    }
}
