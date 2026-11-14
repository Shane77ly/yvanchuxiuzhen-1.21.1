package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 阴系四级执行器使用本体字段；来源的队伍通过值在整次有序扫描中保留。 */
final class FormationEffects {
    private FormationEffects() { }

    static void yin(FormationBaseBlockEntity base) {
        CompoundTag data = base.getPersistentData();
        if (!data.getString("jinengleibie").equals("阴")) return;
        String name = data.getString("jinengname");
        Level level = base.getLevel();
        BlockPos pos = base.getBlockPos();
        boolean absolute = data.getBoolean("zhenzuobiao");
        double x = data.getInt("zhenchufaX") + (absolute ? 0 : pos.getX());
        double y = data.getInt("zhenchufaY") + (absolute ? 0 : pos.getY());
        double z = data.getInt("zhenchufaZ") + (absolute ? 0 : pos.getZ());
        if (data.getBoolean("zhensuiji")) {
            x += level.random.nextInt(11) - 5;
            y += level.random.nextInt(6);
            z += level.random.nextInt(11) - 5;
        }
        Vec3 center = new Vec3(x, y, z);
        boolean allowed = false;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(data.getInt("zhenjvli") / 2d))
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
            if (base.matchesTeam(entity)) allowed = true;
            if (!allowed) continue;
            int rank = data.getInt("zhenLEVEL");
            switch (name) {
                case "阴1" -> Cultivation.addEntitySoul(entity, rank * 2);
                case "阴2" -> SpellDamage.markFormationSoulAttack(entity, rank);
                case "阴3" -> entity.discard();
                case "阴4" -> StatusEffects.markEntityNoAi(entity);
                default -> { }
            }
        }
    }
}
