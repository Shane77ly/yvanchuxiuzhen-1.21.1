package net.yvanchuxiuzhen;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yvanchuxiuzhen.collection.decor.DecorSetup;
import net.yvanchuxiuzhen.platform.Namespaces;
import net.yvanchuxiuzhen.system.craft.CraftSetup;
import net.yvanchuxiuzhen.system.material.MaterialSetup;
import net.yvanchuxiuzhen.system.skill.SkillSetup;

/**
 * 公共入口，两端都会构造。只调用各系统与内容族的 {@code <名称>Setup.register}，
 * 注册器与监听由各自的 Setup 登记。
 */
@Mod(Namespaces.XIUZHEN)
public final class YvanchuxiuzhenMod {
    public YvanchuxiuzhenMod(IEventBus modBus) {
        MaterialSetup.register(modBus);
        SkillSetup.register(modBus);
        CraftSetup.register(modBus);
        DecorSetup.register(modBus);
    }
}
