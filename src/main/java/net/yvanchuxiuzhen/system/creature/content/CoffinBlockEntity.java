package net.yvanchuxiuzhen.system.creature.content;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 棺材保存来源开棺倒计时和生成标记；零槽容器不提供额外存储玩法。 */
public final class CoffinBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private double openTick;
    private boolean spawned;

    public CoffinBlockEntity(BlockPos pos, BlockState state) {
        super(CoffinContent.TYPE.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public double openTick() { return openTick; }
    public void setOpenTick(double openTick) { this.openTick = openTick; }
    public boolean spawned() { return spawned; }
    public void setSpawned(boolean spawned) { this.spawned = spawned; }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }

    /** 每二十游戏刻执行一次，来源倒计时的单位保持为调度次数。 */
    public void sourceTick(ServerLevel server) {
        if (openTick == 0) {
            Vec3 center = Vec3.atCenterOf(worldPosition);
            for (Entity candidate : server.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(4), entity -> true)
                    .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                if (candidate instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
                    setAnimation(server, Math.random() < 0.333 ? 3 : Math.random() < 0.5 ? 2 : 1);
                    openTick = 60;
                    sync(server);
                }
            }
        } else if (openTick >= 1) {
            openTick -= 1;
            sync(server);
            if (openTick == 0) {
                spawned = false;
                sync(server);
            } else if (openTick == 47) {
                setAnimation(server, 0);
            } else if (openTick == 49) {
                setAnimation(server, 4);
            } else if (!spawned && openTick == 59) {
                spawned = true;
                sync(server);
                spawnRefinedZombie(server);
            }
        }
    }

    private void spawnRefinedZombie(ServerLevel server) {
        Zombie zombie = new Zombie(EntityType.ZOMBIE, server);
        zombie.moveTo(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5, server.random.nextFloat() * 360, 0);
        zombie.setDeltaMovement(new Vec3(0, 0.5, 0));
        zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(28);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(350);
        EntityAttributes.update(zombie, stats -> stats
                .withStat(Stat.ATTACK_DEF, 24)
                .withStat(Stat.MANA_DEF, 15)
                .withStat(Stat.SOUL_DEF, 40));
        zombie.setCustomName(Component.literal("被炼化的僵尸"));
        zombie.setHealth(350);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        String clothes = Math.random() < 0.333 ? "05" : Math.random() < 0.5 ? "01" : "02";
        equip(zombie, EquipmentSlot.HEAD, clothes, "helmet");
        equip(zombie, EquipmentSlot.CHEST, clothes, "chestplate");
        equip(zombie, EquipmentSlot.LEGS, clothes, "leggings");
        equip(zombie, EquipmentSlot.FEET, clothes, "boots");
        zombie.finalizeSpawn(server, server.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(zombie);
    }

    private static void equip(Zombie zombie, EquipmentSlot slot, String clothes, String piece) {
        zombie.setItemSlot(slot, new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "clothes_" + clothes + "_" + piece))));
    }

    private void setAnimation(ServerLevel server, int animation) {
        server.setBlock(worldPosition, getBlockState().setValue(CoffinBlock.ANIMATION, animation), Block.UPDATE_ALL);
    }

    private void sync(ServerLevel server) {
        setChanged();
        server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        openTick = tag.getDouble("opentick");
        spawned = tag.getBoolean("shengcheng");
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("opentick", openTick);
        tag.putBoolean("shengcheng", spawned);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.guancai_01"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idle));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }

    private PlayState idle(AnimationState<CoffinBlockEntity> state) {
        return getBlockState().getValue(CoffinBlock.ANIMATION) == 0 ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP;
    }

    private PlayState procedure(AnimationState<CoffinBlockEntity> state) {
        int animation = getBlockState().getValue(CoffinBlock.ANIMATION);
        AnimationController<CoffinBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(CoffinBlock.ANIMATION, 0), Block.UPDATE_ALL);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
