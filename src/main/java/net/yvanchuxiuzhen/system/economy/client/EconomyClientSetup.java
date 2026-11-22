package net.yvanchuxiuzhen.system.economy.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntities;
import net.yvanchuxiuzhen.system.economy.content.BankBlockItem;

/** 银行客户端保留来源登录与存取界面，服务端菜单持有库存和账户。 */
public final class EconomyClientSetup {
    private EconomyClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(BankMenus.BANK.get(), BankScreen::new);
            event.register(BankMenus.LOGIN.get(), BankLoginScreen::new);
            event.register(EconomyMenus.SHOP.get(), CommerceScreen::new);
            event.register(EconomyMenus.STONE_SALE.get(), CommerceScreen::new);
            event.register(EconomyMenus.BASIC_SALE.get(), CommerceScreen::new);
            event.register(EconomyMenus.PLAYER_SHOP.get(), CommerceScreen::new);
            event.register(EconomyMenus.PLAYER_SHOP_SETTINGS.get(), CommerceScreen::new);
            event.register(EconomyMenus.AUCTION_SETTINGS.get(), CommerceScreen::new);
            event.register(EconomyMenus.AUCTION.get(), CommerceScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(EconomyBlockEntities.SHOP.get(), EconomyBlockRenderer::new);
            event.registerBlockEntityRenderer(EconomyBlockEntities.AUCTION.get(), EconomyBlockRenderer::new);
            event.registerBlockEntityRenderer(EconomyBlockEntities.BANK.get(), BankBlockRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof BankBlockItem bank) bank.setRenderProvider(new BankItemRenderer.Provider());
            }
        }));
    }
}
