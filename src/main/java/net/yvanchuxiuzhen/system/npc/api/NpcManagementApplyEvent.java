package net.yvanchuxiuzhen.system.npc.api;

import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * 管理工具向生物所有者提交来源配置快照；所有者负责持久字段、等级显示与属性重算。
 * 任务固定为十项，资质表按来源实体字段名索引；装备快照与工具库存隔离，订阅者只写自己拥有的实体。
 */
public final class NpcManagementApplyEvent extends Event {
    private static final EquipmentSlot[] EQUIPMENT = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private final Mob target;
    private final double level;
    private final List<String> scripts;
    private final String products;
    private final String information;
    private final Map<String, String> ratings;
    private final boolean fixed;
    private final List<ItemStack> equipment;
    private boolean equipmentApplied;

    public NpcManagementApplyEvent(Mob target, double level, List<String> scripts, String products, String information,
            Map<String, String> ratings, boolean fixed, List<ItemStack> equipment) {
        this.target = target;
        this.level = level;
        this.scripts = List.copyOf(scripts);
        this.products = products;
        this.information = information;
        this.ratings = Map.copyOf(ratings);
        this.fixed = fixed;
        this.equipment = equipment.stream().map(ItemStack::copy).toList();
    }

    public Mob target() { return target; }
    public double level() { return level; }
    public List<String> scripts() { return scripts; }
    public String products() { return products; }
    public String information() { return information; }
    public Map<String, String> ratings() { return ratings; }
    public boolean fixed() { return fixed; }

    /** 所有者写入脚本与商品后交付六槽原生装备；每次赋值使用独立栈，不共享工具快照。 */
    public void applyEquipment() {
        for (int index = 0; index < EQUIPMENT.length; index++) {
            target.setItemSlot(EQUIPMENT[index], equipment.get(index).copy());
        }
        equipmentApplied = true;
    }

    /** 入口据此保留尚无档案消费者的生物原生装备交付，避免再次覆盖所有者已交付的装备。 */
    public boolean equipmentApplied() { return equipmentApplied; }
}
