package net.yvanchuxiuzhen.system.book.content;

import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/**
 * 公共书载体的自然使用入口（来源 {@code BookRightClick}）：分派书架藏宝与潜行研习，保留文本、图纸和丹方的优先级。
 */
public final class StudyBookItem extends ContentItem {
    public StudyBookItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        ItemStack stack = result.getObject();
        // 来源比较主手与调用栈的物品类型，副手持相同书载体时也满足入口条件。
        if (!(player instanceof ServerPlayer serverPlayer) || player.getMainHandItem().getItem() != stack.getItem()) {
            return result;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.getString("textdisplay").isEmpty()) {
            if (data.getBoolean("fuluget") && player.isShiftKeyDown()) {
                String command = data.getString("fulumingling").replace("aaa", player.getDisplayName().getString());
                CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO,
                        serverPlayer.serverLevel(), 4, "", Component.empty(), serverPlayer.server, null).withSuppressedOutput();
                serverPlayer.server.getCommands().performPrefixedCommand(source, command);
            }
            return result;
        }
        if (data.getString("TreasureTypeBig").equals("书架")) {
            openTreasure(serverPlayer, stack, data);
        } else if (data.getString("fabaotype").isEmpty() && data.getString("danyaoname").isEmpty() && player.isShiftKeyDown()) {
            study(serverPlayer, stack);
        }
        return result;
    }

    private static void openTreasure(ServerPlayer player, ItemStack stack, CompoundTag data) {
        if (data.getString("TreasureTypeSmall").equals("功法书")) {
            Techniques.giveTreasureBook(player, data.getDouble("TreasureLEVEL"));
            stack.shrink(1);
        } else if (data.getString("TreasureTypeSmall").equals("技能书")) {
            if (data.getDouble("TreasureLEVEL") >= 1) {
                Skills.giveTreasureBook(player);
            }
            stack.shrink(1);
        }
    }

    private static void study(ServerPlayer player, ItemStack stack) {
        Optional<TechniqueBook> technique = Techniques.book(stack).filter(book -> !book.definition().name().isEmpty());
        if (technique.isPresent()) {
            boolean unowned = technique.get().owner().isEmpty();
            Techniques.study(player, stack);
            if (unowned && Techniques.book(stack).orElseThrow().ownedBy(player.getUUID())) {
                player.getPersistentData().putDouble("daduishuliang", 0);
            }
            return;
        }
        Optional<SkillBook> skill = Skills.book(stack).filter(book -> !book.name().isEmpty());
        if (skill.isPresent()) {
            boolean unowned = skill.get().owner().isEmpty();
            Skills.study(player, stack);
            if (unowned && Skills.book(stack).orElseThrow().ownedBy(player.getUUID())) {
                player.getPersistentData().putDouble("daduishuliang", 0);
            }
        }
    }
}
