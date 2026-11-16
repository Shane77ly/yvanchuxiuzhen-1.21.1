package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/** 野外植物来源回调的同步生成通知；Creature独占魔兽和桃木精的构造与初始化。 */
public final class WildPlantCreatureEvent extends Event {
    public enum Kind {
        DEMON_EYE, BLOOD_PEACH
    }

    private final ServerLevel level;
    private final BlockPos pos;
    private final Kind kind;
    @Nullable private final LivingEntity target;

    public WildPlantCreatureEvent(ServerLevel level, BlockPos pos, Kind kind, @Nullable LivingEntity target) {
        this.level = level;
        this.pos = pos.immutable();
        this.kind = kind;
        this.target = target;
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    public LivingEntity target() {
        return target;
    }
}
