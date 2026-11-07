package net.yvanchuxiuzhen.system.skill.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** G 键技能界面的菜单类型，保留来源注册名 jineng_gui。 */
public final class SkillMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<SkillMenu>> SKILL = MENUS.register("jineng_gui", () -> new MenuType<>(SkillMenu::new, FeatureFlags.VANILLA_SET));

    private SkillMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new SkillMenu(id, inventory), Component.literal("技能")));
    }
}
