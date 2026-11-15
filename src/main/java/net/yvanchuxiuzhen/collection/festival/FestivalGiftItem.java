package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 财神服礼包与大红包；自然右键沿来源发放整套服装或逐件掉落随机奖励。 */
public final class FestivalGiftItem extends ContentItem {
    enum Kind {
        COSTUME, RED_PACKET
    }

    private final Kind kind;

    FestivalGiftItem(Properties properties, List<Component> tooltip, Kind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel) {
            if (kind == Kind.COSTUME) {
                for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) {
                    ItemStack costume = new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fuqi_02_" + slot)));
                    ItemHandlerHelper.giveItemToPlayer(player, costume);
                }
            } else {
                RedPacketRewards.Reward reward = RedPacketRewards.roll();
                for (int index = 0; index < reward.count(); index++) {
                    // 来源将同一奖励栈逐次传入掉落实体，不合并为一件多数量栈。
                    ItemEntity drop = new ItemEntity(serverLevel, player.getX(), player.getY() + 1, player.getZ(), reward.stack());
                    drop.setPickUpDelay(10);
                    serverLevel.addFreshEntity(drop);
                }
            }
            if (!player.getAbilities().instabuild) result.getObject().shrink(1);
        }
        return result;
    }
}
