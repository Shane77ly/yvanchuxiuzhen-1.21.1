package net.yvanchuxiuzhen.system.zhenfa.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationElementBlockEntity;

/** 伪装优先于材料：伪装放大两倍，材料按布局角度旋转并缩放至一半。 */
public final class FormationElementRenderer implements BlockEntityRenderer<FormationElementBlockEntity> {
    public FormationElementRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FormationElementBlockEntity element, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack disguise = element.disguise();
        ItemStack rendered = disguise.isEmpty() ? element.material() : disguise;
        if (rendered.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        if (disguise.isEmpty()) {
            pose.mulPose(Axis.YP.rotationDegrees(element.displayAngle()));
            pose.scale(0.5F, 0.5F, 0.5F);
        } else {
            // 1.18.2 的零四元数构造单位矩阵，不改变物品位置与法线。
            pose.scale(2, 2, 2);
        }
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(rendered, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                renderer.getItemModelShaper().getItemModel(rendered));
        pose.popPose();
    }
}
