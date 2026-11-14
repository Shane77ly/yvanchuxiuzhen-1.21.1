package net.yvanchuxiuzhen.system.liandan.api;

import java.util.List;

/** 宝箱成品丹药的来源实例参数；药效等级与持续时间使用宝箱丹药的原单位。 */
public record MedicineReward(String name, int effectCount, int doses, boolean treasure, List<Effect> effects) {
    public MedicineReward {
        effects = List.copyOf(effects);
    }

    public record Effect(String id, int level, int duration) {
    }
}
