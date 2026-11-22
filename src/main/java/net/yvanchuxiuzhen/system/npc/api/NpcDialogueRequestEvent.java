package net.yvanchuxiuzhen.system.npc.api;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 修士按来源顺序提交可选脚本，任务系统负责依据玩家任务目录选择入口。 */
public final class NpcDialogueRequestEvent extends Event {
    private final ServerPlayer player;
    private final List<String> scripts;
    private final String products;
    private final String information;

    public NpcDialogueRequestEvent(ServerPlayer player, List<String> scripts, String products, String information) {
        this.player = player;
        this.scripts = List.copyOf(scripts);
        this.products = products;
        this.information = information;
    }

    public ServerPlayer player() { return player; }
    public List<String> scripts() { return scripts; }
    public String products() { return products; }
    public String information() { return information; }
}
