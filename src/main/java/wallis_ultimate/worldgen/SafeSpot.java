package wallis_ultimate.worldgen;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import wallis_ultimate.WallisUltimate;

/**
 * SURV-006: a safe spot that has been built somewhere in the world. Its area is a circle, at any height. Its id is also
 * its site, and origin is where its blueprint's corner was placed; null for test zones, which have neither.
 */
public record SafeSpot(String id, BlockPos center, int radius, BlockPos signpost, List<BlockPos> beds, @Nullable BlockPos origin) {
    public SafeSpot(String id, BlockPos center, int radius, BlockPos signpost, List<BlockPos> beds) {
        this(id, center, radius, signpost, beds, null);
    }

    public boolean contains(double x, double z) {
        double dx = x - center.getX();
        double dz = z - center.getZ();
        return dx * dx + dz * dz <= (double) radius * radius;
    }

    /** Where travellers arrive: right in front of the signpost. Every safe spot keeps this place free. */
    public BlockPos arrival() {
        return signpost.south();
    }

    public String nameKey() {
        return "safe_spot." + WallisUltimate.MOD_ID + "." + id;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.put("Center", NbtUtils.writeBlockPos(center));
        tag.putInt("Radius", radius);
        tag.put("Signpost", NbtUtils.writeBlockPos(signpost));
        ListTag bedTags = new ListTag();
        beds.forEach(bed -> bedTags.add(NbtUtils.writeBlockPos(bed)));
        tag.put("Beds", bedTags);
        if (origin != null) {
            tag.put("Origin", NbtUtils.writeBlockPos(origin));
        }
        return tag;
    }

    static SafeSpot load(CompoundTag tag) {
        List<BlockPos> beds = new ArrayList<>();
        tag.getList("Beds", Tag.TAG_COMPOUND).forEach(bed -> beds.add(NbtUtils.readBlockPos((CompoundTag) bed)));
        return new SafeSpot(tag.getString("Id"), NbtUtils.readBlockPos(tag.getCompound("Center")), tag.getInt("Radius"),
                NbtUtils.readBlockPos(tag.getCompound("Signpost")), beds, tag.contains("Origin") ? NbtUtils.readBlockPos(tag.getCompound("Origin")) : null);
    }
}
