package net.yvanchuxiuzhen;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.collection.decor.DecorSetup;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.collection.tiandao.TiandaoSetup;
import net.yvanchuxiuzhen.system.attribute.AttributeSetup;
import net.yvanchuxiuzhen.system.book.BookSetup;
import net.yvanchuxiuzhen.system.combat.CombatSetup;
import net.yvanchuxiuzhen.system.cooking.CookingSetup;
import net.yvanchuxiuzhen.system.craft.CraftSetup;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.dungeon.DungeonSetup;
import net.yvanchuxiuzhen.system.economy.EconomySetup;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.gongfa.GongfaSetup;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.liandan.LiandanSetup;
import net.yvanchuxiuzhen.system.lianqi.LianqiSetup;
import net.yvanchuxiuzhen.system.material.MaterialSetup;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.particle.ParticleSetup;
import net.yvanchuxiuzhen.system.plant.PlantSetup;
import net.yvanchuxiuzhen.system.quest.QuestSetup;
import net.yvanchuxiuzhen.system.relation.RelationSetup;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.storage.StorageSetup;
import net.yvanchuxiuzhen.system.town.TownSetup;
import net.yvanchuxiuzhen.system.treasure.TreasureSetup;
import net.yvanchuxiuzhen.system.world.WorldSetup;
import net.yvanchuxiuzhen.system.xiulian.XiulianSetup;
import net.yvanchuxiuzhen.system.yihuo.YihuoSetup;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.RegistryExport;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.platform.content.ContentTabs;

/**
 * 公共入口，两端都会构造。只调用各系统与内容族的 {@code <名称>Setup.register}，
 * 注册器与监听由各自的 Setup 登记。
 */
@Mod(ModId.VALUE)
public final class YvanchuxiuzhenMod {
    public YvanchuxiuzhenMod(IEventBus modBus) {
        RegistryExport.register();
        ModifierKeys.register(modBus);
        ContentTabs.register(modBus);
        PanelNavigation.register(modBus);
        ParticleSetup.register(modBus);
        AttributeSetup.register(modBus);
        BookSetup.register(modBus);
        CombatSetup.register(modBus);
        CookingSetup.register(modBus);
        CraftSetup.register(modBus);
        CreatureSetup.register(modBus);
        DungeonSetup.register(modBus);
        EconomySetup.register(modBus);
        EquipmentSetup.register(modBus);
        FuluSetup.register(modBus);
        GongfaSetup.register(modBus);
        GuchongSetup.register(modBus);
        LiandanSetup.register(modBus);
        LianqiSetup.register(modBus);
        MaterialSetup.register(modBus);
        NpcSetup.register(modBus);
        PlantSetup.register(modBus);
        QuestSetup.register(modBus);
        RelationSetup.register(modBus);
        SkillSetup.register(modBus);
        StorageSetup.register(modBus);
        TownSetup.register(modBus);
        TreasureSetup.register(modBus);
        WorldSetup.register(modBus);
        XiulianSetup.register(modBus);
        YihuoSetup.register(modBus);
        ZhenfaSetup.register(modBus);
        DecorSetup.register(modBus);
        FestivalSetup.register(modBus);
        TiandaoSetup.register(modBus);
    }
}
