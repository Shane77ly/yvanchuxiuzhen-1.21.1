package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/** 酒蛊原生使用、体质同步与存储编解码，以及采药锄自然采集和虫卵实物。 */
final class GuConstitutionHarvestCheck extends ActionCheck {
    private final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private MobEffectInstance originalLuck;
    private MobEffectInstance originalUnluck;
    private boolean originalConstitution;
    private double originalPatience;
    private double originalDigestion;
    private double originalNumber;
    private BlockPos site;

    GuConstitutionHarvestCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("酒蛊与采药锄", report, levelId);
        script(
                new Action(5, this::prepare),
                new Action(8, player -> {
                    resetConstitution(player);
                    player.setGameMode(GameType.SURVIVAL);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item("guchong_02item"));
                    use(player);
                    expectTrue("生存原生右键消耗酒蛊并准确写四个体质字段", player.getMainHandItem().isEmpty() && constitution(player));
                }),
                new Action(8, player -> {
                    expectTrue("酒蛊四个体质字段同步到本人客户端", constitution(minecraft.player));
                    player.setItemInHand(InteractionHand.MAIN_HAND, item("guchong_02item"));
                    use(player);
                    expectTrue("重复原生右键保留酒蛊且不重复增加体质", player.getMainHandItem().getCount() == 1 && constitution(player));
                    minecraft.execute(() -> screenshots.take("gu-constitution-messages.png"));
                }),
                new Action(8, player -> {
                    resetConstitution(player);
                    player.setGameMode(GameType.CREATIVE);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item("guchong_02item"));
                    use(player);
                    expectTrue("创造原生右键获得体质且按1.18.2原生包装保留物品", player.getMainHandItem().getCount() == 1 && constitution(player));
                }),
                new Action(5, player -> {
                    expectTrue("创造获得后的体质同步到本人客户端", constitution(minecraft.player));
                    var ops = player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                    var encoded = CultivationData.CODEC.encodeStart(ops, player.getData(XiulianAttachments.CULTIVATION)).getOrThrow();
                    CultivationData restored = CultivationData.CODEC.parse(ops, encoded).getOrThrow();
                    expectTrue("修炼实际持久化Codec保存重读保留酒蛊四字段", restored.tizhi().getBoolean("tizhiopen_013")
                            && restored.tizhi().getDouble("AlcoholPatience") == 107
                            && restored.tizhi().getDouble("AlcoholDigestionPercentage") == -0.75
                            && restored.tizhi().getDouble("tizhinumber") == 4);
                    harvest(player);
                }));
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalLuck = copy(player.getEffect(MobEffects.LUCK));
        originalUnluck = copy(player.getEffect(MobEffects.UNLUCK));
        originalConstitution = Cultivation.tizhiBoolean(player, "tizhiopen_013");
        originalPatience = Cultivation.tizhiDouble(player, "AlcoholPatience");
        originalDigestion = Cultivation.tizhiDouble(player, "AlcoholDigestionPercentage");
        originalNumber = Cultivation.tizhiDouble(player, "tizhinumber");
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        player.getInventory().clearContent();
        player.setGameMode(GameType.CREATIVE);
        site = player.blockPosition().above(8);
        for (int x = -4; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                setBlock(player, site.offset(x, -1, z), Blocks.DIRT.defaultBlockState());
            }
        }
        setBlock(player, site, Blocks.AIR.defaultBlockState());
        setBlock(player, site.above(), Blocks.AIR.defaultBlockState());
        player.connection.teleport(site.getX() - 2.5, site.getY(), site.getZ() + 0.5, -90, 0);
    }

    private void harvest(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.removeEffect(MobEffects.UNLUCK);
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 600, 50));
        player.setItemInHand(InteractionHand.MAIN_HAND, item("gongjv_008"));
        harvestSingle(player, "原版叶", Blocks.OAK_LEAVES.defaultBlockState());
        harvestSingle(player, "原版花", Blocks.DANDELION.defaultBlockState());
        harvestSingle(player, "来源材质模组叶", BuiltInRegistries.BLOCK.get(id("cassia_03")).defaultBlockState());
        harvestDouble(player, DoubleBlockHalf.LOWER, 1);
        harvestDouble(player, DoubleBlockHalf.UPPER, 2);
        clearSite(player);
        setBlock(player, site, Blocks.STONE.defaultBlockState());
        useOn(player, site);
        expectTrue("药锄拒绝石材且不生成方块或虫卵实物", player.level().getBlockState(site).is(Blocks.STONE) && drops(player).isEmpty());
        clearSite(player);
        player.removeEffect(MobEffects.LUCK);
        player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 600, 0));
        setBlock(player, site, Blocks.DANDELION.defaultBlockState());
        useOn(player, site);
        expectTrue("仅霉运时保留来源负概率而只产花不产虫卵", count(player, Blocks.DANDELION.asItem()) == 1
                && count(player, item("chongluan").getItem()) == 0 && player.level().isEmptyBlock(site));
        expect("以上自定义采集不额外扣耐久", 0, player.getMainHandItem().getDamageValue());
        clearSite(player);
        setBlock(player, site, Blocks.GRASS_BLOCK.defaultBlockState());
        useOn(player, site);
        expectTrue("先执行原生锹铺路且只扣原生一点耐久", player.level().getBlockState(site).is(Blocks.DIRT_PATH)
                && player.getMainHandItem().getDamageValue() == 1 && drops(player).isEmpty());
    }

    private void harvestSingle(ServerPlayer player, String label, BlockState state) {
        clearSite(player);
        setBlock(player, site, state);
        useOn(player, site);
        expectTrue(label + "原生右键产一件方块和一枚高幸运虫卵且移除方块", player.level().isEmptyBlock(site)
                && count(player, state.getBlock().asItem()) == 1 && count(player, item("chongluan").getItem()) == 1
                && drops(player).size() == 2);
    }

    private void harvestDouble(ServerPlayer player, DoubleBlockHalf half, int expectedFlowers) {
        clearSite(player);
        BlockState lower = Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        setBlock(player, site, lower);
        setBlock(player, site.above(), lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        useOn(player, half == DoubleBlockHalf.LOWER ? site : site.above());
        expectTrue("双高植物点击" + half.getSerializedName() + "移除上下两格并保留来源原生掉落数量", player.level().isEmptyBlock(site)
                && player.level().isEmptyBlock(site.above()) && count(player, Blocks.SUNFLOWER.asItem()) == expectedFlowers
                && count(player, item("chongluan").getItem()) == 1);
    }

    private void clearSite(ServerPlayer player) {
        drops(player).forEach(Entity::discard);
        setBlock(player, site.above(), Blocks.AIR.defaultBlockState());
        setBlock(player, site, Blocks.AIR.defaultBlockState());
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(site).inflate(2));
    }

    private int count(ServerPlayer player, Item item) {
        return drops(player).stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        originalBlocks.putIfAbsent(pos, player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 2);
    }

    private static void resetConstitution(ServerPlayer player) {
        Cultivation.editTizhi(player, tag -> {
            tag.putBoolean("tizhiopen_013", false);
            tag.putDouble("AlcoholPatience", 7);
            tag.putDouble("AlcoholDigestionPercentage", 0.25);
            tag.putDouble("tizhinumber", 3);
        });
    }

    private static boolean constitution(Player player) {
        return Cultivation.tizhiBoolean(player, "tizhiopen_013")
                && Cultivation.tizhiDouble(player, "AlcoholPatience") == 107
                && Cultivation.tizhiDouble(player, "AlcoholDigestionPercentage") == -0.75
                && Cultivation.tizhiDouble(player, "tizhinumber") == 4;
    }

    private static void use(ServerPlayer player) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static void useOn(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static MobEffectInstance copy(MobEffectInstance effect) {
        return effect == null ? null : new MobEffectInstance(effect);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        drops(player).forEach(Entity::discard);
        originalBlocks.forEach((pos, state) -> player.level().setBlock(pos, state, 2));
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) {
            player.getInventory().setItem(slot, originalInventory.get(slot));
        }
        Cultivation.editTizhi(player, tag -> {
            tag.putBoolean("tizhiopen_013", originalConstitution);
            tag.putDouble("AlcoholPatience", originalPatience);
            tag.putDouble("AlcoholDigestionPercentage", originalDigestion);
            tag.putDouble("tizhinumber", originalNumber);
        });
        player.removeEffect(MobEffects.LUCK);
        player.removeEffect(MobEffects.UNLUCK);
        if (originalLuck != null) player.addEffect(originalLuck);
        if (originalUnluck != null) player.addEffect(originalUnluck);
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
    }
}
