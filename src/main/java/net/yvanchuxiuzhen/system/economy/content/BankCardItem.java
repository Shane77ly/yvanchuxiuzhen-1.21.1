package net.yvanchuxiuzhen.system.economy.content;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.economy.api.BankCard;
import net.yvanchuxiuzhen.system.economy.api.BankCards;

/** 两种通宝玉都潜行认主；普通玉的普通右键只报告物品自身余额，高级玉入口由全局事件处理。 */
public final class BankCardItem extends ContentItem {
    private final boolean advanced;

    public BankCardItem(Item.Properties properties, List<Component> tooltip, boolean advanced) {
        super(properties, tooltip);
        this.advanced = advanced;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return result;
        ItemStack stack = result.getObject();
        if (player.isShiftKeyDown()) {
            BankCards.bind(serverPlayer, stack);
        } else if (!advanced) {
            BankCard card = BankCards.read(stack);
            if (card.masterUUID().equals(player.getStringUUID())) {
                for (int tier = 1; tier <= 9; tier++) {
                    double amount = card.balance(tier);
                    if (amount >= 1) BankCards.message(serverPlayer, "card_balance",
                            Component.translatable("item.yvanchuxiuzhen.cailiao_19" + tier), new DecimalFormat("0").format(amount));
                }
            } else {
                BankCards.message(serverPlayer, card.masterUUID().isEmpty() ? "unbound" : "not_owner");
            }
        }
        return result;
    }
}
