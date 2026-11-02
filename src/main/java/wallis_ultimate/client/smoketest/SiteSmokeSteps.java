package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.client.TravelScreen;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * District 1's hand-made places in a real client (WORLD-005, STORY-002, SURV-006, WORLD-004): the player starts in
 * front of the cabin looking at it; a look at each place from all sides, without the HUD in the way; then the signpost
 * takes them from one safe spot to another.
 */
final class SiteSmokeSteps {
    private static final List<String> SPOTS = List.of("earls_salvage", "gas_n_guns", "camp_tarp", "stranded_hauler");
    private static final int CHEST_SLOTS = 27;

    private SiteSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        List<ClientSmokeTest.Step> steps = new ArrayList<>();
        Vec3[] home = new Vec3[1];
        steps.add(step("check where the player woke up", 0, player -> {
            home[0] = player.position();
            BlockPos spawn = sites().spawn();
            check("the player wakes up at the cabin's fire", player.blockPosition().distManhattan(spawn) <= 2,
                    "at " + player.blockPosition() + ", spawn " + spawn);
            check("the player looks south, at the cabin", Math.abs(player.getYRot()) < 1.0F, "yaw " + player.getYRot());
            check("the cabin's campfire burns right in front of them", player.level().getBlockState(sites().cabin().offset(13, 0, 3)).is(Blocks.CAMPFIRE),
                    String.valueOf(player.level().getBlockState(sites().cabin().offset(13, 0, 3))));
        }));
        steps.add(step("hide the HUD and stop time at noon", 5, player -> {
            Minecraft.getInstance().options.hideGui = true;
            command("time set noon");
            command("gamerule sendCommandFeedback false");
        }));
        BlockPos[] cabin = new BlockPos[1];
        steps.add(step("find the cabin", 0, player -> cabin[0] = sites().cabin()));
        // Where to stand, relative to the cabin's corner, and which way to look.
        look(steps, cabin, "cabin-front", 13.5, 1.6, 2.5, 0.0F, 5.0F);
        look(steps, cabin, "cabin-porch", 13.5, 2.0, 8.5, 0.0F, 5.0F);
        look(steps, cabin, "cabin-air", -4.5, 18.0, -6.5, -45.0F, 28.0F);
        look(steps, cabin, "cabin-truck", 27.5, 3.0, 4.5, 40.0F, 12.0F);
        look(steps, cabin, "cabin-back", 13.5, 3.0, 31.5, 180.0F, 10.0F);
        look(steps, cabin, "cabin-inside", 13.5, 2.0, 14.5, 0.0F, 15.0F);
        for (String spot : SPOTS) {
            lookAtSafeSpot(steps, spot);
        }
        travel(steps);
        lootChest(steps);
        zombieLineup(steps);
        steps.add(step("back to where the player was", 20, player -> {
            Minecraft.getInstance().options.hideGui = false;
            command("gamerule sendCommandFeedback true");
            command(String.format("tp @s %.2f %.2f %.2f", home[0].x, home[0].y, home[0].z));
        }));
        return steps;
    }

    /** From above, then from where travellers arrive. Being there is also how the team finds a safe spot. */
    private static void lookAtSafeSpot(List<ClientSmokeTest.Step> steps, String id) {
        // Long waits: a spot far away has to reach the client and be drawn first, or the picture shows half of it.
        steps.add(step("fly above " + id, 120, player -> {
            SafeSpot spot = spot(id);
            int back = spot.radius() + 6;
            command("gamemode spectator");
            command(String.format("tp @s %d %d %d -45 28", spot.center().getX() - back, spot.center().getY() + spot.radius() * 2 / 3 + 6, spot.center().getZ() - back));
        }));
        steps.add(step("picture of " + id + " from above", 2, player -> screenshot(id + "-air")));
        steps.add(step("arrive at " + id, 40, player -> {
            BlockPos arrival = spot(id).arrival();
            command("gamemode survival");
            command(String.format("tp @s %.2f %d %.2f 180 0", arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5));
        }));
        steps.add(step("picture of " + id + " from the signpost's side", 2, player -> screenshot(id + "-arrival")));
        steps.add(step("turn around at " + id, 10, player -> command("tp @s ~ ~ ~ 0 5")));
        steps.add(step("the other way at " + id, 2, player -> {
            screenshot(id + "-south");
            check("finding " + id + " is announced", chatContains(Component.translatable(spot(id).nameKey()).getString()), "");
        }));
    }

    /** WORLD-004, the real thing: right-click the signpost, get the list, click a place, arrive there. */
    private static void travel(List<ClientSmokeTest.Step> steps) {
        String from = SPOTS.get(SPOTS.size() - 1);
        String to = SPOTS.get(0);
        steps.add(step("right-click the signpost", 10, player -> {
            Minecraft mc = Minecraft.getInstance();
            BlockPos signpost = spot(from).signpost();
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(signpost), Direction.SOUTH, signpost, false));
        }));
        steps.add(await("the travel list opens", 40, player -> Minecraft.getInstance().screen instanceof TravelScreen));
        steps.add(step("click the first place on the list", 40, player -> {
            Minecraft mc = Minecraft.getInstance();
            screenshot("travel-list");
            List<Button> buttons = mc.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
            check("the list has the three other safe spots", buttons.size() == 3, buttons.size() + " buttons");
            buttons.stream().filter(button -> button.getMessage().getString().equals(Component.translatable(spot(to).nameKey()).getString()))
                    .findFirst().ifPresent(Button::onPress);
        }));
        steps.add(step("check the arrival", 0, player -> {
            check("travel brings the player to the safe spot they picked", player.blockPosition().distManhattan(spot(to).arrival()) <= 2,
                    "at " + player.blockPosition() + ", arrival " + spot(to).arrival());
            check("the travel list closes", Minecraft.getInstance().screen == null, String.valueOf(Minecraft.getInstance().screen));
        }));
    }

    /** WORLD-011, CMBT-003: the cabin's chest holds the Thick Flannel. Taken things stay gone; nothing refills. */
    private static void lootChest(List<ClientSmokeTest.Step> steps) {
        int[] left = new int[1];
        steps.add(step("go to the chest in the cabin", 40, player -> {
            BlockPos cabin = sites().cabin();
            command(String.format("tp @s %d %d %d", cabin.getX() + 15, cabin.getY() + 1, cabin.getZ() + 16));
        }));
        steps.add(step("open the chest", 5, player -> openCabinChest(player)));
        steps.add(await("the chest opens", 40, player -> Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>));
        steps.add(step("take the trinket", 10, player -> {
            screenshot("loot-chest");
            List<Slot> slots = player.containerMenu.slots.subList(0, CHEST_SLOTS);
            left[0] = (int) slots.stream().filter(Slot::hasItem).count() - 1;
            Slot flannel = slots.stream().filter(slot -> slot.getItem().is(ModItems.THICK_FLANNEL.get())).findFirst().orElse(null);
            check("the cabin's chest holds the Thick Flannel", flannel != null, left[0] + " other stacks");
            check("and supplies", left[0] >= 2 && slots.stream().anyMatch(slot -> slot.getItem().is(ModItems.BANDAGE.get())), left[0] + " other stacks");
            if (flannel != null) {
                ClientSmokeTest.click(player, flannel.index, 0, ClickType.QUICK_MOVE);
            }
        }));
        steps.add(step("close the chest", 10, player -> player.closeContainer()));
        steps.add(step("open it again", 5, player -> openCabinChest(player)));
        steps.add(await("the chest opens again", 40, player -> Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>));
        steps.add(step("check what's left", 5, player -> {
            List<Slot> slots = player.containerMenu.slots.subList(0, CHEST_SLOTS);
            check("a looted chest doesn't refill or reroll", slots.stream().filter(Slot::hasItem).count() == left[0]
                    && slots.stream().noneMatch(slot -> slot.getItem().is(ModItems.THICK_FLANNEL.get())), "");
            check("the trinket came along", player.getInventory().contains(new ItemStack(ModItems.THICK_FLANNEL.get())), "");
            player.closeContainer();
        }));
    }

    /** ENEMY-012: all five looks in a row, close up, on a platform in the sky. They can't move, so this shows the pose. */
    private static void zombieLineup(List<ClientSmokeTest.Step> steps) {
        steps.add(step("line up the zombies", 40, player -> {
            // The test world starts on Peaceful, which removes monsters right away.
            command("difficulty normal");
            int x = player.getBlockX() + 40;
            int y = 230;
            int z = player.getBlockZ();
            command(String.format("fill %d %d %d %d %d %d minecraft:grass_block", x - 9, y - 1, z - 3, x + 9, y - 1, z + 8));
            int[][] uuids = {{1, 1, 1, 1}, {1, 1, 1, 0}, {1, 1, 1, 3}, {1, 1, 1, 2}, {1, 1, 1, 5}};
            for (int i = 0; i < uuids.length; i++) {
                // A UUID's hash is the XOR of its four numbers, so these give each look once.
                command(String.format("summon wallis_ultimate:zombie %d %d %d {NoAI:1b,UUID:[I;%d,%d,%d,%d],Rotation:[0f,0f]}", x - 6 + i * 3, y, z,
                        uuids[i][0], uuids[i][1], uuids[i][2], uuids[i][3]));
            }
            command(String.format("tp @s %.2f %d %.2f 180 8", x + 0.5, y, z + 6.5));
        }));
        steps.add(step("picture of the zombies", 2, player -> screenshot("zombie-lineup")));
        steps.add(step("clear the zombies away", 5, player -> {
            command("kill @e[type=wallis_ultimate:zombie,distance=..30]");
            command("difficulty peaceful");
        }));
    }

    private static void openCabinChest(LocalPlayer player) {
        BlockPos chest = sites().cabin().offset(17, 1, 16);
        Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(chest), Direction.WEST, chest, false));
    }

    private static SafeSpot spot(String id) {
        return sites().spot(id).orElseThrow();
    }

    private static void look(List<ClientSmokeTest.Step> steps, BlockPos[] origin, String name, double x, double y, double z, float yaw, float pitch) {
        steps.add(step("go to " + name, 15, player -> {
            command("gamemode spectator");
            command(String.format("tp @s %.2f %.2f %.2f %.1f %.1f", origin[0].getX() + x, origin[0].getY() + y, origin[0].getZ() + z, yaw, pitch));
        }));
        steps.add(step("picture of " + name, 2, player -> {
            screenshot(name);
            command("gamemode survival");
        }));
    }

    /** Read from the integrated server, like everything else the client isn't told. */
    private static DistrictSites sites() {
        return DistrictSites.get(Minecraft.getInstance().getSingleplayerServer().overworld());
    }
}
