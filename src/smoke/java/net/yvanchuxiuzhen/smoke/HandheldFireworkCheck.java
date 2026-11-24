package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.collection.festival.FestivalData;
import net.yvanchuxiuzhen.collection.festival.HandheldFireworkState;
import net.yvanchuxiuzhen.collection.tiandao.NbtEditorData;
import net.yvanchuxiuzhen.platform.ModId;

/** 手持烟花的自然右键分栈、选择槽生命周期、组件保存和创造模式来源分支。 */
final class HandheldFireworkCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private ItemStack burning;
    private double pausedTick;
    private int selected;
    private GameType mode;

    HandheldFireworkCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("手持烟花", report, levelId);
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(8, player -> {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                inventory.add(player.getInventory().getItem(slot).copy());
            }
            selected = player.getInventory().selected;
            mode = player.gameMode.getGameModeForPlayer();
            player.closeContainer();
            player.setGameMode(GameType.SURVIVAL);
        }));
        for (String id : List.of("pao_02", "pao_03", "pao_07")) {
            actions.add(new Action(8, player -> {
                player.getInventory().clearContent();
                select(player, 0);
                ItemStack input = item(id, 4);
                input.set(FestivalData.FIREWORK_STATE.get(), new HandheldFireworkState(false, 37));
                CompoundTag marker = new CompoundTag();
                marker.putString("smoke_marker", "保留原栈");
                input.set(NbtEditorData.TAGS.get(), marker);
                player.getInventory().setItem(0, input);
                player.inventoryMenu.broadcastChanges();
            }));
            actions.add(new Action(8, player -> minecraft.execute(() ->
                    minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND))));
            actions.add(new Action(6, player -> {
                burning = player.getMainHandItem();
                expectTrue(id + " 自然右键留下点燃的一件并继续已有计时", burning.getCount() == 1
                        && state(burning).lit() && state(burning).ticks() > 37);
                expect(id + " 原栈保留其他组件", "保留原栈", marker(burning));
                expect(id + " 分出三件无点燃或原栈数据的同种物品", 3, blankCount(player, id));
                String name = switch (id) {
                    case "pao_02" -> "电光花";
                    case "pao_03" -> "呲花";
                    default -> "喷花";
                };
                expect(id + " 合并语言中文名称", name, minecraft.player.getMainHandItem().getHoverName().getString());
                minecraft.execute(() -> screenshots.take("festival_" + id + ".png"));
                pausedTick = state(burning).ticks();
                int unlitSlot = blankSlot(player, id);
                ItemStack unlit = player.getInventory().getItem(unlitSlot);
                player.getInventory().setItem(0, ItemStack.EMPTY);
                player.getInventory().setItem(9, burning);
                player.getInventory().setItem(unlitSlot, ItemStack.EMPTY);
                player.getInventory().setItem(0, unlit);
                player.inventoryMenu.broadcastChanges();
            }));
            actions.add(new Action(6, player -> {
                expect(id + " 收起后计时暂停", pausedTick, state(burning).ticks());
                burning = ItemStack.parse(player.registryAccess(), burning.save(player.registryAccess())).orElseThrow();
                expectTrue(id + " 物品序列化保留点燃与计时", state(burning).lit()
                        && state(burning).ticks() == pausedTick && marker(burning).equals("保留原栈"));
                ItemStack unlit = player.getMainHandItem();
                ItemStack unlitRestored = ItemStack.parse(player.registryAccess(), unlit.save(player.registryAccess())).orElseThrow();
                expectTrue(id + " 来源选中未点燃栈写入并保存空标签且阻止合栈",
                        unlit.has(FestivalData.FIREWORK_STATE.get()) && !state(unlit).lit()
                                && unlitRestored.has(FestivalData.FIREWORK_STATE.get()) && !state(unlitRestored).lit()
                                && !ItemStack.isSameItemSameComponents(unlitRestored, item(id, 3)));
                player.getInventory().setItem(9, ItemStack.EMPTY);
                player.getInventory().setItem(Inventory.SLOT_OFFHAND, burning);
                select(player, 1);
                player.inventoryMenu.broadcastChanges();
            }));
            actions.add(new Action(6, player -> {
                expect(id + " 第二快捷栏选中时副手燃烧暂停", pausedTick, state(burning).ticks());
                select(player, 0);
            }));
            actions.add(new Action(2, player -> {
                expectTrue(id + " 第一快捷栏选中时副手按来源继续燃烧", state(burning).ticks() > pausedTick);
                double before = state(burning).ticks();
                InteractionResult result = player.gameMode.useItem(player, player.level(), burning, InteractionHand.OFF_HAND);
                expect(id + " 重复点燃保持PASS结果", InteractionResult.PASS, result);
                expect(id + " 重复点燃不清零计时", before, state(burning).ticks());
                burning.set(FestivalData.FIREWORK_STATE.get(), new HandheldFireworkState(true, id.equals("pao_07") ? 1199 : 599));
                burning.getItem().inventoryTick(burning, player.level(), player, Inventory.SLOT_OFFHAND, false);
                expectTrue(id + " 燃放到600或1200刻消费最后一件", burning.isEmpty());
            }));
        }
        actions.add(new Action(2, player -> {
            player.getInventory().clearContent();
            select(player, 0);
            player.setGameMode(GameType.CREATIVE);
            ItemStack stack = item("pao_02", 4);
            player.getInventory().setItem(0, stack);
            player.gameMode.useItem(player, player.level(), stack, InteractionHand.MAIN_HAND);
            expectTrue("创造模式保留来源整栈四件点燃并额外分出三件", stack.getCount() == 4
                    && state(stack).lit() && blankCount(player, "pao_02") == 3);
            stack.set(FestivalData.FIREWORK_STATE.get(), new HandheldFireworkState(true, 599));
            stack.getItem().inventoryTick(stack, player.level(), player, 0, true);
            expectTrue("整栈点燃只消费一件且其余点燃状态与零计时保留", stack.getCount() == 3
                    && state(stack).lit() && state(stack).ticks() == 0);
        }));
        script(actions.toArray(Action[]::new));
    }

    private void select(ServerPlayer player, int slot) {
        player.getInventory().selected = slot;
        minecraft.execute(() -> minecraft.player.getInventory().selected = slot);
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), count);
    }

    private static HandheldFireworkState state(ItemStack stack) {
        return stack.getOrDefault(FestivalData.FIREWORK_STATE.get(), HandheldFireworkState.UNLIT);
    }

    private static String marker(ItemStack stack) {
        return stack.getOrDefault(NbtEditorData.TAGS.get(), new CompoundTag()).getString("smoke_marker");
    }

    private static int blankCount(ServerPlayer player, String id) {
        Item item = item(id, 1).getItem();
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item) && !stack.has(FestivalData.FIREWORK_STATE.get()) && !stack.has(NbtEditorData.TAGS.get())) count += stack.getCount();
        }
        return count;
    }

    private static int blankSlot(ServerPlayer player, String id) {
        Item item = item(id, 1).getItem();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item) && !stack.has(FestivalData.FIREWORK_STATE.get()) && !stack.has(NbtEditorData.TAGS.get())) return slot;
        }
        throw new IllegalStateException("没有来源点燃时分出的未点燃烟花");
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        select(player, selected);
        player.setGameMode(mode);
        player.inventoryMenu.broadcastChanges();
    }
}
