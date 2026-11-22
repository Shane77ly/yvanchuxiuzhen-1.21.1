package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;

/** 地灵界地府固定结构只在首次跨维进入时放置，生成标记保存在地灵界本身。 */
final class DilingGeneration {
    private static final String DATA_NAME = "yvanchuxiuzhen_diling_generation";
    private static final String GENERATED = "difushengcheng";

    private DilingGeneration() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(DilingGeneration::changedDimension);
    }

    private static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getTo().equals(DimensionPortalSpec.DILING.dimension())
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Data data = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load), DATA_NAME);
        if (!data.generated) {
            WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "difujiegou"),
                    new BlockPos(500000, 61, 500000));
            data.generated = true;
            data.setDirty();
        }
    }

    private static final class Data extends SavedData {
        private boolean generated;

        private static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            data.generated = tag.getBoolean(GENERATED);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean(GENERATED, generated);
            return tag;
        }
    }
}
