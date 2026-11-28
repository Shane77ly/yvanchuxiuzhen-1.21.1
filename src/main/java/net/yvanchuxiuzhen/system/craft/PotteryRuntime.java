package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelMenu;
import net.yvanchuxiuzhen.system.craft.data.CraftData;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.PotteryRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftTableRecipes;

/** 陶轮制作、陶坯点火和计划刻烧制；按 JSON 陶轮配方驱动并只按注册物品标识生成。 */
public final class PotteryRuntime {
    private static final TagKey<Item> IGNITERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dahuozhuangzhi"));

    private PotteryRuntime() {
    }

    public static void useWheel(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer opener && level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity wheel) {
            opener.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new PotteryWheelMenu(id, inventory, wheel),
                    Component.translatable("block.yvanchuxiuzhen.taolun")), pos);
            refresh(wheel);
        }
    }

    /** 每页固定八项；开窗和翻页均清显示文本并保留结果槽。 */
    public static void refresh(PotteryWheelBlockEntity wheel) {
        List<PotteryRecipe> recipes = CraftTableRecipes.pottery();
        for (int line = 1; line <= 3; line++) wheel.setText(line, "");
        int page = Math.max(1, wheel.page());
        wheel.setMaxPage(Math.max(1, (recipes.size() + 7) / 8));
        for (int index = 0; index < 8; index++) {
            wheel.setItem(index + 1, ItemStack.EMPTY);
            wheel.setNeedItem(index, 0);
            wheel.setItemName(index, "");
            wheel.setOutputId(index, "");
        }
        int start = (page - 1) * 8;
        for (int index = 0; index < 8; index++) {
            int recipeIndex = start + index;
            if (recipeIndex >= recipes.size()) break;
            PotteryRecipe recipe = recipes.get(recipeIndex);
            ItemStack output = new ItemStack(byId(recipe.outputId()));
            wheel.setNeedItem(index, recipe.cost());
            wheel.setItemName(index, output.getDisplayName().getString());
            wheel.setOutputId(index, recipe.outputId());
            wheel.setItem(index + 1, output);
        }
        wheel.changed();
    }

    public static void changePage(PotteryWheelBlockEntity wheel, boolean forward) {
        int page = wheel.page() + (forward ? 1 : -1);
        int max = wheel.maxPage();
        wheel.setPage(forward && page > max ? 1 : !forward && page < 1 ? max : page);
        refresh(wheel);
    }

    public static void select(PotteryWheelBlockEntity wheel, int index) {
        wheel.setSelected(index);
        wheel.setText(1, wheel.itemName(index).replace("[", "").replace("]", ""));
        wheel.setText(2, "黏土球：");
        wheel.setText(3, wheel.needItem(index) + "个");
        wheel.changed();
    }

    /** 能力库存用于数量预检，原版背包及合成格用于扣除；已有成品直接覆盖。 */
    public static void craft(PotteryWheelBlockEntity wheel, Player player) {
        if (wheel.text(1).isEmpty()) return;
        int selected = wheel.selected();
        String outputId = wheel.outputId(selected);
        int cost = wheel.needItem(selected);
        Item materialItem = materialFor(outputId);
        int count = 0;
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack material = inventory.getStackInSlot(slot);
                if (material.is(materialItem)) count += material.getCount();
            }
        }
        if (count < cost) return;
        ItemStack blank = stack("taopiitem");
        blank.set(CraftData.POTTERY_PRODUCT.get(), outputId);
        wheel.setItem(0, blank);
        player.getInventory().clearOrCountMatchingItems(material -> material.is(materialItem), cost,
                player.inventoryMenu.getCraftSlots());
        wheel.changed();
    }

    private static Item materialFor(String outputId) {
        for (PotteryRecipe recipe : CraftTableRecipes.pottery()) {
            if (recipe.outputId().equals(outputId)) return byId(recipe.materialId());
        }
        return Items.CLAY_BALL;
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
                blank.setBurning(true);
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
        int progress = blank.fireTick() + 1;
        blank.setFireTick(progress);
        int required = blank.getLevel().canSeeSkyFromBelowWater(blank.getBlockPos().above()) ? 48 : 12;
        if (progress >= required) {
            String output = blank.getItem(0).getOrDefault(CraftData.POTTERY_PRODUCT.get(), "").toLowerCase(Locale.ENGLISH);
            blank.setItem(0, new ItemStack(byId(output)));
            blank.setFireTick(0);
            blank.setBurning(true);
        }
        blank.changed();
    }

    private static ItemStack stack(String path) {
        return new ItemStack(item(path));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }

    private static Item byId(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }
}
