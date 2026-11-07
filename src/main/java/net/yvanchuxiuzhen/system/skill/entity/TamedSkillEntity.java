package net.yvanchuxiuzhen.system.skill.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 带自主行为的可驯服技能实体（来源继承 {@code TamableAnimal} 的 {@code SkillFire06Entity}、{@code Skillyin01Entity}）：
 * 施放时以施放者为主人，行为（寻路、攻击目标、刻过程）写在各具体实体类里，不经 {@link EntitySkill} 表。
 * 与 {@link SkillEntity} 共用施放信息 {@link SkillCast}、纹理名与过程动画的同步和 GeckoLib 控制器；模型与属性取自 {@link EntityModelRow}。
 * 没有可喂食的物品（来源 {@code isFood} 恒为假），喂食不触发繁殖或驯服；刷怪蛋仍可通过原生交互生成后代。
 */
public abstract class TamedSkillEntity extends TamableAnimal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(TamedSkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(TamedSkillEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final SkillCast cast = new SkillCast();
    protected final SkillAnimations animationState;

    protected TamedSkillEntity(EntityType<? extends TamedSkillEntity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        animationState = new SkillAnimations(model.lifecycle().animationTransitionTicks());
        xpReward = 0;
        setTexture(model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, SkillAnimations.NO_REQUEST);
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    /** 设置待消费的过程动画请求，消费与播放语义同 {@link SkillEntity#setAnimation}。 */
    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void tick() {
        animationState.tick(entityData, ANIMATION);
        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    protected final EntityModelRow model() {
        return model;
    }

    public SkillCast cast() {
        return cast;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    /** 无食物分支的来源交互：刷怪蛋交给原生流程，已驯服实体在服务端消费非主人的交互。 */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof SpawnEggItem) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide()) {
            return isTame() && isOwnedBy(player) ? InteractionResult.sidedSuccess(true) : InteractionResult.PASS;
        }
        if (isTame()) {
            return isOwnedBy(player) ? super.mobInteract(player, hand) : InteractionResult.CONSUME;
        }
        InteractionResult result = super.mobInteract(player, hand);
        if (result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME) {
            setPersistenceRequired();
        }
        return result;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        TamedSkillEntity offspring = (TamedSkillEntity) getType().create(level);
        offspring.finalizeSpawn(level, level.getCurrentDifficultyAt(offspring.blockPosition()), MobSpawnType.BREEDING, null);
        return offspring;
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == model.lifecycle().deathTicks()) {
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
        animationState.register(controllers, this, model);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
