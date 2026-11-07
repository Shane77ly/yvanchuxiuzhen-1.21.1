package net.yvanchuxiuzhen.system.skill;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 技能箭物品保留来源的蓄力动画；来源释放条件恒为假，结束使用不发射、不损耗。 */
public final class SkillArrowItem extends ContentItem {
    public SkillArrowItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}
