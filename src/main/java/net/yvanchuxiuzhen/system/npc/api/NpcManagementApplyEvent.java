package net.yvanchuxiuzhen.system.npc.api;

import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.Event;

/**
 * 管理工具向生物所有者提交来源配置快照；所有者负责持久字段、等级显示与属性重算。
 * 任务固定为十项，资质表按来源实体字段名索引；集合不可变，订阅者只写自己拥有的实体。
 */
public final class NpcManagementApplyEvent extends Event {
    private final Mob target;
    private final double level;
    private final List<String> scripts;
    private final String products;
    private final String information;
    private final Map<String, String> ratings;
    private final boolean fixed;

    public NpcManagementApplyEvent(Mob target, double level, List<String> scripts, String products, String information,
            Map<String, String> ratings, boolean fixed) {
        this.target = target;
        this.level = level;
        this.scripts = List.copyOf(scripts);
        this.products = products;
        this.information = information;
        this.ratings = Map.copyOf(ratings);
        this.fixed = fixed;
    }

    public Mob target() { return target; }
    public double level() { return level; }
    public List<String> scripts() { return scripts; }
    public String products() { return products; }
    public String information() { return information; }
    public Map<String, String> ratings() { return ratings; }
    public boolean fixed() { return fixed; }
}
