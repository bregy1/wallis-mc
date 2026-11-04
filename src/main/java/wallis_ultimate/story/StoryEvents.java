package wallis_ultimate.story;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * What connects the story to what players do: the opening on first joining, talking to NPCs, the team's first home
 * (WORLD-012), Lurleen moving in and reacting to things (STORY-003), and main NPCs not dying (SURV-011).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StoryEvents {
    /** Long enough for a joining player's client to have the world around them. */
    private static final int OPENING_DELAY_TICKS = 60;
    private static final int CHECK_EVERY_TICKS = 20;
    private static final long TICKS_PER_DAY = 24000L;
    /** Lurleen warns a team without a home about an in-game half hour before dark. */
    private static final long DUSK_FROM = 11000L;
    private static final long DUSK_TO = 12500L;
    private static final String CARL_SPOT = "camp_tarp";
    private static final int CARL_APPEARS_WITHIN = 96;
    /** A home this close to the cabin's Lurleen doesn't need one of its own. */
    private static final int SAME_PLACE_AS_CABIN = 24;
    private static final String FLAG_WARNED_DUSK = "warned_dusk";
    public static final String FLAG_TOLD_SAFE_SPOT = "told_safe_spot";
    private static final String FLAG_TOLD_RESPAWN = "told_respawn";

    private StoryEvents() {
    }

    /**
     * Whether the opening plays by itself when someone first joins. The smoke test's other groups want a quiet world, so it
     * only stays on there when the test asks for the story group; everyone else always gets it.
     */
    public static boolean openingPlaysOnJoin() {
        if (!Boolean.getBoolean("wallis_ultimate.smoketest")) {
            return true;
        }
        return List.of(System.getProperty("wallis_ultimate.smoketest.only", "").split(",")).contains("story");
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount >= OPENING_DELAY_TICKS && player.tickCount % CHECK_EVERY_TICKS == 0) {
            MinecraftServer server = player.server;
            StoryState state = StoryState.get(server);
            if (!state.sawOpening(player.getUUID()) && openingPlaysOnJoin() && !player.isSpectator()) {
                OpeningCutscene.start(player);
                return;
            }
            String team = Teams.teamKey(player);
            warnAtDusk(player, state, team);
            tellAboutSafeSpot(player, state, team);
            placeCarl(player, state);
        }
    }

    private static void warnAtDusk(ServerPlayer player, StoryState state, String team) {
        long time = player.level().getDayTime() % TICKS_PER_DAY;
        if (time >= DUSK_FROM && time < DUSK_TO && state.hasFlag(team, ChapterOneTalks.HEARD_INTRO) && state.home(team) == null
                && state.setFlag(team, FLAG_WARNED_DUSK)) {
            sayToTeam(player, "dusk");
        }
    }

    private static void tellAboutSafeSpot(ServerPlayer player, StoryState state, String team) {
        if (state.hasFlag(team, ChapterOneTalks.HEARD_INTRO) && StoryQuests.foundASafeSpot(player, team) && state.setFlag(team, FLAG_TOLD_SAFE_SPOT)) {
            sayToTeam(player, "safe_spot");
        }
    }

    private static void sayToTeam(ServerPlayer player, String line) {
        Transmissions.fromLurleen(player, line);
        Teams.onlineTeammates(player).forEach(teammate -> Transmissions.fromLurleen(teammate, line));
    }

    /** Carl waits at Camp Tarp, in its chunks: he's only put there once someone is close enough for them to be loaded. */
    private static void placeCarl(ServerPlayer player, StoryState state) {
        if (state.npcSpawned(StoryNpcs.TAG_CARL) || player.level().dimension() != Level.OVERWORLD) {
            return;
        }
        ServerLevel level = player.serverLevel();
        DistrictSites.get(level).spot(CARL_SPOT).ifPresent(spot -> {
            if (spot.center().distSqr(player.blockPosition()) <= CARL_APPEARS_WITHIN * CARL_APPEARS_WITHIN) {
                BlockPos pos = StoryNpcs.freeSpotNear(level, carlSpot(spot));
                if (pos != null && StoryNpcs.spawnCarl(level, pos).isPresent()) {
                    state.markNpcSpawned(StoryNpcs.TAG_CARL);
                }
            }
        });
    }

    /** Beside where travellers arrive: nobody can miss him, which is how he likes it. */
    private static BlockPos carlSpot(SafeSpot spot) {
        return spot.arrival().offset(2, 0, 2);
    }

    /** STORY-003: right-clicking a story NPC talks to them. Canceled here, so Easy NPC's own dialog never opens. */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!StoryNpcs.isStoryNpc(target)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        if (target.getTags().contains(StoryNpcs.TAG_LURLEEN)) {
            LurleenTopics.talkTo(player, target.getTags().contains(StoryNpcs.TAG_LURLEEN_CABIN));
        } else if (target.getTags().contains(StoryNpcs.TAG_CARL)) {
            String team = Teams.teamKey(player);
            boolean talked = StoryState.get(player.server).hasFlag(team, ChapterOneTalks.TALKED_CARL);
            Conversations.start(player, ChapterOneTalks.talk(talked ? ChapterOneTalks.CARL_AGAIN : ChapterOneTalks.CARL_FIRST));
        }
    }

    /** Chapter 1's "Radio voices": the podcast from the bunker the first time, then whatever he's rambling about by now. */
    public static void listenToRadio(ServerPlayer player) {
        boolean heard = StoryState.get(player.server).hasFlag(Teams.teamKey(player), ChapterOneTalks.HEARD_RADIO);
        Conversations.start(player, ChapterOneTalks.talk(heard ? ChapterOneTalks.RADIO_LATER : ChapterOneTalks.RADIO_FIRST));
    }

    /** SURV-011: main NPCs can't die. Works for anything that isn't /kill. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (StoryNpcs.isStoryNpc(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /**
     * WORLD-012: the first bed anyone in a team places is where its first home is. That finishes "Create First Home", and
     * Lurleen moves in. After Beds, so a refused bed doesn't count.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level
                && event.getPlacedBlock().getBlock() instanceof BedBlock && !player.isCreative() && !player.isSpectator()) {
            settleIn(player, level, event.getPos());
        }
    }

    /** Public for GameTests and the story command. */
    public static void settleIn(ServerPlayer player, ServerLevel level, BlockPos bed) {
        StoryState state = StoryState.get(player.server);
        String team = Teams.teamKey(player);
        // Only Lurleen hands out beds, so a team that hasn't met her isn't settling down. (That's a bed from a creative friend or a command.)
        if (state.home(team) != null || !state.hasFlag(team, ChapterOneTalks.HEARD_INTRO)) {
            return;
        }
        state.setHome(team, bed);
        BlockPos spot = StoryNpcs.freeSpotNear(level, bed);
        BlockPos cabin = DistrictSites.get(player.server.overworld()).cabin();
        boolean closeToCabin = cabin != null && level.dimension() == Level.OVERWORLD
                && cabin.offset(StoryNpcs.LURLEEN_CABIN_X, StoryNpcs.LURLEEN_CABIN_Y, StoryNpcs.LURLEEN_CABIN_Z).distSqr(bed) <= SAME_PLACE_AS_CABIN * SAME_PLACE_AS_CABIN;
        if (spot != null && !closeToCabin) {
            StoryNpcs.spawnLurleen(level, spot, false);
        }
        sayToTeam(player, "home");
    }

    /** A player's death is what she has an opinion on next, once. The grave is what they'd forget. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isEndConquered()) {
            StoryState state = StoryState.get(player.server);
            String team = Teams.teamKey(player);
            if (state.hasFlag(team, ChapterOneTalks.HEARD_INTRO) && state.setFlag(team, FLAG_TOLD_RESPAWN)) {
                Transmissions.fromLurleen(player, "respawn");
            }
        }
    }

    /** The seam with the boss: Hog Daddy's death counts however he died, for every team with someone near. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (StoryQuests.isHogDaddy(event.getEntity()) && event.getEntity().level() instanceof ServerLevel level) {
            bossDied(level, event.getEntity());
        }
    }

    /** Public for GameTests: on this branch the boss itself doesn't exist. */
    public static void bossDied(ServerLevel level, Entity boss) {
        double radius = StoryQuests.hogKillRadius();
        for (Player nearby : level.players()) {
            if (nearby instanceof ServerPlayer player && player.distanceToSqr(boss) <= radius * radius) {
                StoryQuests.bossDied(player);
            }
        }
    }
}
