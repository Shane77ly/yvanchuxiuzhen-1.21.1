package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 弹体粒子、保存、命中箭数与无主人落地的针对性检查，并截取来源专用模型。 */
final class SkillParticleCheck extends SystemCheck {
    private final Consumer<String> shot;
    private SkillProjectile spirit;
    private SkillProjectile gold;
    private SkillProjectile fire;
    private IronGolem target;
    private SkillProjectile ownerless;
    private final List<SkillProjectile> models = new ArrayList<>();
    private long launchedAt;
    private int before;

    SkillParticleCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("技能弹体粒子", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            before = libraryParticles();
            onServer(player -> {
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                Cultivation.setTizhiBoolean(player, "arrowskill", true);
                Cultivation.setTizhiBoolean(player, "manxvli", true);
                SkillProjectile arrow = SkillSetup.<SkillProjectile>entityType("projectile_skill_arrow_02").get().create(player.serverLevel());
                arrow.setOwner(player);
                arrow.setBaseDamage(8);
                arrow.getPersistentData().putBoolean("arrowAlt", true);
                arrow.setPos(player.position().add(1.5, 2, 5));
                player.serverLevel().addFreshEntity(arrow);
                spirit = arrow;
                SkillProjectile bolt = SkillSetup.<SkillProjectile>entityType("projectile_skill_gold_01").get().create(player.serverLevel());
                bolt.setOwner(player);
                bolt.setPos(player.position().add(-1.5, 2, 5));
                player.serverLevel().addFreshEntity(bolt);
                gold = bolt;
                persistence(player);
            });
            return true;
        }
        if (step == 2) {
            if (local < 40 || libraryParticles() > 0) {
                return false;
            }
            onServer(player -> {
                ownerless = SkillSetup.<SkillProjectile>entityType("projectile_skill_water_03").get().create(player.serverLevel());
                ownerless.setPos(player.position().add(0, 1.5, 5));
                ownerless.setNoGravity(true);
                ownerless.shoot(0, 0, 1, 1, 0);
                player.serverLevel().setBlock(BlockPos.containing(player.position().add(0, 1.5, 7)), Blocks.STONE.defaultBlockState(), 3);
                player.serverLevel().addFreshEntity(ownerless);
                String[] ids = {"projectile_skill_ice_01", "projectile_skill_ice_02", "projectile_skill_ice_03",
                        "projectile_lingwentexiao", "projectile_skill_tree_01"};
                for (int i = 0; i < ids.length; i++) {
                    SkillProjectile model = SkillSetup.<SkillProjectile>entityType(ids[i]).get().create(player.serverLevel());
                    model.setPos(player.position().add((i - 2) * 2, 2, 5));
                    model.setNoGravity(true);
                    player.serverLevel().addFreshEntity(model);
                    models.add(model);
                }
                launchedAt = player.serverLevel().getGameTime();
            });
            return true;
        }
        if (local < (step == 3 ? 5 : 8)) {
            return false;
        }
        long[] serverTime = {0};
        onServer(player -> serverTime[0] = player.serverLevel().getGameTime());
        if (serverTime[0] - launchedAt < (step == 3 ? 5 : 8)) {
            return false;
        }
        if (step == 3) {
            int particles = libraryParticles();
            if (particles == 0 && local < 40) {
                return false;
            }
            onServer(player -> {
                boolean inGround = ownerless.saveWithoutId(new CompoundTag()).getBoolean("inGround");
                expectTrue("无主人水球真实撞墙后仍广播来源落地粒子；移除=" + ownerless.isRemoved()
                        + "，入地=" + inGround + "，位置=" + ownerless.position() + "，自定义粒子数=" + particles
                        + "，服务端经过刻数=" + (player.serverLevel().getGameTime() - launchedAt), ownerless.isRemoved() && inGround && particles > 0);
            });
            shot.accept("skill-projectile-models.png");
            onServer(player -> models.forEach(SkillProjectile::discard));
            finish();
            return true;
        }
        expectTrue("弹体的自定义粒子从服务端广播到真实客户端提供器", libraryParticles() > before);
        onServer(player -> {
            expectTrue("第02箭不套用增伤模式且保留玩家施放标记", spirit.getBaseDamage() == 8
                    && spirit.getPersistentData().getString("jinengName").isEmpty()
                    && Cultivation.tizhiBoolean(player, "arrowskill") && Cultivation.tizhiBoolean(player, "manxvli"));
            expectTrue("共用火球弹体真实命中后不留下生物身上的箭", target.getHealth() < 100 && target.getArrowCount() == 0);
        });
        shot.accept("skill-projectile-particles.png");
        onServer(player -> {
            spirit.discard();
            gold.discard();
            fire.discard();
            target.discard();
        });
        minecraft.particleEngine.iterateParticles(particle -> particle.remove());
        return true;
    }

    private void persistence(ServerPlayer player) {
        target = EntityType.IRON_GOLEM.create(player.serverLevel());
        target.setNoAi(true);
        target.setPos(player.position().add(8, 0, 0));
        target.getPersistentData().putDouble("LEVELnumber", 17);
        player.serverLevel().addFreshEntity(target);

        SkillProjectile flower = SkillSetup.<SkillProjectile>entityType("projectile_skill_ice_03").get().create(player.serverLevel());
        flower.setOwner(player);
        flower.setPos(player.position().add(0, 10, 0));
        flower.launchedWith(3, 2);
        flower.begin();
        flower.snapshot(target, 1, 1, 1);
        flower.setSkillName("飞羽冰花");
        flower.setAngle(45);
        flower.setReversed(true);
        flower.markSaved();
        for (int i = 0; i < 3; i++) flower.advance();
        CompoundTag saved = flower.saveWithoutId(new CompoundTag());
        SkillProjectile restored = (SkillProjectile) flower.getType().create(player.serverLevel());
        restored.load(saved);
        expectTrue("弹体保存重读保留施放数值、计时、轨迹、初始化标记和原生穿透",
                restored.mana() == 17 && restored.multiplier() == 1 && restored.skillName().equals("飞羽冰花")
                        && restored.ticks() == 3 && restored.angle() == 45 && restored.reversed() && restored.saved()
                        && restored.getPierceLevel() == 2 && restored.isNoGravity());
        restored.tick();
        expectTrue("重读后飞羽冰花接续第三刻和左旋轨迹，保留伤害快照",
                restored.ticks() == 5 && restored.angle() == 25 && restored.mana() == 17 && restored.multiplier() == 1);

        fire = SkillSetup.<SkillProjectile>entityType("projectile_skill_fire_01").get().create(player.serverLevel());
        fire.setOwner(player);
        fire.setPos(player.position().add(3, 1, 0));
        fire.setBaseDamage(1);
        fire.begin();
        fire.snapshot(target, 1, 1, 1);
        fire.setSkillName("火球术");
        fire.shoot(1, 0, 0, 1, 0);
        player.serverLevel().addFreshEntity(fire);
        launchedAt = player.serverLevel().getGameTime();
    }

    private int libraryParticles() {
        int[] count = {0};
        minecraft.particleEngine.iterateParticles(particle -> {
            if (particle.getClass().getName().equals("net.yvanchuxiuzhen.system.particle.client.LibraryParticle")) {
                count[0]++;
            }
        });
        return count[0];
    }
}
