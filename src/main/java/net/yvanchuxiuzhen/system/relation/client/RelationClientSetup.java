package net.yvanchuxiuzhen.system.relation.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.relation.content.RelationMenus;

/** 关系系统的客户端装配：两个菜单的界面。 */
public final class RelationClientSetup {
    private RelationClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RelationClientSetup::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RelationMenus.FRIEND.get(), FriendScreen::new);
        event.register(RelationMenus.TEAM_REQUEST.get(), TeamRequestScreen::new);
    }
}
