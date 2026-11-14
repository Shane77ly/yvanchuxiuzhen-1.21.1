package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.equipment.content.FlyingInstrumentEntity;

/** 建筑锤的真实配方装配、物品使用与左键事件，以及移动覆盖和飞行法器修理边界。 */
final class BuildingHammerCheck extends SystemCheck {
    private static final String[] COLORS = {"black", "green", "grey", "blue", "gilding", "red"};
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> terrain = new LinkedHashMap<>();
    private BlockPos base;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private ItemStack hammer;
    private ItemStack untouched;

    BuildingHammerCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("建筑锤", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 8) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> onServer(this::cycles);
            case 2 -> onServer(this::blocked);
            case 3 -> onServer(this::ordinaryBlocks);
            case 4 -> onServer(this::repair);
            case 5 -> {
                screenshots.take("building_hammer_cycles.png");
                onServer(player -> {
                    expectTrue("方块升降与修理保持锤的数量耐久及组件", ItemStack.matches(untouched, hammer));
                    restore(player);
                });
                finish();
            }
            default -> { }
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        selected = player.getInventory().selected;
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        base = new BlockPos(player.getBlockX() + 8, 240, player.getBlockZ() + 8);
        for (int x = -1; x <= 1; x++) for (int z = -4; z <= -2; z++) {
            place(player, base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        }
        player.connection.teleport(base.getX() + .5, base.getY(), base.getZ() - 2.5, 0, 15);
        ShapedRecipe recipe = (ShapedRecipe) player.server.getRecipeManager().byKey(id("hammerhecheng")).orElseThrow().value();
        CraftingInput input = CraftingInput.of(3, 3, List.of(item("cailiao_211"), item("cailiao_211"), item("cailiao_211"),
                item("cailiao_211"), new ItemStack(Items.STICK), item("cailiao_211"),
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY));
        expectTrue("五份材料与两木棍匹配来源配方", recipe.matches(input, player.level()));
        hammer = recipe.assemble(input, player.registryAccess());
        expectTrue("配方产物为单件建筑锤", hammer.is(BuiltInRegistries.ITEM.get(id("hammer"))) && hammer.getCount() == 1);
        hammer.setDamageValue(17);
        hammer.set(DataComponents.CUSTOM_NAME, Component.literal("升降样本"));
        untouched = hammer.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        player.inventoryMenu.broadcastChanges();
    }

    private void cycles(ServerPlayer player) {
        for (int color = 0; color < COLORS.length; color++) {
            for (int group = 0; group < 2; group++) {
                BlockPos start = base.offset(color * 3, 1, group * 4);
                clearColumn(player, start);
                int first = group == 0 ? 1 : 5;
                Direction facing = Direction.Plane.HORIZONTAL.stream().toList().get(color % 4);
                place(player, start, tile(COLORS[color], first, facing));
                for (int form = first + 1; form <= first + 3; form++) {
                    use(player, start);
                    expect("瓦片上升 " + COLORS[color] + form, tile(COLORS[color], form, facing), player.level().getBlockState(start));
                }
                use(player, start);
                expectTrue("瓦片上移清原位 " + COLORS[color] + first, player.level().isEmptyBlock(start));
                expect("瓦片上移回首段 " + COLORS[color] + first, tile(COLORS[color], first, facing), player.level().getBlockState(start.above()));
                left(player, start.above());
                expectTrue("瓦片下移清上层 " + COLORS[color] + first, player.level().isEmptyBlock(start.above()));
                expect("瓦片下移回末段 " + COLORS[color] + first, tile(COLORS[color], first + 3, facing), player.level().getBlockState(start));
                for (int form = first + 2; form >= first; form--) {
                    left(player, start);
                    expect("瓦片下降 " + COLORS[color] + form, tile(COLORS[color], form, facing), player.level().getBlockState(start));
                }
            }
        }
    }

    private void blocked(ServerPlayer player) {
        BlockPos pos = base.offset(0, 1, 9);
        clearColumn(player, pos);
        place(player, pos.above(), Blocks.STONE.defaultBlockState());
        place(player, pos, tile("black", 4, Direction.WEST));
        use(player, pos);
        expect("上方非空时瓦片保持末段", tile("black", 4, Direction.WEST), player.level().getBlockState(pos));
        place(player, pos.below(), Blocks.STONE.defaultBlockState());
        place(player, pos, tile("red", 5, Direction.EAST));
        left(player, pos);
        expect("下方非空时瓦片保持首段", tile("red", 5, Direction.EAST), player.level().getBlockState(pos));
        place(player, pos, Blocks.STONE.defaultBlockState());
        use(player, pos);
        expectTrue("普通方块上移同样要求空位", player.level().getBlockState(pos).is(Blocks.STONE));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        left(player, pos);
        expectTrue("非锤主手不触发下移", player.level().getBlockState(pos).is(Blocks.STONE));
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
    }

    private void ordinaryBlocks(ServerPlayer player) {
        BlockPos pos = base.offset(4, 1, 9);
        clearColumn(player, pos);
        BlockState wet = Blocks.OAK_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(BlockStateProperties.WATERLOGGED, true);
        place(player, pos, wet);
        use(player, pos);
        expect("普通方块上移保留含水和朝向", wet, player.level().getBlockState(pos.above()));
        left(player, pos.above());
        expect("普通方块下移保留完整状态", wet, player.level().getBlockState(pos));
        clearColumn(player, pos);
        place(player, pos, Blocks.STONE.defaultBlockState());
        place(player, pos.below(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        left(player, pos);
        expectTrue("来源普通方块下降覆盖下方方块并清原位", player.level().getBlockState(pos.below()).is(Blocks.STONE) && player.level().isEmptyBlock(pos));
        clearColumn(player, pos);
        place(player, pos, Blocks.OBSIDIAN.defaultBlockState());
        use(player, pos);
        left(player, pos);
        expectTrue("硬度超过10不移动", player.level().getBlockState(pos).is(Blocks.OBSIDIAN));
        place(player, pos, Blocks.BEDROCK.defaultBlockState());
        use(player, pos);
        expectTrue("来源负硬度仍通过不高于10条件", player.level().getBlockState(pos.above()).is(Blocks.BEDROCK) && player.level().isEmptyBlock(pos));
        clearColumn(player, pos);
        place(player, pos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) player.level().getBlockEntity(pos);
        chest.setItem(0, new ItemStack(Items.EMERALD, 3));
        player.setShiftKeyDown(true);
        use(player, pos);
        player.setShiftKeyDown(false);
        expectTrue("方块实体移动只搬状态并新建空库存", player.level().getBlockEntity(pos.above()) instanceof ChestBlockEntity moved && moved.isEmpty());
        int dropped = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream()
                .filter(entity -> entity.getItem().is(Items.EMERALD)).mapToInt(entity -> entity.getItem().getCount()).sum();
        expect("原位库存经原生移除掉落", 3, dropped);
    }

    private void repair(ServerPlayer player) {
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("flyinginstrument_0101"));
        player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        FlyingInstrumentEntity flying = player.level().getEntitiesOfClass(FlyingInstrumentEntity.class, new AABB(base).inflate(24)).getFirst();
        flying.setNoAi(true);
        flying.setNoGravity(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        flying.setHealth(flying.getMaxHealth() - 2);
        flying.mobInteract(player, InteractionHand.MAIN_HAND);
        expect("锤修理一次恢复一点生命", flying.getMaxHealth() - 1, flying.getHealth());
        flying.mobInteract(player, InteractionHand.MAIN_HAND);
        flying.mobInteract(player, InteractionHand.MAIN_HAND);
        expectTrue("满血持锤不进入骑乘分支", flying.getHealth() == flying.getMaxHealth() && !player.isPassenger());
        flying.discard();
        player.inventoryMenu.broadcastChanges();
    }

    private void restore(ServerPlayer player) {
        for (ItemEntity entity : player.level().getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(24))) entity.discard();
        for (Map.Entry<BlockPos, BlockState> entry : terrain.entrySet()) player.level().setBlock(entry.getKey(), entry.getValue(), 3);
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private void clearColumn(ServerPlayer player, BlockPos pos) {
        for (int dy = -1; dy <= 1; dy++) place(player, pos.offset(0, dy, 0), Blocks.AIR.defaultBlockState());
    }

    private void place(ServerPlayer player, BlockPos pos, BlockState state) {
        terrain.putIfAbsent(pos.immutable(), player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 3);
    }

    private static void use(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static void left(ServerPlayer player, BlockPos pos) {
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.START));
    }

    private static BlockState tile(String color, int form, Direction facing) {
        return BuiltInRegistries.BLOCK.get(id("tileimbrex" + color + "_0" + form)).defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }
}
