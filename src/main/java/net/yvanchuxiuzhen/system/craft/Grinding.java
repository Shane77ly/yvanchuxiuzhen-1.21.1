package net.yvanchuxiuzhen.system.craft;

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
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.GrindingRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftTableRecipes;

/** 按命令坐标访问库存，先掉出旧成品，再依据 JSON 石磨配方执行即时研磨。 */
public final class Grinding {
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
        for (GrindingRecipe recipe : CraftTableRecipes.grinding()) {
            if (!input.is(item(recipe.inputId()))) continue;
            for (int i = 0; i < repetitions; i++) {
                String auxiliaryId = recipe.auxiliaryId().orElse(null);
                ItemStack auxiliary = auxiliaryId == null ? ItemStack.EMPTY : inventory.getStackInSlot(2).copy();
                if (cachedInputCount < recipe.cost()
                        || auxiliaryId != null && (!auxiliary.is(item(auxiliaryId)) || auxiliary.isEmpty())) continue;
                ItemStack result = new ItemStack(item(recipe.outputId()), inventory.getStackInSlot(1).getCount() + 1);
                write(inventory, 1, result);
                ItemStack remaining = inventory.getStackInSlot(0).copy();
                remaining.shrink(recipe.cost());
                write(inventory, 0, remaining);
                if (auxiliaryId != null) {
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

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }
}
