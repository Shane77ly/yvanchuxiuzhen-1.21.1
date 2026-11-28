package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 以本人精血为副手武器或法宝写入主人，保留来源的条件、主手扣费与反馈顺序。 */
public final class JingxveItem extends ContentItem {
    private static final TagKey<Item> WEAPONS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "wuqi"));
    private static final TagKey<Item> INSTRUMENTS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao"));

    public JingxveItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            bind(serverPlayer, result.getObject(), hand);
        }
        return result;
    }

    private static void bind(ServerPlayer player, ItemStack blood, InteractionHand hand) {
        ItemStack instrument = player.getOffhandItem();
        if (!instrument.is(WEAPONS) && !instrument.is(INSTRUMENTS)) {
            message(player, "offhand");
            return;
        }
        if (!(Cultivation.get(player, CultivationValue.LEVEL_NUMBER) >= 11)) {
            message(player, "level");
            return;
        }
        String owner = Equipment.bloodEssence(blood).ownerUuid();
        if (!owner.equals(player.getStringUUID())) {
            message(player, "blood_owner");
            return;
        }
        if (!Equipment.ownerId(instrument).isEmpty()) {
            // 来源在已认主分支重复检查精血主人，因此任意已有主人均使用此提示。
            message(player, "owned");
            return;
        }
        Equipment.bindOwner(instrument, player.getStringUUID());
        message(player, "success");
        // 1.18.2 原生创造使用恢复当前手数量；来源过程只扣主手，恢复范围不含另一只手。
        if (hand != InteractionHand.MAIN_HAND || !player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
    }

    private static void message(ServerPlayer player, String suffix) {
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.jingxve." + suffix), false);
    }
}
