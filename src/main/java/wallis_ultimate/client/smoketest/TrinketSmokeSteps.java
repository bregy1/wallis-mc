package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.Trinkets;

/**
 * CMBT-003 in a real client: trinkets go into their Curios slots and work, the Curios screen shows the slots, and a
 * ridden horse gets the bonus. Also checks that none of our keys share a key with anything else in the pack.
 */
final class TrinketSmokeSteps {
    private static final double WALK_SPEED = 0.1;
    private static final double BASE_ARMOR = 0.0;
    /** Our roll is on T until the pack moves chat to Enter (CMBT-002), so that one clash is known. */
    private static final String KNOWN_CLASH = "key.chat";

    private TrinketSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("check the keys", 0, player -> checkKeys()),
                step("equip trinkets", 10, player -> {
                    command("curios replace head 0 @s with wallis_ultimate:jogging_headband");
                    command("curios replace body 0 @s with wallis_ultimate:thick_flannel");
                    command("curios replace belt 0 @s with wallis_ultimate:worn_spurs");
                }),
                await("the trinkets take effect", 100, player -> player.getAttributeValue(Attributes.ARMOR) > BASE_ARMOR),
                step("check the trinkets", 0, player -> {
                    check("the Jogging Headband makes the player faster",
                            player.getAttributeValue(Attributes.MOVEMENT_SPEED) > WALK_SPEED,
                            "speed " + player.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    check("the Thick Flannel gives armor", player.getAttributeValue(Attributes.ARMOR) == BASE_ARMOR + 2,
                            "armor " + player.getAttributeValue(Attributes.ARMOR));
                }),
                // Curios ignores its key while the window is inactive, and the test window is hidden.
                step("open the trinket screen", 5, player -> {
                    Minecraft.getInstance().setWindowActive(true);
                    pressKey("key.curios.open.desc");
                }),
                await("the trinket screen opens", 100, player -> Minecraft.getInstance().screen != null
                        && Minecraft.getInstance().screen.getClass().getName().contains("curios")),
                step("screenshot the trinket screen", 5, player -> {
                    Minecraft.getInstance().setWindowActive(false);
                    check("the trinket screen opens", Minecraft.getInstance().screen != null
                                    && Minecraft.getInstance().screen.getClass().getName().contains("curios"),
                            "screen " + Minecraft.getInstance().screen);
                    screenshot("trinkets");
                }),
                step("close the trinket screen", 5, LocalPlayer::closeContainer),

                // GEAR-006: how the new items look. Copper tools are locked until the Iron stage, so they show gray.
                step("get the copper tools", 20, player -> {
                    command("give @s wallis_ultimate:copper_pickaxe");
                    command("give @s wallis_ultimate:copper_axe");
                    command("give @s wallis_ultimate:copper_shovel");
                    command("give @s wallis_ultimate:copper_hoe");
                    command("give @s wallis_ultimate:grannys_rabbit_foot");
                    command("give @s wallis_ultimate:lucky_horseshoe");
                    command("give @s wallis_ultimate:spare_clip");
                    command("give @s wallis_ultimate:brass_knuckles");
                    command("give @s wallis_ultimate:nail_bat");
                    command("give @s wallis_ultimate:heavy_shovel");
                    command("give @s wallis_ultimate:pitchfork");
                    command("give @s wallis_ultimate:frying_pan");
                }),
                step("open the inventory", 15, player -> Minecraft.getInstance().setScreen(new InventoryScreen(player))),
                step("screenshot the new items", 0, player -> screenshot("new-items")),
                step("close the inventory", 5, LocalPlayer::closeContainer),

                // On a small platform: the walk may have ended in water, and riders get thrown off with their head under.
                step("build a horse platform", 20, player -> {
                    command("fill ~-3 ~4 ~-3 ~3 ~4 ~3 minecraft:smooth_stone");
                    command("fill ~-3 ~5 ~-3 ~3 ~8 ~3 minecraft:air");
                    command("tp @s ~ ~5 ~");
                }),
                step("get on a horse", 20, player -> {
                    command("summon minecraft:horse ~ ~ ~ {Tame:1b,SaddleItem:{id:\"minecraft:saddle\",Count:1b}}");
                    command("ride @s mount @e[type=minecraft:horse,limit=1,sort=nearest]");
                }),
                await("the player sits on the horse", 100, player -> player.getVehicle() instanceof AbstractHorse),
                step("check the spurs", 10, player -> {
                    double speed = serverHorseSpeed(player);
                    double horseBaseSpeed = serverHorseBaseSpeed(player);
                    check("Worn Spurs make a ridden horse faster",
                            Math.abs(speed - horseBaseSpeed * (1 + Trinkets.SPURS_SPEED)) < 0.001,
                            "speed " + speed + ", base " + horseBaseSpeed);
                    command("ride @s dismount");
                    command("kill @e[type=minecraft:horse]");
                }));
    }

    private static Entity serverVehicle(LocalPlayer player) {
        return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(player.getUUID()).getVehicle();
    }

    private static double serverHorseSpeed(LocalPlayer player) {
        return ((AbstractHorse) serverVehicle(player)).getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    private static double serverHorseBaseSpeed(LocalPlayer player) {
        return ((AbstractHorse) serverVehicle(player)).getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    private static void pressKey(String name) {
        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (key.getName().equals(name)) {
                KeyMapping.click(key.getKey());
                return;
            }
        }
        throw new IllegalStateException("no key named " + name);
    }

    /** Logs and fails on any key one of ours shares with another mod (other mods clash with each other by design). */
    private static void checkKeys() {
        Map<String, List<String>> byKey = new LinkedHashMap<>();
        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (!key.isUnbound()) {
                byKey.computeIfAbsent(key.saveString(), unused -> new ArrayList<>()).add(key.getName());
            }
        }
        List<String> ourClashes = new ArrayList<>();
        byKey.forEach((key, names) -> {
            if (names.size() > 1 && names.stream().anyMatch(name -> name.contains(WallisUltimate.MOD_ID))) {
                log("key clash on " + key + ": " + names);
                List<String> others = new ArrayList<>(names);
                others.removeIf(name -> name.contains(WallisUltimate.MOD_ID) || name.equals(KNOWN_CLASH));
                if (!others.isEmpty()) {
                    ourClashes.add(key + ": " + names);
                }
            }
        });
        check("our keys don't clash with other keys", ourClashes.isEmpty(), ourClashes.toString());
    }
}
