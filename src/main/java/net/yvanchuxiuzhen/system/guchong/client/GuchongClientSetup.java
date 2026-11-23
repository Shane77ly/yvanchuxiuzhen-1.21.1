package net.yvanchuxiuzhen.system.guchong.client;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongCreatures;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongMenus;
import net.yvanchuxiuzhen.system.guchong.content.GuchongFluteItem;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanItem;

/** 蛊虫模型、动画、纹理和来源阴影装配。 */
public final class GuchongClientSetup {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private GuchongClientSetup() { }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(GuchongClientSetup::onTooltip);
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            for (int grade = 1; grade <= 3; grade++) event.register(GuchongMenus.type(grade), TaoguanScreen::new);
            event.register(GuchongMenus.RELEASE.get(), GuchongReleaseScreen::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof GuchongFluteItem flute) flute.setRenderProvider(new GuchongFluteRenderer.Provider());
            }
        }));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(Guchong05Model.LAYER, Guchong05Model::layer));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(GuchongSetup.<Guchong05Entity>entityType("guchong_05").get(), Guchong05Renderer::new);
            for (EntityModelRow row : GuchongCreatures.models()) {
                event.registerEntityRenderer(GuchongSetup.<GuchongEntity>entityType(row.id()).get(),
                        context -> new AnimatedEntityRenderer<>(context, row));
            }
        });
    }

    /** 来源 {@code ItemLoreProcedure:111-140}：显示蛊罐“待收回”标记与各格蛊虫详情。 */
    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getEntity() == null || !(stack.getItem() instanceof TaoguanItem)) {
            return;
        }
        int capacity = TaoguanItem.capacity(TaoguanItem.grade(stack));
        CompoundTag data = GuchongJarData.read(stack);
        List<Component> lines = event.getToolTip();
        if (data.getBoolean("shifangguchong")) {
            lines.add(Component.literal("§e待收回"));
        }
        String viewer = event.getEntity().getStringUUID();
        for (int slot = 0; slot < capacity; slot++) {
            String suffix = Integer.toString(slot);
            String name = data.getString("guchongname" + suffix);
            if (!name.isEmpty()) {
                String prefix = !data.getString("belongToMasterUUID" + suffix).equals(viewer) ? "§4*" : "";
                lines.add(Component.literal(prefix + "§6" + name + "§b" + data.getString("LEVELname" + suffix)
                        + "§f血量（" + WHOLE.format(data.getDouble("health_Num" + suffix)) + "/"
                        + WHOLE.format(data.getDouble("health_Max" + suffix)) + "）修为（"
                        + WHOLE.format(data.getDouble("experience" + suffix)) + "/"
                        + WHOLE.format(data.getDouble("experience_Max" + suffix)) + "）"));
            } else {
                lines.add(Component.literal("§f空"));
            }
        }
    }
}

