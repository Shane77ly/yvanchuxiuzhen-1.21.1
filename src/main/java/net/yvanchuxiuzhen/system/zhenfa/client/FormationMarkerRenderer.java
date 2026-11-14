package net.yvanchuxiuzhen.system.zhenfa.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationBlockIds;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationMarkerBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 阵旗与罗盘根据方块身份读取各自来源几何、纹理和动画。 */
public final class FormationMarkerRenderer extends GeoBlockRenderer<FormationMarkerBlockEntity> {
    public FormationMarkerRenderer(BlockEntityRendererProvider.Context context) { super(new Model()); }
    @Override public RenderType getRenderType(FormationMarkerBlockEntity block, ResourceLocation texture,
            MultiBufferSource buffers, float partialTick) { return RenderType.entityCutout(texture); }

    private static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }

    private static final class Model extends GeoModel<FormationMarkerBlockEntity> {
        @Override public ResourceLocation getModelResource(FormationMarkerBlockEntity block) {
            String id = FormationBlockIds.path(block.getBlockState());
            return resource("geo/" + (id.equals("flagmother") ? "flag_mother" : id.equals("flagson") ? "flag_son" : "luopan") + ".geo.json");
        }
        @Override public ResourceLocation getTextureResource(FormationMarkerBlockEntity block) {
            String id = FormationBlockIds.path(block.getBlockState());
            return resource("textures/block/" + (id.equals("flagmother") ? "flag_mother" : id.equals("flagson") ? "flag_son" : "luopan") + ".png");
        }
        @Override public ResourceLocation getAnimationResource(FormationMarkerBlockEntity block) {
            String id = FormationBlockIds.path(block.getBlockState());
            return resource("animations/" + (id.equals("flagmother") ? "flag_mother" : id.equals("flagson") ? "flag_son" : "luopan") + ".animation.json");
        }
    }
}
