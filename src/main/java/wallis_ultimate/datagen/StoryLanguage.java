package wallis_ultimate.datagen;

import java.util.function.BiConsumer;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.story.ChapterOneTalks;
import wallis_ultimate.story.OpeningCutscene;
import wallis_ultimate.story.Speaker;
import wallis_ultimate.story.StoryEffects;
import wallis_ultimate.story.Talk;
import wallis_ultimate.story.Transmissions;

/**
 * All of Chapter 1's story text for the lang file (STORY-002: all player-facing text goes through lang files). Conversations come
 * from their definitions in ChapterOneTalks; quest text is here, referenced by key from the quest file. Everything is Claude's
 * proposal, for the user to veto, in the rough tone the user asked for. Every key starts with "story.wallis_ultimate.", which the
 * clean-language option relies on.
 */
final class StoryLanguage {
    private static final String STORY = "story." + WallisUltimate.MOD_ID + ".";

    private StoryLanguage() {
    }

    static void addAll(BiConsumer<String, String> add) {
        add.accept(Speaker.NARRATOR.nameKey(), "Narrator");
        add.accept(Speaker.LURLEEN.nameKey(), "Lurleen");
        add.accept(Speaker.CARL.nameKey(), "Conspiracy Carl");
        // Q-175: the one place this parody's name is written. No other text says it, so it can change right here.
        add.accept(Speaker.RADIO_HOST.nameKey(), "Joe Rogaine");
        add.accept(Speaker.RADIO.nameKey(), "Radio");
        add.accept(STORY + "npc.lurleen", "Lurleen");
        add.accept(STORY + "npc.conspiracy_carl", "Conspiracy Carl");
        add.accept(StoryEffects.LIST_NAME_KEY, "Lurleen's List");

        add.accept(STORY + "conversation.title", "Conversation");
        add.accept(STORY + "conversation.next", "Next");
        add.accept(STORY + "conversation.choosing", "%s is choosin', God help us...");
        add.accept(STORY + "conversation.options", "Wallis Ultimate");
        add.accept(STORY + "conversation.clean_off", "Swearing: on");
        add.accept(STORY + "conversation.clean_on", "Swearing: #@!$");
        add.accept("message." + WallisUltimate.MOD_ID + ".tier.current", "Your team is in tier %2$s: %1$s");
        add.accept("tier." + WallisUltimate.MOD_ID + ".scavenger", "Scavenger");
        add.accept("tier." + WallisUltimate.MOD_ID + ".settler", "Settler");
        add.accept("tier." + WallisUltimate.MOD_ID + ".gunsmith", "Gunsmith");
        add.accept("tier." + WallisUltimate.MOD_ID + ".launch", "Launch");

        for (Talk talk : ChapterOneTalks.all()) {
            for (Talk.Node node : talk.nodes()) {
                add.accept(node.textKey(), node.text());
                node.replies().forEach(reply -> add.accept(reply.textKey(), reply.text()));
            }
        }
        ChapterOneTalks.OPENING_CAPTIONS.forEach((name, text) -> add.accept(OpeningCutscene.captionKey(name), text));
        ChapterOneTalks.TRANSMISSIONS.forEach((name, text) -> add.accept(Transmissions.key(name), text));
        quests(add);
    }

    private static void quests(BiConsumer<String, String> add) {
        add.accept(STORY + "quest.file", "Lurleen's List of Shit To Do");
        add.accept(STORY + "quest.chapter", "Chapter 1: Scavenger");
        add.accept(STORY + "quest.chapter_sub", "Wait, what apocalypse?");
        quest(add, "intro", "Mornin', Sunshine", "The world ended. Y'all missed it, you useless bastards.",
                "Some woman with a shotgun says the world ended a week ago. She also says it wasn't Darryl. She also said a lot of things about you and your mother, and they were all fair.",
                "She wrote you a list. This is the list. Don't wipe with it.",
                "Hear Lurleen out");
        quest(add, "swing", "Somethin' to Swing", "A beer can ain't a weapon, no matter what happened at Cousin Ray's wedding.",
                "Lurleen: \"You can't fight nothin' with a can. Cabin's got a crafting table. Make a stone sword, and if you don't cut your own dick off, I'll show you what else you can put nails in.\"",
                "",
                "Make a stone sword");
        quest(add, "home", "Create First Home", "Before dark, shithead. She's not kiddin'.",
                "Lurleen handed everyone a bed. She took them off the Hendersons, who aren't using them. One of them still has some Henderson on it. Nights out here will kill you: by day they're slow, by dark they're not.",
                "Pick a spot, put your bed down, get a roof over it. That's home now, God help it, and Lurleen will move in.",
                "Place your bed");
        quest(add, "dinner", "Hot Meal", "You can't live on beer and raw squirrel.",
                "Lurleen: \"Well, you have been, and look at the shits it gave you. I could hear you from two hollers over. Cook somethin'. Anything. One hot meal and I'll show you Granny's kitchen tricks.\"",
                "A furnace or a campfire will do. Darryl's mama doesn't count, we checked.",
                "Have some cooked food on you");
        quest(add, "zombies", "Darryl's Friends", "Aim for the head. It's the only part they ain't usin'.",
                "Lurleen: \"Don't let three get around you. They get fast in the dark, and they hear loud shit from a long way off. Go put down five, and try not to recognize any of 'em. You owe most of 'em money.\"",
                "",
                "Put down five zombies");
        quest(add, "night", "Sleep It Off", "First night in your own bed. Mr. Henderson's got opinions.",
                "Lurleen: \"You ain't made it through a night yet, not sober. Sleep one off in your own bed. Come mornin', I got somethin' for you, and it ain't what you're hopin', Cletus.\"",
                "It's Granny's Rabbit Foot, one for each of you. Wear it as a charm. At night it makes things a little fairer. It also smells like a pickle jar, because it's been in one since before you were born.",
                "Sleep in a bed");
        quest(add, "radio", "The Bunker Experience", "There's a smug asshole on Darryl's radio.",
                "Lurleen: \"Darryl's old radio's still sittin' in the cabin. Go see if there's anybody left to talk to who ain't you.\"",
                "It's on the drawers at the back of the cabin. Right-click to listen. You'll wish you hadn't.",
                "Listen to the radio");
        quest(add, "safe", "Folks With Walls", "Earl's Salvage, Gas-N-Guns, Camp Tarp, the Stranded Hauler.",
                "That smug asshole on the radio says there are safe spots out there: north, west, east and south of the cabin. They've got walls, they've got beds, and they know how to work metal. They don't know you yet, so you've got a shot.",
                "Find any one of them. Whatever they teach you about smelting, you'll keep. A safe spot's signpost connects it to the others you've found.",
                "Find a safe spot");
        quest(add, "scrap", "Eight Bars", "Copper first, then iron, you lump.",
                "Lurleen: \"So now you can melt shit. Copper first, then iron. Bring home eight bars of iron and I'll show you what a skillet's really for.\"",
                "Copper ore needs a stone pickaxe. Iron ore needs a copper one. Licking it doesn't help. Somebody tried.",
                "Get eight iron ingots");
        quest(add, "carl", "Turn It Down, Carl", "Don't let him start on the chickens. For the love of God.",
                "Lurleen: \"There's a fella at Camp Tarp who won't shut the fuck up on the CB. Carl. Every corpse in the county can hear him. Go tell him to turn it down.\"",
                "Camp Tarp is east of the cabin. You'll hear him before you see him. You'll smell him before that.",
                "Talk to Conspiracy Carl");
        quest(add, "ready", "Tracks Big as a Washtub", "Old Man Hogg's place went quiet. The smell didn't.",
                "Lurleen: \"Hogs ain't hollerin'. And somethin's been goin' round and round the old cabin at night, shittin' like a dump truck. Folks called him Hog Daddy, back when that was funny.\"",
                "Get your best gear and your whole crew. When you're ready, talk to Lurleen at the old cabin. Once it starts, it doesn't stop, so piss first.",
                "Tell Lurleen you're ready, at the cabin");
        quest(add, "hog", "Hog Daddy", "He only ever had the one boot.",
                "It's Old Man Hogg, or it used to be. It's through the wall, it's headed for his farm, and it's angrier than you and a whole lot fatter. Run, then fight it in the pen: make it charge into something solid, and hit it while it's stunned. Try fire. Everybody likes bacon.",
                "",
                "Put down Hog Daddy");
    }

    private static void quest(BiConsumer<String, String> add, String id, String title, String subtitle, String line1, String line2, String task) {
        String prefix = STORY + "quest." + id;
        add.accept(prefix + ".title", title);
        add.accept(prefix + ".subtitle", subtitle);
        add.accept(prefix + ".text1", line1);
        if (!line2.isEmpty()) {
            add.accept(prefix + ".text2", line2);
        }
        add.accept(prefix + ".task", task);
    }
}
