package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;

/** 按命令坐标访问库存，先掉出旧成品，再以缓存的主料数量执行即时研磨。 */
public final class Grinding {
    private record Recipe(String input, int cost, String output, String auxiliary) {
    }

    private static final List<Recipe> RECIPES = List.of(
            new Recipe("shicai_001", 4, "shicai_00102", null),
            new Recipe("shicai_002", 4, "shicai_00202", null),
            new Recipe("minecraft:wheat", 4, "shicai_003", null),
            new Recipe("fuluzhushablock", 1, "fuluzhushaitem", null),
            new Recipe("shicai_046", 4, "food_039", "foodwan"),
            new Recipe("shicai_048", 4, "food_003", "foodwan"),
            new Recipe("shicai_058", 4, "tiaowei_08", null));

    private Grinding() {
    }

    public static void grind(ServerLevel level, Vec3 origin, String count) {
        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(origin), null);
        if (inventory == null) return;
        int previousCount = inventory.getStackInSlot(1).getCount();
        for (int i = 0; i < previousCount; i++) {
            ItemEntity drop = new ItemEntity(level, origin.x + 0.5, origin.y + 0.9, origin.z + 0.5,
                    inventory.getStackInSlot(1).copy());
            drop.setPickUpDelay(10);
            level.addFreshEntity(drop);
        }
        write(inventory, 1, ItemStack.EMPTY);
        ItemStack input = inventory.getStackInSlot(0).copy();
        int cachedInputCount = input.getCount();
        int repetitions = repetitions(count);
        for (Recipe recipe : RECIPES) {
            if (!input.is(item(recipe.input()))) continue;
            for (int i = 0; i < repetitions; i++) {
                ItemStack auxiliary = recipe.auxiliary() == null ? ItemStack.EMPTY : inventory.getStackInSlot(2).copy();
                if (cachedInputCount < recipe.cost()
                        || recipe.auxiliary() != null && (!auxiliary.is(item(recipe.auxiliary())) || auxiliary.isEmpty())) continue;
                ItemStack result = new ItemStack(item(recipe.output()), inventory.getStackInSlot(1).getCount() + 1);
                write(inventory, 1, result);
                ItemStack remaining = inventory.getStackInSlot(0).copy();
                remaining.shrink(recipe.cost());
                write(inventory, 0, remaining);
                if (recipe.auxiliary() != null) {
                    auxiliary.shrink(1);
                    write(inventory, 2, auxiliary);
                }
            }
        }
    }

    private static int repetitions(String value) {
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static void write(IItemHandler inventory, int slot, ItemStack stack) {
        if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(slot, stack);
    }

    private static Item item(String path) {
        ResourceLocation id = path.contains(":") ? ResourceLocation.parse(path) : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        return BuiltInRegistries.ITEM.get(id);
    }
}
