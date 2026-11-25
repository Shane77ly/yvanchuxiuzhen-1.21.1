package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 修者来源的随机身高、资质、技能池、等级名与属性公式。 */
final class Xiuzhe0101Stats {
    private Xiuzhe0101Stats() { }

    /** 来源Xiuzhe0101ShiTiChuShiShengChengShiProcedure，保持条件、数值与阶段顺序。 */
    static void initialize(LevelAccessor world, Entity entity) {

        if ((EntityAttributes.realmName(entity)).equals("")) {
            if (EntityAttributes.level(entity) < 1) {
                EntityAttributes.setLevel(entity, (Xiuzhe0101Support.randomInt(1, 5)));
            }
            randomHeight(entity);
            skillList(entity);
            randomRatings(entity);
            levelName(entity);
            calculate(world, entity);
            String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
            Relations.setTeam(entity, team);
            Relations.addFriendlyTeam(entity, team);
            ((LivingEntity) entity).getAttribute(YaoshouAttributes.SKIN).setBaseValue((Xiuzhe0101Support.randomInt(1, 5)));
            ((Xiuzhe0101Entity) entity).restoreSkin();
        }
    }

    /** 来源XiuzheshengaoProcedure，保持条件、数值与阶段顺序。 */
    static void randomHeight(Entity entity) {

        double linshinum = 0;
        linshinum = Xiuzhe0101Support.randomDouble(0.8, 1.2);
        ((LivingEntity) entity).getAttribute(YaoshouAttributes.SIZE).setBaseValue(linshinum);
    }

    /** 来源XiuzheshuxingsuijiProcedure，保持条件、数值与阶段顺序。 */
    static void randomRatings(Entity entity) {

        double suijishu = 0;
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.HEALTH_MAX, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.HEALTH_MAX, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.HEALTH_MAX, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.HEALTH_MAX, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.HEALTH_MAX, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.HEALTH_REC, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.HEALTH_REC, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.HEALTH_REC, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.HEALTH_REC, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.HEALTH_REC, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.POWER_MAX, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.POWER_MAX, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.POWER_MAX, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.POWER_MAX, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.POWER_MAX, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.POWER_REC, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.POWER_REC, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.POWER_REC, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.POWER_REC, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.POWER_REC, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.SOUL_MAX, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.SOUL_MAX, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.SOUL_MAX, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.SOUL_MAX, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.SOUL_MAX, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.SOUL_REC, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.SOUL_REC, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.SOUL_REC, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.SOUL_REC, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.SOUL_REC, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.ATTACK, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.ATTACK, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.ATTACK, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.ATTACK, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.ATTACK, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.ATTACK_DEF, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.ATTACK_DEF, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.ATTACK_DEF, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.ATTACK_DEF, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.ATTACK_DEF, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.MANA_ATT, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.MANA_ATT, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.MANA_ATT, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.MANA_ATT, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.MANA_ATT, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.MANA_DEF, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.MANA_DEF, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.MANA_DEF, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.MANA_DEF, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.MANA_DEF, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.SOUL_ATT, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.SOUL_ATT, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.SOUL_ATT, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.SOUL_ATT, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.SOUL_ATT, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.SOUL_DEF, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.SOUL_DEF, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.SOUL_DEF, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.SOUL_DEF, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.SOUL_DEF, "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            EntityAttributes.setRating(entity, Stat.MOVE_SPEED, "绝佳");
        } else if (suijishu <= 15) {
            EntityAttributes.setRating(entity, Stat.MOVE_SPEED, "强劲");
        } else if (suijishu <= 30) {
            EntityAttributes.setRating(entity, Stat.MOVE_SPEED, "平庸");
        } else if (suijishu <= 50) {
            EntityAttributes.setRating(entity, Stat.MOVE_SPEED, "勉强");
        } else {
            EntityAttributes.setRating(entity, Stat.MOVE_SPEED, "贫弱");
        }
    }

    /** 来源NpclevelxianshiProcedure，保持条件、数值与阶段顺序。 */
    static void levelName(Entity entity) {

        if (EntityAttributes.level(entity) >= 46) {
            if (EntityAttributes.level(entity) >= 50) {
                EntityAttributes.setRealmName(entity, "化神圆满");
            } else if (EntityAttributes.level(entity) >= 49) {
                EntityAttributes.setRealmName(entity, "化神九级");
            } else if (EntityAttributes.level(entity) >= 48) {
                EntityAttributes.setRealmName(entity, "化神八级");
            } else if (EntityAttributes.level(entity) >= 47) {
                EntityAttributes.setRealmName(entity, "化神七级");
            } else {
                EntityAttributes.setRealmName(entity, "化神六级");
            }
        } else if (EntityAttributes.level(entity) >= 41) {
            if (EntityAttributes.level(entity) >= 45) {
                EntityAttributes.setRealmName(entity, "化神五级");
            } else if (EntityAttributes.level(entity) >= 44) {
                EntityAttributes.setRealmName(entity, "化神四级");
            } else if (EntityAttributes.level(entity) >= 43) {
                EntityAttributes.setRealmName(entity, "化神三级");
            } else if (EntityAttributes.level(entity) >= 42) {
                EntityAttributes.setRealmName(entity, "化神二级");
            } else {
                EntityAttributes.setRealmName(entity, "化神一级");
            }
        } else if (EntityAttributes.level(entity) >= 36) {
            if (EntityAttributes.level(entity) >= 40) {
                EntityAttributes.setRealmName(entity, "元婴圆满");
            } else if (EntityAttributes.level(entity) >= 39) {
                EntityAttributes.setRealmName(entity, "元婴九重");
            } else if (EntityAttributes.level(entity) >= 38) {
                EntityAttributes.setRealmName(entity, "元婴八重");
            } else if (EntityAttributes.level(entity) >= 37) {
                EntityAttributes.setRealmName(entity, "元婴七重");
            } else {
                EntityAttributes.setRealmName(entity, "元婴六重");
            }
        } else if (EntityAttributes.level(entity) >= 31) {
            if (EntityAttributes.level(entity) >= 35) {
                EntityAttributes.setRealmName(entity, "元婴五重");
            } else if (EntityAttributes.level(entity) >= 34) {
                EntityAttributes.setRealmName(entity, "元婴四重");
            } else if (EntityAttributes.level(entity) >= 33) {
                EntityAttributes.setRealmName(entity, "元婴三重");
            } else if (EntityAttributes.level(entity) >= 32) {
                EntityAttributes.setRealmName(entity, "元婴二重");
            } else {
                EntityAttributes.setRealmName(entity, "元婴一重");
            }
        } else if (EntityAttributes.level(entity) >= 26) {
            if (EntityAttributes.level(entity) >= 30) {
                EntityAttributes.setRealmName(entity, "结丹圆满");
            } else if (EntityAttributes.level(entity) >= 29) {
                EntityAttributes.setRealmName(entity, "结丹九转");
            } else if (EntityAttributes.level(entity) >= 28) {
                EntityAttributes.setRealmName(entity, "结丹八转");
            } else if (EntityAttributes.level(entity) >= 27) {
                EntityAttributes.setRealmName(entity, "结丹七转");
            } else {
                EntityAttributes.setRealmName(entity, "结丹六转");
            }
        } else if (EntityAttributes.level(entity) >= 21) {
            if (EntityAttributes.level(entity) >= 25) {
                EntityAttributes.setRealmName(entity, "结丹五转");
            } else if (EntityAttributes.level(entity) >= 24) {
                EntityAttributes.setRealmName(entity, "结丹四转");
            } else if (EntityAttributes.level(entity) >= 23) {
                EntityAttributes.setRealmName(entity, "结丹三转");
            } else if (EntityAttributes.level(entity) >= 22) {
                EntityAttributes.setRealmName(entity, "结丹二转");
            } else {
                EntityAttributes.setRealmName(entity, "结丹一转");
            }
        } else if (EntityAttributes.level(entity) >= 16) {
            if (EntityAttributes.level(entity) >= 20) {
                EntityAttributes.setRealmName(entity, "筑基圆满");
            } else if (EntityAttributes.level(entity) >= 19) {
                EntityAttributes.setRealmName(entity, "筑基九阶");
            } else if (EntityAttributes.level(entity) >= 18) {
                EntityAttributes.setRealmName(entity, "筑基八阶");
            } else if (EntityAttributes.level(entity) >= 17) {
                EntityAttributes.setRealmName(entity, "筑基七阶");
            } else {
                EntityAttributes.setRealmName(entity, "筑基六阶");
            }
        } else if (EntityAttributes.level(entity) >= 11) {
            if (EntityAttributes.level(entity) >= 15) {
                EntityAttributes.setRealmName(entity, "筑基五阶");
            } else if (EntityAttributes.level(entity) >= 14) {
                EntityAttributes.setRealmName(entity, "筑基四阶");
            } else if (EntityAttributes.level(entity) >= 13) {
                EntityAttributes.setRealmName(entity, "筑基三阶");
            } else if (EntityAttributes.level(entity) >= 12) {
                EntityAttributes.setRealmName(entity, "筑基二阶");
            } else {
                EntityAttributes.setRealmName(entity, "筑基一阶");
            }
        } else if (EntityAttributes.level(entity) >= 6) {
            if (EntityAttributes.level(entity) >= 10) {
                EntityAttributes.setRealmName(entity, "练气圆满");
            } else if (EntityAttributes.level(entity) >= 9) {
                EntityAttributes.setRealmName(entity, "练气九层");
            } else if (EntityAttributes.level(entity) >= 8) {
                EntityAttributes.setRealmName(entity, "练气八层");
            } else if (EntityAttributes.level(entity) >= 7) {
                EntityAttributes.setRealmName(entity, "练气七层");
            } else {
                EntityAttributes.setRealmName(entity, "练气六层");
            }
        } else {
            if (EntityAttributes.level(entity) >= 5) {
                EntityAttributes.setRealmName(entity, "练气五层");
            } else if (EntityAttributes.level(entity) >= 4) {
                EntityAttributes.setRealmName(entity, "练气四层");
            } else if (EntityAttributes.level(entity) >= 3) {
                EntityAttributes.setRealmName(entity, "练气三层");
            } else if (EntityAttributes.level(entity) >= 2) {
                EntityAttributes.setRealmName(entity, "练气二层");
            } else if (EntityAttributes.level(entity) >= 1) {
                EntityAttributes.setRealmName(entity, "练气一层");
            } else {
                EntityAttributes.setRealmName(entity, "凡人");
            }
        }
    }

    /** 来源NpcshuxingjisuanProcedure，保持条件、数值与阶段顺序。 */
    static void calculate(LevelAccessor world, Entity entity) {

        double linshinum = 0;
        if ((EntityAttributes.rating(entity, Stat.HEALTH_MAX)).equals("绝佳")) {
            linshinum = 14;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_MAX)).equals("强劲")) {
            linshinum = 13;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_MAX)).equals("平庸")) {
            linshinum = 12;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_MAX)).equals("勉强")) {
            linshinum = 11;
        } else {
            linshinum = 10;
        }
        EntityAttributes.setStat(entity, Stat.HEALTH_MAX, Math.ceil(100 + linshinum * Math.pow(EntityAttributes.level(entity) / 4, 2)));
        SkillDelays.after(20, () -> {
            ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(EntityAttributes.stat(entity, Stat.HEALTH_MAX));
            ((LivingEntity) entity).setHealth((float) EntityAttributes.stat(entity, Stat.HEALTH_MAX));
        });
        if ((EntityAttributes.rating(entity, Stat.HEALTH_REC)).equals("绝佳")) {
            linshinum = 8.4;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_REC)).equals("强劲")) {
            linshinum = 8.8;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_REC)).equals("平庸")) {
            linshinum = 9.2;
        } else if ((EntityAttributes.rating(entity, Stat.HEALTH_REC)).equals("勉强")) {
            linshinum = 9.6;
        } else {
            linshinum = 10;
        }
        EntityAttributes.setStat(entity, Stat.HEALTH_REC, Math.ceil(1 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.POWER_MAX)).equals("绝佳")) {
            linshinum = 28;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_MAX)).equals("强劲")) {
            linshinum = 26;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_MAX)).equals("平庸")) {
            linshinum = 24;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_MAX)).equals("勉强")) {
            linshinum = 22;
        } else {
            linshinum = 20;
        }
        EntityAttributes.setStat(entity, Stat.POWER_MAX, Math.ceil(120 + linshinum * Math.pow(EntityAttributes.level(entity) / 3, 2)));
        if ((EntityAttributes.rating(entity, Stat.POWER_REC)).equals("绝佳")) {
            linshinum = 6;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_REC)).equals("强劲")) {
            linshinum = 6.5;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_REC)).equals("平庸")) {
            linshinum = 7;
        } else if ((EntityAttributes.rating(entity, Stat.POWER_REC)).equals("勉强")) {
            linshinum = 7.5;
        } else {
            linshinum = 8;
        }
        EntityAttributes.setStat(entity, Stat.POWER_REC, Math.ceil(Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.SOUL_MAX)).equals("绝佳")) {
            linshinum = 28;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_MAX)).equals("强劲")) {
            linshinum = 26;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_MAX)).equals("平庸")) {
            linshinum = 24;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_MAX)).equals("勉强")) {
            linshinum = 22;
        } else {
            linshinum = 20;
        }
        EntityAttributes.setStat(entity, Stat.SOUL_MAX, Math.ceil(120 + linshinum * Math.pow(EntityAttributes.level(entity) / 3, 2)));
        if ((EntityAttributes.rating(entity, Stat.SOUL_REC)).equals("绝佳")) {
            linshinum = 6;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_REC)).equals("强劲")) {
            linshinum = 6.5;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_REC)).equals("平庸")) {
            linshinum = 7;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_REC)).equals("勉强")) {
            linshinum = 7.5;
        } else {
            linshinum = 8;
        }
        EntityAttributes.setStat(entity, Stat.SOUL_REC, Math.ceil(Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.ATTACK)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.ATTACK, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((EntityAttributes.stat(entity, Stat.ATTACK)));
        if ((EntityAttributes.rating(entity, Stat.ATTACK_DEF)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK_DEF)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK_DEF)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.ATTACK_DEF)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.ATTACK_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.MANA_ATT)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_ATT)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_ATT)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_ATT)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.MANA_ATT, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.MANA_DEF)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_DEF)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_DEF)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.MANA_DEF)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.MANA_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.SOUL_ATT)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_ATT)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_ATT)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_ATT)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.SOUL_ATT, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.SOUL_DEF)).equals("绝佳")) {
            linshinum = 3;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_DEF)).equals("强劲")) {
            linshinum = 3.2;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_DEF)).equals("平庸")) {
            linshinum = 3.4;
        } else if ((EntityAttributes.rating(entity, Stat.SOUL_DEF)).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        EntityAttributes.setStat(entity, Stat.SOUL_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / linshinum, 2)));
        if ((EntityAttributes.rating(entity, Stat.MOVE_SPEED)).equals("绝佳")) {
            linshinum = 0.28;
        } else if ((EntityAttributes.rating(entity, Stat.MOVE_SPEED)).equals("强劲")) {
            linshinum = 0.26;
        } else if ((EntityAttributes.rating(entity, Stat.MOVE_SPEED)).equals("平庸")) {
            linshinum = 0.24;
        } else if ((EntityAttributes.rating(entity, Stat.MOVE_SPEED)).equals("勉强")) {
            linshinum = 0.22;
        } else {
            linshinum = 0.2;
        }
        EntityAttributes.setStat(entity, Stat.MOVE_SPEED, (linshinum + EntityAttributes.level(entity) * 0.01));
        ((LivingEntity) entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((EntityAttributes.stat(entity, Stat.MOVE_SPEED)));
    }

    /** 来源Xiuzhe0101jinengliebiaoProcedure，保持条件、数值与阶段顺序。 */
    static void skillList(Entity entity) {

        double suijinum = 0;
        double skillnum = 0;
        String skillname = "";
        skillnum = 1;
        for (int index0 = 0; index0 < (int) (5); index0++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 10);
            if (suijinum == 1) {
                skillname = "直拳";
            } else if (suijinum == 2) {
                skillname = "炮拳";
            } else if (suijinum == 3) {
                skillname = "冲拳";
            } else if (suijinum == 4) {
                skillname = "连续出拳";
            } else if (suijinum == 5) {
                skillname = "旋风拳";
            } else if (suijinum == 6) {
                skillname = "闷拳";
            } else if (suijinum == 7) {
                skillname = "寸拳";
            } else if (suijinum == 8) {
                skillname = "碎骨拳";
            } else if (suijinum == 9) {
                skillname = "蓄力重拳";
            } else if (suijinum == 10) {
                skillname = "上勾拳";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
        for (int index1 = 0; index1 < (int) (5); index1++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 6);
            if (suijinum == 1) {
                skillname = "下勾拳";
            } else if (suijinum == 2) {
                skillname = "碎金拳";
            } else if (suijinum == 3) {
                skillname = "幽海拳";
            } else if (suijinum == 4) {
                skillname = "荆棘拳";
            } else if (suijinum == 5) {
                skillname = "烈焰拳";
            } else if (suijinum == 6) {
                skillname = "碎石拳";
            } else if (suijinum == 7) {
                skillname = "";
            } else if (suijinum == 8) {
                skillname = "";
            } else if (suijinum == 9) {
                skillname = "";
            } else if (suijinum == 10) {
                skillname = "";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
        for (int index2 = 0; index2 < (int) (5); index2++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 10);
            if (suijinum == 1) {
                skillname = "直拳";
            } else if (suijinum == 2) {
                skillname = "炮拳";
            } else if (suijinum == 3) {
                skillname = "冲拳";
            } else if (suijinum == 4) {
                skillname = "连续出拳";
            } else if (suijinum == 5) {
                skillname = "旋风拳";
            } else if (suijinum == 6) {
                skillname = "闷拳";
            } else if (suijinum == 7) {
                skillname = "寸拳";
            } else if (suijinum == 8) {
                skillname = "碎骨拳";
            } else if (suijinum == 9) {
                skillname = "蓄力重拳";
            } else if (suijinum == 10) {
                skillname = "上勾拳";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
        for (int index3 = 0; index3 < (int) (5); index3++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 10);
            if (suijinum == 1) {
                skillname = "爆燃符";
            } else if (suijinum == 2) {
                skillname = "碎金拳";
            } else if (suijinum == 3) {
                skillname = "幽海拳";
            } else if (suijinum == 4) {
                skillname = "荆棘拳";
            } else if (suijinum == 5) {
                skillname = "烈焰拳";
            } else if (suijinum == 6) {
                skillname = "落雷符";
            } else if (suijinum == 7) {
                skillname = "剑符";
            } else if (suijinum == 8) {
                skillname = "强压符";
            } else if (suijinum == 9) {
                skillname = "流火符";
            } else if (suijinum == 10) {
                skillname = "风雷符";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
        for (int index4 = 0; index4 < (int) (5); index4++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 6);
            if (suijinum == 1) {
                skillname = "下勾拳";
            } else if (suijinum == 2) {
                skillname = "碎金拳";
            } else if (suijinum == 3) {
                skillname = "幽海拳";
            } else if (suijinum == 4) {
                skillname = "荆棘拳";
            } else if (suijinum == 5) {
                skillname = "烈焰拳";
            } else if (suijinum == 6) {
                skillname = "碎石拳";
            } else if (suijinum == 7) {
                skillname = "";
            } else if (suijinum == 8) {
                skillname = "";
            } else if (suijinum == 9) {
                skillname = "";
            } else if (suijinum == 10) {
                skillname = "";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
        for (int index5 = 0; index5 < (int) (5); index5++) {
            suijinum = Xiuzhe0101Support.randomInt(1, 10);
            if (suijinum == 1) {
                skillname = "爆燃符";
            } else if (suijinum == 2) {
                skillname = "碎金拳";
            } else if (suijinum == 3) {
                skillname = "幽海拳";
            } else if (suijinum == 4) {
                skillname = "荆棘拳";
            } else if (suijinum == 5) {
                skillname = "烈焰拳";
            } else if (suijinum == 6) {
                skillname = "落雷符";
            } else if (suijinum == 7) {
                skillname = "剑符";
            } else if (suijinum == 8) {
                skillname = "强压符";
            } else if (suijinum == 9) {
                skillname = "流火符";
            } else if (suijinum == 10) {
                skillname = "风雷符";
            }
            CreatureData.state(entity).setSkillChoice((int) (skillnum), skillname);
            skillnum = skillnum + 1;
        }
    }
}
