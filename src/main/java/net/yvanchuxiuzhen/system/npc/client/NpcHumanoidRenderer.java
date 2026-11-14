package net.yvanchuxiuzhen.system.npc.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.entity.NpcMob;

/** 09至11号商人的来源玩家骨架、衣甲层与固定皮肤。 */
final class NpcHumanoidRenderer extends HumanoidMobRenderer<NpcMob, HumanoidModel<NpcMob>> {
    private final ResourceLocation texture;

    NpcHumanoidRenderer(EntityRendererProvider.Context context, String skin) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), .5F);
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/" + skin + ".png");
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override public ResourceLocation getTextureLocation(NpcMob entity) { return texture; }
}
