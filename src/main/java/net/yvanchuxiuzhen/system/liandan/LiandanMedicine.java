package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;

/** 药瓶实例的数量合并与服用；药效经原版命令交由已注册效果的所有者执行。 */
public final class LiandanMedicine {
    private LiandanMedicine() {
    }

    public static void use(Level level, Player player, ItemStack stack, Vec3 position) {
        if (!(level instanceof ServerLevel serverLevel) || player.isShiftKeyDown()) {
            return;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.getInt("danyaonum") < 1) {
            return;
        }
        if (stack.getCount() >= 2) {
            combine(player, stack, data);
            return;
        }

        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, position, Vec2.ZERO, serverLevel, 4,
                "", Component.empty(), serverLevel.getServer(), null).withSuppressedOutput();
        applyEffects(data, source, player.getStringUUID(), true);
        data.putInt("danyaonum", data.getInt("danyaonum") - 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (data.getInt("danyaonum") <= 0) {
            replaceHeld(player, stack.getItem(), emptyContainer(stack.getItem()));
        }
    }

    /** 自服与喂药共用实例换算和命令边界；只有自服限制抗性提升的等级。 */
    static void applyEffects(CompoundTag data, CommandSourceStack source, String selector, boolean capResistance) {
        boolean treasure = data.getBoolean("danyaobaoxiang");
        for (int index = 0; data.getInt("danyaolevel" + index) != 0; index++) {
            int effectLevel = treasure ? data.getInt("danyaolevel" + index) : data.getInt("danyaolevel" + index) / 100;
            int seconds = treasure ? data.getInt("danyaotick" + index) : data.getInt("danyaotick" + index) / 20;
            String effect = data.getString("danyaoxiaoguo" + index);
            if (capResistance && (effect.equals("resistance") || effect.equals("minecraft:resistance")) && effectLevel > 3) {
                effectLevel = 3;
            }
            // 来源先计算本次秒数，再修正实例字段；本次零秒仍由 effect 命令判为无效。
            if (data.getInt("danyaotick" + index) < 1) {
                data.putInt("danyaotick" + index, 1);
            }
            if (effectLevel >= 1) {
                source.getServer().getCommands().performPrefixedCommand(source,
                        "effect give " + selector + " " + effect + " " + seconds + " " + (effectLevel - 1));
            }
        }
    }

    /** 耗尽后生成不带原实例组件的空容器，两个满药盒对应各自的空药盒。 */
    static ItemStack emptyContainer(Item original) {
        if (original == item("itemdanyaohulu_0401")) {
            return new ItemStack(item("itemdanyaohulu_04"));
        }
        if (original == item("itemdanyaohulu_0501")) {
            return new ItemStack(item("itemdanyaohulu_05"));
        }
        return new ItemStack(original);
    }

    /** 多瓶先合为一瓶，原实例的全部组件保留，空出的同类容器没有原实例数据。 */
    private static void combine(Player player, ItemStack stack, CompoundTag data) {
        InteractionHand hand = replacementHand(player, stack.getItem());
        ItemStack combined = stack.copyWithCount(1);
        int amount = data.getInt("danyaonum") * stack.getCount();
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(stack.getItem(), stack.getCount() - 1));
        data.putInt("danyaonum", amount);
        combined.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        player.setItemInHand(hand, combined);
        player.getInventory().setChanged();
    }

    private static void replaceHeld(Player player, Item original, ItemStack replacement) {
        player.setItemInHand(replacementHand(player, original), replacement);
        player.getInventory().setChanged();
    }

    /** 来源按副手物品类型决定替换位置，两手同类时也优先副手。 */
    private static InteractionHand replacementHand(Player player, Item item) {
        return player.getOffhandItem().getItem() == item ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
