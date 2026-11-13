package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.BookReadingText;
import net.yvanchuxiuzhen.system.book.client.BookReadingScreen;
import net.yvanchuxiuzhen.system.book.content.BookReadingMenu;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.data.SkillData;

/** 公共书自然阅读检查覆盖文本行持久化、阅读分派、无槽库存和双端显示。 */
final class BookReadingCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private ItemStack technique;
    private int selected;
    private boolean sneaking;
    private boolean shift;
    private boolean hadAnswers;
    private double answers;

    BookReadingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("书籍阅读", report, levelId);
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(8, player -> {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
            technique = Techniques.slot(player).copy();
            selected = player.getInventory().selected;
            sneaking = player.isShiftKeyDown();
            shift = minecraft.options.keyShift.isDown();
            hadAnswers = player.getPersistentData().contains("daduishuliang");
            answers = player.getPersistentData().getDouble("daduishuliang");
            player.getPersistentData().putDouble("daduishuliang", 5);
            player.getInventory().clearContent();
            Techniques.setSlot(player, ItemStack.EMPTY);
            player.setShiftKeyDown(false);
            minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
            CompoundTag data = new CompoundTag();
            data.putString("textdisplay", "甲".repeat(22) + "乙".repeat(22) + "丙");
            data.putString("TreasureTypeBig", "书架");
            data.putString("TreasureTypeSmall", "技能书");
            data.putDouble("TreasureLEVEL", 1);
            data.putString("text11", "旧行");
            data.putString("text12", "旧十二行");
            stage(player, dataBook("book_020", data).copyWithCount(2));
        }));
        actions.add(new Action(10, player -> use()));
        actions.add(new Action(8, player -> {
            if (!checkMenu(player, "文本书自然右键打开中文阅读页且不消费整栈")) return;
            CompoundTag data = data(player.getMainHandItem());
            expect("每二十二字符切第一行", "甲".repeat(22), data.getString("text01"));
            expect("每二十二字符切第二行", "乙".repeat(22), data.getString("text02"));
            expect("尾行与十一行清理", List.of("丙", "", "旧十二行"),
                    List.of(data.getString("text03"), data.getString("text11"), data.getString("text12")));
            expect("文本书整栈数量", 2, player.getMainHandItem().getCount());
            minecraft.execute(() -> screenshots.take("book_reading_text.png"));
        }));
        actions.add(new Action(5, player -> {
            CompoundTag data = new CompoundTag();
            data.putString("textdisplay", "甲".repeat(242) + "尾行");
            stage(player, dataBook("book_032", data));
        }));
        actions.add(new Action(8, player -> use()));
        actions.add(new Action(5, player -> {
            if (!checkMenu(player, "长文本仍只有十一可见行")) return;
            expect("第十二行照来源持久化", "尾行", data(player.getMainHandItem()).getString("text12"));
            expect("可见行数", 11, BookReadingText.lines(player).size());
            stage(player, techniqueBook());
        }));
        actions.add(new Action(8, player -> use()));
        actions.add(new Action(5, player -> {
            if (!checkMenu(player, "未研习功法普通阅读")) return;
            List<String> lines = BookReadingText.lines(player);
            expect("未研习功法需求与隐藏加成", List.of("需要五行俱全才可研习。", "需要达到筑基一阶境界才可研习。", ""),
                    List.of(lines.get(2), lines.get(3), lines.get(4)));
            ItemStack owned = player.getMainHandItem().copy();
            TechniqueBook book = Techniques.book(owned).orElseThrow().withOwner(player.getUUID(), player.getDisplayName().getString())
                    .withProgress(2, 7).withValues(Collections.nCopies(TechniqueStat.values().length, 1.0));
            owned.set(GongfaData.TECHNIQUE.get(), book);
            stage(player, owned);
        }));
        actions.add(new Action(8, player -> use()));
        actions.add(new Action(8, player -> {
            if (!checkMenu(player, "已研习功法普通阅读")) return;
            List<String> lines = BookReadingText.lines(player);
            expect("功法等级经验显示", "修炼功法等级：2（7/40）", lines.get(2));
            expect("阅读页空主修仍按来源比较档位", "§4血量上限增加：1.0§4（血量恢复增加：1.0）", lines.get(3));
            expect("移动速度末行", "§4移动速度增加：1.0", lines.get(9));
            minecraft.execute(() -> screenshots.take("book_reading_technique.png"));
        }));
        for (SkillKind kind : SkillKind.values()) {
            actions.add(new Action(5, player -> stage(player, skillBook(kind, player))));
            actions.add(new Action(8, player -> use()));
            actions.add(new Action(8, player -> {
                if (!checkMenu(player, "已研习" + kind + "普通阅读")) return;
                List<String> lines = BookReadingText.lines(player);
                expect("技能简介 " + kind, "阅读检查简介", lines.get(2));
                if (kind == SkillKind.SPELL) {
                    expect("法术伤害显示", "基础法术伤害：2.0（伤害倍率：3.0）", lines.get(3));
                    expect("正负冷却按原序显示", "技能冷却： §4金+2秒 §a水-3秒", lines.get(5));
                    expect("多项冷却豁免保留最后项", "阴阳道法冷却时间在6秒内时，可以正常释放技能。", lines.get(6));
                    minecraft.execute(() -> screenshots.take("book_reading_spell.png"));
                }
            }));
        }
        actions.add(new Action(5, player -> {
            CompoundTag data = new CompoundTag();
            data.putString("fabaotype", "武器");
            data.putString("tuzhiname", "青锋图纸");
            data.putString("item_level", "下品");
            data.putString("fabaoshuoming", "炼器材料说明");
            for (int slot = 0; slot < 9; slot++) {
                data.putString("cailiao0" + slot + "name", "材料" + slot);
                data.putDouble("cailiao0" + slot + "num", slot + 1);
            }
            stage(player, dataBook("book_031", data));
        }));
        actions.add(new Action(8, player -> use()));
        actions.add(new Action(8, player -> {
            if (!checkMenu(player, "图纸自然阅读")) return;
            List<String> lines = BookReadingText.lines(player);
            expect("图纸标题说明首尾材料", List.of("§6青锋图纸(下品 武器)", "§3炼器材料说明", "材料0(1)", "材料8(9)"),
                    List.of(lines.get(0), lines.get(1), lines.get(2), lines.get(10)));
            minecraft.execute(() -> screenshots.take("book_reading_blueprint.png"));
        }));
        actions.add(new Action(5, player -> {
            CompoundTag data = new CompoundTag();
            data.putString("danyaoname", "清心丹方");
            data.putString("danfangshuoming", "炼丹材料说明");
            for (int slot = 1; slot <= 8; slot++) data.putString("cailiao0" + slot + "name", "药草" + slot);
            stage(player, dataBook("book_021", data));
        }));
        actions.add(new Action(8, player -> use()));
        actions.add(new Action(8, player -> {
            if (!checkMenu(player, "丹方自然阅读")) return;
            List<String> lines = BookReadingText.lines(player);
            expect("丹方标题说明首尾药草", List.of("§6清心丹方", "§3炼丹材料说明", "药草1", "药草8", ""),
                    List.of(lines.get(0), lines.get(1), lines.get(2), lines.get(9), lines.get(10)));
            expect("阅读全程保留答题计数", 5, player.getPersistentData().getDouble("daduishuliang"));
            minecraft.execute(() -> screenshots.take("book_reading_recipe.png"));
        }));
        script(actions.toArray(Action[]::new));
    }

    private void stage(ServerPlayer player, ItemStack stack) {
        player.closeContainer();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, stack);
        player.inventoryMenu.broadcastChanges();
    }

    private void use() {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = 0;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        });
    }

    private boolean checkMenu(ServerPlayer player, String label) {
        boolean opened = player.containerMenu instanceof BookReadingMenu && minecraft.screen instanceof BookReadingScreen;
        expectTrue(label, opened);
        if (!opened) {
            cleanup(player);
            finish();
            return false;
        }
        expect("双端十一行一致", BookReadingText.lines(player), BookReadingText.lines(minecraft.player));
        expectTrue("阅读菜单无物品槽", player.containerMenu.slots.isEmpty());
        expectTrue("主手实例数量与组件同步", ItemStack.matches(player.getMainHandItem(), minecraft.player.getMainHandItem()));
        expect("菜单中文标题", "书籍阅读", minecraft.screen.getTitle().getString());
        return true;
    }

    private static ItemStack dataBook(String item, CompoundTag data) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, item)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static ItemStack techniqueBook() {
        return Techniques.createBook(new TechniqueDefinition(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_002"),
                "阅读检查功法", "黄阶一级", 1, "筑基一阶", 11, "五", "", Map.of(),
                Collections.nCopies(TechniqueStat.values().length, "中档")));
    }

    private static ItemStack skillBook(SkillKind kind, ServerPlayer player) {
        ItemStack stack = dataBook("book_006", new CompoundTag());
        stack.set(SkillData.SKILL.get(), new SkillBook(kind, "阅读检查技能", 1, 1, "无", "阅读检查简介", Optional.of(player.getUUID()),
                player.getDisplayName().getString(), Map.of("jinengmanaatt", 2.0, "jinengshanghai", 3.0, "jinengxiaohao", 4.0,
                "Goldcolddown", 2.0, "Watercolddown", -3.0, "Goldcoldignore", 5.0, "yinyangcoldignore", 6.0),
                Map.of("jinengneeditem", "剑", "smanshiptype", "剑")));
        return stack;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setShiftKeyDown(sneaking);
        Techniques.setSlot(player, technique);
        if (hadAnswers) player.getPersistentData().putDouble("daduishuliang", answers);
        else player.getPersistentData().remove("daduishuliang");
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = selected;
            minecraft.options.keyShift.setDown(shift);
        });
    }
}
