package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/** 关闭真实出身菜单后检查延迟发奖、两书服务端使用、普通登录与空出身边界。 */
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
    private double countBefore;
    private int readyTick;

    BirthInitialRewardsCheck(List<String> report, String levelId) {
        super("落魄家族初始奖励", report, levelId);
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
        if (step == 1) {
            onServer(this::checkRewards);
            return true;
        }
        onServer(player -> {
            try {
                expect("空出身不命中落魄奖励", 0, inventory(player).stream().mapToInt(ItemStack::getCount).sum());
                expect("空体质仍沿来源应用纯阳", countBefore + 2, Cultivation.tizhiDouble(player, "tizhinumber"));
            } finally {
                restore(player);
            }
        });
        finish();
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

    private void checkRewards(ServerPlayer player) {
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
            player.getInventory().clearContent();
            Cultivation.set(player, CultivationText.BIRTH, "");
            Cultivation.set(player, CultivationText.CONSTITUTION, "");
            openAndClose(player);
            readyTick = player.server.getTickCount() + 3;
            expect("空出身关闭不提前计次", countBefore + 1, Cultivation.tizhiDouble(player, "tizhinumber"));
        } catch (RuntimeException error) {
            restore(player);
            throw error;
        }
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
        player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.inventoryMenu.broadcastChanges();
    }
}
