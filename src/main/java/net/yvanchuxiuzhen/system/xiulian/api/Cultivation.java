package net.yvanchuxiuzhen.system.xiulian.api;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/**
 * 修炼系统的公开操作。来源中其他系统对修炼字段的写入只有三种形式：在当前值上加减、乘系数后写回、直接赋值，
 * 且不在写入处按上限截断，因此提供查询、加减与赋值；乘法由调用方读当前值后赋值。
 * 写操作只在服务端调用，写入后同步给本人；客户端只能查询本人的值。
 */
public final class Cultivation {
    /** 灵根与阴阳变化后作为基础层贡献提交给 attribute 的来源键。 */
    public static final String ROOTS_SOURCE = "xiulian:roots";
    private static final Set<CultivationValue> ROOTS = EnumSet.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL, CultivationValue.YINYANG);

    private Cultivation() {
    }

    public static double get(Player player, CultivationValue value) {
        return player.getData(XiulianAttachments.CULTIVATION).get(value);
    }

    public static String get(Player player, CultivationText text) {
        return player.getData(XiulianAttachments.CULTIVATION).get(text);
    }

    public static void add(ServerPlayer player, CultivationValue value, double delta) {
        set(player, value, get(player, value) + delta);
    }

    public static void set(ServerPlayer player, CultivationValue value, double amount) {
        player.getData(XiulianAttachments.CULTIVATION).set(value, amount);
        player.syncData(XiulianAttachments.CULTIVATION);
        if (ROOTS.contains(value)) {
            submitRoots(player);
        }
    }

    public static void set(ServerPlayer player, CultivationText text, String content) {
        player.getData(XiulianAttachments.CULTIVATION).set(text, content);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** {@code thunder_open}：雷劫是否开启。 */
    public static boolean thunderOpen(Player player) {
        return player.getData(XiulianAttachments.CULTIVATION).thunderOpen();
    }

    public static void setThunderOpen(ServerPlayer player, boolean open) {
        player.getData(XiulianAttachments.CULTIVATION).setThunderOpen(open);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 丹田中的本命法宝槽（来源 {@code benmingfabao}）。 */
    public static ItemStack benming(Player player) {
        return player.getData(XiulianAttachments.CULTIVATION).benming();
    }

    public static void setBenming(ServerPlayer player, ItemStack stack) {
        player.getData(XiulianAttachments.CULTIVATION).setBenming(stack);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /**
     * 体质与修正值（来源 {@code tizhiitem} 的标签，键见 {@code docs/systems/xiulian/ledger/tizhiitem_keys.csv}）。
     * 其他系统读取修正值（如 {@code GoldAttCorrectPercentage}）与写入自己的键都经这里。
     */
    public static double tizhiDouble(Player player, String key) {
        return player.getData(XiulianAttachments.CULTIVATION).tizhi().getDouble(key);
    }

    public static void setTizhiDouble(ServerPlayer player, String key, double value) {
        player.getData(XiulianAttachments.CULTIVATION).tizhi().putDouble(key, value);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    public static boolean tizhiBoolean(Player player, String key) {
        return player.getData(XiulianAttachments.CULTIVATION).tizhi().getBoolean(key);
    }

    public static void setTizhiBoolean(ServerPlayer player, String key, boolean value) {
        player.getData(XiulianAttachments.CULTIVATION).tizhi().putBoolean(key, value);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    public static String tizhiString(Player player, String key) {
        return player.getData(XiulianAttachments.CULTIVATION).tizhi().getString(key);
    }

    public static void setTizhiString(ServerPlayer player, String key, String value) {
        player.getData(XiulianAttachments.CULTIVATION).tizhi().putString(key, value);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 灵根值对应 attribute 的五行灵根项（来源中五行系数读 {@code body_*} 与 {@code yinyang}）。 */
    private static void submitRoots(ServerPlayer player) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        PlayerAttributes.setContribution(player, ROOTS_SOURCE, new Contribution(Layer.BASE, Map.of(
                Stat.ROOT_GOLD, data.get(CultivationValue.ROOT_GOLD),
                Stat.ROOT_WATER, data.get(CultivationValue.ROOT_WATER),
                Stat.ROOT_TREE, data.get(CultivationValue.ROOT_TREE),
                Stat.ROOT_FIRE, data.get(CultivationValue.ROOT_FIRE),
                Stat.ROOT_SOIL, data.get(CultivationValue.ROOT_SOIL),
                Stat.YINYANG, data.get(CultivationValue.YINYANG))));
    }
}
