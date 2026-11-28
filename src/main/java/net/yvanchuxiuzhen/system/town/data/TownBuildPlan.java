package net.yvanchuxiuzhen.system.town.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 城镇规划卷轴与规划核心共用的强类型建筑蓝图参数（{@code yvanchuxiuzhen:town_build_plan}）。 */
public record TownBuildPlan(
        String buildName,
        String buildType,
        String buildDirection,
        List<BlockPos> shifts,
        List<BlockPos> lengths,
        int needLog,
        int needStone,
        int needWool,
        int ownedLog,
        int ownedStone,
        int ownedWool) {

    public static final TownBuildPlan EMPTY = new TownBuildPlan(
            "", "", "",
            Collections.nCopies(4, BlockPos.ZERO),
            Collections.nCopies(4, BlockPos.ZERO),
            0, 0, 0, 0, 0, 0);

    public static final Codec<TownBuildPlan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("build_name").forGetter(TownBuildPlan::buildName),
            Codec.STRING.fieldOf("build_type").forGetter(TownBuildPlan::buildType),
            Codec.STRING.fieldOf("build_direction").forGetter(TownBuildPlan::buildDirection),
            BlockPos.CODEC.listOf().fieldOf("shifts").forGetter(TownBuildPlan::shifts),
            BlockPos.CODEC.listOf().fieldOf("lengths").forGetter(TownBuildPlan::lengths),
            Codec.INT.fieldOf("need_log").forGetter(TownBuildPlan::needLog),
            Codec.INT.fieldOf("need_stone").forGetter(TownBuildPlan::needStone),
            Codec.INT.fieldOf("need_wool").forGetter(TownBuildPlan::needWool),
            Codec.INT.fieldOf("owned_log").forGetter(TownBuildPlan::ownedLog),
            Codec.INT.fieldOf("owned_stone").forGetter(TownBuildPlan::ownedStone),
            Codec.INT.fieldOf("owned_wool").forGetter(TownBuildPlan::ownedWool)
    ).apply(instance, TownBuildPlan::new));

    public static final StreamCodec<ByteBuf, TownBuildPlan> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public TownBuildPlan {
        shifts = List.copyOf(shifts);
        lengths = List.copyOf(lengths);
    }

    public BlockPos shift(int orientation1To4) {
        return orientation1To4 >= 1 && orientation1To4 <= shifts.size() ? shifts.get(orientation1To4 - 1) : BlockPos.ZERO;
    }

    public BlockPos length(int orientation1To4) {
        return orientation1To4 >= 1 && orientation1To4 <= lengths.size() ? lengths.get(orientation1To4 - 1) : BlockPos.ZERO;
    }

    public int need(int materialIndex) {
        return switch (materialIndex) {
            case 0 -> needLog;
            case 1 -> needStone;
            case 2 -> needWool;
            default -> 0;
        };
    }

    public int owned(int materialIndex) {
        return switch (materialIndex) {
            case 0 -> ownedLog;
            case 1 -> ownedStone;
            case 2 -> ownedWool;
            default -> 0;
        };
    }

    public TownBuildPlan withDirection(String direction) {
        return new TownBuildPlan(buildName, buildType, direction, shifts, lengths,
                needLog, needStone, needWool, ownedLog, ownedStone, ownedWool);
    }

    public TownBuildPlan withTypeAndDirection(String type, String direction) {
        return new TownBuildPlan(buildName, type, direction, shifts, lengths,
                needLog, needStone, needWool, ownedLog, ownedStone, ownedWool);
    }

    public TownBuildPlan withIdentity(String name, String type, String direction) {
        return new TownBuildPlan(name, type, direction, shifts, lengths,
                needLog, needStone, needWool, ownedLog, ownedStone, ownedWool);
    }

    public TownBuildPlan withNeeds(int log, int stone, int wool) {
        return new TownBuildPlan(buildName, buildType, buildDirection, shifts, lengths,
                log, stone, wool, ownedLog, ownedStone, ownedWool);
    }

    public TownBuildPlan withOwned(int log, int stone, int wool) {
        return new TownBuildPlan(buildName, buildType, buildDirection, shifts, lengths,
                needLog, needStone, needWool, log, stone, wool);
    }
}
