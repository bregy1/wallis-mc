package wallis_ultimate.story;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * STORY-002: the clean-language option replaces swear words with symbols. Whole words only, so "scrap" and "class" stay
 * as they are. Every swear word used in story text has to be covered here; a GameTest checks the lang file against this.
 * No "%" among the symbols: the text still goes through lang formatting after this.
 */
public final class SwearFilter {
    private static final String SYMBOLS = "#@!$&*";
    private static final Pattern SWEARS = Pattern.compile("\\b(?:"
            + "(?:mother|dumb|clown)?fuck\\w*"
            + "|(?:bull|dip|horse|bat|chicken|ape)?shit\\w*"
            + "|(?:god)?damn\\w*"
            + "|ass|asses|ass(?:hole|hat|wipe|clown|face)\\w*|(?:dumb|jack|bad|smart|fat|lard|half)ass\\w*"
            + "|bitch\\w*|bastard\\w*|hell|crap\\w*|piss\\w*|dick\\w*|prick\\w*|cock\\w*|turd\\w*|balls|ballsack\\w*|tits?"
            + "|christ|jesus"
            + ")\\b", Pattern.CASE_INSENSITIVE);

    private SwearFilter() {
    }

    public static String clean(String text) {
        return SWEARS.matcher(text).replaceAll(match -> Matcher.quoteReplacement(symbols(match.group().length())));
    }

    private static String symbols(int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append(SYMBOLS.charAt(i % SYMBOLS.length()));
        }
        return out.toString();
    }
}
