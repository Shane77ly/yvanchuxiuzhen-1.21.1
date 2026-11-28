package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.treasure.TreasureSetup;
import net.yvanchuxiuzhen.system.treasure.TreasureToolSettings;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;
import net.yvanchuxiuzhen.system.treasure.client.TreasureToolScreen;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import net.yvanchuxiuzhen.system.treasure.content.TreasureToolMenu;
import net.yvanchuxiuzhen.system.treasure.content.TreasureToolProjectile;

/** 天道宝箱工具的真实挥动开页、按钮、松手弹体、当前主手命中及计划刻奖励消费。 */
final class TreasureToolCheck extends SystemCheck {
    private final Consumer<String> shot;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<BlockPos, CompoundTag> blockData = new LinkedHashMap<>();
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> joined = this::joined;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean shift;
    private boolean noGravity;
    private BlockPos platform;
    private BlockPos target;
    private TreasureToolProjectile natural;
    private TreasureToolProjectile flight;
    private ItemStack firedTool;
    private boolean replaceOnJoin;

    TreasureToolCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("天道宝箱设置", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < (step == 8 ? 4 : step == 10 ? 40 : step == 13 ? 24 : 8)) return false;
        if ((step == 2 || step == 6) && !(minecraft.screen instanceof TreasureToolScreen)) {
            if (local < 100) return false;
            fail("来源挥动入口未打开管理页");
            onServer(this::cleanup);
            finish();
            return true;
        }
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> minecraft.player.swing(InteractionHand.MAIN_HAND);
            case 2 -> {
                expectTrue("空配置工具实际挥动打开管理页", minecraft.screen instanceof TreasureToolScreen);
                expect("菜单中文", "天道宝箱设置", minecraft.screen.getTitle().getString());
                expect("客户端显示默认大类", "书架", TreasureToolSettings.config(minecraft.player.getMainHandItem()).category());
                expect("客户端显示默认小类", "书桶", TreasureToolSettings.config(minecraft.player.getMainHandItem()).subtype());
                onServer(player -> expect("保留来源客户端默认未生成服务端类别", "",
                        TreasureToolSettings.config(player.getMainHandItem()).category()));
                shot.accept("treasure_tool_menu.png");
            }
            case 3 -> {
                List<Button> buttons = minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
                expect("真实菜单箭头数", 10, buttons.size());
                if (buttons.size() == 10) buttons.get(1).onPress();
            }
            case 4 -> {
                onServer(player -> {
                    expect("类别按钮未给来源空服务端类别补默认", "",
                            TreasureToolSettings.config(player.getMainHandItem()).category());
                    numeric(player);
                });
                minecraft.player.closeContainer();
            }
            case 5 -> onServer(player -> {
                stage(player, configured(item("tiandaobaoxiangitem"), "书架", "书籍堆", 1, 1, 3));
                player.setShiftKeyDown(true);
                player.swing(InteractionHand.MAIN_HAND);
                player.setShiftKeyDown(false);
            });
            case 6 -> {
                expectTrue("已配置工具潜行挥动仍打开管理页", minecraft.screen instanceof TreasureToolScreen);
                minecraft.player.closeContainer();
                onServer(this::stageTarget);
            }
            case 7 -> {
                minecraft.player.setYRot(0);
                minecraft.player.setXRot(0);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            }
            case 8 -> minecraft.gameMode.releaseUsingItem(minecraft.player);
            case 9 -> {
                onServer(player -> {
                    expectTrue("真实松手生成并命中设置弹体", natural != null && natural.isRemoved());
                    TreasureChestBlockEntity chest = (TreasureChestBlockEntity) player.level().getBlockEntity(target);
                    TreasureContainerConfig data = chest.getData(TreasureData.BLOCK_CONTAINER);
                    expect("命中读取发射后切换的主手大类", "宝箱", data.category());
                    expect("命中读取发射后切换的主手小类", "宝石", data.subtype());
                    expect("命中写等级", 1, data.level());
                    expect("命中写最小次数", 1, data.minimum());
                    expect("命中写最大次数", 1, data.maximum());
                    TreasureChestBlockEntity restored = (TreasureChestBlockEntity) BlockEntity.loadStatic(target, chest.getBlockState(),
                            chest.saveWithFullMetadata(player.registryAccess()), player.registryAccess());
                    expect("命中参数随方块序列化", data, restored.getData(TreasureData.BLOCK_CONTAINER));
                    expect("开箱状态随方块序列化", chest.opened(), restored.opened());
                    expectTrue("设置工具不扣件数", firedTool.is(item("tiandaobaoxiangitem").getItem()) && firedTool.getCount() == 1
                            && TreasureToolSettings.config(firedTool).category().equals("书架"));
                });
                shot.accept("treasure_tool_hit.png");
            }
            case 10 -> onServer(player -> {
                List<ItemEntity> rewards = drops(player);
                expect("自然计划刻只掉落一件被弹体指定奖励", 1, rewards.size());
                expectTrue("弹体配置被现有11奖励消费者读取为二级宝石材料", rewards.size() == 1
                        && rewards.getFirst().getItem().is(item("cailiao_161").getItem())
                        && Materials.instance(rewards.getFirst().getItem()).level() == 2);
                rewards.forEach(ItemEntity::discard);
                stage(player, configured(item("tiandaobaoxiangitem"), "宝箱", "宝石", 1, 1, 1));
                player.setXRot(-90);
                player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
                player.releaseUsingItem();
            });
            case 11 -> onServer(player -> {
                expectTrue("第二枚弹体第一刻后关闭重力", flight != null && flight.isNoGravity());
                if (flight != null) {
                    CompoundTag saved = flight.saveWithoutId(new CompoundTag());
                    TreasureToolProjectile restored = TreasureSetup.projectile().get().create(player.level());
                    restored.load(saved);
                    expect("弹体寿命计数随实体序列化", flight.lifeTicks(), restored.lifeTicks());
                    expectTrue("弹体显示来源材料100且禁止拾取", flight.getItem().is(item("cailiao_100").getItem())
                            && flight.pickup == Pickup.DISALLOWED);
                }
            });
            case 12 -> {
            }
            case 13 -> {
                onServer(player -> {
                    expectTrue("无碰撞设置弹体在第二十个真实刻移除", flight != null && flight.isRemoved()
                            && flight.lifeTicks() == 20);
                    cleanup(player);
                });
                expect("设置失败反馈中文", "未成功设置", Component.translatable("message.yvanchuxiuzhen.treasure_tool.failed").getString());
                finish();
            }
            default -> throw new IllegalStateException("未知天道宝箱检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        shift = player.isShiftKeyDown();
        noGravity = player.isNoGravity();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        platform = player.blockPosition().above(12);
        target = platform.south(6).above(2);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 8; z++) {
                place(player, platform.offset(x, 0, z), Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 5; y++) place(player, platform.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.connection.teleport(platform.getX() + .5, platform.getY() + 1, platform.getZ() + .5, 0, 0);
        stage(player, item("tiandaobaoxiangitem"));
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        player.releaseUsingItem();
        expectTrue("来源空服务端类别禁止发射", player.level().getEntitiesOfClass(TreasureToolProjectile.class, area()).isEmpty());
        NeoForge.EVENT_BUS.addListener(joined);
    }

    private void numeric(ServerPlayer player) {
        TreasureToolMenu menu = (TreasureToolMenu) player.containerMenu;
        ItemStack configured = configured(new ItemStack(Items.STONE), "书架", "书籍堆", 8, 64, 64);
        stage(player, configured);
        menu.clickMenuButton(player, 1);
        expect("类别上调同时重置对应小类", "草药堆", TreasureToolSettings.config(configured).subtype());
        menu.clickMenuButton(player, 0);
        menu.clickMenuButton(player, 3);
        expect("小类上调", "书桶", TreasureToolSettings.config(configured).subtype());
        menu.clickMenuButton(player, 2);
        expect("小类下调", "书籍堆", TreasureToolSettings.config(configured).subtype());
        menu.clickMenuButton(player, 5);
        expect("等级八上调回一", 1, TreasureToolSettings.config(configured).level());
        menu.clickMenuButton(player, 4);
        expect("等级一下调回八", 8, TreasureToolSettings.config(configured).level());
        menu.clickMenuButton(player, 7);
        expect("最小六十四上调回一", 1, TreasureToolSettings.config(configured).minimum());
        menu.clickMenuButton(player, 6);
        expect("来源最小值一下调保留服务端零", 0, TreasureToolSettings.config(configured).minimum());
        menu.clickMenuButton(player, 6);
        expect("最小零下调回六十四", 64, TreasureToolSettings.config(configured).minimum());
        menu.clickMenuButton(player, 9);
        expect("最大值上调回一后仍被最小六十四抬高", 64, TreasureToolSettings.config(configured).maximum());
        stage(player, configured(new ItemStack(Items.EMERALD), "纳戒", "炼器师纳戒", 1, 1, 1));
        menu.clickMenuButton(player, 8);
        expect("最大值一下调回六十四", 64, TreasureToolSettings.config(player.getMainHandItem()).maximum());
        menu.clickMenuButton(player, 2);
        expect("来源菜单操作切换后的普通主手物品", "机关术师纳戒", TreasureToolSettings.config(player.getMainHandItem()).subtype());
    }

    private void stageTarget(ServerPlayer player) {
        place(player, target, BuiltInRegistries.BLOCK.get(id("treasure_chest_11")).defaultBlockState());
        stage(player, item("treasure_chest_1101"));
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false));
        expectTrue("受控原生钥匙入口开启11并消费最后一把钥匙", player.getMainHandItem().isEmpty()
                && ((TreasureChestBlockEntity) player.level().getBlockEntity(target)).opened());
        firedTool = configured(item("tiandaobaoxiangitem"), "书架", "书桶", 7, 4, 6);
        stage(player, firedTool);
        replaceOnJoin = true;
    }

    private void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof TreasureToolProjectile projectile)) return;
        if (natural == null) {
            natural = projectile;
            if (replaceOnJoin && projectile.getOwner() instanceof ServerPlayer player) {
                stage(player, configured(new ItemStack(Items.STONE), "宝箱", "宝石", 1, 1, 1));
                replaceOnJoin = false;
            }
        } else {
            flight = projectile;
        }
    }

    private void place(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!blocks.containsKey(pos)) {
            blocks.put(pos.immutable(), player.level().getBlockState(pos));
            BlockEntity original = player.level().getBlockEntity(pos);
            if (original != null) blockData.put(pos.immutable(), original.saveWithFullMetadata(player.registryAccess()));
        }
        player.level().setBlock(pos, state, 3);
    }

    private static void stage(ServerPlayer player, ItemStack stack) {
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.inventoryMenu.broadcastChanges();
    }

    private AABB area() {
        return new AABB(platform).inflate(12);
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, area());
    }

    private static ItemStack configured(ItemStack stack, String category, String subtype, int level, int minimum, int maximum) {
        return TreasureContainers.create(stack.getItem(), category, subtype, level, minimum, maximum);
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(joined);
        if (natural != null) natural.discard();
        if (flight != null) flight.discard();
        drops(player).forEach(ItemEntity::discard);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockEntity current = player.level().getBlockEntity(entry.getKey());
            if (current instanceof TreasureChestBlockEntity chest) chest.clearContent();
            player.level().setBlock(entry.getKey(), entry.getValue(), 3);
            if (blockData.containsKey(entry.getKey())) {
                BlockEntity restored = player.level().getBlockEntity(entry.getKey());
                restored.loadWithComponents(blockData.get(entry.getKey()), player.registryAccess());
                restored.setChanged();
            }
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(shift);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
