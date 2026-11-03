package wallis_ultimate.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * SURV-005: sleeping through the night fully heals (Q-123); leaving the bed early gives nothing. Each player owns
 * one placed bed per planet: a second one can't be placed while the first stands (Q-144). Beds placed by
 * structures (safe spots) belong to nobody, and creative players are exempt so they can build.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Beds {
    private static final long TICKS_PER_DAY = 24000L;
    static final long NIGHTFALL = 12000L;
    /** Hourglass speeds up the night instead of skipping it, so players wake up at dawn, before the day's tick 0. */
    static final long DAWN = 23000L;
    private static final String OWNED_BEDS = WallisUltimate.MOD_ID + ":beds";
    private static final String DIMENSION = "dimension";
    private static final String POS = "pos";

    private Beds() {
    }

    /**
     * Vanilla skips the night by setting the time to morning and then waking everyone with
     * (wakeImmediately = false, updateLevel = false). Getting out of bed yourself uses updateLevel = true.
     */
    public static boolean sleptThroughTheNight(boolean wakeImmediately, boolean updateLevel, long dayTime) {
        long time = dayTime % TICKS_PER_DAY;
        return !wakeImmediately && !updateLevel && (time < NIGHTFALL || time >= DAWN);
    }

    /** The planet a dimension belongs to. There's only Earth so far (the Overworld, its districts, the Nether). */
    public static String planetOf(ResourceKey<Level> dimension) {
        return "earth";
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()
                && sleptThroughTheNight(event.wakeImmediately(), event.updateLevel(), player.level().getDayTime())) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * Refuses the right-click before the bed item places anything. Cancelling the place event instead works too, but
     * undoing an already placed bed leaves its point of interest half registered ("POI data mismatch").
     */
    @SubscribeEvent
    public static void onUseBed(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getItemStack().getItem() instanceof BedItem && player.level() instanceof ServerLevel level
                && refuseSecondBed(player, level)) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    /** Backup for beds placed some other way. */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlacedBlock().getBlock() instanceof BedBlock)) {
            return;
        }
        if (refuseSecondBed(player, level)) {
            event.setCanceled(true);
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        String planet = planetOf(level.dimension());
        CompoundTag bed = new CompoundTag();
        bed.putString(DIMENSION, level.dimension().location().toString());
        bed.putLong(POS, event.getPos().asLong());
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag beds = persisted.getCompound(OWNED_BEDS);
        beds.put(planet, bed);
        persisted.put(OWNED_BEDS, beds);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /** True (and tells the player) if they may not place a bed now because their bed on this planet stands. */
    private static boolean refuseSecondBed(Player player, ServerLevel level) {
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        BlockPos owned = standingBed(player, level, planetOf(level.dimension()));
        if (owned == null) {
            return false;
        }
        player.sendSystemMessage(Component.translatable("message." + WallisUltimate.MOD_ID + ".bed.already_have",
                owned.getX(), owned.getY(), owned.getZ()));
        return true;
    }

    /** Where the player's bed on that planet stands, or null if they have none (or it was picked up or broken). */
    public static BlockPos standingBed(Player player, ServerLevel anyLevel, String planet) {
        // Forge only carries this part of a player's data over when they respawn, and a bed outlives its owner's death.
        CompoundTag bed = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(OWNED_BEDS).getCompound(planet);
        ResourceLocation dimension = ResourceLocation.tryParse(bed.getString(DIMENSION));
        if (bed.isEmpty() || dimension == null) {
            return null;
        }
        ServerLevel level = anyLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        BlockPos pos = BlockPos.of(bed.getLong(POS));
        return level != null && level.getBlockState(pos).getBlock() instanceof BedBlock ? pos : null;
    }
}
