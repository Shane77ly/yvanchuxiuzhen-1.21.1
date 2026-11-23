package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 修炼系统的世界数据：主世界保存每日可获修为上限（来源 {@code MapVariables.dailyexperience}），
 * 各维度独立保存 {@code worldtime} 计时以在满 24000 刻时扣减玩家寿元（来源 {@code WorldVariables.worldtime}）。
 */
public final class XiulianWorldData extends SavedData {
    private static final String NAME = "yvanchuxiuzhen_xiulian";
    private static final String LEVEL_NAME = "yvanchuxiuzhen_xiulian_level";
    private double dailyExperience;

    public static XiulianWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(XiulianWorldData::new, XiulianWorldData::load), NAME);
    }

    static LevelClock levelClock(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LevelClock::new, LevelClock::load), LEVEL_NAME);
    }

    static final class LevelClock extends SavedData {
        private double worldTime;

        private static LevelClock load(CompoundTag tag, HolderLookup.Provider provider) {
            LevelClock clock = new LevelClock();
            clock.worldTime = tag.getDouble("worldtime");
            return clock;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            tag.putDouble("worldtime", worldTime);
            return tag;
        }

        boolean advanceDay() {
            worldTime++;
            if (worldTime >= 24000) {
                worldTime = 0;
                setDirty();
                return true;
            }
            setDirty();
            return false;
        }
    }

    private static XiulianWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        XiulianWorldData data = new XiulianWorldData();
        data.dailyExperience = tag.getDouble("daily_experience");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putDouble("daily_experience", dailyExperience);
        return tag;
    }

    public double dailyExperience() {
        return dailyExperience;
    }

    public void setDailyExperience(double dailyExperience) {
        this.dailyExperience = dailyExperience;
        setDirty();
    }
}
