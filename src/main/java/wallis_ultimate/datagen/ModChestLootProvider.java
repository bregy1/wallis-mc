package wallis_ultimate.datagen;

import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModItems;

/**
 * WORLD-011, CMBT-003: what's in the chests of district 1's places. Everyone finds the same supplies table everywhere,
 * and each place has its own trinkets (proposal in the gear spec; every structure exists once, so each trinket has one
 * home). Lootr rolls all of this per player.
 */
public class ModChestLootProvider implements LootTableSubProvider {
    /** Second chests in the big safe spots, and the base of all the others. */
    public static final String SUPPLIES = "supplies";

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(table(SUPPLIES), supplies());
        output.accept(table("cabin"), supplies().withPool(trinket(ModItems.THICK_FLANNEL.get())));
        output.accept(table("earls_salvage"), supplies()
                .withPool(trinket(ModItems.LUCKY_HORSESHOE.get()))
                .withPool(trinket(ModItems.BRASS_KNUCKLES.get())));
        output.accept(table("gas_n_guns"), supplies().withPool(trinket(ModItems.SPARE_CLIP.get())));
        output.accept(table("camp_tarp"), supplies().withPool(trinket(ModItems.JOGGING_HEADBAND.get())));
        output.accept(table("stranded_hauler"), supplies().withPool(trinket(ModItems.WORN_SPURS.get())));
    }

    public static ResourceLocation table(String name) {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "chests/" + name);
    }

    private static LootPool.Builder trinket(Item item) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item));
    }

    private static LootTable.Builder supplies() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3.0F, 5.0F))
                        .add(some(Items.BREAD, 1, 3, 4))
                        .add(some(Items.APPLE, 1, 2, 3))
                        .add(some(Items.CARROT, 1, 3, 3))
                        .add(some(Items.POTATO, 1, 3, 3))
                        .add(some(Items.TORCH, 2, 6, 4))
                        .add(some(Items.ARROW, 2, 6, 3))
                        .add(some(Items.STRING, 1, 3, 2))
                        .add(some(Items.LEATHER, 1, 2, 2)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(some(ModItems.BANDAGE.get(), 1, 2, 1)));
    }

    private static LootItem.Builder<?> some(Item item, int least, int most, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(least, most)));
    }
}
