package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 26、27、34、38 号符召唤的动画实体（来源 {@code Fulutexiao26/27/34/38Entity} 与各自的更新过程）：
 * 神压符（土手，20 刻后拍击并定身，再 5 刻停顿、15 刻碎裂后消失）、剑魂符（186 刻内按“起手—三连—间歇”节奏对 3×3 个范围造成物理伤害）、
 * 命轮符（120 刻内把 4 格内非友方拉向自身并逐刻造成暗属性伤害）、水龙符（110 刻内抬起 4 格内非友方并逐刻造成水属性伤害）。
 * 模型、属性、导航、伤害免疫与死亡时序取自 {@code content/fulu/entity_models.json}；品阶、召唤者 UUID 与符名由召唤方写入持久数据。
 * 来源在召唤时请求一次 {@code attack} 过程动画，之后回到默认循环，这里等价于每次出现时先播一次再循环。
 */
public final class TalismanBeast extends PathfinderMob implements GeoEntity, AnimatedLook {
    public static final String EARTH_HAND = "fulutexiao_26";
    public static final String SWORD_SOUL = "fulutexiao_27";
    public static final String FATE_WHEEL = "fulutexiao_34";
    public static final String WATER_DRAGON = "fulutexiao_38";

    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;

    public TalismanBeast(EntityType<? extends TalismanBeast> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        switch (model.lifecycle().navigation()) {
            case GROUND -> {
            }
            case WATER -> navigation = new WaterBoundPathNavigation(this, level);
            case FLYING -> {
                navigation = new FlyingPathNavigation(this, level);
                moveControl = new FlyingMoveControl(this, 10, true);
            }
        }
        xpReward = 0;
        setNoAi(model.noAi());
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        AttributeSupplier.Builder attributes = row.attributes();
        row.lifecycle().flyingSpeed().ifPresent(speed -> attributes.add(Attributes.FLYING_SPEED, speed));
        row.lifecycle().swimSpeed().ifPresent(speed -> attributes.add(NeoForgeMod.SWIM_SPEED, speed));
        return attributes;
    }

    @Override
    public String texture() {
        return model.texture();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isPushedByFluid() {
        return model.lifecycle().pushedByFluid() && super.isPushedByFluid();
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return !(type == NeoForgeMod.WATER_TYPE.value() && model.lifecycle().breathesUnderwater()) && super.canDrownInFluidType(type);
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(model.lifecycle().noGravity() || noGravity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        if (model.lifecycle().noGravity()) {
            setNoGravity(true);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !model.lifecycle().ignoresFallDamage() && super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) {
        if (!model.lifecycle().ignoresFallDamage()) {
            super.checkFallDamage(movement, onGround, block, pos);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        for (EntityModelRow.DamageImmunity immunity : model.lifecycle().damageImmunities()) {
            boolean rejected = switch (immunity) {
                case ARROW -> source.getDirectEntity() instanceof AbstractArrow;
                case PLAYER -> source.getDirectEntity() instanceof Player;
                case POTION -> source.getDirectEntity() instanceof ThrownPotion;
                case AREA_EFFECT_CLOUD -> source.getDirectEntity() instanceof AreaEffectCloud;
                case FALL -> source.is(DamageTypes.FALL);
                case CACTUS -> source.is(DamageTypes.CACTUS);
                case DROWN -> source.is(DamageTypes.DROWN);
                case LIGHTNING -> source.is(DamageTypes.LIGHTNING_BOLT);
                case EXPLOSION -> source.is(DamageTypeTags.IS_EXPLOSION);
                case TRIDENT -> source.getMsgId().equals("trident");
                case ANVIL -> source.is(DamageTypes.FALLING_ANVIL);
                case DRAGON_BREATH -> source.is(DamageTypes.DRAGON_BREATH);
                case WITHER -> source.is(DamageTypes.WITHER);
                case WITHER_SKULL -> source.getMsgId().equals("witherSkull");
            };
            if (rejected) {
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level() instanceof ServerLevel level) {
            switch (model.id()) {
                case EARTH_HAND -> earthHandTick(level);
                case SWORD_SOUL -> swordSoulTick(level);
                case FATE_WHEEL -> fateWheelTick(level);
                case WATER_DRAGON -> waterDragonTick(level);
                default -> {
                }
            }
        }
    }

    private CompoundTag data() {
        return getPersistentData();
    }

    private double count(String key) {
        double value = data().getDouble(key) + 1;
        data().putDouble(key, value);
        return value;
    }

    private void strike(ServerLevel level, Entity target, String label, double damage) {
        if (target instanceof LivingEntity living) {
            SpellDamage.command(level, living, "shanghai " + label + " " + data().getString("belongToMasterUUID") + " true " + damage + " 1");
        }
    }

    private List<Entity> nearby(ServerLevel level, Vec3 center, double halfSize) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(halfSize)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    /** 神压符：阶段一 20 刻后拍击 3.5 格内非友方（土属性 {@code 20×品阶}，缓慢、效果 002、虚弱各 40 刻）；阶段二停顿 5 刻；阶段三碎裂粒子 15 刻后消失。 */
    private void earthHandTick(ServerLevel level) {
        if (!data().getBoolean("xiayibu")) {
            if (count("tick") >= 20) {
                level.playSound(null, BlockPos.containing(getX(), getY(), getZ()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1, 1);
                level.sendParticles(Particles.type("smallexplosiongolden"), getX(), getY(), getZ(), 20, 2, 0, 2, 0);
                level.sendParticles(Particles.type("smokegolden"), getX(), getY(), getZ(), 40, 3, 0, 3, 0.1);
                double damage = 20 * data().getDouble("fululevel");
                for (Entity target : nearby(level, position(), 3.5)) {
                    if (!Relations.isFriendly(this, target)) {
                        strike(level, target, "土", damage);
                        if (target instanceof LivingEntity living) {
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                            living.addEffect(new MobEffectInstance(effect("effect_002"), 40, 9));
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 9));
                        }
                    }
                }
                data().putBoolean("xiayibu", true);
                data().putDouble("tick", 0);
            }
        } else if (!data().getBoolean("xiayibu2")) {
            if (count("tick") >= 5) {
                data().putBoolean("xiayibu2", true);
                data().putDouble("tick", 0);
            }
        } else {
            level.sendParticles(Particles.type("brokengolden"), getX(), getY(), getZ(), 10, 0, 0, 0, 0.5);
            if (count("tick") >= 15) {
                discard();
                data().putDouble("tick", 0);
            }
        }
    }

    private static Holder<MobEffect> effect(String id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).orElseThrow();
    }

    /**
     * 剑魂符：186 刻后消失并向四方喷烟；阶段 "" 22 刻后首击，"正式开始" 每 9 刻一击、三击后进入 "暂缓"，"暂缓" 13 刻后回到 "正式开始"。
     * 每击对以自身为中心的 3×3 个（间距 3、半边 1.5）范围内非友方造成 {@code 20×品阶} 物理伤害，伤害来源为召唤者。
     */
    private void swordSoulTick(ServerLevel level) {
        if (count("tick") >= 186) {
            discard();
            for (double[] offset : new double[][] {{0, 5.5}, {0, -5.5}, {-5.5, 0}, {5.5, 0}}) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, getX() + offset[0], getY() + 1, getZ() + offset[1], 20, 0, 1, 0, 0.1);
            }
            data().putDouble("tick", 0);
        }
        boolean attack = false;
        String phase = data().getString("jieduan");
        if (phase.isEmpty()) {
            if (count("tick2") >= 22) {
                attack = true;
                data().putString("jieduan", "正式开始");
                data().putDouble("tick2", 0);
            }
        } else if (phase.equals("正式开始")) {
            if (count("tick2") >= 9) {
                double strikes = count("jishu");
                attack = true;
                data().putDouble("tick2", 0);
                if (strikes >= 3) {
                    data().putDouble("tick2", 0);
                    data().putDouble("jishu", 0);
                    data().putString("jieduan", "暂缓");
                }
            }
        } else if (phase.equals("暂缓")) {
            if (count("tick2") >= 13) {
                attack = true;
                data().putString("jieduan", "正式开始");
                data().putDouble("tick2", 0);
            }
        }
        if (attack) {
            Entity master = master(level);
            float damage = (float) (20 * data().getDouble("fululevel"));
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    Vec3 center = new Vec3(getX() - 3 + column * 3, getY() + 1, getZ() - 3 + row * 3);
                    for (Entity target : nearby(level, center, 1.5)) {
                        if (!Relations.isFriendly(this, target)) {
                            target.hurt(sourceOf(level, master), damage);
                        }
                    }
                }
            }
        }
    }

    private Entity master(ServerLevel level) {
        try {
            return level.getEntity(UUID.fromString(data().getString("belongToMasterUUID")));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /** 来源 {@code EntityDamageSource("generic.player", master)}：召唤者为玩家按玩家攻击，生物按生物攻击，找不到时无来源。 */
    private static DamageSource sourceOf(ServerLevel level, Entity master) {
        if (master instanceof Player player) {
            return level.damageSources().playerAttack(player);
        }
        return master instanceof LivingEntity living ? level.damageSources().mobAttack(living) : level.damageSources().generic();
    }

    /** 命轮符：120 刻后消失；4 格内非友方被水平拉向自身并逐刻受暗属性固定伤害 {@code 12×品阶}。 */
    private void fateWheelTick(ServerLevel level) {
        if (count("tick") >= 120) {
            discard();
            data().putDouble("tick", 0);
        }
        double damage = 12 * data().getDouble("fululevel");
        for (Entity target : nearby(level, position(), 4)) {
            if (!Relations.isFriendly(this, target)) {
                target.setDeltaMovement(new Vec3((getX() - target.getX()) * 0.3, 0, (getZ() - target.getZ()) * 0.3));
                strike(level, target, "暗", damage);
            }
        }
    }

    /** 水龙符：110 刻后消失（100 刻起在上空飘云）；4 格内非友方被轻抬并逐刻受水属性固定伤害 {@code 8×品阶}。 */
    private void waterDragonTick(ServerLevel level) {
        double ticks = count("tick");
        if (ticks >= 110) {
            discard();
            data().putDouble("tick", 0);
        } else if (ticks >= 100) {
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 8, getZ(), 30, 0.5, 1, 0.5, 0.1);
        }
        double height = getY() + data().getDouble("tick") * 0.1;
        level.sendParticles(ParticleTypes.CLOUD, getX(), height, getZ(), 5, 4, 3, 4, 0.1);
        level.sendParticles(ParticleTypes.SPLASH, getX(), height, getZ(), 5, 4, 3, 4, 0.1);
        double damage = data().getDouble("fululevel") * 8;
        for (Entity target : nearby(level, position(), 4)) {
            if (!Relations.isFriendly(this, target)) {
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(), target.getZ(), 5, 0, 1, 0, 0.5);
                level.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getY(), target.getZ(), 5, 0, 1, 0, 0.5);
                target.setDeltaMovement(new Vec3(0, 0.1, 0));
                strike(level, target, "水", damage);
            }
        }
    }

    /** 先播一次 {@code attack} 过程动画，再回到默认循环（来源召唤时请求一次过程动画）。 */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String number = model.id().substring("fulutexiao_".length());
        controllers.add(new AnimationController<>(this, "main", model.lifecycle().animationTransitionTicks(),
                state -> state.setAndContinue(RawAnimation.begin().thenPlay("animation.fulutexiao" + number + ".attack").thenLoop(model.idle()))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
