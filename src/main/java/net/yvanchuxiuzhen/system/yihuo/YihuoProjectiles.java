package net.yvanchuxiuzhen.system.yihuo;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/** 异火材料投掷物的内容登记与编号表。 */
public final class YihuoProjectiles {
    private static final List<Integer> IDS = List.of(221, 222, 223, 224, 225, 226, 227, 228, 229,
            231, 232, 233, 234, 235, 236, 237, 238, 239);

    private YihuoProjectiles() {
    }

    public static List<Integer> ids() {
        return IDS;
    }

    public static void register(ContentTables tables) {
        for (int material : IDS) {
            tables.<YihuoProjectile>entity("projectile_cailiao_" + material,
                    (type, level) -> new YihuoProjectile(type, level, material));
        }
    }

    public static java.util.function.Supplier<EntityType<YihuoProjectile>> type(int material) {
        return YihuoSetup.entityType("projectile_cailiao_" + material);
    }
}
