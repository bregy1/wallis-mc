package wallis_ultimate.boss;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import wallis_ultimate.WallisUltimate;

/**
 * ENEMY-011: what the world remembers about Hog Daddy. Which teams have called him and not beaten him yet (Q-136: every
 * team has its own story state), which have beaten him, and whether a fight was going on when the server stopped: a
 * fight isn't saved, so the farm is put back at the next start.
 */
public final class HogDaddyProgress extends SavedData {
    private static final String DATA_NAME = WallisUltimate.MOD_ID + "_hog_daddy";
    private final Set<String> called = new HashSet<>();
    private final Set<String> beaten = new HashSet<>();
    private boolean fightRunning;

    private HogDaddyProgress() {
    }

    public static HogDaddyProgress get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(HogDaddyProgress::load, HogDaddyProgress::new, DATA_NAME);
    }

    private static HogDaddyProgress load(CompoundTag tag) {
        HogDaddyProgress progress = new HogDaddyProgress();
        tag.getList("Called", Tag.TAG_STRING).forEach(team -> progress.called.add(team.getAsString()));
        tag.getList("Beaten", Tag.TAG_STRING).forEach(team -> progress.beaten.add(team.getAsString()));
        progress.fightRunning = tag.getBoolean("FightRunning");
        return progress;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("Called", names(called));
        tag.put("Beaten", names(beaten));
        tag.putBoolean("FightRunning", fightRunning);
        return tag;
    }

    private static ListTag names(Set<String> teams) {
        ListTag list = new ListTag();
        teams.forEach(team -> list.add(StringTag.valueOf(team)));
        return list;
    }

    public boolean hasCalled(String team) {
        return called.contains(team);
    }

    public void call(String team) {
        if (called.add(team)) {
            setDirty();
        }
    }

    public boolean hasBeaten(String team) {
        return beaten.contains(team);
    }

    public void markBeaten(String team) {
        called.remove(team);
        beaten.add(team);
        setDirty();
    }

    public boolean fightRunning() {
        return fightRunning;
    }

    public void setFightRunning(boolean running) {
        fightRunning = running;
        setDirty();
    }
}
