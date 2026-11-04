package net.yvanchuxiuzhen.system.skill.projectile;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 技能弹体与实体技能共用的小工具：敌友、范围查找、伤害、粒子与方块破碎效果、弹体发射。 */
public final class ProjectileSupport {
    /** 方块破碎的世界事件编号（来源 {@code levelEvent(2001, …)}）。 */
    private static final int BLOCK_BREAK_EVENT = 2001;

    private ProjectileSupport() {
    }

    /** 发射者是否把对方视为友方（来源 {@code Friendpanding*} 的判定方总是发射者）。 */
    public static boolean friendly(Entity owner, Entity other) {
        return Relations.isFriendly(owner, other);
    }

    /** 以 {@code center} 为中心、半边长 {@code inflate} 的盒内全部实体，按到中心的距离从近到远（来源 {@code getEntitiesOfClass} 加排序）。 */
    public static List<Entity> around(ServerLevel level, Vec3 center, double inflate) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(inflate), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    /** 灵力伤害：目标不是生物时没有效果；类别用来源命令的中文标签（金、水、木、火、土、风、冰、光、雷、阴）。 */
    public static void deal(ServerLevel level, Entity target, Entity owner, String label, double mana, double multiplier) {
        if (target instanceof LivingEntity living) {
            SpellDamage.deal(level, living, DamageCategory.byLabel(label), owner, false, mana, multiplier);
        }
    }

    /** 发射者当前所选技能的攻击量与伤害倍率；发射者不是玩家时攻击量取其 {@code LEVELnumber}、倍率为 1（命中时现读，不是发射时的快照）。 */
    public static double[] liveValues(Entity owner) {
        if (owner instanceof Player player) {
            SkillBook book = Skills.book(Skills.selected(player)).orElse(null);
            return book == null ? new double[] {0, 0} : new double[] {book.manaAttack(), book.damageMultiplier()};
        }
        return new double[] {owner.getPersistentData().getDouble("LEVELnumber"), 1};
    }

    public static void particles(ServerLevel level, ParticleOptions particle, Vec3 at, int count, double spreadX, double spreadY, double spreadZ, double speed) {
        level.sendParticles(particle, at.x, at.y, at.z, count, spreadX, spreadY, spreadZ, speed);
    }

    /** 方块破碎的声音与粒子（来源 {@code levelEvent(2001, 位置, 方块状态编号)}）。 */
    public static void breakEffect(ServerLevel level, Vec3 at, BlockState state) {
        level.levelEvent(BLOCK_BREAK_EVENT, BlockPos.containing(at), Block.getId(state));
    }

    /** 从眼睛沿视线看 {@code distance} 格内碰到的方块位置，没有碰到时为视线终点所在的方块（来源 {@code level.clip(ClipContext(OUTLINE, NONE))}）。 */
    public static BlockPos lookBlock(Entity viewer, double distance) {
        Vec3 eye = viewer.getEyePosition(1f);
        return viewer.level().clip(new ClipContext(eye, eye.add(viewer.getViewVector(1f).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer)).getBlockPos();
    }

    /** 点燃：在已有的剩余燃烧秒数（整数除法）上再加 {@code seconds} 秒（来源 {@code setSecondsOnFire(round(剩余刻 / 20) + n)}）。 */
    public static void ignite(Entity target, int seconds) {
        target.igniteForSeconds(target.getRemainingFireTicks() / 20 + seconds);
    }

    /**
     * 在 {@code shooter} 处发射一枚弹体：从 {@code origin} 出发，朝 {@code direction}、速度 1、无散布；
     * 基础伤害、击退与穿透由参数给出。
     */
    public static SkillProjectile shoot(ServerLevel level, Entity shooter, String id, Vec3 origin, Vec3 direction, double baseDamage, int knockback, int pierce) {
        EntityType<SkillProjectile> type = SkillSetup.<SkillProjectile>entityType(id).get();
        SkillProjectile projectile = type.create(level).launchedWith(knockback, pierce);
        projectile.setOwner(shooter);
        projectile.setBaseDamage(baseDamage);
        projectile.setPos(origin);
        projectile.shoot(direction.x, direction.y, direction.z, 1, 0);
        level.addFreshEntity(projectile);
        return projectile;
    }

    /** 从施放者眼睛下 0.1 格处朝视线方向发射（来源各技能施放过程中的弹体生成）。 */
    public static SkillProjectile shootFromEyes(ServerLevel level, Entity shooter, String id, double baseDamage, int knockback, int pierce) {
        Vec3 origin = new Vec3(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        return shoot(level, shooter, id, origin, shooter.getLookAngle(), baseDamage, knockback, pierce);
    }
}
