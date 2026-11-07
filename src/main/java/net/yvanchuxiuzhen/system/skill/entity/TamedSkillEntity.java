package net.yvanchuxiuzhen.system.skill.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 带自主行为的可驯服技能实体（来源继承 {@code TamableAnimal} 的 {@code SkillFire06Entity}、{@code Skillyin01Entity}）：
 * 施放时以施放者为主人，行为（寻路、攻击目标、刻过程）写在各具体实体类里，不经 {@link EntitySkill} 表。
 * 与 {@link SkillEntity} 共用施放信息 {@link SkillCast}、纹理名与过程动画的同步和 GeckoLib 控制器；模型与属性取自 {@link EntityModelRow}。
 * 没有可喂食的物品（来源 {@code isFood} 恒为假），所以不会进入繁殖，也无法靠喂食驯服。
 */
public abstract class TamedSkillEntity extends TamableAnimal implements GeoEntity, SkillLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(TamedSkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(TamedSkillEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final SkillCast cast = new SkillCast();

    protected TamedSkillEntity(EntityType<? extends TamedSkillEntity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setTexture(model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, SkillAnimations.NONE);
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    /** 设置过程动画（来源 {@code setAnimation}），语义同 {@link SkillEntity#setAnimation}：同步给客户端播放一次，与当前值相同的名字不会再次播放。 */
    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    public SkillCast cast() {
        return cast;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return (AgeableMob) getType().create(level);
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == 20) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("texture", texture());
        cast.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
        cast.load(tag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        SkillAnimations.register(controllers, this, model, () -> entityData.get(ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
