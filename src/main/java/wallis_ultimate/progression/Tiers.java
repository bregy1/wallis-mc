package wallis_ultimate.progression;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.bananemdnsa.historystages.data.saveddata.IndividualStageData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * PROG-001, PROG-004, PROG-008: the ordered tiers, defined in data (data/&lt;namespace&gt;/wallis_tiers/*.json), so a new planet
 * adds files without touching these. A team's tier isn't stored: it's the last tier whose stage the team has unlocked. Each tier
 * has one stage, unlocked by the previous tier's milestone quest, so History Stages' team sharing already gives joiners and
 * leavers the right tier (PROG-006), and there's exactly one current tier at any time.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Tiers extends SimpleJsonResourceReloadListener {
    public static final String MESSAGE_CURRENT = "message." + WallisUltimate.MOD_ID + ".tier.current";
    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "wallis_tiers";

    /**
     * @param stage     what opens this tier; empty for the first
     * @param milestone the quest that opens the next tier, as its FTB Quests id; empty until that tier's story is specified
     */
    public record Tier(ResourceLocation id, int order, String planet, String nameKey, String stage, String milestone) {
    }

    private static List<Tier> tiers = List.of();

    private Tiers() {
        super(GSON, FOLDER);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new Tiers());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<Tier> loaded = new ArrayList<>();
        files.forEach((id, element) -> {
            JsonObject json = GsonHelper.convertToJsonObject(element, id.toString());
            loaded.add(new Tier(id, GsonHelper.getAsInt(json, "order"), GsonHelper.getAsString(json, "planet"), GsonHelper.getAsString(json, "name"),
                    GsonHelper.getAsString(json, "stage", ""), GsonHelper.getAsString(json, "milestone", "")));
        });
        loaded.sort(Comparator.comparingInt(Tier::order));
        tiers = List.copyOf(loaded);
        WallisUltimate.LOGGER.info("Loaded {} tiers", tiers.size());
    }

    public static List<Tier> all() {
        return tiers;
    }

    /** The last tier whose stage counts as unlocked; the first tier if none does. */
    @Nullable
    public static Tier current(Predicate<String> stageUnlocked) {
        Tier current = null;
        for (Tier tier : tiers) {
            if (current == null || (!tier.stage().isEmpty() && stageUnlocked.test(tier.stage()))) {
                current = tier;
            }
        }
        return current;
    }

    @Nullable
    public static Tier current(ServerPlayer player) {
        IndividualStageData data = IndividualStageData.get(player.server.overworld());
        UUID id = player.getUUID();
        return current(stage -> data.hasStage(id, stage));
    }
}
