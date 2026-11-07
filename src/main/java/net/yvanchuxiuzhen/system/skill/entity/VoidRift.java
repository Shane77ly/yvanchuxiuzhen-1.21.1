package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 虚空裂隙（来源 {@code Xvkongliexi}）：无重力的传送实体，32 刻后可进入、332 刻后消失。
 * 是否可进入、是否已使用与寿命计数保存在实体的持久数据；一次碰撞消耗整道裂隙，返回维度编号经 xiulian 的体质公开操作保存。
 */
public final class VoidRift extends PathfinderMob implements GeoEntity, SkillLook {
    public static final String ID = "xvkongliexi";
    private static final ResourceKey<Level> VOID = dimension("yvanchuxiuzhen:xvkong");
    private static final Map<Double, ResourceKey<Level>> ORIGINS = Map.of(
            1.0, Level.OVERWORLD, 2.0, Level.NETHER, 3.0, Level.END,
            4.0, dimension("yvanchuxiuzhen:dimensiontianqve"), 5.0, dimension("yvanchuxiuzhen:dimensionxiuluo"),
            6.0, dimension("yvanchuxiuzhen:dimensionrenjian"), 7.0, dimension("yvanchuxiuzhen:dimensionyaohun"),
            8.0, dimension("yvanchuxiuzhen:dimensionwugui"), 9.0, dimension("yvanchuxiuzhen:dimensiondiling"));

    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);

    public VoidRift(EntityType<VoidRift> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setNoAi(true);
        setPersistenceRequired();
        setNoGravity(true);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, 0.3);
    }

    /** 来源 {@code Jinengxvkong}：境界含体质修正达到 31 时，在视线五格处开一道裂隙，先清除目标周围八格内的旧裂隙。 */
    public static void open(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect") < 31) {
            player.displayClientMessage(Component.literal("只有元婴大能才能撕裂虚空，遨游在无尽黑暗中。"), false);
            return;
        }
        player.closeContainer();
        ServerLevel level = player.serverLevel();
        Vec3 eyes = player.getEyePosition(1);
        BlockPos pos = level.clip(new ClipContext(eyes, eyes.add(player.getViewVector(1).scale(5)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();
        Vec3 at = Vec3.atLowerCornerOf(pos);
        for (VoidRift old : level.getEntitiesOfClass(VoidRift.class, new AABB(at, at).inflate(8))) {
            old.discard();
        }
        VoidRift rift = SkillSetup.<VoidRift>entityType(ID).get().create(level);
        rift.moveTo(pos.getX(), pos.getY(), pos.getZ(), player.getYRot() + 180, 0);
        SkillEntities.turn(rift, player.getYRot() + 180, 0);
        rift.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(rift);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + getBbHeight() / 2, getZ(), 10, 2, 2, 2, 0);
            CompoundTag state = getPersistentData();
            double age = state.getDouble("xiaoshitick") + 1;
            state.putDouble("xiaoshitick", age);
            if (!state.getBoolean("keyijinru") && age >= 32) {
                state.putBoolean("keyijinru", true);
            } else if (age >= 332) {
                discard();
                level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + getBbHeight() / 2, getZ(), 40, 0, 0, 0, 0.4);
                state.putDouble("xiaoshitick", 0);
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (player instanceof ServerPlayer serverPlayer) {
            travel(serverPlayer);
        }
    }

    private void travel(ServerPlayer player) {
        CompoundTag state = getPersistentData();
        if (!state.getBoolean("keyijinru") || state.getBoolean("shifouyichuanyve")) {
            return;
        }
        ResourceKey<Level> current = player.level().dimension();
        double origin = 0;
        for (Map.Entry<Double, ResourceKey<Level>> entry : ORIGINS.entrySet()) {
            if (entry.getValue().equals(current)) {
                origin = entry.getKey();
                Cultivation.setTizhiDouble(player, "weidu", origin);
                break;
            }
        }
        boolean returning = current.equals(VOID);
        if (returning || origin >= 1) {
            ResourceKey<Level> destination = VOID;
            Vec3 at;
            if (returning) {
                // 来源在编号未匹配时仍以空路径查找世界，并在查找前清零返回编号。
                destination = ORIGINS.getOrDefault(Cultivation.tizhiDouble(player, "weidu"), dimension(""));
                Cultivation.setTizhiDouble(player, "weidu", 0);
                at = position().scale(50);
            } else {
                at = new Vec3(Math.round(getX() / 50), Math.round(getY() / 50), Math.round(getZ() / 50));
            }
            player.displayClientMessage(Component.literal(returning ? "出来" : "进入"), false);
            if (current.equals(destination)) {
                return;
            }
            ServerLevel nextLevel = player.server.getLevel(destination);
            if (nextLevel != null) {
                player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
                // 1.21.1 的跨维度传送已同步能力、状态效果和本人附件。
                player.teleportTo(nextLevel, at.x, at.y, at.z, player.getYRot(), player.getXRot());
                player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
            }
        }
        state.putBoolean("shifouyichuanyve", true);
    }

    private static ResourceKey<Level> dimension(String id) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(id));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getMsgId().equals("trident") || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) {
            return false;
        }
        return super.hurt(source, amount);
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
        if (++deathTime == 20) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public String texture() {
        return model.texture();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        SkillAnimations.register(controllers, this, model, () -> SkillAnimations.NONE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
