package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.Comparator;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 阵基独占布局参数、灵力、触发计数和控制坐标；二刻计划刻按来源顺序推进。 */
public final class FormationBaseBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] handlers = new IItemHandler[6];

    public FormationBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ZhenfaBlockEntities.BASE.get(), pos, state);
        for (Direction direction : Direction.values()) handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : handlers[direction.ordinal()]; }
    public int power() { return getPersistentData().getInt("power"); }
    public CompoundTag formationSnapshot() { return getPersistentData().copy(); }

    public void detect() {
        FormationLayout.detect(level, worldPosition, getPersistentData());
        changed();
    }

    public void redstone(boolean powered) {
        if (!powered || getPersistentData().getInt("zhenchufa") == 1) getPersistentData().putBoolean("zhenhongshi", powered);
        setChanged();
    }

    public void fuel(Player player) {
        ItemStack held = player.getMainHandItem();
        if (!held.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi")))) return;
        String id = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
        int value = 0;
        for (int rank = 0; rank < 9; rank++) {
            if (id.equals("cailiao_" + (191 + rank))) value = 1 << (rank * 2);
        }
        getPersistentData().putInt("power", power() + value);
        held.shrink(1);
        player.getInventory().setChanged();
        ((ServerLevel) level).sendParticles(Particles.type("lingqi"), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.1);
        changed();
    }

    /** 旗物品只提交控制者及射线结果；参数仍由本阵基读取，坐标维持来源的 double 写入。 */
    public void control(Player player) {
        CompoundTag data = getPersistentData();
        data.putDouble("zhenjiaodu", player.getYRot());
        data.putDouble("zhentaitou", player.getXRot());
        data.putString("ManaAttackUUID", player.getStringUUID());
        data.putDouble("playerX", player.getX());
        data.putDouble("playerY", player.getY());
        data.putDouble("playerZ", player.getZ());
        if (data.getDouble("zhenshijiao") > 0) {
            double distance = 1 + data.getDouble("zhenshijiao") * 0.5 * data.getDouble("zhenjvli");
            Vec3 eye = player.getEyePosition(1);
            BlockPos hit = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(distance)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos();
            data.putDouble("zhenchufaX", hit.getX());
            data.putDouble("zhenchufaY", hit.getY());
            data.putDouble("zhenchufaZ", hit.getZ());
        } else if (data.getDouble("zhenqian") == 3) {
            data.putDouble("zhenchufaX", player.getX());
            data.putDouble("zhenchufaY", player.getY());
            data.putDouble("zhenchufaZ", player.getZ());
        }
        data.putBoolean("zhenbegin", true);
        changed();
    }

    public void scheduledTick(ServerLevel level) {
        CompoundTag data = getPersistentData();
        if (data.getBoolean("zhenfazhengqve")) {
            int cost = data.getInt("powerneed");
            if (cost > 0 && power() > cost) {
                data.putInt("tick", data.getInt("tick") + 1);
                if (data.getInt("tick") >= 10) {
                    if (data.getInt("zhenchufa") == 3) {
                        Vec3 center = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
                        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(data.getInt("zhenjvli") / 2d))
                                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                            if (matchesTeam(entity)) FormationEffects.yin(this);
                            data.putInt("power", power() - cost);
                        }
                    } else if (data.getBoolean("zhenhongshi")) {
                        FormationEffects.yin(this);
                        data.putInt("power", power() - cost);
                    }
                    data.putInt("tick", 0);
                }
                if (data.getBoolean("zhenbegin") || data.getInt("zhenchufa") == 2) {
                    FormationEffects.yin(this);
                    data.putInt("power", power() - cost);
                    data.putBoolean("zhenbegin", false);
                }
            } else {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, 10, 5, 5, 5, 0.1);
            }
        }
        if (!(level.getBlockEntity(worldPosition.above()) instanceof FormationMarkerBlockEntity marker) || marker.compass()) {
            data.putBoolean("zhenfazhengqve", false);
        }
        setChanged();
    }

    boolean matchesTeam(Entity entity) {
        int mode = getPersistentData().getInt("zhenmubiao");
        String team = level.getBlockEntity(worldPosition.above()) instanceof FormationMarkerBlockEntity marker ? marker.team() : "";
        return mode == 1 ? team.equals(Relations.entityTeam(entity)) : mode != 2 || !team.equals(Relations.entityTeam(entity));
    }

    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.zhenji"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(0, ItemStack.EMPTY);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
