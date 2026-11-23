package net.yvanchuxiuzhen.system.combat.damage.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.yvanchuxiuzhen.system.combat.damage.HurtStage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 两种独立实现均在易伤检查通过后、护甲表达式第一次读取当前伤害前结算。 */
@Mixin({LivingEntity.class, Player.class})
public abstract class LivingEntityDamageStageMixin {
    @Inject(method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;getNewDamage()F",
                    ordinal = 0, remap = false), cancellable = true, require = 1, allow = 1)
    private void yvanchuxiuzhen$runHurtStageAfterShield(DamageSource source, float amount, CallbackInfo callback) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (victim.level().isClientSide()) return;
        DamageContainer damage = ((LivingEntityDamageAccess) this).yvanchuxiuzhen$damageContainers().peek();
        if (HurtStage.runAfterShield(victim, damage)) {
            damage.setNewDamage(0);
            callback.cancel();
        }
    }
}
