package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.DamageElement;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.combat.CombatSetup;
import net.yvanchuxiuzhen.system.combat.content.DummyMenu;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyEntity;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyItem;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 战斗系统的实机检查：以铁傀儡（无 AI，100 点生命，法术防御 3、神识防御 2）为靶，核对灵力与神识伤害的结算数值、
 * 命令 {@code /shanghai}、闪避、手下留情，以及 Q18/Q29/Q30 修复与加点、恢复、毒药效果在服务端的结果。
 * 期望值按 docs/systems/combat/architecture.md 的公式手算。
 */
final class CombatCheck extends SystemCheck {
    private IronGolem poisoned;
    private long effectsStart;
    private IronGolem mandrake;
    private IronGolem bystander;
    private IronGolem effectLifecycle;
    private double resistanceBefore;
    private long lifecycleStart;

    private final Screenshots screenshots;
    private WoodenDummyEntity dummy;

    CombatCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("战斗", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::damage);
            case 1 -> {
                long[] now = new long[1];
                onServer(player -> now[0] = player.serverLevel().getGameTime());
                if (now[0] - lifecycleStart < 6) {
                    return false;
                }
                onServer(player -> {
                    MobEffectInstance unyielding = effectLifecycle.getEffect(StatusEffects.holder("skilleffect_08"));
                    MobEffectInstance glowing = effectLifecycle.getEffect(MobEffects.GLOWING);
                    expectTrue("强霸体结束后恢复隐藏弱效果并重新附加同刻发光", unyielding != null && unyielding.getAmplifier() == 0
                            && glowing != null && Math.abs(unyielding.getDuration() - glowing.getDuration()) <= 1);
                    effectLifecycle.removeAllEffects();
                    expect("清除恢复后的霸体还原击退抗性", resistanceBefore, effectLifecycle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
                    effectLifecycle.discard();
                    expect("瞬间灵力恢复 灵力", 105, Cultivation.get(player, CultivationValue.POWER));
                    expectTrue("瞬间灵力恢复后效果移除", !StatusEffects.has(player, "effect_011"));
                    expect("永久生命 已加点次数", 3, Cultivation.get(player, CultivationValue.SKILL_POINTS_HEALTH_MAX));
                    expectTrue("永久生命后效果移除", !StatusEffects.has(player, "effect_008"));
                });
            }
            case 2 -> {
                long[] now = new long[1];
                onServer(player -> now[0] = player.serverLevel().getGameTime());
                if (now[0] - effectsStart < 105) {
                    return false;
                }
                onServer(player -> {
                    near("断肠 100 刻后 生命", 100 - 8, poisoned.getHealth());
                    near("曼陀罗 两个周期的阴伤害 2 × 12²/(12+3)", 100 - 2 * 144.0 / 15, mandrake.getHealth());
                    expectTrue("曼陀罗把烙毒传给身边的生物", bystander.hasEffect(StatusEffects.holder("duyao_14")));
                    poisoned.discard();
                    mandrake.discard();
                    bystander.discard();
                    verifyStage4CombatFixes(player, player.serverLevel());
                    clean(player);
                    dummy(player);
                });
            }
            case 3 -> {
                if (local < 5) {
                    return false;
                }
                onServer(player -> expect("木人桩受击后下一刻回满生命", 1024, dummy.getHealth()));
            }
            case 4 -> {
                if (local < 40) {
                    return false;
                }
                screenshots.take("combat_dummy.png");
                onServer(player -> {
                    expect("木人桩受击后回满生命", 1024, dummy.getHealth());
                    dummy.discard();
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    /** 木人桩：用物品放置，受击后下一刻回满生命；设置界面按钮、装备槽与拿走木人桩（留一个用于截图）。 */
    private void dummy(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos ground = player.blockPosition().offset(0, 0, 4);
        prepareSpace(level, CombatSetup.<WoodenDummyEntity>entityType("murenzhuang").get().getDimensions()
                .makeBoundingBox(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5));
        ItemStack item = new ItemStack(WoodenDummyItem.item(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        player.gameMode.useItemOn(player, level, item, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false));
        List<WoodenDummyEntity> found = level.getEntitiesOfClass(WoodenDummyEntity.class, new AABB(ground).inflate(2));
        expect("木人桩物品放置出一个木人桩", 1, found.size());
        expect("放置消耗一个木人桩物品（创造模式不消耗）", player.isCreative() ? 2 : 1, player.getMainHandItem().getCount());
        dummy = found.get(0);
        dummy.setHealth((float) dummy.getMaxHealth());
        expect("木人桩最大生命", 1024, dummy.getMaxHealth());
        EntityAttributes.setStat(dummy, Stat.MANA_DEF, 0);
        double dealt = hit(player, dummy, new Shanghai.Request(DamageCategory.NONE, player.getUUID(), true, 6, 1));
        near("木人桩受击 6²/(6+0)", 6, dealt);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        DummyMenu.open(player, dummy);
        expectTrue("潜行右键打开设置界面", player.containerMenu instanceof DummyMenu);
        DummyMenu menu = (DummyMenu) player.containerMenu;
        menu.press(player, 0, "12.5");
        menu.press(player, 1, "7");
        menu.press(player, 2, "不是数字");
        expect("设置物理防御基值", 12.5, EntityAttributes.stat(dummy, Stat.ATTACK_DEF));
        expect("设置法术防御基值", 7, EntityAttributes.stat(dummy, Stat.MANA_DEF));
        expect("数字解析失败设为 0", 0, EntityAttributes.stat(dummy, Stat.SOUL_DEF));
        expect("界面标签读到玩家体质里的物理防御基值", 12.5, Cultivation.tizhiDouble(player, "attack_Def"));
        menu.getSlot(0).set(new ItemStack(Items.IRON_HELMET));
        player.closeContainer();
        expect("关闭界面后头盔穿在木人桩上", Items.IRON_HELMET, dummy.getItemBySlot(EquipmentSlot.HEAD).getItem());

        WoodenDummyEntity second = CombatSetup.<WoodenDummyEntity>entityType("murenzhuang").get().create(level);
        second.moveTo(ground.getX() + 3.5, ground.getY() + 1, ground.getZ() + 0.5, 0, 0);
        level.addFreshEntity(second);
        DummyMenu.open(player, second);
        ((DummyMenu) player.containerMenu).press(player, 3, "");
        expectTrue("拿走木人桩后木人桩被移除", second.isRemoved());
        expectTrue("拿走木人桩后玩家得到一个木人桩物品", player.getInventory().contains(new ItemStack(WoodenDummyItem.item())));
        expectTrue("拿走木人桩后界面关闭", !(player.containerMenu instanceof DummyMenu));
        player.getInventory().clearContent();
        player.connection.teleport(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() - 4, 0, 5);
    }

    private void near(String label, double expected, double actual) {
        expectTrue(label + " 期望 " + expected + " 实际 " + actual, Math.abs(expected - actual) < 1e-3);
    }

    private IronGolem golem(ServerPlayer player, double health) {
        return golem(player, health, 3);
    }

    private IronGolem golem(ServerPlayer player, double health, double offsetX) {
        ServerLevel level = player.serverLevel();
        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        golem.moveTo(player.getX() + offsetX, player.getY(), player.getZ(), 0, 0);
        prepareSpace(level, golem.getBoundingBox());
        golem.setNoAi(true);
        golem.setHealth((float) health);
        EntityAttributes.setStat(golem, Stat.MANA_DEF, 3);
        EntityAttributes.setStat(golem, Stat.SOUL_DEF, 2);
        level.addFreshEntity(golem);
        return golem;
    }

    /** 清空实体碰撞体占用的方块，并在完整底面铺设地基，隔离随机地形的碰撞与窒息伤害。 */
    private void prepareSpace(ServerLevel level, AABB bounds) {
        BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos max = BlockPos.containing(Math.nextDown(bounds.maxX), Math.nextDown(bounds.maxY), Math.nextDown(bounds.maxZ));
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        for (BlockPos pos : BlockPos.betweenClosed(min.below(), new BlockPos(max.getX(), min.getY() - 1, max.getZ()))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** 阶段 4 战斗修复核对：Q18 受击功法减伤及不反转正伤、Q29 体质防御修正、Q30 物理破甲/攻击体质/法防mana_Def/空队不免伤。 */
    private void verifyStage4CombatFixes(ServerPlayer player, ServerLevel level) {
        double savedManaDef = PlayerAttributes.value(player, Stat.MANA_DEF);
        double savedSoulDef = PlayerAttributes.value(player, Stat.SOUL_DEF);
        double fireDefCorrect = Cultivation.tizhiDouble(player, "FireDefCorrect");
        double fireDefPct = Cultivation.tizhiDouble(player, "FireDefCorrectPercentage");
        double manaDefPct = Cultivation.tizhiDouble(player, "ManaDefDefCorrectPercentage");
        double soulJingPct = Cultivation.tizhiDouble(player, "SouldefofjingAttCorrectPercentage");
        double attackAttPct = Cultivation.tizhiDouble(player, "AttackAttCorrectPercentage");

        IronGolem caster = golem(player, 100);
        EntityAttributes.setStat(caster, Stat.MANA_ATT, 0);
        GameType prevMode = player.gameMode.getGameModeForPlayer();
        Difficulty prevDifficulty = level.getDifficulty();
        try {
            player.setGameMode(GameType.SURVIVAL);
            level.getServer().setDifficulty(Difficulty.NORMAL, true);
            Cultivation.setTizhiDouble(player, "FireDefCorrect", 0);
            Cultivation.setTizhiDouble(player, "FireDefCorrectPercentage", 0);
            Cultivation.setTizhiDouble(player, "ManaDefDefCorrectPercentage", 0);
            Cultivation.setTizhiDouble(player, "SouldefofjingAttCorrectPercentage", 0);
            player.setHealth(player.getMaxHealth());

            double q18Before = player.getHealth();
            player.invulnerableTime = 0;
            Shanghai.deal(level, player, new Shanghai.Request(DamageCategory.FIRE, caster.getUUID(), false, 13, 1));
            near("Q18 玩家火功法减伤 13 − 10 = 3", 9.0 / (3.0 + savedManaDef), q18Before - player.getHealth());

            double overMitigatedBefore = player.getHealth();
            player.invulnerableTime = 0;
            Shanghai.deal(level, player, new Shanghai.Request(DamageCategory.FIRE, caster.getUUID(), false, 8, 1));
            near("Q18 减伤超出后不反转正伤 8 − 10 ≤ 0", 0, overMitigatedBefore - player.getHealth());

            Cultivation.setTizhiDouble(player, "FireDefCorrect", 3);
            Cultivation.setTizhiDouble(player, "ManaDefDefCorrectPercentage", 1);
            double q29FireBefore = player.getHealth();
            player.invulnerableTime = 0;
            Shanghai.deal(level, player, new Shanghai.Request(DamageCategory.FIRE, null, true, 6, 1));
            near("Q29 体质火防修正+法防倍率", 36.0 / (6.0 + (3.0 + savedManaDef) * 2.0), q29FireBefore - player.getHealth());

            Cultivation.setTizhiDouble(player, "SouldefofjingAttCorrectPercentage", 1);
            double q29SoulBefore = player.getHealth();
            player.invulnerableTime = 0;
            Shanghai.deal(level, player, new Shanghai.Request(DamageCategory.SOUL, null, true, 12, 1));
            near("Q29 体质神识防御倍率", Math.max(0, 12.0 - savedSoulDef * 2.0), q29SoulBefore - player.getHealth());

            IronGolem armorTarget = golem(player, 100, 6);
            EntityAttributes.setStat(armorTarget, Stat.ATTACK_DEF, 4);
            ItemStack statsHelm = new ItemStack(Items.IRON_HELMET);
            statsHelm.set(EquipmentData.STATS.get(), new EquipmentStats(0, 0, 0, 6, 10, 0, 0, 0, 0, 0, 99, 0, 0));
            armorTarget.setItemSlot(EquipmentSlot.HEAD, statsHelm);
            double manaDefDmg = hit(player, armorTarget, new Shanghai.Request(DamageCategory.FIRE, null, true, 6, 1));
            near("Q30 护甲法防读 mana_Def(=10) 而非 mana_Att(=99)", 36.0 / (6.0 + 3.0 + 10.0), manaDefDmg);

            Cultivation.setTizhiDouble(player, "AttackAttCorrectPercentage", 1);
            double physBefore = armorTarget.getHealth();
            armorTarget.invulnerableTime = 0;
            armorTarget.hurt(armorTarget.damageSources().playerAttack(player), 4);
            near("Q30 物理无破甲保留防御(4+6)且读攻击者 AttackAttCorrectPercentage(+100%)", 64.0 / (8.0 + 10.0),
                    physBefore - armorTarget.getHealth());

            player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(level, Items.STICK, Map.of("critical", 50, "criticalatt", 5)));
            double critBefore = armorTarget.getHealth();
            armorTarget.invulnerableTime = 0;
            armorTarget.hurt(armorTarget.damageSources().playerAttack(player), 4);
            // 4 × (1.5 + 5×0.2) × (1 + 1) = 20，公式 20² / (20 + 10)
            near("Q30 物理暴击倍率生效 20²/(20+10)", 400.0 / 30.0, critBefore - armorTarget.getHealth());
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

            Relations.setTeam(caster, "smoketeam");
            Relations.addFriendlyTeam(caster, "");
            double emptyTeamDmg = hit(player, armorTarget, new Shanghai.Request(DamageCategory.NONE, caster.getUUID(), true, 6, 1));
            expectTrue("Q30 空团队不触发友方免伤 实际 " + emptyTeamDmg, emptyTeamDmg > 0);
            armorTarget.discard();
        } finally {
            caster.discard();
            Cultivation.setTizhiDouble(player, "FireDefCorrect", fireDefCorrect);
            Cultivation.setTizhiDouble(player, "FireDefCorrectPercentage", fireDefPct);
            Cultivation.setTizhiDouble(player, "ManaDefDefCorrectPercentage", manaDefPct);
            Cultivation.setTizhiDouble(player, "SouldefofjingAttCorrectPercentage", soulJingPct);
            Cultivation.setTizhiDouble(player, "AttackAttCorrectPercentage", attackAttPct);
            level.getServer().setDifficulty(prevDifficulty, true);
            player.setGameMode(prevMode);
            player.setHealth(player.getMaxHealth());
        }
    }

    /** 结算并返回造成的伤害；闪避随机数为 0 时重试。 */
    private double hit(ServerPlayer player, LivingEntity golem, Shanghai.Request request) {
        double before = golem.getHealth();
        for (int attempt = 0; attempt < 5 && golem.getHealth() == before; attempt++) {
            golem.invulnerableTime = 0;
            Shanghai.deal(player.serverLevel(), golem, request);
        }
        return before - golem.getHealth();
    }

    private void clean(ServerPlayer player) {
        for (String effect : List.of("effect_013", "effect_008", "effect_011", "duyao_14")) {
            StatusEffects.remove(player, effect);
        }
        PlayerAttributes.clearContribution(player, "smoke:combat");
        PlayerAttributes.clearContribution(player, "smoke:combat_gongfa");
    }

    private ItemStack enchanted(ServerLevel level, Item item, Map<String, Integer> levels) {
        ItemStack stack = new ItemStack(item);
        levels.forEach((name, value) -> stack.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key(name)), value));
        return stack;
    }

    private ItemStack enchanted(ServerLevel level, Map<String, Integer> levels) {
        return enchanted(level, Items.DIAMOND_SWORD, levels);
    }

    /** 连锁采集一次挖掉相连的同种方块；穿石把基岩换成破碎的基岩，之后每次破坏降一级。 */
    private void blocks(ServerPlayer player, ServerLevel level) {
        BlockPos base = player.blockPosition().offset(8, 0, 0);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    level.setBlock(base.offset(x, y, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(level, Items.DIAMOND_PICKAXE, Map.of("chain", 1)));
        player.gameMode.destroyBlock(base);
        int left = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    left += level.getBlockState(base.offset(x, y, z)).is(Blocks.STONE) ? 1 : 0;
                }
            }
        }
        expect("连锁采集 2×2×2 石头挖掉一块后剩余", 0, left);
        expectTrue("连锁采集损耗镐的耐久", player.getMainHandItem().getDamageValue() > 1);

        BlockPos rock = base.offset(0, 0, 4);
        level.setBlock(rock.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(rock, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
        player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(level, Items.DIAMOND_PICKAXE, Map.of("chuanshi", 1)));
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, rock, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.START));
        expect("穿石 基岩变为", "bedrock_01", level.getBlockState(rock).getBlock() == BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "bedrock_01")) ? "bedrock_01" : "其他");
        expectTrue("穿石 给自己急迫", player.hasEffect(MobEffects.DIG_SPEED));
        expect("穿石 镐损耗 10 点", 10, player.getMainHandItem().getDamageValue());
        for (String stage : List.of("bedrock_02", "bedrock_03")) {
            player.gameMode.destroyBlock(rock);
            expect("破坏后升一级", stage, level.getBlockState(rock).getBlock() == BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, stage)) ? stage : "其他");
        }
        player.gameMode.destroyBlock(rock);
        expectTrue("第三阶段破坏后消失", level.getBlockState(rock).isAir());
        player.removeEffect(MobEffects.DIG_SPEED);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        level.setBlock(rock.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** 命中附魔：护甲附魔对受击者身边的非友好生物发固定火伤害；主手附魔的控制效果与灵体杀手（取重伤的等级）。 */
    private void enchantments(ServerPlayer player, ServerLevel level) {
        IronGolem wearer = golem(player, 100);
        Relations.setTeam(wearer, "smoketeam");
        Relations.addFriendlyTeam(wearer, "smoketeam");
        wearer.setItemSlot(EquipmentSlot.HEAD, enchanted(level, Map.of("firearmor", 100)));
        IronGolem neighbour = golem(player, 100);
        neighbour.moveTo(wearer.getX() + 1, wearer.getY(), wearer.getZ(), 0, 0);
        float playerHealth = player.getHealth();
        hit(player, wearer, new Shanghai.Request(DamageCategory.NONE, player.getUUID(), true, 6, 1));
        expectTrue("烈焰甲 100 级 身边的非友好生物被固定火伤害击杀", !neighbour.isAlive());
        expectTrue("烈焰甲 穿戴者自己是友方，不受自己的火伤害", wearer.getHealth() > 90);
        expect("烈焰甲 范围外的玩家不受伤", playerHealth, player.getHealth());
        wearer.discard();
        neighbour.discard();

        player.setItemInHand(InteractionHand.MAIN_HAND, enchanted(level, Map.of("threaten", 100, "lingtikiller", 1, "zhongshang", 1)));
        IronGolem target = golem(player, 100);
        hit(player, target, new Shanghai.Request(DamageCategory.NONE, player.getUUID(), true, 6, 1));
        expectTrue("威慑 100 级 目标失明", target.hasEffect(MobEffects.BLINDNESS));
        expectTrue("灵体杀手取重伤的等级 1 × 2 点，加上伤害 4，至少损失 6 点生命 实际 " + target.getHealth(), target.getHealth() <= 94 + 1e-3);
        target.discard();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    /** 原生属性钩子的时序回归：首次/刷新现读时长、弱效果不重放开始、强制施加和隐藏效果恢复。 */
    private void effectLifecycle(ServerPlayer player) {
        effectLifecycle = golem(player, 100, -32);
        resistanceBefore = effectLifecycle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue();
        effectLifecycle.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 80, 0));
        expect("首次霸体写入后发光时长为80刻", 80, effectLifecycle.getEffect(MobEffects.GLOWING).getDuration());
        effectLifecycle.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 240, 1));
        expect("刷新霸体按更新后的240刻附加发光", 240, effectLifecycle.getEffect(MobEffects.GLOWING).getDuration());
        expect("刷新霸体先还原再应用，击退抗性只增加一次", resistanceBefore + 10,
                effectLifecycle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
        effectLifecycle.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 40, 0));
        expect("无效弱施加不重放开始过程", 240, effectLifecycle.getEffect(MobEffects.GLOWING).getDuration());
        effectLifecycle.removeAllEffects();
        expect("批量清除霸体还原击退抗性", resistanceBefore, effectLifecycle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
        effectLifecycle.forceAddEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 80, 0), null);
        expect("原生强制施加同样执行开始钩子", 80, effectLifecycle.getEffect(MobEffects.GLOWING).getDuration());
        effectLifecycle.removeAllEffects();
        effectLifecycle.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 3, 1));
        effectLifecycle.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 120, 0));
        lifecycleStart = player.serverLevel().getGameTime();
    }

    private void compareSavedDamage(ServerPlayer player, String owner, boolean fixed) {
        IronGolem reference = golem(player, 100);
        IronGolem direct = golem(player, 100);
        reference.getRandom().setSeed(1);
        direct.getRandom().setSeed(1);
        player.server.getCommands().performPrefixedCommand(reference.createCommandSourceStack().withSuppressedOutput(),
                "shanghai 火 " + owner + " " + fixed + " 6 1.5");
        SpellDamage.dealSaved(player.serverLevel(), direct, DamageCategory.FIRE, owner, fixed, 6, 1.5);
        near("存档伤害：主人[" + owner + "] 固定=" + fixed, reference.getHealth(), direct.getHealth());
        if (owner.isEmpty()) near("空主人单独核对前移基础量", fixed ? 0 : 1.5 * 1.5 / 4.5, 100 - direct.getHealth());
        direct.discard();
        reference.discard();
    }

    private void damage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        effectLifecycle(player);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        PlayerAttributes.setContribution(player, "smoke:combat", new Contribution(Layer.BASE, Map.of(Stat.MANA_ATT, 5.0, Stat.SOUL_ATT, 2.0, Stat.ROOT_FIRE, 3.0, Stat.POWER_MAX, 500.0)));
        PlayerAttributes.setContribution(player, "smoke:combat_gongfa", new Contribution(Layer.BONUS, Map.of(Stat.GONGFA_FIRE, 5.0, Stat.GONGFA_LEVEL, 8.0)));

        IronGolem golem = golem(player, 100);
        double damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, null, true, 6, 1));
        near("无来源固定火伤害 6²/(6+3)", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, player.getUUID(), false, 4, 1));
        // 攻击量 = ((4 + 法术攻击) × (1 + 体质法术修正) + 体质火修正) × (1 + 体质火修正百分比) × (1 + 火灵根系数) + 功法项。
        // 单独运行时灵根系数 0.3、功法项 10、体质修正为 0，攻击量 23；全部系统检查连续运行时玩家带着前面检查留下的灵根与体质，
        // 所以输入取自实际的属性与体质，只核对公式。
        double attackBase = (4 + PlayerAttributes.value(player, Stat.MANA_ATT)) * (Cultivation.tizhiDouble(player, "ManaattAttCorrectPercentage") + 1);
        attackBase = (attackBase + Cultivation.tizhiDouble(player, "FireAttCorrect")) * (Cultivation.tizhiDouble(player, "FireAttCorrectPercentage") + 1);
        double attack = attackBase * (1 + PlayerAttributes.elementCoefficient(player, DamageElement.FIRE))
                + PlayerAttributes.receivedElementBonus(golem, DamageElement.FIRE);
        near("玩家火伤害 攻击量 " + attack, attack * attack / (attack + 3), damage);
        expectTrue("重击上僵直", StatusEffects.has(golem, "effect_033"));
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, player.getUUID(), true, 6, 1));
        near("有来源固定火伤害 6²/(6+3)", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.SOUL, player.getUUID(), false, 5, 1));
        near("玩家灵魂伤害 (5+神识攻击 3) − 2", 6, damage);
        golem.discard();

        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_013"), 200, 1));
        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.NONE, player.getUUID(), false, 4, 1));
        // 攻击量 (4 + 6) + 法术伤害提高 (1+1)² = 14，总量 14²/(14+3)
        near("法术伤害提高 14²/(14+3)", 14.0 * 14 / 17, damage);
        golem.discard();
        StatusEffects.remove(player, "effect_013");

        golem = golem(player, 5);
        golem.addEffect(new MobEffectInstance(StatusEffects.holder("effect_036"), 200, 0));
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.NONE, null, true, 20, 1));
        near("手下留情 总量不超过当前生命 − 1", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("dodge")), 100);
        golem.setItemSlot(EquipmentSlot.HEAD, helmet);
        expect("四件护甲闪避附魔等级之和", 100, Enchants.armorSum(golem, Enchants.key("dodge")));
        Shanghai.deal(level, golem, new Shanghai.Request(DamageCategory.NONE, null, true, 6, 1));
        near("闪避 100 级 伤害", 0, 100 - golem.getHealth());
        golem.discard();

        golem = golem(player, 100);
        double before = golem.getHealth();
        for (int attempt = 0; attempt < 5 && golem.getHealth() == before; attempt++) {
            golem.invulnerableTime = 0;
            level.getServer().getCommands().performPrefixedCommand(golem.createCommandSourceStack().withSuppressedOutput(), "shanghai 无 无 true 6 1");
        }
        near("命令 /shanghai 无 无 true 6 1", 4, before - golem.getHealth());
        golem.discard();

        for (String owner : List.of("", "无", "非法", "00000000-0000-0000-0000-000000000001",
                player.getStringUUID(), player.getStringUUID().replace("-", ""), "wu true 9 2")) {
            compareSavedDamage(player, owner, false);
            compareSavedDamage(player, owner, true);
        }
        enchantments(player, level);
        blocks(player, level);

        Cultivation.set(player, CultivationValue.POWER, 5);
        Cultivation.set(player, CultivationValue.SKILL_POINTS_HEALTH_MAX, 5);
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_011"), 100, 0));
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_008"), 100, 1));

        effectsStart = level.getGameTime();
        poisoned = golem(player, 100);
        poisoned.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_01"), 400, 1));
        mandrake = golem(player, 100);
        mandrake.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_21"), 400, 0));
        mandrake.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_14"), 400, 0));
        bystander = golem(player, 100, 5);
    }
}
