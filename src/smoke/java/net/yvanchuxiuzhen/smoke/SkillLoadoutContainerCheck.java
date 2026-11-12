package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/** 验证04装配页绑定方块容器物品能力，并在关闭时保留容器内物品。 */
final class SkillLoadoutContainerCheck extends SystemCheck {
    private final ItemStack testBook = book();
    private SkillState originalState;
    private BlockPos chestPos;
    private BlockState originalChestState;
    private CompoundTag originalChestEntity;

    SkillLoadoutContainerCheck(List<String> report, String levelId) {
        super("技能装配容器", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                if (local < 2) return false;
                onServer(player -> {
                    originalState = copyState(Skills.state(player));
                    chestPos = player.blockPosition().above();
                    ServerLevel level = player.serverLevel();
                    originalChestState = level.getBlockState(chestPos);
                    BlockEntity chestEntity = level.getBlockEntity(chestPos);
                    originalChestEntity = chestEntity == null ? null : chestEntity.saveWithFullMetadata(player.registryAccess());
                    level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
                    player.setData(SkillData.STATE, originalState.withPage(1).withSpell(1, testBook.copy()));
                    Skills.openLoadout(player);
                });
                return true;
            }
            case 1 -> {
                if (local < 8) return false;
                boolean menuOpen = minecraft.player.containerMenu instanceof SkillLoadoutMenu
                        && minecraft.screen instanceof SkillLoadoutScreen;
                boolean menuBook = menuOpen && ItemStack.matches(minecraft.player.containerMenu.slots.get(3).getItem(), testBook);
                boolean[] chestBound = {false};
                onServer(player -> {
                    BlockEntity blockEntity = player.serverLevel().getBlockEntity(chestPos);
                    chestBound[0] = blockEntity instanceof ChestBlockEntity chest && ItemStack.matches(chest.getItem(3), testBook);
                });
                expectTrue("默认头顶容器入口打开04屏幕，菜单槽04与箱子槽04同步玩家技能书", menuOpen && menuBook && chestBound[0]);
                minecraft.execute(() -> minecraft.player.closeContainer());
                return true;
            }
            case 2 -> {
                if (local < 6) return false;
                boolean[] retained = {false};
                onServer(player -> {
                    BlockEntity blockEntity = player.serverLevel().getBlockEntity(chestPos);
                    retained[0] = blockEntity instanceof ChestBlockEntity chest && ItemStack.matches(chest.getItem(3), testBook);
                    player.setData(SkillData.STATE, originalState);
                    ServerLevel level = player.serverLevel();
                    restoreBlock(player, level, chestPos, originalChestState, originalChestEntity);
                });
                expectTrue("关闭菜单后测试书仍保存在箱子槽04", retained[0]);
                finish();
                return true;
            }
            default -> {
                finish();
                return true;
            }
        }
    }

    private static ItemStack book() {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_006")));
        stack.set(SkillData.SKILL.get(), new SkillBook(SkillKind.SPELL, "容器绑定检查", 1, 1, "无", "", Optional.empty(), "",
                Map.of("Firecolddown", 2.0), Map.of()));
        return stack;
    }

    private static SkillState copyState(SkillState state) {
        return new SkillState(state.spells().stream().map(ItemStack::copy).toList(), state.movement().copy(), state.flight().copy(),
                state.weaponArt().copy(), state.page(), state.selected().copy(), state.selectedKey(), state.selectedSlot(), state.combo(),
                state.arrow().copy(), state.arrowChoice());
    }

    private static void restoreBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, CompoundTag blockEntityData) {
        level.setBlock(pos, state, 3);
        if (blockEntityData != null) {
            BlockEntity restored = BlockEntity.loadStatic(pos, state, blockEntityData, player.registryAccess());
            level.setBlockEntity(restored);
            restored.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
