package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.decor.DecorCushion;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.client.BirthScreen;
import net.yvanchuxiuzhen.system.xiulian.client.RetreatScreen;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/** 正常首次登录实际领书；凡人研习、蒲团合成和一日闭关，不触发首破测试。 */
final class RenjianBirthSupplyCheck extends SystemCheck {
    private static final BlockPos SEAT = new BlockPos(8, 180, 8);
    private static final BlockPos TABLE = new BlockPos(9, 180, 7);
    private static final String BASIC = "五行基础";
    private static final String FALLEN = "落魄家族";
    private static final String SKILL = "技能书";
    private static final String TECHNIQUE = "功法书";
    private final Screenshots screenshots;
    private final BooleanSupplier readyForDiskExit;
    private UUID newcomer;
    private int selected;
    private int craftInput;
    private double lifetime;
    private double savedExperience;
    private ItemStack savedTechnique;
    private ItemStack drawnSkill;
    private boolean shifting;

    RenjianBirthSupplyCheck(List<String> report, String levelId, Screenshots screenshots, BooleanSupplier readyForDiskExit) {
        super("人间出身至凡人闭关", report, levelId);
        this.screenshots = screenshots;
        this.readyForDiskExit = readyForDiskExit;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            releaseShift();
            fail("步骤 " + step + " 异常：" + exception);
            finish();
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> {
                if (!(minecraft.screen instanceof BirthScreen) || !birth(minecraft.player).equals("流浪侠客"))
                    return await(local, "新档正常登录未打开默认出身界面");
                newcomer = minecraft.player.getUUID();
                onServer(player -> {
                    expectTrue("首次服务端菜单已打开且前置背包为空", player.containerMenu instanceof BirthMenu
                            && player.serverLevel().dimension().equals(Level.OVERWORLD) && player.getInventory().isEmpty()
                            && !player.isCreative() && player.getUUID().equals(newcomer));
                    expect("默认出身", "流浪侠客", birth(player));
                });
                nextBirth();
            }
            case 1 -> {
                if (!birth(minecraft.player).equals("世家纨绔")) return await(local, "第一次原生按钮未同步");
                nextBirth();
            }
            case 2 -> {
                if (!(minecraft.screen instanceof BirthScreen) || !birth(minecraft.player).equals(FALLEN))
                    return await(local, "第二次原生按钮未同步落魄家族");
                onServer(player -> {
                    expect("关闭前服务端出身", FALLEN, birth(player));
                    expectTrue("关闭前未发奖励", player.getInventory().isEmpty());
                });
                screenshots.take("renjian_birth_fallen.png");
                minecraft.player.closeContainer();
            }
            case 3 -> {
                if (minecraft.screen != null || !minecraft.level.dimension().equals(DimensionPortalSpec.RENJIAN.dimension())
                        || count(minecraft.player, s -> shelf(s, SKILL)) != 1 || count(minecraft.player, s -> shelf(s, TECHNIQUE)) != 1)
                    return await(local, "原生关闭后未收到两本一级藏宝书或未做人间传送");
                onServer(player -> {
                    expect("领取的是同一新档玩家", newcomer, player.getUUID());
                    expect("出身关闭后来源传送", DimensionPortalSpec.RENJIAN.dimension(), player.serverLevel().dimension());
                    expect("实际一级技能藏宝书", 1, count(player, s -> shelf(s, SKILL)));
                    expect("实际一级功法藏宝书", 1, count(player, s -> shelf(s, TECHNIQUE)));
                    expect("领奖并未预置抽出的技能", 0, count(player, s -> Skills.book(s).isPresent()));
                    expect("领奖并未预置抽出的功法", 0, count(player, s -> Techniques.book(s).isPresent()));
                    selected = player.getInventory().selected;
                    preparePlatform(player);
                    useReward(player, SKILL);
                });
            }
            case 4 -> {
                if (!shelf(hand(), SKILL) || !nearPlatform()) return await(local, "实际领取的技能藏宝书未到主手");
                useHand();
            }
            case 5 -> {
                if (!hand().isEmpty() || count(minecraft.player, s -> Skills.book(s).isPresent()) != 1)
                    return await(local, "技能藏宝书未消耗或未同步一本随机书");
                onServer(player -> {
                    expect("服务端技能藏宝书已消耗", 0, count(player, s -> shelf(s, SKILL)));
                    expect("服务端产出一本技能书", 1, count(player, s -> Skills.book(s).isPresent()));
                    drawnSkill = find(player, s -> Skills.book(s).isPresent()).copy();
                    SkillBook rolled = Skills.book(drawnSkill).orElseThrow();
                    expectTrue("实际抽出未研习技能：" + rolled.name(), !rolled.name().isEmpty() && rolled.owner().isEmpty());
                    useReward(player, TECHNIQUE);
                });
            }
            case 6 -> {
                if (!shelf(hand(), TECHNIQUE)) return await(local, "实际领取的功法藏宝书未到主手");
                useHand();
            }
            case 7 -> {
                if (!hand().isEmpty() || count(minecraft.player, s -> Techniques.book(s).isPresent()) != 1)
                    return await(local, "功法藏宝书未消耗或未同步一本随机书");
                onServer(this::prepareStudyBook);
            }
            case 8 -> {
                if (!Techniques.book(hand()).filter(book -> book.definition().name().equals(BASIC) && book.owner().isEmpty()).isPresent())
                    return await(local, "未同步未研习五行基础");
                setShift(true);
            }
            case 9 -> {
                boolean[] sneaking = new boolean[1];
                onServer(player -> sneaking[0] = player.isShiftKeyDown());
                if (!sneaking[0]) return await(local, "潜行未抵达服务端");
                useHand();
            }
            case 10 -> {
                if (!owned(minecraft.player, hand())) return await(local, "未通过原生潜行右键研习");
                releaseShift();
                pressPanel();
            }
            case 11 -> {
                if (!(minecraft.screen instanceof AttributeScreen)) return await(local, "新档 K 未打开默认属性页");
                button("功法与技能");
            }
            case 12 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || local < 10) return await(local, "属性页按钮未打开 04");
                AbstractContainerMenu menu = screen.getMenu();
                click(menu, inventorySlot(menu, selected), 0);
                click(menu, 0, 0);
            }
            case 13 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || !owned(minecraft.player, screen.getMenu().getSlot(0).getItem()))
                    return await(local, "04 功法槽未同步");
                screenshots.take("renjian_birth_loadout.png");
            }
            case 14 -> {
                minecraft.player.closeContainer();
            }
            case 15 -> {
                if (minecraft.screen != null || !owned(minecraft.player, Techniques.slot(minecraft.player)))
                    return await(local, "关闭 04 后装配未同步");
                onServer(player -> {
                    expectTrue("服务端功法槽实际写回", owned(player, Techniques.slot(player)));
                    expect("开始前无修为预置", 0, Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expect("开始前仍为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    lifetime = Cultivation.get(player, CultivationValue.LIFETIME);
                    // 只布置工作台和原料；成品、扣料与放置均走原生入口。
                    player.getInventory().setItem(selected, new ItemStack(Items.WHEAT, 4));
                });
            }
            case 16 -> {
                if (!hand().is(Items.WHEAT) || !minecraft.level.getBlockState(TABLE).is(Blocks.CRAFTING_TABLE))
                    return await(local, "工作台或四小麦未同步");
                useOn(TABLE);
            }
            case 17 -> {
                if (!(minecraft.screen instanceof CraftingScreen screen) || local < 6) return await(local, "未打开原生工作台");
                CraftingMenu menu = screen.getMenu();
                craftInput = inventorySlot(menu, selected);
                click(menu, craftInput, 0);
                for (int slot : new int[]{2, 4, 6, 8}) click(menu, slot, 1);
            }
            case 18 -> {
                if (!(minecraft.screen instanceof CraftingScreen screen) || !screen.getMenu().getSlot(0).getItem().is(cushion())
                        || !screen.getMenu().getCarried().isEmpty()) return await(local, "四小麦交叉配方未出现蒲团输出");
                onServer(player -> {
                    expectTrue("服务端工作台计算出的有效蒲团成品", player.containerMenu instanceof CraftingMenu menu
                            && menu.getSlot(0).getItem().is(cushion()) && menu.getCarried().isEmpty());
                });
                screenshots.take("renjian_birth_cushion_recipe.png");
                click(screen.getMenu(), 0, 0);
                click(screen.getMenu(), craftInput, 0);
            }
            case 19 -> {
                if (!(minecraft.screen instanceof CraftingScreen screen) || !hand().is(cushion())
                        || !screen.getMenu().getSlot(0).getItem().isEmpty()) return await(local, "原生输出领取未完成");
                minecraft.player.closeContainer();
            }
            case 20 -> {
                if (minecraft.screen != null) return await(local, "工作台未正常关闭");
                onServer(player -> {
                    expect("合成实耗四小麦", 0, count(player, s -> s.is(Items.WHEAT)));
                    expect("原生合成实际产出一蒲团", 1, count(player, s -> s.is(cushion())));
                });
                useOn(SEAT.below());
            }
            case 21 -> {
                if (!minecraft.level.getBlockState(SEAT).is(cushionBlock()) || !hand().isEmpty())
                    return await(local, "真实放置蒲团未同步");
                useOn(SEAT);
            }
            case 22 -> {
                if (!(minecraft.player.getVehicle() instanceof DecorCushion)) return await(local, "未真正坐到蒲团");
                pressPanel();
            }
            case 23 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || local < 10)
                    return await(local, "K 未重开 04 或尚未经过夹具等待");
                SkillLoadoutMenu menu = screen.getMenu();
                ItemStack visible = menu.getSlot(0).getItem().copy();
                if (minecraft.player.containerMenu != menu || !owned(minecraft.player, visible)
                        || !ItemStack.matches(visible, Techniques.slot(minecraft.player)))
                    return await(local, "当前客户端 04 槽 0 尚未同步本人功法");
                boolean[] initialized = new boolean[1];
                onServer(player -> initialized[0] = player.containerMenu instanceof SkillLoadoutMenu active
                        && active.containerId == menu.containerId
                        && ItemStack.matches(active.getSlot(0).getItem(), visible)
                        && ItemStack.matches(Techniques.slot(player), visible));
                if (!initialized[0]) return await(local, "服务端 04 尚未完成两刻初始化或功法槽未匹配");
                expectTrue("重开 04 菜单 ID 一致，槽 0 与双端持久功法同栈", initialized[0]);
                button("修炼");
            }
            case 24 -> {
                if (!(minecraft.screen instanceof RetreatScreen screen) || screen.getMenu().mode() != 6)
                    return await(local, "04 修炼按钮未打开 06");
                button("增加一天");
            }
            case 25 -> {
                if (Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum") != 1) return await(local, "06 未同步一天");
                onServer(player -> verifyBeforeRetreat(player, "服务端"));
                verifyBeforeRetreat(minecraft.player, "本人");
                screenshots.take("renjian_birth_retreat.png");
                button("开始修炼");
            }
            case 26 -> {
                if (minecraft.screen != null || Cultivation.get(minecraft.player, CultivationValue.LIFETIME) != lifetime - 1
                        || Cultivation.get(minecraft.player, CultivationValue.CULTIVATION_DAYS) != 0
                        || Cultivation.get(minecraft.player, CultivationValue.EXPERIENCE) <= 0)
                    return await(local, "单日原生闭关未完成修为吸收");
                onServer(player -> {
                    verifyRetreat(player, "服务端");
                    savedExperience = Cultivation.get(player, CultivationValue.EXPERIENCE);
                    savedTechnique = Techniques.slot(player).copy();
                    player.stopRiding();
                });
                verifyRetreat(minecraft.player, "本人");
            }
            case 27 -> {
                if (!readyForDiskExit.getAsBoolean()) return false;
                disconnectWorld();
            }
            case 28 -> { return reopened(local); }
            case 29 -> {
                if (!inWorld(local)) return false;
                WorldStopDiagnostics.arm(minecraft.getSingleplayerServer());
                onServer(player -> {
                    verifyReload(player, "同档重进服务端");
                    expectTrue("同档重进服务端蒲团仍在原位", player.level().getBlockState(SEAT).is(cushionBlock()));
                });
                verifyReload(minecraft.player, "同档重进本人");
                expectTrue("同档重进未重新弹出出身界面且蒲团已同步",
                        !(minecraft.screen instanceof BirthScreen) && minecraft.level.getBlockState(SEAT).is(cushionBlock()));
                finish();
            }
            default -> throw new IllegalStateException("未知步骤 " + step);
        }
        return true;
    }

    private void verifyBeforeRetreat(Player player, String side) {
        expect(side + "入关前仍为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect(side + "入关前未经修正的功法门槛", 0, Cultivation.tizhiDouble(player, "LevelNumberCorrect"));
        verifyTechnique(player, side + "入关前");
    }

    private void verifyRetreat(Player player, String side) {
        expect(side + "仍为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect(side + "单日寿元变化", lifetime - 1, Cultivation.get(player, CultivationValue.LIFETIME));
        expect(side + "待吸收为零", 0, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND));
        double experience = Cultivation.get(player, CultivationValue.EXPERIENCE);
        expectTrue(side + "单日吸收修为 " + experience, experience > 0);
        verifyTechnique(player, side + "出关后但功法门槛未开放");
    }

    private void verifyTechnique(Player player, String side) {
        expectTrue(side + "功法槽仍是本人五行基础一层", owned(player, Techniques.slot(player)));
        double experience = Techniques.main(player).map(TechniqueBook::exp).orElse(Double.NaN);
        expect(side + "同槽功法经验", 0, experience);
    }

    private void verifyReload(Player player, String side) {
        expect(side + "玩家身份", newcomer, player.getUUID());
        expect(side + "出身", FALLEN, birth(player));
        expect(side + "维度", DimensionPortalSpec.RENJIAN.dimension(), player.level().dimension());
        verifyRetreat(player, side);
        expect(side + "保存前修为", savedExperience, Cultivation.get(player, CultivationValue.EXPERIENCE));
        expect(side + "闭关剩余天数", 0, Cultivation.get(player, CultivationValue.CULTIVATION_DAYS));
        expectTrue(side + "保存前功法栈与组件", ItemStack.matches(savedTechnique, Techniques.slot(player)));
        expect(side + "实际抽得技能书仍在背包", 1, count(player, s -> ItemStack.matches(drawnSkill, s)));
        expect(side + "已消耗的出身藏宝书未补发", 0, count(player, s -> shelf(s, SKILL) || shelf(s, TECHNIQUE)));
    }

    private void prepareStudyBook(ServerPlayer player) {
        expect("服务端功法藏宝书已消耗", 0, count(player, s -> shelf(s, TECHNIQUE)));
        expect("服务端产出一本功法书", 1, count(player, s -> Techniques.book(s).isPresent()));
        int slot = findSlot(player, s -> Techniques.book(s).isPresent());
        TechniqueBook drawn = Techniques.book(player.getInventory().getItem(slot)).orElseThrow();
        expectTrue("实际抽出未研习功法：" + drawn.definition().name(), drawn.owner().isEmpty());
        if (drawn.definition().name().equals(BASIC)) {
            moveToHand(player, slot);
            expectTrue("研习沿用实际出身书的随机结果", Techniques.book(player.getMainHandItem()).filter(drawn::equals).isPresent());
        } else {
            player.getInventory().setItem(selected, Techniques.createBook(TechniqueLibrary.all().stream()
                    .filter(definition -> definition.name().equals(BASIC)).findFirst().orElseThrow()));
            expectTrue("夹具备未研习五行基础；实际抽得的高门槛功法仍保留",
                    Techniques.book(player.getInventory().getItem(slot)).filter(drawn::equals).isPresent());
        }
    }

    private void preparePlatform(ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(SEAT.offset(-2, -1, -3), SEAT.offset(2, 3, 2))) {
            player.serverLevel().setBlockAndUpdate(pos, pos.getY() == SEAT.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        player.serverLevel().setBlockAndUpdate(TABLE, Blocks.CRAFTING_TABLE.defaultBlockState());
        player.connection.teleport(8.5, 180, 7.5, 0, 30);
    }

    private boolean nearPlatform() {
        return minecraft.player.distanceToSqr(8.5, 180, 7.5) < 4;
    }

    private boolean await(int local, String reason) {
        if (local > 400) {
            releaseShift();
            fail(reason);
            finish();
        }
        return false;
    }

    private void nextBirth() {
        BirthScreen screen = (BirthScreen) minecraft.screen;
        TextureButton next = screen.children().stream().filter(TextureButton.class::isInstance).map(TextureButton.class::cast)
                .max(Comparator.comparingInt(TextureButton::getX)).orElseThrow();
        if (!screen.mouseClicked(next.getX() + 4, next.getY() + 5, 0)) throw new IllegalStateException("出身右箭头未接收点击");
    }

    private static String birth(Player player) {
        return Cultivation.get(player, CultivationText.BIRTH);
    }

    private void pressPanel() {
        var key = Arrays.stream(minecraft.options.keyMappings).filter(value -> value.getName().equals("key.yvanchuxiuzhen.guicaidan"))
                .findFirst().orElseThrow();
        key.setDown(true);
        key.setDown(false);
    }

    private void button(String text) {
        minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(text))
                .map(Button.class::cast).findFirst().orElseThrow().onPress();
    }

    private void setShift(boolean down) {
        shifting = down;
        minecraft.options.keyShift.setDown(down);
        minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player,
                down ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private void releaseShift() {
        if (shifting) setShift(false);
    }

    private int inventorySlot(AbstractContainerMenu menu, int index) {
        return menu.slots.stream().filter(slot -> slot.container == minecraft.player.getInventory() && slot.getContainerSlot() == index)
                .findFirst().orElseThrow().index;
    }

    private void click(AbstractContainerMenu menu, int slot, int button) {
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot, button, ClickType.PICKUP, minecraft.player);
    }

    private ItemStack hand() {
        return minecraft.player.getMainHandItem();
    }

    private void useHand() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void useOn(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
    }

    private void useReward(ServerPlayer player, String kind) {
        moveToHand(player, findSlot(player, stack -> shelf(stack, kind)));
    }

    private void moveToHand(ServerPlayer player, int slot) {
        if (slot == selected) return;
        ItemStack previous = player.getInventory().getItem(selected);
        player.getInventory().setItem(selected, player.getInventory().getItem(slot));
        player.getInventory().setItem(slot, previous);
    }

    private static boolean shelf(ItemStack stack, String kind) {
        TreasureContainerConfig config = TreasureContainers.config(stack);
        return stack.is(item("book_021")) && config.category().equals("书架")
                && config.subtype().equals(kind) && config.level() == 1;
    }

    private static int count(Player player, Predicate<ItemStack> match) {
        return player.getInventory().items.stream().filter(match).mapToInt(ItemStack::getCount).sum();
    }

    private static ItemStack find(Player player, Predicate<ItemStack> match) {
        return player.getInventory().getItem(findSlot(player, match));
    }

    private static int findSlot(Player player, Predicate<ItemStack> match) {
        for (int i = 0; i < player.getInventory().items.size(); i++) if (match.test(player.getInventory().getItem(i))) return i;
        throw new IllegalStateException("背包没有匹配物品");
    }

    private static boolean owned(Player player, ItemStack stack) {
        return Techniques.book(stack).filter(book -> book.definition().name().equals(BASIC) && book.ownedBy(player.getUUID()) && book.level() == 1).isPresent();
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private static Item cushion() {
        return item("putuan");
    }

    private static Block cushionBlock() {
        return Block.byItem(cushion());
    }
}
