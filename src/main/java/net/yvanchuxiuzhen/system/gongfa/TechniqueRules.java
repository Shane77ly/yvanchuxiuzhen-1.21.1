package net.yvanchuxiuzhen.system.gongfa;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;
import net.yvanchuxiuzhen.system.xiulian.api.RootRequirement;

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
    private TechniqueRules() {
    }

    /**
     * 研习功法书（来源 {@code BookRightClick} 的研习分支）：已被本人或他人研习时只提示；境界不低于书的要求且满足灵根要求时，
     * 书记下研习者、层数为 1 并按档位算出十三项加成，关闭当前界面，提示研习成功并发出 {@link TechniqueStudiedEvent}。
     */
    public static void study(ServerPlayer player, ItemStack stack) {
        TechniqueBook book = Techniques.book(stack).orElseThrow();
        if (book.ownedBy(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.study.already_owned"), false);
            return;
        }
        if (book.owner().isPresent()) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.study.owned_by_other"), false);
            return;
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) < book.definition().level()) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.study.level_required",
                    book.definition().levelText()).withStyle(ChatFormatting.DARK_RED), false);
            return;
        }
        Optional<String> unmet = RootRequirement.unmet(player, book.definition().body());
        if (unmet.isPresent()) {
            player.displayClientMessage(Component.literal(unmet.get()), false);
            return;
        }
        player.serverLevel().sendParticles(Particles.type("tiangandizhi"), player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.serverLevel().sendParticles(Particles.type("tiangandizhi_2"), player.getX(), player.getY(), player.getZ(), 20, 2, 0, 2, 0);
        player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
        bindBook(player, stack, book, true);
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.study.success")
                .withStyle(ChatFormatting.GOLD), false);
        player.closeContainer();
        NeoForge.EVENT_BUS.post(new TechniqueStudiedEvent(player, book.definition().name()));
    }

    /** 来源答满重用归属与初始加成写入，不执行普通研习的条件、粒子、提示与附赠技能事件。 */
    public static void completeQuiz(ServerPlayer player, ItemStack stack) {
        TechniqueBook book = Techniques.book(stack).orElseThrow();
        bindBook(player, stack, book, !book.definition().name().isEmpty());
    }

    private static void bindBook(ServerPlayer player, ItemStack stack, TechniqueBook book, boolean initialize) {
        TechniqueBook owned = book.withOwner(player.getUUID(), player.getDisplayName().getString());
        stack.set(GongfaData.TECHNIQUE.get(), initialize ? grown(owned.withProgress(1, book.exp())) : owned);
    }

    /** 按当前层数与档位重算十三项加成（来源 {@code GongfaHealthUp} 等）。 */
    static TechniqueBook grown(TechniqueBook book) {
        List<Double> values = new ArrayList<>();
        for (TechniqueStat stat : TechniqueStat.values()) {
            values.add(stat.value(book.level(), book.definition().tier(stat)));
        }
        return book.withValues(values);
    }

    /** 放入或取出功法栏的书，保留来源交接的物品实例，再提交主修功法与属性贡献。 */
    public static void setSlot(ServerPlayer player, ItemStack stack) {
        player.setData(GongfaData.SLOT, stack);
        submit(player);
    }

    /** 装配页载入后将当前功法原栈记入每秒比较槽。 */
    public static void rememberSlot(ServerPlayer player) {
        player.setData(GongfaData.PREVIOUS_SLOT, Techniques.slot(player));
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

    /** 来源每秒恢复后只比较功法物品与名称，变化时执行一次完整重算并记下当前栏；经验或层数变化不进入此分支。 */
    static void refreshEquipped(ServerPlayer player) {
        ItemStack current = Techniques.slot(player);
        ItemStack previous = player.getData(GongfaData.PREVIOUS_SLOT);
        String currentName = Techniques.book(current).map(book -> book.definition().name()).orElse("");
        String previousName = Techniques.book(previous).map(book -> book.definition().name()).orElse("");
        if (current.getItem() != previous.getItem() || !currentName.equals(previousName)) {
            PlayerAttributes.recalculate(player);
            player.setData(GongfaData.PREVIOUS_SLOT, current);
        }
    }

    /** 给符合各自入口条件的功法书加经验；满所需值可连升，升级后重算十三项加成并提交。 */
    static boolean addExperience(ServerPlayer player, double experience) {
        TechniqueBook book = Techniques.main(player).orElseThrow();
        double level = book.level();
        double exp = book.exp() + experience;
        boolean levelled = false;
        double expMax = Math.pow(level, 2) * book.definition().grade() * 10;
        while (exp >= expMax) {
            level++;
            exp -= expMax;
            expMax = Math.pow(level, 2) * book.definition().grade() * 10;
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.training.level_up",
                    WHOLE.format(level)).withStyle(ChatFormatting.GOLD), false);
            levelled = true;
        }
        TechniqueBook updated = book.withProgress(level, exp);
        ItemStack stack = Techniques.slot(player);
        stack.set(GongfaData.TECHNIQUE.get(), levelled ? grown(updated) : updated);
        player.setData(GongfaData.SLOT, stack);
        submit(player);
        return levelled;
    }

    /** 闭关吸纳元气时按境界修正和四十层限制给功法经验，不检查研习者。 */
    static void onCultivationExperience(ServerPlayer player, double experience) {
        Optional<TechniqueBook> found = Techniques.main(player);
        double cap = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect");
        if (found.isPresent() && found.get().level() < cap && found.get().level() < 40) {
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
            stop(player, "message.yvanchuxiuzhen.gongfa.training.stopped");
            return;
        }
        if (player.getFoodData().getFoodLevel() >= 1) {
            player.causeFoodExhaustion(1);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
            stop(player, "message.yvanchuxiuzhen.gongfa.training.hungry");
        }
        Optional<TechniqueBook> found = Techniques.book(stack);
        if (found.isEmpty() || !found.get().ownedBy(player.getUUID())) {
            stop(player, "message.yvanchuxiuzhen.gongfa.training.not_owned");
            return;
        }
        TechniqueBook book = found.get();
        if (book.level() >= Cultivation.get(player, CultivationValue.LEVEL_NUMBER)) {
            stop(player, "message.yvanchuxiuzhen.gongfa.training.realm_limit");
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
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.gongfa.training.progress",
                WHOLE.format(Cultivation.get(player, CultivationValue.CULTIVATION_DAYS)), WHOLE.format(experience)), true);
        double cap = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect");
        if (book.level() < cap && book.level() < 40 && addExperience(player, experience)) {
            PlayerAttributes.recalculate(player);
        }
    }

    private static void stop(ServerPlayer player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), false);
        Cultivation.setTizhiString(player, "xiuliankaiguan", "");
        Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 0);
    }
}
