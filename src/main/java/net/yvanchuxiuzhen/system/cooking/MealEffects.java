package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;

/** 餐食标签的互斥判定、三类用餐标记与摆盘食物的饱食和容器结算。 */
public final class MealEffects {
    private static final TagKey<Item> STAPLE = ItemTags.create(ResourceLocation.parse("yvanchuxiuzhen:zhushi"));
    private static final TagKey<Item> DISH = ItemTags.create(ResourceLocation.parse("yvanchuxiuzhen:caipin"));
    private static final TagKey<Item> SOUP = ItemTags.create(ResourceLocation.parse("yvanchuxiuzhen:tangshui"));
    private static final TagKey<Item> TEA = ItemTags.create(ResourceLocation.parse("yvanchuxiuzhen:chadian"));
    private static final TagKey<Block> PLATE = BlockTags.create(ResourceLocation.parse("yvanchuxiuzhen:panzhuang"));
    private static final TagKey<Block> BOWL = BlockTags.create(ResourceLocation.parse("yvanchuxiuzhen:wanzhuang"));
    private static final TagKey<Block> LARGE_BOWL = BlockTags.create(ResourceLocation.parse("yvanchuxiuzhen:dapenzhuang"));
    private static final TagKey<Block> IRON_PLATE = BlockTags.create(ResourceLocation.parse("yvanchuxiuzhen:tiepanzhuang"));

    private MealEffects() {
    }

    /** 在原生食用已结算后读取原栈；吃空的栈按原生 AIR 标签身份处理。 */
    public static void afterEating(Level level, LivingEntity entity, ItemStack stack) {
        if (level.isClientSide) return;
        apply(entity, category(stack));
    }

    /** 盘菜不检查饥饿值；先补饱食、结算餐食标记，再按当前方块标签换回默认容器。 */
    public static void usePlate(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        int category = category(new ItemStack(level.getBlockState(pos).getBlock()));
        FoodData food = player.getFoodData();
        switch (category) {
            case 1 -> addFood(food, 12, 2);
            case 2 -> addFood(food, 2, 12);
            case 3 -> addFood(food, 8, 4);
            case 4 -> addFood(food, 2, 2);
            default -> { }
        }
        if (food.getFoodLevel() > 20) food.setFoodLevel(20);
        if (food.getSaturationLevel() > 20) food.setSaturation(20);
        apply(player, category);
        BlockState current = level.getBlockState(pos);
        if (current.is(PLATE)) replace(level, pos, "foodcipan");
        else if (current.is(BOWL)) replace(level, pos, "foodwan");
        else if (current.is(LARGE_BOWL)) replace(level, pos, "fooddawan");
        else if (current.is(IRON_PLATE)) replace(level, pos, "foodtiepan");
    }

    private static int category(ItemStack stack) {
        if (stack.is(STAPLE)) return 1;
        if (stack.is(DISH)) return 2;
        if (stack.is(SOUP)) return 3;
        if (stack.is(TEA)) return 4;
        return 0;
    }

    private static void addFood(FoodData food, int nutrition, int saturation) {
        food.setFoodLevel(food.getFoodLevel() + nutrition);
        food.setSaturation(food.getSaturationLevel() + saturation);
    }

    private static void apply(LivingEntity entity, int category) {
        if (category >= 1 && category <= 3) {
            entity.addEffect(new MobEffectInstance(marker(category), 1200, 0, true, false));
        } else if (category == 4) {
            int count = 0;
            for (int index = 1; index <= 3; index++) if (entity.hasEffect(marker(index))) count++;
            if (count == 1) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 0));
            else if (count == 2) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1));
            else if (count >= 3) entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 2));
            if (count > 0) for (int index = 1; index <= 3; index++) entity.removeEffect(marker(index));
        }
    }

    private static Holder<MobEffect> marker(int category) {
        return BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_0" + (27 + category))));
    }

    private static void replace(Level level, BlockPos pos, String path) {
        level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)).defaultBlockState(), Block.UPDATE_ALL);
    }
}
