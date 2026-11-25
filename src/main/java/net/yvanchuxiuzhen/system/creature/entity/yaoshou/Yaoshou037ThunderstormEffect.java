package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 037雷暴效果每五刻在身边随机三处召下来源黄色落雷。 */
public final class Yaoshou037ThunderstormEffect implements EffectBehavior {
    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        LevelAccessor world = entity.level();
        double weizhiX = 0;
        double weizhiY = 0;
        double weizhiZ = 0;
        double ticks = entity.getData(CreatureData.THUNDERSTORM_TICK) + 1;
        entity.setData(CreatureData.THUNDERSTORM_TICK, ticks);
        if (ticks >= 5) {
            for (int index0 = 0; index0 < (int) (3); index0++) {
                weizhiX = entity.getX() + Yaoshou037054Support.randomInt(-12, 12);
                weizhiY = entity.getY();
                weizhiZ = entity.getZ() + Yaoshou037054Support.randomInt(-12, 12);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderyellow"), weizhiX, weizhiY, weizhiZ, 2, 0, 0, 0, 0);
                if (world instanceof ServerLevel _level)
                    Lightning.cast(_level, new Vec3(weizhiX, weizhiY, weizhiZ), entity, 3, 1, Relations.entityTeam(entity), 2, "黄色", false);
            }
            entity.setData(CreatureData.THUNDERSTORM_TICK, 0.0);
        }
        return true;
    }
}
