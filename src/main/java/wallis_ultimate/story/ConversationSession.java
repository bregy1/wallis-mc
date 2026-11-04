package wallis_ultimate.story;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/**
 * One team's running conversation: who is in it, which line it's on and who may answer. Plain state and rules, without
 * anything that needs a real player, so GameTests can run a three-player conversation (STORY-002). {@link Conversations}
 * connects it to players, packets and the clock.
 */
public final class ConversationSession {
    /** A line stays up this long at least, even for a fast reader on a slow connection. */
    private static final int MIN_LINE_TICKS = 50;
    private static final int LINE_TICKS_PER_CHARACTER = 2;
    private static final int LINE_EXTRA_TICKS = 30;
    /** How long whoever was picked has to answer before it passes to someone else who's there (proposal). */
    public static final int CHOOSER_TIMEOUT_TICKS = 20 * 45;

    private final int id;
    private final Talk talk;
    private final String team;
    private final boolean protectsPlayers;
    private final List<UUID> participants = new ArrayList<>();
    private Talk.Node node;
    @Nullable
    private UUID chooser;
    private UUID lead;
    private int ticksOnLine;
    private boolean ended;

    public ConversationSession(int id, Talk talk, String team, boolean protectsPlayers, UUID lead, List<UUID> others) {
        this.id = id;
        this.talk = talk;
        this.team = team;
        this.protectsPlayers = protectsPlayers;
        this.lead = lead;
        participants.add(lead);
        others.stream().filter(other -> !other.equals(lead)).forEach(participants::add);
    }

    public int id() {
        return id;
    }

    public Talk talk() {
        return talk;
    }

    public String team() {
        return team;
    }

    public boolean protectsPlayers() {
        return protectsPlayers;
    }

    public List<UUID> participants() {
        return List.copyOf(participants);
    }

    public Talk.Node node() {
        return node;
    }

    /** The one member who answers the current line, picked when the line was reached. Null if it asks for no reply. */
    @Nullable
    public UUID chooser() {
        return chooser;
    }

    /** Who may hurry a line along: whoever started the talk, or the next one if they've gone. */
    public UUID lead() {
        return lead;
    }

    public int ticksOnLine() {
        return ticksOnLine;
    }

    public boolean ended() {
        return ended;
    }

    public void end() {
        ended = true;
    }

    /** Goes to a line. Each time a reply is needed, one present member is picked at random to choose it (STORY-002). */
    public void enter(String nodeId, RandomSource random) {
        node = talk.node(nodeId);
        ticksOnLine = 0;
        chooser = node.asksForReply() ? pick(participants, random) : null;
    }

    private static UUID pick(List<UUID> from, RandomSource random) {
        return from.get(random.nextInt(from.size()));
    }

    /** The line that follows a line without replies, or null if the talk is over. */
    @Nullable
    public String afterLine() {
        return node.next();
    }

    public boolean mayAnswer(UUID player, int reply) {
        return !ended && player.equals(chooser) && reply >= 0 && reply < node.replies().size();
    }

    public boolean mayHurry(UUID player) {
        return !ended && !node.asksForReply() && player.equals(lead);
    }

    /** How long a line without replies stays up by itself. */
    public int lineTicks() {
        return Math.max(MIN_LINE_TICKS, LINE_EXTRA_TICKS + node.text().length() * LINE_TICKS_PER_CHARACTER);
    }

    /** One tick further. True if the current line has run out by itself. */
    public boolean tick() {
        ticksOnLine++;
        return !node.asksForReply() && ticksOnLine >= lineTicks();
    }

    /** True if whoever was picked has let the time run out and someone else could answer instead. */
    public boolean chooserTimedOut() {
        return node.asksForReply() && ticksOnLine >= CHOOSER_TIMEOUT_TICKS && participants.size() > 1;
    }

    /** The answer passes on to someone else who's there. */
    public void passChooser(RandomSource random) {
        List<UUID> others = new ArrayList<>(participants);
        others.remove(chooser);
        if (!others.isEmpty()) {
            chooser = pick(others, random);
            ticksOnLine = 0;
        }
    }

    /**
     * Someone left: went offline, walked off or closed the screen. The talk goes on with whoever is still there. Returns
     * true if the choice moved to someone else, so everyone has to be told. A talk nobody is left in ends.
     */
    public boolean remove(UUID player, RandomSource random) {
        if (!participants.remove(player)) {
            return false;
        }
        if (participants.isEmpty()) {
            ended = true;
            return false;
        }
        boolean changed = false;
        if (player.equals(lead)) {
            lead = participants.get(0);
            changed = true;
        }
        if (player.equals(chooser)) {
            chooser = pick(participants, random);
            ticksOnLine = 0;
            changed = true;
        }
        return changed;
    }

    /** A member who came near while the talk runs joins in, watching. */
    public boolean add(UUID player) {
        if (participants.contains(player)) {
            return false;
        }
        participants.add(player);
        return true;
    }
}
