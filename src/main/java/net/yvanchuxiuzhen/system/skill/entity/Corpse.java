package net.yvanchuxiuzhen.system.skill.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 起尸诀的尸体（来源 {@code Skillyin01Entity}）：亡灵，跟随主人，战斗行为由 {@link CorpseAi} 的技能状态机每刻推进，
 * 生成与升级见 {@link Corpses}。战斗数值与技能状态沿用来源放在 {@code getPersistentData} 的键（combat 的伤害结算读 {@code mana_Att} 等）。
 * 受伤、死亡与脚步声音取自僵尸村民与僵尸马；免疫凋零伤害；不会被喂食驯服（见 {@link TamedSkillEntity}），不会远离玩家后消失。
 * 来源的“攻击”控制器只在原版挥臂（{@code getAttackAnim}）时播放，而这里的攻击由过程动画 {@code animation.skillyin01.attack} 承担，所以不另设该控制器。
 */
public final class Corpse extends TamedSkillEntity {
    public static final String ID = "skillyin_01";

    private static final String IDLE = "animation.skillyin01.idle";
    private static final String WALK = "animation.skillyin01.walk";
    private static final String SWIM = "animation.skillyin01.swim";
    private static final String SPRINT = "animation.skillyin01.sprint";
    private static final String DIE = "animation.skillyin01.die";

    public Corpse(EntityType<Corpse> type, Level level, EntityModelRow model) {
        super(type, level, model);
        setPersistenceRequired();
    }

    /** 按等级设定生命、攻击与来源放在 {@code getPersistentData} 的战斗数值（攻击 {@code 4 + 2 × 等级}、生命 {@code 40 + 20 × 等级}），并回满生命。 */
    public void empower(double level) {
        double health = 40 + level * 20;
        double attack = 4 + level * 2;
        CompoundTag data = getPersistentData();
        data.putDouble("LEVELnumber", level);
        data.putDouble("health_Max", health);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        setHealth((float) health);
        data.putDouble("attack_Num", attack);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        data.putDouble("attack_Def", Math.round(attack * 0.6));
        data.putDouble("mana_Att", attack);
        data.putDouble("mana_Def", Math.round(attack * 0.6));
        data.putDouble("soul_Att", Math.round(attack * 0.2));
        data.putDouble("soul_Def", attack);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new FollowOwnerGoal(this, 1, 10, 2));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ZOMBIE_VILLAGER_STEP, 0.15f, 1);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide()) {
            CorpseAi.hurt(this, source.getEntity());
        }
        if (source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void awardKillScore(Entity killed, int score, DamageSource source) {
        super.awardKillScore(killed, score, source);
        if (!level().isClientSide()) {
            Corpses.grow(this, killed);
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) {
            CorpseAi.tick(this);
        }
    }

    /** 移动控制器：移动中走路，濒死时死亡动画，水中游泳，疾跑时跑步，否则待机；再加过程动画。 */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        SkillAnimations.movement(controllers, this, state -> {
            if (state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) {
                return RawAnimation.begin().thenLoop(WALK);
            }
            if (isDeadOrDying()) {
                return RawAnimation.begin().thenPlay(DIE);
            }
            if (isInWaterOrBubble()) {
                return RawAnimation.begin().thenLoop(SWIM);
            }
            if (isSprinting()) {
                return RawAnimation.begin().thenLoop(SPRINT);
            }
            return RawAnimation.begin().thenLoop(IDLE);
        });
        SkillAnimations.procedure(controllers, this, this::requestedAnimation);
    }
}
