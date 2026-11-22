package net.yvanchuxiuzhen.flow;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.book.api.ShelfBooks;
import net.yvanchuxiuzhen.system.lianqi.api.BlueprintReward;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.xiulian.api.BirthInitialRewardsEvent;
import net.yvanchuxiuzhen.system.xiulian.api.BirthItems;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;

/** 按来源出身分支发放初始物品、装备与状态，并执行原传送函数。 */
public final class BirthInitialRewardsFlow {
    private BirthInitialRewardsFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BirthInitialRewardsFlow::grant);
    }

    private static void grant(BirthInitialRewardsEvent event) {
        ServerPlayer player = event.player();
        String birth = Cultivation.get(player, CultivationText.BIRTH);
        switch (birth) {
            case "世家纨绔" -> family(player);
            case "凡人奇遇" -> ordinaryEncounter(player);
            case "失魂士兵" -> soldier(player);
            case "他乡故知" -> distantFriend(player);
            case "落魄家族" -> fallenFamily(player);
            case "远古秘辛" -> ancientSecret(player);
            case "无良商贾" -> unscrupulousMerchant(player);
            case "流浪侠客" -> wanderingSwordsman(player);
            default -> {
            }
        }
        if (birth.equals("失魂士兵") || birth.equals("世家纨绔") || birth.equals("落魄家族")
                || birth.equals("远古秘辛") || birth.equals("凡人奇遇")) {
            teleportBySourceFunction(player, event);
        }
    }

    private static void fallenFamily(ServerPlayer player) {
        ItemStack materials = item("cailiao_191");
        Materials.setInstance(materials, 5, 4);
        materials.setCount(20);
        ItemHandlerHelper.giveItemToPlayer(player, materials);
        ItemHandlerHelper.giveItemToPlayer(player, item("ling_18"));
        ItemHandlerHelper.giveItemToPlayer(player, ShelfBooks.technique(1));
        ItemHandlerHelper.giveItemToPlayer(player, ShelfBooks.skill(1));
    }

    private static void family(ServerPlayer player) {
        give(player, item("ling_10"), 1);
        ItemStack spiritStones = TreasureContainers.create(item("treasure_chest_02").getItem(), "宝箱", "灵石", 1, 2, 7);
        spiritStones.set(DataComponents.CUSTOM_NAME, Component.literal("一小包灵石"));
        give(player, spiritStones, 2);
        give(player, Skills.createBook(item("book_021").getItem(), new SkillBook(SkillKind.MOVEMENT, "灵步身法", 1, 1, "无",
                "双击W、A、S、D可以向四方跳跃，但冷却时间稍微长一些。", Optional.empty(), "",
                Map.of("shenfatick", 20.0, "shenfatickMax", 20.0, "shenfaWjump", 0.7,
                        "shenfaSjump", 0.7, "shenfaAjump", 0.7, "shenfaDjump", 0.7), Map.of())), 1);
        give(player, spiritStones, 1);
        give(player, ShelfBooks.technique(1), 1);
        give(player, ShelfBooks.skill(1), 1);
        ItemStack blueprint = Lianqi.createBlueprintReward(item("book_030").getItem(), new BlueprintReward(
                "精铁剑", "家族祖传的图纸，比普通精铁剑图纸要好一些。", "yvanchuxiuzhen:weaponsword_01",
                "家族祖传的炼制法制成的精铁剑，比普通精铁剑要好一些。", "剑", "黄阶三级", 3,
                List.of(
                        new BlueprintReward.Ingredient("yvanchuxiuzhen:cailiao_201", "精铁锭", 3),
                        new BlueprintReward.Ingredient("yvanchuxiuzhen:cailiao_201", "精铁锭", 3),
                        new BlueprintReward.Ingredient("yvanchuxiuzhen:cailiao_201", "精铁锭", 3))), "yvanchuxiuzhen:book_030");
        blueprint.set(DataComponents.CUSTOM_NAME, Component.literal("精铁剑图纸"));
        give(player, blueprint, 1);
    }

    private static void ordinaryEncounter(ServerPlayer player) {
        ItemStack bag = TreasureContainers.create(item("treasure_chest_02").getItem(), "宝箱", "综合", 1, 8, 8);
        bag.set(DataComponents.CUSTOM_NAME, Component.literal("无名修士储物袋"));
        give(player, bag, 1);
        ItemStack casket = Caskets.create("lianqi yvanchuxiuzhen:weaponspeedbow_01 风语弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 黄阶二级 2 7 7 0 0 0 0 100");
        casket.set(DataComponents.CUSTOM_NAME, Component.literal("一个装有修仙武器的宝匣"));
        give(player, casket, 1);
    }

    private static void soldier(ServerPlayer player) {
        player.setHealth(5);
        player.getFoodData().setFoodLevel(2);
        player.getFoodData().setSaturation(0);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamageValue(114);
        equip(player, EquipmentSlot.HEAD, Items.IRON_HELMET, 51);
        equip(player, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE, 41);
        equip(player, EquipmentSlot.LEGS, Items.IRON_LEGGINGS, 14);
        equip(player, EquipmentSlot.FEET, Items.IRON_BOOTS, 51);
        give(player, sword, 1);
        give(player, new ItemStack(Items.BAKED_POTATO), 1);
    }

    private static void distantFriend(ServerPlayer player) {
        give(player, item("ling_09"), 1);
        give(player, new ItemStack(Items.IRON_AXE), 1);
        give(player, item("qiankundai_01"), 1);
        give(player, new ItemStack(Items.BAKED_POTATO), 8);
    }

    private static void ancientSecret(ServerPlayer player) {
        ItemStack materials = item("cailiao_191");
        Materials.setInstance(materials, 5, 4);
        give(player, materials, 5);
        give(player, BookTexts.create(item("book_030").getItem(), "风雷谷修士曾经的信件。",
                "那个邪修逃到400 2 400的位置去了，师兄们说不用管他，反正也活不了多久了。"), 1);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 1);
        give(player, sword, 1);
    }

    private static void unscrupulousMerchant(ServerPlayer player) {
        give(player, new ItemStack(Items.EMERALD), 28);
        give(player, new ItemStack(Items.BREAD), 12);
        equip(player, EquipmentSlot.HEAD, Items.LEATHER_HELMET, 0);
        equip(player, EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE, 0);
        equip(player, EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS, 0);
        equip(player, EquipmentSlot.FEET, Items.LEATHER_BOOTS, 0);
        Vec3 center = WorldStructures.teleportArrayCenter(player.serverLevel()).orElse(Vec3.ZERO);
        ItemStack paper = new ItemStack(Items.PAPER);
        DecimalFormat coordinates = new DecimalFormat("0");
        paper.set(DataComponents.CUSTOM_NAME, Component.literal(coordinates.format(center.x()) + " "
                + coordinates.format(center.y()) + " " + coordinates.format(center.z())));
        give(player, paper, 1);
    }

    private static void wanderingSwordsman(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, 5));
        Vec3 center = WorldStructures.teleportArrayCenter(player.serverLevel()).orElse(Vec3.ZERO);
        ItemStack compass = BirthItems.ancientStoneDisc(center);
        give(player, new ItemStack(Items.BREAD), 32);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        sword.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
        sword.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
        equipRandom(player, EquipmentSlot.HEAD, Items.IRON_HELMET);
        equipRandom(player, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE);
        equipRandom(player, EquipmentSlot.LEGS, Items.IRON_LEGGINGS);
        equipRandom(player, EquipmentSlot.FEET, Items.IRON_BOOTS);
        give(player, sword, 1);
        give(player, compass, 1);
    }

    private static void equipRandom(ServerPlayer player, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(new Random().nextInt(30) + 1);
        player.setItemSlot(slot, stack);
    }

    private static void equip(ServerPlayer player, EquipmentSlot slot, Item item, int damage) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(damage);
        player.setItemSlot(slot, stack);
    }

    private static void teleportBySourceFunction(ServerPlayer player, BirthInitialRewardsEvent event) {
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO,
                event.sourceLevel(), 4, "", Component.empty(), player.server, null);
        player.server.getCommands().performPrefixedCommand(source, "function yvanchuxiuzhen:playerchuansuo");
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    private static void give(ServerPlayer player, ItemStack stack, int count) {
        stack.setCount(count);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }
}
