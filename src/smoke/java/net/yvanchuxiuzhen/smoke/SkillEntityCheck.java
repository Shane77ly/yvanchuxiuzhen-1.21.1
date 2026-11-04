package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 技能动画实体的实机检查：火系六个、土系六种（滚石术、天外陨石、化石手与巨石锤的正常与潜行两式）技能各施放一次——大火球落地爆炸伤人并点燃、引火点燃面前的目标、控火术熄灭火焰并伤人、
 * 火焰护盾给抗火、火焰漩涡与火龙卷生成实体并在 20 刻时伤人点燃；实体到时消失。
 */
final class SkillEntityCheck extends SystemCheck {
    private enum Kind { BALL, IGNITE, CONTROL, SHIELD, COLUMN, ROLL, METEOR, HAND_ONE, HAND_TWO, HAMMER_ONE, HAMMER_TWO }

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
            new Case("土", "巨石锤", Kind.HAMMER_ONE, "skill_rock_05"), new Case("土", "巨石锤", Kind.HAMMER_TWO, "skill_rock_05", true));
    private static final double HEALTH = 100;
    private static final int WAIT = 70;

    private IronGolem golem;
    private BlockPos firePos;

    SkillEntityCheck(List<String> report, String levelId) {
        super("技能实体", report, levelId);
    }

    private static ItemStack book(ServerPlayer player, Case spell) {
        String cooldowns = spell.body().equals("土") ? "0 0 0 0 1 0 0 0 0 0 100 0" : "0 0 0 1 0 0 0 0 0 100 0 0";
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

    private IronGolem placeGolem(ServerPlayer player, double x, double y, double z) {
        ServerLevel level = player.serverLevel();
        IronGolem placed = EntityType.IRON_GOLEM.create(level);
        placed.moveTo(x, y, z, 0, 0);
        placed.setNoAi(true);
        placed.setHealth((float) HEALTH);
        level.addFreshEntity(placed);
        return placed;
    }

    @Override
    protected boolean step(int step, int local) {
        int index = step / 3;
        if (index >= CASES.size()) {
            onServer(player -> {
                clearArea(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                Skills.setSpell(player, 1, ItemStack.EMPTY);
                SkillSelection.clear(player);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.removeEffect(MobEffects.FIRE_RESISTANCE);
                player.setShiftKeyDown(false);
            });
            finish();
            return true;
        }
        Case spell = CASES.get(index);
        switch (step % 3) {
            case 0 -> onServer(player -> {
                clearArea(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
                Cultivation.set(player, CultivationValue.POWER, 1000);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.setShiftKeyDown(spell.sneak());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 1, book(player, spell));
                SkillSelection.clear(player);
                SkillSelection.select(player, 1);
                expect(spell.name() + " 已选中", spell.name(), Skills.book(Skills.selected(player)).map(SkillBook::name).orElse(""));
                switch (spell.kind()) {
                    case IGNITE, COLUMN, ROLL, HAND_ONE, HAND_TWO, HAMMER_ONE, HAMMER_TWO -> golem = placeGolem(player, player.getX() + (spell.kind() == Kind.IGNITE ? 4 : 5), player.getY(), player.getZ());
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
                    if (kind == Kind.BALL || kind == Kind.METEOR) {
                        SkillEntity ball = found.get(0);
                        ready[0] = ball.getPersistentData().getBoolean("kaishi");
                        if (ready[0]) {
                            golem = placeGolem(player, ball.getX(), ball.getY(), ball.getZ());
                        }
                    } else if (kind == Kind.COLUMN || kind == Kind.ROLL) {
                        golem.moveTo(found.get(0).getX() + 1.5, found.get(0).getY(), found.get(0).getZ());
                    } else if (kind == Kind.HAND_ONE) {
                        SkillEntity hand = found.get(0);
                        SkillEntities.turn(hand, -90, 0);
                        golem.moveTo(hand.getX() + 3.5, hand.getY(), hand.getZ() + 0.5);
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
                if (local < WAIT) {
                    return false;
                }
                onServer(player -> {
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
                return true;
            }
        }
        return true;
    }
}
