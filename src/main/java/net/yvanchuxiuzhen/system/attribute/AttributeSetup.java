package net.yvanchuxiuzhen.system.attribute;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yvanchuxiuzhen.system.attribute.data.AttributeAttachments;

/**
 * 属性系统的启动装配，只在 Mod 构造时调用一次。
 * 登录时按附件重新写入原版属性；重生时原版已从旧实体复制属性基础值，附件随死亡复制，二者一致。
 */
public final class AttributeSetup {
    private AttributeSetup() {
    }

    public static void register(IEventBus modBus) {
        AttributeAttachments.register(modBus);
        NeoForge.EVENT_BUS.addListener(AttributeSetup::onLogin);
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        VanillaAttributes.apply(player, player.getData(AttributeAttachments.ATTRIBUTES).totals());
    }
}
