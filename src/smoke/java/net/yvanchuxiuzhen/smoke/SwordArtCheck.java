package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;

/**
 * 剑法的实机检查：玩家挥动内容表中的剑类武器，经 {@code ItemSwingEvent} 触发武器技——生成对应的剑影、按式数轮换并设置连击窗口与冷却，
 * 各式的挑起、震开、范围伤害与跃起只作用于非同队的目标；技能书类别不对、剑类武器要求的本人研习、非剑类武器、出手冷却都会拒绝出手，
 * 剑影到时消失，连击窗口结束后式数归零。
 */
final class SwordArtCheck extends SystemCheck {
    private static final double HEALTH = 100;
    private static final String[] NAMES = {"中平剑法", "柔云剑法", "拜年剑法", "弱鸡剑法"};

    private record Action(int ticks, Consumer<ServerPlayer> run) {
    }

    private final List<Action> actions;
    private IronGolem target;
    private IronGolem friend;
    private int executed = -1;

    SwordArtCheck(List<String> report, String levelId) {
        super("剑法", report, levelId);
        actions = Arrays.asList(
                new Action(12, this::arena),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "剑", true, 1);
                    swing(player);
                    expect("中平剑法第 1 式生成剑影", 1, shadows(player, 1));
                    expect("式数", 1.0, data(player).getDouble("swordsmanshipnum"));
                    expect("连击窗口", 30.0, data(player).getDouble("swordsmanshiptick"));
                    expect("出手冷却", 10.0, data(player).getDouble("smanshipcolddown"));
                    expect("第 1 式没有伤害", HEALTH, target.getHealth());
                    swing(player);
                    expect("冷却中再挥动不出手", 1.0, data(player).getDouble("swordsmanshipnum"));
                    expect("冷却中不再生成剑影", 1, shadows(player, 1));
                }),
                new Action(2, player -> {
                    stage(player, "weaponwoodsword_02", 1, "三尺青锋", true, 3);
                    swing(player);
                    expect("中平剑法第 3 式挑起目标", 0.6, target.getDeltaMovement().y);
                    expect("同队不受影响", 0.0, friend.getDeltaMovement().y);
                }),
                new Action(2, player -> {
                    stage(player, "weaponbroadsword_03", 1, "剑", true, 5);
                    swing(player);
                    expectTrue("中平剑法第 5 式伤害目标", target.getHealth() < HEALTH);
                    expect("同队不受伤", HEALTH, friend.getHealth());
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "剑", true, 6);
                    swing(player);
                    expect("中平剑法第 6 式挥剑者跃起", 0.4, player.getDeltaMovement().y);
                    expectTrue("中平剑法第 6 式伤害目标", target.getHealth() < HEALTH);
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 2, "剑", true, 1);
                    swing(player);
                    expect("柔云剑法第 1 式震开目标（水平）", 0.8, target.getDeltaMovement().x);
                    expect("柔云剑法第 1 式震开目标（竖直）", 0.5, target.getDeltaMovement().y);
                    expect("柔云剑法生成自己的剑影", 1, shadows(player, 2));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 2, "剑", true, 2);
                    swing(player);
                    expectTrue("柔云剑法第 2 式伤害目标", target.getHealth() < HEALTH);
                    expect("同队不受伤", HEALTH, friend.getHealth());
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 2, "剑", true, 3);
                    swing(player);
                    expect("柔云剑法第 3 式没有效果", HEALTH, target.getHealth());
                    expect("柔云剑法第 3 式不位移", 0.0, target.getDeltaMovement().y);
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 2, "剑", true, 4);
                    swing(player);
                    expectTrue("柔云剑法第 4 式伤害目标", target.getHealth() < HEALTH);
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 2, "剑", true, 6);
                    swing(player);
                    expect("柔云剑法第 6 式挑起目标", 0.6, target.getDeltaMovement().y);
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 3, "剑", true, 1);
                    swing(player);
                    expectTrue("拜年剑法第 1 式伤害目标", target.getHealth() < HEALTH);
                    expect("拜年剑法生成自己的剑影", 1, shadows(player, 3));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 3, "剑", true, 2);
                    swing(player);
                    expect("拜年剑法第 2 式没有效果", HEALTH, target.getHealth());
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 4, "剑", true, 2);
                    swing(player);
                    expect("弱鸡剑法每一式都挑起目标", 0.6, target.getDeltaMovement().y);
                    expect("弱鸡剑法生成自己的剑影", 1, shadows(player, 4));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "剑", true, 4);
                    data(player).putDouble("swordsmanshipnum", 6);
                    swing(player);
                    expect("式数超过上限后回到第 1 式", 1.0, data(player).getDouble("swordsmanshipnum"));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "枪", true, 1);
                    swing(player);
                    expect("武器技类别不是剑不出手", 0, shadows(player, 1));
                    expect("类别不对时式数不变", 0.0, data(player).getDouble("swordsmanshipnum"));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "剑", false, 1);
                    swing(player);
                    expect("剑需要本人研习的书", 0, shadows(player, 1));
                }),
                new Action(2, player -> {
                    stage(player, "weaponepee_01", 1, "剑", false, 1);
                    swing(player);
                    expect("重剑不要求本人研习", 1, shadows(player, 1));
                }),
                new Action(2, player -> {
                    stage(player, "weaponspear_01", 1, "剑", false, 1);
                    swing(player);
                    expect("枪不要求本人研习", 1, shadows(player, 1));
                }),
                new Action(2, player -> {
                    stage(player, "weaponsword_01", 1, "剑", true, 1);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                    swing(player);
                    expect("非剑类武器不触发武器技", 0, shadows(player, 1));
                }),
                new Action(20, player -> {
                    stage(player, "weaponsword_01", 1, "剑", true, 1);
                    swing(player);
                    CompoundTag data = data(player);
                    data.putDouble("swordsmanshiptick", 3);
                    data.putDouble("swordsmanshipnum", 4);
                }),
                new Action(0, player -> {
                    expect("连击窗口结束后式数归零", 0.0, data(player).getDouble("swordsmanshipnum"));
                    expect("出手冷却走完", 0.0, data(player).getDouble("smanshipcolddown"));
                    expect("剑影到时消失", 0L, shadows(player, 1) + shadows(player, 2) + shadows(player, 3) + shadows(player, 4));
                })
        );
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData();
    }

    private static void swing(ServerPlayer player) {
        player.swing(InteractionHand.MAIN_HAND);
    }

    private static AABB area(ServerPlayer player) {
        return new AABB(player.blockPosition()).inflate(24);
    }

    private static long shadows(ServerPlayer player, int art) {
        return player.serverLevel().getEntitiesOfClass(SkillEntity.class, area(player)).stream()
                .filter(entity -> SkillEntities.is(entity, "swordsmanship_0" + art)).count();
    }

    private void clearArea(ServerPlayer player) {
        player.serverLevel().getEntitiesOfClass(SkillEntity.class, area(player)).forEach(SkillEntity::discard);
        if (target != null) {
            target.discard();
            target = null;
        }
        if (friend != null) {
            friend.discard();
            friend = null;
        }
    }

    private void arena(ServerPlayer player) {
        Arena.level(player);
        clearArea(player);
    }

    private IronGolem golem(ServerPlayer player) {
        IronGolem placed = EntityType.IRON_GOLEM.create(player.serverLevel());
        placed.moveTo(player.getX() + 2, player.getY(), player.getZ(), 0, 0);
        placed.setNoAi(true);
        placed.setHealth((float) HEALTH);
        player.serverLevel().addFreshEntity(placed);
        return placed;
    }

    /**
     * 布置一次出手：清场，玩家朝东并手持 {@code item}，武器技槽放入 {@code art} 号剑法（类别 {@code type}，{@code owned} 为真时已被本人研习），
     * 式数设为 {@code step - 1} 使下一次挥动正好是第 {@code step} 式；东侧 2 格放一个目标和一个与玩家同队的对照目标。
     */
    private void stage(ServerPlayer player, String item, int art, String type, boolean owned, int step) {
        clearArea(player);
        player.setYRot(-90);
        player.setXRot(0);
        player.setDeltaMovement(0, 0, 0);
        target = golem(player);
        friend = golem(player);
        Relations.setTeam(friend, Relations.team(player));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, item))));
        String arguments = "yvanchuxiuzhen:book_012 " + NAMES[art - 1] + " 5 5 无 挥剑。 " + type + " sword0" + art + " 6";
        ItemStack book = Skills.createBook(SkillKind.WEAPON_ART, List.of(arguments.split(" ")));
        if (owned) {
            book.set(SkillData.SKILL.get(), Skills.book(book).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
        }
        Skills.setWeaponArt(player, book);
        CompoundTag data = data(player);
        data.putDouble("smanshipcolddown", 0);
        data.putDouble("swordsmanshiptick", 30);
        data.putDouble("swordsmanshipnum", step - 1);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step >= actions.size()) {
            onServer(player -> {
                clearArea(player);
                Skills.setWeaponArt(player, ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                data(player).putDouble("swordsmanshipnum", 0);
            });
            finish();
            return true;
        }
        Action action = actions.get(step);
        if (executed != step) {
            executed = step;
            onServer(action.run());
        }
        return local >= action.ticks();
    }
}
