package net.yvanchuxiuzhen.system.treasure;

import java.text.DecimalFormat;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** 1.18.2 版本的纯奖励表，由 tools/porting/extract_treasure_rewards.py 确定性生成。 */
final class TreasureRewardTables {
    private TreasureRewardTables() {
    }

    static String select(String category, String subtype, double level, RandomSource random) {
        return switch (category) {
            case "书架" -> switch (subtype) {
                case "书籍堆" -> table0101(level, random);
                case "书桶" -> table0102(level, random);
                case "符箓堆" -> tableFulu(level, random);
                case "丹方" -> tableLiandantuzhi(level, random);
                case "功法书" -> tableGongfa(level, random);
                case "技能书" -> tableJineng(level, random);
                default -> "";
            };
            case "药架" -> switch (subtype) {
                case "草药堆" -> tableCaoyao(level, random);
                case "丹药堆" -> tableDanyao(level, random);
                case "恢复类丹药堆" -> tableHuifudanyao(level, random);
                case "状态类丹药堆" -> tableZhuangtaidanyao(level, random);
                case "精进修为丹药堆" -> tableXiuweidanyao(level, random);
                default -> "";
            };
            case "武器架" -> switch (subtype) {
                case "武器架" -> tableWuqi(level, random);
                case "法宝架" -> tableFabao(level, random);
                case "盔甲架" -> tableHujia(level, random);
                case "饰品箱" -> tableShipin(level, random);
                case "装备" -> tableZhuangbei(level, random);
                default -> "";
            };
            case "宝箱" -> switch (subtype) {
                case "妖兽材料" -> tableYaoshoucailiao(level, random);
                case "异火" -> tableYihuocailiao(level, random);
                case "金属材料" -> tableJinshucailiao(level, random);
                case "灵石" -> tableLingshi(level, random);
                case "宝石" -> tableBaoshicailiao(level, random);
                case "妖丹" -> tableYaodancailiao(level, random);
                case "布匹" -> tableBupi(level, random);
                case "综合" -> tableCailiao(level, random);
                default -> "";
            };
            default -> "";
        };
    }

    /** 来源 B:Baoxiang0101Procedure，保留分支及随机边界。 */
    private static String table0101(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        suijizhi = Mth.nextInt(random, 1, 5);
        if (suijizhi == 1) {
            neirong = tableGongfa(level, random);
        } else if (suijizhi == 2) {
            neirong = tableJineng(level, random);
        } else if (suijizhi == 3) {
            neirong = tableShenfa(level, random);
        } else if (suijizhi == 4) {
            neirong = tableFlyskill(level, random);
        } else if (suijizhi == 5) {
            neirong = tableFulu(level, random);
        }
        return neirong;

    }

    /** 来源 B:Baoxiang0102Procedure，保留分支及随机边界。 */
    private static String table0102(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        suijizhi = Mth.nextInt(random, 1, 4);
        if (suijizhi == 1) {
            neirong = tableGongfa(level, random);
        } else if (suijizhi == 2) {
            neirong = tableJineng(level, random);
        } else if (suijizhi == 3) {
            neirong = tableShenfa(level, random);
        } else if (suijizhi == 4) {
            neirong = tableFlyskill(level, random);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangBaoshicailiaoProcedure，保留分支及随机边界。 */
    private static String tableBaoshicailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaopinjie = 0;
        double cailiaolevel = 0;
        if (level <= 1) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_161%无";
            cailiaolevel = 2;
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_096%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_162%无";
            }
            cailiaolevel = 7;
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_098%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_102%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_163%无";
            }
            cailiaolevel = 12;
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_103%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_106%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_107%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_164%无";
            }
            cailiaolevel = 17;
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_109%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_110%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_111%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_112%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_165%无";
            }
            cailiaolevel = 22;
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_115%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_116%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_117%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_118%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_119%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_166%无";
            }
            cailiaolevel = 27;
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 9);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_121%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_122%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_123%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_124%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_126%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_127%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_128%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_129%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_167%无";
            }
            cailiaolevel = 32;
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_131%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_137%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_138%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_140%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_169%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_168%无";
            }
            cailiaolevel = 37;
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangBupiProcedure，保留分支及随机边界。 */
    private static String tableBupi(double level, RandomSource random) {
        double cailiaolevel = 0;
        double cailiaopinjie = 0;
        String neirong = "";
        if (level <= 1) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_171%无";
            cailiaolevel = 4;
        } else if (level == 2) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_172%无";
            cailiaolevel = 9;
        } else if (level == 3) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_173%无";
            cailiaolevel = 14;
        } else if (level == 4) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_174%无";
            cailiaolevel = 19;
        } else if (level == 5) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_175%无";
            cailiaolevel = 24;
        } else if (level == 6) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_176%无";
            cailiaolevel = 29;
        } else if (level == 7) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_177%无";
            cailiaolevel = 34;
        } else if (level == 8) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_178%无";
            cailiaolevel = 39;
        } else if (level >= 9) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_179%无";
            cailiaolevel = 40;
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangCailiaoProcedure，保留分支及随机边界。 */
    private static String tableCailiao(double level, RandomSource random) {
        double danyaonum = 0;
        double suijizhi = 0;
        String neirong = "";
        suijizhi = Mth.nextInt(random, 1, 11);
        if (suijizhi == 1) {
            neirong = tableCaoyao(level, random);
        } else if (suijizhi == 2) {
            neirong = tableZuowu(level, random);
        } else if (suijizhi == 3) {
            neirong = tableYaoshoucailiao(level, random);
        } else if (suijizhi == 4) {
            neirong = tableJinshucailiao(level, random);
        } else if (suijizhi == 5) {
            neirong = tableYihuocailiao(level, random);
        } else if (suijizhi == 6) {
            neirong = tableLingshi(level, random);
        } else if (suijizhi == 7) {
            neirong = tableDuyao(level, random);
        } else if (suijizhi == 8) {
            neirong = tableYaodancailiao(level, random);
        } else if (suijizhi == 9) {
            neirong = tableBaoshicailiao(level, random);
        } else if (suijizhi == 10) {
            neirong = tableLingmucailiao(level, random);
        } else if (suijizhi == 11) {
            neirong = tableBupi(level, random);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangCaoyaoProcedure，保留分支及随机边界。 */
    private static String tableCaoyao(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:peachpeach";
            } else if (suijizhi == 2) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:columbinemimosa";
            } else if (suijizhi == 3) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:mapleresinitem";
            } else if (suijizhi == 4) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:asiaticdayflower";
            } else if (suijizhi == 5) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:privet_01";
            } else if (suijizhi == 6) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:cypressseed";
            } else if (suijizhi == 7) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:camphorfruit";
            } else if (suijizhi == 8) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:ginkgofruit";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:spiritgrass";
            } else if (suijizhi == 2) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:fragranceflower";
            } else if (suijizhi == 3) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:raincloud";
            } else if (suijizhi == 4) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:goldenboilinggrass";
            }
        } else if (level >= 3) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:icicleflower";
            } else if (suijizhi == 2) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:coldleaf";
            } else if (suijizhi == 3) {
                neirong = "cailiaoid/char/yvanchuxiuzhen:bluegrass";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangDanyaoProcedure，保留分支及随机边界。 */
    private static String tableDanyao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double danyaonum = 0;
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/一级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/一级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/1%danyaotick0/int/60%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/1%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/一级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/1%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/一级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/1%danyaotick0/int/40%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/一级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/一级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/1%danyaotick0/int/60%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/1%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/一级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/二级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/二级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/2%danyaotick0/int/90%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/2%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/二级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/2%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/二级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/2%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/二级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/二级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/2%danyaotick0/int/90%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/2%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/二级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/三级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/三级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/3%danyaotick0/int/120%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/3%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/三级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/3%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/三级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/3%danyaotick0/int/80%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/三级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/三级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/3%danyaotick0/int/120%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/3%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/三级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/四级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/四级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/4%danyaotick0/int/150%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/4%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/四级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/4%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/四级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/4%danyaotick0/int/100%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/四级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/四级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/4%danyaotick0/int/150%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/4%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/四级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/五级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/五级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/5%danyaotick0/int/180%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/5%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/五级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/5%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/五级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/5%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/五级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/五级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/5%danyaotick0/int/180%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/5%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/五级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/六级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/六级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/6%danyaotick0/int/210%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/6%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/六级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/6%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/六级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/6%danyaotick0/int/140%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/六级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/六级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/6%danyaotick0/int/210%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/6%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/六级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/七级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/七级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/7%danyaotick0/int/240%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/7%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/七级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/7%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/七级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/7%danyaotick0/int/160%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/七级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/七级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/7%danyaotick0/int/240%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/7%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/七级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/八级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/八级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/8%danyaotick0/int/270%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/8%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/八级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/8%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/八级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/8%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 6) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/八级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            } else if (suijizhi == 7) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/八级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/8%danyaotick0/int/270%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/8%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 8) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            } else if (suijizhi == 9) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/八级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            } else if (suijizhi == 10) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            }
        }
        danyaonum = Mth.nextInt(random, 4, 16);
        neirong = neirong + "" + new DecimalFormat("").format(danyaonum) + "%danyaobaoxiang/logic/真";
        return neirong;

    }

    /** 来源 B:BaoxiangDuyaoProcedure，保留分支及随机边界。 */
    private static String tableDuyao(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 0) {
            neirong = "";
        } else if (level <= 2) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_141%无";
        } else if (level == 3) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_142%无";
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_143%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_144%无";
            }
        } else if (level == 5) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_145%无";
        } else if (level >= 6) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_146%无";
        } else if (level == 7) {
            neirong = "";
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "";
            } else if (suijizhi == 2) {
                neirong = "";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangFabaoProcedure，保留分支及随机边界。 */
    private static String tableFabao(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 7);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_001 板砖 投掷道具，可以重复使用。命中敌方实体造成物理伤害，并有百分之10的几率造成暴击，百分之10的几率造成眩晕，百分之10的概率造成沉默。 法宝 无 0 5 0 0 0 0 0 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_002 青石砖 投掷道具，可以重复使用。命中敌方实体造成物理伤害，并有百分之20的几率造成暴击，百分之20的几率造成眩晕，百分之20的概率造成沉默。 法宝 黄阶二级 2 8 0 0 0 0 0 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_003 毒囊 一次性投掷道具，以命中位置为中心，让半径8格内的敌方实体沾染毒囊内的毒素。 法宝 无 0 1 5 0 0 0 0 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_004 飞蝗石 一次性投掷道具，对命中的敌方实体造成物理伤害，并有百分之5的几率造成重伤。 法宝 无 0 5 0 0 0 0 0 100";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_005 飞刺 投掷道具，可以涂抹毒素并重复使用。对命中的敌方实体造成物理伤害，并有百分之10的几率造成流血。 法宝 黄阶三级 3 9 0 0 0 0 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_008 飞刀 投掷道具，可以涂抹毒素并重复使用。对命中的敌方实体造成物理伤害。 法宝 无 0 5 0 0 0 0 0 100";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 黄阶一级 1 1 6 0 0 0 0 80";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_006 十字标 投掷道具，可以涂抹毒素并重复使用。对命中的敌方实体造成物理伤害，有百分之10几率造成暴击。 法宝 黄阶八级 8 15 0 0 0 0 0 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 黄阶六级 6 1 11 0 0 0 0 80";
            } else if (suijizhi == 3) {
                neirong = "";
            } else if (suijizhi == 4) {
                neirong = "";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_007 蝴蝶标 投掷道具，可以涂抹毒素并重复使用。对命中的敌方实体造成物理伤害。 法宝 玄阶一级 11 19 0 0 0 0 0 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 玄阶一级 11 1 17 0 0 0 0 80";
            } else if (suijizhi == 3) {
                neirong = "";
            } else if (suijizhi == 4) {
                neirong = "";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 1);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 玄阶六级 16 1 27 0 0 0 0 80";
            } else if (suijizhi == 2) {
                neirong = "";
            } else if (suijizhi == 3) {
                neirong = "";
            } else if (suijizhi == 4) {
                neirong = "";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 1);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 地阶一级 21 1 38 0 0 0 0 80";
            } else if (suijizhi == 2) {
                neirong = "";
            } else if (suijizhi == 3) {
                neirong = "";
            } else if (suijizhi == 4) {
                neirong = "";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 1);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:fabao_010 覆火葫芦 右键释放存储在内的火焰，潜行右键可以把前方的火焰收起，葫芦的品级越高，能收起的火焰等级越高。 法宝 地阶六级 26 1 53 0 0 0 0 80";
            } else if (suijizhi == 2) {
                neirong = "";
            } else if (suijizhi == 3) {
                neirong = "";
            } else if (suijizhi == 4) {
                neirong = "";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 8);
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 8);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangFlyskillProcedure，保留分支及随机边界。 */
    private static String tableFlyskill(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level >= 7) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_028%flyskillName/char/海天一线%belongTopinjietext/char/天阶%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/金水土%belongTojianjie/char/脚踏蓝色祥云，速度快如流星。%flyskillxiaohao/int/22%flyskillspeed/double/2.7%flyskilltexiao/char/蓝色祥云";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%flyskillName/char/金霞云%belongTopinjietext/char/天阶%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/金火%belongTojianjie/char/如同火焰一样的金红云霞，恢宏壮观。%flyskillxiaohao/int/20%flyskillspeed/double/2.6%flyskilltexiao/char/红色祥云";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_034%flyskillName/char/血影残阳%belongTopinjietext/char/天阶%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/无%belongTojianjie/char/短时高爆发遁术，可以瞬间从阎王手中逃脱。%flyskillxiaohao/int/40%flyskillspeed/double/3%flyskilltexiao/char/血光";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_033%flyskillName/char/万里云%belongTopinjietext/char/天阶%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/无%belongTojianjie/char/一团白云载着玩家飞行%flyskillxiaohao/int/18%flyskillspeed/double/2.5%flyskilltexiao/char/白色祥云";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%flyskillName/char/暗黑天游术%belongTopinjietext/char/天阶%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/木火土%belongTojianjie/char/飞行起来周身都是滚滚黑气，气势磅礴。%flyskillxiaohao/int/18%flyskillspeed/double/2.5%flyskilltexiao/char/大黑雾";
            }
        } else if (level >= 5) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%flyskillName/char/御风术%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/水木火%belongTojianjie/char/乘风而起，御风飞行。%flyskillxiaohao/int/5%flyskillspeed/double/0.8%flyskilltexiao/char/疾风";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_034%flyskillName/char/血影遁%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/无%belongTojianjie/char/可瞬间爆发出一团血雾，包裹着玩家飞速逃遁。%flyskillxiaohao/int/20%flyskillspeed/double/2%flyskilltexiao/char/血雾";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%flyskillName/char/流星遁%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/金火土%belongTojianjie/char/如同流星划过天际，阵阵热浪掀起大量火花。%flyskillxiaohao/int/7%flyskillspeed/double/1%flyskilltexiao/char/火花";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%flyskillName/char/风雷遁%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/火土金%belongTojianjie/char/如同闪电一样，是地阶里最快的一种遁法。%flyskillxiaohao/int/12%flyskillspeed/double/1.2%flyskilltexiao/char/雷电";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%flyskillName/char/烽烟遁%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/木火土%belongTojianjie/char/裹挟着大量黑气起飞。%flyskillxiaohao/int/7%flyskillspeed/double/1%flyskilltexiao/char/黑雾";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%flyskillName/char/穹宇寻云%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/无%belongTojianjie/char/奇怪的名字，奇怪的飞行技能。%flyskillxiaohao/int/9%flyskillspeed/double/1.1%flyskilltexiao/char/两行云雾";
            }
        } else if (level >= 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_033%flyskillName/char/游云术%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/基础的飞行技能，虽然速度不快，但消耗也不高。%flyskillxiaohao/int/3%flyskillspeed/double/0.5%flyskilltexiao/char/云雾";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_033%flyskillName/char/拔空术%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/触发瞬间会弹射向高空，速度奇快，但飞行速度较慢。%flyskillxiaohao/int/5%flyskillspeed/double/0.3%flyskilltexiao/char/云雾";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangFuluProcedure，保留分支及随机边界。 */
    private static String tableFulu(double level, RandomSource random) {
        double suijizhi = 0;
        double TreasureLEVEL = 0;
        double suijilevel = 0;
        String neirong = "";
        String fululeveltxt = "";
        TreasureLEVEL = level;
        if (TreasureLEVEL <= 1 || TreasureLEVEL == 2) {
            suijilevel = 1;
            fululeveltxt = "黄阶";
        } else if (TreasureLEVEL == 3 || TreasureLEVEL == 4) {
            suijilevel = 2;
            fululeveltxt = "玄阶";
        } else if (TreasureLEVEL == 5 || TreasureLEVEL == 6) {
            suijilevel = 3;
            fululeveltxt = "地阶";
        } else if (TreasureLEVEL == 7 || TreasureLEVEL == 8) {
            suijilevel = 4;
            fululeveltxt = "天阶";
        }
        if (TreasureLEVEL <= 1) {
            suijizhi = Mth.nextInt(random, 1, 14);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_46%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_06%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_34%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_43%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_01%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_04%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_05%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_05%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_09%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_14%无%fululevel/int/";
            }
            if (suijizhi == 11) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_28%无%fululevel/int/";
            } else if (suijizhi == 12) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_29%无%fululevel/int/";
            } else if (suijizhi == 13) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_36%无%fululevel/int/";
            } else if (suijizhi == 14) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_42%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 2) {
            suijizhi = Mth.nextInt(random, 1, 11);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_01%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_04%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_05%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_05%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_09%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_14%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_28%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_29%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_36%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_42%无%fululevel/int/";
            } else if (suijizhi == 11) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_30%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 3) {
            suijizhi = Mth.nextInt(random, 1, 14);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_30%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_18%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_11%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_03%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_07%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_24%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_25%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_26%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_27%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_32%无%fululevel/int/";
            }
            if (suijizhi == 11) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_39%无%fululevel/int/";
            } else if (suijizhi == 12) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_40%无%fululevel/int/";
            } else if (suijizhi == 13) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_44%无%fululevel/int/";
            } else if (suijizhi == 14) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_45%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 4) {
            suijizhi = Mth.nextInt(random, 1, 16);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_02%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_18%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_11%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_03%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_07%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_24%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_25%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_26%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_27%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_32%无%fululevel/int/";
            }
            if (suijizhi == 11) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_39%无%fululevel/int/";
            } else if (suijizhi == 12) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_40%无%fululevel/int/";
            } else if (suijizhi == 13) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_44%无%fululevel/int/";
            } else if (suijizhi == 14) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_45%无%fululevel/int/";
            } else if (suijizhi == 15) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_10%无%fululevel/int/";
            } else if (suijizhi == 16) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_12%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 5) {
            suijizhi = Mth.nextInt(random, 1, 13);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_02%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_10%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_12%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_33%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_38%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_35%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_13%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_15%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_17%无%fululevel/int/";
            }
            if (suijizhi == 11) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 12) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_20%无%fululevel/int/";
            } else if (suijizhi == 13) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_21%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 6) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_20%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_21%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_33%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_38%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_35%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_13%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_15%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_17%无%fululevel/int/";
            }
        } else if (TreasureLEVEL == 7) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_20%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_21%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_35%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_19%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_13%无%fululevel/int/";
            } else if (suijizhi == 7) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_15%无%fululevel/int/";
            } else if (suijizhi == 8) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_17%无%fululevel/int/";
            } else if (suijizhi == 9) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_22%无%fululevel/int/";
            } else if (suijizhi == 10) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_23%无%fululevel/int/";
            }
        } else if (TreasureLEVEL >= 8) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_22%无%fululevel/int/";
            } else if (suijizhi == 2) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_23%无%fululevel/int/";
            } else if (suijizhi == 3) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_31%无%fululevel/int/";
            } else if (suijizhi == 4) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_41%无%fululevel/int/";
            } else if (suijizhi == 5) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_16%无%fululevel/int/";
            } else if (suijizhi == 6) {
                neirong = "fuluid/char/yvanchuxiuzhen:itemfulu_37%无%fululevel/int/";
            }
        }
        neirong = neirong + "" + new DecimalFormat("0").format(suijilevel) + "%fululeveltxt/char/" + fululeveltxt;
        return neirong;

    }

    /** 来源 B:BaoxiangGongfaProcedure，保留分支及随机边界。 */
    private static String tableGongfa(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 10);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/五行基础%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/无%belongToLevel/int/0%belongTobody/char/无%belongTojianjie/char/玩家最最基础的功法，没有特殊之处。%TechniqueHealth/char/下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/最下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/分金术%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/金%belongTojianjie/char/金属性基础功法，额外增加金系技能攻击力。%ExercisesGold/int/1%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/静波籍%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/水%belongTojianjie/char/水属性基础功法，额外增加水系技能攻击力。%ExercisesWater/int/1%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/长青功%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/木%belongTojianjie/char/木属性基础功法，额外增加木系技能攻击力。%ExercisesTree/int/1%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/最下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_016%ExercisesName/char/弄焰诀%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/火%belongTojianjie/char/火属性基础功法，额外增加火系技能攻击力。%ExercisesFire/int/1%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_017%ExercisesName/char/厚土诀%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/土%belongTojianjie/char/土属性基础功法，额外增加土系技能攻击力。%ExercisesSoil/int/1%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/最下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/清风诀%belongTopinjietext/char/黄阶五级%belongTopinjie/int/5%belongToLeveltext/char/练气五层%belongToLevel/int/5%belongTobody/char/木火水%belongTojianjie/char/风系功法，会额外增加移动速度。%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/最下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/下档";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/御雷法%belongTopinjietext/char/黄阶四级%belongTopinjie/int/4%belongToLeveltext/char/练气四层%belongToLevel/int/4%belongTobody/char/火木%belongTojianjie/char/御雷功法，额外增加移动速度，被雷劈中时可以增加法术攻击一段时间。%TechniqueHealth/char/下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_007%ExercisesName/char/阴元功%belongTopinjietext/char/黄阶二级%belongTopinjie/int/2%belongToLeveltext/char/练气二层%belongToLevel/int/2%belongTobody/char/阴水%belongTojianjie/char/相传此功法是由某个玄阶的双修功法分离出来的，不得不说是种遗憾。%TechniqueHealth/char/最下档%TechniqueHeathRec/char/最下档%Techniquepower/char/最下档%TechniquepowerRec/char/最下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/繁星呓语%belongTopinjietext/char/黄阶五级%belongTopinjie/int/5%belongToLeveltext/char/练气五层%belongToLevel/int/5%belongTobody/char/土水%belongTojianjie/char/在夜晚时获得额外灵力恢复。%TechniqueHealth/char/最下档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/最下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/寒脉诀%belongTopinjietext/char/黄阶六级%belongTopinjie/int/6%belongToLeveltext/char/练气六层%belongToLevel/int/6%belongTobody/char/水金%belongTojianjie/char/注重防御与法术防御的功法。%TechniqueHealth/char/下档%TechniqueHeathRec/char/最下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/最下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/下档%TechniqueMana/char/最下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%ExercisesName/char/明光诀%belongTopinjietext/char/黄阶七级%belongTopinjie/int/7%belongToLeveltext/char/练气七层%belongToLevel/int/7%belongTobody/char/阳火%belongTojianjie/char/额外增加火系技能的伤害，并且沐浴在阳光下修炼会有额外修为加成。%ExercisesFire/int/1%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/最下档%TechniqueMana/char/下档%TechniqueManaDef/char/最下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/最下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/御血术%belongTopinjietext/char/黄阶九级%belongTopinjie/int/9%belongToLeveltext/char/练气九层%belongToLevel/int/9%belongTobody/char/水木%belongTojianjie/char/血道入门级功法，亦是一种魔功。%TechniqueHealth/char/中档%TechniqueHeathRec/char/下档%Techniquepower/char/最下档%TechniquepowerRec/char/下档%TechniqueSoul/char/最下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/行尸御火%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/无%belongToLevel/int/0%belongTobody/char/阴火%belongTojianjie/char/死灵类功法，学习后获得《起尸诀》法术。%TechniqueHealth/char/下档%TechniqueHeathRec/char/最下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/中档%TechniqueSoulRec/char/下档%TechniqueAttack/char/最下档%TechniqueAttackDef/char/下档%TechniqueMana/char/最下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 11);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/五行褪凡%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/比《五行基础功法》要好一些，作为过渡功法亦不错。%TechniqueHealth/char/中档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/最下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/苍痕诀%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/金%belongTojianjie/char/金属性进阶功法，额外增加金系技能攻击力。%ExercisesGold/int/2%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/乱流诀%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/水%belongTojianjie/char/水属性进阶功法，额外增加水系技能攻击力。%ExercisesWater/int/2%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/哺灵诀%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/木%belongTojianjie/char/木属性进阶功法，额外增加木系技能攻击力。%ExercisesTree/int/2%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/中档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_016%ExercisesName/char/灼光诀%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/火%belongTojianjie/char/火属性进阶功法，额外增加火系技能攻击力。%ExercisesFire/int/2%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/中档%TechniqueAttackDef/char/下档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_017%ExercisesName/char/尘丰诀%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/土%belongTojianjie/char/土属性进阶功法，额外增加土系技能攻击力。%ExercisesSoil/int/2%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/下档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/丝缕打叶诀%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/木火水%belongTojianjie/char/风系功法，会额外增加移动速度。%ExercisesWater/int/1%ExercisesFire/int/1%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/下档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/中档";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/雷脉功%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/火木%belongTojianjie/char/御雷功法，额外增加移动速度，被雷劈中时可以增加法术攻击一段时间。%ExercisesTree/int/1%ExercisesFire/int/1%TechniqueHealth/char/中档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/下档";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_007%ExercisesName/char/玄阴诀%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/阴水%belongTojianjie/char/在阴气极盛的地方修炼，可以事半功倍。%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/下档%TechniquepowerRec/char/下档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/中档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/流星春水%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/土水%belongTojianjie/char/攻击时一定几率召唤流星砸向前方。%ExercisesWater/int/1%ExercisesSoil/int/1%TechniqueHealth/char/下档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/聚元功%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/金水木火%belongTojianjie/char/修炼时额外获得 百分之（0.25*功法等级）点修为。%TechniqueHealth/char/下档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/下档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/地脉寒元功%belongTopinjietext/char/玄阶六级%belongTopinjie/int/16%belongToLeveltext/char/筑基六阶%belongToLevel/int/16%belongTobody/char/水金%belongTojianjie/char/注重防御与法术防御的功法。%ExercisesGold/int/1%ExercisesWater/int/1%TechniqueHealth/char/中档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/下档%TechniqueAttack/char/下档%TechniqueAttackDef/char/中档%TechniqueMana/char/下档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%ExercisesName/char/大光明功%belongTopinjietext/char/玄阶七级%belongTopinjie/int/17%belongToLeveltext/char/筑基七阶%belongToLevel/int/17%belongTobody/char/阳火%belongTojianjie/char/额外增加火系技能的伤害，并且沐浴在阳光下修炼会有额外修为加成。%ExercisesFire/int/2%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/下档%TechniqueAttackDef/char/下档%TechniqueMana/char/中档%TechniqueManaDef/char/下档%TechniqueSouatt/char/下档%TechniqueSouDef/char/下档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/汲血功%belongTopinjietext/char/玄阶八级%belongTopinjie/int/18%belongToLeveltext/char/筑基八阶%belongToLevel/int/18%belongTobody/char/水木%belongTojianjie/char/每个血道技能命中敌人时，获得1点血精。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/下档%TechniquepowerRec/char/中档%TechniqueSoul/char/下档%TechniqueSoulRec/char/中档%TechniqueAttack/char/下档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/真言吒雷诀%belongTopinjietext/char/玄阶九级%belongTopinjie/int/19%belongToLeveltext/char/筑基九阶%belongToLevel/int/19%belongTobody/char/火%belongTojianjie/char/习得此功法后即可获得《地滚雷》技能。%ExercisesFire/int/2%TechniqueHealth/char/中档%TechniqueHeathRec/char/下档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/下档%TechniqueAttack/char/中档%TechniqueAttackDef/char/下档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/下档%TechniqueSpeed/char/下档";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 12);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/混元五行功%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/无%belongTojianjie/char/玩家灵根越多，战斗获得的额外经验就越多：1灵根增加百分之十，5灵根增加百分之五十。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/下档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/金曜真经%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/金%belongTojianjie/char/金属性高级功法，额外增加金系技能攻击力。%ExercisesGold/int/3%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/上档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/幽水真经%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/水%belongTojianjie/char/水属性高级功法，额外增加水系技能攻击力。%ExercisesWater/int/3%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/乙木真经%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/木%belongTojianjie/char/木属性高级功法，额外增加木系技能攻击力。%ExercisesTree/int/3%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_016%ExercisesName/char/炽焰真经%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/火%belongTojianjie/char/火属性高级功法，额外增加火系技能攻击力。%ExercisesFire/int/3%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/下档%TechniqueSpeed/char/下档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_017%ExercisesName/char/重峦真经%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/土%belongTojianjie/char/土属性高级功法，额外增加土系技能攻击力。%ExercisesSoil/int/3%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/长空云转诀%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/木火水%belongTojianjie/char/风系功法，会额外增加移动速度。%ExercisesWater/int/2%ExercisesFire/int/2%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/上档";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/五雷炼体诀%belongTopinjietext/char/地阶四级%belongTopinjie/int/24%belongToLeveltext/char/结丹四转%belongToLevel/int/24%belongTobody/char/火木%belongTojianjie/char/御雷功法，额外增加移动速度，被雷劈中时可以增加法术攻击一段时间。%ExercisesTree/int/2%ExercisesFire/int/2%TechniqueHealth/char/上档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/中档";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_007%ExercisesName/char/化阴大法%belongTopinjietext/char/地阶二级%belongTopinjie/int/22%belongToLeveltext/char/结丹二转%belongToLevel/int/22%belongTobody/char/阴水%belongTojianjie/char/在阴气极盛的地方修炼，可以事半功倍。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/星君诀%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/土水%belongTojianjie/char/注重法术防御与恢复的功法，并且在攻击时一定几率召唤流星砸向前方。%ExercisesWater/int/2%ExercisesSoil/int/2%TechniqueHealth/char/中档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/聚灵小周天神功%belongTopinjietext/char/地阶二级%belongTopinjie/int/22%belongToLeveltext/char/结丹二转%belongToLevel/int/22%belongTobody/char/金水木火%belongTojianjie/char/修炼时额外获得 百分之（0.5*功法等级）点修为。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 12) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/阡陌心诀%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/无%belongTojianjie/char/强化精神力的功法，可以额外为精神攻击与精力上限增加6点上限。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/霜降真元功%belongTopinjietext/char/地阶六级%belongTopinjie/int/26%belongToLeveltext/char/结丹六转%belongToLevel/int/26%belongTobody/char/水金%belongTojianjie/char/注重防御与法术防御的功法。%ExercisesGold/int/2%ExercisesWater/int/2%TechniqueHealth/char/上档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/中档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%ExercisesName/char/明镜映心诀%belongTopinjietext/char/地阶七级%belongTopinjie/int/27%belongToLeveltext/char/结丹七转%belongToLevel/int/27%belongTobody/char/阳火%belongTojianjie/char/额外增加火系技能的伤害，并且沐浴在阳光下修炼会有额外修为加成。%ExercisesFire/int/3%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_002%ExercisesName/char/人皇诀%belongTopinjietext/char/地阶八级%belongTopinjie/int/28%belongToLeveltext/char/结丹八转%belongToLevel/int/28%belongTobody/char/土水%belongTojianjie/char/远古人皇创立的功法，修习此功法可以提高在人族的统御力。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/上档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/中档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%ExercisesName/char/崩天诀%belongTopinjietext/char/地阶九级%belongTopinjie/int/29%belongToLeveltext/char/结丹九转%belongToLevel/int/29%belongTobody/char/金火%belongTojianjie/char/一门走极端的功法，极致的攻击强化。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/上档%TechniqueSoulRec/char/中档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/中档%TechniqueSouatt/char/上档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_028%ExercisesName/char/无常乱%belongTopinjietext/char/地阶七级%belongTopinjie/int/27%belongToLeveltext/char/结丹七转%belongToLevel/int/27%belongTobody/char/阴阳%belongTojianjie/char/此功法对精神力的强化效果更显著一些。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/中档%TechniqueManaDef/char/中档%TechniqueSouatt/char/最上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_009%ExercisesName/char/紫薇吐纳%belongTopinjietext/char/地阶六级%belongTopinjie/int/26%belongToLeveltext/char/结丹六转%belongToLevel/int/26%belongTobody/char/无%belongTojianjie/char/运转功法与紫薇星共鸣，在星夜下修炼会有额外修为加成。%TechniqueHealth/char/中档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/地血大法%belongTopinjietext/char/地阶六级%belongTopinjie/int/26%belongToLeveltext/char/结丹六转%belongToLevel/int/26%belongTobody/char/水木%belongTojianjie/char/每个血道技能命中敌人时，恢复（功法等级除以2）点血量，满血时掉落1点血精。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/中档%TechniquepowerRec/char/中档%TechniqueSoul/char/中档%TechniqueSoulRec/char/中档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/地煞尸魔功%belongTopinjietext/char/地阶八级%belongTopinjie/int/28%belongToLeveltext/char/结丹八转%belongToLevel/int/28%belongTobody/char/阴火%belongTojianjie/char/死灵类功法，修习此功法可以提高炼尸的成功率。%TechniqueHealth/char/上档%TechniqueHeathRec/char/中档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/中档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/中档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 12);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/五行补天神功%belongTopinjietext/char/天阶一级%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/无%belongTojianjie/char/玩家灵根等级总和越多，战斗获得的额外经验获取量越多：1灵根等级增加百分之五，45灵根等级增加百分之二百二十五。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/中档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/金光玄功%belongTopinjietext/char/天阶三级%belongTopinjie/int/33%belongToLeveltext/char/元婴三重%belongToLevel/int/33%belongTobody/char/金%belongTojianjie/char/金属性顶级功法之一，额外增加金系技能攻击力。%ExercisesGold/int/4%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/天河落%belongTopinjietext/char/天阶三级%belongTopinjie/int/33%belongToLeveltext/char/元婴三重%belongToLevel/int/33%belongTobody/char/水%belongTojianjie/char/水属性顶级功法之一，额外增加水系技能攻击力。%ExercisesWater/int/4%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/最上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/百草真经%belongTopinjietext/char/天阶三级%belongTopinjie/int/33%belongToLeveltext/char/元婴三重%belongToLevel/int/33%belongTobody/char/木%belongTojianjie/char/木属性顶级功法之一，额外增加木系技能攻击力。%ExercisesTree/int/4%TechniqueHealth/char/最上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_016%ExercisesName/char/焚烬炽火诀%belongTopinjietext/char/天阶三级%belongTopinjie/int/33%belongToLeveltext/char/元婴三重%belongToLevel/int/33%belongTobody/char/火%belongTojianjie/char/火属性顶级功法之一，额外增加火系技能攻击力。%ExercisesFire/int/4%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/最上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/中档%TechniqueSpeed/char/中档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_017%ExercisesName/char/擎山震地功%belongTopinjietext/char/天阶三级%belongTopinjie/int/33%belongToLeveltext/char/元婴三重%belongToLevel/int/33%belongTobody/char/土%belongTojianjie/char/土属性顶级功法之一，额外增加土系技能攻击力。%ExercisesSoil/int/4%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/最上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_015%ExercisesName/char/扶摇天罗功%belongTopinjietext/char/天阶五级%belongTopinjie/int/35%belongToLeveltext/char/元婴五重%belongToLevel/int/35%belongTobody/char/木火水%belongTojianjie/char/风系功法，会额外增加移动速度，也是移速增加最多的功法。%ExercisesWater/int/3%ExercisesFire/int/3%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/中档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/中档%TechniqueSpeed/char/最上档";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_013%ExercisesName/char/九霄真雷功%belongTopinjietext/char/天阶四级%belongTopinjie/int/34%belongToLeveltext/char/元婴四重%belongToLevel/int/34%belongTobody/char/火木%belongTojianjie/char/御雷功法，额外增加移动速度，被雷劈中时可以增加法术攻击一段时间。%ExercisesTree/int/3%ExercisesFire/int/3%TechniqueHealth/char/最上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/最上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/上档";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_007%ExercisesName/char/擎天阴魔功%belongTopinjietext/char/天阶二级%belongTopinjie/int/32%belongToLeveltext/char/元婴二重%belongToLevel/int/32%belongTobody/char/阴水%belongTojianjie/char/由魔族功法改动而来，在阴气极盛的地方修炼，可以事半功倍。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/中档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/最上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/聚灵大周天神功%belongTopinjietext/char/天阶二级%belongTopinjie/int/32%belongToLeveltext/char/元婴二重%belongToLevel/int/32%belongTobody/char/金水木火%belongTojianjie/char/修炼时额外获得 百分之（1*功法等级）点修为。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/中档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%ExercisesName/char/天衍神功%belongTopinjietext/char/天阶五级%belongTopinjie/int/35%belongToLeveltext/char/结丹五转%belongToLevel/int/35%belongTobody/char/阴阳%belongTojianjie/char/强化精神力的强大功法，可以额外为精神攻击与精力上限增加10点上限。%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/最上档%TechniqueSoulRec/char/最上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/最上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 12) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%ExercisesName/char/血魔祭魂秘法%belongTopinjietext/char/天阶一级%belongTopinjie/int/31%belongToLeveltext/char/元婴一重%belongToLevel/int/31%belongTobody/char/水木%belongTojianjie/char/血道顶级功法，每次普攻额外造成（功法等级除以2）点真实伤害，并获得2点血精。%TechniqueHealth/char/最上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/下档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/下档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_014%ExercisesName/char/九幽寒魄大法%belongTopinjietext/char/天阶六级%belongTopinjie/int/36%belongToLeveltext/char/元婴六重%belongToLevel/int/36%belongTobody/char/水金%belongTojianjie/char/注重防御与法术防御的功法。%ExercisesGold/int/3%ExercisesWater/int/3%TechniqueHealth/char/最上档%TechniqueHeathRec/char/上档%Techniquepower/char/最上档%TechniquepowerRec/char/上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/最上档%TechniqueMana/char/上档%TechniqueManaDef/char/最上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%ExercisesName/char/亘古元光功%belongTopinjietext/char/天阶七级%belongTopinjie/int/37%belongToLeveltext/char/元婴七重%belongToLevel/int/37%belongTobody/char/阳火%belongTojianjie/char/额外增加火系技能的伤害，并且沐浴在阳光下修炼会有额外修为加成。%ExercisesFire/int/4%TechniqueHealth/char/最上档%TechniqueHeathRec/char/最上档%Techniquepower/char/最上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/最上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_002%ExercisesName/char/天帝君临%belongTopinjietext/char/天阶七级%belongTopinjie/int/37%belongToLeveltext/char/元婴七重%belongToLevel/int/37%belongTobody/char/五%belongTojianjie/char/最强人族功法之一，对五行属性的技能都有不小增幅。%ExercisesGold/int/3%ExercisesWater/int/3%ExercisesTree/int/3%ExercisesFire/int/3%ExercisesSoil/int/3%TechniqueHealth/char/最上档%TechniqueHeathRec/char/最上档%Techniquepower/char/上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/最上档%TechniqueAttack/char/上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/最上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%ExercisesName/char/金廷落%belongTopinjietext/char/天阶八级%belongTopinjie/int/38%belongToLeveltext/char/元婴八重%belongToLevel/int/38%belongTobody/char/金火%belongTojianjie/char/最强人族功法之一，上古时期专门对抗天庭而创立的功法，普通攻击增加 功法等级*0.5 点真实伤害。%ExercisesGold/int/3%TechniqueHealth/char/上档%TechniqueHeathRec/char/上档%Techniquepower/char/上档%TechniquepowerRec/char/上档%TechniqueSoul/char/最上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/最上档%TechniqueMana/char/最上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/最上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/上档";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_018%ExercisesName/char/天王诀%belongTopinjietext/char/天阶六级%belongTopinjie/int/36%belongToLeveltext/char/元婴六重%belongToLevel/int/36%belongTobody/char/土火%belongTojianjie/char/强大的天阶功法，注重强化攻击力，灵力与精力的恢复速度也更快。%TechniqueHealth/char/上档%TechniqueHeathRec/char/最上档%Techniquepower/char/上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/上档%TechniqueMana/char/上档%TechniqueManaDef/char/上档%TechniqueSouatt/char/最上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%ExercisesName/char/道转天地诀%belongTopinjietext/char/天阶十级%belongTopinjie/int/40%belongToLeveltext/char/元婴十重%belongToLevel/int/40%belongTobody/char/无%belongTojianjie/char/最强人族功法之一，追求道法本源，反而不会对灵根有需求。%ExercisesGold/int/2%ExercisesWater/int/2%ExercisesTree/int/2%ExercisesFire/int/2%ExercisesSoil/int/2%TechniqueHealth/char/最上档%TechniqueHeathRec/char/最上档%Techniquepower/char/最上档%TechniquepowerRec/char/最上档%TechniqueSoul/char/上档%TechniqueSoulRec/char/上档%TechniqueAttack/char/最上档%TechniqueAttackDef/char/最上档%TechniqueMana/char/最上档%TechniqueManaDef/char/最上档%TechniqueSouatt/char/上档%TechniqueSouDef/char/上档%TechniqueSpeed/char/最下档";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangHuifudanyaoProcedure，保留分支及随机边界。 */
    private static String tableHuifudanyao(double level, RandomSource random) {
        double danyaonum = 0;
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/一级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/1%danyaotick0/int/60%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/1%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/一级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/一级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/1%danyaotick0/int/60%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/1%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/1%danyaotick0/int/60%danyaonum/int/";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/二级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/2%danyaotick0/int/90%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/2%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/二级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/二级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/2%danyaotick0/int/90%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/2%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/2%danyaotick0/int/90%danyaonum/int/";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/三级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/3%danyaotick0/int/120%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/3%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/三级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/三级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/3%danyaotick0/int/120%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/3%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/3%danyaotick0/int/120%danyaonum/int/";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/四级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/4%danyaotick0/int/150%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/4%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/四级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/四级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/4%danyaotick0/int/150%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/4%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/4%danyaotick0/int/150%danyaonum/int/";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/五级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/5%danyaotick0/int/180%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/5%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/五级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/五级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/5%danyaotick0/int/180%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/5%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/5%danyaotick0/int/180%danyaonum/int/";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/六级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/6%danyaotick0/int/210%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/6%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/六级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/六级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/6%danyaotick0/int/210%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/6%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/6%danyaotick0/int/210%danyaonum/int/";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/七级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/7%danyaotick0/int/240%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/7%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/七级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/七级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/7%danyaotick0/int/240%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/7%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/7%danyaotick0/int/240%danyaonum/int/";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/八级回春丹%shuliang/int/1%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级止血丹%shuliang/int/2%danyaoxiaoguo0/char/regeneration%danyaolevel0/int/8%danyaotick0/int/270%danyaoxiaoguo1/char/instant_health%danyaolevel1/int/8%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_02%danyaoname/char/八级合气丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            } else if (suijizhi == 4) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_03%danyaoname/char/八级融灵丹%shuliang/int/2%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_010%danyaolevel0/int/8%danyaotick0/int/270%danyaoxiaoguo1/char/yvanchuxiuzhen:effect_011%danyaolevel1/int/8%danyaotick1/int/0%danyaonum/int/";
            } else if (suijizhi == 5) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级养精续魄丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_017%danyaolevel0/int/8%danyaotick0/int/270%danyaonum/int/";
            }
        }
        danyaonum = Mth.nextInt(random, 4, 16);
        neirong = neirong + "" + new DecimalFormat("").format(danyaonum) + "%danyaobaoxiang/logic/真";
        return neirong;

    }

    /** 来源 B:BaoxiangHujiaProcedure，保留分支及随机边界。 */
    private static String tableHujia(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_01_helmet 罩甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 黄阶四级 4 4 30 10 3 0 0.03 1 1 1 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_01_chestplate 罩甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 黄阶四级 4 4 30 10 8 0 0.03 1 1 1 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_01_leggins 罩甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 黄阶四级 4 4 30 10 6 0 0.03 1 1 1 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_01_boots 罩甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 黄阶四级 4 4 30 10 3 0 0.03 1 1 1 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_02_helmet 铁胄甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 黄阶九级 9 8 54 18 3 0 0.06 1 1 1 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_02_chestplate 铁胄甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 黄阶九级 9 8 54 18 8 0 0.06 1 1 1 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_02_leggins 铁胄甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 黄阶九级 9 8 54 18 6 0 0.06 1 1 1 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_02_boots 铁胄甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 黄阶九级 9 8 54 18 3 0 0.06 1 1 1 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_03_helmet 山甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 玄阶四级 14 15 95 32 3 0 0.09 2 2 1 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_03_chestplate 山甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 玄阶四级 14 15 95 32 8 0 0.09 2 2 1 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_03_leggins 山甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 玄阶四级 14 15 95 32 6 0 0.09 2 2 1 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_03_boots 山甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 玄阶四级 14 15 95 32 3 0 0.09 2 2 1 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_04_helmet 漆铜战甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 玄阶九级 19 23 140 47 3 0 0.12 3 3 1 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_04_chestplate 漆铜战甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 玄阶九级 19 23 140 47 8 0 0.12 3 3 1 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_04_leggins 漆铜战甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 玄阶九级 19 23 140 47 6 0 0.12 3 3 1 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_04_boots 漆铜战甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 玄阶九级 19 23 140 47 3 0 0.12 3 3 1 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_05_helmet 银铁战甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 地阶四级 24 37 199 67 3 0 0.15 3 3 2 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_05_chestplate 银铁战甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 地阶四级 24 37 199 67 8 0 0.15 3 3 2 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_05_leggins 银铁战甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 地阶四级 24 37 199 67 6 0 0.15 3 3 2 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_05_boots 银铁战甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 地阶四级 24 37 199 67 3 0 0.15 3 3 2 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_06_helmet 风林战甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 地阶九级 29 53 262 88 3 0 0.18 4 4 2 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_06_chestplate 风林战甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 地阶九级 29 53 262 88 8 0 0.18 4 4 2 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_06_leggins 风林战甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 地阶九级 29 53 262 88 6 0 0.18 4 4 2 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_06_boots 风林战甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 地阶九级 29 53 262 88 3 0 0.18 4 4 2 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_07_helmet 金漆山纹甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 天阶四级 34 82 342 114 3 0 0.21 6 6 3 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_07_chestplate 金漆山纹甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 天阶四级 34 82 342 114 8 0 0.21 6 6 3 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_07_leggins 金漆山纹甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 天阶四级 34 82 342 114 6 0 0.21 6 6 3 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_07_boots 金漆山纹甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 天阶四级 34 82 342 114 3 0 0.21 6 6 3 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_08_helmet 明光甲头盔 重甲类的物理防御优异，但精神防御很弱。 头盔 天阶九级 39 114 426 142 3 0 0.24 7 7 3 100";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_08_chestplate 明光甲胸甲 重甲类的物理防御优异，但精神防御很弱。 胸甲 天阶九级 39 114 426 142 8 0 0.24 7 7 3 100";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_08_leggins 明光甲裤子 重甲类的物理防御优异，但精神防御很弱。 裤子 天阶九级 39 114 426 142 6 0 0.24 7 7 3 100";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:armor_08_boots 明光甲战靴 重甲类的物理防御优异，但精神防御很弱。 战靴 天阶九级 39 114 426 142 3 0 0.24 7 7 3 100";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangJinengProcedure，保留分支及随机边界。 */
    private static String tableJineng(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 16);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/金芒术%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/金%belongTojianjie/char/发射一枚有穿透的金属性飞弹。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1.1%jinengxiaohao/doube/1%jinengjieyin/int/0%Goldcolddown/int/5%Watercolddown/int/-1%Goldcoldignore/int/20";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/金刚轮%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/金%belongTojianjie/char/以自身为中心，弹开周身的敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/0.7%jinengxiaohao/doube/0.5%jinengjieyin/int/0%Goldcolddown/int/10%Watercolddown/int/-2%Goldcoldignore/int/30";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水蛇鞭%belongTopinjietext/char/黄阶二级%belongTopinjie/int/2%belongToLeveltext/char/练气二层%belongToLevel/int/2%belongTobody/char/水%belongTojianjie/char/前方召唤一个水鞭，造成范围伤害并击退。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.2%jinengjieyin/int/20%Watercolddown/int/3%Treecolddown/int/-1%Watercoldignore/int/10";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水球术%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/水%belongTojianjie/char/发射一枚水球。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0.8%jinengxiaohao/doube/0.8%jinengjieyin/int/0%Watercolddown/int/4%Treecolddown/int/-1%Watercoldignore/int/30";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/水灵弹%belongTopinjietext/char/黄阶五级%belongTopinjie/int/5%belongToLeveltext/char/练气五层%belongToLevel/int/5%belongTobody/char/水%belongTojianjie/char/发射一枚水灵球，水灵球飞行时间更长，如果在飞行时间结束时还未命中任何东西，则召唤次级水灵球追踪附近实体。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/0.8%jinengjieyin/int/0%Watercolddown/int/4%Treecolddown/int/-1%Watercoldignore/int/30";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/冰锥术%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/水%belongTojianjie/char/发射一枚冰锥，造成较高伤害并叠加2秒冷冻，同时破除霸体状态。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.2%jinengjieyin/int/0%Watercolddown/int/4%Treecolddown/int/-1%Watercoldignore/int/12";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/拈花飞叶%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/木%belongTojianjie/char/发射飞花，如果手持树叶等植物，则不消耗灵力。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/1%jinengjieyin/int/0%Treecolddown/int/5%Firecolddown/int/-1%Treecoldignore/int/30";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/乱枝%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/木%belongTojianjie/char/生长出一堆枯枝，对范围内的敌方实体禁锢，不过范围狭小。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/0.8%jinengjieyin/int/0%Treecolddown/int/5%Firecolddown/int/-1%Treecoldignore/int/30";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/灰木剑%belongTopinjietext/char/黄阶四级%belongTopinjie/int/4%belongToLeveltext/char/练气四层%belongToLevel/int/4%belongTobody/char/木%belongTojianjie/char/生长出一根树枝，挥舞向前方，造成范围伤害，有一定几率流血，但没有击退。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/20%Treecolddown/int/7%Firecolddown/int/-3%Treecoldignore/int/28";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/毒蔓鞭%belongTopinjietext/char/黄阶二级%belongTopinjie/int/2%belongToLeveltext/char/练气二层%belongToLevel/int/2%belongTobody/char/木%belongTojianjie/char/召唤一个带有毒刺的藤鞭向前方攻击，击退敌方实体并给予雷公藤毒素。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/1.5%jinengjieyin/int/5%Treecolddown/int/15%Firecolddown/int/-5%Treecoldignore/int/30";
            }
            if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/风弹术%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/木火%belongTojianjie/char/发射一枚风弹，击退命中的敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0.8%jinengxiaohao/doube/1%jinengjieyin/int/0%Treecolddown/int/5%Firecolddown/int/-1%Treecoldignore/int/30";
            } else if (suijizhi == 12) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/造云术%belongTopinjietext/char/黄阶五级%belongTopinjie/int/5%belongToLeveltext/char/练气五层%belongToLevel/int/5%belongTobody/char/木火%belongTojianjie/char/制造一片白云，把自身隐藏其中。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/20%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 13) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/火球术%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/火%belongTojianjie/char/发射一枚火球，命中时引燃敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.2%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-2%Firecoldignore/int/40";
            } else if (suijizhi == 14) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/引火%belongTopinjietext/char/黄阶四级%belongTopinjie/int/4%belongToLeveltext/char/练气四层%belongToLevel/int/4%belongTobody/char/火%belongTojianjie/char/引燃前方范围内的敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/0.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-2%Firecoldignore/int/80";
            } else if (suijizhi == 15) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/光弹术%belongTopinjietext/char/黄阶三级%belongTopinjie/int/3%belongToLeveltext/char/练气三层%belongToLevel/int/3%belongTobody/char/火%belongTojianjie/char/伤害较低，但冷却很短的光弹，速度也是极快。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/0.8%jinengxiaohao/doube/0.8%jinengjieyin/int/0%Firecolddown/int/3%Soilcolddown/int/-1%Firecoldignore/int/20";
            } else if (suijizhi == 16) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/飞石术%belongTopinjietext/char/黄阶一级%belongTopinjie/int/1%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/土%belongTojianjie/char/向前投掷一个低消耗低冷却，且有抛物线的石头。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/1%jinengxiaohao/doube/1%jinengjieyin/int/0%Soilcolddown/int/5%Goldcolddown/int/-1%Soilcoldignore/int/60";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水连鞭%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/水%belongTojianjie/char/在前方召唤水鞭，对前方挥舞三次，最后一击造成大幅击退。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/20%Watercolddown/int/10%Treecolddown/int/-2%Watercoldignore/int/20";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/飞羽冰花%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/水%belongTojianjie/char/召唤一个巨大雪花，弧线飞向前方，命中造成大量伤害。潜行状态下可改变冰花飞舞的方向。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.2%jinengjieyin/int/0%Watercolddown/int/6%Treecolddown/int/-1%Watercoldignore/int/12";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/满天花雨%belongTopinjietext/char/黄阶七级%belongTopinjie/int/7%belongToLeveltext/char/练气七层%belongToLevel/int/7%belongTobody/char/木%belongTojianjie/char/同一时间释放多个飞叶%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.5%jinengjieyin/int/10%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/40";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/控火术%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/火%belongTojianjie/char/前方如果有火焰方块，则瞬间引爆这些火焰。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/0.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-2%Firecoldignore/int/80";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/掌心雷%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/火%belongTojianjie/char/对前方范围敌方实体造成高额伤害并沉默。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.2%jinengxiaohao/doube/1%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/闪光术%belongTopinjietext/char/黄阶六级%belongTopinjie/int/6%belongToLeveltext/char/练气六层%belongToLevel/int/6%belongTobody/char/火%belongTojianjie/char/召唤强光快速闪一下，对周身范围内敌方实体造成致盲效果。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/1%jinengjieyin/int/10%Firecolddown/int/20%Soilcolddown/int/-2%Firecoldignore/int/40";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 23);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/碎裂金芒%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/金%belongTojianjie/char/连续按下右键发射小型追踪金芒。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0.5%jinengxiaohao/doube/0.5%jinengjieyin/int/0%Goldcolddown/int/10%Watercolddown/int/-2%Goldcoldignore/int/20";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/捆妖轮%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/金%belongTojianjie/char/命中敌方后，让其禁锢3秒。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/0.5%jinengxiaohao/doube/1%jinengjieyin/int/0%Goldcolddown/int/10%Watercolddown/int/-2%Goldcoldignore/int/10";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/金弧轮%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/金%belongTojianjie/char/发射一个金轮，金轮会以弧线飞向固定地点造成高额范围伤害。潜行状态下可改变金弧飞遁的方向。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Goldcolddown/int/5%Watercolddown/int/-1%Goldcoldignore/int/20";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/激流波%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/水%belongTojianjie/char/在前方召唤一条水流，造成范围伤害并击退。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.8%jinengxiaohao/doube/1.5%jinengjieyin/int/20%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/30";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/空浮球%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/水%belongTojianjie/char/前方召唤一个向前漂浮的水球，可以阻挡40点伤害。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0%jinengxiaohao/doube/1.4%jinengjieyin/int/20%Watercolddown/int/10%Treecolddown/int/-2%Watercoldignore/int/0";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/陨河术%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/水%belongTojianjie/char/前方召唤一个巨大水球，对中心敌方实体破除霸体，造成大量伤害，并大幅击退周围敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/20%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/10";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/水灵阵%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/前方召唤一个由数个水灵弹组成的阵法，对范围内敌方实体造成持续伤害。阵法持续8秒%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1%jinengxiaohao/doube/6%jinengjieyin/int/60%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/40";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/玲珑阵%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/前方召唤一个灵水组成的阵法，对范围内的友方实体恢复血量。阵法持续30秒%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/0%jinengxiaohao/doube/4%jinengjieyin/int/40%Watercolddown/int/40%Treecolddown/int/-6%Watercoldignore/int/10";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/连锥术%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/水%belongTojianjie/char/连续按下右键发射小型冰锥，命中造成伤害并增加2秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0.8%jinengxiaohao/doube/2%jinengjieyin/int/0%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/15";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/破碎冰矛%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/发射一根冰矛，命中敌方实体后，碎裂成多个冰锥，第一击会增加6秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Watercolddown/int/6%Treecolddown/int/-1%Watercoldignore/int/30";
            } else if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/裂冰斩%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/水%belongTojianjie/char/向前方斩出一道极寒气流，冻结沿途敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.4%jinengxiaohao/doube/1.2%jinengjieyin/int/10%Watercolddown/int/40%Treecolddown/int/-4%Watercoldignore/int/40";
            } else if (suijizhi == 12) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/生命感知%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/木%belongTojianjie/char/状态技能，让每个飞花都拥有自动追踪目标的能力。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/50";
            }
            if (suijizhi == 13) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/木篱术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/木%belongTojianjie/char/围绕自身生长木篱，用于抵挡远程伤害。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/3%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 14) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/乱木之种%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/木%belongTojianjie/char/前方种植一个隐蔽的小芽，会在一段时间后生长出一堆枯枝，造成小范围禁锢和伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/2%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/5%Firecolddown/int/-1%Treecoldignore/int/30";
            } else if (suijizhi == 15) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/地蔓鞭%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/木%belongTojianjie/char/在前方召唤一个藤鞭，会自动攻击附近敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/20%Treecolddown/int/15%Firecolddown/int/-5%Treecoldignore/int/40";
            } else if (suijizhi == 16) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/聚风术%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/木火%belongTojianjie/char/前方召唤一个飓风漩涡，让范围内敌方实体聚拢到中心。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/5%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 17) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/风飘术%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/木火%belongTojianjie/char/给自身加速一段时间，并且期间内飞行技能的速度更快，消耗更小。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 18) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/连环火球术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/火%belongTojianjie/char/发射伤害虽降低一点，但冷却更短的小火球。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/5%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 19) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/大火球%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/火%belongTojianjie/char/发射一枚巨大火球砸向前方，造成范围伤害并引燃。%jinengneeditem/char/无%jinengmanaatt/int/6%jinengshanghai/doube/2%jinengxiaohao/doube/3%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/60";
            } else if (suijizhi == 20) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/火焰护盾%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/火%belongTojianjie/char/一段时间内，受到的火系法术伤害降低30%%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/40";
            } else if (suijizhi == 21) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/天雷术%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/火%belongTojianjie/char/在前方召唤一道落雷，造成范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 22) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/幻光术%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/火%belongTojianjie/char/发射一个缓慢飞行的粉色光弹，对命中的敌方实体造成伤害并给予混乱效果。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 23) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/滚石术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/土%belongTojianjie/char/召唤一个巨大滚石，滚石向前方碾去，对范围内敌方实体造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/3%jinengjieyin/int/0%Soilcolddown/int/12%Goldcolddown/int/-4%Soilcoldignore/int/60";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/回身鞭%belongTopinjietext/char/玄阶七级%belongTopinjie/int/17%belongToLeveltext/char/筑基七阶%belongToLevel/int/17%belongTobody/char/木%belongTojianjie/char/向前方抽出一鞭，让前方范围内敌方实体抽到自身身边。%jinengneeditem/char/无%jinengmanaatt/int/6%jinengshanghai/doube/2%jinengxiaohao/doube/3%jinengjieyin/int/5%Treecolddown/int/15%Firecolddown/int/-5%Treecoldignore/int/30";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/火焰漩涡%belongTopinjietext/char/玄阶八级%belongTopinjie/int/18%belongToLeveltext/char/筑基八阶%belongToLevel/int/18%belongTobody/char/火%belongTojianjie/char/召唤一个火焰漩涡，把周围敌方实体吸到中心，并持续造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-5%Firecoldignore/int/40";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/万丈光芒%belongTopinjietext/char/玄阶八级%belongTopinjie/int/18%belongToLeveltext/char/筑基八阶%belongToLevel/int/18%belongTobody/char/火%belongTojianjie/char/以自身为中心对大范围的敌方实体造成高额伤害，如果敌方实体血量低于10%则直接抹杀，代价是扣除自身最大生命值30%的血量。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/1.5%jinengxiaohao/doube/10%jinengjieyin/int/30%Firecolddown/int/60%Soilcolddown/int/-6%Firecoldignore/int/30";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_027%jinengName/char/天外陨石%belongTopinjietext/char/玄阶九级%belongTopinjie/int/19%belongToLeveltext/char/筑基九阶%belongToLevel/int/19%belongTobody/char/土%belongTojianjie/char/在前方召唤一个从天而降的陨石，对范围内敌方实体造成高额伤害。%jinengneeditem/char/无%jinengmanaatt/int/7%jinengshanghai/doube/1.8%jinengxiaohao/doube/4%jinengjieyin/int/0%Soilcolddown/int/15%Goldcolddown/int/-5%Soilcoldignore/int/60";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/化石手%belongTopinjietext/char/玄阶六级%belongTopinjie/int/16%belongToLeveltext/char/筑基六阶%belongToLevel/int/16%belongTobody/char/土%belongTojianjie/char/前方召唤一个巨大的岩石巨手，此手会向前狠狠拍下，造成高额伤害，一定几率造成重伤。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.6%jinengxiaohao/doube/3%jinengjieyin/int/20%Soilcolddown/int/20%Goldcolddown/int/-10%Soilcoldignore/int/70";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/火鸟术%belongTopinjietext/char/玄阶九级%belongTopinjie/int/19%belongToLeveltext/char/筑基九阶%belongToLevel/int/19%belongTobody/char/火%belongTojianjie/char/召唤一只火鸟飞向敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/60";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/天堑金轮%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/金%belongTojianjie/char/发射一个巨大的垂直滚动的金色巨轮，对巨轮范围内敌方实体造成伤害，对命中敌人造成高额伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/3%jinengxiaohao/doube/3.5%jinengjieyin/int/20%Goldcolddown/int/20%Watercolddown/int/-4%Goldcoldignore/int/60";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水龙吟%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/水%belongTojianjie/char/在前方召唤水龙，对范围内的敌方实体造成持续伤害并持续击飞。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1.5%jinengxiaohao/doube/6%jinengjieyin/int/20%Watercolddown/int/30%Treecolddown/int/-6%Watercoldignore/int/40";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/冰天雪地%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/水%belongTojianjie/char/召唤一场暴风雪，对范围内的敌方实体造成持续伤害，并且每秒增加1.5秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/0.8%jinengxiaohao/doube/8%jinengjieyin/int/20%Watercolddown/int/40%Treecolddown/int/-4%Watercoldignore/int/10";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/灰木树梯%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/木%belongTojianjie/char/在脚下种植一个快速长高的种子，种子快速长大并弹飞友方实体。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/3%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/聚风化气%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/木火%belongTojianjie/char/前方召唤一个一直向前方移动的飓风漩涡，让范围内敌方实体聚拢到中心，并在飓风结束时造成范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2.5%jinengxiaohao/doube/4%jinengjieyin/int/5%Treecolddown/int/40%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/五雷轰顶%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/火%belongTojianjie/char/在前方召唤五道天雷，造成高额范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/40%Soilcolddown/int/-4%Firecoldignore/int/45";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/地滚雷%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/火%belongTojianjie/char/召唤一个在地面缓慢飘动的球形闪电，靠近敌方实体时爆炸，并用闪电链攻击附近其它敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/60";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/巨石锤%belongTopinjietext/char/地阶四级%belongTopinjie/int/26%belongToLeveltext/char/结丹四转%belongToLevel/int/26%belongTobody/char/土%belongTojianjie/char/召唤一个巨石头颅，向前方狠狠冲去，击退敌方实体，并有一定几率使其混乱。%jinengneeditem/char/无%jinengmanaatt/int/14%jinengshanghai/doube/2.5%jinengxiaohao/doube/5%jinengjieyin/int/10%Soilcolddown/int/30%Goldcolddown/int/-10%Soilcoldignore/int/80";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/火龙卷%belongTopinjietext/char/地阶六级%belongTopinjie/int/26%belongToLeveltext/char/结丹六转%belongToLevel/int/26%belongTobody/char/火%belongTojianjie/char/前方召唤一个巨大的火焰龙卷，引燃附近敌方实体并持续造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/30%Soilcolddown/int/-5%Firecoldignore/int/40";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/惊雷阵%belongTopinjietext/char/地阶七级%belongTopinjie/int/27%belongToLeveltext/char/结丹七转%belongToLevel/int/27%belongTobody/char/火%belongTojianjie/char/召唤一个雷电阵法，对阵法内敌方实体造成持续伤害。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.6%jinengxiaohao/doube/4%jinengjieyin/int/40%Firecolddown/int/60%Soilcolddown/int/-6%Firecoldignore/int/50";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/定魄神光%belongTopinjietext/char/地阶九级%belongTopinjie/int/29%belongToLeveltext/char/结丹九转%belongToLevel/int/29%belongTobody/char/火%belongTojianjie/char/发射一枚高速光弹，对命中敌方实体造成伤害并对其定身30秒%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/4%jinengjieyin/int/0%Firecolddown/int/40%Soilcolddown/int/-4%Firecoldignore/int/60";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 4);
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 4);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangJinshucailiaoProcedure，保留分支及随机边界。 */
    private static String tableJinshucailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaolevel = 0;
        double cailiaopinjie = 0;
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_201%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_211%无";
            }
            cailiaolevel = 3;
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_202%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_212%无";
            }
            cailiaolevel = 8;
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_203%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_213%无";
            }
            cailiaolevel = 13;
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_204%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_214%无";
            }
            cailiaolevel = 18;
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_205%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_215%无";
            }
            cailiaolevel = 23;
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_206%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_216%无";
            }
            cailiaolevel = 28;
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_207%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_217%无";
            }
            cailiaolevel = 33;
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_208%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_218%无";
            }
            cailiaolevel = 38;
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangLiandantuzhiProcedure，保留分支及随机边界。 */
    private static String tableLiandantuzhi(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "danfangid/char/yvanchuxiuzhen:book_030%danyaoname/char/一级辟谷丹%danfangshuoming/char/可以一段时间不用吃饭了。%danyaoshuoming/char/可以一段时间不用吃饭了。%item_level/char/一级%item_level_num/int/1%cailiao01name/char/林桃%cailiao01/char/yvanchuxiuzhen:peachpeach%cailiao02name/char/林桃%cailiao02/char/yvanchuxiuzhen:peachpeach%cailiao03name/char/林桃%cailiao03/char/yvanchuxiuzhen:peachpeach";
            } else if (suijizhi == 2) {
                neirong = "danfangid/char/yvanchuxiuzhen:book_030%danyaoname/char/一级止血丹%danfangshuoming/char/瞬间恢复一定血量后，开始缓慢恢复血量一段时间，是保命回血的好选择！%danyaoshuoming/char/瞬间恢复一定血量后，开始缓慢恢复血量一段时间，是保命回血的好选择！%item_level/char/一级%item_level_num/int/1%cailiao01name/char/拟耧斗菜%cailiao01/char/yvanchuxiuzhen:columbinemimosa%cailiao02name/char/拟耧斗菜%cailiao02/char/yvanchuxiuzhen:columbinemimosa%cailiao03name/char/拟耧斗菜%cailiao03/char/yvanchuxiuzhen:columbinemimosa%cailiao04name/char/拟耧斗菜%cailiao04/char/yvanchuxiuzhen:columbinemimosa%cailiao05name/char/鸭跖草%cailiao05/char/yvanchuxiuzhen:asiaticdayflower%cailiao06name/char/香樟果%cailiao06/char/yvanchuxiuzhen:camphorfruit";
            } else if (suijizhi == 3) {
                neirong = "danfangid/char/yvanchuxiuzhen:book_030%danyaoname/char/一级回春丹%danfangshuoming/char/缓慢恢复自身血量的丹药。%danyaoshuoming/char/缓慢恢复自身血量的丹药。%item_level/char/一级%item_level_num/int/1%cailiao01name/char/芷芳花%cailiao01/char/yvanchuxiuzhen:fragranceflower%cailiao02name/char/芷芳花%cailiao02/char/yvanchuxiuzhen:fragranceflower%cailiao03name/char/银杏果%cailiao03/char/yvanchuxiuzhen:ginkgofruit%cailiao04name/char/银杏果%cailiao04/char/yvanchuxiuzhen:ginkgofruit%cailiao05name/char/银杏果%cailiao05/char/yvanchuxiuzhen:ginkgofruit%cailiao06name/char/银杏果%cailiao06/char/yvanchuxiuzhen:ginkgofruit%cailiao07name/char/银杏果%cailiao07/char/yvanchuxiuzhen:ginkgofruit";
            } else if (suijizhi == 4) {
                neirong = "danfangid/char/yvanchuxiuzhen:book_030%danyaoname/char/一级堕血丹%danfangshuoming/char/缓慢恢复血量，但会伴随着同等级的虚弱。%danyaoshuoming/char/缓慢恢复血量，但会伴随着同等级的虚弱。%item_level/char/一级%item_level_num/int/1%cailiao01name/char/芷芳花%cailiao01/char/yvanchuxiuzhen:fragranceflower%cailiao02name/char/芷芳花%cailiao02/char/yvanchuxiuzhen:fragranceflower";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水连鞭%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/水%belongTojianjie/char/在前方召唤水鞭，对前方挥舞三次，最后一击造成大幅击退。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/20%Watercolddown/int/10%Treecolddown/int/-2%Watercoldignore/int/20";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/飞羽冰花%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/水%belongTojianjie/char/召唤一个巨大雪花，弧线飞向前方，命中造成大量伤害。潜行状态下可改变冰花飞舞的方向。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.2%jinengjieyin/int/0%Watercolddown/int/6%Treecolddown/int/-1%Watercoldignore/int/12";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/满天花雨%belongTopinjietext/char/黄阶七级%belongTopinjie/int/7%belongToLeveltext/char/练气七层%belongToLevel/int/7%belongTobody/char/木%belongTojianjie/char/同一时间释放多个飞叶%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.5%jinengjieyin/int/10%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/40";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/控火术%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/火%belongTojianjie/char/前方如果有火焰方块，则瞬间引爆这些火焰。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/0.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-2%Firecoldignore/int/80";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/掌心雷%belongTopinjietext/char/黄阶八级%belongTopinjie/int/8%belongToLeveltext/char/练气八层%belongToLevel/int/8%belongTobody/char/火%belongTojianjie/char/对前方范围敌方实体造成高额伤害并沉默。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.2%jinengxiaohao/doube/1%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/闪光术%belongTopinjietext/char/黄阶六级%belongTopinjie/int/6%belongToLeveltext/char/练气六层%belongToLevel/int/6%belongTobody/char/火%belongTojianjie/char/召唤强光快速闪一下，对周身范围内敌方实体造成致盲效果。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/1%jinengjieyin/int/10%Firecolddown/int/20%Soilcolddown/int/-2%Firecoldignore/int/40";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 23);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/碎裂金芒%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/金%belongTojianjie/char/连续按下右键发射小型追踪金芒。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/每发0.5%jinengxiaohao/doube/每发0.5%jinengjieyin/int/0%Goldcolddown/int/10%Watercolddown/int/-2%Goldcoldignore/int/20";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/捆妖轮%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/金%belongTojianjie/char/命中敌方后，让其禁锢3秒。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/0.5%jinengxiaohao/doube/1%jinengjieyin/int/0%Goldcolddown/int/10%Watercolddown/int/-2%Goldcoldignore/int/10";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/金弧轮%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/金%belongTojianjie/char/发射一个金轮，金轮会以弧线飞向固定地点造成高额范围伤害。潜行状态下可改变金弧飞遁的方向。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Goldcolddown/int/5%Watercolddown/int/-1%Goldcoldignore/int/20";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/激流波%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/水%belongTojianjie/char/在前方召唤一条水流，造成范围伤害并击退。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.8%jinengxiaohao/doube/1.5%jinengjieyin/int/20%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/30";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/空浮球%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/水%belongTojianjie/char/前方召唤一个向前漂浮的水球，可以阻挡40点伤害。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0%jinengxiaohao/doube/1.4%jinengjieyin/int/20%Watercolddown/int/10%Treecolddown/int/-2%Watercoldignore/int/0";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/陨河术%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/水%belongTojianjie/char/前方召唤一个巨大水球，对中心敌方实体破除霸体，造成大量伤害，并大幅击退周围敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/20%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/10";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/水灵阵%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/前方召唤一个由数个水灵弹组成的阵法，对范围内敌方实体造成持续伤害。阵法持续8秒%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/每段1%jinengxiaohao/doube/6%jinengjieyin/int/60%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/40";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/玲珑阵%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/前方召唤一个灵水组成的阵法，对范围内的友方实体恢复血量。阵法持续30秒%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/0%jinengxiaohao/doube/4%jinengjieyin/int/40%Watercolddown/int/40%Treecolddown/int/-6%Watercoldignore/int/10";
            } else if (suijizhi == 9) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/连锥术%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/水%belongTojianjie/char/连续按下右键发射小型冰锥，命中造成伤害并增加2秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/0.8%jinengxiaohao/doube/2%jinengjieyin/int/0%Watercolddown/int/20%Treecolddown/int/-4%Watercoldignore/int/15";
            } else if (suijizhi == 10) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/破碎冰矛%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/水%belongTojianjie/char/发射一根冰矛，命中敌方实体后，碎裂成多个冰锥，第一击会增加6秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Watercolddown/int/6%Treecolddown/int/-1%Watercoldignore/int/30";
            } else if (suijizhi == 11) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/裂冰斩%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/水%belongTojianjie/char/向前方斩出一道极寒气流，冻结沿途敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.4%jinengxiaohao/doube/1.2%jinengjieyin/int/10%Watercolddown/int/40%Treecolddown/int/-4%Watercoldignore/int/40";
            } else if (suijizhi == 12) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/生命感知%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/木%belongTojianjie/char/状态技能，让每个飞花都拥有自动追踪目标的能力。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/50";
            }
            if (suijizhi == 13) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/木篱术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/木%belongTojianjie/char/围绕自身生长木篱，用于抵挡远程伤害。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/3%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 14) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/乱木之种%belongTopinjietext/char/玄阶三级%belongTopinjie/int/13%belongToLeveltext/char/筑基三阶%belongToLevel/int/13%belongTobody/char/木%belongTojianjie/char/前方种植一个隐蔽的小芽，会在一段时间后生长出一堆枯枝，造成小范围禁锢和伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/2%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/5%Firecolddown/int/-1%Treecoldignore/int/30";
            } else if (suijizhi == 15) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/地蔓鞭%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/木%belongTojianjie/char/在前方召唤一个藤鞭，会自动攻击附近敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/20%Treecolddown/int/15%Firecolddown/int/-5%Treecoldignore/int/40";
            } else if (suijizhi == 16) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/聚风术%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/木火%belongTojianjie/char/前方召唤一个飓风漩涡，让范围内敌方实体聚拢到中心。%jinengneeditem/char/无%jinengmanaatt/int/3%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/5%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 17) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/风飘术%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/木火%belongTojianjie/char/给自身加速一段时间，并且期间内飞行技能的速度更快，消耗更小。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 18) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/连环火球术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/火%belongTojianjie/char/发射伤害虽降低一点，但冷却更短的小火球。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/5%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 19) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/大火球%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/火%belongTojianjie/char/发射一枚巨大火球砸向前方，造成范围伤害并引燃。%jinengneeditem/char/无%jinengmanaatt/int/6%jinengshanghai/doube/2%jinengxiaohao/doube/3%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/60";
            } else if (suijizhi == 20) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/火焰护盾%belongTopinjietext/char/玄阶二级%belongTopinjie/int/12%belongToLeveltext/char/筑基二阶%belongToLevel/int/12%belongTobody/char/火%belongTojianjie/char/一段时间内，受到的火系法术伤害降低30%%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/2%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/40";
            } else if (suijizhi == 21) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/天雷术%belongTopinjietext/char/玄阶五级%belongTopinjie/int/15%belongToLeveltext/char/筑基五阶%belongToLevel/int/15%belongTobody/char/火%belongTojianjie/char/在前方召唤一道落雷，造成范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 22) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/幻光术%belongTopinjietext/char/玄阶四级%belongTopinjie/int/14%belongToLeveltext/char/筑基四阶%belongToLevel/int/14%belongTobody/char/火%belongTojianjie/char/发射一个缓慢飞行的粉色光弹，对命中的敌方实体造成伤害并给予混乱效果。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/1.5%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/40";
            } else if (suijizhi == 23) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/滚石术%belongTopinjietext/char/玄阶一级%belongTopinjie/int/11%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/土%belongTojianjie/char/召唤一个巨大滚石，滚石向前方碾去，对范围内敌方实体造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/3%jinengjieyin/int/0%Soilcolddown/int/12%Goldcolddown/int/-4%Soilcoldignore/int/60";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 6);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_012%jinengName/char/回身鞭%belongTopinjietext/char/玄阶七级%belongTopinjie/int/17%belongToLeveltext/char/筑基七阶%belongToLevel/int/17%belongTobody/char/木%belongTojianjie/char/向前方抽出一鞭，让前方范围内敌方实体抽到自身身边。%jinengneeditem/char/无%jinengmanaatt/int/6%jinengshanghai/doube/2%jinengxiaohao/doube/3%jinengjieyin/int/5%Treecolddown/int/15%Firecolddown/int/-5%Treecoldignore/int/30";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/火焰漩涡%belongTopinjietext/char/玄阶八级%belongTopinjie/int/18%belongToLeveltext/char/筑基八阶%belongToLevel/int/18%belongTobody/char/火%belongTojianjie/char/召唤一个火焰漩涡，把周围敌方实体吸到中心，并持续造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.2%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/20%Soilcolddown/int/-5%Firecoldignore/int/40";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/万丈光芒%belongTopinjietext/char/玄阶八级%belongTopinjie/int/18%belongToLeveltext/char/筑基八阶%belongToLevel/int/18%belongTobody/char/火%belongTojianjie/char/以自身为中心对大范围的敌方实体造成高额伤害，如果敌方实体血量低于10%则直接抹杀，代价是扣除自身最大生命值30%的血量。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/1.5%jinengxiaohao/doube/10%jinengjieyin/int/30%Firecolddown/int/60%Soilcolddown/int/-6%Firecoldignore/int/30";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_027%jinengName/char/天外陨石%belongTopinjietext/char/玄阶九级%belongTopinjie/int/19%belongToLeveltext/char/筑基九阶%belongToLevel/int/19%belongTobody/char/土%belongTojianjie/char/在前方召唤一个从天而降的陨石，对范围内敌方实体造成高额伤害。%jinengneeditem/char/无%jinengmanaatt/int/7%jinengshanghai/doube/1.8%jinengxiaohao/doube/4%jinengjieyin/int/0%Soilcolddown/int/15%Goldcolddown/int/-5%Soilcoldignore/int/60";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/化石手%belongTopinjietext/char/玄阶六级%belongTopinjie/int/16%belongToLeveltext/char/筑基六阶%belongToLevel/int/16%belongTobody/char/土%belongTojianjie/char/前方召唤一个巨大的岩石巨手，此手会向前狠狠拍下，造成高额伤害，一定几率造成重伤。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.6%jinengxiaohao/doube/3%jinengjieyin/int/20%Soilcolddown/int/20%Goldcolddown/int/-10%Soilcoldignore/int/70";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_026%jinengName/char/火鸟术%belongTopinjietext/char/玄阶九级%belongTopinjie/int/19%belongToLeveltext/char/筑基九阶%belongToLevel/int/19%belongTobody/char/火%belongTojianjie/char/召唤一只火鸟飞向敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/%Firecolddown/int/20%Soilcolddown/int/-4%Firecoldignore/int/60";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_003%jinengName/char/天堑金轮%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/金%belongTojianjie/char/发射一个巨大的垂直滚动的金色巨轮，对巨轮范围内敌方实体造成伤害，对命中敌人造成高额伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/3%jinengxiaohao/doube/3.5%jinengjieyin/int/20%Goldcolddown/int/20%Watercolddown/int/-4%Goldcoldignore/int/60";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_005%jinengName/char/水龙吟%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/水%belongTojianjie/char/在前方召唤水龙，对范围内的敌方实体造成持续伤害并持续击飞。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/每段1.5%jinengxiaohao/doube/6%jinengjieyin/int/20%Watercolddown/int/30%Treecolddown/int/-6%Watercoldignore/int/40";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_024%jinengName/char/冰天雪地%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/水%belongTojianjie/char/召唤一场暴风雪，对范围内的敌方实体造成持续伤害，并且每秒增加1.5秒冷冻。%jinengneeditem/char/无%jinengmanaatt/int/4%jinengshanghai/doube/0.8%jinengxiaohao/doube/8%jinengjieyin/int/20%Watercolddown/int/40%Treecolddown/int/-4%Watercoldignore/int/10";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_004%jinengName/char/灰木树梯%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/木%belongTojianjie/char/在脚下种植一个快速长高的种子，种子快速长大并弹飞友方实体。%jinengneeditem/char/无%jinengmanaatt/int/1%jinengshanghai/doube/1%jinengxiaohao/doube/3%jinengjieyin/int/0%Treecolddown/int/20%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_010%jinengName/char/聚风化气%belongTopinjietext/char/地阶一级%belongTopinjie/int/21%belongToLeveltext/char/结丹一转%belongToLevel/int/21%belongTobody/char/木火%belongTojianjie/char/前方召唤一个一直向前方移动的飓风漩涡，让范围内敌方实体聚拢到中心，并在飓风结束时造成范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2.5%jinengxiaohao/doube/4%jinengjieyin/int/5%Treecolddown/int/40%Firecolddown/int/-4%Treecoldignore/int/60";
            } else if (suijizhi == 6) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/五雷轰顶%belongTopinjietext/char/地阶五级%belongTopinjie/int/25%belongToLeveltext/char/结丹五转%belongToLevel/int/25%belongTobody/char/火%belongTojianjie/char/在前方召唤五道天雷，造成高额范围伤害。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/40%Soilcolddown/int/-4%Firecoldignore/int/45";
            } else if (suijizhi == 7) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/地滚雷%belongTopinjietext/char/地阶三级%belongTopinjie/int/23%belongToLeveltext/char/结丹三转%belongToLevel/int/23%belongTobody/char/火%belongTojianjie/char/召唤一个在地面缓慢飘动的球形闪电，靠近敌方实体时爆炸，并用闪电链攻击附近其它敌方实体。%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/1.5%jinengxiaohao/doube/2%jinengjieyin/int/0%Firecolddown/int/10%Soilcolddown/int/-1%Firecoldignore/int/60";
            } else if (suijizhi == 8) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_008%jinengName/char/巨石锤%belongTopinjietext/char/地阶四级%belongTopinjie/int/26%belongToLeveltext/char/结丹四转%belongToLevel/int/26%belongTobody/char/土%belongTojianjie/char/召唤一个巨石头颅，向前方狠狠冲去，击退敌方实体，并有一定几率使其混乱。%jinengneeditem/char/无%jinengmanaatt/int/14%jinengshanghai/doube/2.5%jinengxiaohao/doube/5%jinengjieyin/int/10%Soilcolddown/int/30%Goldcolddown/int/-10%Soilcoldignore/int/80";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_006%jinengName/char/火龙卷%belongTopinjietext/char/地阶六级%belongTopinjie/int/26%belongToLeveltext/char/结丹六转%belongToLevel/int/26%belongTobody/char/火%belongTojianjie/char/前方召唤一个巨大的火焰龙卷，引燃附近敌方实体并持续造成伤害。%jinengneeditem/char/无%jinengmanaatt/int/5%jinengshanghai/doube/1.5%jinengxiaohao/doube/4%jinengjieyin/int/20%Firecolddown/int/30%Soilcolddown/int/-5%Firecoldignore/int/40";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_023%jinengName/char/惊雷阵%belongTopinjietext/char/地阶七级%belongTopinjie/int/27%belongToLeveltext/char/结丹七转%belongToLevel/int/27%belongTobody/char/火%belongTojianjie/char/召唤一个雷电阵法，对阵法内敌方实体造成持续伤害。%jinengneeditem/char/无%jinengmanaatt/int/2%jinengshanghai/doube/1.6%jinengxiaohao/doube/4%jinengjieyin/int/40%Firecolddown/int/60%Soilcolddown/int/-6%Firecoldignore/int/50";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_029%jinengName/char/定魄神光%belongTopinjietext/char/地阶九级%belongTopinjie/int/29%belongToLeveltext/char/结丹九转%belongToLevel/int/29%belongTobody/char/火%belongTojianjie/char/发射一枚高速光弹，对命中敌方实体造成伤害并对其定身30秒%jinengneeditem/char/无%jinengmanaatt/int/10%jinengshanghai/doube/2%jinengxiaohao/doube/4%jinengjieyin/int/0%Firecolddown/int/40%Soilcolddown/int/-4%Firecoldignore/int/60";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 4);
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 4);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangLingmucailiaoProcedure，保留分支及随机边界。 */
    private static String tableLingmucailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaopinjie = 0;
        double cailiaolevel = 0;
        if (level <= 3) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_087%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_088%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_089%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_090%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_241%无";
            }
            cailiaolevel = Mth.nextInt(random, 1, 15);
        } else {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_090%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_091%无";
            } else {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_242%无";
            }
            cailiaolevel = Mth.nextInt(random, 15, 40);
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangLingshiProcedure，保留分支及随机边界。 */
    private static String tableLingshi(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaopinjie = 0;
        double cailiaolevel = 0;
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_191%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_192%无";
            }
            cailiaolevel = 5;
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_192%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_193%无";
            }
            cailiaolevel = 10;
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_193%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_194%无";
            }
            cailiaolevel = 15;
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_194%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_195%无";
            }
            cailiaolevel = 20;
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_195%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_196%无";
            }
            cailiaolevel = 25;
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_196%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_197%无";
            }
            cailiaolevel = 30;
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_197%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_198%无";
            }
            cailiaolevel = 35;
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_198%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_199%无";
            }
            cailiaolevel = 40;
        }
        cailiaopinjie = 4;
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangShenfaProcedure，保留分支及随机边界。 */
    private static String tableShenfa(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/飞步%belongTopinjietext/char/黄阶%belongTopinjie/int/1%belongToLeveltext/char/凡人%belongToLevel/int/0%belongTobody/char/无%belongTojianjie/char/双击W时会向前跳跃，冷却一段时间后才会再次触发%shenfatick/int/20%shenfatickMax/int/10%shenfaWjump/double/1";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/灵步%belongTopinjietext/char/黄阶%belongTopinjie/int/1%belongToLeveltext/char/凡人%belongToLevel/int/0%belongTobody/char/无%belongTojianjie/char/双击W、A、S、D可以向四方跳跃，但冷却时间稍微长一些。%shenfatick/int/40%shenfatickMax/int/40%shenfaWjump/double/0.6%shenfaSjump/double/0.6%shenfaAjump/double/0.6%shenfaDjump/double/0.6";
            }
        } else if (level <= 4) {
            suijizhi = Mth.nextInt(random, 1, 5);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/青云步%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/无%belongTojianjie/char/双击W时，朝向鼠标方向快速弹射过去，并且玩家可以二连跳%shenfatick/int/20%shenfatickMax/int/5%shenfajumptick/int/20%shenfajumptickMax/int/10%shenfaWjump/double/1";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/神行步%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/无%belongTojianjie/char/双击W时会获得加速buff持续8秒，冷却一段时间后才会再次触发%shenfatick/int/80%shenfatickMax/int/10%shenfaWjump/double/1%shenfaWspeed/int/2";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/分烟步%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/无%belongTojianjie/char/双击W时，朝向鼠标方向快速弹射过去，冷却一段时间后才会再次触发。跳跃前会爆出一大团烟云，并让玩家隐形%shenfatick/int/20%shenfatickMax/int/40%shenfaWjump/double/1.2";
            } else if (suijizhi == 4) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/高飞步%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/无%belongTojianjie/char/双击W时，朝向鼠标方向快速弹射过去，而且弹射距离更远%shenfatick/int/20%shenfatickMax/int/10%shenfaWjump/double/2";
            } else if (suijizhi == 5) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/踏风步%belongTopinjietext/char/玄阶%belongTopinjie/int/11%belongToLeveltext/char/练气一层%belongToLevel/int/1%belongTobody/char/无%belongTojianjie/char/玩家连续按下方向键时，会朝向对应方向跳跃。%shenfatick/int/10%shenfatickMax/int/10%shenfaWjump/double/1.5%shenfaSjump/double/1.5%shenfaAjump/double/1.5%shenfaDjump/double/1.5";
            }
        } else if (level <= 6) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/八步赶蝉%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/双击W时，朝向鼠标方向快速弹射过去，并且玩家可以连跳八次%shenfatick/int/10%shenfatickMax/int/80%shenfajumptick/int/10%shenfajumptickMax/int/80%shenfaWjump/double/1";
            } else if (suijizhi == 2) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/分形捉影%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/双击W时，朝向鼠标方向快速弹射过去，同时隐形。并且玩家可以三连跳%shenfatick/int/20%shenfatickMax/int/60%shenfajumptick/int/20%shenfajumptickMax/int/60%shenfaWjump/double/1.5";
            } else if (suijizhi == 3) {
                neirong = "belongToItemID/char/yvanchuxiuzhen:book_022%shenfaName/char/十方追月%belongTopinjietext/char/地阶%belongTopinjie/int/21%belongToLeveltext/char/筑基一阶%belongToLevel/int/11%belongTobody/char/无%belongTojianjie/char/玩家连续按下方向键时，会朝向对应方向跳跃，连续空格和潜行也是%shenfatick/int/20%shenfatickMax/int/40%shenfajumptick/int/20%shenfajumptickMax/int/10%shenfaWjump/double/1.5%shenfaSjump/double/1.5%shenfaAjump/double/1.5%shenfaDjump/double/1.5";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangShipinProcedure，保留分支及随机边界。 */
    private static String tableShipin(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_01 无 头饰类饰品，一般注重冷却缩减。 头饰 黄阶四级 4 yvanchuxiuzhen colddown 1";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_01 无 项坠类饰品，一般注重血量恢复速度。 项坠 黄阶四级 4 health_Rec int 1";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_01 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 黄阶四级 4 power_Rec int 2 soul_Rec int 2";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_01 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 黄阶四级 4 power_Max int 80 soul_Max int 80";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_02 无 头饰类饰品，一般注重冷却缩减。 头饰 黄阶九级 9 yvanchuxiuzhen colddown 2";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_02 无 项坠类饰品，一般注重血量恢复速度。 项坠 黄阶九级 9 health_Rec int 2";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_02 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 黄阶九级 9 power_Rec int 4 soul_Rec int 4";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_02 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 黄阶九级 9 power_Max int 160 soul_Max int 160";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_03 无 头饰类饰品，一般注重冷却缩减。 头饰 玄阶四级 14 yvanchuxiuzhen colddown 3";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_03 无 项坠类饰品，一般注重血量恢复速度。 项坠 玄阶四级 14 health_Rec int 3";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_03 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 玄阶四级 14 power_Rec int 6 soul_Rec int 6";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_03 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 玄阶四级 14 power_Max int 240 soul_Max int 240";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_04 无 头饰类饰品，一般注重冷却缩减。 头饰 玄阶九级 19 yvanchuxiuzhen colddown 4";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_04 无 项坠类饰品，一般注重血量恢复速度。 项坠 玄阶九级 19 health_Rec int 4";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_04 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 玄阶九级 19 power_Rec int 8 soul_Rec int 8";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_04 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 玄阶九级 19 power_Max int 320 soul_Max int 320";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_05 无 头饰类饰品，一般注重冷却缩减。 头饰 地阶四级 24 yvanchuxiuzhen colddown 5";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_05 无 项坠类饰品，一般注重血量恢复速度。 项坠 地阶四级 24 health_Rec int 5";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_05 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 地阶四级 24 power_Rec int 10 soul_Rec int 10";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_05 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 地阶四级 24 power_Max int 400 soul_Max int 400";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_06 无 头饰类饰品，一般注重冷却缩减。 头饰 地阶九级 29 yvanchuxiuzhen colddown 6";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_06 无 项坠类饰品，一般注重血量恢复速度。 项坠 地阶九级 29 health_Rec int 6";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_06 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 地阶九级 29 power_Rec int 12 soul_Rec int 12";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_06 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 地阶九级 29 power_Max int 480 soul_Max int 480";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_07 无 头饰类饰品，一般注重冷却缩减。 头饰 天阶四级 34 yvanchuxiuzhen colddown 7";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_07 无 项坠类饰品，一般注重血量恢复速度。 项坠 天阶四级 34 health_Rec int 7";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_07 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 天阶四级 34 power_Rec int 14 soul_Rec int 14";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_07 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 天阶四级 34 power_Max int 560 soul_Max int 560";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 4);
            if (suijizhi == 1) {
                neirong = "指令%shipin yvanchuxiuzhen:headornaments_08 无 头饰类饰品，一般注重冷却缩减。 头饰 天阶九级 39 yvanchuxiuzhen colddown 8";
            } else if (suijizhi == 2) {
                neirong = "指令%shipin yvanchuxiuzhen:necklaceornaments_08 无 项坠类饰品，一般注重血量恢复速度。 项坠 天阶九级 39 health_Rec int 8";
            } else if (suijizhi == 3) {
                neirong = "指令%shipin yvanchuxiuzhen:handornaments_08 无 手饰类饰品，一般注重灵力与精力的恢复速度。 手饰 天阶九级 39 power_Rec int 16 soul_Rec int 16";
            } else if (suijizhi == 4) {
                neirong = "指令%shipin yvanchuxiuzhen:beltornaments_08 无 腰饰类饰品，一般注重增加灵力与精力的上限。 腰饰 天阶九级 39 power_Max int 640 soul_Max int 640";
            } else if (suijizhi == 5) {
                neirong = "";
            } else if (suijizhi == 6) {
                neirong = "";
            } else if (suijizhi == 7) {
                neirong = "";
            } else if (suijizhi == 8) {
                neirong = "";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangWuqiProcedure，保留分支及随机边界。 */
    private static String tableWuqi(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_01 精铁剑 剑类武器，拥有最平衡的属性。 武器 黄阶二级 2 6 3 1 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_01 雕纹木剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 黄阶一级 1 3 5 3 0 0 0 90 1 minecraft smite 1";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_01 玄铁巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 黄阶三级 3 8 3 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_01 环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 黄阶三级 3 7 2 0 0.05 0 0 100 1 yvanchuxiuzhen critical 1";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_01 红缨枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 黄阶二级 2 6 1 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_01 启灵拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 黄阶一级 1 1 5 2 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_01 风语弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 黄阶二级 2 5 3 1 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_01 蓝羽长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 黄阶三级 3 2 7 1 0 0.05 0 100";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_02 精钢剑 剑类武器，拥有最平衡的属性。 武器 黄阶七级 7 10 4 2 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_02 桃木剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 黄阶六级 6 6 9 6 0 0 0 90 1 minecraft smite 2";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_02 玄钢巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 黄阶八级 8 12 4 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_02 寒光环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 黄阶八级 8 11 3 0 0.05 0 0 100 1 yvanchuxiuzhen critical 2";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_02 决铁枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 黄阶七级 7 10 2 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_02 聚灵拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 黄阶六级 6 1 9 4 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_02 破风弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 黄阶七级 7 8 4 2 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_02 湍流长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 黄阶八级 8 3 11 2 0 0.05 0 100";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_03 寒铁剑 剑类武器，拥有最平衡的属性。 武器 玄阶二级 12 16 6 2 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_03 乌木剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 玄阶一级 11 9 14 9 0 0 0 90 1 minecraft smite 3";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_03 陨铁巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 玄阶三级 13 19 7 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_03 骤风环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 玄阶三级 13 17 5 0 0.05 0 0 100 1 yvanchuxiuzhen critical 3";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_03 湍流枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 玄阶二级 12 16 2 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_03 灵桥拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 玄阶一级 11 2 14 6 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_03 羚角弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 玄阶二级 12 12 6 2 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_03 硬角长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 玄阶三级 13 5 17 3 0 0.05 0 100";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_04 寒锋剑 剑类武器，拥有最平衡的属性。 武器 玄阶七级 17 24 9 3 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_04 血桃木剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 玄阶六级 16 14 22 14 0 0 0 90 1 minecraft smite 4";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_04 陨钢巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 玄阶八级 18 28 10 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_04 寂峦环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 玄阶八级 18 25 7 0 0.05 0 0 100 1 yvanchuxiuzhen critical 4";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_04 寒杀枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 玄阶七级 17 24 3 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_04 化露拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 玄阶六级 16 3 22 9 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_04 月虎弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 玄阶七级 17 18 9 3 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_04 冰魔长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 玄阶八级 18 7 25 4 0 0.05 0 100";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_05 三才剑 剑类武器，拥有最平衡的属性。 武器 地阶二级 22 33 13 5 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_05 雷击木剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 地阶一级 21 19 31 19 0 0 0 90 1 minecraft smite 5";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_05 熔融巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 地阶三级 23 40 14 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_05 冰流环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 地阶三级 23 36 9 0 0.05 0 0 100 1 yvanchuxiuzhen critical 5";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_05 破空枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 地阶二级 22 33 5 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_05 幻烟拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 地阶一级 21 4 31 12 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_05 释影弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 地阶二级 22 25 13 5 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_05 丹鹰长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 地阶三级 23 9 36 5 0 0.05 0 100";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_06 轮回剑 剑类武器，拥有最平衡的属性。 武器 地阶七级 27 45 17 6 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_06 心泉剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 地阶六级 26 27 43 27 0 0 0 90 1 minecraft smite 6";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_06 山岳巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 地阶八级 28 54 18 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_06 飞云环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 地阶八级 28 48 12 0 0.05 0 0 100 1 yvanchuxiuzhen critical 6";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_06 陨星枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 地阶七级 27 45 6 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_06 凌虚拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 地阶六级 26 5 43 16 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_06 瞬雷弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 地阶七级 27 34 17 6 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_06 诡锥长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 地阶八级 28 12 48 6 0 0.05 0 100";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_07 五行八卦剑 剑类武器，拥有最平衡的属性。 武器 天阶二级 32 59 22 8 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_07 回风剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 天阶一级 31 35 56 35 0 0 0 90 1 minecraft smite 7";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_07 百炼巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 天阶三级 33 70 24 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_07 火雨环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 天阶三级 33 62 16 0 0.05 0 0 100 1 yvanchuxiuzhen critical 7";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_07 甲皇枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 天阶二级 32 59 8 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_07 皓月拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 天阶一级 31 6 56 21 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_07 元合弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 天阶二级 32 44 22 8 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_07 翠鸣长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 天阶三级 33 16 62 8 0 0.05 0 100";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 8);
            if (suijizhi == 1) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponsword_08 阴阳两仪剑 剑类武器，拥有最平衡的属性。 武器 天阶七级 37 75 28 10 0 0 0 90";
            } else if (suijizhi == 2) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponwoodsword_08 雀鸣剑 木剑类武器，对不死族拥有更高的杀伤力。 武器 天阶六级 36 45 72 45 0 0 0 90 1 minecraft smite 8";
            } else if (suijizhi == 3) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponepee_08 基石巨剑 巨剑类武器，挥舞速度较慢，但物理攻击强大。 武器 天阶八级 38 88 30 0 -0.05 -0.05 0.1 110";
            } else if (suijizhi == 4) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponbroadsword_08 皓月环首刀 环首刀是不错的近战武器，拥有比剑系武器更高的物理攻击。 武器 天阶八级 38 78 20 0 0.05 0 0 100 1 yvanchuxiuzhen critical 8";
            } else if (suijizhi == 5) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspear_08 神源枪 枪系武器拥有较远的攻击距离，和不错的近战伤害。 武器 天阶七级 37 75 10 0 0 0.05 0 100";
            } else if (suijizhi == 6) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponhossu_08 残阳拂尘 拂尘法术攻击很高，并且挥舞时释放法术伤害。 武器 天阶六级 36 8 72 27 -0.1 0 0 90";
            } else if (suijizhi == 7) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponspeedbow_08 万宝弓 快弓类武器是有一定近战能力的远程武器，并且射击频率很高。 武器 天阶七级 37 56 28 10 0 0 0 90";
            } else if (suijizhi == 8) {
                neirong = "指令%lianqi yvanchuxiuzhen:weaponlongbow_08 金乌长弓 长弓类武器攻击距离最远，并且射击技能搭配特殊箭矢可以造成各种强大效果。 武器 天阶八级 38 20 78 10 0 0.05 0 100";
            }
        }
        return neirong;

    }

    /** 来源 B:BaoxiangXiuweidanyaoProcedure，保留分支及随机边界。 */
    private static String tableXiuweidanyao(double level, RandomSource random) {
        double danyaonum = 0;
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/一级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/1%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/一级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/1%danyaotick0/int/40%danyaonum/int/";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/二级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/2%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/二级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/2%danyaotick0/int/60%danyaonum/int/";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/三级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/3%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/三级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/3%danyaotick0/int/80%danyaonum/int/";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/四级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/4%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/四级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/4%danyaotick0/int/100%danyaonum/int/";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/五级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/5%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/五级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/5%danyaotick0/int/120%danyaonum/int/";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/六级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/6%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/六级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/6%danyaotick0/int/140%danyaonum/int/";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/七级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/7%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/七级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/7%danyaotick0/int/160%danyaonum/int/";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/八级腾龙丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_016%danyaolevel0/int/8%danyaotick0/int/0%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_0501%danyaoname/char/八级培元丹%shuliang/int/1%danyaoxiaoguo0/char/yvanchuxiuzhen:effect_015%danyaolevel0/int/8%danyaotick0/int/180%danyaonum/int/";
            }
        }
        danyaonum = Mth.nextInt(random, 4, 16);
        neirong = neirong + "" + new DecimalFormat("").format(danyaonum) + "%danyaobaoxiang/logic/真";
        return neirong;

    }

    /** 来源 B:BaoxiangYaodancailiaoProcedure，保留分支及随机边界。 */
    private static String tableYaodancailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaopinjie = 0;
        double cailiaolevel = 0;
        if (level <= 1) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_181%无";
            cailiaolevel = Mth.nextInt(random, 1, 5);
        } else if (level == 2) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_182%无";
            cailiaolevel = Mth.nextInt(random, 6, 10);
        } else if (level == 3) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_183%无";
            cailiaolevel = Mth.nextInt(random, 11, 15);
        } else if (level == 4) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_184%无";
            cailiaolevel = Mth.nextInt(random, 16, 20);
        } else if (level == 5) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_185%无";
            cailiaolevel = Mth.nextInt(random, 21, 25);
        } else if (level == 6) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_186%无";
            cailiaolevel = Mth.nextInt(random, 26, 30);
        } else if (level == 7) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_187%无";
            cailiaolevel = Mth.nextInt(random, 31, 35);
        } else if (level == 8) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_188%无";
            cailiaolevel = Mth.nextInt(random, 36, 40);
        } else if (level >= 9) {
            neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_189%无";
            cailiaolevel = Mth.nextInt(random, 41, 45);
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangYaoshoucailiaoProcedure，保留分支及随机边界。 */
    private static String tableYaoshoucailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaolevel = 0;
        double cailiaopinjie = 0;
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 13);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_001%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_021%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_037%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_038%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_044%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_045%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_069%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_087%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_088%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_089%无";
            }
            if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_092%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_093%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_094%无";
            }
            cailiaolevel = Mth.nextInt(random, 1, 5);
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 16);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_002%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_022%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_039%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_046%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_047%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_048%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_049%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_060%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_070%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_071%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_078%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_090%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_095%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_096%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_097%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_141%无";
            }
            cailiaolevel = Mth.nextInt(random, 6, 10);
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 22);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_003%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_004%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_005%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_006%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_027%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_031%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_040%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_050%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_051%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_052%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_072%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_073%无";
            }
            if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_074%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_079%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_080%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_098%无";
            } else if (suijizhi == 17) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_099%无";
            } else if (suijizhi == 18) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_100%无";
            } else if (suijizhi == 19) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_101%无";
            } else if (suijizhi == 20) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_102%无";
            } else if (suijizhi == 21) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_142%无";
            } else if (suijizhi == 22) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_153%无";
            }
            cailiaolevel = Mth.nextInt(random, 11, 15);
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 16);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_007%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_008%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_023%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_032%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_041%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_042%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_061%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_062%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_081%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_103%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_104%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_105%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_106%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_107%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_143%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_144%无";
            }
            cailiaolevel = Mth.nextInt(random, 16, 20);
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 21);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_009%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_010%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_011%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_012%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_013%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_024%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_033%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_034%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_053%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_054%无";
            }
            if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_063%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_064%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_082%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_108%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_109%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_110%无";
            } else if (suijizhi == 17) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_111%无";
            } else if (suijizhi == 18) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_112%无";
            } else if (suijizhi == 19) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_113%无";
            } else if (suijizhi == 20) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_114%无";
            } else if (suijizhi == 21) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_145%无";
            }
            cailiaolevel = Mth.nextInt(random, 21, 25);
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 14);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_014%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_015%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_028%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_035%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_055%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_075%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_091%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_115%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_116%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_117%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_118%无";
            } else if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_119%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_120%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_146%无";
            }
            cailiaolevel = Mth.nextInt(random, 26, 30);
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 22);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_016%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_017%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_025%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_029%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_043%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_056%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_065%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_066%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_067%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_076%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_077%无";
            }
            if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_083%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_084%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_121%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_122%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_123%无";
            } else if (suijizhi == 17) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_124%无";
            } else if (suijizhi == 18) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_125%无";
            } else if (suijizhi == 19) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_126%无";
            } else if (suijizhi == 20) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_127%无";
            } else if (suijizhi == 21) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_128%无";
            } else if (suijizhi == 22) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_129%无";
            }
            cailiaolevel = Mth.nextInt(random, 31, 35);
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 23);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_018%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_019%无";
            } else if (suijizhi == 3) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_020%无";
            } else if (suijizhi == 4) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_026%无";
            } else if (suijizhi == 5) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_030%无";
            } else if (suijizhi == 6) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_036%无";
            } else if (suijizhi == 7) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_057%无";
            } else if (suijizhi == 8) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_058%无";
            } else if (suijizhi == 9) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_059%无";
            } else if (suijizhi == 10) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_068%无";
            } else if (suijizhi == 11) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_085%无";
            }
            if (suijizhi == 12) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_086%无";
            } else if (suijizhi == 13) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_130%无";
            } else if (suijizhi == 14) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_131%无";
            } else if (suijizhi == 15) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_132%无";
            } else if (suijizhi == 16) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_133%无";
            } else if (suijizhi == 17) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_134%无";
            } else if (suijizhi == 18) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_135%无";
            } else if (suijizhi == 19) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_136%无";
            } else if (suijizhi == 20) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_137%无";
            } else if (suijizhi == 21) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_138%无";
            } else if (suijizhi == 22) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_139%无";
            } else if (suijizhi == 23) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_140%无";
            }
            cailiaolevel = Mth.nextInt(random, 36, 40);
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangYihuocailiaoProcedure，保留分支及随机边界。 */
    private static String tableYihuocailiao(double level, RandomSource random) {
        String neirong = "";
        double suijizhi = 0;
        double cailiaopinjie = 0;
        double cailiaolevel = 0;
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_221%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_231%无";
            }
            cailiaolevel = 5;
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_222%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_232%无";
            }
            cailiaolevel = 10;
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_223%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_233%无";
            }
            cailiaolevel = 15;
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_224%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_234%无";
            }
            cailiaolevel = 20;
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_225%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_235%无";
            }
            cailiaolevel = 25;
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_226%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_236%无";
            }
            cailiaolevel = 30;
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_227%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_237%无";
            }
            cailiaolevel = 35;
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 2);
            if (suijizhi == 1) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_228%无";
            } else if (suijizhi == 2) {
                neirong = "cailiaoID/char/yvanchuxiuzhen:cailiao_238%无";
            }
            cailiaolevel = 40;
        }
        cailiaopinjie = materialQuality(level, random);
        neirong = neirong + "%cailiaolevel/int/" + new DecimalFormat("0").format(cailiaolevel) + "%cailiaopinjie/int/" + new DecimalFormat("0").format(cailiaopinjie);
        return neirong;

    }

    /** 来源 B:BaoxiangZhuangbeiProcedure，保留分支及随机边界。 */
    private static String tableZhuangbei(double level, RandomSource random) {
        double suijizhi = 0;
        String neirong = "";
        suijizhi = Mth.nextInt(random, 1, 3);
        if (suijizhi == 1) {
            neirong = tableWuqi(level, random);
        } else if (suijizhi == 2) {
            neirong = tableFabao(level, random);
        } else if (suijizhi == 3) {
            neirong = tableHujia(level, random);
        }
        return neirong;

    }

    /** 来源 B:BaoxiangZhuangtaidanyaoProcedure，保留分支及随机边界。 */
    private static String tableZhuangtaidanyao(double level, RandomSource random) {
        double danyaonum = 0;
        double suijizhi = 0;
        String neirong = "";
        if (level <= 1) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/一级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/一级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/一级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/1%danyaotick0/int/120%danyaonum/int/";
            }
        } else if (level == 2) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/二级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/二级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/二级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/2%danyaotick0/int/240%danyaonum/int/";
            }
        } else if (level == 3) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/三级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/三级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/三级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/3%danyaotick0/int/360%danyaonum/int/";
            }
        } else if (level == 4) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/四级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/四级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/四级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/4%danyaotick0/int/480%danyaonum/int/";
            }
        } else if (level == 5) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/五级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/五级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/五级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/5%danyaotick0/int/600%danyaonum/int/";
            }
        } else if (level == 6) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/六级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/六级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/六级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/6%danyaotick0/int/720%danyaonum/int/";
            }
        } else if (level == 7) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/七级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/七级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/七级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/7%danyaotick0/int/840%danyaonum/int/";
            }
        } else if (level >= 8) {
            suijizhi = Mth.nextInt(random, 1, 3);
            if (suijizhi == 1) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/八级辟谷丹%shuliang/int/1%danyaoxiaoguo0/char/saturation%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            } else if (suijizhi == 2) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_01%danyaoname/char/八级避水丹%shuliang/int/1%danyaoxiaoguo0/char/water_breathing%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            } else if (suijizhi == 3) {
                neirong = "danyaoid/char/yvanchuxiuzhen:itemdanyaohulu_06%danyaoname/char/八级铁肤丹%shuliang/int/1%danyaoxiaoguo0/char/absorption%danyaolevel0/int/8%danyaotick0/int/960%danyaonum/int/";
            }
        }
        danyaonum = Mth.nextInt(random, 4, 16);
        neirong = neirong + "" + new DecimalFormat("").format(danyaonum) + "%danyaobaoxiang/logic/真";
        return neirong;

    }

    /** 来源 B:BaoxiangZuowuProcedure，保留分支及随机边界。 */
    private static String tableZuowu(double level, RandomSource random) {
        double danyaonum = 0;
        double suijizhi = 0;
        String neirong = "";
        suijizhi = Mth.nextInt(random, 1, 64);
        if (suijizhi == 1) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_001";
        } else if (suijizhi == 2) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_002";
        } else if (suijizhi == 3) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_003";
        } else if (suijizhi == 4) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_004";
        } else if (suijizhi == 5) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_005";
        } else if (suijizhi == 6) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_006";
        } else if (suijizhi == 7) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_007";
        } else if (suijizhi == 8) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_008";
        } else if (suijizhi == 9) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_009";
        } else if (suijizhi == 10) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_00901";
        }
        if (suijizhi == 11) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_010";
        } else if (suijizhi == 12) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_011";
        } else if (suijizhi == 13) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_012";
        } else if (suijizhi == 14) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_013";
        } else if (suijizhi == 15) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_014";
        } else if (suijizhi == 16) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_015";
        } else if (suijizhi == 17) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_01501";
        } else if (suijizhi == 18) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_01502";
        } else if (suijizhi == 19) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_016";
        } else if (suijizhi == 20) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_01602";
        }
        if (suijizhi == 21) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_017";
        } else if (suijizhi == 22) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_018";
        } else if (suijizhi == 23) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_019";
        } else if (suijizhi == 24) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_020";
        } else if (suijizhi == 25) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_021";
        } else if (suijizhi == 26) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_022";
        } else if (suijizhi == 27) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_023";
        } else if (suijizhi == 28) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_024";
        } else if (suijizhi == 29) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_025";
        } else if (suijizhi == 30) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_026";
        }
        if (suijizhi == 31) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_027";
        } else if (suijizhi == 32) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_028";
        } else if (suijizhi == 33) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_029";
        } else if (suijizhi == 34) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_030";
        } else if (suijizhi == 35) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_031";
        } else if (suijizhi == 36) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_032";
        } else if (suijizhi == 37) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_033";
        } else if (suijizhi == 38) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_034";
        } else if (suijizhi == 39) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_035";
        } else if (suijizhi == 40) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_036";
        }
        if (suijizhi == 41) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_037";
        } else if (suijizhi == 42) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_038";
        } else if (suijizhi == 43) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_039";
        } else if (suijizhi == 44) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_040";
        } else if (suijizhi == 45) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_041";
        } else if (suijizhi == 46) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_042";
        } else if (suijizhi == 47) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_043";
        } else if (suijizhi == 48) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_044";
        } else if (suijizhi == 49) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_045";
        } else if (suijizhi == 50) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_046";
        }
        if (suijizhi == 51) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_04601";
        } else if (suijizhi == 52) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_047";
        } else if (suijizhi == 53) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_04701";
        } else if (suijizhi == 54) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_048";
        } else if (suijizhi == 55) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_04801";
        } else if (suijizhi == 56) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_049";
        } else if (suijizhi == 57) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_04901";
        } else if (suijizhi == 58) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_050";
        } else if (suijizhi == 59) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_051";
        } else if (suijizhi == 60) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_052";
        }
        if (suijizhi == 61) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_053";
        } else if (suijizhi == 62) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_054";
        } else if (suijizhi == 63) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_055";
        } else if (suijizhi == 64) {
            neirong = "cailiaoid/char/yvanchuxiuzhen:shicai_056";
        }
        return neirong;

    }

    /** 来源 B:SetcailiaopinjieProcedure，保留分支及随机边界。 */
    private static double materialQuality(double level, RandomSource random) {
        double jilv = 0;
        double cailiaopinjie = 0;
        jilv = Mth.nextInt(random, 1, 100);
        if (jilv < 5) {
            cailiaopinjie = 4;
        } else if (jilv >= 5 && jilv < 20) {
            cailiaopinjie = 3;
        } else if (jilv >= 20 && jilv < 60) {
            cailiaopinjie = 2;
        } else if (jilv >= 60 && jilv < 90) {
            cailiaopinjie = 1;
        } else {
            cailiaopinjie = 0;
        }
        return cailiaopinjie;

    }
}
