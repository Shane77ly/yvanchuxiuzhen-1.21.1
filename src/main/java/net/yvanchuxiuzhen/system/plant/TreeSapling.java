package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 树苗来源ID、结构模板、锚点和补写树干规则的逐项对应。 */
enum TreeSapling {
    CASSIA_02("cassia_02", "cassia_07", null, "cassia_04", 3, 3, 3, 3, TemplateRule.FIXED, TrunkRule.VERTICAL),
    LAUREL_02("laurel_02", "laurel_07", "laurel_09", "laurel_05", 3, 3, 3, 3, TemplateRule.LAUREL_BRANCH, TrunkRule.VERTICAL),
    ELM_02("elm_02", "elm_06", null, "elm_04", 3, 3, 3, 3, TemplateRule.FIXED, TrunkRule.VERTICAL),
    PRIVET_02("privet_02", "privet_06", null, "privet_04", 3, 3, 3, 3, TemplateRule.FIXED, TrunkRule.VERTICAL),
    PEACH("peachsapling", "peach_02", "peach_04", "peachlog", 2, 4, 2, 4, TemplateRule.FIRST_40_PERCENT, TrunkRule.VERTICAL),
    SYLVESTRIS("sylvestrissapling", "tree_jiegou14", null, "sylvestrislog", 5, 4, 5, 4, TemplateRule.FIXED, TrunkRule.VERTICAL),
    CHESTNUT("chestnutsapling", "tree_jiegou08", null, "chestnutlog", 5, 5, 5, 5, TemplateRule.FIXED, TrunkRule.CHESTNUT_2X2),
    BEECH("beechsapling", "tree_jiegou19", null, "beechlog", 5, 5, 5, 5, TemplateRule.FIXED, TrunkRule.VERTICAL),
    CYPRESS("cypresssapling", "tree_jiegou05", null, "cypresslog", 3, 3, 3, 3, TemplateRule.FIXED, TrunkRule.VERTICAL),
    TOOTHED("toothedsapling", "tree_jiegou11", null, "toothedlog", 5, 7, 5, 7, TemplateRule.FIXED, TrunkRule.VERTICAL),
    CAMPHOR("camphorsapling", "tree_jiegou06", null, "camphorlog", 2, 3, 2, 3, TemplateRule.FIXED, TrunkRule.VERTICAL),
    NANMU("nanmusapling", "tree_jiegou10", null, "nanmulog", 8, 6, 8, 0, TemplateRule.FIXED, TrunkRule.NANMU_3X7X3),
    GINKGO("ginkgosapling", "tree_jiegou16", null, "ginkgolog", 4, 4, 4, 4, TemplateRule.FIXED, TrunkRule.VERTICAL),
    MAPLE("maplesapling", "maple_02", null, "maplelog", 6, 5, 6, 5, TemplateRule.FIXED, TrunkRule.VERTICAL),
    POPLAR("poplarsapling", "poplar_02", "poplar_04", "poplarlog", 4, 4, 4, 4, TemplateRule.FIRST_40_PERCENT, TrunkRule.VERTICAL),
    WILLOW("willowsapling", "tree_jiegou21", null, "willowlog", 4, 5, 4, 5, TemplateRule.FIXED, TrunkRule.VERTICAL),
    SANDALWOOD("sandalwoodsapling", "sandalwood_tree02", null, "sandalwoodwood", 4, 5, 4, 5, TemplateRule.FIXED, TrunkRule.VERTICAL);

    private final String blockId;
    private final String template;
    private final String alternateTemplate;
    private final String log;
    private final int xOffset;
    private final int yOffset;
    private final int zOffset;
    private final int trunkHeight;
    private final TemplateRule templateRule;
    private final TrunkRule trunkRule;

    TreeSapling(String blockId, String template, String alternateTemplate, String log, int xOffset, int yOffset,
            int zOffset, int trunkHeight, TemplateRule templateRule, TrunkRule trunkRule) {
        this.blockId = blockId;
        this.template = template;
        this.alternateTemplate = alternateTemplate;
        this.log = log;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.trunkHeight = trunkHeight;
        this.templateRule = templateRule;
        this.trunkRule = trunkRule;
    }

    ResourceLocation template() {
        return id(template);
    }

    ResourceLocation alternateTemplate() {
        return id(alternateTemplate);
    }

    Block log() {
        return BuiltInRegistries.BLOCK.get(id(log));
    }

    int xOffset() {
        return xOffset;
    }

    int yOffset() {
        return yOffset;
    }

    int zOffset() {
        return zOffset;
    }

    int trunkHeight() {
        return trunkHeight;
    }

    TemplateRule templateRule() {
        return templateRule;
    }

    TrunkRule trunkRule() {
        return trunkRule;
    }

    static TreeSapling fromBlockId(String blockId) {
        for (TreeSapling sapling : values()) {
            if (sapling.blockId.equals(blockId)) return sapling;
        }
        throw new IllegalArgumentException("未知树苗方块：" + blockId);
    }

    private static ResourceLocation id(String path) {
        return path == null ? null : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    enum TemplateRule {
        FIXED,
        FIRST_40_PERCENT,
        LAUREL_BRANCH
    }

    enum TrunkRule {
        VERTICAL,
        CHESTNUT_2X2,
        NANMU_3X7X3
    }
}
