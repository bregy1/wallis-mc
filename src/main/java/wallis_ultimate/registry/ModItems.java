package wallis_ultimate.registry;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import wallis_ultimate.item.TrinketItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WallisUltimate.MOD_ID);

    // Block items exist so structures can be built in creative; there are no recipes or loot for them (WORLD-006).
    public static final RegistryObject<Item> REINFORCED_CONCRETE = blockItem(ModBlocks.REINFORCED_CONCRETE);
    public static final RegistryObject<Item> RIVETED_STEEL = blockItem(ModBlocks.RIVETED_STEEL);
    public static final RegistryObject<Item> ALIEN_ALLOY = blockItem(ModBlocks.ALIEN_ALLOY);

    /** SURV-010: stacks to 16 (Q-119). */
    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().stacksTo(16)));

    // CMBT-003, Chapter 1 trinkets. Their Curios slots are the item tags in datagen; numbers in Trinkets or here.
    public static final RegistryObject<Item> GRANNYS_RABBIT_FOOT = ITEMS.register("grannys_rabbit_foot", TrinketItem::new);
    public static final RegistryObject<Item> JOGGING_HEADBAND = ITEMS.register("jogging_headband",
            () -> new TrinketItem(Attributes.MOVEMENT_SPEED, 0.10, AttributeModifier.Operation.MULTIPLY_BASE));
    public static final RegistryObject<Item> WORN_SPURS = ITEMS.register("worn_spurs", TrinketItem::new);
    public static final RegistryObject<Item> LUCKY_HORSESHOE = ITEMS.register("lucky_horseshoe", TrinketItem::new);
    public static final RegistryObject<Item> SPARE_CLIP = ITEMS.register("spare_clip", TrinketItem::new);
    public static final RegistryObject<Item> BRASS_KNUCKLES = ITEMS.register("brass_knuckles",
            () -> new TrinketItem(Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<Item> THICK_FLANNEL = ITEMS.register("thick_flannel",
            () -> new TrinketItem(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADDITION));

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
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(GRANNYS_RABBIT_FOOT);
            event.accept(JOGGING_HEADBAND);
            event.accept(WORN_SPURS);
            event.accept(LUCKY_HORSESHOE);
            event.accept(SPARE_CLIP);
            event.accept(BRASS_KNUCKLES);
            event.accept(THICK_FLANNEL);
        }
    }
}
