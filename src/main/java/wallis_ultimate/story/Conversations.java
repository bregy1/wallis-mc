package wallis_ultimate.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.ConversationPacket;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.team.Teams;

/**
 * STORY-002: conversations for the whole team. Every member who is there sees the lines; each time a reply is needed, one
 * of them is picked at random to choose it. Easy NPC's own dialog screens only ever talk to the player who clicked, so
 * NPCs on Easy NPC (STORY-003) open these instead. Only one conversation per team runs at a time.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Conversations {
    /** "Present" (proposal): online, same dimension, this close to where the talk started. */
    public static final double PRESENT_RADIUS = 32.0;
    private static final int MEMBERSHIP_CHECK_TICKS = 20;
    private static final Map<String, Running> RUNNING = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static int nextId = 1;

    private record Running(ConversationSession session, BlockPos anchor, ResourceKey<Level> dimension) {
    }

    private Conversations() {
    }

    /** Starts a talk for the player's team. False if the team is already in one. */
    public static boolean start(ServerPlayer lead, Talk talk, String node, boolean protectsPlayers) {
        String team = Teams.teamKey(lead);
        if (RUNNING.containsKey(team)) {
            return false;
        }
        List<UUID> present = new ArrayList<>();
        presentMembers(lead, lead.blockPosition()).forEach(member -> present.add(member.getUUID()));
        ConversationSession session = new ConversationSession(nextId++, talk, team, protectsPlayers, lead.getUUID(), present);
        RUNNING.put(team, new Running(session, lead.blockPosition(), lead.level().dimension()));
        go(lead.server, session, node);
        return true;
    }

    public static boolean start(ServerPlayer lead, Talk talk) {
        return start(lead, talk, talk.first(), false);
    }

    /** The online members of the player's team who are near a place, the player included if they are. */
    public static List<ServerPlayer> presentMembers(ServerPlayer player, BlockPos around) {
        List<ServerPlayer> present = new ArrayList<>();
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if ((other == player || Teams.sameTeam(player, other)) && isPresent(other, around, player.level().dimension())) {
                present.add(other);
            }
        }
        return present;
    }

    private static boolean isPresent(ServerPlayer player, BlockPos around, ResourceKey<Level> dimension) {
        return player.level().dimension() == dimension && !player.isSpectator()
                && player.blockPosition().distSqr(around) <= PRESENT_RADIUS * PRESENT_RADIUS;
    }

    public static boolean isInConversation(ServerPlayer player) {
        return session(player) != null;
    }

    @Nullable
    private static ConversationSession session(ServerPlayer player) {
        Running running = RUNNING.get(Teams.teamKey(player));
        return running != null && running.session().participants().contains(player.getUUID()) ? running.session() : null;
    }

    private static void go(MinecraftServer server, ConversationSession session, @Nullable String node) {
        if (node == null) {
            finish(server, session);
            return;
        }
        session.enter(node, RANDOM);
        ServerPlayer actor = actor(server, session);
        if (actor != null) {
            session.node().effects().forEach(effect -> effect.apply(session, actor));
        }
        sendToAll(server, session);
    }

    /** Effects run as the lead, who is the one always online unless the whole talk is over. */
    @Nullable
    private static ServerPlayer actor(MinecraftServer server, ConversationSession session) {
        return server.getPlayerList().getPlayer(session.lead());
    }

    private static void finish(MinecraftServer server, ConversationSession session) {
        session.end();
        RUNNING.remove(session.team());
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                send(player, ConversationPacket.closed(session.id()));
            }
        }
    }

    private static void sendToAll(MinecraftServer server, ConversationSession session) {
        if (session.ended()) {
            return;
        }
        Talk.Node node = session.node();
        ServerPlayer chooser = session.chooser() == null ? null : server.getPlayerList().getPlayer(session.chooser());
        String chooserName = chooser == null ? "" : chooser.getGameProfile().getName();
        List<String> replies = node.replies().stream().map(Talk.Reply::textKey).toList();
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                send(player, new ConversationPacket(session.id(), true, node.speaker().nameKey(), node.speaker().color(), node.textKey(), replies,
                        chooserName, id.equals(session.chooser()), id.equals(session.lead()), session.lineTicks()));
            }
        }
    }

    private static void send(ServerPlayer player, ConversationPacket packet) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** The picked member chose a reply. The client only asks; the server checks it's really their turn. */
    public static void answer(ServerPlayer player, int sessionId, int reply) {
        ConversationSession session = session(player);
        if (session != null && session.id() == sessionId && session.mayAnswer(player.getUUID(), reply)) {
            Talk.Reply chosen = session.node().replies().get(reply);
            chosen.effects().forEach(effect -> effect.apply(session, player));
            go(player.server, session, chosen.next());
        }
    }

    /** Whoever started the talk read faster than the line's time. */
    public static void hurry(ServerPlayer player, int sessionId) {
        ConversationSession session = session(player);
        if (session != null && session.id() == sessionId && session.mayHurry(player.getUUID())) {
            go(player.server, session, session.afterLine());
        }
    }

    /** The player closed the screen. The others go on without them; alone, that ends the talk. Talk to the NPC again to restart it. */
    public static void leave(ServerPlayer player, int sessionId) {
        ConversationSession session = session(player);
        if (session != null && session.id() == sessionId) {
            drop(player.server, session, player.getUUID());
        }
    }

    private static void drop(MinecraftServer server, ConversationSession session, UUID id) {
        boolean changed = session.remove(id, RANDOM);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            send(player, ConversationPacket.closed(session.id()));
        }
        if (session.ended()) {
            RUNNING.remove(session.team());
        } else if (changed) {
            sendToAll(server, session);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || RUNNING.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        boolean checkMembers = server.getTickCount() % MEMBERSHIP_CHECK_TICKS == 0;
        for (Running running : List.copyOf(RUNNING.values())) {
            ConversationSession session = running.session();
            if (checkMembers) {
                updateMembers(server, running);
                if (session.ended()) {
                    continue;
                }
            }
            if (session.tick()) {
                go(server, session, session.afterLine());
            } else if (session.chooserTimedOut()) {
                session.passChooser(RANDOM);
                sendToAll(server, session);
            }
        }
    }

    /** Who's still there, and who has come over. */
    private static void updateMembers(MinecraftServer server, Running running) {
        ConversationSession session = running.session();
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !isPresent(player, running.anchor(), running.dimension())) {
                drop(server, session, id);
                if (session.ended()) {
                    return;
                }
            }
        }
        ServerPlayer lead = server.getPlayerList().getPlayer(session.lead());
        if (lead != null) {
            boolean joined = false;
            for (ServerPlayer member : presentMembers(lead, running.anchor())) {
                joined |= session.add(member.getUUID());
            }
            if (joined) {
                sendToAll(server, session);
            }
        }
    }

    /** STORY-010: the talk that's part of a cutscene carries on its promise that nobody in it gets hurt. */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ConversationSession session = session(player);
            if (session != null && session.protectsPlayers()) {
                event.setCanceled(true);
            }
        }
    }

    /** A talk doesn't survive a restart: talk to the NPC again. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        RUNNING.clear();
    }
}
