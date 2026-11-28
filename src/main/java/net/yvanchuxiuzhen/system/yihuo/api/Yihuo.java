package net.yvanchuxiuzhen.system.yihuo.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectile;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectiles;
import net.yvanchuxiuzhen.system.yihuo.data.YihuoData;

/** skill 丹火物品的火块操作入口，服务端保存燃料与施放者。 */
public final class Yihuo {
    private static final TagKey<Block> FIRE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire"));

    private Yihuo() {
    }

    public static DanFireData danFire(ItemStack stack) {
        return stack.getOrDefault(YihuoData.DAN_FIRE.get(), DanFireData.EMPTY);
    }

    public static void markBurnTarget(Entity target, String team, String attackerUuid) {
        target.getData(YihuoData.BURN_STATE).setBurnTarget(team, attackerUuid);
    }

    public static String burnAttacker(Entity target) {
        return target.getData(YihuoData.BURN_STATE).fireAttackUuid();
    }

    public static void setYangTick(Entity target, double value) {
        target.getData(YihuoData.BURN_STATE).setYangTick(value);
    }

    public static void setYinTick(Entity target, double value) {
        target.getData(YihuoData.BURN_STATE).setYinTick(value);
    }

    public static double firePower(BlockEntity block) {
        if (block instanceof FireBlockEntity fire) return fire.power();
        return block == null ? Double.NaN : block.getData(YihuoData.BLOCK_FIRE_POWER);
    }

    public static void addPower(BlockEntity block, double amount) {
        if (block instanceof FireBlockEntity fire) {
            fire.power(fire.power() + amount);
        } else if (block != null) {
            block.setData(YihuoData.BLOCK_FIRE_POWER, block.getData(YihuoData.BLOCK_FIRE_POWER) + amount);
            block.setChanged();
            if (block.getLevel() != null) {
                block.getLevel().sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static ItemStack createDanFire(String ownerUuid, double power) {
        ItemStack fire = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danhuo")));
        fire.set(YihuoData.DAN_FIRE.get(), new DanFireData(ownerUuid, power));
        return fire;
    }

    public static InteractionResult useDanFire(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;
        BlockPos clicked = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        DanFireData fire = danFire(stack);
        if (level.getBlockState(clicked).is(FIRE)) {
            flame(level, clicked, 20);
            addPower(level.getBlockEntity(clicked), fire.power());
            stack.shrink(1);
        } else if (context.getClickedFace() == Direction.UP && level.isEmptyBlock(clicked.above())) {
            BlockPos pos = clicked.above();
            if (fire.ownerUuid().equals(player.getStringUUID())) {
                flame(level, pos, 20);
                level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_221fireblock")).defaultBlockState(), Block.UPDATE_ALL);
                ((FireBlockEntity) level.getBlockEntity(pos)).initialize(player, fire.power());
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

    /** 普通火始终可收；炼器等级 1、6、…、41 依次解锁二至十档。 */
    public static int fabaoAbsorptionGrade(int itemLevel) {
        return itemLevel < 1 ? 1 : Math.min(10, (itemLevel - 1) / 5 + 2);
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

    /** 覆火葫芦从储火发射穿透二级的异火弹体；手持材料直射不经过此入口。 */
    public static YihuoProjectile shootMaterial(ServerPlayer player, int material, float velocity, float spread) {
        ServerLevel level = player.serverLevel();
        YihuoProjectile projectile = YihuoProjectiles.type(material).get().create(level);
        projectile.setGourdPiercing();
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
