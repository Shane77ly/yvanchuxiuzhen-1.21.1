package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;

/** 冒烟夹具读取真实概率结果，避免扩大产品公开接口或产生跨模块重复包。 */
final class TamingProbe {
    private TamingProbe() { }

    static double chance(Entity target, Entity shooter, double grade) {
        try {
            Class<?> consumer = Class.forName("net.yvanchuxiuzhen.system.creature.TalismanTaming");
            Method chance = consumer.getDeclaredMethod("chance", Entity.class, Entity.class, double.class);
            chance.setAccessible(true);
            Object result = chance.invoke(null, target, shooter, grade);
            Method value = result.getClass().getDeclaredMethod("value");
            value.setAccessible(true);
            return (double) value.invoke(result);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法读取神傀符真实概率", exception);
        }
    }
}
