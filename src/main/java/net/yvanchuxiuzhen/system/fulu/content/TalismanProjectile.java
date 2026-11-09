package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.fulu.FuluUse;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/**
 * 持续效果符发出的三种弹体（来源 {@code Fulutexiao46Entity}、{@code Fulutexiao17Entity}、{@code Fulutexiao19Entity}）：
 * 13 号符的火球（14 刻后或命中时爆炸，范围火伤害 {@code 15×品阶}）、17 号符的陨石（命中爆炸 {@code 20×品阶}后消失）、
 * 19 号符的剑气（14 刻内每刻对 1 格范围的非友方造成 {@code 20×品阶} 物理伤害，穿透 5）。
 * 品阶在第一刻从发射者的符箓状态读取。1.21.1 版本的箭没有独立击退字段，来源 13、17 号弹体的击退 1 不再携带。
 */
public final class TalismanProjectile extends AbstractArrow implements ItemSupplier {
    /** 三种弹体及其内容表 ID。 */
    public enum Kind {
        FIREBALL("projectile_fulutexiao_46"), METEOR("projectile_fulutexiao_17"), SWORD("projectile_fulutexiao_19");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private static final int FLIGHT_TICKS = 14;
    private final Kind kind;
    private double grade;
    private boolean recorded;
    private int ticks;

    public TalismanProjectile(EntityType<? extends TalismanProjectile> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
    }

    /** 以来源的伤害 0.1、静音生成并按给定方向以速度 1、无散布发射。 */
    public static void launch(ServerLevel level, EntityType<? extends TalismanProjectile> type, Entity owner, Vec3 at, Vec3 direction) {
        TalismanProjectile projectile = type.create(level);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        projectile.setPos(at);
        projectile.shoot(direction.x, direction.y, direction.z, 1, 0);
        level.addFreshEntity(projectile);
    }

    @Override
    public ItemStack getItem() {
        return switch (kind) {
            case FIREBALL -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_fire_01")));
            case METEOR -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulutexiao_17")));
            case SWORD -> ItemStack.EMPTY;
        };
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public byte getPierceLevel() {
        return (byte) (kind == Kind.SWORD ? 5 : 0);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        impact();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        impact();
    }

    private void impact() {
        if (kind != Kind.SWORD) {
            explode();
            if (kind == Kind.METEOR && !level().isClientSide) {
                discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level && getOwner() != null) {
            switch (kind) {
                case FIREBALL -> fireballTick(level);
                case METEOR -> meteorTick(level);
                case SWORD -> swordTick(level);
            }
        }
        if (inGround) {
            discard();
        }
    }

    private void fireballTick(ServerLevel level) {
        level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.3, getZ(), 2, 0.2, 0.2, 0.2, 0);
        if (!isNoGravity()) {
            double stored = FuluUse.stateValue(getOwner(), "fululevel46");
            grade = stored > 0 ? stored : 1;
            setNoGravity(true);
        }
        if (++ticks >= FLIGHT_TICKS) {
            explode();
            ticks = 0;
            discard();
        }
    }

    private void meteorTick(ServerLevel level) {
        level.sendParticles(Particles.type("disappearpurple"), getX(), getY() + 0.3, getZ(), 1, 0, 0, 0, 0);
        if (!recorded) {
            grade = FuluUse.stateValue(getOwner(), "fulu17level");
            recorded = true;
        }
    }

    private void swordTick(ServerLevel level) {
        ParticleOptions particle = Particles.type("cailiao_231particle");
        level.sendParticles(particle, getX(), getY() + 0.3, getZ(), 2, 0.2, 0.2, 0.2, 0);
        if (!isNoGravity()) {
            grade = FuluUse.stateValue(getOwner(), "fulu19level");
            setNoGravity(true);
        }
        if (++ticks >= FLIGHT_TICKS) {
            level.sendParticles(particle, getX(), getY() + 0.3, getZ(), 10, 0.2, 0.2, 0.2, 1);
            ticks = 0;
            discard();
        }
        Entity owner = getOwner();
        Vec3 center = position();
        for (Entity target : nearby(level, center, 1)) {
            if (target != this && !Relations.isFriendly(owner, target)) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
                target.hurt(sourceOf(level, owner), (float) (20 * grade));
            }
        }
    }

    /** 来源 {@code EntityDamageSource("generic.player", owner)}：玩家按玩家攻击，其他按生物攻击。 */
    private static DamageSource sourceOf(ServerLevel level, Entity owner) {
        if (owner instanceof Player player) {
            return level.damageSources().playerAttack(player);
        }
        return owner instanceof LivingEntity living ? level.damageSources().mobAttack(living) : level.damageSources().generic();
    }

    /** 火球与陨石的爆炸：音效、粒子，并对 1.5 格内的非友方按 {@code /shanghai 火} 造成固定伤害。 */
    private void explode() {
        Entity owner = getOwner();
        if (owner == null || !(level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, BlockPos.containing(getX(), getY(), getZ()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1, 1);
        boolean fire = kind == Kind.FIREBALL;
        level.sendParticles(Particles.type(fire ? "smallexplosionred" : "smallexplosionpurple"), getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        if (fire) {
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 20, 0, 0, 0, 0.5);
            level.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 10, 0, 0, 0, 1);
        } else {
            level.sendParticles(Particles.type("starpurple"), getX(), getY(), getZ(), 20, 0, 0, 0, 1);
        }
        double damage = (fire ? 15 : 20) * grade;
        for (Entity target : nearby(level, position(), 1.5)) {
            if (!Relations.isFriendly(owner, target) && target instanceof LivingEntity living) {
                SpellDamage.command(level, living, "shanghai 火 " + owner.getStringUUID() + " true " + damage + " 1");
            }
        }
    }

    private static List<Entity> nearby(ServerLevel level, Vec3 center, double halfSize) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(halfSize)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("fululevel", grade);
        tag.putBoolean("jilu", recorded);
        tag.putInt("tick", ticks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        grade = tag.getDouble("fululevel");
        recorded = tag.getBoolean("jilu");
        ticks = tag.getInt("tick");
    }
}
