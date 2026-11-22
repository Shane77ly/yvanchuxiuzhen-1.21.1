package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;

/** 世界自然生成设置与首次登录传送阵坐标的唯一写入入口，状态保存在主世界。 */
public final class WorldGeneration {
    private static final Map<MinecraftServer, Data> DATA = new ConcurrentHashMap<>();

    private WorldGeneration() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WorldGeneration::load);
        NeoForge.EVENT_BUS.addListener(WorldGeneration::playerLogin);
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

    private static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.serverLevel().dimension().equals(Level.OVERWORLD)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Data data = DATA.get(level.getServer());
        if (data.teleportArray.generated()) {
            return;
        }
        TeleportArray array = new TeleportArray(false, Mth.nextInt(RandomSource.create(), -300, 300),
                20, Mth.nextInt(RandomSource.create(), -300, 300));
        data.teleportArray = array;
        data.setDirty();

        for (int x = -10; x <= 10; x++) {
            for (int y = -2; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    level.setBlock(new BlockPos(array.x() + x, array.y() + y, array.z() + z),
                            Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chuansongdazhen"),
                new BlockPos(array.x() - 10, array.y() - 2, array.z() - 10));
        data.teleportArray = new TeleportArray(true, array.x(), array.y(), array.z());
        data.setDirty();
    }

    /** 服务端主线程查询全部维度共享的传送阵中心，初始化完成前返回空。 */
    public static Optional<Vec3> teleportArrayCenter(ServerLevel level) {
        Data data = DATA.get(level.getServer());
        TeleportArray array = data.teleportArray;
        return array.generated()
                ? Optional.of(new Vec3(array.x(), array.y(), array.z()))
                : Optional.empty();
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

    /** 来源全局传送阵坐标与主世界生成标记；尚未生成时三轴初值均为零。 */
    private record TeleportArray(boolean generated, int x, int y, int z) {
        private static final Codec<TeleportArray> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("chuansongzhenshengcheng", false).forGetter(TeleportArray::generated),
                Codec.INT.optionalFieldOf("chuansongzhenX", 0).forGetter(TeleportArray::x),
                Codec.INT.optionalFieldOf("chuansongzhenY", 0).forGetter(TeleportArray::y),
                Codec.INT.optionalFieldOf("chuansongzhenZ", 0).forGetter(TeleportArray::z)
        ).apply(instance, TeleportArray::new));
    }

    private static final class Data extends SavedData {
        private static final String NAME = "yvanchuxiuzhen_world_generation";
        private volatile Settings settings = new Settings(false);
        private TeleportArray teleportArray = new TeleportArray(false, 0, 0, 0);

        private static Data load(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Data::new, Data::load), NAME);
        }

        private static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            data.settings = Settings.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag).getOrThrow();
            data.teleportArray = TeleportArray.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag).getOrThrow();
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.merge((CompoundTag) Settings.CODEC.encodeStart(
                    RegistryOps.create(NbtOps.INSTANCE, registries), settings).getOrThrow());
            tag.merge((CompoundTag) TeleportArray.CODEC.encodeStart(
                    RegistryOps.create(NbtOps.INSTANCE, registries), teleportArray).getOrThrow());
            return tag;
        }
    }
}
