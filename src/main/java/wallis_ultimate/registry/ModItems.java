package wallis_ultimate.registry;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.WeaponEffects;
import wallis_ultimate.item.BandageItem;
import wallis_ultimate.item.HeavyShovelItem;
import wallis_ultimate.item.ScavengedWeaponItem;
import wallis_ultimate.item.TrinketItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WallisUltimate.MOD_ID);
    /** Read here so the tier registers while the mod is being constructed, as Forge requires. */
    private static final Tier COPPER = ModTiers.COPPER;

    // Block items exist so structures can be built in creative; there are no recipes or loot for them (WORLD-006).
    public static final RegistryObject<Item> REINFORCED_CONCRETE = blockItem(ModBlocks.REINFORCED_CONCRETE);
    public static final RegistryObject<Item> RIVETED_STEEL = blockItem(ModBlocks.RIVETED_STEEL);
    public static final RegistryObject<Item> ALIEN_ALLOY = blockItem(ModBlocks.ALIEN_ALLOY);

    /** SURV-010: stacks to 16 (Q-119). */
    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().stacksTo(16)));

    // GEAR-006: copper tools, between stone and iron. Attack values follow vanilla's stone and iron tools.
    public static final RegistryObject<Item> COPPER_PICKAXE = ITEMS.register("copper_pickaxe", () -> new PickaxeItem(COPPER, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_AXE = ITEMS.register("copper_axe", () -> new AxeItem(COPPER, 6.5F, -3.15F, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_SHOVEL = ITEMS.register("copper_shovel", () -> new ShovelItem(COPPER, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_HOE = ITEMS.register("copper_hoe", () -> new HoeItem(COPPER, -1, -1.5F, new Item.Properties()));

    // GEAR-007: Chapter 1 melee weapons. Durability, damage, attacks per second, extra reach; starting values.
    public static final RegistryObject<Item> NAIL_BAT = ITEMS.register("nail_bat",
            () -> new ScavengedWeaponItem(150, 5.0, 1.4, 0.0, WeaponEffects::causeBleeding));
    public static final RegistryObject<Item> HEAVY_SHOVEL = ITEMS.register("heavy_shovel", HeavyShovelItem::new);
    public static final RegistryObject<Item> PITCHFORK = ITEMS.register("pitchfork",
            () -> new ScavengedWeaponItem(180, WeaponEffects.PITCHFORK_DAMAGE, 1.2, 1.5, WeaponEffects::stabBehind));
    /** Blocks from the offhand (WeaponEffects); in the main hand it's a weak club. */
    public static final RegistryObject<Item> FRYING_PAN = ITEMS.register("frying_pan",
            () -> new ScavengedWeaponItem(120, 3.0, 1.6, 0.0, (target, attacker) -> {
            }));

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
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(NAIL_BAT);
            event.accept(HEAVY_SHOVEL);
            event.accept(PITCHFORK);
            event.accept(FRYING_PAN);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(COPPER_PICKAXE);
            event.accept(COPPER_AXE);
            event.accept(COPPER_SHOVEL);
            event.accept(COPPER_HOE);
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
