package net.yvanchuxiuzhen.collection.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorBlockEntity;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorContent;

/** 匾额按字数拼左右端和中段，牌位按保存的每字纵坐标显示。 */
public final class InscribedDecorRenderer implements BlockEntityRenderer<InscribedDecorBlockEntity> {
    public InscribedDecorRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(InscribedDecorBlockEntity block, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        CompoundTag data = block.getPersistentData();
        if (data.getString("plaquetxt").isEmpty()) return;
        if (block.variant().memorial()) memorial(data, pose, buffers);
        else plaque(block, data, pose, buffers, light, overlay);
    }
    private static void text(String value, int colour, PoseStack pose, MultiBufferSource buffers) {
        Minecraft.getInstance().font.drawInBatch(value, 5, 5, colour, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, 15728880);
    }
    private static void memorial(CompoundTag data, PoseStack pose, MultiBufferSource buffers) {
        for (int index = 0; index < data.getInt("zishu"); index++) {
            pose.pushPose();
            double y = 2 - data.getDouble("ziweizhi" + index);
            switch (data.getString("fangxiang")) {
                case "北" -> { pose.translate(.75, y, .35); pose.mulPose(Axis.YP.rotationDegrees(180)); }
                case "南" -> pose.translate(.25, y, .65);
                case "东" -> { pose.translate(.65, y, .75); pose.mulPose(Axis.YP.rotationDegrees(90)); }
                default -> { pose.translate(.35, y, .25); pose.mulPose(Axis.YP.rotationDegrees(-90)); }
            }
            pose.scale(.03F, -.03F, .03F);
            text(data.getString("zidi" + index), data.getInt("rgbcolour"), pose, buffers);
            pose.popPose();
        }
    }
    private static void plaque(InscribedDecorBlockEntity block, CompoundTag data, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        int angle = data.getInt("qingjiao");
        int yaw = data.getInt("plaquejiaodu");
        pose.pushPose();
        pose.translate(data.getDouble("wenziX"), angle == 45 ? 1.1 : angle == 20 ? 1.13 : 1.15, data.getDouble("wenziZ"));
        pose.mulPose(Axis.YP.rotationDegrees(yaw + 180));
        if (angle > 1) pose.mulPose(Axis.XP.rotationDegrees(angle));
        pose.scale(.07F, -.07F, .07F);
        text(data.getString("plaquetxt"), data.getInt("rgbcolour"), pose, buffers);
        pose.popPose();
        int count = data.getInt("blocknum");
        int adjustedLight = data.getBoolean("faguang") ? light + 20 : light;
        segment(block, data, 0, 2, pose, buffers, adjustedLight, overlay);
        segment(block, data, count - 1, 3, pose, buffers, adjustedLight, overlay);
        for (int index = 1; index < count - 1; index++) segment(block, data, index, 1, pose, buffers, adjustedLight, overlay);
    }
    private static void segment(InscribedDecorBlockEntity block, CompoundTag data, int index, int part, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(data.getDouble("block" + index + "Xpianyi"), .5, data.getDouble("block" + index + "Zpianyi"));
        pose.mulPose(Axis.YP.rotationDegrees(data.getInt("plaquejiaodu")));
        if (data.getInt("qingjiao") > 1) pose.mulPose(Axis.XP.rotationDegrees(-data.getInt("qingjiao")));
        ItemStack stack = new ItemStack(InscribedDecorContent.block(block.variant().segment(part)));
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay, renderer.getItemModelShaper().getItemModel(stack));
        pose.popPose();
    }
}
