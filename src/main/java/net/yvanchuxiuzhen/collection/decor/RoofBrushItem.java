package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTieredItem;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;

/** 建筑笔保存玩家的两点选择及物品瓦片配置，服务端沿来源公式直接铺设屋顶。 */
public final class RoofBrushItem extends ContentTieredItem {
    private static final LegacyTier TIER = new LegacyTier(0, 4, 2, 1, 2, List.of());
    private static final List<String> TILES = List.of("tileimbrexblack", "tileimbrexblue", "tileimbrexgreen", "tileimbrexred", "tileimbrexgilding", "tileimbrexgrey");

    public RoofBrushItem(Properties properties, List<Component> tooltip) {
        super(TIER, properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build()), tooltip, false);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !state.is(BlockTags.NEEDS_DIAMOND_TOOL) && !state.is(BlockTags.NEEDS_IRON_TOOL)
                && (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_HOE)
                || state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability) || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability) || ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability);
    }

    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return 4; }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
        return true;
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, target, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos();
            int point = ModifierKeys.alt(player) ? 2 : 1;
            CompoundTag data = player.getPersistentData();
            data.putInt("Xp" + point, pos.getX());
            data.putInt("Yp" + point, pos.getY());
            data.putInt("Zp" + point, pos.getZ());
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.roof_point_" + point,
                    Double.toString(pos.getX()), Double.toString(pos.getY()), Double.toString(pos.getZ())), false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level.isClientSide()) return result;
        ItemStack stack = result.getObject();
        RoofBrushConfig config = stack.getOrDefault(DecorData.ROOF_BRUSH.get(), RoofBrushConfig.DEFAULT);
        if (ModifierKeys.ctrl(player)) {
            CompoundTag points = player.getPersistentData();
            if (points.getInt("Xp1") == 0 || points.getInt("Xp2") == 0) {
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.roof_missing_" + (points.getInt("Xp1") == 0 ? 1 : 2)), false);
                return result;
            }
            boolean northSouth = player.getDirection().getAxis() == Direction.Axis.Z;
            if ((northSouth && points.getInt("Zp1") < points.getInt("Zp2")) || (!northSouth && points.getInt("Xp1") < points.getInt("Xp2"))) {
                for (String axis : List.of("X", "Y", "Z")) {
                    int first = points.getInt(axis + "p1");
                    points.putInt(axis + "p1", points.getInt(axis + "p2"));
                    points.putInt(axis + "p2", first);
                }
            }
            if (config.tileType().isEmpty()) {
                config = new RoofBrushConfig(config.roofType(), "tileimbrexgrey");
                stack.set(DecorData.ROOF_BRUSH.get(), config);
            }
            String roof = config.roofType();
            boolean curved = roof.equals("悬山") || roof.equals("歇山") || roof.equals("庑殿");
            String tile = roof.equals("歇山") || roof.equals("庑殿") ? "tileimbrexblack" : config.tileType();
            double width = Math.abs((double) (northSouth ? points.getInt("Zp1") : points.getInt("Xp1"))
                    - (northSouth ? points.getInt("Zp2") : points.getInt("Xp2")));
            player.displayClientMessage(Component.translatable(curved ? "message.yvanchuxiuzhen.roof_place"
                    : "message.yvanchuxiuzhen.roof_parity_" + (width / 2 - Math.round(width / 2) == 0 ? "odd" : "even")), false);
            RoofBrushLayout.generate(new RoofBrushLayout.Area(points.getInt("Xp1"), points.getInt("Yp1"), points.getInt("Zp1"),
                    points.getInt("Xp2"), points.getInt("Zp2"), northSouth), curved, tile,
                    (x, y, z, block, facing) -> place(level, BlockPos.containing(x, y, z), block, Direction.from2DDataValue(facing)),
                    value -> points.putDouble("linshi", value));
        } else if (player.isShiftKeyDown()) {
            int next = (TILES.indexOf(config.tileType()) + 1) % TILES.size();
            stack.set(DecorData.ROOF_BRUSH.get(), new RoofBrushConfig(config.roofType(), TILES.get(next)));
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.roof_tile_" + next), false);
        } else {
            boolean hard = config.roofType().equals("悬山");
            stack.set(DecorData.ROOF_BRUSH.get(), new RoofBrushConfig(hard ? "硬山" : "悬山", config.tileType()));
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.roof_" + (hard ? "hard" : "overhanging")), false);
        }
        return result;
    }

    private static void place(Level level, BlockPos pos, String tile, Direction direction) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, tile.toLowerCase(Locale.ENGLISH)));
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = level.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("facing");
        if (property instanceof DirectionProperty facing && facing.getPossibleValues().contains(direction)) {
            level.setBlock(pos, state.setValue(facing, direction), Block.UPDATE_ALL);
        } else {
            property = state.getBlock().getStateDefinition().getProperty("axis");
            if (property instanceof EnumProperty<?> axis && axis.getPossibleValues().contains(direction.getAxis())) {
                level.setBlock(pos, axisState(state, axis, direction.getAxis()), Block.UPDATE_ALL);
            }
        }
    }

    private static <T extends Comparable<T>> BlockState axisState(BlockState state, Property<T> property, Direction.Axis axis) {
        for (T candidate : property.getPossibleValues()) if (candidate.equals(axis)) return state.setValue(property, candidate);
        return state;
    }
}
