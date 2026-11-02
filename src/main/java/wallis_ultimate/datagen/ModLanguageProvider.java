package wallis_ultimate.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModEntities;
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
        add(ModBlocks.GRAVE.get(), "Grave");
        add("container." + WallisUltimate.MOD_ID + ".grave", "Grave of %s");
        add("message." + WallisUltimate.MOD_ID + ".grave.not_yours", "This isn't your grave.");
        add(ModItems.BANDAGE.get(), "Bandage");
        add(ModEffects.BANDAGED.get(), "Bandaged");
        add(ModEffects.SUNBURN.get(), "Sunburn");
        add(ModEntities.ZOMBIE.get(), "Zombie");
        add(ModItems.ZOMBIE_SPAWN_EGG.get(), "Zombie Spawn Egg");
        add("key.categories." + WallisUltimate.MOD_ID, "Wallis Ultimate");
        add("key." + WallisUltimate.MOD_ID + ".roll", "Dodge Roll");
        add("key." + WallisUltimate.MOD_ID + ".give_up", "Give Up (When Downed)");
        add("hud." + WallisUltimate.MOD_ID + ".downed.title", "You're down!");
        add("hud." + WallisUltimate.MOD_ID + ".downed.time_left", "Time left: %s");
        add("hud." + WallisUltimate.MOD_ID + ".downed.ammo", "Pistol: %s/%s shots (%s to shoot)");
        add("hud." + WallisUltimate.MOD_ID + ".downed.give_up", "Press %s to give up");
        add("hud." + WallisUltimate.MOD_ID + ".reviving", "Reviving...");
        add("hud." + WallisUltimate.MOD_ID + ".teammate_down", "%s is down! %s left");
    }
}
