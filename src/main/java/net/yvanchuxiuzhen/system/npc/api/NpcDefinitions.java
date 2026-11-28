package net.yvanchuxiuzhen.system.npc.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.npc.NpcData;

/** 来源语言表中的拍卖修士目录；返回副本，目录字段由修士系统维护。 */
public final class NpcDefinitions {
    private static final CompoundTag DEFINITIONS = load();

    private NpcDefinitions() {
    }

    private static CompoundTag load() {
        CompoundTag definitions = new CompoundTag();
        int index = 0;
        for (CompoundTag row : ContentFiles.read("content/npc/definitions.json", CompoundTag.CODEC)) {
            definitions.put("noplayercharacter" + ++index, row);
        }
        definitions.putInt("npcmaxnumber", index);
        return definitions;
    }

    public static CompoundTag snapshot() {
        return DEFINITIONS.copy();
    }

    public static boolean isInteractiveNpc(Entity entity) {
        return entity.getData(NpcData.INTERACTIVE_NPC);
    }

    public static void setInteractiveNpc(Entity entity, boolean active) {
        entity.setData(NpcData.INTERACTIVE_NPC, active);
    }
}
