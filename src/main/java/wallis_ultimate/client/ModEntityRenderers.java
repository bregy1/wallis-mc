package wallis_ultimate.client;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
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
        event.registerEntityRenderer(ModEntities.ZOMBIE.get(), RegularZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.DEFENDER.get(), DefenderRenderer::new);
        event.registerEntityRenderer(ModEntities.DROPSHIP.get(), DropshipRenderer::new);
        // GEAR-007: projectiles look like their items; the burning patch is only particles.
        event.registerEntityRenderer(ModEntities.MOONSHINE_MOLOTOV.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.PIPE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAVEL_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BURNING_PATCH.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.HOG_DADDY.get(), HogDaddyRenderer::new);
        event.registerEntityRenderer(ModEntities.ZOMBIE_PIGLET.get(), ZombiePigletRenderer::new);
        event.registerEntityRenderer(ModEntities.GROUND_MARK.get(), GroundMarkRenderer::new);
        event.registerEntityRenderer(ModEntities.MUD_RING.get(), MudRingRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RegularZombieModel.LAYER, RegularZombieModel::createBodyLayer);
        event.registerLayerDefinition(DefenderModel.LAYER, DefenderModel::createBodyLayer);
        event.registerLayerDefinition(DefenderRenderer.PARACHUTE_LAYER, DefenderRenderer::createParachuteLayer);
        event.registerLayerDefinition(DropshipModel.LAYER, DropshipModel::createBodyLayer);
        event.registerLayerDefinition(HogDaddyModel.LAYER, HogDaddyModel::createBodyLayer);
    }
}
