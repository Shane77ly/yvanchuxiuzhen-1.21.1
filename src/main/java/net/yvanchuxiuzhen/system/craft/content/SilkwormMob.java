package net.yvanchuxiuzhen.system.craft.content;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 两种蚕和两种蛾的工艺实体；蛾保留地面导航，蚕可被拾回为默认道具。 */
public final class SilkwormMob extends PathfinderMob implements GeoEntity, AnimatedLook {
    public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(SilkwormMob.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SilkwormMob.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SilkwormMob.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Kind kind;
    private String procedure = "empty";

    public SilkwormMob(EntityType<? extends SilkwormMob> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        xpReward = 0;
        setNoAi(false);
        setTexture(kind.resource());
        if (kind.larva()) {
            goalSelector.addGoal(3, new TemptGoal(this, 1,
                    Ingredient.of(BuiltInRegistries.ITEM.get(id("mulberryleaves"))), false));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOOT, false);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, "");
    }

    public Kind kind() {
        return kind;
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public String getSyncedAnimation() {
        return entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!kind.larva()) return super.mobInteract(player, hand);
        super.mobInteract(player, hand);
        if (!level().isClientSide) {
            String item = kind == Kind.WORM ? "silkworm" : "silkwormgold";
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BuiltInRegistries.ITEM.get(id(item))));
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** 来源实体更新事件先消费上一刻请求，baseTick 随后可写入新的行走请求。 */
    @Override
    public void tick() {
        String requested = getSyncedAnimation();
        if (!requested.equals("undefined")) {
            setAnimation("undefined");
            procedure = requested;
        }
        super.tick();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (kind.larva() && getDeltaMovement().x + getDeltaMovement().z > 0) setAnimation(kind.walk());
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (deathTime == 20) {
            remove(RemovalReason.KILLED);
            dropExperience(lastHurtByPlayer);
        }
    }

    /** 来源在死亡第二十刻结算经验，目标死亡入口的提前调用不重复结算。 */
    @Override
    protected void dropExperience(Entity killer) {
        if (deathTime == 20) super.dropExperience(killer);
    }

    public static AttributeSupplier.Builder attributes(Kind kind) {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, kind.larva() ? 0.1 : 0.2)
                .add(Attributes.MAX_HEALTH, kind.larva() ? 2 : 5).add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, kind == Kind.GOLD_MOTH ? 3 : 1).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movement));
        controllers.add(new AnimationController<>(this, "procedure", 4, this::procedure));
    }

    private PlayState movement(AnimationState<SilkwormMob> state) {
        if (!procedure.equals("empty")) return PlayState.STOP;
        boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)
                || kind == Kind.WORM && isSprinting();
        return state.setAndContinue(RawAnimation.begin().thenLoop(moving ? kind.walk() : kind.idle()));
    }

    private PlayState procedure(AnimationState<SilkwormMob> state) {
        AnimationController<SilkwormMob> controller = state.getController();
        if (!procedure.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(procedure, Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                procedure = "empty";
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    public enum Kind {
        WORM("silkwormentity", "silkworm_entity", true),
        GOLD_WORM("silkwormentitygold", "silkworm_entity_gold", true),
        MOTH("silkwormmoth", "silkworm_moth", false),
        GOLD_MOTH("silkwormmothgold", "silkworm_moth_gold", false);

        private final String id;
        private final String resource;
        private final boolean larva;

        Kind(String id, String resource, boolean larva) {
            this.id = id;
            this.resource = resource;
            this.larva = larva;
        }

        public String id() {
            return id;
        }

        public String resource() {
            return resource;
        }

        public boolean larva() {
            return larva;
        }

        public String idle() {
            return "animation." + resource + ".idle";
        }

        public String walk() {
            return "animation." + resource + ".walk";
        }
    }
}
