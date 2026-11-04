package wallis_ultimate.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.registry.ModKit;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, WallisUltimate.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        hogDaddy();
        add(ModBlocks.REINFORCED_CONCRETE.get(), "Reinforced Concrete");
        add(ModBlocks.RIVETED_STEEL.get(), "Riveted Steel");
        add(ModBlocks.ALIEN_ALLOY.get(), "Alien Alloy");
        add(ModBlocks.GRAVE.get(), "Grave");
        add(ModBlocks.TRAVEL_SIGNPOST.get(), "Travel Signpost");
        ModKit.names().forEach((block, name) -> add(block.get(), name));
        add("safe_spot." + WallisUltimate.MOD_ID + ".earls_salvage", "Earl's Salvage");
        add("safe_spot." + WallisUltimate.MOD_ID + ".gas_n_guns", "Gas-N-Guns");
        add("safe_spot." + WallisUltimate.MOD_ID + ".camp_tarp", "Camp Tarp");
        add("safe_spot." + WallisUltimate.MOD_ID + ".stranded_hauler", "The Stranded Hauler");
        add("message." + WallisUltimate.MOD_ID + ".safe_spot.found", "You found a safe spot: %s. Its signpost connects it to the others you know.");
        add("message." + WallisUltimate.MOD_ID + ".safe_spot.angry", "\"Hey! You break it, you die!\"");
        add(ModEntities.DEFENDER.get(), "Defender");
        add(ModEntities.DROPSHIP.get(), "Dropship");
        add(ModItems.DEFENDER_RIFLE.get(), "Salvaged Rifle");
        add("message." + WallisUltimate.MOD_ID + ".travel.in_combat", "Not in the middle of a fight!");
        add("screen." + WallisUltimate.MOD_ID + ".travel.title", "Where to?");
        add("screen." + WallisUltimate.MOD_ID + ".travel.here", "You're at %s");
        add("screen." + WallisUltimate.MOD_ID + ".travel.none", "You don't know any other safe spots yet.");
        add(ModBlocks.SANDBAGS.get(), "Sandbags");
        add(ModBlocks.SANDBAG_SLAB.get(), "Sandbag Slab");
        add(ModBlocks.BARBED_WIRE.get(), "Barbed Wire");
        add(ModBlocks.OIL_DRUM.get(), "Oil Drum");
        add(ModBlocks.GREEN_TARP.get(), "Green Tarp");
        add(ModBlocks.BLUE_TARP.get(), "Blue Tarp");
        add(ModBlocks.GREEN_TARP_SLOPE.get(), "Sloped Green Tarp");
        add(ModBlocks.BLUE_TARP_SLOPE.get(), "Sloped Blue Tarp");
        add(ModBlocks.SUPPLY_CRATE.get(), "Supply Crate");
        add(ModBlocks.WRECK_BODY.get(), "Wreck Body");
        add(ModBlocks.WRECK_PANEL.get(), "Wreck Panel");
        add(ModBlocks.WRECK_WINDOW.get(), "Wreck Window");
        add(ModBlocks.WRECK_WHEEL.get(), "Wreck Wheel");
        add(ModBlocks.BEER_CANS.get(), "Beer Cans");
        add(ModBlocks.ANTLERS.get(), "Antlers");
        add("container." + WallisUltimate.MOD_ID + ".grave", "Grave of %s");
        add("message." + WallisUltimate.MOD_ID + ".grave.not_yours", "This isn't your grave.");
        add("message." + WallisUltimate.MOD_ID + ".grave.location", "Your things are in your grave at %s, %s, %s. It lasts 60 minutes.");
        add("message." + WallisUltimate.MOD_ID + ".bed.already_have", "You already have a bed on this planet, at %s, %s, %s. Pick that one up first.");
        add("message." + WallisUltimate.MOD_ID + ".horde.uneasy", "You get a really uneasy feeling.");
        add("message." + WallisUltimate.MOD_ID + ".horde.worse", "The feeling gets worse, something is about to happen.");        add("message." + WallisUltimate.MOD_ID + ".horde.no_sleep", "Nobody's sleeping through this. Not with that lot out there.");
        add("menu." + WallisUltimate.MOD_ID + ".disclaimer", "Wallis Ultimate is a work of fiction. All characters, names and events are fictional. Any resemblance to real persons, living or dead, is coincidental or intended as parody.");
        add(ModItems.BANDAGE.get(), "Bandage");
        add(ModItems.COPPER_PICKAXE.get(), "Copper Pickaxe");
        add(ModItems.COPPER_AXE.get(), "Copper Axe");
        add(ModItems.COPPER_SHOVEL.get(), "Copper Shovel");
        add(ModItems.COPPER_HOE.get(), "Copper Hoe");
        add(ModItems.NAIL_BAT.get(), "Nail Bat");
        add(ModItems.HEAVY_SHOVEL.get(), "Heavy Shovel");
        add(ModItems.PITCHFORK.get(), "Pitchfork");
        add(ModItems.FRYING_PAN.get(), "Frying Pan");
        add(ModEffects.BLEEDING.get(), "Bleeding");
        add(ModItems.MOONSHINE_MOLOTOV.get(), "Moonshine Molotov");
        add(ModItems.PIPE_BOMB.get(), "Pipe Bomb");
        add(ModItems.SLINGSHOT.get(), "Slingshot");
        add(ModEntities.MOONSHINE_MOLOTOV.get(), "Moonshine Molotov");
        add(ModEntities.PIPE_BOMB.get(), "Pipe Bomb");
        add(ModEntities.GRAVEL_SHOT.get(), "Gravel Shot");
        add(ModEntities.BURNING_PATCH.get(), "Burning Patch");
        add(ModItems.GRANNYS_RABBIT_FOOT.get(), "Granny's Rabbit Foot");
        add(ModItems.JOGGING_HEADBAND.get(), "Jogging Headband");
        add(ModItems.WORN_SPURS.get(), "Worn Spurs");
        add(ModItems.LUCKY_HORSESHOE.get(), "Lucky Horseshoe");
        add(ModItems.SPARE_CLIP.get(), "Spare Clip");
        add(ModItems.BRASS_KNUCKLES.get(), "Brass Knuckles");
        add(ModItems.THICK_FLANNEL.get(), "Thick Flannel");
        add(ModEffects.BANDAGED.get(), "Bandaged");
        add(ModEffects.SUNBURN.get(), "Sunburn");
        add(ModEffects.HEARTY.get(), "Hearty");
        add(ModEffects.FRAIL.get(), "Frail");
        add(ModEffects.FILLING.get(), "Filling");
        add(ModEffects.SUGAR_BUZZ.get(), "Sugar Buzz");
        add(ModEntities.ZOMBIE.get(), "Zombie");
        add(ModItems.ZOMBIE_SPAWN_EGG.get(), "Zombie Spawn Egg");
        add("key.categories." + WallisUltimate.MOD_ID, "Wallis Ultimate");
        add("key." + WallisUltimate.MOD_ID + ".roll", "Dodge Roll");
        add("key." + WallisUltimate.MOD_ID + ".give_up", "Give Up (When Downed)");
        add("hud." + WallisUltimate.MOD_ID + ".downed.title", "You're down!");
        add("hud." + WallisUltimate.MOD_ID + ".downed.time_left", "Time left: %s");
        add("hud." + WallisUltimate.MOD_ID + ".downed.ammo", "Pistol: %s shots left (%s to shoot)");
        add("hud." + WallisUltimate.MOD_ID + ".downed.give_up", "Press %s to give up");
        add("hud." + WallisUltimate.MOD_ID + ".reviving", "Reviving...");
        add("hud." + WallisUltimate.MOD_ID + ".teammate_down", "%s is down! %s left");
        add(ModBlocks.OLD_RADIO.get(), "Old Radio");
        StoryLanguage.addAll(this::add);
    }

    /** ENEMY-011. The words are proposals (working agreement: Chapter 1 text). Under story., so the clean-language option covers them. */
    private void hogDaddy() {
        add(ModEntities.HOG_DADDY.get(), "Hog Daddy");
        add(ModEntities.ZOMBIE_PIGLET.get(), "Zombie Piglet");
        add(ModEntities.GROUND_MARK.get(), "Ground Mark");
        add(ModEntities.MUD_RING.get(), "Mud Ring");
        add(ModBlocks.STRAPPED_HAY.get(), "Strapped Hay");
        add(ModBlocks.CRUSHED_WRECK.get(), "Crushed Wreck");
        add(ModBlocks.PEN_POST.get(), "Pen Post");
        add(ModBlocks.FEEDING_TROUGH.get(), "Feeding Trough");
        add(ModBlocks.CORN.get(), "Corn");
        String message = "story." + WallisUltimate.MOD_ID + ".hog_daddy.";
        add(message + "snorting", "Something huge is snorting and farting behind that wall. Smells like a dead sow's ass in July.");
        add(message + "run", "RUN, DUMBASSES! Through the corn, to the barn!");
        add(message + "pen", "Get the fat bastard to ram a post or the trough, then beat his ass!");
        add(message + "stunned", "He knocked himself stupid! Kick him in the ass!");
        add(message + "enraged", "He just shit himself with rage! Every post only holds once now!");
        add(message + "beaten", "Hog Daddy's dead. Half a ton of bacon, and every bite of it will kill you.");
        add(message + "wiped", "Hog Daddy got bored of your corpses and went back to his slop. Ring the bell at his gate for round two.");
        add(message + "busy", "Hog Daddy's busy eating somebody else's face. Ring the bell at his gate when he's had his fill.");
        add(message + "already_fighting", "You're already fighting him, genius. He's the big one with the hook.");
        add(message + "already_beaten", "He's dead. You killed him. Leave the poor bastard's carcass alone, you sick freak.");
        add(message + "nobody_standing", "Your whole team's face down in the dirt. Nobody's fighting shit.");
        add(message + "no_farm", "Hog Daddy's farm isn't in this world. Lucky you.");
        add(message + "protected", "Hands off the farm. Go dig in your own shit.");
    }
}
