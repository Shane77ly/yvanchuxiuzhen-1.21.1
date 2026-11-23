package net.yvanchuxiuzhen.system.combat.damage.mixin;

import java.util.Stack;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityDamageAccess {
    @Accessor(value = "damageContainers", remap = false)
    Stack<DamageContainer> yvanchuxiuzhen$damageContainers();
}
