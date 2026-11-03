package net.yvanchuxiuzhen.system.gongfa;

import java.text.DecimalFormat;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlockEntity;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 参悟台的规则（来源 {@code Canwutaiqiehuan}、{@code Canwutaishuaxin}、{@code Canwudianji} 与三个计算过程）。
 * 参悟：消耗寿元 参悟次数² × 品阶 + 20，使槽内功法的所需境界降一层（至少保留 1 层），只能由第一个参悟者继续参悟。
 * 继承：消耗本人研习过的书，把它的累计经验 ×（0.5 +（书的品阶 − 装备功法品阶）× 0.02）加到装备的本人功法上；
 * 只有装备功法因此升级时槽内的书才被消耗（Q27）。按钮编号：0 切换模式，1 开始参悟或继承，2 刷新说明。
 */
public final class Comprehension {
    public static final int BUTTONS = 3;
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final String COMPREHEND_FIRST = "请放入你想要，但限于等级无法学习的功法。";
    private static final String COMPREHEND_SECOND = "通过消耗寿元来参悟功法，降低该功法的等级需求。";
    private static final String INHERIT_FIRST = "以彻底消耗此功法为代价，按比例继承功法部分经验。";
    private static final String INHERIT_SECOND = "槽中的功法将消失，变成经验直接作用于你装备的功法上。";
    private static final String INHERIT_THIRD = "装备功法品阶与消耗功法品阶之间的差值，影响继承比例。";

    private Comprehension() {
    }

    /** 放置时：参悟模式与初始说明。 */
    public static void placed(ComprehensionTableBlockEntity table) {
        table.show(ComprehensionTableBlockEntity.COMPREHEND, COMPREHEND_FIRST, COMPREHEND_SECOND, "", table.inheritable());
    }

    public static void press(ServerPlayer player, ComprehensionTableBlockEntity table, int button) {
        switch (button) {
            case 0 -> toggle(player, table);
            case 1 -> start(player, table);
            default -> refresh(player, table);
        }
    }

    private static boolean comprehending(String mode) {
        return mode.isEmpty() || mode.equals(ComprehensionTableBlockEntity.COMPREHEND);
    }

    private static void toggle(ServerPlayer player, ComprehensionTableBlockEntity table) {
        boolean toInherit = comprehending(table.mode());
        String mode = toInherit ? ComprehensionTableBlockEntity.INHERIT : ComprehensionTableBlockEntity.COMPREHEND;
        if (!table.getItem(0).isEmpty()) {
            table.show(mode, table.text(0), table.text(1), table.text(2), table.inheritable());
            refresh(player, table);
        } else if (toInherit) {
            table.show(mode, INHERIT_FIRST, INHERIT_SECOND, INHERIT_THIRD, table.inheritable());
        } else {
            table.show(mode, COMPREHEND_FIRST, COMPREHEND_SECOND, " ", table.inheritable());
        }
    }

    /** 刷新说明：参悟模式显示所需境界、参悟次数与下次消耗的寿元；继承模式显示可继承经验、品阶差与预计升到的层数。 */
    private static void refresh(ServerPlayer player, ComprehensionTableBlockEntity table) {
        String mode = table.mode();
        Optional<TechniqueBook> found = Techniques.book(table.getItem(0));
        if (table.getItem(0).isEmpty()) {
            if (comprehending(mode)) {
                table.show(mode, COMPREHEND_FIRST, COMPREHEND_SECOND, " ", table.inheritable());
            } else {
                table.show(mode, INHERIT_FIRST, INHERIT_SECOND, INHERIT_THIRD, table.inheritable());
            }
            return;
        }
        TechniqueBook book = found.orElseThrow();
        if (mode.equals(ComprehensionTableBlockEntity.COMPREHEND)) {
            table.show(mode, "当前功法需要达到" + book.definition().levelText() + "才可修习。",
                    "当前参悟次数:§3" + WHOLE.format(book.comprehensions()),
                    "下次参悟需要消耗寿元:§3" + WHOLE.format(lifetimeCost(book)), table.inheritable());
            return;
        }
        if (Techniques.slot(player).isEmpty()) {
            table.show(mode, INHERIT_FIRST, "请先把想要升级的功法装备在身上。", " ", table.inheritable());
            return;
        }
        Optional<TechniqueBook> equipped = Techniques.main(player).filter(main -> main.ownedBy(player.getUUID()));
        if (equipped.isEmpty()) {
            table.show(mode, INHERIT_FIRST, "您当前装备的功法不是您习得的。", " ", table.inheritable());
            return;
        }
        double gradeGap = equipped.get().definition().grade() - book.definition().grade();
        double inheritable = inheritableExp(player, book);
        table.show(mode, INHERIT_FIRST, "可以继承的经验总值及品阶差值:§3" + WHOLE.format(inheritable) + "(" + WHOLE.format(gradeGap) + ")",
                preview(equipped.get(), inheritable), inheritable);
    }

    /** 下次参悟消耗的寿元 = 参悟次数² × 品阶 + 20（来源 {@code Canwutaicanwugongshi}）。 */
    static double lifetimeCost(TechniqueBook book) {
        return Math.pow(book.comprehensions(), 2) * book.definition().grade() + 20;
    }

    /**
     * 可继承经验（来源 {@code Canwutaijingyangongshi}）：书在 1 至当前层之前各层的升级所需经验之和加当前经验，
     * 乘 0.5 +（书的品阶 − 功法栏书的品阶）× 0.02。
     */
    static double inheritableExp(ServerPlayer player, TechniqueBook book) {
        double total = 0;
        double grade = book.definition().grade();
        for (double level = 1; level <= book.level(); level++) {
            total += level == book.level() ? book.exp() : Math.pow(level, 2) * grade * 10;
        }
        double equippedGrade = Techniques.main(player).map(main -> main.definition().grade()).orElse(0.0);
        return total * (0.5 + (grade - equippedGrade) * 0.02);
    }

    /** 预计升到的层数（来源 {@code Canwutaidengjiwenben}）；可继承经验不足 1 时为空。 */
    private static String preview(TechniqueBook equipped, double inheritable) {
        if (inheritable < 1) {
            return "";
        }
        double level = equipped.level();
        double exp = equipped.exp();
        double remaining = inheritable;
        while (remaining > 0) {
            double need = Math.pow(level, 2) * equipped.definition().grade() * 10;
            if (exp + remaining < need) {
                break;
            }
            remaining -= need - exp;
            level++;
            exp = 0;
        }
        return equipped.level() >= level ? "预计无法完成一次升级。" : "预计您装备的功法可以升级到§3" + WHOLE.format(level) + "重";
    }

    private static void start(ServerPlayer player, ComprehensionTableBlockEntity table) {
        if (table.mode().equals(ComprehensionTableBlockEntity.COMPREHEND)) {
            comprehend(player, table);
        } else {
            inherit(player, table);
        }
    }

    private static void comprehend(ServerPlayer player, ComprehensionTableBlockEntity table) {
        ItemStack stack = table.getItem(0);
        if (stack.isEmpty()) {
            message(player, "§4请把您想要学习，但碍于等级限制，暂时无法学习的功法，放入上方的物品槽中。");
            return;
        }
        TechniqueBook book = Techniques.book(stack).orElseThrow();
        if (book.comprehender().filter(id -> !id.equals(player.getUUID())).isPresent()) {
            message(player, "§4您无法参悟别人参悟过的功法。");
            return;
        }
        double cost = lifetimeCost(book);
        if (Cultivation.get(player, CultivationValue.LIFETIME) <= cost) {
            message(player, "§4您的寿元无多，无法支持您完整参悟一次了。");
            return;
        }
        TechniqueDefinition definition = book.definition();
        if (definition.level() <= 1) {
            message(player, "§4此功法已无法再进行参悟了！");
            return;
        }
        Cultivation.add(player, CultivationValue.LIFETIME, -cost);
        double level = definition.level() - 1;
        TechniqueDefinition lowered = new TechniqueDefinition(definition.item(), definition.name(), definition.gradeText(), definition.grade(),
                Cultivation.realmName((int) level), level, definition.body(), definition.intro(), definition.elements(), definition.tiers());
        // 来源把槽内物品写回为数量 1 的副本。
        ItemStack result = stack.copyWithCount(1);
        result.set(GongfaData.TECHNIQUE.get(), book.withComprehension(lowered, book.comprehensions() + 1, player.getUUID()));
        table.setItem(0, result);
        message(player, "§6成功参悟！");
        refresh(player, table);
    }

    private static void inherit(ServerPlayer player, ComprehensionTableBlockEntity table) {
        ItemStack stack = table.getItem(0);
        if (stack.isEmpty()) {
            message(player, "§4请把您想要舍弃的功法，放入上方的物品槽中，它将转换为您装备的功法的养料。");
            return;
        }
        TechniqueBook book = Techniques.book(stack).orElseThrow();
        if (book.owner().isEmpty()) {
            message(player, "§4无人学习过的功法不能被消耗。");
            return;
        }
        if (!book.ownedBy(player.getUUID())) {
            message(player, "§4您无法消耗别人习得的功法。");
            return;
        }
        if (Techniques.slot(player).isEmpty()) {
            message(player, "§4请先装备一本功法。");
            return;
        }
        Optional<TechniqueBook> found = Techniques.main(player).filter(main -> main.ownedBy(player.getUUID()));
        if (found.isEmpty()) {
            message(player, "§4您装备的功法不是您自己习得的。");
            return;
        }
        TechniqueBook equipped = found.get();
        if (equipped.level() >= Cultivation.get(player, CultivationValue.LEVEL_NUMBER)) {
            message(player, "§4功法层数无法超过您的境界！");
            return;
        }
        double remaining = inheritableExp(player, book);
        double level = equipped.level();
        double exp = equipped.exp();
        boolean levelled = false;
        while (remaining > 0) {
            double need = Math.pow(level, 2) * equipped.definition().grade() * 10;
            if (exp + remaining >= need) {
                remaining -= need - exp;
                level++;
                exp = 0;
                levelled = true;
            } else {
                exp += remaining;
                break;
            }
        }
        TechniqueBook updated = equipped.withProgress(level, exp);
        ItemStack slot = Techniques.slot(player).copy();
        slot.set(GongfaData.TECHNIQUE.get(), levelled ? TechniqueRules.grown(updated) : updated);
        TechniqueRules.setSlot(player, slot);
        if (levelled) {
            table.setItem(0, ItemStack.EMPTY);
        }
        message(player, "§6成功继承！");
        refresh(player, table);
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
