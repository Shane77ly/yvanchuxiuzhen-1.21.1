package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.yvanchuxiuzhen.system.combat.api.DamageImmunityEvent;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.LethalDamageEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.combat.damage.CombatDamageTypes;

/** 同一生存玩家使用原版盾牌，从固定正面比较物理与两种项目伤害源。 */
final class SpellShieldCheck extends SystemCheck {
    static final String SELECTOR = "q235spellshield";
    private static final float DAMAGE = 4;
    private static final int TIMEOUT = 200;

    private enum Trial { PHYSICAL, MANA, SOUL, WARD_FRONT, WARD_BACK }

    private record Shield(boolean originalBlock, boolean blocked, boolean canceled, float candidateDamage, float shieldDamage) {}

    private record Settled(float damage, float blockedDamage) {}

    private final Consumer<LivingIncomingDamageEvent> incoming = this::incoming;
    private final Consumer<LivingShieldBlockEvent> shield = this::shield;
    private final Consumer<LivingDamageEvent.Post> settled = this::settled;
    private final Consumer<LethalDamageEvent> lethal = this::observeWard;
    private float wardHealth;
    private double wardAmount;
    private final List<String> order = new ArrayList<>();
    private ServerPlayer defender;
    private Cow attacker;
    private Vec3 defenderPosition;
    private int readyAt;
    private DamageSource active;
    private float incomingAmount;
    private Shield block;
    private Settled resolution;

    SpellShieldCheck(List<String> report, String levelId) {
        super("Q235 原生盾牌正面对照", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            if (!minecraft.player.onGround()) {
                if (local > TIMEOUT) stop("客户端未在超平坦地面就绪");
                return false;
            }
            String[] error = new String[1];
            onServer(player -> error[0] = prepare(player));
            if (error[0] != null) {
                stop(error[0]);
                return false;
            }
            return true;
        }
        if (step == 1) {
            if (!minecraft.player.getOffhandItem().is(Items.SHIELD) || !minecraft.player.getMainHandItem().isEmpty()) {
                if (local > TIMEOUT) stop("未收到副手原版盾牌");
                return false;
            }
            minecraft.options.keyUse.setDown(true);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.OFF_HAND);
            return true;
        }
        Trial trial = Trial.values()[step - 2];
        boolean[] ready = new boolean[1];
        boolean[] passed = new boolean[1];
        if (minecraft.player.isBlocking() && minecraft.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            onServer(player -> {
                ready[0] = player == defender && player.isBlocking() && player.getUsedItemHand() == InteractionHand.OFF_HAND
                        && player.getUseItem().is(Items.SHIELD) && player.invulnerableTime == 0
                        && player.getServer().getTickCount() >= readyAt;
                if (ready[0]) passed[0] = switch (trial) {
                    case WARD_FRONT, WARD_BACK -> wardHit(trial);
                    default -> hit(trial);
                };
            });
        }
        if (!ready[0]) {
            if (local > TIMEOUT) stop(trial + " 未等到双端原生举盾或伤害保护结束");
            return false;
        }
        if (trial == Trial.PHYSICAL && !passed[0]) {
            stop("物理正例未通过，不将后续灵力或神识输出用于方向对照");
        } else if (trial == Trial.WARD_FRONT && !passed[0]) {
            stop("全挡保符对照失败，不继续使用此玩家测试背击");
        } else if (trial == Trial.WARD_BACK) {
            stop(null);
        }
        return true;
    }

    private String prepare(ServerPlayer player) {
        if (!player.onGround() || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
                || player.getArmorValue() != 0 || player.getAbsorptionAmount() != 0 || player.getHealth() <= 3 * DAMAGE
                || !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return "需要已落地、无护甲和吸收、双手空且生命充足的新生存玩家";
        }
        defenderPosition = Vec3.atBottomCenterOf(player.blockPosition());
        Vec3 origin = defenderPosition.add(0, 0, 3);
        Cow source = EntityType.COW.create(player.serverLevel());
        source.moveTo(origin.x, origin.y, origin.z, 180, 0);
        source.setNoAi(true);
        if (!player.serverLevel().addFreshEntity(source)) return "受控伤害来源未进入世界";
        attacker = source;
        defender = player;
        defender.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        // ServerPlayer 登录保护为 60 刻，按服务端刻等待，不写保护字段。
        readyAt = player.getServer().getTickCount() + 65;
        aim();
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, shield);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, settled);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, lethal);
        for (boolean soul : List.of(false, true)) {
            observeTags(CombatDamageTypes.source(player.level(), soul, null), true);
            observeTags(CombatDamageTypes.source(player.level(), soul, attacker), true);
        }
        return null;
    }

    private void aim() {
        defender.setDeltaMovement(Vec3.ZERO);
        defender.connection.teleport(defenderPosition.x, defenderPosition.y, defenderPosition.z, 0, 0);
        defender.setYHeadRot(0);
    }

    private boolean hit(Trial trial) {
        boolean physical = trial == Trial.PHYSICAL;
        DamageSource source = physical ? defender.damageSources().mobAttack(attacker)
                : CombatDamageTypes.source(defender.level(), trial == Trial.SOUL, attacker);
        if (physical && !observeTags(source, false)) return false;
        Vec3 sourcePosition = source.getSourcePosition();
        if (sourcePosition == null || source.getDirectEntity() != attacker || source.getEntity() != attacker) {
            fail(trial + " 没有同一直接与归属来源");
            return false;
        }
        Vec3 playerPosition = defender.position();
        Vec3 delta = playerPosition.subtract(sourcePosition);
        double dot = new Vec3(delta.x, 0, delta.z).normalize().dot(Vec3.directionFromRotation(0, defender.getYHeadRot()));
        if (Math.abs(delta.x) > 0.01 || Math.abs(delta.y) > 0.01 || Math.abs(delta.z + 3) > 0.01 || dot > -0.999
                || defender.getHealth() <= DAMAGE || defender.getAbsorptionAmount() != 0 || defender.getArmorValue() != 0) {
            fail(trial + " 对照前置不符：源=" + sourcePosition + "，玩家=" + defender.position() + "，dot=" + dot);
            return false;
        }
        float health = defender.getHealth();
        int wear = defender.getOffhandItem().getDamageValue();
        order.clear();
        incomingAmount = 0;
        block = null;
        resolution = null;
        active = source;
        // 固定输入隔离伤害公式入口 /shanghai 与施放前置；受伤监听、盾挡和扣耐久仍走原生 ServerPlayer.hurt。
        boolean hurt;
        try {
            hurt = defender.hurt(source, DAMAGE);
        } finally {
            active = null;
        }
        float lostHealth = health - defender.getHealth();
        int addedWear = defender.getOffhandItem().getDamageValue() - wear;
        boolean eventPath = order.equals(List.of("incoming", "shield", "settled")) && incomingAmount >= 3
                && block != null && !block.canceled() && resolution != null;
        boolean shieldResult = eventPath && block.originalBlock() == physical && block.blocked() == physical
                && (physical ? resolution.blockedDamage() == incomingAmount : resolution.blockedDamage() == 0);
        boolean healthResult = resolution != null && Math.abs(lostHealth - resolution.damage()) < 1e-5
                && (physical ? lostHealth == 0 : lostHealth > 0);
        boolean wearResult = block != null && (physical ? addedWear == 1 + (int) block.shieldDamage() && addedWear > 0 : addedWear == 0);
        expectTrue(trial + " " + source.typeHolder().unwrapKey().orElseThrow().location() + " 事件顺序=" + order
                + "，输入=" + DAMAGE + "，有效输入=" + incomingAmount
                + "，玩家=" + playerPosition + "，来源=" + sourcePosition + "，dot=" + dot
                + "，盾已举 " + defender.getTicksUsingItem() + " 刻，hurt 返回=" + hurt, eventPath);
        expectTrue(trial + " 实际格挡：" + block + "，结算=" + resolution, shieldResult);
        expectTrue(trial + " 生命 " + health + " → " + defender.getHealth(), healthResult);
        expectTrue(trial + " 盾耐久损耗值 " + wear + " → " + defender.getOffhandItem().getDamageValue(), wearResult);
        readyAt = defender.getServer().getTickCount() + 25;
        if (trial != Trial.SOUL) aim();
        return shieldResult && healthResult && wearResult;
    }

    /** 在同一个服务端调用内采样，排除自然回血与下一刻副作用。 */
    private boolean wardHit(Trial trial) {
        boolean front = trial == Trial.WARD_FRONT;
        Item ward = BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:itemfulu_16"));
        if (front) {
            if (ward == Items.AIR || defender.getInventory().countItem(ward) != 0
                    || !defender.getInventory().add(new ItemStack(ward, 2))) {
                fail("残血对照需要两个新放入的替死符");
                return false;
            }
            attacker.setItemSlot(EquipmentSlot.MAINHAND, enchantedStick("vampire", 1));
        }
        defender.setHealth(2);
        float absorptionBefore = defender.getAbsorptionAmount();
        int chestWearBefore = defender.getItemBySlot(EquipmentSlot.CHEST).getDamageValue();
        if (!front) {
            defender.addEffect(new MobEffectInstance(StatusEffects.holder("effect_036"), 40));
            boolean equipped = defender.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE);
            float cap = defender.getMaxAbsorption();
            int armor = defender.getArmorValue();
            int wards = defender.getInventory().countItem(ward);
            boolean ready = cap >= DAMAGE && absorptionBefore == DAMAGE && equipped && armor >= 6 && chestWearBefore == 0
                    && wards == 2 && defender.getHealth() == 2 && defender.hasEffect(StatusEffects.holder("effect_036"));
            expectTrue("WARD_BACK 攻击前：吸收=" + absorptionBefore + "/" + cap + "，铁胸甲=" + equipped
                    + "，护甲值=" + armor + "，胸甲损耗=" + chestWearBefore + "，符数=" + wards + "，生命=" + defender.getHealth(), ready);
            if (!ready) return false;
        }
        attacker.setHealth(1);
        Vec3 direction = Vec3.directionFromRotation(0, defender.getYHeadRot()).scale(front ? 3 : -3);
        Vec3 pos = defender.position().add(direction);
        attacker.moveTo(pos.x, pos.y, pos.z, 0, 0);
        int wear = defender.getOffhandItem().getDamageValue();
        order.clear();
        block = null;
        resolution = null;
        wardAmount = Double.NaN;
        wardHealth = Float.NaN;
        Consumer<DamageImmunityEvent> stage = event -> {
            if (event.victim() == defender && event.attacker() == attacker) order.add("hurt-stage");
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, stage);
        active = defender.damageSources().mobAttack(attacker);
        try {
            defender.hurt(active, DAMAGE);
        } finally {
            active = null;
            NeoForge.EVENT_BUS.unregister(stage);
        }
        int loss = defender.getOffhandItem().getDamageValue() - wear;
        int count = defender.getInventory().countItem(ward);
        boolean route = front ? order.equals(List.of("incoming", "shield", "hurt-stage", "settled"))
                && resolution != null && resolution.damage() == 0 && resolution.blockedDamage() == DAMAGE
                : order.equals(List.of("incoming", "shield", "hurt-stage", "ward")) && wardAmount == DAMAGE && wardHealth == 2;
        boolean shieldOk = block != null && !block.canceled() && block.originalBlock() == front && block.blocked() == front
                && loss == (front ? 1 + (int) DAMAGE : 0);
        boolean wardOk = count == (front ? 2 : 1) && defender.getHealth() == (front ? 2 : Math.min(12, defender.getMaxHealth()))
                && defender.hasEffect(MobEffects.GLOWING) == !front;
        ItemStack chestAfter = defender.getItemBySlot(EquipmentSlot.CHEST);
        boolean protectionOk = front || (defender.getAbsorptionAmount() == absorptionBefore && chestAfter.is(Items.IRON_CHESTPLATE)
                && chestAfter.getDamageValue() == chestWearBefore);
        boolean leechOk = near(attacker.getHealth(), front ? 1.0 : 1.05);
        expectTrue(trial + " 路径=" + order + "，伤前生命=" + wardHealth + "，符判定量=" + wardAmount, route);
        expectTrue(trial + " 原版盾挡=" + block + "，盾损=" + loss, shieldOk);
        expectTrue(trial + " 剩符=" + count + "，生命=" + defender.getHealth() + "，吸收=" + defender.getAbsorptionAmount(), wardOk);
        expectTrue(trial + " 攻击者生命=" + attacker.getHealth() + "（Q30 去重后仅 HurtStage 吸血：全挡 +0、背击 +0.05）", leechOk);
        readyAt = defender.getServer().getTickCount() + 25;
        if (front) {
            // 利用两次攻击之间的等待让原版应用胸甲属性；吸收 I 同时提供上限和 4 点吸收。
            defender.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            defender.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200));
        } else {
            expectTrue("WARD_BACK 保护未消耗：吸收 " + absorptionBefore + " → " + defender.getAbsorptionAmount()
                    + "，胸甲 " + chestAfter + "，损耗 " + chestWearBefore + " → " + chestAfter.getDamageValue(), protectionOk);
            checkMobCooldown();
        }
        return route && shieldOk && wardOk && leechOk && protectionOk;
    }

    private void observeWard(LethalDamageEvent event) {
        if (active == null || event.victim() != defender) return;
        order.add("ward");
        wardHealth = defender.getHealth();
        wardAmount = event.amount();
    }

    private ItemStack enchantedStick(String id, int level) {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.enchant(defender.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key(id)), level);
        return stack;
    }

    /** 普通牛不经过 Player 覆写。三次同刻入伤验证公式写回、等量抑制和较大攻击只结算差额。 */
    private void checkMobCooldown() {
        Cow mob = EntityType.COW.create(defender.serverLevel());
        mob.moveTo(attacker.getX() + 2, attacker.getY(), attacker.getZ(), 0, 0);
        mob.setNoAi(true);
        if (!defender.serverLevel().addFreshEntity(mob)) {
            fail("无敌帧对照牛未进入世界");
            return;
        }
        try {
            mob.setItemSlot(EquipmentSlot.HEAD, enchantedStick("attdef", 2));
            mob.setHealth(10);
            attacker.setHealth(1);
            DamageSource source = mob.damageSources().mobAttack(attacker);
            mob.hurt(source, 4);
            expectTrue("普通牛 4 入伤，公式 16/(4+4)=2；仅 HurtStage 吸血 +0.10", near(mob.getHealth(), 8) && near(attacker.getHealth(), 1.1));
            mob.hurt(source, 4);
            expectTrue("同刻等量攻击无伤，不再吸血", near(mob.getHealth(), 8) && near(attacker.getHealth(), 1.1));
            mob.hurt(source, 6);
            expectTrue("同刻 6 入伤仅用差额 2，公式 4/(2+4)", near(mob.getHealth(), 8 - 2.0 / 3)
                    && near(attacker.getHealth(), 1.1 + 0.05 * 2 / 3));
        } finally {
            mob.discard();
        }
    }

    private static boolean near(float actual, double expected) {
        return Math.abs(actual - expected) < 1e-4;
    }

    private boolean observeTags(DamageSource source, boolean expected) {
        boolean armor = source.is(DamageTypeTags.BYPASSES_ARMOR);
        boolean shield = source.is(DamageTypeTags.BYPASSES_SHIELD);
        boolean match = armor == expected && shield == expected;
        expectTrue(source.typeHolder().unwrapKey().orElseThrow().location() + " 生效标签：绕甲=" + armor + "，绕盾=" + shield, match);
        return match;
    }

    private void incoming(LivingIncomingDamageEvent event) {
        if (active == null || event.getEntity() != defender || event.getSource() != active) return;
        order.add(event.isCanceled() ? "incoming-canceled" : "incoming");
        incomingAmount = event.getAmount();
    }

    private void shield(LivingShieldBlockEvent event) {
        if (active == null || event.getEntity() != defender || event.getDamageSource() != active) return;
        order.add("shield");
        block = new Shield(event.getOriginalBlock(), event.getBlocked(), event.isCanceled(), event.getBlockedDamage(), event.shieldDamage());
    }

    private void settled(LivingDamageEvent.Post event) {
        if (active == null || event.getEntity() != defender || event.getSource() != active) return;
        order.add("settled");
        resolution = new Settled(event.getNewDamage(), event.getBlockedDamage());
    }

    private void stop(String reason) {
        if (reason != null) fail(reason);
        minecraft.options.keyUse.setDown(false);
        if (minecraft.player.isUsingItem()) minecraft.gameMode.releaseUsingItem(minecraft.player);
        if (defender != null) {
            NeoForge.EVENT_BUS.unregister(incoming);
            NeoForge.EVENT_BUS.unregister(shield);
            NeoForge.EVENT_BUS.unregister(settled);
            NeoForge.EVENT_BUS.unregister(lethal);
            onServer(player -> attacker.discard());
        }
        finish();
    }
}
