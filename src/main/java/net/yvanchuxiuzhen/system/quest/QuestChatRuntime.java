package net.yvanchuxiuzhen.system.quest;

import java.util.Set;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;

/** 来源连续任务在聊天栏输出，文字按60刻节拍推进，条件与扩展动作在本次调用内继续。 */
final class QuestChatRuntime {
    private static final Set<String> EXTENDED = Set.of("任务进度更改", "任务储存", "物品", "文本插入选项", "判断任务", "高级物品判断",
            "判断当前坐标", "判断当前维度", "跟随玩家", "取消跟随", "删除实体", "伤害实体", "移动到坐标", "任务设定修改", "裂隙解封", "删除方块");

    private QuestChatRuntime() {
    }

    static void run(ServerPlayer player) {
        CompoundTag state = DialogueActions.snapshot(player);
        if (state.getInt("playerxvanze") != 0) {
            state.putInt("renwuweizhi", state.getInt("playerxvanze"));
            state.putInt("playerxvanze", 0);
        }
        while (!state.getString("renwulist").isEmpty()) {
            String[] steps = state.getString("renwulist").split("%");
            int cursor = state.getInt("renwuweizhi");
            String[] action = steps[cursor].split("/");
            switch (action[0]) {
                case "文字", "自发文字" -> {
                    player.displayClientMessage(Component.literal((action[0].equals("自发文字") ? player.getDisplayName().getString() + "：" : "") + action[1]), false);
                    state.putInt("renwuweizhi", cursor + 1);
                    player.setData(QuestData.SESSION, state.copy());
                    // 来源主过程在文字后只调用第二、第三扩展过程，下一条主过程文字留到下一节拍。
                    if (!EXTENDED.contains(steps[cursor + 1].split("/")[0])) return;
                }
                case "选项" -> {
                    prepareOptions(player);
                    player.displayClientMessage(Component.literal(action[1]), false);
                    StringBuilder command = new StringBuilder("tellraw " + player.getDisplayName().getString() + " [");
                    for (int option = 1; option <= DialogueRuntime.integer(action[2]); option++) {
                        if (option > 1) command.append(',');
                        command.append("{\"text\":\"<").append(action[option * 2 + 1]).append(" >\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/taskduihua ")
                                .append(player.getDisplayName().getString()).append(" renwuweizhi ").append(option).append("\"}}");
                        state.putInt("daan" + option, DialogueRuntime.integer(action[option * 2 + 2]));
                    }
                    state.putBoolean("xvanxiang", true);
                    player.setData(QuestData.SESSION, state);
                    command(player, command.append(']').toString());
                    return;
                }
                case "文本插入选项" -> {
                    prepareOptions(player);
                    state.putBoolean("xvanxiang", true);
                    state.putInt("renwuxvanzeweizhi", DialogueRuntime.integer(action[4]));
                    player.setData(QuestData.SESSION, state);
                    command(player, "tellraw " + player.getDisplayName().getString() + " [{\"text\":\"" + action[1]
                            + "\",\"color\":\"white\"},{\"text\":\"<" + action[2]
                            + ">\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/scoreboard players set -2\"}},{\"text\":\""
                            + action[3] + "\",\"color\":\"white\"}]");
                    return;
                }
                case "改变nbt" -> {
                    DialogueRuntime.changeFlag(player, state, action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "判断nbt", "判断数值" -> state.putInt("renwuweizhi", DialogueRuntime.compareFlag(player, action));
                case "判断物品" -> state.putInt("renwuweizhi", DialogueRuntime.integer(action[DialogueRuntime.count(player, DialogueRuntime.item(action[1])) >= DialogueRuntime.number(action[2]) ? 3 : 4]));
                case "高级物品判断" -> state.putInt("renwuweizhi", DialogueRuntime.integer(action[DialogueRuntime.countAdvancedItems(player, action) >= DialogueRuntime.number(action[2]) ? 3 : 4]));
                case "物品" -> {
                    if (action[1].equals("加")) DialogueRuntime.changeItems(player, action);
                    else player.getInventory().clearOrCountMatchingItems(stack -> stack.is(DialogueRuntime.item(action[2])), DialogueRuntime.integer(action[3]), player.inventoryMenu.getCraftSlots());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "判断当前坐标" -> {
                    Vec3 center = new Vec3(DialogueRuntime.number(action[1]), DialogueRuntime.number(action[2]), DialogueRuntime.number(action[3]));
                    boolean present = player.serverLevel().getEntitiesOfClass(Player.class, new AABB(center, center).inflate(DialogueRuntime.number(action[4]) / 2), candidate -> candidate == player).contains(player);
                    state.putInt("renwuweizhi", DialogueRuntime.integer(action[present ? 5 : 6]));
                }
                case "判断当前维度" -> {
                    String dimension = "ResourceKey[minecraft:dimension / ]" + action[1] + "]";
                    state.putInt("renwuweizhi", DialogueRuntime.integer(action[player.level().dimension().toString().equals(dimension) ? 2 : 3]));
                }
                case "判断任务" -> {
                    String status = DialogueActions.journal(player).getString(action[1] + "zhuangtai");
                    state.putInt("renwuweizhi", DialogueRuntime.integer(action[status.equals("完成") ? 2 : status.equals("失败") ? 3 : 4]));
                }
                case "跟随玩家", "取消跟随", "删除实体", "伤害实体", "移动到坐标" -> {
                    DialogueRuntime.controlEntities(player, action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "改变坐标" -> {
                    player.connection.teleport(DialogueRuntime.number(action[1]), DialogueRuntime.number(action[2]), DialogueRuntime.number(action[3]), player.getYRot(), player.getXRot());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "药剂效果", "指令" -> {
                    String command = action[0].equals("指令") ? action[1] : action[1].equals("加")
                            ? "effect give " + player.getDisplayName().getString() + " " + action[2] + " " + action[4] + " " + action[3]
                            : "effect clear " + player.getDisplayName().getString() + " " + action[2];
                    command(player, command);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "跳转" -> state.putInt("renwuweizhi", DialogueRuntime.integer(action[1]));
                case "任务进度更改" -> {
                    state.putString("renwuzhuangtai", action[1]);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "任务储存" -> {
                    if (state.getString("renwuzhuangtai").isEmpty()) state.putString("renwuzhuangtai", "未完成");
                    DialogueRuntime.saveTask(player, state, steps[0].split("/"), action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "任务设定修改" -> {
                    String[] header = steps[0].split("/");
                    String changed = "";
                    if (action[1].equals("名称") || action[1].equals("说明")) {
                        header[action[1].equals("名称") ? 0 : 2] = action[2];
                        changed = String.join("/", header);
                    }
                    steps[0] = changed;
                    state.putString("renwulist", String.join("%", steps));
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "裂隙解封" -> {
                    StructureSearch.search(player.serverLevel(), player.position(), player.getStringUUID());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "删除方块" -> {
                    for (int index = 1; index + 2 < action.length; index += 3) {
                        player.serverLevel().setBlock(BlockPos.containing(DialogueRuntime.number(action[index]), DialogueRuntime.number(action[index + 1]), DialogueRuntime.number(action[index + 2])), Blocks.AIR.defaultBlockState(), 3);
                    }
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "结束" -> {
                    state.putString("renwulist", "");
                    state.putInt("renwuweizhi", 0);
                    state.putBoolean("tasksustained", false);
                    state.putBoolean("xvanxiang", false);
                    state.putInt("tasktick", 0);
                    player.setData(QuestData.SESSION, state);
                    return;
                }
                default -> {
                    player.setData(QuestData.SESSION, state);
                    return;
                }
            }
            player.setData(QuestData.SESSION, state.copy());
        }
    }

    private static void prepareOptions(ServerPlayer player) {
        command(player, "scoreboard objectives add renwuweizhi dummy \"任务选项位置\"");
        player.serverLevel().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, player.server);
    }

    private static void command(ServerPlayer player, String command) {
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, player.serverLevel(), 4,
                "", Component.empty(), player.server, null).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, command);
    }
}
