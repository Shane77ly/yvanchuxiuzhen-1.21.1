package net.yvanchuxiuzhen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import net.yvanchuxiuzhen.collection.decor.client.DecorClientSetup;
import net.yvanchuxiuzhen.collection.festival.client.FestivalClientSetup;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.ModifierKeysClient;
import net.yvanchuxiuzhen.system.book.client.BookClientSetup;
import net.yvanchuxiuzhen.system.combat.client.CombatClientSetup;
import net.yvanchuxiuzhen.system.cooking.client.CookingClientSetup;
import net.yvanchuxiuzhen.system.craft.client.CraftClientSetup;
import net.yvanchuxiuzhen.system.creature.client.CreatureClientSetup;
import net.yvanchuxiuzhen.system.dungeon.client.DungeonClientSetup;
import net.yvanchuxiuzhen.system.economy.client.EconomyClientSetup;
import net.yvanchuxiuzhen.system.fulu.client.FuluClientSetup;
import net.yvanchuxiuzhen.system.fulu.client.InheritanceClientSetup;
import net.yvanchuxiuzhen.system.equipment.client.EquipmentClientSetup;
import net.yvanchuxiuzhen.system.gongfa.client.GongfaClientSetup;
import net.yvanchuxiuzhen.system.guchong.client.GuchongClientSetup;
import net.yvanchuxiuzhen.system.material.client.MaterialClientSetup;
import net.yvanchuxiuzhen.system.npc.client.NpcClientSetup;
import net.yvanchuxiuzhen.system.liandan.client.LiandanClientSetup;
import net.yvanchuxiuzhen.system.lianqi.client.LianqiClientSetup;
import net.yvanchuxiuzhen.panel.client.PanelClientSetup;
import net.yvanchuxiuzhen.system.particle.client.ParticleClientSetup;
import net.yvanchuxiuzhen.system.plant.client.PlantClientSetup;
import net.yvanchuxiuzhen.system.profession.client.ProfessionClientSetup;
import net.yvanchuxiuzhen.system.quest.client.QuestClientSetup;
import net.yvanchuxiuzhen.system.relation.client.RelationClientSetup;
import net.yvanchuxiuzhen.system.skill.client.SkillClientSetup;
import net.yvanchuxiuzhen.system.storage.client.StorageClientSetup;
import net.yvanchuxiuzhen.system.treasure.client.TreasureClientSetup;
import net.yvanchuxiuzhen.system.town.client.TownClientSetup;
import net.yvanchuxiuzhen.system.yihuo.client.YihuoClientSetup;
import net.yvanchuxiuzhen.system.xiulian.client.XiulianClientSetup;
import net.yvanchuxiuzhen.system.world.fluid.client.SpringFluidClientSetup;
import net.yvanchuxiuzhen.system.world.fluid.client.QuicksandFluidClientSetup;
import net.yvanchuxiuzhen.system.world.client.WorldClientSetup;
import net.yvanchuxiuzhen.system.zhenfa.client.ZhenfaClientSetup;
import net.yvanchuxiuzhen.collection.tiandao.client.NbtEditorClientSetup;

/**
 * 客户端入口。FML 只在物理客户端构造本类，并在公共入口 {@link YvanchuxiuzhenMod} 之后构造；
 * 只调用各系统与内容族的 {@code <名称>ClientSetup.register}。
 */
@Mod(value = ModId.VALUE, dist = Dist.CLIENT)
public final class YvanchuxiuzhenClient {
    public YvanchuxiuzhenClient(IEventBus modBus) {
        ModifierKeysClient.register();
        BookClientSetup.register(modBus);
        ParticleClientSetup.register(modBus);
        PlantClientSetup.register(modBus);
        CombatClientSetup.register(modBus);
        CookingClientSetup.register(modBus);
        CraftClientSetup.register(modBus);
        CreatureClientSetup.register(modBus);
        DungeonClientSetup.register(modBus);
        EconomyClientSetup.register(modBus);
        FuluClientSetup.register(modBus);
        InheritanceClientSetup.register(modBus);
        EquipmentClientSetup.register(modBus);
        DecorClientSetup.register(modBus);
        FestivalClientSetup.register(modBus);
        GongfaClientSetup.register(modBus);
        GuchongClientSetup.register(modBus);
        NpcClientSetup.register(modBus);
        QuestClientSetup.register(modBus);
        TownClientSetup.register(modBus);
        PanelClientSetup.register(modBus);
        ProfessionClientSetup.register(modBus);
        RelationClientSetup.register(modBus);
        SkillClientSetup.register(modBus);
        StorageClientSetup.register(modBus);
        TreasureClientSetup.register(modBus);
        MaterialClientSetup.register(modBus);
        LiandanClientSetup.register(modBus);
        LianqiClientSetup.register(modBus);
        YihuoClientSetup.register(modBus);
        XiulianClientSetup.register(modBus);
        SpringFluidClientSetup.register(modBus);
        QuicksandFluidClientSetup.register(modBus);
        WorldClientSetup.register(modBus);
        ZhenfaClientSetup.register(modBus);
        NbtEditorClientSetup.register(modBus);
        NeoForge.EVENT_BUS.addListener(YvanchuxiuzhenClient::onScreenClosing);
        NeoForge.EVENT_BUS.addListener(YvanchuxiuzhenClient::onScreenInitPost);
    }

    private static double lastScreenMouseX;
    private static double lastScreenMouseY;
    private static long lastScreenCloseNanos;

    private static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        lastScreenMouseX = minecraft.mouseHandler.xpos();
        lastScreenMouseY = minecraft.mouseHandler.ypos();
        lastScreenCloseNanos = System.nanoTime();
    }

    private static void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?>)
                || event.getScreen() instanceof InventoryScreen
                || event.getScreen() instanceof CreativeModeInventoryScreen) {
            return;
        }
        if (lastScreenCloseNanos == 0 || System.nanoTime() - lastScreenCloseNanos > 250_000_000L) {
            return;
        }
        lastScreenCloseNanos = 0;
        Minecraft minecraft = Minecraft.getInstance();
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), lastScreenMouseX, lastScreenMouseY);
    }
}
