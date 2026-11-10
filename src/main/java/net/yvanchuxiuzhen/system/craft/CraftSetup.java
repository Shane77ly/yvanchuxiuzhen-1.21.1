package net.yvanchuxiuzhen.system.craft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlock;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockItem;

/**
 * 工艺系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CraftSetup {
    private static final ContentTables CONTENT = new ContentTables("craft")
            .blockFactory("craft:stone_mill", traits -> ContentBlock.create(traits,
                    value -> new StoneMillBlock(value, "stonemill")))
            .blockFactory("craft:stone_roller", traits -> ContentBlock.create(traits,
                    value -> new StoneMillBlock(value, "stoneroller")))
            .itemFactory("craft:stone_mill", (properties, tooltip) -> new StoneMillBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "stonemill")), properties, tooltip))
            .itemFactory("craft:stone_roller", (properties, tooltip) -> new StoneMillBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "stoneroller")), properties, tooltip));

    private CraftSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        CraftBlockEntities.register(modBus);
        CraftMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener(CraftCommands::register);
    }
}
