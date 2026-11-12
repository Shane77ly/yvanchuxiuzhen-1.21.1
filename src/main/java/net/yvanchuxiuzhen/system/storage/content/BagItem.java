package net.yvanchuxiuzhen.system.storage.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 乾坤袋右键绑定实际使用的物品栈；开窗不替换物品，也不复制或迁移库存。 */
public final class BagItem extends ContentItem {
    private final BagKind kind;

    public BagItem(Properties properties, List<Component> tooltip, BagKind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack bag = result.getObject();
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new BagMenu(id, inventory, kind, bag),
                    Component.literal("Qiankundai 0 " + (kind.ordinal() + 1))));
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return kind == BagKind.SMALL ? UseAnim.EAT : super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return kind == BagKind.SMALL ? 32 : super.getUseDuration(stack, entity);
    }
}
