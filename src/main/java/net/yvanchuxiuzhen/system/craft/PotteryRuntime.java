package net.yvanchuxiuzhen.system.craft;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelMenu;

/** 陶轮制作、陶坯点火和计划刻烧制；蛊罐只按注册物品标识生成。 */
public final class PotteryRuntime {
    private static final String[] OUTPUTS = {"taoguan_01item", "taoguan_02item", "taoguan_03item"};
    private static final int[] CLAY_COSTS = {4, 16, 36};
    private static final TagKey<Item> IGNITERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dahuozhuangzhi"));

    private PotteryRuntime() {
    }

    public static void useWheel(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer opener && level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity wheel) {
            opener.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new PotteryWheelMenu(id, inventory, wheel),
                    Component.translatable("block.yvanchuxiuzhen.taolun")), pos);
            refresh(wheel);
        }
    }

    /** 每页固定八项，现有三项只占第一页；开窗和翻页均清显示文本并保留结果槽。 */
    public static void refresh(PotteryWheelBlockEntity wheel) {
        CompoundTag state = wheel.getPersistentData();
        for (int line = 1; line <= 3; line++) state.putString("wenben" + line, "");
        int page = Math.max(1, state.getInt("yeshu"));
        state.putInt("yeshumax", 1);
        for (int index = 0; index < 8; index++) {
            wheel.setItem(index + 1, ItemStack.EMPTY);
            state.putInt("needitem" + index, 0);
            state.putString("itemname" + index, "");
            state.putString("chengpinid" + index, "");
        }
        int start = (page - 1) * 8;
        for (int index = 0; index < 8; index++) {
            int tripleIndex = start + index * 3;
            if (tripleIndex + 2 > OUTPUTS.length * 3) break;
            int recipe = tripleIndex / 3;
            ItemStack output = stack(OUTPUTS[recipe]);
            state.putInt("needitem" + index, CLAY_COSTS[recipe]);
            state.putString("itemname" + index, output.getDisplayName().getString());
            state.putString("chengpinid" + index, ModId.VALUE + ":" + OUTPUTS[recipe]);
            wheel.setItem(index + 1, output);
        }
        wheel.changed();
    }

    public static void changePage(PotteryWheelBlockEntity wheel, boolean forward) {
        CompoundTag state = wheel.getPersistentData();
        int page = state.getInt("yeshu") + (forward ? 1 : -1);
        int max = state.getInt("yeshumax");
        state.putInt("yeshu", forward && page > max ? 1 : !forward && page < 1 ? max : page);
        refresh(wheel);
    }

    public static void select(PotteryWheelBlockEntity wheel, int index) {
        CompoundTag state = wheel.getPersistentData();
        state.putInt("xvanzeweizhi", index);
        state.putString("wenben1", state.getString("itemname" + index).replace("[", "").replace("]", ""));
        state.putString("wenben2", "黏土球：");
        state.putString("wenben3", state.getInt("needitem" + index) + "个");
        wheel.changed();
    }

    /** 能力库存用于数量预检，原版背包及合成格用于扣除；已有成品直接覆盖。 */
    public static void craft(PotteryWheelBlockEntity wheel, Player player) {
        CompoundTag state = wheel.getPersistentData();
        if (state.getString("wenben1").isEmpty()) return;
        int selected = state.getInt("xvanzeweizhi");
        int cost = state.getInt("needitem" + selected);
        int count = 0;
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack material = inventory.getStackInSlot(slot);
                if (material.is(Items.CLAY_BALL)) count += material.getCount();
            }
        }
        if (count < cost) return;
        ItemStack blank = stack("taopiitem");
        CustomData.update(DataComponents.CUSTOM_DATA, blank,
                data -> data.putString("chengpinid", state.getString("chengpinid" + selected)));
        wheel.setItem(0, blank);
        player.getInventory().clearOrCountMatchingItems(material -> material.is(Items.CLAY_BALL), cost,
                player.inventoryMenu.getCraftSlots());
        wheel.changed();
    }

    public static void useBlank(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof PotteryBlankBlockEntity blank)) return;
        if (player.isShiftKeyDown()) {
            ItemHandlerHelper.giveItemToPlayer(player, blank.getItem(0).copyWithCount(1));
            blank.setItem(0, ItemStack.EMPTY);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else if (blank.getItem(0).is(item("taopiitem"))) {
            ItemStack held = player.getMainHandItem();
            if (held.is(Items.FLINT) || held.is(IGNITERS)) {
                blank.getPersistentData().putBoolean("starfire", true);
                blank.changed();
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.pottery_started"), false);
            } else if (blank.burning()) {
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.pottery_burning"), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.pottery_finished"), false);
        }
    }

    /** 方块每一百刻调用；烧成后仍保持点火，下一周期继续读取当前物品的成品标识。 */
    public static void fireStep(PotteryBlankBlockEntity blank) {
        if (!blank.burning()) return;
        CompoundTag state = blank.getPersistentData();
        int progress = state.getInt("firetick") + 1;
        state.putInt("firetick", progress);
        int required = blank.getLevel().canSeeSkyFromBelowWater(blank.getBlockPos().above()) ? 48 : 12;
        if (progress >= required) {
            String output = blank.getItem(0).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                    .getString("chengpinid").toLowerCase(Locale.ENGLISH);
            blank.setItem(0, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(output))));
            state.putInt("firetick", 0);
            state.putBoolean("starfire", true);
        }
        blank.changed();
    }

    private static ItemStack stack(String path) {
        return new ItemStack(item(path));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
