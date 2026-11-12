package net.yvanchuxiuzhen.system.equipment.projectile;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;
import net.yvanchuxiuzhen.system.combat.api.Enchants;

/** 开灵附魔右键产生的无拾取弹体；十六刻后或命中方块时生成对应工具机。 */
public final class KailingProjectile extends AbstractArrow implements ItemSupplier {
    public static final String ID = "projectile_gongjvshechu";

    private ToolMachineEntity.Kind initialKind;
    private int ticks;
    private int enchantLevel;
    private boolean initialized;
    private boolean spawned;

    public KailingProjectile(EntityType<? extends KailingProjectile> type, Level level) {
        this(type, level, ToolMachineEntity.Kind.AXE);
    }

    public KailingProjectile(EntityType<? extends KailingProjectile> type, Level level, ToolMachineEntity.Kind kind) {
        super(type, level);
        initialKind = kind;
        setBaseDamage(.1);
        setCritArrow(false);
        setSilent(true);
    }

    public static KailingProjectile shoot(ServerLevel level, LivingEntity owner, ToolMachineEntity.Kind kind) {
        KailingProjectile projectile = EquipmentSetup.kailingProjectileType().get().create(level);
        projectile.setOwner(owner);
        projectile.initialKind = kind;
        projectile.setPos(owner.getX(), owner.getEyeY() - .1, owner.getZ());
        projectile.shoot(owner.getLookAngle().x, owner.getLookAngle().y, owner.getLookAngle().z, 1, 0);
        level.addFreshEntity(projectile);
        return projectile;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_100")));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel level) {
            initialize();
            level.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            spawnMachine(level);
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            initialize();
            ticks++;
            if (ticks >= 16 && !spawned) {
                spawnMachine(level);
                discard();
            }
        }
        if (inGround) {
            discard();
        }
    }

    /** 来源飞行过程在首刻冻结主人、工具类别和开灵等级，后续不再读取主手。 */
    private void initialize() {
        if (initialized) return;
        Entity owner = getOwner();
        if (owner instanceof LivingEntity living) {
            initialKind = living.getMainHandItem().getItem() instanceof AxeItem ? ToolMachineEntity.Kind.AXE
                    : living.getMainHandItem().getItem() instanceof PickaxeItem ? ToolMachineEntity.Kind.PICKAXE
                    : living.getMainHandItem().getItem() instanceof HoeItem ? ToolMachineEntity.Kind.HOE
                    : living.getMainHandItem().getItem() instanceof ShovelItem ? ToolMachineEntity.Kind.SHOVEL : initialKind;
            enchantLevel = Enchants.level(living, living.getMainHandItem(), Enchants.key("kailing"));
        }
        initialized = true;
        setNoGravity(true);
    }

    private void spawnMachine(ServerLevel level) {
        if (spawned) return;
        spawned = true;
        ToolMachineEntity.spawn(level, getOwner(), initialKind, enchantLevel, position(), ToolMachineEntity.face(level, BlockPos.containing(position())));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("kailing_ticks", ticks);
        tag.putInt("kailing_enchant", enchantLevel);
        tag.putString("kailing_kind", initialKind.id());
        tag.putBoolean("kailing_initialized", initialized);
        tag.putBoolean("kailing_spawned", spawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ticks = tag.getInt("kailing_ticks");
        enchantLevel = tag.getInt("kailing_enchant");
        initialKind = Arrays.stream(ToolMachineEntity.Kind.values())
                .filter(kind -> kind.id().equals(tag.getString("kailing_kind"))).findFirst().orElse(ToolMachineEntity.Kind.AXE);
        initialized = tag.getBoolean("kailing_initialized");
        spawned = tag.getBoolean("kailing_spawned");
    }
}
