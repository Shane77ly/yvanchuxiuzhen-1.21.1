package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 修者来源的随机身高、资质、技能池、等级名与属性公式。 */
final class Xiuzhe0101Stats {
    private Xiuzhe0101Stats() { }

    /** 来源Xiuzhe0101ShiTiChuShiShengChengShiProcedure，保持条件、数值与阶段顺序。 */
    static void initialize(LevelAccessor world, Entity entity) {

        if ((entity.getPersistentData().getString("LEVELname")).equals("")) {
            if (entity.getPersistentData().getDouble("LEVELnumber") < 1) {
                entity.getPersistentData().putDouble("LEVELnumber", (Xiuzhe0101Support.randomInt(1, 5)));
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
            entity.getPersistentData().putString("health_Max_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("health_Max_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("health_Max_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("health_Max_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("health_Max_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("health_Rec_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("health_Rec_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("health_Rec_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("health_Rec_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("health_Rec_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("power_Max_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("power_Max_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("power_Max_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("power_Max_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("power_Max_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("power_Rec_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("power_Rec_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("power_Rec_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("power_Rec_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("power_Rec_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("soul_Max_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("soul_Max_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("soul_Max_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("soul_Max_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("soul_Max_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("soul_Rec_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("soul_Rec_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("soul_Rec_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("soul_Rec_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("soul_Rec_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("attack_Num_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("attack_Num_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("attack_Num_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("attack_Num_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("attack_Num_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("attack_Def_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("attack_Def_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("attack_Def_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("attack_Def_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("attack_Def_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("mana_Att_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("mana_Att_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("mana_Att_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("mana_Att_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("mana_Att_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("mana_Def_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("mana_Def_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("mana_Def_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("mana_Def_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("mana_Def_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("soul_Att_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("soul_Att_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("soul_Att_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("soul_Att_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("soul_Att_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("soul_Def_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("soul_Def_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("soul_Def_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("soul_Def_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("soul_Def_LEVEL", "贫弱");
        }
        suijishu = Xiuzhe0101Support.randomInt(1, 100);
        if (suijishu <= 5) {
            entity.getPersistentData().putString("speed_LEVEL", "绝佳");
        } else if (suijishu <= 15) {
            entity.getPersistentData().putString("speed_LEVEL", "强劲");
        } else if (suijishu <= 30) {
            entity.getPersistentData().putString("speed_LEVEL", "平庸");
        } else if (suijishu <= 50) {
            entity.getPersistentData().putString("speed_LEVEL", "勉强");
        } else {
            entity.getPersistentData().putString("speed_LEVEL", "贫弱");
        }
    }

    /** 来源NpclevelxianshiProcedure，保持条件、数值与阶段顺序。 */
    static void levelName(Entity entity) {

        if (entity.getPersistentData().getDouble("LEVELnumber") >= 46) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 50) {
                entity.getPersistentData().putString("LEVELname", "化神圆满");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 49) {
                entity.getPersistentData().putString("LEVELname", "化神九级");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 48) {
                entity.getPersistentData().putString("LEVELname", "化神八级");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 47) {
                entity.getPersistentData().putString("LEVELname", "化神七级");
            } else {
                entity.getPersistentData().putString("LEVELname", "化神六级");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 41) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 45) {
                entity.getPersistentData().putString("LEVELname", "化神五级");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 44) {
                entity.getPersistentData().putString("LEVELname", "化神四级");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 43) {
                entity.getPersistentData().putString("LEVELname", "化神三级");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 42) {
                entity.getPersistentData().putString("LEVELname", "化神二级");
            } else {
                entity.getPersistentData().putString("LEVELname", "化神一级");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 36) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 40) {
                entity.getPersistentData().putString("LEVELname", "元婴圆满");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 39) {
                entity.getPersistentData().putString("LEVELname", "元婴九重");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 38) {
                entity.getPersistentData().putString("LEVELname", "元婴八重");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 37) {
                entity.getPersistentData().putString("LEVELname", "元婴七重");
            } else {
                entity.getPersistentData().putString("LEVELname", "元婴六重");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 31) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 35) {
                entity.getPersistentData().putString("LEVELname", "元婴五重");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 34) {
                entity.getPersistentData().putString("LEVELname", "元婴四重");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 33) {
                entity.getPersistentData().putString("LEVELname", "元婴三重");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 32) {
                entity.getPersistentData().putString("LEVELname", "元婴二重");
            } else {
                entity.getPersistentData().putString("LEVELname", "元婴一重");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 26) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 30) {
                entity.getPersistentData().putString("LEVELname", "结丹圆满");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 29) {
                entity.getPersistentData().putString("LEVELname", "结丹九转");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 28) {
                entity.getPersistentData().putString("LEVELname", "结丹八转");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 27) {
                entity.getPersistentData().putString("LEVELname", "结丹七转");
            } else {
                entity.getPersistentData().putString("LEVELname", "结丹六转");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 21) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 25) {
                entity.getPersistentData().putString("LEVELname", "结丹五转");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 24) {
                entity.getPersistentData().putString("LEVELname", "结丹四转");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 23) {
                entity.getPersistentData().putString("LEVELname", "结丹三转");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 22) {
                entity.getPersistentData().putString("LEVELname", "结丹二转");
            } else {
                entity.getPersistentData().putString("LEVELname", "结丹一转");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 16) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 20) {
                entity.getPersistentData().putString("LEVELname", "筑基圆满");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 19) {
                entity.getPersistentData().putString("LEVELname", "筑基九阶");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 18) {
                entity.getPersistentData().putString("LEVELname", "筑基八阶");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 17) {
                entity.getPersistentData().putString("LEVELname", "筑基七阶");
            } else {
                entity.getPersistentData().putString("LEVELname", "筑基六阶");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 11) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 15) {
                entity.getPersistentData().putString("LEVELname", "筑基五阶");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 14) {
                entity.getPersistentData().putString("LEVELname", "筑基四阶");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 13) {
                entity.getPersistentData().putString("LEVELname", "筑基三阶");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 12) {
                entity.getPersistentData().putString("LEVELname", "筑基二阶");
            } else {
                entity.getPersistentData().putString("LEVELname", "筑基一阶");
            }
        } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 6) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 10) {
                entity.getPersistentData().putString("LEVELname", "练气圆满");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 9) {
                entity.getPersistentData().putString("LEVELname", "练气九层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 8) {
                entity.getPersistentData().putString("LEVELname", "练气八层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 7) {
                entity.getPersistentData().putString("LEVELname", "练气七层");
            } else {
                entity.getPersistentData().putString("LEVELname", "练气六层");
            }
        } else {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 5) {
                entity.getPersistentData().putString("LEVELname", "练气五层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 4) {
                entity.getPersistentData().putString("LEVELname", "练气四层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 3) {
                entity.getPersistentData().putString("LEVELname", "练气三层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 2) {
                entity.getPersistentData().putString("LEVELname", "练气二层");
            } else if (entity.getPersistentData().getDouble("LEVELnumber") >= 1) {
                entity.getPersistentData().putString("LEVELname", "练气一层");
            } else {
                entity.getPersistentData().putString("LEVELname", "凡人");
            }
        }
    }

    /** 来源NpcshuxingjisuanProcedure，保持条件、数值与阶段顺序。 */
    static void calculate(LevelAccessor world, Entity entity) {

        double linshinum = 0;
        if ((entity.getPersistentData().getString("health_Max_LEVEL")).equals("绝佳")) {
            linshinum = 14;
        } else if ((entity.getPersistentData().getString("health_Max_LEVEL")).equals("强劲")) {
            linshinum = 13;
        } else if ((entity.getPersistentData().getString("health_Max_LEVEL")).equals("平庸")) {
            linshinum = 12;
        } else if ((entity.getPersistentData().getString("health_Max_LEVEL")).equals("勉强")) {
            linshinum = 11;
        } else {
            linshinum = 10;
        }
        entity.getPersistentData().putDouble("health_Max", Math.ceil(100 + linshinum * Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 4, 2)));
        SkillDelays.after(20, () -> {
            ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getPersistentData().getDouble("health_Max"));
            ((LivingEntity) entity).setHealth((float) entity.getPersistentData().getDouble("health_Max"));
        });
        if ((entity.getPersistentData().getString("health_Rec_LEVEL")).equals("绝佳")) {
            linshinum = 8.4;
        } else if ((entity.getPersistentData().getString("health_Rec_LEVEL")).equals("强劲")) {
            linshinum = 8.8;
        } else if ((entity.getPersistentData().getString("health_Rec_LEVEL")).equals("平庸")) {
            linshinum = 9.2;
        } else if ((entity.getPersistentData().getString("health_Rec_LEVEL")).equals("勉强")) {
            linshinum = 9.6;
        } else {
            linshinum = 10;
        }
        entity.getPersistentData().putDouble("health_Rec", Math.ceil(1 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("power_Max_LEVEL")).equals("绝佳")) {
            linshinum = 28;
        } else if ((entity.getPersistentData().getString("power_Max_LEVEL")).equals("强劲")) {
            linshinum = 26;
        } else if ((entity.getPersistentData().getString("power_Max_LEVEL")).equals("平庸")) {
            linshinum = 24;
        } else if ((entity.getPersistentData().getString("power_Max_LEVEL")).equals("勉强")) {
            linshinum = 22;
        } else {
            linshinum = 20;
        }
        entity.getPersistentData().putDouble("power_Max", Math.ceil(120 + linshinum * Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 3, 2)));
        if ((entity.getPersistentData().getString("power_Rec_LEVEL")).equals("绝佳")) {
            linshinum = 6;
        } else if ((entity.getPersistentData().getString("power_Rec_LEVEL")).equals("强劲")) {
            linshinum = 6.5;
        } else if ((entity.getPersistentData().getString("power_Rec_LEVEL")).equals("平庸")) {
            linshinum = 7;
        } else if ((entity.getPersistentData().getString("power_Rec_LEVEL")).equals("勉强")) {
            linshinum = 7.5;
        } else {
            linshinum = 8;
        }
        entity.getPersistentData().putDouble("power_Rec", Math.ceil(Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("soul_Max_LEVEL")).equals("绝佳")) {
            linshinum = 28;
        } else if ((entity.getPersistentData().getString("soul_Max_LEVEL")).equals("强劲")) {
            linshinum = 26;
        } else if ((entity.getPersistentData().getString("soul_Max_LEVEL")).equals("平庸")) {
            linshinum = 24;
        } else if ((entity.getPersistentData().getString("soul_Max_LEVEL")).equals("勉强")) {
            linshinum = 22;
        } else {
            linshinum = 20;
        }
        entity.getPersistentData().putDouble("soul_Max", Math.ceil(120 + linshinum * Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 3, 2)));
        if ((entity.getPersistentData().getString("soul_Rec_LEVEL")).equals("绝佳")) {
            linshinum = 6;
        } else if ((entity.getPersistentData().getString("soul_Rec_LEVEL")).equals("强劲")) {
            linshinum = 6.5;
        } else if ((entity.getPersistentData().getString("soul_Rec_LEVEL")).equals("平庸")) {
            linshinum = 7;
        } else if ((entity.getPersistentData().getString("soul_Rec_LEVEL")).equals("勉强")) {
            linshinum = 7.5;
        } else {
            linshinum = 8;
        }
        entity.getPersistentData().putDouble("soul_Rec", Math.ceil(Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("attack_Num_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("attack_Num_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("attack_Num_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("attack_Num_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("attack_Num", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((entity.getPersistentData().getDouble("attack_Num")));
        if ((entity.getPersistentData().getString("attack_Def_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("attack_Def_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("attack_Def_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("attack_Def_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("attack_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("mana_Att_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("mana_Att_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("mana_Att_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("mana_Att_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("mana_Att", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("mana_Def_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("mana_Def_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("mana_Def_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("mana_Def_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("mana_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("soul_Att_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("soul_Att_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("soul_Att_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("soul_Att_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("soul_Att", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("soul_Def_LEVEL")).equals("绝佳")) {
            linshinum = 3;
        } else if ((entity.getPersistentData().getString("soul_Def_LEVEL")).equals("强劲")) {
            linshinum = 3.2;
        } else if ((entity.getPersistentData().getString("soul_Def_LEVEL")).equals("平庸")) {
            linshinum = 3.4;
        } else if ((entity.getPersistentData().getString("soul_Def_LEVEL")).equals("勉强")) {
            linshinum = 3.6;
        } else {
            linshinum = 3.8;
        }
        entity.getPersistentData().putDouble("soul_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / linshinum, 2)));
        if ((entity.getPersistentData().getString("speed_LEVEL")).equals("绝佳")) {
            linshinum = 0.28;
        } else if ((entity.getPersistentData().getString("speed_LEVEL")).equals("强劲")) {
            linshinum = 0.26;
        } else if ((entity.getPersistentData().getString("speed_LEVEL")).equals("平庸")) {
            linshinum = 0.24;
        } else if ((entity.getPersistentData().getString("speed_LEVEL")).equals("勉强")) {
            linshinum = 0.22;
        } else {
            linshinum = 0.2;
        }
        entity.getPersistentData().putDouble("speed_number", (linshinum + entity.getPersistentData().getDouble("LEVELnumber") * 0.01));
        ((LivingEntity) entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((entity.getPersistentData().getDouble("speed_number")));
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
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
            entity.getPersistentData().putString(("skillxvanze" + Xiuzhe0101Support.whole(skillnum)), skillname);
            skillnum = skillnum + 1;
        }
    }
}
