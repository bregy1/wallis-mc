package wallis_ultimate.story;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import wallis_ultimate.WallisUltimate;

/**
 * PROG-006, Q-136: what each team has done in the story that FTB Quests doesn't track: which conversations it has been
 * through and where its first home is. Quest progress itself stays in FTB Quests. Also remembers who has seen the opening
 * (per player: everyone starts in a team of their own) and which of the single-place NPCs exist already.
 */
public final class StoryState extends SavedData {
    private static final String DATA_NAME = WallisUltimate.MOD_ID + "_story";

    private final Map<String, Set<String>> flags = new HashMap<>();
    private final Map<String, BlockPos> homes = new HashMap<>();
    private final Set<UUID> sawOpening = new HashSet<>();
    private final Set<String> spawnedNpcs = new HashSet<>();

    private StoryState() {
    }

    public static StoryState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StoryState::load, StoryState::new, DATA_NAME);
    }

    private static StoryState load(CompoundTag tag) {
        StoryState state = new StoryState();
        CompoundTag teams = tag.getCompound("Flags");
        for (String team : teams.getAllKeys()) {
            Set<String> set = new HashSet<>();
            teams.getList(team, Tag.TAG_STRING).forEach(flag -> set.add(flag.getAsString()));
            state.flags.put(team, set);
        }
        CompoundTag homes = tag.getCompound("Homes");
        for (String team : homes.getAllKeys()) {
            state.homes.put(team, NbtUtils.readBlockPos(homes.getCompound(team)));
        }
        tag.getList("SawOpening", Tag.TAG_STRING).forEach(id -> state.sawOpening.add(UUID.fromString(id.getAsString())));
        tag.getList("SpawnedNpcs", Tag.TAG_STRING).forEach(id -> state.spawnedNpcs.add(id.getAsString()));
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag teams = new CompoundTag();
        flags.forEach((team, set) -> {
            ListTag list = new ListTag();
            set.forEach(flag -> list.add(StringTag.valueOf(flag)));
            teams.put(team, list);
        });
        tag.put("Flags", teams);
        CompoundTag homeTags = new CompoundTag();
        homes.forEach((team, pos) -> homeTags.put(team, NbtUtils.writeBlockPos(pos)));
        tag.put("Homes", homeTags);
        ListTag seen = new ListTag();
        sawOpening.forEach(id -> seen.add(StringTag.valueOf(id.toString())));
        tag.put("SawOpening", seen);
        ListTag npcs = new ListTag();
        spawnedNpcs.forEach(id -> npcs.add(StringTag.valueOf(id)));
        tag.put("SpawnedNpcs", npcs);
        return tag;
    }

    public boolean hasFlag(String team, String flag) {
        return flags.getOrDefault(team, Set.of()).contains(flag);
    }

    /** True if the team didn't have it yet. */
    public boolean setFlag(String team, String flag) {
        boolean added = flags.computeIfAbsent(team, key -> new HashSet<>()).add(flag);
        if (added) {
            setDirty();
        }
        return added;
    }

    @Nullable
    public BlockPos home(String team) {
        return homes.get(team);
    }

    public void setHome(String team, BlockPos pos) {
        homes.put(team, pos.immutable());
        setDirty();
    }

    public boolean sawOpening(UUID player) {
        return sawOpening.contains(player);
    }

    public void markOpeningSeen(UUID player) {
        if (sawOpening.add(player)) {
            setDirty();
        }
    }

    public boolean npcSpawned(String id) {
        return spawnedNpcs.contains(id);
    }

    public void markNpcSpawned(String id) {
        if (spawnedNpcs.add(id)) {
            setDirty();
        }
    }

    /** PROG-006: a player leaving a team keeps a copy of what the team had done. */
    public void copyTeam(String from, String to) {
        flags.put(to, new HashSet<>(flags.getOrDefault(from, Set.of())));
        BlockPos home = homes.get(from);
        if (home != null) {
            homes.put(to, home);
        }
        setDirty();
    }

    /**
     * A player joining a team. FTB Quests adds the joiner's finished quests to the team's, so their story flags come along
     * too, or the team's conversations would no longer match its quests. The team's home stays, if it has one.
     */
    public void mergeInto(String joiner, String team) {
        flags.computeIfAbsent(team, key -> new HashSet<>()).addAll(flags.getOrDefault(joiner, Set.of()));
        BlockPos joinerHome = homes.get(joiner);
        if (joinerHome != null) {
            homes.putIfAbsent(team, joinerHome);
        }
        setDirty();
    }
}
