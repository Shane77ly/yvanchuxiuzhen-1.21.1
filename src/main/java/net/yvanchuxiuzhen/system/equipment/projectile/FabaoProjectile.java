package net.yvanchuxiuzhen.system.equipment.projectile;

import java.util.Arrays;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.data.FabaoTempData;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 一至八号法宝弹体；原生箭推进后冻结临时法宝，命中按初始化状态重建回收物品。 */
public final class FabaoProjectile extends AbstractArrow implements ItemSupplier {
    public enum Kind {
        FABAO_001("projectile_fabao_001", "fabao_001", 40, 2, .1, 1, false),
        FABAO_002("projectile_fabao_002", "fabao_002", 40, 2, .1, 1, false),
        FABAO_003("projectile_fabao_003", "fabao_003", 40, 2, .1, 0, false),
        FABAO_004("projectile_fabao_004", "fabao_004", 0, 3, 4, 1, true),
        FABAO_005("projectile_fabao_005", "fabao_005", 10, 4, .1, 0, true),
        FABAO_006("projectile_fabao_006", "fabao_006", 20, 4, .1, 0, true),
        FABAO_007("projectile_fabao_007", "fabao_007", 30, 4, .1, 0, true),
        FABAO_008("projectile_fabao_008", "fabao_008", 40, 4, .1, 0, true);

        private final String entityId;
        private final String itemId;
        private final int cooldown;
        private final float speed;
        private final double baseDamage;
        private final int knockback;
        private final boolean autoRelease;

        Kind(String entityId, String itemId, int cooldown, float speed, double baseDamage, int knockback, boolean autoRelease) {
            this.entityId = entityId;
            this.itemId = itemId;
            this.cooldown = cooldown;
            this.speed = speed;
            this.baseDamage = baseDamage;
            this.knockback = knockback;
            this.autoRelease = autoRelease;
        }

        public String entityId() { return entityId; }
        public String itemId() { return itemId; }
        public int cooldown() { return cooldown; }
        public float speed() { return speed; }
        public double baseDamage() { return baseDamage; }
        public int knockback() { return knockback; }
        public boolean autoRelease() { return autoRelease; }
    }

    private static final String[] ENCHANTMENTS = {"fluorescence", "beepoison", "firepoison", "redrefining", "spore", "sinomeninevine", "coldtoxin", "cautery", "snakevenom"};
    private static final String[] POISON_EFFECTS = {"", "duyao_10", "duyao_16", "duyao_13", "duyao_17", "duyao_11", "duyao_12", "duyao_14", "duyao_15"};
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yvanchunpc"));
    private static final TagKey<EntityType<?>> OLD_NPC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jiunpc"));
    private final Kind kind;
    private boolean initialized;
    private ItemStack refinement = ItemStack.EMPTY;
    private final int[] poison = new int[9];

    public FabaoProjectile(EntityType<? extends FabaoProjectile> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        setBaseDamage(kind.baseDamage());
        setCritArrow(false);
        setSilent(true);
    }

    public static FabaoProjectile shoot(ServerLevel level, LivingEntity owner, Kind kind) {
        FabaoProjectile projectile = EquipmentSetup.fabaoProjectileType(kind).get().create(level);
        projectile.setOwner(owner);
        projectile.setPos(owner.getX(), owner.getEyeY() - .1, owner.getZ());
        Vec3 direction = owner.getViewVector(1);
        projectile.shoot(direction.x, direction.y, direction.z, kind.speed(), 0);
        level.addFreshEntity(projectile);
        return projectile;
    }

    private ItemStack newItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, kind.itemId())));
    }

    @Override
    public ItemStack getItem() {
        return kind.ordinal() < Kind.FABAO_005.ordinal() ? newItem() : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        return newItem();
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && !initialized && getOwner() != null && kind != Kind.FABAO_004) {
            FabaoTempData temporary = temporaryItem(getOwner());
            ItemStack source = temporary.item();
            refinement = newItem();
            if (source.has(EquipmentData.STATS.get())) refinement.set(EquipmentData.STATS.get(), source.get(EquipmentData.STATS.get()));
            if (source.has(EquipmentData.PRODUCT.get())) refinement.set(EquipmentData.PRODUCT.get(), source.get(EquipmentData.PRODUCT.get()));
            if (source.has(EquipmentData.REFINEMENT.get())) refinement.set(EquipmentData.REFINEMENT.get(), source.get(EquipmentData.REFINEMENT.get()));
            CompoundTag sourceData = source.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (sourceData.contains("lianqizong")) {
                CustomData.update(DataComponents.CUSTOM_DATA, refinement, data -> data.put("lianqizong", sourceData.getCompound("lianqizong").copy()));
            }
            Component name = temporary.consumed() ? source.getOrDefault(DataComponents.CUSTOM_NAME, Items.AIR.getName(ItemStack.EMPTY)) : source.getDisplayName();
            refinement.set(DataComponents.CUSTOM_NAME, Component.literal(name.getString().replace("[", "").replace("]", "")));
            initialized = true;
            if (!temporary.consumed() && (kind == Kind.FABAO_003 || kind.ordinal() >= Kind.FABAO_005.ordinal())) {
                for (int i = 0; i < poison.length; i++) {
                    poison[i] = enchantmentLevel(source, i);
                    if (kind == Kind.FABAO_003 && poison[i] != 0) break;
                }
            }
        }
        if (inGround) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel level) || getOwner() == null) return;
        Entity target = result.getEntity();
        if (kind == Kind.FABAO_003) {
            FabaoPoisonFields.start(level, position().add(.5, 0, .5), this, getOwner());
            return;
        }
        if (kind == Kind.FABAO_004) {
            if (!Relations.isFriendly(getOwner(), target) && target instanceof LivingEntity living) {
                randomEffect(living, "effect_007", .05, 1200);
            }
            return;
        }
        hitParticles(level);
        if (!(target instanceof Mob || target instanceof Player)) {
            target.discard();
            return;
        }
        boolean allowed = !target.getType().is(NPC) && !target.getType().is(OLD_NPC);
        ItemStack returned = allowed ? returnedItem(target, false) : ItemStack.EMPTY;
        boolean earlyDrop = kind == Kind.FABAO_001 || kind == Kind.FABAO_002;
        if (allowed && earlyDrop) drop(level, returned, getX(), getY(), getZ());
        if ((allowed || earlyDrop) && !Relations.isFriendly(getOwner(), target)) {
            hitLiving(level, (LivingEntity) target, returned);
        }
        if (allowed && !earlyDrop) drop(level, returned, getX(), getY(), getZ());
    }

    private void hitLiving(ServerLevel level, LivingEntity target, ItemStack returned) {
        double damage = returned.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY).physicalAttack();
        switch (kind) {
            case FABAO_001, FABAO_002 -> {
                double chance = kind == Kind.FABAO_001 ? .1 : .2;
                randomEffect(target, "effect_001", chance, 100);
                randomEffect(target, "effect_002", chance, 100);
                if (kind == Kind.FABAO_002 && random.nextDouble() < .1) damage *= 1.5;
                hitDamage(target, DamageTypes.FALLING_BLOCK, damage);
            }
            case FABAO_005 -> {
                hitDamage(target, DamageTypes.FALLING_BLOCK, damage);
                if (randomEffect(target, "effect_004", .1, 1200)) {
                    level.sendParticles(Particles.type("smokered"), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 10, 0, 0, 0, .5);
                }
                applyPoison(target, initialized ? poison : poisonLevels(returned));
            }
            case FABAO_006 -> {
                if (!initialized) hitDamage(target, DamageTypes.FALLING_BLOCK, damage);
                hitDamage(target, DamageTypes.GENERIC, damage * (random.nextDouble() < .1 ? 1.5 : 1));
                applyPoison(target, initialized ? poison : poisonLevels(returned));
            }
            case FABAO_007, FABAO_008 -> {
                hitDamage(target, initialized ? DamageTypes.GENERIC : DamageTypes.FALLING_BLOCK, damage);
                if (kind == Kind.FABAO_008) randomEffect(target, "effect_003", .1, 1200);
                applyPoison(target, initialized ? poison : poisonLevels(returned));
            }
            default -> throw new IllegalStateException("不使用回收命中路径的法宝：" + kind);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(level() instanceof ServerLevel level) || getOwner() == null) return;
        if (kind == Kind.FABAO_003) {
            FabaoPoisonFields.start(level, new Vec3(result.getBlockPos().getX() + .5, result.getBlockPos().getY(), result.getBlockPos().getZ() + .5), this, getOwner());
        } else if (kind != Kind.FABAO_004) {
            drop(level, returnedItem(getOwner(), true), result.getBlockPos().getX(), result.getBlockPos().getY() + 1, result.getBlockPos().getZ());
            if (kind == Kind.FABAO_001 || kind == Kind.FABAO_002) playHitSound(level);
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 10, 0, 0, 0, .1);
        }
    }

    private ItemStack returnedItem(Entity fallbackOwner, boolean blockHit) {
        ItemStack returned = newItem();
        if (initialized) {
            returned.applyComponents(refinement.getComponentsPatch());
            if (blockHit) {
                if (returned.has(EquipmentData.REFINEMENT.get())) {
                    CustomData.update(EquipmentData.REFINEMENT.get(), returned, data -> data.putBoolean("tebiefabao", true));
                }
                CustomData.update(DataComponents.CUSTOM_DATA, returned, data -> {
                    CompoundTag lianqi = data.getCompound("lianqizong");
                    lianqi.putBoolean("tebiefabao", true);
                    data.put("lianqizong", lianqi);
                });
                if (kind.ordinal() >= Kind.FABAO_005.ordinal()) {
                    for (int i = 0; i < poison.length; i++) {
                        if (poison[i] != 0) returned.enchant(level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchants.key(ENCHANTMENTS[i])), poison[i]);
                    }
                }
            }
        } else {
            returned.applyComponents(temporaryItem(fallbackOwner).item().getComponentsPatch());
        }
        return returned;
    }

    private static FabaoTempData temporaryItem(Entity entity) {
        return entity instanceof Player player ? player.getData(EquipmentData.FABAO_TEMP.get()) : FabaoTempData.EMPTY;
    }

    private int[] poisonLevels(ItemStack stack) {
        int[] levels = new int[ENCHANTMENTS.length];
        for (int i = 0; i < levels.length; i++) levels[i] = enchantmentLevel(stack, i);
        return levels;
    }

    private int enchantmentLevel(ItemStack stack, int index) {
        return stack.getEnchantmentLevel(level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchants.key(ENCHANTMENTS[index])));
    }

    void applyFieldPoison(LivingEntity target) {
        applyPoison(target, poison);
    }

    private static void applyPoison(LivingEntity target, int[] levels) {
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != 0) {
                if (i == 0) target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, levels[i] - 1));
                else StatusEffects.add(target, POISON_EFFECTS[i], 1200, levels[i] - 1);
            }
        }
    }

    private void hitParticles(ServerLevel level) {
        if (kind == Kind.FABAO_001 || kind == Kind.FABAO_002) {
            playHitSound(level);
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 10, 0, 0, 0, .1);
        } else {
            for (int i = 0; i < (kind == Kind.FABAO_008 ? 2 : 1); i++) {
                level.sendParticles(Particles.type("xvedi"), getX(), getY(), getZ(), 10, 0, 0, 0, .5);
            }
        }
    }

    private void playHitSound(ServerLevel level) {
        level.playSound(null, blockPosition(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao001")), SoundSource.NEUTRAL, 1, 1);
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        if (kind.knockback() <= 0) return;
        double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(kind.knockback() * .6 * resistance);
        if (push.lengthSqr() > 0) target.push(push.x, .1, push.z);
    }

    private void hitDamage(LivingEntity target, ResourceKey<DamageType> type, double amount) {
        target.hurt(target.damageSources().source(type, this, getOwner()), (float) amount);
    }

    private boolean randomEffect(LivingEntity target, String effect, double chance, int duration) {
        if (random.nextDouble() >= chance) return false;
        StatusEffects.add(target, effect, duration, 0);
        return true;
    }

    private static void drop(ServerLevel level, ItemStack stack, double x, double y, double z) {
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("fabao_initialized", initialized);
        if (!refinement.isEmpty()) tag.put("fabao_refinement", refinement.save(level().registryAccess()));
        tag.putIntArray("fabao_poison", poison);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        initialized = tag.getBoolean("fabao_initialized");
        refinement = ItemStack.parseOptional(level().registryAccess(), tag.getCompound("fabao_refinement"));
        int[] levels = tag.getIntArray("fabao_poison");
        Arrays.fill(poison, 0);
        System.arraycopy(levels, 0, poison, 0, Math.min(levels.length, poison.length));
    }
}
