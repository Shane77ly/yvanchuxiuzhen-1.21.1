package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** 四个持久饰品槽：头饰、项坠、腰佩、手饰。对外返回物品副本，菜单关闭时由装备系统整体提交。 */
public record AccessoryInventory(ItemStack head, ItemStack necklace, ItemStack belt, ItemStack hand) {
    public static final AccessoryInventory EMPTY = new AccessoryInventory(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    public static final Codec<AccessoryInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("head").forGetter(AccessoryInventory::head),
            ItemStack.OPTIONAL_CODEC.fieldOf("necklace").forGetter(AccessoryInventory::necklace),
            ItemStack.OPTIONAL_CODEC.fieldOf("belt").forGetter(AccessoryInventory::belt),
            ItemStack.OPTIONAL_CODEC.fieldOf("hand").forGetter(AccessoryInventory::hand)
    ).apply(instance, AccessoryInventory::new));

    public AccessoryInventory {
        head = head.copy();
        necklace = necklace.copy();
        belt = belt.copy();
        hand = hand.copy();
    }

    @Override
    public ItemStack head() {
        return head.copy();
    }

    @Override
    public ItemStack necklace() {
        return necklace.copy();
    }

    @Override
    public ItemStack belt() {
        return belt.copy();
    }

    @Override
    public ItemStack hand() {
        return hand.copy();
    }

    public List<ItemStack> items() {
        return List.of(head(), necklace(), belt(), hand());
    }
}
