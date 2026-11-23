package net.yvanchuxiuzhen.system.npc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.npc.api.NpcDialogueRequestEvent;
import net.yvanchuxiuzhen.system.npc.entity.NpcProfiles;

/** 来源全局修士右键入口，实例脚本由修士持有，玩家会话交任务与经济系统维护。 */
public final class NpcInteractions {
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "npcentity"));
    private static final String SHOP_SCRIPT = "商店任务/hvsdgfsdhfs/一个通用的商店任务。/0/未完成/无/无%打开界面/商店%结束";
    private static final String ORES = "yvanchuxiuzhen:cailiao_207raw%无%灵石%135150%无%yvanchuxiuzhen:cailiao_208raw%无%灵石%540660%无%yvanchuxiuzhen:cailiao_209raw%无%灵石%835560%无%yvanchuxiuzhen:cailiao_216raw%无%灵石%33780%无%yvanchuxiuzhen:cailiao_217raw%无%灵石%135150%无%yvanchuxiuzhen:cailiao_219raw%无%灵石%835560%无%yvanchuxiuzhen:cailiao_16402%无%灵石%1530%无%yvanchuxiuzhen:cailiao_16502%无%灵石%6135%无%yvanchuxiuzhen:cailiao_16602%无%灵石%24570%无%yvanchuxiuzhen:cailiao_16702%无%灵石%98295%无%yvanchuxiuzhen:cailiao_16802%无%灵石%393210%无%yvanchuxiuzhen:cailiao_16902%无%灵石%835560%无";

    private NpcInteractions() { }

    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        Entity npc = event.getTarget();
        if (player.getMainHandItem().getItem() instanceof NpcManagementItem) {
            if (npc instanceof Mob managed) NpcProfiles.apply(player.serverLevel(), event.getPos(), managed, player.getMainHandItem());
            return;
        }
        CompoundTag data = npc.getPersistentData();
        if (npc.getType().is(NPC)) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()).getPath();
            switch (id) {
                case "businessman_01" -> data.putString("renwulist1", "txt.renwulist.npc01");
                case "businessman_02", "businessman_05", "businessman_06", "businessman_07" -> {
                    String key = "npc" + id.substring(id.length() - 2);
                    data.putString("renwulist1", "txt.renwulist." + key);
                    data.putString("productlist", "txt.shoplist." + key);
                    data.putString("shopinformation", "txt.shopdescribe." + key);
                }
                case "businessman_03" -> {
                    refreshCatalog(npc, 1);
                    data.putString("renwulist1", "txt.renwulist.npc03");
                    data.putString("shopinformation", "txt.shopdescribe.npc03");
                }
                case "businessman_04" -> {
                    data.putString("tasklist1", "");
                    data.putString("renwulist1", "txt.renwulist.npc04");
                    data.putString("productlist", "");
                    data.putString("shopinformation", "");
                }
                case "daguanjia" -> {
                    // 来源查询NPC自身的玩家能力，非玩家没有该能力，实际进入普通对话。
                    data.putString("renwulist1", "txt.renwulist.npcdaguanjia04");
                    data.putString("renwulist2", "");
                    data.putString("renwulist3", "");
                }
                case "caishenye" -> {
                    data.putString("renwulist1", "txt.renwulist.npccaishenye");
                    data.putString("productlist", "txt.shoplist.npccaishenye");
                    data.putString("shopinformation", "txt.shopdescribe.npccaishenye");
                }
                case "businessman_09" -> {
                    data.putString("renwulist1", SHOP_SCRIPT);
                    data.putString("shopinformation", "矿石商人%矿石商人%在这里可以买到很多矿石材料。");
                    data.putString("productlist", ORES);
                }
                case "businessman_10", "businessman_11" -> {
                    boolean high = id.equals("businessman_11");
                    data.putString("renwulist1", SHOP_SCRIPT);
                    data.putString("shopinformation", high ? "高级材料商%高级材料%在此购买高级材料" : "中级材料商%中级材料%在此购买中级材料");
                    refreshCatalog(npc, high ? 3 : 2);
                }
                default -> { }
            }
            data.putBoolean("renwuzuopen", true);
        }
        if (!data.getBoolean("renwuzuopen") || BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())
                .equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandao_np_csetitem"))) return;
        List<String> scripts = new ArrayList<>();
        for (int index = 1; ; index++) {
            String dialogue = data.getString("renwulist" + index);
            String task = data.getString("tasklist" + index);
            if (dialogue.isEmpty() && task.isEmpty()) break;
            scripts.add(dialogue.isEmpty() ? task : dialogue);
        }
        NeoForge.EVENT_BUS.post(new NpcDialogueRequestEvent(player, scripts, data.getString("productlist"), data.getString("shopinformation")));
    }

    private static void refreshCatalog(Entity npc, int grade) {
        Scoreboard scoreboard = npc.level().getScoreboard();
        Objective objective = scoreboard.getObjective("keyishuaxin");
        String existing = npc.getPersistentData().getString("productlist");
        if (existing.isEmpty() || existing.startsWith("minecraft:air%")
                || (objective != null && scoreboard.getOrCreatePlayerScore(npc, objective).get() == 1)) {
            npc.getPersistentData().putString("productlist", EconomyCatalogs.materialShop(grade));
            if (objective != null) scoreboard.getOrCreatePlayerScore(npc, objective).set(0);
        }
    }
}
