package net.yvanchuxiuzhen.system.world.portal;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 来源六种维度工具：右键空位尝试在已有天道维度框架中生成对应门户并消耗一点耐久。 */
public final class DimensionPortalItem extends ContentItem {
    private final DimensionPortalSpec spec;

    public DimensionPortalItem(Properties properties, List<Component> tooltip, DimensionPortalSpec spec) {
        super(properties, tooltip);
        this.spec = spec;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        var pos = context.getClickedPos().relative(context.getClickedFace());
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        if (!level.isEmptyBlock(pos)) {
            return InteractionResult.FAIL;
        }
        DimensionPortalBlock.portalSpawn(level, pos, spec);
        stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return InteractionResult.SUCCESS;
    }
}
