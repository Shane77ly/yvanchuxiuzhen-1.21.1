package net.yvanchuxiuzhen.system.economy.content;

import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 丹元类消耗品（cailiao_200 / cailiao_210）：按 1.18.2 来源过程结算修为、灵力、瓶颈与神识。 */
public final class SpiritEssenceItem extends ContentItem {
    private final boolean soulEssence;

    public SpiritEssenceItem(Properties properties, List<Component> tooltip, boolean soulEssence) {
        super(properties, tooltip);
        this.soulEssence = soulEssence;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
        if (soulEssence) {
            Cultivation.add(player, CultivationValue.SOUL, PlayerAttributes.value(player, Stat.SOUL_MAX) * 0.1);
            return result;
        }
        double gain = Cultivation.get(player, CultivationValue.EXPERIENCE_MAX) * 0.1;
        player.displayClientMessage(Component.literal("修为获得" + gain + "点！"), false);
        Cultivation.set(player, CultivationValue.POWER, Cultivation.get(player, CultivationValue.POWER) * 0.8);
        Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, gain);
        int roll = Mth.nextInt(player.getRandom(), 1, 80);
        if (roll <= 20) {
            double rate = Cultivation.get(player, CultivationValue.EXPERIENCE_RATE);
            if (rate > 0.05) {
                Cultivation.set(player, CultivationValue.EXPERIENCE_RATE, rate - 0.05);
            } else {
                Cultivation.set(player, CultivationValue.EXPERIENCE_MAX,
                        Cultivation.get(player, CultivationValue.EXPERIENCE_MAX) * 1.05);
            }
        } else if (roll <= 40) {
            Cultivation.set(player, CultivationValue.EXPERIENCE_MAX,
                    Cultivation.get(player, CultivationValue.EXPERIENCE_MAX) * 1.05);
        } else if (roll <= 60) {
            double delta = -PlayerAttributes.baseValue(player, Stat.POWER_MAX) * 0.05;
            PlayerAttributes.setContribution(player, "economy:cailiao_200", new Contribution(Layer.BASE, Map.of(Stat.POWER_MAX, delta)));
        } else {
            Cultivation.add(player, CultivationValue.EXPERIENCE_HINDER, 0.05);
        }
        return result;
    }
}
