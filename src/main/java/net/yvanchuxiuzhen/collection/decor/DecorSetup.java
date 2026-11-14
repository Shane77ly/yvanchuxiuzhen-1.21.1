package net.yvanchuxiuzhen.collection.decor;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/**
 * 装饰内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class DecorSetup {
    private static final ContentTables CONTENT = new ContentTables("decor")
            .blockFactory("decor:rabbit_cage", traits -> ContentBlock.create(traits, RabbitCageBlock::new))
            .blockFactory("decor:open_rabbit_cage", traits -> ContentBlock.create(traits, OpenRabbitCageBlock::new))
            .itemFactory("decor:cage", RabbitCageItem::new)
            .livingEntity("shitiyizi", DecorSeat::new, DecorSeat::attributes);

    static {
        for (FurnitureStorageVariant variant : FurnitureStorageVariant.values()) {
            CONTENT.blockFactory(variant.factory(), traits -> ContentBlock.create(traits, row -> new FurnitureStorageBlock(row, variant)));
        }
    }

    private DecorSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        RabbitCageBlockEntities.register(modBus);
        FurnitureStorageContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(AuctionSeats::requested);
    }

    public static EntityType<DecorSeat> seatType() { return CONTENT.<DecorSeat>entityType("shitiyizi").get(); }
}
