package wallis_ultimate;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModItems;

@Mod(WallisUltimate.MOD_ID)
public class WallisUltimate {
    public static final String MOD_ID = "wallis_ultimate";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WallisUltimate(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTabs);
        ModNetwork.register();
    }
}
