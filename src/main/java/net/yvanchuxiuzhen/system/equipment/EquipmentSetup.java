package net.yvanchuxiuzhen.system.equipment;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.ItemRow;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import net.yvanchuxiuzhen.system.equipment.content.Fabao010Item;
import net.yvanchuxiuzhen.system.equipment.content.Fabao010huoyanItem;
import net.yvanchuxiuzhen.system.equipment.content.Fabao011Item;
import net.yvanchuxiuzhen.system.equipment.content.Fabao012Item;
import net.yvanchuxiuzhen.system.equipment.content.Fabao014Item;
import net.yvanchuxiuzhen.system.equipment.content.Fabao016Item;
import net.yvanchuxiuzhen.system.equipment.content.Fabao022Item;
import net.yvanchuxiuzhen.system.equipment.content.FuchenItem;
import net.yvanchuxiuzhen.system.equipment.content.EquipmentMenus;
import net.yvanchuxiuzhen.system.equipment.content.HossuItem;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.projectile.Fabao010huoyan;
import net.yvanchuxiuzhen.system.equipment.projectile.Fuchen;
import net.yvanchuxiuzhen.system.equipment.projectile.KailingProjectile;
import net.yvanchuxiuzhen.system.equipment.projectile.Fabao014;

/**
 * 装备系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EquipmentSetup {
    private static final ContentTables CONTENT = new ContentTables("equipment");

    private EquipmentSetup() {
    }

    public static void register(IEventBus modBus) {
        EquipmentData.register(modBus);
        EquipmentMenus.register(modBus);
        CONTENT.itemFactory("equipment:fuchen", FuchenItem::new);
        CONTENT.itemFactory("equipment:fabao_010", Fabao010Item::new);
        CONTENT.itemFactory("equipment:fabao_010huoyan", Fabao010huoyanItem::new);
        for (ItemRow row : ContentFiles.read("content/equipment/items.json", ItemRow.CODEC)) {
            if (row.factory().equals("equipment:fabao_011")) {
                LegacyTier tier = LegacyTier.of(row.tool().orElseThrow().tier());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new Fabao011Item(tier, properties, tooltip));
            }
            if (row.factory().equals("equipment:fabao_012")) {
                LegacyTier tier = LegacyTier.of(row.tool().orElseThrow().tier());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new Fabao012Item(tier, properties, tooltip));
            }
            if (row.factory().equals("equipment:fabao_014")) {
                CONTENT.itemFactory(row.factory(), Fabao014Item::new);
            }
            if (row.factory().equals("equipment:fabao_016")) {
                LegacyTier tier = LegacyTier.of(row.tool().orElseThrow().tier());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new Fabao016Item(tier, properties, tooltip));
            }
            if (row.factory().equals("equipment:fabao_022")) {
                CONTENT.itemFactory(row.factory(), Fabao022Item::new);
            }
            if (row.factory().startsWith("equipment:weaponhossu_")) {
                LegacyTier tier = LegacyTier.of(row.tool().orElseThrow().tier());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new HossuItem(tier, properties, tooltip));
            }
            if (row.factory().startsWith("equipment:yaohua_0101_")) {
                ItemRow.Armor armor = row.armor().orElseThrow();
                ArmorItem.Type type = ArmorItem.Type.valueOf(armor.slot());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new DemonArmorItem(CONTENT.armorMaterial(armor.material()), type,
                        properties.durability(type.getDurability(row.durabilityMultiplier().orElseThrow()))));
            }
        }
        CONTENT.entity(Fuchen.ID, Fuchen::new);
        CONTENT.entity(Fabao010huoyan.ID, Fabao010huoyan::new);
        for (EntityModelRow model : ContentFiles.read("content/equipment/entity_models.json", EntityModelRow.CODEC)) {
            ToolMachineEntity.Kind kind = switch (model.id()) {
                case "gongjvfu" -> ToolMachineEntity.Kind.AXE;
                case "gongjvgao" -> ToolMachineEntity.Kind.PICKAXE;
                case "gongjvchan" -> ToolMachineEntity.Kind.HOE;
                case "gongjvchu" -> ToolMachineEntity.Kind.SHOVEL;
                default -> null;
            };
            if (kind != null) {
                CONTENT.<ToolMachineEntity>livingEntity(model.id(), (type, level) -> new ToolMachineEntity(type, level, model, kind), () -> ToolMachineEntity.attributes(model));
            }
        }
        CONTENT.<KailingProjectile>entity(KailingProjectile.ID, KailingProjectile::new);
        CONTENT.<Fabao014>entity(Fabao014.ID, Fabao014::new);
        CONTENT.register(modBus);
        NeoForge.EVENT_BUS.addListener(EquipmentWeapons::tick);
        NeoForge.EVENT_BUS.addListener(EquipmentWeapons::rightClick);
        NeoForge.EVENT_BUS.addListener(AccessoryCommand::register);
        NeoForge.EVENT_BUS.addListener(Accessories::tick);
        NeoForge.EVENT_BUS.addListener(Accessories::flight);
        NeoForge.EVENT_BUS.addListener(EquipmentWeapons::swing);
        NeoForge.EVENT_BUS.addListener(DemonTransformation::request);
        NeoForge.EVENT_BUS.addListener(DemonTransformation::tick);
        NeoForge.EVENT_BUS.addListener(DemonTransformation::endTick);
        NeoForge.EVENT_BUS.addListener(DemonTransformation::stopped);
        NeoForge.EVENT_BUS.addListener(EquipmentAttributes::tick);
        NeoForge.EVENT_BUS.addListener(EquipmentAttributes::armorStat);
        NeoForge.EVENT_BUS.addListener(EquipmentAttributes::weaponStat);
        NeoForge.EVENT_BUS.addListener(EquipmentAttributes::modifiers);
    }

    public static Supplier<EntityType<Fuchen>> fuchenType() {
        return CONTENT.entityType(Fuchen.ID);
    }

    public static Supplier<EntityType<KailingProjectile>> kailingProjectileType() {
        return CONTENT.entityType(KailingProjectile.ID);
    }

    public static Supplier<EntityType<Fabao014>> fabao014Type() {
        return CONTENT.entityType(Fabao014.ID);
    }

    public static Supplier<EntityType<Fabao010huoyan>> fabao010FireType() {
        return CONTENT.entityType(Fabao010huoyan.ID);
    }

    public static Supplier<EntityType<ToolMachineEntity>> toolMachineType(ToolMachineEntity.Kind kind) {
        return CONTENT.entityType(kind.id());
    }
}
