package net.yvanchuxiuzhen.smoke;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.creature.item.YaoshouProjectileItem;

/** 三件来源投射物品经原生服务端使用/松手入口的固定参数和主副手边界检查。 */
final class YaoshouProjectileItemCheck extends SystemCheck {
    private static final List<ShotCase> CASES = List.of(
            new ShotCase("yaoshou_016duye", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 1, 1, 3),
            new ShotCase("yaoshou_016duye", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 20, 1, 3),
            new ShotCase("yaoshou_016duye", InteractionHand.OFF_HAND, GameType.SURVIVAL, 1, 1, 3),
            new ShotCase("yaoshou_016duye", InteractionHand.MAIN_HAND, GameType.CREATIVE, 1, 1, 3),
            new ShotCase("yaoshou_026huoyan", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 1, 0.1, 1),
            new ShotCase("yaoshou_026huoyan", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 20, 0.1, 1),
            new ShotCase("yaoshou_026huoyan", InteractionHand.OFF_HAND, GameType.SURVIVAL, 1, 0.1, 1),
            new ShotCase("yaoshou_026huoyan", InteractionHand.MAIN_HAND, GameType.CREATIVE, 1, 0.1, 1),
            new ShotCase("yaoshou_028shuiqiang", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 1, 0.1, 1),
            new ShotCase("yaoshou_028shuiqiang", InteractionHand.MAIN_HAND, GameType.SURVIVAL, 20, 0.1, 1),
            new ShotCase("yaoshou_028shuiqiang", InteractionHand.OFF_HAND, GameType.SURVIVAL, 1, 0.1, 1),
            new ShotCase("yaoshou_028shuiqiang", InteractionHand.MAIN_HAND, GameType.CREATIVE, 1, 0.1, 1));
    private final Set<UUID> previousArrows = new HashSet<>();
    private final Set<UUID> createdArrows = new HashSet<>();
    // 保持客户端使用键按下，且不允许自动右键与受控服务端入口竞争。
    private final Consumer<InputEvent.InteractionKeyMappingTriggered> useInput = event -> {
        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    };
    private Vec3 originalPosition;
    private Vec3 originalMovement;
    private ItemStack originalMain;
    private ItemStack originalOff;
    private GameType originalMode;
    private boolean originalInvulnerable;
    private boolean originalNoGravity;
    private boolean originalUseKey;
    private float originalYaw;
    private float originalPitch;
    private int caseIndex;
    private boolean prepared;
    private boolean using;

    YaoshouProjectileItemCheck(List<String> report, String levelId) {
        super("妖兽投射物品", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> {
                originalUseKey = minecraft.options.keyUse.isDown();
                NeoForge.EVENT_BUS.addListener(useInput);
                minecraft.options.keyUse.setDown(true);
                onServer(this::prepare);
                yield true;
            }
            case 1 -> launchCases(local);
            case 2 -> {
                expect("毒液中文名称", "毒液", I18n.get("item.yvanchuxiuzhen.yaoshou_016duye"));
                expect("蓝焰火中文名称", "蓝焰火", I18n.get("item.yvanchuxiuzhen.yaoshou_026huoyan"));
                expect("水枪中文名称", "水枪", I18n.get("item.yvanchuxiuzhen.yaoshou_028shuiqiang"));
                onServer(this::cleanup);
                restoreInput();
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知妖兽投射物品步骤 " + step);
        };
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalMovement = player.getDeltaMovement();
        originalMain = player.getMainHandItem().copy();
        originalOff = player.getOffhandItem().copy();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalInvulnerable = player.isInvulnerable();
        originalNoGravity = player.isNoGravity();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        prepared = true;
        player.stopUsingItem();
        for (String identity : List.of("yaoshou_016duye", "yaoshou_026huoyan", "yaoshou_028shuiqiang")) {
            if (!(BuiltInRegistries.ITEM.get(id(identity)) instanceof YaoshouProjectileItem)
                    || !BuiltInRegistries.ENTITY_TYPE.containsKey(id("projectile_" + identity))) {
                abort(player, identity + "物品/弹体工厂未接线，停止依赖步骤");
                return;
            }
        }
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(player.serverLevel(), player.getX(), player.getY() + 24, player.getZ(), 0, 0);
    }

    private boolean launchCases(int local) {
        boolean[] completed = {false};
        onServer(player -> {
            ShotCase current = CASES.get(caseIndex);
            if (!using) {
                using = begin(player, current);
                return;
            }
            if (!player.isUsingItem() || player.getUsedItemHand() != current.hand()) {
                abort(player, current.label() + "原生持续使用提前丢失，停止松手步骤");
                return;
            }
            if (player.getTicksUsingItem() < current.heldTicks()) return;
            player.releaseUsingItem();
            List<AbstractArrow> incoming = arrows(player).stream()
                    .filter(arrow -> !previousArrows.contains(arrow.getUUID())).toList();
            expect(current.label() + "原生松手仅生成一枚弹体", 1, incoming.size());
            if (incoming.size() != 1) {
                incoming.forEach(arrow -> createdArrows.add(arrow.getUUID()));
                abort(player, current.label() + "弹体数量前置失败，停止依赖断言");
                return;
            }
            AbstractArrow projectile = incoming.getFirst();
            createdArrows.add(projectile.getUUID());
            checkShot(player, current, projectile);
            projectile.discard();
            using = false;
            caseIndex++;
            completed[0] = caseIndex == CASES.size();
        });
        if (!completed[0] && local > 800) onServer(player -> abort(player, "原生物品使用检查未在时限内完成"));
        return completed[0];
    }

    private boolean begin(ServerPlayer player, ShotCase current) {
        player.setGameMode(current.mode());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id(current.item())), current.count());
        player.setItemInHand(current.hand(), stack);
        previousArrows.clear();
        arrows(player).forEach(arrow -> previousArrows.add(arrow.getUUID()));
        expect(current.label() + "来源持续时间", 72000, stack.getUseDuration(player));
        expect(current.label() + "来源使用动画", UseAnim.NONE, stack.getUseAnimation());
        expectTrue(current.label() + "来源无耐久组件", !stack.isDamageableItem());
        InteractionResult result = player.gameMode.useItem(player, player.level(), stack, current.hand());
        boolean started = result == InteractionResult.SUCCESS && player.isUsingItem() && player.getUsedItemHand() == current.hand();
        expectTrue(current.label() + "原生使用开始当前手", started);
        if (!started) {
            abort(player, current.label() + "物品使用前置失败，停止依赖步骤");
            return false;
        }
        return true;
    }

    private void checkShot(ServerPlayer player, ShotCase current, AbstractArrow projectile) {
        expect(current.label() + "弹体身份", id("projectile_" + current.item()), BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()));
        expectTrue(current.label() + "原生主人", projectile.getOwner() == player);
        expect(current.label() + "来源发射横坐标", player.getX(), projectile.getX());
        expect(current.label() + "来源眼高发射位置", player.getEyeY() - (double) 0.1F, projectile.getY());
        expect(current.label() + "来源发射纵坐标", player.getZ(), projectile.getZ());
        expect(current.label() + "固定速度不按持用刻数缩放", 2, projectile.getDeltaMovement().length());
        expect(current.label() + "零散布沿玩家视线", 1, projectile.getDeltaMovement().normalize().dot(player.getViewVector(1)));
        expect(current.label() + "来源基础伤害", current.damage(), projectile.getBaseDamage());
        expectTrue(current.label() + "来源无暴击且弹体静音", !projectile.isCritArrow() && projectile.isSilent());
        expect(current.label() + "来源禁止拾取", AbstractArrow.Pickup.DISALLOWED, projectile.pickup);
        expect(current.label() + "玩家物品保留零穿透", 0, projectile.getPierceLevel());
        if (!current.item().equals("yaoshou_016duye")) {
            CompoundTag saved = projectile.saveWithoutId(new CompoundTag());
            expect(current.label() + "玩家物品使用零击退", 0, saved.getInt("SourceKnockback"));
        }
        expectTrue(current.label() + "松手结束持续使用", !player.isUsingItem());
        ItemStack retained = player.getItemInHand(current.hand());
        expect(current.label() + "来源无耐久物品保留数量", current.count(), retained.getCount());
        expect(current.label() + "来源无耐久物品保持零损伤", 0, retained.getDamageValue());
    }

    private List<AbstractArrow> arrows(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(AbstractArrow.class, new AABB(player.blockPosition()).inflate(64),
                arrow -> arrow.getOwner() == player && BuiltInRegistries.ENTITY_TYPE.getKey(arrow.getType()).getNamespace().equals("yvanchuxiuzhen"));
    }

    private void abort(ServerPlayer player, String message) {
        fail(message);
        cleanup(player);
        minecraft.execute(this::restoreInput);
        finish();
    }

    private void restoreInput() {
        minecraft.options.keyUse.setDown(originalUseKey);
        NeoForge.EVENT_BUS.unregister(useInput);
    }

    private void cleanup(ServerPlayer player) {
        if (!prepared) return;
        player.stopUsingItem();
        for (UUID identity : createdArrows) {
            Entity entity = player.serverLevel().getEntity(identity);
            if (entity != null) entity.discard();
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, originalMain);
        player.setItemInHand(InteractionHand.OFF_HAND, originalOff);
        player.setGameMode(originalMode);
        player.setInvulnerable(originalInvulnerable);
        player.setNoGravity(originalNoGravity);
        player.teleportTo(player.serverLevel(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setDeltaMovement(originalMovement);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }

    private record ShotCase(String item, InteractionHand hand, GameType mode, int heldTicks, double damage, int count) {
        private String label() {
            return item + " " + mode + " " + hand + " 持用" + heldTicks + "服务端刻";
        }
    }
}
