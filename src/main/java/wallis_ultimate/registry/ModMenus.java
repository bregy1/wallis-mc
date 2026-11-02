package wallis_ultimate.registry;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.block.GraveMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WallisUltimate.MOD_ID);

    /** SURV-004. */
    public static final RegistryObject<MenuType<GraveMenu>> GRAVE = MENUS.register("grave",
            () -> new MenuType<>(GraveMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }
}
