package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.liandan.data.LiandanData;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 01宝箱状态丹药的随机生产、原生药瓶服用、剂数保存及服尽返空瓶的跨系统检查。 */
final class TreasureMedicineRewardCheck extends SystemCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private final CompoundTag food = new CompoundTag();
    private AABB area;
    private GameType mode;
    private int selected;
    private int doses;
    private Item carrier;
    private Candidate candidate;
    private ItemStack saved;
    private float absorption;
    private boolean shift;
    private boolean useKey;

    TreasureMedicineRewardCheck(List<String> report, String levelId) {
        super("宝箱丹药消费", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 6) return reopened(local);
        if (step == 7) return inWorld(local);
        if (local < (step == 2 ? 4 : 8)) return false;
        if (step >= 9) {
            int index = step - 9;
            if (index < 2 * (doses - 1)) {
                if (index % 2 == 0) {
                    useItem();
                } else {
                    int remaining = doses - 2 - index / 2;
                    onServer(player -> verifyDose(player, remaining, "重进后第" + (index / 2 + 1) + "次服用"));
                }
            } else {
                onServer(player -> {
                    verifyFeeding(player);
                    cleanup(player);
                });
                finish();
            }
            return true;
        }
        switch (step) {
            case 0 -> {
                useKey = minecraft.options.keyUse.isDown();
                minecraft.options.keyUse.setDown(false);
                onServer(this::prepare);
            }
            case 1 -> useItem();
            case 2 -> onServer(this::collect);
            case 3 -> useItem();
            case 4 -> onServer(player -> {
                verifyDose(player, doses - 1, "首次自然服用");
                verifyEffect(player);
                saved = player.getMainHandItem().copy();
            });
            case 5 -> leaveWorld();
            case 8 -> onServer(player -> {
                expectTrue("真实保存重进保留宝箱丹药全部组件及余量", ItemStack.matches(saved, player.getMainHandItem()));
                expectTrue("重进后宝箱丹药组件双端一致", ItemStack.matches(player.getMainHandItem(), minecraft.player.getMainHandItem()));
                expect("重进后药瓶仍仅一件", 1, player.getMainHandItem().getCount());
            });
            default -> throw new IllegalStateException("未知宝箱丹药消费检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        player.stopUsingItem();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        shift = player.isShiftKeyDown();
        absorption = player.getAbsorptionAmount();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        player.getActiveEffects().forEach(value -> effects.add(new MobEffectInstance(value)));
        player.getFoodData().addAdditionalSaveData(food);
        player.removeAllEffects();
        player.setAbsorptionAmount(0);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        minecraft.options.keyShift.setDown(false);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        area = new AABB(player.blockPosition()).inflate(8);
        clearDrops(player);
        ItemStack chest = TreasureContainers.create(item("treasure_chest_01"), "药架", "状态类丹药堆", 1, 1, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, chest);
        player.inventoryMenu.broadcastChanges();
    }

    private void collect(ServerPlayer player) {
        expectTrue("01自然使用消耗唯一宝箱载体", player.getMainHandItem().isEmpty());
        List<ItemEntity> drops = drops(player);
        expect("状态丹药表实际生成一件掉落实体", 1, drops.size());
        if (drops.size() != 1) {
            cleanup(player);
            finish();
            return;
        }
        ItemStack reward = drops.getFirst().getItem().copy();
        MedicineInstance data = Liandan.medicine(reward);
        candidate = candidate(data.name());
        expectTrue("状态丹药随机结果属于来源一级三候选", candidate != null);
        if (candidate == null) {
            cleanup(player);
            finish();
            return;
        }
        carrier = item(candidate.carrier());
        doses = data.doses();
        expect("随机候选原始中文丹名", candidate.name(), data.name());
        expect("来源药效数量", candidate.effects().size(), data.effects().size());
        boolean effectsMatch = data.effects().size() == candidate.effects().size();
        for (int index = 0; index < candidate.effects().size(); index++) {
            EffectSpec expected = candidate.effects().get(index);
            MedicineInstance.Effect actual = index < data.effects().size() ? data.effects().get(index) : new MedicineInstance.Effect("", 0, 0);
            expect("来源药效" + index, expected.id(), actual.id());
            expect("来源药效等级" + index, expected.level(), actual.level());
            expect("来源药效时间" + index, expected.tick(), actual.duration());
            effectsMatch &= expected.id().equals(actual.id())
                    && expected.level() == actual.level()
                    && expected.tick() == actual.duration();
        }
        expectTrue("奖励物品与来源药瓶一致且仅一件", reward.is(carrier) && reward.getCount() == 1);
        expect("奖励自定义名中文一致", candidate.name(), reward.getHoverName().getString());
        expectTrue("药瓶按宝箱单位保存且来源四至十六剂", data.treasure() && doses >= 4 && doses <= 16);
        if (!reward.is(carrier) || reward.getCount() != 1 || data.effects().size() != candidate.effects().size()
                || !data.treasure()
                || !effectsMatch || doses < 4 || doses > 16) {
            cleanup(player);
            finish();
            return;
        }
        drops.getFirst().discard();
        player.setItemInHand(InteractionHand.MAIN_HAND, reward);
        player.inventoryMenu.broadcastChanges();
    }

    private void verifyDose(ServerPlayer player, int remaining, String label) {
        ItemStack actual = player.getMainHandItem();
        if (remaining > 0) {
            MedicineInstance medicine = Liandan.medicine(actual);
            expect(label + "仅扣一剂", remaining, medicine.doses());
            expectTrue(label + "保留唯一原药瓶及宝箱标志", actual.is(carrier) && actual.getCount() == 1
                    && medicine.treasure());
        } else {
            expectTrue("服尽后返一件同类新空容器", actual.is(carrier) && actual.getCount() == 1);
            expectTrue("服尽后空容器舍弃原丹名与实例字段", !actual.has(DataComponents.CUSTOM_NAME)
                    && !actual.has(LiandanData.MEDICINE.get()) && !actual.has(DataComponents.CUSTOM_DATA));
        }
        expectTrue(label + "药瓶数量与实例双端一致", ItemStack.matches(actual, minecraft.player.getMainHandItem()));
        expect("整条消费路径没有新增其他库存栈", 1,
                player.getInventory().items.stream().filter(stack -> !stack.isEmpty()).count());
    }

    private void verifyEffect(ServerPlayer player) {
        for (EffectSpec expected : candidate.effects()) {
            Holder<MobEffect> holder = effectHolder(expected.id());
            if (holder == null || expected.tick() <= 0 || expected.id().equals("instant_health")) continue;
            MobEffectInstance active = player.getEffect(holder);
            expectTrue("真实服用已调用可观测药效 " + expected.id(), active != null);
            if (active == null) continue;
            expect("药效 " + expected.id() + " 产生零基放大值", expected.level() - 1, active.getAmplifier());
            int maximumTicks = expected.id().equals("saturation") ? expected.tick() : expected.tick() * 20;
            expectTrue("药效 " + expected.id() + " 实际时长符合药效单位", active.getDuration() > maximumTicks - 40
                    && active.getDuration() <= maximumTicks);
            if (expected.id().equals("absorption")) expectTrue("铁肤丹实际授予伤害吸收", player.getAbsorptionAmount() > 0);
        }
    }

    private void verifyFeeding(ServerPlayer player) {
        Cow cow = EntityType.COW.create(player.serverLevel());
        cow.setPos(player.position());
        player.serverLevel().addFreshEntity(cow);
        try {
            ItemStack bottle = new ItemStack(item("itemdanyaohulu_01"));
            Liandan.setMedicine(bottle, new MedicineInstance("", "", "", 2, false, List.of(
                    new MedicineInstance.Effect("speed", 100, 0),
                    new MedicineInstance.Effect("resistance", 900, 201))));
            player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
            player.setShiftKeyDown(true);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, cow));
            MobEffectInstance resistance = cow.getEffect(MobEffects.DAMAGE_RESISTANCE);
            expectTrue("喂药不施加零秒效果，继续对目标施加未封顶的十秒抗性九级", !cow.hasEffect(MobEffects.MOVEMENT_SPEED)
                    && resistance != null && resistance.getAmplifier() == 8 && resistance.getDuration() == 200);
            expectTrue("喂药扣一剂并回写原零时长", Liandan.medicine(bottle).doses() == 1
                    && Liandan.medicine(bottle).effects().getFirst().duration() == 1);
        } finally {
            cow.discard();
        }
    }

    private static Holder<MobEffect> effectHolder(String id) {
        ResourceLocation location = id.indexOf(':') >= 0 ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(id.startsWith("effect_") ? ModId.VALUE : "minecraft", id);
        return BuiltInRegistries.MOB_EFFECT.getHolder(location).orElse(null);
    }

    private static Candidate candidate(String name) {
        return switch (name) {
            case "一级辟谷丹" -> new Candidate(name, "itemdanyaohulu_01", List.of(new EffectSpec("saturation", 1, 120)));
            case "一级避水丹" -> new Candidate(name, "itemdanyaohulu_01", List.of(
                    new EffectSpec("water_breathing", 1, 120)));
            case "一级铁肤丹" -> new Candidate(name, "itemdanyaohulu_06", List.of(
                    new EffectSpec("absorption", 1, 120)));
            default -> null;
        };
    }

    private record Candidate(String name, String carrier, List<EffectSpec> effects) {
        private Candidate {
            effects = List.copyOf(effects);
        }
    }

    private record EffectSpec(String id, int level, int tick) {
    }

    private void useItem() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, area);
    }

    private void clearDrops(ServerPlayer player) {
        drops(player).forEach(ItemEntity::discard);
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private void cleanup(ServerPlayer player) {
        clearDrops(player);
        player.stopUsingItem();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.removeAllEffects();
        effects.forEach(player::addEffect);
        player.setAbsorptionAmount(absorption);
        player.getFoodData().readAdditionalSaveData(food);
        player.setGameMode(mode);
        player.setShiftKeyDown(shift);
        minecraft.options.keyShift.setDown(shift);
        minecraft.options.keyUse.setDown(useKey);
        player.inventoryMenu.broadcastChanges();
    }
}
