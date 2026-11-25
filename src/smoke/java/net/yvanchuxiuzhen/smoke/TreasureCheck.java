package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 物品与地面宝箱自然消费、奖励实例和开箱状态的必要生命周期检查。 */
final class TreasureCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> persisted = new ArrayList<>();
    private BlockPos platform;
    private BlockPos ground;
    private BlockPos ordinary;
    private BlockPos locked;
    private BlockPos saved;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private int money;

    TreasureCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("宝箱", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 18) return reopened(local);
        if (step == 19) return inWorld(local);
        if (local < (step == 16 ? 45 : step == 15 || step == 22 ? 110 : 8)) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> useItem();
            case 2 -> onServer(player -> {
                expect("01物品自然使用只消费一个", 1, player.getMainHandItem().getCount());
                ItemStack reward = takeSingleDrop(player);
                expectTrue("一级宝石表自然产出二级材料161", reward.is(item("cailiao_161").getItem())
                        && Materials.instance(reward).level() == 2);
                player.getInventory().setItem(9, reward);
                stage(player, chestItem("treasure_chest_02", "宝箱", "宝石", 1));
            });
            case 3 -> useItem();
            case 4 -> onServer(player -> {
                expect("02物品立即发放并消费一个", 1, player.getMainHandItem().getCount());
                expectTrue("02物品沿同一奖励表发放材料", takeSingleDrop(player).is(item("cailiao_161").getItem()));
                stageGround(player, "treasure_chest_04");
            });
            case 5 -> use(ground);
            case 6 -> onServer(player -> {
                ItemStack reward = takeSingleDrop(player);
                expectTrue("地面04自然消费并生成可读取功法组件", player.level().isEmptyBlock(ground) && Techniques.book(reward).isPresent());
                player.getInventory().setItem(10, reward);
                stageGround(player, "treasure_chest_05");
            });
            case 7 -> use(ground);
            case 8 -> onServer(player -> {
                ItemStack reward = takeSingleDrop(player);
                expectTrue("地面05自然消费并生成可读取技能组件", player.level().isEmptyBlock(ground) && Skills.book(reward).isPresent());
                player.getInventory().setItem(11, reward);
                stageGround(player, "treasure_chest_06");
            });
            case 9 -> use(ground);
            case 10 -> onServer(player -> {
                money = player.getInventory().countItem(item("money_01").getItem());
                expectTrue("地面06移除且实际给予一至二十通宝", player.level().isEmptyBlock(ground) && money >= 1 && money <= 20);
                player.getInventory().setItem(12, player.getInventory().removeItemNoUpdate(0));
                stage(player, ItemStack.EMPTY);
            });
            case 11 -> {
                use(locked);
                lookAt(locked);
            }
            case 12 -> {
                onServer(player -> {
                    expectTrue("无钥匙自然右键保持上锁未打开", !chest(player, locked).opened());
                    ItemStack keys = item("treasure_chest_1101").copyWithCount(2);
                    keys.set(DataComponents.CUSTOM_NAME, Component.literal("藏阁钥匙"));
                    stage(player, keys);
                });
                screenshots.take("treasure_locked.png");
            }
            case 13 -> use(locked);
            case 14 -> {
                onServer(player -> {
                    TreasureChestBlockEntity chest = chest(player, locked);
                    TreasureContainerConfig config = chest.getData(TreasureData.BLOCK_CONTAINER);
                    expect("钥匙自然开箱消耗一个", 1, player.getMainHandItem().getCount());
                    expectTrue("锁箱自然初始化类别等级及数量", chest.opened()
                            && !config.category().isEmpty() && !config.subtype().isEmpty()
                            && config.level() >= 1 && config.level() <= 8
                            && config.minimum() == 1 && config.maximum() >= 1);
                    CompoundTag serialized = chest.saveWithFullMetadata(player.registryAccess());
                    TreasureChestBlockEntity restored = new TreasureChestBlockEntity(locked, chest.getBlockState());
                    restored.loadWithComponents(serialized, player.registryAccess());
                    expect("开箱中打开标志真实序列化重读", chest.opened(), restored.opened());
                    expect("开箱中计时真实序列化重读", chest.tickCount(), restored.tickCount());
                    expect("开箱中状态真实序列化重读", chest.openState(), restored.openState());
                    expect("开箱中容器配置真实序列化重读", config, restored.getData(TreasureData.BLOCK_CONTAINER));
                    clearDrops(player);
                });
            }
            case 15 -> {
                onServer(player -> {
                    expectTrue("11首次自然开箱按第五次计划刻移除", player.level().isEmptyBlock(locked));
                    clearDrops(player);
                });
                use(ordinary);
                lookAt(ordinary);
            }
            case 16 -> {
                onServer(player -> {
                    TreasureChestBlockEntity chest = chest(player, ordinary);
                    expectTrue("03二十刻计划刻推进至发奖状态", chest != null && chest.tickCount() >= 2
                            && chest.openState().equals("爆物品"));
                    int count = drops(player).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
                    expectTrue("03自然开箱发放二至五件来源材料", count >= 2 && count <= 5);
                    clearDrops(player);
                    chest(player, saved).setItem(0, new ItemStack(Items.DIAMOND));
                    chest(player, saved).sync();
                    for (int slot : List.of(9, 10, 11)) persisted.add(player.getInventory().getItem(slot).copy());
                });
                screenshots.take("treasure_open.png");
            }
            case 17 -> leaveWorld();
            case 20 -> onServer(player -> {
                expectTrue("正常保存重进后已开两箱按第五次计划刻移除", player.level().isEmptyBlock(ordinary) && player.level().isEmptyBlock(locked));
                TreasureChestBlockEntity chest = chest(player, saved);
                expectTrue("正常重进保留未开锁箱和唯一真实库存", chest != null
                        && !chest.opened() && chest.getItem(0).is(Items.DIAMOND));
                for (int index = 0; index < persisted.size(); index++) {
                    expectTrue("奖励实例保存重进槽" + (9 + index), ItemStack.matches(persisted.get(index), player.getInventory().getItem(9 + index)));
                }
                expect("通宝保存重进", money, player.getInventory().countItem(item("money_01").getItem()));
                expect("重进保留剩余钥匙", 1, player.getMainHandItem().getCount());
                expect("重进保留钥匙自定义名称", "藏阁钥匙", player.getMainHandItem().getHoverName().getString());
                clearDrops(player);
            });
            case 21 -> use(saved);
            case 22 -> {
                onServer(player -> {
                    expectTrue("重进后锁箱仍可自然开箱并结束", player.level().isEmptyBlock(saved));
                    expect("第二次锁箱消耗最后一枚钥匙", 0,
                            player.getInventory().countItem(item("treasure_chest_1101").getItem()));
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知宝箱检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        platform = player.blockPosition().above(8);
        ground = platform.south(2).above();
        ordinary = platform.north(2).above();
        locked = platform.east(2).above();
        saved = platform.west(2).above();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                player.level().setBlock(platform.offset(dx, 0, dz), Blocks.STONE.defaultBlockState(), 3);
                for (int dy = 1; dy <= 3; dy++) player.level().setBlock(platform.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        minecraft.options.keyShift.setDown(false);
        player.getInventory().clearContent();
        player.connection.teleport(platform.getX() + .5, platform.getY() + 1, platform.getZ() + .5, 90, 15);
        player.level().setBlock(ordinary, block("treasure_chest_03"), 3);
        player.level().setBlock(locked, block("treasure_chest_11"), 3);
        player.level().setBlock(saved, block("treasure_chest_11"), 3);
        stage(player, chestItem("treasure_chest_01", "宝箱", "宝石", 1));
    }

    private void stageGround(ServerPlayer player, String name) {
        stage(player, ItemStack.EMPTY);
        player.level().setBlock(ground, block(name), 3);
    }

    private void stage(ServerPlayer player, ItemStack stack) {
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.inventoryMenu.broadcastChanges();
    }

    private void useItem() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void use(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private void lookAt(BlockPos pos) {
        Vec3 delta = Vec3.atCenterOf(pos).subtract(minecraft.player.getEyePosition());
        minecraft.player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90);
        minecraft.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(platform).inflate(6));
    }

    private ItemStack takeSingleDrop(ServerPlayer player) {
        List<ItemEntity> drops = drops(player);
        expect("单件奖励实际掉落实体数", 1, drops.size());
        ItemStack result = drops.size() == 1 ? drops.getFirst().getItem().copy() : ItemStack.EMPTY;
        drops.forEach(ItemEntity::discard);
        return result;
    }

    private void clearDrops(ServerPlayer player) {
        drops(player).forEach(ItemEntity::discard);
    }

    private static TreasureChestBlockEntity chest(ServerPlayer player, BlockPos pos) {
        return player.level().getBlockEntity(pos) instanceof TreasureChestBlockEntity chest ? chest : null;
    }

    private static ItemStack chestItem(String name, String category, String subtype, int level) {
        ItemStack stack = TreasureContainers.create(item(name).getItem(), category, subtype, level, 1, 1);
        stack.setCount(2);
        return stack;
    }

    private static ItemStack item(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(name)));
    }

    private static BlockState block(String name) {
        return BuiltInRegistries.BLOCK.get(id(name)).defaultBlockState();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name);
    }

    private void cleanup(ServerPlayer player) {
        clearDrops(player);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
    }
}
