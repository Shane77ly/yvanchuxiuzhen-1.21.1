package net.yvanchuxiuzhen.system.quest;

import java.util.Locale;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.EconomyDialogueRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.npc.api.NpcDialogueRequestEvent;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;

/** 来源普通对话的顺序执行器；会话、任务目录和剧情扩展数据均由服务端维护。 */
public final class DialogueRuntime {
    private DialogueRuntime() {
    }

    public static void request(EconomyDialogueRequestEvent event) {
        DialogueActions.request(event.player(), event.script());
    }

    public static void requestNpc(NpcDialogueRequestEvent event) {
        for (String script : event.scripts()) {
            String id = resolve(script).split("%")[0].split("/")[1];
            if (DialogueActions.finished(event.player(), id)) continue;
            DialogueActions.request(event.player(), script);
            EconomyMenus.setShopInformation(event.player(), event.information());
            EconomyMenus.setProductList(event.player(), event.information().isEmpty() ? "" : event.products());
            return;
        }
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) player.setData(QuestData.SESSION, new CompoundTag());
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag state = DialogueActions.snapshot(player);
        if (!state.getBoolean("renwubegin")) return;
        state.putBoolean("renwubegin", false);
        String script = state.getString("renwulist");
        if (script.isEmpty()) {
            player.setData(QuestData.SESSION, state);
            return;
        }
        String[] header = resolve(script).split("%")[0].split("/");
        CompoundTag journal = DialogueActions.journal(player);
        String id = header[1];
        if (journal.getString(id + "UUID").equals(id)) {
            if (DialogueActions.finished(player, id)) {
                player.displayClientMessage(Component.literal("您已完成" + journal.getString(id + "name") + "任务，无法重复执行任务。"), false);
            } else {
                state.putInt("renwuweizhi", journal.getInt(id + "jindu"));
            }
        } else {
            state.putInt("renwuweizhi", 1);
        }
        player.setData(QuestData.SESSION, state);
        run(player, BlockPos.containing(player.getX(), player.getY(), player.getZ()));
    }

    public static void next(ServerPlayer player, BlockPos pos) {
        CompoundTag state = DialogueActions.snapshot(player);
        state.putInt("renwuweizhi", state.getInt("renwuweizhi") + 1);
        player.setData(QuestData.SESSION, state);
        run(player, pos);
    }

    public static boolean choose(ServerPlayer player, int choice, BlockPos pos) {
        CompoundTag state = DialogueActions.snapshot(player);
        int target = state.getInt("daan" + choice);
        if (target <= 0) return false;
        state.putInt("renwuweizhi", target);
        player.setData(QuestData.SESSION, state);
        run(player, pos);
        return true;
    }

    public static void run(ServerPlayer player, BlockPos pos) {
        CompoundTag state = DialogueActions.snapshot(player);
        for (int i = 1; i <= 4; i++) {
            state.putString("daantxt" + i, "");
            state.putInt("daan" + i, 0);
        }
        while (!state.getString("renwulist").isEmpty()) {
            String[] steps = resolve(state.getString("renwulist")).split("%");
            int cursor = state.getInt("renwuweizhi");
            String[] action = steps[cursor].split("/");
            switch (action[0]) {
                case "文字", "自发文字", "选项" -> {
                    state.putString("TXTxianshi", (action[0].equals("自发文字") ? player.getDisplayName().getString() + "：" : "") + action[1]);
                    boolean choices = action[0].equals("选项");
                    if (choices) {
                        for (int i = 1; i <= integer(action[2]); i++) {
                            state.putString("daantxt" + i, action[i * 2 + 1]);
                            state.putInt("daan" + i, integer(action[i * 2 + 2]));
                        }
                    }
                    player.setData(QuestData.SESSION, state);
                    BlockPos anchor = choices ? pos : BlockPos.containing(player.getX(), player.getY() + 1, player.getZ());
                    player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new DialogueMenu(id, inventory, anchor, choices),
                            Component.literal(choices ? "对话选项" : "对话")), anchor);
                    return;
                }
                case "判断物品" -> state.putInt("renwuweizhi", integer(action[count(player, item(action[1])) >= number(action[2]) ? 3 : 4]));
                case "判断当前坐标" -> {
                    Vec3 center = new Vec3(number(action[1]), number(action[2]), number(action[3]));
                    boolean present = player.serverLevel().getEntitiesOfClass(Player.class,
                            new AABB(center, center).inflate(number(action[4]) / 2), candidate -> candidate == player).contains(player);
                    state.putInt("renwuweizhi", integer(action[present ? 5 : 6]));
                }
                case "判断当前维度" -> {
                    // 来源在维度字符串中多拼一个右括号，因此按实际比较结果保留失败分支。
                    String dimension = "ResourceKey[minecraft:dimension / ]" + action[1] + "]";
                    state.putInt("renwuweizhi", integer(action[player.level().dimension().toString().equals(dimension) ? 2 : 3]));
                }
                case "物品" -> {
                    changeItems(player, action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "改变nbt" -> {
                    changeFlag(player, state, action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "判断nbt", "判断数值" -> state.putInt("renwuweizhi", compareFlag(player, action));
                case "判断任务" -> {
                    String status = DialogueActions.journal(player).getString(action[1] + "zhuangtai");
                    state.putInt("renwuweizhi", integer(action[status.equals("完成") ? 2 : status.equals("失败") ? 3 : 4]));
                }
                case "跳转" -> state.putInt("renwuweizhi", integer(action[1]));
                case "任务进度更改" -> {
                    state.putString("renwuzhuangtai", action[1]);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "任务储存" -> {
                    saveTask(player, state, steps[0].split("/"), action);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "任务设定修改" -> {
                    String[] header = steps[0].split("/");
                    header[action[1].equals("名称") ? 0 : 2] = action[2];
                    steps[0] = String.join("/", header);
                    state.putString("renwulist", String.join("%", steps));
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "删除方块" -> {
                    for (int i = 1; i + 2 < action.length; i += 3) {
                        player.serverLevel().removeBlock(BlockPos.containing(number(action[i]), number(action[i + 1]), number(action[i + 2])), false);
                    }
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "裂隙解封" -> {
                    StructureSearch.search(player.serverLevel(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), player.getStringUUID());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "改变坐标" -> {
                    player.connection.teleport(number(action[1]), number(action[2]), number(action[3]), player.getYRot(), player.getXRot());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "药剂效果", "指令" -> {
                    String command = action[0].equals("指令") ? action[1] : action[1].equals("加")
                            ? "effect give " + player.getDisplayName().getString() + " " + action[2] + " " + action[4] + " " + action[3]
                            : "effect clear " + player.getDisplayName().getString() + " " + action[2];
                    CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, player.serverLevel(), 4,
                            "", Component.empty(), player.server, null).withSuppressedOutput();
                    player.server.getCommands().performPrefixedCommand(source, command);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case "任务转接" -> {
                    state.putString("renwulist", resolve(action[1]));
                    state.putInt("renwuweizhi", 0);
                    state.putBoolean("renwubegin", true);
                    player.setData(QuestData.SESSION, state);
                    return;
                }
                case "打开界面" -> {
                    clearScript(state);
                    player.setData(QuestData.SESSION, state);
                    EconomyMenus.open(player, BlockPos.containing(player.getX(), player.getY() + 1, player.getZ()), action[1]);
                    return;
                }
                case "结束" -> {
                    clearScript(state);
                    player.setData(QuestData.SESSION, state);
                    player.closeContainer();
                    return;
                }
                default -> {
                    // 第0步是来源任务头；未接入的扩展动作保留当前位置，不将其当作成功。
                    player.setData(QuestData.SESSION, state);
                    return;
                }
            }
            player.setData(QuestData.SESSION, state.copy());
        }
    }

    private static void saveTask(ServerPlayer player, CompoundTag state, String[] header, String[] action) {
        CompoundTag journal = DialogueActions.journal(player);
        String id = header[1];
        if (!journal.getString(id + "UUID").equals(id)) {
            String list = journal.getString("renwulist");
            journal.putString("renwulist", list.isEmpty() ? id : list + "%" + id);
        }
        journal.putString(id + "name", header[0]);
        journal.putString(id + "UUID", id);
        journal.putString(id + "shuoming", header[2]);
        journal.putInt(id + "jindu", integer(action[1]));
        String status = state.getString("renwuzhuangtai");
        journal.putString(id + "zhuangtai", status.isEmpty() ? "未完成" : status);
        journal.putString(id + "xvqiu", header[5]);
        journal.putString(id + "jiangli", header[6]);
        journal.putString(id + "zhengti", state.getString("renwulist"));
        journal.putBoolean(id + "gaojirenwu", !state.getBoolean("tasksustained"));
        player.setData(QuestData.JOURNAL, journal);
    }

    private static int compareFlag(ServerPlayer player, String[] action) {
        CompoundTag flags = player.getData(QuestData.FLAGS);
        boolean literal = action[0].equals("判断数值");
        int result;
        if (action[1].equals("char") || action[1].equals("str")) {
            result = flags.getString(action[2]).equals(literal ? action[3] : flags.getString(action[3])) ? 4 : 5;
        } else if (action[1].equals("logic")) {
            result = flags.getBoolean(action[2]) == (literal ? action[3].equals("真") : flags.getBoolean(action[3])) ? 4 : 5;
        } else {
            double left = action[1].equals("int") ? flags.getInt(action[2]) : flags.getDouble(action[2]);
            double right = literal ? number(action[3]) : action[1].equals("int") ? flags.getInt(action[3]) : flags.getDouble(action[3]);
            result = left > right ? 4 : left < right ? 5 : 6;
        }
        return integer(action[result]);
    }

    private static void changeFlag(ServerPlayer player, CompoundTag state, String[] action) {
        CompoundTag flags = player.getData(QuestData.FLAGS).copy();
        if (action[1].equals("char") || action[1].equals("str")) {
            flags.putString(action[2], (action[3].equals("加") ? state.getString(action[2]) : "") + action[4]);
        } else if (action[1].equals("logic")) {
            flags.putBoolean(action[2], action[4].equals("真"));
        } else {
            double old = action[1].equals("int") ? flags.getInt(action[2]) : flags.getDouble(action[2]);
            double operand = number(action[4]);
            double result = switch (action[3]) {
                case "加" -> old + operand;
                case "减" -> old - operand;
                case "乘" -> old * operand;
                case "除" -> old / operand;
                default -> operand;
            };
            if (action[1].equals("int")) flags.putInt(action[2], (int) result);
            else flags.putDouble(action[2], result);
        }
        player.setData(QuestData.FLAGS, flags);
    }

    private static void changeItems(ServerPlayer player, String[] action) {
        Item kind = item(action[2]);
        int amount = integer(action[3]);
        if (action[1].equals("加")) {
            ItemStack stack = new ItemStack(kind, amount);
            CompoundTag data = new CompoundTag();
            if (!action[4].equals("无") && !action[4].isEmpty()) {
                for (int i = 4; i + 2 < action.length; i += 3) {
                    String key = action[i];
                    String value = action[i + 2];
                    switch (action[i + 1]) {
                        case "char", "str" -> data.putString(key, value);
                        case "logic" -> data.putBoolean(key, value.equals("真"));
                        case "int" -> data.putInt(key, integer(value));
                        case "double" -> data.putDouble(key, number(value));
                        case "name" -> stack.set(DataComponents.CUSTOM_NAME, Component.literal("§" + key + value));
                        default -> { }
                    }
                }
            }
            if (!data.isEmpty()) stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        } else if (action[4].equals("无") || action[4].isEmpty()) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(kind), amount, player.inventoryMenu.getCraftSlots());
        } else {
            // 来源带NBT扣除在足量栈分支先把剩余数清零，再按零删除；此分支保持该实际次序。
            PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
            int nbtIndex = 4;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.is(kind)) continue;
                CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                boolean matches = false;
                for (int i = 0; i < (action.length - 4) / 3; i++) {
                    String key = action[nbtIndex];
                    String value = action[nbtIndex + 2];
                    matches = switch (action[nbtIndex + 1]) {
                        case "char", "str" -> data.getString(key).equals(value);
                        case "logic" -> data.getBoolean(key) == value.equals("真");
                        case "int" -> data.getInt(key) == number(value);
                        case "double" -> data.getDouble(key) == number(value);
                        default -> false;
                    };
                    if (!matches) break;
                    nbtIndex += 3;
                }
                if (!matches) continue;
                if (amount < 1) break;
                if (stack.getCount() >= amount) amount = 0;
                else {
                    amount -= stack.getCount();
                    player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(kind), stack.getCount(), player.inventoryMenu.getCraftSlots());
                }
            }
        }
    }

    private static int count(Player player, Item kind) {
        PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(kind)) count += stack.getCount();
        }
        return count;
    }

    public static String resolve(String value) {
        return Component.translatable(value).getString();
    }

    private static Item item(String id) {
        ResourceLocation location = ResourceLocation.parse(id.toLowerCase(Locale.ENGLISH));
        if (location.getNamespace().equals("yvanchuxitong")) location = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, location.getPath());
        return BuiltInRegistries.ITEM.get(location);
    }

    private static int integer(String value) {
        return (int) number(value);
    }

    private static double number(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static void clearScript(CompoundTag state) {
        state.putString("renwulist", "");
        state.putInt("renwuweizhi", 0);
    }
}
