package net.yvanchuxiuzhen.flow;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.xiulian.api.BirthSupplyEvent;

/** 传承灵物和家族百宝袋的固定来源奖励，由内容所有者构造带业务参数的实例。 */
public final class BirthSupplyFlow {
    private record FamilyBundle(String material, int count, List<String> items) {
    }

    private static final List<FamilyBundle> FAMILY = List.of(
            new FamilyBundle("cailiao_191", 10, List.of("food_007", "food_073", "food_055", "food_065")),
            new FamilyBundle("cailiao_191", 15, List.of("food_004", "food_008", "food_003", "food_017")),
            new FamilyBundle("cailiao_191", 20, List.of("food_010", "food_011", "food_054", "food_022")),
            new FamilyBundle("cailiao_192", 6, List.of("food_013", "food_014", "food_055", "food_019")),
            new FamilyBundle("cailiao_192", 8, List.of("food_077", "food_034", "food_075", "food_049")),
            new FamilyBundle("cailiao_192", 10, List.of("food_018", "food_061", "food_074", "food_026")),
            new FamilyBundle("cailiao_192", 12, List.of("food_015", "food_063", "food_009", "food_002")),
            new FamilyBundle("cailiao_193", 4, List.of("food_021", "food_064", "food_054", "food_023")),
            new FamilyBundle("cailiao_193", 6, List.of("food_037", "food_035", "food_009", "food_059")),
            new FamilyBundle("cailiao_193", 8, List.of("food_076", "food_071", "food_075", "food_006")),
            new FamilyBundle("cailiao_193", 10, List.of("food_044", "food_001", "food_027", "food_057")),
            new FamilyBundle("cailiao_193", 12, List.of("food_038", "food_020", "food_074", "food_053")),
            new FamilyBundle("cailiao_194", 4, List.of("food_040", "food_025", "food_039", "food_041")),
            new FamilyBundle("cailiao_194", 6, List.of("food_031", "food_033", "food_055", "food_043", "flyinginstrument_0101")),
            new FamilyBundle("cailiao_194", 8, List.of("food_032", "food_058", "food_027", "food_065")),
            new FamilyBundle("cailiao_194", 10, List.of("food_036", "food_068", "food_056", "food_051")),
            new FamilyBundle("cailiao_194", 12, List.of("food_045", "food_070", "food_003", "food_048")),
            new FamilyBundle("cailiao_195", 4, List.of("food_047", "food_069", "food_074", "food_050")),
            new FamilyBundle("cailiao_195", 6, List.of("food_072", "food_066", "food_039", "food_052")),
            new FamilyBundle("cailiao_195", 8, List.of("food_042", "food_067", "food_054", "food_053"))
    );

    private BirthSupplyFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BirthSupplyFlow::grant);
    }

    private static void grant(BirthSupplyEvent event) {
        double stage = event.stage();
        if (stage != Math.rint(stage) || stage < 1 || stage > 30) return;
        if (event.kind() == BirthSupplyEvent.Kind.INHERITANCE) inheritance(event.player(), (int) stage);
        else if (stage <= FAMILY.size()) family(event.player(), (int) stage);
    }

    private static void inheritance(ServerPlayer player, int stage) {
        switch (stage) {
            case 1 -> material(player, "cailiao_191", 10, 5);
            case 2 -> {
                ItemStack paper = new ItemStack(Items.PAPER);
                paper.set(DataComponents.CUSTOM_NAME, Component.literal("杜康酒配方：两个小麦和两个高粱，在木甑中蒸馏。"));
                give(player, paper, 10);
            }
            case 3 -> plain(player, "jiuniang_004", 3);
            case 4 -> plain(player, "itemfulu_46", 10);
            case 5 -> plain(player, "money_02", 10);
            case 6 -> material(player, "cailiao_192", 10, 10);
            case 7 -> plain(player, "qiankundai_01", 1);
            case 8 -> plain(player, "fulufuzhi", 40);
            case 9 -> material(player, "cailiao_183", 2, 15);
            case 10 -> give(player, Fulu.createReward(item("itemfulu_09"), 2), 10);
            case 11 -> material(player, "cailiao_193", 10, 15);
            case 12 -> plain(player, "flyinginstrument_0201", 1);
            case 13 -> plain(player, "motiao_02", 20);
            case 14 -> give(player, Fulu.createReward(item("itemfulu_11"), 2, "玄阶"), 10);
            case 15 -> plain(player, "qiankundai_02", 1);
            case 16 -> material(player, "cailiao_194", 10, 20);
            case 17 -> plain(player, "money_03", 10);
            case 18 -> material(player, "cailiao_185", 1, 25);
            case 19 -> plain(player, "cailiao_146", 10);
            case 20 -> give(player, Fulu.createReward(item("itemfulu_10"), 2, "玄阶"), 10);
            case 21 -> material(player, "cailiao_195", 10, 25);
            case 22 -> material(player, "cailiao_222", 1, 10);
            case 23 -> give(player, Skills.createBook(item("book_012"), new SkillBook(SkillKind.MOVEMENT, "分形捉影", 21, 11, "无",
                    "双击W时，朝向鼠标方向快速弹射过去，同时隐形，并且玩家可以三连跳。", Optional.empty(), "",
                    Map.of("shenfatick", 20.0, "shenfatickMax", 60.0, "shenfajumptick", 20.0, "shenfajumptickMax", 60.0,
                            "shenfaWjump", 1.5, "shenfaSjump", 0.0, "shenfaAjump", 0.0, "shenfaDjump", 0.0, "shenfaWspeed", 0.0), Map.of())), 1);
            case 24 -> give(player, Fulu.createReward(item("itemfulu_46"), 3, "地阶"), 10);
            case 25 -> plain(player, "money_04", 10);
            case 26 -> material(player, "cailiao_196", 10, 30);
            case 27 -> plain(player, "alchemy_05", 1);
            case 28 -> material(player, "cailiao_187", 1, 35);
            case 29 -> plain(player, "motiao_03", 20);
        }
    }

    private static void family(ServerPlayer player, int stage) {
        FamilyBundle bundle = FAMILY.get(stage - 1);
        plain(player, bundle.material(), bundle.count());
        for (String id : bundle.items()) plain(player, id, 1);
        switch (stage) {
            case 2 -> give(player, Liandan.createMedicineReward(item("itemdanyaohulu_06"),
                    new MedicineReward("回春养元丹", 2, 10, true, List.of(
                            new MedicineReward.Effect("regeneration", 2, 120),
                            new MedicineReward.Effect("yvanchuxiuzhen:effect_016", 1, 30)))), 1);
            case 4 -> {
                give(player, Caskets.create("lianqi yvanchuxiuzhen:fabao_011 飓风扇 （特供）右键吹出一阵强风，吹飞前面范围的敌方实体，并造成法术伤害；潜行右键则直接弹开自身周围的实体，并造成法术伤害。 法宝 黄阶八级 8 2 15 3 0 0 0 80 1 yvanchuxiuzhen shuxingatt_10 1"), 1);
            }
            case 6 -> give(player, Liandan.createMedicineReward(item("itemdanyaohulu_06"),
                    new MedicineReward("秘制固本培元丹", 1, 8, true, List.of(
                            new MedicineReward.Effect("yvanchuxiuzhen:effect_015", 1, 60)))), 1);
            case 8 -> {
                give(player, Caskets.create("lianqi yvanchuxiuzhen:armor_03_helmet 山甲头盔 （特供）家族赐予的盔甲。 头盔 玄阶2级 12 8 200 100 2 0 0 10 5 5 100 1 minecraft projectile_protection 1"), 1);
                give(player, Caskets.create("lianqi yvanchuxiuzhen:armor_03_chestplate 山甲胸甲 （特供）家族赐予的盔甲。 胸甲 玄阶2级 12 12 180 20 6 0 0.1 20 10 0 100 1 minecraft protection 1"), 1);
                give(player, Caskets.create("lianqi yvanchuxiuzhen:armor_03_leggings 山甲护腿 （特供）家族赐予的盔甲。 裤子 玄阶2级 12 10 100 0 4 0 0.1 16 8 0 100 1 minecraft feather_falling 1"), 1);
                give(player, Caskets.create("lianqi yvanchuxiuzhen:armor_03_boots 山甲战靴 （特供）家族赐予的盔甲。 战靴 玄阶2级 12 8 20 0 2 0 0 10 5 0 100 1 minecraft unbreaking 1"), 1);
            }
            case 10 -> give(player, movementBook(), 1);
            case 12 -> give(player, waterBook(), 1);
        }
    }

    private static ItemStack movementBook() {
        return Skills.createBook(item("book_020"), new SkillBook(SkillKind.MOVEMENT, "踏风步", 11, 11, "无",
                "玩家连续按下方向键时，会朝向对应方向跳跃。", Optional.empty(), "",
                Map.of("shenfatick", 10.0, "shenfatickMax", 10.0, "shenfaWjump", 1.5,
                        "shenfaSjump", 1.5, "shenfaAjump", 1.5, "shenfaDjump", 1.5),
                Map.of("belongToItemID", "yvanchuxiuzhen:book_020")));
    }

    private static ItemStack waterBook() {
        return Skills.createBook(item("book_020"), new SkillBook(SkillKind.SPELL, "激流波", 12, 12, "水",
                "在前方召唤一条水流，造成范围伤害并击退。", Optional.empty(), "",
                Map.of("jinengmanaatt", 5.0, "jinengshanghai", 1.8, "jinengxiaohao", 1.5, "jinengjieyin", 20.0,
                        "Watercolddown", 20.0, "Treecolddown", -4.0, "Watercoldignore", 30.0),
                Map.of("jinengneeditem", "无", "belongToItemID", "yvanchuxiuzhen:book_005")));
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }

    private static void plain(ServerPlayer player, String id, int count) {
        give(player, new ItemStack(item(id)), count);
    }

    private static void material(ServerPlayer player, String id, int count, int level) {
        ItemStack stack = new ItemStack(item(id));
        Materials.setInstance(stack, level, 4);
        give(player, stack, count);
    }

    private static void give(ServerPlayer player, ItemStack stack, int count) {
        stack.setCount(count);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }
}
