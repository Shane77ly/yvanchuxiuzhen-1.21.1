package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.material.client.MaterialAnalysisScreen;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisButtonPayload;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;
import net.yvanchuxiuzhen.system.storage.client.BagScreen;
import net.yvanchuxiuzhen.system.storage.client.MaterialBagScreen;
import net.yvanchuxiuzhen.system.storage.content.BagKind;
import net.yvanchuxiuzhen.system.storage.content.BagMenu;
import net.yvanchuxiuzhen.system.storage.content.MaterialBagMenu;
import net.yvanchuxiuzhen.system.storage.data.StorageData;

/** 乾坤袋资产路径：四档真实网络槽位操作与退出重进，核对数量和材料实例组件没有丢失或回滚。 */
final class StorageCheck extends SystemCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Consumer<String> screenshot;
    private ItemStack sample;
    private int selected;
    private MaterialWarehouse originalWarehouse;
    private MaterialWarehouse savedWarehouse;
    private MaterialAnalysis originalAnalysis;
    private final Map<Profession, ProfessionProgress> originalProfessions = new EnumMap<>(Profession.class);
    private BlockPos orePos;
    private BlockState originalOre;

    StorageCheck(List<String> report, String levelId, Consumer<String> screenshot) {
        super("乾坤袋", report, levelId);
        this.screenshot = screenshot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                selected = player.getInventory().selected;
                originalWarehouse = MaterialStorage.snapshot(player);
                originalAnalysis = player.getData(MaterialData.ANALYSIS);
                for (Profession profession : Profession.values()) originalProfessions.put(profession, player.getData(ProfessionAttachments.PROGRESS).get(profession));
                for (int index = 0; index < player.getInventory().getContainerSize(); index++) inventory.add(player.getInventory().getItem(index).copy());
                player.getInventory().clearContent();
                player.getInventory().selected = 0;
                sample = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_018")), 37);
                sample.set(DataComponents.CUSTOM_NAME, Component.literal("储物检查材料"));
                sample.set(MaterialData.INSTANCE.get(), new MaterialInstance(17, 3));
            });
            return true;
        }
        if (step <= 28) {
            int round = (step - 1) / 7;
            BagKind kind = BagKind.values()[round];
            int phase = (step - 1) % 7;
            if (local < 12) return false;
            switch (phase) {
                case 0 -> onServer(player -> {
                    if (round > 0) expectTrue("切走主手后前一档袋自动关闭", player.containerMenu == player.inventoryMenu);
                    player.getInventory().setItem(0, new ItemStack(kind.item()));
                    player.getInventory().setItem(9, sample.copy());
                    player.getInventory().setItem(10, new ItemStack(BagKind.values()[(round + 1) % 4].item()));
                    player.inventoryMenu.broadcastChanges();
                });
                case 1 -> open();
                case 2 -> {
                    onServer(player -> expectTrue(kind.id() + " 真实右键打开对应原生界面并同步绑定库存", player.containerMenu instanceof BagMenu menu
                            && menu.kind() == kind && menu.slots.size() == kind.size() + 36
                            && minecraft.screen instanceof BagScreen && minecraft.player.containerMenu.getType() == menu.getType()));
                    click(kind.size(), 0, ClickType.PICKUP);
                    click(kind.size() - 1, 0, ClickType.PICKUP);
                }
                case 3 -> {
                    onServer(player -> expectTrue(kind.id() + " 末槽存入37件材料且数量、名称和实例组件双端一致", matches(player, kind.size() - 1, sample)
                            && player.containerMenu.slots.get(kind.size()).getItem().isEmpty() && player.containerMenu.getCarried().isEmpty()));
                    click(kind.size() - 1, 1, ClickType.PICKUP);
                    click(0, 0, ClickType.PICKUP);
                    click(kind.size() - 1, 0, ClickType.QUICK_MOVE);
                    click(kind.size() + 35, 0, ClickType.QUICK_MOVE);
                    click(kind.size() + 1, 0, ClickType.PICKUP);
                    click(1, 0, ClickType.PICKUP);
                    click(kind.size() + 1, 0, ClickType.PICKUP);
                }
                case 4 -> {
                    onServer(player -> {
                        ItemStack bag = player.getMainHandItem();
                        ItemStack decoded = ItemStack.parse(player.registryAccess(), bag.save(player.registryAccess())).orElseThrow();
                        IItemHandler items = bag.getCapability(Capabilities.ItemHandler.ITEM);
                        expectTrue(kind.id() + " 分堆与Shift往返合并后仍为37件，储物物品被拒且序列化保留全部组件", matches(player, 0, sample)
                                && player.containerMenu.slots.get(kind.size() - 1).getItem().isEmpty()
                                && player.containerMenu.slots.get(1).getItem().isEmpty()
                                && player.getInventory().getItem(10).is(BagKind.values()[(round + 1) % 4].item())
                                && player.containerMenu.getCarried().isEmpty() && ItemStack.matches(bag, decoded)
                                && items.getSlots() == kind.size() && !items.insertItem(2, new ItemStack(kind.item()), true).isEmpty());
                    });
                    minecraft.player.closeContainer();
                }
                case 5 -> open();
                case 6 -> {
                    onServer(player -> {
                        expectTrue(kind.id() + " 关闭重开保留原袋库存及客户端内容", player.containerMenu instanceof BagMenu && matches(player, 0, sample));
                        player.getInventory().setItem(20 + round, player.getMainHandItem());
                        player.getInventory().setItem(0, ItemStack.EMPTY);
                        player.inventoryMenu.broadcastChanges();
                    });
                }
                default -> throw new IllegalStateException("乾坤袋检查阶段 " + phase);
            }
            if (phase == 3) screenshot.accept("storage-" + kind.id());
            return true;
        }
        switch (step) {
            case 29 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("切走主手后第四档袋自动关闭", player.containerMenu == player.inventoryMenu));
                leaveWorld();
            }
            case 30 -> {
                return reopened(local);
            }
            case 31 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    for (BagKind kind : BagKind.values()) {
                        ItemStack bag = player.getInventory().getItem(20 + kind.ordinal());
                        ItemStack clientBag = minecraft.player.getInventory().getItem(20 + kind.ordinal());
                        IItemHandler items = bag.getCapability(Capabilities.ItemHandler.ITEM);
                        expectTrue(kind.id() + " 退出重进后袋归原库存，37件材料与实例组件持久化并同步", bag.is(kind.item())
                                && ItemStack.matches(bag, clientBag) && ItemStack.matches(sample, items.getStackInSlot(0))
                                && items.getStackInSlot(kind.size() - 1).isEmpty());
                    }
                });
            }
            case 32 -> onServer(player -> {
                player.getInventory().clearContent();
                player.setData(StorageData.WAREHOUSE, MaterialWarehouse.EMPTY);
                player.setData(MaterialData.ANALYSIS, MaterialAnalysis.EMPTY);
                Materials.initializeYihuo(player);
                expectTrue("新玩家异火初始化保存六条已揭示效果及特殊标记", Materials.revealedEntries(player, ModId.VALUE + ":cailiao_221").size() == 6
                        && player.getData(MaterialData.ANALYSIS).yihuoInitialized());
                Professions.reset(player);
                Professions.addExperience(player, Profession.GATHERING, 98);
                orePos = player.blockPosition().offset(2, 0, 0);
                originalOre = player.serverLevel().getBlockState(orePos);
                player.serverLevel().setBlockAndUpdate(orePos, Blocks.DIAMOND_ORE.defaultBlockState());
                player.getInventory().setItem(0, new ItemStack(Items.NETHERITE_PICKAXE));
                player.gameMode.destroyBlock(orePos);
                expectTrue("真实原版破坏矿石入口增加2采集经验", Professions.experience(player, Profession.GATHERING) == 100);
            });
            case 33 -> {
                if (local < 12) return false;
                onServer(player -> {
                    expectTrue("每刻职业升级扣旧上限并同步本人", Professions.level(player, Profession.GATHERING) == 1
                            && Professions.experience(player, Profession.GATHERING) == 0 && Professions.experienceMax(player, Profession.GATHERING) == 100
                            && Professions.level(minecraft.player, Profession.GATHERING) == 1);
                    player.serverLevel().setBlockAndUpdate(orePos, originalOre);
                    player.getInventory().setItem(0, item("qiankundai_06", 1));
                    player.getInventory().setItem(9, sample.copy());
                    ItemStack low = item("cailiao_018", 5);
                    Materials.setInstance(low, 1, 0);
                    player.getInventory().setItem(10, low);
                    ItemStack high = sample.copy();
                    Materials.setInstance(high, 42, 3);
                    expectTrue("材料袋输入按目标标签排除非材料、异火和42级材料", MaterialStorage.accepts(sample) && !MaterialStorage.accepts(high)
                            && !MaterialStorage.accepts(item("cailiao_221", 1)) && !MaterialStorage.accepts(new ItemStack(Items.STONE)));
                    player.inventoryMenu.broadcastChanges();
                });
            }
            case 34 -> {
                if (local < 12) return false;
                open();
            }
            case 35 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("材料袋真实右键打开46槽原生菜单", player.containerMenu instanceof MaterialBagMenu
                        && player.containerMenu.slots.size() == 82 && minecraft.screen instanceof MaterialBagScreen));
                click(46, 0, ClickType.PICKUP);
                click(0, 0, ClickType.PICKUP);
                button(2);
            }
            case 36 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("整栈37件入玩家仓，展示为17级五品样本且双端一致", MaterialStorage.snapshot(player).total() == 37
                        && MaterialStorage.snapshot(player).equals(MaterialStorage.snapshot(minecraft.player))
                        && player.containerMenu.slots.get(0).getItem().isEmpty()
                        && player.containerMenu.slots.get(1).getItem().getCount() == 17
                        && Materials.instance(player.containerMenu.slots.get(1).getItem()).quality() == 5));
                click(1, 0, ClickType.PICKUP);
                button(8);
                button(4);
            }
            case 37 -> {
                if (local < 12) return false;
                onServer(player -> {
                    int returned = 0;
                    for (ItemStack stack : player.getInventory().items) {
                        if (stack.is(sample.getItem()) && Materials.instance(stack).level() == 17 && Materials.instance(stack).quality() == 3
                                && !stack.has(DataComponents.CUSTOM_NAME)) returned += stack.getCount();
                    }
                    expectTrue("展示槽拒拿取，品质按钮仅交付一件并按来源丢弃名称", player.containerMenu.getCarried().isEmpty()
                            && MaterialStorage.snapshot(player).total() == 36 && returned == 1);
                });
                option(9, true);
                click(47, 0, ClickType.PICKUP);
                click(0, 0, ClickType.PICKUP);
            }
            case 38 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("自动存入仅处理输入槽并合并同ID不同等级", MaterialStorage.snapshot(player).total() == 41
                        && MaterialStorage.snapshot(player).styles() == 2 && player.containerMenu.slots.get(0).getItem().isEmpty()));
                option(0, true);
            }
            case 39 -> {
                if (local < 25) return false;
                onServer(player -> expectTrue("未辨识材料不通过血量属性筛选", player.containerMenu.slots.subList(1, 46).stream().allMatch(slot -> slot.getItem().isEmpty())));
                option(0, false);
                button(53);
            }
            case 40 -> {
                if (local < 25) return false;
                onServer(player -> expectTrue("从大到小排序先显示17级", player.containerMenu.slots.get(1).getItem().getCount() == 17));
                button(53);
            }
            case 41 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("从小到大排序先显示1级", player.containerMenu.slots.get(1).getItem().getCount() == 1));
                option(10, true);
                minecraft.player.closeContainer();
            }
            case 42 -> {
                if (local < 12) return false;
                onServer(player -> {
                    expectTrue("关闭菜单保存自动存入和允许扣仓偏好", MaterialStorage.snapshot(player).preferences().automatic()
                            && MaterialStorage.snapshot(player).preferences().spending());
                    player.getInventory().clearContent();
                    player.getInventory().setItem(0, item("qiancailubook", 1));
                    ItemStack input = item("cailiao_018", 1);
                    Materials.setInstance(input, 41, 4);
                    player.getInventory().setItem(9, input);
                    player.inventoryMenu.broadcastChanges();
                });
            }
            case 43 -> {
                if (local < 12) return false;
                open();
            }
            case 44 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("千材录真实右键打开输入菜单和原生界面", player.containerMenu instanceof MaterialAnalysisMenu
                        && minecraft.screen instanceof MaterialAnalysisScreen));
                click(1, 0, ClickType.PICKUP);
                click(0, 0, ClickType.PICKUP);
                analysisButton(1);
            }
            case 45 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("首次点击辨识只刷新材料，不扣仓不推进", player.getData(MaterialData.ANALYSIS).progress(ModId.VALUE + ":cailiao_018") == 0
                        && MaterialStorage.snapshot(player).total() == 41 && ((MaterialAnalysisMenu) player.containerMenu).selectedId().endsWith(":cailiao_018")));
                analysisButton(1);
            }
            case 46 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("进度0免费揭示来源第一条expnum并同步", Materials.revealedEntries(player, ModId.VALUE + ":cailiao_018").getFirst().name().equals("expnum")
                        && MaterialStorage.snapshot(player).total() == 41 && Materials.revealedEntries(minecraft.player, ModId.VALUE + ":cailiao_018").size() == 1));
                analysisButton(1);
            }
            case 47 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("第二次辨识成本2优先扣仓1级零品，输入不变", MaterialStorage.snapshot(player).total() == 39
                        && MaterialStorage.snapshot(player).group(ModId.VALUE + ":cailiao_018").count(1, 0) == 3
                        && player.containerMenu.slots.get(0).getItem().getCount() == 1));
                analysisButton(1);
            }
            case 48 -> {
                if (local < 12) return false;
                onServer(player -> expectTrue("第三次成本4跨等级扣仓并保留来源等级计数差异", MaterialStorage.snapshot(player).total() == 35
                        && MaterialStorage.snapshot(player).styles() == 1
                        && MaterialStorage.snapshot(player).group(ModId.VALUE + ":cailiao_018").levelCount() == 2
                        && MaterialStorage.snapshot(player).group(ModId.VALUE + ":cailiao_018").count(17, 3) == 35
                        && Materials.revealedEntries(player, ModId.VALUE + ":cailiao_018").get(2).name().equals("powernum")));
                screenshot.accept("storage-material-analysis");
                minecraft.player.closeContainer();
            }
            case 49 -> {
                if (local < 12) return false;
                onServer(player -> {
                    player.getInventory().setItem(0, item("qiankundai_06", 1));
                    player.inventoryMenu.broadcastChanges();
                });
            }
            case 50 -> {
                if (local < 12) return false;
                open();
            }
            case 51 -> {
                if (local < 12) return false;
                option(0, true);
            }
            case 52 -> {
                if (local < 25) return false;
                onServer(player -> expectTrue("已揭示heanum材料通过属性筛选", player.containerMenu.slots.get(1).getItem().getCount() == 17));
                option(8, true);
            }
            case 53 -> {
                if (local < 25) return false;
                onServer(player -> expectTrue("多个属性筛选按AND，不显示缺精神防御的材料", player.containerMenu.slots.subList(1, 46).stream().allMatch(slot -> slot.getItem().isEmpty())));
                option(0, false);
                option(8, false);
            }
            case 54 -> {
                if (local < 25) return false;
                screenshot.accept("storage-material-warehouse");
                minecraft.player.closeContainer();
            }
            case 55 -> {
                if (local < 12) return false;
                onServer(player -> savedWarehouse = MaterialStorage.snapshot(player));
                kill();
            }
            case 56 -> {
                return respawned(local);
            }
            case 57 -> {
                if (!inWorld(local)) return false;
                onServer(player -> expectTrue("真实死亡重生后材料仓快照双端一致、辨识进度与采集等级保留", savedWarehouse.equals(MaterialStorage.snapshot(player))
                        && savedWarehouse.equals(MaterialStorage.snapshot(minecraft.player))
                        && Materials.revealedEntries(player, ModId.VALUE + ":cailiao_018").size() == 3
                        && Professions.level(player, Profession.GATHERING) == 1 && Professions.level(minecraft.player, Profession.GATHERING) == 1));
                leaveWorld();
            }
            case 58 -> {
                return reopened(local);
            }
            case 59 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    expectTrue("退出重进后材料仓快照、辨识条目数与采集等级持久化，双端一致", savedWarehouse.equals(MaterialStorage.snapshot(player))
                            && savedWarehouse.equals(MaterialStorage.snapshot(minecraft.player))
                            && Materials.revealedEntries(player, ModId.VALUE + ":cailiao_018").size() == 3
                            && Materials.revealedEntries(minecraft.player, ModId.VALUE + ":cailiao_018").size() == 3
                            && Professions.level(player, Profession.GATHERING) == 1 && Professions.level(minecraft.player, Profession.GATHERING) == 1);
                    player.setData(StorageData.WAREHOUSE, originalWarehouse);
                    player.setData(MaterialData.ANALYSIS, originalAnalysis);
                    originalProfessions.forEach((profession, progress) -> player.getData(ProfessionAttachments.PROGRESS).set(profession, progress));
                    player.syncData(ProfessionAttachments.PROGRESS);
                    player.getInventory().clearContent();
                    for (int index = 0; index < inventory.size(); index++) player.getInventory().setItem(index, inventory.get(index));
                    player.getInventory().selected = selected;
                    player.inventoryMenu.broadcastChanges();
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private boolean matches(ServerPlayer player, int slot, ItemStack expected) {
        return ItemStack.matches(expected, player.containerMenu.slots.get(slot).getItem())
                && ItemStack.matches(expected, minecraft.player.containerMenu.slots.get(slot).getItem());
    }

    private void open() {
        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
    }

    private void click(int slot, int button, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, button, type, minecraft.player);
    }

    private void button(int id) {
        minecraft.gameMode.handleInventoryButtonClick(minecraft.player.containerMenu.containerId, id);
    }

    private void option(int id, boolean selected) {
        button(100 + id * 2 + (selected ? 1 : 0));
    }

    private void analysisButton(int id) {
        PacketDistributor.sendToServer(new MaterialAnalysisButtonPayload(minecraft.player.containerMenu.containerId, id));
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), count);
    }
}
