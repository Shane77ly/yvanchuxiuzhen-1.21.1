package net.yvanchuxiuzhen.system.craft;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockEntity;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.SilkReelRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftTableRecipes;

/** 缫丝轮启动后沿既有二十刻周期运行五次，按启动朝向从前方低一格取蛹并依据 JSON 配方产丝。 */
public final class SilkReelRuntime {
    private static final TagKey<Block> FIRE = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire"));

    private SilkReelRuntime() {
    }

    public static void use(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof SilkReelBlockEntity reel)) return;
        if (reel.reeling()) return;
        reel.animate(1);
        if (level.isClientSide) return;
        reel.setReeling(true);
        reel.setDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getName());
        reel.sync();
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SilkReelBlockEntity reel)) return;
        if (!reel.reeling()) return;
        double tick = reel.tick() + 1;
        reel.setTick(tick);
        reel.sync();
        if (!(tick >= 5)) return;
        BlockPos input = switch (reel.direction()) {
            case "north" -> pos.north().below();
            case "south" -> pos.south().below();
            case "west" -> pos.west().below();
            case "east" -> pos.east().below();
            default -> null;
        };
        if (input != null) process(level, pos, input);
        reel.setReeling(false);
        reel.setTick(0);
        reel.sync();
    }

    private static void process(ServerLevel level, BlockPos pos, BlockPos input) {
        for (SilkReelRecipe recipe : CraftTableRecipes.silkReel()) {
            IItemHandler inventory = inventory(level, input);
            ItemStack stack = inventory == null ? ItemStack.EMPTY : inventory.getStackInSlot(0).copy();
            if (!stack.is(item(recipe.inputId()))) continue;
            spawn(level, pos, recipe.outputId());
            IItemHandler current = inventory(level, input);
            if (current instanceof IItemHandlerModifiable modifiable) {
                ItemStack remaining = current.getStackInSlot(0).copy();
                remaining.shrink(1);
                modifiable.setStackInSlot(0, remaining);
            }
            return;
        }
        if (!level.getBlockState(input).is(Blocks.WATER_CAULDRON) || !level.getBlockState(input.below()).is(FIRE)) return;
        Vec3 center = Vec3.atCenterOf(input);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(0.5), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        for (Entity entity : entities) {
            if (!(entity instanceof ItemEntity drop)) continue;
            for (SilkReelRecipe recipe : CraftTableRecipes.silkReel()) {
                if (!drop.getItem().is(item(recipe.inputId()))) continue;
                spawn(level, pos, recipe.outputId());
                drop.discard();
                return;
            }
        }
    }

    /** 来源只查询邻居方块实体的无方向能力，普通容器路径不要求热源。 */
    private static IItemHandler inventory(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) == null ? null : level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    private static void spawn(ServerLevel level, BlockPos pos, String outputId) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                new ItemStack(item(outputId)));
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }
}
