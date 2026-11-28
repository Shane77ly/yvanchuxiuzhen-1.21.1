package net.yvanchuxiuzhen.system.dungeon;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonPlayerData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonTransientData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonWorldData;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/** 玩家副本倒计时、维度进入和回程；持久副本标志与来源实体自由标志分别推进。 */
public final class DungeonPlayers {
    public static final ResourceKey<Level> PAINTING = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao_009weidu"));
    private static final Set<String> DIMENSIONS = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end",
            "yvanchuxiuzhen:dimensiontianqve", "yvanchuxiuzhen:dimensionxiuluo", "yvanchuxiuzhen:dimensionrenjian",
            "yvanchuxiuzhen:dimensionyaohun", "yvanchuxiuzhen:dimensiondiling", "yvanchuxiuzhen:dimensionwugui");

    private DungeonPlayers() {
    }

    public static boolean supported(ResourceKey<Level> dimension) { return DIMENSIONS.contains(dimension.location().toString()); }
    public static Block block(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)); }

    public static void tick(CultivationSecondEvent event) {
        ServerPlayer player = event.player();
        DungeonPlayerData state = player.getData(DungeonData.PLAYER);
        DungeonTransientData transientState = player.getData(DungeonData.PLAYER_TRANSIENT);
        if (transientState.portalOpened() && !player.level().getBlockState(player.blockPosition()).is(block("tiandaofuben"))) {
            transientState = transientState.withPortalOpened(false);
            player.setData(DungeonData.PLAYER_TRANSIENT, transientState);
        }
        if (state.waiting()) {
            state = state.withCountdown(state.countdown() - 1);
            List<ServerPlayer> members = waitingPlayers(player);
            for (ServerPlayer member : members) member.displayClientMessage(Component.literal(new DecimalFormat("0").format(state.countdown()) + "秒后进入战斗"), true);
            if (state.countdown() <= 0) {
                BlockPos pos = state.controllerPos();
                ServerLevel level = player.serverLevel();
                replaceController(level, pos);
                for (ServerPlayer member : members) member.teleportTo(state.entryX(), state.entryY(), state.entryZ());
                if (level.getBlockEntity(pos) instanceof DungeonBlockEntity controller) {
                    controller.setDungeonName(state.dungeonName());
                    controller.setDefinitionId(state.definitionId());
                    controller.setReturnDimension(state.returnDimension());
                    controller.setReturnPos(state.returnPos().getX(), state.returnPos().getY(), state.returnPos().getZ());
                    controller.setOpenRequested(true);
                    controller.changed();
                }
                state = state.withWaiting(false);
            }
            player.setData(DungeonData.PLAYER, state);
        } else if (transientState.pendingReturn()) {
            player.setData(DungeonData.PLAYER, state.withReturning(true));
            player.setData(DungeonData.PLAYER_TRANSIENT, transientState.withPendingReturn(false));
        } else if (!player.level().dimension().equals(PAINTING) && state.returning()) {
            if (player.level().getBlockEntity(state.returnPos()) instanceof DungeonBlockEntity entrance) {
                entrance.setOnCooldown(false);
                if (entrance.entranceControllerX() == 0) {
                    BlockPos ctrl = state.controllerPos();
                    entrance.setEntranceControllerPos(ctrl.getX(), ctrl.getY(), ctrl.getZ());
                }
                if (entrance.entrancePlayerX() == 0) {
                    entrance.setEntrancePlayerPos(state.entryX(), state.entryY(), state.entryZ());
                }
                entrance.changed();
            }
            player.setData(DungeonData.PLAYER, state.withReturning(false));
        }
    }

    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension().equals(PAINTING)) enteredPainting(player);
    }

    public static void entityJoined(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) return;
        if (supported(player.level().dimension())) restoreGameMode(player);
        if (!player.level().dimension().equals(PAINTING)) PaintingRuntime.returned(player);
    }

    /** 恢复附件中来源四种游戏模式；识别成功即清空标记，未知文本保留。 */
    public static void restoreGameMode(ServerPlayer player) {
        DungeonPlayerData state = player.getData(DungeonData.PLAYER);
        GameType mode = switch (state.previousGameMode()) {
            case "冒险" -> GameType.ADVENTURE;
            case "生存" -> GameType.SURVIVAL;
            case "创造" -> GameType.CREATIVE;
            case "旁观" -> GameType.SPECTATOR;
            default -> null;
        };
        if (mode != null) {
            player.setGameMode(mode);
            player.setData(DungeonData.PLAYER, state.withPreviousGameMode(""));
        }
    }

    private static void enteredPainting(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        DungeonWorldData global = DungeonWorldData.get(player.server);
        if (!global.lobbyGenerated()) {
            WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fuben001"), new BlockPos(-48, 98, -48));
            Vec3 center = new Vec3(-48, 98, -48);
            for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(24), entity -> true)) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
                if (id.equals("shitiyizi") || id.equals("shitiputuan")) entity.discard();
            }
            global.markLobbyGenerated();
        }
        DungeonPlayerData state = player.getData(DungeonData.PLAYER);
        if (state.waiting() || !Relations.entityTemporaryTeam(player).isEmpty()) {
            if (state.needGenerate()) {
                double x = global.instanceX();
                if (state.templateId().equals("fuben002")) {
                    WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fuben002"), BlockPos.containing(x, 98, -100));
                    state = state.withControllerPos(BlockPos.containing(x, 98, -100))
                            .withEntryPos(x + 16, 100, -96);
                    player.setData(DungeonData.PLAYER_TRANSIENT, player.getData(DungeonData.PLAYER_TRANSIENT)
                            .withPreviousGameMode(state.previousGameMode()));
                }
                global.advanceInstance();
                player.setData(DungeonData.PLAYER, state.withNeedGenerate(false));
            }
        } else PaintingRuntime.entered(player);
    }

    private static List<ServerPlayer> waitingPlayers(ServerPlayer leader) {
        Vec3 center = leader.position();
        return leader.serverLevel().getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(32), player -> Relations.isFriendly(leader, player)
                && player.getX() <= -17 && player.getX() >= -46 && player.getZ() <= -17 && player.getZ() >= -46).stream()
                .sorted(Comparator.comparingDouble(player -> player.distanceToSqr(center))).toList();
    }

    private static void replaceController(ServerLevel level, BlockPos pos) {
        BlockState previous = level.getBlockState(pos);
        BlockState next = block("fubenblock").defaultBlockState();
        for (Map.Entry<Property<?>, Comparable<?>> entry : previous.getValues().entrySet()) next = copyProperty(next, entry.getKey().getName(), entry.getValue());
        BlockEntity old = level.getBlockEntity(pos);
        CompoundTag saved = old == null ? null : old.saveWithFullMetadata(level.registryAccess());
        if (old != null) old.setRemoved();
        level.setBlock(pos, next, 3);
        if (saved != null && level.getBlockEntity(pos) instanceof DungeonBlockEntity replacement) replacement.loadWithComponents(saved, level.registryAccess());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState copyProperty(BlockState state, String name, Comparable value) {
        Property property = state.getBlock().getStateDefinition().getProperty(name);
        return property != null && property.getPossibleValues().contains(value) ? state.setValue(property, value) : state;
    }

    /** 返回true对应来源在同维度命中时直接退出调用过程。 */
    static boolean returnFromController(ServerPlayer player, DungeonBlockEntity controller) {
        ResourceKey<Level> target = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(controller.returnDimension()));
        return travel(player, target, Mth.floor(controller.returnX()), Mth.floor(controller.returnY()), Mth.floor(controller.returnZ()));
    }

    public static boolean travel(ServerPlayer player, ResourceKey<Level> target, double x, double y, double z) {
        if (player.level().dimension().equals(target)) return true;
        ServerLevel destination = player.server.getLevel(target);
        if (destination != null) {
            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
            player.teleportTo(destination, x, y, z, player.getYRot(), player.getXRot());
            player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
            for (MobEffectInstance effect : player.getActiveEffects()) player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
            player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        }
        return false;
    }
}
