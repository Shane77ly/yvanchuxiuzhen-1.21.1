package net.yvanchuxiuzhen.system.skill.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;

/**
 * 玩家六个冷却分组的剩余刻数（附件 {@code skill_cooldown}，来源 {@code colddownGold}、{@code colddownWater}、
 * {@code colddownTree}、{@code colddownFire}、{@code colddownSoil}、{@code colddownYinyang}），按 {@link SkillElement} 次序排列。
 * 来源复制策略为只在非死亡克隆时复制，死亡后清零。值不可变。
 */
public record SkillCooldowns(List<Double> ticks) {
    public static final SkillCooldowns EMPTY = new SkillCooldowns(Collections.nCopies(SkillElement.values().length, 0.0));
    public static final Codec<SkillCooldowns> CODEC = Codec.DOUBLE.listOf(SkillElement.values().length, SkillElement.values().length)
            .xmap(SkillCooldowns::new, SkillCooldowns::ticks);
    public static final StreamCodec<ByteBuf, SkillCooldowns> STREAM_CODEC = ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list(SkillElement.values().length))
            .map(SkillCooldowns::new, SkillCooldowns::ticks);

    public double get(SkillElement element) {
        return ticks.get(element.ordinal());
    }

    public SkillCooldowns with(SkillElement element, double value) {
        List<Double> next = new ArrayList<>(ticks);
        next.set(element.ordinal(), value);
        return new SkillCooldowns(next);
    }
}
