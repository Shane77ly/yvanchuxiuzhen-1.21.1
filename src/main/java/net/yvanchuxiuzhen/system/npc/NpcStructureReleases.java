package net.yvanchuxiuzhen.system.npc;

import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/** 结构释放后的修士补充由NPC系统执行，商品刷新沿用来源计分板消费者。 */
public final class NpcStructureReleases {
    private NpcStructureReleases() {
    }

    public static void structureReleased(StructureReleasedEvent event) {
        ServerLevel level = event.level();
        switch (event.structureName()) {
            case "花繁城" -> {
                Vec3 center = new Vec3(-99, 63, -278);
                for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2)).stream()
                        .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                    if (hasIdentity(entity, "businessman_21")) entity.getPersistentData().putString("renwulist2", NpcStructureData.TAVERN_TASK);
                }
                spawn(level, "businessman_09", -75, 63, -360);
                spawn(level, "businessman_10", -65, 63, -363);
                spawn(level, "businessman_11", -74, 63, -356);
            }
            case "雪地商店" -> refresh(level, new Vec3(-319, 70, 985));
            case "平原马车商店" -> refresh(level, new Vec3(220, 72, 377));
            case "平原营地商店" -> refresh(level, new Vec3(-360, 71, 706));
            case "野外营地" -> refresh(level, new Vec3(613, 64, -1168));
            default -> {
            }
        }
    }

    private static boolean hasIdentity(Entity entity, String id) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private static void refresh(ServerLevel level, Vec3 center) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(5)).stream()
                .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
            if (!hasIdentity(entity, "businessman_03")) continue;
            Scoreboard scoreboard = level.getScoreboard();
            Objective objective = scoreboard.getObjective("keyishuaxin");
            if (objective == null) objective = scoreboard.addObjective("keyishuaxin", ObjectiveCriteria.DUMMY,
                    Component.literal("keyishuaxin"), ObjectiveCriteria.RenderType.INTEGER, false, null);
            scoreboard.getOrCreatePlayerScore(entity, objective).set(1);
        }
    }

    private static void spawn(ServerLevel level, String id, int x, int y, int z) {
        Mob mob = (Mob) NpcSetup.entityType(id).get().create(level);
        mob.moveTo(x, y, z, 0, 0);
        mob.setYBodyRot(0);
        mob.setYHeadRot(0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(mob);
    }
}
