package net.yvanchuxiuzhen.system.economy;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 每个维度的八类收购倍率，按来源世界数据持久化；零值在世界载入时归一。 */
public final class EconomyPrices extends SavedData {
    public static final List<String> KEYS = List.of("gongfajiage", "tuzhijiage", "zhuangbeijiage", "danyaojiage",
            "fulujiage", "shipinjiage", "yaocaijiage", "cailiaojiage");
    private CompoundTag values = new CompoundTag();

    public EconomyPrices() {
        for (String key : KEYS) values.putDouble(key, 1);
    }

    public static EconomyPrices get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(EconomyPrices::new, EconomyPrices::load), "yvanchuxiuzhen_economy");
    }

    private static EconomyPrices load(CompoundTag tag, HolderLookup.Provider registries) {
        EconomyPrices data = new EconomyPrices();
        data.values = tag.getCompound("prices").copy();
        for (String key : KEYS) if (data.values.getDouble(key) == 0) data.values.putDouble(key, 1);
        data.setDirty();
        return data;
    }

    public double multiplier(String key) { return values.getDouble(key); }

    public void set(String key, double value) { values.putDouble(key, value); setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("prices", values.copy());
        return tag;
    }
}
