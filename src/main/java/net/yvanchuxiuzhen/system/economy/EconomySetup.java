package net.yvanchuxiuzhen.system.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.content.AuctionChairBlock;
import net.yvanchuxiuzhen.system.economy.content.AuctionTokenItem;
import net.yvanchuxiuzhen.system.economy.content.BankCardItem;
import net.yvanchuxiuzhen.system.economy.content.BankBlock;
import net.yvanchuxiuzhen.system.economy.content.BankBlockItem;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlock;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntities;
import net.yvanchuxiuzhen.system.economy.content.SpiritEssenceItem;
import net.yvanchuxiuzhen.system.economy.content.SpiritStoneBundleItem;
import net.yvanchuxiuzhen.system.economy.content.SpiritStoneItem;
import net.yvanchuxiuzhen.system.economy.content.SpiritStoneOreBlock;
import net.yvanchuxiuzhen.system.economy.data.BankData;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;

/**
 * 经济系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EconomySetup {
    private static final ContentTables CONTENT = new ContentTables("economy")
            .itemFactory("economy:bank_card", (properties, tooltip) -> new BankCardItem(properties, tooltip, false))
            .itemFactory("economy:bank_card_advanced", (properties, tooltip) -> new BankCardItem(properties, tooltip, true))
            .itemFactory("economy:auction_token", AuctionTokenItem::new)
            .itemFactory("economy:cailiao_200", (properties, tooltip) -> new SpiritEssenceItem(properties, tooltip, false))
            .itemFactory("economy:cailiao_210", (properties, tooltip) -> new SpiritEssenceItem(properties, tooltip, true))
            .blockFactory("economy:spirit_stone_ore", traits -> ContentBlock.create(traits, SpiritStoneOreBlock::new))
            .blockFactory("economy:bank", traits -> ContentBlock.create(traits, BankBlock::new))
            .itemFactory("economy:bank", (properties, tooltip) -> new BankBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandiyinhang")), properties, tooltip))
            .blockFactory("economy:shop", traits -> ContentBlock.create(traits, source -> new EconomyBlock(source, false)))
            .blockFactory("economy:auction", traits -> ContentBlock.create(traits, source -> new EconomyBlock(source, true)))
            .blockFactory("economy:auction_chair", traits -> ContentBlock.create(traits, AuctionChairBlock::new));

    static {
        for (int level = 1; level <= 9; level++) {
            int tier = level;
            CONTENT.itemFactory("economy:spirit_stone_" + tier, (properties, tooltip) -> new SpiritStoneItem(properties, tooltip, tier));
            CONTENT.itemFactory("economy:spirit_stone_bundle_" + tier,
                    (properties, tooltip) -> new SpiritStoneBundleItem(properties, tooltip, tier));
        }
    }

    private EconomySetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        BankData.register(modBus);
        BankMenus.register(modBus);
        EconomyMenus.register(modBus);
        CommerceData.register(modBus);
        EconomyBlockEntities.register(modBus);
        CommerceTrading.initialize();
        NeoForge.EVENT_BUS.addListener(BankCardInteraction::rightClick);
        NeoForge.EVENT_BUS.addListener(BankUpdates::tick);
        NeoForge.EVENT_BUS.addListener(BankUpdates::stopped);
        NeoForge.EVENT_BUS.addListener(CommerceEntrances::rightClick);
        NeoForge.EVENT_BUS.addListener(CommerceUpdates::tick);
        NeoForge.EVENT_BUS.addListener(CommerceUpdates::stopped);
    }
}
