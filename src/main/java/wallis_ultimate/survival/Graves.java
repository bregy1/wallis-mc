package wallis_ultimate.survival;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.GraveBlockEntity;
import wallis_ultimate.registry.ModBlocks;

/**
 * SURV-004: on death, a player's items go into a grave instead of dropping. After a death in lava or the void
 * the grave goes to the last safe spot the player stood on. If no grave can be placed, items drop as in vanilla.
 * The player is told where their grave is (Q-122).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Graves {
    private static final String SAFE_POS = WallisUltimate.MOD_ID + ":last_safe_pos";
    private static final String SAFE_DIMENSION = WallisUltimate.MOD_ID + ":last_safe_dimension";
    private static final int SEARCH_UP = 8;
    private static final int SEARCH_RADIUS = 2;

    private Graves() {
    }

    /** Remembers where the player stands as their last safe spot. */
    public static void rememberSafeSpot(Player player) {
        CompoundTag data = player.getPersistentData();
        data.putLong(SAFE_POS, player.blockPosition().asLong());
        data.putString(SAFE_DIMENSION, player.level().dimension().location().toString());
    }

    public static boolean diedInLavaOrVoid(Player player, DamageSource source) {
        return source.is(DamageTypes.LAVA) || source.is(DamageTypes.FELL_OUT_OF_WORLD) || player.isInLava()
                || player.getY() < player.level().getMinBuildHeight();
    }

    /** Where the graves went, and the stacks that found no room in any grave. */
    public record Result(List<BlockPos> graves, List<ItemStack> leftovers) {
    }

    /**
     * Places as many graves as the stacks need (54 stacks each) near the death spot, or near the last safe spot
     * after a death in lava or the void.
     */
    public static Result placeGraves(Player owner, DamageSource source, List<ItemStack> stacks) {
        ServerLevel level = (ServerLevel) owner.level();
        BlockPos start = owner.blockPosition();
        CompoundTag data = owner.getPersistentData();
        if (diedInLavaOrVoid(owner, source) && data.contains(SAFE_POS)) {
            ResourceLocation dimension = ResourceLocation.tryParse(data.getString(SAFE_DIMENSION));
            ServerLevel safeLevel = dimension == null ? null : level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
            if (safeLevel != null) {
                level = safeLevel;
                start = BlockPos.of(data.getLong(SAFE_POS));
            }
        }
        List<BlockPos> placed = new ArrayList<>();
        List<ItemStack> remaining = stacks;
        while (!remaining.isEmpty()) {
            BlockPos pos = findGravePos(level, start);
            if (pos == null || !level.setBlock(pos, ModBlocks.GRAVE.get().defaultBlockState(), 3)
                    || !(level.getBlockEntity(pos) instanceof GraveBlockEntity grave)) {
                break;
            }
            grave.setOwner(owner, level.getGameTime());
            remaining = grave.addItems(remaining);
            placed.add(pos);
        }
        return new Result(placed, remaining);
    }

    /** The free spot closest to {@code start}, searching a little around and upward; null if there's none. */
    static BlockPos findGravePos(Level level, BlockPos start) {
        int y = Math.max(level.getMinBuildHeight(), Math.min(level.getMaxBuildHeight() - 1, start.getY()));
        BlockPos origin = new BlockPos(start.getX(), y, start.getZ());
        for (int dy = 0; dy <= SEARCH_UP; dy++) {
            BlockPos best = null;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RADIUS, dy, -SEARCH_RADIUS), origin.offset(SEARCH_RADIUS, dy, SEARCH_RADIUS))) {
                if (isFree(level, pos) && (best == null || pos.distSqr(origin) < best.distSqr(origin))) {
                    best = pos.immutable();
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private static boolean isFree(Level level, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && !state.hasBlockEntity() && !level.getFluidState(pos).is(FluidTags.LAVA);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide() && player.onGround()
                && !player.isInLava() && !player.isSpectator()) {
            rememberSafeSpot(player);
        }
    }

    /** Late, so changes other mods make to the drops end up in the grave too. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || event.getDrops().isEmpty()) {
            return;
        }
        List<ItemStack> stacks = event.getDrops().stream().map(ItemEntity::getItem).filter(stack -> !stack.isEmpty()).toList();
        Result result = placeGraves(player, event.getSource(), stacks);
        if (result.graves().isEmpty()) {
            WallisUltimate.LOGGER.warn("No room for a grave for {} at {}; items drop instead", player.getGameProfile().getName(), player.blockPosition());
            return;
        }
        BlockPos grave = result.graves().get(0);
        player.sendSystemMessage(Component.translatable("message." + WallisUltimate.MOD_ID + ".grave.location",
                grave.getX(), grave.getY(), grave.getZ()));
        // Whatever didn't fit drops as in vanilla.
        event.getDrops().clear();
        for (ItemStack stack : result.leftovers()) {
            event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
        }
    }
}
