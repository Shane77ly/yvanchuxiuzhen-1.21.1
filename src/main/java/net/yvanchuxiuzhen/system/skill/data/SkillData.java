package net.yvanchuxiuzhen.system.skill.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;

/**
 * 技能系统的数据登记：书上的技能实例组件 {@code skill}，玩家的技能装配与选择 {@code skill_state}（来源全部克隆事件复制）、
 * 冷却 {@code skill_cooldown}（来源仅非死亡克隆复制）；二者只同步给本人。
 */
public final class SkillData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<DataComponentType<SkillBook>> SKILL = COMPONENTS.registerComponentType("skill",
            builder -> builder.persistent(SkillBook.CODEC).networkSynchronized(SkillBook.STREAM_CODEC.cast()));
    public static final Supplier<AttachmentType<SkillState>> STATE = ATTACHMENTS.register("skill_state", () -> AttachmentType
            .builder(() -> SkillState.EMPTY)
            .serialize(SkillState.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, SkillState.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<SkillCooldowns>> COOLDOWNS = ATTACHMENTS.register("skill_cooldown", () -> AttachmentType
            .builder(() -> SkillCooldowns.EMPTY)
            .serialize(SkillCooldowns.CODEC)
            .sync((holder, player) -> holder == player, SkillCooldowns.STREAM_CODEC)
            .build());

    private SkillData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
