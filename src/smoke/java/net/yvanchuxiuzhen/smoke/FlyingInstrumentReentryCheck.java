package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.FlyingInstrumentData;
import net.yvanchuxiuzhen.system.equipment.content.FlyingInstrumentEntity;

/** 普通主手投放飞叶，真实退出并重开同档后，由原玩家潜行取回该 UUID 的法器。 */
final class FlyingInstrumentReentryCheck extends SystemCheck {
    private static final ResourceLocation ITEM = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "flyinginstrument_0101");
    private static final Component NAME = Component.literal("归舟·同档重逢");
    private static final String MARK = "smoke_flying_reentry";
    private static final float HEALTH = 7.5f;
    private static final double POWER = 73.25;
    private final UUID mark = UUID.randomUUID();
    private UUID playerId;
    private UUID instrumentId;
    private ResourceKey<Level> dimension;
    private int selected;
    private GameType mode;
    private boolean shifting;
    private boolean completed;

    FlyingInstrumentReentryCheck(List<String> report, String levelId) {
        super("飞行法器同档重进", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(this::deploy);
                if (instrumentId == null) finish();
                else leaveWorld();
            }
            case 1 -> { return reopened(local); }
            case 2 -> { return inWorld(local); }
            case 3 -> {
                if (local % 10 != 0 && local < 200) return false;
                onServer(player -> recall(player, local >= 200));
                if (!completed) return false;
                finish();
            }
            default -> throw new IllegalStateException("未知重进步骤 " + step);
        }
        return true;
    }

    private void deploy(ServerPlayer player) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).isEmpty()) { slot = i; break; }
        }
        if (slot < 0) { fail("需要一个空快捷栏槽，未改动玩家背包"); return; }
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        shifting = player.isShiftKeyDown();
        dimension = player.level().dimension();
        playerId = player.getUUID();
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.getInventory().selected = slot;
        ItemStack initial = new ItemStack(BuiltInRegistries.ITEM.get(ITEM));
        initial.set(DataComponents.CUSTOM_NAME, NAME);
        initial.set(DataComponents.REPAIR_COST, 7);
        CustomData.update(DataComponents.CUSTOM_DATA, initial, tag -> tag.putUUID(MARK, mark));
        player.setItemInHand(InteractionHand.MAIN_HAND, initial);
        var area = player.getBoundingBox().inflate(4);
        List<UUID> before = player.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area).stream()
                .map(Entity::getUUID).toList();
        player.gameMode.useItem(player, player.level(), initial, InteractionHand.MAIN_HAND);
        List<FlyingInstrumentEntity> spawned = player.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area,
                entity -> !before.contains(entity.getUUID()));
        if (spawned.size() != 1 || !player.getMainHandItem().isEmpty()) {
            fail("主手使用后应只新增一台法器并清空手持槽；新增=" + spawned.size());
            spawned.forEach(Entity::discard);
            restore(player);
            return;
        }
        FlyingInstrumentEntity entity = spawned.getFirst();
        instrumentId = entity.getUUID();
        entity.setHealth(HEALTH);
        entity.setPower(POWER);
        expectTrue("投放原件的标记、改名和额外组件已进入实体 " + instrumentId, original(entity.getMainHandItem()));
    }

    private void recall(ServerPlayer player, boolean timeout) {
        if (!playerId.equals(player.getUUID()) || !dimension.equals(player.level().dimension())) {
            fail("重进后不是投放玩家或所在维度");
            completed = true;
            return;
        }
        Entity found = player.serverLevel().getEntity(instrumentId);
        if (found == null && !timeout) return;
        try {
            if (!(found instanceof FlyingInstrumentEntity entity)) {
                fail("重进后未加载原 UUID 的飞行法器 " + instrumentId);
                return;
            }
            expectTrue("同一 UUID 实体重进后仍持有原件 " + instrumentId, original(entity.getMainHandItem()));
            expect("重进实体生命", HEALTH, entity.getHealth());
            expect("重进实体储灵", POWER, entity.power());
            expect("取回前背包内没有本次标记物品", 0, markedCount(player));
            player.setShiftKeyDown(true);
            player.interactOn(entity, InteractionHand.MAIN_HAND);
            expectTrue("潜行交互移除已记录的实体", entity.isRemoved());
            expect("取回后背包恰有一件本次标记物品", 1, markedCount(player));
            for (ItemStack stack : player.getInventory().items) {
                if (!marked(stack)) continue;
                expectTrue("取回同型法器并保留完整自定义名和额外组件", original(stack));
                FlyingInstrumentData data = Equipment.flyingInstrument(stack);
                expect("取回物品生命", HEALTH, data.health());
                expect("取回物品储灵", POWER, data.power());
            }
            if (!entity.isRemoved()) entity.discard();
        } finally {
            restore(player);
            completed = true;
        }
    }

    private boolean marked(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.hasUUID(MARK) && mark.equals(data.getUUID(MARK));
    }

    private boolean original(ItemStack stack) {
        return marked(stack) && stack.is(BuiltInRegistries.ITEM.get(ITEM)) && stack.getCount() == 1
                && NAME.equals(stack.get(DataComponents.CUSTOM_NAME)) && stack.getOrDefault(DataComponents.REPAIR_COST, 0) == 7;
    }

    private int markedCount(ServerPlayer player) {
        return player.getInventory().items.stream().filter(this::marked).mapToInt(ItemStack::getCount).sum();
    }

    private void restore(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (marked(player.getInventory().getItem(i))) player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        player.getInventory().selected = selected;
        player.setShiftKeyDown(shifting);
        player.setGameMode(mode);
        player.inventoryMenu.broadcastChanges();
    }
}
