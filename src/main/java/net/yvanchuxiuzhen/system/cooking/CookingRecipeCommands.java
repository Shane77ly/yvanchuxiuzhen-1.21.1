package net.yvanchuxiuzhen.system.cooking;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.cooking.api.Cooking;
import net.yvanchuxiuzhen.system.cooking.api.CookingRecipeData;

/** 菜谱命令按来源空白分词构建单件载体，名称组件与烹饪字段分别保存。 */
public final class CookingRecipeCommands {
    private static final String[] GRADES = {"一级", "二级", "三级", "四级", "五级", "六级", "七级", "八级", "九级"};

    private CookingRecipeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shipu").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(CookingRecipeCommands::execute))
                .executes(CookingRecipeCommands::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getEntity();
        if (entity == null) entity = FakePlayerFactory.getMinecraft(source.getLevel());
        String[] words = context.getInput().split("\\s+");
        ItemStack recipe = CookingRecipes.stack(argument(words, 0).toLowerCase(Locale.ENGLISH));
        recipe.set(DataComponents.CUSTOM_NAME, Component.literal(argument(words, 1)));
        double grade = number(argument(words, 2));
        List<String> ingredients = new ArrayList<>(9);
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            ingredients.add(argument(words, ingredient + 8));
        }
        Cooking.setRecipe(recipe, new CookingRecipeData(
                "",
                grade,
                gradeText(grade),
                argument(words, 3),
                (int) number(argument(words, 4)),
                argument(words, 5),
                argument(words, 6),
                argument(words, 7),
                ingredients));
        if (entity instanceof Player player) {
            recipe.setCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, recipe);
        }
        return 0;
    }

    /** 来源跳过完整输入的首个分词，后续缺失参数读取为空串。 */
    private static String argument(String[] words, int index) {
        return index + 1 < words.length ? words[index + 1] : "";
    }

    private static String gradeText(double grade) {
        for (int index = 0; index < GRADES.length; index++) {
            if (grade == index + 1) return GRADES[index];
        }
        return "无品级";
    }

    /** 来源数值解析失败为零，非整数、NaN及无穷值按Double原生解析结果保存。 */
    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
