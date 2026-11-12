package net.yvanchuxiuzhen.system.equipment.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 覆情法宝的动物发情与幻术范围效果；敌友筛选只用于来源潜行分支。 */
public final class Fabao012Item extends ContentSword {
    public Fabao012Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer
                && !StatusEffects.has(serverPlayer, "effect_002")) {
            BlockPos block = serverLevel.clip(new ClipContext(serverPlayer.getEyePosition(1),
                    serverPlayer.getEyePosition(1).add(serverPlayer.getViewVector(1).scale(4)), ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, serverPlayer)).getBlockPos();
            Vec3 center = new Vec3(block.getX(), block.getY(), block.getZ());
            if (serverPlayer.isShiftKeyDown()) {
                serverLevel.sendParticles(Particles.type("fenyan"), center.x, center.y, center.z, 20, 4, 0, 4, 0);
                affectIllusion(serverLevel, serverPlayer, center);
                serverPlayer.getCooldowns().addCooldown(result.getObject().getItem(), 600);
            } else {
                serverLevel.sendParticles(ParticleTypes.HEART, center.x, center.y, center.z, 20, 4, 0, 4, 0);
                serverLevel.sendParticles(Particles.type("fenyan"), center.x, center.y, center.z, 20, 4, 0, 4, 0.1);
                affectAnimals(serverLevel, center);
                serverPlayer.getCooldowns().addCooldown(result.getObject().getItem(), 200);
            }
        }
        return result;
    }

    private static List<Entity> targets(ServerLevel level, Vec3 center) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(4), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static void affectAnimals(ServerLevel level, Vec3 center) {
        for (Entity target : targets(level, center)) {
            if (target instanceof Animal animal) {
                animal.setInLoveTime(200);
            }
        }
    }

    private static void affectIllusion(ServerLevel level, ServerPlayer player, Vec3 center) {
        for (Entity target : targets(level, center)) {
            if (!Relations.isFriendly(player, target) && target instanceof LivingEntity living) {
                StatusEffects.add(living, "effect_032", 1200, 0);
            }
        }
    }
}
