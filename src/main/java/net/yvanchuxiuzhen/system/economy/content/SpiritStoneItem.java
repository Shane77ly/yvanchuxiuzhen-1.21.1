package net.yvanchuxiuzhen.system.economy.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 九级灵石沿来源食用后增加当前灵力与待吸收修为，不在此处按灵力上限截断。 */
public final class SpiritStoneItem extends ContentItem {
    private final int tier;

    public SpiritStoneItem(Properties properties, List<Component> tooltip, int tier) {
        super(properties, tooltip);
        this.tier = tier;
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) {
            int power = 1 << ((tier - 1) * 2);
            Cultivation.add(player, CultivationValue.POWER, power);
            if (tier >= 2) Cultivation.add(player, CultivationValue.EXPERIENCE_STAND, power / 4);
            String message = switch (tier) {
                case 1 -> "§a获得1点灵力。";
                case 2 -> "§a获得4点灵力，修为也增加了一点。";
                case 3 -> "§a获得16点灵力，修为也增加了一些。";
                default -> "§a获得" + power + "点灵力，修为增加了" + power / 4 * Cultivation.get(player, CultivationValue.EXPERIENCE_RATE) + "点";
            };
            player.displayClientMessage(Component.literal(message), false);
        }
        return result;
    }
}
