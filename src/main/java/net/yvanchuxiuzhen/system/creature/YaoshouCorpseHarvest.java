package net.yvanchuxiuzhen.system.creature;

import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 妖兽尸体普通右键采集；使用独立采集表，不更改实体的死亡掉落。 */
final class YaoshouCorpseHarvest {
    private static final LootContextParamSet HARVEST_CONTEXT = LootContextParamSet.builder()
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.DAMAGE_SOURCE)
            .required(LootContextParams.TOOL)
            .build();
    private static final TagKey<Item> CORE = TagKey.create(Registries.ITEM, id("yaodan"));
    private static final TagKey<Item> MATERIAL = TagKey.create(Registries.ITEM, id("cailiao"));

    private YaoshouCorpseHarvest() { }

    static void harvest(ServerLevel level, Entity corpse) {
        CreatureState state = CreatureData.state(corpse);
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(corpse.getType()).toString();
        String tableName = type.replace(ModId.VALUE + ":", "");
        ResourceKey<LootTable> tableId = ResourceKey.create(Registries.LOOT_TABLE, id("entities/" + tableName + "list"));
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableId);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, corpse.position())
                .withParameter(LootContextParams.THIS_ENTITY, corpse)
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().magic())
                .withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance())
                .create(HARVEST_CONTEXT);
        ItemStack reward = draw(level, table, params);
        int rank = (int) EntityAttributes.level(corpse);
        if (reward.is(CORE) && !state.beastCoreDropped()) {
            int tier = Mth.clamp((rank - 1) / 5, 0, 8);
            reward = new ItemStack(BuiltInRegistries.ITEM.get(id("cailiao_" + (181 + tier))));
            Materials.setInstance(reward, rank, quality());
            state.setBeastCoreDropped(true);
        } else {
            // 同一尸体已出妖丹后，命中妖丹项时按来源重新抽整表。
            while (reward.is(CORE)) reward = draw(level, table, params);
            if (reward.is(MATERIAL)) Materials.setInstance(reward, rank, quality());
        }
        ItemEntity drop = new ItemEntity(level, corpse.getX(), corpse.blockPosition().getY() + 1, corpse.getZ(), reward);
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
        double count = state.harvestCount() + 1;
        state.setHarvestCount(count);
        if (count >= 5) corpse.discard();
    }

    private static ItemStack draw(ServerLevel level, LootTable table, LootParams params) {
        List<ItemStack> rolls = table.getRandomItems(params);
        return rolls.get(level.getRandom().nextInt(rolls.size()));
    }

    private static int quality() {
        int roll = new Random().nextInt(100) + 1;
        return roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
