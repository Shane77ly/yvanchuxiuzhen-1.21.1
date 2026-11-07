package net.yvanchuxiuzhen.system.skill.entity;

import java.text.DecimalFormat;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 伤害范围提示（来源 {@code Shanghaifanwei}）：动画显示判定范围，普通提示按倒计时移除，惊蛰闻雷存续 40 刻。
 * 控雷阵在第 20 刻以该实体的位置和持久数据调用落雷命令；其余技能的伤害由各自的生成者结算。
 */
public final class DamageArea extends PathfinderMob implements GeoEntity, AnimatedLook {
    public static final String ID = "shanghaifanwei";
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(DamageArea.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);

    public DamageArea(EntityType<DamageArea> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setNoAi(true);
        setPersistenceRequired();
        setNoGravity(true);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, 0.3).add(NeoForgeMod.SWIM_SPEED, 0.3);
    }

    /** 生成者提供动画、技能分支、持续刻数与控雷阵攻击量；普通提示的技能名为空。 */
    public static void show(Entity owner, Vec3 at, String animation, String skillName, double duration, double attackDamage) {
        ServerLevel level = (ServerLevel) owner.level();
        DamageArea area = SkillSetup.<DamageArea>entityType(ID).get().create(level);
        area.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360, 0);
        area.setAnimation(animation);
        CompoundTag state = area.getPersistentData();
        if (skillName.isEmpty()) {
            Relations.copyAllegiance(owner, area);
        } else {
            Relations.setTeam(area, Relations.team(owner));
        }
        state.putString("jinengname", skillName);
        state.putDouble("daojishi", duration);
        if (skillName.equals("控雷阵")) {
            state.putDouble("AttackDamage", attackDamage);
            state.putString("AttackUUID", owner.getStringUUID());
        }
        level.addFreshEntity(area);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, SkillAnimations.NONE);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag state = getPersistentData();
        String skill = state.getString("jinengname");
        if (skill.equals("控雷阵") || skill.equals("惊蛰闻雷")) {
            double age = state.getDouble("jishiqi") + 1;
            state.putDouble("jishiqi", age);
            if (age >= (skill.equals("控雷阵") ? 20 : 40)) {
                if (skill.equals("控雷阵")) {
                    CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, position(), Vec2.ZERO, level,
                            4, "", Component.empty(), level.getServer(), null).withSuppressedOutput();
                    String command = "lightninglanding " + new DecimalFormat("0").format(state.getDouble("AttackDamage"))
                            + " " + state.getString("AttackUUID") + " 1 " + Relations.team(this) + " 2 白色";
                    level.getServer().getCommands().performPrefixedCommand(source, command);
                }
                state.putDouble("jishiqi", 0);
                discard();
            }
        } else {
            double remaining = state.getDouble("daojishi") - 1;
            state.putDouble("daojishi", remaining);
            if (remaining <= 0) {
                discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !SkillImmunity.rejectsDamage(model.lifecycle().damageImmunities(), source) && super.hurt(source, amount);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 1) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public String texture() {
        return model.texture();
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
