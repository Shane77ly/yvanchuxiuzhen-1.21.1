package net.yvanchuxiuzhen.system.economy.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.economy.CommerceCatalog;

/** 经济商店目录与随机材料货架生成入口，全面由 {@code content/economy/shop_catalogs.json} + Codec 驱动。 */
public final class EconomyCatalogs {
    private static Map<String, ShopCatalogDefinition> catalogs;

    private EconomyCatalogs() { }

    public static ShopCatalogDefinition catalog(String id) {
        return find(id).orElse(ShopCatalogDefinition.EMPTY);
    }

    public static Optional<ShopCatalogDefinition> find(String id) {
        if (id == null || id.isEmpty()) return Optional.empty();
        return Optional.ofNullable(all().get(id));
    }

    public static List<ShopCatalogDefinition> allList() {
        return List.copyOf(all().values());
    }

    private static Map<String, ShopCatalogDefinition> all() {
        if (catalogs == null) {
            Map<String, ShopCatalogDefinition> loaded = new LinkedHashMap<>();
            for (ShopCatalogDefinition entry : ContentFiles.read("content/economy/shop_catalogs.json", ShopCatalogDefinition.CODEC)) {
                loaded.put(entry.id(), entry);
            }
            catalogs = Map.copyOf(loaded);
        }
        return catalogs;
    }

    public static ShopCatalogDefinition materialShop(int grade, ShopCatalogDefinition.ShopInfo info) {
        if (grade < 1 || grade > 3) throw new IllegalArgumentException("材料商店档次：" + grade);
        RandomSource random = RandomSource.create();
        List<ShopCatalogDefinition.Product> products = new ArrayList<>(60);
        for (int index = 0; index < 60; index++) {
            int roll = random.nextInt(101);
            // 来源前两档先判断>=20；后续>=50分支因此不可达。
            int base = grade == 3 ? roll >= 70 ? 36 : 31 : (grade - 1) * 15 + (roll >= 20 ? 11 : 1);
            int level = base + random.nextInt(5);
            int qualityRoll = random.nextInt(100) + 1;
            int quality = qualityRoll > 98 ? 4 : qualityRoll > 88 ? 3 : qualityRoll > 70 ? 2 : qualityRoll > 20 ? 1 : 0;
            int price = (int) (CommerceCatalog.materialShopPrice(level) * (quality + 1) * 5);
            var item = BuiltInRegistries.ITEM.getTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chushoucailiao")))
                    .flatMap(tag -> tag.getRandomElement(random)).map(holder -> holder.value()).orElse(Items.AIR);
            products.add(new ShopCatalogDefinition.Product(
                    BuiltInRegistries.ITEM.getKey(item),
                    Optional.empty(),
                    ShopCatalogDefinition.Currency.STONES,
                    price,
                    new ShopCatalogDefinition.MaterialPayload(level, quality)));
        }
        return new ShopCatalogDefinition("material_shop_" + grade, info == null ? ShopCatalogDefinition.ShopInfo.EMPTY : info, products);
    }
}
