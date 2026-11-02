package wallis_ultimate.client.smoketest;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;
import wallis_ultimate.WallisUltimate;

/**
 * Dev-only client smoke test: {@code bash tools/smoketest.sh} (or {@code ./gradlew runClient -PsmokeTest}). From the
 * title screen it creates a fresh survival world, walks around, runs the feature steps, takes screenshots
 * (run/screenshots/smoketest-*.png) and quits. Every check logs a "[SMOKETEST] PASS" or "[SMOKETEST] FAIL" line.
 * Does nothing unless the wallis_ultimate.smoketest system property is set in a dev run.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientSmokeTest {
    static final boolean ENABLED = !FMLEnvironment.production && Boolean.getBoolean("wallis_ultimate.smoketest");
    private static final String WORLD_NAME = "wallis-smoketest";
    private static final String PREFIX = "[SMOKETEST] ";
    /** Gives up after 5 minutes of game ticks, so a stuck step can't keep the game open. */
    private static final int MAX_TICKS = 20 * 60 * 5;
    /** Lets chunks, the HUD and JEI settle after the world opens. */
    private static final int SETTLE_TICKS = 60;

    private enum Phase { WAITING_FOR_TITLE, LOADING, SETTLING, RUNNING, QUITTING }

    /**
     * One step: runs once, then the runner waits {@code waitAfter} ticks before the next one. A step with
     * {@code ready} instead waits until it's true (at most {@code waitAfter} ticks): the server can lag behind the
     * client, so checks that depend on a command having arrived should wait for it rather than for a fixed time.
     */
    record Step(String name, int waitAfter, Consumer<LocalPlayer> action, @Nullable Predicate<LocalPlayer> ready) {
    }

    private static Phase phase = Phase.WAITING_FOR_TITLE;
    private static final Deque<Step> steps = new ArrayDeque<>();
    private static int ticks;
    private static int waitTicks;
    private static int walkTicks;
    private static int screenshots;
    private static int passed;
    private static int failed;
    private static boolean pauseOnLostFocus;
    private static boolean windowHidden;
    private static ItemStack tooltip = ItemStack.EMPTY;
    private static final int TITLE_TICKS = 20;
    private static int titleTicks;
    private static final List<String> chat = new ArrayList<>();
    @Nullable
    private static Step awaiting;
    private static int awaitedTicks;

    private ClientSmokeTest() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!windowHidden) {
            // Hidden, the window never gets focus, so it can't grab the mouse or keyboard while you keep working.
            // The game still renders into its own framebuffer, which is what the screenshots read.
            GLFW.glfwHideWindow(mc.getWindow().getWindow());
            windowHidden = true;
        }
        if (phase == Phase.WAITING_FOR_TITLE) {
            // Let the title screen draw for a moment, so its screenshot shows the disclaimer (STORY-014).
            if (mc.screen instanceof TitleScreen && mc.getOverlay() == null && ++titleTicks == TITLE_TICKS) {
                screenshot("title");
                createWorld(mc);
            }
            return;
        }
        if (phase == Phase.QUITTING) {
            return;
        }
        if (++ticks > MAX_TICKS) {
            check("the smoke test finishes in time", false, "stuck in " + phase + ", next step: "
                    + (steps.isEmpty() ? "none" : steps.peek().name()));
            quit(mc);
            return;
        }
        switch (phase) {
            case LOADING -> {
                if (mc.player != null && mc.level != null && mc.screen == null) {
                    log("world open after " + ticks + " ticks at " + mc.player.blockPosition().toShortString());
                    phase = Phase.SETTLING;
                    waitTicks = SETTLE_TICKS;
                }
            }
            case SETTLING -> {
                if (--waitTicks <= 0) {
                    steps.addAll(allSteps());
                    phase = Phase.RUNNING;
                }
            }
            case RUNNING -> runSteps(mc);
            default -> {
            }
        }
    }

    private static List<Step> allSteps() {
        List<Step> all = new ArrayList<>();
        all.add(step("screenshot at spawn", 0, player -> screenshot("spawn")));
        all.add(step("make the player safe", 10, player -> {
            command("effect give @s minecraft:resistance infinite 4 true");
            command("time set day");
            command("weather clear");
        }));
        all.addAll(SiteSmokeSteps.steps());
        // bash tools/testsnapshot.sh look: just a look at the structures, which takes a minute instead of several.
        if (Boolean.getBoolean("wallis_ultimate.smoketest.sites")) {
            return all;
        }
        all.addAll(walkAround());
        all.addAll(DietSmokeSteps.steps());
        all.addAll(TrinketSmokeSteps.steps());
        all.addAll(LockSmokeSteps.steps());
        all.addAll(SurvivalSmokeSteps.steps());
        all.addAll(PropSmokeSteps.steps());
        all.addAll(ZombieSmokeSteps.steps());
        all.add(step("walk again", 45, player -> walk(40, 180)));
        all.add(step("screenshot at the end", 0, player -> screenshot("end")));
        return all;
    }

    private static List<Step> walkAround() {
        Vec3[] start = new Vec3[1];
        return List.of(
                step("walk forward", 65, player -> {
                    start[0] = player.position();
                    walk(60, 0);
                }),
                step("turn and walk", 45, player -> walk(40, 90)),
                step("check walking", 0, player -> {
                    double moved = player.position().subtract(start[0]).horizontalDistance();
                    check("walking moves the player", moved > 3, "moved " + Math.round(moved) + " blocks");
                    screenshot("walked");
                }));
    }

    private static void runSteps(Minecraft mc) {
        if (walkTicks > 0) {
            boolean walking = --walkTicks > 0;
            mc.options.keyUp.setDown(walking);
            mc.options.keySprint.setDown(walking);
            mc.options.keyJump.setDown(walking);
        }
        if (awaiting != null) {
            boolean ready = awaiting.ready().test(mc.player);
            if (!ready && ++awaitedTicks < awaiting.waitAfter()) {
                return;
            }
            log((ready ? "waited " : "gave up waiting after ") + awaitedTicks + " ticks for: " + awaiting.name());
            awaiting = null;
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        Step step = steps.poll();
        if (step != null && step.ready() != null) {
            awaiting = step;
            awaitedTicks = 0;
            return;
        }
        if (step == null) {
            quit(mc);
            return;
        }
        log("step: " + step.name());
        try {
            step.action().accept(mc.player);
        } catch (RuntimeException e) {
            WallisUltimate.LOGGER.error(PREFIX + "step '{}' threw", step.name(), e);
            check(step.name(), false, e.toString());
        }
        waitTicks = step.waitAfter();
    }

    private static void createWorld(Minecraft mc) {
        phase = Phase.LOADING;
        // The game window isn't focused during the test; a pause screen would stop the world.
        pauseOnLostFocus = mc.options.pauseOnLostFocus;
        mc.options.pauseOnLostFocus = false;
        // Silent, without touching the saved volume settings.
        mc.getSoundManager().updateSourceVolume(SoundSource.MASTER, 0.0F);
        String name;
        try {
            name = FileUtil.findAvailableName(mc.getLevelSource().getBaseDir(), WORLD_NAME, "");
        } catch (IOException e) {
            check("a fresh world can be created", false, e.toString());
            quit(mc);
            return;
        }
        log("creating world '" + name + "'");
        LevelSettings settings = new LevelSettings(name, GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.tell(() -> mc.createWorldOpenFlows().createFreshLevel(name, settings, WorldOptions.defaultWithRandomSeed(),
                WorldPresets::createNormalWorldDimensions));
    }

    private static void quit(Minecraft mc) {
        phase = Phase.QUITTING;
        mc.options.keyUp.setDown(false);
        mc.options.keySprint.setDown(false);
        mc.options.keyJump.setDown(false);
        log("DONE " + passed + " passed, " + failed + " failed");
        mc.tell(() -> {
            mc.options.pauseOnLostFocus = pauseOnLostFocus;
            if (mc.level != null) {
                mc.level.disconnect();
                mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            }
            mc.stop();
        });
    }

    /** Draws {@link #showTooltip}'s stack the way hovering it would, so screenshots show the real tooltip. */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (ENABLED && !tooltip.isEmpty()) {
            Screen screen = event.getScreen();
            event.getGuiGraphics().renderTooltip(Minecraft.getInstance().font, tooltip, screen.width / 2 - 70,
                    screen.height / 2 + 30);
        }
    }

    /** Remembers chat and system messages, so steps can check what the player was told. */
    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        if (ENABLED) {
            chat.add(event.getMessage().getString());
        }
    }

    // Helpers for the step lists.

    static boolean chatContains(String text) {
        return chat.stream().anyMatch(message -> message.contains(text));
    }

    /** Shows the stack's tooltip on every screen until called with an empty stack. */
    static void showTooltip(ItemStack stack) {
        tooltip = stack;
    }

    /** The text as the font draws it (some mods hide parts from plain {@link Component#getString()}). */
    static String renderedText(Component text) {
        StringBuilder out = new StringBuilder();
        Language.getInstance().getVisualOrder(text).accept((index, style, codePoint) -> {
            out.appendCodePoint(codePoint);
            return true;
        });
        return out.toString();
    }

    static Step step(String name, int waitAfter, Consumer<LocalPlayer> action) {
        return new Step(name, waitAfter, action, null);
    }

    /** Waits until the condition holds, at most {@code maxTicks}; the check step after it reports what happened. */
    static Step await(String name, int maxTicks, Predicate<LocalPlayer> ready) {
        return new Step(name, maxTicks, player -> {
        }, ready);
    }

    static void log(String message) {
        WallisUltimate.LOGGER.info(PREFIX + message);
    }

    static void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            log("PASS " + name);
        } else {
            failed++;
            log("FAIL " + name + ": " + detail);
        }
    }

    /** Runs a command as the player, like typing it in chat. */
    static void command(String command) {
        Minecraft.getInstance().player.connection.sendCommand(command);
    }

    /** Walks forward (sprinting and jumping, to get over blocks) for the given ticks after turning. */
    static void walk(int forTicks, float turnDegrees) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setYRot(player.getYRot() + turnDegrees);
        walkTicks = forTicks;
    }

    static void screenshot(String name) {
        Minecraft mc = Minecraft.getInstance();
        String file = String.format("smoketest-%02d-%s.png", ++screenshots, name);
        Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {
        });
        log("screenshot " + file);
    }

    /** A click in the open menu (the inventory when no screen is open), sent to the server like a real click. */
    static void click(LocalPlayer player, int menuSlot, int button, ClickType type) {
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, menuSlot, button,
                type, player);
    }

    /** Index in the player's {@link Inventory} (0–8 hotbar, 9–35 main) to the matching slot of the inventory menu. */
    static int inventoryMenuSlot(int inventoryIndex) {
        return inventoryIndex < Inventory.getSelectionSize() ? 36 + inventoryIndex : inventoryIndex;
    }

    /** Index in the player's {@link Inventory} (0–8 hotbar, 9–35 main) to the matching slot of a crafting table. */
    static int craftingMenuSlot(int inventoryIndex) {
        return inventoryIndex < Inventory.getSelectionSize() ? 37 + inventoryIndex : inventoryIndex + 1;
    }

    /** Counts an item in the inventory indexes [from, to). */
    static int count(LocalPlayer player, Item item, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** The first inventory index in [from, to) holding the item. */
    static int find(LocalPlayer player, Item item, int from, int to) {
        for (int i = from; i < to; i++) {
            if (player.getInventory().items.get(i).is(item)) {
                return i;
            }
        }
        throw new IllegalStateException("no " + item + " in inventory slots " + from + "-" + (to - 1) + ": "
                + describe(player));
    }

    /** The non-empty inventory slots, for failure messages. */
    static String describe(LocalPlayer player) {
        StringBuilder text = new StringBuilder();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                text.append(text.isEmpty() ? "" : ", ").append(i).append('=').append(stack.getCount()).append(' ')
                        .append(stack.getItem());
            }
        }
        return "inventory [" + text + "], selected " + inventory.selected;
    }
}
