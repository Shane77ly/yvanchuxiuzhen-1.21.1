package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.system.guchong.client.TaoguanScreen;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;

/** 蛊罐实机检查：覆盖一级罐的开窗、虫物品移入、关闭存档与重开恢复。 */
final class GuchongCheck extends ActionCheck {
    private final Screenshots screenshots;

    GuchongCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("蛊罐", report, levelId);
        this.screenshots = screenshots;
        script(
                new Action(8, this::openJar),
                new Action(20, this::checkMenuAndStore),
                new Action(20, this::closeAndReopen),
                new Action(20, this::checkRestoredJar)
        );
    }

    private void openJar(ServerPlayer player) {
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("taoguan_01item"));
        useJar(player);
    }

    private void checkMenuAndStore(ServerPlayer player) {
        expectTrue("一级蛊罐实际开窗并同步中文标题与虫槽",
                player.containerMenu instanceof TaoguanMenu menu && menu.grade() == 1 && menu.slots.size() == 37
                        && minecraft.screen instanceof TaoguanScreen screen && screen.getTitle().getString().equals("小蛊罐")
                        && minecraft.player.containerMenu.slots.size() == 37);
        minecraft.execute(() -> screenshots.take("guchong-jar-01-chinese.png"));
        TaoguanMenu menu = (TaoguanMenu) player.containerMenu;
        ItemStack insect = item("guchong_03item");
        CompoundTag data = new CompoundTag();
        data.putString("guchongid", "yvanchuxiuzhen:guchong_03");
        data.putDouble("health_Num", 7);
        data.putDouble("health_Max", 10);
        data.putString("guguanuuid", "smoke_individual");
        GuchongJarData.write(insect, data);
        menu.slots.get(1).set(insect);
        menu.quickMoveStack(player, 1);
        expectTrue("蛊物品快捷移动进罐且不复制",
                menu.slots.getFirst().getItem().is(insect.getItem()) && menu.slots.get(1).getItem().isEmpty());
    }

    private void closeAndReopen(ServerPlayer player) {
        player.closeContainer();
        CompoundTag packed = player.getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        expectTrue("关罐将蛊虫身份和当前生命写入物品",
                packed.getString("guchongid0").equals("yvanchuxiuzhen:guchong_03") && packed.getDouble("health_Num0") == 7);
        useJar(player);
    }

    private void checkRestoredJar(ServerPlayer player) {
        ItemStack restored = player.containerMenu.slots.getFirst().getItem();
        CompoundTag data = restored.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        expectTrue("重开蛊罐恢复虫身份与来源生命字段",
                restored.is(item("guchong_03item").getItem()) && data.getDouble("health_Max") == 10
                        && !data.contains("health_Num"));
    }

    private void useJar(ServerPlayer player) {
        player.setShiftKeyDown(true);
        player.getMainHandItem().getItem().use(player.serverLevel(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    private ItemStack item(String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path));
        return new ItemStack(item);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.setGameMode(GameType.CREATIVE);
    }
}
