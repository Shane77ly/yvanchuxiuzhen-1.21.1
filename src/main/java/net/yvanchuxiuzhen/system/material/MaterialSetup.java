package net.yvanchuxiuzhen.system.material;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;
import net.yvanchuxiuzhen.system.material.content.MaterialMenus;
import net.yvanchuxiuzhen.system.material.content.MoltenMetalItem;
import net.yvanchuxiuzhen.system.material.content.PeachFoodItem;
import net.yvanchuxiuzhen.system.material.content.MaterialOreBlock;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisButtonPayload;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisViewPayload;

/**
 * 材料系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class MaterialSetup {
    private static final ContentTables CONTENT = new ContentTables("material")
            .blockFactory("material:ore_with_fallback", MaterialOreBlock::createOre)
            .itemFactory("material:peach_food", PeachFoodItem::new);

    static {
        for (int first : new int[]{201, 211}) {
            for (int index = 0; index < 9; index++) {
                int number = first + index;
                int level = Math.min(40, 3 + 5 * index);
                CONTENT.itemFactory("material:molten_metal_" + number,
                        (properties, tooltip) -> new MoltenMetalItem(properties, tooltip, number, level));
            }
        }
    }

    private MaterialSetup() {
    }

    public static void register(IEventBus modBus) {
        MaterialData.register(modBus);
        MaterialMenus.register(modBus);
        CONTENT.register(modBus);
        modBus.addListener(MaterialSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (event.getEntity() instanceof ServerPlayer player
                    && BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString().equals("yvanchuxiuzhen:qiancailubook")) {
                player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new MaterialAnalysisMenu(id, inventory, player.blockPosition()),
                        Component.literal("QiancailuGui")), player.blockPosition());
            }
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(MaterialAnalysisButtonPayload.TYPE, MaterialAnalysisButtonPayload.STREAM_CODEC, MaterialAnalysisButtonPayload::handle)
                .playToClient(MaterialAnalysisViewPayload.TYPE, MaterialAnalysisViewPayload.STREAM_CODEC, MaterialAnalysisViewPayload::handle);
    }
}
