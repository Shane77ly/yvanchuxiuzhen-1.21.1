package net.yvanchuxiuzhen.system.skill.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * 玩家的技能装配与选择状态（附件 {@code skill_state}）。技能栏 {@link #SPELL_SLOTS} 槽分四页，每页四槽
 * （来源 {@code jineng01}–{@code jineng16}、{@code jinengweizhi}），另有身法 {@code qinggong}、轻功 {@code flyjineng}、
 * 武器技 {@code wuqiskill} 各一槽。当前选择：{@code selected} 是所选技能书（{@code jinengxvanze}），{@code selectedKey} 是选择它的
 * 按键序号 1–4（{@code jinengxvanzeshuzhi}），{@code selectedSlot} 是它在技能栏的序号 1–16（{@code jinengshijixvanze}，0 表示没有），
 * {@code combo} 是连招开关（{@code jinenglianzhao}）。值不可变，修改时生成新值写回附件。
 */
public record SkillState(List<ItemStack> spells, ItemStack movement, ItemStack flight, ItemStack weaponArt, int page, ItemStack selected,
                         int selectedKey, int selectedSlot, boolean combo) {
    public static final int SPELL_SLOTS = 16;
    public static final int PAGE_SIZE = 4;
    public static final SkillState EMPTY = new SkillState(Collections.nCopies(SPELL_SLOTS, ItemStack.EMPTY), ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, 1, ItemStack.EMPTY, 0, 0, false);

    public static final Codec<SkillState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf(SPELL_SLOTS, SPELL_SLOTS).fieldOf("spells").forGetter(SkillState::spells),
            ItemStack.OPTIONAL_CODEC.fieldOf("movement").forGetter(SkillState::movement),
            ItemStack.OPTIONAL_CODEC.fieldOf("flight").forGetter(SkillState::flight),
            ItemStack.OPTIONAL_CODEC.fieldOf("weapon_art").forGetter(SkillState::weaponArt),
            Codec.INT.fieldOf("page").forGetter(SkillState::page),
            ItemStack.OPTIONAL_CODEC.fieldOf("selected").forGetter(SkillState::selected),
            Codec.INT.fieldOf("selected_key").forGetter(SkillState::selectedKey),
            Codec.INT.fieldOf("selected_slot").forGetter(SkillState::selectedSlot),
            Codec.BOOL.fieldOf("combo").forGetter(SkillState::combo)
    ).apply(instance, SkillState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillState> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    /** 第 {@code slot}（1–16）个技能槽中的书。 */
    public ItemStack spell(int slot) {
        return spells.get(slot - 1);
    }

    public SkillState withSpell(int slot, ItemStack stack) {
        List<ItemStack> next = new ArrayList<>(spells);
        next.set(slot - 1, stack);
        return new SkillState(next, movement, flight, weaponArt, page, selected, selectedKey, selectedSlot, combo);
    }

    public SkillState withMovement(ItemStack stack) {
        return new SkillState(spells, stack, flight, weaponArt, page, selected, selectedKey, selectedSlot, combo);
    }

    public SkillState withFlight(ItemStack stack) {
        return new SkillState(spells, movement, stack, weaponArt, page, selected, selectedKey, selectedSlot, combo);
    }

    public SkillState withWeaponArt(ItemStack stack) {
        return new SkillState(spells, movement, flight, stack, page, selected, selectedKey, selectedSlot, combo);
    }

    public SkillState withPage(int next) {
        return new SkillState(spells, movement, flight, weaponArt, next, selected, selectedKey, selectedSlot, combo);
    }

    public SkillState withSelection(ItemStack stack, int key, int slot) {
        return new SkillState(spells, movement, flight, weaponArt, page, stack, key, slot, combo);
    }

    public SkillState withKey(int key) {
        return new SkillState(spells, movement, flight, weaponArt, page, selected, key, selectedSlot, combo);
    }

    public SkillState withCombo(boolean next) {
        return new SkillState(spells, movement, flight, weaponArt, page, selected, selectedKey, selectedSlot, next);
    }
}
