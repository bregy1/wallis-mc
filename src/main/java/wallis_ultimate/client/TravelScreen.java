package wallis_ultimate.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.TravelRequestPacket;

/** WORLD-004: the signpost's list, one button per safe spot the team has found. The server decides whether it works. */
public class TravelScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_HEIGHT = 24;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int HINT_COLOR = 0xA0A0A0;

    private final String here;
    private final List<String> known;

    private TravelScreen(String here, List<String> known) {
        super(Component.translatable("screen." + WallisUltimate.MOD_ID + ".travel.title"));
        this.here = here;
        this.known = known;
    }

    public static void open(String here, List<String> known) {
        Minecraft.getInstance().setScreen(new TravelScreen(here, known));
    }

    private static Component nameOf(String spotId) {
        return Component.translatable("safe_spot." + WallisUltimate.MOD_ID + "." + spotId);
    }

    @Override
    protected void init() {
        int x = (width - BUTTON_WIDTH) / 2;
        int y = height / 2 - known.size() * ROW_HEIGHT / 2;
        for (String spot : known) {
            addRenderableWidget(Button.builder(nameOf(spot), button -> {
                ModNetwork.CHANNEL.sendToServer(new TravelRequestPacket(spot));
                onClose();
            }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int top = height / 2 - known.size() * ROW_HEIGHT / 2;
        graphics.drawCenteredString(font, title, width / 2, top - 36, TEXT_COLOR);
        graphics.drawCenteredString(font, Component.translatable("screen." + WallisUltimate.MOD_ID + ".travel.here", nameOf(here)), width / 2, top - 22, HINT_COLOR);
        if (known.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("screen." + WallisUltimate.MOD_ID + ".travel.none"), width / 2, top, HINT_COLOR);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
