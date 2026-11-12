package net.yvanchuxiuzhen.system.lianqi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketMenu;

/** 缝纫配方预览、共享页码和扣料发放；材料检查忽略实例组件并遍历玩家完整库存。 */
public final class SewingBasket {
    private static final List<Recipe> RECIPES = ContentFiles.read("content/lianqi/sewing_recipes.json", Recipe.CODEC);

    private SewingBasket() {
    }

    public static void opened(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        LianqiDelays.run(2, () -> select(player, level, pos));
    }

    /** 每次选择先清当前缝纫菜单，再从原坐标读取页码；同型菜单之间仍保留延后写入次序。 */
    public static void select(ServerPlayer player, Level level, BlockPos pos) {
        SewingBasketMenu current = player.containerMenu instanceof SewingBasketMenu menu ? menu : null;
        if (current != null) {
            for (int slot = 0; slot < 8; slot++) current.setPreview(slot, ItemStack.EMPTY);
        }
        if (!(level.getBlockEntity(pos) instanceof SewingBasketBlockEntity basket)) return;
        Recipe recipe = RECIPES.get(basket.getPersistentData().getInt("xvanze"));
        basket.getPersistentData().putInt("pagemax", RECIPES.size() - 1);
        basket.changed();
        if (current == null) return;
        for (int slot = 0; slot < recipe.outputs().size(); slot++) {
            current.setPreview(slot, new ItemStack(BuiltInRegistries.ITEM.get(recipe.outputs().get(slot))));
        }
        for (int index = 0; index < recipe.materials().size(); index++) {
            Material material = recipe.materials().get(index);
            current.setPreview(index + 4, new ItemStack(BuiltInRegistries.ITEM.get(material.item()), material.count()));
        }
    }

    public static void page(ServerPlayer player, SewingBasketBlockEntity basket, boolean forward) {
        int maximum = basket.getPersistentData().getInt("pagemax");
        int page = basket.getPersistentData().getInt("xvanze");
        if (forward && page <= maximum) {
            page++;
            if (page > maximum) page = 0;
        } else if (!forward && page >= 0) {
            page--;
            if (page < 0) page = maximum;
        }
        basket.getPersistentData().putInt("xvanze", page);
        select(player, basket.getLevel(), basket.getBlockPos());
    }

    /** 四个需求槽分别检查后按槽序扣费；复制四个结果槽，各压为一件，预览保留以供再次制作。 */
    public static void craft(ServerPlayer player, SewingBasketMenu menu) {
        boolean hasOutput = false;
        for (int slot = 0; slot < 4; slot++) hasOutput |= !menu.getSlot(slot).getItem().isEmpty();
        if (!hasOutput) return;
        for (int slot = 4; slot < 8; slot++) {
            ItemStack material = menu.getSlot(slot).getItem();
            if (material.isEmpty()) continue;
            int count = 0;
            for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                ItemStack carried = player.getInventory().getItem(index);
                if (carried.getItem() == material.getItem()) count += carried.getCount();
            }
            if (count == 0 || count < material.getCount()) return;
        }
        for (int slot = 4; slot < 8; slot++) {
            ItemStack material = menu.getSlot(slot).getItem();
            if (!material.isEmpty()) {
                player.getInventory().clearOrCountMatchingItems(stack -> stack.getItem() == material.getItem(),
                        material.getCount(), player.inventoryMenu.getCraftSlots());
            }
        }
        for (int slot = 0; slot < 4; slot++) {
            ItemStack output = menu.getSlot(slot).getItem();
            if (!output.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, output.copyWithCount(1));
        }
        menu.broadcastChanges();
    }

    public static void closed(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SewingBasketBlockEntity basket) {
            for (int slot = 0; slot < 8; slot++) basket.setItem(slot, ItemStack.EMPTY);
            basket.changed();
        }
    }

    /** 表内顺序对应来源世界加载时写入的十五页有效衣物配方。 */
    private record Recipe(List<ResourceLocation> outputs, List<Material> materials) {
        private static final Codec<Recipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("outputs").forGetter(Recipe::outputs),
                Material.CODEC.listOf().fieldOf("materials").forGetter(Recipe::materials)
        ).apply(instance, Recipe::new));
    }

    private record Material(ResourceLocation item, int count) {
        private static final Codec<Material> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Material::item),
                Codec.INT.fieldOf("count").forGetter(Material::count)
        ).apply(instance, Material::new));
    }
}
