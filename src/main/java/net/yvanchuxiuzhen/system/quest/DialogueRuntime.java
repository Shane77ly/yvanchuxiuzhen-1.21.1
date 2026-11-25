package net.yvanchuxiuzhen.system.quest;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.economy.api.EconomyDialogueRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyItems;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.npc.api.NpcDialogueRequestEvent;
import net.yvanchuxiuzhen.system.npc.api.NpcFollowers;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplates;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;
import net.yvanchuxiuzhen.system.xiulian.api.BirthItems;

/** 强类型对话执行器；全面基于 {@link QuestTemplateDefinition} 与 {@link DialogueNodeDefinition} 驱动。 */
public final class DialogueRuntime {
    private static final ResourceLocation ASCENSION_TOKEN = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_09");
    private static final ResourceLocation INHERITANCE_RELIC = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_18");

    private DialogueRuntime() {
    }

    public static void request(EconomyDialogueRequestEvent event) {
        DialogueActions.request(event.player(), event.script());
    }

    /** 全局右键先读取主手物品，再由物品 use 处理效果或增加领取次数。 */
    public static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        ItemStack stack = player.getMainHandItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId.equals(ASCENSION_TOKEN)) {
            DialogueActions.request(player, "ascension");
        } else if (player.isShiftKeyDown() && itemId.equals(INHERITANCE_RELIC)
                && (int) BirthItems.supplyCount(stack) == 30) {
            DialogueActions.request(player, "inheritance");
        }
    }

    public static void requestNpc(NpcDialogueRequestEvent event) {
        for (String scriptId : event.scripts()) {
            Optional<QuestTemplateDefinition> template = QuestTemplates.find(scriptId);
            if (template.isEmpty()) continue;
            if (DialogueActions.finished(event.player(), template.get().questId())) continue;
            ShopCatalogDefinition shop = event.catalog().filter(c -> !c.info().isEmpty() || !c.products().isEmpty())
                    .or(() -> EconomyCatalogs.find(event.shopId()))
                    .orElse(ShopCatalogDefinition.EMPTY);
            EconomyMenus.setShopCatalog(event.player(), shop);
            DialogueActions.request(event.player(), template.get());
            return;
        }
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) player.setData(QuestData.SESSION, new CompoundTag());
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag state = DialogueActions.snapshot(player);
        if (!state.getBoolean("renwubegin") && !state.getBoolean("taskbegin")) {
            if (state.getBoolean("tasksustained") && !state.getBoolean("xvanxiang")) {
                int tick = state.getInt("tasktick") + 1;
                state.putInt("tasktick", tick);
                player.setData(QuestData.SESSION, state);
                if (tick >= 60) {
                    QuestChatRuntime.run(player);
                    CompoundTag updated = DialogueActions.snapshot(player);
                    updated.putInt("tasktick", 0);
                    player.setData(QuestData.SESSION, updated);
                }
            }
            return;
        }
        state.putBoolean("renwubegin", false);
        state.putBoolean("taskbegin", false);
        Optional<QuestTemplateDefinition> template = DialogueActions.readTemplate(state);
        if (template.isEmpty()) {
            player.setData(QuestData.SESSION, state);
            if (player.containerMenu != player.inventoryMenu) {
                player.closeContainer();
            }
            return;
        }
        String id = template.get().questId();
        CompoundTag journal = DialogueActions.journal(player);
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
        while (true) {
            QuestTemplateDefinition template = DialogueActions.readTemplate(state).orElse(null);
            if (template == null) return;
            int cursor = state.getInt("renwuweizhi");
            DialogueNodeDefinition node = template.node(cursor).orElse(null);
            if (node == null) {
                player.setData(QuestData.SESSION, state);
                return;
            }
            switch (node) {
                case DialogueNodeDefinition.TextNode text -> {
                    String prefix = text.playerSpeak() ? player.getDisplayName().getString() + "：" : "";
                    state.putString("TXTxianshi", prefix + text.text());
                    player.setData(QuestData.SESSION, state);
                    BlockPos anchor = BlockPos.containing(player.getX(), player.getY() + 1, player.getZ());
                    player.openMenu(new SeamlessMenuProvider((id, inventory, ignored) -> new DialogueMenu(id, inventory, anchor, false),
                            Component.literal("对话")), anchor);
                    return;
                }
                case DialogueNodeDefinition.ChoiceNode choice -> {
                    state.putString("TXTxianshi", choice.prompt());
                    int count = Math.min(4, choice.options().size());
                    for (int index = 0; index < count; index++) {
                        DialogueNodeDefinition.ChoiceOption option = choice.options().get(index);
                        state.putString("daantxt" + (index + 1), option.label());
                        state.putInt("daan" + (index + 1), option.target());
                    }
                    player.setData(QuestData.SESSION, state);
                    player.openMenu(new SeamlessMenuProvider((id, inventory, ignored) -> new DialogueMenu(id, inventory, pos, true),
                            Component.literal("对话选项")), pos);
                    return;
                }
                case DialogueNodeDefinition.CheckItemNode check -> state.putInt("renwuweizhi",
                        count(player, item(check.item())) >= check.count() ? check.passTarget() : check.failTarget());
                case DialogueNodeDefinition.CheckAdvancedItemNode check -> state.putInt("renwuweizhi",
                        countAdvancedItems(player, check) >= check.count() ? check.passTarget() : check.failTarget());
                case DialogueNodeDefinition.EntityControlNode entityControl -> {
                    controlEntities(player, entityControl);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CheckPosNode checkPos -> {
                    Vec3 center = new Vec3(checkPos.x(), checkPos.y(), checkPos.z());
                    boolean present = player.serverLevel().getEntitiesOfClass(Player.class,
                            new AABB(center, center).inflate(checkPos.size() / 2), candidate -> candidate == player).contains(player);
                    state.putInt("renwuweizhi", present ? checkPos.insideTarget() : checkPos.outsideTarget());
                }
                case DialogueNodeDefinition.CheckDimensionNode checkDim -> {
                    // 来源在维度字符串中多拼一个右括号，因此按实际比较结果保留失败分支。
                    String dimension = "ResourceKey[minecraft:dimension / ]" + checkDim.dimension() + "]";
                    state.putInt("renwuweizhi", player.level().dimension().toString().equals(dimension)
                            ? checkDim.matchTarget() : checkDim.mismatchTarget());
                }
                case DialogueNodeDefinition.ItemActionNode itemAction -> {
                    if (itemAction.give()) giveItem(player, itemAction);
                    else takeItem(player, itemAction);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.ModifyFlagNode modify -> {
                    changeFlag(player, state, modify);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CheckFlagNode check -> state.putInt("renwuweizhi", compareFlag(player, check));
                case DialogueNodeDefinition.CheckQuestNode checkQuest -> {
                    String status = DialogueActions.journal(player).getString(checkQuest.questId() + "zhuangtai");
                    state.putInt("renwuweizhi", status.equals("完成") ? checkQuest.completedTarget()
                            : status.equals("失败") ? checkQuest.failedTarget() : checkQuest.otherwiseTarget());
                }
                case DialogueNodeDefinition.JumpNode jump -> state.putInt("renwuweizhi", jump.target());
                case DialogueNodeDefinition.SetQuestStatusNode status -> {
                    state.putString("renwuzhuangtai", status.status());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.SaveQuestNode save -> {
                    saveTask(player, state, template, save.resumeCursor());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.ModifyQuestHeaderNode modify -> {
                    QuestTemplateDefinition updated = modify.field().equals("名称")
                            ? template.withHeader(modify.value(), template.description())
                            : modify.field().equals("说明")
                                    ? template.withHeader(template.title(), modify.value())
                                    : template;
                    DialogueActions.writeTemplate(state, updated);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.RemoveBlocksNode remove -> {
                    for (BlockPos targetPos : remove.positions()) {
                        player.serverLevel().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.UnsealRiftNode ignored -> {
                    StructureSearch.search(player.serverLevel(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), player.getStringUUID());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.TeleportNode teleport -> {
                    player.connection.teleport(teleport.x(), teleport.y(), teleport.z(), player.getYRot(), player.getXRot());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.EffectNode effect -> {
                    String command = effect.op().equals("加")
                            ? "effect give " + player.getDisplayName().getString() + " " + effect.effectId() + " " + effect.duration() + " " + effect.amplifier()
                            : "effect clear " + player.getDisplayName().getString() + " " + effect.effectId();
                    executeCommand(player, command);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CommandNode cmd -> {
                    executeCommand(player, cmd.command());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.TransferQuestNode transfer -> {
                    DialogueActions.request(player, transfer.targetDialogueId());
                    return;
                }
                case DialogueNodeDefinition.OpenMenuNode menu -> {
                    clearScript(state);
                    player.setData(QuestData.SESSION, state);
                    EconomyMenus.open(player, BlockPos.containing(player.getX(), player.getY() + 1, player.getZ()), menu.menu());
                    return;
                }
                case DialogueNodeDefinition.EndNode ignored -> {
                    clearScript(state);
                    player.setData(QuestData.SESSION, state);
                    player.closeContainer();
                    return;
                }
                case DialogueNodeDefinition.InsertChatOptionNode ignored -> {
                    player.setData(QuestData.SESSION, state);
                    return;
                }
            }
            player.setData(QuestData.SESSION, state.copy());
        }
    }

    private static void executeCommand(ServerPlayer player, String command) {
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, player.serverLevel(), 4,
                "", Component.empty(), player.server, null).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, command);
    }

    static void saveTask(ServerPlayer player, CompoundTag state, QuestTemplateDefinition template, int resumeCursor) {
        CompoundTag journal = DialogueActions.journal(player);
        String id = template.questId();
        if (!journal.getString(id + "UUID").equals(id)) {
            ListTag ids = journal.getList("quest_ids", Tag.TAG_STRING);
            ids.add(StringTag.valueOf(id));
            journal.put("quest_ids", ids);
        }
        journal.putString(id + "name", template.title());
        journal.putString(id + "UUID", id);
        journal.putString(id + "shuoming", template.description());
        journal.putInt(id + "jindu", resumeCursor);
        String status = state.getString("renwuzhuangtai");
        journal.putString(id + "zhuangtai", status.isEmpty() ? "未完成" : status);
        journal.putString(id + "xvqiu", template.requirement());
        journal.putString(id + "jiangli", template.rewardSummary());
        journal.putString(id + "template_id", template.id());
        journal.put(id + "template", QuestTemplateDefinition.CODEC.encodeStart(NbtOps.INSTANCE, template).getOrThrow());
        journal.putBoolean(id + "gaojirenwu", !state.getBoolean("tasksustained"));
        player.setData(QuestData.JOURNAL, journal);
    }

    static int compareFlag(ServerPlayer player, DialogueNodeDefinition.CheckFlagNode node) {
        CompoundTag flags = player.getData(QuestData.FLAGS);
        if (node.fieldType().equals("char") || node.fieldType().equals("str")) {
            boolean match = flags.getString(node.key()).equals(node.literal() ? node.targetOrLiteral() : flags.getString(node.targetOrLiteral()));
            return match ? node.firstTarget() : node.secondTarget();
        }
        if (node.fieldType().equals("logic")) {
            boolean match = flags.getBoolean(node.key()) == (node.literal() ? node.targetOrLiteral().equals("真") : flags.getBoolean(node.targetOrLiteral()));
            return match ? node.firstTarget() : node.secondTarget();
        }
        double left = node.fieldType().equals("int") ? flags.getInt(node.key()) : flags.getDouble(node.key());
        double right = node.literal() ? number(node.targetOrLiteral())
                : node.fieldType().equals("int") ? flags.getInt(node.targetOrLiteral()) : flags.getDouble(node.targetOrLiteral());
        if (left > right) return node.firstTarget();
        if (left < right) return node.secondTarget();
        return node.equalTarget();
    }

    static void changeFlag(ServerPlayer player, CompoundTag state, DialogueNodeDefinition.ModifyFlagNode node) {
        CompoundTag flags = player.getData(QuestData.FLAGS).copy();
        if (node.fieldType().equals("char") || node.fieldType().equals("str")) {
            flags.putString(node.key(), (node.op().equals("加") ? state.getString(node.key()) : "") + node.value());
        } else if (node.fieldType().equals("logic")) {
            flags.putBoolean(node.key(), node.value().equals("真"));
        } else {
            double old = node.fieldType().equals("int") ? flags.getInt(node.key()) : flags.getDouble(node.key());
            double operand = number(node.value());
            double result = switch (node.op()) {
                case "加" -> old + operand;
                case "减" -> old - operand;
                case "乘" -> old * operand;
                case "除" -> old / operand;
                default -> operand;
            };
            if (node.fieldType().equals("int")) flags.putInt(node.key(), (int) result);
            else flags.putDouble(node.key(), result);
        }
        player.setData(QuestData.FLAGS, flags);
    }

    static void giveItem(ServerPlayer player, DialogueNodeDefinition.ItemActionNode node) {
        ItemStack stack = EconomyItems.create(node.item(), node.count(), node.displayName(), node.colorCode(), node.payload());
        for (DialogueNodeDefinition.FieldFilter filter : node.filters()) {
            if (filter.key().equals("yikaiqi") || filter.key().equals("geiyvcishu")) {
                BirthItems.setSupplyCount(stack, number(filter.value()));
            }
        }
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }

    private static void takeItem(ServerPlayer player, DialogueNodeDefinition.ItemActionNode node) {
        Item kind = item(node.item());
        int amount = node.count();
        if (node.filters().isEmpty()) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(kind), amount, player.inventoryMenu.getCraftSlots());
            return;
        }
        // 来源带NBT扣除在足量栈分支先把剩余数清零，再按零删除；此分支保持该实际次序。
        PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.is(kind)) continue;
            if (!matchesFilters(stack, node.filters())) continue;
            if (amount < 1) break;
            if (stack.getCount() >= amount) amount = 0;
            else {
                amount -= stack.getCount();
                player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(kind), stack.getCount(), player.inventoryMenu.getCraftSlots());
            }
        }
    }

    static int count(Player player, Item kind) {
        PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(kind)) count += stack.getCount();
        }
        return count;
    }

    static int countAdvancedItems(Player player, DialogueNodeDefinition.CheckAdvancedItemNode node) {
        ItemStack expected = new ItemStack(item(node.item()));
        expected.set(DataComponents.CUSTOM_NAME, Component.literal(node.displayName()));
        PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.is(expected.getItem()) || !stack.getDisplayName().getString().equals(expected.getDisplayName().getString())) continue;
            if (node.filters().isEmpty() || matchesFilters(stack, node.filters())) count += stack.getCount();
        }
        return count;
    }

    /** 读取材料组件等级（Q276）、饰品命令属性组件（Q253）与出生补给计数。 */
    private static boolean matchesFilters(ItemStack stack, List<DialogueNodeDefinition.FieldFilter> filters) {
        EquipmentProperties properties = Equipment.properties(stack);
        MaterialInstance material = Materials.instance(stack);
        for (DialogueNodeDefinition.FieldFilter filter : filters) {
            boolean matched = switch (filter.fieldType()) {
                case "char", "str" -> properties.text(filter.key()).equals(filter.value());
                case "logic" -> properties.fields().containsKey(filter.key())
                        && (properties.number(filter.key()) != 0) == filter.value().equals("真");
                case "int", "double" -> fieldNumber(stack, properties, material, filter.key()) == number(filter.value());
                default -> false;
            };
            if (!matched) return false;
        }
        return true;
    }

    private static double fieldNumber(ItemStack stack, EquipmentProperties properties, MaterialInstance material, String key) {
        if (key.equals("yikaiqi") || key.equals("geiyvcishu")) {
            double count = BirthItems.supplyCount(stack);
            if (count > 0) return count;
        }
        if (key.equals("cailiaolevel") && !material.equals(MaterialInstance.EMPTY)) return material.level();
        if (key.equals("cailiaopinjie") && !material.equals(MaterialInstance.EMPTY)) return material.quality();
        if (properties.fields().containsKey(key)) return properties.number(key);
        return 0;
    }

    static void controlEntities(ServerPlayer player, DialogueNodeDefinition.EntityControlNode node) {
        Vec3 center = new Vec3(node.x(), node.y(), node.z());
        String type = node.entityType().startsWith("yvanchuxitong:")
                ? ModId.VALUE + node.entityType().substring("yvanchuxitong".length())
                : node.entityType();
        List<Entity> entities = player.serverLevel().getEntitiesOfClass(Entity.class,
                        new AABB(center, center).inflate(node.radius() / 2), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        for (Entity entity : entities) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(type)) continue;
            switch (node.op()) {
                case "跟随玩家" -> NpcFollowers.follow(entity, player);
                case "取消跟随" -> NpcFollowers.stopFollowing(entity);
                case "删除实体" -> entity.discard();
                case "移动到坐标" -> {
                    if (entity instanceof Mob mob && node.extraArgs().size() >= 4) {
                        mob.getNavigation().moveTo(
                                number(node.extraArgs().get(0)),
                                number(node.extraArgs().get(1)),
                                number(node.extraArgs().get(2)),
                                number(node.extraArgs().get(3)));
                    }
                }
                case "伤害实体" -> {
                    if (node.extraArgs().size() >= 4) {
                        String kind = node.extraArgs().get(0);
                        double amount = number(node.extraArgs().get(1));
                        boolean self = node.extraArgs().get(2).equals("自己");
                        if (kind.equals("法术") || kind.equals("精神")) {
                            double extra = node.extraArgs().size() >= 5 ? number(node.extraArgs().get(4)) : 0;
                            SpellDamage.markQuestAttack(entity, kind.equals("精神"), amount,
                                    self ? player.getStringUUID() : "wu", node.extraArgs().get(3), extra);
                        } else {
                            entity.hurt(self ? player.damageSources().playerAttack(player) : player.damageSources().generic(),
                                    (float) amount);
                        }
                    }
                }
                default -> { }
            }
        }
    }

    static Item item(ResourceLocation location) {
        if (location.getNamespace().equals("yvanchuxitong")) {
            location = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, location.getPath());
        }
        return BuiltInRegistries.ITEM.get(location);
    }

    static double number(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static void clearScript(CompoundTag state) {
        DialogueActions.clearTemplate(state);
        state.putInt("renwuweizhi", 0);
    }
}
