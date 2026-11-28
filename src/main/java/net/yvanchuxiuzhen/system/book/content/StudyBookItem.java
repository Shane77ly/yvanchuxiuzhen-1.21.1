package net.yvanchuxiuzhen.system.book.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.book.api.BookQuizzes;
import net.yvanchuxiuzhen.system.book.api.BookTextData;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluLearningBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/**
 * 公共书载体的自然使用入口（来源 {@code BookRightClick}）：依次分派文本、藏宝、图纸、丹方及研习或阅读。
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
        BookTextData text = BookTexts.get(stack);
        FuluLearningBook learning = Fulu.learningBook(stack);
        if (!text.textDisplay().isEmpty()) {
            if (learning.enabled() && player.isShiftKeyDown()) {
                String command = learning.command().replace("aaa", player.getDisplayName().getString());
                CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO,
                        serverPlayer.serverLevel(), 4, "", Component.empty(), serverPlayer.server, null).withSuppressedOutput();
                serverPlayer.server.getCommands().performPrefixedCommand(source, command);
            } else {
                splitText(stack, text);
                serverPlayer.inventoryMenu.broadcastChanges();
                BookMenus.open(serverPlayer);
            }
            return result;
        }
        TreasureContainerConfig treasure = TreasureContainers.config(stack);
        if (treasure.category().equals("书架")) {
            openTreasure(serverPlayer, stack, treasure);
        } else if (Lianqi.blueprint(stack).isPresent() || Liandan.recipe(stack).isPresent()) {
            BookMenus.open(serverPlayer);
        } else if (player.isShiftKeyDown()) {
            study(serverPlayer, stack);
        } else {
            BookMenus.open(serverPlayer);
        }
        return result;
    }

    /** 来源以 Java 字符串的每二十二个 UTF-16 单元切分全部行，不足十一行补空。 */
    private static void splitText(ItemStack stack, BookTextData text) {
        String content = text.textDisplay();
        List<String> lines = new ArrayList<>(Math.max(11, (content.length() + 21) / 22));
        for (int offset = 0; offset < content.length(); offset += 22) {
            lines.add(content.substring(offset, Math.min(offset + 22, content.length())));
        }
        while (lines.size() < 11) lines.add("");
        for (int index = lines.size(); index < text.lines().size(); index++) {
            lines.add(text.lines().get(index));
        }
        BookTexts.set(stack, text.withLines(lines));
    }

    private static void openTreasure(ServerPlayer player, ItemStack stack, TreasureContainerConfig treasure) {
        if (treasure.subtype().equals("功法书")) {
            Techniques.giveTreasureBook(player, treasure.level());
            if (!player.getAbilities().instabuild) stack.shrink(1);
        } else if (treasure.subtype().equals("技能书")) {
            if (treasure.level() >= 1) {
                Skills.giveTreasureBook(player);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
    }

    private static void study(ServerPlayer player, ItemStack stack) {
        Optional<TechniqueBook> technique = Techniques.book(stack).filter(book -> !book.definition().name().isEmpty());
        if (technique.isPresent()) {
            boolean unowned = technique.get().owner().isEmpty();
            Techniques.study(player, stack);
            if (unowned && Techniques.book(stack).orElseThrow().ownedBy(player.getUUID())) {
                BookQuizzes.setAnswerCount(player, 0);
            }
            return;
        }
        Optional<SkillBook> skill = Skills.book(stack).filter(book -> !book.name().isEmpty());
        if (skill.isPresent()) {
            boolean unowned = skill.get().owner().isEmpty();
            Skills.study(player, stack);
            if (unowned && Skills.book(stack).orElseThrow().ownedBy(player.getUUID())) {
                BookQuizzes.setAnswerCount(player, 0);
            }
        }
    }
}
