package net.yvanchuxiuzhen.system.npc;

import java.util.ArrayList;
import java.util.List;

/** 管理工具在手持物品数据中的可编辑配置字段。 */
public final class NpcManagementData {
    public static final List<String> KEYS = keys();

    private NpcManagementData() { }

    private static List<String> keys() {
        List<String> keys = new ArrayList<>(List.of("npcname", "npclevel"));
        for (int index = 1; index <= 10; index++) keys.add("renwulist" + index);
        keys.addAll(List.of("productlist", "shopinformation", "healthmaxtxt", "healthrectxt", "powermaxtxt", "powerrectxt",
                "jinglimaxtxt", "jinglirectxt", "attacktxt", "defensetxt", "manaatttxt", "manadeftxt", "soulatttxt",
                "souldeftxt", "speednumtxt"));
        return List.copyOf(keys);
    }
}
