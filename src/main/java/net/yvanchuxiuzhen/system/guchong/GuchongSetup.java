package net.yvanchuxiuzhen.system.guchong;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongCreatures;
import net.yvanchuxiuzhen.system.guchong.content.GuchongBlockEntities;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlock;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanItem;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;
import net.yvanchuxiuzhen.system.guchong.content.GuchongMenus;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 蛊虫系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class GuchongSetup {
    private static final ContentTables CONTENT = new ContentTables("guchong")
            .blockFactory("guchong:taoguan_01", traits -> ContentBlock.create(traits, value -> new TaoguanBlock(value, 1)))
            .blockFactory("guchong:taoguan_02", traits -> ContentBlock.create(traits, value -> new TaoguanBlock(value, 2)))
            .blockFactory("guchong:taoguan_03", traits -> ContentBlock.create(traits, value -> new TaoguanBlock(value, 3)));

    private GuchongSetup() {
    }

    public static void register(IEventBus modBus) {
        GuchongCreatures.register(CONTENT, modBus);
        for (int number = 1; number <= 3; number++) {
            int grade = number;
            CONTENT.itemFactory("guchong:taoguan_0" + grade + "item", (properties, tooltip) -> new TaoguanItem(properties, tooltip, grade));
        }
        CONTENT.register(modBus);
        GuchongBlockEntities.register(modBus);
        GuchongMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity().containerMenu instanceof TaoguanMenu menu
                    && TaoguanItem.grade(event.getEntity().getMainHandItem()) != menu.grade()) event.getEntity().closeContainer();
        });
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }
}
