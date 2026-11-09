package net.yvanchuxiuzhen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yvanchuxiuzhen.collection.decor.client.DecorClientSetup;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.ModifierKeysClient;
import net.yvanchuxiuzhen.system.combat.client.CombatClientSetup;
import net.yvanchuxiuzhen.system.equipment.client.EquipmentClientSetup;
import net.yvanchuxiuzhen.system.gongfa.client.GongfaClientSetup;
import net.yvanchuxiuzhen.system.material.client.MaterialClientSetup;
import net.yvanchuxiuzhen.system.liandan.client.LiandanClientSetup;
import net.yvanchuxiuzhen.system.lianqi.client.LianqiClientSetup;
import net.yvanchuxiuzhen.panel.client.PanelClientSetup;
import net.yvanchuxiuzhen.system.particle.client.ParticleClientSetup;
import net.yvanchuxiuzhen.system.relation.client.RelationClientSetup;
import net.yvanchuxiuzhen.system.skill.client.SkillClientSetup;
import net.yvanchuxiuzhen.system.storage.client.StorageClientSetup;
import net.yvanchuxiuzhen.system.yihuo.client.YihuoClientSetup;
import net.yvanchuxiuzhen.system.xiulian.client.XiulianClientSetup;

/**
 * 客户端入口。FML 只在物理客户端构造本类，并在公共入口 {@link YvanchuxiuzhenMod} 之后构造；
 * 只调用各系统与内容族的 {@code <名称>ClientSetup.register}。
 */
@Mod(value = ModId.VALUE, dist = Dist.CLIENT)
public final class YvanchuxiuzhenClient {
    public YvanchuxiuzhenClient(IEventBus modBus) {
        ModifierKeysClient.register();
        ParticleClientSetup.register(modBus);
        CombatClientSetup.register(modBus);
        EquipmentClientSetup.register(modBus);
        DecorClientSetup.register(modBus);
        GongfaClientSetup.register(modBus);
        PanelClientSetup.register(modBus);
        RelationClientSetup.register(modBus);
        SkillClientSetup.register(modBus);
        StorageClientSetup.register(modBus);
        MaterialClientSetup.register(modBus);
        LiandanClientSetup.register(modBus);
        LianqiClientSetup.register(modBus);
        YihuoClientSetup.register(modBus);
        XiulianClientSetup.register(modBus);
    }
}
