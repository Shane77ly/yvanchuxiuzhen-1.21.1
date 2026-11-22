package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;

/** 建筑锤保留来源剑的工具参数，方块升降不消耗耐久。 */
public final class BuildingHammerItem extends ContentSword {
    private static final LegacyTier TIER = new LegacyTier(100, 4, 2, 1, 2, List.of());

    public BuildingHammerItem(Properties properties, List<Component> tooltip) {
        super(TIER, properties.attributes(createAttributes(TIER, 3, -3)), tooltip, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        if (!context.getLevel().isClientSide()) BuildingHammer.raise(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }
}
