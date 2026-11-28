package net.yvanchuxiuzhen.system.economy;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.data.AuctionLots;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 拍卖拍品 JSON 目录装载与抽槽分配，不再解析斜线三元组文本。 */
final class AuctionGoods {
    private static List<AuctionGoodDefinition> catalog;

    private AuctionGoods() { }

    static List<AuctionGoodDefinition> byTier(int tier) {
        if (catalog == null) {
            catalog = ContentFiles.read("content/economy/auction_goods.json", AuctionGoodDefinition.CODEC);
        }
        return catalog.stream().filter(entry -> entry.tier() == tier).toList();
    }

    static void fill(EconomyBlockEntity block, List<AuctionGoodDefinition> goods, RandomSource random) {
        if (goods.isEmpty()) return;
        for (int slot = 0; slot < block.state().auction().auctionItemCount; slot++) {
            AuctionGoodDefinition def = goods.get(random.nextInt(goods.size()));
            ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(def.item()), 1);
            if (def.materialLevel().isPresent() || def.materialQuality().isPresent()) {
                Materials.setInstance(result, def.materialLevel().orElse(0), def.materialQuality().orElse(0));
            }
            if (!def.lots().isEmpty()) {
                result.set(CommerceData.AUCTION_LOTS.get(), new AuctionLots(def.lots()));
            }
            block.setItem(slot, result);
        }
    }
}
