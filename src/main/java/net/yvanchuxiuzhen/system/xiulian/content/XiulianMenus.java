package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 修炼系统的出身、升级、丹田、属性加点与闭关菜单，保留来源注册名。 */
public final class XiulianMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BirthMenu>> BIRTH = MENUS.register("player_join_gui",
            () -> new MenuType<>(BirthMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<LevelUpMenu>> LEVEL_UP = MENUS.register("playershengjixianshi",
            () -> new MenuType<>(LevelUpMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<DantianMenu>> DANTIAN = MENUS.register("dantian_gui",
            () -> new MenuType<>(DantianMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<SkillPointMenu>> SKILL_POINTS = MENUS.register("player_gui_02",
            () -> IMenuTypeExtension.create((id, inventory, data) -> new SkillPointMenu(id, inventory, data.readBlockPos())));
    public static final Supplier<MenuType<RetreatMenu>> RETREAT = MENUS.register("player_gui_06",
            () -> new MenuType<>((id, inventory) -> new RetreatMenu(id, inventory, 6), FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<RetreatMenu>> MEDITATION = MENUS.register("player_gui_07",
            () -> new MenuType<>((id, inventory) -> new RetreatMenu(id, inventory, 7), FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<RetreatMenu>> STUDY = MENUS.register("player_gui_08",
            () -> new MenuType<>((id, inventory) -> new RetreatMenu(id, inventory, 8), FeatureFlags.VANILLA_SET));

    private XiulianMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
