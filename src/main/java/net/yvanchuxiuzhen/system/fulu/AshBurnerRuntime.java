package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerMenu;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 烧灰按四十次十刻调度处理逆序首个非空槽，产灰失败仍清空所选整栈。 */
public final class AshBurnerRuntime {
    private AshBurnerRuntime() {
    }

    public static void use(ServerPlayer player, BlockPos pos) {
        AshBurnerBlockEntity burner = (AshBurnerBlockEntity) player.level().getBlockEntity(pos);
        if (player.getMainHandItem().is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "huoyvan")))) {
            if (!burner.isEmpty()) {
                burner.getPersistentData().putBoolean("begin", true);
                burner.changed();
            }
        } else AshBurnerMenu.open(player, pos);
    }

    public static void tick(ServerLevel level, AshBurnerBlockEntity burner) {
        CompoundTag data = burner.getPersistentData();
        if (!data.getBoolean("begin")) return;
        BlockPos pos = burner.getBlockPos();
        level.sendParticles(Particles.type("smokeblack"), pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0);
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0);
        int elapsed = data.getInt("ranshaotick") + 1;
        data.putInt("ranshaotick", elapsed);
        if (elapsed >= 40) {
            for (int slot = 8; slot >= 0; slot--) {
                if (!burner.getItem(slot).isEmpty()) {
                    collectSoot(level, pos);
                    burner.setItem(slot, ItemStack.EMPTY);
                    level.updateNeighborsAt(pos, burner.getBlockState().getBlock());
                    break;
                }
                if (slot == 0) data.putBoolean("begin", false);
            }
            data.putInt("ranshaotick", 0);
        }
        burner.changed();
    }

    private static void collectSoot(ServerLevel level, BlockPos pos) {
        for (int height = 1; height <= 6; height++) {
            BlockPos target = pos.above(height);
            BlockState state = level.getBlockState(target);
            if (state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yanhui")))) {
                String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                int next = switch (id) {
                    case "yvanchuxiuzhen:yanhui_01" -> 2;
                    case "yvanchuxiuzhen:yanhui_02" -> 3;
                    case "yvanchuxiuzhen:yanhui_03" -> 4;
                    case "yvanchuxiuzhen:yanhui_04" -> 5;
                    case "yvanchuxiuzhen:yanhui_05" -> 6;
                    case "yvanchuxiuzhen:yanhui_06" -> 7;
                    default -> 8;
                };
                level.setBlock(target, soot(next), 3);
                break;
            }
            if (level.isEmptyBlock(target) && level.getBlockState(target.above()).isFaceSturdy(level, target.above(), Direction.DOWN)) {
                level.setBlock(target, soot(1), 3);
                break;
            }
        }
    }

    private static BlockState soot(int layers) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yanhui_0" + layers)).defaultBlockState();
    }

    public static void help(ServerPlayer player) {
        player.displayClientMessage(Component.literal("此方块用于制作烟灰，烟灰是制作符箓用墨的基础材料之一。"), false);
        player.displayClientMessage(Component.literal("在烧灰架里放入原木，并用打火石等引燃工具点燃。"), false);
        player.displayClientMessage(Component.literal("在烧灰架上方数格位置，放置一个地面平整的方块并等待一段时间，即可收集到烟灰。"), false);
        player.displayClientMessage(Component.literal("烟灰、朱砂与妖兽材料，可以在铁锅或砂锅处制作墨的料坯。"), false);
    }
}
