package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.entity.VoidRift;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 虚空裂隙的门槛、生命周期与正式虚空维度下的传送行为；进入直接调用碰撞方法，返回由真实玩家碰撞触发。
 * 计时边界在一次服务端动作内推进，避免用客户端等待刻数推断实体的精确年龄。
 */
final class VoidRiftCheck extends ActionCheck {
    private static final ResourceKey<Level> VOID = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("yvanchuxiuzhen:xvkong"));
    private ServerLevel originalLevel;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private double originalRank;
    private double originalCorrection;
    private double originalReturn;
    private boolean originalFlying;
    private VoidRift entrance;
    private VoidRift distant;
    private VoidRift exit;

    VoidRiftCheck(List<String> report, String levelId) {
        super("虚空裂隙", report, levelId);
        script(
                new Action(12, player -> {
                    originalLevel = player.serverLevel();
                    originalPosition = player.position();
                    originalYaw = player.getYRot();
                    originalPitch = player.getXRot();
                    originalRank = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
                    originalCorrection = Cultivation.tizhiDouble(player, "LevelNumberCorrect");
                    originalReturn = Cultivation.tizhiDouble(player, "weidu");
                    originalFlying = player.getAbilities().flying;
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                    player.connection.teleport(120.5, 100, -74.5, -90, 0);
                    Arena.level(player);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 30);
                    Cultivation.setTizhiDouble(player, "LevelNumberCorrect", 0);
                    Skills.openVoidRift(player);
                    expect("境界不足时不生成", 0, nearby(player).size());
                }),
                new Action(20, player -> {
                    Cultivation.setTizhiDouble(player, "LevelNumberCorrect", 1);
                    Skills.openVoidRift(player);
                    VoidRift first = nearby(player).get(0);
                    expectTrue("体质修正达到门槛，按视线方块坐标与反向生成",
                            first.position().equals(new Vec3(125, 101, -75)) && first.getYRot() == 90 && first.isNoGravity());
                    distant = spawn(player.serverLevel(), new Vec3(150, 101, -75));
                    Skills.openVoidRift(player);
                    entrance = nearby(player).get(0);
                    expectTrue("只替换目标周围八格内的裂隙", first.isRemoved() && !distant.isRemoved());
                    entrance.getPersistentData().putDouble("xiaoshitick", 30);
                    entrance.baseTick();
                    entrance.playerTouch(player);
                    expectTrue("31 刻时碰撞不能进入，也不消耗裂隙", player.level().dimension().equals(Level.OVERWORLD)
                            && !entrance.getPersistentData().getBoolean("shifouyichuanyve"));
                    entrance.baseTick();
                    entrance.playerTouch(player);
                    expectTrue("32 刻进入：裂隙三轴坐标除五十并四舍五入，保存原维度",
                            player.level().dimension().equals(VOID) && player.position().equals(new Vec3(3, 2, -1))
                                    && Cultivation.tizhiDouble(player, "weidu") == 1);
                    entrance.playerTouch(player);
                    expectTrue("已使用的裂隙不能再次传送", player.level().dimension().equals(VOID)
                            && Cultivation.tizhiDouble(player, "weidu") == 1);
                }),
                new Action(20, player -> {
                    exit = spawn(player.serverLevel(), new Vec3(3, 2, -1));
                    exit.getPersistentData().putBoolean("keyijinru", true);
                    // 玩家与裂隙重叠，下一服务端刻由 Player 的真实碰撞入口触发返回。
                }),
                new Action(12, player -> {
                    expectTrue("碰撞返回原维度：三轴乘五十并清空返回编号", player.level().dimension().equals(Level.OVERWORLD)
                            && player.position().equals(new Vec3(150, 100, -50)) && Cultivation.tizhiDouble(player, "weidu") == 0);
                    player.teleportTo(player.server.getLevel(VOID), 0, 100, 0, 0, 0);
                    Cultivation.setTizhiDouble(player, "weidu", 6);
                    VoidRift missing = spawn(player.serverLevel(), new Vec3(5, 100, 5));
                    missing.getPersistentData().putBoolean("keyijinru", true);
                    missing.playerTouch(player);
                    expectTrue("目标维度未迁移时不传送，仍按来源清编号并消耗裂隙", player.level().dimension().equals(VOID)
                            && Cultivation.tizhiDouble(player, "weidu") == 0 && missing.getPersistentData().getBoolean("shifouyichuanyve"));
                    expectTrue("拒绝玩家、闪电与仙人掌伤害", !missing.hurt(player.damageSources().playerAttack(player), 1)
                            && !missing.hurt(player.damageSources().lightningBolt(), 1) && !missing.hurt(player.damageSources().cactus(), 1));
                    missing.getPersistentData().putDouble("xiaoshitick", 77);
                    CompoundTag saved = missing.saveWithoutId(new CompoundTag());
                    VoidRift restored = SkillSetup.<VoidRift>entityType(VoidRift.ID).get().create(player.serverLevel());
                    restored.load(saved);
                    expectTrue("保存重读保留年龄、可进入和已使用状态", restored.getPersistentData().getDouble("xiaoshitick") == 77
                            && restored.getPersistentData().getBoolean("keyijinru") && restored.getPersistentData().getBoolean("shifouyichuanyve"));
                    restored.getPersistentData().putDouble("xiaoshitick", 330);
                    restored.baseTick();
                    expectTrue("331 刻仍存续", !restored.isRemoved());
                    restored.baseTick();
                    expectTrue("332 刻移除并复位计数", restored.isRemoved() && restored.getPersistentData().getDouble("xiaoshitick") == 0);
                    missing.discard();
                })
        );
    }

    private static List<VoidRift> nearby(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(VoidRift.class, new AABB(new Vec3(125, 101, -75), new Vec3(125, 101, -75)).inflate(8));
    }

    private static VoidRift spawn(ServerLevel level, Vec3 at) {
        VoidRift rift = SkillSetup.<VoidRift>entityType(VoidRift.ID).get().create(level);
        rift.moveTo(at.x, at.y, at.z, 0, 0);
        level.addFreshEntity(rift);
        return rift;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        entrance.discard();
        distant.discard();
        exit.discard();
        player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, originalRank);
        Cultivation.setTizhiDouble(player, "LevelNumberCorrect", originalCorrection);
        Cultivation.setTizhiDouble(player, "weidu", originalReturn);
        player.getAbilities().flying = originalFlying;
        player.onUpdateAbilities();
    }
}
