package net.yvanchuxiuzhen.system.equipment.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 覆风法宝的范围伤害、击退与冷却；敌友和法术伤害分别交给关系、战斗系统结算。 */
public final class Fabao011Item extends ContentSword {
    public Fabao011Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer
                && !StatusEffects.has(serverPlayer, "effect_002")) {
            if (serverPlayer.isShiftKeyDown()) {
                castRingParticles(serverLevel, serverPlayer);
                cast(serverLevel, serverPlayer, serverPlayer.position(), 5, 200, result.getObject());
            } else {
                castDirectionalParticles(serverLevel, serverPlayer);
                BlockPos block = serverLevel.clip(new ClipContext(serverPlayer.getEyePosition(1),
                        serverPlayer.getEyePosition(1).add(serverPlayer.getViewVector(1).scale(5)), ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE, serverPlayer)).getBlockPos();
                cast(serverLevel, serverPlayer, new Vec3(block.getX(), block.getY(), block.getZ()), 4, 100, result.getObject());
            }
        }
        return result;
    }

    private static void castRingParticles(ServerLevel level, ServerPlayer player) {
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 20, 0, 0, 0, 1);
        double y = player.getY() + player.getBbHeight() * 0.5;
        for (int index = 0; index < 40; index++) {
            double angle = index * (360.0 / 40);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            level.sendParticles(ParticleTypes.CLOUD, player.getX() + 2 * cos, y, player.getZ() + 2 * sin,
                    0, 0.5 * cos, 0, 0.5 * sin, 0.5);
        }
    }

    private static void castDirectionalParticles(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        RandomSource random = player.getRandom();
        for (int index = 0; index < 20; index++) {
            level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(), 0,
                    look.x + Mth.nextDouble(random, -0.5, 0.5),
                    look.y + Mth.nextDouble(random, -0.5, 0.5),
                    look.z + Mth.nextDouble(random, -0.5, 0.5), 1);
        }
    }

    private static void cast(ServerLevel level, ServerPlayer player, Vec3 center, double radius, int cooldown, ItemStack stack) {
        List<Entity> targets = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        for (Entity target : targets) {
            if (Relations.isFriendly(player, target)) {
                continue;
            }
            if (target instanceof LivingEntity living) {
                SpellDamage.deal(level, living, DamageCategory.WIND, player, false, 5, 1.2);
            }
            if (!(target instanceof LivingEntity living) || !StatusEffects.has(living, "skilleffect_08")) {
                target.setDeltaMovement((target.getX() - player.getX()) * 0.5, 0.5, (target.getZ() - player.getZ()) * 0.5);
            }
        }
        player.getCooldowns().addCooldown(stack.getItem(), cooldown);
    }
}
