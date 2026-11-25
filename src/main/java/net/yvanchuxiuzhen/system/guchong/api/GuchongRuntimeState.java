package net.yvanchuxiuzhen.system.guchong.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** 蛊虫实体的认主、主人、所属蛊罐槽位、看家坐标与指令攻击目标状态。 */
public record GuchongRuntimeState(
        boolean tamed,
        String ownerUuid,
        String jarUuid,
        int jarSlot,
        boolean guarding,
        BlockPos homePos,
        String commandTargetUuid
) {
    public static final GuchongRuntimeState EMPTY = new GuchongRuntimeState(false, "", "", 0, false, BlockPos.ZERO, "");
    public static final Codec<GuchongRuntimeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("tamed", false).forGetter(GuchongRuntimeState::tamed),
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(GuchongRuntimeState::ownerUuid),
            Codec.STRING.optionalFieldOf("jar_uuid", "").forGetter(GuchongRuntimeState::jarUuid),
            Codec.INT.optionalFieldOf("jar_slot", 0).forGetter(GuchongRuntimeState::jarSlot),
            Codec.BOOL.optionalFieldOf("guarding", false).forGetter(GuchongRuntimeState::guarding),
            BlockPos.CODEC.optionalFieldOf("home_pos", BlockPos.ZERO).forGetter(GuchongRuntimeState::homePos),
            Codec.STRING.optionalFieldOf("command_target_uuid", "").forGetter(GuchongRuntimeState::commandTargetUuid)
    ).apply(instance, GuchongRuntimeState::new));

    public GuchongRuntimeState {
        ownerUuid = ownerUuid == null ? "" : ownerUuid;
        jarUuid = jarUuid == null ? "" : jarUuid;
        homePos = homePos == null ? BlockPos.ZERO : homePos;
        commandTargetUuid = commandTargetUuid == null ? "" : commandTargetUuid;
    }

    public GuchongRuntimeState withTamed(boolean nextTamed) {
        return new GuchongRuntimeState(nextTamed, ownerUuid, jarUuid, jarSlot, guarding, homePos, commandTargetUuid);
    }

    public GuchongRuntimeState withOwnerUuid(String nextOwnerUuid) {
        return new GuchongRuntimeState(tamed, nextOwnerUuid, jarUuid, jarSlot, guarding, homePos, commandTargetUuid);
    }

    public GuchongRuntimeState withJar(String nextJarUuid, int nextJarSlot) {
        return new GuchongRuntimeState(tamed, ownerUuid, nextJarUuid, nextJarSlot, guarding, homePos, commandTargetUuid);
    }

    public GuchongRuntimeState withGuard(boolean nextGuarding, BlockPos nextHomePos) {
        return new GuchongRuntimeState(tamed, ownerUuid, jarUuid, jarSlot, nextGuarding, nextHomePos, commandTargetUuid);
    }

    public GuchongRuntimeState withCommandTargetUuid(String nextTarget) {
        return new GuchongRuntimeState(tamed, ownerUuid, jarUuid, jarSlot, guarding, homePos, nextTarget);
    }
}
