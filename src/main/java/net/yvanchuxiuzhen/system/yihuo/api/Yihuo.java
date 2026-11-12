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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectile;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectiles;

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

    /** 覆火葫芦按来源的八格立方扫描消费火块；返回实际吸收的火焰等级，未满足品阶时保留方块。 */
    public static int absorbFire(ServerLevel level, BlockPos pos, int maximumGrade) {
        int grade = fireGrade(level.getBlockState(pos));
        if (grade == 0 || grade > maximumGrade) {
            return 0;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return grade;
    }

    /** 1.18.2 版本的法宝品阶门槛：品阶 1 无门槛，之后每五级解锁一档。 */
    public static int fabaoAbsorptionGrade(int itemLevel) {
        return itemLevel < 1 ? 0 : Math.min(10, (itemLevel - 1) / 5 + 1);
    }

    /** 从异火材料编号得到火块等级；普通火与灵魂火均为一级。 */
    public static int fireGrade(BlockState state) {
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return 1;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getNamespace().equals(ModId.VALUE) ? id.getPath() : "";
        if (!path.startsWith("cailiao_") || !path.endsWith("fireblock")) {
            return 0;
        }
        String number = path.substring("cailiao_".length(), path.length() - "fireblock".length());
        if (number.length() != 3 || !(number.startsWith("22") || number.startsWith("23"))) {
            return 0;
        }
        int material = Integer.parseInt(number);
        if (!YihuoProjectiles.ids().contains(material)) {
            return 0;
        }
        return material % 10 + 1;
    }

    /** 从指定编号发射异火材料弹体；弹体的命中与火块写入仍由 yihuo 所有。 */
    public static YihuoProjectile shootMaterial(ServerPlayer player, int material, float velocity, float spread) {
        ServerLevel level = player.serverLevel();
        YihuoProjectile projectile = YihuoProjectiles.type(material).get().create(level);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        projectile.shoot(player.getViewVector(1).x, player.getViewVector(1).y, player.getViewVector(1).z, velocity, spread);
        level.addFreshEntity(projectile);
        return projectile;
    }

    private static void flame(ServerLevel level, BlockPos pos, int count) {
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + .5, pos.getY(), pos.getZ() + .5, count, .5, .5, .5, 0);
    }
}
