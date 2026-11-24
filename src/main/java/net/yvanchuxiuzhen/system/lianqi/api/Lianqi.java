package net.yvanchuxiuzhen.system.lianqi.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.lianqi.LianqiRules;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.data.LianqiData;

/** 炼器系统对外提供的图纸构造与鼎炉火力写入口。 */
public final class Lianqi {
    private Lianqi() {
    }

    public static void setWeaponLevelUp(ServerLevel level, boolean value) {
        level.getGameRules().getRule(LianqiRules.WEAPON_LEVEL_UP).set(value, level.getServer());
    }

    public static Optional<LianqiBlueprint> blueprint(ItemStack stack) {
        return Optional.ofNullable(stack.get(LianqiData.BLUEPRINT.get())).filter(LianqiBlueprint::isPresent);
    }

    public static void setBlueprint(ItemStack stack, LianqiBlueprint blueprint) {
        stack.set(LianqiData.BLUEPRINT.get(), blueprint);
    }

    /** 副本奖励沿用来源根配方字段，交给普通图纸阅读与炼器入口消费。 */
    public static ItemStack createBlueprintReward(Item carrier, BlueprintReward reward) {
        return createBlueprintReward(carrier, reward, "");
    }

    /** 来源出身奖励额外记录图纸载体身份，由炼器所有者写入组件。 */
    public static ItemStack createBlueprintReward(Item carrier, BlueprintReward reward, String sourceCarrierId) {
        ItemStack stack = new ItemStack(carrier);
        List<LianqiBlueprint.Ingredient> ingredients = new ArrayList<>(reward.ingredients().size());
        for (BlueprintReward.Ingredient ingredient : reward.ingredients()) {
            ingredients.add(new LianqiBlueprint.Ingredient(ingredient.id(), ingredient.name(), "", ingredient.count()));
        }
        setBlueprint(stack, new LianqiBlueprint(reward.name(), "", reward.description(), reward.itemId(),
                reward.itemDescription(), "", reward.type(), reward.levelText(), reward.level(),
                sourceCarrierId, ingredients));
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
