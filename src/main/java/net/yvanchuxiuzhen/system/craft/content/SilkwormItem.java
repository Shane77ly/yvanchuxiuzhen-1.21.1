package net.yvanchuxiuzhen.system.craft.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 两种蚕道具点击非蚕盒方块的上表面时生成对应工艺实体。 */
public final class SilkwormItem extends ContentItem {
    private final String entity;

    public SilkwormItem(Properties properties, List<Component> tooltip, String entity) {
        super(properties, tooltip);
        this.entity = entity;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == Direction.UP
                && !context.getLevel().getBlockState(pos).is(BuiltInRegistries.BLOCK.get(id("canhe")))) {
            if (context.getLevel() instanceof ServerLevel level) {
                Entity worm = BuiltInRegistries.ENTITY_TYPE.get(id(entity)).create(level);
                worm.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
                worm.setYBodyRot(0);
                worm.setYHeadRot(0);
                if (worm instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(worm.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                }
                level.addFreshEntity(worm);
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
