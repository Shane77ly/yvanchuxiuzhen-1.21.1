package net.yvanchuxiuzhen.system.npc;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.npc.content.QiankunmentuBlock;
import net.yvanchuxiuzhen.system.npc.content.QiankunMenus;
import net.yvanchuxiuzhen.system.npc.content.QiankunBlockItem;
import net.yvanchuxiuzhen.system.npc.content.QiankunBlockEntities;
import net.yvanchuxiuzhen.system.npc.content.QiankunBlock;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.economy.api.AuctionNpcRequestEvent;
import net.yvanchuxiuzhen.system.npc.api.NpcDefinitions;
import net.yvanchuxiuzhen.system.npc.entity.NpcCreatures;
import net.yvanchuxiuzhen.system.npc.entity.NpcProfiles;
import net.yvanchuxiuzhen.system.npc.entity.NpcUnderworldCreatures;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/** 修士系统的启动装配，只在 Mod 构造时调用一次。 */
public final class NpcSetup {
    private static final ContentTables CONTENT = new ContentTables("npc")
            .blockFactory("npc:qiankundaoren", traits -> ContentBlock.create(traits, QiankunBlock::new))
            .blockFactory("npc:qiankunmentu", traits -> ContentBlock.create(traits, QiankunmentuBlock::new))
            .itemFactory("npc:qiankundaoren", (properties, tooltip) -> new QiankunBlockItem(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "qiankundaoren")), properties, tooltip))
            .itemFactory("npc:qiankunmentu", (properties, tooltip) -> new QiankunBlockItem(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "qiankunmentu")), properties, tooltip));

    private NpcSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.itemFactory("npc:manager", NpcManagementItem::new);
        NpcCreatures.register(CONTENT);
        NpcUnderworldCreatures.register(CONTENT);
        CONTENT.register(modBus);
        QiankunBlockEntities.register(modBus);
        QiankunMenus.register(modBus);
        NpcManagementMenus.register(modBus);
        modBus.addListener(NpcManagementMenus::registerPayloads);
        NeoForge.EVENT_BUS.addListener(NpcInteractions::interact);
        NeoForge.EVENT_BUS.addListener(NpcProfiles::applyOwnedProfile);
        NeoForge.EVENT_BUS.addListener(NpcFollowerRuntime::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, StructureReleasedEvent.class, NpcStructureReleases::structureReleased);
        NeoForge.EVENT_BUS.addListener((AuctionNpcRequestEvent event) -> event.setDefinitions(NpcDefinitions.snapshot()));
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }
}
