package net.yvanchuxiuzhen.system.gongfa.api;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.TechniqueRules;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;

/**
 * 功法系统的公开操作。功法书实例（{@link TechniqueBook}）在书的物品组件上，功法栏是玩家附件；
 * 其他系统读功法名、五行值、层数、品阶与研习者经 {@link #main} 与 {@link #book}，放入与取出功法栏经 {@link #setSlot}。
 */
public final class Techniques {
    private Techniques() {
    }

    /** 物品上的功法实例；不是功法书时为空。 */
    public static Optional<TechniqueBook> book(ItemStack stack) {
        return Optional.ofNullable(stack.get(GongfaData.TECHNIQUE.get()));
    }

    /** 功法栏中的书（来源 {@code gongfa} 字段）。 */
    public static ItemStack slot(Player player) {
        return player.getData(GongfaData.SLOT);
    }

    /** 功法栏中的功法实例；栏空或不是功法书时为空。 */
    public static Optional<TechniqueBook> main(Player player) {
        return book(slot(player));
    }

    /** 放入或取出功法栏（面板的功法页、符箓与流程的重置），随后重新提交主修功法信息与属性贡献。 */
    public static void setSlot(ServerPlayer player, ItemStack stack) {
        TechniqueRules.setSlot(player, stack);
    }

    /** 研习手中的功法书（书籍右键的研习分支）；条件不满足时只提示。 */
    public static void study(ServerPlayer player, ItemStack stack) {
        TechniqueRules.study(player, stack);
    }

    /** 由定义生成一本未研习的功法书（管理命令 {@code /technique} 与给书的玩法）。 */
    public static ItemStack createBook(TechniqueDefinition definition) {
        return TechniqueLibrary.book(definition);
    }

    /** 书架藏宝里的功法书（来源 {@code BookGiveGongfa}）：按藏宝等级取表随机给一本，没有书时不给。 */
    public static void giveTreasureBook(ServerPlayer player, double treasureLevel) {
        ItemStack book = TechniqueLibrary.roll(treasureLevel, player.getRandom());
        if (!book.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, book);
        }
    }
}
