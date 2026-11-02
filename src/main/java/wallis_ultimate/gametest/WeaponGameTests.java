package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.Noise;
import wallis_ultimate.combat.WeaponEffects;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;

/** Checks for GEAR-007 (melee weapons) and the noise part of ENEMY-006. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponGameTests {
    private static final String PLATFORM = "platform";

    private WeaponGameTests() {
    }

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

    private static double modifier(Item item, Attribute attribute) {
        return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @GameTest(template = PLATFORM)
    public static void weaponsHaveTheirStats(GameTestHelper helper) {
        // The modifiers add to a bare hand's 1 damage and 4 attacks per second.
        Item bat = ModItems.NAIL_BAT.get();
        if (modifier(bat, Attributes.ATTACK_DAMAGE) != 4.0 || Math.abs(modifier(bat, Attributes.ATTACK_SPEED) + 2.6) > 0.001) {
            helper.fail("Nail Bat: damage " + modifier(bat, Attributes.ATTACK_DAMAGE) + ", speed " + modifier(bat, Attributes.ATTACK_SPEED));
        }
        Item fork = ModItems.PITCHFORK.get();
        if (modifier(fork, ForgeMod.ENTITY_REACH.get()) != 1.5) {
            helper.fail("Pitchfork reach " + modifier(fork, ForgeMod.ENTITY_REACH.get()));
        }
        helper.succeed();
    }

    /** The Nail Bat makes its target bleed, which keeps hurting. */
    @GameTest(template = PLATFORM, timeoutTicks = 60)
    public static void theNailBatCausesBleeding(GameTestHelper helper) {
        Player player = mockPlayer(helper, "wp_bat", new BlockPos(1, 2, 1));
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(5, 2, 5));
        ItemStack bat = new ItemStack(ModItems.NAIL_BAT.get());
        bat.hurtEnemy(zombie, player);
        if (!zombie.hasEffect(ModEffects.BLEEDING.get())) {
            helper.fail("no Bleeding");
        }
        float health = zombie.getHealth();
        helper.runAfterDelay(45, () -> {
            if (zombie.getHealth() >= health) {
                helper.fail("Bleeding did no damage: " + zombie.getHealth());
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** The Heavy Shovel stuns on a hit and still digs. */
    @GameTest(template = PLATFORM)
    public static void theHeavyShovelStunsAndDigs(GameTestHelper helper) {
        Player player = mockPlayer(helper, "wp_shovel", new BlockPos(1, 2, 1));
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(5, 2, 5));
        ItemStack shovel = new ItemStack(ModItems.HEAVY_SHOVEL.get());
        shovel.hurtEnemy(zombie, player);
        if (!zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            helper.fail("no stun");
        }
        float speed = shovel.getDestroySpeed(Blocks.DIRT.defaultBlockState());
        if (speed != new ItemStack(Items.STONE_SHOVEL).getDestroySpeed(Blocks.DIRT.defaultBlockState())) {
            helper.fail("digs dirt at " + speed);
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Pitchfork also stabs the enemy right behind its target, but not one off to the side. */
    @GameTest(template = PLATFORM)
    public static void thePitchforkHitsTwoInALine(GameTestHelper helper) {
        Player player = mockPlayer(helper, "wp_fork", new BlockPos(1, 2, 3));
        RegularZombie target = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(3, 2, 3));
        RegularZombie behind = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(4, 2, 3));
        RegularZombie aside = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(3, 2, 7));
        new ItemStack(ModItems.PITCHFORK.get()).hurtEnemy(target, player);
        if (behind.getHealth() >= behind.getMaxHealth()) {
            helper.fail("the enemy behind wasn't hit");
        }
        if (aside.getHealth() < aside.getMaxHealth()) {
            helper.fail("the enemy off to the side was hit");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Frying Pan blocks one hit from the front, then needs a moment; hits from behind get through. */
    @GameTest(template = PLATFORM)
    public static void theFryingPanBlocksOneHitFromTheFront(GameTestHelper helper) {
        Player player = mockPlayer(helper, "wp_pan", new BlockPos(3, 2, 3));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.FRYING_PAN.get()));
        // Yaw 0 looks toward +z.
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        RegularZombie front = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(3, 2, 6));
        RegularZombie back = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(3, 2, 1));
        if (attack(helper, player, back)) {
            helper.fail("blocked a hit from behind");
        }
        if (!attack(helper, player, front)) {
            helper.fail("didn't block a hit from the front");
        }
        if (attack(helper, player, front)) {
            helper.fail("blocked again during the cooldown");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    private static boolean attack(GameTestHelper helper, Player player, RegularZombie zombie) {
        LivingAttackEvent event = new LivingAttackEvent(player, helper.getLevel().damageSources().mobAttack(zombie), 5.0F);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    /** ENEMY-006: zombies in earshot walk to the noise, others don't hear it. */
    @GameTest(template = PLATFORM, timeoutTicks = 100)
    public static void zombiesWalkToNoises(GameTestHelper helper) {
        RegularZombie near = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(1, 2, 1));
        near.setPersistenceRequired();
        Vec3 noise = helper.absoluteVec(new Vec3(6.5, 2, 6.5));
        double before = near.position().distanceTo(noise);
        Noise.make(helper.getLevel(), noise, 12.0);
        Noise.make(helper.getLevel(), noise.add(100, 0, 0), 12.0);
        if (near.heardNoise() == null) {
            helper.fail("the zombie didn't hear the noise");
        }
        helper.runAfterDelay(60, () -> {
            double after = near.position().distanceTo(noise);
            if (after > before - 2.0) {
                helper.fail("the zombie didn't walk toward the noise: " + before + " to " + after);
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
