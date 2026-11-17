package net.yvanchuxiuzhen.system.lianqi.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;

/** 炼器系统对外提供的图纸构造与鼎炉火力写入口。 */
public final class Lianqi {
    private Lianqi() {
    }

    /** 副本奖励沿用来源根配方字段，交给普通图纸阅读与炼器入口消费。 */
    public static ItemStack createBlueprintReward(Item carrier, BlueprintReward reward) {
        ItemStack stack = new ItemStack(carrier);
        CompoundTag data = new CompoundTag();
        data.putString("tuzhiname", reward.name());
        data.putString("tuzhishuoming", reward.description());
        data.putString("fabaoid", reward.itemId());
        data.putString("fabaoshuoming", reward.itemDescription());
        data.putString("fabaotype", reward.type());
        data.putString("item_level", reward.levelText());
        data.putInt("item_level_num", reward.level());
        for (int index = 0; index < reward.ingredients().size(); index++) {
            BlueprintReward.Ingredient ingredient = reward.ingredients().get(index);
            String key = "cailiao0" + index;
            data.putString(key, ingredient.id());
            data.putString(key + "name", ingredient.name());
            data.putInt(key + "num", ingredient.count());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    /** 来源出身奖励额外记录图纸载体身份，由炼器所有者写入根字段。 */
    public static ItemStack createBlueprintReward(Item carrier, BlueprintReward reward, String sourceCarrierId) {
        ItemStack stack = createBlueprintReward(carrier, reward);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putString("belongToItemID", sourceCarrierId));
        return stack;
    }

    /** 火力符覆盖炉内火力，不重置已流逝的火力计时。 */
    public static void boostHeat(ServerLevel level, BlockPos pos, double power) {
        if (level.getBlockEntity(pos) instanceof TripodBlockEntity furnace) {
            furnace.getPersistentData().putDouble("firepower", power);
            furnace.changed();
        }
    }
}
