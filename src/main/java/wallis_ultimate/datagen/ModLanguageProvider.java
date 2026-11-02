package wallis_ultimate.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModItems;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, WallisUltimate.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.REINFORCED_CONCRETE.get(), "Reinforced Concrete");
        add(ModBlocks.RIVETED_STEEL.get(), "Riveted Steel");
        add(ModBlocks.ALIEN_ALLOY.get(), "Alien Alloy");
        add(ModItems.BANDAGE.get(), "Bandage");
        add(ModEffects.BANDAGED.get(), "Bandaged");
        add("key.categories." + WallisUltimate.MOD_ID, "Wallis Ultimate");
        add("key." + WallisUltimate.MOD_ID + ".roll", "Dodge Roll");
    }
}
