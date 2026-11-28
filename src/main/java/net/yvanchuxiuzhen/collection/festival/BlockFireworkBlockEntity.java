package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 三种烟花共用来源零槽容器契约，各自保存引信、燃放状态与计划刻计数。 */
public final class BlockFireworkBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final BlockFireworkKind kind;
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];
    private boolean begin;
    private boolean begin2;
    private double tick;

    public BlockFireworkBlockEntity(BlockFireworkKind kind, BlockPos pos, BlockState state) {
        super(BlockFireworks.type(kind), pos, state);
        this.kind = kind;
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }
    public boolean begin() { return begin; }
    public boolean begin2() { return begin2; }
    public void setTick(double value) { tick = value; changed(); }

    public void ignite() {
        begin = true;
        changed();
    }

    public void scheduledTick(ServerLevel level) {
        if (begin) {
            tick += 1;
            double height = kind == BlockFireworkKind.BATTERY ? 2 : kind == BlockFireworkKind.FOUNTAIN ? .7 : 0;
            level.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + .5, worldPosition.getY() + height,
                    worldPosition.getZ() + .5, 5, 0, 0, 0, kind == BlockFireworkKind.BATTERY ? 0 : .1);
            if (tick >= (kind == BlockFireworkKind.BATTERY ? 2 : 20)) {
                if (kind == BlockFireworkKind.FIRECRACKER) level.playSound(null, worldPosition,
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao1001")), SoundSource.NEUTRAL, 2, 1);
                begin = false;
                begin2 = true;
                tick = 0;
            }
            changed();
        } else if (begin2) {
            switch (kind) {
                case FIRECRACKER -> firecracker(level);
                case FOUNTAIN -> fountain(level);
                case BATTERY -> battery(level);
            }
            tick += 1;
            if (tick >= (kind == BlockFireworkKind.BATTERY ? 60 : 600)) {
                begin2 = false;
                if (kind == BlockFireworkKind.FIRECRACKER) {
                    for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
                        if (level.getBlockEntity(worldPosition.offset(x, y, z)) instanceof BlockFireworkBlockEntity next
                                && next.kind == BlockFireworkKind.FIRECRACKER) next.ignite();
                    }
                }
                tick = 0;
                changed();
                level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else changed();
        }
    }

    private void firecracker(ServerLevel level) {
        double x = worldPosition.getX() + .5, y = worldPosition.getY() + .5, z = worldPosition.getZ() + .5;
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 5, 0, 0, 0, .1);
        level.sendParticles(Particles.type("pao_1001"), x, y, z, 5, .3, .3, .3, 1);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, .3, .3, .3, 0);
    }

    private void fountain(ServerLevel level) {
        double x = worldPosition.getX() + .5, y = worldPosition.getY() + .7, z = worldPosition.getZ() + .5;
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, .1);
        for (int index = 0; index < 3; index++) directed(level, "pao_0201", x, y, z);
        directed(level, "pao_030" + (1 + level.random.nextInt(6)), x, y, z);
    }

    private static void directed(ServerLevel level, String id, double x, double y, double z) {
        double dx = Math.random() - .5, dz = Math.random() - .5;
        for (ServerPlayer viewer : level.players()) level.sendParticles(viewer, Particles.type(id), true, x, y, z, 0, dx, 1, dz, 1);
    }

    private void battery(ServerLevel level) {
        double x = worldPosition.getX() + .5, y = worldPosition.getY() + 2.5, z = worldPosition.getZ() + .5;
        level.sendParticles(Particles.type("pao_0201"), x, y - .5, z, 20, 0, 0, 0, .1);
        Pao13Entity.battery(level, x, y, z);
        level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1, 1);
    }

    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block." + ModId.VALUE + "." + kind.id()); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(0, ItemStack.EMPTY);
        begin = tag.getBoolean("begin");
        begin2 = tag.getBoolean("begin2");
        tick = tag.getDouble("tick");
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (begin) tag.putBoolean("begin", true);
        if (begin2) tag.putBoolean("begin2", true);
        if (tick != 0) tag.putDouble("tick", tick);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
