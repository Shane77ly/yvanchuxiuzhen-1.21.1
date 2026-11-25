package net.yvanchuxiuzhen.system.combat.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.PendingSpellAttack;

/** 战斗系统的 NeoForge 实体附件注册表。 */
public final class CombatData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<AttachmentType<CombatEntityState>> ENTITY_STATE = ATTACHMENTS.register("combat_entity_state",
            () -> AttachmentType.builder(() -> CombatEntityState.EMPTY).serialize(CombatEntityState.CODEC).build());

    public static final Supplier<AttachmentType<PendingSpellAttack>> PENDING_ATTACK = ATTACHMENTS.register("pending_spell_attack",
            () -> AttachmentType.builder(() -> PendingSpellAttack.EMPTY).serialize(PendingSpellAttack.CODEC).build());

    private CombatData() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
