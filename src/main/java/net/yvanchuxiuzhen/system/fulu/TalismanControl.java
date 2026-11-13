package net.yvanchuxiuzhen.system.fulu;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.fulu.api.TalismanTamingEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 神傀符的非妖兽命中与按显示名控制；服务端写速度，实体移动和跟踪沿用原生生命周期。 */
public final class TalismanControl implements EffectBehavior {
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulutexiao_18");
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    public static void hit(ServerLevel level, Entity projectile, Entity target, Entity shooter, double grade) {
        target.getPersistentData().putString("beikongzhi", shooter.getDisplayName().getString());
        if (target.getType().is(BEASTS)) {
            NeoForge.EVENT_BUS.post(new TalismanTamingEvent(level, target, shooter, grade));
        } else if (projectile.getPersistentData().getBoolean("jiuxiaoguo")) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(EFFECT).orElseThrow(),
                        (int) (grade * grade * 300), 0));
            }
        } else {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, (int) (grade * 2 - 1), true, true));
            }
            if (target instanceof ServerPlayer player) {
                Cultivation.set(player, CultivationValue.SOUL,
                        Math.max(0, Cultivation.get(player, CultivationValue.SOUL) - grade * grade * 40));
            }
        }
    }

    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        boolean active = true;
        Vec3 center = entity.position();
        for (Entity controller : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(16))
                .stream().sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
            if (!controller.getDisplayName().getString().equals(entity.getPersistentData().getString("beikongzhi"))) {
                continue;
            }
            if (controller.isShiftKeyDown()) {
                entity.setDeltaMovement(entity.position().subtract(controller.position()).scale(1.5));
                entity.getPersistentData().putString("beikongzhi", "");
                active = false;
            } else {
                Vec3 eye = controller.getEyePosition(1);
                BlockPos pos = controller.level().clip(new ClipContext(eye, eye.add(controller.getViewVector(1).scale(8)),
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, controller)).getBlockPos();
                entity.setDeltaMovement(Vec3.atLowerCornerOf(pos).subtract(entity.position()).scale(0.3));
            }
        }
        return active;
    }

    @Override
    public void ended(LivingEntity entity, int amplifier) {
        entity.getPersistentData().putString("beikongzhi", "");
    }
}
