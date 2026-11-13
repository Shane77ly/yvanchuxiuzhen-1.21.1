package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 霓裳草开花时的同步服务端通知；植物持有阶段，妖兽召引由生物系统处理。 */
public final class NichangGrassBloomEvent extends Event {
    private final ServerLevel level;
    private final BlockPos pos;

    public NichangGrassBloomEvent(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos.immutable();
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }
}
