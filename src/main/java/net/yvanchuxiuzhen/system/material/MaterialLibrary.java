package net.yvanchuxiuzhen.system.material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;
import net.yvanchuxiuzhen.system.material.data.MaterialData;

/** 材料定义目录与公开实例操作；生产系统通过这里读取，不能直接访问玩家附件。 */
public final class MaterialLibrary {
    private static final Map<String, MaterialDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        for (MaterialDefinition definition : ContentFiles.read("content/material/definitions.json", MaterialDefinition.CODEC)) {
            if (DEFINITIONS.put(definition.itemId(), definition) != null) {
                throw new IllegalStateException("重复材料定义 " + definition.itemId());
            }
        }
    }

    private MaterialLibrary() {
    }

    public static List<MaterialDefinition> all() {
        return DEFINITIONS.values().stream().toList();
    }

    public static MaterialDefinition definition(String itemId) {
        return DEFINITIONS.get(itemId);
    }

    public static MaterialDefinition definition(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return DEFINITIONS.get(id.toString());
    }

    public static MaterialInstance instance(ItemStack stack) {
        return stack.getOrDefault(MaterialData.INSTANCE.get(), MaterialInstance.EMPTY);
    }

    public static void setInstance(ItemStack stack, int level, int quality) {
        stack.set(MaterialData.INSTANCE.get(), new MaterialInstance(level, quality));
    }

    public static MaterialAnalysis analysis(ServerPlayer player) {
        return player.getData(MaterialData.ANALYSIS);
    }

    public static void initializeYihuo(ServerPlayer player) {
        MaterialAnalysis analysis = analysis(player);
        if (analysis.yihuoInitialized()) {
            return;
        }
        MaterialAnalysis next = analysis;
        for (int number = 221; number <= 239; number++) {
            String itemId = "yvanchuxiuzhen:cailiao_" + number;
            MaterialDefinition definition = definition(itemId);
            if (definition != null) {
                // 来源登录过程对这 19 条异火材料统一写入固定的 jindumax=6。
                next = next.withProgress(itemId, 6);
            }
        }
        player.setData(MaterialData.ANALYSIS, next.withYihuoInitialized());
        player.syncData(MaterialData.ANALYSIS);
    }
}
