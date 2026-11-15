package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 九档灵石原矿右键整组开封，并核对满背包余物掉落。 */
final class SpiritStoneBundleCheck extends SystemCheck {
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> dropListener = this::captureDrop;
    private GameType originalMode;
    private int originalSelected;
    private Item expectedOutput;
    private UUID outputDrop;

    SpiritStoneBundleCheck(List<String> report, String levelId) {
        super("灵石原矿开封", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 8) return false;
        if (step == 0) {
            onServer(this::prepare);
        } else if (step <= 9) {
            onServer(player -> checkTier(player, step));
        } else if (step == 10) {
            onServer(this::checkFullInventoryDrop);
        } else {
            onServer(this::restore);
            finish();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        originalSelected = player.getInventory().selected;
        originalMode = player.gameMode.getGameModeForPlayer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        NeoForge.EVENT_BUS.addListener(dropListener);
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();
    }

    private void checkTier(ServerPlayer player, int tier) {
        player.getInventory().clearContent();
        expectedOutput = item("cailiao_19" + tier).getItem();
        ItemStack input = item("cailiao_19" + tier + "01");
        input.setCount(3);
        player.getInventory().setItem(0, input);
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);

        expectTrue(tier + "级原矿整组已消耗", player.getMainHandItem().isEmpty());
        ItemStack output = findOutput(player, expectedOutput);
        expectTrue(tier + "级原矿生成同等级灵石", !output.isEmpty());
        if (!output.isEmpty()) {
            MaterialInstance material = Materials.instance(output);
            expect(tier + "级原矿保留整组数量", 3, output.getCount());
            expect(tier + "级原矿对应材料等级", tier * 5, material.level());
            expect(tier + "级原矿材料品阶", 4, material.quality());
        }
    }

    private void checkFullInventoryDrop(ServerPlayer player) {
        player.getInventory().clearContent();
        for (int slot = 0; slot < 36; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        expectedOutput = item("cailiao_199").getItem();
        outputDrop = null;
        player.getInventory().setItem(0, new ItemStack(item("cailiao_19901").getItem(), 2));
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);

        expectTrue("满背包开封后消耗原矿", player.getMainHandItem().isEmpty());
        ItemEntity dropped = outputDrop == null ? null : (ItemEntity) player.serverLevel().getEntity(outputDrop);
        expectTrue("满背包开封将灵石掉落在地", dropped != null);
        if (dropped != null) {
            expect("满背包掉落数量", 2, dropped.getItem().getCount());
            expect("满背包掉落保留等级", 45, Materials.instance(dropped.getItem()).level());
            expect("满背包掉落保留品阶", 4, Materials.instance(dropped.getItem()).quality());
        }
    }

    private void captureDrop(EntityJoinLevelEvent event) {
        if (expectedOutput == null || !(event.getEntity() instanceof ItemEntity itemEntity)
                || !itemEntity.getItem().is(expectedOutput)) return;
        outputDrop = itemEntity.getUUID();
        itemEntity.setNoGravity(true);
        itemEntity.setPickUpDelay(1200);
    }

    private void restore(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(dropListener);
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) {
            player.getInventory().setItem(slot, originalInventory.get(slot));
        }
        player.getInventory().selected = originalSelected;
        player.setGameMode(originalMode);
        if (outputDrop != null && player.serverLevel().getEntity(outputDrop) != null) {
            player.serverLevel().getEntity(outputDrop).discard();
        }
        player.inventoryMenu.broadcastChanges();
    }

    private static ItemStack findOutput(ServerPlayer player, Item expected) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(expected)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }
}
