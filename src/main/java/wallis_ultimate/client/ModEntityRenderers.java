package wallis_ultimate.client;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEntities;

@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        // Vanilla's zombie model and texture until the regular zombie gets its own look (ENEMY-012).
        event.registerEntityRenderer(ModEntities.ZOMBIE.get(), ZombieRenderer::new);
        // GEAR-007: projectiles look like their items; the burning patch is only particles.
        event.registerEntityRenderer(ModEntities.MOONSHINE_MOLOTOV.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.PIPE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAVEL_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BURNING_PATCH.get(), NoopRenderer::new);
    }
}
