package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 商人及大管家的来源实例生命周期；交易、对话与玩家档案由各数据所有者维护。 */
public class NpcMob extends PathfinderMob {
    private final CompoundTag profile;

    public NpcMob(EntityType<? extends NpcMob> type, Level level, CompoundTag profile) {
        super(type, level);
        this.profile = profile;
        xpReward = 0;
        setPersistenceRequired();
        if (kind().equals("businessman_09") || kind().equals("businessman_10")) {
            boolean ore = kind().equals("businessman_09");
            equip(EquipmentSlot.MAINHAND, ore ? "cailiao_201raw" : "cailiao_031");
            equipClothes(ore ? "clothes_20" : "clothes_18");
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }

    protected final String kind() { return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath(); }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6));
        if (kind().equals("daguanjia")) {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {
                @Override protected boolean canPerformAttack(LivingEntity target) {
                    return isTimeToAttack() && distanceToSqr(target) <= getBbWidth() * getBbWidth() + target.getBbWidth();
                }
            });
            targetSelector.addGoal(3, new HurtByTargetGoal(this));
            goalSelector.addGoal(4, new RandomLookAroundGoal(this));
            goalSelector.addGoal(5, new FloatGoal(this));
        } else {
            goalSelector.addGoal(2, new RandomLookAroundGoal(this));
            goalSelector.addGoal(3, new FloatGoal(this));
        }
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public boolean hurt(DamageSource source, float amount) {
        sourceHurt();
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)) return false;
        return super.hurt(source, amount);
    }

    protected void sourceHurt() { }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        CompoundTag state = getPersistentData();
        if (state.getString("LEVELname").isEmpty()) {
            if (state.getDouble("LEVELnumber") < 1) state.putDouble("LEVELnumber", profile.getDouble("level"));
            state.merge(profile.getCompound("state"));
            CompoundTag localized = profile.getCompound("localized");
            for (String key : localized.getAllKeys()) state.putString(key, Component.translatable(localized.getString(key)).getString());
            NpcStats.calculate(this);
            String team = BuiltInRegistries.ENTITY_TYPE.getKey(getType()).toString().replace(':', '_');
            Relations.setTeam(this, team);
            Relations.addFriendlyTeam(this, team);
            if (kind().equals("businessman_11")) equipClothes("clothes_19");
            if (refreshesShop()) setRefreshScore();
        }
        return result;
    }

    @Override public void baseTick() {
        super.baseTick();
        if (level().isClientSide()) return;
        CompoundTag data = getPersistentData();
        if (refreshesShop() && (data.getDouble("shopshuaxintick") + 24000 <= level().getDayTime()
                || data.getDouble("shopshuaxintick") > level().getDayTime())) {
            setRefreshScore();
            data.putDouble("shopshuaxintick", level().getDayTime());
        }
        if (kind().equals("businessman_04") && isAlive()) {
            data.putDouble("entitytick", data.getDouble("entitytick") + 1);
            if (data.getDouble("entitytick") >= (getTarget() == null ? 20 : 100)) data.putDouble("entitytick", 0);
        }
    }

    private boolean refreshesShop() {
        return kind().equals("businessman_03") || kind().equals("businessman_10") || kind().equals("businessman_11");
    }

    private void setRefreshScore() {
        Scoreboard scoreboard = level().getScoreboard();
        Objective objective = scoreboard.getObjective("keyishuaxin");
        if (objective == null) objective = scoreboard.addObjective("keyishuaxin", ObjectiveCriteria.DUMMY,
                Component.literal("keyishuaxin"), ObjectiveCriteria.RenderType.INTEGER, false, null);
        scoreboard.getOrCreatePlayerScore(this, objective).set(1);
    }

    private void equipClothes(String clothes) {
        equip(EquipmentSlot.HEAD, clothes + "_helmet");
        equip(EquipmentSlot.CHEST, clothes + "_chestplate");
        equip(EquipmentSlot.LEGS, clothes + "_leggings");
        equip(EquipmentSlot.FEET, clothes + "_boots");
    }

    private void equip(EquipmentSlot slot, String id) {
        setItemSlot(slot, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))));
    }
}
