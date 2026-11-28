package net.yvanchuxiuzhen.system.cooking.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.yvanchuxiuzhen.platform.ModId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** 仅对两种小肉按营养值等量治疗；食物消费与繁殖仍由狼的原版交互处理。 */
@Mixin(Wolf.class)
public abstract class WolfFeedingMixin {
    @Unique
    private static final ResourceLocation yvanchuxiuzhen$raw = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_060");
    @Unique
    private static final ResourceLocation yvanchuxiuzhen$cooked = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "food_084");

    @ModifyConstant(method = "mobInteract", constant = @Constant(floatValue = 2.0F), require = 1, allow = 1)
    private float yvanchuxiuzhen$foodHealingMultiplier(float vanilla, @Local Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return yvanchuxiuzhen$raw.equals(id) || yvanchuxiuzhen$cooked.equals(id) ? 1.0F : vanilla;
    }
}
