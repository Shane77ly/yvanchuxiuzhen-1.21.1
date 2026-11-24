package net.yvanchuxiuzhen.system.xiulian;

import java.util.Optional;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.xiulian.api.BirthItems;
import net.yvanchuxiuzhen.system.xiulian.api.SpiritDiscData;

/** 灵盘右键时读取世界传送阵坐标，绘制气流并按来源状态提示方向。 */
public final class Ling11Guidance {
    private Ling11Guidance() {
    }

    public static void use(ServerPlayer player, ItemStack stack) {
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.literal("好像失灵了？"), false);
            return;
        }
        Optional<Vec3> center = WorldStructures.teleportArrayCenter(player.serverLevel());
        if (center.isEmpty()) {
            player.displayClientMessage(Component.literal("一个空的石盘？"), false);
            return;
        }
        Vec3 target = center.orElseThrow();
        if (target.x() + target.y() + target.z() == 0) {
            player.displayClientMessage(Component.literal("一个空的石盘？"), false);
            return;
        }

        double slope = (target.z() - player.getZ()) / (target.x() - player.getX());
        double intercept = player.getZ() - slope * player.getX();
        boolean negativeDirection = player.getX() - target.x() > 0;
        double offset = negativeDirection ? -1 : 1;
        SimpleParticleType particle = Particles.type("lingqi");
        for (int index = 0; index < 5; index++) {
            double x = player.getX() + offset;
            double z = slope * x + intercept;
            player.serverLevel().sendParticles(particle, x, player.getY() + 1, z, 3, 0.1, 0.1, 0.1, 0);
            offset += negativeDirection ? -0.5 : 0.5;
        }

        SpiritDiscData disc = BirthItems.spiritDisc(stack);
        if (disc.state().isEmpty()) {
            message(player, "好像有一道气流在指引我。");
            BirthItems.setSpiritDisc(stack, disc.withState("追踪"));
        } else if (disc.state().equals("追踪")) {
            giveDirectionHint(player, target);
        }
    }

    private static void giveDirectionHint(ServerPlayer player, Vec3 target) {
        double horizontal = Math.abs(player.getX() - target.x() + player.getZ() - target.z());
        double vertical = player.getY() - target.y();
        if (horizontal <= 8 && Math.abs(vertical) <= 10) {
            message(player, "这是个大阵！貌似在中间有启动按钮。");
        } else if (horizontal <= 16 && vertical <= 0) {
            message(player, "目标应该在上方。");
        } else if (horizontal <= 16 && Math.abs(vertical) <= 10) {
            message(player, "目标非常接近！");
        } else if (horizontal <= 16 && vertical >= 10) {
            message(player, "目标肯定在下方很深的地方。");
        } else if (Math.random() < 0.12) {
            message(player, "跟上去看看？");
        } else if (Math.random() < 0.166) {
            message(player, "不知道尽头通向哪里？");
        } else if (Math.random() < 0.2) {
            message(player, "跟上吧，至少不会变坏。");
        } else if (Math.random() < 0.25) {
            message(player, "反正不要钱，追上去看看。");
        } else if (Math.random() < 0.333) {
            message(player, "不知是谁的手笔。");
        }
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(player.getDisplayName().getString() + "：" + text), false);
    }
}
