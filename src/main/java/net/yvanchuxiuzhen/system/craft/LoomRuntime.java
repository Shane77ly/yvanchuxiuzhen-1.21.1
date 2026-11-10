package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.LoomBlock;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 织布机即时加工主手材料；背包辅助材料按来源顺序匹配和扣除，设备库存不参与加工。 */
public final class LoomRuntime {
    private static final List<Recipe> BASIC = List.of(
            new Recipe("mianitem", "cailiao_171", 4),
            new Recipe("maitem", "cailiao_172", 9),
            new Recipe("cailiao_039", "cailiao_173", 14),
            new Recipe("silkthread", "cailiao_174", 19),
            new Recipe("silkthreadgold", "cailiao_175", 24),
            new Recipe("silkthreadred", "cailiao_176", 29),
            new Recipe("cailiao_17701", "cailiao_177", 34),
            new Recipe("cailiao_17801", "cailiao_178", 39));
    private static final List<Fabric> CLOUD_FABRICS = List.of(
            new Fabric("cailiao_179", 41),
            new Fabric("cailiao_178", 40),
            new Fabric("cailiao_177", 35),
            new Fabric("cailiao_176", 30),
            new Fabric("cailiao_175", 25),
            new Fabric("cailiao_174", 20),
            new Fabric("cailiao_173", 15),
            new Fabric("cailiao_172", 10),
            new Fabric("cailiao_171", 5));

    private LoomRuntime() {
    }

    public static void use(Level world, BlockPos pos, Player player) {
        if (world instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    5, 0, 0.1, 0, 0.1);
        }
        world.setBlock(pos, world.getBlockState(pos).setValue(LoomBlock.ANIMATION, 1), Block.UPDATE_ALL);
        if (world.isClientSide) return;

        ItemStack main = player.getMainHandItem();
        for (Recipe recipe : BASIC) {
            if (!main.is(item(recipe.input()))) continue;
            give(player, recipe.output(), 1, recipe.level());
            main.shrink(1);
            return;
        }
        if (main.is(item("cailiao_209"))) {
            weaveMetal(player, main, item("cailiao_219"));
        } else if (main.is(item("cailiao_219"))) {
            weaveMetal(player, main, item("cailiao_209"));
        } else if (main.is(item("thundergrass"))) {
            for (Fabric fabric : CLOUD_FABRICS) {
                Item ingredient = item(fabric.item());
                if (!contains(player, ingredient)) continue;
                consume(player, ingredient);
                give(player, "cailiao_243", 1, fabric.level());
                main.shrink(1);
                return;
            }
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.loom_requires_cloth")
                    .withStyle(ChatFormatting.DARK_RED), false);
        }
    }

    private static void weaveMetal(Player player, ItemStack main, Item auxiliary) {
        if (!contains(player, auxiliary)) return;
        give(player, "cailiao_179", 2, 41);
        main.shrink(1);
        consume(player, auxiliary);
    }

    /** 来源 contains 只比较物品身份，保留已写入等级和品阶的材料匹配。 */
    private static boolean contains(Player player, Item item) {
        return player.getInventory().contains(stack -> !stack.isEmpty() && stack.is(item));
    }

    private static void consume(Player player, Item item) {
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), 1, player.inventoryMenu.getCraftSlots());
    }

    private static void give(Player player, String id, int count, int level) {
        ItemStack result = new ItemStack(item(id), count);
        int roll = player.getRandom().nextInt(100) + 1;
        int quality = roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
        Materials.setInstance(result, level, quality);
        ItemHandlerHelper.giveItemToPlayer(player, result);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private record Recipe(String input, String output, int level) {
    }

    private record Fabric(String item, int level) {
    }
}
