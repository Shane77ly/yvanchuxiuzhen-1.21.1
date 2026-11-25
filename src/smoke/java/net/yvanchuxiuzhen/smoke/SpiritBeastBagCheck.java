package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.SpiritBeastBagRuntime;
import net.yvanchuxiuzhen.system.creature.client.SpiritBeastBagDyeScreen;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenu;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastDyeToolInventory;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;
import net.yvanchuxiuzhen.system.creature.item.SpiritBeastBagItem;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import org.lwjgl.glfw.GLFW;

/** 灵兽袋原生收放、染色菜单扣料、死亡清袋与同存档重进的必要检查。 */
final class SpiritBeastBagCheck extends SystemCheck {
    private static final List<Item> DYES = List.of(Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE,
            Items.LIGHT_BLUE_DYE, Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
            Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE,
            Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE);
    private static final int COLOR_START = 6;
    private static final int AFTER_COLORS = COLOR_START + 16 * 6;
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> entities = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final CompoundTag expectedFields = specimenFields();
    private int executed = -1;
    private Vec3 originalPosition;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private Difficulty difficulty;
    private boolean shift;
    private BlockPos floor;
    private YaoshouEntity active;
    private UUID activeUuid;
    private String bagUuid;
    private ItemStack dyeBag;
    private ItemStack savedBag;
    private ItemStack savedMainTool;
    private ItemStack savedOffTool;
    private ItemStack unrelatedBag;
    private ItemStack duplicateBag;

    SpiritBeastBagCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("灵兽袋", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step >= COLOR_START && step < AFTER_COLORS) {
            return color((step - COLOR_START) / 6, (step - COLOR_START) % 6, step, local);
        }
        return switch (step) {
            case 0 -> action(step, local, 8, this::prepare);
            case 1 -> action(step, local, 5, this::captureCases);
            case 2 -> action(step, local, 8, this::releaseCases);
            case 3 -> action(step, local, 8, this::recapture);
            case 4 -> action(step, local, 8, player -> openTool(player, InteractionHand.MAIN_HAND));
            case 5 -> requireMenu(local, "主手工具原生打开中文染色菜单");
            case AFTER_COLORS -> action(step, local, 8, this::storeMainAndOpenOffhand);
            case AFTER_COLORS + 1 -> requireMenu(local, "副手工具原生打开独立染色菜单");
            case AFTER_COLORS + 2 -> action(step, local, 8, this::prepareReload);
            case AFTER_COLORS + 3 -> { leaveWorld(); yield true; }
            case AFTER_COLORS + 4 -> reopened(local);
            case AFTER_COLORS + 5 -> inWorld(local);
            case AFTER_COLORS + 6 -> action(step, local, 8, this::verifyReloadAndDeath);
            case AFTER_COLORS + 7 -> action(step, local, 8, player -> {
                player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().getItem(21));
                player.getInventory().setItem(21, ItemStack.EMPTY);
                openTool(player, InteractionHand.MAIN_HAND);
            });
            case AFTER_COLORS + 8 -> requireMenu(local, "真实重进后工具三槽再次开窗同步");
            case AFTER_COLORS + 9 -> {
                if (local < 8) yield false;
                screenshots.take("spirit-beast-bag-rejoined-tool.png");
                onServer(this::cleanup);
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知灵兽袋检查步骤 " + step);
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
        if (!(item("lingshoudai_01").getItem() instanceof SpiritBeastBagItem)) {
            throw new IllegalStateException("灵兽袋专属物品工厂未接入");
        }
        originalPosition = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        difficulty = player.level().getDifficulty();
        shift = minecraft.options.keyShift.isDown();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        floor = player.blockPosition().above(16);
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos pos = floor.offset(x, 0, z);
                blocks.put(pos, player.level().getBlockState(pos));
                player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            }
        }
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 35);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
    }

    private void captureCases(ServerPlayer player) {
        active = beast(player);
        active.setOwnerUUID(UUID.randomUUID());
        ItemStack empty = item("lingshoudai_01");
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        capture(player, active);
        expectTrue("错误主人不能收纳且不生成袋UUID", !active.isRemoved() && data(empty).isEmpty());
        active.tame(player);

        CreatureData.state(active).setBagUuid("different_bag");
        capture(player, active);
        expectTrue("错误袋UUID在收纳过程前拒绝且不改袋", !active.isRemoved() && data(empty).isEmpty());
        CreatureData.state(active).setBagUuid("");

        ItemStack occupied = item("lingshoudai_02");
        CompoundTag occupiedData = new CompoundTag();
        occupiedData.putString("lingshoudaiuuid", "occupied_bag");
        occupiedData.putString("yaoshouid", ModId.VALUE + ":yaoshou_002");
        SpiritBeastBagRuntime.write(occupied, occupiedData);
        player.setItemInHand(InteractionHand.MAIN_HAND, occupied);
        capture(player, active);
        expectTrue("已占用袋拒绝无关联妖兽并保持原内容", !active.isRemoved() && data(occupied).equals(occupiedData));

        Wolf wolf = EntityType.WOLF.create(player.serverLevel());
        wolf.moveTo(player.position());
        wolf.tame(player);
        player.serverLevel().addFreshEntity(wolf);
        entities.add(wolf.getUUID());
        ItemStack wolfBag = item("lingshoudai_03");
        player.setItemInHand(InteractionHand.MAIN_HAND, wolfBag);
        capture(player, wolf);
        expectTrue("Q226普通驯服狼被外层妖兽标签排除且灵兽袋未写入", wolf.getAttribute(YaoshouAttributes.SIZE) == null
                && data(wolfBag).isEmpty() && !wolf.isRemoved());
        wolf.discard();

        empty.set(DataComponents.CUSTOM_NAME, Component.literal("[灵兽样本]"));
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        capture(player, active);
        bagUuid = data(empty).getString("lingshoudaiuuid");
        expectTrue("原生潜行交互收纳有效妖兽并保留袋实例", active.isRemoved() && empty.getCount() == 1
                && bagUuid.length() == 8 && bagUuid.chars().allMatch(value -> "abcdefjhigklmnopqrstuvwxy".indexOf(value) >= 0));
        expectTrue("收纳注册身份主人与剥离括号名称", data(empty).getString("yaoshouid").equals(ModId.VALUE + ":yaoshou_001")
                && data(empty).getString("belongToMasterUUID").equals(player.getStringUUID())
                && data(empty).getString("yaoshouname").equals("袋中花妖") && data(empty).getBoolean("yaoshouxvnfu"));
        expectTrue("收纳保持来源空玩家能力团队及选定字段边界", data(empty).getString("team").isEmpty()
                && !data(empty).contains("caijinumber") && !data(empty).contains("yaodandiaoluo"));
        verifyFields(data(empty), "收纳");
        expect("收纳当前生命", 37.5, data(empty).getDouble("health_Num"));
        expect("收纳生命恢复", 2.25, data(empty).getDouble("health_Rec"));
        expect("收纳体型", 1.7, data(empty).getDouble("entitydaxiao"));
        expect("收纳皮肤", 3, data(empty).getDouble("yaoshoupifuxianshi"));
        player.inventoryMenu.broadcastChanges();
    }

    private void releaseCases(ServerPlayer player) {
        ItemStack bag = player.getMainHandItem();
        player.setShiftKeyDown(true);
        use(player, InteractionHand.MAIN_HAND);
        expectTrue("潜行普通右键不释放", beasts(player).isEmpty() && !data(bag).getBoolean("yaoshoushifang"));
        CompoundTag wrongOwner = data(bag);
        wrongOwner.putString("belongToMasterUUID", "wrong_owner");
        SpiritBeastBagRuntime.write(bag, wrongOwner);
        player.setShiftKeyDown(false);
        use(player, InteractionHand.MAIN_HAND);
        expectTrue("错误袋主人不释放", beasts(player).isEmpty() && !data(bag).getBoolean("yaoshoushifang"));
        wrongOwner.putString("belongToMasterUUID", player.getStringUUID());
        SpiritBeastBagRuntime.write(bag, wrongOwner);
        use(player, InteractionHand.MAIN_HAND);
        active = onlyBeast(player);
        entities.add(active.getUUID());
        anchor(active);
        verifyEntity(player, "原生普通右键释放");
        expectTrue("释放写待收回而不消费袋", data(bag).getBoolean("yaoshoushifang") && bag.getCount() == 1);
        use(player, InteractionHand.MAIN_HAND);
        expect("待收回状态不能重复释放", 1, beasts(player).size());
        player.inventoryMenu.broadcastChanges();
    }

    private void recapture(ServerPlayer player) {
        active.setHealth(31.25F);
        EntityAttributes.update(active, stats -> stats.withExperience(42.5));
        capture(player, active);
        require(active.isRemoved(), "关联妖兽再次潜行收纳未完成");
        ItemStack bag = player.getMainHandItem();
        expectTrue("同袋回收保存当前生命经验并清待收回", data(bag).getDouble("health_Num") == 31.25
                && data(bag).getDouble("experience") == 42.5 && !data(bag).getBoolean("yaoshoushifang")
                && data(bag).getString("lingshoudaiuuid").equals(bagUuid));
        expectedFields.putDouble("experience", 42.5);
        dyeBag = bag.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, item("lingshoudaizhuanhuanitem"));
        player.inventoryMenu.broadcastChanges();
    }

    private boolean color(int color, int phase, int step, int local) {
        if (phase == 0) return action(step, local, 8, player -> prepareColor(player, color));
        if (!requireMenu(local, "染色" + (color + 1) + "菜单同步")) return false;
        if (phase == 1) {
            click(3, ClickType.QUICK_MOVE);
            click(4, ClickType.QUICK_MOVE);
            return true;
        }
        if (phase == 2) {
            if (local < 8) return false;
            onServer(player -> {
                SpiritBeastBagDyeMenu menu = serverMenu(player);
                require(ItemStack.matches(dyeBag, menu.getSlot(0).getItem()) && menu.getSlot(1).getItem().is(DYES.get(color)),
                        "原生快捷移动未把袋与对应染料放入菜单 " + color);
                expectTrue("Q227未上色袋不能放首槽且输出槽拒绝放入", !menu.getSlot(0).mayPlace(item("lingshoudai_00"))
                        && !menu.getSlot(2).mayPlace(item("lingshoudai_01")));
            });
            Button button = minecraft.screen.children().stream().filter(Button.class::isInstance)
                    .map(Button.class::cast).findFirst().orElseThrow();
            expectTrue("染色" + (color + 1) + "原生鼠标点击按钮", minecraft.screen.mouseClicked(button.getX() + 2, button.getY() + 2, 0));
            minecraft.screen.mouseReleased(button.getX() + 2, button.getY() + 2, 0);
            return true;
        }
        if (phase == 3) {
            if (local < 8) return false;
            onServer(player -> verifyColor(player, color));
            if (color == 0) {
                minecraft.getToasts().clear();
                GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), 2, 2);
            } else if (color == 15) hoverOutput();
            return true;
        }
        if (phase == 4) {
            if (local < 8) return false;
            if (color == 0) {
                if (executed != step) {
                    executed = step;
                    screenshots.take("spirit-beast-bag-dye-menu.png");
                    hoverOutput();
                }
                if (local < 16) return false;
            }
            if (color == 0 || color == 15) screenshots.take("spirit-beast-bag-dye-" + (color + 1) + "-tooltip.png");
            click(2, ClickType.QUICK_MOVE);
            return true;
        }
        return action(step, local, 8, player -> {
            SpiritBeastBagDyeMenu menu = serverMenu(player);
            require(menu.getSlot(2).getItem().isEmpty(), "输出快捷移动后仍占用输出槽");
            Item output = item("lingshoudai_%02d".formatted(color + 1)).getItem();
            int found = -1;
            int remainingDye = 0;
            List<String> dyeSlots = new ArrayList<>();
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(output)) {
                    require(found == -1, "染色后出现重复输出袋");
                    found = slot;
                }
                if (stack.is(DYES.get(color))) {
                    remainingDye += stack.getCount();
                    dyeSlots.add(slot + ":" + stack.getCount());
                }
            }
            require(found >= 0, "快捷移动未把输出袋交还玩家");
            dyeBag = player.getInventory().getItem(found).copy();
            player.getInventory().setItem(found, ItemStack.EMPTY);
            expectTrue("染色" + (color + 1) + "输出快捷移动与余料守恒 输出=" + dyeBag.getCount()
                    + " 染料=" + remainingDye + " 染料槽=" + dyeSlots, dyeBag.getCount() == 1 && remainingDye == 1);
        });
    }

    private void prepareColor(ServerPlayer player, int color) {
        SpiritBeastBagDyeMenu menu = serverMenu(player);
        for (int slot = 0; slot < 3; slot++) menu.getSlot(slot).set(ItemStack.EMPTY);
        player.getInventory().setItem(9, dyeBag.copy());
        player.getInventory().setItem(10, new ItemStack(DYES.get(color), 2));
        menu.broadcastChanges();
    }

    private void verifyColor(ServerPlayer player, int color) {
        SpiritBeastBagDyeMenu menu = serverMenu(player);
        ItemStack output = menu.getSlot(2).getItem();
        require(output.is(item("lingshoudai_%02d".formatted(color + 1)).getItem()), "染色输出身份错误 " + color);
        expectTrue("染色" + (color + 1) + "按来源映射并各扣一个", output.getCount() == 1
                && menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty());
        expectTrue("染色" + (color + 1) + "保留所有输入组件且双端输出一致", output.getComponentsPatch().equals(dyeBag.getComponentsPatch())
                && ItemStack.matches(output, minecraft.player.containerMenu.getSlot(2).getItem()));
        List<String> tooltip = minecraft.player.containerMenu.getSlot(2).getItem()
                .getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL)
                .stream().map(Component::getString).toList();
        expectTrue("染色" + (color + 1) + "Tooltip包含中文生命恢复与操作且无原始键", tooltip.stream().anyMatch(line -> line.contains("血量：31/141（2）"))
                && tooltip.stream().anyMatch(line -> line.contains("潜行状态下右键妖兽可以收起"))
                && tooltip.stream().noneMatch(line -> line.contains("tooltip.yvanchuxiuzhen") || line.contains("message.yvanchuxiuzhen")));
        ItemStack before = output.copy();
        menu.getSlot(0).set(dyeBag.copy());
        menu.getSlot(1).set(new ItemStack(Items.WHITE_DYE));
        menu.clickMenuButton(player, 0);
        expectTrue("染色" + (color + 1) + "输出被占用时不消费输入", ItemStack.matches(before, menu.getSlot(2).getItem())
                && ItemStack.matches(dyeBag, menu.getSlot(0).getItem()) && menu.getSlot(1).getItem().getCount() == 1);
        menu.getSlot(0).set(ItemStack.EMPTY);
        menu.getSlot(1).set(ItemStack.EMPTY);
        menu.broadcastChanges();
    }

    private void storeMainAndOpenOffhand(ServerPlayer player) {
        SpiritBeastBagDyeMenu menu = serverMenu(player);
        menu.getSlot(0).set(dyeBag.copy());
        menu.getSlot(1).set(new ItemStack(Items.BLUE_DYE));
        menu.getSlot(2).set(item("lingshoudai_05"));
        savedMainTool = player.getMainHandItem().copy();
        player.closeContainer();
        expectTrue("主手工具关闭后保留三槽组件", ItemStack.matches(savedMainTool, player.getMainHandItem()));
        player.getInventory().setItem(21, player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ItemStack offTool = item("lingshoudaizhuanhuanitem");
        offTool.set(DataComponents.CUSTOM_NAME, Component.literal("副手染色工具"));
        SpiritBeastDyeToolInventory contents = new SpiritBeastDyeToolInventory(offTool);
        contents.setStackInSlot(0, item("lingshoudai_04"));
        contents.setStackInSlot(1, new ItemStack(Items.RED_DYE));
        contents.setStackInSlot(2, item("lingshoudai_13"));
        player.setItemInHand(InteractionHand.OFF_HAND, offTool);
        openTool(player, InteractionHand.OFF_HAND);
    }

    private void prepareReload(ServerPlayer player) {
        SpiritBeastBagDyeMenu menu = serverMenu(player);
        expectTrue("副手菜单读取独立三槽且不改主手工具", menu.getSlot(0).getItem().is(item("lingshoudai_04").getItem())
                && menu.getSlot(1).getItem().is(Items.RED_DYE) && menu.getSlot(2).getItem().is(item("lingshoudai_13").getItem())
                && ItemStack.matches(savedMainTool, player.getInventory().getItem(21)));
        savedOffTool = player.getOffhandItem().copy();
        player.closeContainer();
        player.getInventory().setItem(22, player.getOffhandItem());
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, dyeBag);
        use(player, InteractionHand.MAIN_HAND);
        active = onlyBeast(player);
        entities.add(active.getUUID());
        anchor(active);
        active.moveTo(player.position().add(2, 0, 0));
        activeUuid = active.getUUID();
        verifyEntity(player, "染色后再次释放");
        StatusEffects.add(active, "effect_007", 2400, 0);
        savedBag = player.getMainHandItem().copy();
        player.getInventory().setItem(20, player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        unrelatedBag = item("lingshoudai_16");
        CompoundTag unrelated = new CompoundTag();
        unrelated.putString("lingshoudaiuuid", "unrelated_bag");
        unrelated.putInt("sentinel", 17);
        SpiritBeastBagRuntime.write(unrelatedBag, unrelated);
        player.getInventory().setItem(23, unrelatedBag.copy());
        duplicateBag = savedBag.copy();
        player.getInventory().setItem(24, duplicateBag.copy());
        player.inventoryMenu.broadcastChanges();
    }

    private void verifyReloadAndDeath(ServerPlayer player) {
        Entity restored = player.serverLevel().getEntity(activeUuid);
        require(restored instanceof YaoshouEntity, "真实重进后未恢复灵兽袋释放妖兽 " + activeUuid);
        active = (YaoshouEntity) restored;
        verifyEntity(player, "真实重进");
        for (int slot : List.of(20, 21, 22)) {
            ItemStack expected = slot == 20 ? savedBag : slot == 21 ? savedMainTool : savedOffTool;
            expectTrue("真实重进背包槽" + slot + "完整组件与双端同步", ItemStack.matches(expected, player.getInventory().getItem(slot))
                    && ItemStack.matches(expected, minecraft.player.getInventory().getItem(slot)));
        }
        active.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        ItemStack emptied = player.getInventory().getItem(20);
        expectTrue("来源死亡入口重建首个非手持关联袋", active.isDeadOrDying() && emptied.is(savedBag.getItem())
                && emptied.getCount() == 1 && data(emptied).isEmpty() && emptied.getHoverName().getString().equals("灵兽样本"));
        expectTrue("死亡只清首个同UUID袋且不改其它UUID袋", ItemStack.matches(duplicateBag, player.getInventory().getItem(24))
                && ItemStack.matches(unrelatedBag, player.getInventory().getItem(23)));
        player.inventoryMenu.broadcastChanges();
    }

    private YaoshouEntity beast(ServerPlayer player) {
        YaoshouEntity beast = (YaoshouEntity) CreatureSetup.entityType("yaoshou_001").get().create(player.serverLevel());
        beast.moveTo(player.position().add(2, 0, 0));
        beast.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(beast.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        EntityAttributes.update(beast, current -> {
            EntityStats updated = current.withRealmName(expectedFields.getString("LEVELname"))
                    .withLevel(expectedFields.getDouble("LEVELnumber"))
                    .withExperienceMax(expectedFields.getDouble("experience_Max"))
                    .withExperience(expectedFields.getDouble("experience"));
            List<String> statKeys = List.of("health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
                    "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed_number");
            List<String> ratingKeys = List.of("health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL",
                    "soul_Max_LEVEL", "soul_Rec_LEVEL", "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL",
                    "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL");
            for (int i = 0; i < EntityAttributes.COMBAT_STATS.size(); i++) {
                Stat stat = EntityAttributes.COMBAT_STATS.get(i);
                updated = updated.withStat(stat, expectedFields.getDouble(statKeys.get(i)))
                        .withRating(stat, expectedFields.getString(ratingKeys.get(i)));
            }
            return updated;
        });
        CreatureState state = CreatureData.state(beast);
        state.setHarvestCount(7);
        state.setBeastCoreDropped(true);
        state.setCharacter(expectedFields.getString("character"));
        state.setCharacterDef(expectedFields.getDouble("character_def"));
        state.setCharacterAtt(expectedFields.getDouble("character_att"));
        state.setCharacterInterval(expectedFields.getDouble("character_interval"));
        state.setCharacterResist(expectedFields.getDouble("character_resist"));
        beast.getAttribute(Attributes.MAX_HEALTH).setBaseValue(141.25);
        beast.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(19.75);
        beast.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.37);
        beast.getAttribute(YaoshouAttributes.SIZE).setBaseValue(1.7);
        beast.getAttribute(YaoshouAttributes.SKIN).setBaseValue(3);
        beast.setHealth(37.5F);
        beast.setCustomName(Component.literal("[袋中花妖]"));
        beast.tame(player);
        Relations.setTeam(beast, "r07_original_creature");
        anchor(beast);
        player.serverLevel().addFreshEntity(beast);
        entities.add(beast.getUUID());
        return beast;
    }

    private void verifyEntity(ServerPlayer player, String phase) {
        verifyEntityFields(active, phase);
        CreatureState state = CreatureData.state(active);
        expectTrue(phase + "恢复原生主人名称与袋UUID", active.isOwnedBy(player) && active.getDisplayName().getString().contains("袋中花妖")
                && state.bagUuid().equals(bagUuid)
                && state.masterUuid().equals(player.getStringUUID()));
        expectTrue(phase + "继承当前主人阵营好友且不恢复采集状态", Relations.entityTeam(active).equals(Relations.team(player))
                && Relations.friendlyTeams(active).equals(Relations.friendlyTeams(player))
                && state.harvestCount() == 0 && !state.beastCoreDropped());
        expect(phase + "最大生命属性", 141.25, active.getAttributeBaseValue(Attributes.MAX_HEALTH));
        expect(phase + "原生攻击属性", 19.75, active.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
        expect(phase + "原生速度属性", .37, active.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
        expect(phase + "体型属性", 1.7, active.getAttributeBaseValue(YaoshouAttributes.SIZE));
        expect(phase + "皮肤属性", 3, active.getAttributeBaseValue(YaoshouAttributes.SKIN));
        expect(phase + "当前生命", expectedFields.getDouble("experience") == 42.5 ? 31.25 : 37.5, active.getHealth());
    }

    private void verifyEntityFields(YaoshouEntity entity, String phase) {
        EntityStats stats = EntityAttributes.get(entity);
        CreatureState state = CreatureData.state(entity);
        expect(phase + "选定字段LEVELname", expectedFields.getString("LEVELname"), stats.realmName());
        expect(phase + "选定字段LEVELnumber", expectedFields.getDouble("LEVELnumber"), stats.level());
        expect(phase + "选定字段experience_Max", expectedFields.getDouble("experience_Max"), stats.experienceMax());
        expect(phase + "选定字段experience", expectedFields.getDouble("experience"), stats.experience());
        List<String> statKeys = List.of("health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
                "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed_number");
        List<String> ratingKeys = List.of("health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL",
                "soul_Max_LEVEL", "soul_Rec_LEVEL", "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL",
                "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL");
        for (int i = 0; i < EntityAttributes.COMBAT_STATS.size(); i++) {
            Stat stat = EntityAttributes.COMBAT_STATS.get(i);
            expect(phase + "选定字段" + statKeys.get(i), expectedFields.getDouble(statKeys.get(i)), stats.stat(stat));
            expect(phase + "选定字段" + ratingKeys.get(i), expectedFields.getString(ratingKeys.get(i)), stats.rating(stat));
        }
        expect(phase + "选定字段character", expectedFields.getString("character"), state.character());
        expect(phase + "选定字段character_def", expectedFields.getDouble("character_def"), state.characterDef());
        expect(phase + "选定字段character_att", expectedFields.getDouble("character_att"), state.characterAtt());
        expect(phase + "选定字段character_interval", expectedFields.getDouble("character_interval"), state.characterInterval());
        expect(phase + "选定字段character_resist", expectedFields.getDouble("character_resist"), state.characterResist());
    }

    private void verifyFields(CompoundTag actual, String phase) {
        for (String key : expectedFields.getAllKeys()) expect(phase + "选定字段" + key, expectedFields.get(key), actual.get(key));
    }

    private boolean requireMenu(int local, String label) {
        if (local < 8) return false;
        boolean clientReady = minecraft.screen instanceof SpiritBeastBagDyeScreen
                && minecraft.screen.getTitle().getString().equals("灵兽袋染色")
                && minecraft.player.containerMenu instanceof SpiritBeastBagDyeMenu menu && menu.getCarried().isEmpty();
        boolean[] serverReady = {false};
        onServer(player -> serverReady[0] = player.containerMenu instanceof SpiritBeastBagDyeMenu menu
                && menu.getCarried().isEmpty() && menu.containerId == minecraft.player.containerMenu.containerId);
        if (clientReady && serverReady[0]) return true;
        if (local > 100) throw new IllegalStateException(label + "未就绪");
        return false;
    }

    private static SpiritBeastBagDyeMenu serverMenu(ServerPlayer player) {
        if (!(player.containerMenu instanceof SpiritBeastBagDyeMenu menu)) throw new IllegalStateException("服务端灵兽袋菜单未开启");
        return menu;
    }

    private void hoverOutput() {
        double x = (minecraft.screen.width - 176) / 2.0 + 132;
        double y = (minecraft.screen.height - 166) / 2.0 + 43;
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), x * minecraft.getWindow().getScreenWidth() / minecraft.screen.width,
                y * minecraft.getWindow().getScreenHeight() / minecraft.screen.height);
    }

    private void click(int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private static void capture(ServerPlayer player, Entity beast) {
        player.setShiftKeyDown(true);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    private static void use(ServerPlayer player, InteractionHand hand) {
        player.gameMode.useItem(player, player.level(), player.getItemInHand(hand), hand);
    }

    private static void openTool(ServerPlayer player, InteractionHand hand) {
        player.setShiftKeyDown(false);
        use(player, hand);
    }

    private static void anchor(YaoshouEntity beast) {
        beast.setNoAi(true);
        beast.setNoGravity(true);
        beast.setDeltaMovement(Vec3.ZERO);
    }

    private List<YaoshouEntity> beasts(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(YaoshouEntity.class, player.getBoundingBox().inflate(12), Entity::isAlive);
    }

    private YaoshouEntity onlyBeast(ServerPlayer player) {
        List<YaoshouEntity> beasts = beasts(player);
        require(beasts.size() == 1, "预期一只灵兽袋释放妖兽，实际 " + beasts.size());
        return beasts.getFirst();
    }

    private static CompoundTag data(ItemStack stack) {
        return SpiritBeastBagRuntime.read(stack);
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    private static void require(boolean ready, String message) {
        if (!ready) throw new IllegalStateException(message);
    }

    private static CompoundTag specimenFields() {
        CompoundTag values = new CompoundTag();
        values.putString("LEVELname", "二阶中级");
        values.putDouble("LEVELnumber", 8);
        values.putDouble("experience_Max", 321.75);
        values.putDouble("experience", 12.5);
        values.putDouble("health_Max", 141.25);
        values.putDouble("health_Rec", 2.25);
        values.putDouble("power_Max", 223.5);
        values.putDouble("power_Rec", 3.75);
        values.putDouble("soul_Max", 334.75);
        values.putDouble("soul_Rec", 4.5);
        values.putDouble("attack_Num", 19.75);
        values.putDouble("attack_Def", 20.5);
        values.putDouble("mana_Att", 21.25);
        values.putDouble("mana_Def", 22.75);
        values.putDouble("soul_Att", 23.5);
        values.putDouble("soul_Def", 24.25);
        values.putDouble("speed_number", .37);
        String[] prefixes = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
                "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
        String[] ratings = {"绝佳", "强劲", "平庸", "勉强", "贫弱"};
        for (int index = 0; index < prefixes.length; index++) values.putString(prefixes[index] + "_LEVEL", ratings[index % ratings.length]);
        values.putString("character", "谨慎");
        values.putDouble("character_def", 1.5);
        values.putDouble("character_att", .5);
        values.putDouble("character_interval", 1.5);
        values.putDouble("character_resist", 1.3);
        return values;
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        for (UUID id : entities) {
            Entity entity = player.serverLevel().getEntity(id);
            if (entity != null) entity.discard();
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, yaw, pitch);
        blocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.setGameMode(mode);
        player.server.setDifficulty(difficulty, true);
        player.setShiftKeyDown(shift);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.options.keyShift.setDown(shift));
    }
}
