package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceMenu;

/** 传承的十一种符头循环与授予，普通使用不消耗方块或改变共享选择。 */
public final class InheritanceRuntime {
    private InheritanceRuntime() {
    }

    public static void use(Level level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof InheritanceBlockEntity inheritance)) return;
        if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE && player.isShiftKeyDown()) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new InheritanceMenu(id, inventory, inheritance),
                    inheritance.getDisplayName()), pos);
            return;
        }
        int head = inheritance.getPersistentData().getInt("weizhi");
        if (head == 0) {
            player.displayClientMessage(Component.literal("空"), true);
        } else if (head >= 1 && head <= 11) {
            CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.atLowerCornerOf(pos), Vec2.ZERO,
                    serverPlayer.serverLevel(), 4, "", Component.empty(), serverPlayer.server, null).withSuppressedOutput();
            Fulu.command(source, player.getDisplayName().getString() + " unlock futou add " + head);
        }
    }

    public static void cycle(InheritanceBlockEntity inheritance) {
        int current = inheritance.getPersistentData().getInt("weizhi");
        inheritance.getPersistentData().putInt("weizhi", current >= 1 && current < 11 ? current + 1 : 1);
        inheritance.changed();
    }
}
