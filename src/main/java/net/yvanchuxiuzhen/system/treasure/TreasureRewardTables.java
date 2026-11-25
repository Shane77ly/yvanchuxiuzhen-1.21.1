package net.yvanchuxiuzhen.system.treasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.DispatchKey;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.EmptyReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.LevelTier;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.MultiDropReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.PoolRefReward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.Reward;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.TierContext;
import net.yvanchuxiuzhen.system.treasure.TreasurePoolDefinition.WeightedEntry;

/**
 * 宝箱、书架、药架与武器架奖励池的 JSON 加载与加权抽样分发器；
 * 全部配置由 {@code content/treasure/reward_tables.json} 经 {@link TreasurePoolDefinition#CODEC} 驱动。
 */
public final class TreasureRewardTables {
    static final String RESOURCE_PATH = "content/treasure/reward_tables.json";
    private static final int MAX_DEPTH = 16;

    private static final List<TreasurePoolDefinition> POOLS = List.copyOf(
            ContentFiles.read(RESOURCE_PATH, TreasurePoolDefinition.CODEC));
    private static final Map<String, TreasurePoolDefinition> BY_ID = indexById(POOLS);
    private static final Map<DispatchKey, TreasurePoolDefinition> BY_DISPATCH = indexByDispatch(POOLS);

    private TreasureRewardTables() {
    }

    public static List<TreasurePoolDefinition> pools() {
        return POOLS;
    }

    /** 按容器大类/小类、等级与随机源抽样；无匹配分段或命中空分支时返回空。 */
    public static Optional<List<Selection>> select(String category, String subtype, double level, RandomSource random) {
        TreasurePoolDefinition pool = BY_DISPATCH.get(new DispatchKey(category, subtype));
        if (pool == null) {
            return Optional.empty();
        }
        return samplePool(pool, level, random);
    }

    /** 按奖励池定义直接抽样，供单元测试与内部子池调用验证。 */
    public static Optional<List<Selection>> samplePool(TreasurePoolDefinition pool, double level, RandomSource random) {
        List<Selection> resolved = new ArrayList<>();
        if (!resolve(pool, level, random, 0, resolved) || resolved.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(List.copyOf(resolved));
    }

    private static boolean resolve(TreasurePoolDefinition pool, double level, RandomSource random,
                                   int depth, List<Selection> out) {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException("宝箱奖励池嵌套过深或存在循环：" + pool.id());
        }
        Optional<LevelTier> tier = pool.tierFor(level);
        if (tier.isEmpty()) {
            return false;
        }
        Optional<WeightedEntry> entry = tier.get().pick(random);
        if (entry.isEmpty()) {
            return false;
        }
        return expand(entry.get().reward(), tier.get().effectiveContext(), level, random, depth, out);
    }

    private static boolean expand(Reward reward, TierContext context, double level, RandomSource random,
                                  int depth, List<Selection> out) {
        if (reward instanceof EmptyReward) {
            return false;
        }
        if (reward instanceof PoolRefReward ref) {
            TreasurePoolDefinition target = BY_ID.get(ref.pool());
            if (target == null) {
                throw new IllegalStateException("宝箱奖励池引用不存在：" + ref.pool());
            }
            int rolls = ref.rolls().map(range -> range.roll(random)).orElse(1);
            for (int index = 0; index < rolls; index++) {
                if (!resolve(target, level, random, depth + 1, out)) {
                    return false;
                }
            }
            return true;
        }
        if (reward instanceof MultiDropReward multi) {
            for (Reward child : multi.rewards()) {
                if (!expand(child, context, level, random, depth + 1, out)) {
                    return false;
                }
            }
            return true;
        }
        out.add(new Selection(reward, context));
        return true;
    }

    private static Map<String, TreasurePoolDefinition> indexById(List<TreasurePoolDefinition> pools) {
        Map<String, TreasurePoolDefinition> index = new HashMap<>();
        for (TreasurePoolDefinition pool : pools) {
            TreasurePoolDefinition previous = index.put(pool.id(), pool);
            if (previous != null) {
                throw new IllegalStateException("重复的宝箱奖励池 ID：" + pool.id());
            }
        }
        return Map.copyOf(index);
    }

    private static Map<DispatchKey, TreasurePoolDefinition> indexByDispatch(List<TreasurePoolDefinition> pools) {
        Map<DispatchKey, TreasurePoolDefinition> index = new HashMap<>();
        for (TreasurePoolDefinition pool : pools) {
            for (DispatchKey key : pool.dispatches()) {
                TreasurePoolDefinition previous = index.put(key, pool);
                if (previous != null) {
                    throw new IllegalStateException("重复的宝箱分派键：" + key.category() + "/" + key.subtype());
                }
            }
        }
        return Map.copyOf(index);
    }

    /** 已解析的单条发放结果与其所属等级分段上下文。 */
    public record Selection(Reward reward, TierContext context) {
    }
}
