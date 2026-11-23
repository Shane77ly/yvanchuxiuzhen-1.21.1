package net.yvanchuxiuzhen.system.economy.client;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntities;
import net.yvanchuxiuzhen.system.economy.content.BankBlockItem;

/** 银行客户端保留来源登录与存取界面，服务端菜单持有库存和账户。 */
public final class EconomyClientSetup {
    private EconomyClientSetup() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EconomyClientSetup::onTooltip);
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

    /** 来源 {@code Lore02Procedure}：拍卖设施布置令牌 {@code ling_20} 用法与当前放置数量提示。 */
    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("yvanchuxiuzhen:ling_20")) {
            return;
        }
        List<Component> lines = event.getToolTip();
        lines.add(Component.literal("暂未开放拍卖行，请等待下次更新！"));
        lines.add(Component.literal("手持此物，右键地面可以放置拍卖台，放置好拍卖台后，再次用此物右键其它地面，可以放置座位，一共可以放置10个座位和1个拍卖台。"));
        lines.add(Component.literal("手持此物，右键座位可以快速收起座位；右键拍卖台则同时收起拍卖台和所有座位。"));
        int count = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("paimaifangzhi");
        if (count == 0) {
            lines.add(Component.literal("§b还未放置任何拍卖设施。"));
        } else if (count == 1) {
            lines.add(Component.literal("§b已放置拍卖台，未放置座位。"));
        } else if (count > 1) {
            lines.add(Component.literal("§b已放置拍卖台，当前放置了§6" + (count - 1) + "§b个座位。"));
        }
    }
}
