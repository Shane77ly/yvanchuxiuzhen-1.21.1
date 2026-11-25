package net.yvanchuxiuzhen.system.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonWorldData;
import net.yvanchuxiuzhen.system.dungeon.data.PaintingState;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;

/** 画里乾坤的双次交互、私人结构生成与两端物品快照由副本系统保存。 */
public final class PaintingRuntime {
    private PaintingRuntime() {
    }

    public static void tick(DungeonBlockEntity block) {
        CompoundTag data = block.state();
        if (data.getBoolean("chuanyve")) {
            data.putDouble("blocktick", data.getDouble("blocktick") + 1);
            if (data.getDouble("blocktick") >= 8) {
                data.putBoolean("chuanyve", false);
                data.putDouble("blocktick", 0);
            }
            block.changed();
        }
    }

    public static void use(DungeonBlockEntity block, ServerPlayer player) {
        ItemStack painting = block.getItem(0).copy();
        PaintingState item = state(painting);
        if (player.isShiftKeyDown()) {
            if (item.ownerUuid().equals(player.getStringUUID())) {
                painting.setCount(1);
                ItemHandlerHelper.giveItemToPlayer(player, painting);
                block.setItem(0, ItemStack.EMPTY);
                player.level().setBlock(block.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            } else player.displayClientMessage(Component.literal("§4您不是此画里乾坤的主人，无法收起此法宝！"), false);
            return;
        }
        boolean inside = player.level().dimension().equals(DungeonPlayers.PAINTING);
        if (!inside && !DungeonPlayers.supported(player.level().dimension())) {
            player.displayClientMessage(Component.literal("§4在这个维度无法使用。"), false);
            return;
        }
        if (!block.state().getBoolean("chuanyve")) {
            block.animate(1);
            block.state().putBoolean("chuanyve", true);
            block.changed();
            return;
        }
        if (inside) {
            item = item.withReturning(true);
            save(painting, item);
            player.setData(DungeonData.PAINTING_TRANSIT, painting);
            ResourceKey<Level> destination = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(returnDimension(item.returnDimension())));
            DungeonPlayers.travel(player, destination, item.rukouX() + 0.5, item.rukouY() + 2, item.rukouZ() + 0.5);
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        double number = !item.activated() ? DungeonWorldData.get(player.server).nextPainting(player.serverLevel()) : item.paintingNumber();
        item = item.enter(dimensionName(player.level().dimension()), !item.activated(), number);
        save(painting, item);
        player.setData(DungeonData.PAINTING_TRANSIT, painting.copy());
        block.setItem(0, painting.copyWithCount(1));
        block.changed();
        DungeonPlayers.travel(player, DungeonPlayers.PAINTING, item.chukouX() + 0.5, item.chukouY() + 2, item.chukouZ() + 0.5);
    }

    static void entered(ServerPlayer player) {
        ItemStack painting = player.getData(DungeonData.PAINTING_TRANSIT);
        PaintingState item = state(painting);
        if (!item.entering()) return;
        BlockPos exit = item.chukouPos();
        if (!item.placed()) {
            WorldStructures.place(player.serverLevel(), ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao008jiegou"), exit.offset(-24, 0, -24));
            exit = exit.above(2);
            player.level().setBlock(exit, DungeonPlayers.block("fabao_009").defaultBlockState(), 3);
            item = item.withPlacedExitRaised();
        } else {
            item = item.withEntering(false);
        }
        save(painting, item);
        store(player, exit, painting);
    }

    static void returned(ServerPlayer player) {
        ItemStack painting = player.getData(DungeonData.PAINTING_TRANSIT);
        PaintingState item = state(painting);
        if (!item.returning()) return;
        item = item.withReturning(false);
        save(painting, item);
        player.setData(DungeonData.PAINTING_TRANSIT, painting.copy());
        store(player, item.rukouPos(), painting);
    }

    private static void store(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (player.level().getBlockEntity(pos) instanceof DungeonBlockEntity block) {
            block.setItem(0, stack.copyWithCount(1));
            block.changed();
        }
    }

    public static PaintingState state(ItemStack stack) { return stack.getOrDefault(DungeonData.PAINTING_STATE.get(), PaintingState.EMPTY); }
    public static void save(ItemStack stack, PaintingState state) { stack.set(DungeonData.PAINTING_STATE.get(), state); }

    private static String dimensionName(ResourceKey<Level> dimension) {
        return switch (dimension.location().toString()) {
            case "minecraft:overworld" -> "主世界";
            case "minecraft:the_nether" -> "地狱";
            case "minecraft:the_end" -> "末地";
            case "yvanchuxiuzhen:dimensiontianqve" -> "天阙界";
            case "yvanchuxiuzhen:dimensionxiuluo" -> "修罗界";
            case "yvanchuxiuzhen:dimensionrenjian" -> "人间界";
            case "yvanchuxiuzhen:dimensionyaohun" -> "妖魂界";
            case "yvanchuxiuzhen:dimensiondiling" -> "地灵界";
            case "yvanchuxiuzhen:dimensionwugui" -> "无归界";
            default -> "";
        };
    }

    private static String returnDimension(String name) {
        return switch (name) {
            case "主世界" -> "minecraft:overworld";
            case "地狱" -> "minecraft:the_nether";
            case "末地" -> "minecraft:the_end";
            case "天阙界" -> "yvanchuxiuzhen:dimensiontianqve";
            case "修罗界" -> "yvanchuxiuzhen:dimensionxiuluo";
            case "人间界" -> "yvanchuxiuzhen:dimensionrenjian";
            case "妖魂界" -> "yvanchuxiuzhen:dimensionyaohun";
            case "无归界" -> "yvanchuxiuzhen:dimensiondiling";
            case "地灵界" -> "yvanchuxiuzhen:dimensionwugui";
            default -> "";
        };
    }
}
