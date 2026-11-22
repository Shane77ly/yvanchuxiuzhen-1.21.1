package net.yvanchuxiuzhen.system.creature.item;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.creature.SpiritBeastBagRuntime;

/** 来源灵兽袋普通右键释放妖兽；收纳由带袋潜行交互完成。 */
public final class SpiritBeastBagItem extends ContentItem {
    public SpiritBeastBagItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        ItemStack bag = result.getObject();
        String path = BuiltInRegistries.ITEM.getKey(bag.getItem()).getPath();
        if (path.equals("lingshoudai_00")) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag.dye_first"), false);
        } else if (level instanceof ServerLevel serverLevel) {
            SpiritBeastBagRuntime.release(serverLevel, player, bag);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        if (!SpiritBeastBagRuntime.isBag(stack)) return;
        CompoundTag data = SpiritBeastBagRuntime.read(stack);
        lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.usage").withStyle(ChatFormatting.WHITE));
        if (data.getBoolean("yaoshoushifang")) {
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.return").withStyle(ChatFormatting.YELLOW));
        }
        if (!data.getString("yaoshouname").isEmpty()) {
            lines.add(Component.literal(data.getString("yaoshouname")).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.progress",
                    data.getString("LEVELname"), number(data.getDouble("experience")), number(data.getDouble("experience_Max")))
                    .withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.health",
                    number(data.getDouble("health_Num")), number(data.getDouble("health_Max")), number(data.getDouble("health_Rec")))
                    .withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.physical",
                    number(data.getDouble("attack_Num")), number(data.getDouble("attack_Def"))).withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.magic",
                    number(data.getDouble("mana_Att")), number(data.getDouble("mana_Def"))).withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.spirit",
                    number(data.getDouble("soul_Att")), number(data.getDouble("soul_Def"))).withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable("tooltip." + ModId.VALUE + ".spirit_beast_bag.speed",
                    data.getDouble("speed_number")).withStyle(ChatFormatting.WHITE));
        }
    }

    private static String number(double value) {
        return new DecimalFormat("0").format(value);
    }
}
