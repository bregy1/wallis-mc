package wallis_ultimate.boss;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.HogFarmBuild;

/**
 * ENEMY-011: what makes the farm a sealed arena. The ring can't be broken, but players could still dig under it or
 * pillar over it, so nothing can be mined or placed anywhere on its footprint, at any height, in or out of a fight; the
 * fight also needs its posts and corn where they belong. Monsters don't spawn on it either, and the bell at the gate is
 * how a team that lost tries again.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HogFarmRules {
    private HogFarmRules() {
    }

    /** The farm at this place, or null. It only exists in the Overworld. */
    @Nullable
    private static HogFarm farmAt(LevelAccessor level, double x, double z) {
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.dimension() != Level.OVERWORLD) {
            return null;
        }
        HogFarm farm = HogFarm.of(serverLevel);
        return farm != null && farm.contains(x, z) ? farm : null;
    }

    private static boolean protects(LevelAccessor level, BlockPos pos, @Nullable Player player) {
        return (player == null || !player.isCreative()) && farmAt(level, pos.getX() + 0.5, pos.getZ() + 0.5) != null;
    }

    private static void refuse(Player player) {
        player.displayClientMessage(Component.translatable("message." + WallisUltimate.MOD_ID + ".hog_daddy.protected"), true);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (protects(event.getLevel(), event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            refuse(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && protects(event.getLevel(), event.getPos(), player)) {
            event.setCanceled(true);
            refuse(player);
        }
    }

    /** A water column would carry a player over the ring just as well as a pillar. */
    @SubscribeEvent
    public static void onBucket(FillBucketEvent event) {
        if (event.getTarget() != null && farmAt(event.getLevel(), event.getTarget().getLocation().x, event.getTarget().getLocation().z) != null
                && !event.getEntity().isCreative()) {
            event.setCanceled(true);
            refuse(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> protects(event.getLevel(), pos, null));
    }

    @SubscribeEvent
    public static void onSpawnCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof Enemy && farmAt(event.getLevel(), event.getX(), event.getZ()) != null) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * The quest only starts the fight once (ENEMY-011, the seam with the story), so a team that lost needs another way
     * in. Only for a team that has already called him, so it can't be used to skip ahead in the story.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        HogFarm farm = farmAt(event.getLevel(), event.getPos().getX() + 0.5, event.getPos().getZ() + 0.5);
        if (farm != null && event.getPos().equals(farm.at(HogFarmBuild.BELL))
                && HogDaddyProgress.get(player.server.overworld()).hasCalled(Teams.teamKey(player))) {
            HogDaddyFight.start(player);
        }
    }
}
