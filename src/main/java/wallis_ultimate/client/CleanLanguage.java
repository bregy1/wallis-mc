package wallis_ultimate.client;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.story.StoryOptions;
import wallis_ultimate.story.SwearFilter;

/**
 * STORY-002's client option. All story text is translated through keys starting with "story.wallis_ultimate.", whether it's a
 * conversation, a caption or a quest in FTB Quests' own screen. With the option on, this wraps the game's language and cleans
 * exactly those. That reaches FTB Quests' screens without touching them. Vanilla swaps the language with
 * {@link Language#inject} itself, and translated text re-reads it as soon as the instance changes.
 */
public final class CleanLanguage extends Language {
    private static final String STORY_PREFIX = "story." + WallisUltimate.MOD_ID + ".";

    private final Language wrapped;

    private CleanLanguage(Language wrapped) {
        this.wrapped = wrapped;
    }

    /** The first resource reload can come before Forge has read the config. Off until it has. */
    public static boolean isOn() {
        return StoryOptions.SPEC.isLoaded() && StoryOptions.CLEAN_LANGUAGE.get();
    }

    public static void toggle() {
        StoryOptions.CLEAN_LANGUAGE.set(!isOn());
        StoryOptions.CLEAN_LANGUAGE.save();
        apply();
    }

    /** Puts the wrapper on or takes it off, to match the option. Also needed after a resource reload loads a new language. */
    public static void apply() {
        Language current = Language.getInstance();
        Language plain = current instanceof CleanLanguage clean ? clean.wrapped : current;
        Language wanted = isOn() ? new CleanLanguage(plain) : plain;
        if (wanted != current) {
            Language.inject(wanted);
        }
    }

    @Override
    public String getOrDefault(String key, String fallback) {
        String text = wrapped.getOrDefault(key, fallback);
        return key.startsWith(STORY_PREFIX) ? SwearFilter.clean(text) : text;
    }

    @Override
    public boolean has(String key) {
        return wrapped.has(key);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return wrapped.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText text) {
        return wrapped.getVisualOrder(text);
    }
}
