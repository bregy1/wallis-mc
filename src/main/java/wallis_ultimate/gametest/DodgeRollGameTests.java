package wallis_ultimate.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.DodgeRoll;

/** Checks for CMBT-002: the forward dodge roll (server logic). */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DodgeRollGameTests {
    private static final String PLATFORM = "platform";

    private DodgeRollGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void rollNeedsGroundAndRespectsCooldown(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setOnGround(false);
        if (DodgeRoll.tryRoll(player)) {
            helper.fail("rolled in mid-air");
        }
        player.setOnGround(true);
        if (!DodgeRoll.tryRoll(player)) {
            helper.fail("could not roll on the ground");
        }
        Vec3 motion = player.getDeltaMovement();
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (Math.abs(horizontal - DodgeRoll.ROLL_SPEED) > 0.001) {
            helper.fail("roll speed was " + horizontal);
        }
        if (DodgeRoll.tryRoll(player)) {
            helper.fail("rolled again during the cooldown");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60)
    public static void rollGivesBriefDamageImmunity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setOnGround(true);
        DodgeRoll.tryRoll(player);
        float before = player.getHealth();
        player.hurt(helper.getLevel().damageSources().generic(), 4.0F);
        if (player.getHealth() != before) {
            helper.fail("took damage during the roll");
        }
        helper.runAfterDelay(DodgeRoll.IMMUNITY_TICKS + 2, () -> {
            player.hurt(helper.getLevel().damageSources().generic(), 4.0F);
            if (player.getHealth() >= before) {
                helper.fail("still immune after the roll");
            }
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM)
    public static void rollCostsASprintJumpOfHunger(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setOnGround(true);
        float before = player.getFoodData().getExhaustionLevel();
        DodgeRoll.tryRoll(player);
        float spent = player.getFoodData().getExhaustionLevel() - before;
        if (Math.abs(spent - DodgeRoll.EXHAUSTION) > 0.0001F) {
            helper.fail("exhaustion changed by " + spent);
        }
        helper.succeed();
    }
}
