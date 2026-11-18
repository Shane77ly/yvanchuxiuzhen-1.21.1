package net.yvanchuxiuzhen.collection.decor;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.plant.api.PlantSoilSupport;

/**
 * 装饰内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class DecorSetup {
    private static final ContentTables CONTENT = new ContentTables("decor")
            .blockFactory("decor:plant_soil", PlantSoilSupport::create)
            .blockFactory("decor:rabbit_cage", traits -> ContentBlock.create(traits, RabbitCageBlock::new))
            .blockFactory("decor:open_rabbit_cage", traits -> ContentBlock.create(traits, OpenRabbitCageBlock::new))
            .blockFactory("decor:cushion", traits -> ContentBlock.create(traits, CushionBlock::new))
            .itemFactory("decor:cage", RabbitCageItem::new)
            .itemFactory("decor:hammer", BuildingHammerItem::new)
            .itemFactory("decor:roof_brush", RoofBrushItem::new)
            .itemFactory("decor:memorial_tablet", MemorialTabletItem::new)
            .livingEntity("shitiyizi", DecorSeat::new, DecorSeat::attributes)
            .livingEntity("shitiputuan", DecorCushion::new, DecorCushion::attributes);

    static {
        CONTENT.blockFactory(AnimatedDecorVariant.FORTUNE.factory(), traits -> ContentBlock.create(traits, AnimatedDecorBlock.Fortune::new));
        CONTENT.blockFactory(AnimatedDecorVariant.LADDER.factory(), traits -> ContentBlock.create(traits, AnimatedDecorBlock::new));
        for (AnimatedDecorVariant variant : AnimatedDecorVariant.values()) {
            CONTENT.itemFactory(variant.factory(), (properties, tooltip) -> new AnimatedDecorItem(variant, properties, tooltip));
        }
        for (FurnitureStorageVariant variant : FurnitureStorageVariant.values()) {
            CONTENT.blockFactory(variant.factory(), traits -> ContentBlock.create(traits, row -> new FurnitureStorageBlock(row, variant)));
        }
        for (DisplayFurnitureVariant variant : DisplayFurnitureVariant.values()) {
            CONTENT.blockFactory(variant.factory(), traits -> ContentBlock.create(traits, row -> row.waterlogged() ? new WaterloggedDisplayFurnitureBlock(row, variant) : new DisplayFurnitureBlock(row, variant)));
        }
        for (InscribedDecorVariant variant : InscribedDecorVariant.VALUES) {
            CONTENT.blockFactory(variant.factory(), traits -> ContentBlock.create(traits, row -> new InscribedDecorBlock(row, variant)));
        }
        for (UtilityFurnitureVariant variant : UtilityFurnitureVariant.values()) {
            CONTENT.blockFactory(variant.factory(), traits -> ContentBlock.create(traits, row -> row.waterlogged() ? new WaterloggedUtilityFurnitureBlock(row, variant) : new UtilityFurnitureBlock(row, variant)));
        }
    }

    private DecorSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        HotOil.register(modBus);
        RabbitCageBlockEntities.register(modBus);
        FurnitureStorageContent.register(modBus);
        DisplayFurnitureContent.register(modBus);
        InscribedDecorContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(InscribedDecor::rightClick);
        AnimatedDecorContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(FortuneDraws::tick);
        NeoForge.EVENT_BUS.addListener(FortuneDraws::stopped);
        UtilityFurnitureContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(DisplayFurnitureMenuActions::tick);
        NeoForge.EVENT_BUS.addListener(AuctionSeats::requested);
        NeoForge.EVENT_BUS.addListener(BuildingHammer::leftClick);
    }

    public static EntityType<DecorCushion> cushionType() { return CONTENT.<DecorCushion>entityType("shitiputuan").get(); }

    public static EntityType<DecorSeat> seatType() { return CONTENT.<DecorSeat>entityType("shitiyizi").get(); }
}
