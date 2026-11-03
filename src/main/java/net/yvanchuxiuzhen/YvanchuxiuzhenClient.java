package net.yvanchuxiuzhen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yvanchuxiuzhen.collection.decor.client.DecorClientSetup;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.client.RelationClientSetup;
import net.yvanchuxiuzhen.system.xiulian.client.XiulianClientSetup;

/**
 * 客户端入口。FML 只在物理客户端构造本类，并在公共入口 {@link YvanchuxiuzhenMod} 之后构造；
 * 只调用各系统与内容族的 {@code <名称>ClientSetup.register}。
 */
@Mod(value = ModId.VALUE, dist = Dist.CLIENT)
public final class YvanchuxiuzhenClient {
    public YvanchuxiuzhenClient(IEventBus modBus) {
        DecorClientSetup.register(modBus);
        RelationClientSetup.register(modBus);
        XiulianClientSetup.register(modBus);
    }
}
