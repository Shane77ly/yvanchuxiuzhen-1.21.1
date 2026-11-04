package net.yvanchuxiuzhen.system.skill.projectile;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/**
 * 技能弹体（来源各 {@code SkillXxx} 弹体物品对应的箭类实体）：基础伤害 0.1 的无拾取箭，飞行、命中实体与命中方块的行为由
 * {@link ProjectileSkill} 承担。命中或停在方块里时消失。技能数值在弹体第一个更新刻从发射者取一次（{@link #begin}，来源用
 * “是否无重力”判断第一刻），发射者不是玩家时用发射者的 {@code LEVELnumber} 作攻击量。弹体击退与穿透等级由发射方式决定，
 * 1.21.1 的箭没有这两项的设置方法，这里覆盖读取它们的方法。
 */
public final class SkillProjectile extends AbstractArrow implements ItemSupplier {
    private final ProjectileSkill skill;
    private final ItemStack icon;
    private int knockback;
    private byte pierce;
    private int ticks;
    private double mana;
    private double multiplier;
    private String skillName = "";
    private double angle;
    private boolean reversed;
    private boolean saved;

    public SkillProjectile(EntityType<SkillProjectile> type, Level level, ProjectileSkill skill) {
        super(type, level);
        this.skill = skill;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        this.icon = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id.substring("projectile_".length()))));
        setBaseDamage(0.1);
        setCritArrow(false);
        setSilent(true);
    }

    /** 设置击退与穿透等级（来源发射时传给箭的 {@code setKnockback}、{@code setPierceLevel}）。 */
    public SkillProjectile launchedWith(int knockback, int pierce) {
        this.knockback = knockback;
        this.pierce = (byte) pierce;
        return this;
    }

    @Override
    public ItemStack getItem() {
        return icon;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public byte getPierceLevel() {
        return pierce;
    }

    @Override
    protected void doKnockback(LivingEntity entity, DamageSource damageSource) {
        if (knockback > 0) {
            double resistance = Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 push = getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(knockback * 0.6 * resistance);
            if (push.lengthSqr() > 0.0) {
                entity.push(push.x, 0.1, push.z);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel) {
            skill.tick(this, owner);
        }
        if (inGround) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel) {
            skill.hitEntity(this, result.getEntity(), owner);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel) {
            skill.hitBlock(this, result, owner);
        }
    }

    /** 第一个更新刻：改为无重力并返回真；之后返回假（来源用“是否无重力”判断）。 */
    public boolean begin() {
        if (isNoGravity()) {
            return false;
        }
        setNoGravity(true);
        return true;
    }

    /**
     * 记下发射者所选技能的攻击量与倍率，各乘给定系数；发射者不是玩家时攻击量取其 {@code LEVELnumber}，倍率取 {@code mobMultiplier}。
     */
    public void snapshot(Entity owner, double manaScale, double playerMultiplierScale, double mobMultiplier) {
        if (owner instanceof Player player) {
            SkillBook book = Skills.book(Skills.selected(player)).orElse(null);
            mana = book == null ? 0 : book.manaAttack() * manaScale;
            multiplier = book == null ? 0 : book.damageMultiplier() * playerMultiplierScale;
            skillName = book == null ? "" : book.name();
        } else {
            mana = owner.getPersistentData().getDouble("LEVELnumber");
            multiplier = mobMultiplier;
        }
    }

    /** 弹体的计时加一并返回新值。 */
    public int advance() {
        return ++ticks;
    }

    public int ticks() {
        return ticks;
    }

    public double mana() {
        return mana;
    }

    public double multiplier() {
        return multiplier;
    }

    public String skillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    /** 数值是否已记下（保留重力的弹体不用“是否无重力”判断第一刻）。 */
    public boolean saved() {
        return saved;
    }

    public void markSaved() {
        saved = true;
    }

    public double angle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean reversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
}
