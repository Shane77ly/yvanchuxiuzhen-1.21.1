package net.yvanchuxiuzhen.system.treasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipeReward;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 宝箱总表的来源字段解释与实际发放；等级属于本次开箱，不写玩家档案。 */
public final class TreasureRewards {
    private TreasureRewards() {
    }

    /** 来源 Baoxiangzong：先抽发放次数，再逐次抽奖励并在命令位置附近掉落。 */
    public static void open(ServerLevel level, Vec3 at, String category, String subtype, double grade, int minimum, int maximum) {
        int count = Mth.nextInt(level.random, minimum, maximum);
        for (int index = 0; index < count; index++) {
            String reward = TreasureRewardTables.select(category, subtype, grade, level.random);
            String[] parts = reward.split("%");
            // 来源总表127—138行直接读取首段第三个字段；空表及只追加品阶字段的分支中断当次命令。
            if (!parts[0].equals("指令") && parts[0].split("/").length < 3) return;
            if (parts[0].equals("指令")) {
                CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, at.add(0.5, 0.5, 0.5), Vec2.ZERO,
                        level, 4, "", Component.empty(), level.getServer(), null).withSuppressedOutput();
                level.getServer().getCommands().performPrefixedCommand(source, parts[1]);
            } else {
                ItemStack product = create(reward);
                ItemEntity drop = new ItemEntity(level, at.x + 0.5, at.y + 1, at.z + 0.5, product);
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }
        }
    }

    /** 来源奖励串：第一段选承载物品，第二段命名，其余段按显式类型写入。 */
    public static ItemStack create(String reward) {
        String[] parts = reward.split("%");
        String[] identity = parts[0].split("/");
        ResourceLocation item = ResourceLocation.parse(identity[2].toLowerCase(Locale.ENGLISH));
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(item));
        Map<String, String> texts = new HashMap<>();
        Map<String, Double> numbers = new HashMap<>();
        Map<String, Boolean> flags = new HashMap<>();
        for (int index = 1; index < parts.length; index++) {
            String[] field = parts[index].split("/");
            if (field[0].equals("无")) continue;
            if (index == 1) {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(field[2]));
                texts.put(field[0], field[2]);
            } else {
                switch (field[1]) {
                    case "char" -> texts.put(field[0], field[2]);
                    case "int" -> numbers.put(field[0], (double) (int) number(field[2]));
                    case "double", "doube" -> numbers.put(field[0], number(field[2]));
                    case "logic" -> flags.put(field[0], field[2].equals("真"));
                    default -> { }
                }
            }
        }
        Fields fields = new Fields(texts, numbers);
        if (texts.containsKey("ExercisesName")) {
            Map<String, Double> elements = new HashMap<>();
            for (String element : List.of("Gold", "Water", "Tree", "Fire", "Soil")) {
                elements.put(element.toLowerCase(Locale.ENGLISH), fields.number("Exercises" + element));
            }
            List<String> tiers = Arrays.stream(TechniqueStat.values()).map(stat -> fields.text(stat.tierKey())).toList();
            TechniqueDefinition definition = new TechniqueDefinition(item, fields.text("ExercisesName"), fields.text("belongTopinjietext"),
                    fields.number("belongTopinjie"), fields.text("belongToLeveltext"), fields.number("belongToLevel"),
                    fields.text("belongTobody"), fields.text("belongTojianjie"), Map.copyOf(elements), tiers);
            return Techniques.createBook(definition);
        }
        if (texts.containsKey("jinengName") || texts.containsKey("shenfaName") || texts.containsKey("flyskillName")) {
            SkillKind kind = texts.containsKey("jinengName") ? SkillKind.SPELL : texts.containsKey("shenfaName") ? SkillKind.MOVEMENT : SkillKind.FLIGHT;
            String nameKey = kind == SkillKind.SPELL ? "jinengName" : kind == SkillKind.MOVEMENT ? "shenfaName" : "flyskillName";
            SkillBook book = new SkillBook(kind, fields.text(nameKey), fields.number("belongTopinjie"), fields.number("belongToLevel"),
                    fields.text("belongTobody"), fields.text("belongTojianjie"), Optional.empty(), "", Map.copyOf(numbers), Map.copyOf(texts));
            return Skills.createBook(BuiltInRegistries.ITEM.get(item), book);
        }
        if (numbers.containsKey("fululevel")) {
            return Fulu.createReward(stack.getItem(), (int) fields.number("fululevel"), fields.text("fululeveltxt"));
        }
        if (identity[0].equals("danyaoid")) {
            List<MedicineReward.Effect> effects = new ArrayList<>();
            for (int index = 0; texts.containsKey("danyaoxiaoguo" + index); index++) {
                effects.add(new MedicineReward.Effect(fields.text("danyaoxiaoguo" + index),
                        (int) fields.number("danyaolevel" + index), (int) fields.number("danyaotick" + index)));
            }
            return Liandan.createMedicineReward(stack.getItem(), new MedicineReward(fields.text("danyaoname"),
                    (int) fields.number("shuliang"), (int) fields.number("danyaonum"), flags.getOrDefault("danyaobaoxiang", false), effects));
        }
        if (identity[0].equals("danfangid")) {
            List<MedicineRecipeReward.Ingredient> ingredients = new ArrayList<>();
            for (int index = 1; texts.containsKey("cailiao0" + index); index++) {
                ingredients.add(new MedicineRecipeReward.Ingredient(fields.text("cailiao0" + index), fields.text("cailiao0" + index + "name")));
            }
            return Liandan.createRecipeReward(stack.getItem(), new MedicineRecipeReward(fields.text("danyaoname"),
                    fields.text("danfangshuoming"), fields.text("danyaoshuoming"), fields.text("item_level"),
                    (int) fields.number("item_level_num"), ingredients));
        }
        if (numbers.containsKey("cailiaolevel") || numbers.containsKey("cailiaopinjie")) {
            Materials.setInstance(stack, (int) fields.number("cailiaolevel"), (int) fields.number("cailiaopinjie"));
        }
        return stack;
    }

    /** 缺少来源根字段时，读取语义与 CompoundTag 的空文本、零数值一致。 */
    private record Fields(Map<String, String> texts, Map<String, Double> numbers) {
        String text(String key) {
            return texts.getOrDefault(key, "");
        }

        double number(String key) {
            return numbers.getOrDefault(key, 0.0);
        }
    }

    /** 来源数值转换失败为零，供命令和奖励字段共用。 */
    static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
