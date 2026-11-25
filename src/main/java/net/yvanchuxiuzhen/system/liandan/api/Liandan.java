package net.yvanchuxiuzhen.system.liandan.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbCommand;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.data.LiandanData;

/** 炼丹系统的跨系统写入口，管理命令和火力符分别调用所属数据的操作。 */
public final class Liandan {
    private Liandan() {
    }

    public static MedicineInstance medicine(ItemStack stack) {
        return stack.getOrDefault(LiandanData.MEDICINE.get(), MedicineInstance.EMPTY);
    }

    public static void setMedicine(ItemStack stack, MedicineInstance medicine) {
        stack.set(LiandanData.MEDICINE.get(), medicine);
    }

    public static MedicineRecipe recipe(ItemStack stack) {
        return stack.getOrDefault(LiandanData.RECIPE.get(), MedicineRecipe.EMPTY);
    }

    public static void setRecipe(ItemStack stack, MedicineRecipe recipe) {
        stack.set(LiandanData.RECIPE.get(), recipe);
    }

    /** 奖励层提供来源参数，成品丹药实例仍由炼丹系统构造。 */
    public static ItemStack createMedicineReward(Item carrier, MedicineReward reward) {
        ItemStack stack = new ItemStack(carrier);
        List<MedicineInstance.Effect> effects = new ArrayList<>(reward.effects().size());
        for (MedicineReward.Effect effect : reward.effects()) {
            effects.add(new MedicineInstance.Effect(effect.id(), effect.level(), effect.duration()));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(reward.name()));
        setMedicine(stack, new MedicineInstance("", reward.name(), "", reward.doses(), reward.treasure(), effects));
        return stack;
    }

    /** 生成由公共书阅读、药理与炼丹配方消费者读取的宝箱丹方。 */
    public static ItemStack createRecipeReward(Item carrier, MedicineRecipeReward reward) {
        ItemStack stack = new ItemStack(carrier);
        List<MedicineRecipe.Ingredient> ingredients = new ArrayList<>(reward.ingredients().size());
        for (MedicineRecipeReward.Ingredient ingredient : reward.ingredients()) {
            ingredients.add(new MedicineRecipe.Ingredient(ingredient.id(), ingredient.name()));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(reward.name()));
        setRecipe(stack, new MedicineRecipe(reward.name(), reward.description(), reward.medicineDescription(),
                reward.levelText(), reward.level(), "", ingredients));
        return stack;
    }

    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity furnace) {
            furnace.setFirePower(power);
            furnace.changed();
        }
    }

    public static void unlockHerbs(ServerPlayer player) {
        LiandanHerbAnalysis.unlockAll(player);
    }

    /** 共享原初系统命令的药草分支，保持来源参数和执行维度语义。 */
    public static int command(CommandSourceStack source, String arguments) {
        return LiandanHerbCommand.run(source, arguments);
    }
}
