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
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/** 验证04装配页绑定方块容器物品能力，并在关闭时保留容器内物品。 */
final class SkillLoadoutContainerCheck extends SystemCheck {
    private final ItemStack[] testSlots = books();
    private SkillState originalState;
    private ItemStack originalTechniqueSlot;
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
                    originalTechniqueSlot = Techniques.slot(player).copy();
                    chestPos = player.blockPosition().above();
                    ServerLevel level = player.serverLevel();
                    originalChestState = level.getBlockState(chestPos);
                    BlockEntity chestEntity = level.getBlockEntity(chestPos);
                    originalChestEntity = chestEntity == null ? null : chestEntity.saveWithFullMetadata(player.registryAccess());
                    level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
                    Techniques.setSlot(player, testSlots[0]);
                    Skills.setMovement(player, testSlots[1].copy());
                    Skills.setFlight(player, testSlots[2].copy());
                    for (int slot = 0; slot < SkillState.PAGE_SIZE; slot++) {
                        Skills.setSpell(player, slot + 1, testSlots[slot + 3].copy());
                    }
                    Skills.setWeaponArt(player, testSlots[7].copy());
                    player.setData(SkillData.STATE, Skills.state(player).withPage(1));
                    Skills.openLoadout(player);
                });
                return true;
            }
            case 1 -> {
                if (local < 8) return false;
                boolean menuOpen = minecraft.player.containerMenu instanceof SkillLoadoutMenu
                        && minecraft.screen instanceof SkillLoadoutScreen;
                boolean menuBook = menuOpen && ItemStack.matches(minecraft.player.containerMenu.slots.get(3).getItem(), testSlots[3]);
                boolean[] chestBound = {false};
                boolean[] chestMatches = new boolean[testSlots.length];
                onServer(player -> {
                    BlockEntity blockEntity = player.serverLevel().getBlockEntity(chestPos);
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        chestBound[0] = ItemStack.matches(chest.getItem(3), testSlots[3]);
                        for (int slot = 0; slot < testSlots.length; slot++) {
                            chestMatches[slot] = ItemStack.matches(chest.getItem(slot), testSlots[slot]);
                        }
                    }
                });
                expectTrue("默认头顶容器入口打开04屏幕，菜单槽04与箱子槽04同步玩家技能书", menuOpen && menuBook && chestBound[0]);
                for (int slot = 0; slot < testSlots.length; slot++) {
                    boolean menuMatches = menuOpen && ItemStack.matches(minecraft.player.containerMenu.slots.get(slot).getItem(), testSlots[slot]);
                    expectTrue("普通箱子槽" + (slot + 1) + "绑定菜单槽" + (slot + 1), menuMatches && chestMatches[slot]);
                }
                minecraft.execute(() -> minecraft.player.closeContainer());
                return true;
            }
            case 2 -> {
                if (local < 6) return false;
                boolean[] retained = {false};
                boolean[] retainedSlots = new boolean[testSlots.length];
                onServer(player -> {
                    BlockEntity blockEntity = player.serverLevel().getBlockEntity(chestPos);
                    retained[0] = blockEntity instanceof ChestBlockEntity chest && ItemStack.matches(chest.getItem(3), testSlots[3]);
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        for (int slot = 0; slot < testSlots.length; slot++) {
                            retainedSlots[slot] = ItemStack.matches(chest.getItem(slot), testSlots[slot]);
                        }
                    }
                    player.setData(SkillData.STATE, originalState);
                    Techniques.setSlot(player, originalTechniqueSlot);
                    ServerLevel level = player.serverLevel();
                    restoreBlock(player, level, chestPos, originalChestState, originalChestEntity);
                });
                expectTrue("关闭菜单后测试书仍保存在箱子槽04", retained[0]);
                for (int slot = 0; slot < retainedSlots.length; slot++) {
                    expectTrue("关闭菜单后箱子槽" + (slot + 1) + "仍保留测试物品", retainedSlots[slot]);
                }
                finish();
                return true;
            }
            default -> {
                finish();
                return true;
            }
        }
    }

    private static ItemStack[] books() {
        return new ItemStack[] {
                Techniques.createBook(TechniqueLibrary.all().getFirst()),
                book(SkillKind.MOVEMENT, "容器身法槽"),
                book(SkillKind.FLIGHT, "容器飞行槽"),
                book(SkillKind.SPELL, "容器技能槽一"),
                book(SkillKind.SPELL, "容器技能槽二"),
                book(SkillKind.SPELL, "容器技能槽三"),
                book(SkillKind.SPELL, "容器技能槽四"),
                book(SkillKind.WEAPON_ART, "容器武器技槽")
        };
    }

    private static ItemStack book(SkillKind kind, String name) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_006")));
        stack.set(SkillData.SKILL.get(), new SkillBook(kind, name, 1, 1, "无", "", Optional.empty(), "",
                kind == SkillKind.SPELL ? Map.of("Firecolddown", 2.0) : Map.of(), Map.of()));
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
