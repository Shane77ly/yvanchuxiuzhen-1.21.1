package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 装饰内容族的客户端装配：云块与草方块的模型继承 {@code block/leaves}，按所在生物群系的草色着色。 */
public final class DecorClientSetup {
    private static final BlockColor GRASS_COLOR = (state, level, pos, tintIndex) ->
            level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.get(0.5D, 1.0D);

    private DecorClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(DecorClientSetup::registerBlockColors);
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(GRASS_COLOR, block("cloudblock"), block("grassblock"));
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
