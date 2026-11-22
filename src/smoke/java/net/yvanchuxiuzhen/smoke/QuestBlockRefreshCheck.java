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
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;

/** 通过原生玩家方块右键检查两处商店修补的手别、潜行顺序与方块存档。 */
final class QuestBlockRefreshCheck extends SystemCheck {
    private static final BlockPos MATERIAL_SHOP = new BlockPos(-103, 64, -263);
    private static final BlockPos CLOTHING_SHOP = new BlockPos(-51, 64, -320);
    private static final String OLD_INFORMATION = "陈氏服饰%陈氏%在这里可以买到初级服饰。";
    private static final String NEW_INFORMATION = "陈氏服饰%陈氏%在这里可以买到初级服饰和一些布匹。";
    private static final String CLOTHING_SHA256 = "b19f8adaaac9fd4d30a6676e560044c9bcf9d00f91833b79c6452b4fb5173ee1";
    private static final String SENTINEL_PRODUCTS = "待修补货架";

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
        CompoundTag data = block.getPersistentData();
        data.putBoolean("shuaxin", true);
        data.putString("productlist1", SENTINEL_PRODUCTS);
        use(player, MATERIAL_SHOP, current);
        String firstProducts = data.getString("productlist1");
        expectTrue("材料店当前手右键消费刷新标记并生成六十件商品",
                !data.getBoolean("shuaxin") && firstProducts.split("%", -1).length == 300);
        use(player, MATERIAL_SHOP, current);
        expect("刷新标记消费后再次右键保留货架", firstProducts, data.getString("productlist1"));

        data.putBoolean("shuaxin", true);
        data.putString("productlist1", SENTINEL_PRODUCTS);
        use(player, MATERIAL_SHOP, other);
        expectTrue("非当前手右键不消费标记或修改货架", data.getBoolean("shuaxin")
                && data.getString("productlist1").equals(SENTINEL_PRODUCTS));

        player.setShiftKeyDown(true);
        use(player, MATERIAL_SHOP, current);
        player.setShiftKeyDown(false);
        String sneakingProducts = data.getString("productlist1");
        expectTrue("持物潜行绕过方块交互前仍修补材料货架",
                !data.getBoolean("shuaxin") && sneakingProducts.split("%", -1).length == 300);
        DisguisedQuestBlockEntity restored = read(player, block);
        expectTrue("材料店保存重读保留消费后的标记与完整货架", !restored.getPersistentData().getBoolean("shuaxin")
                && restored.getPersistentData().getString("productlist1").equals(sneakingProducts));
    }

    private void clothing(ServerPlayer player, DisguisedQuestBlockEntity block, InteractionHand current) {
        CompoundTag data = block.getPersistentData();
        String mismatch = OLD_INFORMATION + " ";
        data.putString("shopinformation1", mismatch);
        data.putString("productlist1", SENTINEL_PRODUCTS);
        use(player, CLOTHING_SHOP, current);
        expectTrue("陈氏旧说明只差末尾空格时不改说明或商品", data.getString("shopinformation1").equals(mismatch)
                && data.getString("productlist1").equals(SENTINEL_PRODUCTS));

        data.putString("shopinformation1", OLD_INFORMATION);
        use(player, CLOTHING_SHOP, current);
        expect("精确旧说明替换为来源中文说明", NEW_INFORMATION, data.getString("shopinformation1"));
        String products = data.getString("productlist1");
        expectTrue("陈氏整份十一件商品与来源提取摘要一致", products.split("%", -1).length == 55
                && sha256(products).equals(CLOTHING_SHA256));
        DisguisedQuestBlockEntity restored = read(player, block);
        expectTrue("陈氏保存重读保留中文说明与完整商品", restored.getPersistentData().getString("shopinformation1").equals(NEW_INFORMATION)
                && restored.getPersistentData().getString("productlist1").equals(products));
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
