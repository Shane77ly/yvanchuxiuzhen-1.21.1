package net.yvanchuxiuzhen.system.skill.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.SkillPageEvent;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/** 来源 PlayerGUI04 的八个装配槽；技能翻页时保存当前四槽，其余装配在关闭时交回各数据所有者。 */
public final class SkillLoadoutMenu extends AbstractContainerMenu {
    private static final int LOADOUT_SLOTS = 8;
    private final BlockPos pos;
    private static final List<String> HELP = List.of(
            "此界面的所有槽位对应的功法、技能，均需要您在手持功法技能书时，潜行并右键进行学习后，才可装备。",
            "左上角最大的槽位放置功法。",
            "左下角分别放置：轻功、武器法门、飞行技能",
            "轻功可以让您在连续按动方向键时，快速朝着对应方向跳跃。有些轻功还会有额外效果，比如在施展轻功后短时间提高你的移动速度等。",
            "当前模组还未实装武器法门，故而此栏位为空。",
            "飞行技能非常重要，一般在金丹期才可以装备，可让您快速脱离战圈。但飞行技能对灵力的消耗是很大的，所以前期长距离旅行还是需要一个飞行法器。",
            "右侧四个栏位是技能位置，可以切换不同技能页，一共装备16个技能。",
            "装备好技能页后，按下键盘“G”键打开技能选择界面，选择对应技能后，手持武器鼠标右键释放。",
            "（个别技能在你按住“shift”键或“ctrl”键或“alt”键时再点击鼠标右键，会有特殊使用方法哟）");

    public SkillLoadoutMenu(int id, Inventory inventory, BlockPos pos) {
        super(SkillMenus.LOADOUT.get(), id);
        this.pos = pos.immutable();
        IItemHandler bound = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        IItemHandler items = bound == null ? new ItemStackHandler(LOADOUT_SLOTS) : bound;
        addSlot(new SlotItemHandler(items, 0, 173, 168) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Techniques.book(stack).filter(book -> !book.definition().name().isEmpty()).isPresent();
            }
        });
        addSkillSlot(items, 1, 167, 206, SkillKind.MOVEMENT);
        addSkillSlot(items, 2, 167, 226, SkillKind.FLIGHT);
        addSkillSlot(items, 3, 296, 206, SkillKind.SPELL);
        addSkillSlot(items, 4, 316, 206, SkillKind.SPELL);
        addSkillSlot(items, 5, 296, 226, SkillKind.SPELL);
        addSkillSlot(items, 6, 316, 226, SkillKind.SPELL);
        addSkillSlot(items, 7, 234, 206, SkillKind.WEAPON_ART);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    private void addSkillSlot(IItemHandler items, int index, int x, int y, SkillKind kind) {
        addSlot(new SlotItemHandler(items, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Skills.book(stack).filter(book -> book.kind() == kind && !book.name().isEmpty()).isPresent();
            }
        });
    }

    /** 两刻后把持久装配栈按数量一载入当时的菜单，共享引用保留开窗期间的书籍成长。 */
    public void initialize(ServerPlayer player) {
        Techniques.setSlot(player, Techniques.slot(player).copyWithCount(1));
        slots.get(0).set(Techniques.slot(player));
        ItemStack movement = Skills.movement(player);
        movement.setCount(1);
        Skills.setMovement(player, movement);
        slots.get(1).set(movement);
        ItemStack flight = Skills.flight(player);
        flight.setCount(1);
        Skills.setFlight(player, flight);
        slots.get(2).set(flight);
        ItemStack weaponArt = Skills.weaponArt(player);
        weaponArt.setCount(1);
        Skills.setWeaponArt(player, weaponArt);
        slots.get(7).set(weaponArt);
        loadPage(player);
    }

    private void savePage(ServerPlayer player) {
        int start = (Skills.state(player).page() - 1) * SkillState.PAGE_SIZE;
        for (int index = 0; index < SkillState.PAGE_SIZE; index++) {
            Skills.setSpell(player, start + index + 1, slots.get(index + 3).getItem().copy());
        }
    }

    private void loadPage(ServerPlayer player) {
        int start = (Skills.state(player).page() - 1) * SkillState.PAGE_SIZE;
        for (int index = 0; index < SkillState.PAGE_SIZE; index++) {
            ItemStack spell = Skills.spell(player, start + index + 1);
            spell.setCount(1);
            Skills.setSpell(player, start + index + 1, spell);
            slots.get(index + 3).set(spell);
        }
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        switch (button) {
            case 0, 1 -> {
                savePage(serverPlayer);
                Skills.turnPage(serverPlayer, button == 0 ? -1 : 1);
                loadPage(serverPlayer);
            }
            case 2 -> NeoForge.EVENT_BUS.post(new SkillPageEvent(serverPlayer, 1, pos));
            case 4 -> NeoForge.EVENT_BUS.post(new SkillPageEvent(serverPlayer, 3, pos));
            case 7 -> HELP.forEach(line -> serverPlayer.displayClientMessage(Component.literal(line), false));
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < LOADOUT_SLOTS) {
            if (!moveItemStackTo(stack, LOADOUT_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, LOADOUT_SLOTS, false)) {
            if (index < LOADOUT_SLOTS + 27) {
                if (!moveItemStackTo(stack, LOADOUT_SLOTS + 27, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, LOADOUT_SLOTS, LOADOUT_SLOTS + 27, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有堆叠时仍以槽内物品检查过滤，空槽阶段只向第一个可接收槽移动。 */
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int step = reverse ? -1 : 1;
        if (stack.isStackable()) {
            for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
                Slot slot = slots.get(index);
                ItemStack existing = slot.getItem();
                if (!slot.mayPlace(existing) || existing.isEmpty() || !ItemStack.isSameItemSameComponents(stack, existing)) continue;
                int count = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount());
                if (count > 0) {
                    stack.shrink(count);
                    existing.grow(count);
                    slot.set(existing);
                    moved = true;
                }
            }
        }
        for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
            Slot slot = slots.get(index);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                slot.set(stack.split(Math.min(stack.getCount(), slot.getMaxStackSize())));
                slot.setChanged();
                moved = true;
                break;
            }
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** 八个自定义槽均由装配字段接收；死亡、断开与普通关闭都不再次返还或掉落。 */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            Techniques.setSlot(serverPlayer, slots.get(0).getItem());
            Skills.setMovement(serverPlayer, slots.get(1).getItem().copy());
            Skills.setFlight(serverPlayer, slots.get(2).getItem().copy());
            Skills.setWeaponArt(serverPlayer, slots.get(7).getItem().copy());
            savePage(serverPlayer);
        }
    }
}
