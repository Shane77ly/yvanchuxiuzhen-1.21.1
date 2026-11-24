package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.collection.decor.DecorData;
import net.yvanchuxiuzhen.collection.decor.PlaqueInscription;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorBlockEntity;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorMenu;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorPayload;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorVariant;
import net.yvanchuxiuzhen.collection.decor.client.InscribedDecorScreen;
import net.yvanchuxiuzhen.platform.ModId;

/** 九木种文字装饰的原生菜单、真实消息、染色、回收重放和磁盘保存重进检查。 */
final class InscribedDecorCheck extends SystemCheck {
    private static final List<InscribedDecorVariant> VARIANTS = InscribedDecorVariant.VALUES;
    private static final String LONG_TEXT = "天地玄黄宇宙洪荒日月";
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, SavedBlock> blocks = new LinkedHashMap<>();
    private final List<CompoundTag> savedText = new ArrayList<>();
    private final Set<UUID> originalDrops = new HashSet<>();
    private BlockPos origin;
    private Vec3 position;
    private Vec3 velocity;
    private float yaw;
    private float pitch;
    private int selected;
    private int closedContainer;
    private GameType mode;
    private boolean sneaking;
    private boolean noGravity;
    private boolean shift;

    InscribedDecorCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("牌匾牌位", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        if (step >= 1 && step <= 54) return editVariant((step - 1) / 3, (step - 1) % 3, local);
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                minecraft.options.keyShift.setDown(false);
            }
            case 55 -> onServer(player -> open(player, 0));
            case 56 -> {
                if (!menu(0, local)) return false;
                if (local < 10) return false;
                expect("重开牌匾回填已保存文字", text(0), input().getValue());
                input().setValue(LONG_TEXT + "盈");
                click(0);
                PacketDistributor.sendToServer(new InscribedDecorPayload(minecraft.player.containerMenu.containerId + 1, 0, "plaquetxt:错误容器,"));
            }
            case 57 -> {
                if (local < 20) return false;
                onServer(player -> expect("超十字及错误容器消息均不覆盖文字", text(0), data(player, 0).getString("plaquetxt")));
                click(1);
                click(2);
            }
            case 58 -> {
                if (local < 20) return false;
                onServer(player -> {
                    expect("实际倾角按钮切到20度", 20, data(player, 0).getDouble("qingjiao"));
                    expectTrue("实际半格按钮同步标记", data(player, 0).getBoolean("plaqueaddfive"));
                });
                click(1);
            }
            case 59 -> {
                if (local < 20) return false;
                onServer(player -> expect("实际倾角按钮切到45度", 45, data(player, 0).getDouble("qingjiao")));
                click(1);
                input().setValue(LONG_TEXT);
                click(0);
            }
            case 60 -> {
                if (local < 20) return false;
                onServer(player -> {
                    expect("倾角三次循环回零", 0, data(player, 0).getDouble("qingjiao"));
                    expect("十字确认产生八段牌匾", 8, data(player, 0).getDouble("blocknum"));
                    expect("十字确认保留完整文本", LONG_TEXT, data(player, 0).getString("plaquetxt"));
                    expect("北向十字半格文字位置", 4.48, data(player, 0).getDouble("wenziX"));
                });
                screenshots.take("inscribed_decor_plaque_controls.png");
                closedContainer = minecraft.player.containerMenu.containerId;
                minecraft.player.closeContainer();
            }
            case 61 -> {
                if (local < 10) return false;
                PacketDistributor.sendToServer(new InscribedDecorPayload(closedContainer, 0, "plaquetxt:关闭后写入,"));
            }
            case 62 -> {
                if (local < 20) return false;
                onServer(player -> {
                    expect("关闭菜单后的旧消息不能写入", LONG_TEXT, data(player, 0).getString("plaquetxt"));
                    dyeAndReplay(player);
                    for (int index = 0; index < VARIANTS.size(); index++) savedText.add(data(player, index).copy());
                    player.connection.teleport(origin.getX() + 9, origin.getY() + 4, origin.getZ() - 7, 0, 24);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 63 -> {
                if (local < 30) return false;
                clientData("保存前");
                screenshots.take("inscribed_decor_before_save.png");
                leaveWorld();
            }
            case 64 -> { return reopened(local); }
            case 65 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    for (int index = 0; index < VARIANTS.size(); index++) {
                        expect("真实重进完整文字字段 " + VARIANTS.get(index).id(), savedText.get(index), data(player, index));
                    }
                });
            }
            case 66 -> {
                if (local < 20) return false;
                clientData("重进后");
                screenshots.take("inscribed_decor_reloaded.png");
                onServer(player -> {
                    breakAndReplace(player);
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知牌匾牌位检查步骤 " + step);
        }
        return true;
    }

    private boolean editVariant(int index, int phase, int local) {
        if (phase == 0) onServer(player -> open(player, index));
        else if (phase == 1) {
            if (!menu(index, local)) return false;
            if (local < 10) return false;
            String kind = VARIANTS.get(index).memorial() ? "paiwei_gui" : "plaque_gui";
            expect("原生菜单中文标题 " + VARIANTS.get(index).id(), I18n.get("menu.yvanchuxiuzhen.decor." + kind), minecraft.screen.getTitle().getString());
            expectTrue("输入框中文提示存在 " + kind, I18n.exists("gui.yvanchuxiuzhen." + kind + ".plaquetxt"));
            input().setValue(text(index));
            if (index < 2) screenshots.take("inscribed_decor_" + kind + ".png");
            click(0);
        } else {
            if (local < 20) return false;
            onServer(player -> {
                expect("屏幕提交经消息保存 " + VARIANTS.get(index).id(), text(index), data(player, index).getString("plaquetxt"));
                if (VARIANTS.get(index).memorial()) {
                    expectTrue("牌位确认关闭服务端菜单 " + index, player.containerMenu == player.inventoryMenu);
                    expect("牌位逐字数 " + index, text(index).length(), data(player, index).getInt("zishu"));
                    expect("牌位首字竖排位置 " + index, index == 17 ? 0 : .24, data(player, index).getDouble("ziweizhi0"));
                }
                player.closeContainer();
            });
            expect("文字方块客户端更新 " + index, text(index), clientData(index).getString("plaquetxt"));
        }
        return true;
    }

    private boolean menu(int index, int local) {
        if (minecraft.screen instanceof InscribedDecorScreen && minecraft.player.containerMenu instanceof InscribedDecorMenu menu
                && menu.kind().memorial() == VARIANTS.get(index).memorial()) return true;
        if (local > 100) {
            fail("斧子原生交互未打开对应客户端菜单 " + VARIANTS.get(index).id());
            onServer(this::cleanup);
            finish();
        }
        return false;
    }
    private EditBox input() {
        return minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
    }
    private void click(int index) {
        Button button = minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList().get(index);
        expectTrue("实际菜单鼠标点击 " + button.getMessage().getString(), minecraft.screen.mouseClicked(button.getX() + 2, button.getY() + 2, 0));
        minecraft.screen.mouseReleased(button.getX() + 2, button.getY() + 2, 0);
    }
    private static String text(int index) { return index == 17 ? "先人长眠之位" : "原初修真"; }

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
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(35)).forEach(drop -> originalDrops.add(drop.getUUID()));
        for (int index = 0; index < VARIANTS.size(); index++) {
            BlockPos pos = pos(index);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above(), pos.above(2))) {
                blocks.put(fixture, SavedBlock.capture(player, fixture));
                player.level().removeBlockEntity(fixture);
                player.level().setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.level().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            place(player, index, new ItemStack(item(VARIANTS.get(index).itemId())), index * 90);
            expectTrue("真实放置正确方块实体 " + VARIANTS.get(index).id(), player.level().getBlockEntity(pos) instanceof InscribedDecorBlockEntity block
                    && block.variant().equals(VARIANTS.get(index)));
            expectTrue("生存放置扣除一件 " + index, player.getMainHandItem().isEmpty());
        }
    }
    private void open(ServerPlayer player, int index) {
        BlockPos pos = pos(index);
        player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 15);
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        use(player, pos);
        expectTrue("来源斧子标签打开正确服务端菜单 " + index, player.containerMenu instanceof InscribedDecorMenu menu
                && menu.kind().memorial() == VARIANTS.get(index).memorial());
    }
    private void dyeAndReplay(ServerPlayer player) {
        for (int index = 0; index < VARIANTS.size(); index++) {
            InscribedDecorVariant variant = VARIANTS.get(index);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LIGHT_BLUE_DYE, 2));
            use(player, pos(index));
            expect("浅蓝染料消费一件 " + variant.id(), 1, player.getMainHandItem().getCount());
            expect("Q211浅蓝映射黄色 " + variant.id(), 0xFFFFFF00, data(player, index).getInt("rgbcolour"));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLOW_INK_SAC, 2));
            use(player, pos(index));
            expectTrue("发光消费并同步标记 " + variant.id(), player.getMainHandItem().getCount() == 1 && data(player, index).getBoolean("faguang"));
            if (!variant.memorial()) continue;
            player.getInventory().clearContent();
            player.setShiftKeyDown(true);
            use(player, pos(index));
            player.setShiftKeyDown(false);
            ItemStack recovered = player.getInventory().items.stream().filter(stack -> stack.is(item(variant.itemId()))).findFirst().orElse(ItemStack.EMPTY).copy();
            expectTrue("空手潜行收回同木种单件并移除方块 " + variant.id(), player.level().isEmptyBlock(pos(index)) && recovered.getCount() == 1);
            PlaqueInscription inscription = recovered.getOrDefault(DecorData.PLAQUE_INSCRIPTION.get(), PlaqueInscription.EMPTY);
            expect("收回物品保留自定义名称 " + variant.id(), text(index), recovered.getHoverName().getString());
            expect("收回物品保留文字 " + variant.id(), text(index), inscription.text());
            expectTrue("Q212收回不复制颜色与发光 " + variant.id(), recovered.get(DataComponents.CUSTOM_DATA) == null);
            player.getInventory().clearContent();
            place(player, index, recovered, 90);
            expect("回收后再放仍有文字 " + variant.id(), text(index), data(player, index).getString("plaquetxt"));
            expect("重新放置按新朝向写文字朝向 " + variant.id(), "东", data(player, index).getString("fangxiang"));
            expectTrue("回收重放颜色及发光恢复默认 " + variant.id(), !data(player, index).contains("rgbcolour") && !data(player, index).getBoolean("faguang"));
        }
    }
    private void breakAndReplace(ServerPlayer player) {
        for (int index = 0; index < VARIANTS.size(); index++) {
            InscribedDecorVariant variant = VARIANTS.get(index);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            expectTrue("玩家破坏成功 " + variant.id(), player.gameMode.destroyBlock(pos(index)));
            List<ItemEntity> drops = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(pos(index)).inflate(1.5)).stream()
                    .filter(drop -> !originalDrops.contains(drop.getUUID()) && drop.getItem().is(item(variant.itemId()))).toList();
            expect("普通破坏仅掉一件对应物品 " + variant.id(), 1, drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum());
            if (drops.isEmpty()) continue;
            ItemStack dropped = drops.getFirst().getItem().copy();
            drops.forEach(ItemEntity::discard);
            expectTrue("普通破坏不带编辑文字 " + variant.id(), !dropped.has(DecorData.PLAQUE_INSCRIPTION.get()));
            place(player, index, dropped, 180);
            expectTrue("掉落物可再放且新实例文字为空 " + variant.id(), data(player, index).getString("plaquetxt").isEmpty());
        }
    }
    private void clientData(String phase) {
        for (int index = 0; index < VARIANTS.size(); index++) expect(phase + "客户端完整文字字段 " + VARIANTS.get(index).id(), savedText.get(index), clientData(index));
    }
    private CompoundTag clientData(int index) { return minecraft.level.getBlockEntity(pos(index)).getPersistentData(); }
    private CompoundTag data(ServerPlayer player, int index) { return player.level().getBlockEntity(pos(index)).getPersistentData(); }
    private BlockPos pos(int index) { return origin.offset((index % 6) * 4, 0, (index / 6) * 4); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)); }
    private void place(ServerPlayer player, int index, ItemStack stack, float rotation) {
        BlockPos pos = pos(index);
        player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, rotation, 15);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.gameMode.useItemOn(player, player.level(), stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
        expectTrue("自然放置方向背离玩家 " + index, player.level().getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING) == player.getDirection().getOpposite());
    }
    private static void use(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }
    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(35)).stream()
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
