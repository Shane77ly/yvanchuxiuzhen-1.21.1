package net.yvanchuxiuzhen.system.gongfa;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStudiedEvent;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/**
 * 功法的研习、功法栏与成长规则。功法栏变化或栏内的书升级后，把主修功法信息提交给 xiulian，
 * 把加成与五行、层数作为加成层贡献 {@value #SOURCE} 提交给 attribute：十三项加成只在本人研习时计入，五行与层数不检查研习者
 * （来源 {@code Shuxingjisuan}、{@code Shuxingjisuan2}）。
 */
public final class TechniqueRules {
    public static final String SOURCE = "gongfa:main";
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    /** 五行值与 attribute 的功法五行项。 */
    private static final Map<String, Stat> ELEMENT_STATS = Map.of("gold", Stat.GONGFA_GOLD, "water", Stat.GONGFA_WATER,
            "tree", Stat.GONGFA_TREE, "fire", Stat.GONGFA_FIRE, "soil", Stat.GONGFA_SOIL);
    /** 灵根要求中的字与对应条件、不满足时的提示，按来源的判定次序；任一满足即可研习，提示取最后一个不满足的。 */
    private static final List<Requirement> REQUIREMENTS = List.of(
            new Requirement("金", "§4需要金灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_GOLD) >= 1),
            new Requirement("水", "§4需要水灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_WATER) >= 1),
            new Requirement("木", "§4需要木灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_TREE) >= 1),
            new Requirement("火", "§4需要火灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_FIRE) >= 1),
            new Requirement("土", "§4需要土灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_SOIL) >= 1),
            new Requirement("阴", "§4需要体质为阴！", player -> Cultivation.get(player, CultivationValue.YINYANG) <= -1),
            new Requirement("阳", "§4需要体质为阳！", player -> Cultivation.get(player, CultivationValue.YINYANG) >= 1),
            new Requirement("功", "§4需要功德！", player -> Cultivation.get(player, CultivationValue.MERITS) >= 1),
            new Requirement("业", "§4需要业力！", player -> Cultivation.get(player, CultivationValue.MERITS) <= -1),
            new Requirement("罡", "§4需要罡气！", player -> Cultivation.get(player, CultivationValue.VIGOROUS) >= 1),
            new Requirement("煞", "§4需要煞气！", player -> Cultivation.get(player, CultivationValue.VIGOROUS) <= -1));

    private record Requirement(String mark, String hint, Predicate<ServerPlayer> met) {
    }

    private TechniqueRules() {
    }

    /**
     * 研习功法书（来源 {@code BookRightClick} 的研习分支）：已被本人或他人研习时只提示；境界不低于书的要求且满足灵根要求时，
     * 书记下研习者、层数为 1 并按档位算出十三项加成，关闭当前界面，提示研习成功并发出 {@link TechniqueStudiedEvent}。
     */
    public static void study(ServerPlayer player, ItemStack stack) {
        TechniqueBook book = Techniques.book(stack).orElseThrow();
        if (book.ownedBy(player.getUUID())) {
            player.displayClientMessage(Component.literal("您已学习此功法！"), false);
            return;
        }
        if (book.owner().isPresent()) {
            player.displayClientMessage(Component.literal("此功法已被别人习得。"), false);
            return;
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) < book.definition().level()) {
            player.displayClientMessage(Component.literal("§4需要至少达到" + book.definition().levelText() + "境界才可研习。"), false);
            return;
        }
        String body = book.definition().body();
        boolean allowed = body.isEmpty() || body.contains("无");
        String hint = "";
        if (!allowed && body.contains("五")) {
            allowed = hasAllRoots(player);
            hint = allowed ? "" : "§4需要五行灵根！";
        } else if (!allowed) {
            for (Requirement requirement : REQUIREMENTS) {
                if (body.contains(requirement.mark())) {
                    if (requirement.met().test(player)) {
                        allowed = true;
                    } else {
                        hint = requirement.hint();
                    }
                }
            }
        }
        if (!allowed) {
            player.displayClientMessage(Component.literal(hint), false);
            return;
        }
        stack.set(GongfaData.TECHNIQUE.get(), grown(book.withOwner(player.getUUID(), player.getDisplayName().getString()).withProgress(1, book.exp())));
        player.displayClientMessage(Component.literal("§6研习成功！"), false);
        player.closeContainer();
        NeoForge.EVENT_BUS.post(new TechniqueStudiedEvent(player, book.definition().name()));
    }

    private static boolean hasAllRoots(ServerPlayer player) {
        return Cultivation.get(player, CultivationValue.ROOT_GOLD) >= 1 && Cultivation.get(player, CultivationValue.ROOT_WATER) >= 1
                && Cultivation.get(player, CultivationValue.ROOT_TREE) >= 1 && Cultivation.get(player, CultivationValue.ROOT_FIRE) >= 1
                && Cultivation.get(player, CultivationValue.ROOT_SOIL) >= 1;
    }

    /** 按当前层数与档位重算十三项加成（来源 {@code GongfaHealthUp} 等）。 */
    static TechniqueBook grown(TechniqueBook book) {
        List<Double> values = new ArrayList<>();
        for (TechniqueStat stat : TechniqueStat.values()) {
            values.add(stat.value(book.level(), book.definition().tier(stat)));
        }
        return book.withValues(values);
    }

    /** 放入或取出功法栏的书，随后提交主修功法信息与属性贡献。 */
    public static void setSlot(ServerPlayer player, ItemStack stack) {
        player.setData(GongfaData.SLOT, stack.copy());
        submit(player);
    }

    /** 把功法栏的书提交给 xiulian（主修功法信息）与 attribute（加成层贡献）；栏空或不是功法书时清除。 */
    public static void submit(ServerPlayer player) {
        ItemStack stack = Techniques.slot(player);
        Optional<TechniqueBook> found = Techniques.book(stack);
        if (found.isEmpty()) {
            Cultivation.setMainTechnique(player, stack.isEmpty() ? MainTechnique.NONE : new MainTechnique(true, false, "", "", 0, 0));
            PlayerAttributes.clearContribution(player, SOURCE);
            return;
        }
        TechniqueBook book = found.get();
        boolean owned = book.ownedBy(player.getUUID());
        Cultivation.setMainTechnique(player, new MainTechnique(true, owned, book.definition().name(), book.definition().body(),
                book.definition().grade(), book.level()));
        Map<Stat, Double> values = new EnumMap<>(Stat.class);
        if (owned) {
            for (TechniqueStat stat : TechniqueStat.values()) {
                values.put(stat.stat(), book.value(stat));
            }
        }
        ELEMENT_STATS.forEach((element, stat) -> values.put(stat, book.definition().element(element)));
        values.put(Stat.GONGFA_LEVEL, book.level());
        PlayerAttributes.setContribution(player, SOURCE, new Contribution(Layer.BONUS, values));
    }

    /**
     * 给功法栏的书加经验：层数低于境界层数 + {@code LevelNumberCorrect} 且低于 40 时，经验满所需值即升一层（可连升），
     * 每升一层提示一次；升级后重算十三项加成并重新提交。
     */
    static void addExperience(ServerPlayer player, double experience) {
        TechniqueBook book = Techniques.main(player).orElseThrow();
        double cap = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect");
        if (book.level() >= cap || book.level() >= 40) {
            return;
        }
        double level = book.level();
        double exp = book.exp() + experience;
        boolean levelled = false;
        double expMax = Math.pow(level, 2) * book.definition().grade() * 10;
        while (exp >= expMax) {
            level++;
            exp -= expMax;
            expMax = Math.pow(level, 2) * book.definition().grade() * 10;
            player.displayClientMessage(Component.literal("§6功法升级到" + WHOLE.format(level) + "重天"), false);
            levelled = true;
        }
        TechniqueBook updated = book.withProgress(level, exp);
        ItemStack stack = Techniques.slot(player).copy();
        stack.set(GongfaData.TECHNIQUE.get(), levelled ? grown(updated) : updated);
        setSlot(player, stack);
    }

    /** 闭关修炼每天吸纳元气后，主修功法得经验 = 吸纳的元气 × 1.5（来源 {@code Kaishixiulian}，不检查研习者）。 */
    static void onCultivationExperience(ServerPlayer player, double experience) {
        if (Techniques.main(player).isPresent()) {
            addExperience(player, experience * 1.5);
        }
    }

    /**
     * 功法修炼（来源 {@code Kaishigongfa}，{@code xiuliankaiguan} 为“功法”时每秒一次）：闭关天数至少 1 时，骑乘中且功法栏有书才进行，
     * 每次消耗 1 点饥饿（饿时虚弱 30 秒并结束闭关，本次仍结算）；书须本人研习、层数低于境界层数，罡煞向 0 靠 1，天数与寿元各减 1，
     * 得功法经验 品阶 ×（层数 + 1）× 2.5。不满足时提示并结束闭关。
     */
    static void train(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) < 1) {
            return;
        }
        ItemStack stack = Techniques.slot(player);
        if (stack.isEmpty() || !player.isPassenger()) {
            stop(player, "结束研习");
            return;
        }
        if (player.getFoodData().getFoodLevel() >= 1) {
            player.causeFoodExhaustion(1);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
            stop(player, "太饿了，无法继续修炼。");
        }
        Optional<TechniqueBook> found = Techniques.book(stack);
        if (found.isEmpty() || !found.get().ownedBy(player.getUUID())) {
            stop(player, "不是我的功法，直接拒绝");
            return;
        }
        TechniqueBook book = found.get();
        if (book.level() >= Cultivation.get(player, CultivationValue.LEVEL_NUMBER)) {
            stop(player, "功法层级无法高于您的境界，请提升境界后再修炼功法。");
            return;
        }
        double vigorous = Cultivation.get(player, CultivationValue.VIGOROUS);
        if (vigorous >= 1) {
            Cultivation.add(player, CultivationValue.VIGOROUS, -1);
        } else if (vigorous <= -1) {
            Cultivation.add(player, CultivationValue.VIGOROUS, 1);
        }
        Cultivation.add(player, CultivationValue.CULTIVATION_DAYS, -1);
        Cultivation.add(player, CultivationValue.LIFETIME, -1);
        double experience = book.definition().grade() * (book.level() + 1) * 2.5;
        player.displayClientMessage(Component.literal("§f距离出关" + WHOLE.format(Cultivation.get(player, CultivationValue.CULTIVATION_DAYS))
                + "天（§b功法经验获得" + WHOLE.format(experience) + "点§f）"), true);
        addExperience(player, experience);
    }

    private static void stop(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), false);
        Cultivation.setTizhiString(player, "xiuliankaiguan", "");
        Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 0);
    }
}
