package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.equipment.content.FlyingInstrumentEntity;

/** 八种飞行法器分别检查来源补能、实体和物品保存、潜行取回、死亡掉落及一点生命重召唤。 */
final class FlyingInstrumentDeathCheck extends ActionCheck {
    private record Message(String key, List<String> arguments) {
    }

    private static final String POWER_MESSAGE = "message.yvanchuxiuzhen.flyinginstrument.power";
    private static final String FULL_MESSAGE = "message.yvanchuxiuzhen.flyinginstrument.full";
    private static final List<String> ITEMS = List.of("flyinginstrument_0101", "flyinginstrument_0201",
            "flyinginstrument_0301", "flyinginstrument_0401", "flyinginstrument_0501", "flyinginstrument_0601",
            "flyinginstrument_0701", "flyinginstrument_0801");
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<Entity> created = new ArrayList<>();
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final List<Message> expectedMessages = new ArrayList<>();
    private GameType gameType;
    private boolean shifting;
    private int selected;
    private AABB area;

    FlyingInstrumentDeathCheck(List<String> report, String levelId) {
        super("飞行法器死亡恢复", report, levelId);
        NeoForge.EVENT_BUS.addListener(this::captureChat);
        script(new Action(10, this::lifecycles), new Action(1, this::checkMessages));
    }

    private void lifecycles(ServerPlayer player) {
        gameType = player.gameMode.getGameModeForPlayer();
        shifting = player.isShiftKeyDown();
        selected = player.getInventory().selected;
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        area = player.getBoundingBox().inflate(16);
        for (String path : ITEMS) lifecycle(player, path);
    }

    private void lifecycle(ServerPlayer player, String path) {
        ItemStack initial = item(path);
        CompoundTag marker = new CompoundTag();
        marker.putInt("smoke_marker", 27);
        initial.set(DataComponents.CUSTOM_DATA, CustomData.of(marker));
        initial.set(DataComponents.CUSTOM_NAME, Component.literal(path + "保存"));
        FlyingInstrumentEntity entity = summon(player, initial);
        expectTrue(path + "物品使用召唤对应实体并清空手持槽", entity != null
                && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals(path.substring(0, path.length() - 2))
                && player.getMainHandItem().isEmpty());
        if (entity == null) return;

        recharge(player, entity, path);
        entity.getPersistentData().putDouble("power", 73.25);
        entity.setHealth(7.5f);
        CompoundTag saved = entity.saveWithoutId(new CompoundTag());
        if (path.equals(ITEMS.getFirst())) checkPreviousSave(player, initial, entity, saved);
        FlyingInstrumentEntity loaded = (FlyingInstrumentEntity) entity.getType().create(player.serverLevel());
        loaded.load(saved);
        created.add(loaded);
        entity.discard();
        expectTrue(path + "实体保存重读保留当前生命灵力及完整原物品", loaded.getHealth() == 7.5
                && loaded.getPersistentData().getDouble("power") == 73.25
                && ItemStack.isSameItemSameComponents(loaded.getMainHandItem(), entity.getMainHandItem()));

        player.setShiftKeyDown(true);
        loaded.mobInteract(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        ItemStack returned = inventoryItem(player, initial);
        expectTrue(path + "潜行取回保留原组件并写入当前生命灵力", loaded.isRemoved()
                && data(returned).getInt("smoke_marker") == 27
                && initial.get(DataComponents.CUSTOM_NAME).equals(returned.get(DataComponents.CUSTOM_NAME))
                && data(returned).getDouble("health") == 7.5 && data(returned).getDouble("power") == 73.25);
        if (returned.isEmpty()) return;

        ItemStack restoredItem = ItemStack.parse(player.registryAccess(), returned.save(player.registryAccess())).orElseThrow();
        player.getInventory().clearContent();
        entity = summon(player, restoredItem);
        expectTrue(path + "取回物品保存重读后重召保留受损生命灵力", entity != null && entity.getHealth() == 7.5
                && entity.getPersistentData().getDouble("power") == 73.25
                && ItemStack.isSameItemSameComponents(returned, restoredItem));
        if (entity == null) return;

        Set<UUID> previousDrops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .map(Entity::getUUID).collect(Collectors.toSet());
        entity.setHealth(1);
        entity.hurt(player.damageSources().generic(), 2);
        List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, area,
                dropped -> !previousDrops.contains(dropped.getUUID()));
        created.addAll(drops);
        List<ItemEntity> deathDrops = drops.stream().filter(dropped -> dropped.getItem().is(initial.getItem()))
                .filter(dropped -> data(dropped.getItem()).getDouble("health") == 1).toList();
        expectTrue(path + "死亡生成独立的一点生命法器物品", !entity.isAlive() && deathDrops.size() == 1);
        if (deathDrops.size() != 1) return;

        ItemEntity dropped = deathDrops.getFirst();
        CompoundTag dropData = data(dropped.getItem());
        expectTrue(path + "死亡掉落保留八字段且拾取延迟精确十刻", dropped.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 10
                && dropped.getY() == entity.getY() + 1 && dropData.getDouble("power") == 73.25
                && dropData.getDouble("health_Max") == entity.getMaxHealth()
                && dropData.getDouble("power_Max") == data(returned).getDouble("power_Max")
                && dropData.getDouble("speed_num") == data(returned).getDouble("speed_num")
                && dropData.getString("flyname").equals(data(returned).getString("flyname"))
                && dropData.getString("flyitemID").equals(data(returned).getString("flyitemID"))
                && dropData.getString("flyentityID").equals(data(returned).getString("flyentityID"))
                && !dropData.contains("smoke_marker") && !dropped.getItem().has(DataComponents.CUSTOM_NAME));
        ItemStack recovered = ItemStack.parse(player.registryAccess(), dropped.getItem().save(player.registryAccess())).orElseThrow();
        expectTrue(path + "死亡物品完整组件保存重读一致", ItemStack.isSameItemSameComponents(dropped.getItem(), recovered));
        dropped.discard();
        FlyingInstrumentEntity resummoned = summon(player, recovered);
        expectTrue(path + "死亡物品重召保持一点生命和储存灵力", resummoned != null && resummoned.getHealth() == 1
                && resummoned.getPersistentData().getDouble("power") == 73.25);
        created.forEach(Entity::discard);
        player.getInventory().clearContent();
    }

    private void checkPreviousSave(ServerPlayer player, ItemStack initial, FlyingInstrumentEntity entity, CompoundTag saved) {
        CompoundTag previous = saved.copy();
        previous.remove("HandItems");
        FlyingInstrumentEntity loaded = (FlyingInstrumentEntity) entity.getType().create(player.serverLevel());
        loaded.load(previous);
        player.setShiftKeyDown(true);
        loaded.mobInteract(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        ItemStack returned = inventoryItem(player, initial);
        expectTrue("补丁前空装备槽的已投放法器仍能取回", loaded.isRemoved() && !returned.isEmpty()
                && data(returned).getDouble("power") == 73.25 && data(returned).getDouble("health") == 7.5
                && !returned.has(DataComponents.CUSTOM_NAME));
        player.getInventory().clearContent();
    }

    private void recharge(ServerPlayer player, FlyingInstrumentEntity entity, String path) {
        CompoundTag state = entity.getPersistentData();
        double maximum = state.getDouble("power_Max");
        String formattedMaximum = Integer.toString((int) maximum);
        ItemStack reserve = item("cailiao_191");
        reserve.setCount(3);
        reserve.set(DataComponents.CUSTOM_NAME, Component.literal("先扣库存"));
        ItemStack held = item("cailiao_191");
        held.setCount(2);
        held.set(DataComponents.CUSTOM_NAME, Component.literal("后扣主手"));
        player.getInventory().setItem(0, reserve);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        state.putDouble("power", 0);
        entity.mobInteract(player, InteractionHand.MAIN_HAND);
        expectedMessages.add(new Message(POWER_MESSAGE, List.of("1", formattedMaximum)));
        expectTrue(path + "补能按物品身份从最早匹配库存槽扣一件", state.getDouble("power") == 1
                && reserve.getCount() == 2 && held.getCount() == 2 && !player.isPassenger());

        state.putDouble("power", maximum - 1);
        entity.mobInteract(player, InteractionHand.MAIN_HAND);
        expectedMessages.add(new Message(POWER_MESSAGE, List.of(formattedMaximum, formattedMaximum)));
        expectTrue(path + "补能恰好到上限仍只扣一件", state.getDouble("power") == maximum
                && reserve.getCount() == 1 && held.getCount() == 2);

        player.getInventory().clearContent();
        held = item("cailiao_192");
        held.setCount(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        state.putDouble("power", maximum - 1);
        entity.mobInteract(player, InteractionHand.MAIN_HAND);
        expectedMessages.add(new Message(POWER_MESSAGE, List.of(Integer.toString((int) maximum + 3), formattedMaximum)));
        expectedMessages.add(new Message(FULL_MESSAGE, List.of()));
        expectTrue(path + "补能超过上限时扣一件并最终钳到上限", state.getDouble("power") == maximum && held.getCount() == 1);

        entity.mobInteract(player, InteractionHand.MAIN_HAND);
        expectedMessages.add(new Message(POWER_MESSAGE, List.of(formattedMaximum, formattedMaximum)));
        expectTrue(path + "已满储灵交互不扣料或进入骑乘", state.getDouble("power") == maximum && held.getCount() == 1
                && !player.isPassenger());

        state.putDouble("power", maximum + 4);
        entity.mobInteract(player, InteractionHand.MAIN_HAND);
        expectedMessages.add(new Message(POWER_MESSAGE, List.of(Integer.toString((int) maximum + 4), formattedMaximum)));
        expectedMessages.add(new Message(FULL_MESSAGE, List.of()));
        expectTrue(path + "已有超限储灵交互只钳值且不额外扣料", state.getDouble("power") == maximum && held.getCount() == 1);
        player.getInventory().clearContent();
    }

    private void captureChat(ClientChatReceivedEvent.System event) {
        if (event.getMessage().getContents() instanceof TranslatableContents content
                && (content.getKey().equals(POWER_MESSAGE) || content.getKey().equals(FULL_MESSAGE))) {
            messages.add(new Message(content.getKey(), Arrays.stream(content.getArgs()).map(Object::toString).toList()));
        }
    }

    private void checkMessages(ServerPlayer player) {
        expectTrue("八类补能客户端收到未钳储灵值和补完提示且顺序符合来源", messages.equals(expectedMessages));
        if (!messages.equals(expectedMessages)) expect("实际补能消息序列", expectedMessages, messages);
    }

    private FlyingInstrumentEntity summon(ServerPlayer player, ItemStack stack) {
        Set<UUID> previous = player.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area).stream()
                .map(Entity::getUUID).collect(Collectors.toSet());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        List<FlyingInstrumentEntity> summoned = player.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area,
                entity -> !previous.contains(entity.getUUID()));
        created.addAll(summoned);
        return summoned.size() == 1 ? summoned.getFirst() : null;
    }

    private static ItemStack inventoryItem(ServerPlayer player, ItemStack expected) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(expected.getItem())) return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path)));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        created.forEach(Entity::discard);
        for (int slot = 0; slot < inventory.size(); slot++) {
            player.getInventory().setItem(slot, inventory.get(slot));
        }
        player.setShiftKeyDown(shifting);
        player.getInventory().selected = selected;
        player.setGameMode(gameType);
        player.inventoryMenu.broadcastChanges();
    }
}
