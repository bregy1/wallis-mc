package wallis_ultimate.story;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;

/**
 * STORY-003: the story's main NPCs, on Easy NPC. Each is one of Easy NPC's entities with our skin from the mod's own textures
 * (placeholder art). They stand where they're put and watch whoever comes near. Right-clicks and damage are handled in
 * {@link StoryEvents}, because Easy NPC's own dialog screen can't do team conversations.
 */
public final class StoryNpcs {
    public static final String TAG_NPC = WallisUltimate.MOD_ID + ".story_npc";
    public static final String TAG_LURLEEN = WallisUltimate.MOD_ID + ".lurleen";
    public static final String TAG_LURLEEN_CABIN = WallisUltimate.MOD_ID + ".lurleen_cabin";
    public static final String TAG_CARL = WallisUltimate.MOD_ID + ".carl";
    /** The cabin's Lurleen stands across the fire from where players wake up, relative to the cabin's corner. */
    public static final int LURLEEN_CABIN_X = 13;
    public static final int LURLEEN_CABIN_Y = 0;
    public static final int LURLEEN_CABIN_Z = 6;
    private static final float FACING_NORTH = 180.0F;
    private static final int FREE_SPOT_RADIUS = 5;
    private static final ResourceLocation NPC_SLIM = ResourceLocation.fromNamespaceAndPath("easy_npc", "humanoid_slim");
    private static final ResourceLocation NPC_WIDE = ResourceLocation.fromNamespaceAndPath("easy_npc", "humanoid");

    private StoryNpcs() {
    }

    public static boolean isStoryNpc(Entity entity) {
        return entity.getTags().contains(TAG_NPC);
    }

    public static Optional<Mob> spawnLurleen(ServerLevel level, BlockPos pos, boolean atCabin) {
        Optional<Mob> lurleen = spawn(level, NPC_SLIM, "lurleen", pos, FACING_NORTH);
        lurleen.ifPresent(mob -> {
            mob.addTag(TAG_LURLEEN);
            if (atCabin) {
                mob.addTag(TAG_LURLEEN_CABIN);
            }
        });
        return lurleen;
    }

    public static Optional<Mob> spawnCarl(ServerLevel level, BlockPos pos) {
        Optional<Mob> carl = spawn(level, NPC_WIDE, "conspiracy_carl", pos, FACING_NORTH);
        carl.ifPresent(mob -> mob.addTag(TAG_CARL));
        return carl;
    }

    /**
     * An Easy NPC set up the way /summon sets up one without data, then given its skin through the entity's data. Easy NPC
     * 's SkinDataEntry reads "SkinData" with type RESOURCE_LOCATION, so our own texture works without code against its classes.
     */
    private static Optional<Mob> spawn(ServerLevel level, ResourceLocation type, String name, BlockPos pos, float yaw) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(type);
        if (entityType == null || !(entityType.create(level) instanceof Mob mob)) {
            WallisUltimate.LOGGER.error("Easy NPC's {} isn't there, so {} can't appear", type, name);
            return Optional.empty();
        }
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
        mob.setYHeadRot(yaw);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null, null);
        CompoundTag data = mob.saveWithoutId(new CompoundTag());
        CompoundTag skin = new CompoundTag();
        skin.putString("Type", "RESOURCE_LOCATION");
        skin.putString("Texture", ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/npc/" + name + ".png").toString());
        data.put("SkinData", skin);
        mob.load(data);
        mob.setCustomName(Component.translatable("story." + WallisUltimate.MOD_ID + ".npc." + name));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.addTag(TAG_NPC);
        level.addFreshEntity(mob);
        return Optional.of(mob);
    }

    /** The closest place near a position where someone can stand, or null. */
    @Nullable
    public static BlockPos freeSpotNear(ServerLevel level, BlockPos around) {
        for (int radius = 0; radius <= FREE_SPOT_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                        continue;
                    }
                    for (int y = 1; y >= -2; y--) {
                        BlockPos pos = around.offset(x, y, z);
                        if (canStandAt(level, pos)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        AABB body = new AABB(pos.getX() + 0.2, pos.getY(), pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 1.9, pos.getZ() + 0.8);
        return below.isFaceSturdy(level, pos.below(), Direction.UP) && level.noCollision(body) && level.getFluidState(pos).isEmpty();
    }
}
