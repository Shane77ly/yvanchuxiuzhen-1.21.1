package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;

/** 生物系统的客户端渲染装配。 */
public final class CreatureClientSetup {
    private CreatureClientSetup() {
    }

    public static void register(IEventBus modBus) {
        YaoshouClient.register(modBus);
        ButterflyClient.register(modBus);
        OrganismClient.register(modBus);
    }
}
