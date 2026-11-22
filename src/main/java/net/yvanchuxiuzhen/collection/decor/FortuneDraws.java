package net.yvanchuxiuzhen.collection.decor;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 签筒每次右键独立等待四十个服务端刻，按来源系统时钟和世界时间抽签。 */
public final class FortuneDraws {
    private static final ConcurrentLinkedQueue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private FortuneDraws() { }

    public static void shake(Level level, BlockPos pos, Player player) {
        level.setBlock(pos, level.getBlockState(pos).setValue(AnimatedDecorBlock.FORTUNE_ANIMATION, 1), Block.UPDATE_ALL);
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoqiantong"));
        if (level.isClientSide) level.playLocalSound(pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, sound, SoundSource.NEUTRAL, 1, 1, false);
        else level.playSound(null, BlockPos.containing(pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5), sound, SoundSource.NEUTRAL, 1, 1);
        PENDING.add(new Pending(level, pos.immutable(), player));
    }

    public static void tick(ServerTickEvent.Post event) {
        for (Pending pending : PENDING) {
            if (--pending.ticks == 0 && PENDING.remove(pending)) draw(pending.level, pending.pos, pending.player);
        }
    }
    public static void stopped(ServerStoppedEvent event) { PENDING.clear(); }

    private static void draw(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) server.sendParticles(ParticleTypes.WAX_ON, pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, 5, 0, 0, 0, 1);
        String time = Long.toString(level.getDayTime());
        double dayTimeTail = Double.parseDouble(time.length() >= 2 ? time.substring(time.length() - 2) : time);
        int fortune = (int) Math.ceil((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND) + dayTimeTail) / 7);
        int grade = grade(fortune);
        if (!player.level().isClientSide) {
            player.displayClientMessage(grade == 0 ? Component.empty() : Component.translatable("message.yvanchuxiuzhen.decor.fortune.grade." + grade), false);
            player.displayClientMessage(fortune < 1 || fortune > 36 ? Component.literal("§f") : Component.translatable("message.yvanchuxiuzhen.decor.fortune.poem." + fortune), false);
        }
        if (level.isClientSide) NeoForge.EVENT_BUS.post(new ClientResult(grade == 0 ? 7 : grade));
    }

    private static int grade(int fortune) {
        if (fortune < 1 || fortune > 36) return 0;
        if (fortune <= 3) return 1;
        if (fortune <= 8) return 2;
        if (fortune <= 15) return 3;
        if (fortune <= 21) return 4;
        if (fortune <= 28) return 5;
        if (fortune <= 33) return 6;
        return 7;
    }

    /** 集成服的客户端待办由共享队列中的服务端刻推进，结果仅发布本地签条激活事件；专服没有客户端待办。 */
    public static final class ClientResult extends Event {
        private final int grade;
        private ClientResult(int grade) { this.grade = grade; }
        public int grade() { return grade; }
    }

    private static final class Pending {
        private final Level level;
        private final BlockPos pos;
        private final Player player;
        private int ticks = 40;
        private Pending(Level level, BlockPos pos, Player player) {
            this.level = level;
            this.pos = pos;
            this.player = player;
        }
    }
}
