package net.yvanchuxiuzhen.system.economy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 每个维度的八类收购倍率，按来源世界数据持久化；零值在世界载入时归一。 */
public final class EconomyPrices extends SavedData {
    public static final List<String> KEYS = List.of("gongfajiage", "tuzhijiage", "zhuangbeijiage", "danyaojiage",
            "fulujiage", "shipinjiage", "yaocaijiage", "cailiaojiage");
    private final Map<String, Double> values = new LinkedHashMap<>();

    public EconomyPrices() {
        for (String key : KEYS) values.put(key, 1.0);
    }

    public static EconomyPrices get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(EconomyPrices::new, EconomyPrices::load), "yvanchuxiuzhen_economy");
    }

    private static EconomyPrices load(CompoundTag tag, HolderLookup.Provider registries) {
        EconomyPrices data = new EconomyPrices();
        CompoundTag prices = tag.getCompound("prices");
        for (String key : KEYS) {
            double val = prices.getDouble(key);
            data.values.put(key, val == 0.0 ? 1.0 : val);
        }
        data.setDirty();
        return data;
    }

    public double multiplier(String key) { return values.getOrDefault(key, 0.0); }

    public void set(String key, double value) { values.put(key, value); setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag prices = new CompoundTag();
        values.forEach(prices::putDouble);
        tag.put("prices", prices);
        return tag;
    }
}
