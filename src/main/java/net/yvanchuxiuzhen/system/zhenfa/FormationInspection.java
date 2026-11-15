package net.yvanchuxiuzhen.system.zhenfa;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 阵法检测反馈操作；结果发送给参数坐标附近的玩家。 */
public final class FormationInspection {
    private FormationInspection() { }

    public static void report(ServerLevel level, Entity inspected, double rank, BlockPos center) {
        Vec3 point = new Vec3(center.getX(), center.getY(), center.getZ());
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(point, point).inflate(0.5)).stream()
                .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(point))).toList()) {
            if (!(entity instanceof Player recipient)) continue;
            if (rank >= 1) recipient.displayClientMessage(Component.literal(inspected.getDisplayName().getString()), false);
            if (rank >= 2) {
                String name;
                double experience;
                double maximum;
                if (inspected instanceof Player player) {
                    name = Cultivation.get(player, CultivationText.LEVEL_NAME);
                    experience = Cultivation.get(player, CultivationValue.EXPERIENCE);
                    maximum = Cultivation.get(player, CultivationValue.EXPERIENCE_MAX);
                } else {
                    name = inspected.getPersistentData().getString("LEVELname");
                    experience = inspected.getPersistentData().getDouble("experience");
                    maximum = inspected.getPersistentData().getDouble("experience_Max");
                }
                recipient.displayClientMessage(Component.literal(name + "（" + experience + "/" + maximum + "）"), false);
            } else {
                String name = inspected instanceof Player player
                        ? Cultivation.get(player, CultivationText.LEVEL_NAME)
                        : inspected.getPersistentData().getString("LEVELname");
                recipient.displayClientMessage(Component.literal(name), false);
            }
        }
    }
}
