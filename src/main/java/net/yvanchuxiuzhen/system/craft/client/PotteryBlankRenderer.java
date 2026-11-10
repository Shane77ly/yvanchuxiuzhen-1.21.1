package net.yvanchuxiuzhen.system.craft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankBlockEntity;

/** 陶坯燃烧时附加显示来源火焰物品，普通陶坯几何由方块模型绘制。 */
public final class PotteryBlankRenderer implements BlockEntityRenderer<PotteryBlankBlockEntity> {
    public PotteryBlankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotteryBlankBlockEntity blank, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (!blank.burning()) return;
        ItemStack flame = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_221fireblock")));
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        // 1.18.2 的零四元数构造单位矩阵，保持当前位置与法线即可保留该变换。
        Minecraft.getInstance().getItemRenderer().renderStatic(null, flame, ItemDisplayContext.FIXED, true,
                pose, buffers, blank.getLevel(), light, overlay, 0);
        pose.popPose();
    }
}
