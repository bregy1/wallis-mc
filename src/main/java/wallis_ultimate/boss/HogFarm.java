package wallis_ultimate.boss;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.HogFarmBuild;

/** ENEMY-011: where Hog Daddy's farm stands in this world, and where everything on it is. */
public final class HogFarm {
    private static Blueprint realBlueprint;

    private final BlockPos origin;
    private final Blueprint blueprint;

    public HogFarm(BlockPos origin, Blueprint blueprint) {
        this.origin = origin;
        this.blueprint = blueprint;
    }

    /** The farm of this world, or null until it's built. */
    @Nullable
    public static HogFarm of(ServerLevel overworld) {
        BlockPos origin = DistrictSites.get(overworld).hogFarm();
        if (origin == null) {
            return null;
        }
        if (realBlueprint == null) {
            realBlueprint = HogFarmBuild.blueprint();
        }
        return new HogFarm(origin, realBlueprint);
    }

    public BlockPos origin() {
        return origin;
    }

    public Blueprint blueprint() {
        return blueprint;
    }

    public BlockPos at(String mark) {
        return origin.offset(blueprint.mark(mark));
    }

    public Vec3 standingAt(String mark) {
        return Vec3.atBottomCenterOf(at(mark));
    }

    /** The whole footprint, ring included, at any height: nothing is mined or placed here, not even underneath. */
    public boolean contains(double x, double z) {
        return x >= origin.getX() && x < origin.getX() + blueprint.sizeX() && z >= origin.getZ() && z < origin.getZ() + blueprint.sizeZ();
    }

    /** Past the barn's south door, in the barn or the pen. */
    public boolean reachedBarn(Vec3 position) {
        return contains(position.x, position.z) && position.z <= at(HogFarmBuild.BARN_LINE).getZ() + 1;
    }

    public int pigletSpawns() {
        int count = 0;
        while (blueprint.mark(HogFarmBuild.PIGLET + count) != null) {
            count++;
        }
        return count;
    }

    public BlockPos pigletSpawn(int number) {
        return at(HogFarmBuild.PIGLET + number);
    }
}
