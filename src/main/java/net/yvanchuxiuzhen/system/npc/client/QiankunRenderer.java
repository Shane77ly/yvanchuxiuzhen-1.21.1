package net.yvanchuxiuzhen.system.npc.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.yvanchuxiuzhen.platform.ModId;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 乾坤方块实体按来源资产渲染，保持透明实体渲染类型。 */
public final class QiankunRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    public QiankunRenderer(BlockEntityRendererProvider.Context context, String id) { super(new Model<>(id)); }
    @Override public RenderType getRenderType(T block, ResourceLocation texture, MultiBufferSource buffers, float partialTick) { return RenderType.entityTranslucent(texture); }
    private static final class Model<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {
        private final String id;
        Model(String id) { this.id = id; }
        @Override public ResourceLocation getModelResource(T block) { return resource("geo/" + id + ".geo.json"); }
        @Override public ResourceLocation getTextureResource(T block) { return resource("textures/blocks/" + id + ".png"); }
        @Override public ResourceLocation getAnimationResource(T block) { return resource("animations/" + id + ".animation.json"); }
    }
    static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
