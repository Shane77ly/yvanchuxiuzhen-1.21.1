package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.xiulian.api.BirthItems;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 经真实物品use核对领取门槛、实例参数、特殊奖励和两件载体次数保存。 */
final class BirthSupplyCheck extends SystemCheck {
    BirthSupplyCheck(List<String> report, String levelId) {
        super("出身分次补给", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::exercise);
        finish();
        return true;
    }

    private void exercise(ServerPlayer player) {
        List<ItemStack> inventory = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        String birth = Cultivation.get(player, CultivationText.BIRTH);
        try {
            inheritance(player);
            family(player);
        } finally {
            player.getInventory().clearContent();
            for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
            player.getCooldowns().removeCooldown(item("jiazubaibaodai"));
            Cultivation.set(player, CultivationValue.LEVEL_NUMBER, level);
            Cultivation.set(player, CultivationText.BIRTH, birth);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private void inheritance(ServerPlayer player) {
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
        ItemStack carrier = prepare(player, "ling_18", 0);
        use(player, carrier);
        expect("零境界拒绝首份", 0, BirthItems.supplyCount(carrier));
        expect("拒绝时没有奖励", 0, rewardCount(player));
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 1);
        use(player, carrier);
        expect("首份次数先增加", 1, BirthItems.supplyCount(carrier));
        ItemStack material = find(player, "cailiao_191");
        expect("首份材料十件", 10, material.getCount());
        expect("首份材料等级", 5, Materials.instance(material).level());
        expect("首份材料品阶", 4, Materials.instance(material).quality());
        use(player, carrier);
        expect("境界必须严格大于旧次数", 1, BirthItems.supplyCount(carrier));
        expect("严格门槛未重复发奖", 10, find(player, "cailiao_191").getCount());

        carrier = prepare(player, "ling_18", 0.5);
        use(player, carrier);
        expect("小数次数保持double自增", 1.5, BirthItems.supplyCount(carrier));
        expect("小数次数不误命中整数奖励", 0, rewardCount(player));
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 40);
        carrier = prepare(player, "ling_18", 9);
        use(player, carrier);
        ItemStack talisman = find(player, "itemfulu_09");
        TalismanInstance talismanData = Fulu.talisman(talisman);
        expect("第十份符箓数量", 10, talisman.getCount());
        expect("第十份符箓品阶", 2, talismanData.level());
        expectTrue("第十份不补造品阶文字", talismanData.levelText().isEmpty());
        carrier = prepare(player, "ling_18", 22);
        use(player, carrier);
        ItemStack reference = Skills.createBook(SkillKind.MOVEMENT, List.of("yvanchuxiuzhen:book_012 分形捉影 21 11 无 双击W时，朝向鼠标方向快速弹射过去，同时隐形，并且玩家可以三连跳。 20 60 20 60 1.5 0 0 0 0".split(" ")));
        expectTrue("第二十三份背包身法书与手动命令生成的完整物品一致", ItemStack.matches(reference, find(player, "book_012")));
        carrier = prepare(player, "ling_18", 29);
        use(player, carrier);
        expect("Q197第三十次仍自增", 30, BirthItems.supplyCount(carrier));
        expect("Q197第三十次没有奖励", 0, rewardCount(player));
        use(player, carrier);
        expect("耗尽后次数保持", 30, BirthItems.supplyCount(carrier));
        reloadCount(player, carrier, "yikaiqi", 30);
    }

    private void family(ServerPlayer player) {
        Cultivation.set(player, CultivationText.BIRTH, "散修");
        ItemStack carrier = prepare(player, "jiazubaibaodai", 0);
        use(player, carrier);
        expect("非家族出身不计次", 0, BirthItems.supplyCount(carrier));
        expect("非家族出身不发奖", 0, rewardCount(player));
        expectTrue("认证失败不冷却", !player.getCooldowns().isOnCooldown(carrier.getItem()));
        Cultivation.set(player, CultivationText.BIRTH, "世家纨绔");
        use(player, carrier);
        expect("家族首份五栈合计十四件", 14, rewardCount(player));
        ItemStack material = find(player, "cailiao_191");
        expect("家族材料保持默认等级", 0, Materials.instance(material).level());
        expect("家族材料保持默认品阶", 0, Materials.instance(material).quality());
        expectTrue("领取后设置原生冷却", player.getCooldowns().isOnCooldown(carrier.getItem()));
        carrier = prepare(player, "jiazubaibaodai", 1);
        use(player, carrier);
        MedicineInstance medicine = Liandan.medicine(find(player, "itemdanyaohulu_06"));
        expect("回春养元丹十份", 10, medicine.doses());
        expect("回春养元丹两效果", 2, medicine.effects().size());
        expect("回春养元丹再生单位", 120, medicine.effects().get(0).duration());
        expect("回春养元丹第二效果", "yvanchuxiuzhen:effect_016", medicine.effects().get(1).id());
        carrier = prepare(player, "jiazubaibaodai", 3);
        use(player, carrier);
        expectTrue("第四份飓风扇宝匣保留命令", Caskets.command(find(player, "baoxiaitem"))
                .startsWith("lianqi yvanchuxiuzhen:fabao_011 飓风扇 "));
        carrier = prepare(player, "jiazubaibaodai", 5);
        use(player, carrier);
        medicine = Liandan.medicine(find(player, "itemdanyaohulu_06"));
        expect("固本培元丹八份", 8, medicine.doses());
        expect("固本培元丹效果", "yvanchuxiuzhen:effect_015", medicine.effects().get(0).id());
        carrier = prepare(player, "jiazubaibaodai", 7);
        use(player, carrier);
        expect("第八份四个独立宝匣", 4, count(player, "baoxiaitem"));
        long commands = inventory(player).stream().filter(stack -> stack.is(item("baoxiaitem")))
                .map(Caskets::command).distinct().count();
        expect("四个宝匣指令独立", 4, commands);
        carrier = prepare(player, "jiazubaibaodai", 9);
        use(player, carrier);
        expectTrue("第十份踏风步", Skills.book(find(player, "book_020"))
                .filter(book -> book.name().equals("踏风步") && book.number("shenfaWjump") == 1.5).isPresent());
        carrier = prepare(player, "jiazubaibaodai", 11);
        use(player, carrier);
        expectTrue("Q198实际承载020但身份005", Skills.book(find(player, "book_020"))
                .filter(book -> book.name().equals("激流波") && book.kind() == SkillKind.SPELL
                        && book.text("belongToItemID").equals("yvanchuxiuzhen:book_005")
                        && book.number("Treecolddown") == -4).isPresent());
        carrier = prepare(player, "jiazubaibaodai", 13);
        use(player, carrier);
        expect("第十四份保留飞行法器", 1, count(player, "flyinginstrument_0101"));
        carrier = prepare(player, "jiazubaibaodai", 19);
        use(player, carrier);
        expect("第二十次材料数量", 8, count(player, "cailiao_195"));
        expect("第二十次计次", 20, BirthItems.supplyCount(carrier));
        reloadCount(player, carrier, "geiyvcishu", 20);
        player.getCooldowns().removeCooldown(carrier.getItem());
        int before = rewardCount(player);
        use(player, carrier);
        expect("家族耗尽不重复发奖", before, rewardCount(player));
        expectTrue("家族耗尽不追加冷却", !player.getCooldowns().isOnCooldown(carrier.getItem()));
        expect("载体不消耗", 1, carrier.getCount());
    }

    private void reloadCount(ServerPlayer player, ItemStack stack, String key, double count) {
        ItemStack loaded = ItemStack.parseOptional(player.registryAccess(), (CompoundTag) stack.save(player.registryAccess()));
        expect("载体次数序列化重读" + key, count, BirthItems.supplyCount(loaded));
    }

    private static ItemStack prepare(ServerPlayer player, String id, double count) {
        player.getInventory().clearContent();
        ItemStack stack = new ItemStack(item(id));
        BirthItems.setSupplyCount(stack, count);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.getCooldowns().removeCooldown(stack.getItem());
        return stack;
    }

    private static void use(ServerPlayer player, ItemStack stack) {
        stack.getItem().use(player.serverLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }

    private static List<ItemStack> inventory(ServerPlayer player) {
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) result.add(player.getInventory().getItem(slot));
        return result;
    }

    private static ItemStack find(ServerPlayer player, String id) {
        return inventory(player).stream().filter(stack -> stack.is(item(id))).findFirst().orElse(ItemStack.EMPTY);
    }

    private static int count(ServerPlayer player, String id) {
        return inventory(player).stream().filter(stack -> stack.is(item(id))).mapToInt(ItemStack::getCount).sum();
    }

    private static int rewardCount(ServerPlayer player) {
        return inventory(player).stream().filter(stack -> !stack.is(item("ling_18")) && !stack.is(item("jiazubaibaodai")))
                .mapToInt(ItemStack::getCount).sum();
    }
}
