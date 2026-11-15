package net.yvanchuxiuzhen.flow;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.book.api.ShelfBooks;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.xiulian.api.BirthInitialRewardsEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;

/** 落魄家族初始材料、传承物和两本书按来源顺序发放，随后执行原传送函数。 */
public final class BirthInitialRewardsFlow {
    private BirthInitialRewardsFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BirthInitialRewardsFlow::grant);
    }

    private static void grant(BirthInitialRewardsEvent event) {
        ServerPlayer player = event.player();
        if (!Cultivation.get(player, CultivationText.BIRTH).equals("落魄家族")) return;
        ItemStack materials = item("cailiao_191");
        Materials.setInstance(materials, 5, 4);
        materials.setCount(20);
        ItemHandlerHelper.giveItemToPlayer(player, materials);
        ItemHandlerHelper.giveItemToPlayer(player, item("ling_18"));
        ItemHandlerHelper.giveItemToPlayer(player, ShelfBooks.technique(1));
        ItemHandlerHelper.giveItemToPlayer(player, ShelfBooks.skill(1));
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO,
                event.sourceLevel(), 4, "", Component.empty(), player.server, null);
        player.server.getCommands().performPrefixedCommand(source, "function yvanchuxiuzhen:playerchuansuo");
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }
}
