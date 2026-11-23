package net.yvanchuxiuzhen.system.npc.content;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 门徒任务设置页菜单；按钮在服务端修改方块实体 renwuset。 */
public final class QiankunMenus {
    private static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<QiankunmentuMenu>> DISCIPLE = TYPES.register("qiankunmentu_gui", () -> IMenuTypeExtension.create(QiankunmentuMenu::new));
    private QiankunMenus() { }
    public static void register(IEventBus bus) { TYPES.register(bus); }
    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new QiankunmentuMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.qiankunmentu")), buffer -> buffer.writeBlockPos(pos));
    }
}
