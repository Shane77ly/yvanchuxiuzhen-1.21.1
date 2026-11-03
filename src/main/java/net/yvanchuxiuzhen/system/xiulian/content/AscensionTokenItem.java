package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 升仙令（来源 {@code Ling09}，出身“他乡故知”的初始物品）：右键记下体质键 {@code shengxianling}，并给 600 秒的漂浮 VI 与缓慢 XXI
 * （不显示粒子与图标）。进入人间时据该键安置玩家的部分归 world。不消耗。
 */
public final class AscensionTokenItem extends ContentItem {
    private static final int TICKS = 12000;

    public AscensionTokenItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Cultivation.setTizhiBoolean(serverPlayer, "shengxianling", true);
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TICKS, 5, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TICKS, 20, false, false));
        }
        return super.use(level, player, hand);
    }
}
