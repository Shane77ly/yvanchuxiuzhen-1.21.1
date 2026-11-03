package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 修炼系统的世界数据，保存在主世界：每日可获修为上限（来源 {@code MapVariables.dailyexperience}，初值 0 表示不限制）。
 */
public final class XiulianWorldData extends SavedData {
    private static final String NAME = "yvanchuxiuzhen_xiulian";
    private double dailyExperience;

    public static XiulianWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(XiulianWorldData::new, XiulianWorldData::load), NAME);
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
