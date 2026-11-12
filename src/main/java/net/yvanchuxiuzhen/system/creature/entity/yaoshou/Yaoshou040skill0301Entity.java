package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** 040skill0301游离光箭型弹体：追踪、范围命中、方块命中与来源寿命。 */
public final class Yaoshou040skill0301Entity extends AbstractArrow implements ItemSupplier {
    public Yaoshou040skill0301Entity(EntityType<? extends Yaoshou040skill0301Entity> type, Level level) { super(type, level); }
    @Override public ItemStack getItem() {
        return ItemStack.EMPTY;
    }
    @Override public byte getPierceLevel() { return 1; }
    @Override protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou_040skill_0301")));
    }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }
    @Override protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Yaoshou040skill0301DangTouZhiWuJiZhongShiTiShi.execute(level(), this, getOwner());
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Yaoshou040skill0301DangTouSheWuSheZhongCiFangKuai.execute(level(), getOwner(), this);
    }
    @Override public void tick() {
        super.tick();
        Yaoshou040skill0301TouZhiWuFeiXingTick.execute(level(), getOwner(), this);
        if (inGround) discard();
    }
}
