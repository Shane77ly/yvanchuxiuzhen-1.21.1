package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/**
 * 功法的实机检查：研习条件（境界、灵根）与研习后的层数和加成，放入功法栏后提交给 xiulian 与 attribute，
 * 闭关吸纳元气给功法加经验并升级，书的提示文本，书架藏宝给书，取出功法栏后清除。
 */
final class GongfaCheck extends SystemCheck {
    private TechniqueDefinition basic;
    private double healthBefore;

    GongfaCheck(List<String> report, String levelId) {
        super("功法", report, levelId);
    }

    private static TechniqueDefinition named(String name) {
        return TechniqueLibrary.all().stream().filter(definition -> definition.name().equals(name)).findFirst().orElseThrow();
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                expect("功法定义数", 67, TechniqueLibrary.all().size());
                basic = named("五行基础");
                TechniqueDefinition gold = named("分金术");
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                Cultivation.set(player, CultivationValue.ROOT_GOLD, 0);
                ItemStack goldBook = Techniques.createBook(gold);
                expect("功法书的物品", gold.item(), BuiltInRegistries.ITEM.getKey(goldBook.getItem()));
                Techniques.study(player, goldBook);
                expectTrue("境界不足不能研习", Techniques.book(goldBook).orElseThrow().owner().isEmpty());
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 5);
                Techniques.study(player, goldBook);
                expectTrue("没有金灵根不能研习", Techniques.book(goldBook).orElseThrow().owner().isEmpty());
                Cultivation.set(player, CultivationValue.ROOT_GOLD, 1);
                Techniques.study(player, goldBook);
                expectTrue("有金灵根后研习成功", Techniques.book(goldBook).orElseThrow().ownedBy(player.getUUID()));

                ItemStack book = Techniques.createBook(basic);
                Techniques.study(player, book);
                TechniqueBook studied = Techniques.book(book).orElseThrow();
                expect("研习后层数", 1.0, studied.level());
                expect("研习后生命加成 ⌈64 × 0.8⌉", 52.0, studied.value(TechniqueStat.HEALTH_MAX));
                healthBefore = PlayerAttributes.value(player, Stat.HEALTH_MAX);
                Techniques.setSlot(player, book);
                MainTechnique main = Cultivation.mainTechnique(player);
                expectTrue("提交主修功法 本人所有", main.present() && main.owned());
                expect("提交主修功法 名字", "五行基础", main.name());
                expect("提交主修功法 五行", "无", main.element());
                expect("生命上限计入功法加成", healthBefore + 52, PlayerAttributes.value(player, Stat.HEALTH_MAX));
                expect("功法层数项", 1.0, PlayerAttributes.value(player, Stat.GONGFA_LEVEL));
                NeoForge.EVENT_BUS.post(new CultivationExperienceEvent(player, 10));
                TechniqueBook grown = Techniques.main(player).orElseThrow();
                expect("闭关经验 × 1.5 后升到 2 层", 2.0, grown.level());
                expect("升级后剩余经验 15 − 10", 5.0, grown.exp());
                expect("升级后生命加成 ⌈(60 + 2 × 4) × 0.8⌉", 55.0, grown.value(TechniqueStat.HEALTH_MAX));
                expect("升级后重新提交", 2.0, Cultivation.mainTechnique(player).level());
            });
            case 1 -> {
                if (local < 10) {
                    return false;
                }
                ItemStack slot = Techniques.slot(minecraft.player);
                List<String> lines = slot.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL)
                        .stream().map(Component::getString).toList();
                expectTrue("提示文本 品阶与重天 " + lines, lines.contains("§6黄阶一级(2重天)"));
                expectTrue("提示文本 经验", lines.contains("§b(5/40)"));
                expectTrue("提示文本 生命加成", lines.stream().anyMatch(line -> line.startsWith("§f血量上限增加：55.0")));
                onServer(player -> {
                    int before = countBooks(player.getInventory().items);
                    Techniques.giveTreasureBook(player, 1);
                    expect("书架藏宝给一本功法书", before + 1, countBooks(player.getInventory().items));
                    Techniques.setSlot(player, ItemStack.EMPTY);
                    expectTrue("取出后主修功法为空", !Cultivation.mainTechnique(player).present());
                    expect("取出后生命上限复原", healthBefore, PlayerAttributes.value(player, Stat.HEALTH_MAX));
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private static int countBooks(List<ItemStack> items) {
        return items.stream().filter(stack -> Techniques.book(stack).isPresent()).mapToInt(ItemStack::getCount).sum();
    }
}
