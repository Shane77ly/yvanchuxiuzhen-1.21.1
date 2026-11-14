package net.yvanchuxiuzhen.smoke;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryInventory;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.client.AccessoryScreen;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 单人检查受控能力队名的同步、K键进入03与原生退出消息；不代表建队、加入或多人副本验收。 */
final class TemporaryTeamCheck extends SystemCheck {
    private static final String TEAM = "临时队伍检查";
    private static final String BODY_TEAM = "本体队伍检查";
    private final Screenshots screenshots;
    private String originalTeam;
    private String originalBodyTeam;
    private AccessoryInventory originalAccessories;
    private int originalPage;

    TemporaryTeamCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("临时队伍", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(player -> {
                    player.closeContainer();
                    originalTeam = Relations.temporaryTeam(player);
                    originalBodyTeam = Relations.entityTemporaryTeam(player);
                    originalAccessories = Equipment.accessories(player);
                    originalPage = player.getData(PanelNavigation.PAGE);
                    Relations.setTemporaryTeam(player, TEAM);
                    Relations.setEntityTemporaryTeam(player, BODY_TEAM);
                    player.setData(PanelNavigation.PAGE, 0);
                    CompoundTag saved = player.saveWithoutId(new CompoundTag());
                    String restored = Codec.STRING.parse(NbtOps.INSTANCE,
                            saved.getCompound("neoforge:attachments").get("yvanchuxiuzhen:temporary_team")).getOrThrow();
                    expect("受控能力队名经玩家存档读取保持", TEAM, restored);
                });
            }
            case 1 -> {
                if (local < 10) return false;
                expect("能力队名仅经服务端接口同步到本人", TEAM, Relations.temporaryTeam(minecraft.player));
                key();
            }
            case 2 -> {
                if (local < 10) return false;
                expectTrue("K键自然打开属性页", minecraft.screen instanceof AttributeScreen);
                onServer(player -> expectTrue("属性页服务端菜单身份正确", player.containerMenu instanceof PlayerPanelMenu));
                button("饰品装备").onPress();
            }
            case 3 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("03自然入口与受控能力队名保持", player.containerMenu instanceof AccessoryMenu
                        && player.getData(PanelNavigation.PAGE) == 3 && Relations.temporaryTeam(player).equals(TEAM)));
                expectTrue("03真实客户端菜单已打开", minecraft.screen instanceof AccessoryScreen);
                String leave = Component.translatable("gui.yvanchuxiuzhen.player_gui_03.temporary_team.leave").getString();
                Button button = button(leave);
                expectTrue("来源退出按钮中文及超出500宽坐标保持", leave.equals("退出临时队伍")
                        && button.getX() == (minecraft.screen.width - 500) / 2 + 915
                        && button.getY() == (minecraft.screen.height - 500) / 2 + 63);
                screenshots.take("temporary-team-source-layout.png");
                button.onPress();
            }
            case 4 -> {
                if (local < 10) return false;
                onServer(player -> expectTrue("原生菜单退出只清能力队名并保留本体队名", Relations.temporaryTeam(player).isEmpty()
                        && Relations.entityTemporaryTeam(player).equals(BODY_TEAM) && player.containerMenu instanceof AccessoryMenu));
                expect("退出结果同步到客户端", "", Relations.temporaryTeam(minecraft.player));
                expect("来源无队伍中文可解析", "未加入任何队伍",
                        Component.translatable("gui.yvanchuxiuzhen.player_gui_03.temporary_team.empty").getString());
                expect("来源旧队名退出提示中文可解析", "您已离开队伍“" + TEAM + "”。",
                        Component.translatable("message.yvanchuxiuzhen.temporary_team.left", TEAM).getString());
                screenshots.take("temporary-team-left.png");
                onServer(this::restore);
                finish();
            }
            default -> throw new IllegalStateException("未知临时队伍检查步骤 " + step);
        }
        return true;
    }

    private void key() {
        var key = Arrays.stream(minecraft.options.keyMappings)
                .filter(mapping -> mapping.getName().equals("key.yvanchuxiuzhen.guicaidan")).findFirst().orElseThrow();
        key.setDown(true);
        key.setDown(false);
    }

    private Button button(String name) {
        return minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow();
    }

    private void restore(ServerPlayer player) {
        player.closeContainer();
        Relations.setTemporaryTeam(player, originalTeam);
        Relations.setEntityTemporaryTeam(player, originalBodyTeam);
        player.setData(PanelNavigation.PAGE, originalPage);
        player.setData(EquipmentData.ACCESSORIES, new AccessoryData(originalAccessories));
    }
}
