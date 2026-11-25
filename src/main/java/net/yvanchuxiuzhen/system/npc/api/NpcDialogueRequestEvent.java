package net.yvanchuxiuzhen.system.npc.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;

/** 修士按来源顺序提交可选对话模板与商店规约，任务系统负责依据玩家任务目录选择入口。 */
public final class NpcDialogueRequestEvent extends Event {
    private final ServerPlayer player;
    private final List<String> scripts;
    private final String products;
    private final String information;
    private final String shopId;
    private final Optional<ShopCatalogDefinition> catalog;

    public NpcDialogueRequestEvent(ServerPlayer player, List<String> scripts, String products, String information) {
        this(player, scripts, products, information, "", Optional.empty());
    }

    public NpcDialogueRequestEvent(ServerPlayer player,
                                   List<String> scripts,
                                   String products,
                                   String information,
                                   String shopId,
                                   Optional<ShopCatalogDefinition> catalog) {
        this.player = player;
        this.scripts = List.copyOf(scripts);
        this.products = products == null ? "" : products;
        this.information = information == null ? "" : information;
        this.shopId = shopId == null ? "" : shopId;
        this.catalog = catalog == null ? Optional.empty() : catalog;
    }

    public ServerPlayer player() { return player; }
    public List<String> scripts() { return scripts; }
    public String products() { return products; }
    public String information() { return information; }
    public String shopId() { return shopId; }
    public Optional<ShopCatalogDefinition> catalog() { return catalog; }
}
