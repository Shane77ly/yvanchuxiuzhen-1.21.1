package net.yvanchuxiuzhen.system.equipment;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.ItemRow;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.equipment.content.FuchenItem;
import net.yvanchuxiuzhen.system.equipment.content.HossuItem;
import net.yvanchuxiuzhen.system.equipment.projectile.Fuchen;

/**
 * 装备系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EquipmentSetup {
    private static final ContentTables CONTENT = new ContentTables("equipment");

    private EquipmentSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.itemFactory("equipment:fuchen", FuchenItem::new);
        for (ItemRow row : ContentFiles.read("content/equipment/items.json", ItemRow.CODEC)) {
            if (row.factory().startsWith("equipment:weaponhossu_")) {
                LegacyTier tier = LegacyTier.of(row.tool().orElseThrow().tier());
                CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new HossuItem(tier, properties, tooltip));
            }
        }
        CONTENT.entity(Fuchen.ID, Fuchen::new);
        CONTENT.register(modBus);
        NeoForge.EVENT_BUS.addListener(EquipmentWeapons::tick);
        NeoForge.EVENT_BUS.addListener(EquipmentWeapons::swing);
    }

    public static Supplier<EntityType<Fuchen>> fuchenType() {
        return CONTENT.entityType(Fuchen.ID);
    }
}
