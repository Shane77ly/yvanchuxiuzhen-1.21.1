package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorContent;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorItem;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorVariant;
import net.yvanchuxiuzhen.collection.decor.FortuneDraws;
import net.yvanchuxiuzhen.platform.ModId;

/** 两种动画装饰及来源签条激活画面的客户端装配。 */
public final class AnimatedDecorClient {
    private AnimatedDecorClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (AnimatedDecorVariant variant : AnimatedDecorVariant.values()) {
                event.registerBlockEntityRenderer(AnimatedDecorContent.type(variant), AnimatedDecorRenderer::new);
            }
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof AnimatedDecorItem animated) animated.setRenderProvider(new AnimatedDecorItemRenderer.Provider());
            }
        }));
        NeoForge.EVENT_BUS.addListener((FortuneDraws.ClientResult event) -> {
            Minecraft minecraft = Minecraft.getInstance();
            ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guaqian_0" + event.grade())));
            minecraft.execute(() -> minecraft.gameRenderer.displayItemActivation(result));
        });
    }
}
