package wallis_ultimate.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEntities;

/** Rules for {@link RegularZombie} that vanilla handles outside the zombie class. */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegularZombieEvents {
    private RegularZombieEvents() {
    }

    /** ENEMY-005: regular zombies don't damage blocks, so they don't trample farmland either. */
    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof RegularZombie) {
            event.setCanceled(true);
        }
    }

    /**
     * Vanilla's reinforcements (hard difficulty, when hurt) would be vanilla zombies. Same odds as vanilla, but the
     * reinforcement is one of ours.
     */
    @SubscribeEvent
    public static void onSummonAid(ZombieEvent.SummonAidEvent event) {
        if (!(event.getEntity() instanceof RegularZombie zombie)) {
            return;
        }
        Level level = event.getLevel();
        if (event.getAttacker() != null && level.getDifficulty() == Difficulty.HARD
                && zombie.getRandom().nextFloat() < event.getSummonChance()
                && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            event.setCustomSummonedAid(ModEntities.ZOMBIE.get().create(level));
            event.setResult(Event.Result.ALLOW);
        } else {
            event.setResult(Event.Result.DENY);
        }
    }
}
