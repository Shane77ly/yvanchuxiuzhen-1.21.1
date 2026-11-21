package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.panel.network.OpenPanelPayload;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;
import net.yvanchuxiuzhen.system.xiulian.api.TribulationLightningEvent;
import net.yvanchuxiuzhen.system.xiulian.content.DantianMenu;
import net.yvanchuxiuzhen.system.xiulian.tribulation.TribulationCloud;

/** 专用新档：控制突破前置及雷型，菜单、自然发劫、结算与存档重开均走生产入口。 */
final class DantianTribulationLifecycleCheck extends SystemCheck {
    private UUID playerId;
    private double speedBefore;
    private int strikes;
    private Consumer<TribulationLightningEvent> observer;

    DantianTribulationLifecycleCheck(List<String> report, String levelId) {
        super("丹田过劫重进", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(p -> {
                    playerId = p.getUUID();
                    p.setGameMode(GameType.SURVIVAL);
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 20);
                    Cultivation.set(p, CultivationValue.ROOT_FIRE, 1);
                    Cultivation.set(p, CultivationValue.EXPERIENCE, 0);
                    Cultivation.set(p, CultivationValue.EXPERIENCE_MAX, 1000);
                    Cultivation.setMainTechnique(p, new MainTechnique(true, true, "渡劫样本", "水火", 31, 25));
                    PlayerAttributes.setContribution(p, "smoke:realm_lifecycle", new Contribution(Layer.BONUS, Map.of(Stat.HEALTH_MAX, 1300.0)));
                    p.setHealth(p.getMaxHealth());
                    ItemStack pill = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_181")));
                    Materials.setInstance(pill, 31, 1);
                    p.getInventory().setItem(9, pill);
                    expectTrue("初始本命槽为空且没有旧金丹", Cultivation.benming(p).isEmpty() && !opened(p, 109));
                });
                openPanel();
            }
            case 1, 9 -> {
                if (!(minecraft.player.containerMenu instanceof PlayerPanelMenu panel)) return waiting(local, "属性面板");
                minecraft.gameMode.handleInventoryButtonClick(panel.containerId, 6);
            }
            case 2 -> {
                if (!(minecraft.player.containerMenu instanceof DantianMenu menu) || !sample(menu.getSlot(1).getItem())) return waiting(local, "丹田背包中的样本");
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, 1, 0, ClickType.QUICK_MOVE, minecraft.player);
            }
            case 3 -> {
                boolean[] placed = {false};
                onServer(p -> placed[0] = p.containerMenu instanceof DantianMenu menu && sample(menu.getSlot(0).getItem())
                        && p.getInventory().getItem(9).isEmpty() && Cultivation.benming(p).isEmpty());
                if (!placed[0]) return waiting(local, "服务端槽0收到物品、关闭前未写附件");
                minecraft.player.closeContainer();
            }
            case 4 -> {
                if (!sample(Cultivation.benming(minecraft.player))) return waiting(local, "关窗写回本命槽并同步");
                onServer(p -> {
                    expectTrue("丹田关闭写回31级妖丹", sample(Cultivation.benming(p)));
                    arm(p, 20);
                });
            }
            case 5, 11 -> {
                boolean[] found = {false};
                onServer(p -> {
                    for (TribulationCloud cloud : p.serverLevel().getEntitiesOfClass(TribulationCloud.class, p.getBoundingBox().inflate(100))) {
                        var data = cloud.getPersistentData();
                        if (!p.getStringUUID().equals(data.getString("playerUUID"))) continue;
                        expectTrue("满修为自然发劫且尚未晋级", Cultivation.thunderOpen(p) && Cultivation.get(p, CultivationValue.THUNDER_MAX) == 1
                                && Cultivation.get(p, CultivationValue.LEVEL_NUMBER) == (step == 5 ? 20 : 30) && !data.getString("thunder1").isEmpty());
                        data.putString("thunder1", "雷1");
                        strikes = 0;
                        observer = event -> { if (event.level() == p.serverLevel()) strikes++; };
                        NeoForge.EVENT_BUS.addListener(observer);
                        found[0] = true;
                        break;
                    }
                });
                if (!found[0]) return waiting(local, "本人自然生成的劫云");
            }
            case 6, 12 -> {
                if (Cultivation.get(minecraft.player, CultivationValue.LEVEL_NUMBER) != (step == 6 ? 21 : 31)) {
                    return waiting(local, "劫云自然结算后的本人层数同步");
                }
                onServer(p -> {
                    expect("雷劫结束后的层数", step == 6 ? 21 : 31, Cultivation.get(p, CultivationValue.LEVEL_NUMBER));
                    expect("单组瞬雷发出九次真实落雷请求", 9, strikes);
                    stopObserving();
                    expectTrue("劫云收尾与每秒晋级均已消费", !Cultivation.thunderOpen(p) && !Cultivation.tizhiBoolean(p, "jinji")
                            && Cultivation.get(p, CultivationValue.THUNDER_MAX) == 0);
                    expectTrue("妖丹金丹109且本命已消耗", opened(p, 109) && Cultivation.benming(p).isEmpty());
                    if (step == 12) {
                        expectTrue("新版默认属性封顶，面板生命不能代替实际生命", PlayerAttributes.value(p, Stat.HEALTH_MAX) >= 1200 && p.getMaxHealth() == 1024);
                        expectTrue("保留旧金丹，实际生命不足只结普通元婴", opened(p, 201) && !opened(p, 203));
                        expect("普通元婴本命培养速度增加1", speedBefore + 1, Cultivation.tizhiDouble(p, "WeaponCultivateSpeed"));
                    }
                });
                if (minecraft.player.containerMenu != minecraft.player.inventoryMenu) minecraft.player.closeContainer();
                if (step == 6) leaveWorld(); else finish();
            }
            case 7 -> { return reopened(local); }
            case 8 -> {
                if (!inWorld(local)) return false;
                onServer(p -> {
                    expect("重进同一玩家", playerId, p.getUUID());
                    expect("重进结丹层数", 21, Cultivation.get(p, CultivationValue.LEVEL_NUMBER));
                    expectTrue("服务端重载旧金丹且本命槽为空", opened(p, 109) && Cultivation.benming(p).isEmpty());
                });
                expectTrue("重进本人同步金丹与空槽", opened(minecraft.player, 109) && Cultivation.benming(minecraft.player).isEmpty());
                openPanel();
            }
            case 10 -> {
                if (!(minecraft.player.containerMenu instanceof DantianMenu menu)) return waiting(local, "重进丹田页");
                if (!"tizhiopen_109".equals(Cultivation.tizhiString(minecraft.player, "jindanname"))) return waiting(local, "丹田页读回金丹名109");
                expectTrue("重开没有返还或重建妖丹", menu.getSlot(0).getItem().isEmpty() && minecraft.player.getInventory().items.stream().noneMatch(DantianTribulationLifecycleCheck::sample));
                minecraft.player.closeContainer();
                onServer(p -> {
                    expectTrue("主修门槛重进后仍满足", Cultivation.mainTechnique(p).owned() && Cultivation.mainTechnique(p).level() == 25
                            && Cultivation.mainTechnique(p).grade() == 31 && Cultivation.mainTechnique(p).element().equals("水火"));
                    speedBefore = Cultivation.tizhiDouble(p, "WeaponCultivateSpeed");
                    arm(p, 30);
                });
            }
            default -> throw new IllegalStateException("丹田过劫检查步骤 " + step);
        }
        return true;
    }

    private static boolean sample(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_181")))
                && Materials.instance(stack).level() == 31;
    }

    private static boolean opened(Player player, int id) { return Cultivation.tizhiBoolean(player, "tizhiopen_" + id); }

    private static void openPanel() { PacketDistributor.sendToServer(new OpenPanelPayload()); }

    private static void arm(ServerPlayer p, int level) {
        Cultivation.set(p, CultivationValue.LEVEL_NUMBER, level);
        Cultivation.setTizhiDouble(p, "ThunderSufferingNumber", level == 20 ? -12 : -24);
        Cultivation.setTizhiDouble(p, "ThunderSufferingAttack", level == 20 ? -29 : -59);
        Cultivation.set(p, CultivationValue.EXPERIENCE, Cultivation.get(p, CultivationValue.EXPERIENCE_MAX));
    }

    private boolean waiting(int local, String stage) {
        if (local > 800) {
            if (observer != null) onServer(p -> stopObserving());
            fail("等待超时：" + stage);
            finish();
        }
        return false;
    }

    private void stopObserving() {
        NeoForge.EVENT_BUS.unregister(observer);
        observer = null;
    }
}
