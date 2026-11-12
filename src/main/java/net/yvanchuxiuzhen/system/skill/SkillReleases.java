package net.yvanchuxiuzhen.system.skill;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;

/**
 * 技能施放的分派表（来源 {@code Jinenglist01}）：按灵根要求的首字（金、水、木、火、土、阴）与技能名找到施放过程。
 * 来源按首字分组后逐名比较，同名技能在不同首字下互不相干；没有登记的组合施放时什么也不发生。
 */
public final class SkillReleases {
    /** 施放过程：在施放者处释放技能的效果。 */
    @FunctionalInterface
    public interface Release {
        void cast(ServerPlayer caster, SkillBook book);
    }

    private static final Map<String, Release> BY_NAME = new HashMap<>();

    private SkillReleases() {
    }

    /** 登记首字为 {@code bodyMark} 的若干技能名共用的施放过程。 */
    public static void register(String bodyMark, Release release, String... names) {
        for (String name : names) {
            BY_NAME.put(bodyMark + name, release);
        }
    }

    static void dispatch(ServerPlayer caster, SkillBook book) {
        Release release = BY_NAME.get(book.body().substring(0, Math.min(1, book.body().length())) + book.name());
        if (release != null) {
            release.cast(caster, book);
        }
    }
}
