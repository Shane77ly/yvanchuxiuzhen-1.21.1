package net.yvanchuxiuzhen.system.economy.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 按来源整组开封为同数量、对应等级与品阶的灵石。 */
public final class SpiritStoneBundleItem extends ContentItem {
    private final int tier;

    public SpiritStoneBundleItem(Properties properties, List<Component> tooltip, int tier) {
        super(properties, tooltip);
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack input = player.getItemInHand(hand);
            int count = input.getCount();
            ItemStack output = new ItemStack(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_19" + tier)), count);
            Materials.setInstance(output, tier * 5, 4);
            ItemHandlerHelper.giveItemToPlayer(serverPlayer, output);
            input.shrink(count);
        }
        return result;
    }
}
