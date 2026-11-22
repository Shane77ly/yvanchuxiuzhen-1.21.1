package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 受控璞玉雕琢、奇遇与客户端使用后的保存重进，不代表自然采矿。 */
final class JadeCarvingCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final Consumer<EntityJoinLevelEvent> listener = this::capture;
    private final List<Entity> spawned = new ArrayList<>();
    private ListTag inventory;
    private ListTag carvedInventory;
    private MaterialInstance carvedInstance;
    private GameType mode;
    private int selected;
    private double chance;
    private double number;
    private double soil;
    private boolean shiftKey;
    private boolean capturing;
    private boolean prepared;
    private boolean listening;

    JadeCarvingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("璞玉雕琢与奇遇", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            fail("步骤 " + step + " 异常：" + exception);
            if (prepared && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::restore);
            minecraft.options.keyShift.setDown(shiftKey);
            finish();
            return false;
        }
    }

    private boolean advance(int step, int local) {
        if (step == 5) {
            if (minecraft.screen instanceof TitleScreen && local >= 20) return reopened(local);
            if (local > 900) abort("退出后未到标题界面");
            return false;
        }
        if (step == 6) return inWorld(local);
        if (local < 10) return false;
        switch (step) {
            case 0 -> {
                shiftKey = minecraft.options.keyShift.isDown();
                minecraft.options.keyShift.setDown(false);
                onServer(player -> {
                    prepare(player);
                    checkFamily(player);
                    checkFortunes(player);
                    checkRewards(player);
                    checkFullInventoryDrops(player);
                    prepareClient(player);
                });
            }
            case 1 -> {
                minecraft.player.getInventory().selected = 0;
                if (!minecraft.player.getMainHandItem().is(item("cailiao_16202"))) return await(local, "璞玉未同步到客户端");
                expect("客户端璞玉中文名称", "珲黄玉璞玉", minecraft.player.getMainHandItem().getHoverName().getString());
                expect("客户端正常右键返回来源 PASS", InteractionResult.PASS,
                        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND));
            }
            case 2 -> {
                ItemStack output = find(minecraft.player.getInventory().items, item("cailiao_162"));
                if (output.isEmpty()) return await(local, "雕琢结果未同步到客户端");
                expect("客户端成玉中文名称", "珲黄玉", output.getHoverName().getString());
                expect("客户端成玉奇遇等级", 40, Materials.instance(output).level());
                onServer(player -> {
                    ItemStack actual = find(player.getInventory().items, item("cailiao_162"));
                    expect("服务端原璞玉剩余一件", 1, player.getMainHandItem().getCount());
                    expect("客户端与服务端材料实例一致", Materials.instance(output), Materials.instance(actual));
                    carvedInstance = Materials.instance(actual);
                    carvedInventory = player.getInventory().save(new ListTag());
                });
                minecraft.setScreen(new InventoryScreen(minecraft.player));
            }
            case 3 -> {
                if (!(minecraft.screen instanceof InventoryScreen)) return await(local, "无法打开雕琢结果背包");
                screenshots.take("jade_carving_inventory.png");
                minecraft.player.closeContainer();
            }
            case 4 -> leaveWorld();
            case 7 -> {
                onServer(player -> {
                    expectTrue("保存重进保留雕琢后的库存", carvedInventory.equals(player.getInventory().save(new ListTag())));
                    ItemStack output = find(player.getInventory().items, item("cailiao_162"));
                    expect("保存重进保留材料实例", carvedInstance, Materials.instance(output));
                    expect("保存重进仍有一件璞玉", 1, player.getInventory().countItem(item("cailiao_16202")));
                    restore(player);
                });
                minecraft.options.keyShift.setDown(shiftKey);
                finish();
            }
            default -> throw new IllegalStateException("未知步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        inventory = player.getInventory().save(new ListTag());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        chance = Cultivation.tizhiDouble(player, "jiyvgailv");
        number = Cultivation.tizhiDouble(player, "qiyvbianhao");
        soil = Cultivation.tizhiDouble(player, "killentitypointssoil");
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().selected = 0;
        player.setShiftKeyDown(false);
        NeoForge.EVENT_BUS.addListener(listener);
        listening = true;
        prepared = true;
    }

    private void checkFamily(ServerPlayer player) {
        int[] levels = {2, 7, 12, 17, 22, 27, 32, 37, 40};
        for (int jade = 161; jade <= 169; jade++) {
            reset(player, jade, 2, 0, 0, 0);
            use(player, false);
            ItemStack output = find(player.getInventory().items, item("cailiao_" + jade));
            expect(jade + " 普通右键扣一件", 1, player.getMainHandItem().getCount());
            expect(jade + " 普通右键成玉数量", 1, player.getInventory().countItem(item("cailiao_" + jade)));
            expect(jade + " 普通雕琢基础等级", levels[jade - 161], Materials.instance(output).level());
            expectTrue(jade + " 随机品阶在来源范围内", qualityInRange(output));
            expectTrue(jade + " 未绑定璞玉刷新奇遇字段", validFortune(player));

            reset(player, jade, 64, 6, 97, 4);
            use(player, true);
            expect(jade + " 潜行整组消耗", 0, player.getInventory().countItem(item("cailiao_" + jade + "02")));
            expect(jade + " 潜行整组逐件产出", 64, player.getInventory().countItem(item("cailiao_" + jade)));
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(item("cailiao_" + jade))) {
                    expect(jade + " 批量基础等级", levels[jade - 161], Materials.instance(stack).level());
                    expectTrue(jade + " 批量品阶在来源范围内", qualityInRange(stack));
                }
            }
            expect(jade + " 潜行不刷新奇遇概率", 97, Cultivation.tizhiDouble(player, "jiyvgailv"));
            expect(jade + " 潜行不刷新奇遇编号", 4, Cultivation.tizhiDouble(player, "qiyvbianhao"));
        }
    }

    private void checkFortunes(ServerPlayer player) {
        for (int fortune = 1; fortune <= 8; fortune++) {
            reset(player, 161, 2, fortune, 100, 99);
            use(player, false);
            ItemStack output = find(player.getInventory().items, item("cailiao_161"));
            int expected = switch (fortune) {
                case 1 -> 4;
                case 2 -> 7;
                case 3 -> 12;
                case 4 -> 40;
                case 7 -> 1;
                default -> 2;
            };
            expect("奇遇 " + fortune + " 成玉等级", expected, Materials.instance(output).level());
            if (fortune == 5) expectTrue("奇遇5提高品阶并封顶", Materials.instance(output).quality() >= 1 && qualityInRange(output));
            if (fortune == 6) expect("奇遇6品阶为4", 4, Materials.instance(output).quality());
            if (fortune == 8) expect("奇遇8品阶为0", 0, Materials.instance(output).quality());
            expect("物品奇遇优先且不刷新概率", 100, Cultivation.tizhiDouble(player, "jiyvgailv"));
            expect("物品奇遇优先且不刷新体质编号", 99, Cultivation.tizhiDouble(player, "qiyvbianhao"));
            expect("物品奇遇不触发体质功法掉落", 0, spawned.size());
        }
        reset(player, 168, 2, 3, 0, 0);
        use(player, false);
        expect("奇遇加级封顶40", 40, Materials.instance(find(player.getInventory().items, item("cailiao_168"))).level());
        reset(player, 161, 2, 0, 94, 4);
        use(player, false);
        expect("无物品编号时使用体质奇遇", 40, Materials.instance(find(player.getInventory().items, item("cailiao_161"))).level());
        expectTrue("体质奇遇后重新生成合法状态", validFortune(player));
    }

    private void checkRewards(ServerPlayer player) {
        checkItemReward(player, 9, "cailiao_021", true);
        checkItemReward(player, 25, "cailiao_060", true);
        checkItemReward(player, 65, "cailiao_138", true);
        checkItemReward(player, 66, "minecraft:diamond", false);
        checkItemReward(player, 74, "minecraft:raw_gold", false);
        checkItemReward(player, 75, "cailiao_201raw", false);
        checkItemReward(player, 86, "cailiao_219raw", false);
        checkItemReward(player, 87, "minecraft:obsidian", false);
        checkItemReward(player, 97, "minecraft:bone_meal", false);
        for (int fortune = 98; fortune <= 100; fortune++) checkBook(player, fortune);
        reset(player, 161, 2, 101, 0, 0);
        use(player, false);
        expect("奇遇101不生成额外实体", 0, spawned.size());
        checkSpawn(player, 102, "minecraft:silverfish", 3, false);
        checkSpawn(player, 103, "yvanchuxiuzhen:yaoshou_045", 1, true);
        checkSpawn(player, 104, "yvanchuxiuzhen:yaoshou_041", 1, true);
        checkSpawn(player, 105, "yvanchuxiuzhen:guchong_04", 1, true);
        Cultivation.setTizhiDouble(player, "killentitypointssoil", 7);
        int[] totals = {17, 37, 77, 157};
        for (int fortune = 106; fortune <= 109; fortune++) {
            reset(player, 161, 2, fortune, 0, 0);
            use(player, false);
            expect("奇遇 " + fortune + " 土属性熟练度", totals[fortune - 106], Cultivation.tizhiDouble(player, "killentitypointssoil"));
        }
        discardSpawned();
    }

    /** 满背包时先交付再扣璞玉；只检查本次使用同步生成的实体。 */
    private void checkFullInventoryDrops(ServerPlayer player) {
        try {
            fillInventory(player, 1, 0, 0, 0);
            use(player, false);
            List<ItemEntity> drops = capturedDrops();
            expect("满包普通末枚生成实体总数", 1, spawned.size());
            expect("满包普通末枚掉落成玉", 1, drops.size());
            expectTrue("满包普通末枚扣除后手中为空", player.getMainHandItem().isEmpty());
            expectTrue("满包普通末枚其余主背包不变", filledSlotsIntact(player));
            expectTrue("满包普通末枚刷新体质奇遇", validFortune(player));
            expectTrue("满包普通成玉身份等级品阶", drops.size() == 1 && jadeDropsValid(drops, 2));
            expectTrue("满包普通成玉实际落地及拾取延迟40刻", helperDropsValid(player, drops, 1));
            discardSpawned();

            fillInventory(player, 2, 4, 0, 0);
            use(player, false);
            drops = capturedDrops();
            expect("满包普通余栈生成实体总数", 1, spawned.size());
            expect("满包普通余栈璞玉剩余", 1, player.getMainHandItem().getCount());
            expectTrue("满包普通余栈其余主背包不变", filledSlotsIntact(player));
            expectTrue("满包普通余栈奇遇成玉等级40并实际落地", helperDropsValid(player, drops, 1)
                    && jadeDropsValid(drops, 40));
            discardSpawned();

            fillInventory(player, 64, 6, 97, 4);
            use(player, true);
            drops = capturedDrops();
            expect("满包潜行整组逐件生成实体", 64, spawned.size());
            expect("满包潜行整组成玉掉落实体", 64, drops.size());
            expectTrue("满包潜行整组扣除后手中为空", player.getMainHandItem().isEmpty());
            expectTrue("满包潜行整组其余主背包不变", filledSlotsIntact(player));
            expect("满包潜行不刷新奇遇概率", 97, Cultivation.tizhiDouble(player, "jiyvgailv"));
            expect("满包潜行不刷新奇遇编号", 4, Cultivation.tizhiDouble(player, "qiyvbianhao"));
            expectTrue("满包潜行逐件成玉保留基础等级与品阶", drops.size() == 64 && jadeDropsValid(drops, 2));
            expectTrue("满包潜行逐件实体已入世界及拾取延迟40刻", helperDropsValid(player, drops, 64));
            discardSpawned();

            checkFullInventoryReward(player, 9, "cailiao_021", true);
            checkFullInventoryReward(player, 66, "minecraft:diamond", false);
            checkFullInventoryReward(player, 97, "minecraft:bone_meal", false);
            checkFullInventoryBook(player);
        } finally {
            discardSpawned();
        }
    }

    private void checkFullInventoryReward(ServerPlayer player, int fortune, String id, boolean material) {
        fillInventory(player, 1, fortune, 100, 4);
        use(player, false);
        List<ItemEntity> drops = capturedDrops();
        List<ItemEntity> jade = drops.stream().filter(drop -> drop.getItem().is(item("cailiao_161"))).toList();
        List<ItemEntity> reward = drops.stream().filter(drop -> drop.getItem().is(item(id))).toList();
        String label = "满包奇遇" + fortune;
        expect(label + "生成实体总数", 2, spawned.size());
        expect(label + "成玉与奖励各一件", 1, jade.size());
        expect(label + "奖励实体数", 1, reward.size());
        expectTrue(label + "所有掉落实际入世界及延迟40刻", helperDropsValid(player, drops, 2));
        expectTrue(label + "成玉实例保留", jade.size() == 1 && jadeDropsValid(jade, 2));
        expectTrue(label + "奖励数量一件", reward.size() == 1 && reward.getFirst().getItem().getCount() == 1);
        if (material) {
            expectTrue(label + "奖励材料等级和品阶", reward.size() == 1
                    && Materials.instance(reward.getFirst().getItem()).level() >= 1
                    && Materials.instance(reward.getFirst().getItem()).level() <= 40
                    && qualityInRange(reward.getFirst().getItem()));
        }
        expectTrue(label + "末枚璞玉消耗且其余主背包不变", player.getMainHandItem().isEmpty() && filledSlotsIntact(player));
        expect(label + "物品编号不刷新体质概率", 100, Cultivation.tizhiDouble(player, "jiyvgailv"));
        expect(label + "物品编号不刷新体质奇遇", 4, Cultivation.tizhiDouble(player, "qiyvbianhao"));
        discardSpawned();
    }

    private void checkFullInventoryBook(ServerPlayer player) {
        fillInventory(player, 1, 98, 0, 0);
        use(player, false);
        List<ItemEntity> drops = capturedDrops();
        List<ItemEntity> jade = drops.stream().filter(drop -> drop.getItem().is(item("cailiao_161"))).toList();
        List<ItemEntity> books = drops.stream().filter(drop -> Techniques.book(drop.getItem()).isPresent()).toList();
        expect("满包奇遇98生成实体总数", 2, spawned.size());
        expect("满包奇遇98成玉实体", 1, jade.size());
        expect("满包奇遇98功法实体", 1, books.size());
        expectTrue("满包奇遇98成玉实际落地及延迟40刻", helperDropsValid(player, jade, 1) && jadeDropsValid(jade, 2));
        expectTrue("满包奇遇98功法按命令在玩家位置落地并延迟10刻", books.size() == 1
                && inWorld(player, books.getFirst()) && pickupDelay(books.getFirst()) == 10
                && atPosition(player, books.getFirst(), 0)
                && Techniques.book(books.getFirst().getItem()).orElseThrow().definition().name().equals("尘丰诀"));
        expectTrue("满包奇遇98末枚璞玉消耗且其余主背包不变", player.getMainHandItem().isEmpty() && filledSlotsIntact(player));
        discardSpawned();
    }

    private void fillInventory(ServerPlayer player, int count, int fortune, double chance, double number) {
        reset(player, 161, count, fortune, chance, number);
        for (int slot = 1; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
    }

    private static boolean filledSlotsIntact(ServerPlayer player) {
        for (int slot = 1; slot < player.getInventory().items.size(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(Items.COBBLESTONE) || stack.getCount() != 64) return false;
        }
        return true;
    }

    private List<ItemEntity> capturedDrops() {
        return spawned.stream().filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast).toList();
    }

    private static boolean jadeDropsValid(List<ItemEntity> drops, int level) {
        return drops.stream().allMatch(drop -> drop.getItem().is(item("cailiao_161"))
                && drop.getItem().getCount() == 1 && Materials.instance(drop.getItem()).level() == level
                && qualityInRange(drop.getItem()));
    }

    private static boolean helperDropsValid(ServerPlayer player, List<ItemEntity> drops, int count) {
        return drops.size() == count && drops.stream().allMatch(drop -> inWorld(player, drop)
                && drop.getItem().getCount() == 1 && pickupDelay(drop) == 40 && atPosition(player, drop, 0.5)
                && drop.getDeltaMovement().x == 0 && drop.getDeltaMovement().z == 0);
    }

    private static boolean inWorld(ServerPlayer player, ItemEntity drop) {
        return !drop.isRemoved() && player.serverLevel().getEntity(drop.getUUID()) == drop;
    }

    private static int pickupDelay(ItemEntity drop) {
        return drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay");
    }

    private static boolean atPosition(ServerPlayer player, ItemEntity drop, double yOffset) {
        return Math.abs(drop.getX() - player.getX()) < 1e-6
                && Math.abs(drop.getY() - player.getY() - yOffset) < 1e-6
                && Math.abs(drop.getZ() - player.getZ()) < 1e-6;
    }

    private void checkItemReward(ServerPlayer player, int fortune, String id, boolean material) {
        reset(player, 161, 2, fortune, 0, 0);
        use(player, false);
        Item reward = item(id);
        ItemStack stack = find(player.getInventory().items, reward);
        expect("奇遇 " + fortune + " 奖励数量", 1, player.getInventory().countItem(reward));
        if (material) {
            expectTrue("奇遇 " + fortune + " 材料等级在1至40", Materials.instance(stack).level() >= 1 && Materials.instance(stack).level() <= 40);
            expectTrue("奇遇 " + fortune + " 材料品阶在0至4", qualityInRange(stack));
        }
        expect("奇遇 " + fortune + " 同时产出一件玉石", 1, player.getInventory().countItem(item("cailiao_161")));
        if (fortune == 97) expect("奇遇97先进入骨粉分支", 0, spawned.size());
    }

    private void checkBook(ServerPlayer player, int fortune) {
        reset(player, 161, 2, fortune, 0, 0);
        use(player, false);
        List<ItemEntity> books = spawned.stream().filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast).toList();
        expect("奇遇 " + fortune + " 生成一本功法书", 1, books.size());
        if (!books.isEmpty()) {
            ItemStack stack = books.getFirst().getItem();
            TechniqueBook book = Techniques.book(stack).orElse(null);
            expectTrue("奇遇 " + fortune + " 带功法实例和拾取延迟", book != null && books.getFirst().hasPickUpDelay());
            if (book != null) {
                String[] names = {"尘丰诀", "重峦真经", "擎山震地功"};
                expect("奇遇 " + fortune + " 功法名称", names[fortune - 98], book.definition().name());
                expect("奇遇 " + fortune + " 功法品阶", 13 + (fortune - 98) * 10, book.definition().grade());
                expect("奇遇 " + fortune + " 土系数", fortune - 96, book.definition().element("soil"));
                expect("来源命令不写移速档", "", book.definition().tiers().get(12));
            }
        }
        discardSpawned();
    }

    private void checkSpawn(ServerPlayer player, int fortune, String id, int count, boolean hasLevel) {
        reset(player, 161, 2, fortune, 0, 0);
        use(player, false);
        List<Entity> matching = spawned.stream().filter(entity -> id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString())).toList();
        expect("奇遇 " + fortune + " 出生数量", count, matching.size());
        for (Entity entity : matching) {
            if (hasLevel) {
                double level = entity.getPersistentData().getDouble("LEVELnumber");
                expectTrue("奇遇 " + fortune + " 出生等级在1至35", level >= 1 && level <= 35);
            }
        }
        discardSpawned();
    }

    private void reset(ServerPlayer player, int jade, int count, int fortune, double chance, double number) {
        discardSpawned();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        ItemStack input = new ItemStack(item("cailiao_" + jade + "02"), count);
        if (fortune > 0) CustomData.update(DataComponents.CUSTOM_DATA, input, tag -> tag.putDouble("qiyvbianhao", fortune));
        player.setItemInHand(InteractionHand.MAIN_HAND, input);
        Cultivation.setTizhiDouble(player, "jiyvgailv", chance);
        Cultivation.setTizhiDouble(player, "qiyvbianhao", number);
    }

    private void use(ServerPlayer player, boolean sneaking) {
        player.setShiftKeyDown(sneaking);
        capturing = true;
        try {
            player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        } finally {
            capturing = false;
            player.setShiftKeyDown(false);
        }
    }

    private void capture(EntityJoinLevelEvent event) {
        if (!capturing || event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob) mob.setNoAi(true);
        spawned.add(entity);
    }

    private void discardSpawned() {
        for (Entity entity : spawned) entity.discard();
        spawned.clear();
    }

    private void prepareClient(ServerPlayer player) {
        reset(player, 162, 2, 4, 0, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private void restore(ServerPlayer player) {
        if (listening) NeoForge.EVENT_BUS.unregister(listener);
        listening = false;
        discardSpawned();
        player.getInventory().clearContent();
        player.getInventory().load(inventory);
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(false);
        Cultivation.setTizhiDouble(player, "jiyvgailv", chance);
        Cultivation.setTizhiDouble(player, "qiyvbianhao", number);
        Cultivation.setTizhiDouble(player, "killentitypointssoil", soil);
        player.inventoryMenu.broadcastChanges();
        prepared = false;
    }

    private boolean await(int local, String reason) {
        if (local > 300) abort(reason);
        return false;
    }

    private void abort(String reason) {
        fail(reason);
        if (prepared && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::restore);
        minecraft.options.keyShift.setDown(shiftKey);
        finish();
    }

    private static boolean validFortune(ServerPlayer player) {
        double chance = Cultivation.tizhiDouble(player, "jiyvgailv");
        double number = Cultivation.tizhiDouble(player, "qiyvbianhao");
        return chance >= 1 && chance <= 100 && number >= 1 && number <= 109;
    }

    private static boolean qualityInRange(ItemStack stack) {
        return !stack.isEmpty() && Materials.instance(stack).quality() >= 0 && Materials.instance(stack).quality() <= 4;
    }

    private static ItemStack find(List<ItemStack> inventory, Item item) {
        return inventory.stream().filter(stack -> stack.is(item)).findFirst().orElse(ItemStack.EMPTY);
    }

    private static Item item(String id) {
        ResourceLocation key = id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id);
        Item item = BuiltInRegistries.ITEM.get(key);
        if (item == Items.AIR) throw new IllegalStateException("未注册的测试物品：" + key);
        return item;
    }
}
