package net.yvanchuxiuzhen.collection.decor;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;

/** 兔笼的放置、诱捕、收取与重新放出；动物仍由原生实体保存和更新。 */
final class RabbitCages {
    private static final TagKey<Item> VEGETABLE = TagKey.create(Registries.ITEM, id("vegetable"));
    private static final TagKey<Item> SEED = TagKey.create(Registries.ITEM, id("seed"));

    private RabbitCages() { }

    static void place(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return;
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();
        BlockPos pos = face == Direction.DOWN ? context.getClickedPos().below(2) : context.getClickedPos().relative(face);
        Direction facing = face.getAxis().isHorizontal() ? face.getOpposite() : player.getDirection().getOpposite();
        if (!level.isEmptyBlock(pos)) return;
        level.setBlock(pos, block(false).defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        ItemStack stack = context.getItemInHand();
        CritterCageData cageData = stack.getOrDefault(DecorData.CRITTER_CAGE.get(), CritterCageData.EMPTY);
        Vec3 center = new Vec3(pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5);
        if (stack.is(item(2))) {
            int number = Math.max(1, cageData.rabbitCount());
            for (int index = 0; index < number; index++) {
                spawn(level, new Rabbit(EntityType.RABBIT, level), center);
                for (Entity entity : nearby(level, center, .5)) {
                    CompoundTag saved = new CompoundTag();
                    entity.saveWithoutId(saved);
                    saved.putDouble("RabbitType", cageData.rabbitVariant());
                    entity.load(saved);
                }
            }
        } else if (stack.is(item(3))) {
            int number = Math.max(1, cageData.chickenCount());
            for (int index = 0; index < number; index++) spawn(level, new Chicken(EntityType.CHICKEN, level), center);
        }
        stack.shrink(1);
    }

    private static void spawn(ServerLevel level, Mob animal, Vec3 center) {
        animal.moveTo(center.x, center.y, center.z, level.random.nextFloat() * 360, 0);
        animal.finalizeSpawn(level, level.getCurrentDifficultyAt(animal.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(animal);
    }

    static void interact(ServerLevel level, BlockPos pos, Player player, boolean open) {
        if (open) {
            RabbitCageBlockEntity cage = (RabbitCageBlockEntity) level.getBlockEntity(pos);
            if (player.isShiftKeyDown()) {
                ItemHandlerHelper.giveItemToPlayer(player, emptyCage());
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                ItemStack held = player.getMainHandItem();
                if (!held.is(VEGETABLE) && !held.is(SEED)) return;
                if (!cage.getItem(0).isEmpty()) {
                    ItemEntity dropped = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), cage.getItem(0).copy());
                    dropped.setPickUpDelay(10);
                    level.addFreshEntity(dropped);
                }
                cage.setItem(0, new ItemStack(held.getItem()));
                held.shrink(1);
                cage.sync();
            }
        } else if (player.isShiftKeyDown()) {
            ItemStack result = ItemStack.EMPTY;
            for (Entity entity : nearby(level, Vec3.atCenterOf(pos), .5)) {
                if (entity instanceof Rabbit rabbit) {
                    result = new ItemStack(item(2));
                    result.set(DecorData.CRITTER_CAGE.get(), new CritterCageData(1, rabbit.getVariant().id(), 0));
                    rabbit.discard();
                } else if (entity instanceof Chicken chicken) {
                    result = new ItemStack(item(3));
                    result.set(DecorData.CRITTER_CAGE.get(), new CritterCageData(0, 0, 1));
                    chicken.discard();
                }
            }
            ItemHandlerHelper.giveItemToPlayer(player, result.isEmpty() ? emptyCage() : result);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            level.setBlock(pos, changeState(level.getBlockState(pos), true), 3);
            RabbitCageBlockEntity cage = (RabbitCageBlockEntity) level.getBlockEntity(pos);
            cage.getPersistentData().putBoolean("qiangzhi", true);
            cage.sync();
        }
    }

    static void tick(ServerLevel level, BlockPos pos, RabbitCageBlockEntity cage) {
        ItemStack bait = cage.getItem(0);
        boolean rabbitBait = bait.is(VEGETABLE);
        if (rabbitBait || bait.is(SEED)) {
            for (Entity entity : nearby(level, Vec3.atLowerCornerOf(pos), 8)) {
                if ((rabbitBait && entity instanceof Rabbit || !rabbitBait && entity instanceof Chicken) && entity instanceof Mob mob)
                    mob.getNavigation().moveTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 1.5);
            }
        }
        CompoundTag data = cage.getPersistentData();
        if (data.getBoolean("qiangzhi")) {
            data.putInt("longzitick", data.getInt("longzitick") + 1);
            if (data.getInt("longzitick") >= 5) {
                data.putBoolean("qiangzhi", false);
                data.putInt("longzitick", 0);
            }
            cage.sync();
        }
    }

    static void capture(ServerLevel level, BlockPos pos, Entity entity) {
        RabbitCageBlockEntity cage = (RabbitCageBlockEntity) level.getBlockEntity(pos);
        if (cage.getPersistentData().getBoolean("qiangzhi") || !(entity instanceof Rabbit || entity instanceof Chicken)) return;
        entity.teleportTo(pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5);
        cage.setItem(0, ItemStack.EMPTY);
        level.setBlock(pos, changeState(level.getBlockState(pos), false), 3);
    }

    private static BlockState changeState(BlockState old, boolean open) {
        return block(open).defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, old.getValue(BlockStateProperties.HORIZONTAL_FACING))
                .setValue(BlockStateProperties.WATERLOGGED, old.getValue(BlockStateProperties.WATERLOGGED));
    }

    private static List<Entity> nearby(ServerLevel level, Vec3 center, double radius) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
    static ItemStack emptyCage() { return new ItemStack(item(1)); }
    private static Item item(int number) { return BuiltInRegistries.ITEM.get(id("zhuangshitulongitem_0" + number)); }
    private static Block block(boolean open) { return BuiltInRegistries.BLOCK.get(id("zhuangshi_tulong_0" + (open ? 2 : 1))); }
}
