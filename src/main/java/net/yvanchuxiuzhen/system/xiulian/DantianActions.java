package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.DantianReturnEvent;
import net.yvanchuxiuzhen.system.xiulian.content.DantianMenu;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/**
 * 丹田界面的服务端规则（来源 {@code DantianGuiDangZheGeJieMianDaKaiShi}、{@code Dantianguiopen}、{@code Dantianguiclose}、
 * {@code Tizhipaixv}、{@code Tizhifanye}、{@code Tizhipageup/down}、{@code Tizhidianji1}–{@code 9} 与说明按钮）。
 * 体质列表、页码、金丹与元婴名、本命显示标记沿用来源的 {@code tizhiitem} 键，随修炼附件同步给本人供界面显示。
 */
public final class DantianActions {
    /** 按钮编号：0 上一页、1 下一页、2–10 第 1–9 行体质说明、11 出身说明、12 返回、13 本命法宝说明、14 属性熟练度说明。 */
    public static final int BUTTONS = 15;
    private static final TagKey<Item> YAODAN = tag("yaodan");
    private static final TagKey<Item> FABAO = tag("fabao");
    private static final TagKey<Item> WUQI = tag("wuqi");
    private static final ResourceKey<Enchantment> QILING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "qiling"));
    /** 体质编号按三位数字写（{@code tizhiopen_001}–{@code 999}），排序扫描到此为止。 */
    private static final int LAST_CONSTITUTION = 999;

    private DantianActions() {
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 打开时整理页码、金丹与元婴名和体质列表，本命槽放入本命法宝（数量 1），按槽内是否有物品设置本命显示标记。 */
    public static void open(ServerPlayer player) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        CompoundTag tizhi = data.tizhi();
        if (tizhi.getDouble("tizhipagenum") <= 0) {
            tizhi.putDouble("tizhipagenum", 1);
            tizhi.putDouble("tizhipagenummax", 1);
        } else if (tizhi.getDouble("tizhipagenummax") <= 0) {
            tizhi.putDouble("tizhipagenummax", Math.max(Math.ceil(tizhi.getDouble("tizhinumber") / 9), 1));
        }
        double level = data.get(CultivationValue.LEVEL_NUMBER);
        if (level >= 21 && level < 31) {
            nameFirstOpen(tizhi, "jindanname", 101);
        } else if (level >= 31) {
            nameFirstOpen(tizhi, "yvanyingname", 201);
        }
        sort(tizhi);
        ItemStack benming = data.benming().copyWithCount(1);
        tizhi.putBoolean("benmingxianshi", !benming.isEmpty());
        player.syncData(XiulianAttachments.CULTIVATION);
        SimpleContainer slot = new SimpleContainer(1);
        slot.setItem(0, benming);
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new DantianMenu(id, inventory, slot), Component.literal("DantianGui")));
    }

    /** 关闭时槽内物品成为本命法宝；槽空则清空本命槽并取消本命显示，结丹期起放入物品时显示本命。 */
    public static void close(ServerPlayer player, ItemStack slotItem) {
        CultivationData data = player.getData(XiulianAttachments.CULTIVATION);
        if (slotItem.isEmpty()) {
            data.setBenming(ItemStack.EMPTY);
            data.tizhi().putBoolean("benmingxianshi", false);
        } else {
            data.setBenming(slotItem.copy());
            if (data.get(CultivationValue.LEVEL_NUMBER) >= 21) {
                data.tizhi().putBoolean("benmingxianshi", true);
            }
        }
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /** 本命槽只接受妖丹，以及带“启灵”附魔的法宝与武器（来源 {@code Dantianfangzhileixing}）。 */
    public static boolean canHold(ItemStack stack) {
        if (stack.is(YAODAN)) {
            return true;
        }
        return (stack.is(FABAO) || stack.is(WUQI))
                && stack.getTagEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.is(QILING));
    }

    public static void press(ServerPlayer player, int button) {
        CompoundTag tizhi = player.getData(XiulianAttachments.CULTIVATION).tizhi();
        switch (button) {
            case 0 -> turnPage(player, tizhi, -1);
            case 1 -> turnPage(player, tizhi, 1);
            case 11 -> {
                Birth birth = Birth.of(Cultivation.get(player, CultivationText.BIRTH));
                if (birth != null) {
                    player.displayClientMessage(Component.literal(birth.message()), false);
                }
            }
            case 12 -> {
                if (!NeoForge.EVENT_BUS.post(new DantianReturnEvent(player)).isCanceled()) {
                    player.closeContainer();
                }
            }
            case 13 -> {
                message(player, "本命法宝，顾名思义，它是一种特殊法宝，与其主人的生命紧密相连。故而本命法宝威力奇大，并且会增强法宝自身的神通。");
                message(player, "但是，培养本命法宝需要让法宝认主，并且法宝也需要有“启灵”附魔，您的境界也必须达到结丹期以上。");
                message(player, "满足条件后，把法宝放入此界面中间的物品槽内，就可以自动培养了。");
                message(player, "本命法宝等级越高，释放的技能伤害就越高，所以等级自然是越高越好。但是培养本命法宝是一个水磨功夫，启灵等级越高，培养速度越快。");
                message(player, "本命法宝的等级与法宝等级是两个不同的属性，可以同时存在且互不干扰。本命法宝的等级不会超过您当前的等级。");
            }
            case 14 -> {
                message(player, "属性熟练度是记录您属性伤害击杀敌人的累积积分，本身只是一种统计数字，并不直接影响您的属性。");
                message(player, "但是如果您要凝结元婴或者金丹，那么这个统计将是一项重要指标。只有达到某个值，才能凝结某种元婴、金丹。");
            }
            default -> {
                String id = tizhi.getString("tizhixianshi" + (button - 1) + "id");
                if (!id.isEmpty()) {
                    player.displayClientMessage(Component.translatable("tizhi.shuoming." + id), false);
                }
            }
        }
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }

    /** 从 {@code from} 起的 100 个编号中第一个打开的体质记为金丹（101 起）或元婴（201 起）名，没有时保留原值。 */
    private static void nameFirstOpen(CompoundTag tizhi, String key, int from) {
        for (int number = from; number < from + 100; number++) {
            if (tizhi.getBoolean("tizhiopen_" + number)) {
                tizhi.putString(key, "tizhiopen_" + number);
                return;
            }
        }
    }

    /**
     * 按编号顺序把打开的体质记为 {@code tizhipaixv<序号>id}，共记 {@code tizhinumber} 个；最大页数按体质数每页 9 个计算
     * （原值不大于 0 时为 1）。来源在打开的体质少于 {@code tizhinumber} 时无限循环，这里扫描到最后一个三位编号为止（Q24）。
     */
    private static void sort(CompoundTag tizhi) {
        double count = tizhi.getDouble("tizhinumber");
        tizhi.putDouble("tizhipagenum", Math.max(tizhi.getDouble("tizhipagenum"), 1));
        tizhi.putDouble("tizhipagenummax", tizhi.getDouble("tizhipagenummax") <= 0 ? 1 : Math.ceil(count / 9));
        int found = 0;
        for (int number = 1; found < count && number <= LAST_CONSTITUTION; number++) {
            String id = String.format("tizhiopen_%03d", number);
            if (tizhi.getBoolean(id)) {
                found++;
                tizhi.putString("tizhipaixv" + found + "id", id);
            }
        }
        fillPage(tizhi);
    }

    /** 上一页、下一页（来源 {@code Tizhipagedown}、{@code Tizhipageup}）：页码在 1 与最大页数之间移动，再刷新本页。 */
    private static void turnPage(ServerPlayer player, CompoundTag tizhi, int step) {
        if (tizhi.getDouble("tizhipagenum") <= 0) {
            tizhi.putDouble("tizhipagenum", 1);
            tizhi.putDouble("tizhipagenummax", 1);
        }
        if (tizhi.getDouble("tizhipagenummax") <= 0) {
            tizhi.putDouble("tizhipagenummax", Math.ceil(tizhi.getDouble("tizhipagenum") / 9));
        }
        double page = tizhi.getDouble("tizhipagenum");
        if (step < 0 ? page > 1 : page < tizhi.getDouble("tizhipagenummax")) {
            tizhi.putDouble("tizhipagenum", page + step);
        }
        fillPage(tizhi);
        player.syncData(XiulianAttachments.CULTIVATION);
    }

    /**
     * 本页 9 行 {@code tizhixianshi<行>id} 取排序中从 (页码 − 1) × 9 + 1 起的体质。来源的循环条件使第 9 行只在体质数
     * 大于本页起点 + 8 时填写，最后一页恰好 9 个时第 9 行为空（Q24）。
     */
    private static void fillPage(CompoundTag tizhi) {
        double count = tizhi.getDouble("tizhinumber");
        double start = (tizhi.getDouble("tizhipagenum") - 1) * 9 + 1;
        for (int row = 1; row <= 9; row++) {
            double position = start + row - 1;
            boolean filled = row < 9 || position < count;
            tizhi.putString("tizhixianshi" + row + "id", filled ? tizhi.getString("tizhipaixv" + (int) position + "id") : "");
        }
    }
}
