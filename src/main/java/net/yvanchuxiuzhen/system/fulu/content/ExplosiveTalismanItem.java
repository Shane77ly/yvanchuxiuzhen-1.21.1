package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 按物品品阶在点击方块相邻的空格放置爆裂符。 */
public final class ExplosiveTalismanItem extends ContentItem {
    public ExplosiveTalismanItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.adventure_forbidden"), false);
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        double grade = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("fululevel");
        String blockId = grade < 1 ? "fulutexiao_42"
                : grade == 1 ? "fulutexiao_42"
                : grade == 2 ? "fulutexiao_4202"
                : grade == 3 ? "fulutexiao_4203"
                : grade >= 4 ? "fulutexiao_4204" : null;

        BlockPos clickedPos = context.getClickedPos();
        BlockPos placePos = clickedPos.relative(context.getClickedFace());
        if (level.isEmptyBlock(placePos)) {
            Block block = blockId == null ? Blocks.AIR
                    : BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, blockId));
            BlockState state = block.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = context.getClickedFace().getAxis().isVertical()
                        ? player.getDirection() : context.getClickedFace();
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            }
            level.setBlock(placePos, state, Block.UPDATE_ALL);
            level.playSound(null, clickedPos,
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulushifang")),
                    SoundSource.NEUTRAL, 1, 1);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
