package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

/** 修者技能共用的来源随机、目标查找和独立技能结束操作。 */
public final class Xiuzhe0101Support {
    private Xiuzhe0101Support() { }

    public static int randomInt(int minimum, int maximum) {
        Random random = new Random();
        return minimum >= maximum ? minimum : random.nextInt(maximum - minimum + 1) + minimum;
    }

    public static double randomDouble(double minimum, double maximum) {
        Random random = new Random();
        return minimum >= maximum ? minimum : random.nextDouble() * (maximum - minimum) + minimum;
    }

    public static Entity target(LevelAccessor world, Entity entity) {
        if (!(world instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UUID.fromString(entity.getPersistentData().getString("gongjizhe")));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }

    public static String whole(double value) { return new DecimalFormat("0").format(value); }

    /** 来源Entityjinengjieshu只清理该技能的当前名、阶段和两个刻计数。 */
    public static void finish(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putString("jineng", "");
        data.putString("jinengjinxing", "");
        data.putDouble("entitytick", 0);
        data.putDouble("entitytick2", 0);
    }
}
