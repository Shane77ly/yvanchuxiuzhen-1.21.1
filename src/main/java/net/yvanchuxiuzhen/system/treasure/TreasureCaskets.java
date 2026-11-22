package net.yvanchuxiuzhen.system.treasure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.system.treasure.content.TreasureCasketBlockEntity;

/** 宝匣在自身坐标执行配置指令；五个全服结束刻后的移除任务只存在于当前服务端会话。 */
public final class TreasureCaskets {
    private static final List<Pending> PENDING = new ArrayList<>();

    private TreasureCaskets() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TreasureCaskets::tick);
        NeoForge.EVENT_BUS.addListener(TreasureCaskets::stopped);
    }

    public static void use(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel level) || !(level.getBlockEntity(pos) instanceof TreasureCasketBlockEntity casket)) return;
        CompoundTag data = casket.getPersistentData();
        String command = data.getString("zhiling");
        if (command.isEmpty() || data.getBoolean("yikaiqi")) return;
        level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, Vec3.atLowerCornerOf(pos),
                Vec2.ZERO, level, 4, "", Component.empty(), level.getServer(), null).withSuppressedOutput(), command);
        PENDING.add(new Pending(level, pos.immutable()));
        BlockEntity current = level.getBlockEntity(pos);
        if (current != null) {
            current.getPersistentData().putBoolean("yikaiqi", true);
            current.setChanged();
        }
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
    }

    private static void tick(ServerTickEvent.Post event) {
        List<Pending> due = new ArrayList<>();
        PENDING.removeIf(pending -> {
            if (pending.level.getServer() == event.getServer() && --pending.remaining <= 0) {
                due.add(pending);
                return true;
            }
            return false;
        });
        for (Pending pending : due) pending.level.setBlock(pending.pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void stopped(ServerStoppedEvent event) {
        PENDING.removeIf(pending -> pending.level.getServer() == event.getServer());
    }

    private static final class Pending {
        private final ServerLevel level;
        private final BlockPos pos;
        private int remaining = 5;

        private Pending(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }
    }
}
