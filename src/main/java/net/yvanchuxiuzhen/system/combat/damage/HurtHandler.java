package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 伤害事件入口。1.18.2 版本先在 {@code LivingAttackEvent} 处理命中（{@link AttackStage}），
 * 再在 {@code LivingHurtEvent} 处理受伤（{@link HurtStage}）；1.21.1 版本这两个事件合并为
 * {@link LivingIncomingDamageEvent}，按同一顺序依次执行。没有攻击者的伤害（摔落、窒息、岩浆等）两个阶段都不处理。
 */
public final class HurtHandler {
    private HurtHandler() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(HurtHandler::onIncoming);
    }

    private static void onIncoming(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (victim.level().isClientSide() || attacker == null) {
            return;
        }
        AttackStage.run(victim, attacker, event.getAmount());
        HurtStage.run(event, victim, attacker);
    }
}
