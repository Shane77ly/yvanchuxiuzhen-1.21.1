package net.yvanchuxiuzhen.system.skill.api;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 技能书的四种类别，对应来源四个生成命令 {@code /jineng}、{@code /smanship}、{@code /shenfa}、{@code /flyskill}
 * 及书上名字字段 {@code jinengName}、{@code smanshipName}、{@code shenfaName}、{@code flyskillName}。
 */
public enum SkillKind implements StringRepresentable {
    /** 技能：占技能栏 16 槽之一，右键施放。 */
    SPELL("jineng"),
    /** 武器技：挥动武器时触发。 */
    WEAPON_ART("smanship"),
    /** 身法：双击方向键位移。 */
    MOVEMENT("shenfa"),
    /** 轻功（飞行）：飞行状态下的速度与特效。 */
    FLIGHT("flyskill");

    public static final Codec<SkillKind> CODEC = StringRepresentable.fromEnum(SkillKind::values);

    private final String id;

    SkillKind(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
