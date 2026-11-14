package net.yvanchuxiuzhen.system.world.entity;

import java.util.ArrayList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.WorldSetup;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 寻灵盘光环每刻跟随同维度主人，每二十刻搜索，一百刻后消失。 */
public final class GuidanceHalo extends TamableAnimal implements GeoEntity {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(GuidanceHalo.class, EntityDataSerializers.STRING);
    private static final String NAVIGATION = "structure_navigation";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GuidanceHalo(EntityType<? extends GuidanceHalo> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "kong");
    }

    public String texture() { return entityData.get(TEXTURE); }

    /** 同一主人已召出的光环共享世界系统导航标签，重载存档后各自独立。 */
    public void shareNavigation(Player owner) {
        CompoundTag navigation = owner.getPersistentData().getCompound(NAVIGATION);
        owner.getPersistentData().put(NAVIGATION, navigation);
        navigation.putString("belongToMasterUUID", owner.getStringUUID());
        navigation.putString("playeruuid", owner.getStringUUID());
        getPersistentData().put(NAVIGATION, navigation);
        getPersistentData().putString("team", "");
    }

    public void target(Vec3 position) {
        CompoundTag navigation = getPersistentData().getCompound(NAVIGATION);
        navigation.putDouble("mubiaoX", position.x);
        navigation.putDouble("mubiaoY", position.y);
        navigation.putDouble("mubiaoZ", position.z);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) { }
    @Override protected void pushEntities() { }
    @Override protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) { }
    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof SpawnEggItem) return super.mobInteract(player, hand);
        if (level().isClientSide) return isTame() && isOwnedBy(player) ? InteractionResult.sidedSuccess(true) : InteractionResult.PASS;
        if (isTame()) return isOwnedBy(player) ? super.mobInteract(player, hand) : InteractionResult.CONSUME;
        InteractionResult result = super.mobInteract(player, hand);
        if (result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME) setPersistenceRequired();
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof AbstractArrow || direct instanceof Player || direct instanceof ThrownPotion || direct instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getMsgId().equals("trident") || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        CompoundTag data = getPersistentData();
        CompoundTag navigation = data.getCompound(NAVIGATION);
        data.putDouble("guanghuantick", data.getDouble("guanghuantick") + 1);
        if (data.getDouble("guanghuantick") >= 100) {
            data.putDouble("guanghuantick", 0);
            if (!level().isClientSide) discard();
        }
        String owner = navigation.getString("belongToMasterUUID");
        if (!owner.isEmpty()) {
            boolean present = false;
            for (Player player : new ArrayList<>(level().players())) {
                if (!player.getStringUUID().equals(owner)) continue;
                present = true;
                teleportTo(player.getX(), player.getY() + 1, player.getZ());
                lookAt(EntityAnchorArgument.Anchor.EYES,
                        new Vec3(navigation.getDouble("mubiaoX"), navigation.getDouble("mubiaoY"), navigation.getDouble("mubiaoZ")));
                data.putDouble("guanghuantick2", data.getDouble("guanghuantick2") + 1);
                if (data.getDouble("guanghuantick2") >= 20) {
                    if (level() instanceof ServerLevel server) StructureSearch.search(server, position(), player.getStringUUID());
                    data.putDouble("guanghuantick2", 0);
                }
                break;
            }
            if (!present && !level().isClientSide) discard();
        }
        refreshDimensions();
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        GuidanceHalo child = WorldSetup.<GuidanceHalo>entityType("guanghuan").get().create(level);
        child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.BREEDING, null);
        return child;
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 1) remove(RemovalReason.KILLED);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16).add(Attributes.FLYING_SPEED, 0.3);
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.idle"))));
    }
}
