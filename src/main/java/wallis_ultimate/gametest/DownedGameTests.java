package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.DodgeRoll;
import wallis_ultimate.survival.Downed;
import wallis_ultimate.survival.Revive;

/** Checks for SURV-001 (downed state) and SURV-002 (revive), server logic. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DownedGameTests {
    private static final String PLATFORM = "platform";
    private static final float LETHAL = 1000.0F;

    private DownedGameTests() {
    }

    /**
     * A mock player with its own name, so scoreboard teams can tell players apart. Not added to the level, so it isn't
     * ticked. Relative y=1 is the platform's stone floor; stand at y=2.
     */
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

    private static PlayerTeam team(GameTestHelper helper, String name, Player... members) {
        Scoreboard scoreboard = helper.getLevel().getScoreboard();
        PlayerTeam old = scoreboard.getPlayerTeam(name);
        if (old != null) {
            scoreboard.removePlayerTeam(old);
        }
        PlayerTeam team = scoreboard.addPlayerTeam(name);
        for (Player member : members) {
            scoreboard.addPlayerToTeam(member.getScoreboardName(), team);
        }
        return team;
    }

    private static void removeTeam(GameTestHelper helper, PlayerTeam team) {
        helper.getLevel().getScoreboard().removePlayerTeam(team);
    }

    private static void knockDown(GameTestHelper helper, Player player) {
        player.hurt(helper.getLevel().damageSources().generic(), LETHAL);
        if (!Downed.isDowned(player)) {
            helper.fail("lethal damage did not down " + player.getScoreboardName());
        }
    }

    @GameTest(template = PLATFORM)
    public static void lethalDamageDownsThePlayer(GameTestHelper helper) {
        Player player = mockPlayer(helper, "dn_down", new BlockPos(2, 2, 2));
        knockDown(helper, player);
        if (player.isDeadOrDying()) {
            helper.fail("downed player is dead");
        }
        if (Downed.ticksLeft(player) != Downed.DOWNED_TICKS || Downed.ammo(player) != Downed.PISTOL_SHOTS) {
            helper.fail("downed with " + Downed.ticksLeft(player) + " ticks and " + Downed.ammo(player) + " shots");
        }
        if (!player.hasEffect(MobEffects.GLOWING)) {
            helper.fail("downed player is not marked");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void voidLavaAndKillAreInstant(GameTestHelper helper) {
        Player voidPlayer = mockPlayer(helper, "dn_void", new BlockPos(1, 2, 1));
        voidPlayer.hurt(helper.getLevel().damageSources().fellOutOfWorld(), LETHAL);
        Player lavaPlayer = mockPlayer(helper, "dn_lava", new BlockPos(3, 2, 1));
        lavaPlayer.hurt(helper.getLevel().damageSources().lava(), LETHAL);
        Player killedPlayer = mockPlayer(helper, "dn_kill", new BlockPos(5, 2, 1));
        killedPlayer.kill();
        for (Player player : new Player[] {voidPlayer, lavaPlayer, killedPlayer}) {
            if (Downed.isDowned(player) || !player.isDeadOrDying()) {
                helper.fail(player.getScoreboardName() + " went down instead of dying");
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void downedPlayersTakeNoDamageAndAreIgnored(GameTestHelper helper) {
        Player player = mockPlayer(helper, "dn_ignored", new BlockPos(2, 2, 2));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        zombie.setNoAi(true);
        try {
            zombie.setTarget(player);
            if (zombie.getTarget() != player) {
                helper.fail("setup: zombie did not target the player");
            }
            knockDown(helper, player);
            if (zombie.getTarget() != null) {
                helper.fail("zombie kept its downed target");
            }
            zombie.setTarget(player);
            if (zombie.getTarget() != null) {
                helper.fail("zombie could target a downed player");
            }
            player.hurt(helper.getLevel().damageSources().mobAttack(zombie), 4.0F);
            if (player.getHealth() != 1.0F) {
                helper.fail("downed player took damage, health " + player.getHealth());
            }
        } finally {
            zombie.discard();
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void downedTimerEndsInDeath(GameTestHelper helper) {
        Player player = mockPlayer(helper, "dn_timer", new BlockPos(2, 2, 2));
        knockDown(helper, player);
        for (int i = 0; i < Downed.DOWNED_TICKS - 1; i++) {
            Downed.tickDowned(player);
        }
        if (!Downed.isDowned(player) || player.isDeadOrDying()) {
            helper.fail("died before 90 seconds were up");
        }
        Downed.tickDowned(player);
        if (Downed.isDowned(player) || !player.isDeadOrDying()) {
            helper.fail("still alive after 90 seconds");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void givingUpKills(GameTestHelper helper) {
        Player standing = mockPlayer(helper, "dn_standing", new BlockPos(1, 2, 1));
        Downed.giveUp(standing);
        if (standing.isDeadOrDying()) {
            helper.fail("a player who isn't downed could give up");
        }
        Player player = mockPlayer(helper, "dn_giveup", new BlockPos(3, 2, 3));
        knockDown(helper, player);
        Downed.giveUp(player);
        if (Downed.isDowned(player) || !player.isDeadOrDying()) {
            helper.fail("giving up did not kill");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void pistolHasTenShotsAndHits(GameTestHelper helper) {
        Player player = mockPlayer(helper, "dn_pistol", new BlockPos(1, 2, 1));
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        pig.setNoAi(true);
        try {
            if (Downed.firePistol(player) != null || Downed.ammo(player) != 0) {
                helper.fail("pistol fired while standing");
            }
            knockDown(helper, player);
            player.lookAt(EntityAnchorArgument.Anchor.EYES, pig.position().add(0.0, pig.getBbHeight() / 2.0, 0.0));
            LivingEntity hit = Downed.firePistol(player);
            if (hit != pig) {
                helper.fail("pistol missed the pig, hit " + hit);
            }
            if (Math.abs(pig.getHealth() - (pig.getMaxHealth() - Downed.PISTOL_DAMAGE)) > 0.01F) {
                helper.fail("pig health after one shot: " + pig.getHealth());
            }
            for (int shot = 2; shot <= Downed.PISTOL_SHOTS; shot++) {
                Downed.firePistol(player);
            }
            if (Downed.ammo(player) != 0) {
                helper.fail("ammo left after ten shots: " + Downed.ammo(player));
            }
            if (Downed.firePistol(player) != null || Downed.ammo(player) != 0) {
                helper.fail("fired an eleventh shot");
            }
        } finally {
            pig.discard();
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 120)
    public static void reviveTakesThreeAndAHalfSecondsAndGivesHalfHealth(GameTestHelper helper) {
        Player reviver = mockPlayer(helper, "rv_medic", new BlockPos(2, 2, 2));
        Player target = mockPlayer(helper, "rv_patient", new BlockPos(3, 2, 2));
        PlayerTeam team = team(helper, "wu_t_revive", reviver, target);
        knockDown(helper, target);
        double speed = reviver.getAttributeValue(Attributes.MOVEMENT_SPEED);
        helper.onEachTick(() -> {
            if (Downed.isDowned(target)) {
                Revive.hold(reviver, target);
                Revive.hold(reviver, target); // a second report in the same tick must not count twice
            }
        });
        helper.runAfterDelay(Revive.REVIVE_TICKS - 3, () -> {
            if (!Downed.isDowned(target)) {
                helper.fail("revived too early");
            }
            if (reviver.getAttributeValue(Attributes.MOVEMENT_SPEED) != 0.0) {
                helper.fail("reviver can move while reviving");
            }
        });
        helper.runAfterDelay(Revive.REVIVE_TICKS + 3, () -> {
            removeTeam(helper, team);
            if (Downed.isDowned(target)) {
                helper.fail("not revived after 3.5 seconds");
            }
            if (Math.abs(target.getHealth() - target.getMaxHealth() / 2.0F) > 0.01F) {
                helper.fail("revived with " + target.getHealth() + " health");
            }
            if (Revive.isReviving(reviver) || reviver.getAttributeValue(Attributes.MOVEMENT_SPEED) != speed) {
                helper.fail("reviver still frozen after the revive");
            }
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM)
    public static void damageCancelsTheRevive(GameTestHelper helper) {
        Player reviver = mockPlayer(helper, "rv_hurt", new BlockPos(2, 2, 2));
        Player target = mockPlayer(helper, "rv_waiting", new BlockPos(3, 2, 2));
        PlayerTeam team = team(helper, "wu_t_hurt", reviver, target);
        try {
            knockDown(helper, target);
            Revive.hold(reviver, target);
            if (!Revive.isReviving(reviver) || Revive.progress(reviver) != 1) {
                helper.fail("revive did not start");
            }
            reviver.hurt(helper.getLevel().damageSources().generic(), 1.0F);
            if (Revive.isReviving(reviver) || Revive.progress(reviver) != 0) {
                helper.fail("revive went on after the reviver was hurt");
            }
            if (reviver.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.0) {
                helper.fail("reviver still frozen after the revive was cancelled");
            }
        } finally {
            removeTeam(helper, team);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void onlyStandingTeammatesInRangeCanRevive(GameTestHelper helper) {
        Player target = mockPlayer(helper, "rv_alone", new BlockPos(1, 2, 1));
        Player stranger = mockPlayer(helper, "rv_stranger", new BlockPos(2, 2, 1));
        Player downedMate = mockPlayer(helper, "rv_alsodown", new BlockPos(1, 2, 2));
        Player farMate = mockPlayer(helper, "rv_far", new BlockPos(6, 2, 6));
        PlayerTeam team = team(helper, "wu_t_only", target, downedMate, farMate);
        try {
            knockDown(helper, target);
            knockDown(helper, downedMate);
            for (Player reviver : new Player[] {stranger, downedMate, farMate}) {
                Revive.hold(reviver, target);
                if (Revive.isReviving(reviver)) {
                    helper.fail(reviver.getScoreboardName() + " could start a revive");
                }
            }
            if (!Downed.isDowned(target)) {
                helper.fail("target got up");
            }
        } finally {
            removeTeam(helper, team);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void noRollingWhileDowned(GameTestHelper helper) {
        Player player = mockPlayer(helper, "dn_noroll", new BlockPos(2, 2, 2));
        knockDown(helper, player);
        player.setOnGround(true);
        if (DodgeRoll.tryRoll(player)) {
            helper.fail("rolled while downed");
        }
        helper.succeed();
    }
}
