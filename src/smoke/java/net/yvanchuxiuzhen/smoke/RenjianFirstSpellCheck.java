package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.book.api.ShelfBooks;
import net.yvanchuxiuzhen.system.skill.SkillLibrary;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.client.SkillScreen;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 同一玩家实际首破并重进后，观察原生技能施放、弹体碰撞及独立于箭伤害的灵力伤害。 */
final class RenjianFirstSpellCheck {
    private static final String GOLD_BOLT = "金芒术";
    private static final String RECOVERY_CONTROL = "smoke_first_spell_power_recovery";
    private static final ResourceLocation PROJECTILE = id("projectile_skill_gold_01");
    private static final ResourceKey<DamageType> SPELL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, id("linglidamage_player"));
    private static final TagKey<Item> WEAPON = TagKey.create(Registries.ITEM, id("wuqi"));
    private static final TagKey<Item> BOW = TagKey.create(Registries.ITEM, id("yibow"));
    private static final List<String> TABLE = List.of(GOLD_BOLT, "金刚轮", "水蛇鞭", "冰锥术", "拈花飞叶", "乱枝", "灰木剑",
            "毒蔓鞭", "风弹术", "火球术", "引火", "光弹术", "飞石术");

    private final RenjianStarterCheck host;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Screenshots screenshots;
    private final String shotPrefix;
    private final List<Cow> targets = new ArrayList<>();
    private final Set<Cow> impacted = new HashSet<>();
    private final Set<Cow> damaged = new HashSet<>();
    private final Consumer<PlayerInteractEvent.RightClickItem> beforeUse = this::beforeUse;
    private final Consumer<PlayerInteractEvent.RightClickItem> afterUse = this::afterUse;
    private final Consumer<EntityJoinLevelEvent> join = this::joined;
    private final Consumer<ProjectileImpactEvent> impact = this::impacted;
    private final Consumer<LivingDamageEvent.Post> damage = this::damaged;
    private final Consumer<LivingDeathEvent> death = this::died;
    private Cow killTarget;
    private SkillProjectile finalShot;
    private long impactTick = -1;
    private boolean survivedArrow;
    private int deaths;
    private double meritsBefore, vigorousBefore, experienceBefore, expectedExperience;
    private ServerPlayer shooter;
    private int selectedSlot;
    private int uses;
    private int completedUses;
    private int spawned;
    private double beforePower;
    private double afterPower;
    private double beforeCooldown;
    private double afterCooldown;
    private double expectedCooldown;
    private double cost;
    private double fullPower;
    private List<Integer> damagedIds = List.of();
    private int armedAt;
    private boolean observing;

    RenjianFirstSpellCheck(RenjianStarterCheck host, Screenshots screenshots, String shotPrefix) {
        this.host = host;
        this.screenshots = screenshots;
        this.shotPrefix = shotPrefix;
    }

    boolean advance(int step, int local) {
        switch (step) {
            case 0 -> host.onServer(this::prepareBook);
            case 1 -> {
                if (!isShelf(minecraft.player.getMainHandItem())) return await(local, "一级技能藏宝书未同步");
                use();
            }
            case 2 -> {
                if (!minecraft.player.getMainHandItem().isEmpty() || countRolls(minecraft.player) != 1)
                    return await(local, "开书应消费载体并同步一本自然抽取的技能书");
                host.onServer(this::chooseBook);
            }
            case 3 -> {
                if (goldBook(minecraft.player.getMainHandItem()).filter(book -> book.owner().isEmpty()).isEmpty())
                    return await(local, "未研习的金芒术未同步到主手");
                shift(true);
            }
            case 4 -> {
                boolean[] down = new boolean[1];
                host.onServer(player -> down[0] = player.isShiftKeyDown());
                if (!down[0]) return await(local, "服务端未收到潜行");
                use();
            }
            case 5 -> {
                if (!owned(minecraft.player, minecraft.player.getMainHandItem())) return await(local, "原生研习归属未同步");
                shift(false);
                host.onServer(player -> host.expectTrue("服务端原生研习的书属于本人", owned(player, player.getMainHandItem())));
                press("guicaidan");
            }
            case 6 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || local < 10)
                    return await(local, "K 未打开 04 技能装配");
                var menu = screen.getMenu();
                int source = menu.slots.stream().filter(slot -> slot.container == minecraft.player.getInventory()
                        && slot.getContainerSlot() == selectedSlot).findFirst().orElseThrow().index;
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, source, 0, ClickType.PICKUP, minecraft.player);
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, 3, 0, ClickType.PICKUP, minecraft.player);
            }
            case 7 -> {
                if (!(minecraft.screen instanceof SkillLoadoutScreen screen) || !owned(minecraft.player, screen.getMenu().getSlot(3).getItem()))
                    return await(local, "04 第一技能槽未收到研习书");
                screenshots.take(shotPrefix + "_gold_loadout.png");
                minecraft.player.closeContainer();
            }
            case 8 -> {
                if (minecraft.screen != null || !owned(minecraft.player, Skills.spell(minecraft.player, 1)))
                    return await(local, "关闭 04 后未同步槽 1");
                host.onServer(player -> host.expectTrue("服务端槽 1 实际写回", owned(player, Skills.spell(player, 1))));
                press("ganjian");
            }
            case 9 -> {
                if (!(minecraft.screen instanceof SkillScreen) || local < 8) return await(local, "G 未打开技能选择");
                screenshots.take(shotPrefix + "_gold_select.png");
                minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals("技能 1"))
                        .map(Button.class::cast).findFirst().orElseThrow().onPress();
            }
            case 10 -> {
                if (minecraft.screen != null || !owned(minecraft.player, Skills.selected(minecraft.player)))
                    return await(local, "原生按钮未选择金芒术并回传本人");
                host.onServer(this::prepareArena);
            }
            case 11 -> {
                if (!readyToShoot() || power(minecraft.player) >= cost) return await(local, "木剑、目标朝向或不足灵力未同步");
                use();
            }
            case 12 -> {
                boolean[] ready = new boolean[1];
                host.onServer(player -> {
                    if (completedUses < 1 || player.tickCount < armedAt + 10) return;
                    host.expect("不足时只到达一次服务端右键", 1, uses);
                    host.expectTrue("不足时入口读到的实际灵力低于费用", beforePower < cost);
                    host.expect("不足时不扣费", beforePower, afterPower);
                    host.expect("不足时不增加冷却", beforeCooldown, afterCooldown);
                    host.expect("不足时未生成弹体", 0, spawned);
                    host.expectTrue("不足时目标未损血", targets.stream().allMatch(cow -> cow.getHealth() == cow.getMaxHealth()));
                    fullPower = PlayerAttributes.value(player, Stat.POWER_MAX);
                    if (fullPower < cost) throw new IllegalStateException("真实练气一层灵力上限不足以施放样本");
                    Cultivation.set(player, CultivationValue.POWER, fullPower);
                    ready[0] = true;
                });
                if (!ready[0]) return await(local, "不足分支的原生木剑右键未到服务端");
            }
            case 13 -> {
                if (Math.abs(power(minecraft.player) - fullPower) > 1e-6) return await(local, "恢复到灵力上限的测试条件未同步");
                use();
            }
            case 14 -> {
                if (Skills.cooldown(minecraft.player, SkillElement.GOLD) <= 0 || Math.abs(power(minecraft.player) - (fullPower - cost)) > 1e-6)
                    return await(local, "施放后的本人金冷却或灵力扣减未同步");
                boolean[] ready = new boolean[1];
                host.onServer(player -> {
                    if (completedUses < 2 || player.tickCount < armedAt + 10) return;
                    host.expect("成功时累计两次原生右键（不足一次、成功一次）", 2, uses);
                    host.expect("成功时只生成一枚属于玩家的金芒", 1, spawned);
                    host.expect("一次施放扣除的灵力（同一服务端事件的前后快照）", cost, beforePower - afterPower);
                    host.expect("一次施放增加的金冷却（刻）", expectedCooldown, afterCooldown - beforeCooldown);
                    host.expectTrue("服务端真实弹体碰撞到布置的实体", !impacted.isEmpty());
                    host.expectTrue("碰撞目标实际受到来自玩家的灵力伤害并损血",
                            !damaged.isEmpty() && impacted.containsAll(damaged) && damaged.stream().allMatch(cow -> cow.getHealth() < cow.getMaxHealth()));
                    damagedIds = damaged.stream().map(Cow::getId).toList();
                    ready[0] = true;
                });
                if (!ready[0]) return await(local, "成功分支未收到服务端右键之后的完整弹体寿命窗口");
            }
            case 15 -> {
                if (!damagedIds.isEmpty() && damagedIds.stream().noneMatch(id -> minecraft.level.getEntity(id) instanceof Cow cow
                        && cow.getHealth() < cow.getMaxHealth())) return await(local, "客户端未收到指定命中实体的损血");
                host.expectTrue("本人同步了指定实体的损血", !damagedIds.isEmpty());
                screenshots.take(shotPrefix + "_gold_after_cast.png");
                if ("renjianfirstspell".equals(System.getProperty("yvanchuxiuzhen.smoke.only"))) {
                    host.onServer(player -> PlayerAttributes.clearContribution(player, RECOVERY_CONTROL));
                    break;
                }
                detach();
                host.finish();
            }
            case 16 -> {
                if (!readyToShoot() || Skills.cooldown(minecraft.player, SkillElement.GOLD) > 0 || power(minecraft.player) < cost)
                    return await(local, "收尾施放的木剑、朝向、自然冷却或灵力未就绪");
                host.onServer(this::prepareKill);
                use();
            }
            case 17 -> {
                boolean[] observed = {false};
                host.onServer(player -> {
                    if (completedUses < 3 || player.tickCount < armedAt + 12) return;
                    host.expect("收尾只增加一次原生右键", 3, uses);
                    host.expect("收尾只增加一枚本人金芒", 2, spawned);
                    host.expectTrue("收尾牛被本次金芒命中并扛过箭伤", impactTick >= 0 && survivedArrow);
                    host.expect("同一头牛死亡一次（当前生命 " + killTarget.getHealth() + "）", 1, deaths);
                    if (deaths != 1 || killTarget.isAlive()) throw new IllegalStateException("收尾未形成目标死亡，不进入吸收检查");
                    observed[0] = true;
                });
                if (!observed[0]) return await(local, "未收到收尾右键或弹体寿命窗口未结束");
            }
            case 18 -> {
                if (!absorbed(minecraft.player)) return await(local, "未收到金芒击杀后的正常吸收、功德或罡煞同步");
                host.onServer(player -> host.expectTrue("服务端正常吸收且与本人收益一致", absorbed(player)));
                detach();
                host.finish();
            }
            default -> throw new IllegalStateException("未知首技步骤 " + step);
        }
        return true;
    }

    private void prepareBook(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) != 1 || Cultivation.get(player, CultivationValue.ROOT_GOLD) < 1
                || Skills.state(player).page() != 1 || !Skills.spell(player, 1).isEmpty() || !Skills.selected(player).isEmpty()
                || !player.getMainHandItem().isEmpty() || countRolls(player) != 0)
            throw new IllegalStateException("首技要求同一玩家首破重进后维持金根、空主手和未装配技能");
        selectedSlot = player.getInventory().selected;
        player.getInventory().setItem(selectedSlot, ShelfBooks.skill(1));
        player.setData(PanelNavigation.PAGE, 4);
    }

    private void chooseBook(ServerPlayer player) {
        if (countRolls(player) != 1 || !player.getMainHandItem().isEmpty()) throw new IllegalStateException("服务端抽签结果或载体扣除不符");
        int slot = -1;
        for (int index = 0; index < 36; index++) {
            if (tableBook(player.getInventory().getItem(index))) {
                slot = index;
                break;
            }
        }
        ItemStack roll = player.getInventory().getItem(slot);
        SkillBook drawn = Skills.book(roll).orElseThrow();
        host.expectTrue("自然抽得一本未研习技能：" + drawn.name(), drawn.owner().isEmpty());
        ItemStack sample = roll;
        if (drawn.name().equals(GOLD_BOLT)) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        } else {
            // 仅保证验收样本：保留玩家实际抽中的书，用独立随机源在同一生产表取未研习金芒术。
            RandomSource fixtureRandom = RandomSource.create(0);
            for (int index = 0; index < 128; index++) {
                sample = SkillLibrary.treasure(fixtureRandom);
                if (goldBook(sample).isPresent()) break;
            }
        }
        SkillBook book = goldBook(sample).orElseThrow();
        host.expectTrue(drawn.name().equals(GOLD_BOLT) ? "样本沿用本次随机抽取" : "样本由夹具补充，本次随机书保留",
                book.owner().isEmpty() && book.level() == 1 && book.body().equals("金") && book.neededWeapon().equals("无"));
        player.getInventory().setItem(selectedSlot, sample);
    }

    private void prepareArena(ServerPlayer player) {
        if (!owned(player, Skills.selected(player)) || Skills.state(player).selectedSlot() != 1 || Skills.cooldown(player, SkillElement.GOLD) != 0)
            throw new IllegalStateException("服务端未选择第一槽金芒术或冷却初值非零");
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        if (!sword.is(WEAPON) || sword.is(BOW)) throw new IllegalStateException("木剑未归入来源允许施法的武器标签");
        player.getInventory().setItem(selectedSlot, sword);
        var level = player.serverLevel();
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(6, 179, -4), new BlockPos(10, 183, 8))) {
            level.setBlockAndUpdate(pos, pos.getY() == 179 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        player.connection.teleport(8.5, 180, 7.5, 180, 5);
        for (double z : new double[]{4.5, 2.5, 0.5}) {
            Cow cow = EntityType.COW.create(level);
            if (cow == null) throw new IllegalStateException("无法创建测试目标");
            cow.setNoAi(true);
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
            cow.setHealth(40);
            cow.moveTo(8.5, 180, z, 0, 0);
            if (!level.addFreshEntity(cow)) throw new IllegalStateException("测试目标未加入同维度");
            targets.add(cow);
        }
        cost = expectedCost(player);
        if (!(cost > 0)) throw new IllegalStateException("金芒术需要非零实际费用才能验证不足分支");
        // 不让每秒恢复覆盖不足和扣费两端的快照；只在观察窗口抵消灵力恢复贡献。
        double naturalRecovery = PlayerAttributes.value(player, Stat.POWER_REC)
                + PlayerAttributes.value(player, Stat.POWER_MAX) * Cultivation.tizhiDouble(player, "PowerRestorePercentage");
        PlayerAttributes.setContribution(player, RECOVERY_CONTROL, new Contribution(Layer.BONUS, Map.of(Stat.POWER_REC, -naturalRecovery)));
        Cultivation.set(player, CultivationValue.POWER, 0);
        shooter = player;
        observing = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, beforeUse);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, afterUse);
        NeoForge.EVENT_BUS.addListener(join);
        NeoForge.EVENT_BUS.addListener(impact);
        NeoForge.EVENT_BUS.addListener(damage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, death);
    }

    private void prepareKill(ServerPlayer player) {
        if (!player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension()) || !owned(player, Skills.selected(player)))
            throw new IllegalStateException("收尾需要同一人间道玩家仍选中本人金芒术");
        targets.forEach(Cow::discard);
        killTarget = EntityType.COW.create(player.serverLevel());
        if (killTarget == null) throw new IllegalStateException("无法创建收尾普通牛");
        killTarget.setNoAi(true);
        killTarget.moveTo(8.5, 180, 4.5, 0, 0);
        // 保留原生生命上限；1 点箭伤不能单独致死，剩余生命由真实法伤结算。
        killTarget.setHealth(1.25F);
        if (!player.serverLevel().addFreshEntity(killTarget)) throw new IllegalStateException("收尾普通牛未加入场地");
        host.expect("普通牛实际生命上限", 10, killTarget.getMaxHealth());
        host.expect("普通牛层数", 0, EntityAttributes.level(killTarget));
        host.expect("收尾前待吸收为零", 0, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND));
        meritsBefore = Cultivation.get(player, CultivationValue.MERITS);
        vigorousBefore = Cultivation.get(player, CultivationValue.VIGOROUS);
        experienceBefore = Cultivation.get(player, CultivationValue.EXPERIENCE);
        expectedExperience = experienceBefore + 2 * Cultivation.get(player, CultivationValue.EXPERIENCE_RATE);
        host.expectTrue("收尾时修为效率为正", expectedExperience > experienceBefore);
    }

    private boolean absorbed(Player player) {
        return Math.abs(Cultivation.get(player, CultivationValue.EXPERIENCE) - expectedExperience) < 1e-6
                && Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) == 0
                && Cultivation.get(player, CultivationValue.MERITS) == meritsBefore - 1
                && Cultivation.get(player, CultivationValue.VIGOROUS) == vigorousBefore;
    }

    private static double expectedCost(ServerPlayer player) {
        // 来源系数为 1；原版无附魔木剑无炼器组件，不提供额外 mana_Att。
        return (PlayerAttributes.baseValue(player, Stat.MANA_ATT) + Cultivation.tizhiDouble(player, "SkillConsumePower"))
                * (1 + Cultivation.tizhiDouble(player, "SkillConsumePowerPercentage"));
    }

    private void beforeUse(PlayerInteractEvent.RightClickItem event) {
        if (isCastUse(event)) {
            uses++;
            armedAt = shooter.tickCount;
            beforePower = power(shooter);
            beforeCooldown = Skills.cooldown(shooter, SkillElement.GOLD);
            expectedCooldown = 100 * (1 - PlayerAttributes.value(shooter, Stat.COOLDOWN_REDUCTION))
                    * (1 + Cultivation.tizhiDouble(shooter, "SkillColdCorrectPercentage"));
        }
    }

    private void afterUse(PlayerInteractEvent.RightClickItem event) {
        if (isCastUse(event)) {
            completedUses++;
            afterPower = power(shooter);
            afterCooldown = Skills.cooldown(shooter, SkillElement.GOLD);
        }
    }

    private boolean isCastUse(PlayerInteractEvent.RightClickItem event) {
        return event.getEntity() == shooter && event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().is(Items.WOODEN_SWORD);
    }

    private void joined(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof SkillProjectile shot && ownShot(shot)) {
            spawned++;
            if (killTarget != null) finalShot = shot;
        }
    }

    private void impacted(ProjectileImpactEvent event) {
        if (finalShot != null && event.getProjectile() == finalShot && event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() == killTarget) impactTick = shooter.level().getGameTime();
        if (event.getProjectile() instanceof SkillProjectile shot && ownShot(shot)
                && event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof Cow cow && targets.contains(cow)) {
            impacted.add(cow);
        }
    }

    private void damaged(LivingDamageEvent.Post event) {
        if (event.getEntity() == killTarget && event.getSource().is(DamageTypes.ARROW) && event.getSource().getDirectEntity() == finalShot
                && event.getSource().getEntity() == shooter && event.getNewDamage() > 0) survivedArrow = killTarget.isAlive();
        if (event.getEntity() instanceof Cow cow && targets.contains(cow) && event.getSource().is(SPELL_DAMAGE)
                && event.getSource().getEntity() == shooter && event.getNewDamage() > 0) damaged.add(cow);
    }

    private void died(LivingDeathEvent event) {
        if (event.getEntity() != killTarget) return;
        deaths++;
        host.expectTrue("收尾牛在金芒命中同刻由本人法伤致死，实际来源=" + event.getSource().getMsgId(),
                survivedArrow && impactTick == shooter.level().getGameTime() && event.getSource().is(SPELL_DAMAGE)
                        && event.getSource().getEntity() == shooter);
        host.expect("死亡当刻待吸收修为只增加一头普通牛的收益", 2, Cultivation.get(shooter, CultivationValue.EXPERIENCE_STAND));
        host.expect("死亡当刻仍未吸收", experienceBefore, Cultivation.get(shooter, CultivationValue.EXPERIENCE));
        host.expect("死亡当刻功德减一", meritsBefore - 1, Cultivation.get(shooter, CultivationValue.MERITS));
        host.expect("练气一层击杀无层数牛不扣罡煞", vigorousBefore, Cultivation.get(shooter, CultivationValue.VIGOROUS));
    }

    private boolean ownShot(SkillProjectile shot) {
        return shot.level() == shooter.level() && shot.getOwner() == shooter && EntityType.getKey(shot.getType()).equals(PROJECTILE);
    }

    void detach() {
        if (!observing) return;
        NeoForge.EVENT_BUS.unregister(beforeUse);
        NeoForge.EVENT_BUS.unregister(afterUse);
        NeoForge.EVENT_BUS.unregister(join);
        NeoForge.EVENT_BUS.unregister(impact);
        NeoForge.EVENT_BUS.unregister(damage);
        NeoForge.EVENT_BUS.unregister(death);
        observing = false;
        host.onServer(player -> PlayerAttributes.clearContribution(player, RECOVERY_CONTROL));
    }

    private boolean readyToShoot() {
        return minecraft.player.getMainHandItem().is(Items.WOODEN_SWORD)
                && minecraft.player.distanceToSqr(8.5, 180, 7.5) < 0.25 && minecraft.player.getLookAngle().z < -0.99;
    }

    private boolean await(int local, String reason) {
        if (local > 300) {
            detach();
            shift(false);
            host.fail(reason);
            host.finish();
        }
        return false;
    }

    private void use() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void shift(boolean down) {
        minecraft.options.keyShift.setDown(down);
        minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player, down
                ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
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

    private static boolean isShelf(ItemStack stack) {
        TreasureContainerConfig config = TreasureContainers.config(stack);
        return config.category().equals("书架") && config.subtype().equals("技能书") && config.level() == 1;
    }

    private static Optional<SkillBook> goldBook(ItemStack stack) {
        return Skills.book(stack).filter(book -> book.name().equals(GOLD_BOLT));
    }

    private static boolean owned(Player player, ItemStack stack) {
        return goldBook(stack).filter(book -> book.ownedBy(player.getUUID())).isPresent();
    }

    private static boolean tableBook(ItemStack stack) {
        return Skills.book(stack).filter(book -> TABLE.contains(book.name())).isPresent();
    }

    private static long countRolls(Player player) {
        return player.getInventory().items.stream().filter(RenjianFirstSpellCheck::tableBook).count();
    }

    private static double power(Player player) {
        return Cultivation.get(player, CultivationValue.POWER);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name);
    }
}
