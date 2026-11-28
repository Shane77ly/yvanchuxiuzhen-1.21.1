package net.yvanchuxiuzhen.system.treasure;

import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipeReward;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.CommandReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.FuluReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.IntRange;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.ItemReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.MaterialReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.MedicineRewardSpec;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.RecipeRewardSpec;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.Reward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.SkillReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.TechniqueReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.TierContext;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.WeightedQuality;
import net.yvanchuxiuzhen.system.treasure.TreasureRewardTables.Selection;

/** 宝箱总表的强类型奖励构造与掉落发放；等级属于本次开箱，不写玩家档案。 */
public final class TreasureRewards {
    private TreasureRewards() {
    }

    /** 先按容器配置范围抽取发放次数，再逐次由 {@link TreasureRewardTables} 抽样并在容器位置附近掉落。 */
    public static void open(ServerLevel level, Vec3 at, String category, String subtype, double grade, int minimum, int maximum) {
        int count = Mth.nextInt(level.random, minimum, maximum);
        for (int index = 0; index < count; index++) {
            Optional<List<Selection>> selected = TreasureRewardTables.select(category, subtype, grade, level.random);
            if (selected.isEmpty()) {
                return;
            }
            for (Selection drop : selected.get()) {
                if (drop.reward() instanceof CommandReward command) {
                    CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, at.add(0.5, 0.5, 0.5), Vec2.ZERO,
                            level, 4, "", Component.empty(), level.getServer(), null).withSuppressedOutput();
                    level.getServer().getCommands().performPrefixedCommand(source, command.command());
                } else {
                    ItemStack product = create(drop.reward(), drop.context(), level.random);
                    ItemEntity entity = new ItemEntity(level, at.x + 0.5, at.y + 1, at.z + 0.5, product);
                    entity.setPickUpDelay(10);
                    level.addFreshEntity(entity);
                }
            }
        }
    }

    /** 根据强类型奖励规约与等级分段上下文构造对应系统的成品物品栈。 */
    static ItemStack create(Reward reward, TierContext context, RandomSource random) {
        return switch (reward) {
            case ItemReward item -> {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(item.item()));
                item.displayName().ifPresent(name -> stack.set(DataComponents.CUSTOM_NAME, Component.literal(name)));
                yield applyCount(stack, item.count(), random);
            }
            case MaterialReward material -> {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(material.item()));
                Optional<IntRange> levelRange = material.materialLevel().or(context::materialLevel);
                Optional<List<WeightedQuality>> qualities = material.materialQualities().or(context::materialQualities);
                if (levelRange.isPresent() || qualities.isPresent()) {
                    int materialLevel = levelRange.map(range -> range.roll(random)).orElse(0);
                    int quality = qualities.map(list -> WeightedQuality.pick(list, random)).orElse(0);
                    Materials.setInstance(stack, materialLevel, quality);
                }
                yield applyCount(stack, material.count(), random);
            }
            case FuluReward fulu -> {
                int fuluLevel = fulu.fuluLevel().or(context::fuluLevel).orElse(0);
                String tierText = fulu.fuluTierText().or(context::fuluTierText).orElse("");
                ItemStack stack = Fulu.createReward(BuiltInRegistries.ITEM.get(fulu.item()), fuluLevel, tierText);
                yield applyCount(stack, fulu.count(), random);
            }
            case MedicineRewardSpec medicine -> {
                int doses = medicine.packCount().or(context::medicinePackCount).map(range -> range.roll(random)).orElse(0);
                ItemStack stack = Liandan.createMedicineReward(BuiltInRegistries.ITEM.get(medicine.item()),
                        new MedicineReward(medicine.name(), medicine.dose(), doses, medicine.treasure(), medicine.effects()));
                yield applyCount(stack, medicine.count(), random);
            }
            case RecipeRewardSpec recipe -> {
                ItemStack stack = Liandan.createRecipeReward(BuiltInRegistries.ITEM.get(recipe.item()),
                        new MedicineRecipeReward(recipe.medicineName(), recipe.recipeDescription(), recipe.medicineDescription(),
                                recipe.levelText(), recipe.level(), recipe.ingredients()));
                yield applyCount(stack, recipe.count(), random);
            }
            case TechniqueReward technique -> {
                ItemStack stack = Techniques.createBook(technique.definition());
                yield applyCount(stack, technique.count(), random);
            }
            case SkillReward skill -> {
                SkillBook book = new SkillBook(skill.skillKind(), skill.name(), skill.grade(), skill.level(),
                        skill.body(), skill.intro(), Optional.empty(), "", skill.numbers(), skill.texts());
                ItemStack stack = Skills.createBook(BuiltInRegistries.ITEM.get(skill.item()), book);
                yield applyCount(stack, skill.count(), random);
            }
            default -> throw new IllegalStateException("非物品奖励未在抽样阶段展开：" + reward.kind());
        };
    }

    private static ItemStack applyCount(ItemStack stack, Optional<IntRange> count, RandomSource random) {
        count.ifPresent(range -> stack.setCount(range.roll(random)));
        return stack;
    }

    /** 来源数值转换失败为零，供命令参数解析使用。 */
    static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
