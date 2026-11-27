package net.yvanchuxiuzhen.smoke;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition;
import net.yvanchuxiuzhen.system.treasure.TreasureRewards;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.FuluReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.IntRange;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.ItemReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.LevelTier;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.MaterialReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.MultiDropReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.RecipeRewardSpec;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.TierContext;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.WeightedEntry;
import net.yvanchuxiuzhen.system.treasure.TreasureRewardTables;
import net.yvanchuxiuzhen.system.treasure.TreasureRewardTables.Selection;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 物品与地面宝箱自然消费、奖励实例和开箱状态的必要生命周期检查。 */
final class TreasureCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> persisted = new ArrayList<>();
    private BlockPos platform;
    private BlockPos ground;
    private BlockPos ordinary;
    private BlockPos locked;
    private BlockPos saved;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private int money;

    TreasureCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("宝箱", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 18) return reopened(local);
        if (step == 19) return inWorld(local);
        if (local < (step == 16 ? 45 : step == 15 || step == 22 ? 110 : 8)) return false;
        switch (step) {
            case 0 -> {
                verifyRewardPools();
                onServer(this::prepare);
            }
            case 1 -> useItem();
            case 2 -> onServer(player -> {
                expect("01物品自然使用只消费一个", 1, player.getMainHandItem().getCount());
                ItemStack reward = takeSingleDrop(player);
                expectTrue("一级宝石表自然产出二级材料161", reward.is(item("cailiao_161").getItem())
                        && Materials.instance(reward).level() == 2);
                player.getInventory().setItem(9, reward);
                stage(player, chestItem("treasure_chest_02", "宝箱", "宝石", 1));
            });
            case 3 -> useItem();
            case 4 -> onServer(player -> {
                expect("02物品立即发放并消费一个", 1, player.getMainHandItem().getCount());
                expectTrue("02物品沿同一奖励表发放材料", takeSingleDrop(player).is(item("cailiao_161").getItem()));
                stageGround(player, "treasure_chest_04");
            });
            case 5 -> use(ground);
            case 6 -> onServer(player -> {
                ItemStack reward = takeSingleDrop(player);
                expectTrue("地面04自然消费并生成可读取功法组件", player.level().isEmptyBlock(ground) && Techniques.book(reward).isPresent());
                player.getInventory().setItem(10, reward);
                stageGround(player, "treasure_chest_05");
            });
            case 7 -> use(ground);
            case 8 -> onServer(player -> {
                ItemStack reward = takeSingleDrop(player);
                expectTrue("地面05自然消费并生成可读取技能组件", player.level().isEmptyBlock(ground) && Skills.book(reward).isPresent());
                player.getInventory().setItem(11, reward);
                stageGround(player, "treasure_chest_06");
            });
            case 9 -> use(ground);
            case 10 -> onServer(player -> {
                money = player.getInventory().countItem(item("money_01").getItem());
                expectTrue("地面06移除且实际给予一至二十通宝", player.level().isEmptyBlock(ground) && money >= 1 && money <= 20);
                player.getInventory().setItem(12, player.getInventory().removeItemNoUpdate(0));
                stage(player, ItemStack.EMPTY);
            });
            case 11 -> {
                use(locked);
                lookAt(locked);
            }
            case 12 -> {
                onServer(player -> {
                    expectTrue("无钥匙自然右键保持上锁未打开", !chest(player, locked).opened());
                    ItemStack keys = item("treasure_chest_1101").copyWithCount(2);
                    keys.set(DataComponents.CUSTOM_NAME, Component.literal("藏阁钥匙"));
                    stage(player, keys);
                });
                screenshots.take("treasure_locked.png");
            }
            case 13 -> use(locked);
            case 14 -> {
                onServer(player -> {
                    TreasureChestBlockEntity chest = chest(player, locked);
                    TreasureContainerConfig config = chest.getData(TreasureData.BLOCK_CONTAINER);
                    expect("钥匙自然开箱消耗一个", 1, player.getMainHandItem().getCount());
                    expectTrue("锁箱自然初始化类别等级及数量", chest.opened()
                            && !config.category().isEmpty() && !config.subtype().isEmpty()
                            && config.level() >= 1 && config.level() <= 8
                            && config.minimum() == 1 && config.maximum() >= 1);
                    CompoundTag serialized = chest.saveWithFullMetadata(player.registryAccess());
                    TreasureChestBlockEntity restored = new TreasureChestBlockEntity(locked, chest.getBlockState());
                    restored.loadWithComponents(serialized, player.registryAccess());
                    expect("开箱中打开标志真实序列化重读", chest.opened(), restored.opened());
                    expect("开箱中计时真实序列化重读", chest.tickCount(), restored.tickCount());
                    expect("开箱中状态真实序列化重读", chest.openState(), restored.openState());
                    expect("开箱中容器配置真实序列化重读", config, restored.getData(TreasureData.BLOCK_CONTAINER));
                    clearDrops(player);
                });
            }
            case 15 -> {
                onServer(player -> {
                    expectTrue("11首次自然开箱按第五次计划刻移除", player.level().isEmptyBlock(locked));
                    clearDrops(player);
                    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                        if (slot != 0 && (slot < 9 || slot > 12)) player.getInventory().setItem(slot, ItemStack.EMPTY);
                    }
                });
                use(ordinary);
                lookAt(ordinary);
            }
            case 16 -> {
                onServer(player -> {
                    TreasureChestBlockEntity chest = chest(player, ordinary);
                    expectTrue("03二十刻计划刻推进至发奖状态", chest != null && chest.tickCount() >= 2
                            && chest.openState().equals("爆物品"));
                    int groundCount = drops(player).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
                    int pickedCount = 0;
                    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                        if (slot == 0 || (slot >= 9 && slot <= 12)) continue;
                        pickedCount += player.getInventory().getItem(slot).getCount();
                    }
                    int count = groundCount + pickedCount;
                    expectTrue("03自然开箱发放二至五件来源材料（地面=" + groundCount + "，拾取=" + pickedCount + "）",
                            count >= 2 && count <= 5);
                    clearDrops(player);
                    chest(player, saved).setItem(0, new ItemStack(Items.DIAMOND));
                    chest(player, saved).sync();
                    for (int slot : List.of(9, 10, 11)) persisted.add(player.getInventory().getItem(slot).copy());
                });
                screenshots.take("treasure_open.png");
            }
            case 17 -> leaveWorld();
            case 20 -> onServer(player -> {
                expectTrue("正常保存重进后已开两箱按第五次计划刻移除", player.level().isEmptyBlock(ordinary) && player.level().isEmptyBlock(locked));
                TreasureChestBlockEntity chest = chest(player, saved);
                expectTrue("正常重进保留未开锁箱和唯一真实库存", chest != null
                        && !chest.opened() && chest.getItem(0).is(Items.DIAMOND));
                for (int index = 0; index < persisted.size(); index++) {
                    expectTrue("奖励实例保存重进槽" + (9 + index), ItemStack.matches(persisted.get(index), player.getInventory().getItem(9 + index)));
                }
                expect("通宝保存重进", money, player.getInventory().countItem(item("money_01").getItem()));
                expect("重进保留剩余钥匙", 1, player.getMainHandItem().getCount());
                expect("重进保留钥匙自定义名称", "藏阁钥匙", player.getMainHandItem().getHoverName().getString());
                clearDrops(player);
            });
            case 21 -> use(saved);
            case 22 -> {
                onServer(player -> {
                    expectTrue("重进后锁箱仍可自然开箱并结束", player.level().isEmptyBlock(saved));
                    expect("第二次锁箱消耗最后一枚钥匙", 0,
                            player.getInventory().countItem(item("treasure_chest_1101").getItem()));
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知宝箱检查步骤 " + step);
        }
        return true;
    }

    private void verifyRewardPools() {
        List<TreasurePoolDefinition> pools = TreasureRewardTables.pools();
        expect("JSON 奖励池总数", 29, pools.size());
        expect("JSON 分派总数", 24, pools.stream().mapToInt(pool -> pool.dispatches().size()).sum());
        for (TreasurePoolDefinition pool : pools) {
            JsonElement encoded = TreasurePoolDefinition.CODEC.encodeStart(JsonOps.INSTANCE, pool).getOrThrow();
            TreasurePoolDefinition decoded = TreasurePoolDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            expect("奖励池 Codec 往返一致：" + pool.id(), pool, decoded);
        }
        RandomSource random = RandomSource.create(20261125L);
        for (TreasurePoolDefinition pool : pools) {
            for (TreasurePoolDefinition.DispatchKey key : pool.dispatches()) {
                TreasureRewardTables.select(key.category(), key.subtype(), 1.0, random);
            }
        }
        expectTrue("未知分类与子类返回空", TreasureRewardTables.select("未知大类", "不存在", 1.0, random).isEmpty());
        expectTrue("一级宝石表解析强类型材料奖励", TreasureRewardTables.select("宝箱", "宝石", 1.0, random)
                .map(list -> list.getFirst().reward() instanceof MaterialReward material
                        && material.item().equals(id("cailiao_161")))
                .orElse(false));
        expectTrue("一级丹方表解析强类型丹方奖励", TreasureRewardTables.select("书架", "丹方", 1.0, random)
                .map(list -> list.getFirst().reward() instanceof RecipeRewardSpec)
                .orElse(false));
        Optional<List<Selection>> fulu1 = TreasureRewardTables.select("书架", "符箓堆", 1.0, random);
        expectTrue("一级符箓表注入黄阶上下文", fulu1.map(list -> list.getFirst().reward() instanceof FuluReward
                && list.getFirst().context().fuluLevel().equals(Optional.of(1))
                && list.getFirst().context().fuluTierText().equals(Optional.of("黄阶"))).orElse(false));
        Optional<List<Selection>> fulu9 = TreasureRewardTables.select("书架", "符箓堆", 9.0, random);
        expect("八级符箓保留天阶", Optional.of("天阶"), TreasureRewardTables.select("书架", "符箓堆", 8.0, random)
                .flatMap(list -> list.getFirst().context().fuluTierText()));
        expect("八级半符箓仍能抽出且等阶为空", Optional.of(""), TreasureRewardTables.select("书架", "符箓堆", 8.5, random)
                .filter(list -> list.getFirst().reward() instanceof FuluReward).flatMap(list -> list.getFirst().context().fuluTierText()));
        expectTrue("九级符箓表保留空等阶回退", fulu9.map(list -> list.getFirst().context().fuluLevel().equals(Optional.of(0))).orElse(false));
        expectTrue("七级法宝表保留空分支语义", TreasureRewardTables.select("武器架", "法宝架", 7.0, random).isEmpty());

        TreasurePoolDefinition bundle = new TreasurePoolDefinition("custom_bundle", List.of(), List.of(new LevelTier(
                Optional.empty(), Optional.empty(), Optional.of(TierContext.EMPTY),
                List.of(new WeightedEntry(1, new MultiDropReward(List.of(
                        new ItemReward(id("money_01"), Optional.of(new IntRange(2, 5)), Optional.of("测试红包")),
                        new ItemReward(id("cailiao_191"), Optional.empty(), Optional.empty())
                )))))));
        TreasurePoolDefinition decodedBundle = TreasurePoolDefinition.CODEC.parse(JsonOps.INSTANCE,
                TreasurePoolDefinition.CODEC.encodeStart(JsonOps.INSTANCE, bundle).getOrThrow()).getOrThrow();
        List<Selection> bundleDrops = TreasureRewardTables.samplePool(decodedBundle, 1.0, random).orElseThrow();
        expect("多物品组合抽样数量", 2, bundleDrops.size());
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        platform = player.blockPosition().above(8);
        ground = platform.south(2).above();
        ordinary = platform.north(2).above();
        locked = platform.east(2).above();
        saved = platform.west(2).above();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                player.level().setBlock(platform.offset(dx, 0, dz), Blocks.STONE.defaultBlockState(), 3);
                for (int dy = 1; dy <= 3; dy++) player.level().setBlock(platform.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        minecraft.options.keyShift.setDown(false);
        player.getInventory().clearContent();
        player.connection.teleport(platform.getX() + .5, platform.getY() + 1, platform.getZ() + .5, 90, 15);
        player.level().setBlock(ordinary, block("treasure_chest_03"), 3);
        verifyLeggingDrops(player);
        verifyTier3MonsterMaterials(player);
        player.level().setBlock(locked, block("treasure_chest_11"), 3);
        player.level().setBlock(saved, block("treasure_chest_11"), 3);
        stage(player, chestItem("treasure_chest_01", "宝箱", "宝石", 1));
    }

    private void verifyLeggingDrops(ServerPlayer player) {
        var pool = TreasureRewardTables.pools().stream().filter(p -> p.id().equals("hujia")).findFirst().orElseThrow();
        var level = player.serverLevel();
        var source = level.getServer().createCommandSourceStack().withLevel(level).withPosition(Vec3.atCenterOf(ordinary));
        for (int grade = 1; grade <= 8; grade++) {
            var tier = pool.tiers().get(grade - 1);
            var leggings = (TreasurePoolDefinition.CommandReward) tier.entries().get(2).reward();
            level.getServer().getCommands().performPrefixedCommand(source, leggings.command());
            ItemStack drop = takeSingleDrop(player);
            expectTrue("盔甲架第" + grade + "档护腿掉落并携带成品组件", drop.is(item("armor_0" + grade + "_leggings").getItem())
                    && Equipment.product(drop).filter(product -> product.type().equals("裤子")).isPresent());
        }
    }

    private void verifyTier3MonsterMaterials(ServerPlayer player) {
        var pool = TreasureRewardTables.pools().stream().filter(p -> p.id().equals("yaoshoucailiao")).findFirst().orElseThrow();
        var tier3 = pool.tiers().stream().filter(t -> t.minLevel().orElse(0.0) == 3.0).findFirst().orElseThrow();
        expect("Q177 三级妖兽材料移除空签后条目数", 21, tier3.entries().size());
        expectTrue("Q177 三级妖兽材料不含未注册 cailiao_153", tier3.entries().stream()
                .noneMatch(entry -> entry.reward() instanceof TreasurePoolDefinition.MaterialReward m
                        && m.item().getPath().equals("cailiao_153")));
        expectTrue("Q177 三级妖兽材料其余 21 签均已注册", tier3.entries().stream()
                .allMatch(entry -> entry.reward() instanceof TreasurePoolDefinition.MaterialReward m
                        && BuiltInRegistries.ITEM.get(m.item()) != Items.AIR));
        var level = player.serverLevel();
        TreasureRewards.open(level, Vec3.atCenterOf(ordinary), "宝箱", "妖兽材料", 3.0, 1, 1);
        expectTrue("Q177 三级妖兽材料实开掉落非空", !takeSingleDrop(player).isEmpty());
    }

    private void stageGround(ServerPlayer player, String name) {
        stage(player, ItemStack.EMPTY);
        player.level().setBlock(ground, block(name), 3);
    }

    private void stage(ServerPlayer player, ItemStack stack) {
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.inventoryMenu.broadcastChanges();
    }

    private void useItem() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void use(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private void lookAt(BlockPos pos) {
        Vec3 delta = Vec3.atCenterOf(pos).subtract(minecraft.player.getEyePosition());
        minecraft.player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90);
        minecraft.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(platform).inflate(6));
    }

    private ItemStack takeSingleDrop(ServerPlayer player) {
        List<ItemEntity> drops = drops(player);
        expect("单件奖励实际掉落实体数", 1, drops.size());
        ItemStack result = drops.size() == 1 ? drops.getFirst().getItem().copy() : ItemStack.EMPTY;
        drops.forEach(ItemEntity::discard);
        return result;
    }

    private void clearDrops(ServerPlayer player) {
        drops(player).forEach(ItemEntity::discard);
    }

    private static TreasureChestBlockEntity chest(ServerPlayer player, BlockPos pos) {
        return player.level().getBlockEntity(pos) instanceof TreasureChestBlockEntity chest ? chest : null;
    }

    private static ItemStack chestItem(String name, String category, String subtype, int level) {
        ItemStack stack = TreasureContainers.create(item(name).getItem(), category, subtype, level, 1, 1);
        stack.setCount(2);
        return stack;
    }

    private static ItemStack item(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(name)));
    }

    private static BlockState block(String name) {
        return BuiltInRegistries.BLOCK.get(id(name)).defaultBlockState();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name);
    }

    private void cleanup(ServerPlayer player) {
        clearDrops(player);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
    }
}
