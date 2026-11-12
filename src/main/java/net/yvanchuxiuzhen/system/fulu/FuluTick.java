package net.yvanchuxiuzhen.system.fulu;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 持续效果符的逐刻推进（来源 {@code FuluTick}、{@code Fulutick2}）：玩家的状态在体质标签，非玩家实体的状态在实体持久数据，
 * 两处使用同一组键与同一段过程。每个效果按 {@code 计数 += 1} 推进，达到上限时清除生效标记与品阶并减少 {@code fulunumber}。
 *
 * <p>来源在服务端与客户端各跑一遍并用客户端粒子表现；这里只在服务端推进状态，粒子经 {@code sendParticles}
 * 以零数量形式发给客户端，由客户端按给定速度生成，与客户端 {@code addParticle} 等效。
 * 风压对玩家目标只改服务端运动量，不补发运动包，与来源一致。
 */
final class FuluTick {
    private static final String FIRE = "火";

    private FuluTick() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(FuluTick::playerTick);
        NeoForge.EVENT_BUS.addListener(FuluTick::entityTick);
    }

    private static void playerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && Cultivation.tizhiDouble(player, "fulunumber") >= 1) {
            Cultivation.editTizhi(player, state -> run(player, state));
        }
    }

    private static void entityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !(entity instanceof Player) && entity.level() instanceof ServerLevel
                && entity.getPersistentData().getDouble("fulunumber") >= 1) {
            run(entity, entity.getPersistentData());
        }
    }

    private static void run(LivingEntity entity, CompoundTag state) {
        ServerLevel level = (ServerLevel) entity.level();
        if (state.getBoolean("fuluxiaoguo02")) {
            flameJet(level, entity, state);
        }
        if (state.getBoolean("fuluxiaoguo03")) {
            windPressure(level, entity, state);
        }
        if (state.getBoolean("fuluxiaoguo13")) {
            volley(level, entity, state, "13", Kind.FIREBALL, 5, 100);
        }
        if (state.getBoolean("fuluxiaoguo17")) {
            volley(level, entity, state, "17", Kind.METEOR, 5, 600);
        }
        if (state.getBoolean("fuluxiaoguo19")) {
            volley(level, entity, state, "19", Kind.SWORD, 5, 600);
        }
        if (state.getBoolean("fuluxiaoguo21")) {
            ring(level, entity, state, "21", Particles.type("fulutexiao_21"));
        }
        if (state.getBoolean("fuluxiaoguo24")) {
            flameWhirl(level, entity, state);
        }
        if (state.getBoolean("fuluxiaoguo31")) {
            fastTime(level, state);
        }
        if (state.getBoolean("fuluxiaoguo39")) {
            ring(level, entity, state, "39", ParticleTypes.SPLASH);
        }
    }

    /** 02：每刻五个火焰粒子；沿视线 2、4、6、8 格取四个点，各点 1 格范围内的非友方受火属性固定伤害 {@code 品阶×4}（至少 1）。 */
    private static void flameJet(ServerLevel level, LivingEntity entity, CompoundTag state) {
        Vec3 look = entity.getLookAngle();
        for (int index = 0; index < 5; index++) {
            spray(level, ParticleTypes.FLAME, entity.getX(), entity.getY() + 1, entity.getZ(),
                    look.x * Mth.nextDouble(entity.getRandom(), 0.5, 1.5), look.y * Mth.nextDouble(entity.getRandom(), 0.5, 1.5),
                    look.z * Mth.nextDouble(entity.getRandom(), 0.5, 1.5));
        }
        Vec3 eye = entity.getEyePosition(1f);
        Vec3 view = entity.getViewVector(1f);
        double damage = Math.max(1, state.getDouble("fulu02level") * 4);
        double distance = 2;
        for (int index = 0; index < 4; index++) {
            BlockPos spot = level.clip(new ClipContext(eye, eye.add(view.scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
            level.sendParticles(ParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 5, 1, 1, 1, 0.1);
            for (Entity target : nearby(level, new Vec3(spot.getX(), spot.getY(), spot.getZ()), 1)) {
                if (!Relations.isFriendly(entity, target)) {
                    fireStrike(level, entity, target, damage);
                }
            }
            distance += 2;
        }
        advance(state, "02", 80 + 20 * state.getDouble("fulu02level"));
    }

    /** 03：脚下云雾，每 20 刻向外爆发；4.5 格内非友方被向外推开，推力与品阶成正比，持续 {@code 200 + 40×品阶} 刻。 */
    private static void windPressure(ServerLevel level, LivingEntity entity, CompoundTag state) {
        for (int index = 0; index < 2; index++) {
            spray(level, ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2, entity.getZ(),
                    Mth.nextDouble(entity.getRandom(), -1, 1), 0, Mth.nextDouble(entity.getRandom(), -1, 1));
        }
        double burst = state.getDouble("fulu0301tick") + 1;
        if (burst >= 20) {
            for (int index = 0; index < 60; index++) {
                spray(level, ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.5, entity.getZ(),
                        Mth.nextDouble(entity.getRandom(), -0.5, 0.5), 0, Mth.nextDouble(entity.getRandom(), -0.5, 0.5));
            }
            burst = 0;
        }
        state.putDouble("fulu0301tick", burst);
        double push = 0.1 * state.getDouble("fulu03level");
        for (Entity target : nearby(level, entity.position(), 4.5)) {
            if (!Relations.isFriendly(entity, target)) {
                target.setDeltaMovement(new Vec3((target.getX() - entity.getX()) * push, 0.2, (target.getZ() - entity.getZ()) * push));
            }
        }
        advance(state, "03", 200 + 40 * state.getDouble("fulu03level"));
    }

    /**
     * 13、17、19：每 5 刻发出一枚弹体。13 的火球与 19 的剑气出现在身边水平 ±4、高 1 至 3 格内并沿视线射出，
     * 13 同时把 {@code fululevel46} 记为当前品阶；17 的陨石出现在水平 ±8、头顶 32 格处向下落。持续 100、600、600 刻。
     */
    private static void volley(ServerLevel level, LivingEntity entity, CompoundTag state, String suffix, Kind kind, int interval, double limit) {
        String shot = "fulu" + suffix + "01tick";
        double count = state.getDouble(shot) + 1;
        if (count >= interval) {
            Vec3 at;
            Vec3 direction;
            if (kind == Kind.METEOR) {
                at = new Vec3(entity.getX() + Mth.nextDouble(entity.getRandom(), -8, 8), entity.getY() + 32, entity.getZ() + Mth.nextDouble(entity.getRandom(), -8, 8));
                direction = new Vec3(0, -1, 0);
            } else {
                if (kind == Kind.FIREBALL) {
                    state.putDouble("fululevel46", state.getDouble("fulu13level"));
                }
                at = new Vec3(entity.getX() + Mth.nextDouble(entity.getRandom(), -4, 4), entity.getY() + Mth.nextDouble(entity.getRandom(), 1, 3),
                        entity.getZ() + Mth.nextDouble(entity.getRandom(), -4, 4));
                direction = entity.getLookAngle();
            }
            TalismanProjectile.launch(level, FuluSetup.projectile(kind).get(), entity, at, direction);
            count = 0;
        }
        state.putDouble(shot, count);
        advance(state, suffix, limit);
    }

    /** 24：周身 3 格内每刻五个材料 233 粒子；3.5 格内非友方受火属性固定伤害 {@code 品阶×5}（至少 1），持续 1200 刻。 */
    private static void flameWhirl(ServerLevel level, LivingEntity entity, CompoundTag state) {
        for (int index = 0; index < 5; index++) {
            spray(level, Particles.type("cailiao_233particle"), entity.getX() + Mth.nextDouble(entity.getRandom(), -3, 3), entity.getY() + 0.5,
                    entity.getZ() + Mth.nextDouble(entity.getRandom(), -3, 3), 0, 1, 0);
        }
        double damage = Math.max(1, state.getDouble("fulu24level") * 5);
        for (Entity target : nearby(level, entity.position(), 3.5)) {
            if (!Relations.isFriendly(entity, target)) {
                fireStrike(level, entity, target, damage);
            }
        }
        advance(state, "24", 1200);
    }

    /** 31：每刻把世界时间向前拨 {@code 4×品阶²}，持续 1200 刻；来源把时间截为 int 再写入。 */
    private static void fastTime(ServerLevel level, CompoundTag state) {
        level.setDayTime((int) (level.getDayTime() + 4 * Math.pow(state.getDouble("fulu31level"), 2)));
        advance(state, "31", 1200);
    }

    /** 21、39：每刻在脚下起每 18° 一枚、共 20 枚的 1 格半径粒子环，环高随计数每刻升 0.15 格，满 20 刻结束。 */
    private static void ring(ServerLevel level, LivingEntity entity, CompoundTag state, String suffix, ParticleOptions particle) {
        double height = entity.getY() + state.getDouble("fulu" + suffix + "tick") * 0.15;
        double angle = 0;
        for (int index = 0; index < 20; index++) {
            level.sendParticles(particle, entity.getX() + Math.cos(Math.toRadians(angle)), height, entity.getZ() + Math.sin(Math.toRadians(angle)), 1, 0, 0, 0, 0);
            angle += 18;
        }
        advance(state, suffix, 20);
    }

    /** 该编号的计数加一；达到上限时清零计数与品阶、关闭生效标记并减少生效数。 */
    private static void advance(CompoundTag state, String suffix, double limit) {
        String tick = "fulu" + suffix + "tick";
        double count = state.getDouble(tick) + 1;
        state.putDouble(tick, count);
        if (count >= limit) {
            state.putDouble(tick, 0);
            state.putBoolean("fuluxiaoguo" + suffix, false);
            state.putDouble("fulu" + suffix + "level", 0);
            state.putDouble("fulunumber", state.getDouble("fulunumber") - 1);
        }
    }

    /** 来源经 {@code /shanghai 火 <施放者> true <伤害> 1} 命令结算，随后清除目标的受伤无敌时间以便逐刻命中。 */
    private static void fireStrike(ServerLevel level, Entity caster, Entity target, double damage) {
        if (target instanceof LivingEntity living) {
            SpellDamage.command(level, living, "shanghai " + FIRE + " " + caster.getStringUUID() + " true " + damage + " 1");
            living.invulnerableTime = 0;
        }
    }

    /** 以点为中心、边长 {@code 2×halfSize} 的方盒内所有实体，按到中心的距离由近到远。 */
    static List<Entity> nearby(ServerLevel level, Vec3 center, double halfSize) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(halfSize)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    static void spray(ServerLevel level, ParticleOptions type, double x, double y, double z, double vx, double vy, double vz) {
        level.sendParticles(type, x, y, z, 0, vx, vy, vz, 1);
    }
}
