package net.yvanchuxiuzhen.system.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.data.AuctionLots;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 来源拍卖三元组导入器；根数据在本场逐件累积，抽取允许重复。 */
final class AuctionGoods {
    private AuctionGoods() { }

    static void fill(EconomyBlockEntity block, List<String> goods, RandomSource random) {
        CompoundTag root = new CompoundTag();
        CompoundTag nested = new CompoundTag();
        String nestedName = "";
        double remaining = 0;
        ItemStack result = ItemStack.EMPTY;
        for (int slot = 0; slot < block.state().getInt("paimaiitemnumber"); slot++) {
            String[] fields = goods.get(random.nextInt(goods.size())).split("/");
            for (int offset = 0; offset < fields.length / 3 * 3; offset += 3) {
                String key = fields[offset], type = fields[offset + 1], value = fields[offset + 2];
                if (type.equals("tag")) {
                    nestedName = key;
                    nested = root.getCompound(key);
                    remaining = CommerceGoods.number(value);
                    continue;
                }
                if (type.equals("name")) result.set(DataComponents.CUSTOM_NAME, Component.literal(value));
                if (type.equals("物品id")) result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(value.toLowerCase(Locale.ENGLISH).replace("yvanchuxitong:", "yvanchuxiuzhen:"))));
                CompoundTag target = remaining > 0 ? nested : root;
                boolean written = true;
                switch (type) {
                    case "name", "物品id", "str", "char" -> target.putString(key, value);
                    case "int" -> target.putInt(key, (int) CommerceGoods.number(value));
                    case "double" -> target.putDouble(key, CommerceGoods.number(value));
                    case "logic" -> target.putBoolean(key, value.equals("true"));
                    default -> written = false;
                }
                if (written && remaining > 0 && --remaining <= 0) root.put(nestedName, nested);
            }
            // 来源setTag覆盖显示名标签；1.21.1分拆组件后同样清除此临时显示名。
            result.remove(DataComponents.CUSTOM_NAME);
            if (root.contains("cailiaolevel") || root.contains("cailiaopinjie")) {
                Materials.setInstance(result, root.getInt("cailiaolevel"), root.getInt("cailiaopinjie"));
            }
            List<AuctionLots.Entry> entries = new ArrayList<>();
            for (int index = 1; root.contains("goodsitem" + index); index++) {
                CompoundTag lot = root.getCompound("goodsitem" + index);
                entries.add(new AuctionLots.Entry(lot.getString("goodstype"), lot.getInt("goodsstartingprice"), lot.getInt("goodslevel")));
            }
            if (!entries.isEmpty()) result.set(CommerceData.AUCTION_LOTS.get(), new AuctionLots(entries));
            block.setItem(slot, result.copyWithCount(1));
        }
    }
}
