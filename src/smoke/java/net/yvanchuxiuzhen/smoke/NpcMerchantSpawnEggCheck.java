package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.yvanchuxiuzhen.system.npc.entity.NpcAnimated;
import net.yvanchuxiuzhen.system.npc.entity.NpcMob;

/** 固定商人与大管家十一枚刷怪蛋的登记目标、来源颜色及实体构造类型。 */
final class NpcMerchantSpawnEggCheck extends SystemCheck {
    private static final String MOD = "yvanchuxiuzhen";
    private static final List<Definition> EGGS = List.of(
            new Definition("businessman_01", -3368704, -13312, NpcAnimated.class),
            new Definition("businessman_02", -16750900, -205, NpcAnimated.class),
            new Definition("businessman_03", -6724096, -13312, NpcAnimated.class),
            new Definition("businessman_04", -13057, -1, NpcAnimated.class),
            new Definition("businessman_05", -4992426, -3878456, NpcAnimated.class),
            new Definition("businessman_06", -1, -1, NpcAnimated.class),
            new Definition("businessman_07", -6724096, -3167623, NpcAnimated.class),
            new Definition("daguanjia", -3368449, -10079488, NpcAnimated.class),
            new Definition("businessman_09", -1, -1, NpcMob.class),
            new Definition("businessman_10", -1, -1, NpcMob.class),
            new Definition("businessman_11", -1, -1, NpcMob.class));

    NpcMerchantSpawnEggCheck(List<String> report, String levelId) {
        super("固定商人刷怪蛋", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step != 0) {
            throw new IllegalStateException("未知固定商人刷怪蛋检查步骤 " + step);
        }
        if (local < 10) {
            return false;
        }
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        for (Definition definition : EGGS) {
            ResourceLocation entityKey = ResourceLocation.fromNamespaceAndPath(MOD, definition.entityId());
            ResourceLocation itemKey = ResourceLocation.fromNamespaceAndPath(MOD, definition.entityId() + "_spawn_egg");
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            expect("实体注册目标 " + definition.entityId(), entityKey, BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            if (entityType != null) {
                expectTrue("实体构造类型 " + definition.entityId(), definition.entityClass().isInstance(entityType.create(player.serverLevel())));
            }
            expectTrue("刷怪蛋物品已登记 " + definition.entityId(), item instanceof SpawnEggItem);
            if (!(item instanceof SpawnEggItem egg)) {
                continue;
            }
            expect("刷怪蛋底色 " + definition.entityId(), definition.baseColor(), egg.getColor(0));
            expect("刷怪蛋斑色 " + definition.entityId(), definition.spotColor(), egg.getColor(1));
            expect("刷怪蛋目标 " + definition.entityId(), entityKey,
                    BuiltInRegistries.ENTITY_TYPE.getKey(egg.getType(new ItemStack(item))));
        }
    }

    private record Definition(String entityId, int baseColor, int spotColor, Class<? extends Entity> entityClass) {
    }
}
