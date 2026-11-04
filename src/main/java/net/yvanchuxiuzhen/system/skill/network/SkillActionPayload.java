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

/**
 * 技能按键与界面按钮（客户端 → 服务端，来源各 {@code Anjian*}、{@code Tabanjian}、{@code Jinengqvxiao} 按键消息与
 * {@code JinengGuiButtonMessage}）。动作由服务端按自己的状态执行，客户端不传任何状态。
 */
public record SkillActionPayload(Action action) implements CustomPacketPayload {
    public static final Type<SkillActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> Action.values()[index], Action::ordinal), SkillActionPayload::action, SkillActionPayload::new);

    /** 选择当前页第 1–4 个键的技能（技能界面内的 Z、X、C、V）、取消选择（B）、下一页（Tab）、上一页与下一页按钮、连招开关。 */
    public enum Action {
        SELECT_1, SELECT_2, SELECT_3, SELECT_4, CANCEL, NEXT_PAGE, PAGE_UP, PAGE_DOWN, TOGGLE_COMBO
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
        }
    }
}
