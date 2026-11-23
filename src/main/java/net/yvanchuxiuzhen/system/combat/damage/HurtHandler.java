package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** 盾挡前按原始入伤处理命中；盾挡及无敌帧之后的受伤处理见 {@link HurtStage}。 */
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
        AttackStage.run(victim, attacker, event.getOriginalAmount());
    }
}
