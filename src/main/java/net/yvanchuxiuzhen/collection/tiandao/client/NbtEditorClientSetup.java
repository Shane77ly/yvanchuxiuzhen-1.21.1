package net.yvanchuxiuzhen.collection.tiandao.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.collection.tiandao.NbtEditorSetup;

/** 物品NBT编辑页的客户端装配，不参与专用服务端类加载。 */
public final class NbtEditorClientSetup {
    private NbtEditorClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(NbtEditorSetup.MENU.get(), NbtEditorScreen::new));
    }
}
