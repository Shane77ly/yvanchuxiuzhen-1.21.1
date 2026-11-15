package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 酒蛊入体后一次性解锁千杯不倒，体质字段由修炼系统写入并同步。 */
public final class GuchongConstitutionItem extends ContentItem {
    public GuchongConstitutionItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (Cultivation.tizhiBoolean(serverPlayer, "tizhiopen_013")) {
                serverPlayer.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.guchong_constitution_known"), false);
            } else {
                Cultivation.editTizhi(serverPlayer, data -> {
                    data.putBoolean("tizhiopen_013", true);
                    data.putDouble("AlcoholPatience", data.getDouble("AlcoholPatience") + 100);
                    data.putDouble("AlcoholDigestionPercentage", data.getDouble("AlcoholDigestionPercentage") - 1);
                    data.putDouble("tizhinumber", data.getDouble("tizhinumber") + 1);
                });
                serverPlayer.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.guchong_constitution_gained"), false);
                result.getObject().shrink(1);
            }
        }
        return result;
    }
}
