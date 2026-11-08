package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.storage.client.BagScreen;
import net.yvanchuxiuzhen.system.storage.content.BagKind;
import net.yvanchuxiuzhen.system.storage.content.BagMenu;

/** 乾坤袋资产路径：四档真实网络槽位操作与退出重进，核对数量和材料实例组件没有丢失或回滚。 */
final class StorageCheck extends SystemCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Consumer<String> screenshot;
    private ItemStack sample;
    private int selected;

    StorageCheck(List<String> report, String levelId, Consumer<String> screenshot) {
        super("乾坤袋", report, levelId);
        this.screenshot = screenshot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                selected = player.getInventory().selected;
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
}
