package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.fulu.content.TalismanBeast;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 修者来源技能对符箓系统的公开施放操作，由符箓系统写入持续效果与弹体数据。 */
public final class NpcTalismans {
    private NpcTalismans() { }

    /** 来源Npcskill0301的雷符实体：继承友方列表引用，品阶和主人在出生前写入。 */
    public static void summonThunderPull(ServerLevel level, Entity caster, Vec3 position, double grade) {
        TalismanBeast beast = FuluSetup.beast(TalismanBeast.THUNDER_PULL).get().create(level);
        beast.moveTo(position.x, position.y, position.z, level.getRandom().nextFloat() * 360F, 0);
        Relations.shareEntityAllegiance(caster, beast);
        beast.getPersistentData().putDouble("fululevel", grade);
        beast.getPersistentData().putString("belongToMasterUUID", caster.getStringUUID());
        beast.finalizeSpawn(level, level.getCurrentDifficultyAt(beast.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(beast);
    }

    /** 来源Npcskill0302启用持续喷火，同时增加正在生效的符箓计数。 */
    public static void activateFlameJet(Entity caster, double grade) {
        CompoundTag state = caster.getPersistentData();
        state.putBoolean("fuluxiaoguo02", true);
        state.putDouble("fulu02level", grade);
        state.putDouble("fulunumber", state.getDouble("fulunumber") + 1);
    }

    /** 来源Npcskill0303只设置风压标记与品阶，不增加正在生效的符箓计数。 */
    public static void activateWindPressure(Entity caster, double grade) {
        CompoundTag state = caster.getPersistentData();
        state.putBoolean("fuluxiaoguo03", true);
        state.putDouble("fulu03level", grade);
    }

    /** 来源Npcskill0304发射穿透十的气剑；箭首刻读取并清除施放者暂存品阶。 */
    public static void shootQiSword(ServerLevel level, Entity caster, double grade) {
        caster.getPersistentData().putDouble("fulu05level", grade);
        TalismanProjectile.launch(level, FuluSetup.projectile(TalismanProjectile.Kind.QI_SWORD).get(), caster,
                new Vec3(caster.getX(), caster.getEyeY() - 0.1, caster.getZ()), caster.getLookAngle());
    }
}
