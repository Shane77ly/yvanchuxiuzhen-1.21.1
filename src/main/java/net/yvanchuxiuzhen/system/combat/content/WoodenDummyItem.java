package net.yvanchuxiuzhen.system.combat.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.CombatSetup;

/**
 * 木人桩物品（来源 {@code MurenzhuangitemItem}）：右键方块的上表面，上方是空气时在其上放置一个木人桩，
 * 朝向与玩家相反，并消耗一个物品。
 */
public final class WoodenDummyItem extends ContentItem {
    public WoodenDummyItem(Item.Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    public static Item item() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "murenzhuangitem"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() != Direction.UP || !context.getLevel().isEmptyBlock(pos.above()) || context.getPlayer() == null) {
            return super.useOn(context);
        }
        if (context.getLevel() instanceof ServerLevel level) {
            WoodenDummyEntity dummy = CombatSetup.<WoodenDummyEntity>entityType("murenzhuang").get().create(level);
            float yaw = context.getPlayer().getYRot() + 180;
            dummy.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, yaw, 0);
            dummy.setYBodyRot(yaw);
            dummy.setYHeadRot(yaw);
            dummy.finalizeSpawn(level, level.getCurrentDifficultyAt(dummy.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(dummy);
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
