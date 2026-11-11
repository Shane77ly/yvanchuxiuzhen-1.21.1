package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.system.plant.FungusCrop;
import net.yvanchuxiuzhen.system.plant.FungusPlanting;
import net.yvanchuxiuzhen.system.plant.LegumeCrop;
import net.yvanchuxiuzhen.system.plant.LegumePlanting;
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

    public static InteractionResult shicai040(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_040);
    }

    public static InteractionResult shicai041(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_041);
    }

    public static InteractionResult shicai042(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_042);
    }

    public static InteractionResult shicai043(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_043);
    }

    public static InteractionResult shicai044(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_044);
    }

    public static InteractionResult shicai045(UseOnContext context) {
        return FungusPlanting.useOn(context, FungusCrop.SHICAI_045);
    }

    public static InteractionResult shicai046(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_046);
    }

    public static InteractionResult shicai047(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_047);
    }

    public static InteractionResult shicai048(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_048);
    }

    public static InteractionResult shicai049(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_049);
    }

    public static InteractionResult shicai050(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_050);
    }

    public static InteractionResult shicai051(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_051);
    }

    public static InteractionResult shicai052(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_052);
    }

    public static InteractionResult shicai053(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_053);
    }

    public static InteractionResult shicai054(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_054);
    }

    public static InteractionResult shicai055(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_055);
    }

    public static InteractionResult shicai056(UseOnContext context) {
        return LegumePlanting.plant(context, LegumeCrop.SHICAI_056);
    }
}
