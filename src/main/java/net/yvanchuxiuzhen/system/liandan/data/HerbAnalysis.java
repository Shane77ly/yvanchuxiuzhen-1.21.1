package net.yvanchuxiuzhen.system.liandan.data;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

/** 玩家药草解析真值与最近一次界面缓存的只读快照，读取原始数据时返回独立副本。 */
public record HerbAnalysis(CompoundTag data) {
    public static final HerbAnalysis EMPTY = new HerbAnalysis(new CompoundTag());
    public static final Codec<HerbAnalysis> CODEC = CompoundTag.CODEC.xmap(HerbAnalysis::new, HerbAnalysis::data);

    public HerbAnalysis {
        data = data.copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }

    public int progress(String itemId) {
        return data.getInt(itemId + "jindu");
    }

    public int maximum(String itemId) {
        return data.getInt(itemId + "jindumax");
    }

    public String selectedName() {
        return data.getString("txtname");
    }

    public String selectedId() {
        return data.getString("txtitemid");
    }

    public String progressText() {
        return data.getString("txtjindu");
    }

    public String needText() {
        return data.getString("txtjiesuoneed");
    }

    /** 界面药效使用从一开始的原序号，未解析行仍保留来源冒号。 */
    public String effectText(int ordinal) {
        return "§b" + data.getString("txtyaoxiao" + ordinal) + ":§8" + data.getString("txtyaoxiao" + ordinal + "jieshao");
    }

    public List<RevealedEffect> revealed(String itemId) {
        List<RevealedEffect> result = new ArrayList<>();
        for (int ordinal = 1; ordinal <= progress(itemId); ordinal++) {
            result.add(new RevealedEffect(data.getString(itemId + "yaoxiao" + ordinal),
                    data.getString(itemId + "yaoxiao" + ordinal + "txt")));
        }
        return List.copyOf(result);
    }

    public record RevealedEffect(String name, String description) {
    }
}
