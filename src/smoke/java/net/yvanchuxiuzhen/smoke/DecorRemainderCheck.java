package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.collection.decor.DecorCushion;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureMenu;
import net.yvanchuxiuzhen.collection.decor.UtilityFurnitureBlockEntity;
import net.yvanchuxiuzhen.collection.decor.client.DisplayFurnitureScreen;
import net.yvanchuxiuzhen.platform.ModId;

/** 蒲团和三种余族家具的原生入口、方向库存、杯子操作、客户端同步与真实保存重进检查。 */
final class DecorRemainderCheck extends SystemCheck {
    private static final String[] IDS = {"putuan", "beechfurniture_23", "beechfurniture_29", "zisha_01"};
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, SavedBlock> blocks = new LinkedHashMap<>();
    private final Set<UUID> originalDrops = new HashSet<>();
    private BlockPos origin;
    private Vec3 position;
    private Vec3 velocity;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean noGravity;
    private boolean shift;
    private UUID cushionId;
    private final Map<Integer, String> displayDirections = new LinkedHashMap<>();

    DecorRemainderCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("装饰余族", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                minecraft.options.keyShift.setDown(false);
            }
            case 1 -> onServer(player -> use(player, pos(1)));
            case 2 -> {
                if (!menu(local, false, "榉木都承盘")) return false;
                onServer(player -> {
                    expectTrue("都承盘来源单槽菜单", player.containerMenu instanceof DisplayFurnitureMenu menu && !menu.cabinet());
                    player.getInventory().setItem(9, new ItemStack(Items.DIAMOND, 7));
                    player.containerMenu.broadcastChanges();
                });
            }
            case 3 -> {
                if (local < 10) return false;
                minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 1, 0, ClickType.QUICK_MOVE, minecraft.player);
            }
            case 4 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expect("都承盘真实QUICK_MOVE保留七件堆叠", 7, furniture(player, 1).getItem(0).getCount());
                    player.closeContainer();
                    use(player, pos(2));
                });
            }
            case 5 -> {
                if (!menu(local, true, "榉木衣架")) return false;
                screenshots.take("decor_remainder_rack_menu.png");
                onServer(player -> {
                    expectTrue("衣架复用供桌三槽菜单", player.containerMenu instanceof DisplayFurnitureMenu menu && menu.cabinet());
                    player.closeContainer();
                    capabilities(player);
                    teapot(player);
                    DecorCushion cushion = (DecorCushion) player.serverLevel().getEntity(cushionId);
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    use(player, pos(0));
                    expectTrue("蒲团真实右键成为独立载具乘客", player.getVehicle() == cushion && !cushion.isPushable());
                    player.stopRiding();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.connection.teleport(origin.getX() + 6, origin.getY() + 3, origin.getZ() - 6, 0, 20);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 6 -> {
                if (local < 20) return false;
                clientInventory();
                screenshots.take("decor_remainder_before_save.png");
                leaveWorld();
            }
            case 7 -> { return reopened(local); }
            case 8 -> {
                if (!inWorld(local)) return false;
                onServer(this::reloaded);
            }
            case 9 -> {
                if (local < 20) return false;
                clientInventory();
                screenshots.take("decor_remainder_reloaded.png");
                onServer(player -> {
                    player.getInventory().clearContent();
                    use(player, pos(1));
                });
            }
            case 10 -> {
                if (!menu(local, false, "榉木都承盘")) return false;
                minecraft.gameMode.handleInventoryButtonClick(minecraft.player.containerMenu.containerId, 0);
            }
            case 11 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("Q219原生网络按钮默认转一号博古架且只返一件并清整槽", player.level().getBlockState(pos(1)).is(block("beechfurniture_1501"))
                            && player.getInventory().countItem(Items.DIAMOND) == 1);
                    expect("都承盘网络换型保留文字朝向", displayDirections.get(1),
                            ((UtilityFurnitureBlockEntity) player.level().getBlockEntity(pos(1))).displayDirection());
                    player.closeContainer();
                    use(player, pos(0));
                    expectTrue("重进后蒲团可以再次乘坐原载具", player.getVehicle() != null && player.getVehicle().getUUID().equals(cushionId));
                    player.stopRiding();
                    player.gameMode.destroyBlock(pos(0));
                    expectTrue("玩家真实破坏蒲团移除其载具", player.serverLevel().getEntity(cushionId) == null
                            || player.serverLevel().getEntity(cushionId).isRemoved());
                    removalCallbacks(player);
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知装饰余族检查步骤 " + step);
        }
        return true;
    }

    private boolean menu(int local, boolean rack, String name) {
        if (!(minecraft.screen instanceof DisplayFurnitureScreen)) {
            if (local < 100) return false;
            fail(name + "原生右键未打开客户端菜单");
            onServer(this::cleanup);
            finish();
            return false;
        }
        expect("菜单中文语义标题 " + name, I18n.get("block.yvanchuxiuzhen." + IDS[rack ? 2 : 1]), minecraft.screen.getTitle().getString());
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        velocity = player.getDeltaMovement();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        sneaking = player.isShiftKeyDown();
        noGravity = player.isNoGravity();
        shift = minecraft.options.keyShift.isDown();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        origin = player.blockPosition().above(8);
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(18)).forEach(drop -> originalDrops.add(drop.getUUID()));
        for (int index = 0; index < IDS.length; index++) {
            BlockPos pos = pos(index);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above(), pos.above(2), pos.north(), pos.south(), pos.west(), pos.east())) {
                blocks.put(fixture, SavedBlock.capture(player, fixture));
                player.level().removeBlockEntity(fixture);
                player.level().setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.level().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            if (index < 3) {
                for (Direction side : Direction.Plane.HORIZONTAL) player.level().setBlock(pos.relative(side), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                player.level().setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, index * 90, 15);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block(IDS[index])));
            player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
            expectTrue("来源方块物品真实放置并扣件 " + IDS[index], player.level().getBlockState(pos).is(block(IDS[index])) && player.getMainHandItem().isEmpty());
            BlockState state = player.level().getBlockState(pos);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            expect("放置朝向取玩家水平方向的反向 " + IDS[index], player.getDirection().getOpposite(), facing);
            expectTrue("来源含水能力边界 " + IDS[index], index < 3
                    ? state.getValue(BlockStateProperties.WATERLOGGED) && state.getFluidState().is(Fluids.WATER)
                    : !state.hasProperty(BlockStateProperties.WATERLOGGED));
            if (index > 0) {
                String direction = switch (facing) { case NORTH -> "北"; case SOUTH -> "南"; case WEST -> "西"; default -> "东"; };
                displayDirections.put(index, direction);
                expect("自然放置写入来源文字朝向 " + IDS[index], direction, furniture(player, index).displayDirection());
            }
        }
        List<DecorCushion> cushions = player.level().getEntitiesOfClass(DecorCushion.class, new AABB(pos(0)).inflate(1));
        expect("蒲团放置仅生成一只独立载具", 1, cushions.size());
        if (cushions.size() != 1) {
            cleanup(player);
            finish();
            return;
        }
        cushionId = cushions.getFirst().getUUID();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private void capabilities(ServerPlayer player) {
        for (int index = 1; index <= 3; index++) {
            UtilityFurnitureBlockEntity furniture = furniture(player, index);
            for (Direction direction : Direction.values()) {
                IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), direction);
                for (int slot = 0; slot < furniture.getContainerSize(); slot++) {
                    ItemStack previous = furniture.getItem(slot).copy();
                    furniture.setItem(slot, ItemStack.EMPTY);
                    expectTrue("六面允许插入 " + IDS[index] + direction + slot, handler.insertItem(slot, new ItemStack(Items.STICK), false).isEmpty());
                    expect("六面取出遵守来源规则 " + IDS[index] + direction + slot, index == 3 ? 1 : 0, handler.extractItem(slot, 1, false).getCount());
                    furniture.setItem(slot, previous);
                }
            }
            expect("来源堆叠上限 " + IDS[index], index == 3 ? 1 : 64, furniture.getMaxStackSize());
            if (index < 3) {
                ItemStack previous = furniture.getItem(0).copy();
                IItemHandlerModifiable handler = (IItemHandlerModifiable) player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), null);
                handler.setStackInSlot(0, new ItemStack(Items.DIAMOND_SWORD, 3));
                expect("来源无方向强制赋槽保留超物品上限数量 " + IDS[index], 3, furniture.getItem(0).getCount());
                furniture.setItem(0, previous);
            }
            if (index == 2) {
                IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos(index), null);
                for (int slot = 0; slot < furniture.getContainerSize(); slot++) handler.insertItem(slot, new ItemStack(Items.EMERALD, slot + 2), false);
                expect("衣架无方向共享原库存且允许取出", 1, handler.extractItem(2, 1, false).getCount());
                expect("衣架末槽保留剩余三件", 3, furniture.getItem(2).getCount());
                ItemStack swords = new ItemStack(Items.DIAMOND_SWORD, 3);
                swords.set(DataComponents.CUSTOM_NAME, Component.literal("来源强制库存"));
                furniture.setItem(0, swords);
            }
        }
    }

    private void teapot(ServerPlayer player) {
        UtilityFurnitureBlockEntity teapot = furniture(player, 3);
        ItemStack cups = new ItemStack(block("zisha_02"), 6);
        cups.set(DataComponents.CUSTOM_NAME, Component.literal("杯子组件不可复制"));
        player.setItemInHand(InteractionHand.MAIN_HAND, cups);
        for (int i = 0; i < 4; i++) use(player, pos(3));
        expect("四次自然右键装四杯并扣四件", 2, cups.getCount());
        for (int slot = 0; slot < 4; slot++) expectTrue("装入新普通杯而非复制组件 " + slot,
                ItemStack.matches(new ItemStack(block("zisha_02")), teapot.getItem(slot)));
        use(player, pos(3));
        expect("满壶不继续扣杯", 2, cups.getCount());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        use(player, pos(3));
        expectTrue("非杯主手按逆序取最后一杯", teapot.getItem(3).isEmpty() && !teapot.getItem(2).isEmpty());
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, cups);
        use(player, pos(3));
        expectTrue("创造模式仍扣杯并补首个空槽", cups.getCount() == 1 && teapot.getItem(3).is(block("zisha_02").asItem()));
        player.setGameMode(GameType.SURVIVAL);
    }

    private void clientInventory() {
        for (int index = 1; index <= 3; index++) {
            BlockEntity entity = minecraft.level.getBlockEntity(pos(index));
            expectTrue("客户端收到文字朝向和库存 " + IDS[index], entity instanceof UtilityFurnitureBlockEntity furniture
                    && furniture.displayDirection().equals(displayDirections.get(index)) && !furniture.getItem(furniture.getContainerSize() - 1).isEmpty());
            if (index == 2) expectTrue("客户端保留衣架强制赋槽的三把带名剑", entity instanceof UtilityFurnitureBlockEntity furniture
                    && furniture.getItem(0).getCount() == 3 && furniture.getItem(0).getHoverName().getString().equals("来源强制库存"));
        }
    }
    private void reloaded(ServerPlayer player) {
        expectTrue("真实重进蒲团载具UUID仍在且未重复生成", player.serverLevel().getEntity(cushionId) instanceof DecorCushion
                && player.level().getEntitiesOfClass(DecorCushion.class, new AABB(pos(0)).inflate(1)).size() == 1);
        expect("真实重进都承盘保留七件", 7, furniture(player, 1).getItem(0).getCount());
        expect("真实重进衣架末槽保留三件", 3, furniture(player, 2).getItem(2).getCount());
        expectTrue("真实重进保留强制赋槽三把剑和组件", furniture(player, 2).getItem(0).getCount() == 3
                && furniture(player, 2).getItem(0).getHoverName().getString().equals("来源强制库存"));
        for (int index = 1; index <= 3; index++) expect("真实重进文字朝向 " + IDS[index], displayDirections.get(index), furniture(player, index).displayDirection());
        for (int slot = 0; slot < 4; slot++) expectTrue("真实重进四杯仍在 " + slot, furniture(player, 3).getItem(slot).is(block("zisha_02").asItem()));
    }

    private void removalCallbacks(ServerPlayer player) {
        expect("蒲团空loot来源回退掉自身", 1, drops(player, block("putuan").asItem()));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.gameMode.destroyBlock(pos(2));
        expect("衣架玩家破坏掉出六枚绿宝石", 6, drops(player, Items.EMERALD));
        expect("衣架玩家破坏保留强制库存的三把剑", 3, drops(player, Items.DIAMOND_SWORD));
        expect("衣架玩家破坏掉出自身", 1, drops(player, block("beechfurniture_29").asItem()));
        player.gameMode.destroyBlock(pos(3));
        expect("紫砂壶玩家破坏掉出四枚普通杯", 4, drops(player, block("zisha_02").asItem()));
        expect("紫砂壶玩家破坏掉出自身", 1, drops(player, block("zisha_01").asItem()));
        Vec3 dismountPosition = player.position();
        AABB dismountBounds = player.getBoundingBox();
        player.connection.teleport(pos(0).getX() + .5, pos(0).getY(), pos(0).getZ() - 2, 0, 15);
        player.setDeltaMovement(Vec3.ZERO);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block("putuan")));
        InteractionResult placement = player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos(0).below()), Direction.UP, pos(0).below(), false));
        BlockState placedState = player.level().getBlockState(pos(0));
        boolean placed = placedState.is(block("putuan")) && player.getMainHandItem().isEmpty();
        expectTrue("第二次蒲团自然放置并扣件，结果=" + placement + "，下车位置=" + dismountPosition
                + "，下车碰撞箱=" + dismountBounds + "，放置位置=" + player.position()
                + "，方块=" + placedState + "，主手=" + player.getMainHandItem(), placed);
        if (!placed) return;
        List<DecorCushion> cushions = player.level().getEntitiesOfClass(DecorCushion.class, new AABB(pos(0)).inflate(1));
        expect("第二次自然放置重新生成蒲团载具", 1, cushions.size());
        if (cushions.size() != 1) return;
        cushionId = cushions.getFirst().getUUID();
        block("putuan").wasExploded(player.level(), pos(0), new Explosion(player.level(), null,
                pos(0).getX() + .5, pos(0).getY(), pos(0).getZ() + .5, 0, false, Explosion.BlockInteraction.KEEP));
        expectTrue("来源爆炸回调删除中心蒲团载具", player.serverLevel().getEntity(cushionId) == null
                || player.serverLevel().getEntity(cushionId).isRemoved());
    }
    private int drops(ServerPlayer player, Item item) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(18)).stream()
                .filter(drop -> !originalDrops.contains(drop.getUUID()) && drop.getItem().is(item)).mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.stopRiding();
        if (cushionId != null && player.serverLevel().getEntity(cushionId) != null) player.serverLevel().getEntity(cushionId).discard();
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(18)).stream()
                .filter(drop -> !originalDrops.contains(drop.getUUID())).forEach(ItemEntity::discard);
        for (SavedBlock saved : blocks.values()) saved.restore(player);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(sneaking);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.setDeltaMovement(velocity);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.options.keyShift.setDown(shift));
    }
    private BlockPos pos(int index) { return origin.offset(index * 4, 0, 0); }
    private UtilityFurnitureBlockEntity furniture(ServerPlayer player, int index) { return (UtilityFurnitureBlockEntity) player.level().getBlockEntity(pos(index)); }
    private static Block block(String path) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)); }
    private static void use(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }
    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            BlockEntity entity = player.level().getBlockEntity(pos);
            return new SavedBlock(pos, player.level().getBlockState(pos), entity == null ? null : entity.saveWithFullMetadata(player.registryAccess()));
        }
        void restore(ServerPlayer player) {
            player.level().removeBlockEntity(pos);
            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            player.level().setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) player.level().setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
