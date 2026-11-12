package net.yvanchuxiuzhen.system.fulu.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** 玩家知识按来源学习顺序保存；零保留异常命令产生的空项，界面当前选择由画符台持有。 */
public record FuluKnowledge(List<Integer> heads, List<Integer> pillars, List<Integer> cores) {
    public static final FuluKnowledge INITIAL = new FuluKnowledge(List.of(1), List.of(1, 2, 3, 4), List.of(1));
    public static final Codec<FuluKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().fieldOf("heads").forGetter(FuluKnowledge::heads),
            Codec.INT.listOf().fieldOf("pillars").forGetter(FuluKnowledge::pillars),
            Codec.INT.listOf().fieldOf("cores").forGetter(FuluKnowledge::cores)
    ).apply(instance, FuluKnowledge::new));

    public FuluKnowledge {
        heads = List.copyOf(heads);
        pillars = List.copyOf(pillars);
        cores = List.copyOf(cores);
    }

    /** 来源按分隔符读取时忽略末尾空项，中间空项仍占据选择位置。 */
    public FuluKnowledge visible() {
        return new FuluKnowledge(trimTrailingEmpty(heads), trimTrailingEmpty(pillars), trimTrailingEmpty(cores));
    }

    private static List<Integer> trimTrailingEmpty(List<Integer> values) {
        int end = values.size();
        while (end > 0 && values.get(end - 1) == 0) end--;
        return values.subList(0, end);
    }
}
