package net.yvanchuxiuzhen.system.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** 三种地面藏宝载体的来源右键消费：功法、技能与通宝。 */
public final class TreasureInteractions {
    private TreasureInteractions() {
    }

    public static void use(Level world, BlockPos pos, Player player) {
        if (!(world instanceof ServerLevel level)) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        if (!id.getNamespace().equals("yvanchuxiuzhen")) return;
        switch (id.getPath()) {
            case "treasure_chest_04" -> TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), "书架", "功法书", Mth.nextInt(level.random, 1, 2), 1, 1);
            case "treasure_chest_05" -> TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), "书架", "技能书", Mth.nextInt(level.random, 1, 2), 1, 1);
            case "treasure_chest_06" -> {
                ItemStack money = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:money_01")), Mth.nextInt(level.random, 1, 20));
                ItemHandlerHelper.giveItemToPlayer(player, money);
            }
            default -> { return; }
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
