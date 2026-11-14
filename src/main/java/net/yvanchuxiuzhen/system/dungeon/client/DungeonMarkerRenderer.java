package net.yvanchuxiuzhen.system.dungeon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;

/** 占位符ID按来源平铺于方块上方，文字不改变服务端配置。 */
public final class DungeonMarkerRenderer implements BlockEntityRenderer<DungeonBlockEntity> {
    public DungeonMarkerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DungeonBlockEntity block, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        String id = block.getPersistentData().getString("zhanweifuid");
        if (id.isEmpty()) return;
        pose.pushPose();
        pose.translate(.5, 1.5, .5);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(.1F, .1F, .1F);
        Minecraft.getInstance().font.drawInBatch(id, 5, 5, 0xFFFFFFFF, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }
}
