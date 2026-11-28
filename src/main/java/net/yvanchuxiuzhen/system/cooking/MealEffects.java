package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.MealPlateRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingTableRecipes;

/** 餐食标签的互斥判定、三类用餐标记与摆盘食物的饱食和容器结算。 */
public final class MealEffects {
    private MealEffects() {
    }

    /** 在原生食用已结算后读取原栈；吃空的栈按原生 AIR 标签身份处理。 */
    public static void afterEating(Level level, LivingEntity entity, ItemStack stack) {
        if (level.isClientSide) return;
        apply(entity, categoryRule(stack));
    }

    /** 盘菜不检查饥饿值；先补饱食、结算餐食标记，再按当前方块标签换回默认容器。 */
    public static void usePlate(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        MealPlateRule itemRule = categoryRule(new ItemStack(level.getBlockState(pos).getBlock()));
        FoodData food = player.getFoodData();
        if (itemRule != null) {
            food.setFoodLevel(Math.min(20, food.getFoodLevel() + itemRule.nutrition()));
            food.setSaturation(Math.min(20, food.getSaturationLevel() + itemRule.saturation()));
            apply(player, itemRule);
        }
        BlockState current = level.getBlockState(pos);
        for (MealPlateRule rule : CookingTableRecipes.mealPlates()) {
            if (current.is(BlockTags.create(ResourceLocation.parse(rule.blockTag())))) {
                level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse(rule.containerBlock())).defaultBlockState(),
                        Block.UPDATE_ALL);
                break;
            }
        }
    }

    private static MealPlateRule categoryRule(ItemStack stack) {
        for (MealPlateRule rule : CookingTableRecipes.mealPlates()) {
            if (stack.is(ItemTags.create(ResourceLocation.parse(rule.itemTag())))) return rule;
        }
        return null;
    }

    private static void apply(LivingEntity entity, MealPlateRule rule) {
        if (rule == null) return;
        if (rule.markerEffect().isPresent()) {
            entity.addEffect(new MobEffectInstance(marker(rule.markerEffect().get()), 1200, 0, true, false));
            return;
        }
        int count = 0;
        for (MealPlateRule entry : CookingTableRecipes.mealPlates()) {
            if (entry.markerEffect().isPresent() && entity.hasEffect(marker(entry.markerEffect().get()))) count++;
        }
        if (count == 1) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 0));
        else if (count == 2) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1));
        else if (count >= 3) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 2));
        if (count > 0) {
            for (MealPlateRule entry : CookingTableRecipes.mealPlates()) {
                entry.markerEffect().ifPresent(id -> entity.removeEffect(marker(id)));
            }
        }
    }

    private static Holder<MobEffect> marker(String effectId) {
        return BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.parse(effectId)));
    }
}
