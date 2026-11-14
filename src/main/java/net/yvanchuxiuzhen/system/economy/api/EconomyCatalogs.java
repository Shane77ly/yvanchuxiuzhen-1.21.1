package net.yvanchuxiuzhen.system.economy.api;

import java.util.StringJoiner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.system.economy.CommerceCatalog;

/** NPC向经济所有者请求来源三档随机材料商品表，返回60件五元组。 */
public final class EconomyCatalogs {
    private EconomyCatalogs() { }

    public static String materialShop(int grade) {
        if (grade < 1 || grade > 3) throw new IllegalArgumentException("材料商店档次：" + grade);
        RandomSource random = RandomSource.create();
        StringJoiner products = new StringJoiner("%");
        for (int index = 0; index < 60; index++) {
            int roll = random.nextInt(101);
            // 来源前两档先判断>=20；后续>=50分支因此不可达。
            int base = grade == 3 ? roll >= 70 ? 36 : 31 : (grade - 1) * 15 + (roll >= 20 ? 11 : 1);
            int level = base + random.nextInt(5);
            int qualityRoll = random.nextInt(100) + 1;
            int quality = qualityRoll > 98 ? 4 : qualityRoll > 88 ? 3 : qualityRoll > 70 ? 2 : qualityRoll > 20 ? 1 : 0;
            int price = (int) (CommerceCatalog.materialShopPrice(level) * (quality + 1) * 5);
            var item = BuiltInRegistries.ITEM.getTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "chushoucailiao")))
                    .flatMap(tag -> tag.getRandomElement(random)).map(holder -> holder.value()).orElse(Items.AIR);
            products.add(BuiltInRegistries.ITEM.getKey(item) + "%无%灵石%" + price + "%cailiaolevel/int/" + level + "/cailiaopinjie/int/" + quality);
        }
        return products.toString();
    }
}
