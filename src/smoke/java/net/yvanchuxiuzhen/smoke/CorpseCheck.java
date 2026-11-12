package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.Corpse;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 起尸诀（可驯服亡灵实体）的主路径检查：施放在四角放出四只（等级数值、主人、技能栏序号、亡灵标签）；技能状态机——远处的目标选择“靠近”并走近、
 * 沉默时近身“普攻”伤人、喷射粘液施毒后复位、死亡拥抱缓慢并伤人、被骑乘时“挣脱”；存续到期消失；杀死实体让书积累修为并升级；起尸诀附魔命中放出一只。
 */
final class CorpseCheck extends ActionCheck {
    private static final double HEALTH = 1000;
    private static final double RANK = 10;
    private static final String SPELL = "起尸诀";
    /** 死亡拥抱前冲 14 刻、每刻 0.4 格，再看前方 3 格处：目标放在约 8.6 格外才落在判定内。 */
    private static final double HUG_DISTANCE = 8.6;

    private IronGolem golem;
    private double startDistance;

    CorpseCheck(List<String> report, String levelId) {
        super("起尸诀", report, levelId);
        script(
                new Action(12, player -> {
                    clearArea(player);
                    Arena.level(player);
                    player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
                    Cultivation.set(player, CultivationValue.POWER, 1000);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                    Skills.setSpell(player, 1, book(player, 4));
                    SkillSelection.clear(player);
                    SkillSelection.select(player, 1);
                    expect("起尸诀已选中", SPELL, Skills.book(Skills.selected(player)).map(SkillBook::name).orElse(""));
                }),
                new Action(2, player -> {
                    player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    SkillCasting.onRightClick(player);
                    List<Corpse> corpses = corpses(player);
                    expect("施放在四角放出四只", 4, corpses.size());
                    Corpse first = corpses.get(0);
                    expectTrue("主人是施放者且同队", first.isTame() && first.getOwnerUUID().equals(player.getUUID()) && Relations.isFriendly(first, player));
                    expect("最大生命 40 + 20 × 等级（等级 ceil(品阶 × 0.5)）", 80.0, first.getMaxHealth());
                    expect("攻击 4 + 2 × 等级", 8.0, first.getPersistentData().getDouble("attack_Num"));
                    expect("技能栏序号", 1.0, first.getPersistentData().getDouble("qishijveweizhi"));
                    expectTrue("亡灵", first.getType().is(EntityTypeTags.UNDEAD));
                    expect("四个角各一只", 4, corpses.stream().map(corpse -> (corpse.getX() > player.getX() ? 1 : 0) + (corpse.getZ() > player.getZ() ? 2 : 0)).distinct().count());
                }),
                new Action(24, player -> {
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    golem = placeGolem(player, 8);
                    corpse.getPersistentData().putString("linshigongjizhe", golem.getStringUUID());
                    startDistance = corpse.distanceTo(golem);
                }),
                new Action(2, player -> {
                    Corpse corpse = corpses(player).get(0);
                    expectTrue("远处的目标：靠近后距离缩短 " + corpse.distanceTo(golem), corpse.distanceTo(golem) < startDistance - 1);
                    expect("目标已记下", golem.getStringUUID(), corpse.getPersistentData().getString("gongjizhe"));
                }),
                new Action(26, player -> {
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    StatusEffects.add(corpse, "effect_002", 600, 0);
                    golem = placeGolem(player, 1.2);
                    corpse.getPersistentData().putString("linshigongjizhe", golem.getStringUUID());
                }),
                new Action(2, player -> expectTrue("沉默时近身只有普攻：目标受伤 " + golem.getHealth(), golem.getHealth() < HEALTH)),
                new Action(4, player -> {
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    golem = placeGolem(player, 3);
                    stiffen(corpse);
                    start(corpse, "喷射粘液");
                }),
                new Action(52, player -> expectTrue("喷射粘液：自身霸体", StatusEffects.has(corpses(player).get(0), "skilleffect_08"))),
                new Action(50, player -> {
                    expectTrue("喷射粘液：目标中毒", StatusEffects.has(golem, "duyao_20"));
                    expectTrue("喷射粘液：目标受伤", golem.getHealth() < HEALTH);
                    expect("喷射粘液：阶段", "结束", corpses(player).get(0).getPersistentData().getString("jinengjinxing"));
                }),
                new Action(28, player -> {
                    expect("喷射粘液：60 刻后复位", "", corpses(player).get(0).getPersistentData().getString("jineng"));
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    golem = placeGolem(player, HUG_DISTANCE);
                    start(corpse, "死亡拥抱");
                }),
                new Action(2, player -> {
                    expectTrue("死亡拥抱：目标缓慢", golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
                    expectTrue("死亡拥抱：目标受伤", golem.getHealth() < HEALTH);
                }),
                new Action(20, player -> {
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    golem = placeGolem(player, 8);
                    ArmorStand stand = EntityType.ARMOR_STAND.create(player.serverLevel());
                    stand.moveTo(corpse.getX(), corpse.getY(), corpse.getZ());
                    player.serverLevel().addFreshEntity(stand);
                    corpse.startRiding(stand, true);
                    corpse.getPersistentData().putString("linshigongjizhe", golem.getStringUUID());
                }),
                new Action(26, player -> expectTrue("被骑乘时先选挣脱并准备，期间仍被骑乘", corpses(player).get(0).isPassenger())),
                new Action(2, player -> expectTrue("挣脱后脱离骑乘", !corpses(player).get(0).isPassenger())),
                new Action(2, player -> {
                    clearArea(player);
                    Corpse corpse = spawnCorpse(player, 0);
                    corpse.getPersistentData().putDouble("cunxvtick", 599);
                }),
                new Action(46, player -> expect("存续到期：进入清除自身", "清除自身", corpses(player).get(0).getPersistentData().getString("jineng"))),
                new Action(2, player -> expect("清除自身后消失", 0, corpses(player).size())),
                new Action(2, player -> {
                    clearArea(player);
                    golem = placeGolem(player, 3);
                    golem.getPersistentData().putDouble("LEVELnumber", 100);
                    Corpse corpse = spawnCorpse(player, 0);
                    corpse.getPersistentData().putDouble("qishijveweizhi", 1);
                    corpse.awardKillScore(golem, 0, player.damageSources().generic());
                    SkillBook book = Skills.book(Skills.spell(player, 1)).orElseThrow();
                    expect("修为加被杀实体的 LEVELnumber", 100.0, book.number("jinengExp"));
                    corpse.awardKillScore(golem, 0, player.damageSources().generic());
                    book = Skills.book(Skills.spell(player, 1)).orElseThrow();
                    expect("达到 品阶 × 30 后扣除", 80.0, book.number("jinengExp"));
                    expect("等级加一", 1.0, book.number("LEVELnumber"));
                    expect("品阶文字按等级改写", "黄阶一级", book.text("belongTopinjietext"));
                }),
                new Action(2, player -> {
                    clearArea(player);
                    golem = placeGolem(player, 3);
                    enchant(player, 255);
                    NeoForge.EVENT_BUS.post(new HitEvent(golem, player, 5, false));
                    List<Corpse> corpses = corpses(player);
                    expect("起尸诀附魔命中放出一只", 1, corpses.size());
                    expect("附魔等级 × 2", 510.0, corpses.get(0).getPersistentData().getDouble("LEVELnumber"));
                })
        );
    }

    private static AABB area(ServerPlayer player) {
        return new AABB(player.blockPosition()).inflate(24);
    }

    private static List<Corpse> corpses(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Corpse.class, area(player));
    }

    /** 品阶 {@code grade} 的起尸诀书（阴组冷却 1 秒、上限 100 秒）。 */
    private static ItemStack book(ServerPlayer player, int grade) {
        String arguments = "yvanchuxiuzhen:book_003 " + SPELL + " " + grade + " 1 阴 x 无 3 1 0 0 0 0 0 0 0 1 0 0 0 0 0 100";
        ItemStack stack = Skills.createBook(SkillKind.SPELL, List.of(arguments.split(" ")));
        stack.set(SkillData.SKILL.get(), Skills.book(stack).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    /** 在玩家处放一只等级 {@link #RANK} 的尸体，以玩家为主人。 */
    private Corpse spawnCorpse(ServerPlayer player, double dx) {
        Corpse corpse = SkillSetup.<Corpse>entityType(Corpse.ID).get().create(player.serverLevel());
        Relations.copyAllegiance(player, corpse);
        corpse.tame(player);
        corpse.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
        corpse.empower(RANK);
        corpse.moveTo(player.getX() + dx, player.getY(), player.getZ(), -90, 0);
        player.serverLevel().addFreshEntity(corpse);
        return corpse;
    }

    /** 在玩家东侧 {@code dx} 处放一个不动的铁傀儡（非友方）。 */
    private IronGolem placeGolem(ServerPlayer player, double dx) {
        IronGolem placed = EntityType.IRON_GOLEM.create(player.serverLevel());
        placed.moveTo(player.getX() + dx, player.getY(), player.getZ(), 0, 0);
        placed.setNoAi(true);
        placed.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
        placed.setHealth((float) HEALTH);
        player.serverLevel().addFreshEntity(placed);
        return placed;
    }

    /** 僵直（{@code effect_033}）中没有可选技能：技能结束复位后不会立刻选出下一个。 */
    private static void stiffen(Corpse corpse) {
        StatusEffects.add(corpse, "effect_033", 2000, 0);
    }

    /** 直接让尸体以 {@code golem} 为目标开始一个技能（跳过随机选择）。 */
    private void start(Corpse corpse, String skill) {
        corpse.getPersistentData().putString("gongjizhe", golem.getStringUUID());
        corpse.getPersistentData().putString("linshigongjizhe", golem.getStringUUID());
        corpse.getPersistentData().putString("jineng", skill);
        corpse.getPersistentData().putString("jinengjinxing", "");
        corpse.getPersistentData().putDouble("entitytick", 0);
    }

    private void enchant(ServerPlayer player, int level) {
        Registry<Enchantment> enchantments = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        if (level > 0) {
            sword.enchant(enchantments.getHolderOrThrow(Enchants.key("qishi")), level);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
    }

    private void clearArea(ServerPlayer player) {
        corpses(player).forEach(Entity::discard);
        for (Entity other : player.serverLevel().getEntitiesOfClass(Entity.class, area(player), entity -> entity instanceof IronGolem || entity instanceof ArmorStand)) {
            other.discard();
        }
        golem = null;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clearArea(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Skills.setSpell(player, 1, ItemStack.EMPTY);
        SkillSelection.clear(player);
        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
    }
}
