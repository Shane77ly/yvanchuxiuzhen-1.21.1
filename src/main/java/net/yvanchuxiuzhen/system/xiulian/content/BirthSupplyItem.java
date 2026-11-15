package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.xiulian.api.BirthSupplyEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 传承灵物与家族百宝袋保管自身领取次数，奖励由具体流程交给各内容所有者构造。 */
public final class BirthSupplyItem extends ContentItem {
    private final BirthSupplyEvent.Kind kind;

    public BirthSupplyItem(Properties properties, List<Component> tooltip, BirthSupplyEvent.Kind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = result.getObject();
            if (kind == BirthSupplyEvent.Kind.INHERITANCE) inheritance(serverPlayer, stack);
            else family(serverPlayer, stack);
        }
        return result;
    }

    private void inheritance(ServerPlayer player, ItemStack stack) {
        double count = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("yikaiqi");
        if (count < 30) {
            if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) > count) {
                grant(player, stack, "yikaiqi", count + 1);
            } else {
                player.displayClientMessage(Component.literal("§4当前境界没有能取出的宝物了，请努力修炼！"), false);
            }
        } else {
            player.displayClientMessage(Component.literal("§4传承灵物已空。"), false);
        }
    }

    private void family(ServerPlayer player, ItemStack stack) {
        if (!Cultivation.get(player, CultivationText.BIRTH).equals("世家纨绔")) {
            player.displayClientMessage(Component.literal("§4认证失败，非家族子弟。"), false);
            return;
        }
        player.displayClientMessage(Component.literal("认证成功，是家族子弟。"), false);
        double count = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("geiyvcishu");
        if (count < 20) {
            grant(player, stack, "geiyvcishu", count + 1);
            player.getCooldowns().addCooldown(stack.getItem(), 20);
        } else {
            player.displayClientMessage(Component.literal("§4已全部领完。"), false);
        }
    }

    private void grant(ServerPlayer player, ItemStack stack, String key, double stage) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(key, stage));
        NeoForge.EVENT_BUS.post(new BirthSupplyEvent(player, kind, stage));
    }
}
