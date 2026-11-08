package net.yvanchuxiuzhen.system.skill.content;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 技能选择和功法技能装配的菜单类型，保留来源注册名 jineng_gui 与 player_gui_04。 */
public final class SkillMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<SkillMenu>> SKILL = MENUS.register("jineng_gui", () -> new MenuType<>(SkillMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<SkillLoadoutMenu>> LOADOUT = MENUS.register("player_gui_04",
            () -> IMenuTypeExtension.create((id, inventory, data) -> new SkillLoadoutMenu(id, inventory, data.readBlockPos())));

    private SkillMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new SkillMenu(id, inventory), Component.literal("技能")));
    }

    public static void openLoadout(ServerPlayer player) {
        openLoadout(player, player.blockPosition().above());
    }

    public static void openLoadout(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new SkillLoadoutMenu(id, inventory, pos), Component.literal("PlayerGUI04")), pos);
    }
}
