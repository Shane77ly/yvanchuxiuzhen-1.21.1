package net.yvanchuxiuzhen.system.dungeon.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.dungeon.DungeonDefinitions;
import net.yvanchuxiuzhen.system.dungeon.DungeonEntrances;
import net.yvanchuxiuzhen.system.dungeon.DungeonPlayers;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenu;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenus;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;

/** 副本自然交互、来源按钮、重生模式恢复与镇妖塔补充定义的服务端入口。 */
public final class Dungeons {
    private Dungeons() {
    }

    public static void open(ServerPlayer player, BlockPos pos, boolean management) {
        if (player.level().getBlockEntity(pos) instanceof DungeonBlockEntity block) {
            int kind = switch (block.kind()) {
                case "tiandaofuben" -> management ? DungeonMenu.MANAGE : DungeonMenu.OPEN;
                case "fubenblock" -> DungeonMenu.CONTROLLER;
                case "fubenentity" -> DungeonMenu.MARKER;
                default -> -1;
            };
            if (kind >= 0) DungeonMenus.open(player, block, kind);
        }
    }

    public static void enter(ServerPlayer player, BlockPos pos) {
        if (player.level().getBlockEntity(pos) instanceof DungeonBlockEntity block && block.kind().equals("tiandaofuben")) DungeonEntrances.enter(player, block);
    }

    public static void configureTower(ServerLevel level) {
        DungeonDefinitions.configureTower(level);
    }

    /** 普通重生按来源附件恢复游戏模式，调用不受玩家当前维度限制。 */
    public static void restoreRespawnGameMode(ServerPlayer player) {
        DungeonPlayers.restoreGameMode(player);
    }

    /** 轮回清理 chuansongitem，只清画里乾坤的玩家传送栈。 */
    public static void clearPaintingTransit(ServerPlayer player) {
        player.setData(DungeonData.PAINTING_TRANSIT, ItemStack.EMPTY);
    }
}
