package net.yvanchuxiuzhen.system.npc.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;
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
        CompoundTag item = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.getX() + .5, position.getY() + 1,
                position.getZ() + .5, 10, 1, 2, 1, 0);
        String name = item.getString("npcname");
        if (!name.isEmpty()) {
            npc.setCustomName(Component.literal(name));
            npc.setCustomNameVisible(true);
        }
        List<String> scripts = new ArrayList<>();
        for (int index = 1; index <= 10; index++) scripts.add(item.getString("renwulist" + index));
        Map<String, String> ratings = new LinkedHashMap<>();
        for (int index = 0; index < ITEM_RATINGS.length; index++) {
            ratings.put(NPC_RATINGS[index], item.getString(ITEM_RATINGS[index]));
        }
        NpcManagementApplyEvent event = new NpcManagementApplyEvent(npc, item.getDouble("npclevel"), scripts,
                item.getString("productlist"), item.getString("shopinformation"), ratings, item.getBoolean("npcset"), equipment(npc, item));
        NeoForge.EVENT_BUS.post(event);
        if (!event.equipmentApplied()) event.applyEquipment();
    }

    public static void applyOwnedProfile(NpcManagementApplyEvent event) {
        Mob npc = event.target();
        if (!(npc instanceof NpcMob || npc instanceof NpcResident || npc instanceof NpcFlyingShuttle
                || npc instanceof NpcUnderworldGuard || npc instanceof NpcUnderworldJudge)) return;
        CompoundTag data = npc.getPersistentData();
        for (int index = 0; index < event.scripts().size(); index++) {
            String key = "renwulist" + (index + 1);
            String script = event.scripts().get(index);
            if (!script.isEmpty()) {
                data.putString(key, script);
                data.putBoolean("renwuzuopen", true);
            }
        }
        String products = event.products();
        if (!products.isEmpty()) {
            data.putString("productlist", products);
            data.putString("shopinformation", event.information());
        }
        event.applyEquipment();
        data.putDouble("LEVELnumber", event.level());
        event.ratings().forEach(data::putString);
        if (event.fixed()) data.putBoolean("npcset", true);
        NpcStats.calculate(npc);
    }

    private static List<ItemStack> equipment(Mob npc, CompoundTag item) {
        ListTag inventory = item.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
        ItemStack[] stacks = new ItemStack[6];
        Arrays.fill(stacks, ItemStack.EMPTY);
        for (int index = 0; index < inventory.size(); index++) {
            CompoundTag entry = inventory.getCompound(index);
            int slot = entry.getByte("Slot") & 255;
            if (slot < stacks.length) stacks[slot] = ItemStack.parseOptional(npc.registryAccess(), entry);
        }
        for (int index = 0; index < 2; index++) {
            if (!stacks[index].isEmpty()) stacks[index] = stacks[index].copyWithCount(1);
        }
        return List.of(stacks);
    }
}
