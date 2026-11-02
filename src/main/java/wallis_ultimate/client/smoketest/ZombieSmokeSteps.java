package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModItems;

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

    private static RegularZombie bleedingTarget;

    private ZombieSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                // On the ground: monsters more than 128 blocks away from a player (height counts) never spawn.
                step("let night spawns happen around the player", 20, player -> {
                    command("time set 18000");
                    command("difficulty normal");
                }),
                await("night spawns appear", 600, player -> loadedEntities().anyMatch(entity -> entity instanceof RegularZombie)),
                step("check the night spawns", 0, player -> {
                    Map<String, Long> monsters = loadedEntities().filter(entity -> entity instanceof Enemy)
                            .collect(Collectors.groupingBy(entity -> EntityType.getKey(entity.getType()).toString(),
                                    TreeMap::new, Collectors.counting()));
                    log("monsters loaded after nightfall: " + monsters);
                    long regular = loadedEntities().filter(entity -> entity instanceof RegularZombie).count();
                    long vanilla = loadedEntities().filter(entity -> VANILLA_ZOMBIES.contains(entity.getType())).count();
                    check("monsters spawn at night, and no vanilla zombies among them", !monsters.isEmpty() && vanilla == 0,
                            monsters.toString());
                    check("regular zombies spawn naturally at night", regular > 0, monsters.toString());
                }),
                step("clear the world, build a platform under open sky and summon three zombies", 10, player -> {
                    command("gamerule doMobSpawning false");
                    command("kill @e[type=!minecraft:player]");
                    command("fill ~-6 199 ~-6 ~6 199 ~6 minecraft:smooth_stone");
                    command("tp @s ~ 200 ~");
                    // Zombie hits would push the player off the platform.
                    command("attribute @s minecraft:generic.knockback_resistance base set 1");
                    command(String.format(SUMMON, "~5 ~ ~"));
                    command(String.format(SUMMON, "~-5 ~ ~"));
                    command(String.format(SUMMON, "~ ~ ~5"));
                }),
                await("the three zombies arrive", 200, player -> zombiesNear(player).size() == 3),
                step("give one of them Sunburn", 10, player -> command(
                        "effect give @e[type=wallis_ultimate:zombie,limit=1,sort=nearest] wallis_ultimate:sunburn infinite 0 true")),
                await("the zombies reach the player", 200, player -> zombiesNear(player).stream().anyMatch(zombie -> player.distanceTo(zombie) < 3)),
                step("check the night", 0, player -> {
                    List<RegularZombie> zombies = zombiesNear(player);
                    double nearest = zombies.stream().mapToDouble(player::distanceTo).min().orElse(99);
                    check("three regular zombies come for the player at night",
                            zombies.size() == 3 && nearest < 3, zombies.size() + " zombies, nearest " + Math.round(nearest) + " blocks");
                    screenshot("zombies-night");
                }),
                step("make it day", 20, player -> command("time set 1000")),
                await("the Sunburn zombie catches fire", 400, player -> zombiesNear(player).stream().anyMatch(RegularZombie::isOnFire)),
                step("check the day", 0, player -> {
                    List<RegularZombie> zombies = zombiesNear(player);
                    long burning = zombies.stream().filter(RegularZombie::isOnFire).count();
                    check("by day only the zombie with Sunburn burns", zombies.size() == 3 && burning == 1,
                            burning + " of " + zombies.size() + " zombies burning");
                    screenshot("zombies-day");
                }),
                // GEAR-007: a real swing with a real weapon.
                step("get a Nail Bat", 10, player -> {
                    command("history individual unlock @s scavenger_weapons");
                    command("give @s wallis_ultimate:nail_bat");
                }),
                await("the Nail Bat arrives", 100, player -> player.getInventory().countItem(ModItems.NAIL_BAT.get()) == 1),
                // Selecting a weapon restarts the attack recharge, and a weak hit wouldn't get through a burning zombie's
                // moment of immunity. A real player swings a moment later, too.
                step("hold the Nail Bat", 30, player -> {
                    for (int i = 0; i < 9; i++) {
                        if (player.getInventory().getItem(i).is(ModItems.NAIL_BAT.get())) {
                            player.getInventory().selected = i;
                        }
                    }
                }),
                step("hit a zombie with the Nail Bat", 5, player -> {
                    bleedingTarget = zombiesNear(player).stream().filter(zombie -> !zombie.isOnFire())
                            .min(Comparator.comparingDouble(player::distanceTo)).orElseThrow();
                    Minecraft.getInstance().gameMode.attack(player, bleedingTarget);
                }),
                await("the zombie bleeds", 100, player -> bleedsOnServer(bleedingTarget)),
                step("check the bleeding", 0, player -> check("hitting a zombie with the Nail Bat makes it bleed",
                        bleedsOnServer(bleedingTarget), attackDetails(player))),
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

    /** The client doesn't get a mob's effects, so ask the integrated server's copy of the zombie. */
    private static boolean bleedsOnServer(RegularZombie zombie) {
        Entity onServer = Minecraft.getInstance().getSingleplayerServer().overworld().getEntity(zombie.getUUID());
        return onServer instanceof RegularZombie serverZombie && serverZombie.hasEffect(ModEffects.BLEEDING.get());
    }

    /** What the integrated server saw, for the failure message. */
    private static String attackDetails(LocalPlayer player) {
        Entity zombie = Minecraft.getInstance().getSingleplayerServer().overworld().getEntity(bleedingTarget.getUUID());
        LivingEntity serverPlayer = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(player.getUUID());
        return "server: player holds " + serverPlayer.getMainHandItem() + ", zombie " + zombie
                + (zombie instanceof LivingEntity living ? ", health " + living.getHealth() + "/" + living.getMaxHealth() + ", last hurt by " + living.getLastHurtByMob() : "")
                + ", distance " + player.distanceTo(bleedingTarget);
    }
}
