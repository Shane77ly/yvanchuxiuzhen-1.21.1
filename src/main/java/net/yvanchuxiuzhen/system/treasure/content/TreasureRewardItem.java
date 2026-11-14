package net.yvanchuxiuzhen.system.treasure.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.treasure.TreasureRewards;

/** 物品宝箱 01/02：有宝箱大类型时立即发放并消耗一个载体。 */
public final class TreasureRewardItem extends ContentItem {
    public TreasureRewardItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        ItemStack stack = result.getObject();
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (level instanceof ServerLevel serverLevel && !data.getString("TreasureTypeBig").isEmpty()) {
            TreasureRewards.open(serverLevel, player.position().add(0.5, 0.5, 0.5), data.getString("TreasureTypeBig"), data.getString("TreasureTypeSmall"),
                    Math.rint(data.getDouble("TreasureLEVEL")), (int) Math.rint(data.getDouble("minimumvalue")), (int) Math.rint(data.getDouble("Maximumvalue")));
            stack.shrink(1);
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
