package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** 玩家副本持久态（等待入场、控制器落点、回程坐标与原游戏模式），随玩家存档保存。 */
public record DungeonPlayerData(
        boolean waiting,
        double countdown,
        BlockPos controllerPos,
        double entryX,
        double entryY,
        double entryZ,
        String dungeonName,
        String definitionId,
        String returnDimension,
        BlockPos returnPos,
        boolean returning,
        String previousGameMode,
        boolean needGenerate,
        String templateId
) {
    public static final DungeonPlayerData EMPTY = new DungeonPlayerData(
            false, 0, BlockPos.ZERO, 0, 0, 0, "", "", "", BlockPos.ZERO, false, "", false, "");

    public static final Codec<DungeonPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("waiting", false).forGetter(DungeonPlayerData::waiting),
            Codec.DOUBLE.optionalFieldOf("countdown", 0.0).forGetter(DungeonPlayerData::countdown),
            BlockPos.CODEC.optionalFieldOf("controller_pos", BlockPos.ZERO).forGetter(DungeonPlayerData::controllerPos),
            Codec.DOUBLE.optionalFieldOf("entry_x", 0.0).forGetter(DungeonPlayerData::entryX),
            Codec.DOUBLE.optionalFieldOf("entry_y", 0.0).forGetter(DungeonPlayerData::entryY),
            Codec.DOUBLE.optionalFieldOf("entry_z", 0.0).forGetter(DungeonPlayerData::entryZ),
            Codec.STRING.optionalFieldOf("dungeon_name", "").forGetter(DungeonPlayerData::dungeonName),
            Codec.STRING.optionalFieldOf("definition_id", "").forGetter(DungeonPlayerData::definitionId),
            Codec.STRING.optionalFieldOf("return_dimension", "").forGetter(DungeonPlayerData::returnDimension),
            BlockPos.CODEC.optionalFieldOf("return_pos", BlockPos.ZERO).forGetter(DungeonPlayerData::returnPos),
            Codec.BOOL.optionalFieldOf("returning", false).forGetter(DungeonPlayerData::returning),
            Codec.STRING.optionalFieldOf("previous_game_mode", "").forGetter(DungeonPlayerData::previousGameMode),
            Codec.BOOL.optionalFieldOf("need_generate", false).forGetter(DungeonPlayerData::needGenerate),
            Codec.STRING.optionalFieldOf("template_id", "").forGetter(DungeonPlayerData::templateId)
    ).apply(instance, DungeonPlayerData::new));

    public DungeonPlayerData withWaiting(boolean value) {
        return new DungeonPlayerData(value, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withCountdown(double value) {
        return new DungeonPlayerData(waiting, value, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withControllerPos(BlockPos value) {
        return new DungeonPlayerData(waiting, countdown, value, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withEntryPos(double x, double y, double z) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, x, y, z, dungeonName, definitionId,
                returnDimension, returnPos, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withReturnPos(BlockPos value) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, value, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withReturnDimension(String value) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                value, returnPos, returning, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withReturning(boolean value) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, value, previousGameMode, needGenerate, templateId);
    }

    public DungeonPlayerData withPreviousGameMode(String value) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, returning, value, needGenerate, templateId);
    }

    public DungeonPlayerData withNeedGenerate(boolean value) {
        return new DungeonPlayerData(waiting, countdown, controllerPos, entryX, entryY, entryZ, dungeonName, definitionId,
                returnDimension, returnPos, returning, previousGameMode, value, templateId);
    }

    public DungeonPlayerData withEnteredInfo(String name, String defId, String template, boolean waitingFlag, double timer) {
        return new DungeonPlayerData(waitingFlag, timer, controllerPos, entryX, entryY, entryZ, name, defId,
                returnDimension, returnPos, returning, previousGameMode, needGenerate, template);
    }
}
