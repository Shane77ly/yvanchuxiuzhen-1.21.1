package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;

/** 尸体收纳符的库存条件与载体写入；尸体字段由生物系统按来源选择。 */
public final class CorpseTalismans {
    private CorpseTalismans() { }

    public static boolean hasEmptySeal(Player player) {
        Item empty = item("itemfulu_50");
        return player.getInventory().contains(stack -> stack.is(empty));
    }

    /** 扣除一张空符并给予一张封存符，背包余量交原生给予流程处理。 */
    public static void seal(Player player, CompoundTag corpse) {
        Item empty = item("itemfulu_50");
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(empty), 1, player.inventoryMenu.getCraftSlots());
        ItemStack sealed = new ItemStack(item("itemfulu_5001"));
        sealed.set(FuluData.SEALED_CORPSE.get(), new SealedCorpse(corpse));
        ItemHandlerHelper.giveItemToPlayer(player, sealed);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
