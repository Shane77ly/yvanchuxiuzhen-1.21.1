package net.yvanchuxiuzhen.smoke;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;

/** 通过原生玩家方块右键检查两处商店修补的手别、潜行顺序与方块存档。 */
final class QuestBlockRefreshCheck extends SystemCheck {
    private static final BlockPos MATERIAL_SHOP = new BlockPos(-103, 64, -263);
    private static final BlockPos CLOTHING_SHOP = new BlockPos(-51, 64, -320);
    private static final String OLD_SHOP_ID = "fanhuacheng09_21_2_16_s1";
    private static final String REPAIRED_SHOP_ID = "huafancheng_clothing_repaired";
    private static final String OLD_DESCRIPTION = "在这里可以买到初级服饰。";
    private static final String NEW_DESCRIPTION = "在这里可以买到初级服饰和一些布匹。";

    QuestBlockRefreshCheck(List<String> report, String levelId) {
        super("任务方块商店刷新", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step != 0) throw new IllegalStateException("未知任务方块刷新检查步骤 " + step);
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        SavedBlock materialBefore = SavedBlock.capture(player, MATERIAL_SHOP);
        SavedBlock clothingBefore = SavedBlock.capture(player, CLOTHING_SHOP);
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        GameType mode = player.gameMode.getGameModeForPlayer();
        boolean sneaking = player.isShiftKeyDown();
        InteractionHand current = player.getUsedItemHand();
        InteractionHand other = current == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        try {
            player.setGameMode(GameType.CREATIVE);
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_INGOT));
            DisguisedQuestBlockEntity material = install(player, MATERIAL_SHOP);
            DisguisedQuestBlockEntity clothing = install(player, CLOTHING_SHOP);
            material(player, material, current, other);
            clothing(player, clothing, current);
        } finally {
            materialBefore.restore(player);
            clothingBefore.restore(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);
            player.setGameMode(mode);
            player.setShiftKeyDown(sneaking);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private void material(ServerPlayer player, DisguisedQuestBlockEntity block, InteractionHand current, InteractionHand other) {
        block.setRefreshDue(true);
        block.setShopId(1, "npc03");
        block.setCustomShop(1, ShopCatalogDefinition.EMPTY);
        use(player, MATERIAL_SHOP, current);
        ShopCatalogDefinition firstShop = block.customShop(1);
        expectTrue("材料店当前手右键消费刷新标记并生成六十件强类型商品",
                !block.refreshDue() && firstShop.products().size() == 60);
        use(player, MATERIAL_SHOP, current);
        expect("刷新标记消费后再次右键保留货架", firstShop, block.customShop(1));

        block.setRefreshDue(true);
        block.setShopId(1, "npc02");
        block.setCustomShop(1, ShopCatalogDefinition.EMPTY);
        use(player, MATERIAL_SHOP, other);
        expectTrue("非当前手右键不消费标记或修改货架", block.refreshDue()
                && block.customShop(1).products().isEmpty() && block.shopId(1).equals("npc02"));

        player.setShiftKeyDown(true);
        use(player, MATERIAL_SHOP, current);
        player.setShiftKeyDown(false);
        ShopCatalogDefinition sneakingShop = block.customShop(1);
        expectTrue("持物潜行绕过方块交互前仍修补材料货架",
                !block.refreshDue() && sneakingShop.products().size() == 60);
        DisguisedQuestBlockEntity restored = read(player, block);
        expectTrue("材料店保存重读保留消费后的标记与完整货架", !restored.refreshDue()
                && restored.customShop(1).equals(sneakingShop));
    }

    private void clothing(ServerPlayer player, DisguisedQuestBlockEntity block, InteractionHand current) {
        block.setShopId(1, "npc02");
        block.setCustomShop(1, ShopCatalogDefinition.EMPTY);
        use(player, CLOTHING_SHOP, current);
        expectTrue("非陈氏旧说明时不改说明或商品", block.shopId(1).equals("npc02"));

        block.setShopId(1, OLD_SHOP_ID);
        block.setCustomShop(1, ShopCatalogDefinition.EMPTY);
        expect("陈氏旧说明匹配", OLD_DESCRIPTION, block.resolveShop(1).info().description());
        use(player, CLOTHING_SHOP, current);
        expect("精确旧说明替换为来源中文商店ID", REPAIRED_SHOP_ID, block.shopId(1));
        ShopCatalogDefinition repaired = block.resolveShop(1);
        expect("精确旧说明替换为来源中文说明", NEW_DESCRIPTION, repaired.info().description());
        expect("陈氏整份十一件强类型商品", 11, repaired.products().size());
        DisguisedQuestBlockEntity restored = read(player, block);
        expectTrue("陈氏保存重读保留商店ID与完整商品", restored.shopId(1).equals(REPAIRED_SHOP_ID)
                && restored.resolveShop(1).equals(repaired));
    }

    private static void use(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        player.gameMode.useItemOn(player, player.serverLevel(), player.getItemInHand(hand), hand,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static DisguisedQuestBlockEntity install(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        level.removeBlockEntity(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "renwuweizhuangfangkuai")).defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        return (DisguisedQuestBlockEntity) level.getBlockEntity(pos);
    }

    private static DisguisedQuestBlockEntity read(ServerPlayer player, DisguisedQuestBlockEntity block) {
        return (DisguisedQuestBlockEntity) BlockEntity.loadStatic(block.getBlockPos(), block.getBlockState(),
                block.saveWithFullMetadata(player.registryAccess()), player.registryAccess());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Java运行时缺少SHA-256", exception);
        }
    }

    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            ServerLevel level = player.serverLevel();
            BlockEntity block = level.getBlockEntity(pos);
            return new SavedBlock(pos, level.getBlockState(pos), block == null ? null : block.saveWithFullMetadata(player.registryAccess()));
        }

        void restore(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
