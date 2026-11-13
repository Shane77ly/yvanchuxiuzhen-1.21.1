package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MouseHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.client.BookBindingScreen;
import net.yvanchuxiuzhen.system.book.content.BookBindingBlockEntity;
import net.yvanchuxiuzhen.system.book.content.BookBindingMenu;

/** 装订台自然放置、进料、领取、关闭、真实重进和破坏掉落；前置配方由原生配方对象装配。 */
final class BookBindingCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private BlockPos floor;
    private BlockPos station;
    private BlockPos stand;
    private BlockState oldFloor;
    private BlockState oldStation;
    private BlockState oldStand;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean shift;

    BookBindingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("装订书籍", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 10) return false;
                use(floor);
            }
            case 2 -> {
                if (local < 10) return false;
                boolean[] placed = new boolean[1];
                onServer(player -> {
                    placed[0] = table(player) != null;
                    expectTrue("来源合成结果经自然右键放置为真实装订台", placed[0]);
                    if (!placed[0]) cleanup(player);
                });
                if (!placed[0]) { finish(); return true; }
                use(station);
            }
            case 3 -> {
                if (local < 10) return false;
                if (!opened()) return true;
                onServer(player -> {
                    expect("中文菜单标题", "装订针线", minecraft.screen.getTitle().getString());
                    expect("菜单槽数三十八加玩家三十六", 74, player.containerMenu.slots.size());
                    expectTrue("无料时全部三十六结果位为空", emptyOutputs(table(player)));
                    BookBindingMenu menu = (BookBindingMenu) player.containerMenu;
                    expectTrue("纸线和输出位维持各自过滤", menu.customSlot(0).mayPlace(new ItemStack(Items.PAPER))
                            && !menu.customSlot(0).mayPlace(new ItemStack(Items.STRING))
                            && menu.customSlot(1).mayPlace(new ItemStack(Items.STRING))
                            && !menu.customSlot(2).mayPlace(item("book_001", 1)));
                });
                screenshots.take("book_binding_empty.png");
                click(38, ClickType.QUICK_MOVE);
                click(39, ClickType.QUICK_MOVE);
            }
            case 4 -> {
                if (local < 10) return false;
                onServer(player -> {
                    expectTrue("两纸三线不足时没有结果", table(player).getItem(0).getCount() == 2
                            && table(player).getItem(1).getCount() == 3 && emptyOutputs(table(player)));
                });
                click(40, ClickType.QUICK_MOVE);
            }
            case 5 -> {
                if (local < 10) {
                    hover(0);
                    return false;
                }
                onServer(player -> {
                    BookBindingBlockEntity table = table(player);
                    int count = 0;
                    for (int slot = 2; slot < 38; slot++) if (!table.getItem(slot).isEmpty()) count++;
                    expect("来源目录实际二十四种", 24, count);
                    expectTrue("目录顺序首尾及余下十二槽为空", table.getItem(2).is(item("book_001", 1).getItem())
                            && table.getItem(25).is(item("book_035", 1).getItem()) && table.getItem(26).isEmpty() && table.getItem(37).isEmpty());
                    expectTrue("侧面禁止预览取放且输入可取", table.handler(Direction.DOWN).extractItem(2, 1, true).isEmpty()
                            && table.handler(Direction.UP).insertItem(2, item("book_001", 1), true).getCount() == 1
                            && table.handler(Direction.DOWN).extractItem(0, 1, true).is(Items.PAPER));
                    for (int slot = 0; slot < 38; slot++) expectTrue("库存双端同步槽" + slot,
                            ItemStack.matches(((BookBindingMenu) player.containerMenu).customSlot(slot).getItem(),
                                    ((BookBindingMenu) minecraft.player.containerMenu).customSlot(slot).getItem()));
                });
                screenshots.take("book_binding_catalog.png");
                click(0, ClickType.PICKUP);
            }
            case 6 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("普通领取一本空白书消耗三纸一线并清目录", player.containerMenu.getCarried().is(item("book_001", 1).getItem())
                        && player.containerMenu.getCarried().getCount() == 1 && table(player).getItem(0).isEmpty()
                        && table(player).getItem(1).getCount() == 2 && emptyOutputs(table(player))));
                click(41, ClickType.PICKUP);
                onServer(player -> {
                    player.getInventory().setItem(9, new ItemStack(Items.PAPER, 6));
                    player.containerMenu.broadcastChanges();
                });
            }
            case 7 -> {
                if (local < 8) return false;
                click(38, ClickType.QUICK_MOVE);
            }
            case 8 -> {
                if (local < 10) return false;
                click(23, ClickType.QUICK_MOVE);
            }
            case 9 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("快捷领取末项空白书仍只扣一次三纸一线", count(player, "book_001") == 1
                        && count(player, "book_035") == 1 && table(player).getItem(0).getCount() == 3 && table(player).getItem(1).getCount() == 1));
                minecraft.player.closeContainer();
            }
            case 10 -> {
                if (local < 10) return false;
                onServer(player -> {
                    BookBindingBlockEntity original = table(player);
                    expectTrue("关闭清所有预览且保留真实输入", emptyOutputs(original)
                            && original.getItem(0).getCount() == 3 && original.getItem(1).getCount() == 1);
                    CompoundTag tag = original.saveWithFullMetadata(player.registryAccess());
                    BookBindingBlockEntity restored = new BookBindingBlockEntity(station, original.getBlockState());
                    restored.loadWithComponents(tag, player.registryAccess());
                    expectTrue("三十八槽经真实方块实体序列化重读保持", restored.getContainerSize() == 38
                            && ItemStack.matches(original.getItem(0), restored.getItem(0))
                            && ItemStack.matches(original.getItem(1), restored.getItem(1)) && emptyOutputs(restored));
                    expectTrue("比较器响应剩余输入", original.getBlockState().getAnalogOutputSignal(player.level(), station) > 0);
                });
                use(station);
            }
            case 11 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("重开依剩余材料重建目录", !table(player).getItem(2).isEmpty()));
                leaveWorld();
            }
            case 12 -> { return reopened(local); }
            case 13 -> {
                if (!inWorld(local)) return false;
                onServer(player -> expectTrue("打开菜单退出重进后只保存输入和已取书", emptyOutputs(table(player))
                        && table(player).getItem(0).getCount() == 3 && table(player).getItem(1).getCount() == 1
                        && count(player, "book_001") == 1 && count(player, "book_035") == 1));
                use(station);
            }
            case 14 -> {
                if (local < 10) {
                    hover(23);
                    return false;
                }
                onServer(player -> expectTrue("存档重进后自然打开恢复二十四项目录", !table(player).getItem(2).isEmpty()
                        && table(player).getItem(25).is(item("book_035", 1).getItem())));
                screenshots.take("book_binding_reopened.png");
                minecraft.player.closeContainer();
            }
            case 15 -> {
                if (local < 10) return false;
                onServer(player -> {
                    player.gameMode.destroyBlock(station);
                    List<ItemEntity> drops = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(station).inflate(1));
                    expect("关闭后破坏掉落三张余纸", 3, dropCount(drops, "minecraft:paper"));
                    expect("关闭后破坏掉落一根余线", 1, dropCount(drops, "minecraft:string"));
                    expect("关闭后破坏掉落一件装订台", 1, dropCount(drops, ModId.VALUE + ":makebook"));
                    expectTrue("关闭后破坏不掉落目录预览", drops.stream().noneMatch(drop ->
                            BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).getPath().startsWith("book_")));
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
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        floor = player.blockPosition().above(4);
        station = floor.above();
        stand = floor.east(2);
        oldFloor = player.level().getBlockState(floor);
        oldStation = player.level().getBlockState(station);
        oldStand = player.level().getBlockState(stand);
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(stand, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        ShapedRecipe recipe = (ShapedRecipe) player.server.getRecipeManager().byKey(id("makebookhecheng")).orElseThrow().value();
        CraftingInput input = CraftingInput.of(3, 3, List.of(new ItemStack(Items.STRING), ItemStack.EMPTY, new ItemStack(Items.STRING),
                new ItemStack(Items.STRING), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.STRING),
                new ItemStack(Items.STRING), ItemStack.EMPTY, new ItemStack(Items.STRING)));
        expectTrue("六线与木板来源合成配方可用", recipe.matches(input, player.level()));
        ItemStack crafted = recipe.assemble(input, player.registryAccess());
        expectTrue("来源配方产物为一件装订台", crafted.is(item("makebook", 1).getItem()) && crafted.getCount() == 1);
        expectTrue("装订台保留来源含水状态", BuiltInRegistries.BLOCK.get(id("makebook")).defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, true).getFluidState().getType() == Fluids.WATER);
        player.setItemInHand(InteractionHand.MAIN_HAND, crafted);
        player.getInventory().setItem(9, new ItemStack(Items.PAPER, 2));
        player.getInventory().setItem(10, new ItemStack(Items.STRING, 3));
        player.getInventory().setItem(11, new ItemStack(Items.PAPER));
        player.inventoryMenu.broadcastChanges();
        player.connection.teleport(floor.getX() + 2.5, floor.getY() + 1, floor.getZ() + .5, 90, 0);
    }

    private boolean opened() {
        boolean result = minecraft.screen instanceof BookBindingScreen && minecraft.player.containerMenu instanceof BookBindingMenu;
        expectTrue("自然右键打开装订界面", result);
        if (!result) {
            onServer(this::cleanup);
            finish();
        }
        return result;
    }

    private void use(BlockPos pos) {
        minecraft.player.getInventory().selected = 0;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0, .5, 0), Direction.UP, pos, false));
    }

    private void click(int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private void hover(int slot) {
        int x = (minecraft.screen.width - 176) / 2 + 15 + slot % 9 * 18;
        int y = (minecraft.screen.height - 204) / 2 + 17 + slot / 9 * 18;
        try {
            Method move = MouseHandler.class.getDeclaredMethod("onMove", long.class, double.class, double.class);
            move.setAccessible(true);
            move.invoke(minecraft.mouseHandler, minecraft.getWindow().getWindow(),
                    x * (double) minecraft.getWindow().getScreenWidth() / minecraft.getWindow().getGuiScaledWidth(),
                    y * (double) minecraft.getWindow().getScreenHeight() / minecraft.getWindow().getGuiScaledHeight());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法设置装订台悬停坐标", exception);
        }
    }

    private BookBindingBlockEntity table(ServerPlayer player) {
        return player.level().getBlockEntity(station) instanceof BookBindingBlockEntity table ? table : null;
    }

    private static boolean emptyOutputs(BookBindingBlockEntity table) {
        for (int slot = 2; slot < 38; slot++) if (!table.getItem(slot).isEmpty()) return false;
        return true;
    }

    private static int count(ServerPlayer player, String name) {
        return player.getInventory().countItem(item(name, 1).getItem());
    }

    private static int dropCount(List<ItemEntity> drops, String name) {
        return drops.stream().filter(drop -> BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString().equals(name))
                .mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        if (table(player) != null) table(player).clearContent();
        player.level().setBlock(station, oldStation, 3);
        player.level().setBlock(floor, oldFloor, 3);
        player.level().setBlock(stand, oldStand, 3);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(sneaking);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.options.keyShift.setDown(shift));
    }

    private static ItemStack item(String name, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(name)), count);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name);
    }
}
