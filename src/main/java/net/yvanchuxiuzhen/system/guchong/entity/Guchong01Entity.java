package net.yvanchuxiuzhen.system.guchong.entity;

import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;

/** 鼓上蚤只跟随与漫步；每十九刻挑衅附近生物按距离次序攻击前一只生物。 */
public final class Guchong01Entity extends GuchongEntity {
    private double tick1;
    private double tick2;

    public Guchong01Entity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new GuchongFollowOwnerGoal(this, 1, 10, 2, false));
        goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
    }

    @Override
    protected void initializeSourceData() {
        GuchongStats.initialize(this, 7, "平庸", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱",
                "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱");
    }

    @Override
    protected void sourceTick() {
        tick1 += 1;
        if (tick1 >= 19) {
            provokeNearby();
            tick1 = 0;
        }
        tick2 += 1;
        if (tick2 >= 77) {
            ResourceLocation sound = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong01");
            if (level().isClientSide()) {
                level().playLocalSound(getX(), getY(), getZ(), BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.NEUTRAL, 1, 1, false);
            } else {
                level().playSound(null, blockPosition(), BuiltInRegistries.SOUND_EVENT.get(sound), SoundSource.NEUTRAL, 1, 1);
            }
            tick2 = 0;
        }
        GuchongBehavior.tick(this);
    }

    private void provokeNearby() {
        String previous = "";
        Vec3 center = position();
        for (Entity candidate : level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(4), value -> true)
                .stream().sorted(Comparator.comparingDouble(value -> value.distanceToSqr(center))).toList()) {
            if (!(candidate instanceof Mob mob) || candidate == this || !candidate.isAlive()) continue;
            if (!previous.isEmpty() && !previous.equals(mob.getStringUUID()) && mob.getData(GuchongData.PROVOKE_TARGET).isEmpty()) {
                mob.setData(GuchongData.PROVOKE_TARGET, previous);
                if (level() instanceof ServerLevel level && GuchongBehavior.findEntity(level, previous) instanceof LivingEntity target) {
                    mob.setTarget(target);
                }
            }
            previous = mob.getStringUUID();
        }
    }
}
