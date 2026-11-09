package net.yvanchuxiuzhen.system.fulu;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 一次性释放的符箓（来源 {@code Itemfulu04...} 与 {@code Itemfulu25...} 共用过程中不依赖新实体的分支）：
 * 04 弹飞、06 落雷、07 烈焰、08 冰封、11 护体、25 加速、46 火球。统一流程为先付精力、播释放声、取品阶，再按编号执行，最后消耗一张。
 */
final class FuluInstant {
    private static final Set<Integer> NUMBERS = Set.of(4, 6, 7, 8, 11, 25, 46);

    private FuluInstant() {
    }

    static boolean handles(int number) {
        return NUMBERS.contains(number);
    }

    static void use(ServerPlayer player, ItemStack stack, int number) {
        if (!FuluUse.pay(player, stack)) {
            return;
        }
        FuluUse.cast(player);
        double grade = FuluUse.grade(stack);
        ServerLevel level = player.serverLevel();
        switch (number) {
            case 4 -> burst(level, player, grade);
            case 6 -> lightning(level, player, grade);
            case 7 -> scorch(level, player, grade);
            case 8 -> freeze(level, player, grade);
            case 11 -> shield(level, player, grade);
            case 25 -> haste(level, player, grade);
            case 46 -> fireball(level, player, grade);
            default -> throw new IllegalArgumentException("not an instant talisman: " + number);
        }
        stack.shrink(1);
    }

    /** 04：20 枚符箓 21 粒子；速度提升 100 刻（等级 {@code 3+2×品阶}）；3 格内非友方被向外上抛。 */
    private static void burst(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(Particles.type("fulutexiao_21"), player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, (int) (3 + grade * 2)));
        for (Entity target : FuluTick.nearby(level, player.position(), 3)) {
            if (!Relations.isFriendly(player, target)) {
                target.setDeltaMovement(new Vec3((target.getX() - player.getX()) * 0.4, 0.5, (target.getZ() - player.getZ()) * 0.4));
            }
        }
    }

    /** 06：视线 7 格处（下移至空气）落下红色独立落雷，伤害基数 {@code 30+20×品阶}。 */
    private static void lightning(ServerLevel level, ServerPlayer player, double grade) {
        BlockPos spot = lowered(level, player, 7);
        Lightning.command(level, new Vec3(spot.getX(), spot.getY(), spot.getZ()),
                "lightninglanding " + (30 + grade * 20) + " " + player.getStringUUID() + " 1 " + Relations.team(player) + " 4 红色 独立");
    }

    /** 07：向前喷 20 枚火焰；视线 6 格处 3 格内非友方记录点燃来源，并受 {@code effect_023}（{@code 400+200×品阶} 刻，等级 {@code 2×(品阶−1)}）。 */
    private static void scorch(ServerLevel level, ServerPlayer player, double grade) {
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 20; index++) {
            FuluTick.spray(level, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        BlockPos spot = looked(level, player, 6);
        for (Entity target : FuluTick.nearby(level, Vec3.atLowerCornerOf(spot), 3)) {
            if (!Relations.isFriendly(player, target)) {
                markFire(player, target);
                level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5, 0.8, 0.5, 0.1);
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(effect("effect_023"), (int) (400 + grade * 200), (int) ((grade - 1) * 2)));
                }
            }
        }
    }

    /** 08：向前喷云雾与火焰；视线 5 格处 3 格内非友方受 {@code effect_023} 与缓慢（{@code 200+40×品阶} 刻，等级 1），并被向外击飞。 */
    private static void freeze(ServerLevel level, ServerPlayer player, double grade) {
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 30; index++) {
            FuluTick.spray(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        for (int index = 0; index < 15; index++) {
            FuluTick.spray(level, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        BlockPos spot = looked(level, player, 5);
        for (Entity target : FuluTick.nearby(level, Vec3.atLowerCornerOf(spot), 3)) {
            if (!Relations.isFriendly(player, target)) {
                for (int index = 0; index < 15; index++) {
                    FuluTick.spray(level, ParticleTypes.CLOUD, target.getX(), target.getY() + 1, target.getZ(),
                            look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
                }
                markFire(player, target);
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(effect("effect_023"), (int) (200 + grade * 40), 1));
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (200 + grade * 40), 1));
                }
                target.setDeltaMovement(new Vec3(target.getX() - player.getX(), 0.5, target.getZ() - player.getZ()));
            }
        }
    }

    /** 11：吸收 4800 刻（等级 {@code 6×品阶}）与抗性提升 4800 刻（等级 {@code 品阶−1}），均为环境效果且不显示粒子。 */
    private static void shield(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(Particles.type("fulutexiao_11"), player.getX(), player.getY() + 1, player.getZ(), 10, 1, 1, 1, 1);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, (int) (6 * grade), true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, (int) (grade - 1), true, false));
    }

    /** 25：速度提升 3600 刻、力量提升 1200 刻，等级均为 {@code 2×品阶−1}。 */
    private static void haste(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(ParticleTypes.SPIT, player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, (int) (grade * 2 - 1)));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, (int) (grade * 2 - 1)));
    }

    /**
     * 46：从眼前射出一枚火球。来源把品阶写进玩家的实体持久数据，而火球读的是玩家体质里的 {@code fululevel46}（仅 13 号符写入），
     * 所以玩家直接使用 46 号符的火球按品阶 1 结算；保持来源（登记为 Q66）。
     */
    private static void fireball(ServerLevel level, ServerPlayer player, double grade) {
        TalismanProjectile.launch(level, FuluSetup.projectile(Kind.FIREBALL).get(), player, new Vec3(player.getX(), player.getEyeY() - 0.1, player.getZ()), player.getLookAngle());
        player.getPersistentData().putDouble("fululevel46", grade);
    }

    private static BlockPos looked(ServerLevel level, Entity viewer, double distance) {
        Vec3 eye = viewer.getEyePosition(1f);
        return level.clip(new ClipContext(eye, eye.add(viewer.getViewVector(1f).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer)).getBlockPos();
    }

    /** 视线落点向下取至多 3 格内最先为空气的位置（先试 3、再 2、再 1）。 */
    private static BlockPos lowered(ServerLevel level, Entity viewer, double distance) {
        BlockPos spot = looked(level, viewer, distance);
        for (int drop = 3; drop >= 1; drop--) {
            if (level.isEmptyBlock(spot.below(drop))) {
                return spot.below(drop);
            }
        }
        return spot;
    }

    private static void markFire(Entity caster, Entity target) {
        String team = Relations.team(caster);
        if (target instanceof Player victim && victim instanceof ServerPlayer serverVictim) {
            Cultivation.setTizhiString(serverVictim, "fireteam", team);
            Cultivation.setTizhiString(serverVictim, "fireAttackUUID", caster.getStringUUID());
        } else {
            target.getPersistentData().putString("fireteam", team);
            target.getPersistentData().putString("fireAttackUUID", caster.getStringUUID());
        }
    }

    private static Holder<MobEffect> effect(String id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).orElseThrow();
    }
}
