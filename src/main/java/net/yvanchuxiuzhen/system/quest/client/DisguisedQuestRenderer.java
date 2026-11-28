package net.yvanchuxiuzhen.system.quest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;

/** 展示槽按来源持久朝向绘制；方块与普通物品分别使用来源缩放及固定左手姿态。 */
public final class DisguisedQuestRenderer implements BlockEntityRenderer<DisguisedQuestBlockEntity> {
    public DisguisedQuestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DisguisedQuestBlockEntity block, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = block.getItem(0);
        if (stack.isEmpty()) return;
        boolean blockItem = stack.getItem() instanceof BlockItem item && !item.getBlock().defaultBlockState().isAir();
        pose.pushPose();
        pose.translate(.5, blockItem ? .5 : .1, .5);
        float rotation = switch (block.displayDirection()) {
            case "北" -> 0;
            case "南" -> 180;
            case "西" -> 90;
            default -> 270;
        };
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        if (blockItem) {
            pose.scale(2, 2, 2);
        } else {
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.scale(.7F, .7F, .7F);
        }
        var renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                renderer.getItemModelShaper().getItemModel(stack));
        pose.popPose();
    }
}
