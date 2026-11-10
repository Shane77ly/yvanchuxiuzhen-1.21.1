package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.system.plant.LotusPlanting;
import net.yvanchuxiuzhen.system.plant.ProduceCrop;
import net.yvanchuxiuzhen.system.plant.ProducePlanting;
import net.yvanchuxiuzhen.system.plant.VegetablePlanting;

/** 可食播种物的跨系统操作；只由Plant写作物状态，食用属性和物品身份仍归Cooking。 */
public final class Planting {
    private Planting() {
    }

    public static InteractionResult lotus(UseOnContext context) {
        return LotusPlanting.plant(context);
    }

    public static InteractionResult garlic(UseOnContext context) {
        return VegetablePlanting.plantGarlic(context);
    }

    public static InteractionResult shicai028(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_028);
    }

    public static InteractionResult shicai029(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_029);
    }

    public static InteractionResult shicai030(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_030);
    }

    public static InteractionResult shicai031(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_031);
    }

    public static InteractionResult shicai032(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_032);
    }

    public static InteractionResult shicai033(UseOnContext context) {
        return ProducePlanting.useOn(context, ProduceCrop.SHICAI_033);
    }
}
