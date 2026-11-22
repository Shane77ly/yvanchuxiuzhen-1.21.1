package net.yvanchuxiuzhen.system.material;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 来源原初系统的材料管理解锁分支，只在命令执行维度按玩家显示名定位。 */
public final class MaterialUnlockCommand {
    private MaterialUnlockCommand() {
    }

    public static boolean run(CommandSourceStack source, String raw) {
        String[] arguments = raw.trim().split("\\s+");
        if (arguments.length < 3 || !arguments[1].equals("unlock") || !arguments[2].equals("cailiao")) return false;
        Entity executor = source.getEntity();
        if (executor == null) executor = FakePlayerFactory.getMinecraft(source.getLevel());
        for (ServerPlayer player : source.getLevel().players()) {
            if (player.getDisplayName().getString().equals(arguments[0])) {
                Materials.unlockAll(player);
                if (arguments.length > 3 && arguments[3].equals("true") && executor instanceof Player owner) {
                    owner.displayClientMessage(Component.literal("§6已解锁所有炼器材料的属性显示。"), false);
                }
            } else if (executor instanceof Player owner) {
                owner.displayClientMessage(Component.literal("§4未找到名为“" + arguments[0] + "”的玩家"), false);
            }
        }
        return true;
    }
}
