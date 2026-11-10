package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.craft.CarpentryRecipes.Cost;
import net.yvanchuxiuzhen.system.craft.CarpentryRecipes.Recipe;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.CarpentryMenu;

/** 木匠台候选展示、选择快照与即时制作，真值保存在同一方块实体。 */
public final class CarpentryRuntime {
    private CarpentryRuntime() {
    }

    public static void use(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer opener && level.getBlockEntity(pos) instanceof CarpentryBlockEntity table) {
            refreshCandidates(table);
            opener.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new CarpentryMenu(id, inventory, table),
                    Component.translatable("block.yvanchuxiuzhen.mugongtai")), pos);
        }
    }

    /** 打开和类别切换只刷新展示槽，不重置上次选择、预览、需求或已制作结果。 */
    public static void refreshCandidates(CarpentryBlockEntity table) {
        for (int slot = 0; slot < 40; slot++) table.setItem(slot, ItemStack.EMPTY);
        List<Recipe> recipes = CarpentryRecipes.category(table.category());
        for (int slot = 0; slot < recipes.size(); slot++) table.setItem(slot, new ItemStack(recipes.get(slot).item()));
        table.changed();
    }

    public static void cycleCategory(CarpentryBlockEntity table) {
        switch (table.category()) {
            case CarpentryRecipes.BEECH -> table.category(CarpentryRecipes.MISCELLANEOUS);
            case CarpentryRecipes.MISCELLANEOUS -> table.category(CarpentryRecipes.BEECH);
            default -> { }
        }
        refreshCandidates(table);
    }

    public static void select(CarpentryBlockEntity table, int selected) {
        table.selected(selected);
        table.setItem(40, ItemStack.EMPTY);
        table.getPersistentData().putString("jiajvname", "");
        for (int row = 0; row < 5; row++) {
            table.getPersistentData().putString("needitem" + row + "id", "");
            table.getPersistentData().putInt("needitem" + row + "num", 0);
            table.getPersistentData().putString("needitem" + row + "name", "");
        }
        List<Recipe> recipes = CarpentryRecipes.category(table.category());
        if (selected < recipes.size()) {
            Recipe recipe = recipes.get(selected);
            ItemStack preview = new ItemStack(recipe.item());
            table.setItem(40, preview);
            table.getPersistentData().putString("jiajvname", preview.getDisplayName().getString());
            for (int row = 0; row < recipe.costs().size(); row++) {
                Cost cost = recipe.costs().get(row);
                table.getPersistentData().putString("needitem" + row + "id", cost.itemId());
                table.getPersistentData().putInt("needitem" + row + "num", cost.count());
                table.getPersistentData().putString("needitem" + row + "name",
                        new ItemStack(cost.item()).getDisplayName().getString() + "：" + cost.count());
            }
        }
        table.changed();
    }

    /** 五行共享累计数量；每行仍要求对应材料存在，成品物种按预览覆盖并累加旧结果数量。 */
    public static void craft(CarpentryBlockEntity table, Player player) {
        ItemStack preview = table.getItem(40);
        if (preview.isEmpty()) return;
        Inventory inventory = player.getInventory();
        int accumulated = 0;
        int passed = 0;
        for (int row = 0; row < 5; row++) {
            String itemId = table.requirementId(row);
            if (itemId.isEmpty()) {
                passed++;
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            if (inventory.contains(stack -> !stack.isEmpty() && stack.is(item))) {
                if (player.isAlive()) {
                    for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                        ItemStack stack = inventory.getItem(slot);
                        if (stack.is(item)) accumulated += stack.getCount();
                    }
                }
                if (accumulated >= table.requirementCount(row)) passed++;
            }
        }
        if (passed < 5) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.missing_materials")
                    .withStyle(ChatFormatting.DARK_RED), false);
            return;
        }
        table.setItem(41, preview.copyWithCount(table.getItem(41).getCount() + 1));
        table.getLevel().scheduleTick(table.getBlockPos(), table.getBlockState().getBlock(), 1);
        for (int row = 0; row < 5; row++) {
            String itemId = table.requirementId(row);
            if (itemId.isEmpty()) continue;
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            if (inventory.contains(stack -> !stack.isEmpty() && stack.is(item))) {
                inventory.clearOrCountMatchingItems(stack -> stack.is(item), table.requirementCount(row),
                        player.inventoryMenu.getCraftSlots());
            }
        }
        table.changed();
    }
}
