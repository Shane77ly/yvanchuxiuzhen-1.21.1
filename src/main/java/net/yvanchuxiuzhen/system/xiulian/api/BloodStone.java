package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.xiulian.content.BloodStoneBlockEntity;

/**
 * 吸血石的公开操作：流血效果（来源 {@code Effect004ZaiXiaoGuoChiXuShiMeiKeFaSheng}）每次扣血时，给实体脚下的吸血石加充血量
 * （效果等级 + 1）。该位置不是吸血石时不做任何事。只在服务端调用。
 */
public final class BloodStone {
    private BloodStone() {
    }

    public static void addBlood(Level level, BlockPos pos, int amount) {
        if (level.getBlockEntity(pos) instanceof BloodStoneBlockEntity stone) {
            stone.setBlood(stone.blood() + amount);
        }
    }

    public static int blood(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BloodStoneBlockEntity stone ? stone.blood() : 0;
    }
}
