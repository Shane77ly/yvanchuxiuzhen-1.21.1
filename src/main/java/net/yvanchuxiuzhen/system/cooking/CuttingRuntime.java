package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CleaverBlock;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBlockEntity;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBoardBlock;

/** 案板原引用放料、逆序收料与切菜动画的服务端状态；切菜不转换食材。 */
public final class CuttingRuntime {
    private static final TagKey<Item> INGREDIENTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "foodanban"));

    private CuttingRuntime() {
    }

    public static void placed(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CuttingBlockEntity board) {
            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            int angle = switch (direction) {
                case NORTH -> 0;
                case SOUTH -> 180;
                case WEST -> -90;
                default -> 90;
            };
            board.getPersistentData().putInt("jiaodu", angle);
            board.changed();
            server.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("message.yvanchuxiuzhen.cutting.direction." + direction.getName()), false);
        }
    }

    public static void used(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof CuttingBlockEntity board)) return;
        if (player.isShiftKeyDown()) {
            for (int slot = 8; slot >= 0; slot--) {
                if (!board.getItem(slot).isEmpty()) {
                    ItemStack returned = board.getItem(slot).copy();
                    returned.setCount(1);
                    ItemHandlerHelper.giveItemToPlayer(player, returned);
                    board.setItem(slot, ItemStack.EMPTY);
                    board.changed();
                    break;
                }
            }
        } else if (player.getMainHandItem().is(INGREDIENTS)) {
            for (int slot = 0; slot < 9; slot++) {
                if (board.getItem(slot).isEmpty()) {
                    ItemStack shared = player.getMainHandItem();
                    int count = shared.getCount();
                    shared.setCount(1);
                    board.setItem(slot, shared);
                    shared.setCount(count - 1);
                    player.setItemInHand(InteractionHand.MAIN_HAND, shared);
                    player.getInventory().setChanged();
                    board.changed();
                    break;
                }
            }
        }
    }

    public static void boardTick(ServerLevel level, BlockPos pos) {
        CuttingBlockEntity board = (CuttingBlockEntity) level.getBlockEntity(pos);
        CompoundTag data = board.getPersistentData();
        if (data.getBoolean("qiecaibegin")) {
            data.putInt("tick", data.getInt("tick") + 1);
            if (data.getInt("tick") >= 8) {
                data.putBoolean("qiecaibegin", false);
                data.putInt("tick", 0);
            }
            board.changed();
        }
    }

    public static void cleaverTick(ServerLevel level, BlockPos pos) {
        CuttingBlockEntity cleaver = (CuttingBlockEntity) level.getBlockEntity(pos);
        CompoundTag data = cleaver.getPersistentData();
        data.putDouble("tick", data.getDouble("tick") + 1);
        cleaver.changed();
        if (data.getDouble("tick") >= 8) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static void leftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide || event.getHand() != player.getUsedItemHand()) return;
        if (!BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).equals(id("caidao"))) return;
        BlockPos pos = event.getPos();
        BlockState board = level.getBlockState(pos);
        if (board.getBlock() instanceof CuttingBoardBlock && level.isEmptyBlock(pos.above())) {
            Block cleaver = BuiltInRegistries.BLOCK.get(id("caidaoblock"));
            level.setBlock(pos.above(), cleaver.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos.above(), level.getBlockState(pos.above()).setValue(BlockStateProperties.HORIZONTAL_FACING,
                    board.getValue(BlockStateProperties.HORIZONTAL_FACING)), Block.UPDATE_ALL);
            level.setBlock(pos.above(), level.getBlockState(pos.above()).setValue(CleaverBlock.ANIMATION, 1), Block.UPDATE_ALL);
            CuttingBlockEntity entity = (CuttingBlockEntity) level.getBlockEntity(pos);
            entity.getPersistentData().putBoolean("qiecaibegin", true);
            entity.changed();
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
