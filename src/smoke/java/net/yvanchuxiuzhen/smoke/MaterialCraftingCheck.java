package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 使用原生工作台菜单核对材料实例的合成事件及快捷取出边界。 */
final class MaterialCraftingCheck extends SystemCheck {
    private final List<UUID> drops = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> dropListener = this::captureDrop;

    MaterialCraftingCheck(List<String> report, String levelId) {
        super("材料合成实例", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 8) return false;
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        player.closeContainer();
        List<ItemStack> saved = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) saved.add(player.getInventory().getItem(i).copy());
        int selected = player.getInventory().selected;
        BlockPos pos = player.blockPosition().above(3);
        if (!player.level().isEmptyBlock(pos)) throw new IllegalStateException("工作台夹具位置非空：" + pos);
        player.level().setBlockAndUpdate(pos, Blocks.CRAFTING_TABLE.defaultBlockState());
        NeoForge.EVENT_BUS.addListener(dropListener);
        try {
            player.getInventory().clearContent();
            player.openMenu(Blocks.CRAFTING_TABLE.defaultBlockState().getMenuProvider(player.level(), pos));
            if (!(player.containerMenu instanceof CraftingMenu)) throw new IllegalStateException("工作台菜单未打开");
            AbstractContainerMenu menu = player.containerMenu;
            craft(player, menu, item("peachlog"), 9, item("cailiao_241"), 5, 2);
            craft(player, menu, item("cailiao_190"), 9, item("cailiao_191"), 5, 4);
            for (int n = 192; n <= 199; n++) {
                craft(player, menu, item("cailiao_" + (n - 1)), 5, item("cailiao_" + n), (n - 190) * 5, 4);
            }
            fillGrid(player, menu, item("cailiao_192"), 1);
            expectTrue("分解配方结果", menu.getSlot(0).getItem().is(item("cailiao_191")) && menu.getSlot(0).getItem().getCount() == 4);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            expect("分解成品等级品阶", new MaterialInstance(5, 4), Materials.instance(menu.getCarried()));
            menu.setCarried(ItemStack.EMPTY);

            fillGrid(player, menu, Items.OAK_LOG, 1);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            expectTrue("非材料合成结果未写材料实例", menu.getCarried().is(Items.OAK_PLANKS)
                    && Materials.instance(menu.getCarried()).equals(MaterialInstance.EMPTY));
            menu.setCarried(ItemStack.EMPTY);

            fillGrid(player, menu, item("cailiao_192"), 1);
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            ItemStack shifted = player.getInventory().items.stream().filter(s -> s.is(item("cailiao_191"))).findFirst().orElse(ItemStack.EMPTY);
            expectTrue("快捷分解进入背包四件", shifted.getCount() == 4);
            expect("快捷转移后事件副本不回写背包", MaterialInstance.EMPTY, Materials.instance(shifted));

            fillGrid(player, menu, item("cailiao_192"), 1);
            for (int i = 0; i < 36; i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            expectTrue("满包快捷转移失败且配方保留", menu.getSlot(0).getItem().is(item("cailiao_191"))
                    && menu.getSlot(1).getItem().is(item("cailiao_192")));
            menu.clicked(0, 1, ClickType.THROW, player);
            ItemEntity drop = drops.isEmpty() ? null : (ItemEntity) player.serverLevel().getEntity(drops.getLast());
            expectTrue("满包主动丢出合成结果", drop != null && drop.getItem().is(item("cailiao_191")) && drop.getItem().getCount() == 4);
            if (drop != null) expect("丢出结果等级品阶", new MaterialInstance(5, 4), Materials.instance(drop.getItem()));
        } finally {
            NeoForge.EVENT_BUS.unregister(dropListener);
            player.containerMenu.setCarried(ItemStack.EMPTY);
            if (player.containerMenu instanceof CraftingMenu) {
                for (int slot = 1; slot <= 9; slot++) player.containerMenu.getSlot(slot).setByPlayer(ItemStack.EMPTY);
            }
            player.closeContainer();
            for (UUID id : drops) {
                var entity = player.serverLevel().getEntity(id);
                if (entity != null) entity.discard();
            }
            player.getInventory().clearContent();
            for (int i = 0; i < saved.size(); i++) player.getInventory().setItem(i, saved.get(i));
            player.getInventory().selected = selected;
            player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            player.inventoryMenu.broadcastChanges();
        }
    }

    private void craft(ServerPlayer player, AbstractContainerMenu menu, Item input, int count, Item output, int level, int quality) {
        fillGrid(player, menu, input, count);
        expectTrue("配方结果 " + output, menu.getSlot(0).getItem().is(output));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        ItemStack result = menu.getCarried();
        expectTrue("普通取出 " + output, result.is(output) && result.getCount() == 1);
        expect("普通取出等级品阶 " + output, new MaterialInstance(level, quality), Materials.instance(result));
        menu.setCarried(ItemStack.EMPTY);
    }

    private static void fillGrid(ServerPlayer player, AbstractContainerMenu menu, Item input, int count) {
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(input, count));
        Slot from = menu.slots.stream().filter(s -> s.container == player.getInventory() && s.getContainerSlot() == 0).findFirst().orElseThrow();
        menu.clicked(from.index, 0, ClickType.PICKUP, player);
        for (int slot = 1; slot <= count; slot++) menu.clicked(slot, 1, ClickType.PICKUP, player);
        if (!menu.getCarried().isEmpty()) throw new IllegalStateException("配方投料后光标未清空");
    }

    private void captureDrop(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity entity && entity.getItem().is(item("cailiao_191"))) {
            drops.add(entity.getUUID());
            entity.setNoGravity(true);
            entity.setPickUpDelay(1200);
        }
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name)).orElseThrow();
    }
}
