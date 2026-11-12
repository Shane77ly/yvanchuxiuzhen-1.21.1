package net.yvanchuxiuzhen.system.cooking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.cooking.content.TablewareBlock;
import net.yvanchuxiuzhen.system.cooking.content.TablewareBlockEntity;

/** 静态餐具模型上方按来源高度绘制槽内同类餐具，不显示异类自动化物品。 */
public final class TablewareRenderer implements BlockEntityRenderer<TablewareBlockEntity> {
    public TablewareRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TablewareBlockEntity tableware, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack stored = tableware.getItem(0);
        if (!stored.is(tableware.getBlockState().getBlock().asItem())) return;
        TablewareBlock.Kind kind = tableware.kind();
        ItemStack rendered = new ItemStack(tableware.getBlockState().getBlock());
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        int layers = Math.min(stored.getCount(), kind.capacity());
        for (int layer = 0; layer < layers; layer++) {
            pose.pushPose();
            pose.translate(0.5, kind.layerHeight(layer), 0.5);
            pose.scale(2, 2, 2);
            renderer.render(rendered, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                    renderer.getItemModelShaper().getItemModel(rendered));
            pose.popPose();
        }
    }
}
