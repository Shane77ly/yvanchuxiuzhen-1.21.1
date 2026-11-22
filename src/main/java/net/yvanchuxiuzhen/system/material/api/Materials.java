package net.yvanchuxiuzhen.system.material.api;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;
import net.yvanchuxiuzhen.system.material.MaterialUnlockCommand;

/** 材料系统供其他系统调用的公开入口。 */
public final class Materials {
    private Materials() {
    }

    public static MaterialDefinition definition(ItemStack stack) {
        return MaterialLibrary.definition(stack);
    }

    public static MaterialInstance instance(ItemStack stack) {
        return MaterialLibrary.instance(stack);
    }

    public static void setInstance(ItemStack stack, int level, int quality) {
        MaterialLibrary.setInstance(stack, level, quality);
    }

    /** 来源筛选只读取已揭示且 open 为假的名称，保留原辨识顺序。 */
    public static List<String> revealedAttributes(Player player, String itemId) {
        return MaterialLibrary.analysis(player).revealed(itemId).stream()
                .filter(entry -> !entry.open()).map(entry -> entry.name()).toList();
    }

    public static List<MaterialRevealedEntry> revealedEntries(Player player, String itemId) {
        return MaterialLibrary.analysis(player).revealed(itemId);
    }

    public static void initializeYihuo(ServerPlayer player) {
        MaterialLibrary.initializeYihuo(player);
    }

    /** 管理解锁写满全部材料辨识，保留来源特殊标记与自然辨识的差别。 */
    public static void unlockAll(ServerPlayer player) {
        MaterialLibrary.unlockAll(player);
    }

    /** 供共享原初系统命令分派材料分支，返回是否已识别此分支。 */
    public static boolean command(CommandSourceStack source, String arguments) {
        return MaterialUnlockCommand.run(source, arguments);
    }
}
