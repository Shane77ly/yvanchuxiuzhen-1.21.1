package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 九级灵石由真实右键及原生服用计时完成，核对库存扣减与完成瞬间的修炼增量。 */
final class SpiritStoneCheck extends SystemCheck {
    private static final int HOTBAR = 8;
    private static final int[] POWER = {1, 4, 16, 64, 256, 1024, 4096, 16384, 65536};
    private static final int[] EXPERIENCE = {0, 1, 4, 16, 64, 256, 1024, 4096, 16384};
    private final Map<CultivationValue, Double> originalValues = new EnumMap<>(CultivationValue.class);
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private final CompoundTag originalFood = new CompoundTag();
    private final Consumer<LivingEntityUseItemEvent.Tick> ticking = this::beforeFinish;
    private final Consumer<LivingEntityUseItemEvent.Finish> finishing = this::afterFinish;
    private String playerId;
    private GameType originalMode;
    private int originalSelected;
    private boolean originalUseKey;
    private Item expectedItem;
    private int tier;
    private int finishes;
    private boolean finalTick;
    private double beforePower;
    private double beforeExperience;
    private double gainedPower;
    private double gainedExperience;
    private int resultCount;

    SpiritStoneCheck(List<String> report, String levelId) {
        super("灵石真实服用", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 10) return false;
        if (step == 0) {
            originalUseKey = minecraft.options.keyUse.isDown();
            minecraft.options.keyUse.setDown(false);
            onServer(this::prepare);
            return true;
        }
        if (step <= 27) {
            int phase = (step - 1) % 3;
            int currentTier = (step - 1) / 3 + 1;
            if (phase == 0) {
                onServer(player -> stage(player, currentTier));
                return true;
            }
            if (phase == 1) {
                minecraft.player.getInventory().selected = HOTBAR;
                expect("客户端已收到" + currentTier + "级两枚灵石", 2, minecraft.player.getMainHandItem().getCount());
                minecraft.options.keyUse.setDown(true);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                return true;
            }
            boolean[] completed = {false};
            onServer(player -> completed[0] = finishes > 0);
            if (!completed[0]) {
                if (local > 240) {
                    fail(currentTier + "级灵石真实右键后未完成原生服用");
                    restore();
                    finish();
                }
                return false;
            }
            minecraft.options.keyUse.setDown(false);
            minecraft.gameMode.releaseUsingItem(minecraft.player);
            onServer(player -> {
                expect(currentTier + "级灵石只完成一次服用", 1, finishes);
                expectTrue(currentTier + "级灵石经过最后一个原生服用刻", finalTick);
                expect(currentTier + "级灵石完成回调结果保留另一枚", 1, resultCount);
                expect(currentTier + "级灵石真实主手库存两枚扣至一枚", 1, player.getMainHandItem().getCount());
                expect(currentTier + "级灵石完成瞬间增加灵力", POWER[currentTier - 1], gainedPower);
                expect(currentTier + "级灵石完成瞬间增加待吸收修为", EXPERIENCE[currentTier - 1], gainedExperience);
                tier = 0;
                player.stopUsingItem();
            });
            return true;
        }
        restore();
        finish();
        return true;
    }

    private void prepare(ServerPlayer player) {
        playerId = player.getStringUUID();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalSelected = player.getInventory().selected;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        for (CultivationValue value : CultivationValue.values()) originalValues.put(value, Cultivation.get(player, value));
        player.getFoodData().addAdditionalSaveData(originalFood);
        player.closeContainer();
        player.stopUsingItem();
        player.getInventory().clearContent();
        player.setGameMode(GameType.SURVIVAL);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ticking);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, finishing);
    }

    private void stage(ServerPlayer player, int currentTier) {
        player.stopUsingItem();
        tier = currentTier;
        finishes = 0;
        finalTick = false;
        gainedPower = 0;
        gainedExperience = 0;
        resultCount = -1;
        expectedItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_19" + tier));
        player.getInventory().selected = HOTBAR;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(expectedItem, 2));
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setExhaustion(0);
        player.inventoryMenu.broadcastChanges();
        expectTrue(tier + "级灵石在饱食时仍允许服用", player.getMainHandItem().getFoodProperties(player) != null
                && player.getMainHandItem().getFoodProperties(player).canAlwaysEat());
    }

    private void beforeFinish(LivingEntityUseItemEvent.Tick event) {
        if (tier == 0 || !(event.getEntity() instanceof ServerPlayer player) || !player.getStringUUID().equals(playerId)
                || !event.getItem().is(expectedItem) || event.getDuration() != 1) return;
        finalTick = true;
        beforePower = Cultivation.get(player, CultivationValue.POWER);
        beforeExperience = Cultivation.get(player, CultivationValue.EXPERIENCE_STAND);
    }

    private void afterFinish(LivingEntityUseItemEvent.Finish event) {
        if (tier == 0 || !(event.getEntity() instanceof ServerPlayer player) || !player.getStringUUID().equals(playerId)
                || !event.getItem().is(expectedItem)) return;
        gainedPower = Cultivation.get(player, CultivationValue.POWER) - beforePower;
        gainedExperience = Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) - beforeExperience;
        resultCount = event.getResultStack().getCount();
        finishes++;
        minecraft.execute(() -> minecraft.options.keyUse.setDown(false));
    }

    private void restore() {
        minecraft.options.keyUse.setDown(false);
        minecraft.gameMode.releaseUsingItem(minecraft.player);
        onServer(player -> {
            tier = 0;
            NeoForge.EVENT_BUS.unregister(ticking);
            NeoForge.EVENT_BUS.unregister(finishing);
            player.stopUsingItem();
            player.getInventory().clearContent();
            for (int slot = 0; slot < originalInventory.size(); slot++) player.getInventory().setItem(slot, originalInventory.get(slot));
            player.getInventory().selected = originalSelected;
            originalValues.forEach((value, amount) -> Cultivation.set(player, value, amount));
            player.getFoodData().readAdditionalSaveData(originalFood);
            player.setGameMode(originalMode);
            player.inventoryMenu.broadcastChanges();
        });
        minecraft.options.keyUse.setDown(originalUseKey);
    }
}
