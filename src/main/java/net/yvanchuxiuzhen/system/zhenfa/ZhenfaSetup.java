package net.yvanchuxiuzhen.system.zhenfa;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationArrowItem;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationBaseBlock;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationDisplayItem;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationElementBlock;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationFlagItem;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationMarkerBlock;
import net.yvanchuxiuzhen.system.zhenfa.content.ZhenfaBlockEntities;
import net.yvanchuxiuzhen.system.zhenfa.data.FormationAttachments;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationActor;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationArrow;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationFalling;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationLauncher;

/**
 * 阵法系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class ZhenfaSetup {
    private static final ContentTables CONTENT = new ContentTables("zhenfa")
            .blockFactory("zhenfa:element", traits -> ContentBlock.create(traits, FormationElementBlock::new))
            .blockFactory("zhenfa:base", traits -> ContentBlock.create(traits, FormationBaseBlock::new))
            .blockFactory("zhenfa:marker", traits -> ContentBlock.create(traits, FormationMarkerBlock::new))
            .itemFactory("zhenfa:flagmother_display", (properties, tooltip) -> display("flagmother", properties, tooltip))
            .itemFactory("zhenfa:flagson_display", (properties, tooltip) -> display("flagson", properties, tooltip))
            .itemFactory("zhenfa:flag_mother", (properties, tooltip) -> flag(properties, tooltip, true))
            .itemFactory("zhenfa:flag_son", (properties, tooltip) -> flag(properties, tooltip, false))
            .itemFactory("zhenfa:compass", (properties, tooltip) -> display("luopan", properties, tooltip));

    private ZhenfaSetup() {
    }

    public static void register(IEventBus modBus) {
        EntityModelRow falling = fallingModel();
        CONTENT.livingEntity("zhenfashiti", FormationLauncher::new, FormationActor::attributes);
        CONTENT.<FormationFalling>livingEntity("zhenjineng_03", (type, level) -> new FormationFalling(type, level, falling), FormationActor::attributes);
        CONTENT.<FormationArrow>entity("projectile_zhenyang_01", FormationArrow::new);
        CONTENT.itemFactory("zhenfa:yang_arrow", (properties, tooltip) -> new FormationArrowItem(properties));
        CONTENT.register(modBus);
        ZhenfaBlockEntities.register(modBus);
        FormationAttachments.register(modBus);
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) { return CONTENT.entityType(id); }

    public static EntityModelRow fallingModel() {
        return ContentFiles.read("content/zhenfa/entity_models.json", EntityModelRow.CODEC).getFirst();
    }

    private static FormationFlagItem flag(Item.Properties properties, List<Component> tooltip, boolean mother) {
        LegacyTier tier = new LegacyTier(0, 4, mother ? 0 : -1, 1, 2, List.of());
        properties.component(DataComponents.UNBREAKABLE, new Unbreakable(false));
        properties.attributes(SwordItem.createAttributes(tier, 3, -2));
        return new FormationFlagItem(tier, properties, tooltip, mother);
    }

    private static FormationDisplayItem display(String id, Item.Properties properties, List<Component> tooltip) {
        return new FormationDisplayItem(
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), properties, tooltip);
    }
}
