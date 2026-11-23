package net.yvanchuxiuzhen.system.world.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;

/** 浓雾群系（biomefog_01..04 / biomeyoumeng_02）的客户端雾色与雾距过渡。 */
final class WorldFogClient {
    private static double wuqixianshi;

    private WorldFogClient() { }

    static void register() {
        NeoForge.EVENT_BUS.register(WorldFogClient.class);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            wuqixianshi = 0;
            return;
        }
        boolean inFogBiome = minecraft.level.getBiome(player.blockPosition()).unwrapKey()
                .map(key -> {
                    ResourceLocation id = key.location();
                    return id.getNamespace().equals(ModId.VALUE)
                            && (id.getPath().startsWith("biomefog_") || id.getPath().equals("biomeyoumeng_02"));
                }).orElse(false);
        if (inFogBiome) {
            if (wuqixianshi == 0) wuqixianshi = 600;
            else if (wuqixianshi > 8) wuqixianshi -= 1;
        } else if (wuqixianshi > 0) {
            if (wuqixianshi < 600) wuqixianshi += 1;
            else wuqixianshi = 0;
        }
    }

    @SubscribeEvent
    static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (wuqixianshi <= 0) return;
        event.setRed(0f);
        event.setGreen(51f / 255f);
        event.setBlue(102f / 255f);
    }

    @SubscribeEvent
    static void onRenderFog(ViewportEvent.RenderFog event) {
        if (wuqixianshi <= 0) return;
        event.setNearPlaneDistance((float) wuqixianshi);
        event.setFarPlaneDistance((float) (wuqixianshi + 30));
        event.setCanceled(true);
    }
}
