package net.yvanchuxiuzhen.system.economy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;

/** 商铺商品与拍卖当前货物沿来源位置、比例、朝向和FIXED左手变换绘制。 */
final class EconomyBlockRenderer implements BlockEntityRenderer<EconomyBlockEntity> {
    EconomyBlockRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(EconomyBlockEntity block, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, block.auction() ? 1 : 1.5, 0.5);
        String direction = block.state().getString("fangxiang");
        float rotation = block.auction() ? direction.equals("北") ? 0 : direction.equals("南") ? 180 : direction.equals("西") ? 90 : -90 : direction.equals("南北") ? 0 : 90;
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        if (!block.auction()) pose.scale(0.6F, 0.6F, 0.6F);
        ItemStack item = block.getItem(block.auction() ? 20 : 0);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(item, ItemDisplayContext.FIXED, true, pose, buffer, light, overlay, renderer.getItemModelShaper().getItemModel(item));
        pose.popPose();
    }
}
