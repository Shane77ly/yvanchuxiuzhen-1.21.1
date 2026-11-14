package net.yvanchuxiuzhen.system.economy.content;

import net.minecraft.core.BlockPos;
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
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;

/** 来源单卡槽登录页；验证本人后先清旧槽，再打开银行并把卡固定为一枚放入新槽0。 */
public final class BankLoginMenu extends BankInventoryMenu {
    private static final TagKey<Item> BANK_CARDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "bank"));

    public BankLoginMenu(int id, Inventory inventory, BlockPos pos) {
        super(BankMenus.LOGIN.get(), id, inventory, pos, 1, 0);
        addCustom(new SlotItemHandler(items, 0, 133, 157) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(BANK_CARDS);
            }
        });
        addPlayerInventory(inventory);
    }

    public BankLoginMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || button < 0 || button > 2 || !player.level().hasChunkAt(pos)) return false;
        if (button == 0) {
            ItemStack card = getSlot(0).getItem();
            if (!card.is(BANK_CARDS)) {
                BankCards.message(serverPlayer, "login_empty");
            } else if (!BankCards.ownedBy(card, serverPlayer)) {
                BankCards.message(serverPlayer, "login_not_owner");
            } else {
                ItemStack transferred = card.copy();
                getSlot(0).set(ItemStack.EMPTY);
                broadcastChanges();
                BankMenus.openBank(serverPlayer, pos);
                transferred.setCount(1);
                if (player.containerMenu instanceof BankMenu menu) {
                    menu.getSlot(0).set(transferred);
                    menu.broadcastChanges();
                }
            }
        } else if (button == 1) {
            player.closeContainer();
        } else {
            BankMenus.explain(serverPlayer);
        }
        return true;
    }
}
