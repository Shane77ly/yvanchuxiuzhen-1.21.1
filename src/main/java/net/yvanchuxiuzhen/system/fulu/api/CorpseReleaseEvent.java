package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 封存符在方块顶面释放尸体的同步服务端通知；载体数据以副本交给生物系统。 */
public final class CorpseReleaseEvent extends Event {
    private final ServerLevel level;
    private final BlockPos clickedPos;
    private final CompoundTag corpse;

    public CorpseReleaseEvent(ServerLevel level, BlockPos clickedPos, CompoundTag corpse) {
        this.level = level;
        this.clickedPos = clickedPos.immutable();
        this.corpse = corpse.copy();
    }

    public ServerLevel level() { return level; }
    public BlockPos clickedPos() { return clickedPos; }
    public CompoundTag corpse() { return corpse.copy(); }
}
