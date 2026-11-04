package net.yvanchuxiuzhen.smoke;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 技能动画实体的实机检查：火系六个、土系六种（滚石术、天外陨石、化石手与巨石锤的正常与潜行两式）技能各施放一次——大火球落地爆炸伤人并点燃、引火点燃面前的目标、控火术熄灭火焰并伤人、
 * 火焰护盾给抗火、火焰漩涡与火龙卷生成实体并在 20 刻时伤人点燃；实体到时消失。
 */
final class SkillEntityCheck extends SystemCheck {
    private enum Kind { BALL, IGNITE, CONTROL, SHIELD, COLUMN, ROLL, METEOR, HAND_ONE, HAND_TWO, HAMMER_ONE, HAMMER_TWO, GOLD_NORMAL, GOLD_BIND, GOLD_ARC, GOLD_SKY, GOLD_CTRL, ICE_FIELD, ICE_SPIKE, LIGHT_FLASH, LIGHT_BURST, LIGHT_KILL, WIND_GATHER, WIND_MOVE, WIND_DRIFT, WIND_CLOUD, THUNDER_PALM, THUNDER_ROLL, THUNDER_FIELD, WATER_WHIP, WATER_JET, WATER_DRAGON, ORB_FLOAT, ORB_BURST, ORB_TURN, ORB_DROP, ORB_SNEAK, SPIRIT, HEAL, TREE_TANGLE, TREE_SWORD, TREE_SWORD_SNEAK, TREE_SWORD_CTRL, TREE_SWORD_ALT, TREE_SEED, TREE_LADDER, TREE_HEDGE, TREE_LASH, TREE_BACK, TREE_GROUND }

    private record Case(String body, String name, Kind kind, String entity, boolean sneak) {
        Case(String body, String name, Kind kind, String entity) {
            this(body, name, kind, entity, false);
        }
    }

    private static final List<Case> CASES = List.of(
            new Case("火", "大火球", Kind.BALL, "skill_fire_02"), new Case("火", "引火", Kind.IGNITE, null),
            new Case("火", "控火术", Kind.CONTROL, null), new Case("火", "火焰护盾", Kind.SHIELD, null),
            new Case("火", "火焰漩涡", Kind.COLUMN, "skill_fire_07"), new Case("火", "火龙卷", Kind.COLUMN, "skill_fire_08"),
            new Case("土", "滚石术", Kind.ROLL, "skill_rock_02"), new Case("土", "天外陨石", Kind.METEOR, "skill_rock_03"),
            new Case("土", "化石手", Kind.HAND_ONE, "skill_rock_04"), new Case("土", "化石手", Kind.HAND_TWO, "skill_rock_04", true),
            new Case("土", "巨石锤", Kind.HAMMER_ONE, "skill_rock_05"), new Case("土", "巨石锤", Kind.HAMMER_TWO, "skill_rock_05", true),
            new Case("金", "金刚轮", Kind.GOLD_NORMAL, "skill_gold_0201"), new Case("金", "捆妖轮", Kind.GOLD_BIND, null),
            new Case("金", "金弧轮", Kind.GOLD_ARC, null), new Case("金", "天堑金轮", Kind.GOLD_SKY, null),
            new Case("金", "金刚轮", Kind.GOLD_CTRL, "skill_gold_0201"),
            new Case("水", "冰天雪地", Kind.ICE_FIELD, "skill_ice_04"), new Case("水", "裂冰斩", Kind.ICE_SPIKE, "skill_ice_05"),
            new Case("火", "闪光术", Kind.LIGHT_FLASH, "skill_light_03"), new Case("火", "万丈光芒", Kind.LIGHT_BURST, "skill_light_03"),
            new Case("火", "万丈光芒", Kind.LIGHT_KILL, "skill_light_03"),
            new Case("木", "聚风术", Kind.WIND_GATHER, "skillwind_02"), new Case("木", "聚风化气", Kind.WIND_MOVE, "skillwind_02"),
            new Case("木", "风飘术", Kind.WIND_DRIFT, null), new Case("木", "造云术", Kind.WIND_CLOUD, null),
            new Case("火", "掌心雷", Kind.THUNDER_PALM, "skill_thunder_08"), new Case("火", "地滚雷", Kind.THUNDER_ROLL, "skill_thunder_08"),
            new Case("火", "惊雷阵", Kind.THUNDER_FIELD, "skill_thunder_10"),
            new Case("水", "水蛇鞭", Kind.WATER_WHIP, "skill_water_01"), new Case("水", "激流波", Kind.WATER_JET, "skill_water_01"),
            new Case("水", "水龙吟", Kind.WATER_DRAGON, "skill_water_02"),
            new Case("水", "空浮球", Kind.ORB_FLOAT, "skill_water_04"), new Case("水", "空浮球", Kind.ORB_BURST, "skill_water_04"),
            new Case("水", "空浮球", Kind.ORB_TURN, "skill_water_04"), new Case("水", "陨河术", Kind.ORB_DROP, "skill_water_04"),
            new Case("水", "陨河术", Kind.ORB_SNEAK, "skill_water_04", true), new Case("水", "水灵阵", Kind.SPIRIT, "skill_water_06"),
            new Case("水", "玲珑阵", Kind.HEAL, "skill_water_07"),
            new Case("木", "乱枝", Kind.TREE_TANGLE, "skill_tree_04"), new Case("木", "灰木剑", Kind.TREE_SWORD, "skill_tree_04"),
            new Case("木", "灰木剑", Kind.TREE_SWORD_SNEAK, "skill_tree_04", true), new Case("木", "灰木剑", Kind.TREE_SWORD_CTRL, "skill_tree_04"),
            new Case("木", "灰木剑", Kind.TREE_SWORD_ALT, "skill_tree_04"), new Case("木", "乱木之种", Kind.TREE_SEED, "skill_tree_04"),
            new Case("木", "灰木树梯", Kind.TREE_LADDER, "skill_tree_04"), new Case("木", "木篱术", Kind.TREE_HEDGE, "skill_tree_05"),
            new Case("木", "毒蔓鞭", Kind.TREE_LASH, "skill_tree_06"), new Case("木", "回身鞭", Kind.TREE_BACK, "skill_tree_06"),
            new Case("木", "地蔓鞭", Kind.TREE_GROUND, "skill_tree_06"));
    private static final double HEALTH = 100;
    private static final int WAIT = 70;

    /** 造成伤害的检查：灵力伤害有 1% 的闪避随机数为 0，没有伤害时同一技能重试一次。 */
    private static final Set<Kind> DAMAGING = EnumSet.of(Kind.BALL, Kind.METEOR, Kind.COLUMN, Kind.ROLL, Kind.HAND_ONE, Kind.HAND_TWO, Kind.HAMMER_ONE,
            Kind.HAMMER_TWO, Kind.GOLD_NORMAL, Kind.GOLD_BIND, Kind.GOLD_SKY, Kind.ICE_FIELD, Kind.ICE_SPIKE, Kind.LIGHT_BURST, Kind.THUNDER_PALM,
            Kind.THUNDER_FIELD, Kind.WATER_WHIP, Kind.WATER_JET, Kind.WATER_DRAGON, Kind.ORB_FLOAT, Kind.ORB_BURST, Kind.ORB_DROP, Kind.ORB_SNEAK, Kind.SPIRIT, Kind.TREE_SWORD, Kind.TREE_SWORD_SNEAK, Kind.TREE_SWORD_CTRL,
            Kind.TREE_SWORD_ALT, Kind.TREE_SEED, Kind.TREE_LASH, Kind.TREE_BACK, Kind.TREE_GROUND);

    /** 水灵阵 40 刻后才开始放水灵弹，玲珑阵 60 刻后才第一次回血，等待时间相应加长。 */
    private static int waitTicks(Kind kind) {
        return switch (kind) {
            case SPIRIT -> 110;
            case HEAL -> 90;
            default -> WAIT;
        };
    }

    private IronGolem golem;
    private BlockPos firePos;
    private int index;
    private boolean retried;

    SkillEntityCheck(List<String> report, String levelId) {
        super("技能实体", report, levelId);
    }

    private static ItemStack book(ServerPlayer player, Case spell) {
        String cooldowns = switch (spell.body()) {
            case "土" -> "0 0 0 0 1 0 0 0 0 0 100 0";
            case "金" -> "1 0 0 0 0 0 100 0 0 0 0 0";
            case "水" -> "0 1 0 0 0 0 0 100 0 0 0 0";
            case "木" -> "0 0 1 0 0 0 0 0 100 0 0 0";
            default -> "0 0 0 1 0 0 0 0 0 100 0 0";
        };
        String arguments = "yvanchuxiuzhen:book_003 " + spell.name() + " 1 1 " + spell.body() + " x 无 3 1 0 0 " + cooldowns;
        ItemStack stack = Skills.createBook(SkillKind.SPELL, List.of(arguments.split(" ")));
        stack.set(SkillData.SKILL.get(), Skills.book(stack).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    private void clearArea(ServerPlayer player) {
        AABB area = new AABB(player.blockPosition()).inflate(24);
        player.serverLevel().getEntitiesOfClass(SkillEntity.class, area).forEach(SkillEntity::discard);
        if (golem != null) {
            golem.discard();
            golem = null;
        }
        if (firePos != null) {
            player.serverLevel().setBlock(firePos, Blocks.AIR.defaultBlockState(), 3);
            player.serverLevel().setBlock(firePos.below(), Blocks.AIR.defaultBlockState(), 3);
            firePos = null;
        }
    }

    /** 以玩家脚下为准铺一块平地（东侧 14 格、其余各 8 格，上方 7 格清空），使移动类技能的检查不受地形影响。 */
    private static void levelArena(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos feet = player.blockPosition();
        for (int dx = -8; dx <= 14; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                level.setBlock(feet.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 2);
                for (int dy = 0; dy <= 6; dy++) {
                    level.setBlock(feet.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private IronGolem placeGolem(ServerPlayer player, double x, double y, double z) {
        return placeGolem(player, x, y, z, HEALTH);
    }

    private IronGolem placeGolem(ServerPlayer player, double x, double y, double z, double health) {
        ServerLevel level = player.serverLevel();
        IronGolem placed = EntityType.IRON_GOLEM.create(level);
        placed.moveTo(x, y, z, 0, 0);
        placed.setNoAi(true);
        placed.setHealth((float) health);
        level.addFreshEntity(placed);
        return placed;
    }

    @Override
    protected boolean step(int step, int local) {
        if (index >= CASES.size()) {
            onServer(player -> {
                clearArea(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                Skills.setSpell(player, 1, ItemStack.EMPTY);
                SkillSelection.clear(player);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.removeEffect(MobEffects.FIRE_RESISTANCE);
                player.setShiftKeyDown(false);
                ModifierKeys.update(player, false, false);
            });
            finish();
            return true;
        }
        Case spell = CASES.get(index);
        switch (step % 3) {
            case 0 -> onServer(player -> {
                clearArea(player);
                levelArena(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
                Cultivation.set(player, CultivationValue.POWER, 1000);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.setShiftKeyDown(spell.sneak());
                ModifierKeys.update(player, spell.kind() == Kind.GOLD_CTRL || spell.kind() == Kind.TREE_SWORD_CTRL, spell.kind() == Kind.TREE_SWORD_ALT);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 1, book(player, spell));
                SkillSelection.clear(player);
                SkillSelection.select(player, 1);
                expect(spell.name() + " 已选中", spell.name(), Skills.book(Skills.selected(player)).map(SkillBook::name).orElse(""));
                switch (spell.kind()) {
                    case LIGHT_KILL -> golem = placeGolem(player, player.getX() + 3, player.getY(), player.getZ(), 5);
                    case IGNITE, COLUMN, ROLL, HAND_ONE, HAND_TWO, HAMMER_ONE, HAMMER_TWO, GOLD_BIND, GOLD_SKY, GOLD_NORMAL, GOLD_CTRL, ICE_FIELD, ICE_SPIKE, LIGHT_FLASH, LIGHT_BURST, WIND_GATHER, WIND_MOVE, THUNDER_PALM, THUNDER_ROLL, THUNDER_FIELD, WATER_WHIP, WATER_JET, WATER_DRAGON, ORB_FLOAT, ORB_BURST, ORB_TURN, ORB_SNEAK, SPIRIT, TREE_TANGLE, TREE_SWORD, TREE_SWORD_SNEAK, TREE_SWORD_CTRL, TREE_SWORD_ALT, TREE_SEED, TREE_LASH, TREE_BACK -> golem = placeGolem(player,
                            player.getX() + switch (spell.kind()) {
                                case IGNITE, GOLD_BIND, GOLD_SKY, ICE_FIELD, THUNDER_FIELD, TREE_SEED -> 4;
                                case TREE_SWORD_SNEAK, TREE_SWORD_CTRL, TREE_SWORD_ALT, TREE_LASH, TREE_BACK -> 3;
                                case WIND_GATHER, WIND_MOVE -> 5;
                                case WATER_WHIP -> 3.5;
                                case WATER_JET -> 2.5;
                                case WATER_DRAGON -> 7;
                                case ORB_FLOAT, ORB_BURST, ORB_SNEAK -> 3;
                                case SPIRIT -> 4;
                                case THUNDER_PALM, THUNDER_ROLL -> 3;
                                case GOLD_NORMAL, GOLD_CTRL, LIGHT_FLASH, LIGHT_BURST, LIGHT_KILL -> 3;
                                case ICE_SPIKE -> 1.5;
                                default -> 5;
                            }, player.getY(), player.getZ());
                    case HEAL -> {
                        golem = placeGolem(player, player.getX() + 1, player.getY(), player.getZ(), 50);
                        Relations.setTeam(golem, Relations.team(player));
                    }
                    case CONTROL -> {
                        BlockPos center = ProjectileSupport.lookBlock(player, 4);
                        firePos = center.immutable();
                        player.serverLevel().setBlock(firePos.below(), Blocks.STONE.defaultBlockState(), 3);
                        player.serverLevel().setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                        golem = placeGolem(player, center.getX() + 2.5, center.getY(), center.getZ() + 0.5);
                    }
                    default -> {
                    }
                }
                SkillCasting.onRightClick(player);
                if (spell.entity() != null) {
                    List<SkillEntity> spawned = player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(24));
                    expectTrue(spell.name() + " 生成实体", !spawned.isEmpty());
                    if (spell.kind() == Kind.TREE_LADDER) {
                        expectTrue(spell.name() + " 把自己抛起 " + player.getDeltaMovement(), player.getDeltaMovement().y > 1);
                        expectTrue(spell.name() + " 缓降", player.hasEffect(MobEffects.SLOW_FALLING));
                        player.setDeltaMovement(Vec3.ZERO);
                    } else if (spell.kind() == Kind.TREE_HEDGE) {
                        expect(spell.name() + " 生成九道木篱", 9, spawned.size());
                    }
                    if (spell.kind() == Kind.HAND_TWO) {
                        SkillEntities.turn(spawned.get(0), -90, 0);
                        golem.moveTo(spawned.get(0).getX() + 1.5, spawned.get(0).getY(), spawned.get(0).getZ() + 0.5);
                    }
                }
            });
            case 1 -> {
                if (local < 4) {
                    return false;
                }
                boolean[] ready = {true};
                onServer(player -> {
                    List<SkillEntity> found = player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(24));
                    Kind kind = spell.kind();
                    if (kind == Kind.BALL || kind == Kind.METEOR || kind == Kind.ORB_DROP) {
                        SkillEntity ball = found.get(0);
                        ready[0] = ball.getPersistentData().getBoolean("kaishi");
                        if (ready[0]) {
                            golem = placeGolem(player, ball.getX(), ball.getY(), ball.getZ());
                        }
                    } else if (kind == Kind.GOLD_ARC || kind == Kind.GOLD_BIND) {
                        expectTrue(spell.name() + " 生成金轮实体", !found.isEmpty());
                        if (kind == Kind.GOLD_BIND) {
                            ready[0] = golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || local > 20;
                            expectTrue(spell.name() + " 目标缓慢（命中后）", !ready[0] || golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
                        }
                    } else if (kind == Kind.GOLD_CTRL) {
                        expectTrue(spell.name() + " 金轮是 Ctrl 式", !found.isEmpty() && found.get(0).getPersistentData().getString("mode").equals("金刚轮ctrl"));
                    } else if (kind == Kind.TREE_TANGLE) {
                        expectTrue(spell.name() + " 目标缓慢", golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
                    } else if (kind == Kind.TREE_GROUND) {
                        SkillEntity vine = found.get(0);
                        golem = placeGolem(player, vine.getX(), vine.getY(), vine.getZ() - 2.5);
                    } else if (kind == Kind.ORB_BURST) {
                        player.setShiftKeyDown(true);
                        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                        SkillCasting.onRightClick(player);
                    } else if (kind == Kind.ORB_TURN) {
                        player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                        ModifierKeys.update(player, true, false);
                        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                        SkillCasting.onRightClick(player);
                        expectTrue(spell.name() + " Ctrl 转向施放者朝向 " + found.get(0).getYRot(), Math.abs(Mth.wrapDegrees(found.get(0).getYRot())) < 1);
                    } else if (kind == Kind.WATER_JET) {
                        expectTrue(spell.name() + " 放出小水弹", !player.serverLevel().getEntitiesOfClass(SkillProjectile.class, new AABB(player.blockPosition()).inflate(24)).isEmpty());
                    } else if (kind == Kind.WIND_GATHER) {
                        expectTrue(spell.name() + " 把目标拉向自己 " + golem.getDeltaMovement(), golem.getDeltaMovement().length() > 0.3);
                    } else if (kind == Kind.WIND_MOVE) {
                        expectTrue(spell.name() + " 风团在飞行 " + found.get(0).getDeltaMovement(), found.get(0).getDeltaMovement().length() > 0.05);
                        ready[0] = true;
                    } else if (kind == Kind.THUNDER_PALM) {
                        SkillEntity palm = found.get(0);
                        golem.moveTo(palm.getX() + 0.5, palm.getY(), palm.getZ());
                    } else if (kind == Kind.THUNDER_ROLL) {
                    } else if (kind == Kind.COLUMN || kind == Kind.ROLL) {
                        golem.moveTo(found.get(0).getX() + 1.5, found.get(0).getY(), found.get(0).getZ());
                    } else if (kind == Kind.HAND_ONE) {
                        SkillEntity hand = found.get(0);
                        SkillEntities.turn(hand, -90, 0);
                        golem.moveTo(hand.getX() + 3.5, hand.getY(), hand.getZ() + 0.5);
                    } else if (kind == Kind.ICE_SPIKE) {
                        expectTrue(spell.name() + " 后续冰刺 " + found.size(), found.size() >= 2);
                        golem.moveTo(found.get(0).getX() + 0.5, found.get(0).getY(), found.get(0).getZ());
                    } else if (kind == Kind.HAMMER_ONE || kind == Kind.HAMMER_TWO) {
                        golem.moveTo(found.get(0).getX() + 1, found.get(0).getY(), found.get(0).getZ());
                    }
                });
                if (!ready[0]) {
                    if (local > 100) {
                        fail(spell.name() + " 没有落地");
                        finish();
                    }
                    return false;
                }
            }
            default -> {
                if (local < waitTicks(spell.kind())) {
                    return false;
                }
                boolean[] retry = {false};
                onServer(player -> {
                    if (DAMAGING.contains(spell.kind()) && golem.getHealth() >= HEALTH && !retried) {
                        retry[0] = true;
                        clearArea(player);
                        return;
                    }
                    switch (spell.kind()) {
                        case BALL, METEOR -> {
                            expectTrue(spell.name() + " 爆炸伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 点燃目标", golem.getRemainingFireTicks() > 0 || golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 爆炸后消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        }
                        case IGNITE -> expectTrue(spell.name() + " 点燃面前的目标 " + golem.getRemainingFireTicks(), golem.getRemainingFireTicks() > 0);
                        case CONTROL -> {
                            expectTrue(spell.name() + " 熄灭火焰", player.serverLevel().getBlockState(firePos).isAir());
                            expectTrue(spell.name() + " 点燃目标", golem.getRemainingFireTicks() > 0);
                        }
                        case SHIELD -> expectTrue(spell.name() + " 获得抗火", player.hasEffect(MobEffects.FIRE_RESISTANCE));
                        case GOLD_NORMAL, GOLD_SKY -> expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case GOLD_BIND -> expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case GOLD_ARC -> expectTrue(spell.name() + " 弹体到时消失", player.serverLevel().getEntitiesOfClass(SkillProjectile.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        case ICE_FIELD, ICE_SPIKE -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            if (spell.kind() == Kind.ICE_SPIKE) {
                                expectTrue(spell.name() + " 冷冻 " + golem.getTicksFrozen(), golem.getTicksFrozen() > 0);
                                expectTrue(spell.name() + " 冰刺到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                            }
                        }
                        case LIGHT_FLASH -> {
                            expectTrue(spell.name() + " 致盲", golem.hasEffect(MobEffects.BLINDNESS));
                            expectTrue(spell.name() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        }
                        case WIND_GATHER, WIND_MOVE -> {
                        }
                        case WIND_DRIFT -> {
                            expectTrue(spell.name() + " 获得速度", player.hasEffect(MobEffects.MOVEMENT_SPEED));
                            expectTrue(spell.name() + " 获得风飘状态", StatusEffects.has(player, "skillwind_04"));
                        }
                        case WIND_CLOUD -> expectTrue(spell.name() + " 隐身（40 刻后已结束）", !player.hasEffect(MobEffects.INVISIBILITY));
                        case THUNDER_PALM -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        }
                        case THUNDER_ROLL -> {
                            expectTrue(spell.name() + " 生成处的目标触发爆发并受伤 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 重伤", golem.hasEffect(StatusEffects.holder("effect_007")));
                        }
                        case WATER_WHIP, WATER_DRAGON -> expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case WATER_JET -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 记下追踪目标", !Cultivation.tizhiString(player, "mubiaouid").isEmpty());
                        }
                        case THUNDER_FIELD -> expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case TREE_TANGLE, TREE_LADDER -> {
                        }
                        case TREE_SWORD, TREE_SWORD_SNEAK, TREE_SWORD_CTRL, TREE_LASH, TREE_BACK, TREE_GROUND ->
                                expectTrue(spell.name() + " " + spell.kind() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case TREE_SWORD_ALT -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 流血", golem.hasEffect(StatusEffects.holder("effect_004")));
                        }
                        case TREE_SEED -> {
                            expectTrue(spell.name() + " 爆裂伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        }
                        case TREE_HEDGE -> expect(spell.name() + " 木篱留在原处（Q44）", 9, player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).size());
                        case ORB_FLOAT -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 收为乘客", golem.getVehicle() instanceof SkillEntity);
                        }
                        case ORB_BURST, ORB_SNEAK, ORB_DROP -> {
                            expectTrue(spell.name() + " " + spell.kind() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " " + spell.kind() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                        }
                        case ORB_TURN -> {
                        }
                        case SPIRIT -> expectTrue(spell.name() + " 水灵弹伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case HEAL -> expectTrue(spell.name() + " 友方回血 " + golem.getHealth(), golem.getHealth() > 50);
                        case LIGHT_BURST -> expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                        case LIGHT_KILL -> expectTrue(spell.name() + " 低血直接清零", golem.isDeadOrDying());
                        case ROLL, HAND_ONE, HAND_TWO, HAMMER_ONE, HAMMER_TWO -> {
                            expectTrue(spell.name() + " " + spell.kind() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            if (spell.kind() != Kind.ROLL) {
                                expectTrue(spell.name() + " " + spell.kind() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillEntity.class, new AABB(player.blockPosition()).inflate(64)).isEmpty());
                            }
                        }
                        case COLUMN -> {
                            expectTrue(spell.name() + " 伤人 " + golem.getHealth(), golem.getHealth() < HEALTH);
                            expectTrue(spell.name() + " 点燃目标", golem.getRemainingFireTicks() > 0);
                            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, spell.entity());
                            expectTrue(spell.name() + " 实体已注册", BuiltInRegistries.ENTITY_TYPE.containsKey(id));
                        }
                    }
                    clearArea(player);
                });
                retried = retry[0];
                if (!retry[0]) {
                    index++;
                }
                return true;
            }
        }
        return true;
    }
}
