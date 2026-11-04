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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.DamageElement;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 战斗系统的实机检查：以铁傀儡（无 AI，100 点生命，法术防御 3、神识防御 2）为靶，核对灵力与神识伤害的结算数值、
 * 命令 {@code /shanghai}、闪避、手下留情，以及加点机会、灵力恢复和毒药效果在服务端的结果。
 * 期望值按 docs/systems/combat/architecture.md 的公式手算；闪避随机数为 0 时本次伤害无效（约 1/101，Q30），
 * 每次结算最多重试 5 次，取第一次产生的伤害。
 */
final class CombatCheck extends SystemCheck {
    private IronGolem poisoned;
    private IronGolem mandrake;
    private IronGolem bystander;

    CombatCheck(List<String> report, String levelId) {
        super("战斗", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::damage);
            case 1 -> {
                if (local < 5) {
                    return false;
                }
                onServer(player -> {
                    expect("瞬间灵力恢复 灵力", 105, Cultivation.get(player, CultivationValue.POWER));
                    expectTrue("瞬间灵力恢复后效果移除", !StatusEffects.has(player, "effect_011"));
                    expect("永久生命 已加点次数", 3, Cultivation.get(player, CultivationValue.SKILL_POINTS_HEALTH_MAX));
                    expectTrue("永久生命后效果移除", !StatusEffects.has(player, "effect_008"));
                });
            }
            case 2 -> {
                if (local < 110) {
                    return false;
                }
                onServer(player -> {
                    near("断肠 100 刻后 生命", 100 - 8, poisoned.getHealth());
                    near("曼陀罗 两个周期的阴伤害 2 × 12²/(12+3)", 100 - 2 * 144.0 / 15, mandrake.getHealth());
                    expectTrue("曼陀罗把烙毒传给身边的生物", bystander.hasEffect(StatusEffects.holder("duyao_14")));
                    poisoned.discard();
                    mandrake.discard();
                    bystander.discard();
                    clean(player);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private void near(String label, double expected, double actual) {
        expectTrue(label + " 期望 " + expected + " 实际 " + actual, Math.abs(expected - actual) < 1e-3);
    }

    private IronGolem golem(ServerPlayer player, double health) {
        ServerLevel level = player.serverLevel();
        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        golem.moveTo(player.getX() + 3, player.getY(), player.getZ(), 0, 0);
        golem.setNoAi(true);
        golem.setHealth((float) health);
        golem.getPersistentData().putDouble("mana_Def", 3);
        golem.getPersistentData().putDouble("soul_Def", 2);
        level.addFreshEntity(golem);
        return golem;
    }

    /** 结算并返回造成的伤害；闪避随机数为 0 时重试。 */
    private double hit(ServerPlayer player, IronGolem golem, Shanghai.Request request) {
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

    private void damage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
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
                + PlayerAttributes.receivedElementBonus(player, DamageElement.FIRE);
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

        enchantments(player, level);
        blocks(player, level);

        Cultivation.set(player, CultivationValue.POWER, 5);
        Cultivation.set(player, CultivationValue.SKILL_POINTS_HEALTH_MAX, 5);
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_011"), 100, 0));
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_008"), 100, 1));

        poisoned = golem(player, 100);
        poisoned.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_01"), 400, 1));
        mandrake = golem(player, 100);
        mandrake.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_21"), 400, 0));
        mandrake.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_14"), 400, 0));
        bystander = golem(player, 100);
        bystander.moveTo(mandrake.getX() + 2, mandrake.getY(), mandrake.getZ(), 0, 0);
    }
}
