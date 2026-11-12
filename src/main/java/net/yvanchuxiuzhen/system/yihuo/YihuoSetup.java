package net.yvanchuxiuzhen.system.yihuo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import java.util.function.Supplier;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.material.api.Materials;

/**
 * 异火系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class YihuoSetup {
    private static final ContentTables CONTENT = new ContentTables("yihuo");

    private YihuoSetup() {
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        CONTENT.blockFactory("yihuo:fire", traits -> ContentBlock.create(traits, FireBlock::new));
        for (int material : YihuoProjectiles.ids()) {
            CONTENT.itemFactory("yihuo:cailiao_" + material,
                    (properties, tooltip) -> new YihuoItem(material, properties, tooltip));
        }
        YihuoEffects.register(CONTENT);
        YihuoProjectiles.register(CONTENT);
        CONTENT.register(modBus);
        YihuoBlockEntities.register(modBus);
        NeoForge.EVENT_BUS.addListener(YihuoSetup::onLogin);
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Materials.initializeYihuo(player);
        }
    }
}
