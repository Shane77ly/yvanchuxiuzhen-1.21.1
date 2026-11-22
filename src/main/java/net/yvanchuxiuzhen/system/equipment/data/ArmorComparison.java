package net.yvanchuxiuzhen.system.equipment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;

/** 来源四甲比较槽：运行时保留物品引用，存档保存各槽，克隆只复制槽而保留物品引用。 */
public final class ArmorComparison {
    /** 1.18.2 允许空栈保存根标记；1.21.1 隐藏空栈组件，因此仅为空栈保留同一引用上的标记。 */
    private static final Map<ItemStack, String> EMPTY_MARKERS = new WeakHashMap<>();
    private record SavedStack(ItemStack item, String emptyMarker) {
        private static final Codec<SavedStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(SavedStack::item),
                Codec.STRING.optionalFieldOf("empty_marker", "").forGetter(SavedStack::emptyMarker)
        ).apply(instance, SavedStack::new));

        private ItemStack restore() {
            if (!item.isEmpty()) return item;
            ItemStack empty = new ItemStack(Items.AIR, 0);
            mark(empty, emptyMarker);
            return empty;
        }
    }

    private static final Codec<ItemStack> STACK_CODEC = SavedStack.CODEC.xmap(SavedStack::restore,
            stack -> new SavedStack(stack, stack.isEmpty() ? marker(stack) : ""));
    public static final Codec<ArmorComparison> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            STACK_CODEC.fieldOf("kuijia1").forGetter(value -> value.head),
            STACK_CODEC.fieldOf("kuijia2").forGetter(value -> value.chest),
            STACK_CODEC.fieldOf("kuijia3").forGetter(value -> value.legs),
            STACK_CODEC.fieldOf("kuijia4").forGetter(value -> value.feet)
    ).apply(instance, ArmorComparison::new));

    private ItemStack head;
    private ItemStack chest;
    private ItemStack legs;
    private ItemStack feet;

    public ArmorComparison() {
        this(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    private ArmorComparison(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
    }

    public ItemStack armor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> head;
            case CHEST -> chest;
            case LEGS -> legs;
            case FEET -> feet;
            default -> throw new IllegalArgumentException("护甲比较槽：" + slot);
        };
    }

    public void armor(EquipmentSlot slot, ItemStack stack) {
        switch (slot) {
            case HEAD -> head = stack;
            case CHEST -> chest = stack;
            case LEGS -> legs = stack;
            case FEET -> feet = stack;
            default -> throw new IllegalArgumentException("护甲比较槽：" + slot);
        }
    }

    public ArmorComparison copySlots() {
        return new ArmorComparison(head, chest, legs, feet);
    }

    public static String marker(ItemStack stack) {
        return stack.isEmpty() ? EMPTY_MARKERS.getOrDefault(stack, "") : Equipment.properties(stack).text("bubianzhi");
    }

    public static void mark(ItemStack stack, String marker) {
        if (stack.isEmpty()) EMPTY_MARKERS.put(stack, marker);
        else Equipment.updateRootField(stack, "bubianzhi", StringTag.valueOf(marker));
    }
}
