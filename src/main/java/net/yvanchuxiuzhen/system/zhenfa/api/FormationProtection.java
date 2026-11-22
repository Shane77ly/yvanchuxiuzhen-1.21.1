package net.yvanchuxiuzhen.system.zhenfa.api;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.yvanchuxiuzhen.system.zhenfa.data.FormationAttachments;

/** 阳二的方块罡气公开操作；计数属于阵法，依附目标方块实体持久化。 */
public final class FormationProtection {
    private FormationProtection() { }

    public static int amount(BlockEntity target) {
        return target.getData(FormationAttachments.GANGQI);
    }

    public static void add(BlockEntity target, int amount) {
        target.setData(FormationAttachments.GANGQI, amount(target) + amount);
    }
}
