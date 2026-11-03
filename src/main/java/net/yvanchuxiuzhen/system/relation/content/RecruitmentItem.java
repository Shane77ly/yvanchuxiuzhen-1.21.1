package net.yvanchuxiuzhen.system.relation.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.relation.FriendList;

/**
 * 团队招募令 {@code ling_15}：潜行使用时打开好友界面（来源 {@code Ling15DangYouJianDianJiKongQiShiShiTiDeWeiZhi}）。
 * 右键玩家发好友申请由 {@code RelationSetup} 的实体交互监听处理。
 */
public final class RecruitmentItem extends ContentItem {
    public RecruitmentItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && player.isShiftKeyDown()) {
            FriendList.open(serverPlayer);
        }
        return super.use(level, player, hand);
    }
}
