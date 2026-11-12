package net.yvanchuxiuzhen.system.lianqi;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 炼器制作和升级按材料原始条目累计数值，成品属性及实例由装备系统构造。 */
final class TripodMaterials {
    private TripodMaterials() {
    }

    static void calculate(TripodBlockEntity furnace, boolean upgrade) {
        ItemStack previous = upgrade ? furnace.getItem(0).copy() : ItemStack.EMPTY;
        CompoundTag recipe = upgrade ? Equipment.refiningMaterials(previous) : data(furnace.getItem(10));
        CompoundTag raw = upgrade ? recipe.copy() : new CompoundTag();
        if (upgrade) furnace.setItem(0, ItemStack.EMPTY);
        else for (String key : new String[] {"tuzhiname", "fabaoid", "fabaoshuoming", "fabaotype"}) {
            raw.putString(key, recipe.getString(key));
        }
        CompoundTag state = furnace.getPersistentData();
        Accumulator accumulator = new Accumulator(raw);
        double weight = 0;
        for (int slot = upgrade ? 1 : 0; slot <= 8; slot++) {
            String requirement = recipe.getString("cailiao0" + slot);
            if (!upgrade && (requirement.isEmpty() || requirement.equals("空"))) continue;
            ItemStack input = furnace.getItem(slot);
            MaterialDefinition definition = Materials.definition(input);
            if (definition == null) continue;
            int count = upgrade ? input.getCount() : recipe.getInt("cailiao0" + slot + "num");
            int unitWeight = definition.fixedWeight().orElseThrow();
            weight += unitWeight * count;
            state.putInt("lianqiTickMax", state.getInt("lianqiTickMax") + count * 5);
            accumulator.add(definition, Materials.instance(input), count);
            if (upgrade) furnace.setItem(slot, ItemStack.EMPTY);
            else input.shrink(count);
        }
        raw.putInt("fumozongshu", accumulator.enchantments);
        state.putInt("lianqiTickMax", (int) (state.getInt("lianqiTickMax") * (1 - state.getInt("TripodFire") * .05)));
        weight *= 1 - state.getInt("TripodPurification") * .01;
        if (upgrade) weight = Math.max(raw.getInt("weightnum"), raw.getInt("weightnum") + Math.max(1, weight));
        raw.putInt("weightnum", (int) weight);
        if (raw.getInt("weightnum") >= 160) {
            ItemStack waste = new ItemStack(Items.IRON_BLOCK);
            waste.set(DataComponents.CUSTOM_NAME, Component.literal("废弃金属"));
            state.put("item", waste.save(furnace.getLevel().registryAccess()));
            if (raw.getInt("weightnum") >= 250) {
                state.putInt("zhadingtick", 2);
                state.putInt("zhadinglevel", (int) Math.round(raw.getDouble("weightnum") / 20));
            }
        } else {
            ItemStack result = Equipment.createRefinedProduct(furnace.getLevel().registryAccess(), recipe, raw, previous);
            furnace.setItem(11, result);
            state.put("item", result.save(furnace.getLevel().registryAccess()));
            int level = Equipment.product(result).orElseThrow().level();
            state.putInt("jingyanhuoqv", level);
            furnace.setItem(11, ItemStack.EMPTY);
            int difference = level - (state.getInt("TripodLevel") + state.getInt("RefinerLEVEL")) * 5;
            state.putBoolean("lianqishibai", difference > 0 && Math.random() < difference * .1);
        }
        furnace.changed();
    }

    static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** 来源工作变量在未知属性间保留，材料顺序和品阶分支均直接影响累计结果。 */
    private static final class Accumulator {
        private final CompoundTag raw;
        private int enchantments;
        private double attribute;

        private Accumulator(CompoundTag raw) {
            this.raw = raw;
            enchantments = raw.getInt("fumozongshu");
        }

        private void add(MaterialDefinition definition, MaterialInstance instance, int count) {
            int level = Math.max(1, instance.level());
            int quality = instance.quality();
            double multiplier = quality == 4 ? 1.15 : quality == 3 ? 1.05 : quality == 1 ? .95 : .85;
            int length = Math.min(definition.entries().size(), quality + 3);
            for (int index = 0; index < length; index++) {
                MaterialDefinition.Entry entry = definition.entries().get(index);
                String name = entry.name();
                if (entry.namespace().equals("attribute")) {
                    attribute = switch (name) {
                        case "heanum" -> 3 * level + 5 * Math.floor(level / 10.0);
                        case "powernum", "jingnum" -> (10 * level + 10 * Math.floor(level / 10.0) + 8) / 4;
                        case "attnum", "mananum", "soulnum" -> 6 * level + 4 * Math.floor(level / 10.0) + 22;
                        case "attdefnum", "manadefnum", "souldefnum" -> 1.5 * level + Math.floor(level / 10.0) + 6;
                        case "spenum", "expnum" -> 5 * level;
                        case "attspeednum", "knockbacknum" -> 2 * level + 8;
                        case "armortoughness", "armnum" -> level;
                        default -> attribute;
                    };
                    attribute *= multiplier;
                    attribute *= definition.fixedWeight().orElseThrow() * .2;
                    raw.putDouble(name, raw.getDouble(name) + attribute * count);
                } else if (entry.namespace().equals("teshu")) {
                    raw.putBoolean(name, true);
                } else if (!entry.namespace().isEmpty()) {
                    double amount = (level + 5) * multiplier;
                    amount *= definition.fixedWeight().orElseThrow() * .2;
                    for (int group = 0; group <= enchantments; group++) {
                        String key = "fumo" + group;
                        if (raw.getString(key).isEmpty()) {
                            raw.putString(key, name);
                            raw.putString(key + "kongjian", entry.namespace());
                            raw.putDouble(key + "num", amount * count * (name.equals("qiling") ? 2 : 1));
                            enchantments++;
                            break;
                        } else if (raw.getString(key).equals(name)) {
                            raw.putDouble(key + "num", raw.getDouble(key + "num") + amount * count * (name.equals("qiling") ? 2 : 1));
                            break;
                        }
                    }
                } else break;
            }
        }
    }
}
