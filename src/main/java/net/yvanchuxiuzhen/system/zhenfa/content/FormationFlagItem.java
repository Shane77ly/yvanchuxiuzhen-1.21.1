package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 母旗或子旗物品负责放置、同队绑定、灵力查询和阵基控制。 */
public final class FormationFlagItem extends ContentSword {
    private final boolean mother;

    public FormationFlagItem(Tier tier, Properties properties, List<Component> tooltip, boolean mother) {
        super(tier, properties, tooltip, false);
        this.mother = mother;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel) || !(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
        ItemStack held = player.getMainHandItem();
        BlockPos clicked = context.getClickedPos();
        if (context.getClickedFace() == Direction.UP && level.isEmptyBlock(clicked.above())) {
            boolean heldMother = held.getItem() == registeredItem("flagmotheritem");
            boolean heldSon = held.getItem() == registeredItem("flagsonitem");
            if (heldMother || heldSon) {
                String id = heldMother ? "flagmother" : "flagson";
                BlockPos target = clicked.above();
                var block = BuiltInRegistries.BLOCK.get(resource(id));
                level.setBlock(target, block.defaultBlockState().setValue(FormationMarkerBlock.FACING, player.getDirection()), 3);
                if (level.getBlockEntity(target) instanceof FormationMarkerBlockEntity marker) {
                    marker.team(Relations.team(player));
                    IItemHandlerModifiable handler = (IItemHandlerModifiable) marker.handler(null);
                    handler.setStackInSlot(0, held.copyWithCount(1));
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.getInventory().setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        if (!(level.getBlockEntity(clicked) instanceof FormationMarkerBlockEntity marker) || marker.compass()) return InteractionResult.PASS;
        String playerTeam = Relations.team(player);
        if (held.getItem() == registeredItem("flagmotheritem") && FormationBlockIds.path(level.getBlockState(clicked)).equals("flagson")
                && !marker.childBound() && marker.team().equals(playerTeam)) {
            var data = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int count = (int) data.getDouble("ziqishuliang") + 1;
            data.putDouble("ziqishuliang", count);
            data.putDouble("qimubiaoX" + count, clicked.getX());
            data.putDouble("qimubiaoY" + count, clicked.getY());
            data.putDouble("qimubiaoZ" + count, clicked.getZ());
            held.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            marker.childBound(true);
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.flag_bound_son"), false);
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() == registeredItem("flagsonitem")
                && FormationBlockIds.path(level.getBlockState(clicked)).equals("flagmother") && marker.team().equals(playerTeam)) {
            var data = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            data.putDouble("qimubiaoX", clicked.getX());
            data.putDouble("qimubiaoY", clicked.getY());
            data.putDouble("qimubiaoZ", clicked.getZ());
            held.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.flag_bound_mother"), false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.pass(stack);
        if (ModifierKeys.alt(player)) {
            if (mother) {
                var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                int count = (int) data.getDouble("ziqishuliang");
                for (int i = 1; i <= count; i++) {
                    double x = data.getDouble("qimubiaoX" + i);
                    double y = data.getDouble("qimubiaoY" + i);
                    double z = data.getDouble("qimubiaoZ" + i);
                    if (x + y + z != 0) reportPower(server, player, i, BlockPos.containing(x, y, z));
                }
            } else {
                var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                reportPower(server, player, 0, BlockPos.containing(data.getDouble("qimubiaoX"), data.getDouble("qimubiaoY"), data.getDouble("qimubiaoZ")));
            }
        } else if (mother) {
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int count = (int) data.getDouble("ziqishuliang");
            for (int i = 1; i <= count; i++) {
                double x = data.getDouble("qimubiaoX" + i);
                double y = data.getDouble("qimubiaoY" + i);
                double z = data.getDouble("qimubiaoZ" + i);
                if (x + y + z != 0) control(server, player, BlockPos.containing(x, y, z));
            }
        } else {
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            double x = data.getDouble("qimubiaoX");
            double y = data.getDouble("qimubiaoY");
            double z = data.getDouble("qimubiaoZ");
            if (x + y + z != 0) control(server, player, BlockPos.containing(x, y, z));
        }
        return InteractionResultHolder.pass(stack);
    }

    private static void reportPower(ServerLevel level, Player player, int index, BlockPos flagPos) {
        BlockPos base = flagPos.below();
        BlockEntityAccess access = new BlockEntityAccess(level, base);
        int power = access.base() == null ? -1 : access.base().power();
        String key = index == 0 ? "message.yvanchuxiuzhen.flag_power_single" : "message.yvanchuxiuzhen.flag_power_indexed";
        player.displayClientMessage(index == 0 ? Component.translatable(key, power) : Component.translatable(key, index, power), false);
    }

    private static void control(ServerLevel level, Player player, BlockPos flagPos) {
        FormationBaseBlockEntity base = new BlockEntityAccess(level, flagPos.below()).base();
        if (base != null) base.control(player);
    }

    private static Item registeredItem(String path) {
        return BuiltInRegistries.ITEM.get(resource(path));
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private record BlockEntityAccess(ServerLevel level, BlockPos pos) {
        FormationBaseBlockEntity base() {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof FormationBaseBlockEntity base ? base : null;
        }
    }
}
