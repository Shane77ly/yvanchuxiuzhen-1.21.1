package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.yvanchuxiuzhen.platform.ModId;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 桃木精：技能阶段保存于实体自身 AI 状态字段。 */
public final class TaomujingEntity extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(TaomujingEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(TaomujingEntity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private CompoundTag aiState = new CompoundTag();

    public TaomujingEntity(EntityType<? extends TaomujingEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public CompoundTag aiState() {
        return aiState;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("AiState", aiState.copy());
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        aiState = tag.contains("AiState") ? tag.getCompound("AiState").copy() : new CompoundTag();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16)
                .add(YaoshouAttributes.SIZE);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "taoshujing"); builder.define(ANIMATION, "undefined");
    }

    @Override protected void registerGoals() { super.registerGoals(); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public boolean hurt(DamageSource source, float amount) {
        TaomujingBehavior.hurt(this, source.getEntity());
        return !source.is(DamageTypes.FALL) && !source.is(DamageTypes.CACTUS) && !source.is(DamageTypes.DROWN) && super.hurt(source, amount);
    }

    @Override public void die(DamageSource source) {
        super.die(source);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (EntityAttributes.realmName(this).isEmpty()) initializeSourceData();
        return result;
    }

    private void initializeSourceData() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        var enchantments = level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        sword.enchant(enchantments.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingtikiller"))), 2);
        sword.enchant(enchantments.getOrThrow(Enchantments.SMITE), 2);
        setItemInHand(InteractionHand.MAIN_HAND, sword);
        double level = EntityAttributes.level(this) < 1 ? 11 + random.nextInt(5) : EntityAttributes.level(this);
        EntityAttributes.setLevel(this, level);
        getAttribute(YaoshouAttributes.SIZE).setBaseValue((random.nextInt(31) + 70) * .01 + level / 4 * .1);
        CreatureSourceStats.initialize(this, 11, true, false,
                "平庸", "绝佳", "勉强", "勉强", "贫弱", "贫弱", "平庸", "强劲", "贫弱", "平庸", "贫弱", "贫弱", "贫弱");
    }

    @Override public void baseTick() {
        super.baseTick();
        if (isAlive() && getMainHandItem().isEmpty()) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_201"))));
        }
        TaomujingBehavior.tick(this);
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) { entityData.set(ANIMATION, "undefined"); procedureAnimation = request; }
    }

    @Override public float getScale() { return (float) getAttributeValue(YaoshouAttributes.SIZE); }

    @Override protected void tickDeath() { if (++deathTime == 600) { remove(RemovalReason.KILLED); dropExperience(this); } }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TaomujingEntity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.taoshujing.die", Animation.LoopType.PLAY_ONCE));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.taoshujing.sneak"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.taoshujing.idle"));
        }));
        controllers.add(new AnimationController<TaomujingEntity>(this, "procedure", 4, state -> {
            AnimationController<TaomujingEntity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureAnimation = "empty";
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
