package net.yvanchuxiuzhen.system.combat.content;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.yvanchuxiuzhen.platform.ModId;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 木人桩（来源 {@code MurenzhuangEntity}）：练习用的动画实体，1024 点生命，无 AI、不受推挤，不接受药水云、跌落、仙人掌、
 * 溺水、凋零的伤害。每次受击先按战利品表 {@code entities/suijizhanlipin} 随机取一件材料选择受击动画
 * （{@code cailiao_191}–{@code 199} 对应 attacked01–05），下一刻向攻击的玩家报告这次承受的伤害并回满生命；
 * 潜行右键打开设置界面；死亡 20 刻后掉出一个木人桩物品。
 */
public final class WoodenDummyEntity extends PathfinderMob implements GeoEntity {
    private static final ResourceKey<LootTable> HIT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "entities/suijizhanlipin"));
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.murenzhuang.idle");
    private static final double HEALTH = 1024;

    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    /** 受击后下一刻要接收伤害报告的攻击者；没有玩家时为空。 */
    private UUID reportTo;
    private boolean resetPending;

    public WoodenDummyEntity(EntityType<? extends WoodenDummyEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        react(source.getEntity());
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud) {
            return false;
        }
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    /** 受击反应：有攻击者时选动画并安排下一刻的伤害报告与回血（来源的受击过程要求有攻击者）。 */
    private void react(Entity attacker) {
        if (attacker == null || !(level() instanceof ServerLevel server)) {
            return;
        }
        List<ItemStack> drawn = server.getServer().reloadableRegistries().getLootTable(HIT_TABLE).getRandomItems(new LootParams.Builder(server).create(LootContextParamSets.EMPTY));
        ItemStack picked = drawn.get(random.nextInt(drawn.size()));
        triggerAnim("main", "attacked0" + animationNumber(BuiltInRegistries.ITEM.getKey(picked.getItem()).getPath()));
        reportTo = attacker instanceof Player player ? player.getUUID() : null;
        resetPending = true;
    }

    /** 材料对应的受击动画序号：191、192 → 1，193、194 → 2，195、196 → 3，197、198 → 4，其余 → 5。 */
    private static int animationNumber(String material) {
        return switch (material) {
            case "cailiao_191", "cailiao_192" -> 1;
            case "cailiao_193", "cailiao_194" -> 2;
            case "cailiao_195", "cailiao_196" -> 3;
            case "cailiao_197", "cailiao_198" -> 4;
            default -> 5;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (resetPending && level() instanceof ServerLevel server) {
            if (reportTo != null && server.getPlayerByUUID(reportTo) instanceof Player player) {
                player.displayClientMessage(Component.literal("伤害：" + (getMaxHealth() - getHealth())), false);
            }
            setHealth((float) HEALTH);
            reportTo = null;
            resetPending = false;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        if (!level().isClientSide() && player.isShiftKeyDown()) {
            DummyMenu.open(player, this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == 20) {
            remove(RemovalReason.KILLED);
            ItemEntity drop = new ItemEntity(level(), getX(), getY() + 1, getZ(), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "murenzhuangitem"))));
            drop.setPickUpDelay(10);
            level().addFreshEntity(drop);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<WoodenDummyEntity> main = new AnimationController<>(this, "main", 0, state -> state.setAndContinue(IDLE));
        for (int number = 1; number <= 5; number++) {
            main.triggerableAnim("attacked0" + number, RawAnimation.begin().thenPlay("animation.murenzhuang.attacked0" + number));
        }
        controllers.add(main);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
