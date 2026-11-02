package wallis_ultimate.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WallisUltimate.MOD_ID);

    /** ENEMY-012: the regular zombie, sized like vanilla's. Natural spawns: forge/biome_modifier/*_zombies.json. */
    public static final RegistryObject<EntityType<RegularZombie>> ZOMBIE = ENTITY_TYPES.register("zombie",
            () -> EntityType.Builder.<RegularZombie>of(RegularZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(WallisUltimate.MOD_ID + ":zombie"));

    private ModEntities() {
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ZOMBIE.get(), RegularZombie.createAttributes().build());
    }

    /** Same spawn rules as vanilla zombies: on the ground, in the dark. */
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
