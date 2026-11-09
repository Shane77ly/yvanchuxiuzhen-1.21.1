package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 药理台按八槽次序预览药效，并把名称、说明与材料清单写入丹方媒介。 */
public final class Pharmacology {
    private Pharmacology() {
    }

    /** 预览使用装置药性快照，保留材料及媒介；槽十一保存一件临时钻石。 */
    public static void refresh(BlockEntity table, Container inventory, String inputName) {
        CompoundTag medicine = new CompoundTag();
        medicine.putString("danyaoname", inputName.isEmpty() || inputName.equals("丹方名称")
                ? defaultName(inventory.getItem(1)) : inputName);
        CompoundTag herbs = table.getPersistentData().getCompound("zong");
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack ingredient = inventory.getItem(slot);
            if (ingredient.isEmpty()) continue;
            String herb = BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString();
            for (int effect = 0; effect < herbs.getInt(herb + "number"); effect++) {
                String prefix = herb + "effect" + effect;
                String name = herbs.getString(prefix + "name");
                int index = 0;
                boolean existing = false;
                for (; index < medicine.getInt("shuliang") + 1; index++) {
                    if (medicine.getString("danyaoxiaoguo" + index).equals(name)) {
                        LiandanBrewing.combine(medicine, "danyaolevel" + index, herbs.getString(prefix + "symbollevel"),
                                herbs.getInt(prefix + "symbollevelnum"), false);
                        LiandanBrewing.combine(medicine, "danyaotick" + index, herbs.getString(prefix + "symboltime"),
                                herbs.getInt(prefix + "symboltimenum"), true);
                        existing = true;
                        break;
                    }
                }
                if (!existing) {
                    index--;
                    medicine.putString("danyaoxiaoguo" + index, name);
                    LiandanBrewing.initialize(medicine, "danyaolevel" + index, herbs.getString(prefix + "symbollevel"),
                            herbs.getInt(prefix + "symbollevelnum"));
                    LiandanBrewing.initialize(medicine, "danyaotick" + index, herbs.getString(prefix + "symboltime"),
                            herbs.getInt(prefix + "symboltimenum"));
                    medicine.putInt("shuliang", medicine.getInt("shuliang") + 1);
                }
            }
        }
        ItemStack preview = new ItemStack(Items.DIAMOND);
        preview.set(DataComponents.CUSTOM_DATA, CustomData.of(medicine));
        inventory.setItem(11, preview);
        updateDisplay(table, medicine);
    }

    /** 丹方写入只在服务端修改真实菜单槽，完整替换媒介原有实例组件且不扣除材料。 */
    public static void write(Player player, AbstractContainerMenu menu, String inputName) {
        if (!(player instanceof ServerPlayer)) return;
        ItemStack medium = menu.getSlot(0).getItem();
        if (medium.isEmpty()) {
            player.displayClientMessage(Component.literal("§4请放置一个承载丹方的媒介，比如线装书。"), false);
            return;
        }
        int count = 0;
        for (int slot = 1; slot <= 8; slot++) count += menu.getSlot(slot).getItem().getCount();
        if (count < 1) {
            player.displayClientMessage(Component.literal("§4至少需要一株药草。"), false);
            return;
        }
        String name = inputName.isEmpty() ? defaultName(menu.getSlot(1).getItem()) : inputName;
        CompoundTag recipe = new CompoundTag();
        recipe.putString("danyaoname", name);
        recipe.putString("danfangshuoming", menu.getSlot(9).getItem().getDisplayName().getString());
        recipe.putString("danyaoshuoming", menu.getSlot(10).getItem().getDisplayName().getString());
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack ingredient = menu.getSlot(slot).getItem();
            recipe.putString("cailiao0" + slot + "name", ingredient.getDisplayName().getString());
            recipe.putString("cailiao0" + slot, BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString());
        }
        ItemStack result = new ItemStack(medium.getItem(), medium.getCount());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(recipe));
        result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        menu.getSlot(0).set(result);
    }

    /** 来源每次只清前十行缓存，药效行仍按原索引写入，不压缩无效等级。 */
    private static void updateDisplay(BlockEntity table, CompoundTag medicine) {
        CompoundTag state = table.getPersistentData();
        for (int index = 0; index < 10; index++) state.putString("effectxianshi" + index, "");
        for (int index = 0; !medicine.getString("danyaoxiaoguo" + index).isEmpty(); index++) {
            int level = medicine.getInt("danyaolevel" + index);
            if (level < 100) continue;
            String name = medicine.getString("danyaoxiaoguo" + index);
            String translation = name.contains(":") ? "effect." + name.replace(':', '.') : "effect.minecraft." + name;
            state.putString("effectxianshi" + index, level / 100 + "级" + Component.translatable(translation).getString()
                    + "：" + medicine.getInt("danyaotick" + index) / 20 + "秒");
        }
    }

    private static String defaultName(ItemStack ingredient) {
        return ingredient.getDisplayName().getString().substring(1, 3) + "丹";
    }
}
