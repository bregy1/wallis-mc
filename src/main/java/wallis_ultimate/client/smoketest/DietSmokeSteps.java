package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import wallis_ultimate.registry.ModEffects;

/**
 * SURV-009 in a real client: a new player starts without diet penalties, and each food group's level gives its
 * benefit when high and its penalty when low. The diet mod applies effects every few seconds, hence the waits.
 */
final class DietSmokeSteps {
    private static final int EFFECT_TICKS = 200;
    private static final double BASE_ATTACK_DAMAGE = 1.0;
    private static final double BASE_MAX_HEALTH = 20.0;

    private DietSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("wait for the first diet update", 120, player -> {
                }),
                step("check the new player", 0, player -> check("a new player starts without diet penalties",
                        player.getMaxHealth() == BASE_MAX_HEALTH && !player.hasEffect(MobEffects.WEAKNESS)
                                && !player.hasEffect(ModEffects.FRAIL.get()) && !player.hasEffect(MobEffects.HUNGER),
                        "max health " + player.getMaxHealth() + ", effects " + player.getActiveEffects())),

                group("proteins", 1),
                await("high proteins", EFFECT_TICKS, player -> serverAttackDamage(player) > BASE_ATTACK_DAMAGE),
                checkGroup("high proteins give more attack damage",
                        player -> serverAttackDamage(player) > BASE_ATTACK_DAMAGE),
                group("proteins", 0),
                await("low proteins", EFFECT_TICKS, player -> player.hasEffect(MobEffects.WEAKNESS)),
                checkGroup("low proteins give Weakness", player -> player.hasEffect(MobEffects.WEAKNESS)),

                group("fruits", 1),
                await("high fruits", EFFECT_TICKS, player -> player.getMaxHealth() > BASE_MAX_HEALTH),
                checkGroup("high fruits give +2 max health", player -> player.getMaxHealth() == BASE_MAX_HEALTH + 2),
                group("fruits", 0),
                await("low fruits", EFFECT_TICKS, player -> player.getMaxHealth() < BASE_MAX_HEALTH),
                checkGroup("low fruits give -2 max health", player -> player.getMaxHealth() == BASE_MAX_HEALTH - 2),

                group("vegetables", 1),
                await("high vegetables", EFFECT_TICKS, player -> player.hasEffect(ModEffects.HEARTY.get())),
                checkGroup("high vegetables make the player Hearty", player -> player.hasEffect(ModEffects.HEARTY.get())),
                group("vegetables", 0),
                await("low vegetables", EFFECT_TICKS, player -> player.hasEffect(ModEffects.FRAIL.get())),
                checkGroup("low vegetables make the player Frail", player -> player.hasEffect(ModEffects.FRAIL.get())),

                group("grains", 1),
                await("high grains", EFFECT_TICKS, player -> player.hasEffect(ModEffects.FILLING.get())),
                checkGroup("high grains give Filling", player -> player.hasEffect(ModEffects.FILLING.get())),
                group("grains", 0),
                await("low grains", EFFECT_TICKS, player -> player.hasEffect(MobEffects.HUNGER)),
                checkGroup("low grains make hunger drain faster", player -> player.hasEffect(MobEffects.HUNGER)),

                group("sugars", 1),
                await("high sugars", EFFECT_TICKS, player -> player.hasEffect(ModEffects.SUGAR_BUZZ.get())),
                checkGroup("high sugars give Sugar Buzz", player -> player.hasEffect(ModEffects.SUGAR_BUZZ.get())),

                step("reset the diet", 10, player -> command("diet reset @s")));
    }

    private static ClientSmokeTest.Step group(String group, int value) {
        return step("set " + group + " to " + value, 5, player -> command("diet set @s " + group + " " + value));
    }

    private static ClientSmokeTest.Step checkGroup(String name, Predicate<LocalPlayer> ok) {
        return step("check: " + name, 0, player -> check(name, ok.test(player),
                "max health " + player.getMaxHealth() + ", attack " + serverAttackDamage(player)
                        + ", effects " + player.getActiveEffects()));
    }

    /** Attack damage isn't synced to the client, so ask the integrated server's copy of the player. */
    private static double serverAttackDamage(LocalPlayer player) {
        return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(player.getUUID())
                .getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
}
