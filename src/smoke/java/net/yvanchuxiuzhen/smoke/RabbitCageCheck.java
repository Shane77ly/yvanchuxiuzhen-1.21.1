package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.yvanchuxiuzhen.collection.decor.CritterCageData;
import net.yvanchuxiuzhen.collection.decor.DecorData;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.collection.decor.RabbitCageBlockEntity;

/** 兔笼配方、自然交互、保护时段、两类动物和诱饵及满笼存档的针对性检查。 */
final class RabbitCageCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private Vec3 originalPosition;
    private GameType originalMode;
    private int originalSelected;
    private boolean originalShift;
    private BlockPos cage;
    private BlockPos savedCage;
    private UUID rabbitId;
    private long openedAt;

    RabbitCageCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("兔笼整组", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        if (step == 5) return reopened(local);
        if (step == 6) return inWorld(local);
        if (local < 15) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> onServer(this::openRabbitFixture);
            case 2 -> onServer(player -> {
                player.serverLevel().getEntity(rabbitId).move(MoverType.SELF, Vec3.ZERO);
                expectTrue("保护时段动物碰撞仍保持开笼", player.level().getBlockState(cage).is(block("zhuangshi_tulong_02")));
                expectTrue("保护状态已随开笼建立", cage(player, cage).forcedOpen());
                IItemHandler unsided = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, cage, null);
                IItemHandler sided = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, cage, Direction.UP);
                expect("无方向能力单槽上限", 1, unsided.getSlotLimit(0));
                expectTrue("侧面禁止插入", !sided.insertItem(0, new ItemStack(Items.CARROT), true).isEmpty());
                expectTrue("侧面禁止抽取", sided.extractItem(0, 1, true).isEmpty());
            });
            case 3 -> {
                boolean[] ready = {false};
                onServer(player -> ready[0] = player.level().getGameTime() >= openedAt + 105);
                if (!ready[0]) return false;
                onServer(this::collectRabbitAndSaveBait);
            }
            case 4 -> {
                screenshots.take("rabbit_cage_saved_bait.png");
                leaveWorld();
            }
            case 7 -> onServer(this::reloadAndReleaseRabbit);
            case 8 -> onServer(this::collectAndReleaseChicken);
            case 9 -> {
                onServer(this::cleanup);
                finish();
            }
            default -> throw new IllegalStateException("未知兔笼检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalSelected = player.getInventory().selected;
        originalShift = player.isShiftKeyDown();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        cage = player.blockPosition().above(12);
        savedCage = cage.east(3);
        for (BlockPos pos : BlockPos.betweenClosed(cage.offset(-2, -1, -3), cage.offset(5, -1, 3)))
            player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        player.connection.teleport(cage.getX() + .5, cage.getY(), cage.getZ() - 2, 0, 20);
        ShapedRecipe recipe = (ShapedRecipe) player.server.getRecipeManager().byKey(id("zhuangshitulonghecheng")).orElseThrow().value();
        CraftingInput input = CraftingInput.of(3, 3, List.of(new ItemStack(Items.BAMBOO), new ItemStack(Items.STICK), new ItemStack(Items.BAMBOO),
                new ItemStack(Items.STICK), ItemStack.EMPTY, new ItemStack(Items.STICK),
                new ItemStack(Items.BAMBOO), new ItemStack(Items.STICK), new ItemStack(Items.BAMBOO)));
        expectTrue("四竹四棍配方实际匹配", recipe.matches(input, player.level()));
        ItemStack result = recipe.assemble(input, player.registryAccess());
        expectTrue("配方产出一件空笼", result.is(item("zhuangshitulongitem_01")) && result.getCount() == 1);
        result.setCount(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, result);
        player.inventoryMenu.broadcastChanges();
    }

    private void openRabbitFixture(ServerPlayer player) {
        use(player, cage.below(), Direction.UP, false);
        expectTrue("空笼真实放置闭笼", player.level().getBlockState(cage).is(block("zhuangshi_tulong_01")));
        expect("放置消耗一个空笼", 1, player.getMainHandItem().getCount());
        List<ItemStack> blockDrops = Block.getDrops(player.level().getBlockState(cage), player.serverLevel(), cage, null);
        expectTrue("闭笼挖掘掉落空笼", blockDrops.size() == 1 && blockDrops.getFirst().is(item("zhuangshitulongitem_01")));
        player.getInventory().setItem(10, player.getMainHandItem().copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(player, cage, Direction.NORTH, false);
        openedAt = player.level().getGameTime();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 2));
        use(player, cage, Direction.NORTH, false);
        expect("主手胡萝卜消耗一件", 1, player.getMainHandItem().getCount());
        expectTrue("真实右键写入单件诱饵", cage(player, cage).getItem(0).is(Items.CARROT) && cage(player, cage).getItem(0).getCount() == 1);
        Rabbit rabbit = new Rabbit(EntityType.RABBIT, player.level());
        rabbit.setVariant(Rabbit.Variant.GOLD);
        rabbit.setAge(-24000);
        rabbit.setCustomName(Component.literal("rabbit-cage-source-fixture"));
        rabbit.setNoAi(true);
        rabbit.setNoGravity(true);
        rabbit.moveTo(cage.getX() + .5, cage.getY() + .2, cage.getZ() + .5);
        rabbit.setDeltaMovement(.001, 0, 0);
        player.serverLevel().addFreshEntity(rabbit);
        rabbitId = rabbit.getUUID();
    }

    private void collectRabbitAndSaveBait(ServerPlayer player) {
        player.serverLevel().getEntity(rabbitId).move(MoverType.SELF, Vec3.ZERO);
        expectTrue("百刻后原生碰撞关闭笼子", player.level().getBlockState(cage).is(block("zhuangshi_tulong_01")));
        expectTrue("捕获后原生兔实体仍存活", player.serverLevel().getEntity(rabbitId) instanceof Rabbit);
        expect("捕获消费诱饵无掉落", 0, drops(player, cage, Items.CARROT));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(player, cage, Direction.NORTH, true);
        ItemStack filled = find(player, "zhuangshitulongitem_02");
        CritterCageData data = filled.getOrDefault(DecorData.CRITTER_CAGE.get(), CritterCageData.EMPTY);
        expect("收笼兔数量", 1, data.rabbitCount());
        expect("收笼保留兔种", Rabbit.Variant.GOLD.id(), data.rabbitVariant());
        expectTrue("收笼删除原世界动物", player.serverLevel().getEntity(rabbitId) == null);
        expectTrue("收笼移除方块", player.level().isEmptyBlock(cage));
        player.getInventory().removeItem(filled);
        player.getInventory().setItem(9, filled.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().getItem(10).copy());
        player.getInventory().setItem(10, ItemStack.EMPTY);
        use(player, savedCage.below(), Direction.UP, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(player, savedCage, Direction.NORTH, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 2));
        use(player, savedCage, Direction.NORTH, false);
        player.connection.teleport(savedCage.getX() + .5, savedCage.getY(), savedCage.getZ() - 2, 0, 25);
        player.inventoryMenu.broadcastChanges();
    }

    private void reloadAndReleaseRabbit(ServerPlayer player) {
        ItemStack saved = player.getInventory().getItem(9);
        expectTrue("满笼物品随玩家存档恢复", saved.is(item("zhuangshitulongitem_02")));
        expect("满笼兔种随存档恢复", Rabbit.Variant.GOLD.id(), saved.getOrDefault(DecorData.CRITTER_CAGE.get(), CritterCageData.EMPTY).rabbitVariant());
        expectTrue("开笼诱饵随方块存档恢复", cage(player, savedCage).getItem(0).is(Items.CARROT));
        expectTrue("保护计数由自然二十刻更新结束", !cage(player, savedCage).forcedOpen());
        player.setItemInHand(InteractionHand.MAIN_HAND, saved.copy());
        player.getInventory().setItem(9, ItemStack.EMPTY);
        player.connection.teleport(cage.getX() + .5, cage.getY(), cage.getZ() - 2, 0, 20);
        use(player, cage.below(), Direction.UP, false);
        List<Rabbit> released = player.level().getEntitiesOfClass(Rabbit.class, new AABB(cage), entity -> true);
        expect("再次放置生成一只原生兔", 1, released.size());
        Rabbit rabbit = released.getFirst();
        expect("放出后仍为来源兔种", Rabbit.Variant.GOLD, rabbit.getVariant());
        expectTrue("放出按来源重建年龄与名称", !rabbit.isBaby() && !rabbit.hasCustomName() && !rabbit.getUUID().equals(rabbitId));
        expectTrue("放出消耗满笼物品", player.getMainHandItem().isEmpty());
        player.connection.teleport(savedCage.getX() + .5, savedCage.getY(), savedCage.getZ() - 2, 0, 20);
        ItemStack seed = new ItemStack(item("shicai_001seed"), 2);
        seed.set(DataComponents.CUSTOM_NAME, Component.literal("rabbit-cage-bait-fixture"));
        player.setItemInHand(InteractionHand.MAIN_HAND, seed);
        use(player, savedCage, Direction.NORTH, false);
        expect("更换诱饵掉出旧胡萝卜", 1, drops(player, savedCage, Items.CARROT));
        expectTrue("来源重新构造诱饵不继承手持名称", !cage(player, savedCage).getItem(0).has(DataComponents.CUSTOM_NAME));
        expect("种子诱饵消耗一件", 1, player.getMainHandItem().getCount());
        Chicken chicken = new Chicken(EntityType.CHICKEN, player.level());
        chicken.setNoAi(true);
        chicken.setNoGravity(true);
        chicken.moveTo(savedCage.getX() + .5, savedCage.getY() + .2, savedCage.getZ() + .5);
        chicken.setDeltaMovement(.001, 0, 0);
        player.serverLevel().addFreshEntity(chicken);
    }

    private void collectAndReleaseChicken(ServerPlayer player) {
        for (Chicken chicken : player.level().getEntitiesOfClass(Chicken.class, new AABB(savedCage))) {
            chicken.move(MoverType.SELF, Vec3.ZERO);
        }
        expectTrue("原生鸡碰撞关闭开笼", player.level().getBlockState(savedCage).is(block("zhuangshi_tulong_01")));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(player, savedCage, Direction.NORTH, true);
        ItemStack filled = find(player, "zhuangshitulongitem_03");
        expect("鸡笼保存来源数量", 1, filled.getOrDefault(DecorData.CRITTER_CAGE.get(), CritterCageData.EMPTY).chickenCount());
        player.getInventory().removeItem(filled);
        player.setItemInHand(InteractionHand.MAIN_HAND, filled.copy());
        use(player, savedCage.below(), Direction.UP, false);
        expect("鸡笼再次放置生成一只原生鸡", 1, player.level().getEntitiesOfClass(Chicken.class, new AABB(savedCage), entity -> true).size());
        expectTrue("鸡笼再次放置消耗物品", player.getMainHandItem().isEmpty());
        use(player, savedCage, Direction.NORTH, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("shicai_001seed")));
        use(player, savedCage, Direction.NORTH, false);
        use(player, savedCage, Direction.NORTH, true);
        expectTrue("潜行收取开笼返回空笼", !find(player, "zhuangshitulongitem_01").isEmpty());
        expect("潜行收取开笼掉出诱饵", 1, drops(player, savedCage, item("shicai_001seed")));
    }

    private void cleanup(ServerPlayer player) {
        for (Entity entity : player.level().getEntitiesOfClass(Entity.class, new AABB(cage).expandTowards(4, 1, 0).inflate(2),
                entity -> entity instanceof Rabbit || entity instanceof Chicken || entity instanceof ItemEntity)) entity.discard();
        for (BlockPos pos : BlockPos.betweenClosed(cage.offset(-2, -1, -3), cage.offset(5, 1, 3)))
            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = originalSelected;
        player.setShiftKeyDown(originalShift);
        player.setGameMode(originalMode);
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, 0, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private static void use(ServerPlayer player, BlockPos pos, Direction face, boolean shift) {
        player.setShiftKeyDown(shift);
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
        player.setShiftKeyDown(false);
    }

    private static RabbitCageBlockEntity cage(ServerPlayer player, BlockPos pos) { return (RabbitCageBlockEntity) player.level().getBlockEntity(pos); }
    private static ItemStack find(ServerPlayer player, String path) {
        for (ItemStack stack : player.getInventory().items) if (stack.is(item(path))) return stack;
        return ItemStack.EMPTY;
    }
    private static int drops(ServerPlayer player, BlockPos pos, Item item) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2), entity -> entity.getItem().is(item)).stream()
                .mapToInt(entity -> entity.getItem().getCount()).sum();
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.get(id(path)); }
    private static Block block(String path) { return BuiltInRegistries.BLOCK.get(id(path)); }
}
