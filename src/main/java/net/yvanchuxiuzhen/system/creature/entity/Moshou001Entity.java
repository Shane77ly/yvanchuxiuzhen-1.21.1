package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;

/** 001 号魔兽；来源为节肢类 Monster，保留受击打断技能和 80 tick 死亡动画窗口。 */
public final class Moshou001Entity extends MoshouEntity {
    public Moshou001Entity(EntityType<? extends MoshouEntity> type, Level level) { super(type, level, "moshou001", 80); }
    @Override protected double meleeSpeed() { return 1.2; }
    @Override public SoundEvent getAmbientSound() { return SoundEvents.SPIDER_STEP; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(SoundEvents.SPIDER_AMBIENT, 0.15F, 1); }
    @Override protected void initializeSourceData() {
        CreatureSourceStats.initialize(this, 30, false,
                "强劲", "平庸", "平庸", "勉强", "贫弱", "贫弱", "勉强", "强劲", "勉强", "强劲", "贫弱", "平庸", "贫弱");
    }
    @Override protected void sourceTick() {
        if (isAlive() && getTarget() != null) {
            double tick = getPersistentData().getDouble("entitytick") + 1;
            if (tick >= 40) { setDeltaMovement(getLookAngle().x, .4, getLookAngle().z); tick = 0; }
            getPersistentData().putDouble("entitytick", tick);
        }
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        Item material = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_132"));
        if (player.getMainHandItem().is(material)) {
            getPersistentData().putBoolean("shengge", true);
            if (!level().isClientSide()) addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 1));
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(material), 1, player.inventoryMenu.getCraftSlots());
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override protected void onSourceDeath() {
        if (!(level() instanceof ServerLevel level)) return;
        if (getPersistentData().getBoolean("shengge")) {
            drop(level, "cailiao_17801"); drop(level, "cailiao_17801"); drop(level, "cailiao_17701");
        } else {
            for (int count = 0; count < 4; count++) drop(level, "cailiao_17701");
        }
    }

    private void drop(ServerLevel level, String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
        ItemEntity entity = new ItemEntity(level, getX(), getY() + 1.5, getZ(), new ItemStack(item));
        entity.setPickUpDelay(10); level.addFreshEntity(entity);
    }
}
