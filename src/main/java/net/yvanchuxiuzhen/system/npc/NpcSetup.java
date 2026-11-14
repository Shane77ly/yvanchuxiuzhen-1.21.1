package net.yvanchuxiuzhen.system.npc;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.economy.api.AuctionNpcRequestEvent;
import net.yvanchuxiuzhen.system.npc.api.NpcDefinitions;
import net.yvanchuxiuzhen.system.npc.entity.NpcCreatures;

/**
 * 修士系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class NpcSetup {
    private static final ContentTables CONTENT = new ContentTables("npc");

    private NpcSetup() {
    }

    public static void register(IEventBus modBus) {
        NpcCreatures.register(CONTENT);
        CONTENT.register(modBus);
        NeoForge.EVENT_BUS.addListener(NpcInteractions::interact);
        NeoForge.EVENT_BUS.addListener((AuctionNpcRequestEvent event) -> event.setDefinitions(NpcDefinitions.snapshot()));
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }
}
