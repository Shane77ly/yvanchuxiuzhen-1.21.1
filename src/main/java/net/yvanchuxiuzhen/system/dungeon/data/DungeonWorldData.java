package net.yvanchuxiuzhen.system.dungeon.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 副本全服序号、候场大厅与来源各维度画卷序号随世界存盘。 */
public final class DungeonWorldData extends SavedData {
    private double instance;
    private boolean lobby;
    private final Map<String, Double> paintings = new HashMap<>();

    public static DungeonWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(DungeonWorldData::new, DungeonWorldData::load), "yvanchuxiuzhen_dungeon");
    }

    private static DungeonWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        DungeonWorldData data = new DungeonWorldData();
        data.instance = tag.getDouble("fubenweizhi");
        data.lobby = tag.getBoolean("datingshengcheng");
        CompoundTag numbers = tag.getCompound("painting_numbers");
        for (String key : numbers.getAllKeys()) data.paintings.put(key, numbers.getDouble(key));
        return data;
    }

    public double instanceX() {
        return -100 - 100 * instance;
    }

    public void advanceInstance() {
        instance++;
        setDirty();
    }

    public boolean lobbyGenerated() {
        return lobby;
    }

    public void markLobbyGenerated() {
        lobby = true;
        setDirty();
    }

    public double nextPainting(ServerLevel origin) {
        String key = origin.dimension().location().toString();
        double next = paintings.getOrDefault(key, 0.0) + 1;
        paintings.put(key, next);
        setDirty();
        return next;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("fubenweizhi", instance);
        tag.putBoolean("datingshengcheng", lobby);
        CompoundTag numbers = new CompoundTag();
        paintings.forEach(numbers::putDouble);
        tag.put("painting_numbers", numbers);
        return tag;
    }
}
