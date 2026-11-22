package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenus;

/** 生物系统的客户端渲染装配。 */
public final class CreatureClientSetup {
    private CreatureClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(SpiritBeastBagDyeMenus.TYPE.get(), SpiritBeastBagDyeScreen::new));
        NeoForge.EVENT_BUS.addListener((ItemTossEvent event) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof SpiritBeastBagDyeScreen && event.getEntity().getItem().is(SpiritBeastBagDyeMenus.toolItem())) {
                minecraft.player.closeContainer();
            }
        });
        YaoshouClient.register(modBus);
        ButterflyClient.register(modBus);
        OrganismClient.register(modBus);
        NaturalCreatureClient.register(modBus);
        GhostCreatureClient.register(modBus);
        BossCreatureClient.register(modBus);
        CoffinClient.register(modBus);
        JiantouClient.register(modBus);
    }
}
