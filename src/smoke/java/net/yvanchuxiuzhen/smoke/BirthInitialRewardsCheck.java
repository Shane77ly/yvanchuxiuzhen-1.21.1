package net.yvanchuxiuzhen.smoke;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/** 关闭出身菜单后检查八种奖励分支、来源顺序、随机装备边界与空出身边界。 */
final class BirthInitialRewardsCheck extends SystemCheck {
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private final Map<CultivationValue, Double> originalRoots = new EnumMap<>(CultivationValue.class);
    private CompoundTag originalTizhi;
    private String originalBirth;
    private String originalConstitution;
    private ServerLevel originalLevel;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private float originalHealth;
    private int originalFood;
    private float originalSaturation;
    private MobEffectInstance originalBadOmen;
    private double countBefore;
    private int readyTick;

    BirthInitialRewardsCheck(List<String> report, String levelId) {
        super("出身初始奖励", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::start);
            return true;
        }
        if (local < 6) return false;
        boolean[] ready = new boolean[1];
        onServer(player -> ready[0] = player.server.getTickCount() >= readyTick);
        if (!ready[0]) return false;
        switch (step) {
            case 1 -> onServer(player -> {
                advanceBirth(player, () -> checkFallenRewards(player), "世家纨绔", 1);
            });
            case 2 -> onServer(player -> {
                advanceBirth(player, () -> checkFamilyRewards(player), "凡人奇遇", 2);
            });
            case 3 -> onServer(player -> {
                advanceBirth(player, () -> checkOrdinaryEncounter(player), "失魂士兵", 3);
            });
            case 4 -> onServer(player -> {
                advanceBirth(player, () -> checkSoldier(player), "他乡故知", 4);
            });
            case 5 -> onServer(player -> {
                advanceBirth(player, () -> checkDistantFriend(player), "远古秘辛", 5);
            });
            case 6 -> onServer(player -> {
                advanceBirth(player, () -> checkAncientSecret(player), "无良商贾", 6);
            });
            case 7 -> onServer(player -> {
                advanceBirth(player, () -> checkMerchant(player), "流浪侠客", 7);
            });
            case 8 -> onServer(player -> {
                advanceBirth(player, () -> checkWanderingSwordsman(player), "", 8);
            });
            case 9 -> {
                onServer(player -> {
                    try {
                        expect("空出身不发初始物品", 0, inventory(player).stream().mapToInt(ItemStack::getCount).sum());
                        expect("空体质仍沿来源应用纯阳", countBefore + 9, Cultivation.tizhiDouble(player, "tizhinumber"));
                    } finally {
                        restore(player);
                    }
                });
                finish();
            }
            default -> throw new IllegalStateException("未知出身检查步骤 " + step);
        }
        return true;
    }

    private void start(ServerPlayer player) {
        inventory(player).forEach(stack -> originalInventory.add(stack.copy()));
        for (CultivationValue root : BirthSelection.ROOTS) originalRoots.put(root, Cultivation.get(player, root));
        originalBirth = Cultivation.get(player, CultivationText.BIRTH);
        originalConstitution = Cultivation.get(player, CultivationText.CONSTITUTION);
        originalLevel = player.serverLevel();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalHealth = player.getHealth();
        originalFood = player.getFoodData().getFoodLevel();
        originalSaturation = player.getFoodData().getSaturationLevel();
        MobEffectInstance badOmen = player.getEffect(MobEffects.BAD_OMEN);
        originalBadOmen = badOmen == null ? null : new MobEffectInstance(badOmen);
        player.removeEffect(MobEffects.BAD_OMEN);
        Cultivation.editTizhi(player, data -> originalTizhi = data.copy());
        countBefore = Cultivation.tizhiDouble(player, "tizhinumber");
        player.getInventory().clearContent();
        Cultivation.set(player, CultivationText.BIRTH, "落魄家族");
        Cultivation.set(player, CultivationText.CONSTITUTION, "太初混沌体");
        // 正常登录的来源门槛由已有灵根排除；只关闭菜单时执行本包奖励。
        Cultivation.set(player, CultivationValue.ROOT_GOLD, 1);
        for (CultivationValue root : BirthSelection.ROOTS) {
            if (root != CultivationValue.ROOT_GOLD) Cultivation.set(player, root, 0);
        }
        openAndClose(player);
        readyTick = player.server.getTickCount() + 3;
        expect("关闭同一调用内不提前发奖", 0, inventory(player).stream().mapToInt(ItemStack::getCount).sum());
        expect("关闭同一调用内体质未提前计次", countBefore, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkFallenRewards(ServerPlayer player) {
        try {
            List<ItemStack> rewards = inventory(player).stream().filter(stack -> !stack.isEmpty()).toList();
            expect("完整初始包四个栈", 4, rewards.size());
            expect("初始材料身份", "yvanchuxiuzhen:cailiao_191", itemId(rewards.getFirst()));
            expect("材料数量二十", 20, rewards.getFirst().getCount());
            expect("材料为五级", 5, Materials.instance(rewards.getFirst()).level());
            expect("材料为四品", 4, Materials.instance(rewards.getFirst()).quality());
            expect("传承物第二个发放", "yvanchuxiuzhen:ling_18", itemId(rewards.get(1)));
            expect("功法书保留藏宝载体", "yvanchuxiuzhen:book_021", itemId(rewards.get(2)));
            expect("技能书保留藏宝载体", "yvanchuxiuzhen:book_021", itemId(rewards.get(3)));
            expect("功法藏宝书第三个", "功法书", data(rewards.get(2)).getString("TreasureTypeSmall"));
            expect("技能藏宝书第四个", "技能书", data(rewards.get(3)).getString("TreasureTypeSmall"));
            expect("初始藏宝书等级", 1, data(rewards.get(2)).getDouble("TreasureLEVEL"));
            expect("技能藏宝书等级", 1, data(rewards.get(3)).getDouble("TreasureLEVEL"));
            expect("初始功法书中文名", "一本功法书", rewards.get(2).getHoverName().getString());
            expect("初始技能书中文名", "一本技能书", rewards.get(3).getHoverName().getString());
            expect("延迟结束后体质计次", countBefore + 1, Cultivation.tizhiDouble(player, "tizhinumber"));
            BirthSelection.onLogin(player);
            expectTrue("已选灵根登录不再次打开出身菜单", !(player.containerMenu instanceof BirthMenu));
            expect("普通登录不追加奖励", 23, inventory(player).stream().mapToInt(ItemStack::getCount).sum());

            ItemStack technique = rewards.get(2).copy();
            ItemStack skill = rewards.get(3).copy();
            consume(player, technique);
            expectTrue("初始功法藏宝书服务端use生成实例", inventory(player).stream().anyMatch(stack -> Techniques.book(stack).isPresent()));
            expect("功法藏宝书消耗一件", 0, technique.getCount());
            consume(player, skill);
            expectTrue("初始技能藏宝书服务端use生成实例", inventory(player).stream().anyMatch(stack -> Skills.book(stack).isPresent()));
            expect("技能藏宝书消耗一件", 0, skill.getCount());
        } catch (RuntimeException error) {
            restore(player);
            throw error;
        }
    }

    private void checkFamilyRewards(ServerPlayer player) {
        try {
            List<ItemStack> rewards = inventory(player).stream().filter(stack -> !stack.isEmpty()).toList();
            expect("家族初始包六个栈", 6, rewards.size());
            expect("家族令牌优先发放", "yvanchuxiuzhen:ling_10", itemId(rewards.getFirst()));
            expect("家族宝箱第二栈", "yvanchuxiuzhen:treasure_chest_02", itemId(rewards.get(1)));
            expect("家族宝箱合计三件", 3, rewards.get(1).getCount());
            expect("家族宝箱中文名", "一小包灵石", rewards.get(1).getHoverName().getString());
            expect("身法书位于两本藏宝书之前", "yvanchuxiuzhen:book_021", itemId(rewards.get(2)));
            expect("身法书类型", SkillKind.MOVEMENT, Skills.book(rewards.get(2)).orElseThrow().kind());
            expect("身法书名称", "灵步身法", Skills.book(rewards.get(2)).orElseThrow().name());
            expect("身法书冷却", 20, Skills.book(rewards.get(2)).orElseThrow().number("shenfatick"));
            expect("家族功法藏宝书", "功法书", data(rewards.get(3)).getString("TreasureTypeSmall"));
            expect("家族技能藏宝书", "技能书", data(rewards.get(4)).getString("TreasureTypeSmall"));
            expect("家族图纸末位发放", "yvanchuxiuzhen:book_030", itemId(rewards.get(5)));
            expect("图纸载体身份", "yvanchuxiuzhen:book_030", data(rewards.get(5)).getString("belongToItemID"));
            expect("家族图纸名称", "精铁剑图纸", rewards.get(5).getHoverName().getString());
            expect("图纸配方名称保持精铁剑", "精铁剑", data(rewards.get(5)).getString("tuzhiname"));
            expect("家族图纸含三项配方", 3, IntStream.range(0, 3)
                    .filter(index -> data(rewards.get(5)).getString("cailiao0" + index).equals("yvanchuxiuzhen:cailiao_201"))
                    .count());
            expect("家族关闭后体质计次", countBefore + 2, Cultivation.tizhiDouble(player, "tizhinumber"));
        } catch (RuntimeException error) {
            restore(player);
            throw error;
        }
    }

    private void checkOrdinaryEncounter(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("凡人奇遇两个奖励栈", 2, rewards.size());
        expect("无名修士储物袋物品", "yvanchuxiuzhen:treasure_chest_02", itemId(rewards.getFirst()));
        expect("无名修士储物袋名称", "无名修士储物袋", rewards.getFirst().getHoverName().getString());
        expect("无名修士储物袋类别", "宝箱", data(rewards.getFirst()).getString("TreasureTypeBig"));
        expect("无名修士储物袋子类", "综合", data(rewards.getFirst()).getString("TreasureTypeSmall"));
        expect("无名修士储物袋固定下限", 8, data(rewards.getFirst()).getDouble("minimumvalue"));
        expect("无名修士储物袋固定上限", 8, data(rewards.getFirst()).getDouble("Maximumvalue"));
        expect("风语弓宝匣身份", "yvanchuxiuzhen:baoxiaitem", itemId(rewards.get(1)));
        expect("风语弓宝匣名称", "一个装有修仙武器的宝匣", rewards.get(1).getHoverName().getString());
        expect("风语弓宝匣保留来源完整指令", "lianqi yvanchuxiuzhen:weaponspeedbow_01 风语弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 黄阶二级 2 7 7 0 0 0 0 100",
                data(rewards.get(1)).getString("zhiling"));
        expect("凡人奇遇关闭后体质计次", countBefore + 3, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkSoldier(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("失魂士兵两个背包奖励栈", 2, rewards.size());
        expect("失魂士兵生命", 5, player.getHealth());
        expect("失魂士兵饥饿", 2, player.getFoodData().getFoodLevel());
        expect("失魂士兵饱和", 0, player.getFoodData().getSaturationLevel());
        expect("失魂士兵铁头盔", Items.IRON_HELMET, player.getItemBySlot(EquipmentSlot.HEAD).getItem());
        expect("失魂士兵头盔耐久", 51, player.getItemBySlot(EquipmentSlot.HEAD).getDamageValue());
        expect("失魂士兵铁胸甲", Items.IRON_CHESTPLATE, player.getItemBySlot(EquipmentSlot.CHEST).getItem());
        expect("失魂士兵胸甲耐久", 41, player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue());
        expect("失魂士兵铁护腿", Items.IRON_LEGGINGS, player.getItemBySlot(EquipmentSlot.LEGS).getItem());
        expect("失魂士兵护腿耐久", 14, player.getItemBySlot(EquipmentSlot.LEGS).getDamageValue());
        expect("失魂士兵铁靴", Items.IRON_BOOTS, player.getItemBySlot(EquipmentSlot.FEET).getItem());
        expect("失魂士兵靴子耐久", 51, player.getItemBySlot(EquipmentSlot.FEET).getDamageValue());
        expect("失魂士兵损耗铁剑", 114, find(rewards, Items.IRON_SWORD).getDamageValue());
        expect("失魂士兵烤马铃薯", 1, find(rewards, Items.BAKED_POTATO).getCount());
        expect("失魂士兵关闭后体质计次", countBefore + 4, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkDistantFriend(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("他乡故知四个奖励栈", 4, rewards.size());
        expect("他乡故知升仙令顺序", "yvanchuxiuzhen:ling_09", itemId(rewards.get(0)));
        expect("他乡故知铁斧顺序", Items.IRON_AXE, rewards.get(1).getItem());
        expect("他乡故知乾坤袋顺序", "yvanchuxiuzhen:qiankundai_01", itemId(rewards.get(2)));
        expect("他乡故知烤马铃薯数量", 8, rewards.get(3).getCount());
        expect("他乡故知关闭后体质计次", countBefore + 5, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkAncientSecret(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("远古秘辛三个奖励栈", 3, rewards.size());
        expect("远古秘辛材料身份", "yvanchuxiuzhen:cailiao_191", itemId(rewards.get(0)));
        expect("远古秘辛材料数量", 5, rewards.get(0).getCount());
        expect("远古秘辛材料等级", 5, Materials.instance(rewards.get(0)).level());
        expect("远古秘辛材料品阶", 4, Materials.instance(rewards.get(0)).quality());
        expect("远古秘辛文本书载体", "yvanchuxiuzhen:book_030", itemId(rewards.get(1)));
        expect("远古秘辛文本书名称", "风雷谷修士曾经的信件。", rewards.get(1).getHoverName().getString());
        expect("远古秘辛文本书正文", "那个邪修逃到400 2 400的位置去了，师兄们说不用管他，反正也活不了多久了。",
                data(rewards.get(1)).getString("textdisplay"));
        expect("远古秘辛铁剑身份", Items.IRON_SWORD, rewards.get(2).getItem());
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        expect("远古秘辛锋利等级", 1, EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SHARPNESS), rewards.get(2)));
        expect("远古秘辛关闭后体质计次", countBefore + 6, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkMerchant(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("无良商贾三件背包奖励", 3, rewards.size());
        expect("无良商贾绿宝石数量", 28, find(rewards, Items.EMERALD).getCount());
        expect("无良商贾面包数量", 12, find(rewards, Items.BREAD).getCount());
        ItemStack paper = find(rewards, Items.PAPER);
        Vec3 center = WorldStructures.teleportArrayCenter(player.serverLevel()).orElse(Vec3.ZERO);
        DecimalFormat coordinates = new DecimalFormat("0");
        expect("无良商贾纸张记录全局坐标", coordinates.format(center.x()) + " " + coordinates.format(center.y()) + " "
                + coordinates.format(center.z()), paper.getHoverName().getString());
        expect("无良商贾皮头盔", Items.LEATHER_HELMET, player.getItemBySlot(EquipmentSlot.HEAD).getItem());
        expect("无良商贾皮胸甲", Items.LEATHER_CHESTPLATE, player.getItemBySlot(EquipmentSlot.CHEST).getItem());
        expect("无良商贾皮护腿", Items.LEATHER_LEGGINGS, player.getItemBySlot(EquipmentSlot.LEGS).getItem());
        expect("无良商贾皮靴", Items.LEATHER_BOOTS, player.getItemBySlot(EquipmentSlot.FEET).getItem());
        expect("无良商贾关闭后体质计次", countBefore + 7, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void checkWanderingSwordsman(ServerPlayer player) {
        List<ItemStack> rewards = nonEmptyInventory(player);
        expect("流浪侠客三个背包奖励栈", 3, rewards.size());
        expect("流浪侠客面包数量", 32, find(rewards, Items.BREAD).getCount());
        ItemStack sword = find(rewards, Items.DIAMOND_SWORD);
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        expect("流浪侠客钻石剑锋利", 1, EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SHARPNESS), sword));
        expect("流浪侠客钻石剑亡灵杀手", 1, EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SMITE), sword));
        ItemStack compass = find(rewards, BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:ling_11")));
        CompoundTag compassData = data(compass);
        Vec3 center = WorldStructures.teleportArrayCenter(player.serverLevel()).orElse(Vec3.ZERO);
        expect("流浪侠客石盘全局X坐标副本", center.x(), compassData.getDouble("mubiaoX"));
        expect("流浪侠客石盘全局Y坐标副本", center.y(), compassData.getDouble("mubiaoY"));
        expect("流浪侠客石盘全局Z坐标副本", center.z(), compassData.getDouble("mubiaoZ"));
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack armor = player.getItemBySlot(slot);
            expect("流浪侠客随机铁甲身份 " + slot, expectedArmor(slot), armor.getItem());
            expectTrue("流浪侠客随机铁甲耐久范围 " + slot, armor.getDamageValue() >= 1 && armor.getDamageValue() <= 30);
        }
        MobEffectInstance badOmen = player.getEffect(MobEffects.BAD_OMEN);
        expectTrue("流浪侠客不祥之兆放大器与时长", badOmen != null && badOmen.getAmplifier() == 5
                && badOmen.getDuration() <= 120000 && badOmen.getDuration() > 119950);
        expect("流浪侠客关闭后体质计次", countBefore + 8, Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void beginBirth(ServerPlayer player, String birth, String constitution, int completedCloses) {
        player.getInventory().clearContent();
        Cultivation.set(player, CultivationText.BIRTH, birth);
        Cultivation.set(player, CultivationText.CONSTITUTION, constitution);
        openAndClose(player);
        readyTick = player.server.getTickCount() + 3;
        expect("出身分支 " + (birth.isEmpty() ? "空" : birth) + " 延迟关闭不提前计次", countBefore + completedCloses,
                Cultivation.tizhiDouble(player, "tizhinumber"));
    }

    private void advanceBirth(ServerPlayer player, Runnable assertions, String nextBirth, int completedCloses) {
        try {
            assertions.run();
            beginBirth(player, nextBirth, nextBirth.isEmpty() ? "" : "太初混沌体", completedCloses);
        } catch (RuntimeException error) {
            restore(player);
            throw error;
        }
    }

    private static List<ItemStack> nonEmptyInventory(ServerPlayer player) {
        return IntStream.range(0, 36).mapToObj(player.getInventory()::getItem).filter(stack -> !stack.isEmpty()).toList();
    }

    private static ItemStack find(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(stack -> stack.is(item)).findFirst().orElseThrow();
    }

    private static Item expectedArmor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.IRON_HELMET;
            case CHEST -> Items.IRON_CHESTPLATE;
            case LEGS -> Items.IRON_LEGGINGS;
            case FEET -> Items.IRON_BOOTS;
            default -> throw new IllegalArgumentException("非护甲槽位 " + slot);
        };
    }

    private static void openAndClose(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new BirthMenu(id, inventory), Component.literal("PlayerJoinGUI")));
        player.closeContainer();
    }

    private static void consume(ServerPlayer player, ItemStack book) {
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, book);
        book.getItem().use(player.serverLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static List<ItemStack> inventory(ServerPlayer player) {
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) result.add(player.getInventory().getItem(slot));
        return result;
    }

    private void restore(ServerPlayer player) {
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) player.getInventory().setItem(slot, originalInventory.get(slot));
        Cultivation.set(player, CultivationText.BIRTH, originalBirth);
        Cultivation.set(player, CultivationText.CONSTITUTION, originalConstitution);
        originalRoots.forEach((root, value) -> Cultivation.set(player, root, value));
        Cultivation.editTizhi(player, data -> {
            for (String key : List.of("tizhiopen_001", "LevelupCorrectXiuweixishou", "tizhinumber", "tizhiopen_012", "AttackAttCorrectPercentage")) {
                if (originalTizhi.contains(key)) data.put(key, originalTizhi.get(key).copy());
                else data.remove(key);
            }
        });
        player.setHealth(Math.min(originalHealth, player.getMaxHealth()));
        player.getFoodData().setFoodLevel(originalFood);
        player.getFoodData().setSaturation(originalSaturation);
        player.removeEffect(MobEffects.BAD_OMEN);
        if (originalBadOmen != null) player.addEffect(originalBadOmen);
        player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.inventoryMenu.broadcastChanges();
    }
}
