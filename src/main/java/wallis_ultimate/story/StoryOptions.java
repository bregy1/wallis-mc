package wallis_ultimate.story;

import net.minecraftforge.common.ForgeConfigSpec;

/** STORY-002: the client's own story options, in config/wallis_ultimate-client.toml. Swearing stays on by default. */
public final class StoryOptions {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CLEAN_LANGUAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLEAN_LANGUAGE = builder.comment("Replace swear words in story text with symbols.").define("cleanLanguage", false);
        SPEC = builder.build();
    }

    private StoryOptions() {
    }
}
