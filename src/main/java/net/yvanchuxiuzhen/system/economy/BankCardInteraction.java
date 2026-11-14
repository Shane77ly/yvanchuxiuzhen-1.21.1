package net.yvanchuxiuzhen.system.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.BankCard;
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.BankDialogueRequestEvent;

/** 来源全局物品右键的高级通宝玉分支，保留使用手过滤、主手识别和非潜行条件。 */
public final class BankCardInteraction {
    private static final ResourceLocation ADVANCED_CARD = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_13");

    private BankCardInteraction() {
    }

    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand() || player.isShiftKeyDown()) return;
        ItemStack stack = player.getMainHandItem();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ADVANCED_CARD)) return;
        BankCard card = BankCards.read(stack);
        if (card.masterUUID().equals(player.getStringUUID())) {
            NeoForge.EVENT_BUS.post(new BankDialogueRequestEvent(player));
        } else {
            BankCards.message(player, card.masterUUID().isEmpty() ? "unbound" : "not_owner");
        }
    }
}
