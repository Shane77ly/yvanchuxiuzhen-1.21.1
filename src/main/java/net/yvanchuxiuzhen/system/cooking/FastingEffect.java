package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;

/** 辟谷按实体持久计时每十五刻补一点饥饿值；饱食时暂停计数，效果移除不清计时。 */
final class FastingEffect implements EffectBehavior {
    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        int food = entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0;
        if (food < 20) {
            CompoundTag data = entity.getPersistentData();
            double elapsed = data.getDouble("baoshidu") + 1;
            data.putDouble("baoshidu", elapsed);
            if (elapsed >= 15) {
                if (entity instanceof Player player) player.getFoodData().setFoodLevel(food + 1);
                data.putDouble("baoshidu", 0);
            }
        }
        return true;
    }
}
