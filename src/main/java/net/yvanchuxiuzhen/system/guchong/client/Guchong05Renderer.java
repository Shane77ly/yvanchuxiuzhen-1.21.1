package net.yvanchuxiuzhen.system.guchong.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;

/** 荧光蛊按来源注册原生模型、透明空纹理和0阴影渲染器。 */
final class Guchong05Renderer extends MobRenderer<Guchong05Entity, Guchong05Model> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ModId.VALUE, "textures/entities/kong.png");

    Guchong05Renderer(EntityRendererProvider.Context context) {
        super(context, new Guchong05Model(context.bakeLayer(Guchong05Model.LAYER)), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(Guchong05Entity entity) {
        return TEXTURE;
    }
}
