package net.yvanchuxiuzhen.flow;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;

/** 按来源普通重生顺序编排修炼修正、副本模式恢复、人间界返回与延迟属性重算。 */
public final class PlayerRespawnFlow {
    private static final ResourceKey<Level> HUMAN_REALM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian"));

    private PlayerRespawnFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerRespawnFlow::onRespawn);
    }

    /** 来源未按死亡原因或末地返回标记过滤；延迟重算保留事件入口的世界与玩家对象。 */
    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel entryLevel = player.serverLevel();
        Cultivation.rebirth(player);
        Dungeons.restoreRespawnGameMode(player);
        returnToHumanRealm(player);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, new AttributeRecalculation(entryLevel, player));
    }

    private static void returnToHumanRealm(ServerPlayer player) {
        if (!player.level().dimension().equals(HUMAN_REALM) && Cultivation.get(player, CultivationText.REVIVE).equals("人间界")) {
            ServerLevel destination = player.server.getLevel(HUMAN_REALM);
            if (destination != null) {
                // 此事件先于连接替换死亡对象；原生传送通过连接写坐标，须先指向已加入世界的重生对象。
                player.connection.player = player;
                player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
                player.teleportTo(destination, 0, 32, 0, player.getYRot(), player.getXRot());
                player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
                }
                player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
            }
            Cultivation.set(player, CultivationText.REVIVE, "");
        }
    }

    /** 每次重生独立计满二十个服务端结束刻，按来源对捕获的同一玩家和原世界执行一次重算。 */
    private static final class AttributeRecalculation implements Consumer<ServerTickEvent.Post> {
        private final ServerLevel entryLevel;
        private final ServerPlayer player;
        private int remaining = 20;

        private AttributeRecalculation(ServerLevel entryLevel, ServerPlayer player) {
            this.entryLevel = entryLevel;
            this.player = player;
        }

        @Override
        public void accept(ServerTickEvent.Post event) {
            remaining--;
            if (remaining == 0) {
                PlayerAttributes.recalculate(entryLevel, player);
                NeoForge.EVENT_BUS.unregister(this);
            }
        }
    }
}
