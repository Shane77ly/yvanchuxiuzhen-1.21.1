package net.yvanchuxiuzhen.system.liandan;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;

/** 药瓶实例的数量合并与服用；药效经原版命令交由已注册效果的所有者执行。 */
public final class LiandanMedicine {
    private LiandanMedicine() {
    }

    public static void use(Level level, Player player, ItemStack stack, Vec3 position) {
        if (!(level instanceof ServerLevel serverLevel) || player.isShiftKeyDown()) {
            return;
        }
        MedicineInstance data = Liandan.medicine(stack);
        if (data.doses() < 1) {
            return;
        }
        if (stack.getCount() >= 2) {
            combine(player, stack, data);
            return;
        }

        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, position, Vec2.ZERO, serverLevel, 4,
                "", Component.empty(), serverLevel.getServer(), null).withSuppressedOutput();
        List<MedicineInstance.Effect> clamped = applyEffects(data, source, player.getStringUUID(), true);
        int remaining = data.doses() - 1;
        Liandan.setMedicine(stack, data.withDosesAndEffects(remaining, clamped));
        if (remaining <= 0) {
            replaceHeld(player, stack.getItem(), emptyContainer(stack.getItem()));
        }
    }

    /** 自服与喂药共用实例换算和命令边界；只有自服限制抗性提升的等级。 */
    static List<MedicineInstance.Effect> applyEffects(MedicineInstance data, CommandSourceStack source,
                                                      String selector, boolean capResistance) {
        boolean treasure = data.treasure();
        List<MedicineInstance.Effect> updated = new ArrayList<>(data.effects());
        boolean stopped = false;
        for (int index = 0; index < updated.size(); index++) {
            MedicineInstance.Effect entry = updated.get(index);
            if (!stopped && entry.level() != 0) {
                int effectLevel = treasure ? entry.level() : entry.level() / 100;
                int seconds = treasure ? entry.duration() : entry.duration() / 20;
                String effect = entry.id();
                if (capResistance && (effect.equals("resistance") || effect.equals("minecraft:resistance")) && effectLevel > 3) {
                    effectLevel = 3;
                }
                // 来源先计算本次秒数，再修正实例字段；本次零秒仍由 effect 命令判为无效。
                if (entry.duration() < 1) {
                    updated.set(index, new MedicineInstance.Effect(effect, entry.level(), 1));
                }
                if (effectLevel >= 1) {
                    source.getServer().getCommands().performPrefixedCommand(source,
                            "effect give " + selector + " " + effect + " " + seconds + " " + (effectLevel - 1));
                }
            } else {
                stopped = true;
            }
        }
        return updated;
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
    private static void combine(Player player, ItemStack stack, MedicineInstance data) {
        InteractionHand hand = replacementHand(player, stack.getItem());
        ItemStack combined = stack.copyWithCount(1);
        int amount = data.doses() * stack.getCount();
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(stack.getItem(), stack.getCount() - 1));
        Liandan.setMedicine(combined, data.withDoses(amount));
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
