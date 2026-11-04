package net.yvanchuxiuzhen.system.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.data.SkillData;

/**
 * 技能书的生成：命令参数（来源 {@code JinengGO}、{@code SmanshipGO}、{@code Shenfazhiling}、{@code FlyskillGO}）、
 * 藏宝技能书（来源 {@code Jinenglistzong}、{@code Jinengget01}、{@code Jinengget02}，数据 {@code content/skill/treasure_books.json}）、
 * 研习功法附赠的技能（数据 {@code content/skill/studied_bonuses.json}）。
 */
public final class SkillLibrary {
    /** 藏宝技能书的随机数范围：1–13。 */
    private static final int TREASURE_ROLLS = 13;

    private record Treasure(int roll, ResourceLocation item, String name, double grade, double level, String body, String intro, String needed,
                            Map<String, Double> numbers) {
        static final Codec<Treasure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("roll").forGetter(Treasure::roll),
                ResourceLocation.CODEC.fieldOf("item").forGetter(Treasure::item),
                Codec.STRING.fieldOf("name").forGetter(Treasure::name),
                Codec.DOUBLE.fieldOf("grade").forGetter(Treasure::grade),
                Codec.DOUBLE.fieldOf("level").forGetter(Treasure::level),
                Codec.STRING.fieldOf("body").forGetter(Treasure::body),
                Codec.STRING.fieldOf("intro").forGetter(Treasure::intro),
                Codec.STRING.fieldOf("needed").forGetter(Treasure::needed),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("numbers").forGetter(Treasure::numbers)
        ).apply(instance, Treasure::new));
    }

    private record Bonus(String technique, String arguments) {
        static final Codec<Bonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("technique").forGetter(Bonus::technique),
                Codec.STRING.fieldOf("arguments").forGetter(Bonus::arguments)
        ).apply(instance, Bonus::new));
    }

    private static final Map<Integer, Treasure> TREASURES = new HashMap<>();
    private static final Map<String, String> BONUSES = new HashMap<>();

    static {
        for (Treasure row : ContentFiles.read("content/skill/treasure_books.json", Treasure.CODEC)) {
            TREASURES.put(row.roll(), row);
        }
        for (Bonus row : ContentFiles.read("content/skill/studied_bonuses.json", Bonus.CODEC)) {
            BONUSES.put(row.technique(), row.arguments());
        }
    }

    private SkillLibrary() {
    }

    /** 藏宝技能书：在 1–13 取随机数，对应一本未研习的技能书。 */
    public static ItemStack treasure(RandomSource random) {
        Treasure row = TREASURES.get(Mth.nextInt(random, 1, TREASURE_ROLLS));
        Item item = BuiltInRegistries.ITEM.get(row.item());
        SkillBook book = new SkillBook(SkillKind.SPELL, row.name(), row.grade(), row.level(), row.body(), row.intro(), Optional.empty(), "",
                row.numbers(), Map.of("jinengneeditem", row.needed()));
        return create(item, book);
    }

    /** 研习该功法后附赠的技能书参数（命令参数串）；没有附赠时为空。 */
    public static Optional<String> bonusArguments(String technique) {
        return Optional.ofNullable(BONUSES.get(technique));
    }

    /** 带名字与技能组件的技能书。 */
    public static ItemStack create(Item carrier, SkillBook book) {
        ItemStack stack = new ItemStack(carrier);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(book.name()));
        stack.set(SkillData.SKILL.get(), book);
        return stack;
    }

    /**
     * 按命令参数生成技能书。前六项公共：承载物品 ID、名字、品阶、所需境界、灵根要求、简介；其余按类别——
     * 技能：所需武器、攻击倍率、伤害倍率、消耗系数、禁施时间、六组冷却秒数、六组冷却上限秒数；
     * 武器技：类型、编号、次数上限；身法：九项；轻功：消耗、速度、特效。数值解析失败为 0，缺失的参数按空处理。
     * 承载物品 ID 无法识别时得到空栈。
     */
    public static ItemStack fromArguments(SkillKind kind, List<String> arguments) {
        Item carrier = BuiltInRegistries.ITEM.get(ResourceLocation.parse(argument(arguments, 0).toLowerCase(Locale.ENGLISH)));
        if (carrier == Items.AIR) {
            return ItemStack.EMPTY;
        }
        Map<String, Double> numbers = new HashMap<>();
        Map<String, String> texts = new HashMap<>();
        switch (kind) {
            case SPELL -> {
                texts.put("jinengneeditem", argument(arguments, 6));
                numbers.put("jinengmanaatt", number(arguments, 7));
                numbers.put("jinengshanghai", number(arguments, 8));
                numbers.put("jinengxiaohao", number(arguments, 9));
                numbers.put("jinengjieyin", number(arguments, 10));
                SkillElement[] elements = SkillElement.values();
                for (int index = 0; index < elements.length; index++) {
                    numbers.put(elements[index].cooldownKey(), number(arguments, 11 + index));
                    numbers.put(elements[index].ignoreKey(), number(arguments, 17 + index));
                }
            }
            case WEAPON_ART -> {
                texts.put("smanshiptype", argument(arguments, 6));
                texts.put("smanshipID", argument(arguments, 7));
                numbers.put("smanshipnumMax", number(arguments, 8));
            }
            case MOVEMENT -> {
                String[] keys = {"shenfatick", "shenfatickMax", "shenfajumptick", "shenfajumptickMax", "shenfaWjump", "shenfaSjump",
                        "shenfaAjump", "shenfaDjump", "shenfaWspeed"};
                for (int index = 0; index < keys.length; index++) {
                    numbers.put(keys[index], number(arguments, 6 + index));
                }
            }
            case FLIGHT -> {
                numbers.put("flyskillxiaohao", number(arguments, 6));
                numbers.put("flyskillspeed", number(arguments, 7));
                texts.put("flyskilltexiao", argument(arguments, 8));
            }
        }
        SkillBook book = new SkillBook(kind, argument(arguments, 1), number(arguments, 2), number(arguments, 3), argument(arguments, 4),
                argument(arguments, 5), Optional.empty(), "", numbers, texts);
        return create(carrier, book);
    }

    private static String argument(List<String> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : "";
    }

    private static double number(List<String> arguments, int index) {
        try {
            return Double.parseDouble(argument(arguments, index).trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
