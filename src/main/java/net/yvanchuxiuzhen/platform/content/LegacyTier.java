package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * 来源工具的匿名 Tier。1.21.1 的 Tier 没有等级，这里保留 1.18.2 的 {@code getLevel()} 供方块的挖掘判定使用，
 * 并把等级对应到原版“不能正确采掘”的方块标签：0 木、1 石、2 铁、3 钻石、4 及以上下界合金。
 */
public record LegacyTier(int uses, float speed, float damageBonus, int level, int enchantability, List<String> repair) implements Tier {
    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damageBonus;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return switch (Math.min(level, 4)) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ingredient(repair);
    }

    /** 按物品 ID 列表创建修复材料，列表为空表示没有修复材料。 */
    static Ingredient ingredient(List<String> ids) {
        return ids.isEmpty() ? Ingredient.EMPTY
                : Ingredient.of(ids.stream().map(id -> (ItemLike) BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toArray(ItemLike[]::new));
    }

    public static LegacyTier of(ItemRow.LegacyTierRow row) {
        return new LegacyTier(row.uses(), row.speed(), row.damageBonus(), row.level(), row.enchantability(), row.repair().orElse(List.of()));
    }
}
