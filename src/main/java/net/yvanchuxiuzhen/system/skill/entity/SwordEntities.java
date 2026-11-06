package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 剑法的剑影实体与四种剑法的出手（来源 {@code Swordsmanship01DangShiTiGengXinKeShi} 与 {@code Swordsmanship01chufa}–{@code 04chufa}）：
 * 中平剑法、柔云剑法、拜年剑法、弱鸡剑法。出手时在挥剑者处生成对应的剑影实体，按剑法的第几式播放动画并结算范围效果；
 * 剑影实体本身只播放动画，存在 13 刻后消失。范围效果的敌我判定与来源一致，比较两者的团队名，名字不同即受影响，包括非生物实体。
 */
public final class SwordEntities {
    private static final String[] ARTS = {"中平剑法", "柔云剑法", "拜年剑法", "弱鸡剑法"};
    private static final int LIFETIME = 13;

    private SwordEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        for (int index = 1; index <= ARTS.length; index++) {
            skills.put(id(index), new SwordShadow());
        }
    }

    /** 剑法名对应的序号（1–4），不是四种剑法之一时为 0。 */
    public static int artIndex(String name) {
        for (int index = 0; index < ARTS.length; index++) {
            if (ARTS[index].equals(name)) {
                return index + 1;
            }
        }
        return 0;
    }

    /**
     * 第 {@code step} 式出手：生成剑影并播放动画，再按剑法与式数结算效果。
     * 中平剑法第 3 式把视线 2 格处 1.5 格内的非同队实体挑起，第 5 式伤害 3 格内的非同队实体（攻击力的 100%），第 6 式挥剑者跃起并伤害视线处 2 格内的目标（150%）；
     * 柔云剑法第 1 式把视线处 1.5 格内的非同队实体震开，第 2、4 式伤害身边 2 格内的目标（70%），第 6 式挑起视线处的目标；
     * 拜年剑法第 1 式伤害视线处 2 格内的目标（50%）；弱鸡剑法每一式都挑起视线处 1.5 格内的非同队实体。
     */
    public static void perform(ServerPlayer player, int art, int step) {
        ServerLevel level = (ServerLevel) player.level();
        double height = art == 1 ? 1 : 1.5;
        SkillEntity shadow = SkillEntities.summon(level, player, id(art), "", 0, 0, ARTS[art - 1]);
        shadow.moveTo(player.getX(), player.getY() + height, player.getZ(), player.getYRot(), player.getXRot());
        shadow.setYBodyRot(player.getYRot());
        shadow.setYHeadRot(player.getYRot());
        SkillEntities.spawn(level, shadow, true);
        Vec3 chest = new Vec3(player.getX(), player.getY() + height, player.getZ());
        for (Entity other : ProjectileSupport.around(level, chest, 0.5)) {
            if (SkillEntities.is(other, id(art)) && other instanceof SkillEntity sword) {
                sword.setAnimation("animation.swordsmanship0" + art + ".skill" + step);
            }
        }
        Vec3 aim = Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(player, 2));
        switch (art) {
            case 1 -> {
                if (step == 3) {
                    launch(level, player, aim, 1.5);
                } else if (step == 5) {
                    strike(level, player, player.position(), 3, 1);
                } else if (step == 6) {
                    hop(player);
                    strike(level, player, aim, 2, 1.5);
                }
            }
            case 2 -> {
                if (step == 1) {
                    blast(level, player, aim, 1.5);
                } else if (step == 2 || step == 4) {
                    strike(level, player, player.position(), 2, 0.7);
                } else if (step == 6) {
                    launch(level, player, aim, 1.5);
                }
            }
            case 3 -> {
                if (step == 1) {
                    strike(level, player, aim, 2, 0.5);
                }
            }
            default -> launch(level, player, aim, 1.5);
        }
    }

    private static String id(int art) {
        return "swordsmanship_0" + art;
    }

    /** 非同队（团队名不同）的实体，来源比较实体 {@code getPersistentData} 里的 {@code team}，玩家的值与其团队一致。 */
    private static boolean hostile(Entity player, Entity other) {
        return !Relations.team(other).equals(Relations.team(player));
    }

    /** 挑起：把范围内的非同队实体速度设为向上 0.6。 */
    private static void launch(ServerLevel level, ServerPlayer player, Vec3 center, double radius) {
        for (Entity other : ProjectileSupport.around(level, center, radius)) {
            if (hostile(player, other)) {
                other.setDeltaMovement(new Vec3(0, 0.6, 0));
            }
        }
    }

    /** 震开：范围内的非同队、非挥剑者本人的实体沿远离挥剑者的方向被推出，并向上 0.5。 */
    private static void blast(ServerLevel level, ServerPlayer player, Vec3 center, double radius) {
        for (Entity other : ProjectileSupport.around(level, center, radius)) {
            if (hostile(player, other) && other != player) {
                other.setDeltaMovement(new Vec3((other.getX() - player.getX()) * 0.4, 0.5, (other.getZ() - player.getZ()) * 0.4));
            }
        }
    }

    /** 范围伤害：范围内的非同队实体受到挥剑者攻击力属性乘以倍率的伤害。 */
    private static void strike(ServerLevel level, ServerPlayer player, Vec3 center, double radius, double ratio) {
        float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * ratio);
        for (Entity other : ProjectileSupport.around(level, center, radius)) {
            if (hostile(player, other)) {
                other.hurt(level.damageSources().playerAttack(player), damage);
            }
        }
    }

    /** 来源在挥剑者所在的一侧把速度设为向上 0.4；玩家的速度由客户端决定，这里通知客户端采用。 */
    private static void hop(ServerPlayer player) {
        player.setDeltaMovement(new Vec3(0, 0.4, 0));
        player.hurtMarked = true;
    }

    /** 剑影：第 13 刻消失。 */
    private static final class SwordShadow implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= LIFETIME) {
                self.discard();
            }
        }
    }
}
