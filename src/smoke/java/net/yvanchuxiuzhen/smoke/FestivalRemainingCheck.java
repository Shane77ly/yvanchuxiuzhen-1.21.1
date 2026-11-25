package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.collection.festival.FestivalData;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.collection.festival.ThrownFirework;
import net.yvanchuxiuzhen.platform.ModId;

/** 礼包、红包、胸甲计时及三类抛出烟花的自然入口与实例保存检查。 */
final class FestivalRemainingCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> spawned = new ArrayList<>();
    private GameType mode;
    private int selected;
    private float yaw;
    private float pitch;

    FestivalRemainingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("节日剩余族", report, levelId);
        script(
                new Action(2, this::prepare),
                new Action(2, this::gifts),
                new Action(2, this::redPacket),
                new Action(2, this::costume),
                new Action(2, this::thrown),
                new Action(25, player -> {}),
                new Action(2, player -> minecraft.execute(() -> screenshots.take("festival_remaining.png")))
        );
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        yaw = player.getYRot();
        pitch = player.getXRot();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
    }

    private void gifts(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item("fuqi_05", 3));
        expect("礼包自然右键保持来源PASS", InteractionResult.PASS, use(player, InteractionHand.MAIN_HAND));
        expect("生存礼包扣一件", 2, player.getMainHandItem().getCount());
        for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) expect("礼包发放" + slot, 1, count(player, "fuqi_02_" + slot));
        player.setGameMode(GameType.CREATIVE);
        use(player, InteractionHand.MAIN_HAND);
        expect("创造礼包发奖且保持数量", 2, player.getMainHandItem().getCount());
        for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) expect("创造礼包仍发放" + slot, 2, count(player, "fuqi_02_" + slot));
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, item("fuqi_05", 2));
        use(player, InteractionHand.OFF_HAND);
        expect("副手礼包扣副手一件", 1, player.getOffhandItem().getCount());
        expect("副手礼包保持主手数量", 2, player.getMainHandItem().getCount());
        for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) expect("副手礼包发放" + slot, 3, count(player, "fuqi_02_" + slot));
        player.getInventory().clearContent();
    }

    private void redPacket(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item("fuqi_06", 3));
        for (GameType gameType : List.of(GameType.SURVIVAL, GameType.CREATIVE)) {
            player.setGameMode(gameType);
            Set<UUID> before = new HashSet<>();
            drops(player).forEach(entity -> before.add(entity.getUUID()));
            expect(gameType + " 红包自然右键保持PASS", InteractionResult.PASS, use(player, InteractionHand.MAIN_HAND));
            expect(gameType + " 红包来源扣件结果", 2, player.getMainHandItem().getCount());
            List<ItemEntity> rewards = drops(player).stream().filter(entity -> !before.contains(entity.getUUID())).toList();
            expectTrue(gameType + " 红包自然入口逐个生成奖励", !rewards.isEmpty());
            if (!rewards.isEmpty()) {
                ItemStack first = rewards.getFirst().getItem();
                expectTrue(gameType + " 单次红包全部掉落同一种单件奖励且有拾取延迟", rewards.stream().allMatch(entity ->
                        ItemStack.isSameItemSameComponents(first, entity.getItem()) && entity.getItem().getCount() == 1 && entity.hasPickUpDelay()));
            }
            rewards.forEach(Entity::discard);
        }
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
    }

    private void costume(ServerPlayer player) {
        ItemStack chest = item("fuqi_02_chestplate", 1);
        chest.set(FestivalData.COSTUME_TICK.get(), 7.0);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        expect("只穿胸甲即可推进金币计时", 8, costumeTick(chest));
        chest = ItemStack.parse(player.registryAccess(), chest.save(player.registryAccess())).orElseThrow();
        expect("胸甲物品存储保留计时", 8, costumeTick(chest));
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        player.getInventory().setItem(9, chest);
        for (int tick = 0; tick < 5; tick++) NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        expect("脱下胸甲后计时暂停", 8, costumeTick(chest));
        player.getInventory().setItem(9, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        chest.set(FestivalData.COSTUME_TICK.get(), 19.0);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        expect("穿回胸甲在二十刻周期归零", 0, costumeTick(chest));
        for (String slot : List.of("helmet", "leggings", "boots")) {
            EquipmentSlot equipment = switch (slot) {
                case "helmet" -> EquipmentSlot.HEAD;
                case "leggings" -> EquipmentSlot.LEGS;
                default -> EquipmentSlot.FEET;
            };
            player.setItemSlot(equipment, item("fuqi_02_" + slot, 1));
        }
        player.inventoryMenu.broadcastChanges();
    }

    private void thrown(ServerPlayer player) {
        player.setYRot(0);
        player.setXRot(-20);
        for (ThrownFirework.Kind kind : ThrownFirework.Kind.values()) {
            String itemId = kind.id().substring(0, 6);
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, item(itemId, 3));
            naturalThrow(player, kind, InteractionHand.MAIN_HAND);
            expect(itemId + " 生存抛出扣一件", 2, player.getMainHandItem().getCount());
            player.setGameMode(GameType.CREATIVE);
            naturalThrow(player, kind, InteractionHand.MAIN_HAND);
            expect(itemId + " 创造抛出保持数量", 2, player.getMainHandItem().getCount());
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.OFF_HAND, item(itemId, 2));
            naturalThrow(player, kind, InteractionHand.OFF_HAND);
            expect(itemId + " 副手抛出只扣副手", 1, player.getOffhandItem().getCount());
            expect(itemId + " 副手抛出保持主手数量", 2, player.getMainHandItem().getCount());
            lifecycle(player, kind);
        }
    }

    private void naturalThrow(ServerPlayer player, ThrownFirework.Kind kind, InteractionHand hand) {
        Set<UUID> before = new HashSet<>();
        fireworks(player).forEach(entity -> before.add(entity.getUUID()));
        expect(kind.id() + " 自然抛出保持来源PASS", InteractionResult.PASS, use(player, hand));
        List<ThrownFirework> created = fireworks(player).stream().filter(entity -> !before.contains(entity.getUUID())).toList();
        expect(kind.id() + " 自然使用只生成一个对应实体", 1, created.size());
        for (ThrownFirework entity : created) {
            spawned.add(entity.getUUID());
            expect(kind.id() + " 自然使用实体身份", kind.id(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath());
            expect(kind.id() + " 抛出高度", player.getY() + 1.4, entity.getY());
            expect(kind.id() + " 初速度为完整视线向量", player.getLookAngle(), entity.getDeltaMovement());
        }
    }

    private void lifecycle(ServerPlayer player, ThrownFirework.Kind kind) {
        ThrownFirework entity = FestivalSetup.thrownFireworkType(kind).get().create(player.level());
        entity.moveTo(player.getX(), player.getY() + 2, player.getZ(), 0, 0);
        for (int tick = 0; tick < 19; tick++) entity.baseTick();
        expectTrue(kind.id() + " 第十九刻尚未引燃", !entity.lit());
        CompoundTag saved = new CompoundTag();
        entity.save(saved);
        entity = (ThrownFirework) EntityType.loadEntityRecursive(saved, player.level(), restored -> restored);
        entity.baseTick();
        expectTrue(kind.id() + " 存储重读后第二十刻引燃并归零", entity.lit() && entity.fireworkTicks() == 0);
        entity.baseTick();
        if (kind == ThrownFirework.Kind.PAO_05) expect("五号引燃下一刻向上速度", .78, entity.getDeltaMovement().y);
        if (kind == ThrownFirework.Kind.PAO_06) expect("六号引燃下一刻向上速度", .06, entity.getDeltaMovement().y);
        expectTrue(kind.id() + " 摔落伤害免疫", !entity.hurt(player.damageSources().fall(), 1));
        Arrow arrow = EntityType.ARROW.create(player.level());
        boolean arrowHurt = entity.hurt(player.damageSources().arrow(arrow, player), 1);
        expectTrue(kind.id() + " 箭伤免疫只适用四号", arrowHurt == (kind != ThrownFirework.Kind.PAO_04));
        int lastTick = switch (kind) {
            case PAO_04 -> 199;
            case PAO_05 -> 39;
            case PAO_06 -> 159;
        };
        entity.setFireworkTicks(lastTick - 1);
        entity.baseTick();
        expectTrue(kind.id() + " 燃放末刻前保留实体", !entity.isRemoved());
        entity.save(saved = new CompoundTag());
        entity = (ThrownFirework) EntityType.loadEntityRecursive(saved, player.level(), restored -> restored);
        entity.baseTick();
        expectTrue(kind.id() + " 燃放状态重读后到期销毁", entity.isRemoved());
    }

    private List<ThrownFirework> fireworks(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ThrownFirework.class, player.getBoundingBox().inflate(16));
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4));
    }

    private InteractionResult use(ServerPlayer player, InteractionHand hand) {
        return player.gameMode.useItem(player, player.level(), player.getItemInHand(hand), hand);
    }

    private static ItemStack item(String id, int amount) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), amount);
    }

    private static double costumeTick(ItemStack stack) {
        return stack.getOrDefault(FestivalData.COSTUME_TICK.get(), 0.0);
    }

    private static int count(ServerPlayer player, String id) {
        return player.getInventory().countItem(item(id, 1).getItem());
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        for (UUID uuid : spawned) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
