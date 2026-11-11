package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBoardBlock;
import net.yvanchuxiuzhen.system.cooking.content.CleaverBlock;
import net.yvanchuxiuzhen.system.cooking.content.CleaverBlockItem;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.TablewareBlock;
import net.yvanchuxiuzhen.system.cooking.content.TablewareBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.IronPlateBlock;
import net.yvanchuxiuzhen.system.cooking.content.CookingBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.CookingMenus;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlock;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockItem;
import net.yvanchuxiuzhen.system.cooking.network.CookingButtonPayload;
import net.yvanchuxiuzhen.system.cooking.content.NaturalMonkeyWineBlock;
import net.yvanchuxiuzhen.system.cooking.content.WineBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.WineItem;
import net.yvanchuxiuzhen.system.cooking.content.WineJarBlock;
import net.yvanchuxiuzhen.system.cooking.content.WineMenus;
import net.yvanchuxiuzhen.system.cooking.content.WoodenStillBlock;
import net.yvanchuxiuzhen.system.cooking.content.MealFoodItem;
import net.yvanchuxiuzhen.system.cooking.content.PlantableIngredientItem;
import net.yvanchuxiuzhen.system.cooking.content.PlatedFoodBlock;
import net.yvanchuxiuzhen.system.cooking.content.PoisonousFoodItem;
import net.yvanchuxiuzhen.system.cooking.content.RemainderFoodItem;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlock;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockItem;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.SteamerMenus;
import net.yvanchuxiuzhen.system.cooking.network.SteamerButtonPayload;
import net.yvanchuxiuzhen.system.cooking.data.CookingAttachments;
import net.yvanchuxiuzhen.system.cooking.network.WineButtonPayload;
import net.yvanchuxiuzhen.system.cooking.api.Alcohol;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterBreakthroughEvent;
import net.yvanchuxiuzhen.system.plant.api.Planting;

/**
 * 烹饪系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CookingSetup {
    private static final ContentTables CONTENT = new ContentTables("cooking")
            .blockFactory("cooking:iron_plate", traits -> ContentBlock.create(traits, IronPlateBlock::new))
            .blockFactory("cooking:cutting_board", traits -> ContentBlock.create(traits, CuttingBoardBlock::new))
            .blockFactory("cooking:cleaver", traits -> ContentBlock.create(traits, CleaverBlock::new))
            .itemFactory("cooking:cleaver", (properties, tooltip) -> new CleaverBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "caidaoblock")), properties, tooltip))
            .blockFactory("cooking:bowl", traits -> ContentBlock.create(traits,
                    row -> new TablewareBlock.Waterlogged(row, TablewareBlock.Kind.BOWL)))
            .blockFactory("cooking:plate", traits -> ContentBlock.create(traits,
                    row -> new TablewareBlock(row, TablewareBlock.Kind.PLATE)))
            .blockFactory("cooking:large_bowl", traits -> ContentBlock.create(traits,
                    row -> new TablewareBlock.Waterlogged(row, TablewareBlock.Kind.LARGE_BOWL)))
            .blockFactory("cooking:plated_food", traits -> ContentBlock.create(traits, PlatedFoodBlock::new))
            .itemFactory("cooking:meal_food", MealFoodItem::new)
            .itemFactory("cooking:lotus_seed", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::lotus))
            .itemFactory("cooking:plantable_garlic", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::garlic))
            .itemFactory("cooking:produce028_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai028))
            .itemFactory("cooking:produce029_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai029))
            .itemFactory("cooking:produce030_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai030))
            .itemFactory("cooking:produce031_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai031))
            .itemFactory("cooking:produce032_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai032))
            .itemFactory("cooking:produce033_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai033))
            .itemFactory("cooking:fungus040_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai040))
            .itemFactory("cooking:fungus041_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai041))
            .itemFactory("cooking:fungus042_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai042))
            .itemFactory("cooking:fungus043_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai043))
            .itemFactory("cooking:fungus044_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai044))
            .itemFactory("cooking:fungus045_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai045))
            .itemFactory("cooking:legume046_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai046))
            .itemFactory("cooking:legume047_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai047))
            .itemFactory("cooking:legume048_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai048))
            .itemFactory("cooking:legume049_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai049))
            .itemFactory("cooking:legume050_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai050))
            .itemFactory("cooking:legume051_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai051))
            .itemFactory("cooking:legume052_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai052))
            .itemFactory("cooking:legume053_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai053))
            .itemFactory("cooking:legume054_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai054))
            .itemFactory("cooking:legume055_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai055))
            .itemFactory("cooking:legume056_food", (properties, tooltip) -> new PlantableIngredientItem(properties, tooltip, Planting::shicai056))
            .itemFactory("cooking:poisonous_food", PoisonousFoodItem::new)
            .itemFactory("cooking:remainder_bone", (properties, tooltip) -> new RemainderFoodItem(properties, tooltip,
                    ResourceLocation.withDefaultNamespace("bone")))
            .itemFactory("cooking:remainder_shicai_03401", (properties, tooltip) -> new RemainderFoodItem(properties, tooltip,
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_03401")))
            .itemFactory("cooking:remainder_shicai_03601", (properties, tooltip) -> new RemainderFoodItem(properties, tooltip,
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_03601")))
            .itemFactory("cooking:remainder_shicai_03701", (properties, tooltip) -> new RemainderFoodItem(properties, tooltip,
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_03701")))
            .blockFactory("cooking:steamer", traits -> ContentBlock.create(traits, SteamerBlock.Pot::new))
            .blockFactory("cooking:steaming_basket", traits -> ContentBlock.create(traits, SteamerBlock.Tray::new))
            .itemFactory("cooking:steamer", (properties, tooltip) -> new SteamerBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "foodzhengguo")), properties, tooltip))
            .itemFactory("cooking:steaming_basket", (properties, tooltip) -> new SteamerBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "foodlongti")), properties, tooltip))
            .blockFactory("cooking:wooden_still", traits -> ContentBlock.create(traits, WoodenStillBlock::new))
            .blockFactory("cooking:wine_jar", traits -> ContentBlock.create(traits, WineJarBlock::new))
            .blockFactory("cooking:waterlogged_wine_jar", traits -> ContentBlock.create(traits, WineJarBlock.Waterlogged::new))
            .blockFactory("cooking:natural_monkey_wine", traits -> ContentBlock.create(traits, NaturalMonkeyWineBlock::new))
            .itemFactory("cooking:wine", WineItem::new)
            .blockFactory("cooking:iron_pot", traits -> ContentBlock.create(traits, CookingPotBlock.Iron::new))
            .blockFactory("cooking:clay_pot", traits -> ContentBlock.create(traits, CookingPotBlock.Clay::new))
            .itemFactory("cooking:iron_pot", (properties, tooltip) -> new CookingPotBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fooddaguo")), properties, tooltip))
            .itemFactory("cooking:clay_pot", (properties, tooltip) -> new CookingPotBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fooddunguo")), properties, tooltip));

    private CookingSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.effectBehavior("effect_031", new FastingEffect());
        CONTENT.register(modBus);
        NeoForge.EVENT_BUS.addListener(CookingRecipeCommands::register);
        TablewareBlockEntities.register(modBus);
        CuttingBlockEntities.register(modBus);
        NeoForge.EVENT_BUS.addListener(CuttingRuntime::leftClick);
        CookingBlockEntities.register(modBus);
        CookingMenus.register(modBus);
        SteamerBlockEntities.register(modBus);
        SteamerMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener(SteamerRuntime::rightClick);
        WineBlockEntities.register(modBus);
        WineMenus.register(modBus);
        CookingAttachments.register(modBus);
        NeoForge.EVENT_BUS.addListener((CultivationAfterBreakthroughEvent event) -> Alcohol.metabolize(event.player()));
        modBus.addListener(CookingSetup::payloads);
        NeoForge.EVENT_BUS.addListener(CookingRuntime::rightClick);
        NeoForge.EVENT_BUS.addListener(CookingDelays::tick);
        NeoForge.EVENT_BUS.addListener(CookingDelays::stopped);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SteamerButtonPayload.TYPE, SteamerButtonPayload.STREAM_CODEC, SteamerButtonPayload::handle);
        event.registrar("1").playToServer(WineButtonPayload.TYPE, WineButtonPayload.STREAM_CODEC, WineButtonPayload::handle);
        event.registrar("1").playToServer(CookingButtonPayload.TYPE, CookingButtonPayload.STREAM_CODEC, CookingButtonPayload::handle);
    }
}
