package net.yvanchuxiuzhen.system.material.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 熔液按份凝锭，每锭独立决定品阶；本批产物全部给予后才扣原料。 */
public final class MoltenMetalItem extends ContentItem {
    private final ResourceLocation ingot;
    private final int materialLevel;

    public MoltenMetalItem(Properties properties, List<Component> tooltip, int number, int materialLevel) {
        super(properties, tooltip);
        this.ingot = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_" + number);
        this.materialLevel = materialLevel;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack input = result.getObject();
            int count = Math.min(64, input.getCount());
            for (int i = 0; i < count; i++) {
                int roll = serverPlayer.getRandom().nextInt(100) + 1;
                int quality = roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
                ItemStack output = new ItemStack(BuiltInRegistries.ITEM.get(ingot));
                Materials.setInstance(output, materialLevel, quality);
                ItemHandlerHelper.giveItemToPlayer(serverPlayer, output);
            }
            input.shrink(count);
        }
        return result;
    }
}
