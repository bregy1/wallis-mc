package wallis_ultimate.story;

import static wallis_ultimate.story.Speaker.CARL;
import static wallis_ultimate.story.Speaker.LURLEEN;
import static wallis_ultimate.story.Speaker.RADIO;
import static wallis_ultimate.story.Speaker.RADIO_HOST;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything said in Chapter 1 (STORY-002, STORY-003, STORY-016). All of it is Claude's proposal under the working agreement;
 * the user can veto any line. The user's tone note: funny and very rough, crude and irreverent, sparing nobody, the players
 * included. Our own jokes only, and no slurs aimed at people for who they are. Every swear word used here has to be covered by
 * SwearFilter; a GameTest checks that. Players only get this through the lang file, generated from here by StoryLanguage.
 */
public final class ChapterOneTalks {
    public static final String INTRO = "lurleen_intro";
    public static final String BANTER = "lurleen_banter";
    public static final String RADIO_FIRST = "radio_show";
    public static final String RADIO_LATER = "radio_later";
    public static final String CARL_FIRST = "carl_first";
    public static final String CARL_AGAIN = "carl_again";
    public static final String ENDING = "lurleen_ending";
    public static final String READY = "lurleen_ready";
    public static final String AGAIN = "lurleen_again";
    /** The command the boss worker's fight starts with (seam, ENEMY-011). Also used when the team goes back for another try. */
    public static final String BOSS_START_COMMAND = "wallis_boss hog_daddy start";

    /** Team story flags: set by talks, read by quest tasks and by what Lurleen talks about. */
    public static final String HEARD_INTRO = "heard_intro";
    public static final String HEARD_RADIO = "heard_radio";
    public static final String TALKED_CARL = "talked_carl";
    public static final String BELIEVED_CARL = "believed_carl";
    public static final String HOG_READY = "hog_ready";
    public static final String HEARD_ENDING = "heard_ending";

    public static final String HOME_REACTION = "lurleen_moved_in";
    public static final String FOOT_REACTION = "lurleen_foot";
    public static final String NUDGE_SWING = "lurleen_swing";
    public static final String NUDGE_HOME = "lurleen_home";
    public static final String NUDGE_DINNER = "lurleen_dinner";
    public static final String NUDGE_ZOMBIES = "lurleen_zombies";
    public static final String NUDGE_NIGHT = "lurleen_night";
    public static final String NUDGE_RADIO = "lurleen_radio";
    public static final String NUDGE_SAFE = "lurleen_safe";
    public static final String NUDGE_SCRAP = "lurleen_scrap";
    public static final String NUDGE_CARL = "lurleen_carl";
    public static final String NUDGE_HOG = "lurleen_hog";
    public static final String NUDGE_LIST = "lurleen_list_reminder";

    private static final Map<String, Talk> TALKS = new LinkedHashMap<>();
    /** What Lurleen says about each quest, the first time a team asks. */
    private static final Map<String, String> NUDGES = orderedNudges();

    /** Lines over the radio, lang key name to text (STORY-003, ENEMY-006). */
    public static final Map<String, String> TRANSMISSIONS = Map.of(
            "dusk", "Sun's goin' down, shitheads. If y'all ain't under a roof by now, I'll see you at the funeral. There won't be a funeral. There'll be a zombie wearin' your hat.",
            "safe_spot", "Y'all found folks? Holy shit. Tell Earl that crooked old bastard still owes me for the still parts. Then watch how they work metal. You might learn somethin', for the first time since birth.",
            "respawn", "Welcome back, Cletus. You left all your shit in a hole with your name on it. Go get it before somethin' wipes its ass with it.",
            "home", "Oh, that's where we're livin'? Jesus wept. Fine, I'm on my way. Don't touch nothin', and whoever's been shittin' in the corner can stop.",
            "horde_starting", "They're comin', a whole goddamn mess of 'em! Get somewhere you can hold, right fuckin' now, and if you gotta shit yourself, do it walkin'!");

    /** The opening's lines on screen, in order: lang key name to text (STORY-010). */
    public static final Map<String, String> OPENING_CAPTIONS = orderedOpening();

    static {
        add(intro());
        add(banter());
        add(radio());
        add(radioLater());
        add(carl());
        add(carlAgain());
        add(ready());
        add(again());
        add(ending());
        for (Map.Entry<String, String> nudge : NUDGES.entrySet()) {
            add(Talk.of(nudge.getKey()).line("say", LURLEEN, nudge.getValue()).build());
        }
        add(Talk.of(HOME_REACTION).line("say", LURLEEN, "Well. It's a hole, but it's your hole. Smells like a dead man's socks and a bad decision. I'll take the corner where I can see the door. And y'all can quit starin' at my knot, it's load-bearin'.").build());
        add(Talk.of(FOOT_REACTION)
                .line("say", LURLEEN, "Still breathin'? Damn, I lost a bet. Here. Granny raised rabbits. Ate 'em, skinned 'em, kept the feet in a pickle jar. Eighty-one years and never one bad night. They only work after dark, don't ask me why.")
                .reply("What happened to Granny?", "granny")
                .reply("Isn't that a lot of feet?", "feet")
                .line("granny", LURLEEN, "Last week was a bad night. She took four of 'em with her, and one of 'em with her dentures.").end()
                .line("feet", LURLEEN, "Lot of rabbits, Cletus. Not much luck for them. Jar's still good for pickles, if you're hungry.").end()
                .build());
    }

    private ChapterOneTalks() {
    }

    public static Talk talk(String id) {
        Talk talk = TALKS.get(id);
        if (talk == null) {
            throw new IllegalArgumentException("No talk '" + id + "'");
        }
        return talk;
    }

    public static boolean exists(String id) {
        return TALKS.containsKey(id);
    }

    public static List<Talk> all() {
        return List.copyOf(TALKS.values());
    }

    private static void add(Talk talk) {
        TALKS.put(talk.id(), talk);
    }

    private static Map<String, String> orderedOpening() {
        Map<String, String> lines = new LinkedHashMap<>();
        lines.put("weekend", "Day nine of what you dumb sons of bitches still call a quiet weekend.");
        lines.put("boot", "Nobody remembers days one through eight. Somebody's boot is in the still. Somebody's foot is in the boot. Best batch yet.");
        lines.put("darryl", "Here comes Darryl, back from the outhouse. Darryl looks like shit.");
        lines.put("always", "Then again, he's been green since that thing with the gas station sushi.");
        lines.put("can", "You raise a can. He raises an arm. The arm falls off. Classic Darryl.");
        lines.put("blam", "BLAM.");
        lines.put("who", "Somebody just shot Darryl's head clean off. Rude. He owed you twenty bucks.");
        return lines;
    }

    private static Map<String, String> orderedNudges() {
        Map<String, String> nudges = new LinkedHashMap<>();
        nudges.put(NUDGE_SWING, "You can't fight nothin' with a beer can, unless you're plannin' to bore 'em to death. Cabin's got a crafting table. Make a stone sword, and if you don't cut your own dick off, I'll show you what else you can put nails in.");
        nudges.put(NUDGE_HOME, "Sun don't wait, shitheads. Pick a spot, put a bed down, get a roof over it. I ain't sleepin' outside with you dumbasses, and neither are you, on account of you'd be dead.");
        nudges.put(NUDGE_DINNER, "You can't live on beer and raw squirrel. Well, you have been, and look at the shits it gave you. I could hear you from two hollers over. Cook somethin'. Anything. One hot meal and I'll show you Granny's kitchen tricks.");
        nudges.put(NUDGE_ZOMBIES, "Time you learned. Hit 'em in the head, it's the only part they ain't usin'. Hell, same goes for you. Don't let three get around you. They get fast in the dark, and they hear loud shit from a long way off. Go put down five.");
        nudges.put(NUDGE_NIGHT, "You ain't made it through a night yet, not sober. Sleep one off in your own bed, and if you hear somethin' scratchin' at the door, that's Mr. Henderson wantin' his bed back. Come mornin', I got somethin' for you.");
        nudges.put(NUDGE_RADIO, "Darryl's old radio's still sittin' in the cabin. I've been hearin' a voice on it, some smug asshole. Go see if there's anybody left to talk to who ain't you. God knows I'd pay for it.");
        nudges.put(NUDGE_SAFE, "So that radio asshole knows where folks are holed up. Go find some. They got walls, they know how to work metal, and right now y'all don't know which end of a furnace is the hot one. I watched one of you check.");
        nudges.put(NUDGE_SCRAP, "So now you can melt shit. Copper first, then iron. Bring home eight bars of iron and I'll show you what a skillet's really for. Hint: it ain't eggs, and it involves your head.");
        nudges.put(NUDGE_CARL, "There's a fella at Camp Tarp who won't shut the fuck up on the CB. Carl. Every corpse in the county can hear him. Go tell him to turn it down, and for Christ's sake don't let him start on the chickens.");
        nudges.put(NUDGE_HOG, "Y'all smell that? Worse than you, I mean. Old Man Hogg's place went quiet three days back. Hogs ain't hollerin'. And I found tracks big as a washtub, goin' round and round the old cabin, and a pile of shit I could park a truck in. Folks called him Hog Daddy, back when that was funny. Get your best gear and your whole crew. When you're ready, meet me at the cabin.");
        nudges.put(NUDGE_LIST, "It's on the list, Cletus. I wrote it down so I wouldn't have to say it twice. If you can't read, lick it, I don't give a shit.");
        return nudges;
    }

    private static Talk intro() {
        return Talk.of(INTRO)
                .line("start", LURLEEN, "That wasn't Darryl, dipshit. That was his mama. Y'all been wavin' a beer at a dead woman for ten goddamn minutes, and she still looked more sober than you.")
                .reply("She looked better than usual.", "better")
                .reply("Who the fuck are you?", "who")
                .reply("Is there more beer?", "beer")
                .line("better", LURLEEN, "Lord. I was hopin' it was the moonshine that made you this stupid. Turns out the moonshine's been carryin' you.").then("ended")
                .line("who", LURLEEN, "Lurleen. Two hollers over. You've met me, shit-for-brains. You tried to dance with me on the Fourth of July. Twice. Once with the truck.").then("ended")
                .line("beer", LURLEEN, "There ain't more anything, Cletus. That's what I came all the way up here to tell you, and I'm already sorry I did.").then("ended")
                .line("ended", LURLEEN, "Hear that? No trucks. No dogs. No Hank on the radio. The whole goddamn world ended about a week back. You sorry sacks of shit slept through it.")
                .reply("Ended how?", "how")
                .reply("So... no work Monday?", "monday")
                .line("monday", LURLEEN, "No, Cletus. No work Monday. Your boss is out by the highway, eatin' the mailman.").then("how")
                .line("how", LURLEEN, "Dead folks get up and bite. Bit folks die and get up. TV said it got loose from some Army lab up north, then the TV lady ate the weatherman live on the air. Best ratings that station ever had. Now the Army shoots whoever comes askin'.")
                .line("list", LURLEEN, "So here's how this goes. I wrote y'all a list, 'cause none of you can hold three things in your head, and one of you can't hold his piss. Try not to eat it.")
                .does(StoryEffects.lurleensList())
                .line("beds", LURLEEN, "And the Hendersons ain't usin' their beds no more. Take one. Mr. Henderson's still in it, so shake him out first. Put it under a roof before dark. By day they're slow and they cook in the sun. By night they ain't, and you're the cook-out.")
                .reply("Why help us?", "why")
                .reply("You movin' in with us?", "movein")
                .line("why", LURLEEN, "'Cause everybody else I know is dead, shootin', or Dale. My brother's out there somewhere, dumb as a sack of hammers. Till I find him, y'all are the last idiots I got, and that should scare the shit out of every one of us.").then("done")
                .line("movein", LURLEEN, "Wherever them beds go, I'll be along. Keep your hands to yourself and your eyes above the flannel, or I'll feed you your own balls with a fork. And I'll make you say grace first.").then("done")
                .line("done", LURLEEN, "Now get. Day's burnin', and so's Darryl's mama. Somebody put her out, she's stinkin' up the yard.")
                .does(StoryEffects.flag(HEARD_INTRO))
                .end()
                .build();
    }

    /** One line each, picked at random when Lurleen has nothing to add. */
    private static Talk banter() {
        return Talk.of(BANTER)
                .line("smell", LURLEEN, "You smell that? That's you. And I've been downwind of a dead mule.").end()
                .line("swept", LURLEEN, "If you're gonna die, do it outside. I just swept, and you people bleed like stuck hogs.").end()
                .line("septic", LURLEEN, "I've seen better-lookin' things crawl out of a septic tank. One of 'em's my ex, and he had better manners.").end()
                .line("beer", LURLEEN, "No, I will not hold your beer. There ain't no beer. Quit lookin' at me like I shit in your last can.").end()
                .line("granny", LURLEEN, "Granny would've had y'all sorted by now. Granny's dead, and she'd still have y'all sorted. She'd have done it with a spoon.").end()
                .line("eyes", LURLEEN, "Eyes up, Cletus. The knot ain't goin' anywhere, and neither are you if you keep that up.").end()
                .line("dale", LURLEEN, "You ever see a skinny fool with a dip ring wore into his back pocket and his fly open, you tell me. That's Dale. That's my idiot. These here are just the local ones.").end()
                .line("army", LURLEEN, "Don't go lookin' for the Army. They ain't lookin' for you, and they shoot a hell of a lot straighter than you piss.").end()
                .line("barrel", LURLEEN, "One of you shit in the rain barrel. I ain't askin' who. I'm just tellin' you that's what you been drinkin'.").end()
                .line("believed", LURLEEN, "You believed Carl. Bless your heart, you dumb son of a bitch. That man once tried to sell me a horse, said it was a spy. It was just a real judgmental horse.").end()
                .line("doubted", LURLEEN, "Aliens. Sure. That man once tried to sell me a horse, said it was a spy. Then he ate it, when it wouldn't talk.").end()
                .build();
    }

    /** The podcast from the bunker. Radio voices begins; the next broadcast has the first hint about the Moon. */
    private static Talk radio() {
        return Talk.of(RADIO_FIRST)
                .line("static", RADIO, "...kssshh... ...kssshhhhhh...")
                .line("back", RADIO_HOST, "...and we're back. This is The Bunker Experience, comin' to you live from my bunker, day eleven of whatever the fuck this is. No guests today. My guests all got eaten. One of 'em during the ad read.")
                .line("tank", RADIO_HOST, "I got a year of elk jerky down here, I got a sensory deprivation tank, I got a crate of hair foam that ain't done shit, and I got questions, man. Have you ever really looked at a zombie? Like, really looked? I did. It shit itself and kept walkin'. That's discipline, bro.")
                .line("places", RADIO_HOST, "Anyway. People are out there. I'm gettin' calls. Earl's Salvage, up north. Gas-N-Guns out west. Some tents they call Camp Tarp, out east. A busted rig down south. I'm told it smells like a dead trucker, 'cause it's got one.")
                .line("alone", RADIO_HOST, "If you can hear this, you ain't alone, bro. Get to one of those. They got walls. They got beds. They got a guy named Earl. Nobody's said anything good about Earl.")
                .line("sponsor", RADIO_HOST, "This episode is brought to you by nobody, 'cause they're all dead. Use code DEAD for nothin' off. Stay hydrated, you beautiful pieces of meat.")
                .does(StoryEffects.flag(HEARD_RADIO))
                .end()
                .build();
    }

    private static Talk radioLater() {
        return Talk.of(RADIO_LATER)
                .line("moon", RADIO_HOST, "...think about it, man. We went to the Moon, what, six times? Then we just fuckin' stopped. Why'd we stop? Somebody told us to stop. I'm just askin' questions. Hold on, I think I shit in the tank.")
                .line("static", RADIO, "...kssshhhhhh...")
                .end()
                .build();
    }

    /** Conspiracy Carl: right about nearly everything, believed by nobody. The first clue (STORY-016). */
    private static Talk carl() {
        return Talk.of(CARL_FIRST)
                .line("start", CARL, "STOP right there, fuckface! Say somethin' only a human would say.")
                .reply("Get bent, you lunatic.", "human")
                .reply("Beep boop.", "boop")
                .line("human", CARL, "...Okay. Okay, yeah, that's real human. Real shitty, too. Lurleen send you? Tell that flannel-wearin' bitch I ain't turnin' the CB down.").then("lab")
                .line("boop", CARL, "That's EXACTLY what one of them would say to throw me off. Which means you ain't one. Either that or you're the dumbest goddamn alien in the whole goddamn fleet.").then("lab")
                .line("lab", CARL, "You think this came out of some Army lab? The Army couldn't keep a lid on a chili cook-off. This ain't from here. It's ALIENS, man. I been tellin' people for years. They keep checkin' my medication.")
                .line("clipboard", CARL, "Think, you dumb shit! Who gains? Not the dead. Not the Army. I seen folks out by the reservoir at night, in robes, with a clipboard. Who the hell brings a clipboard to a lake?")
                .line("chickens", CARL, "And the chickens. I ain't sayin' more about the chickens. You go look one in the eye, then you tell me that's from around here. I looked. I ain't had a solid shit since.")
                .reply("Sure, Carl. I believe you.", "believe", StoryEffects.flag(BELIEVED_CARL))
                .reply("You've got a pie tin on your head.", "tin")
                .line("believe", CARL, "You do? Nobody ever... Huh. Well, you're either the smartest son of a bitch left alive or the stupidest. Keep your water boiled and your eyes on the sky.").does(StoryEffects.flag(TALKED_CARL)).end()
                .line("tin", CARL, "It's FOIL, asshole. A pie tin don't block shit. Go on, laugh it up. Keep your water boiled and your eyes on the sky.").does(StoryEffects.flag(TALKED_CARL)).end()
                .build();
    }

    private static Talk carlAgain() {
        return Talk.of(CARL_AGAIN)
                .line("water", CARL, "Still human? Good. Don't drink out of the reservoir. I ain't sayin' why. I don't know why. But they're real fuckin' interested in it, and I drank out of it once, and my piss came out blue.").end()
                .build();
    }

    /** Only at the cabin: the team decides when the fight starts. */
    private static Talk ready() {
        return Talk.of(READY)
                .line("ask", LURLEEN, "This is where that fat son of a bitch keeps comin' back to, every night. Smell it? You ready? Once it starts, it don't stop, and it sure as shit don't care who's cryin'.")
                .reply("Let's go kill that fat fuck.", "go", StoryEffects.flag(HOG_READY))
                .reply("Not yet.", "wait")
                .line("go", LURLEEN, "Then get your backs to somethin' and listen. You hear that? That's the wall. Try not to piss yourselves till it's through it.").end()
                .line("wait", LURLEEN, "Then quit standin' around where it eats, you're makin' it easy.").end()
                .build();
    }

    /** If the fight went badly: another go, while the thing still lives. */
    private static Talk again() {
        return Talk.of(AGAIN)
                .line("ask", LURLEEN, "You still got legs, that's somethin'. That big bastard ain't done, and neither are you. Go again?")
                .reply("Let's finish it.", "go", StoryEffects.command(BOSS_START_COMMAND))
                .reply("Give me a minute.", "wait")
                .line("go", LURLEEN, "That's the spirit. Dumb as a bag of hammers, but that's the spirit.").end()
                .line("wait", LURLEEN, "Take your time. I'm sure it won't eat nobody else. Oh, wait.").end()
                .build();
    }

    /** After Hog Daddy: what it means, and the cliffhanger into Chapter 2. */
    private static Talk ending() {
        return Talk.of(ENDING)
                .line("boot", LURLEEN, "That was Old Man Hogg. That's his boot. He only ever had the one. Lost the other in the wood chipper, along with what he called his good foot.")
                .line("bite", LURLEEN, "A bite don't do that. A bite don't make a man ten foot tall with a gut like that and a hook for a hand. Somethin' else got into him, and I don't know what. And I've seen what that man ate.")
                .reply("Carl might know.", "carl")
                .reply("Did he leave any bacon?", "bacon")
                .line("carl", LURLEEN, "Carl thinks his toaster's listenin'. Carl once tried to arrest a goose. Now shut your damn mouth, radio's goin'.").then("radio")
                .line("bacon", LURLEEN, "...I hate that I checked. There ain't. Now shut your damn mouth, radio's goin'.").then("radio")
                .line("radio", RADIO_HOST, "...anybody out there? It's your boy in the bunker. Bad news, bros. Raiders up on the highway, takin' whatever's left and whoever's left. And I got folks callin' in about people in robes, out by the reservoir. Fencin' it off. Nobody knows what the fuck they want.")
                .line("robes", LURLEEN, "...Robes.")
                .reply("You know somethin'?", "know")
                .line("know", LURLEEN, "No. Get some real walls up, and do it quick. Whatever that was, it was just the fuckin' neighbors. This ain't our land no more.")
                .does(StoryEffects.flag(HEARD_ENDING))
                .end()
                .build();
    }
}
