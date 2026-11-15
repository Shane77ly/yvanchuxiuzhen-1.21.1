package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureBlockEntity;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureMenu;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureVariant;
import net.yvanchuxiuzhen.collection.decor.client.DisplayFurnitureScreen;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockDisplayLock;

/** 天道工具和六种展示家具的必要单项，涵盖真实使用、共享锁持久化及退出重进后的展示。 */
final class TiandaoDisplayCheck extends SystemCheck {
    private static final DisplayFurnitureVariant[] VARIANTS = DisplayFurnitureVariant.values();
    private static final String[] PLACED_DIRECTIONS = {"北", "东", "南", "西", "北", "东"};
    private final Screenshots screenshots;
    private final List<String> observations;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, SavedBlock> blocks = new LinkedHashMap<>();
    private final List<UUID> entities = new ArrayList<>();
    private final Set<UUID> originalDrops = new HashSet<>();
    private BlockPos origin;
    private Vec3 position;
    private Vec3 velocity;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean shift;
    private boolean noGravity;

    TiandaoDisplayCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("天道工具与展示家具", report, levelId);
        this.screenshots = screenshots;
        observations = report;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                minecraft.options.keyShift.setDown(false);
            }
            case 1 -> {
                onServer(player -> {
                    movement(player);
                    displayEntities(player);
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    useBlock(player, pos(0));
                });
            }
            case 2 -> {
                if (!(minecraft.screen instanceof DisplayFurnitureScreen)) {
                    if (local < 100) return false;
                    fail("供桌真实右键未打开客户端展示菜单");
                    onServer(this::cleanup);
                    finish();
                    return true;
                }
                expect("供桌中文语义标题", I18n.get("block.yvanchuxiuzhen.beechfurniture_03"), minecraft.screen.getTitle().getString());
                onServer(player -> {
                    expectTrue("供桌服务端三展示槽加背包", player.containerMenu instanceof DisplayFurnitureMenu menu
                            && menu.cabinet() && menu.slots.size() == 39);
                    player.getInventory().setItem(9, witness(false, 3));
                    player.containerMenu.broadcastChanges();
                });
            }
            case 3 -> {
                if (local < 10) return false;
                click(3);
            }
            case 4 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("供桌菜单真实QUICK_MOVE按来源装满三个展示槽并保留组件",
                        display(player, 0).getItem(0).getCount() == 1 && display(player, 0).getItem(1).getCount() == 1
                                && display(player, 0).getItem(2).getCount() == 1
                                && ItemStack.matches(witness(false, 1), display(player, 0).getItem(0))
                                && ItemStack.matches(witness(false, 1), display(player, 0).getItem(1))
                                && ItemStack.matches(witness(false, 1), display(player, 0).getItem(2))
                                && player.getInventory().getItem(9).isEmpty()));
                screenshots.take("tiandao_display_cabinet_menu.png");
                click(0);
            }
            case 5 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("供桌菜单真实取回单件且其他展示件及背包数量守恒",
                            display(player, 0).getItem(0).isEmpty() && display(player, 0).getItem(1).getCount() == 1
                            && display(player, 0).getItem(2).getCount() == 1
                            && player.getInventory().countItem(Items.DIAMOND) == 1);
                    player.closeContainer();
                    furniture(player);
                    player.connection.teleport(origin.getX() + 4.5, origin.getY() + 1.6, origin.getZ() - 4.5, 0, 13);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 6 -> {
                if (local < 15) return false;
                leaveWorld();
            }
            case 7 -> { return reopened(local); }
            case 8 -> {
                if (!inWorld(local)) return false;
                onServer(this::reloaded);
            }
            case 9 -> {
                if (local < 15) return false;
                expectTrue("展示菜单切换按钮中文合并有效", I18n.exists("gui.yvanchuxiuzhen.beechfurniture_15_gui.button_qie_huan")
                        && !I18n.get("gui.yvanchuxiuzhen.beechfurniture_15_gui.button_qie_huan").contains("gui."));
                screenshots.take("tiandao_display_reloaded.png");
                onServer(player -> {
                    player.getInventory().clearContent();
                    player.connection.teleport(pos(1).getX() + .5, pos(1).getY(), pos(1).getZ() - 2, 0, 15);
                    player.openMenu(display(player, 1), pos(1));
                });
            }
            case 10 -> {
                if (!(minecraft.screen instanceof DisplayFurnitureScreen)) {
                    if (local < 100) return false;
                    fail("架独立MenuProvider未打开客户端来源菜单");
                    onServer(this::cleanup);
                    finish();
                    return true;
                }
                if (local < 10) return false;
                onServer(player -> expectTrue("锁不阻断来源独立MenuProvider的一槽菜单", player.containerMenu instanceof DisplayFurnitureMenu menu
                        && !menu.cabinet() && menu.slots.size() == 37));
                screenshots.take("tiandao_display_stand_menu.png");
                Button button = minecraft.screen.children().stream().filter(child -> child instanceof Button value
                                && value.getMessage().equals(Component.translatable("gui.yvanchuxiuzhen.beechfurniture_15_gui.button_qie_huan")))
                        .map(child -> (Button) child).findFirst().orElseThrow();
                button.onPress();
            }
            case 11 -> {
                if (local < 10) return false;
                onServer(player -> {
                    DisplayFurnitureBlockEntity changed = display(player, 1);
                    expectTrue("菜单按钮先返还一件再按默认状态换型", changed.variant() == DisplayFurnitureVariant.STAND_3
                            && changed.getItem(0).isEmpty() && player.getInventory().countItem(Items.DIAMOND) == 1
                            && changed.displayDirection().equals(PLACED_DIRECTIONS[1]) && !BlockDisplayLock.locked(changed));
                    menuTickCoordinate(player);
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知天道展示检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        velocity = player.getDeltaMovement();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        sneaking = player.isShiftKeyDown();
        shift = minecraft.options.keyShift.isDown();
        noGravity = player.isNoGravity();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        origin = player.blockPosition().above(8);
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(18)).forEach(drop -> originalDrops.add(drop.getUUID()));
        for (int index = 0; index < VARIANTS.length; index++) {
            BlockPos pos = pos(index);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above(), pos.above(2))) {
                capture(player, fixture);
                player.level().removeBlockEntity(fixture);
                player.level().setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.level().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, index % 4 * 90, 15);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block(VARIANTS[index])));
            player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
            expectTrue("方块物品真实放置并扣一件 " + VARIANTS[index].id(), player.level().getBlockState(pos).is(block(VARIANTS[index]))
                    && player.level().getBlockEntity(pos) instanceof DisplayFurnitureBlockEntity && player.getMainHandItem().isEmpty());
            expect("放置保存来源文字朝向 " + VARIANTS[index].id(), PLACED_DIRECTIONS[index], display(player, index).displayDirection());
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    private void movement(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item("ling_08", 2));
        checkMovement(player, 0, 0, new Vec3(0, 0, 1));
        checkMovement(player, 90, 30, new Vec3(-1, -.5, 0));
        checkMovement(player, 180, 90, new Vec3(0, -1, -1));
        player.setDeltaMovement(Vec3.ZERO);
    }

    private void checkMovement(ServerPlayer player, float yRot, float xRot, Vec3 expected) {
        player.setYRot(yRot);
        player.setXRot(xRot);
        InteractionResult result = player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        Vec3 actual = player.getDeltaMovement();
        expectTrue("穿梭令实际use视角 " + yRot + "/" + xRot, actual.distanceToSqr(expected) < 1e-12);
        expectTrue("穿梭令保留PASS和两件原栈 " + yRot + "/" + xRot,
                result == InteractionResult.PASS && player.getMainHandItem().is(item("ling_08", 1).getItem())
                        && player.getMainHandItem().getCount() == 2);
    }

    private void displayEntities(ServerPlayer player) {
        player.connection.teleport(origin.getX() + 14.5, origin.getY(), origin.getZ() + .5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("ling_16", 1));
        ArmorStand first = stand(player, player.position().add(0, 0, 1));
        ArmorStand second = stand(player, player.position().add(1, 0, 1));
        player.setShiftKeyDown(false);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        expectTrue("物品锁空气use同时锁定范围内两盔甲架", data(first).getBoolean("Marker") && data(second).getBoolean("Marker"));
        player.setShiftKeyDown(true);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        expectTrue("潜行空气use隐藏同一范围两盔甲架", first.isInvisible() && second.isInvisible());
        ArmorStand clicked = stand(player, player.position().add(4, 0, 0));
        player.setShiftKeyDown(false);
        interact(player, clicked, InteractionHand.OFF_HAND);
        expectTrue("实体事件保留来源使用手过滤", !data(clicked).getBoolean("Marker"));
        interact(player, clicked, InteractionHand.MAIN_HAND);
        expectTrue("真实实体右键事件设置盔甲架Marker", data(clicked).getBoolean("Marker"));
        player.setShiftKeyDown(true);
        interact(player, clicked, InteractionHand.MAIN_HAND);
        expectTrue("真实实体右键事件隐藏盔甲架", clicked.isInvisible());
        BlockPos framePos = origin.offset(15, 1, 3);
        for (BlockPos support : List.of(framePos.south(), framePos.above().south())) {
            capture(player, support);
            player.level().removeBlockEntity(support);
            player.level().setBlock(support, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
        ItemFrame frame = new ItemFrame(player.level(), framePos, Direction.NORTH);
        GlowItemFrame glow = new GlowItemFrame(player.level(), framePos.above(), Direction.NORTH);
        for (ItemFrame target : List.of(frame, glow)) {
            target.setItem(witness(false, 1));
            player.level().addFreshEntity(target);
            entities.add(target.getUUID());
            player.setShiftKeyDown(false);
            interact(player, target, InteractionHand.MAIN_HAND);
            expectTrue("真实物品框事件设置Fixed " + BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getPath(), data(target).getBoolean("Fixed"));
            player.setShiftKeyDown(true);
            interact(player, target, InteractionHand.MAIN_HAND);
            expectTrue("真实物品框事件设置Invisible并保留展示物 " + BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getPath(), target.isInvisible()
                    && ItemStack.matches(witness(false, 1), target.getItem()));
        }
        expectTrue("展示实体操作不消耗物品锁", player.getMainHandItem().getCount() == 1);
        for (UUID id : entities) {
            Entity target = player.serverLevel().getEntity(id);
            if (target != null) target.discard();
        }
        player.stopUsingItem();
        player.setShiftKeyDown(false);
    }

    private void furniture(ServerPlayer player) {
        for (int index = 0; index < VARIANTS.length; index++) {
            DisplayFurnitureVariant original = VARIANTS[index];
            BlockPos pos = pos(index);
            player.getInventory().clearContent();
            player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 15);
            player.setShiftKeyDown(false);
            DisplayFurnitureBlockEntity display = display(player, index);
            IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            ItemStack leftover = handler.insertItem(0, witness(false, 3), false);
            expectTrue("无方向能力最多接收一件并和BE共库 " + original.id(), leftover.getCount() == 2
                    && ItemStack.matches(witness(false, 1), display.getItem(0)));
            handler.extractItem(0, 1, false);
            if (!original.cabinet()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, witness(false, 3));
                useBlock(player, pos);
                expectTrue("架普通右键复制一件且主手减一 " + original.id(), ItemStack.matches(witness(false, 1), display.getItem(0))
                        && player.getMainHandItem().getCount() == 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD, 2));
                useBlock(player, pos);
                expectTrue("架替换返还旧件并放一件新件 " + original.id(), display.getItem(0).is(Items.EMERALD)
                        && display.getItem(0).getCount() == 1 && player.getMainHandItem().getCount() == 1
                        && player.getInventory().countItem(Items.DIAMOND) == 1);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                useBlock(player, pos);
                expectTrue("架空手取回原槽 " + original.id(), display.getItem(0).isEmpty() && player.getInventory().countItem(Items.EMERALD) == 1);
                player.setItemInHand(InteractionHand.MAIN_HAND, item("ling_16", 2));
                useBlock(player, pos);
                expectTrue("Q192普通右键仍可把物品锁放入未锁架 " + original.id(), display.getItem(0).is(item("ling_16", 1).getItem())
                        && player.getMainHandItem().getCount() == 1 && !BlockDisplayLock.locked(display));
                display.setItem(0, witness(index % 2 == 0, 1));
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setShiftKeyDown(true);
                useBlock(player, pos);
                display = display(player, index);
                expectTrue("潜行右键换下一型并保留单件和文字朝向 " + original.id(), display.variant() == original.next()
                        && ItemStack.matches(witness(index % 2 == 0, 1), display.getItem(0))
                        && display.displayDirection().equals(PLACED_DIRECTIONS[index]));
                ItemStack expectedDrop = witness(index % 2 == 0, 1);
                int drops = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1)).stream()
                        .filter(drop -> ItemStack.isSameItemSameComponents(expectedDrop, drop.getItem()))
                        .mapToInt(drop -> drop.getItem().getCount()).sum();
                observations.add("INFO 天道工具与展示家具 Q194 " + original.id() + "潜行换型附近同组件掉落=" + drops + "；只记录本次观测，裁定仍开放");
            } else {
                display.setItem(0, witness(false, 1));
                display.setItem(1, witness(true, 1));
            }
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, item("ling_16", 1));
            useBlock(player, pos);
            expectTrue("物品useOn实际锁定家具且不扣物品 " + original.id(), BlockDisplayLock.locked(display)
                    && player.getMainHandItem().getCount() == 1);
            BlockState lockedState = player.level().getBlockState(pos);
            ItemStack stored = display.getItem(0).copy();
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 3));
            useBlock(player, pos);
            expectTrue("锁后普通右键拒绝开页或换件 " + original.id(), player.containerMenu == player.inventoryMenu
                    && ItemStack.matches(stored, display.getItem(0)) && player.getMainHandItem().getCount() == 3);
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            useBlock(player, pos);
            expectTrue("锁后潜行空手拒绝换型 " + original.id(), player.level().getBlockState(pos) == lockedState
                    && ItemStack.matches(stored, display.getItem(0)));
            CompoundTag saved = display.saveWithFullMetadata(player.registryAccess());
            DisplayFurnitureBlockEntity restored = new DisplayFurnitureBlockEntity(display.variant(), pos, lockedState);
            restored.loadWithComponents(saved, player.registryAccess());
            expectTrue("库存和共享布尔锁完整序列化 " + original.id(), BlockDisplayLock.locked(restored)
                    && ItemStack.matches(stored, restored.getItem(0)) && restored.displayDirection().equals(PLACED_DIRECTIONS[index]));
        }
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    private void reloaded(ServerPlayer player) {
        for (int index = 0; index < VARIANTS.length; index++) {
            DisplayFurnitureBlockEntity display = display(player, index);
            DisplayFurnitureVariant expected = VARIANTS[index].cabinet() ? VARIANTS[index] : VARIANTS[index].next();
            expectTrue("真实保存重进保留展示身份库存文字和共享锁 " + VARIANTS[index].id(), display.variant() == expected
                    && BlockDisplayLock.locked(display) && display.displayDirection().equals(PLACED_DIRECTIONS[index])
                    && ItemStack.matches(witness(index != 0 && index % 2 == 0, 1), display.getItem(0)));
            expectTrue("重进客户端恢复展示库存 " + VARIANTS[index].id(), minecraft.level.getBlockEntity(pos(index)) instanceof DisplayFurnitureBlockEntity client
                    && client.variant() == expected && ItemStack.matches(display.getItem(0), client.getItem(0)));
        }
        expectTrue("重进供桌第二展示位保留方块组件", ItemStack.matches(witness(true, 1), display(player, 0).getItem(1)));
    }

    private void menuTickCoordinate(ServerPlayer player) {
        BlockPos feet = origin.offset(15, 0, 5);
        capture(player, feet);
        player.level().removeBlockEntity(feet);
        player.level().setBlock(feet, Blocks.BARREL.defaultBlockState(), Block.UPDATE_ALL);
        IItemHandler inventory = player.level().getCapability(Capabilities.ItemHandler.BLOCK, feet, null);
        inventory.insertItem(0, witness(false, 2), false);
        player.connection.teleport(feet.getX() + .5, feet.getY(), feet.getZ() + .5, 0, 0);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        expectTrue("Q196空架菜单的真实Post事件清玩家坐标槽零且不归还", inventory.getStackInSlot(0).isEmpty()
                && player.getInventory().countItem(Items.DIAMOND) == 1 && player.containerMenu.getSlot(0).getItem().isEmpty());
    }

    private ArmorStand stand(ServerPlayer player, Vec3 position) {
        ArmorStand stand = new ArmorStand(player.level(), position.x, position.y, position.z);
        stand.setNoGravity(true);
        player.level().addFreshEntity(stand);
        entities.add(stand.getUUID());
        return stand;
    }

    private static void interact(ServerPlayer player, Entity target, InteractionHand hand) {
        player.startUsingItem(InteractionHand.MAIN_HAND);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, hand, target));
        player.stopUsingItem();
    }

    private static void useBlock(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }

    private void click(int slot) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
    }

    private void capture(ServerPlayer player, BlockPos pos) {
        blocks.computeIfAbsent(pos, ignored -> SavedBlock.capture(player, pos));
    }

    private BlockPos pos(int index) { return origin.offset(index % 3 * 4, 0, index / 3 * 4); }
    private DisplayFurnitureBlockEntity display(ServerPlayer player, int index) { return (DisplayFurnitureBlockEntity) player.level().getBlockEntity(pos(index)); }
    private static Block block(DisplayFurnitureVariant variant) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.id())); }
    private static ItemStack item(String path, int count) { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)), count); }
    private static ItemStack witness(boolean block, int count) {
        ItemStack stack = new ItemStack(block ? Items.DIAMOND_BLOCK : Items.DIAMOND, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(block ? "展示方块验收" : "展示物品验收"));
        return stack;
    }
    private static CompoundTag data(Entity entity) {
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        return tag;
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        for (UUID id : entities) {
            Entity entity = player.serverLevel().getEntity(id);
            if (entity != null) entity.discard();
        }
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
