package net.yvanchuxiuzhen.system.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.RenjianEnteredEvent;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;

/** 人间道固定场景只随实际跨维进入初始化，生成标记保存在人间道本身。 */
final class RenjianGeneration {
    private static final String DATA_NAME = "yvanchuxiuzhen_renjian_generation";
    private static final String GENERATED = "qiankundaochang";
    private static final List<Furnishing> FURNISHINGS = new ArrayList<>();

    private RenjianGeneration() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(RenjianGeneration::changedDimension);
        NeoForge.EVENT_BUS.addListener(RenjianGeneration::endTick);
        NeoForge.EVENT_BUS.addListener(RenjianGeneration::stopped);
    }

    private static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getTo().equals(DimensionPortalSpec.RENJIAN.dimension())
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Data data = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load), DATA_NAME);
        if (!data.generated) {
            sealQiankunShell(level);
            WorldStructures.place(level, id("qiankundaorenjvsuo"), new BlockPos(-12, 30, -29));
            clearQiankunInteriorFluids(level);
            Mob steward = (Mob) BuiltInRegistries.ENTITY_TYPE.get(id("daguanjia")).create(level);
            steward.moveTo(0, 32, -2, 0, 0);
            steward.setYBodyRot(0);
            steward.setYHeadRot(0);
            steward.finalizeSpawn(level, level.getCurrentDifficultyAt(steward.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(steward);
            data.generated = true;
            data.setDirty();
            player.setRespawnPosition(level.dimension(), player.blockPosition(), player.getYRot(), true, false);
            WorldStructures.place(level, id("xiaoshiwangjiegou"), new BlockPos(390, 0, 355));
            FURNISHINGS.add(new Furnishing(level));
        }
        NeoForge.EVENT_BUS.post(new RenjianEnteredEvent(player));
    }

    private static void endTick(ServerTickEvent.Post event) {
        FURNISHINGS.removeIf(task -> {
            ServerLevel level = task.level;
            if (level.getServer() != event.getServer() || --task.remaining > 0) {
                return false;
            }
            BlockPos support = new BlockPos(0, 34, -27);
            level.setBlock(support, BuiltInRegistries.BLOCK.get(id("beechfurniture_1701")).defaultBlockState(), Block.UPDATE_ALL);
            BlockPos table = support.above();
            level.setBlock(table, BuiltInRegistries.BLOCK.get(id("canwutai_block")).defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(table, level.getBlockState(table).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), Block.UPDATE_ALL);
            return true;
        });
    }

    private static void stopped(ServerStoppedEvent event) {
        FURNISHINGS.removeIf(task -> task.level.getServer() == event.getServer());
    }

    private static void sealQiankunShell(ServerLevel level) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -13; x <= 13; x++) {
            for (int y = 29; y <= 46; y++) {
                for (int z = -30; z <= 5; z++) {
                    if (x != -13 && x != 13 && y != 29 && y != 46 && z != -30 && z != 5) continue;
                    cursor.set(x, y, z);
                    if (!level.getFluidState(cursor).isEmpty()) {
                        level.setBlock(cursor, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static void clearQiankunInteriorFluids(ServerLevel level) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -12; x <= 12; x++) {
            for (int y = 30; y <= 45; y++) {
                for (int z = -29; z <= 4; z++) {
                    cursor.set(x, y, z);
                    if (!level.getFluidState(cursor).isEmpty()) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static final class Furnishing {
        private final ServerLevel level;
        private int remaining = 60;

        private Furnishing(ServerLevel level) {
            this.level = level;
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
