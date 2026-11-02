package net.yvanchuxiuzhen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.yvanchuxiuzhen.platform.Namespaces;

/**
 * 客户端入口。FML 只在物理客户端构造本类，并在公共入口 {@link YvanchuxiuzhenMod} 之后构造。
 */
@Mod(value = Namespaces.XIUZHEN, dist = Dist.CLIENT)
public final class YvanchuxiuzhenClient {
}
