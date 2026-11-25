package net.yvanchuxiuzhen.system.xiulian.api;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.CultivationCommand;
import net.yvanchuxiuzhen.system.xiulian.LevelUp;
import net.yvanchuxiuzhen.system.xiulian.Rebirth;
import net.yvanchuxiuzhen.system.xiulian.SkillPoints;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;
import net.yvanchuxiuzhen.system.xiulian.content.SkillPointMenu;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/**
 * 修炼系统的公开操作。来源中其他系统对修炼字段的写入只有三种形式：在当前值上加减、乘系数后写回、直接赋值，
 * 且不在写入处按上限截断，因此提供查询、加减与赋值；乘法由调用方读当前值后赋值。
 * 写操作只在服务端调用，写入后同步给本人；客户端只能查询本人的值。
 */
public final class Cultivation {
    private static final List<CultivationValue> REINCARNATION_VALUES = List.of(CultivationValue.LEVEL_NUMBER,
            CultivationValue.EXPERIENCE_MAX, CultivationValue.EXPERIENCE, CultivationValue.EXPERIENCE_STAND,
            CultivationValue.EXPERIENCE_RATE, CultivationValue.EXPERIENCE_HINDER, CultivationValue.POWER, CultivationValue.SOUL,
            CultivationValue.SKILL_POINTS, CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER, CultivationValue.ROOT_TREE,
            CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL, CultivationValue.YINYANG, CultivationValue.MERITS,
            CultivationValue.VIGOROUS, CultivationValue.THUNDER_MAX, CultivationValue.THUNDER, CultivationValue.THUNDER_ATT,
            CultivationValue.BLOOD_LINEAGE_LEVEL, CultivationValue.BLOOD_LINEAGE_MAX, CultivationValue.BLOOD_LINEAGE_NUM,
            CultivationValue.SKILL_POINTS_HEALTH_MAX, CultivationValue.SKILL_POINTS_POWER_MAX, CultivationValue.SKILL_POINTS_SOUL_MAX,
            CultivationValue.SKILL_POINTS_ATTACK, CultivationValue.SKILL_POINTS_MANA, CultivationValue.SKILL_POINTS_SOUL,
            CultivationValue.LIFETIME, CultivationValue.RANDOMLIFT, CultivationValue.CULTIVATION_DAYS);
    private static final Set<Stat> REINCARNATION_GROWTH = EnumSet.of(Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX,
            Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC, Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF,
            Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.COOLDOWN_REDUCTION);
    /** 灵根与阴阳变化后作为基础层贡献提交给 attribute 的来源键。 */
    public static final String ROOTS_SOURCE = "xiulian:roots";
    /** 属性成长（来源写入 {@code health_Max} 等基础字段的累计量）作为基础层贡献提交给 attribute 的来源键。 */
    public static final String GROWTH_SOURCE = "xiulian:growth";
    private static final Set<CultivationValue> ROOTS = EnumSet.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL, CultivationValue.YINYANG);

    private Cultivation() {
    }

    /** 普通重生的修炼片段：按来源顺序修正生命、灵力、神识、待吸收修为与寿元。 */
    public static void rebirth(ServerPlayer player) {
        Rebirth.rebirth(player);
    }

    /** 轮回 37 的修炼和基础成长复位，保留本命、复活标记、闭关闭却与已有恢复显示。 */
    public static double resetForReincarnation(ServerPlayer player) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        REINCARNATION_VALUES.forEach(value -> data.set(value, value.initial()));
        for (CultivationText text : List.of(CultivationText.LEVEL_NAME, CultivationText.RACE, CultivationText.RACE_NAME,
                CultivationText.BLOOD_LINEAGE, CultivationText.CONSTITUTION, CultivationText.BIRTH)) {
            data.set(text, text.initial());
        }
        data.setThunderOpen(false);
        double extraLocks = data.tizhi().getDouble("liftlockewai");
        data.set(CultivationValue.LIFTLOCK, 1 + extraLocks);
        data.growth().keySet().removeAll(REINCARNATION_GROWTH);
        PlayerAttributes.setContribution(player, GROWTH_SOURCE, new Contribution(Layer.BASE, data.growth()));
        submitRoots(player);
        player.syncData(XiulianAttachments.CULTIVATION);
        return extraLocks;
    }

    /** 轮回库存处理之后按来源规则重抽五根，替换体质标签并安排出身界面。 */
    public static void finishReincarnation(ServerPlayer player, ServerLevel sourceLevel, double extraLocks) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        if (BirthSelection.ROOTS.stream().mapToDouble(data::get).sum() < 1) {
            BirthSelection.rollReincarnationRoots(player, sourceLevel, data);
            submitRoots(player);
        }
        CompoundTag tizhi = data.tizhi();
        for (String key : Set.copyOf(tizhi.getAllKeys())) tizhi.remove(key);
        tizhi.putDouble("liftlockewai", extraLocks);
        player.syncData(XiulianAttachments.CULTIVATION);
        BirthSelection.openAfterReincarnation(player);
    }

    /** 非玩家轮回仍执行来源的五次灵根随机取值，但没有玩家能力可写。 */
    public static void rollNonPlayerReincarnationRoots(ServerLevel level) {
        int cap = level.getGameRules().getInt(XiulianRules.ROOT_LEVEL);
        for (int index = 0; index < 5; index++) {
            Random random = new Random();
            if (cap > 0) random.nextInt(cap + 1);
        }
    }

    /** 为拥有服务端玩家界面的调用者延迟打开出身选择。 */
    public static void openBirthAfterReincarnation(ServerPlayer player) {
        BirthSelection.openAfterReincarnation(player);
    }

    /** 执行共享原初管理命令中的修炼分支；世界规则使用命令源维度，玩家档案使用已匹配目标。 */
    public static boolean command(ServerLevel level, ServerPlayer player, String action, String subject,
            String operation, String value, String feedback) {
        return CultivationCommand.execute(level, player, action, subject, operation, value, feedback);
    }

    public static double entitySoul(Entity entity) {
        return entity.getData(XiulianAttachments.ENTITY_SOUL);
    }

    public static void setEntitySoul(Entity entity, double value) {
        entity.setData(XiulianAttachments.ENTITY_SOUL, value);
    }

    /** 阴一阵法增加本体实体 soul；玩家修炼能力的 SOUL 另由原有接口维护。 */
    public static void addEntitySoul(Entity entity, int amount) {
        setEntitySoul(entity, entitySoul(entity) + amount);
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

    /** 在同一次同步内修改体质标签的多个键；用于逐刻推进计数的持续效果，避免每个键各发一次同步。 */
    public static void editTizhi(ServerPlayer player, Consumer<CompoundTag> edit) {
        edit.accept(player.getData(XiulianAttachments.CULTIVATION).tizhi());
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    public static String tizhiString(Player player, String key) {
        return player.getData(XiulianAttachments.CULTIVATION).tizhi().getString(key);
    }

    public static void setTizhiString(ServerPlayer player, String key, String value) {
        player.getData(XiulianAttachments.CULTIVATION).tizhi().putString(key, value);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 主修功法信息，由 gongfa 在功法栏变化、功法升级与登录时提交。 */
    public static MainTechnique mainTechnique(Player player) {
        return player.getData(XiulianAttachments.CULTIVATION).mainTechnique();
    }

    public static void setMainTechnique(ServerPlayer player, MainTechnique technique) {
        player.getData(XiulianAttachments.CULTIVATION).setMainTechnique(technique);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 本命槽内物品的培养进度，由 equipment 在培养本命法宝时提交，供丹田界面显示。 */
    public static BenmingProgress benmingProgress(Player player) {
        return player.getData(XiulianAttachments.CULTIVATION).benmingProgress();
    }

    public static void setBenmingProgress(ServerPlayer player, BenmingProgress progress) {
        player.getData(XiulianAttachments.CULTIVATION).setBenmingProgress(progress);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 在指定的最终打开坐标创建属性加点页，跨页记忆由汇总导航维护。 */
    public static void openSkillPoints(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new SkillPointMenu(id, inventory, pos),
                Component.translatable("gui.yvanchuxiuzhen.player_gui_02.label_shu_xing_jia_dian")), pos);
    }

    /** 分配一个属性点（面板第 2 页的加点按钮）；属性点不足或该项已达当前境界上限时只提示。 */
    public static void spendSkillPoint(ServerPlayer player, SkillPoint point) {
        SkillPoints.spend(player, point);
    }

    /** 层数对应的境界名（如 1 为“练气一层”，41 以上为“化神”）。 */
    public static String realmName(int level) {
        return LevelUp.realmName(level);
    }

    /** 管理命令 {@code /yvanchu … chongzhilinggen}：五行灵根重新随机 0–9 并重算阴阳。 */
    public static void resetRoots(ServerPlayer player) {
        BirthSelection.resetRoots(player);
    }

    /** 管理命令 {@code /yvanchu … chongzhilinggenmax}：五行灵根全为 9，阴阳为 0。 */
    public static void maximizeRoots(ServerPlayer player) {
        BirthSelection.maximizeRoots(player);
    }

    /** 给基础属性加成长量（升级修正、境界修正、属性点、单项提升），累计后提交给 attribute。 */
    public static void addGrowth(ServerPlayer player, Stat stat, double delta) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        data.growth().merge(stat, delta, Double::sum);
        player.syncData(XiulianAttachments.CULTIVATION);
        PlayerAttributes.setContribution(player, GROWTH_SOURCE, new Contribution(Layer.BASE, data.growth()));
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
