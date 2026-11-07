package net.yvanchuxiuzhen.system.yihuo.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;

/** skill 丹火物品的火块操作入口，服务端保存燃料与施放者。 */
public final class Yihuo {
    private static final TagKey<Block> FIRE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire"));

    private Yihuo() {
    }

    public static InteractionResult useDanFire(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;
        BlockPos clicked = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        CompoundTag item = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (level.getBlockState(clicked).is(FIRE)) {
            flame(level, clicked, 20);
            BlockEntity block = level.getBlockEntity(clicked);
            if (block != null) {
                CompoundTag data = block.getPersistentData();
                data.putDouble("power", data.getDouble("power") + item.getDouble("power"));
                block.setChanged();
                level.sendBlockUpdated(clicked, block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
            }
            stack.shrink(1);
        } else if (context.getClickedFace() == Direction.UP && level.isEmptyBlock(clicked.above())) {
            BlockPos pos = clicked.above();
            if (item.getString("ManaAttackUUID").equals(player.getStringUUID())) {
                flame(level, pos, 20);
                level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_221fireblock")).defaultBlockState(), Block.UPDATE_ALL);
                ((FireBlockEntity) level.getBlockEntity(pos)).initialize(player, item.getDouble("power"));
                stack.shrink(1);
            } else {
                flame(level, pos, 4);
                level.sendParticles(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY(), pos.getZ() + .5, 12, .5, .5, .5, 0);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void flame(ServerLevel level, BlockPos pos, int count) {
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + .5, pos.getY(), pos.getZ() + .5, count, .5, .5, .5, 0);
    }
}
