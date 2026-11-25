package net.yvanchuxiuzhen.system.equipment.content;

import java.text.DecimalFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.FlyingInstrumentData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 飞行法器实体：保存来源法器字段，处理取回、充灵力、骑乘飞行、伤害免疫与死亡掉落。 */
public final class FlyingInstrumentEntity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final TagKey<Item> LINGSHI = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi"));
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(FlyingInstrumentEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(FlyingInstrumentEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final FlyingInstrumentItem.Definition definition;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedure = "";
    private String flyItemId = "";
    private String flyEntityId = "";
    private String flyName = "";
    private double healthMax;
    private double powerMax;
    private double power;
    private double speedNum;
    private boolean flying;
    private double directionYaw;
    private double anglePitch;

    public FlyingInstrumentEntity(EntityType<? extends FlyingInstrumentEntity> type, Level level, EntityModelRow model,
            FlyingInstrumentItem.Definition definition) {
        super(type, level);
        this.model = model;
        this.definition = definition;
        setNoAi(model.noAi());
        setPersistenceRequired();
        xpReward = 0;
        setTexture(model.texture());
    }

    public double power() { return power; }
    public void setPower(double power) { this.power = power; }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes();
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, FlyingInstrumentItem.Definition definition, ItemStack stack) {
        FlyingInstrumentEntity entity = EquipmentSetup.<FlyingInstrumentEntity>flyingInstrumentType(definition.entityId()).get().create(level);
        entity.configure(owner, stack);
        entity.moveTo(owner.getX(), owner.getY(), owner.getZ(), level.random.nextFloat() * 360, 0);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(entity);
    }

    private void configure(ServerPlayer owner, ItemStack stack) {
        FlyingInstrumentData item = Equipment.flyingInstrument(stack);
        flyItemId = item.itemId();
        flyEntityId = item.entityId();
        flyName = item.name();
        healthMax = item.healthMax();
        powerMax = item.powerMax();
        power = item.power();
        speedNum = item.speed();
        flying = false;
        // 来源无方向物品能力的0号槽为原生主手，完整原物品随实体装备槽保存。
        setItemInHand(InteractionHand.MAIN_HAND, stack.copyWithCount(1));
        Relations.copyAllegiance(owner, this);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthMax);
        setHealth((float) (item.health() > 0 ? item.health() : healthMax));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    private void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    private void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void tick() {
        String requested = entityData.get(ANIMATION);
        if (!requested.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedure = requested;
        }
        super.tick();
        if (model.lifecycle().noGravity()) setNoGravity(true);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) updateFlight();
        refreshDimensions();
    }

    private void updateFlight() {
        if (!isVehicle()) {
            if (isInWater()) {
                setDeltaMovement(new Vec3(0, .1, 0));
            } else if (level().isEmptyBlock(blockPosition().below())) {
                setDeltaMovement(new Vec3(0, -.2, 0));
            }
            if (isNoGravity()) setNoGravity(false);
            return;
        }
        if (StatusEffects.consumeEntityFlightBlock(this)) {
            if (isNoGravity()) setNoGravity(false);
            return;
        }
        for (Entity passenger : getPassengers()) {
            if (!(passenger instanceof ServerPlayer player)) continue;
            double speed = speedNum;
            if (Cultivation.get(player, CultivationValue.POWER) + power >= speed) {
                directionYaw = player.getYRot();
                anglePitch = player.getXRot();
                if (flying) {
                    if (power >= 0) {
                        power -= speed;
                    } else {
                        Cultivation.add(player, CultivationValue.POWER, -speed);
                    }
                    setYRot((float) directionYaw);
                    setXRot((float) anglePitch);
                    setYBodyRot(getYRot());
                    setYHeadRot(getYRot());
                    yRotO = getYRot();
                    xRotO = getXRot();
                    yHeadRotO = getYRot();
                    yBodyRotO = getYRot();
                    setDeltaMovement(getLookAngle().scale(speed * 2));
                }
                setNoGravity(true);
                fallDistance = 0;
            } else if (isNoGravity()) {
                setNoGravity(false);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                ItemStack returned = itemStack();
                if (!returned.isEmpty()) ItemHandlerHelper.giveItemToPlayer(serverPlayer, returned);
                discard();
            } else {
                handleUse(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    private void handleUse(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.is(LINGSHI)) {
            double maximum = powerMax;
            if (power < maximum) {
                player.serverLevel().sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 10, 1, 1, 1, .05);
                int amount = materialPower(held);
                if (amount > 0) {
                    power += amount;
                    Item ingredient = held.getItem();
                    player.getInventory().clearOrCountMatchingItems(stack -> stack.is(ingredient), 1, player.inventoryMenu.getCraftSlots());
                }
            }
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.flyinginstrument.power",
                    new DecimalFormat("0").format(power), new DecimalFormat("0").format(maximum)), false);
            if (power > maximum) {
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.flyinginstrument.full"), false);
                power = maximum;
            }
        } else if (isItem(held, "hammer")) {
            if (getHealth() < getMaxHealth()) {
                player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 10, 1, 1, 1, 0);
                setHealth(getHealth() + 1);
            }
        } else {
            player.startRiding(this, true);
        }
    }

    private static int materialPower(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return switch (id) {
            case "cailiao_191" -> 1;
            case "cailiao_192" -> 4;
            case "cailiao_193" -> 16;
            case "cailiao_194" -> 64;
            case "cailiao_195" -> 256;
            case "cailiao_196" -> 1024;
            case "cailiao_197" -> 4096;
            case "cailiao_198" -> 16384;
            case "cailiao_199" -> 65536;
            default -> 0;
        };
    }

    private static boolean isItem(ItemStack stack, String id) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private ItemStack itemStack() {
        ItemStack original = getMainHandItem().copyWithCount(1);
        // 此前已投放的法器只保存物品 ID 和飞行字段，没有保存装备槽。
        if (original.isEmpty() && flyItemId.equals(ModId.VALUE + ":" + definition.itemId())) {
            original = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, definition.itemId())));
        }
        return itemStack(original, getHealth());
    }

    private ItemStack itemStack(ItemStack stack, double health) {
        if (stack.isEmpty()) return stack;
        stack.set(EquipmentData.FLYING_INSTRUMENT.get(), new FlyingInstrumentData(
                flyItemId,
                flyEntityId,
                flyName,
                healthMax,
                health,
                powerMax,
                power,
                speedNum));
        return stack;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        double offset = switch (definition.entityId()) {
            case "flyinginstrument_01", "flyinginstrument_02", "flyinginstrument_03" -> -.2;
            case "flyinginstrument_05" -> -.7;
            case "flyinginstrument_06" -> .1;
            case "flyinginstrument_07" -> -.3;
            default -> 0;
        };
        return super.getPassengerRidingPosition(passenger).add(0, offset, 0);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) {
            Item instrument = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, definition.itemId()));
            ItemStack drop = itemStack(new ItemStack(instrument), 1);
            if (!drop.isEmpty()) {
                ItemEntity item = new ItemEntity(level(), getX(), getY() + 1, getZ(), drop);
                item.setPickUpDelay(10);
                level().addFreshEntity(item);
            }
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == 20) remove(RemovalReason.KILLED);
    }

    public double powerMax() { return powerMax; }
    public boolean flying() { return flying; }
    public void setFlying(boolean flying) { this.flying = flying; }
    public String flyItemId() { return flyItemId; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("texture", texture());
        tag.putString("flyItemId", flyItemId);
        tag.putString("flyEntityId", flyEntityId);
        tag.putString("flyName", flyName);
        tag.putDouble("healthMax", healthMax);
        tag.putDouble("powerMax", powerMax);
        tag.putDouble("power", power);
        tag.putDouble("speedNum", speedNum);
        tag.putBoolean("flying", flying);
        tag.putDouble("directionYaw", directionYaw);
        tag.putDouble("anglePitch", anglePitch);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
        flyItemId = tag.getString("flyItemId");
        flyEntityId = tag.getString("flyEntityId");
        flyName = tag.getString("flyName");
        healthMax = tag.getDouble("healthMax");
        powerMax = tag.getDouble("powerMax");
        power = tag.getDouble("power");
        speedNum = tag.getDouble("speedNum");
        flying = tag.getBoolean("flying");
        directionYaw = tag.getDouble("directionYaw");
        anglePitch = tag.getDouble("anglePitch");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedure.isEmpty()) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.loop(onGround())));
        }));
        controllers.add(new AnimationController<>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<?> controller = state.getController();
            if (!procedure.isEmpty() && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedure, Animation.LoopType.PLAY_ONCE));
                procedure = "";
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
