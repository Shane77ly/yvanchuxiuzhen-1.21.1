package net.yvanchuxiuzhen.system.skill.api;

/** 妖兽等外部系统按来源刻数安排技能表现回调。 */
public final class SkillDelays {
    private SkillDelays() { }

    public static void after(int ticks, Runnable task) {
        net.yvanchuxiuzhen.system.skill.SkillDelays.after(ticks, task);
    }
}
