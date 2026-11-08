package net.yvanchuxiuzhen.system.liandan.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.liandan.LiandanMedicine;
import net.yvanchuxiuzhen.system.liandan.MedicineContainerPlacement;

/** 八种丹药容器的自然右键入口；空药盒仅有方块右键入口。 */
public final class MedicineContainerItem extends ContentItem {
    private final boolean airUse;

    public MedicineContainerItem(Properties properties, List<Component> tooltip, boolean airUse) {
        super(properties, tooltip);
        this.airUse = airUse;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (airUse) {
            LiandanMedicine.use(level, player, result.getObject(), player.position());
        }
        // 返回来源调用栈的 PASS；换瓶写入手中，调用栈对象本身不替换。
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Player player = context.getPlayer();
        if (player != null) {
            if (player.isShiftKeyDown()) {
                MedicineContainerPlacement.place(context);
            } else {
                LiandanMedicine.use(context.getLevel(), player, context.getItemInHand(), Vec3.atLowerCornerOf(context.getClickedPos()));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
