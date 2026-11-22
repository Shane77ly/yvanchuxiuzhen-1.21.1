package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.tiandao.NbtEditorBlockEntity;
import net.yvanchuxiuzhen.collection.tiandao.client.NbtEditorScreen;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;

/** 原生右键、八种真实控件请求、双端类型和数量、库存保存读回与破坏回收的必要单项。 */
final class NbtEditorCheck extends SystemCheck {
    private static final String[] KEYS = {"flag", "label", "byte", "decimal", "float", "integer", "long", "short"};
    private static final String[] VALUES = {"true", "中文样本", "130.9", "-3.75", "1.25", "42.9", "9007199254740992", "65535.7"};
    private static final Tag[] EXPECTED = {ByteTag.ONE, StringTag.valueOf("中文样本"), ByteTag.valueOf((byte) -126),
            DoubleTag.valueOf(-3.75), FloatTag.valueOf(1.25F), IntTag.valueOf(42), LongTag.valueOf(9007199254740992L), ShortTag.valueOf((short) -1)};
    private BlockPos pos;
    private Vec3 position;
    private ItemStack hand;
    private ItemStack expected;
    private boolean prepared;

    NbtEditorCheck(List<String> report, String levelId) {
        super("天道NBT编辑", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step > 0 && local < 10) return false;
        if (step >= 2 && step < 18) {
            if (!(minecraft.screen instanceof NbtEditorScreen)) {
                if (local < 100) return false;
                fail("未打开NBT编辑器原生界面");
                restore();
                finish();
                return true;
            }
            int type = (step - 2) / 2;
            if (step % 2 == 0) {
                if (type == 0) {
                    expect("原生页面两输入框", 2, inputs().size());
                    expect("八种类型独立复选框", 8, checks().size());
                    expectTrue("全部类型初始未选中", checks().stream().noneMatch(Checkbox::selected));
                    expect("设置按钮汉化", "设置NBT", buttons().getFirst().getMessage().getString());
                }
                send(KEYS[type], VALUES[type], type);
            } else {
                expect(KEYS[type] + "本人同步真实NBT类型和值", EXPECTED[type], data(minecraft.player.containerMenu.getSlot(0).getItem()).get(KEYS[type]));
                onServer(player -> {
                    expect(KEYS[type] + "服务端真实NBT类型和值", EXPECTED[type], data(editor(player).getItem(0)).get(KEYS[type]));
                    expect("来源编辑强制数量为1", 1, editor(player).getItem(0).getCount());
                });
            }
            return true;
        }
        switch (step) {
            case 0 -> onServer(player -> {
                pos = player.blockPosition().above(4);
                if (!player.level().isEmptyBlock(pos)) {
                    fail("夹具上方位置非空，未覆盖原方块");
                    finish();
                    return;
                }
                position = player.position();
                hand = player.getMainHandItem().copy();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.level().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "tiandaoitemnbt")).defaultBlockState(), 3);
                editor(player).setItem(0, new ItemStack(Items.STONE, 7));
                player.teleportTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
                prepared = true;
            });
            case 1 -> minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            case 18 -> send("power_Max", "12.5", 3);
            case 19 -> {
                expect("装备根字段本人同步", 12.5, Equipment.properties(minecraft.player.containerMenu.getSlot(0).getItem()).number("power_Max"));
                onServer(player -> {
                    ItemStack stack = editor(player).getItem(0);
                    expect("装备所有者消费根属性", 12.5, Equipment.properties(stack).number("power_Max"));
                    ItemStack reloaded = ItemStack.parse(player.registryAccess(), stack.save(player.registryAccess())).orElseThrow();
                    expectTrue("编辑物品完整组件保存读回", ItemStack.matches(stack, reloaded));
                    CompoundTag saved = editor(player).saveWithFullMetadata(player.registryAccess());
                    NbtEditorBlockEntity restored = (NbtEditorBlockEntity) BlockEntity.loadStatic(pos, editor(player).getBlockState(), saved, player.registryAccess());
                    expectTrue("编辑器库存保存读回", ItemStack.matches(stack, restored.getItem(0)));
                    for (Direction direction : Direction.values()) expectTrue("方向" + direction + "禁止抽取", editor(player).handler(direction).extractItem(0, 1, true).isEmpty());
                    expectTrue("无方向菜单能力可以抽取", !editor(player).handler(null).extractItem(0, 1, true).isEmpty());
                    expected = stack.copy();
                });
                minecraft.player.closeContainer();
            }
            case 20 -> onServer(player -> {
                expectTrue("关闭后物品留在编辑器", ItemStack.matches(expected, editor(player).getItem(0)));
                List<ItemEntity> before = player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
                player.level().destroyBlock(pos, true, player);
                List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2), entity -> !before.contains(entity));
                expect("破坏归还槽内单件而不掉落工具方块", 1, drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum());
                expectTrue("回收物品完整组件保持", drops.size() == 1 && ItemStack.matches(expected, drops.getFirst().getItem()));
                drops.forEach(ItemEntity::discard);
            });
            case 21 -> {
                restore();
                finish();
            }
            default -> throw new IllegalStateException("未知NBT编辑检查步骤 " + step);
        }
        return true;
    }

    private void send(String name, String content, int selected) {
        inputs().get(0).setValue(name);
        inputs().get(1).setValue(content);
        List<Checkbox> choices = checks();
        for (int index = 0; index < choices.size(); index++) if (choices.get(index).selected() != (index == selected)) choices.get(index).onPress();
        buttons().getFirst().onPress();
    }

    private List<EditBox> inputs() { return minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList(); }
    private List<Checkbox> checks() { return minecraft.screen.children().stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).toList(); }
    private List<Button> buttons() { return minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList(); }
    private static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    private NbtEditorBlockEntity editor(ServerPlayer player) { return (NbtEditorBlockEntity) player.level().getBlockEntity(pos); }

    private void restore() {
        if (!prepared) return;
        onServer(player -> {
            player.closeContainer();
            if (player.level().getBlockEntity(pos) instanceof NbtEditorBlockEntity editor) {
                editor.clearContent();
                player.level().removeBlock(pos, false);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            player.teleportTo(position.x, position.y, position.z);
        });
    }
}
