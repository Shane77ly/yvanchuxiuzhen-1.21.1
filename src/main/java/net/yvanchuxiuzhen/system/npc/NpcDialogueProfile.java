package net.yvanchuxiuzhen.system.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;

/** 修士实体的实例任务脚本/模板ID列表、强类型商店配置、固定标记及刷新时刻。 */
public record NpcDialogueProfile(
        List<String> scripts,
        String products,
        String shopInfo,
        String shopId,
        Optional<ShopCatalogDefinition> catalog,
        boolean fixed,
        double shopRefreshTick
) {
    public static final NpcDialogueProfile EMPTY = new NpcDialogueProfile(
            List.of(), "", "", "", Optional.empty(), false, 0);
    public static final Codec<NpcDialogueProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("scripts", List.of()).forGetter(NpcDialogueProfile::scripts),
            Codec.STRING.optionalFieldOf("products", "").forGetter(NpcDialogueProfile::products),
            Codec.STRING.optionalFieldOf("shop_info", "").forGetter(NpcDialogueProfile::shopInfo),
            Codec.STRING.optionalFieldOf("shop_id", "").forGetter(NpcDialogueProfile::shopId),
            ShopCatalogDefinition.CODEC.optionalFieldOf("catalog").forGetter(NpcDialogueProfile::catalog),
            Codec.BOOL.optionalFieldOf("fixed", false).forGetter(NpcDialogueProfile::fixed),
            Codec.DOUBLE.optionalFieldOf("shop_refresh_tick", 0.0).forGetter(NpcDialogueProfile::shopRefreshTick)
    ).apply(instance, NpcDialogueProfile::new));

    public NpcDialogueProfile(List<String> scripts, String products, String shopInfo, boolean fixed, double shopRefreshTick) {
        this(scripts, products, shopInfo, "", Optional.empty(), fixed, shopRefreshTick);
    }

    public NpcDialogueProfile {
        List<String> copy = new ArrayList<>(scripts.size());
        for (String script : scripts) {
            copy.add(script == null ? "" : script);
        }
        scripts = List.copyOf(copy);
        products = products == null ? "" : products;
        shopInfo = shopInfo == null ? "" : shopInfo;
        shopId = shopId == null ? "" : shopId;
        catalog = catalog == null ? Optional.empty() : catalog;
    }

    public String script(int slotIndex) {
        return slotIndex >= 0 && slotIndex < scripts.size() ? scripts.get(slotIndex) : "";
    }

    /** 按来源连续非空下标收集当前可交付给对话系统的脚本/模板列表。 */
    public List<String> activeScripts() {
        List<String> active = new ArrayList<>();
        for (String script : scripts) {
            if (script.isEmpty()) break;
            active.add(script);
        }
        return active;
    }

    public NpcDialogueProfile withScript(int slotIndex, String value) {
        List<String> next = new ArrayList<>(Math.max(scripts.size(), slotIndex + 1));
        next.addAll(scripts);
        while (next.size() <= slotIndex) {
            next.add("");
        }
        next.set(slotIndex, value == null ? "" : value);
        return new NpcDialogueProfile(next, products, shopInfo, shopId, catalog, fixed, shopRefreshTick);
    }

    public NpcDialogueProfile withProducts(String nextProducts) {
        return new NpcDialogueProfile(scripts, nextProducts, shopInfo, shopId, catalog, fixed, shopRefreshTick);
    }

    public NpcDialogueProfile withShopInfo(String nextShopInfo) {
        return new NpcDialogueProfile(scripts, products, nextShopInfo, shopId, catalog, fixed, shopRefreshTick);
    }

    public NpcDialogueProfile withShopId(String nextShopId) {
        return new NpcDialogueProfile(scripts, products, shopInfo, nextShopId, catalog, fixed, shopRefreshTick);
    }

    public NpcDialogueProfile withCatalog(Optional<ShopCatalogDefinition> nextCatalog) {
        return new NpcDialogueProfile(scripts, products, shopInfo, shopId, nextCatalog, fixed, shopRefreshTick);
    }

    public NpcDialogueProfile withFixed(boolean nextFixed) {
        return new NpcDialogueProfile(scripts, products, shopInfo, shopId, catalog, nextFixed, shopRefreshTick);
    }

    public NpcDialogueProfile withShopRefreshTick(double nextTick) {
        return new NpcDialogueProfile(scripts, products, shopInfo, shopId, catalog, fixed, nextTick);
    }
}
