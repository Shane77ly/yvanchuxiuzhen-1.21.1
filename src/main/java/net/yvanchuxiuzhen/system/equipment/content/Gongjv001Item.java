package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentAxe;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.platform.content.block.TraitedBlock;

/** 镰刀右键清理来源植物材质方块，并保留来源的范围与冷却。 */
public final class Gongjv001Item extends ContentAxe {
    public Gongjv001Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            clearPlants(serverLevel, serverPlayer, result.getObject());
        }
        return result;
    }

    private static void clearPlants(ServerLevel level, ServerPlayer player, ItemStack stack) {
        BlockPos origin = player.blockPosition();
        for (int x = -8; x < 8; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -8; z < 8; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (isPlant(state)) {
                        Block.dropResources(state, level, origin, null);
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
        player.getCooldowns().addCooldown(stack.getItem(), 10);
    }

    private static boolean isPlant(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof TraitedBlock traited) {
            String material = traited.traits().row().identity().material();
            if (material.equals("PLANT") || material.equals("REPLACEABLE_PLANT")) {
                return true;
            }
        }
        return block instanceof BushBlock || block instanceof VineBlock || block instanceof GrowingPlantBlock
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS);
    }
}
