package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.SkillText;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/**
 * 起尸诀的尸体来源与成长：
 * <ul>
 *   <li>技能“起尸诀”（来源 {@code Skillyin01shifang}）：在施放者四角（东南、西南、西北、东北各 2 格）各放出一只，等级 {@code ceil(品阶 × 0.5)}，
 *       从脚下一格起向上最多找 10 格，找到本格与上一格都是空气处放出；记下施放时所选技能的序号 {@code qishijveweizhi}（来源先写进玩家体质物品的同名键再读回，
 *       这里直接取技能栏序号）。</li>
 *   <li>起尸诀附魔 {@code qishi} 的命中（来源 {@code EntityHurt2} 的一段，订阅 combat 的 {@link HitEvent}）：随机数 1–100 小于附魔等级时，
 *       在攻击者周围随机找位置（最多试 4 次，找到空气、可替换的植物就停，没找到则用最后一次的位置）放出<b>一只</b>，等级 {@code 附魔等级 × 2}，
 *       经初始生成，没有技能序号（所以不会让书升级）。来源按 1.18.2 的材质 {@code PLANT}、{@code REPLACEABLE_PLANT} 判断植物，这里取可替换方块标签与灌木类方块。</li>
 *   <li>成长（来源 {@code Skillyin01DangZheGeShiTiShaSiLingGeShiTi}）：尸体杀死实体时，在主人所在维度找到主人，取技能栏里记下序号的书；是“起尸诀”且品阶低于 40 时，
 *       修为 {@code jinengExp} 加上<b>被杀实体</b>的 {@code LEVELnumber}（不是尸体的）；修为达到 {@code 品阶 × 30} 时扣除该值，书的等级 {@code LEVELnumber} 加一，
 *       品阶文字 {@code belongTopinjietext} 按新等级改写（1–40 级为黄、玄、地、天阶一至十级），品阶数值不变。</li>
 * </ul>
 */
public final class Corpses {
    private static final String SPELL = "起尸诀";
    private static final String YIN = "阴";
    private static final int[][] CORNERS = {{2, 2}, {-2, 2}, {-2, -2}, {2, -2}};
    private static final int RELEASE_SEARCH = 10;
    private static final int ENCHANT_SEARCH = 4;

    private Corpses() {
    }

    public static void register() {
        SkillReleases.register(YIN, Corpses::release, SPELL);
    }

    private static Corpse create(ServerLevel level, Entity master, double rank) {
        Corpse corpse = SkillSetup.<Corpse>entityType(Corpse.ID).get().create(level);
        Relations.copyAllegiance(master, corpse);
        if (master instanceof Player player) {
            corpse.tame(player);
        }
        corpse.getPersistentData().putString("belongToMasterUUID", master.getStringUUID());
        corpse.empower(rank);
        return corpse;
    }

    private static void release(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        double rank = Math.ceil(book.grade() * 0.5);
        for (int[] corner : CORNERS) {
            double x = caster.getX() + corner[0];
            double z = caster.getZ() + corner[1];
            double y = caster.getY() - 1;
            for (int index = 0; index < RELEASE_SEARCH; index++) {
                if (level.isEmptyBlock(BlockPos.containing(x, y, z)) && level.isEmptyBlock(BlockPos.containing(x, y + 1, z))) {
                    break;
                }
                y++;
            }
            Corpse corpse = create(level, caster, rank);
            corpse.moveTo(x, y, z, level.getRandom().nextFloat() * 360F, 0);
            corpse.getPersistentData().putDouble("qishijveweizhi", Skills.state(caster).selectedSlot());
            level.addFreshEntity(corpse);
        }
    }

    public static void onHit(HitEvent event) {
        if (!(event.attacker() instanceof LivingEntity attacker) || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        int enchant = Enchants.mainHand(attacker, Enchants.key("qishi"));
        if (enchant == 0 || Mth.nextInt(level.getRandom(), 1, 100) >= enchant) {
            return;
        }
        double x = attacker.getX();
        double z = attacker.getZ();
        for (int index = 0; index < ENCHANT_SEARCH; index++) {
            x = attacker.getX() + Mth.nextInt(level.getRandom(), -4, 4);
            z = attacker.getZ() + Mth.nextInt(level.getRandom(), -4, 4);
            if (passable(level, BlockPos.containing(x, attacker.getY(), z))) {
                break;
            }
        }
        Corpse corpse = create(level, attacker, enchant * 2.0);
        corpse.moveTo(x, attacker.getY(), z, level.getRandom().nextFloat() * 360F, 0);
        corpse.finalizeSpawn(level, level.getCurrentDifficultyAt(corpse.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(corpse);
    }

    private static boolean passable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.isEmptyBlock(pos) || state.is(BlockTags.REPLACEABLE) || state.getBlock() instanceof BushBlock;
    }

    static void grow(Corpse corpse, Entity killed) {
        ServerLevel level = (ServerLevel) corpse.level();
        int slot = (int) corpse.getPersistentData().getDouble("qishijveweizhi");
        if (slot < 1 || slot > SkillState.SPELL_SLOTS) {
            return;
        }
        String master = corpse.getPersistentData().getString("belongToMasterUUID");
        for (ServerPlayer player : level.players()) {
            if (!player.getStringUUID().equals(master)) {
                continue;
            }
            ItemStack stack = Skills.spell(player, slot).copy();
            Optional<SkillBook> found = Skills.book(stack).filter(book -> book.name().equals(SPELL) && book.grade() < 40);
            if (found.isPresent()) {
                stack.set(SkillData.SKILL.get(), advance(found.get(), killed.getPersistentData().getDouble("LEVELnumber")));
                Skills.setSpell(player, slot, stack);
            }
            return;
        }
    }

    private static SkillBook advance(SkillBook book, double gained) {
        double experience = book.number("jinengExp") + gained;
        double needed = book.grade() * 30;
        if (experience < needed) {
            return book.withNumber("jinengExp", experience);
        }
        double level = book.number("LEVELnumber") + 1;
        SkillBook next = book.withNumber("jinengExp", experience - needed).withNumber("LEVELnumber", level);
        return level >= 1 && level <= 40 ? next.withText("belongTopinjietext", SkillText.grade(level)) : next;
    }
}
