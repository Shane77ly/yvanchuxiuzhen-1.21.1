package net.yvanchuxiuzhen.system.profession.api;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** 十种副职业，顺序与来源 {@code FuzhiyeLevelUp} 的独立升级分支一致。 */
public enum Profession implements StringRepresentable {
    ALCHEMY("alchemy", "炼丹", "炼丹术提升一级！"),
    REFINING("refining", "炼器", "炼器术提升一级！"),
    RUNE("rune", "画符", "绘符术提升一级！"),
    GATHERING("gathering", "采集", "采集等级提升一级！"),
    COOKING("cooking", "厨艺", "厨艺提升一级！"),
    PUPPET("puppet", "控偶", "控偶术提升一级！"),
    BREEDING("breeding", "御灵", "御灵术提升一级！"),
    GHOST("ghost", "养鬼", "养鬼术提升一级！"),
    INSECT("insect", "养蛊", "养蛊术提升一级！"),
    BLOOD("blood", "血道", "血道提升一级！");

    public static final Codec<Profession> CODEC = StringRepresentable.fromEnum(Profession::values);

    private final String id;
    private final String displayName;
    private final String levelUpMessage;

    Profession(String id, String displayName, String levelUpMessage) {
        this.id = id;
        this.displayName = displayName;
        this.levelUpMessage = levelUpMessage;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String levelUpMessage() {
        return levelUpMessage;
    }
}
