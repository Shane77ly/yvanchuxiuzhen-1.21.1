package net.yvanchuxiuzhen.system.equipment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties.Value;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableBlockEntity;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 饰品制作台的三槽配方与根字段成品；由服务端菜单调用，完成后覆盖结果槽并播放对应类别动画。 */
public final class JewelryCrafting {
    private JewelryCrafting() {
    }

    /** 按当前类别和三个输入槽立即制作一件饰品；每槽消耗一件，动画状态不限制再次合成。 */
    public static void craft(JewelryTableBlockEntity table) {
        for (Category category : Category.values()) {
            if (!category.name.equals(table.category())) continue;
            for (Recipe recipe : category.recipes) {
                if (!matches(table, recipe)) continue;
                ItemStack result = new ItemStack(item(recipe.output));
                Map<String, Value> fields = new LinkedHashMap<>();
                fields.put("item_level", Value.text(recipe.grade));
                fields.put("item_level_num", Value.decimal(recipe.level));
                switch (category) {
                    case HEAD -> result.enchant(table.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                            .getHolderOrThrow(Enchants.key("colddown")), (int) recipe.effect);
                    case NECKLACE -> fields.put("health_Rec", Value.decimal(recipe.effect));
                    case HAND -> {
                        fields.put("power_Rec", Value.decimal(recipe.effect));
                        fields.put("soul_Rec", Value.decimal(recipe.effect));
                    }
                    case BELT -> {
                        fields.put("power_Max", Value.decimal(recipe.effect));
                        fields.put("soul_Max", Value.decimal(recipe.effect));
                    }
                }
                fields.put("tuzhiname", Value.text(result.getDisplayName().getString()));
                fields.put("fabaotype", Value.text(category.type));
                fields.put("fabaoid", Value.text(BuiltInRegistries.ITEM.getKey(result.getItem()).toString()));
                fields.put("fabaoshuoming", Value.text(category.description));
                result.set(EquipmentData.PROPERTIES.get(), new EquipmentProperties(fields));
                table.setItem(3, result);
                for (int slot = 0; slot < 3; slot++) {
                    ItemStack input = table.getItem(slot).copy();
                    input.shrink(1);
                    table.setItem(slot, input);
                }
                table.playAnimation(category.animation);
                return;
            }
            return;
        }
    }

    /** 向玩家聊天栏发送指定类别的九条配方；编号1–4依次为头簪、项坠、手镯、腰佩。 */
    public static void hints(ServerPlayer player, int category) {
        for (Category entry : Category.values()) {
            if (entry.animation != category) continue;
            for (String hint : entry.hints) {
                player.displayClientMessage(Component.literal(hint), false);
            }
            return;
        }
    }

    private static boolean matches(JewelryTableBlockEntity table, Recipe recipe) {
        return table.getItem(0).is(item(recipe.first))
                && table.getItem(1).is(item(recipe.second))
                && table.getItem(2).is(item(recipe.third));
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 品质数值与效果值均为来源根字段的小数类型，品质文字独立于数值。 */
    private record Recipe(String output, String first, String second, String third, String grade, double level, double effect) {
    }

    private enum Category {
        HEAD("头簪", "头饰", "头饰类饰品，一般注重冷却缩减。", 1, List.of(
                new Recipe("headornaments_01", "cailiao_088", "cailiao_191", "cailiao_161", "黄阶四级", 4, 1),
                new Recipe("headornaments_02", "cailiao_202", "cailiao_192", "cailiao_162", "黄阶九级", 9, 2),
                new Recipe("headornaments_03", "cailiao_203", "cailiao_193", "cailiao_098", "玄阶四级", 14, 3),
                new Recipe("headornaments_04", "cailiao_204", "cailiao_194", "cailiao_106", "玄阶九级", 19, 4),
                new Recipe("headornaments_05", "cailiao_205", "cailiao_195", "cailiao_111", "地阶四级", 24, 5),
                new Recipe("headornaments_06", "cailiao_206", "cailiao_196", "cailiao_117", "地阶九级", 29, 6),
                new Recipe("headornaments_07", "cailiao_207", "cailiao_197", "cailiao_128", "天阶四级", 34, 7),
                new Recipe("headornaments_08", "cailiao_208", "cailiao_198", "cailiao_137", "天阶九级", 39, 8),
                new Recipe("headornaments_09", "cailiao_209", "cailiao_199", "cailiao_169", "超品", 44, 9)), List.of(
                "木雕头簪:花枝+一级灵石+玉石",
                "烁光头簪:精钢锭+二级灵石+珲黄玉",
                "镶玉头簪:霜铁锭+三级灵石+冰钻",
                "梅花琉璃簪:霜金锭+四级灵石+神鸟玉",
                "八宝翡翠簪:寒潭精铁+五级灵石+驼峰玉",
                "金丝明珠簪:黑曜合金+六级灵石+无忧玉",
                "双衔齐飞簪:湛蓝精金+七级灵石+波涛石",
                "晶玉华光簪:午银+八级灵石+玄阴珠",
                "鎏金镶翅簪:辉耀庚金+九级灵石+道合墨玉")),
        NECKLACE("项坠", "项坠", "项坠类饰品，一般注重血量恢复速度。", 2, List.of(
                new Recipe("necklaceornaments_01", "cailiao_201", "cailiao_191", "cailiao_181", "黄阶四级", 4, 1),
                new Recipe("necklaceornaments_02", "cailiao_202", "cailiao_192", "cailiao_182", "黄阶九级", 9, 2),
                new Recipe("necklaceornaments_03", "cailiao_203", "cailiao_193", "cailiao_183", "玄阶四级", 14, 3),
                new Recipe("necklaceornaments_04", "cailiao_204", "cailiao_194", "cailiao_184", "玄阶九级", 19, 4),
                new Recipe("necklaceornaments_05", "cailiao_205", "cailiao_195", "cailiao_185", "地阶四级", 24, 5),
                new Recipe("necklaceornaments_06", "cailiao_206", "cailiao_196", "cailiao_186", "地阶九级", 29, 6),
                new Recipe("necklaceornaments_07", "cailiao_207", "cailiao_197", "cailiao_187", "玄阶四级", 34, 7),
                new Recipe("necklaceornaments_08", "cailiao_208", "cailiao_198", "cailiao_188", "天阶九级", 39, 8),
                new Recipe("necklaceornaments_09", "cailiao_209", "cailiao_199", "cailiao_189", "超品", 44, 9)), List.of(
                "镂空项坠:精铁锭+一级灵石+一级妖丹",
                "衔珠项坠:精钢锭+二级灵石+二级妖丹",
                "玲珑项坠:霜铁锭+三级灵石+三级妖丹",
                "银凤镂花项坠:霜金锭+四级灵石+四级妖丹",
                "碧玉藤花项坠:寒潭精铁+五级灵石+五级妖丹",
                "宝蓝辉璀项坠:黑曜合金+六级灵石+六级妖丹",
                "镶宝玲珑项坠:湛蓝精金+七级灵石+七级妖丹",
                "赤金凤尾项坠:午银+八级灵石+八级妖丹",
                "葳蕤曳光项坠:辉耀庚金+九级灵石+九级妖丹")),
        HAND("手镯", "手饰", "手饰类饰品，一般注重灵力与精力的恢复速度。", 3, List.of(
                new Recipe("handornaments_01", "cailiao_201", "cailiao_191", "cailiao_161", "黄阶四级", 4, 2),
                new Recipe("handornaments_02", "cailiao_202", "cailiao_192", "cailiao_162", "黄阶九级", 9, 4),
                new Recipe("handornaments_03", "cailiao_203", "cailiao_193", "cailiao_102", "玄阶四级", 14, 6),
                new Recipe("handornaments_04", "cailiao_204", "cailiao_194", "cailiao_107", "玄阶九级", 19, 8),
                new Recipe("handornaments_05", "cailiao_205", "cailiao_195", "cailiao_110", "地阶四级", 24, 10),
                new Recipe("handornaments_06", "cailiao_206", "cailiao_196", "cailiao_116", "地阶九级", 29, 12),
                new Recipe("handornaments_07", "cailiao_207", "cailiao_197", "cailiao_123", "玄阶四级", 34, 14),
                new Recipe("handornaments_08", "cailiao_208", "cailiao_198", "cailiao_140", "天阶九级", 39, 16),
                new Recipe("handornaments_09", "cailiao_209", "cailiao_199", "cailiao_169", "超品", 44, 18)), List.of(
                "美玉手镯:精铁锭+一级灵石+玉石",
                "繁花手镯:精钢锭+二级灵石+珲黄玉",
                "碎霜手镯:霜铁锭+三级灵石+熔晶石",
                "慧心静气镯:霜金锭+四级灵石+辰星石",
                "菱花嵌翡镯:寒潭精铁+五级灵石+太阳晶石",
                "鹊翎金雕镯:黑曜合金+六级灵石+霜霖珠",
                "宝玉雕光镯:湛蓝精金+七级灵石+麒麟纹珠",
                "玲花镶金镯:午银+八级灵石+冥神珠",
                "凤翅金鳞镯:辉耀庚金+九级灵石+道合墨玉")),
        BELT("腰佩", "腰饰", "腰饰类饰品，一般注重增加灵力与精力的上限。", 4, List.of(
                new Recipe("beltornaments_01", "cailiao_171", "cailiao_191", "cailiao_161", "黄阶四级", 4, 80),
                new Recipe("beltornaments_02", "cailiao_172", "cailiao_192", "cailiao_162", "黄阶九级", 9, 160),
                new Recipe("beltornaments_03", "cailiao_173", "cailiao_193", "cailiao_163", "玄阶四级", 14, 240),
                new Recipe("beltornaments_04", "cailiao_174", "cailiao_194", "cailiao_164", "玄阶九级", 19, 320),
                new Recipe("beltornaments_05", "cailiao_175", "cailiao_195", "cailiao_165", "地阶四级", 24, 400),
                new Recipe("beltornaments_06", "cailiao_176", "cailiao_196", "cailiao_166", "地阶九级", 29, 480),
                new Recipe("beltornaments_07", "cailiao_177", "cailiao_197", "cailiao_167", "玄阶四级", 34, 560),
                new Recipe("beltornaments_08", "cailiao_178", "cailiao_198", "cailiao_168", "天阶九级", 39, 640),
                new Recipe("beltornaments_09", "cailiao_179", "cailiao_199", "cailiao_169", "超品", 44, 720)), List.of(
                "青云佩:灵布+一级灵石+玉石",
                "双兽纹玉佩:韧麻+二级灵石+珲黄玉",
                "鸳鸯同心如意佩:倚罗布+三级灵石+翠玉",
                "青玉镂雕牡丹佩:妆花缎+四级灵石+宝华玉",
                "碧玉滕花佩:龙绡缎+五级灵石+滴松玉",
                "白玉螭纹佩:牵花丝绸+六级灵石+葳蕤白玉",
                "冰花芙蓉佩:金丝绒绸+七级灵石+琼涟玉",
                "龙纹祥云佩:浣花绫+八级灵石+璇音美玉",
                "墨玉镶珠龙纹佩:千丝锦+九级灵石+道合墨玉"));

        private final String name;
        private final String type;
        private final String description;
        private final int animation;
        private final List<Recipe> recipes;
        private final List<String> hints;

        Category(String name, String type, String description, int animation, List<Recipe> recipes, List<String> hints) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.animation = animation;
            this.recipes = recipes;
            this.hints = hints;
        }
    }
}
