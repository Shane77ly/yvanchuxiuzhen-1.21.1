package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.treasure.content.TreasureCasketBlockEntity;

/** 通过原生服务端玩家右键验证来源宝匣配置、成品消费、库存保存及五个真实结束刻的移除。 */
final class TreasureCasketCheck extends SystemCheck {
    private static final String PRODUCT_COMMAND = "lianqi yvanchuxiuzhen:weaponsword_04 寒锋剑 剑类武器，拥有最平衡的属性。 武器 玄阶七级 17 24 9 3 0 0 0 90";
    private final Consumer<ServerTickEvent.Post> observer = this::observe;
    private final List<SavedBlock> blocks = new ArrayList<>();
    private final List<Boolean> lifecycle = new ArrayList<>();
    private final List<Boolean> replacementLifecycle = new ArrayList<>();
    private ServerLevel level;
    private BlockPos productPos;
    private BlockPos emptyPos;
    private BlockPos replacedPos;
    private ItemStack oldMain;
    private ItemStack oldOff;
    private GameType oldMode;
    private boolean oldSneaking;

    TreasureCasketCheck(List<String> report, String levelId) {
        super("宝匣", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            tooltip();
            onServer(this::prepare);
            return true;
        }
        if (step != 1) throw new IllegalStateException("未知宝匣检查步骤 " + step);
        if (local < 12) return false;
        onServer(player -> {
            NeoForge.EVENT_BUS.unregister(observer);
            try {
                expect("首次开启后第五个全服结束刻移除", List.of(true, true, true, true, false), lifecycle);
                expect("来源延迟移除覆盖途中替换的方块", List.of(true, true, true, true, false), replacementLifecycle);
                expectTrue("空配置未开启且延迟后仍保留", level.getBlockEntity(emptyPos) instanceof TreasureCasketBlockEntity empty
                        && !empty.opened());
                List<ItemEntity> rewards = drops(productPos);
                expect("延迟移除不会重复发放成品", 1, rewards.size());
                expectTrue("成品槽清空后宝匣移除没有额外库存掉落", rewards.stream().allMatch(drop ->
                        drop.getItem().is(BuiltInRegistries.ITEM.get(id("weaponsword_04")))));
            } finally {
                cleanup(player);
            }
        });
        finish();
        return true;
    }

    private void prepare(ServerPlayer player) {
        level = player.serverLevel();
        oldMain = player.getMainHandItem().copy();
        oldOff = player.getOffhandItem().copy();
        oldMode = player.gameMode.getGameModeForPlayer();
        oldSneaking = player.isShiftKeyDown();
        productPos = player.blockPosition().above(10).east(3);
        emptyPos = productPos.west(3);
        replacedPos = productPos.east(3);
        for (BlockPos pos : List.of(productPos, emptyPos, replacedPos)) {
            for (BlockPos part : List.of(pos.below(), pos)) blocks.add(SavedBlock.capture(level, part));
            level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, configured(PRODUCT_COMMAND, 2));
        use(player, productPos.below(), Direction.NORTH);
        expectTrue("侧面使用不放置且不扣物品", level.isEmptyBlock(productPos) && player.getMainHandItem().getCount() == 2);
        level.setBlock(productPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        use(player, productPos.below(), Direction.UP);
        expectTrue("上方占位时不覆盖且不扣物品", level.getBlockState(productPos).is(Blocks.STONE) && player.getMainHandItem().getCount() == 2);
        level.setBlock(productPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        use(player, productPos.below(), Direction.UP);
        TreasureCasketBlockEntity casket = (TreasureCasketBlockEntity) level.getBlockEntity(productPos);
        expectTrue("顶面空位放置复制指令并只扣一个", casket != null && player.getMainHandItem().getCount() == 1
                && casket.command().equals(PRODUCT_COMMAND));
        expectTrue("放置方向与来源一致且初始不含水", casket.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)
                == player.getDirection().getOpposite() && !casket.getBlockState().getValue(BlockStateProperties.WATERLOGGED));
        expect("宝匣保留生成Java不可破坏硬度", -1, casket.getBlockState().getDestroySpeed(level, productPos));
        inventory(player, casket);

        player.setItemInHand(InteractionHand.MAIN_HAND, configured("", 1));
        use(player, emptyPos.below(), Direction.UP);
        use(player, emptyPos, Direction.UP);
        expectTrue("空配置自然右键不打开", !((TreasureCasketBlockEntity) level.getBlockEntity(emptyPos)).opened());

        player.setItemInHand(InteractionHand.MAIN_HAND, configured("不存在的宝匣指令", 1));
        use(player, replacedPos.below(), Direction.UP);
        use(player, replacedPos, Direction.UP);
        expectTrue("无效指令仍写入一次性开启标志", ((TreasureCasketBlockEntity) level.getBlockEntity(replacedPos)).opened());
        level.setBlock(replacedPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, observer);
        use(player, productPos, Direction.UP);
        expectTrue("首次自然开启执行成品并清空中转槽", casket.opened() && casket.getItem(0).isEmpty());
        List<ItemEntity> rewards = drops(productPos);
        expect("首次开启只有一件实际成品掉落", 1, rewards.size());
        expectTrue("来源NPC成品指令生成可消费装备实例", rewards.size() == 1
                && Equipment.product(rewards.getFirst().getItem()).filter(product -> product.name().equals("寒锋剑")
                        && product.grade().equals("玄阶七级") && product.level() == 17 && product.weight() == 90).isPresent());
        expectTrue("成品实际掉落坐标为宝匣整数坐标", rewards.size() == 1 && rewards.getFirst().position().equals(Vec3.atLowerCornerOf(productPos)));
        use(player, productPos, Direction.UP);
        expect("同刻重复开启不重复发奖", 1, drops(productPos).size());
        CompoundTag saved = casket.saveWithFullMetadata(player.registryAccess());
        TreasureCasketBlockEntity restored = (TreasureCasketBlockEntity) BlockEntity.loadStatic(productPos, casket.getBlockState(), saved, player.registryAccess());
        expectTrue("开启标志及配置真实序列化重读", restored.opened()
                && restored.command().equals(PRODUCT_COMMAND));
    }

    private void inventory(ServerPlayer player, TreasureCasketBlockEntity casket) {
        casket.setItem(0, new ItemStack(Items.DIAMOND, 17));
        IItemHandler unsided = level.getCapability(Capabilities.ItemHandler.BLOCK, productPos, null);
        boolean shared = unsided != null && unsided.getSlots() == 1 && unsided.getSlotLimit(0) == 64;
        for (Direction direction : Direction.values()) {
            IItemHandler sided = level.getCapability(Capabilities.ItemHandler.BLOCK, productPos, direction);
            shared &= sided != null && sided.getStackInSlot(0).is(Items.DIAMOND) && sided.getStackInSlot(0).getCount() == 17;
        }
        expectTrue("无方向与六面能力访问同一六十四件槽", shared);
        ((IItemHandlerModifiable) unsided).setStackInSlot(0, new ItemStack(Items.DIAMOND, 18));
        TreasureCasketBlockEntity restored = (TreasureCasketBlockEntity) BlockEntity.loadStatic(productPos, casket.getBlockState(),
                casket.saveWithFullMetadata(player.registryAccess()), player.registryAccess());
        expectTrue("持久库存和未开配置真实序列化重读", restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 18
                && restored.command().equals(PRODUCT_COMMAND) && !restored.opened());
        casket.setItem(0, ItemStack.EMPTY);
    }

    private void tooltip() {
        List<String> weapon = configured(PRODUCT_COMMAND, 1).getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL)
                .stream().map(Component::getString).toList();
        expectTrue("宝匣实际Tooltip保留来源品阶与物理攻击", weapon.contains("§6玄阶七级") && weapon.contains("§b物理攻击+24")
                && weapon.stream().noneMatch(line -> line.contains("tooltip.yvanchuxiuzhen")));
        String armorCommand = "lianqi yvanchuxiuzhen:armor_01_helmet 布帽 说明 头盔 黄阶一级 1 31 47 53 59 0 0 61 67 71 79";
        List<String> armor = configured(armorCommand, 1).getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL)
                .stream().map(Component::getString).toList();
        expectTrue("护甲预览保留来源血量条件读七值却显示八值", armor.contains("§b血量上限+47") && armor.contains("§b灵力上限+47"));
    }

    private void observe(ServerTickEvent.Post event) {
        if (event.getServer() != level.getServer() || lifecycle.size() >= 5) return;
        lifecycle.add(level.getBlockState(productPos).is(BuiltInRegistries.BLOCK.get(id("baoxia"))));
        replacementLifecycle.add(level.getBlockState(replacedPos).is(Blocks.STONE));
    }

    private static ItemStack configured(String command, int count) {
        return Caskets.create(command).copyWithCount(count);
    }

    private static void use(ServerPlayer player, BlockPos pos, Direction face) {
        player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
    }

    private List<ItemEntity> drops(BlockPos pos) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
    }

    private void cleanup(ServerPlayer player) {
        drops(productPos).forEach(ItemEntity::discard);
        drops(emptyPos).forEach(ItemEntity::discard);
        drops(replacedPos).forEach(ItemEntity::discard);
        for (SavedBlock block : blocks) block.restore(level);
        player.setItemInHand(InteractionHand.MAIN_HAND, oldMain);
        player.setItemInHand(InteractionHand.OFF_HAND, oldOff);
        player.setGameMode(oldMode);
        player.setShiftKeyDown(oldSneaking);
        player.inventoryMenu.broadcastChanges();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }

    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerLevel level, BlockPos pos) {
            BlockEntity block = level.getBlockEntity(pos);
            return new SavedBlock(pos, level.getBlockState(pos), block == null ? null : block.saveWithFullMetadata(level.registryAccess()));
        }

        void restore(ServerLevel level) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, level.registryAccess()));
        }
    }
}
