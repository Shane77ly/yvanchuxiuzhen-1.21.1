package net.yvanchuxiuzhen.system.skill.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.SkillMovement;
import net.yvanchuxiuzhen.system.skill.SkillGuard;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/**
 * 技能按键与界面按钮（客户端 → 服务端，来源各 {@code Anjian*}、{@code Tabanjian}、{@code Jinengqvxiao} 按键消息与
 * {@code JinengGuiButtonMessage}）。动作由服务端按自己的状态执行，客户端不传任何状态。
 */
public record SkillActionPayload(Action action) implements CustomPacketPayload {
    public static final Type<SkillActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> Action.values()[index], Action::ordinal), SkillActionPayload::action, SkillActionPayload::new);

    /** 当前页技能、取消、翻页、连招、十一项箭种选择与身法轻功；新动作在末尾追加。 */
    public enum Action {
        SELECT_1, SELECT_2, SELECT_3, SELECT_4, CANCEL, NEXT_PAGE, PAGE_UP, PAGE_DOWN, TOGGLE_COMBO,
        ARROW_1, ARROW_2, ARROW_3, ARROW_4, ARROW_5, ARROW_6, ARROW_7, ARROW_8, ARROW_9, ARROW_10, ARROW_11,
        MOVE_W, MOVE_S, MOVE_A, MOVE_D, JUMP, TOGGLE_FLIGHT, OPEN_MENU, GUARD_PRESS, GUARD_RELEASE
    }

    @Override
    public Type<SkillActionPayload> type() {
        return TYPE;
    }

    public static void handle(SkillActionPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        switch (payload.action()) {
            case SELECT_1 -> SkillSelection.select(player, 1);
            case SELECT_2 -> SkillSelection.select(player, 2);
            case SELECT_3 -> SkillSelection.select(player, 3);
            case SELECT_4 -> SkillSelection.select(player, 4);
            case CANCEL -> SkillSelection.clear(player);
            case NEXT_PAGE -> SkillSelection.nextPage(player);
            case PAGE_UP -> SkillSelection.turnPage(player, 1);
            case PAGE_DOWN -> SkillSelection.turnPage(player, -1);
            case TOGGLE_COMBO -> SkillSelection.toggleCombo(player);
            case ARROW_1, ARROW_2, ARROW_3, ARROW_4, ARROW_5, ARROW_6, ARROW_7, ARROW_8, ARROW_9, ARROW_10, ARROW_11 ->
                    Skills.selectArrow(player, payload.action().ordinal() - Action.ARROW_1.ordinal() + 1);
            case MOVE_W -> SkillMovement.press(player, SkillMovement.Input.W);
            case MOVE_S -> SkillMovement.press(player, SkillMovement.Input.S);
            case MOVE_A -> SkillMovement.press(player, SkillMovement.Input.A);
            case MOVE_D -> SkillMovement.press(player, SkillMovement.Input.D);
            case JUMP -> SkillMovement.press(player, SkillMovement.Input.JUMP);
            case TOGGLE_FLIGHT -> Skills.toggleFlight(player);
            case OPEN_MENU -> Skills.openMenu(player);
            case GUARD_PRESS -> SkillGuard.press(player);
            case GUARD_RELEASE -> SkillGuard.release(player);
        }
    }
}
