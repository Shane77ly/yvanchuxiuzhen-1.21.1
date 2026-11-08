package net.yvanchuxiuzhen.system.profession;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionData;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;

/** 副职业附件与玩家每刻末尾升级入口；来源 {@code PlayerTick:394 -> FuzhiyeLevelUp}。 */
public final class ProfessionSetup {
    private ProfessionSetup() {
    }

    public static void register(IEventBus modBus) {
        ProfessionAttachments.register(modBus);
        Gathering.register();
        NeoForge.EVENT_BUS.addListener(ProfessionSetup::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ProfessionSetup::onLogin);
        NeoForge.EVENT_BUS.addListener(ProfessionSetup::onRespawn);
        NeoForge.EVENT_BUS.addListener(ProfessionSetup::onChangedDimension);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ProfessionData data = player.getData(ProfessionAttachments.PROGRESS);
        for (Profession profession : Profession.values()) {
            ProfessionProgress current = data.get(profession);
            if (current.level() < 9 && current.experience() >= current.experienceMax()) {
                double level = current.level() + 1;
                data.set(profession, new ProfessionProgress(level,
                        current.experience() - current.experienceMax(), level * 100));
                if (profession == Profession.GATHERING) {
                    mirrorGatheringLevel(player);
                }
                player.syncData(ProfessionAttachments.PROGRESS);
                player.displayClientMessage(Component.literal(profession.levelUpMessage()).withStyle(ChatFormatting.GOLD), false);
            }
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    private static void sync(ServerPlayer player) {
        mirrorGatheringLevel(player);
        player.syncData(ProfessionAttachments.PROGRESS);
    }

    /** 来源 {@code LeftClickBlock} 向原初系统复制采集等级；兼容现有 combat 对该持久字段的读取。 */
    private static void mirrorGatheringLevel(ServerPlayer player) {
        player.getPersistentData().putDouble("collection_LEVEL", Professions.level(player, Profession.GATHERING));
    }
}
