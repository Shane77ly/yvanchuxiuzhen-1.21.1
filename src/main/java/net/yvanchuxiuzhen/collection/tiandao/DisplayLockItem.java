package net.yvanchuxiuzhen.collection.tiandao;

import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.content.BlockDisplayLock;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 展示锁向方块写入共享锁状态，并按来源锁定或隐藏盔甲架与物品框。 */
public final class DisplayLockItem extends ContentItem {
    public DisplayLockItem(Properties properties, List<Component> tooltip) { super(properties, tooltip); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            BlockEntity block = level.getBlockEntity(context.getClickedPos());
            if (block != null) BlockDisplayLock.lock(block);
            level.sendBlockUpdated(context.getClickedPos(), level.getBlockState(context.getClickedPos()),
                    level.getBlockState(context.getClickedPos()), Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        Vec3 center = player.position();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2)).stream()
                .sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center))).toList()) {
            if (entity instanceof ArmorStand) changeEntity(player, entity);
        }
        return result;
    }

    public static void interact(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (event.getHand() == player.getUsedItemHand() && player.getMainHandItem().getItem() instanceof DisplayLockItem
                && (event.getTarget() instanceof ArmorStand || event.getTarget() instanceof ItemFrame)) {
            changeEntity(player, event.getTarget());
        }
    }

    private static void changeEntity(Player player, Entity target) {
        boolean stand = target instanceof ArmorStand;
        CompoundTag data = new CompoundTag();
        target.saveWithoutId(data);
        data.putBoolean(player.isShiftKeyDown() ? "Invisible" : stand ? "Marker" : "Fixed", true);
        target.load(data);
        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.display_lock."
                    + (player.isShiftKeyDown() ? "hide_" : "lock_") + (stand ? "stand" : "frame")), false);
        }
    }
}
