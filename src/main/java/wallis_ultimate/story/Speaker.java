package wallis_ultimate.story;

import wallis_ultimate.WallisUltimate;

/** Who a line of story text belongs to: their name (through the lang file) and the color it's drawn in. */
public enum Speaker {
    NARRATOR("narrator", 0xB0B0B0),
    LURLEEN("lurleen", 0xFF7A5C),
    CARL("carl", 0x8FD3FF),
    DUSTY("dusty", 0xFFD36B),
    RADIO("radio", 0xA8C8A0);

    private final String id;
    private final int color;

    Speaker(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public String nameKey() {
        return "story." + WallisUltimate.MOD_ID + ".speaker." + id;
    }
}
