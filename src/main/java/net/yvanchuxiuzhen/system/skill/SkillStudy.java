package net.yvanchuxiuzhen.system.skill;

import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.RootRequirement;

/**
 * 技能书的研习（来源 {@code BookRightClick} 研习分支，功法书、技能书、武器技书、身法书、轻功书共用）：
 * 已被本人或他人研习时只提示；境界不低于书的要求且满足灵根要求时，书记下研习者，关闭当前界面并提示研习成功。
 * 研习成功时发送两种天干地支粒子和闪光；答题计数（{@code daduishuliang}）由 book 的自然研习入口在成功后清零。
 */
public final class SkillStudy {
    private SkillStudy() {
    }

    public static void study(ServerPlayer player, ItemStack stack) {
        SkillBook book = Skills.book(stack).orElseThrow();
        if (book.ownedBy(player.getUUID())) {
            player.displayClientMessage(Component.literal("您已学习此功法！"), false);
            return;
        }
        if (book.owner().isPresent()) {
            player.displayClientMessage(Component.literal("此功法已被别人习得。"), false);
            return;
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) < book.level()) {
            player.displayClientMessage(Component.literal("§4需要至少达到" + SkillText.level(book.level()) + "境界才可研习。"), false);
            return;
        }
        Optional<String> unmet = RootRequirement.unmet(player, book.body());
        if (unmet.isPresent()) {
            player.displayClientMessage(Component.literal(unmet.get()), false);
            return;
        }
        player.serverLevel().sendParticles(Particles.type("tiangandizhi"), player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.serverLevel().sendParticles(Particles.type("tiangandizhi_2"), player.getX(), player.getY(), player.getZ(), 20, 2, 0, 2, 0);
        player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
        stack.set(SkillData.SKILL.get(), book.withOwner(player.getUUID(), player.getDisplayName().getString()));
        player.displayClientMessage(Component.literal("§6研习成功！"), false);
        player.closeContainer();
    }
}
