package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 只在蛊罐重读后布置受控放虫目标。 */
final class RenjianControlledJarScene {
    private final List<String> report;

    RenjianControlledJarScene(List<String> report) { this.report = report; }

    boolean provideTarget(ServerPlayer player, RenjianWildJarWitness jar) {
        BlockPos pos = jar.position();
        BlockPos feet = groundNear(player, pos, 4, 5, 6);
        if (feet == null) return false;
        if (!jar.releaseAreaEmpty()) throw new IllegalStateException("受控目标入场前，罐八格内已有合法动物");
        ServerLevel level = player.serverLevel();
        Sheep sheep = EntityType.SHEEP.spawn(level, feet, MobSpawnType.COMMAND);
        if (sheep == null) throw new IllegalStateException("未能布置受控目标羊");
        sheep.setNoAi(true);
        String team = "smoke_jar_target";
        if (team.equals(jar.team())) team += "_other";
        Relations.setTeam(sheep, team);
        jar.expectControlledTarget(sheep);
        report.add("INFO CONTROLLED_JAR_TARGET uuid=" + sheep.getUUID() + " pos=" + sheep.blockPosition()
                + " distance=" + Math.sqrt(sheep.distanceToSqr(Vec3.atCenterOf(pos)))
                + " targetTeam=" + Relations.entityTeam(sheep) + " jarTeam=" + jar.team() + " controlledTarget=true");
        return true;
    }

    private static BlockPos groundNear(ServerPlayer player, BlockPos center, int... radii) {
        ServerLevel level = player.serverLevel();
        for (int radius : radii) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos base = center.relative(direction, radius);
                for (int dy : new int[] {0, 1, -1, 2, -2}) {
                    BlockPos feet = base.offset(0, dy, 0);
                    if (!level.hasChunkAt(feet) || !level.isPositionEntityTicking(feet)
                            || !level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above())) continue;
                    var floor = level.getBlockState(feet.below());
                    if ((floor.is(Blocks.GRASS_BLOCK) || floor.is(Blocks.DIRT))
                            && level.getEntities(null, new AABB(feet).expandTowards(0, 1, 0)).isEmpty()) return feet;
                }
            }
        }
        return null;
    }
}
