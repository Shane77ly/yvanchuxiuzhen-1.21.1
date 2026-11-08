package net.yvanchuxiuzhen.system.book.content;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/**
 * 公共书载体的潜行研习入口（来源 {@code BookRightClick}）：按业务实例分派功法与四类技能，成功后清零答题计数。
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
        if (player instanceof ServerPlayer serverPlayer && player.isShiftKeyDown()
                && player.getMainHandItem().getItem() == stack.getItem()) {
            study(serverPlayer, stack);
        }
        return result;
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
