package net.yvanchuxiuzhen.system.book;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.content.BookBindingMenu;

/** 来源装订台按玩家菜单末刻刷新目录；获得空白载体时才扣纸和线。 */
public final class BookBindingRuntime {
    private static final List<String> CATALOG = List.of("book_001", "book_002", "book_003", "book_004", "book_005", "book_006",
            "book_007", "book_008", "book_009", "book_010", "book_011", "book_012", "book_020", "book_021", "book_023", "book_024",
            "book_025", "book_026", "book_027", "book_028", "book_029", "book_033", "book_034", "book_035");

    private BookBindingRuntime() {
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.containerMenu instanceof BookBindingMenu menu) {
            refresh(menu);
        }
    }

    public static void refresh(BookBindingMenu menu) {
        ItemStack paper = menu.customSlot(0).getItem();
        ItemStack string = menu.customSlot(1).getItem();
        if (paper.is(Items.PAPER) && paper.getCount() >= 3 && string.is(Items.STRING) && string.getCount() >= 1) {
            for (int index = 0; index < CATALOG.size(); index++) {
                menu.customSlot(index + 2).set(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(ModId.VALUE, CATALOG.get(index)))));
            }
        } else {
            clear(menu);
        }
        menu.broadcastChanges();
    }

    public static void clear(BookBindingMenu menu) {
        for (int slot = 2; slot < 38; slot++) menu.customSlot(slot).set(ItemStack.EMPTY);
    }

    /** 来源 onTake 每次移除至多三张纸和一根线，不另加取出时的材料门槛。 */
    public static void take(BookBindingMenu menu) {
        menu.customSlot(0).remove(3);
        menu.customSlot(1).remove(1);
        menu.broadcastChanges();
    }
}
