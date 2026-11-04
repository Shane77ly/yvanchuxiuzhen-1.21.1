package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * 查询武器上的一项数值（来源 {@code main(entity).nbtD("mana_Att")}），在 {@code NeoForge.EVENT_BUS} 上发出。
 * 武器数值的存放归 lianqi 与 equipment，监听者用 {@link #add} 累加；没有监听者时为 0。
 */
public final class WeaponStatEvent extends Event {
    private final ItemStack weapon;
    private final String key;
    private double total;

    public WeaponStatEvent(ItemStack weapon, String key) {
        this.weapon = weapon;
        this.key = key;
    }

    public ItemStack weapon() {
        return weapon;
    }

    /** 来源的键名，如 {@code mana_Att}。 */
    public String key() {
        return key;
    }

    public void add(double amount) {
        total += amount;
    }

    public double total() {
        return total;
    }
}
