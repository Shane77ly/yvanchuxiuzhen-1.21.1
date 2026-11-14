package net.yvanchuxiuzhen.system.world;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** 来源 MapVariables 的已寻遗迹串保存在主世界，全部维度与玩家共用。 */
final class StructureExplorationData extends SavedData {
    private String released = "wu%kong";

    static StructureExplorationData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(StructureExplorationData::new, StructureExplorationData::load),
                "yvanchuxiuzhen_structure_exploration");
    }

    private static StructureExplorationData load(CompoundTag tag, HolderLookup.Provider registries) {
        StructureExplorationData data = new StructureExplorationData();
        data.released = tag.getString("jiegoubaocunlist");
        return data;
    }

    List<String> released() { return Arrays.asList(released.split("%")); }

    void mark(String id) {
        released += "%" + id;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("jiegoubaocunlist", released);
        return tag;
    }
}
