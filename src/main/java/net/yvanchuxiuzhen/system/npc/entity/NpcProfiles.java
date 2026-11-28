package net.yvanchuxiuzhen.system.npc.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.npc.NpcData;
import net.yvanchuxiuzhen.system.npc.NpcDialogueProfile;
import net.yvanchuxiuzhen.system.npc.NpcToolProfile;
import net.yvanchuxiuzhen.system.npc.api.NpcDefinitions;
import net.yvanchuxiuzhen.system.npc.api.NpcManagementApplyEvent;

/** 管理工具处理原生姓名、装备及粒子，持久配置交给实体所有者。 */
public final class NpcProfiles {
    private static final String[] ITEM_RATINGS = {
            "healthmaxtxt", "healthrectxt", "powermaxtxt", "powerrectxt", "jinglimaxtxt", "jinglirectxt",
            "attacktxt", "defensetxt", "manaatttxt", "manadeftxt", "soulatttxt", "souldeftxt", "speednumtxt"
    };
    private static final String[] NPC_RATINGS = {
            "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL", "soul_Max_LEVEL", "soul_Rec_LEVEL",
            "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL", "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL"
    };

    private NpcProfiles() { }

    public static void apply(ServerLevel level, BlockPos position, Mob npc, ItemStack tool) {
        NpcToolProfile profile = tool.getOrDefault(NpcData.TOOL_PROFILE.get(), NpcToolProfile.EMPTY);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.getX() + .5, position.getY() + 1,
                position.getZ() + .5, 10, 1, 2, 1, 0);
        String name = profile.name();
        if (!name.isEmpty()) {
            npc.setCustomName(Component.literal(name));
            npc.setCustomNameVisible(true);
        }
        List<String> scripts = new ArrayList<>();
        for (int index = 0; index < 10; index++) scripts.add(profile.quest(index));
        Map<String, String> ratings = new LinkedHashMap<>();
        for (int index = 0; index < ITEM_RATINGS.length; index++) {
            ratings.put(NPC_RATINGS[index], profile.ratings().getOrDefault(ITEM_RATINGS[index], ""));
        }
        NpcManagementApplyEvent event = new NpcManagementApplyEvent(npc, profile.level(), scripts,
                profile.products(), profile.shopInfo(), ratings, profile.fixed(), equipment(profile));
        NeoForge.EVENT_BUS.post(event);
        if (!event.equipmentApplied()) event.applyEquipment();
    }

    public static void applyOwnedProfile(NpcManagementApplyEvent event) {
        Mob npc = event.target();
        if (!(npc instanceof NpcMob || npc instanceof NpcResident || npc instanceof NpcFlyingShuttle
                || npc instanceof NpcUnderworldGuard || npc instanceof NpcUnderworldJudge)) {
            applyGenericMobProfile(event, npc);
            return;
        }
        writeProfileData(event, npc);
        event.applyEquipment();
        NpcStats.calculate(npc);
    }

    /**
     * 来源管理工具也接受带完整原生攻击属性的其他自定义 Mob；这些实体没有 NPC 专属所有者，
     * 由管理工具在本系统保存来源档案并复用同一套资质公式。缺少攻击属性的原版 Mob 继续
     * 保持 Q159 的来源边界，由入口只交付姓名与装备。
     */
    private static void applyGenericMobProfile(NpcManagementApplyEvent event, Mob npc) {
        if (npc.getAttribute(Attributes.ATTACK_DAMAGE) == null) return;
        writeProfileData(event, npc);
        event.applyEquipment();
        NpcStats.calculate(npc);
    }

    private static void writeProfileData(NpcManagementApplyEvent event, Mob npc) {
        NpcDialogueProfile profile = npc.getData(NpcData.PROFILE);
        for (int index = 0; index < event.scripts().size(); index++) {
            String script = event.scripts().get(index);
            if (!script.isEmpty()) {
                profile = profile.withScript(index, script);
                NpcDefinitions.setInteractiveNpc(npc, true);
            }
        }
        String products = event.products();
        if (!products.isEmpty()) {
            profile = profile.withProducts(products).withShopInfo(event.information());
        }
        if (event.fixed()) profile = profile.withFixed(true);
        npc.setData(NpcData.PROFILE, profile);

        Map<Stat, String> ratings = new EnumMap<>(Stat.class);
        for (int index = 0; index < NPC_RATINGS.length; index++) {
            String value = event.ratings().get(NPC_RATINGS[index]);
            if (value != null) ratings.put(EntityAttributes.COMBAT_STATS.get(index), value);
        }
        EntityAttributes.update(npc, stats -> stats.withLevel(event.level()).withRatings(ratings));
    }

    private static List<ItemStack> equipment(NpcToolProfile profile) {
        ItemStack[] stacks = new ItemStack[6];
        for (int index = 0; index < stacks.length; index++) {
            ItemStack stack = profile.equipment().get(index).copy();
            stacks[index] = index < 2 && !stack.isEmpty() ? stack.copyWithCount(1) : stack;
        }
        return List.of(stacks);
    }
}
