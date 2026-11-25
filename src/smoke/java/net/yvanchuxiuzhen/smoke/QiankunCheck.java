package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.Button;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.client.QiankunmentuScreen;
import net.yvanchuxiuzhen.system.npc.content.QiankundaorenBlockEntity;
import net.yvanchuxiuzhen.system.npc.content.QiankunmentuBlockEntity;
import net.yvanchuxiuzhen.system.npc.content.QiankunmentuMenu;

/** 检查来源门控、门徒编号及库存同步、道人自然烟雾周期与真实保存重进。 */
final class QiankunCheck extends SystemCheck {
    private static final Component MATERIAL_NAME = Component.literal("门徒保存检查材料");
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> oldBlocks = new HashMap<>();
    private final Map<BlockPos, CompoundTag> oldBlockEntities = new HashMap<>();
    private BlockPos floor;
    private BlockPos daoPos;
    private BlockPos disciplePos;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean shift;
    private boolean noGravity;

    QiankunCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("乾坤道人门徒", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                shift = minecraft.options.keyShift.isDown();
                minecraft.options.keyShift.setDown(false);
                expect("乾坤道人中文名称", "乾坤道人", I18n.get("block.yvanchuxiuzhen.qiankundaoren"));
                expect("乾坤门徒中文名称", "乾坤门徒", I18n.get("block.yvanchuxiuzhen.qiankunmentu"));
                expect("任务编号中文动态文本", "§6第3个任务", I18n.get("gui.yvanchuxiuzhen.qiankunmentu_gui.task_index", "3"));
                onServer(this::prepare);
            }
            case 1 -> {
                if (local < 12) return false;
                if (!entitiesReady()) { abort("乾坤方块实体未就绪"); return true; }
                useBlock(disciplePos);
            }
            case 2 -> {
                if (local < 10) return false;
                expectTrue("创意普通右键不打开门徒设置页", !(minecraft.player.containerMenu instanceof QiankunmentuMenu));
                minecraft.options.keyShift.setDown(true);
            }
            case 3 -> {
                if (local < 10) return false;
                useBlock(disciplePos);
            }
            case 4 -> {
                if (!(minecraft.screen instanceof QiankunmentuScreen)
                        || !(minecraft.player.containerMenu instanceof QiankunmentuMenu)) {
                    if (local > 200) abort("创意潜行右键门徒没有打开设置页");
                    return false;
                }
                minecraft.options.keyShift.setDown(false);
                expect("门徒设置页标题为中文", "乾坤门徒", minecraft.screen.getTitle().getString());
                expect("门徒设置页没有物品槽", 0, minecraft.player.containerMenu.slots.size());
                button().onPress();
            }
            case 5 -> {
                if (local < 10) return false;
                checkIndex("第一次按钮", 1);
                for (int index = 0; index < 9; index++) button().onPress();
            }
            case 6 -> {
                if (local < 10) return false;
                checkIndex("十次按钮回绕", 0);
                for (int index = 0; index < 3; index++) button().onPress();
            }
            case 7 -> {
                if (local < 10) return false;
                checkIndex("回绕后继续选择", 3);
                screenshots.take("qiankun_disciple_index.png");
                onServer(this::checkInventory);
                minecraft.player.closeContainer();
                onServer(player -> { player.setGameMode(GameType.SURVIVAL); player.setShiftKeyDown(false); });
            }
            case 8 -> {
                if (local < 10) return false;
                useBlock(disciplePos);
            }
            case 9 -> {
                if (local < 10) return false;
                expectTrue("生存普通右键不打开门徒设置页", !(minecraft.player.containerMenu instanceof QiankunmentuMenu));
                useBlock(daoPos);
            }
            case 10 -> {
                if (local < 10) return false;
                onServer(player -> {
                    QiankundaorenBlockEntity dao = dao(player);
                    expect("道人普通右键按UUID写准备状态", "准备", dao.playerStage(player.getUUID()));
                    expectTrue("道人自然定时刻推进烟雾计数", dao.isSmokeBurst()
                            && dao.smokeTick() > 0 && dao.smokeTick() < 54);
                });
                expectTrue("烟雾中间计数同步到客户端", minecraft.level.getBlockEntity(daoPos) instanceof QiankundaorenBlockEntity entity
                        && entity.smokeTick() > 0);
            }
            case 11 -> {
                if (local < 130) return false;
                checkSmokeFinished("自然烟雾周期结束");
                useBlock(daoPos);
            }
            case 12 -> {
                if (local < 12) return false;
                checkSmokeFinished("同一玩家重复右键不重启");
                screenshots.take("qiankun_blocks.png");
                leaveWorld();
            }
            case 13 -> { return reopened(local); }
            case 14 -> {
                if (!inWorld(local)) return false;
                if (!entitiesReady()) { abort("保存重进没有恢复乾坤方块实体"); return true; }
                checkIndex("真实保存重进", 3);
                checkSmokeFinished("真实保存重进");
                onServer(player -> {
                    expect("真实保存重进保留道人UUID状态", "准备", dao(player).playerStage(player.getUUID()));
                    ItemStack material = disciple(player).getItem(0);
                    expectTrue("真实保存重进保留一槽物品及实例组件", material.is(Items.DIAMOND)
                            && material.getCount() == 1 && MATERIAL_NAME.equals(material.get(DataComponents.CUSTOM_NAME)));
                    cleanup(player);
                });
                minecraft.options.keyShift.setDown(shift);
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
        noGravity = player.isNoGravity();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        floor = player.blockPosition().above(4);
        daoPos = floor.offset(0, 0, 2);
        disciplePos = floor.offset(2, 0, 2);
        for (BlockPos pos : List.of(floor, daoPos, disciplePos)) {
            oldBlocks.put(pos, player.level().getBlockState(pos));
            BlockEntity original = player.level().getBlockEntity(pos);
            if (original != null) oldBlockEntities.put(pos, original.saveWithFullMetadata(player.registryAccess()));
        }
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(daoPos, block("qiankundaoren").defaultBlockState(), 3);
        player.level().setBlock(disciplePos, block("qiankunmentu").defaultBlockState(), 3);
        player.setGameMode(GameType.CREATIVE);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();
        player.connection.teleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 25);
    }

    private boolean entitiesReady() {
        boolean[] ready = new boolean[1];
        onServer(player -> ready[0] = player.level().getBlockEntity(daoPos) instanceof QiankundaorenBlockEntity
                && player.level().getBlockEntity(disciplePos) instanceof QiankunmentuBlockEntity);
        return ready[0];
    }

    private void checkInventory(ServerPlayer player) {
        IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, disciplePos, null);
        expectTrue("门徒无方向能力暴露一槽", handler != null && handler.getSlots() == 1);
        if (handler == null || handler.getSlots() != 1) return;
        ItemStack material = new ItemStack(Items.DIAMOND, 2);
        material.set(DataComponents.CUSTOM_NAME, MATERIAL_NAME);
        ItemStack remainder = handler.insertItem(0, material, false);
        expectTrue("门徒槽上限一件并保留插入余量", remainder.getCount() == 1 && handler.getStackInSlot(0).getCount() == 1);
        for (Direction direction : Direction.values()) {
            IItemHandler sided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, disciplePos, direction);
            expectTrue("门徒六面共用一槽 " + direction, sided != null && sided.getSlots() == 1
                    && ItemStack.matches(sided.getStackInSlot(0), handler.getStackInSlot(0)));
            IItemHandler empty = player.level().getCapability(Capabilities.ItemHandler.BLOCK, daoPos, direction);
            expectTrue("道人六面保持零槽 " + direction, empty != null && empty.getSlots() == 0);
        }
        IItemHandler empty = player.level().getCapability(Capabilities.ItemHandler.BLOCK, daoPos, null);
        expectTrue("道人无方向保持零槽", empty != null && empty.getSlots() == 0);
    }

    private void checkIndex(String label, double expected) {
        onServer(player -> expect(label + "服务端任务编号", expected, disciple(player).renwuset()));
        expectTrue(label + "客户端任务编号同步", minecraft.level.getBlockEntity(disciplePos) instanceof QiankunmentuBlockEntity entity
                && entity.renwuset() == expected);
    }

    private void checkSmokeFinished(String label) {
        onServer(player -> expectTrue(label + "服务端关闭并清零", finished(dao(player))));
        expectTrue(label + "客户端关闭并清零", minecraft.level.getBlockEntity(daoPos) instanceof QiankundaorenBlockEntity entity
                && finished(entity));
    }

    private static boolean finished(QiankundaorenBlockEntity dao) {
        return !dao.isSmokeBurst() && dao.smokeTick() == 0;
    }

    private Button button() {
        return minecraft.screen.children().stream().filter(child -> child instanceof Button widget && widget.getMessage().getString().equals("+"))
                .map(child -> (Button) child).findFirst().orElseThrow();
    }

    private void useBlock(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
    }

    private QiankundaorenBlockEntity dao(ServerPlayer player) {
        return (QiankundaorenBlockEntity) player.level().getBlockEntity(daoPos);
    }

    private QiankunmentuBlockEntity disciple(ServerPlayer player) {
        return (QiankunmentuBlockEntity) player.level().getBlockEntity(disciplePos);
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        for (Map.Entry<BlockPos, BlockState> entry : oldBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            player.level().setBlock(pos, entry.getValue(), 3);
            if (oldBlockEntities.containsKey(pos) && player.level().getBlockEntity(pos) != null) {
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
    }

    private void abort(String reason) {
        fail(reason);
        onServer(this::cleanup);
        minecraft.options.keyShift.setDown(shift);
        finish();
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }
}
