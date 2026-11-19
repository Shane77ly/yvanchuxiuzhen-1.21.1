package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.collection.decor.DecorCushion;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.client.SkillScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.client.LevelUpScreen;
import net.yvanchuxiuzhen.system.xiulian.client.RetreatScreen;

/** 从出身归档的独立副本继续：原书研习、装配与同一技能的两次原生右键。 */
final class RenjianRewardSpellCheck extends SystemCheck {
    static final String SELECTOR = "renjianrewardspell";
    static final String SAVE_ID = "renjian-reward-spell-replay";
    private static final String ARCHIVE_ID = "smoke1795016816985";
    private static final UUID PLAYER_ID = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final BlockPos SEAT = new BlockPos(8, 180, 8);
    private static final int BOOK_SLOT = 6;
    private static final int LOADOUT_SLOT = 3;
    private static final int RETREAT_DAYS = 49;
    private static final int UI_TIMEOUT = 240;
    private static final String RECOVERY_CONTROL = "smoke_reward_spell_power_recovery";
    private static final ResourceLocation PROJECTILE = id("projectile_skill_tree_01");
    private static final TagKey<Item> WEAPON = TagKey.create(Registries.ITEM, id("wuqi"));
    private static final TagKey<Item> BOW = TagKey.create(Registries.ITEM, id("yibow"));

    private final Consumer<PlayerInteractEvent.RightClickItem> beforeUse = this::beforeUse;
    private final Consumer<PlayerInteractEvent.RightClickItem> afterUse = this::afterUse;
    private final Consumer<EntityJoinLevelEvent> join = this::joined;
    private final Consumer<ClientChatReceivedEvent.System> chat = event -> {
        if (event.isOverlay() && "灵力不足".equals(ChatFormatting.stripFormatting(event.getMessage().getString()))) {
            this.insufficientMessages++;
        }
    };
    private ItemStack archived;
    private ItemStack learned;
    private double lifetime;
    private boolean shifting;
    private ServerPlayer shooter;
    private double cost;
    private double fullPower;
    private int uses;
    private int completedUses;
    private int ownProjectiles;
    private int ownLeaves;
    private int insufficientMessages;
    private CastSample before;
    private CastSample after;

    private record CastSample(int tick, double power, double tree, double fire, int projectiles, int leaves, boolean sameBook) {
        static CastSample read(ServerPlayer player, int projectiles, int leaves, ItemStack learned) {
            return new CastSample(player.tickCount, powerOf(player), Skills.cooldown(player, SkillElement.TREE),
                    Skills.cooldown(player, SkillElement.FIRE), projectiles, leaves, selected(player, learned));
        }
    }

    RenjianRewardSpellCheck(List<String> report, String levelId) {
        super("人间奖励首技回放", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            stop("阶段 " + step + " 异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        var player = minecraft.player;
        switch (step) {
            case 0 -> {
                if (!preflight()) return false;
                minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(SEAT).add(0, 0.5, 0), Direction.UP, SEAT, false));
            }
            case 1 -> {
                if (!(player.getVehicle() instanceof DecorCushion) || !serverHas(server -> server.getVehicle() instanceof DecorCushion)) {
                    return await(local, UI_TIMEOUT, "未坐上归档蒲团");
                }
                press("guicaidan");
            }
            case 2 -> {
                if (!retreatScreen()) return await(local, UI_TIMEOUT, "K 未打开 06 修炼页");
                // 归档日收益为 2；恰在最后一天跨门槛，不触发仍有剩余天数的突破惩罚。
                for (int i = 0; i < 5; i++) button("增加十天").onPress();
                button("减少一天").onPress();
            }
            case 3 -> {
                if (Cultivation.tizhiDouble(player, "xiuliandaynum") != RETREAT_DAYS
                        || !serverHas(server -> Cultivation.tizhiDouble(server, "xiuliandaynum") == RETREAT_DAYS)) {
                    return await(local, UI_TIMEOUT, "06 未选定 49 天");
                }
                button("开始修炼").onPress();
            }
            case 4 -> {
                if (minecraft.screen != null || Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) <= 0) {
                    return await(local, UI_TIMEOUT, "原生闭关未启动");
                }
                expectTrue("已坐蒲团、由 06 按钮启动闭关；未写入修为、境界、灵根或食物", true);
            }
            case 5 -> {
                if (local > 60 && Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) == 0
                        && Cultivation.get(player, CultivationValue.LEVEL_NUMBER) == 0) {
                    stop("闭关提前停止或收益不足；" + state(player));
                    return false;
                }
                if (!(minecraft.screen instanceof LevelUpScreen) || !breakthrough(player)
                        || !serverHas(this::breakthrough)) return await(local, 1500, "闭关未完成 49 天并打开练气突破界面");
                expectTrue("双方确认原生首破、寿元净变化 7300−49 且奖励书未变化；" + state(player), true);
                minecraft.screen.children().stream().filter(TextureButton.class::isInstance).map(TextureButton.class::cast)
                        .findFirst().orElseThrow().onPress();
            }
            case 6 -> {
                if (minecraft.screen != null) return await(local, UI_TIMEOUT, "突破界面未关闭");
                shift(true);
            }
            case 7 -> {
                if (player.isPassenger() || !serverHas(server -> !server.isPassenger())) return await(local, UI_TIMEOUT, "潜行未离开蒲团");
                shift(false);
                player.getInventory().selected = BOOK_SLOT;
            }
            case 8 -> {
                if (!ItemStack.matches(player.getMainHandItem(), archived)
                        || !serverHas(server -> ItemStack.matches(server.getMainHandItem(), archived))) {
                    return await(local, UI_TIMEOUT, "原背包第七格未同步到主手");
                }
                shift(true);
            }
            case 9 -> {
                if (!serverHas(server -> server.isShiftKeyDown() && ItemStack.matches(server.getMainHandItem(), archived))) {
                    return await(local, UI_TIMEOUT, "服务端未收到持原书潜行");
                }
                useHand();
            }
            case 10 -> {
                if (!ItemStack.matches(player.getMainHandItem(), learned)
                        || !serverHas(server -> ItemStack.matches(server.getMainHandItem(), learned))) {
                    return await(local, UI_TIMEOUT, "原生研习未同步，或原书除归属外的组件发生变化");
                }
                shift(false);
                expectTrue("客户端与服务端原书只新增研习归属", true);
                press("guicaidan");
            }
            case 11 -> {
                if (!retreatScreen()) return await(local, UI_TIMEOUT, "研习后 K 未重开 06");
                button("功法与技能").onPress();
            }
            case 12 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || local < 10) {
                    return await(local, UI_TIMEOUT, "06 未进入 04 装配页");
                }
                var menu = screen.getMenu();
                int source = menu.slots.stream().filter(slot -> slot.container == player.getInventory() && slot.getContainerSlot() == BOOK_SLOT)
                        .findFirst().orElseThrow().index;
                if (!ItemStack.matches(menu.getSlot(source).getItem(), learned) || !menu.getSlot(LOADOUT_SLOT).getItem().isEmpty()
                        || !menu.getCarried().isEmpty()) {
                    stop("04 背包、槽 3 或光标与原书移动前置不符");
                    return false;
                }
                click(menu, source);
                click(menu, LOADOUT_SLOT);
            }
            case 13 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen)
                        || !movedToMenu(player, screen.getMenu())
                        || !serverHas(server -> server.containerMenu instanceof SkillLoadoutMenu menu
                                && menu.containerId == screen.getMenu().containerId && movedToMenu(server, menu))) {
                    return await(local, UI_TIMEOUT, "04 双端未确认原书由背包移至槽 3");
                }
                player.closeContainer();
            }
            case 14 -> {
                if (minecraft.screen != null || !inLoadout(player) || !serverHas(this::inLoadout)) {
                    return await(local, UI_TIMEOUT, "关闭 04 后未写入技能 1");
                }
                expectTrue("双端技能 1 写回精确书栈，背包不保留副本", true);
                press("ganjian");
            }
            case 15 -> {
                if (!(minecraft.screen instanceof SkillScreen) || local < 8) return await(local, UI_TIMEOUT, "G 未打开技能选择");
                button("技能 1").onPress();
            }
            case 16 -> {
                if (minecraft.screen != null || !selected(player, learned) || !serverHas(server -> selected(server, learned))) {
                    return await(local, UI_TIMEOUT, "G 选择槽 1 后未同步原书");
                }
                onServer(this::prepareCast);
                expectTrue("原书已选定；仅施法夹具提供无词条木剑及受控灵力，预计费用=" + cost + "，满灵力=" + fullPower, true);
            }
            case 17 -> {
                if (!player.getMainHandItem().is(Items.WOODEN_SWORD) || powerOf(player) != 0) {
                    return await(local, UI_TIMEOUT, "木剑或不足灵力未同步");
                }
                useHand();
            }
            case 18 -> {
                if (insufficientMessages == 0 || !serverHas(server -> completedUses >= 1 && server.tickCount >= after.tick() + 8)) {
                    return await(local, UI_TIMEOUT, "未收到服务端右键或客户端动作栏“灵力不足”");
                }
                if (!serverHas(this::verifyInsufficient)) {
                    stop("不足分支未通过；不进入足额施放");
                    return false;
                }
                expect("客户端不足动作栏次数", 1, insufficientMessages);
                onServer(server -> Cultivation.set(server, CultivationValue.POWER, fullPower));
            }
            case 19 -> {
                if (!player.getMainHandItem().is(Items.WOODEN_SWORD) || powerOf(player) < cost) {
                    return await(local, UI_TIMEOUT, "足额灵力未同步");
                }
                useHand();
            }
            case 20 -> {
                if (!serverHas(server -> completedUses >= 2 && server.tickCount >= after.tick() + 8)) {
                    return await(local, UI_TIMEOUT, "未观察到第二次服务端右键");
                }
                onServer(this::verifyAffordable);
                expect("足额入口未额外发出不足动作栏", 1, insufficientMessages);
                detach();
                finish();
            }
            default -> throw new IllegalStateException("未知阶段 " + step);
        }
        return true;
    }

    private boolean preflight() {
        var client = minecraft.player;
        onServer(server -> {
            if (!initialState(server) || server.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
                    || !server.serverLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
                    || server.getFoodData().getFoodLevel() < 15) {
                fail("归档服务端预检不匹配：" + state(server));
                return;
            }
            archived = server.getInventory().getItem(BOOK_SLOT).copy();
            learned = archived.copy();
            learned.set(SkillData.SKILL.get(), Skills.book(archived).orElseThrow().withOwner(PLAYER_ID, server.getDisplayName().getString()));
            lifetime = Cultivation.get(server, CultivationValue.LIFETIME);
        });
        if (archived == null || minecraft.screen != null || !initialState(client)
                || !ItemStack.matches(archived, client.getInventory().getItem(BOOK_SLOT))) {
            stop("回放预检不匹配：需 " + ARCHIVE_ID + " 的首次副本；本人状态=" + state(client));
            return false;
        }
        expectTrue("归档预检：人间道生存、落魄家族、原背包唯一未研习拈花飞叶、凡人 2/100；五行基础为原归档夹具所备", true);
        return true;
    }

    private static boolean initialState(Player player) {
        ItemStack stack = player.getInventory().getItem(BOOK_SLOT);
        var technique = Cultivation.mainTechnique(player);
        var state = Skills.state(player);
        return player.getUUID().equals(PLAYER_ID) && player.isAlive() && !player.isPassenger()
                && player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension())
                && player.distanceToSqr(8.5, 180, 8.5) < 1 && player.getMainHandItem().isEmpty()
                && player.level().getBlockState(SEAT).is(Block.byItem(BuiltInRegistries.ITEM.get(id("putuan"))))
                && Cultivation.get(player, CultivationText.BIRTH).equals("落魄家族")
                && Cultivation.get(player, CultivationValue.ROOT_TREE) == 7
                && Cultivation.get(player, CultivationValue.LEVEL_NUMBER) == 0
                && Cultivation.get(player, CultivationValue.EXPERIENCE) == 2
                && Cultivation.get(player, CultivationValue.EXPERIENCE_MAX) == 100
                && Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) == 0
                && Cultivation.get(player, CultivationValue.LIFETIME) == 25549
                && Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) == 0
                && Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN) <= 1
                && Cultivation.tizhiDouble(player, "xiuliandaynum") == 0 && !Cultivation.tizhiBoolean(player, "jinji")
                && technique.present() && technique.owned() && technique.name().equals("五行基础") && technique.grade() == 1 && technique.level() == 1
                && player.getData(PanelNavigation.PAGE) == 6 && state.page() == 1 && !state.combo()
                && state.selected().isEmpty() && state.spells().stream().allMatch(ItemStack::isEmpty) && cleanCooldowns(player)
                && stack.getCount() == 1 && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id("book_012"))
                && Skills.book(stack).filter(RenjianRewardSpellCheck::sourceBook).isPresent() && countSkillBooks(player) == 1;
    }

    private static boolean sourceBook(SkillBook book) {
        return book.kind() == SkillKind.SPELL && book.name().equals("拈花飞叶") && book.owner().isEmpty()
                && book.grade() == 1 && book.level() == 1
                && book.body().equals("木") && book.neededWeapon().equals("无") && book.consumption() == 1
                && book.cooldown(SkillElement.TREE) == 5 && book.cooldownIgnore(SkillElement.TREE) == 30
                && book.cooldown(SkillElement.FIRE) == -5 && book.cooldownIgnore(SkillElement.FIRE) == 0;
    }

    private boolean breakthrough(Player player) {
        return player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension())
                && Cultivation.get(player, CultivationValue.LEVEL_NUMBER) == 1
                && Cultivation.get(player, CultivationText.LEVEL_NAME).equals("练气一层")
                && Cultivation.get(player, CultivationValue.LIFETIME) == lifetime + 7300 - RETREAT_DAYS
                && Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) == 0
                && Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) == 0
                && ItemStack.matches(archived, player.getInventory().getItem(BOOK_SLOT)) && countSkillBooks(player) == 1;
    }

    private boolean movedToMenu(Player player, AbstractContainerMenu menu) {
        return menu.getCarried().isEmpty() && countSkillBooks(player) == 0 && ItemStack.matches(menu.getSlot(LOADOUT_SLOT).getItem(), learned);
    }

    private boolean inLoadout(Player player) {
        return countSkillBooks(player) == 0 && ItemStack.matches(Skills.spell(player, 1), learned);
    }

    private static boolean selected(Player player, ItemStack learned) {
        return Skills.state(player).selectedSlot() == 1 && ItemStack.matches(Skills.selected(player), learned)
                && ItemStack.matches(Skills.spell(player, 1), learned);
    }

    private void prepareCast(ServerPlayer player) {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        if (!player.getMainHandItem().isEmpty() || !sword.is(WEAPON) || sword.is(BOW) || !cleanCooldowns(player)) {
            throw new IllegalStateException("木剑入口或初始冷却条件不满足");
        }
        // 该原版木剑没有炼器词条；来源书的消耗系数为 1。
        cost = (PlayerAttributes.baseValue(player, Stat.MANA_ATT) + Cultivation.tizhiDouble(player, "SkillConsumePower"))
                * (1 + Cultivation.tizhiDouble(player, "SkillConsumePowerPercentage"));
        fullPower = PlayerAttributes.value(player, Stat.POWER_MAX);
        if (!(cost > 0 && fullPower >= cost)) throw new IllegalStateException("无法构造两种灵力条件：" + cost + " / " + fullPower);
        double recovery = PlayerAttributes.value(player, Stat.POWER_REC)
                + fullPower * Cultivation.tizhiDouble(player, "PowerRestorePercentage");
        shooter = player;
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, beforeUse);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, afterUse);
        NeoForge.EVENT_BUS.addListener(join);
        NeoForge.EVENT_BUS.addListener(chat);
        PlayerAttributes.setContribution(player, RECOVERY_CONTROL, new Contribution(Layer.BONUS, Map.of(Stat.POWER_REC, -recovery)));
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        Cultivation.set(player, CultivationValue.POWER, 0);
    }

    private boolean verifyInsufficient(ServerPlayer player) {
        boolean ok = uses == 1 && completedUses == 1 && before.sameBook() && after.sameBook()
                && before.tick() == after.tick()
                && before.power() == 0 && after.power() == before.power() && before.tree() == 0 && before.fire() == 0
                && after.tree() == 0 && after.fire() == 0 && after.projectiles() == 0 && ownProjectiles == 0 && cleanCooldowns(player);
        expectTrue("不足不扣费、不改木火冷却、不生成本人弹体；入口=" + before + "，出口=" + after, ok);
        return ok;
    }

    private void verifyAffordable(ServerPlayer player) {
        double cooldownScale = (1 - PlayerAttributes.value(player, Stat.COOLDOWN_REDUCTION))
                * (1 + Cultivation.tizhiDouble(player, "SkillColdCorrectPercentage"));
        expectTrue("足额右键实际一次、原书未变化且从零木火冷却施放；入口=" + before + "，出口=" + after,
                uses == 2 && completedUses == 2 && before.sameBook() && after.sameBook() && before.tick() == after.tick() && before.power() >= cost
                        && before.tree() == 0 && before.fire() == 0);
        expect("同一右键事件实际扣费", cost, before.power() - after.power());
        expect("木冷却，刻", 100 * cooldownScale, after.tree());
        // 从零火冷却施放走普通累加分支，不走已有火冷却时才触发的返还分支；下一玩家刻归零。
        expect("火冷却同事件瞬时值，刻", -100 * cooldownScale, after.fire());
        expectTrue("右键事件只生成一枚同维度本人拈花飞叶弹体",
                before.projectiles() == 0 && after.projectiles() == 1 && after.leaves() == 1 && ownProjectiles == 1);
    }

    private void beforeUse(PlayerInteractEvent.RightClickItem event) {
        if (!castUse(event)) return;
        uses++;
        before = CastSample.read(shooter, ownProjectiles, ownLeaves, learned);
    }

    private void afterUse(PlayerInteractEvent.RightClickItem event) {
        if (!castUse(event)) return;
        after = CastSample.read(shooter, ownProjectiles, ownLeaves, learned);
        completedUses++;
    }

    private boolean castUse(PlayerInteractEvent.RightClickItem event) {
        return event.getEntity() == shooter && event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().is(Items.WOODEN_SWORD);
    }

    private void joined(EntityJoinLevelEvent event) {
        if (shooter != null && event.getLevel() == shooter.level() && event.getEntity() instanceof SkillProjectile shot && shot.getOwner() == shooter) {
            ownProjectiles++;
            if (EntityType.getKey(shot.getType()).equals(PROJECTILE)) ownLeaves++;
        }
    }

    private void detach() {
        if (shooter == null) return;
        onServer(player -> {
            NeoForge.EVENT_BUS.unregister(beforeUse);
            NeoForge.EVENT_BUS.unregister(afterUse);
            NeoForge.EVENT_BUS.unregister(join);
            PlayerAttributes.clearContribution(shooter, RECOVERY_CONTROL);
            shooter = null;
        });
        NeoForge.EVENT_BUS.unregister(chat);
    }

    private boolean serverHas(Predicate<ServerPlayer> condition) {
        boolean[] result = new boolean[1];
        onServer(player -> result[0] = condition.test(player));
        return result[0];
    }

    private boolean await(int local, int bound, String reason) {
        if (local > bound) stop(reason + "；" + state(minecraft.player));
        return false;
    }

    private void stop(String reason) {
        if (shifting) shift(false);
        detach();
        fail(reason);
        finish();
    }

    private static String state(Player player) {
        return "身份=" + player.getUUID() + "，维度=" + player.level().dimension().location() + "，位置=" + player.position()
                + "，层数=" + Cultivation.get(player, CultivationValue.LEVEL_NUMBER)
                + "，修为=" + Cultivation.get(player, CultivationValue.EXPERIENCE) + "/" + Cultivation.get(player, CultivationValue.EXPERIENCE_MAX)
                + "，寿元=" + Cultivation.get(player, CultivationValue.LIFETIME) + "，食物=" + player.getFoodData().getFoodLevel()
                + "，剩余闭关=" + Cultivation.get(player, CultivationValue.CULTIVATION_DAYS)
                + "，闭关冷却=" + Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN);
    }

    private boolean retreatScreen() {
        return minecraft.screen instanceof RetreatScreen screen && screen.getMenu().mode() == 6;
    }

    private Button button(String label) {
        return minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(label))
                .map(Button.class::cast).findFirst().orElseThrow(() -> new IllegalStateException("找不到按钮 " + label));
    }

    private void press(String name) {
        for (var key : minecraft.options.keyMappings) {
            if (key.getName().equals("key.yvanchuxiuzhen." + name)) {
                key.setDown(true);
                key.setDown(false);
                return;
            }
        }
        throw new IllegalStateException("缺少键位 " + name);
    }

    private void shift(boolean down) {
        shifting = down;
        minecraft.options.keyShift.setDown(down);
        minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player,
                down ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private void useHand() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void click(AbstractContainerMenu menu, int slot) {
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.PICKUP, minecraft.player);
    }

    private static long countSkillBooks(Player player) {
        return player.getInventory().items.stream().filter(stack -> Skills.book(stack).isPresent()).count()
                + player.getInventory().offhand.stream().filter(stack -> Skills.book(stack).isPresent()).count();
    }

    private static boolean cleanCooldowns(Player player) {
        for (SkillElement element : SkillElement.values()) if (Skills.cooldown(player, element) != 0) return false;
        return true;
    }

    private static double powerOf(Player player) {
        return Cultivation.get(player, CultivationValue.POWER);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
