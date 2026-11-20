package net.yvanchuxiuzhen.system.creature;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.creature.content.CoffinContent;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenus;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastDyeToolInventory;
import net.yvanchuxiuzhen.system.creature.entity.BossCreatures;
import net.yvanchuxiuzhen.system.creature.entity.Butterflies;
import net.yvanchuxiuzhen.system.creature.entity.GhostCreatures;
import net.yvanchuxiuzhen.system.creature.entity.NaturalCreatures;
import net.yvanchuxiuzhen.system.creature.entity.Organisms;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou053BlizzardEffect;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouCreatures;
import net.yvanchuxiuzhen.system.creature.item.SpiritBeastBagItem;
import net.yvanchuxiuzhen.system.creature.item.SpiritBeastDyeToolItem;
import net.yvanchuxiuzhen.system.creature.item.YaoshouProjectileItem;

/**
 * 生物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CreatureSetup {
    private static final ContentTables CONTENT = new ContentTables("creature");

    private CreatureSetup() {
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        CoffinContent.register(CONTENT, modBus);
        CONTENT.effectBehavior("yaoshou_053baofengxve", new Yaoshou053BlizzardEffect());
        CONTENT.itemFactory("creature:spirit_beast_bag", SpiritBeastBagItem::new);
        CONTENT.itemFactory("creature:spirit_beast_dye_tool", SpiritBeastDyeToolItem::new);
        CONTENT.itemFactory("creature:yaoshou_projectile", YaoshouProjectileItem::new);
        SpiritBeastBagDyeMenus.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, context) -> new SpiritBeastDyeToolInventory(stack), SpiritBeastBagDyeMenus.toolItem()));
        NeoForge.EVENT_BUS.addListener(SpiritBeastBagRuntime::interact);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, false, EntityTickEvent.Pre.class, YaoshouRecovery::onTick);
        NeoForge.EVENT_BUS.addListener(YaoshouGrowth::onDeath);
        NeoForge.EVENT_BUS.addListener(GuchongGrowthConsumer::onVictimData);
        NeoForge.EVENT_BUS.addListener(WildPlantCreatures::spawn);
        NeoForge.EVENT_BUS.addListener(GuchongGrowthConsumer::onUpgrade);
        YaoshouCreatures.register(CONTENT, modBus);
        Butterflies.register(CONTENT, modBus);
        Organisms.register(CONTENT, modBus);
        NaturalCreatures.register(CONTENT, modBus);
        GhostCreatures.register(CONTENT);
        BossCreatures.register(CONTENT);
        NichangGrassAttraction.register();
        TalismanTaming.register();
        TalismanCorpseSealing.register();
        CONTENT.register(modBus);
    }
}
