package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;

/**
 * 强类型对话与任务节点规约，按 {@code kind} 字段多态反序列化，替代旧版 {@code %}/{@code /} 脚本行。
 */
public sealed interface DialogueNodeDefinition permits
        DialogueNodeDefinition.TextNode,
        DialogueNodeDefinition.ChoiceNode,
        DialogueNodeDefinition.CheckItemNode,
        DialogueNodeDefinition.CheckAdvancedItemNode,
        DialogueNodeDefinition.CheckQuestNode,
        DialogueNodeDefinition.CheckPosNode,
        DialogueNodeDefinition.CheckDimensionNode,
        DialogueNodeDefinition.CheckFlagNode,
        DialogueNodeDefinition.ModifyFlagNode,
        DialogueNodeDefinition.ItemActionNode,
        DialogueNodeDefinition.JumpNode,
        DialogueNodeDefinition.SetQuestStatusNode,
        DialogueNodeDefinition.SaveQuestNode,
        DialogueNodeDefinition.ModifyQuestHeaderNode,
        DialogueNodeDefinition.OpenMenuNode,
        DialogueNodeDefinition.TeleportNode,
        DialogueNodeDefinition.RemoveBlocksNode,
        DialogueNodeDefinition.UnsealRiftNode,
        DialogueNodeDefinition.EntityControlNode,
        DialogueNodeDefinition.EffectNode,
        DialogueNodeDefinition.CommandNode,
        DialogueNodeDefinition.TransferQuestNode,
        DialogueNodeDefinition.InsertChatOptionNode,
        DialogueNodeDefinition.EndNode {

    Codec<DialogueNodeDefinition> CODEC = Codec.STRING.dispatch("kind", DialogueNodeDefinition::kind, kind -> switch (kind) {
        case "text" -> TextNode.MAP_CODEC;
        case "choice" -> ChoiceNode.MAP_CODEC;
        case "check_item" -> CheckItemNode.MAP_CODEC;
        case "check_advanced_item" -> CheckAdvancedItemNode.MAP_CODEC;
        case "check_quest" -> CheckQuestNode.MAP_CODEC;
        case "check_pos" -> CheckPosNode.MAP_CODEC;
        case "check_dimension" -> CheckDimensionNode.MAP_CODEC;
        case "check_flag" -> CheckFlagNode.MAP_CODEC;
        case "modify_flag" -> ModifyFlagNode.MAP_CODEC;
        case "item_action" -> ItemActionNode.MAP_CODEC;
        case "jump" -> JumpNode.MAP_CODEC;
        case "set_quest_status" -> SetQuestStatusNode.MAP_CODEC;
        case "save_quest" -> SaveQuestNode.MAP_CODEC;
        case "modify_quest_header" -> ModifyQuestHeaderNode.MAP_CODEC;
        case "open_menu" -> OpenMenuNode.MAP_CODEC;
        case "teleport" -> TeleportNode.MAP_CODEC;
        case "remove_blocks" -> RemoveBlocksNode.MAP_CODEC;
        case "unseal_rift" -> UnsealRiftNode.MAP_CODEC;
        case "entity_control" -> EntityControlNode.MAP_CODEC;
        case "effect" -> EffectNode.MAP_CODEC;
        case "command" -> CommandNode.MAP_CODEC;
        case "transfer_quest" -> TransferQuestNode.MAP_CODEC;
        case "insert_chat_option" -> InsertChatOptionNode.MAP_CODEC;
        case "end" -> EndNode.MAP_CODEC;
        default -> throw new IllegalArgumentException("未知对话节点类型：" + kind);
    });

    String kind();

    /** 字段过滤项，供高级物品匹配与带条件扣物共用。 */
    record FieldFilter(String key, String fieldType, String value) {
        public static final Codec<FieldFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter(FieldFilter::key),
                Codec.STRING.fieldOf("field_type").forGetter(FieldFilter::fieldType),
                Codec.STRING.fieldOf("value").forGetter(FieldFilter::value)
        ).apply(instance, FieldFilter::new));
    }

    /** 选项分支。 */
    record ChoiceOption(String label, int target) {
        public static final Codec<ChoiceOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("label").forGetter(ChoiceOption::label),
                Codec.INT.fieldOf("target").forGetter(ChoiceOption::target)
        ).apply(instance, ChoiceOption::new));
    }

    /** 旁白或玩家自发台词（{@code playerSpeak == true} 对应“自发文字”）。 */
    record TextNode(boolean playerSpeak, String text) implements DialogueNodeDefinition {
        public static final MapCodec<TextNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("player_speak", false).forGetter(TextNode::playerSpeak),
                Codec.STRING.fieldOf("text").forGetter(TextNode::text)
        ).apply(instance, TextNode::new));

        @Override
        public String kind() {
            return "text";
        }
    }

    /** 最多四项的交互选项节点。 */
    record ChoiceNode(String prompt, List<ChoiceOption> options) implements DialogueNodeDefinition {
        public static final MapCodec<ChoiceNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("prompt").forGetter(ChoiceNode::prompt),
                ChoiceOption.CODEC.listOf().fieldOf("options").forGetter(ChoiceNode::options)
        ).apply(instance, ChoiceNode::new));

        public ChoiceNode {
            options = List.copyOf(options);
        }

        @Override
        public String kind() {
            return "choice";
        }
    }

    /** 基础物品数量条件跳转。 */
    record CheckItemNode(ResourceLocation item, int count, int passTarget, int failTarget) implements DialogueNodeDefinition {
        public static final MapCodec<CheckItemNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(CheckItemNode::item),
                Codec.INT.fieldOf("count").forGetter(CheckItemNode::count),
                Codec.INT.fieldOf("pass_target").forGetter(CheckItemNode::passTarget),
                Codec.INT.fieldOf("fail_target").forGetter(CheckItemNode::failTarget)
        ).apply(instance, CheckItemNode::new));

        @Override
        public String kind() {
            return "check_item";
        }
    }

    /** 按显示名与属性过滤器检查物品数量并分支跳转。 */
    record CheckAdvancedItemNode(ResourceLocation item,
                                 int count,
                                 String displayName,
                                 List<FieldFilter> filters,
                                 int passTarget,
                                 int failTarget) implements DialogueNodeDefinition {
        public static final MapCodec<CheckAdvancedItemNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(CheckAdvancedItemNode::item),
                Codec.INT.fieldOf("count").forGetter(CheckAdvancedItemNode::count),
                Codec.STRING.fieldOf("display_name").forGetter(CheckAdvancedItemNode::displayName),
                FieldFilter.CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(CheckAdvancedItemNode::filters),
                Codec.INT.fieldOf("pass_target").forGetter(CheckAdvancedItemNode::passTarget),
                Codec.INT.fieldOf("fail_target").forGetter(CheckAdvancedItemNode::failTarget)
        ).apply(instance, CheckAdvancedItemNode::new));

        public CheckAdvancedItemNode {
            filters = List.copyOf(filters);
        }

        @Override
        public String kind() {
            return "check_advanced_item";
        }
    }

    /** 任务状态分支（完成 / 失败 / 其他）。 */
    record CheckQuestNode(String questId,
                          int completedTarget,
                          int failedTarget,
                          int otherwiseTarget) implements DialogueNodeDefinition {
        public static final MapCodec<CheckQuestNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("quest_id").forGetter(CheckQuestNode::questId),
                Codec.INT.fieldOf("completed_target").forGetter(CheckQuestNode::completedTarget),
                Codec.INT.fieldOf("failed_target").forGetter(CheckQuestNode::failedTarget),
                Codec.INT.fieldOf("otherwise_target").forGetter(CheckQuestNode::otherwiseTarget)
        ).apply(instance, CheckQuestNode::new));

        @Override
        public String kind() {
            return "check_quest";
        }
    }

    /** 玩家坐标盒范围分支判断。 */
    record CheckPosNode(double x, double y, double z, double size, int insideTarget, int outsideTarget)
            implements DialogueNodeDefinition {
        public static final MapCodec<CheckPosNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(CheckPosNode::x),
                Codec.DOUBLE.fieldOf("y").forGetter(CheckPosNode::y),
                Codec.DOUBLE.fieldOf("z").forGetter(CheckPosNode::z),
                Codec.DOUBLE.fieldOf("size").forGetter(CheckPosNode::size),
                Codec.INT.fieldOf("inside_target").forGetter(CheckPosNode::insideTarget),
                Codec.INT.fieldOf("outside_target").forGetter(CheckPosNode::outsideTarget)
        ).apply(instance, CheckPosNode::new));

        @Override
        public String kind() {
            return "check_pos";
        }
    }

    /** 维度条件分支判断。 */
    record CheckDimensionNode(String dimension, int matchTarget, int mismatchTarget) implements DialogueNodeDefinition {
        public static final MapCodec<CheckDimensionNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("dimension").forGetter(CheckDimensionNode::dimension),
                Codec.INT.fieldOf("match_target").forGetter(CheckDimensionNode::matchTarget),
                Codec.INT.fieldOf("mismatch_target").forGetter(CheckDimensionNode::mismatchTarget)
        ).apply(instance, CheckDimensionNode::new));

        @Override
        public String kind() {
            return "check_dimension";
        }
    }

    /** 玩家任务旗标对比或字面量对比分支（对应“判断nbt”与“判断数值”）。 */
    record CheckFlagNode(boolean literal,
                         String fieldType,
                         String key,
                         String targetOrLiteral,
                         int firstTarget,
                         int secondTarget,
                         int equalTarget) implements DialogueNodeDefinition {
        public static final MapCodec<CheckFlagNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("literal").forGetter(CheckFlagNode::literal),
                Codec.STRING.fieldOf("field_type").forGetter(CheckFlagNode::fieldType),
                Codec.STRING.fieldOf("key").forGetter(CheckFlagNode::key),
                Codec.STRING.fieldOf("target_or_literal").forGetter(CheckFlagNode::targetOrLiteral),
                Codec.INT.fieldOf("first_target").forGetter(CheckFlagNode::firstTarget),
                Codec.INT.fieldOf("second_target").forGetter(CheckFlagNode::secondTarget),
                Codec.INT.optionalFieldOf("equal_target", 0).forGetter(CheckFlagNode::equalTarget)
        ).apply(instance, CheckFlagNode::new));

        @Override
        public String kind() {
            return "check_flag";
        }
    }

    /** 修改玩家任务持久化旗标。 */
    record ModifyFlagNode(String fieldType, String key, String op, String value) implements DialogueNodeDefinition {
        public static final MapCodec<ModifyFlagNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("field_type").forGetter(ModifyFlagNode::fieldType),
                Codec.STRING.fieldOf("key").forGetter(ModifyFlagNode::key),
                Codec.STRING.fieldOf("op").forGetter(ModifyFlagNode::op),
                Codec.STRING.fieldOf("value").forGetter(ModifyFlagNode::value)
        ).apply(instance, ModifyFlagNode::new));

        @Override
        public String kind() {
            return "modify_flag";
        }
    }

    /** 给予或扣除玩家物品（支持强类型载荷或过滤条件）。 */
    record ItemActionNode(boolean give,
                          ResourceLocation item,
                          int count,
                          Optional<String> displayName,
                          Optional<String> colorCode,
                          ShopCatalogDefinition.ItemPayload payload,
                          List<FieldFilter> filters) implements DialogueNodeDefinition {
        public static final MapCodec<ItemActionNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("give").forGetter(ItemActionNode::give),
                ResourceLocation.CODEC.fieldOf("item").forGetter(ItemActionNode::item),
                Codec.INT.fieldOf("count").forGetter(ItemActionNode::count),
                Codec.STRING.optionalFieldOf("display_name").forGetter(ItemActionNode::displayName),
                Codec.STRING.optionalFieldOf("color_code").forGetter(ItemActionNode::colorCode),
                ShopCatalogDefinition.ItemPayload.CODEC.optionalFieldOf("payload", ShopCatalogDefinition.NoPayload.INSTANCE)
                        .forGetter(ItemActionNode::payload),
                FieldFilter.CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(ItemActionNode::filters)
        ).apply(instance, ItemActionNode::new));

        public ItemActionNode {
            payload = payload == null ? ShopCatalogDefinition.NoPayload.INSTANCE : payload;
            filters = List.copyOf(filters);
        }

        @Override
        public String kind() {
            return "item_action";
        }
    }

    /** 无条件跳转到指定 1-based 步骤游标。 */
    record JumpNode(int target) implements DialogueNodeDefinition {
        public static final MapCodec<JumpNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("target").forGetter(JumpNode::target)
        ).apply(instance, JumpNode::new));

        @Override
        public String kind() {
            return "jump";
        }
    }

    /** 更新当前会话的任务完成状态文本。 */
    record SetQuestStatusNode(String status) implements DialogueNodeDefinition {
        public static final MapCodec<SetQuestStatusNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("status").forGetter(SetQuestStatusNode::status)
        ).apply(instance, SetQuestStatusNode::new));

        @Override
        public String kind() {
            return "set_quest_status";
        }
    }

    /** 将当前任务状态与断点游标存入玩家任务目录。 */
    record SaveQuestNode(int resumeCursor) implements DialogueNodeDefinition {
        public static final MapCodec<SaveQuestNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("resume_cursor").forGetter(SaveQuestNode::resumeCursor)
        ).apply(instance, SaveQuestNode::new));

        @Override
        public String kind() {
            return "save_quest";
        }
    }

    /** 运行时覆写当前任务的名称或说明。 */
    record ModifyQuestHeaderNode(String field, String value) implements DialogueNodeDefinition {
        public static final MapCodec<ModifyQuestHeaderNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("field").forGetter(ModifyQuestHeaderNode::field),
                Codec.STRING.fieldOf("value").forGetter(ModifyQuestHeaderNode::value)
        ).apply(instance, ModifyQuestHeaderNode::new));

        @Override
        public String kind() {
            return "modify_quest_header";
        }
    }

    /** 结束对话并打开经济系统指定菜单。 */
    record OpenMenuNode(String menu) implements DialogueNodeDefinition {
        public static final MapCodec<OpenMenuNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("menu").forGetter(OpenMenuNode::menu)
        ).apply(instance, OpenMenuNode::new));

        @Override
        public String kind() {
            return "open_menu";
        }
    }

    /** 传送玩家到目标坐标。 */
    record TeleportNode(double x, double y, double z) implements DialogueNodeDefinition {
        public static final MapCodec<TeleportNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(TeleportNode::x),
                Codec.DOUBLE.fieldOf("y").forGetter(TeleportNode::y),
                Codec.DOUBLE.fieldOf("z").forGetter(TeleportNode::z)
        ).apply(instance, TeleportNode::new));

        @Override
        public String kind() {
            return "teleport";
        }
    }

    /** 批量清空目标方块坐标。 */
    record RemoveBlocksNode(List<BlockPos> positions) implements DialogueNodeDefinition {
        public static final MapCodec<RemoveBlocksNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockPos.CODEC.listOf().fieldOf("positions").forGetter(RemoveBlocksNode::positions)
        ).apply(instance, RemoveBlocksNode::new));

        public RemoveBlocksNode {
            positions = List.copyOf(positions);
        }

        @Override
        public String kind() {
            return "remove_blocks";
        }
    }

    /** 解封附近空间裂隙。 */
    record UnsealRiftNode() implements DialogueNodeDefinition {
        public static final UnsealRiftNode INSTANCE = new UnsealRiftNode();
        public static final MapCodec<UnsealRiftNode> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public String kind() {
            return "unseal_rift";
        }
    }

    /** 对范围内指定类型实体执行跟随、移除、移动或伤害控制。 */
    record EntityControlNode(String op,
                             String entityType,
                             double x,
                             double y,
                             double z,
                             double radius,
                             List<String> extraArgs) implements DialogueNodeDefinition {
        public static final MapCodec<EntityControlNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("op").forGetter(EntityControlNode::op),
                Codec.STRING.fieldOf("entity_type").forGetter(EntityControlNode::entityType),
                Codec.DOUBLE.fieldOf("x").forGetter(EntityControlNode::x),
                Codec.DOUBLE.fieldOf("y").forGetter(EntityControlNode::y),
                Codec.DOUBLE.fieldOf("z").forGetter(EntityControlNode::z),
                Codec.DOUBLE.fieldOf("radius").forGetter(EntityControlNode::radius),
                Codec.STRING.listOf().optionalFieldOf("extra_args", List.of()).forGetter(EntityControlNode::extraArgs)
        ).apply(instance, EntityControlNode::new));

        public EntityControlNode {
            extraArgs = List.copyOf(extraArgs);
        }

        @Override
        public String kind() {
            return "entity_control";
        }
    }

    /** 给予或清除药水效果（暂通过既有执行器转发，阶段 3 统一直调 API）。 */
    record EffectNode(String op, String effectId, int amplifier, int duration) implements DialogueNodeDefinition {
        public static final MapCodec<EffectNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("op").forGetter(EffectNode::op),
                Codec.STRING.fieldOf("effect_id").forGetter(EffectNode::effectId),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectNode::amplifier),
                Codec.INT.optionalFieldOf("duration", 0).forGetter(EffectNode::duration)
        ).apply(instance, EffectNode::new));

        @Override
        public String kind() {
            return "effect";
        }
    }

    /** 保留至阶段 3 重构的对话指令节点。 */
    record CommandNode(String command) implements DialogueNodeDefinition {
        public static final MapCodec<CommandNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("command").forGetter(CommandNode::command)
        ).apply(instance, CommandNode::new));

        @Override
        public String kind() {
            return "command";
        }
    }

    /** 切换到另一条预设对话或任务模板。 */
    record TransferQuestNode(String targetDialogueId) implements DialogueNodeDefinition {
        public static final MapCodec<TransferQuestNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("target_dialogue_id").forGetter(TransferQuestNode::targetDialogueId)
        ).apply(instance, TransferQuestNode::new));

        @Override
        public String kind() {
            return "transfer_quest";
        }
    }

    /** 连续对话中向聊天栏插入可点击续接项。 */
    record InsertChatOptionNode(String prefix, String option, String suffix, int target) implements DialogueNodeDefinition {
        public static final MapCodec<InsertChatOptionNode> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("prefix").forGetter(InsertChatOptionNode::prefix),
                Codec.STRING.fieldOf("option").forGetter(InsertChatOptionNode::option),
                Codec.STRING.fieldOf("suffix").forGetter(InsertChatOptionNode::suffix),
                Codec.INT.fieldOf("target").forGetter(InsertChatOptionNode::target)
        ).apply(instance, InsertChatOptionNode::new));

        @Override
        public String kind() {
            return "insert_chat_option";
        }
    }

    /** 结束当前对话或连续任务。 */
    record EndNode() implements DialogueNodeDefinition {
        public static final EndNode INSTANCE = new EndNode();
        public static final MapCodec<EndNode> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public String kind() {
            return "end";
        }
    }
}
