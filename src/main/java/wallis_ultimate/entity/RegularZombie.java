package wallis_ultimate.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import wallis_ultimate.registry.ModEffects;

/**
 * The regular zombie (ENEMY-012), our own type that replaces vanilla zombies in natural spawns (Q-131, Q-135).
 * Vanilla's zombie with our rules: slower than a walking player by day and a bit faster at night (ENEMY-001), only
 * the ones with Sunburn burn in daylight (ENEMY-002), never a baby (Q-133), and no block damage: no door breaking,
 * no turtle eggs, no turning into a drowned (ENEMY-005). Looks like a vanilla zombie until it gets its own model.
 */
public class RegularZombie extends Zombie {
    /** Share of spawns that get Sunburn (ENEMY-002). */
    public static final float SUNBURN_CHANCE = 0.15F;
    /** How far a walking player gets per tick on normal ground (4.317 m/s). */
    public static final double PLAYER_WALK_BLOCKS_PER_TICK = 0.21585;
    /** Movement speed attributes: 80% of a walking player's speed by day, 110% at night (Q-132). */
    public static final double DAY_SPEED = speedAttributeFor(0.8 * PLAYER_WALK_BLOCKS_PER_TICK);
    public static final double NIGHT_SPEED = speedAttributeFor(1.1 * PLAYER_WALK_BLOCKS_PER_TICK);
    /** Night is the part of the day when beds work in clear weather; thunderstorms don't count. */
    public static final long NIGHT_START = 12542;
    public static final long NIGHT_END = 23460;
    private static final AttributeModifier NIGHT_SPEED_BONUS = new AttributeModifier(
            UUID.fromString("d09917a7-31a5-4af0-98bc-212ba2c7dc80"), "Night speed", NIGHT_SPEED / DAY_SPEED - 1.0,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    public RegularZombie(EntityType<? extends RegularZombie> type, Level level) {
        super(type, level);
    }

    /**
     * On normal ground (friction 0.6 × 0.91 = 0.546) a mob settles at 0.98 × speed² / 0.454 blocks per tick: its
     * walk input (scaled by 0.98) and its acceleration both grow with the attribute. So speed buffs work on the
     * square, too: +25% ground speed needs about +11.8% on the attribute.
     */
    public static double speedAttributeFor(double blocksPerTick) {
        return Math.sqrt(blocksPerTick * 0.454 / 0.98);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, DAY_SPEED);
    }

    public static boolean isNight(Level level) {
        long time = level.getDayTime() % 24000L;
        return !level.dimensionType().hasFixedTime() && time >= NIGHT_START && time < NIGHT_END;
    }

    public boolean burnsInSunlight() {
        return hasEffect(ModEffects.SUNBURN.get());
    }

    /** Hidden: no particles and no icon, so players can't tell which zombies have it (Q-134). */
    public void giveSunburn() {
        addEffect(new MobEffectInstance(ModEffects.SUNBURN.get(), MobEffectInstance.INFINITE_DURATION, 0, false, false));
    }

    @Override
    protected void registerGoals() {
        // Vanilla's goals minus the one that tramples turtle eggs.
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        addBehaviourGoals();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        boolean night = isNight(level());
        if (night != speed.hasModifier(NIGHT_SPEED_BONUS)) {
            if (night) {
                speed.addTransientModifier(NIGHT_SPEED_BONUS);
            } else {
                speed.removeModifier(NIGHT_SPEED_BONUS);
            }
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        // Without group data, vanilla rolls for a baby and a chicken jockey.
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason,
                spawnData == null ? new ZombieGroupData(false, false) : spawnData, tag);
        if (getRandom().nextFloat() < SUNBURN_CHANCE) {
            giveSunburn();
        }
        return data;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    protected boolean isSunSensitive() {
        return burnsInSunlight();
    }

    @Override
    protected boolean supportsBreakDoorGoal() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    /** Vanilla zombie drops. */
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.ZOMBIE.getDefaultLootTable();
    }
}
