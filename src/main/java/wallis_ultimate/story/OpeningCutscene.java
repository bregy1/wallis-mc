package wallis_ultimate.story;

import com.immersivecinematics.immersive_cinematics.trigger.server.ScriptEventManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;

/**
 * STORY-002, STORY-010: the comedy opening. The camera runs on ImmersiveCinematics (pack/immersive_cinematics/scripts/
 * wallis_opening.json), which also handles the skip vote: it ends when everyone watching held the skip key. The rest happens
 * here, on the server, in step with it: the lines on screen, "Darryl" stumbling up, the shot, Lurleen. While it runs, nobody in
 * it can be hurt and enemies nearby hold still. When it ends, Lurleen's first conversation starts by itself.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OpeningCutscene {
    public static final String SCRIPT = "wallis_opening";
    public static final String TAG_ACTOR = WallisUltimate.MOD_ID + ".cutscene_actor";
    /** How long the script runs. */
    public static final int LENGTH_TICKS = 27 * 20;
    /** Clients start a moment late and report the end a moment later still. After this, a client that never reported is ignored. */
    private static final int END_GRACE_TICKS = 100;
    /** A client that hasn't started the script after this long is treated as if it had finished. */
    private static final int START_GRACE_TICKS = 100;
    private static final int HOLD_STILL_RADIUS = 48;

    private static final int DARRYL_APPEARS = 12 * 20;
    private static final int DARRYL_WALK_EVERY = 20;
    private static final int SHOT = 21 * 20;
    private static final int LURLEEN_APPEARS = 21 * 20 + 10;
    /** Relative to the cabin's corner: he comes round the truck and toward the fire. */
    private static final int[] DARRYL_FROM = {20, 0, 6};
    private static final int[] DARRYL_TO = {15, 0, 5};
    /** The antlers over the door are what the script's camera is placed against. */
    private static final int[] ANTLERS = {13, 4, 12};

    private record Beat(int tick, String caption, int ticks) {
    }

    private static final List<Beat> BEATS = List.of(
            new Beat(30, "weekend", 90),
            new Beat(125, "boot", 110),
            new Beat(240, "darryl", 58),
            new Beat(300, "always", 68),
            new Beat(370, "can", 50),
            new Beat(SHOT, "blam", 30),
            new Beat(SHOT + 35, "who", 80));

    /** One showing, for everyone present when it started. */
    private static final class Showing {
        final List<UUID> viewers = new ArrayList<>();
        final List<UUID> started = new ArrayList<>();
        final List<UUID> finished = new ArrayList<>();
        final BlockPos cabin;
        UUID lead;
        int tick;
        @Nullable
        Mob darryl;

        Showing(BlockPos cabin) {
            this.cabin = cabin;
        }
    }

    private static final List<Showing> SHOWINGS = new ArrayList<>();

    private OpeningCutscene() {
    }

    /** Is a player in a showing right now? */
    public static boolean isWatching(ServerPlayer player) {
        return SHOWINGS.stream().anyMatch(showing -> showing.viewers.contains(player.getUUID()));
    }

    /** How far along the showing a player is in, in ticks; -1 if they aren't in one. For tests. */
    public static int tick(ServerPlayer player) {
        return SHOWINGS.stream().filter(showing -> showing.viewers.contains(player.getUUID())).mapToInt(showing -> showing.tick).findFirst().orElse(-1);
    }

    /** Starts the opening for the player and every team member with them, unless one of them is already in one. */
    public static boolean start(ServerPlayer lead) {
        MinecraftServer server = lead.server;
        ServerLevel overworld = server.overworld();
        BlockPos cabin = DistrictSites.get(overworld).cabin();
        if (cabin == null || isWatching(lead)) {
            return false;
        }
        StoryState state = StoryState.get(server);
        // The script's camera needs the antlers. Somebody may have mined them: then it's only the conversation.
        boolean canPlay = overworld.getBlockState(cabin.offset(ANTLERS[0], ANTLERS[1], ANTLERS[2])).is(ModBlocks.ANTLERS.get());
        BlockPos spawn = DistrictSites.get(overworld).spawn();
        List<ServerPlayer> present = Conversations.presentMembers(lead, spawn);
        if (present.isEmpty()) {
            present = List.of(lead);
        }
        Showing showing = new Showing(cabin);
        showing.lead = lead.getUUID();
        for (ServerPlayer viewer : present) {
            state.markOpeningSeen(viewer.getUUID());
            showing.viewers.add(viewer.getUUID());
            if (canPlay) {
                // The script finds its antlers around the command's position, so the command is run from the spawn.
                CommandSourceStack source = viewer.createCommandSourceStack().withPosition(Vec3.atCenterOf(spawn)).withPermission(2).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, "icinematics play " + SCRIPT + " " + viewer.getGameProfile().getName());
            }
        }
        if (!canPlay) {
            ensureCabinLurleen(server);
            Conversations.start(lead, ChapterOneTalks.talk(ChapterOneTalks.INTRO), ChapterOneTalks.talk(ChapterOneTalks.INTRO).first(), true);
            return true;
        }
        SHOWINGS.add(showing);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SHOWINGS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (Showing showing : List.copyOf(SHOWINGS)) {
            run(server, showing);
        }
    }

    private static void run(MinecraftServer server, Showing showing) {
        showing.tick++;
        ServerLevel level = server.overworld();
        for (Beat beat : BEATS) {
            if (beat.tick() == showing.tick) {
                caption(server, showing, beat);
            }
        }
        holdEnemiesStill(level, showing);
        if (showing.tick == DARRYL_APPEARS) {
            showing.darryl = spawnDarryl(level, showing.cabin);
        }
        if (showing.darryl != null && showing.tick % DARRYL_WALK_EVERY == 0 && showing.tick < SHOT) {
            showing.darryl.setTarget(null);
            BlockPos to = showing.cabin.offset(DARRYL_TO[0], DARRYL_TO[1], DARRYL_TO[2]);
            showing.darryl.getNavigation().moveTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5, 1.0);
        }
        if (showing.tick == SHOT) {
            shoot(level, showing);
        }
        if (showing.tick == LURLEEN_APPEARS) {
            ensureCabinLurleen(server);
        }
        if (showing.tick >= LENGTH_TICKS + END_GRACE_TICKS || everyoneDone(server, showing)) {
            finish(server, showing);
        }
    }

    /** ImmersiveCinematics knows who is still watching: it ends a showing early when the skip vote passes. */
    private static boolean everyoneDone(MinecraftServer server, Showing showing) {
        ScriptEventManager manager = ScriptEventManager.INSTANCE;
        for (UUID viewer : showing.viewers) {
            ServerPlayer player = server.getPlayerList().getPlayer(viewer);
            if (player == null) {
                continue;
            }
            if (manager.isPlayerPlayingScript(viewer, SCRIPT)) {
                if (!showing.started.contains(viewer)) {
                    showing.started.add(viewer);
                }
                return false;
            }
            if (!showing.started.contains(viewer) && showing.tick < START_GRACE_TICKS) {
                return false;
            }
        }
        return true;
    }

    private static void caption(MinecraftServer server, Showing showing, Beat beat) {
        for (UUID viewer : showing.viewers) {
            ServerPlayer player = server.getPlayerList().getPlayer(viewer);
            if (player != null) {
                Transmissions.caption(player, Speaker.NARRATOR, captionKey(beat.caption()), beat.ticks());
            }
        }
    }

    public static String captionKey(String name) {
        return "story." + WallisUltimate.MOD_ID + ".caption.opening." + name;
    }

    /** The mob's own AI would go for the nearest player, so it's kept pointed at where the scene wants it. */
    @Nullable
    private static Mob spawnDarryl(ServerLevel level, BlockPos cabin) {
        Mob darryl = ModEntities.ZOMBIE.get().create(level);
        if (darryl == null) {
            return null;
        }
        BlockPos from = cabin.offset(DARRYL_FROM[0], DARRYL_FROM[1], DARRYL_FROM[2]);
        darryl.moveTo(from.getX() + 0.5, from.getY(), from.getZ() + 0.5, 90.0F, 0.0F);
        darryl.setPersistenceRequired();
        darryl.addTag(TAG_ACTOR);
        level.addFreshEntity(darryl);
        return darryl;
    }

    private static void shoot(ServerLevel level, Showing showing) {
        BlockPos shooter = showing.cabin.offset(StoryNpcs.LURLEEN_CABIN_X, StoryNpcs.LURLEEN_CABIN_Y, StoryNpcs.LURLEEN_CABIN_Z);
        level.playSound(null, shooter, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 1.5F);
        level.sendParticles(ParticleTypes.SMOKE, shooter.getX() + 0.5, shooter.getY() + 1.4, shooter.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.01);
        if (showing.darryl != null && showing.darryl.isAlive()) {
            level.sendParticles(ParticleTypes.POOF, showing.darryl.getX(), showing.darryl.getY() + 1.2, showing.darryl.getZ(), 12, 0.2, 0.4, 0.2, 0.02);
            showing.darryl.discard();
        }
        showing.darryl = null;
    }

    public static void ensureCabinLurleen(MinecraftServer server) {
        StoryState state = StoryState.get(server);
        ServerLevel level = server.overworld();
        BlockPos cabin = DistrictSites.get(level).cabin();
        if (cabin == null || state.npcSpawned(StoryNpcs.TAG_LURLEEN_CABIN)) {
            return;
        }
        BlockPos pos = cabin.offset(StoryNpcs.LURLEEN_CABIN_X, StoryNpcs.LURLEEN_CABIN_Y, StoryNpcs.LURLEEN_CABIN_Z);
        if (StoryNpcs.spawnLurleen(level, pos, true).isPresent()) {
            state.markNpcSpawned(StoryNpcs.TAG_LURLEEN_CABIN);
        }
    }

    private static void finish(MinecraftServer server, Showing showing) {
        SHOWINGS.remove(showing);
        if (showing.darryl != null) {
            showing.darryl.discard();
        }
        ensureCabinLurleen(server);
        ServerPlayer lead = server.getPlayerList().getPlayer(showing.lead);
        if (lead == null) {
            lead = showing.viewers.stream().map(server.getPlayerList()::getPlayer).filter(player -> player != null).findFirst().orElse(null);
        }
        if (lead != null) {
            Talk intro = ChapterOneTalks.talk(ChapterOneTalks.INTRO);
            if (!StoryState.get(server).hasFlag(Teams.teamKey(lead), ChapterOneTalks.HEARD_INTRO)) {
                Conversations.start(lead, intro, intro.first(), true);
            }
        }
    }

    /** STORY-010: enemies near a showing hold still. Nothing is saved on them, so a crash can't leave a frozen zombie behind. */
    private static void holdEnemiesStill(ServerLevel level, Showing showing) {
        AABB area = new AABB(showing.cabin).inflate(HOLD_STILL_RADIUS);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area, mob -> mob instanceof Enemy && !mob.getTags().contains(TAG_ACTOR))) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
        }
    }

    /** STORY-010: nobody in a showing gets hurt. */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isWatching(player)) {
            event.setCanceled(true);
        }
    }

    /** Darryl stumbles past the players without going for them. */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!SHOWINGS.isEmpty() && event.getEntity().getTags().contains(TAG_ACTOR)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SHOWINGS.clear();
    }
}
