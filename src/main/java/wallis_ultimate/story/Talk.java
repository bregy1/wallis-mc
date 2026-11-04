package wallis_ultimate.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import wallis_ultimate.WallisUltimate;

/**
 * One conversation (STORY-002): lines, each by one speaker, some of them with short replies to pick from. The text is
 * written here next to the structure, so a whole scene reads in one place; players only ever get it through the lang file
 * (StoryLanguage generates it from these definitions, and the server only sends the keys).
 */
public final class Talk {
    /** What a line or reply does when it's reached. Runs on the server. */
    @FunctionalInterface
    public interface Effect {
        void apply(ConversationSession session, ServerPlayer actor);
    }

    public record Reply(String textKey, String text, @Nullable String next, List<Effect> effects) {
    }

    public record Node(String id, Speaker speaker, String textKey, String text, List<Reply> replies, @Nullable String next, List<Effect> effects) {
        public boolean asksForReply() {
            return !replies.isEmpty();
        }
    }

    private final String id;
    private final String first;
    private final Map<String, Node> nodes;

    private Talk(String id, String first, Map<String, Node> nodes) {
        this.id = id;
        this.first = first;
        this.nodes = nodes;
    }

    public String id() {
        return id;
    }

    public String first() {
        return first;
    }

    public Node node(String nodeId) {
        Node node = nodes.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("No line '" + nodeId + "' in talk '" + id + "'");
        }
        return node;
    }

    public boolean hasNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public List<Node> nodes() {
        return List.copyOf(nodes.values());
    }

    public static Builder of(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private final List<MutableNode> lines = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        /** A new line. Without replies, the talk goes on with the line written after it. */
        public Builder line(String nodeId, Speaker speaker, String text) {
            lines.add(new MutableNode(nodeId, speaker, text));
            return this;
        }

        /** A reply to the last line. A null {@code next} ends the talk. */
        public Builder reply(String text, @Nullable String next, Effect... effects) {
            last().replies.add(new MutableReply(text, next, List.of(effects)));
            return this;
        }

        /** The last line goes on with this one instead of the one written after it. */
        public Builder then(String next) {
            last().next = next;
            return this;
        }

        /** The talk ends after the last line. */
        public Builder end() {
            last().ends = true;
            return this;
        }

        /** Runs when the last line is reached. */
        public Builder does(Effect... effects) {
            last().effects.addAll(List.of(effects));
            return this;
        }

        private MutableNode last() {
            return lines.get(lines.size() - 1);
        }

        public Talk build() {
            Map<String, Node> nodes = new LinkedHashMap<>();
            String prefix = "story." + WallisUltimate.MOD_ID + ".talk." + id + ".";
            for (int i = 0; i < lines.size(); i++) {
                MutableNode line = lines.get(i);
                String key = prefix + line.id;
                List<Reply> replies = new ArrayList<>();
                for (int r = 0; r < line.replies.size(); r++) {
                    MutableReply reply = line.replies.get(r);
                    replies.add(new Reply(key + ".r" + r, reply.text, reply.next, reply.effects));
                }
                String next = line.next;
                if (next == null && !line.ends && replies.isEmpty() && i + 1 < lines.size()) {
                    next = lines.get(i + 1).id;
                }
                nodes.put(line.id, new Node(line.id, line.speaker, key, line.text, List.copyOf(replies), next, List.copyOf(line.effects)));
            }
            for (Node node : nodes.values()) {
                check(nodes, node.id, node.next);
                node.replies.forEach(reply -> check(nodes, node.id, reply.next));
            }
            return new Talk(id, lines.get(0).id, nodes);
        }

        private void check(Map<String, Node> nodes, String from, @Nullable String to) {
            if (to != null && !nodes.containsKey(to)) {
                throw new IllegalStateException("Talk '" + id + "': line '" + from + "' goes on to '" + to + "', which doesn't exist");
            }
        }
    }

    private static final class MutableNode {
        final String id;
        final Speaker speaker;
        final String text;
        final List<MutableReply> replies = new ArrayList<>();
        final List<Effect> effects = new ArrayList<>();
        @Nullable
        String next;
        boolean ends;

        MutableNode(String id, Speaker speaker, String text) {
            this.id = id;
            this.speaker = speaker;
            this.text = text;
        }
    }

    private record MutableReply(String text, @Nullable String next, List<Effect> effects) {
    }
}
