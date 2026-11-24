package net.yvanchuxiuzhen.system.treasure.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.treasure.TreasureRewards;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 物品宝箱 01/02：有宝箱大类型时立即发放并消耗一个载体。 */
public final class TreasureRewardItem extends ContentItem {
    public TreasureRewardItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        ItemStack stack = result.getObject();
        TreasureContainerConfig config = TreasureContainers.config(stack);
        if (level instanceof ServerLevel serverLevel && config.isPresent()) {
            TreasureRewards.open(serverLevel, player.position().add(0.5, 0.5, 0.5), config.category(), config.subtype(),
                    Math.rint(config.level()), (int) Math.rint(config.minimum()), (int) Math.rint(config.maximum()));
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(this).getPath().equals("treasure_chest_02") ? UseAnim.EAT : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return BuiltInRegistries.ITEM.getKey(this).getPath().equals("treasure_chest_02") ? 32 : 0;
    }
}
