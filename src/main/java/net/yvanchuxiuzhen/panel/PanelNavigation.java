package net.yvanchuxiuzhen.panel;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.panel.network.OpenPanelPayload;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryPageEvent;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.skill.api.SkillPageEvent;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.DantianReturnEvent;

/** 玩家分页导航只拥有 dangqianGUI；属性汇总只读公开数据，饰品与装配菜单分别归 equipment、skill。 */
public final class PanelNavigation {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<Integer>> PAGE = ATTACHMENTS.register("player_panel_page", () -> AttachmentType
            .builder(() -> 0).serialize(Codec.INT).copyOnDeath().sync((holder, player) -> holder == player, ByteBufCodecs.INT).build());
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<PlayerPanelMenu>> ATTRIBUTES = MENUS.register("player_gui_01",
            () -> new MenuType<>(PlayerPanelMenu::new, FeatureFlags.VANILLA_SET));

    private PanelNavigation() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(OpenPanelPayload::register);
        NeoForge.EVENT_BUS.addListener(PanelNavigation::fromAccessories);
        NeoForge.EVENT_BUS.addListener(PanelNavigation::fromLoadout);
        NeoForge.EVENT_BUS.addListener(PanelNavigation::fromDantian);
    }

    public static void open(ServerPlayer player) {
        switch (player.getData(PAGE)) {
            case 3 -> openAccessories(player);
            case 4 -> openLoadout(player);
            default -> openAttributes(player);
        }
    }

    public static void openAttributes(ServerPlayer player) {
        openAttributes(player, player.blockPosition().above());
    }

    public static void openAttributes(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new PlayerPanelMenu(id, inventory, pos), Component.literal("PlayerGUI01")));
        player.setData(PAGE, 1);
    }

    public static void openAccessories(ServerPlayer player) {
        openAccessories(player, player.blockPosition().above());
    }

    public static void openAccessories(ServerPlayer player, BlockPos pos) {
        Equipment.openAccessories(player, pos);
        player.setData(PAGE, 3);
    }

    private static void fromAccessories(AccessoryPageEvent event) {
        if (event.page() == 4) openLoadout(event.player(), event.pos().above());
        else openAttributes(event.player(), event.pos().above());
    }

    public static void openLoadout(ServerPlayer player) {
        openLoadout(player, player.blockPosition().above());
    }

    public static void openLoadout(ServerPlayer player, BlockPos pos) {
        Skills.openLoadout(player, pos);
        player.setData(PAGE, 4);
    }

    private static void fromLoadout(SkillPageEvent event) {
        if (event.page() == 3) openAccessories(event.player(), event.pos().above());
        else openAttributes(event.player(), event.pos().above());
    }

    private static void fromDantian(DantianReturnEvent event) {
        int page = event.player().getData(PAGE);
        if (page == 1 || page == 3 || page == 4) {
            open(event.player());
            event.setCanceled(true);
        }
    }
}
