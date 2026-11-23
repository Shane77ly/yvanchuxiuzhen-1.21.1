package net.yvanchuxiuzhen.system.creature.content;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.network.SpiritBeastBagDyeButtonPayload;

/** 灵兽袋染色菜单注册与服务端按钮通道。 */
public final class SpiritBeastBagDyeMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<SpiritBeastBagDyeMenu>> TYPE = MENUS.register("lingshoudaizhuanhuan",
            () -> IMenuTypeExtension.create(SpiritBeastBagDyeMenu::new));

    private SpiritBeastBagDyeMenus() { }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener(SpiritBeastBagDyeMenus::registerPayloads);
    }

    public static void open(ServerPlayer player, InteractionHand hand) {
        BlockPos position = player.blockPosition();
        player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new SpiritBeastBagDyeMenu(id, inventory, position, hand),
                Component.translatable("menu." + ModId.VALUE + ".spirit_beast_dye")), buffer -> {
                    buffer.writeBlockPos(position);
                    buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
                });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SpiritBeastBagDyeButtonPayload.TYPE, SpiritBeastBagDyeButtonPayload.STREAM_CODEC,
                SpiritBeastBagDyeButtonPayload::handle);
    }

    public static Item toolItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudaizhuanhuanitem"));
    }
}
