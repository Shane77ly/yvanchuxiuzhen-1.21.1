package net.yvanchuxiuzhen.system.liandan.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbCommand;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;

/** 炼丹系统的跨系统写入口，管理命令和火力符分别调用所属数据的操作。 */
public final class Liandan {
    private Liandan() {
    }

    /** 奖励层提供来源参数，成品丹药实例仍由炼丹系统构造。 */
    public static ItemStack createMedicineReward(Item carrier, MedicineReward reward) {
        ItemStack stack = new ItemStack(carrier);
        CompoundTag data = new CompoundTag();
        data.putString("danyaoname", reward.name());
        data.putInt("shuliang", reward.effectCount());
        data.putInt("danyaonum", reward.doses());
        data.putBoolean("danyaobaoxiang", reward.treasure());
        for (int index = 0; index < reward.effects().size(); index++) {
            MedicineReward.Effect effect = reward.effects().get(index);
            data.putString("danyaoxiaoguo" + index, effect.id());
            data.putInt("danyaolevel" + index, effect.level());
            data.putInt("danyaotick" + index, effect.duration());
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(reward.name()));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    /** 生成由公共书阅读、药理与炼丹配方消费者读取的宝箱丹方。 */
    public static ItemStack createRecipeReward(Item carrier, MedicineRecipeReward reward) {
        ItemStack stack = new ItemStack(carrier);
        CompoundTag data = new CompoundTag();
        data.putString("danyaoname", reward.name());
        data.putString("danfangshuoming", reward.description());
        data.putString("danyaoshuoming", reward.medicineDescription());
        data.putString("item_level", reward.levelText());
        data.putInt("item_level_num", reward.level());
        for (int index = 0; index < reward.ingredients().size(); index++) {
            String suffix = "0" + (index + 1);
            MedicineRecipeReward.Ingredient ingredient = reward.ingredients().get(index);
            data.putString("cailiao" + suffix, ingredient.id());
            data.putString("cailiao" + suffix + "name", ingredient.name());
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(reward.name()));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity furnace) {
            furnace.getPersistentData().putDouble("firepower", power);
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
