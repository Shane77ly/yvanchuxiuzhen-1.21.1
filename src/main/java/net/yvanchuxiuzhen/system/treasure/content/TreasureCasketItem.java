package net.yvanchuxiuzhen.system.treasure.content;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 配置宝匣只在上表面且上方为空时放置，指令随新方块实体保存；提示沿用来源指令字段次序。 */
public final class TreasureCasketItem extends ContentItem {
    public TreasureCasketItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        if (!level.isClientSide() && player != null && context.getClickedFace() == Direction.UP && level.isEmptyBlock(pos)) {
            level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("yvanchuxiuzhen:baoxia")).defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, player.getDirection().getOpposite()), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof TreasureCasketBlockEntity casket) {
                casket.getPersistentData().putString("zhiling", context.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag().getString("zhiling"));
                casket.sync();
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        String command = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("zhiling");
        List<String> fields = Arrays.asList(command.replace(" ", "/").split("/"));
        if (fields.size() < 6 || !fields.getFirst().equals("lianqi")) return;
        lines.add(Component.literal("§6" + fields.get(5)));
        lines.add(Component.literal("§b" + fields.get(4)));
        lines.add(Component.literal("§f" + fields.get(3)));
        String type = fields.get(4);
        if (type.equals("武器") || type.equals("法宝")) {
            String[] labels = {"physical_attack", "spell_attack", "soul_attack", "attack_speed", "movement_speed", "knockback_resistance", "weight"};
            for (int index = 0; index < labels.length; index++) numberLine(lines, fields, 7 + index, 7 + index, labels[index]);
            if (fields.size() >= 15) enchantments(lines, fields, 14, 15);
        } else if (List.of("头盔", "胸甲", "裤子", "战靴").contains(type)) {
            String[] labels = {"health", "power", "soul", "armor", "movement_speed", "knockback_resistance", "physical_defense", "spell_defense", "soul_defense", "weight"};
            for (int index = 0; index < labels.length; index++) numberLine(lines, fields, 7 + index, index == 0 ? 8 : 7 + index, labels[index]);
            if (fields.size() >= 18) enchantments(lines, fields, 17, 18);
        }
    }

    private static void numberLine(List<Component> lines, List<String> fields, int condition, int value, String label) {
        if (condition < fields.size() && value < fields.size() && !fields.get(condition).equals("0")) {
            lines.add(Component.translatable("tooltip.yvanchuxiuzhen.casket." + label, fields.get(value)));
        }
    }

    private static void enchantments(List<Component> lines, List<String> fields, int countIndex, int first) {
        int count = (int) number(fields.get(countIndex));
        for (int index = 0; index < count; index++) {
            int offset = first + index * 3;
            if (offset + 2 >= fields.size()) break;
            Component name = Component.translatable("enchantment." + fields.get(offset).replace("yvanchuxitong", "yvanchuxiuzhen") + "." + fields.get(offset + 1));
            lines.add(Component.translatable("tooltip.yvanchuxiuzhen.casket.enchantment", fields.get(offset + 2), name));
        }
    }

    private static double number(String text) {
        try { return Double.parseDouble(text.trim()); }
        catch (NumberFormatException error) { return 0; }
    }
}
