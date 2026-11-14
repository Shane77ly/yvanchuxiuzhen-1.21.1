package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageBlockEntity;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageMenu;
import net.yvanchuxiuzhen.collection.decor.client.FurnitureStorageScreen;

/** 八种储物家具经自然放置和右键打开，检查末槽转移、侧面能力、真实重进及破坏掉落。 */
final class FurnitureStorageCheck extends SystemCheck {
    private static final int[] NUMBERS = {13, 14, 18, 19, 20, 21, 22, 24};
    private static final int[] SIZES = {54, 27, 36, 27, 54, 27, 27, 27};
    private static final int[] MENUS = {13, 14, 18, 14, 13, 14, 14, 14};
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<SavedBlock> blocks = new ArrayList<>();
    private BlockPos origin;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;

    FurnitureStorageCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("八种储物家具", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            return true;
        }
        if (step <= 40) return furniture((step - 1) / 5, (step - 1) % 5, local);
        switch (step) {
            case 41 -> leaveWorld();
            case 42 -> { return reopened(local); }
            case 43 -> {
                if (!inWorld(local)) return false;
                onServer(this::reloadAndDestroy);
                finish();
            }
            default -> throw new IllegalStateException("未知储物家具检查步骤 " + step);
        }
        return true;
    }

    private boolean furniture(int index, int phase, int local) {
        if (phase == 0) {
            onServer(player -> {
                player.getInventory().clearContent();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                use(player, pos(index));
            });
            return true;
        }
        if (local < 10) return false;
        if (phase == 1) {
            boolean[] opened = {false};
            onServer(player -> opened[0] = player.containerMenu instanceof FurnitureStorageMenu);
            if (!(minecraft.screen instanceof FurnitureStorageScreen) || !opened[0]) {
                if (local < 80) return false;
                fail("家具" + NUMBERS[index] + "未从自然右键打开客户端和服务端储物菜单");
                onServer(this::cleanup);
                finish();
                return true;
            }
            expect("家具中文界面名称" + NUMBERS[index], I18n.get("block.yvanchuxiuzhen." + path(index)), minecraft.screen.getTitle().getString());
            if (index < 3) screenshots.take("furniture_storage_" + NUMBERS[index] + ".png");
            onServer(player -> prepareTransfer(player, index));
        } else if (phase == 2) {
            click(SIZES[index]);
        } else if (phase == 3) {
            onServer(player -> expectTrue("快捷放入能到达最后一个槽" + NUMBERS[index],
                    ItemStack.matches(witness(7), storage(player, index).getItem(SIZES[index] - 1))
                            && player.getInventory().getItem(9).isEmpty()));
            click(SIZES[index] - 1);
        } else {
            onServer(player -> {
                expectTrue("末槽快捷取出反向进入末格快捷栏且物品数据保持" + NUMBERS[index],
                        storage(player, index).getItem(SIZES[index] - 1).isEmpty()
                                && ItemStack.matches(witness(7), player.getInventory().getItem(8)));
                FurnitureStorageBlockEntity storage = storage(player, index);
                storage.clearContent();
                storage.setItem(SIZES[index] - 1, witness(5));
                expectTrue("来源比较器响应库存" + NUMBERS[index], storage.getBlockState().getAnalogOutputSignal(player.level(), pos(index)) > 0);
            });
            minecraft.player.closeContainer();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        sneaking = player.isShiftKeyDown();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        origin = player.blockPosition().above(12);
        for (int index = 0; index < NUMBERS.length; index++) {
            BlockPos pos = pos(index);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above(), pos.north(2).below(), pos.north(2), pos.north(2).above())) {
                blocks.add(SavedBlock.capture(player, fixture));
                player.level().setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.level().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            player.level().setBlock(pos.north(2).below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 15);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block(index)));
            player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
            expectTrue("方块物品自然放置真实家具" + NUMBERS[index], player.level().getBlockState(pos).is(block(index))
                    && player.level().getBlockEntity(pos) instanceof FurnitureStorageBlockEntity && player.getMainHandItem().isEmpty());
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    private void prepareTransfer(ServerPlayer player, int index) {
        FurnitureStorageMenu menu = (FurnitureStorageMenu) player.containerMenu;
        expect("来源菜单复用映射" + NUMBERS[index], "beechfurniture_" + MENUS[index] + "_gui",
                BuiltInRegistries.MENU.getKey(menu.getType()).getPath());
        expect("来源库存加玩家背包容量" + NUMBERS[index], SIZES[index] + 36, menu.slots.size());
        int last = SIZES[index] - 1;
        IItemHandler unsided = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), null);
        IItemHandler top = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), Direction.UP);
        expectTrue("侧面能力实际放入带名字物品" + NUMBERS[index], top.insertItem(last, witness(3), false).isEmpty());
        for (Direction direction : Direction.values()) {
            IItemHandler side = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), direction);
            expectTrue("来源六面可放不可取" + NUMBERS[index] + direction.getName(),
                    side.insertItem(last, witness(1), true).isEmpty() && side.extractItem(last, 1, false).isEmpty());
        }
        expectTrue("无方向能力可取且玩家菜单绑定同一库存" + NUMBERS[index],
                ItemStack.matches(witness(2), unsided.extractItem(last, 2, false))
                        && ItemStack.matches(witness(1), menu.getSlot(last).getItem()));
        FurnitureStorageBlockEntity storage = storage(player, index);
        storage.clearContent();
        for (int slot = 0; slot < last; slot++) storage.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(9, witness(7));
        menu.broadcastChanges();
    }

    private void reloadAndDestroy(ServerPlayer player) {
        for (int index = 0; index < NUMBERS.length; index++) {
            BlockPos pos = pos(index);
            FurnitureStorageBlockEntity storage = storage(player, index);
            expectTrue("真实退出重进保留正确家具末槽及物品数据" + NUMBERS[index], storage.getContainerSize() == SIZES[index]
                    && ItemStack.matches(witness(5), storage.getItem(SIZES[index] - 1)) && storage.getItem(0).isEmpty());
            player.gameMode.destroyBlock(pos);
            List<ItemEntity> drops = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(.75));
            expect("破坏掉落全部带名字库存" + NUMBERS[index], 5, drops.stream()
                    .filter(drop -> ItemStack.isSameItemSameComponents(witness(1), drop.getItem()))
                    .mapToInt(drop -> drop.getItem().getCount()).sum());
            Block expected = block(index);
            expect("破坏掉落一件家具" + NUMBERS[index], 1, drops.stream().filter(drop -> drop.getItem().is(expected.asItem()))
                    .mapToInt(drop -> drop.getItem().getCount()).sum());
            expectTrue("破坏后能力随方块实体移除" + NUMBERS[index], player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null) == null);
            drops.forEach(ItemEntity::discard);
        }
        cleanup(player);
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        for (SavedBlock block : blocks) block.restore(player);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setShiftKeyDown(sneaking);
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private void click(int slot) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
    }

    private static ItemStack witness(int count) {
        ItemStack stack = new ItemStack(Items.DIAMOND, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("储物家具验收物品"));
        return stack;
    }

    private static void use(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }

    private BlockPos pos(int index) { return origin.offset(index % 4 * 4, 0, index / 4 * 4); }
    private static String path(int index) { return "beechfurniture_" + NUMBERS[index]; }
    private static Block block(int index) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path(index))); }
    private FurnitureStorageBlockEntity storage(ServerPlayer player, int index) { return (FurnitureStorageBlockEntity) player.level().getBlockEntity(pos(index)); }

    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            BlockEntity block = player.level().getBlockEntity(pos);
            return new SavedBlock(pos, player.level().getBlockState(pos), block == null ? null : block.saveWithFullMetadata(player.registryAccess()));
        }

        void restore(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
