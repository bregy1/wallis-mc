package wallis_ultimate.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.item.BandageItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WallisUltimate.MOD_ID);

    // Block items exist so structures can be built in creative; there are no recipes or loot for them (WORLD-006).
    public static final RegistryObject<Item> REINFORCED_CONCRETE = blockItem(ModBlocks.REINFORCED_CONCRETE);
    public static final RegistryObject<Item> RIVETED_STEEL = blockItem(ModBlocks.RIVETED_STEEL);
    public static final RegistryObject<Item> ALIEN_ALLOY = blockItem(ModBlocks.ALIEN_ALLOY);

    /** SURV-010: stacks to 16 (Q-119). */
    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().stacksTo(16)));

    /** ENEMY-012, creative only. Dark green with red spots, so it doesn't look like vanilla's zombie egg. */
    public static final RegistryObject<Item> ZOMBIE_SPAWN_EGG = ITEMS.register("zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ZOMBIE, 0x3B4A2E, 0x7A1F1F, new Item.Properties()));

    private ModItems() {
    }

    private static RegistryObject<Item> blockItem(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(REINFORCED_CONCRETE);
            event.accept(RIVETED_STEEL);
            event.accept(ALIEN_ALLOY);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BANDAGE);
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ZOMBIE_SPAWN_EGG);
        }
    }
}
