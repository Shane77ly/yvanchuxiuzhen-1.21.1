package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;

/** 03与11通过真实能力查询验证无方向插入、六面共库、提取和序列化。 */
final class TreasureChestCapabilityCheck extends SystemCheck {
    TreasureChestCapabilityCheck(List<String> report, String levelId) {
        super("宝箱无方向库存", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(player -> {
            check(player, "treasure_chest_03", player.blockPosition().above(8));
            check(player, "treasure_chest_11", player.blockPosition().above(8).east(2));
        });
        finish();
        return true;
    }

    private void check(ServerPlayer player, String name, BlockPos pos) {
        BlockState previous = player.level().getBlockState(pos);
        BlockEntity original = player.level().getBlockEntity(pos);
        CompoundTag previousData = original == null ? null : original.saveWithFullMetadata(player.registryAccess());
        player.level().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name)).defaultBlockState(), 3);
        TreasureChestBlockEntity chest = (TreasureChestBlockEntity) player.level().getBlockEntity(pos);
        IItemHandler unsided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        expectTrue(name + "无方向物品能力存在且只有一个单件槽", unsided != null && unsided.getSlots() == 1 && unsided.getSlotLimit(0) == 1);
        if (unsided != null) {
            ItemStack remainder = unsided.insertItem(0, new ItemStack(Items.DIAMOND, 3), false);
            expectTrue(name + "真实无方向插入只接收一件", remainder.is(Items.DIAMOND) && remainder.getCount() == 2
                    && chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 1);
            boolean shared = true;
            for (Direction direction : Direction.values()) {
                IItemHandler sided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
                shared &= sided != null && sided.getStackInSlot(0).is(Items.DIAMOND) && sided.getStackInSlot(0).getCount() == 1;
            }
            expectTrue(name + "六面真实能力读回无方向写入的唯一库存", shared);
            IItemHandler south = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.SOUTH);
            ItemStack extracted = south == null ? ItemStack.EMPTY : south.extractItem(0, 1, false);
            expectTrue(name + "侧面提取同步清空无方向库存", extracted.is(Items.DIAMOND) && unsided.getStackInSlot(0).isEmpty());
            expectTrue(name + "无方向再次插入接受一枚绿宝石", unsided.insertItem(0, new ItemStack(Items.EMERALD), false).isEmpty());
            TreasureChestBlockEntity restored = (TreasureChestBlockEntity) BlockEntity.loadStatic(pos, chest.getBlockState(),
                    chest.saveWithFullMetadata(player.registryAccess()), player.registryAccess());
            boolean persisted = restored != null && restored.handler(null).getStackInSlot(0).is(Items.EMERALD);
            for (Direction direction : Direction.values()) {
                persisted &= restored != null && restored.handler(direction).getStackInSlot(0).is(Items.EMERALD);
            }
            expectTrue(name + "序列化重读后无方向和六面仍共享绿宝石库存", persisted);
            unsided.extractItem(0, 1, false);
        }
        chest.clearContent();
        player.level().setBlock(pos, previous, 3);
        BlockEntity restored = player.level().getBlockEntity(pos);
        if (previousData != null && restored != null) {
            restored.loadWithComponents(previousData, player.registryAccess());
            restored.setChanged();
        }
    }
}
