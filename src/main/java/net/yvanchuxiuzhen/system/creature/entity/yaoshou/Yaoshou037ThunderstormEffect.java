package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
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
        entity.getPersistentData().putDouble("leibaotick", (entity.getPersistentData().getDouble("leibaotick") + 1));
        if (entity.getPersistentData().getDouble("leibaotick") >= 5) {
            for (int index0 = 0; index0 < (int) (3); index0++) {
                weizhiX = entity.getX() + Yaoshou037054Support.randomInt(-12, 12);
                weizhiY = entity.getY();
                weizhiZ = entity.getZ() + Yaoshou037054Support.randomInt(-12, 12);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderyellow"), weizhiX, weizhiY, weizhiZ, 2, 0, 0, 0, 0);
                if (world instanceof ServerLevel _level)
                    _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(weizhiX, weizhiY, weizhiZ), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                            ("lightninglanding 3 " + entity.getStringUUID() + " 1 " + Relations.entityTeam(entity) + " 2 黄色"));
            }
            entity.getPersistentData().putDouble("leibaotick", 0);
        }
        return true;
    }
}
