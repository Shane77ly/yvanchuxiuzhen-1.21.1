package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 三档满槽地上罐与末槽装饰物的真实保存重进、原生取物和收罐再放置检查。 */
final class GuchongGroundSaveCheck extends SystemCheck {
    private static final int[] CAPACITIES = {1, 4, 9};
    private static final String[] NUMBERS = {"LEVELnumber", "experience_Max", "experience", "health_Max", "health_Num", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"};
    private static final String[] RATINGS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
            "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
    private final Screenshots screenshots;
    private final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private final List<BlockPos> homes = new ArrayList<>();
    private final List<List<ItemStack>> expectedInsects = new ArrayList<>();
    private final List<CompoundTag> expectedJarData = new ArrayList<>();
    private final List<TaoguanBlockEntity> originalJars = new ArrayList<>();
    private int executed = -1;
    private Vec3 originalPosition;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean shift;
    private boolean ctrl;
    private boolean alt;
    private boolean decorated = true;

    GuchongGroundSaveCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("地上蛊罐保存", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 8, this::prepare);
            case 1 -> synchronizedJars(local);
            case 2 -> { leaveWorld(); yield true; }
            case 3 -> reopened(local);
            case 4 -> inWorld(local);
            case 5 -> synchronizedJars(local);
            case 6 -> action(step, local, 8, this::verifyReload);
            case 7 -> {
                if (local < 8) yield false;
                screenshots.take("guchong-three-ground-jars-rejoined.png");
                yield true;
            }
            case 8 -> action(step, local, 8, this::pickUp);
            case 9 -> action(step, local, 8, this::replace);
            case 10 -> synchronizedJars(local);
            case 11 -> action(step, local, 8, this::verifyReplaced);
            case 12 -> { onServer(this::cleanup); finish(); yield true; }
            default -> throw new IllegalStateException("未知地上蛊罐保存步骤 " + step);
        };
    }

    private boolean action(int step, int local, int delay, Consumer<ServerPlayer> run) {
        if (executed != step) {
            executed = step;
            onServer(run);
        }
        return local >= delay;
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        shift = player.isShiftKeyDown();
        ctrl = ModifierKeys.ctrl(player);
        alt = ModifierKeys.alt(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        ModifierKeys.update(player, false, false);
        BlockPos floor = player.blockPosition().above(32);
        for (int x = -6; x <= 6; x++) {
            for (int z = -2; z <= 8; z++) setBlock(player, floor.offset(x, 0, z), Blocks.STONE.defaultBlockState());
        }
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 15);
        for (int grade = 1; grade <= 3; grade++) {
            BlockPos home = floor.offset((grade - 2) * 4, 1, 6);
            homes.add(home);
            require(player.level().isEmptyBlock(home), "场地的地上蛊罐位置不为空 " + home);
            originalBlocks.put(home, player.level().getBlockState(home));
            player.level().setBlock(home.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            List<ItemStack> insects = new ArrayList<>();
            CompoundTag packed = new CompoundTag();
            packed.putString("guguanuuid", uuid(grade));
            for (int slot = 0; slot < CAPACITIES[grade - 1]; slot++) {
                ItemStack insect = specimen(grade, slot);
                insects.add(insect.copy());
                GuchongJarData.pack(insect, packed, slot, false);
            }
            ItemStack jar = item("taoguan_0" + grade + "item");
            GuchongJarData.write(jar, packed);
            player.setItemInHand(InteractionHand.MAIN_HAND, jar);
            useOn(player, home.below());
            TaoguanBlockEntity placed = jar(player, grade);
            require(player.getMainHandItem().isEmpty(), "原生放罐没有清除主手 " + grade);
            require(placed.insectSlots() == CAPACITIES[grade - 1], "地上罐容量错误 " + grade);
            for (int slot = 0; slot < insects.size(); slot++) {
                require(ItemStack.matches(insects.get(slot), placed.getItem(slot)), "原生放罐漏字段或错槽 " + grade + "/" + slot);
            }
            ItemStack blood = item("jingxve");
            CompoundTag bloodData = new CompoundTag();
            bloodData.putInt("playerlevelnum", 10);
            GuchongJarData.write(blood, bloodData);
            player.setItemInHand(InteractionHand.MAIN_HAND, blood);
            useOn(player, home);
            expectTrue(grade + "档精血一次认满全部" + insects.size() + "槽且只扣一份", player.getMainHandItem().isEmpty());
            for (int slot = 0; slot < insects.size(); slot++) {
                ItemStack expected = insects.get(slot);
                CompoundTag data = GuchongJarData.read(expected);
                data.putBoolean("guchongxvnfu", true);
                data.putString("team", Relations.entityTeam(player));
                data.putString("belongToMasterUUID", player.getStringUUID());
                GuchongJarData.write(expected, data);
                require(ItemStack.matches(expected, placed.getItem(slot)), "精血认主错槽或改写无关字段 " + grade + "/" + slot);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.OAK_LOG, 2));
            useOn(player, home);
            expectTrue(grade + "档末槽装饰物独立于满容量蛊虫且扣一个", placed.getItem(insects.size()).is(Items.OAK_LOG)
                    && placed.getItem(insects.size()).getCount() == 1 && player.getMainHandItem().getCount() == 1);
            expectedInsects.add(insects);
            expectedJarData.add(placed.jarData().copy());
            originalJars.add(placed);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    private boolean synchronizedJars(int local) {
        if (local < 8) return false;
        boolean ready = true;
        for (int grade = 1; grade <= 3; grade++) {
            if (!(minecraft.level.getBlockEntity(homes.get(grade - 1)) instanceof TaoguanBlockEntity client)) {
                ready = false;
                continue;
            }
            List<ItemStack> expected = expectedInsects.get(grade - 1);
            ready &= client.insectSlots() == expected.size() && client.getContainerSize() == expected.size() + 1
                    && client.jarData().equals(expectedJarData.get(grade - 1));
            for (int slot = 0; slot < expected.size(); slot++) ready &= ItemStack.matches(expected.get(slot), client.getItem(slot));
            ready &= decorated ? client.getItem(expected.size()).is(Items.OAK_LOG) : client.getItem(expected.size()).isEmpty();
        }
        if (!ready && local > 100) throw new IllegalStateException("三档地上罐客户端库存或关联数据未同步");
        return ready;
    }

    private void verifyReload(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) {
            TaoguanBlockEntity restored = jar(player, grade);
            List<ItemStack> expected = expectedInsects.get(grade - 1);
            expectTrue(grade + "档真实重进创建新方块实体并保留罐UUID阵营及友方数据", restored != originalJars.get(grade - 1)
                    && restored.jarData().equals(expectedJarData.get(grade - 1)) && restored.jarData().getString("guguanuuid").equals(uuid(grade))
                    && !restored.jarData().getBoolean("shifangguchong"));
            for (int slot = 0; slot < expected.size(); slot++) {
                expectTrue(grade + "档真实重进第" + (slot + 1) + "槽保持完整个体组件且与客户端一致",
                        ItemStack.matches(expected.get(slot), restored.getItem(slot))
                                && ItemStack.matches(restored.getItem(slot), clientJar(grade).getItem(slot)));
            }
            expectTrue(grade + "档真实重进保留独立装饰槽且中文名称可用", restored.getItem(expected.size()).is(Items.OAK_LOG)
                    && restored.getItem(expected.size()).getCount() == 1 && !restored.getDisplayName().getString().contains("block.yvanchuxiuzhen"));
        }
    }

    private void pickUp(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) {
            TaoguanBlockEntity placed = jar(player, grade);
            int before = player.getInventory().countItem(Items.OAK_LOG);
            player.setShiftKeyDown(true);
            useOn(player, homes.get(grade - 1));
            player.setShiftKeyDown(false);
            expectTrue(grade + "档潜行取装饰物不动全部虫槽", placed.getItem(placed.insectSlots()).isEmpty()
                    && player.getInventory().countItem(Items.OAK_LOG) == before + 1);
            if (!player.getMainHandItem().isEmpty()) {
                player.getInventory().setItem(25 + grade, player.getMainHandItem());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            ModifierKeys.update(player, true, false);
            useOn(player, homes.get(grade - 1));
            ModifierKeys.update(player, false, false);
            require(player.level().isEmptyBlock(homes.get(grade - 1)), "Ctrl收罐未移除地上罐 " + grade);
            ItemStack picked = ItemStack.EMPTY;
            for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                if (candidate.is(item("taoguan_0" + grade + "item").getItem())) {
                    require(picked.isEmpty(), "收罐产生重复物品 " + grade);
                    picked = candidate.copy();
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
            require(!picked.isEmpty(), "Ctrl收罐没有交还对应物品 " + grade);
            CompoundTag packed = GuchongJarData.read(picked);
            expectTrue(grade + "档原生收罐仅交还一个且保持UUID", picked.getCount() == 1 && packed.getString("guguanuuid").equals(uuid(grade)));
            for (int slot = 0; slot < expectedInsects.get(grade - 1).size(); slot++) {
                CompoundTag expected = GuchongJarData.read(expectedInsects.get(grade - 1).get(slot));
                boolean exact = true;
                for (String key : expected.getAllKeys()) {
                    Tag value = List.of(NUMBERS).contains(key) ? IntTag.valueOf(expected.getInt(key)) : expected.get(key);
                    exact &= value.equals(packed.get(key + slot));
                }
                expectTrue(grade + "档收罐第" + (slot + 1) + "槽完整来源字段按整数读取且速度保留小数", exact);
            }
            player.getInventory().setItem(20 + grade, picked);
        }
        decorated = false;
        player.inventoryMenu.broadcastChanges();
    }

    private void replace(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().getItem(20 + grade));
            player.getInventory().setItem(20 + grade, ItemStack.EMPTY);
            useOn(player, homes.get(grade - 1).below());
            require(player.getMainHandItem().isEmpty(), "收回罐没有再次自然放置 " + grade);
            for (ItemStack expected : expectedInsects.get(grade - 1)) {
                CompoundTag data = GuchongJarData.read(expected);
                for (String key : NUMBERS) data.putDouble(key, data.getInt(key));
                GuchongJarData.write(expected, data);
            }
            expectedJarData.set(grade - 1, jar(player, grade).jarData().copy());
        }
        player.inventoryMenu.broadcastChanges();
    }

    private void verifyReplaced(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) {
            TaoguanBlockEntity placed = jar(player, grade);
            List<ItemStack> expected = expectedInsects.get(grade - 1);
            for (int slot = 0; slot < expected.size(); slot++) {
                expectTrue(grade + "档收罐再放置第" + (slot + 1) + "槽保持完整字段及来源取整", ItemStack.matches(expected.get(slot), placed.getItem(slot)));
            }
            expectTrue(grade + "档再次放置保持UUID且装饰物不重复", placed.jarData().getString("guguanuuid").equals(uuid(grade))
                    && placed.getItem(expected.size()).isEmpty());
        }
    }

    private static ItemStack specimen(int grade, int slot) {
        String id = List.of("guchong_01", "guchong_03", "guchong_04").get(slot % 3);
        String name = grade + "档第" + (slot + 1) + "只样本";
        ItemStack insect = item(id + "item");
        CompoundTag data = new CompoundTag();
        data.putString("guchongid", ModId.VALUE + ":" + id);
        data.putString("guchongname", "[" + name + "]");
        data.putString("LEVELname", "一阶初级");
        data.putString("belongToMasterUUID", "");
        data.putString("team", "r10_before_blood_" + grade + "_" + slot);
        for (int index = 0; index < NUMBERS.length; index++) data.putDouble(NUMBERS[index], grade * 100 + slot * 10 + index + .75);
        data.putDouble("LEVELnumber", 1 + slot % 3);
        data.putDouble("health_Max", 300.75 + slot);
        data.putDouble("health_Num", 31.75 + slot);
        data.putDouble("speed_number", .165 + slot * .01);
        data.putBoolean("guchongxvnfu", false);
        for (int index = 0; index < RATINGS.length; index++) data.putString(RATINGS[index] + "_LEVEL", List.of("平庸", "贫弱", "强劲").get(index % 3));
        GuchongJarData.write(insect, data);
        insect.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return insect;
    }

    private TaoguanBlockEntity jar(ServerPlayer player, int grade) {
        if (!(player.level().getBlockEntity(homes.get(grade - 1)) instanceof TaoguanBlockEntity jar)) {
            throw new IllegalStateException("缺少地上蛊罐 " + grade);
        }
        return jar;
    }

    private TaoguanBlockEntity clientJar(int grade) {
        return (TaoguanBlockEntity) minecraft.level.getBlockEntity(homes.get(grade - 1));
    }

    private static String uuid(int grade) { return "r10_ground_save_" + grade; }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    private static void useOn(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        originalBlocks.putIfAbsent(pos, player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 3);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private void cleanup(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) jar(player, grade).clearContent();
        originalBlocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) player.getInventory().setItem(slot, originalInventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(shift);
        ModifierKeys.update(player, ctrl, alt);
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
