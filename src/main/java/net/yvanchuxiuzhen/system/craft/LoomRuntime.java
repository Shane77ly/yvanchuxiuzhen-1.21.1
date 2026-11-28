package net.yvanchuxiuzhen.system.craft;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.craft.content.LoomBlock;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomBasicRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomCloudRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomPairRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftTableRecipes;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 织布机即时加工主手材料；按 JSON 配方匹配并按来源顺序扣除背包辅助材料。 */
public final class LoomRuntime {
    private LoomRuntime() {
    }

    public static void use(Level world, BlockPos pos, Player player) {
        if (world instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    5, 0, 0.1, 0, 0.1);
        }
        world.setBlock(pos, world.getBlockState(pos).setValue(LoomBlock.ANIMATION, 1), Block.UPDATE_ALL);
        if (world.isClientSide) return;

        ItemStack main = player.getMainHandItem();
        for (LoomBasicRecipe recipe : CraftTableRecipes.loomBasic()) {
            if (!main.is(item(recipe.inputId()))) continue;
            give(player, recipe.outputId(), 1, recipe.materialLevel());
            main.shrink(1);
            return;
        }
        for (LoomPairRecipe pair : CraftTableRecipes.loomPairs()) {
            Item itemA = item(pair.inputA());
            Item itemB = item(pair.inputB());
            if (main.is(itemA)) {
                weavePair(player, main, itemB, pair);
                return;
            }
            if (main.is(itemB)) {
                weavePair(player, main, itemA, pair);
                return;
            }
        }
        var cloudRecipes = CraftTableRecipes.loomCloud();
        if (!cloudRecipes.isEmpty() && main.is(item(cloudRecipes.getFirst().mainId()))) {
            for (LoomCloudRecipe cloud : cloudRecipes) {
                if (!main.is(item(cloud.mainId()))) continue;
                Item auxiliary = item(cloud.auxiliaryId());
                if (!contains(player, auxiliary)) continue;
                consume(player, auxiliary);
                give(player, cloud.outputId(), 1, cloud.materialLevel());
                main.shrink(1);
                return;
            }
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.loom_requires_cloth")
                    .withStyle(ChatFormatting.DARK_RED), false);
        }
    }

    private static void weavePair(Player player, ItemStack main, Item auxiliary, LoomPairRecipe pair) {
        if (!contains(player, auxiliary)) return;
        give(player, pair.outputId(), pair.count(), pair.materialLevel());
        main.shrink(1);
        consume(player, auxiliary);
    }

    /** 来源 contains 只比较物品身份，保留已写入等级和品阶的材料匹配。 */
    private static boolean contains(Player player, Item item) {
        return player.getInventory().contains(stack -> !stack.isEmpty() && stack.is(item));
    }

    private static void consume(Player player, Item item) {
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), 1, player.inventoryMenu.getCraftSlots());
    }

    private static void give(Player player, String outputId, int count, int level) {
        ItemStack result = new ItemStack(item(outputId), count);
        int roll = player.getRandom().nextInt(100) + 1;
        int quality = roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
        Materials.setInstance(result, level, quality);
        ItemHandlerHelper.giveItemToPlayer(player, result);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }
}
