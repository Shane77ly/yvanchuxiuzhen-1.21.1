package net.yvanchuxiuzhen.system.lianqi;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;

/** 图纸保存、按原顺序装料与按钮领取；玩家仓及器物实例通过所属系统公开入口写入。 */
final class TripodRecipes {
    private static final String[] TYPES = {"武器", "法宝", "头盔", "胸甲", "裤子", "战靴"};

    private TripodRecipes() {
    }

    static void save(TripodBlockEntity furnace, ServerPlayer player) {
        if (furnace.getPersistentData().getBoolean("lianqiBegin")) return;
        ItemStack equipment = furnace.getItem(0);
        int type = TripodRuntime.equipmentType(equipment);
        if (type < 0 || !TripodRuntime.hasMaterial(furnace)) return;
        ItemStack carrier = furnace.getItem(10).copy();
        CompoundTag recipe = TripodMaterials.data(carrier);
        if (!recipe.getString("fabaotype").isEmpty()) {
            TripodRuntime.message(player, "§4" + new ItemStack(carrier.getItem()).getDisplayName().getString() + "内已有一份图纸，不可覆盖。");
            return;
        }
        recipe.putString("tuzhiname", equipment.getDisplayName().getString());
        recipe.putString("fabaoid", TripodRuntime.itemId(equipment));
        recipe.putString("fabaotype", TYPES[type]);
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack material = furnace.getItem(slot);
            String key = "cailiao0" + slot;
            recipe.putString(key, TripodRuntime.itemId(material));
            recipe.putInt(key + "num", material.getCount());
            recipe.putString(key + "name", material.getDisplayName().getString());
        }
        carrier.set(DataComponents.CUSTOM_DATA, CustomData.of(recipe));
        carrier.setCount(1);
        furnace.setItem(10, carrier);
        furnace.changed();
        TripodRuntime.message(player, "图纸内容已保存");
    }

    static void load(TripodBlockEntity furnace, ServerPlayer player) {
        CompoundTag recipe = TripodMaterials.data(furnace.getItem(10));
        if (recipe.getString("fabaotype").isEmpty()) {
            TripodRuntime.message(player, "§4请在左边的物品栏放入图纸。");
            return;
        }
        furnace.getPersistentData().putString("lianqiwenzi", recipe.getString("fabaotype"));
        for (int slot = 0; slot <= 8; slot++) {
            ItemHandlerHelper.giveItemToPlayer(player, furnace.getItem(slot).copy());
            furnace.setItem(slot, ItemStack.EMPTY);
        }
        boolean stopped = false;
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        for (int slot = 0; slot <= 8; slot++) {
            String id = recipe.getString("cailiao0" + slot);
            if (id.isEmpty()) continue;
            int required = recipe.getInt("cailiao0" + slot + "num");
            if (MaterialStorage.snapshot(player).preferences().spending()) {
                MaterialStorage.RefiningWithdrawal withdrawal = MaterialStorage.consumeForRefining(player, id, required, stopped);
                for (ItemStack replacement : withdrawal.replacements()) furnace.setItem(slot, replacement);
                stopped = withdrawal.stopped();
            } else if (inventory != null) {
                for (int index = 0; index < inventory.getSlots(); index++) {
                    ItemStack stack = inventory.getStackInSlot(index);
                    if (TripodRuntime.itemId(stack).equals(id) && stack.getCount() >= required) {
                        furnace.setItem(slot, stack.copyWithCount(required));
                        stack.shrink(required);
                        break;
                    }
                }
            }
        }
        furnace.changed();
    }

    static void claim(TripodBlockEntity furnace, ServerPlayer player) {
        CompoundTag state = furnace.getPersistentData();
        int experience = state.getInt("jingyanhuoqv");
        if (experience >= 1) {
            Professions.addExperience(player, Profession.REFINING, experience);
            TripodRuntime.message(player, "§6炼器经验增减量为" + experience);
            state.putInt("jingyanhuoqv", 0);
        }
        ItemStack result = furnace.getItem(9);
        if (TripodRuntime.tagged(result, "cailiao")) {
            int level = materialLevel(TripodRuntime.itemId(result));
            int count = result.getCount();
            for (int index = 0; index < count; index++) {
                ItemStack material = result.copyWithCount(1);
                int roll = player.getRandom().nextInt(100) + 1;
                int quality = roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
                Materials.setInstance(material, level, quality);
                ItemHandlerHelper.giveItemToPlayer(player, material);
            }
        } else ItemHandlerHelper.giveItemToPlayer(player, result.copy());
        furnace.setItem(9, ItemStack.EMPTY);
        furnace.changed();
        player.containerMenu.broadcastChanges();
    }

    private static int materialLevel(String id) {
        int[] levels = {3, 8, 13, 18, 23, 28, 33, 38, 40};
        for (int index = 0; index < levels.length; index++) {
            if (id.equals("yvanchuxiuzhen:cailiao_" + (201 + index)) || id.equals("yvanchuxiuzhen:cailiao_" + (211 + index))) return levels[index];
        }
        return 0;
    }
}
