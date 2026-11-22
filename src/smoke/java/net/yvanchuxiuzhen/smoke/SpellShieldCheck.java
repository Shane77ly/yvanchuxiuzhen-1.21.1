package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.yvanchuxiuzhen.system.combat.damage.CombatDamageTypes;

/** 同一生存玩家使用原版盾牌，从固定正面比较物理与两种项目伤害源。 */
final class SpellShieldCheck extends SystemCheck {
    static final String SELECTOR = "q235spellshield";
    private static final float DAMAGE = 4;
    private static final int TIMEOUT = 200;

    private enum Trial { PHYSICAL, MANA, SOUL }

    private record Shield(boolean originalBlock, boolean blocked, boolean canceled, float candidateDamage, float shieldDamage) {}

    private record Settled(float damage, float blockedDamage) {}

    private final Consumer<LivingIncomingDamageEvent> incoming = this::incoming;
    private final Consumer<LivingShieldBlockEvent> shield = this::shield;
    private final Consumer<LivingDamageEvent.Post> settled = this::settled;
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
                if (ready[0]) passed[0] = hit(trial);
            });
        }
        if (!ready[0]) {
            if (local > TIMEOUT) stop(trial + " 未等到双端原生举盾或伤害保护结束");
            return false;
        }
        if (trial == Trial.PHYSICAL && !passed[0]) {
            stop("物理正例未通过，不将后续灵力或神识输出用于方向对照");
        } else if (trial == Trial.SOUL) {
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
            onServer(player -> attacker.discard());
        }
        finish();
    }
}
