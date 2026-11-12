package net.yvanchuxiuzhen.system.storage.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** 玩家材料仓的不可变快照；计数与等级标记分别保存，以保留来源各消费者的更新规则。 */
public record MaterialWarehouse(List<Group> groups, int total, int styles, int pageMax, Preferences preferences) {
    public static final MaterialWarehouse EMPTY = new MaterialWarehouse(List.of(), 0, 0, 1, Preferences.EMPTY);
    public static final Codec<MaterialWarehouse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Group.CODEC.listOf().fieldOf("groups").forGetter(MaterialWarehouse::groups),
            Codec.INT.fieldOf("total").forGetter(MaterialWarehouse::total),
            Codec.INT.fieldOf("styles").forGetter(MaterialWarehouse::styles),
            Codec.INT.fieldOf("page_max").forGetter(MaterialWarehouse::pageMax),
            Preferences.CODEC.fieldOf("preferences").forGetter(MaterialWarehouse::preferences)
    ).apply(instance, MaterialWarehouse::new));

    public MaterialWarehouse {
        groups = List.copyOf(groups);
    }

    public Group group(String itemId) {
        return groups.stream().filter(group -> group.itemId().equals(itemId)).findFirst().orElse(null);
    }

    public int count(String itemId) {
        Group group = group(itemId);
        return group == null ? 0 : group.total();
    }

    public record Grade(int level, int quality, int count) {
        public static final Codec<Grade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("level").forGetter(Grade::level),
                Codec.INT.fieldOf("quality").forGetter(Grade::quality),
                Codec.INT.fieldOf("count").forGetter(Grade::count)
        ).apply(instance, Grade::new));
    }

    public record Group(String itemId, int total, int levelCount, List<Integer> activeLevels, List<Grade> amounts) {
        public static final Codec<Group> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(Group::itemId),
                Codec.INT.fieldOf("total").forGetter(Group::total),
                Codec.INT.fieldOf("level_count").forGetter(Group::levelCount),
                Codec.INT.listOf().fieldOf("active_levels").forGetter(Group::activeLevels),
                Grade.CODEC.listOf().fieldOf("amounts").forGetter(Group::amounts)
        ).apply(instance, Group::new));

        public Group {
            activeLevels = List.copyOf(activeLevels);
            amounts = List.copyOf(amounts);
        }

        public int count(int level, int quality) {
            return amounts.stream().filter(amount -> amount.level() == level && amount.quality() == quality)
                    .mapToInt(Grade::count).findFirst().orElse(0);
        }

        /** 来源取出与扣料只用五档品阶判断一个等级是否耗尽。 */
        public int count(int level) {
            int count = 0;
            for (int quality = 0; quality <= 4; quality++) count += count(level, quality);
            return count;
        }
    }

    public record Preferences(int page, int sort, int filters, boolean automatic, boolean spending) {
        public static final Preferences EMPTY = new Preferences(1, 0, 0, false, false);
        public static final Codec<Preferences> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("page").forGetter(Preferences::page),
                Codec.INT.fieldOf("sort").forGetter(Preferences::sort),
                Codec.INT.fieldOf("filters").forGetter(Preferences::filters),
                Codec.BOOL.fieldOf("automatic").forGetter(Preferences::automatic),
                Codec.BOOL.fieldOf("spending").forGetter(Preferences::spending)
        ).apply(instance, Preferences::new));
    }
}
