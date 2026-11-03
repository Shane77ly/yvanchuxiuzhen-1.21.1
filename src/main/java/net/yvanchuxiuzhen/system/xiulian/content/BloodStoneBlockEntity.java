package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 吸血石的方块实体：保存充血量（来源方块实体数据 {@code xveyetianchong}）。充血量变化时同步给客户端，供渲染血池高度。
 */
public final class BloodStoneBlockEntity extends BlockEntity {
    private static final String BLOOD = "xveyetianchong";
    private int blood;

    public BloodStoneBlockEntity(BlockPos pos, BlockState state) {
        super(XiulianBlockEntities.BLOOD_STONE.get(), pos, state);
    }

    public int blood() {
        return blood;
    }

    public void setBlood(int blood) {
        this.blood = blood;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        blood = tag.getInt(BLOOD);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(BLOOD, blood);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
