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
    private final CompoundTag state = new CompoundTag();

    public FormationBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ZhenfaBlockEntities.BASE.get(), pos, state);
        for (Direction direction : Direction.values()) handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : handlers[direction.ordinal()]; }
    public int power() { return state.getInt("power"); }
    public CompoundTag state() { return state; }
    public CompoundTag formationSnapshot() { return state.copy(); }

    public void detect() {
        FormationLayout.detect(level, worldPosition, state);
        changed();
    }

    public void redstone(boolean powered) {
        if (!powered || state.getInt("zhenchufa") == 1) state.putBoolean("zhenhongshi", powered);
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
        state.putInt("power", power() + value);
        held.shrink(1);
        player.getInventory().setChanged();
        ((ServerLevel) level).sendParticles(Particles.type("lingqi"), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.1);
        changed();
    }

    /** 旗物品只提交控制者及射线结果；参数仍由本阵基读取，坐标维持来源的 double 写入。 */
    public void control(Player player) {
        state.putDouble("zhenjiaodu", player.getYRot());
        state.putDouble("zhentaitou", player.getXRot());
        state.putString("ManaAttackUUID", player.getStringUUID());
        state.putDouble("playerX", player.getX());
        state.putDouble("playerY", player.getY());
        state.putDouble("playerZ", player.getZ());
        if (state.getDouble("zhenshijiao") > 0) {
            double distance = 1 + state.getDouble("zhenshijiao") * 0.5 * state.getDouble("zhenjvli");
            Vec3 eye = player.getEyePosition(1);
            BlockPos hit = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(distance)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos();
            state.putDouble("zhenchufaX", hit.getX());
            state.putDouble("zhenchufaY", hit.getY());
            state.putDouble("zhenchufaZ", hit.getZ());
        } else if (state.getDouble("zhenqian") == 3) {
            state.putDouble("zhenchufaX", player.getX());
            state.putDouble("zhenchufaY", player.getY());
            state.putDouble("zhenchufaZ", player.getZ());
        }
        state.putBoolean("zhenbegin", true);
        changed();
    }

    public void scheduledTick(ServerLevel level) {
        if (state.getBoolean("zhenfazhengqve")) {
            int cost = state.getInt("powerneed");
            if (cost > 0 && power() > cost) {
                state.putInt("tick", state.getInt("tick") + 1);
                if (state.getInt("tick") >= 10) {
                    if (state.getInt("zhenchufa") == 3) {
                        Vec3 center = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
                        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(state.getInt("zhenjvli") / 2d))
                                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                            if (matchesTeam(entity)) FormationEffects.execute(this);
                            state.putInt("power", power() - cost);
                        }
                    } else if (state.getBoolean("zhenhongshi")) {
                        FormationEffects.execute(this);
                        state.putInt("power", power() - cost);
                    }
                    state.putInt("tick", 0);
                }
                if (state.getBoolean("zhenbegin") || state.getInt("zhenchufa") == 2) {
                    FormationEffects.execute(this);
                    state.putInt("power", power() - cost);
                    state.putBoolean("zhenbegin", false);
                }
            } else {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, 10, 5, 5, 5, 0.1);
            }
        }
        if (!(level.getBlockEntity(worldPosition.above()) instanceof FormationMarkerBlockEntity marker) || marker.compass()) {
            state.putBoolean("zhenfazhengqve", false);
        }
        setChanged();
    }

    boolean matchesTeam(Entity entity) {
        int mode = state.getInt("zhenmubiao");
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
        state.getAllKeys().clear();
        if (tag.contains("formation_state")) state.merge(tag.getCompound("formation_state"));
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!state.isEmpty()) tag.put("formation_state", state.copy());
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
