package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import wallis_ultimate.entity.RegularZombie;

/**
 * The regular zombie in a real client (ENEMY-001, ENEMY-002, ENEMY-012). Night spawns around the player are regular
 * zombies only. Then, on a platform under open sky, three zombies come for the player at night, and by day only the
 * one with Sunburn catches fire.
 */
final class ZombieSmokeSteps {
    private static final Set<EntityType<?>> VANILLA_ZOMBIES = Set.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.ZOMBIE_VILLAGER);
    /** Summoned with data, so they skip the spawn rolls (Sunburn, helmets): exactly one gets Sunburn below. */
    private static final String SUMMON = "summon wallis_ultimate:zombie %s {Tags:[\"smoketest\"]}";

    private ZombieSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("build a platform under open sky and let night spawns happen", 100, player -> {
                    command("fill ~-6 199 ~-6 ~6 199 ~6 minecraft:smooth_stone");
                    command("tp @s ~ 200 ~");
                    // Zombie hits would push the player off the platform.
                    command("attribute @s minecraft:generic.knockback_resistance base set 1");
                    command("time set 18000");
                    command("difficulty normal");
                }),
                step("check the night spawns", 0, player -> {
                    long regular = loadedEntities().filter(entity -> entity instanceof RegularZombie).count();
                    long vanilla = loadedEntities().filter(entity -> VANILLA_ZOMBIES.contains(entity.getType())).count();
                    check("night spawns are regular zombies, never vanilla ones", regular > 0 && vanilla == 0,
                            regular + " regular, " + vanilla + " vanilla zombies loaded");
                }),
                step("clear the world and summon three zombies", 10, player -> {
                    command("gamerule doMobSpawning false");
                    command("kill @e[type=!minecraft:player]");
                    command(String.format(SUMMON, "~5 ~ ~"));
                    command(String.format(SUMMON, "~-5 ~ ~"));
                    command(String.format(SUMMON, "~ ~ ~5"));
                }),
                step("give one of them Sunburn", 40, player -> command(
                        "effect give @e[type=wallis_ultimate:zombie,limit=1,sort=nearest] wallis_ultimate:sunburn infinite 0 true")),
                step("check the night", 0, player -> {
                    List<RegularZombie> zombies = zombiesNear(player);
                    double nearest = zombies.stream().mapToDouble(player::distanceTo).min().orElse(99);
                    check("three regular zombies come for the player at night",
                            zombies.size() == 3 && nearest < 3, zombies.size() + " zombies, nearest " + Math.round(nearest) + " blocks");
                    screenshot("zombies-night");
                }),
                step("make it day", 200, player -> command("time set 1000")),
                step("check the day", 0, player -> {
                    List<RegularZombie> zombies = zombiesNear(player);
                    long burning = zombies.stream().filter(RegularZombie::isOnFire).count();
                    check("by day only the zombie with Sunburn burns", zombies.size() == 3 && burning == 1,
                            burning + " of " + zombies.size() + " zombies burning");
                    screenshot("zombies-day");
                }),
                step("clean up", 10, player -> {
                    command("kill @e[type=wallis_ultimate:zombie]");
                    command("difficulty peaceful");
                    command("gamerule doMobSpawning true");
                    command("attribute @s minecraft:generic.knockback_resistance base set 0");
                    log("zombie steps done at " + player.blockPosition().toShortString());
                }));
    }

    private static Stream<Entity> loadedEntities() {
        return StreamSupport.stream(Minecraft.getInstance().level.entitiesForRendering().spliterator(), false);
    }

    private static List<RegularZombie> zombiesNear(LocalPlayer player) {
        return player.level().getEntitiesOfClass(RegularZombie.class, player.getBoundingBox().inflate(16));
    }
}
