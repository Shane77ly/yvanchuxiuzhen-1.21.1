package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 独立新存档中检查满包成玉自然拾取及未拾取实体的原生保存重进。 */
final class JadePickupPersistenceCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final Consumer<EntityJoinLevelEvent> listener = this::capture;
    private final List<UUID> captured = new ArrayList<>();
    private ListTag originalInventory;
    private GameType originalMode;
    private int originalSelected;
    private double originalChance;
    private double originalNumber;
    private boolean shiftKey;
    private boolean prepared;
    private boolean listening;
    private boolean capturing;
    private UUID pickedId;
    private UUID savedId;
    private ItemStack pickedStack;
    private ItemStack savedStack;

    JadePickupPersistenceCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("璞玉满包拾取与掉落重进", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            abort("步骤 " + step + " 异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> {
                shiftKey = minecraft.options.keyShift.isDown();
                minecraft.options.keyShift.setDown(false);
                onServer(this::prepare);
            }
            case 1 -> {
                boolean[] ready = {false};
                onServer(player -> {
                    if (!player.serverLevel().isPositionEntityTicking(new BlockPos(0, 181, 0))) return;
                    ready[0] = true;
                    fill(player, 161, false);
                    ItemEntity drop = carve(player, item("cailiao_161"));
                    pickedId = drop.getUUID();
                    pickedStack = drop.getItem().copy();
                    expect("第一件成玉等级", 40, Materials.instance(pickedStack).level());
                    expectTrue("满包雕琢实体真实入世界且拾取延迟40刻", player.serverLevel().getEntity(pickedId) == drop
                            && delay(drop) == 40 && player.getMainHandItem().getCount() == 1);
                    player.getInventory().setItem(1, ItemStack.EMPTY);
                    expectTrue("释放空位时拾取延迟仍在且成玉仍在世界", drop.hasPickUpDelay()
                            && player.serverLevel().getEntity(pickedId) == drop && player.getInventory().getItem(1).isEmpty());
                    player.inventoryMenu.broadcastChanges();
                });
                if (!ready[0]) {
                    if (local > 200) abort("雕琢平台区块未进入实体刻状态");
                    return false;
                }
            }
            case 2 -> {
                boolean[] complete = {false};
                onServer(player -> {
                    Entity entity = player.serverLevel().getEntity(pickedId);
                    ItemStack slot = player.getInventory().getItem(1);
                    if (entity == null && ItemStack.matches(pickedStack, slot)) {
                        expectTrue("自然碰撞后实体消失且完整成玉进入空位", true);
                        complete[0] = true;
                    } else if (entity == null) {
                        throw new IllegalStateException("成玉实体消失但指定空位没有完整成玉");
                    }
                });
                if (!complete[0]) {
                    if (local > 400) abort("等待原生自然拾取超时");
                    return false;
                }
            }
            case 3 -> {
                onServer(player -> {
                    fill(player, 162, true);
                    ItemEntity drop = carve(player, item("cailiao_162"));
                    savedId = drop.getUUID();
                    savedStack = drop.getItem().copy();
                    expect("第二件成玉等级", 40, Materials.instance(savedStack).level());
                    expectTrue("第二件满包掉落实体真实入世界且延迟40刻", player.serverLevel().getEntity(savedId) == drop
                            && delay(drop) == 40 && player.getMainHandItem().getCount() == 1);
                    player.teleportTo(player.serverLevel(), 8.5, 181, 0.5, 0, 0);
                    player.inventoryMenu.broadcastChanges();
                });
            }
            case 4 -> {
                if (local < 100) return false;
                onServer(player -> {
                    Entity entity = player.serverLevel().getEntity(savedId);
                    if (!(entity instanceof ItemEntity drop)) throw new IllegalStateException("保存前未找到第二件掉落实体");
                    expectTrue("未拾取成玉延迟结束后仍在世界", !drop.hasPickUpDelay()
                            && ItemStack.matches(savedStack, drop.getItem()) && player.distanceToSqr(drop) > 25);
                    expectTrue("已拾取成玉保留在背包", ItemStack.matches(pickedStack, player.getInventory().getItem(35)));
                    expect("未拾取实体UUID", savedId.toString(), drop.getUUID().toString());
                });
                screenshots.take("jade-pickup-before-exit.png");
                leaveWorld();
            }
            case 5 -> {
                if (minecraft.screen instanceof TitleScreen && local >= 20) return reopened(local);
                if (local > 900) abort("退出后未到标题界面");
                return false;
            }
            case 6 -> { return inWorld(local); }
            case 7 -> {
                onServer(player -> {
                    Entity entity = player.serverLevel().getEntity(savedId);
                    expectTrue("真实重进后原UUID的未拾取成玉存在", entity instanceof ItemEntity);
                    if (entity instanceof ItemEntity drop) {
                        expectTrue("重进掉落物数量和全部组件保持", ItemStack.matches(savedStack, drop.getItem()));
                        expect("重进掉落物材料实例", Materials.instance(savedStack), Materials.instance(drop.getItem()));
                        expectTrue("重进检查时玩家仍远离掉落物", player.distanceToSqr(drop) > 25);
                    }
                    expectTrue("真实重进后已拾取成玉仍在背包", ItemStack.matches(pickedStack, player.getInventory().getItem(35)));
                    restore(player, false);
                });
                screenshots.take("jade-pickup-after-reentry.png");
                minecraft.options.keyShift.setDown(shiftKey);
                finish();
            }
            default -> throw new IllegalStateException("未知检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        originalInventory = player.getInventory().save(new ListTag());
        originalSelected = player.getInventory().selected;
        originalMode = player.gameMode.getGameModeForPlayer();
        originalChance = Cultivation.tizhiDouble(player, "jiyvgailv");
        originalNumber = Cultivation.tizhiDouble(player, "qiyvbianhao");
        prepared = true;
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        for (int x = -2; x <= 10; x++) {
            for (int z = -2; z <= 2; z++) {
                player.serverLevel().setBlockAndUpdate(new BlockPos(x, 180, z), Blocks.STONE.defaultBlockState());
                for (int y = 181; y <= 184; y++) {
                    player.serverLevel().setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        player.teleportTo(player.serverLevel(), 0.5, 181, 0.5, 0, 0);
        NeoForge.EVENT_BUS.addListener(listener);
        listening = true;
    }

    private void fill(ServerPlayer player, int jade, boolean keepPicked) {
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        for (int slot = 1; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        if (keepPicked) player.getInventory().setItem(35, pickedStack.copy());
        ItemStack input = new ItemStack(item("cailiao_" + jade + "02"), 2);
        Materials.setFortuneIndex(input, 4);
        player.setItemInHand(InteractionHand.MAIN_HAND, input);
        Cultivation.setTizhiDouble(player, "jiyvgailv", 0);
        Cultivation.setTizhiDouble(player, "qiyvbianhao", 0);
        player.setShiftKeyDown(false);
    }

    private ItemEntity carve(ServerPlayer player, Item expected) {
        int before = captured.size();
        capturing = true;
        try {
            player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        } finally {
            capturing = false;
        }
        if (captured.size() != before + 1) throw new IllegalStateException("雕琢应生成一件实体，实际 " + (captured.size() - before));
        Entity entity = player.serverLevel().getEntity(captured.get(before));
        if (!(entity instanceof ItemEntity drop) || !drop.getItem().is(expected) || drop.getItem().getCount() != 1) {
            throw new IllegalStateException("雕琢掉落物未入世界或身份、数量不符");
        }
        return drop;
    }

    private void capture(EntityJoinLevelEvent event) {
        if (capturing && !event.getLevel().isClientSide()) captured.add(event.getEntity().getUUID());
    }

    private static int delay(ItemEntity drop) {
        return drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay");
    }

    private void restore(ServerPlayer player, boolean removeDrops) {
        if (listening) NeoForge.EVENT_BUS.unregister(listener);
        listening = false;
        if (removeDrops) {
            for (UUID id : captured) {
                Entity entity = player.serverLevel().getEntity(id);
                if (entity != null) entity.discard();
            }
        }
        player.getInventory().clearContent();
        player.getInventory().load(originalInventory);
        player.getInventory().selected = originalSelected;
        player.setGameMode(originalMode);
        player.setShiftKeyDown(false);
        Cultivation.setTizhiDouble(player, "jiyvgailv", originalChance);
        Cultivation.setTizhiDouble(player, "qiyvbianhao", originalNumber);
        player.inventoryMenu.broadcastChanges();
        prepared = false;
    }

    private void abort(String message) {
        fail(message);
        if (prepared && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(player -> restore(player, true));
        minecraft.options.keyShift.setDown(shiftKey);
        finish();
    }

    private static Item item(String id) {
        Item value = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
        if (value == Items.AIR) throw new IllegalStateException("未注册的测试物品：" + id);
        return value;
    }
}
