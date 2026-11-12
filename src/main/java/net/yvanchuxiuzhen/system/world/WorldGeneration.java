package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** 世界自然生成设置的唯一写入入口，状态按来源MapVariables保存在主世界。 */
public final class WorldGeneration {
    private static final Map<MinecraftServer, Data> DATA = new ConcurrentHashMap<>();

    private WorldGeneration() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WorldGeneration::load);
        NeoForge.EVENT_BUS.addListener(WorldGeneration::stop);
    }

    private static void load(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD)) {
            // 主世界Load在初始化出生区块前触发，存档只在此服务端线程读取并发布。
            DATA.put(level.getServer(), Data.load(level.getServer()));
        }
    }

    private static void stop(ServerStoppedEvent event) {
        DATA.remove(event.getServer());
    }

    public static boolean randomStructuresAllowed(MinecraftServer server) {
        return !DATA.get(server).settings.randomStructuresLocked();
    }

    public static void setRandomStructuresAllowed(MinecraftServer server, boolean allowed) {
        Data data = DATA.get(server);
        data.settings = new Settings(!allowed);
        data.setDirty();
    }

    /** 来源jiegoushengcheng为true时锁定随机结构，初值false。 */
    public record Settings(boolean randomStructuresLocked) {
        public static final Codec<Settings> CODEC = Codec.BOOL.optionalFieldOf("jiegoushengcheng", false)
                .xmap(Settings::new, Settings::randomStructuresLocked).codec();
        public static final StreamCodec<ByteBuf, Settings> STREAM_CODEC = ByteBufCodecs.BOOL
                .map(Settings::new, Settings::randomStructuresLocked);
    }

    private static final class Data extends SavedData {
        private static final String NAME = "yvanchuxiuzhen_world_generation";
        private volatile Settings settings = new Settings(false);

        private static Data load(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Data::new, Data::load), NAME);
        }

        private static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            data.settings = Settings.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag).getOrThrow();
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.merge((CompoundTag) Settings.CODEC.encodeStart(
                    RegistryOps.create(NbtOps.INSTANCE, registries), settings).getOrThrow());
            return tag;
        }
    }
}
