package net.yvanchuxiuzhen.system.material.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 材料辨识向库存所有者请求可消费总数，或按等级1–40、品阶0–4消费指定材料。 */
public final class MaterialInventoryRequestEvent extends Event {
    private final ServerPlayer player;
    private final String itemId;
    private final Action action;
    private final int amount;
    private int result;

    public MaterialInventoryRequestEvent(ServerPlayer player, String itemId, Action action, int amount) {
        this.player = player;
        this.itemId = itemId;
        this.action = action;
        this.amount = amount;
    }

    public ServerPlayer player() {
        return player;
    }

    public String itemId() {
        return itemId;
    }

    public Action action() {
        return action;
    }

    public int amount() {
        return amount;
    }

    public int result() {
        return result;
    }

    /** COUNT 返回允许自动消费的仓总数；CONSUME 返回实际已扣件数。 */
    public void respond(int value) {
        result = value;
    }

    public enum Action {
        COUNT, CONSUME
    }
}
