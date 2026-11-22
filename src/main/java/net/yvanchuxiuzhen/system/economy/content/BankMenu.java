package net.yvanchuxiuzhen.system.economy.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.BankUpdates;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;

/** 来源11槽银行：0卡、1至9只读展示、10存款输入；关闭只归还卡和未存入的输入。 */
public final class BankMenu extends BankInventoryMenu {
    private static final TagKey<Item> SPIRIT_STONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi"));

    public BankMenu(int id, Inventory inventory, BlockPos pos) {
        super(BankMenus.BANK.get(), id, inventory, pos, 11, 0, 10);
        addCustom(new SlotItemHandler(items, 0, 133, 157) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int tier = 1; tier <= 9; tier++) {
            addCustom(new SlotItemHandler(items, tier, 133 + (tier - 1) % 3 * 81, 184 + (tier - 1) / 3 * 27) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        addCustom(new SlotItemHandler(items, 10, 70, 157) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(SPIRIT_STONES);
            }
        });
        addPlayerInventory(inventory);
        if (inventory.player instanceof ServerPlayer player) BankUpdates.opened(player);
    }

    public BankMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || button < 0 || button > 11 || !player.level().hasChunkAt(pos)) return false;
        if (button <= 8) {
            BankAccounts.withdrawOne(serverPlayer, button + 1);
            BankAccounts.refresh(serverPlayer);
        } else if (button == 9) {
            ItemStack input = getSlot(10).getItem();
            if (input.is(SPIRIT_STONES)) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(input.getItem());
                for (int tier = 1; tier <= 9; tier++) {
                    if (itemId.equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_19" + tier))) {
                        BankAccounts.depositCount(serverPlayer, tier, input.getCount());
                        break;
                    }
                }
                getSlot(10).set(ItemStack.EMPTY);
                broadcastChanges();
            } else {
                BankCards.message(serverPlayer, "deposit_empty");
            }
            BankUpdates.refreshLater(serverPlayer);
        } else if (button == 10) {
            player.closeContainer();
        } else {
            BankMenus.explain(serverPlayer);
        }
        return true;
    }
}
