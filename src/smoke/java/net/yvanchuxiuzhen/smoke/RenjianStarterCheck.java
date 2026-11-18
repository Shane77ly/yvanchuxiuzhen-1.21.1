package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.decor.DecorCushion;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.api.ShelfBooks;
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.XiulianWorldData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.client.RetreatScreen;

/** 受控供给一级藏宝书，实际开书、研习、04 装配、蒲团放置与两轮 06，最后读同一磁盘存档。 */
final class RenjianStarterCheck extends SystemCheck {
    private static final BlockPos SEAT = new BlockPos(8, 180, 8);
    private static final BlockPos STANDING = SEAT.north();
    private static final ResourceLocation CUSHION = id("putuan");
    private static final String TECHNIQUE_CAP_CORRECTION = "LevelNumberCorrect";
    private static final double SECOND_RETREAT_CORRECTION = 2;
    private final Screenshots screenshots;
    private final ResourceKey<Level> destination;
    private double gained;
    private double startLifetime;
    private double threshold;
    private double points;
    private double cooldown;
    private int selected;
    private final String shotPrefix;
    private final RenjianFirstSpellCheck firstSpell;
    private final Runnable breakthroughEvidence;
    private final BooleanSupplier readyForDiskExit;
    private boolean diskExitCompleted;

    RenjianStarterCheck(List<String> report, String levelId, Screenshots screenshots, boolean human) {
        this(report, levelId, screenshots, human, false);
    }

    RenjianStarterCheck(List<String> report, String levelId, Screenshots screenshots, boolean human, boolean withFirstSpell) {
        this(report, levelId, screenshots, human, withFirstSpell, null, null);
    }

    RenjianStarterCheck(List<String> report, String levelId, Screenshots screenshots, Runnable breakthroughEvidence,
            BooleanSupplier readyForDiskExit) {
        this(report, levelId, screenshots, true, true, breakthroughEvidence, readyForDiskExit);
    }

    private RenjianStarterCheck(List<String> report, String levelId, Screenshots screenshots, boolean human, boolean withFirstSpell,
            Runnable breakthroughEvidence, BooleanSupplier readyForDiskExit) {
        super((human ? "人间" : "主世界") + (withFirstSpell ? "首破至金芒术" : "凡人功法"), report, levelId);
        this.screenshots = screenshots;
        destination = human ? DimensionPortalSpec.RENJIAN.dimension() : Level.OVERWORLD;
        shotPrefix = human ? "renjian_starter" : "overworld_starter";
        firstSpell = withFirstSpell ? new RenjianFirstSpellCheck(this, screenshots, shotPrefix) : null;
        this.breakthroughEvidence = breakthroughEvidence;
        this.readyForDiskExit = readyForDiskExit;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            if (firstSpell != null) firstSpell.detach();
            releaseShift();
            fail("步骤 " + step + " 异常：" + exception);
            finish();
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (!readyAtPlatform() || !mainHand().is(item("book_021"))) return await(local, "未同步场地或一级藏宝书");
                useHand();
            }
            case 2 -> {
                if (!mainHand().isEmpty() || rolledIn(minecraft.player) == 0) return await(local, "未消费藏宝书或未收到随机功法");
                onServer(player -> {
                    int rollSlot = findRolledSlot(player);
                    if (rollSlot < 0) throw new IllegalStateException("缺少服务端随机功法");
                    TechniqueBook rolled = Techniques.book(player.getInventory().getItem(rollSlot)).orElseThrow();
                    expectTrue("一级表只产生一本未研习的有效功法：" + rolled.definition().name(),
                            rolled.owner().isEmpty() && rolledIn(player) == 1);
                    boolean usedRoll = rolled.definition().name().equals("五行基础");
                    ItemStack study = usedRoll ? player.getInventory().getItem(rollSlot) : Techniques.createBook(starter());
                    if (usedRoll) player.getInventory().setItem(rollSlot, ItemStack.EMPTY);
                    player.getInventory().setItem(selected, study);
                    expectTrue(usedRoll ? "研习样本沿用本次随机抽取" : "研习样本受控提供未研习书，本次随机书保留",
                            Techniques.book(study).orElseThrow().owner().isEmpty());
                });
            }
            case 3 -> {
                if (!Techniques.book(mainHand()).filter(book -> starter().name().equals(book.definition().name()) && book.owner().isEmpty()).isPresent())
                    return await(local, "未研习五行基础未同步");
                minecraft.options.keyShift.setDown(true);
                minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player,
                        ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            }
            case 4 -> {
                boolean[] sneaking = new boolean[1];
                onServer(player -> sneaking[0] = player.isShiftKeyDown());
                if (!sneaking[0]) return await(local, "潜行状态未到服务端");
                useHand();
            }
            case 5 -> {
                if (!Techniques.book(mainHand()).filter(book -> book.ownedBy(minecraft.player.getUUID()) && book.level() == 1).isPresent())
                    return await(local, "原生潜行右键未研习或未同步");
                releaseShift();
                onServer(player -> {
                    TechniqueBook learned = Techniques.book(player.getMainHandItem()).orElseThrow();
                    expectTrue("服务端研习归属为本人且一层", learned.ownedBy(player.getUUID()) && learned.level() == 1);
                    expect("首次研习经验", 0, learned.exp());
                    expect("仍为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                });
                pressPanel();
            }
            case 6 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || local < 10) return await(local, "K 未打开 04 或尚未初始同步");
                int source = screen.getMenu().slots.stream()
                        .filter(slot -> slot.container == minecraft.player.getInventory() && slot.getContainerSlot() == selected)
                        .findFirst().orElseThrow().index;
                click(screen.getMenu().containerId, source);
                click(screen.getMenu().containerId, 0);
            }
            case 7 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen)
                        || !Techniques.book(screen.getMenu().getSlot(0).getItem()).filter(book -> book.ownedBy(minecraft.player.getUUID())).isPresent())
                    return await(local, "04 功法槽实际点击未同步");
                screenshots.take(shotPrefix + "_04.png");
                minecraft.player.closeContainer();
            }
            case 8 -> {
                if (minecraft.screen != null || !mainTechniqueReady(minecraft.player)) return await(local, "正常关闭 04 后未写回功法");
                onServer(player -> {
                    expectTrue("04 关闭实际写入服务端功法槽", mainTechniqueReady(player));
                    player.getInventory().setItem(selected, item("putuan").getDefaultInstance());
                    player.setData(PanelNavigation.PAGE, 6);
                });
            }
            case 9 -> {
                if (!mainHand().is(item("putuan"))) return await(local, "供测试实际放置的蒲团未同步");
                minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND, topOf(SEAT.below()));
            }
            case 10 -> {
                if (!minecraft.level.getBlockState(SEAT).is(BuiltInRegistries.BLOCK.get(CUSHION)) || !mainHand().isEmpty())
                    return await(local, "原生放置蒲团或物品扣除未同步");
                minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND, topOf(SEAT));
            }
            case 11, 16 -> {
                if (!(minecraft.player.getVehicle() instanceof DecorCushion)) return await(local, "原生蒲团乘客未同步");
                onServer(player -> expectTrue("服务端蒲团实际乘坐", player.getVehicle() instanceof DecorCushion));
                pressPanel();
            }
            case 12, 17 -> {
                if (!(minecraft.screen instanceof RetreatScreen screen) || screen.getMenu().mode() != 6) return await(local, "K 未打开 06");
                button("增加一天");
            }
            case 13, 18 -> {
                if (Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum") != 1) return await(local, "06 设定日数未同步");
                double correction = step == 13 ? 0 : SECOND_RETREAT_CORRECTION;
                if (step == 18 && Cultivation.tizhiDouble(minecraft.player, TECHNIQUE_CAP_CORRECTION) != correction)
                    return await(local, "第二轮夹具功法门槛修正未同步");
                onServer(player -> verifyBeforeRetreat(player, "服务端", correction));
                verifyBeforeRetreat(minecraft.player, "本人", correction);
                if (step == 13) screenshots.take(shotPrefix + "_06.png");
                button("开始修炼");
            }
            case 14 -> {
                if (!settled(startLifetime - 1)) return await(local, "第一轮闭关未实际结算");
                onServer(player -> verifyFirst(player, "服务端"));
                verifyFirst(minecraft.player, "本人");
            }
            case 15 -> onServer(player -> {
                threshold = Cultivation.get(player, CultivationValue.EXPERIENCE_MAX);
                points = Cultivation.get(player, CultivationValue.SKILL_POINTS);
                // 仅缩短首破等待：不调用吸收、突破或启动修炼接口。
                Cultivation.set(player, CultivationValue.EXPERIENCE, threshold - gained * 0.5);
                // 只在第二轮跨过功法经验门槛；境界仍由实际闭关与首破流程推进。
                Cultivation.setTizhiDouble(player, TECHNIQUE_CAP_CORRECTION, SECOND_RETREAT_CORRECTION);
            });
            case 19 -> {
                if (!settled(startLifetime - 2 + 7300) || Cultivation.get(minecraft.player, CultivationValue.LEVEL_NUMBER) < 1)
                    return await(local, "第二轮未跨过真实首破门槛");
                onServer(player -> {
                    verifyBreakthrough(player, "服务端");
                    cooldown = Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN);
                    Cultivation.setTizhiDouble(player, TECHNIQUE_CAP_CORRECTION, 0);
                    player.stopRiding();
                });
                verifyBreakthrough(minecraft.player, "本人");
                if (breakthroughEvidence != null) breakthroughEvidence.run();
                screenshots.take(shotPrefix + "_breakthrough.png");
                if (readyForDiskExit == null) leaveWorld();
            }
            case 20 -> {
                if (readyForDiskExit != null && !diskExitCompleted) {
                    if (readyForDiskExit.getAsBoolean()) {
                        disconnectWorld();
                        diskExitCompleted = true;
                    }
                    return false;
                }
                return reopened(local);
            }
            case 21 -> {
                if (!inWorld(local)) return false;
                if (readyForDiskExit != null) WorldStopDiagnostics.arm(minecraft.getSingleplayerServer());
            }
            case 22 -> {
                onServer(player -> {
                    verifyBreakthrough(player, "磁盘重进服务端");
                    expect("磁盘重进服务端未保留夹具门槛修正", 0, Cultivation.tizhiDouble(player, TECHNIQUE_CAP_CORRECTION));
                    expectTrue("磁盘重进疲劳非负且只可随自然时间衰减", withinCooldown(player));
                });
                verifyBreakthrough(minecraft.player, "磁盘重进本人");
                expect("磁盘重进本人未保留夹具门槛修正", 0, Cultivation.tizhiDouble(minecraft.player, TECHNIQUE_CAP_CORRECTION));
                expectTrue("本人重进疲劳不超过离线前", withinCooldown(minecraft.player));
                if (firstSpell == null) finish();
            }
            default -> {
                if (firstSpell == null) throw new IllegalStateException("未知早期功法步骤 " + step);
                return firstSpell.advance(step - 23, local);
            }
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD) || !Techniques.slot(player).isEmpty() || rolledIn(player) != 0
                || Cultivation.get(player, CultivationValue.LEVEL_NUMBER) != 0) throw new IllegalStateException("要求单独运行的全新凡人存档");
        ServerLevel level = player.server.getLevel(destination);
        if (level == null) throw new IllegalStateException("缺少目标维度 " + destination);
        player.setGameMode(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        XiulianWorldData.get(player.server).setDailyExperience(0);
        Cultivation.set(player, CultivationValue.ROOT_GOLD, 5);
        startLifetime = Cultivation.get(player, CultivationValue.LIFETIME);
        player.setData(PanelNavigation.PAGE, 4);
        selected = player.getInventory().selected;
        for (BlockPos pos : BlockPos.betweenClosed(SEAT.offset(-2, -1, -3), SEAT.offset(2, 3, 2))) {
            level.setBlockAndUpdate(pos, pos.getY() == SEAT.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        if (player.serverLevel() == level) player.connection.teleport(8.5, 180, 7.5, 0, 30);
        else player.teleportTo(level, 8.5, 180, 7.5, 0, 30);
        player.getInventory().setItem(selected, ShelfBooks.technique(1));
        String biome = level.getBiome(SEAT).unwrapKey().orElseThrow().location().toString();
        double bonus = switch (biome) {
            case "yvanchuxiuzhen:biomecloud_02" -> 0.2;
            case "yvanchuxiuzhen:biomejade_01", "yvanchuxiuzhen:biomejade_02", "yvanchuxiuzhen:biomejade_03",
                    "yvanchuxiuzhen:biomepeach_01", "yvanchuxiuzhen:biomepeach_02" -> 0.1;
            case "yvanchuxiuzhen:biomecloud_01", "yvanchuxiuzhen:biomestone_01", "yvanchuxiuzhen:biomestone_02",
                    "yvanchuxiuzhen:biomestone_03" -> -0.1;
            case "yvanchuxiuzhen:biomecloud_03", "yvanchuxiuzhen:biomewinter_01", "yvanchuxiuzhen:biomewinter_02",
                    "yvanchuxiuzhen:biomewinter_03" -> -0.2;
            default -> 0;
        };
        gained = 2 * ((destination.equals(Level.OVERWORLD) ? 0.5 : 1) + bonus);
        expectTrue("样本场地群系 " + biome + "；期望闭关元气 " + gained, gained > 0);
    }

    private void verifyBeforeRetreat(Player player, String label, double correction) {
        String entry = label + (correction == 0 ? "原生第一轮 06 入关前" : "受控第二轮 06 入关前");
        expect(entry + "境界", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect(entry + "功法门槛修正", correction, Cultivation.tizhiDouble(player, TECHNIQUE_CAP_CORRECTION));
        verifyTechnique(player, entry, 0);
    }

    private void verifyFirst(Player player, String label) {
        expect(label + "第一轮修为实际吸收", gained, Cultivation.get(player, CultivationValue.EXPERIENCE));
        expect(label + "第一轮待吸收清零", 0, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND));
        expect(label + "第一轮仍为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect(label + "第一轮寿元", startLifetime - 1, Cultivation.get(player, CultivationValue.LIFETIME));
        verifyTechnique(player, label + "第一轮功法门槛未开放", 0);
    }

    private void verifyBreakthrough(Player player, String label) {
        expect(label + "保存维度", destination, player.level().dimension());
        expect(label + "实际首破层数", 1, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect(label + "境界名", "练气一层", Cultivation.get(player, CultivationText.LEVEL_NAME));
        expect(label + "首破后按五根重算门槛", 200, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
        expect(label + "扣旧门槛后余量", gained * 0.5, Cultivation.get(player, CultivationValue.EXPERIENCE));
        expect(label + "新增属性点", points + 5, Cultivation.get(player, CultivationValue.SKILL_POINTS));
        expect(label + "两轮寿元与首次突破增寿", startLifetime - 2 + 7300, Cultivation.get(player, CultivationValue.LIFETIME));
        verifyTechnique(player, label + "仅第二轮获得功法经验", gained * 1.5);
    }

    private void verifyTechnique(Player player, String label, double expectedExperience) {
        expectTrue(label + "：功法槽仍是本人五行基础一层", mainTechniqueReady(player));
        double experience = Techniques.main(player).map(TechniqueBook::exp).orElse(Double.NaN);
        expect(label + "：同槽功法经验", expectedExperience, experience);
    }

    private boolean withinCooldown(Player player) {
        double current = Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN);
        return current >= 0 && current <= cooldown;
    }

    private boolean settled(double expectedLifetime) {
        return minecraft.screen == null && Cultivation.get(minecraft.player, CultivationValue.CULTIVATION_DAYS) == 0
                && Cultivation.get(minecraft.player, CultivationValue.LIFETIME) == expectedLifetime;
    }

    private boolean readyAtPlatform() {
        return minecraft.level.dimension().equals(destination) && minecraft.player.distanceToSqr(8.5, 180, 7.5) < 4;
    }

    private boolean await(int local, String reason) {
        if (local > 300) {
            releaseShift();
            fail(reason);
            finish();
        }
        return false;
    }

    private ItemStack mainHand() {
        return minecraft.player.getMainHandItem();
    }

    private void releaseShift() {
        minecraft.options.keyShift.setDown(false);
        if (minecraft.player != null) {
            minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player,
                    ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    private void useHand() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void click(int menu, int slot) {
        minecraft.gameMode.handleInventoryMouseClick(menu, slot, 0, ClickType.PICKUP, minecraft.player);
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

    private static boolean mainTechniqueReady(Player player) {
        return Techniques.main(player).filter(book -> book.definition().name().equals("五行基础")
                && book.ownedBy(player.getUUID()) && book.level() == 1).isPresent();
    }

    private static boolean isTableOne(ItemStack stack) {
        return Techniques.book(stack).filter(book -> List.of("五行基础", "分金术", "静波籍", "长青功", "弄焰诀", "厚土诀",
                "清风诀", "御雷法", "阴元功", "繁星呓语").contains(book.definition().name())).isPresent();
    }

    private static long rolledIn(Player player) {
        return player.getInventory().items.stream().filter(RenjianStarterCheck::isTableOne).count();
    }

    private static int findRolledSlot(Player player) {
        for (int slot = 0; slot < 36; slot++) if (isTableOne(player.getInventory().getItem(slot))) return slot;
        return -1;
    }

    private static TechniqueDefinition starter() {
        return TechniqueLibrary.all().stream().filter(definition -> definition.name().equals("五行基础")).findFirst().orElseThrow();
    }

    private static BlockHitResult topOf(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(id(name));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name);
    }
}
