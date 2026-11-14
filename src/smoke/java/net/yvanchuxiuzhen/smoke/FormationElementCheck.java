package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationElementBlockEntity;

/** 阵元自然放置、材料取放、伪装、六面及无方向共库、保存重进与破坏回收的必要单项。 */
final class FormationElementCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> oldBlocks = new HashMap<>();
    private final Map<BlockPos, CompoundTag> oldBlockEntities = new HashMap<>();
    private final Set<UUID> oldDrops = new HashSet<>();
    private BlockPos floor;
    private BlockPos station;
    private BlockPos origin;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean shift;
    private boolean noGravity;

    FormationElementCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("阵元载体", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 10) return false;
                use(floor, 0);
            }
            case 2 -> {
                if (local < 10) return false;
                boolean[] placed = new boolean[1];
                onServer(player -> {
                    placed[0] = player.level().getBlockEntity(station) instanceof FormationElementBlockEntity;
                    expectTrue("普通物品经自然右键放置为阵元实体", placed[0]);
                    if (!placed[0]) cleanup(player);
                });
                if (!placed[0]) { finish(); return true; }
                use(station, 1);
            }
            case 3 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("布阵材料存一件并保留实例组件", element(player).material().is(item("cailiao_191").getItem())
                            && element(player).material().getCount() == 1
                            && Component.literal("阵元检查材料").equals(element(player).material().get(DataComponents.CUSTOM_NAME))
                            && player.getInventory().getItem(1).getCount() == 2);
                    expectTrue("材料槽同步到客户端展示", synchronizedItems(player));
                    // 阵基检测尚未迁移，本次只通过公开操作种入已核实的坐标与展示角度。
                    element(player).assignFormation(origin, 75F);
                });
                use(station, 2);
            }
            case 4 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("普通非材料物品不取出槽零", !element(player).material().isEmpty()
                            && player.getInventory().getItem(2).is(Items.STICK));
                    expectTrue("布局角度同步至客户端", clientElement().displayAngle() == 75F);
                });
                screenshots.take("formation_element_material.png");
                use(station, 3);
            }
            case 5 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("非材料方块生成无实例组件伪装且不消耗主手", element(player).disguise().is(Items.DIAMOND_BLOCK)
                            && element(player).disguise().getCount() == 1
                            && element(player).disguise().get(DataComponents.CUSTOM_NAME) == null
                            && player.getInventory().getItem(3).getCount() == 2);
                    expectTrue("伪装与材料两槽同步且比较器为十五", synchronizedItems(player)
                            && element(player).getBlockState().getAnalogOutputSignal(player.level(), station) == 15);
                });
                screenshots.take("formation_element_disguise.png");
                use(station, 3);
            }
            case 6 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("再次持方块仅清伪装并保留材料和主手数量", element(player).disguise().isEmpty()
                        && !element(player).material().isEmpty() && player.getInventory().getItem(3).getCount() == 2));
                use(station, 0);
            }
            case 7 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("空手收回一件材料且总数守恒", element(player).material().isEmpty()
                            && player.getInventory().countItem(item("cailiao_191").getItem()) == 3);
                    automation(player);
                    CompoundTag saved = element(player).saveWithFullMetadata(player.registryAccess());
                    FormationElementBlockEntity restored = new FormationElementBlockEntity(station, element(player).getBlockState());
                    restored.loadWithComponents(saved, player.registryAccess());
                    expectTrue("两槽与布局坐标角度经实体序列化保持", ItemStack.matches(restored.material(), element(player).material())
                            && ItemStack.matches(restored.disguise(), element(player).disguise())
                            && restored.formationOrigin().equals(origin) && restored.displayAngle() == 75F);
                });
                leaveWorld();
            }
            case 8 -> { return reopened(local); }
            case 9 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    expectTrue("真实保存重进保留材料伪装坐标与角度", element(player).material().is(item("cailiao_191").getItem())
                            && element(player).disguise().is(Items.DIAMOND_BLOCK)
                            && element(player).formationOrigin().equals(origin) && element(player).displayAngle() == 75F);
                    expectTrue("重进后客户端恢复两槽展示", synchronizedItems(player));
                });
                screenshots.take("formation_element_reloaded.png");
                onServer(player -> {
                    player.getInventory().selected = 4;
                    player.gameMode.destroyBlock(station);
                    List<ItemEntity> drops = newDrops(player);
                    expect("正常工具破坏回收一件阵元", 1, countDrops(drops, item("zhenyvan")));
                    expect("正常工具破坏回收一件材料", 1, countDrops(drops, item("cailiao_191")));
                    expect("来源生成的伪装同样掉落一件", 1, countDrops(drops, new ItemStack(Items.DIAMOND_BLOCK)));
                    drops.forEach(ItemEntity::discard);
                    cleanup(player);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        sneaking = player.isShiftKeyDown();
        shift = minecraft.options.keyShift.isDown();
        noGravity = player.isNoGravity();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        floor = player.blockPosition().above(4);
        station = floor.above();
        origin = floor.offset(-2, 0, 6);
        for (BlockPos pos : List.of(floor, station, floor.east(2))) {
            oldBlocks.put(pos, player.level().getBlockState(pos));
            BlockEntity original = player.level().getBlockEntity(pos);
            if (original != null) oldBlockEntities.put(pos, original.saveWithFullMetadata(player.registryAccess()));
        }
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(station).inflate(2)).forEach(drop -> oldDrops.add(drop.getUUID()));
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
        player.level().setBlock(floor.east(2), Blocks.STONE.defaultBlockState(), 3);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, item("zhenyvan"));
        ItemStack material = item("cailiao_191").copyWithCount(3);
        material.set(DataComponents.CUSTOM_NAME, Component.literal("阵元检查材料"));
        player.getInventory().setItem(1, material);
        player.getInventory().setItem(2, new ItemStack(Items.STICK));
        ItemStack disguise = new ItemStack(Items.DIAMOND_BLOCK, 2);
        disguise.set(DataComponents.CUSTOM_NAME, Component.literal("阵元检查伪装"));
        player.getInventory().setItem(3, disguise);
        player.getInventory().setItem(4, new ItemStack(Items.DIAMOND_PICKAXE));
        player.inventoryMenu.broadcastChanges();
        player.connection.teleport(floor.getX() + 2.5, floor.getY() + 1, floor.getZ() + 0.5, 90, 15);
    }

    private void automation(ServerPlayer player) {
        IItemHandler unsided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, station, null);
        expectTrue("空方向能力按来源父类提供两槽", unsided != null && unsided.getSlots() == 2);
        if (unsided == null) return;
        expectTrue("空方向能力真实插入只接收一件", unsided.insertItem(0, new ItemStack(Items.DIAMOND, 3), false).getCount() == 2);
        unsided.insertItem(1, new ItemStack(Items.EMERALD), false);
        for (Direction direction : Direction.values()) {
            IItemHandler sided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, station, direction);
            expectTrue("六面能力共用两槽且容量一 " + direction, sided.getSlots() == 2
                    && sided.getStackInSlot(0).is(Items.DIAMOND) && sided.getStackInSlot(1).is(Items.EMERALD)
                    && sided.getSlotLimit(0) == 1 && sided.getSlotLimit(1) == 1);
        }
        IItemHandler down = player.level().getCapability(Capabilities.ItemHandler.BLOCK, station, Direction.DOWN);
        expectTrue("侧面取出立即清空六面及空方向共库", down.extractItem(0, 1, false).is(Items.DIAMOND)
                && down.extractItem(1, 1, false).is(Items.EMERALD)
                && unsided.getStackInSlot(0).isEmpty() && unsided.getStackInSlot(1).isEmpty());
        unsided.insertItem(0, item("cailiao_191"), false);
        useOnServer(player, 3);
    }

    private void useOnServer(ServerPlayer player, int slot) {
        player.getInventory().selected = slot;
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(station), Direction.UP, station, false));
    }

    private void use(BlockPos pos, int slot) {
        minecraft.player.getInventory().selected = slot;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
    }

    private FormationElementBlockEntity element(ServerPlayer player) {
        return (FormationElementBlockEntity) player.level().getBlockEntity(station);
    }

    private FormationElementBlockEntity clientElement() {
        return (FormationElementBlockEntity) minecraft.level.getBlockEntity(station);
    }

    private boolean synchronizedItems(ServerPlayer player) {
        FormationElementBlockEntity client = clientElement();
        return client != null && ItemStack.matches(client.material(), element(player).material())
                && ItemStack.matches(client.disguise(), element(player).disguise());
    }

    private List<ItemEntity> newDrops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(station).inflate(2)).stream()
                .filter(drop -> !oldDrops.contains(drop.getUUID())).toList();
    }

    private static int countDrops(List<ItemEntity> drops, ItemStack stack) {
        return drops.stream().filter(drop -> drop.getItem().is(stack.getItem())).mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private void cleanup(ServerPlayer player) {
        if (player.level().getBlockEntity(station) instanceof FormationElementBlockEntity element) element.clearContent();
        for (Map.Entry<BlockPos, BlockState> entry : oldBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            player.level().setBlock(pos, entry.getValue(), 3);
            if (oldBlockEntities.containsKey(pos)) {
                BlockEntity restored = player.level().getBlockEntity(pos);
                restored.loadWithComponents(oldBlockEntities.get(pos), player.registryAccess());
                restored.setChanged();
                player.level().sendBlockUpdated(pos, entry.getValue(), entry.getValue(), 3);
            }
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(sneaking);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.options.keyShift.setDown(shift));
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }
}
