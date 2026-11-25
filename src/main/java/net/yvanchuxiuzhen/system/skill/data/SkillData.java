package net.yvanchuxiuzhen.system.skill.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;

/**
 * 技能系统的数据登记：书上的技能实例组件 {@code skill}，玩家的技能装配与选择 {@code skill_state}（来源全部克隆事件复制）、
 * 冷却 {@code skill_cooldown}（来源仅非死亡克隆复制）、飞行开关 {@code skill_flying}（来源全部克隆事件复制）；只同步给本人。
 * 技能状态克隆时沿用各物品引用，存档仍分别序列化各槽与当前选择。
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
            .copyHandler((state, holder, provider) -> state)
            .sync((holder, player) -> holder == player, SkillState.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<SkillCooldowns>> COOLDOWNS = ATTACHMENTS.register("skill_cooldown", () -> AttachmentType
            .builder(() -> SkillCooldowns.EMPTY)
            .serialize(SkillCooldowns.CODEC)
            .sync((holder, player) -> holder == player, SkillCooldowns.STREAM_CODEC)
            .build());
    /** 来源 flybegin：持久化并在全部克隆事件复制，只同步给本人。 */
    public static final Supplier<AttachmentType<Boolean>> FLYING = ATTACHMENTS.register("skill_flying", () -> AttachmentType
            .builder(() -> false)
            .serialize(Codec.BOOL)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.BOOL)
            .build());
    /** 格挡剩余时间（序列化且同步给本人）。 */
    public static final Supplier<AttachmentType<Double>> GUARD_REMAINING = ATTACHMENTS.register("skill_guard", () -> AttachmentType
            .builder(() -> 0.0)
            .serialize(Codec.DOUBLE)
            .sync((holder, player) -> holder == player, ByteBufCodecs.DOUBLE)
            .build());
    /** 格挡冷却计数。 */
    public static final Supplier<AttachmentType<Double>> GUARD_COOLDOWN = ATTACHMENTS.register("skill_guard_cooldown", () -> AttachmentType
            .builder(() -> 0.0)
            .serialize(Codec.DOUBLE)
            .build());
    /** 身法双击窗口与共享冷却。 */
    public static final Supplier<AttachmentType<SkillMovementState>> MOVEMENT = ATTACHMENTS.register("skill_movement", () -> AttachmentType
            .builder(() -> SkillMovementState.EMPTY)
            .serialize(SkillMovementState.CODEC)
            .build());
    /** 武器技连招式数、窗口与冷却。 */
    public static final Supplier<AttachmentType<WeaponArtState>> WEAPON_ART = ATTACHMENTS.register("skill_weapon_art", () -> AttachmentType
            .builder(() -> WeaponArtState.EMPTY)
            .serialize(WeaponArtState.CODEC)
            .build());

    private SkillData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
