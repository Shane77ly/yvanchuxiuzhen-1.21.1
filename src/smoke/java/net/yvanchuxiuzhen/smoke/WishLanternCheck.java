package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.collection.festival.WishLantern;

/** 祈愿灯自然放置、旋转上升、实体存储以及来源伤害与死亡边界的单项检查。 */
final class WishLanternCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> spawned = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private int originalSelected;
    private boolean originalShift;
    private boolean originalFlying;
    private BlockPos base;
    private Vec3 releasedAt;
    private float releasedYaw;

    WishLanternCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("祈愿灯", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 20) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> onServer(this::place);
            case 2 -> {
                if (local < 40) return false;
                onServer(this::observeFlight);
            }
            case 3 -> screenshots.take("wish_lantern_flight.png");
            case 4 -> onServer(this::serializationAndDamage);
            case 5 -> {
                onServer(this::cleanup);
                finish();
            }
            default -> throw new IllegalStateException("未知祈愿灯检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalSelected = player.getInventory().selected;
        originalShift = player.isShiftKeyDown();
        originalFlying = player.getAbilities().flying;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setShiftKeyDown(false);
        player.setGameMode(GameType.SURVIVAL);
        base = player.blockPosition().above(12);
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-3, 0, -4), base.offset(3, 0, 3))) {
            blocks.put(pos.immutable(), player.level().getBlockState(pos));
            player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        }
        blocks.put(base.above(), player.level().getBlockState(base.above()));
        player.level().setBlock(base.above(), Blocks.AIR.defaultBlockState(), 3);
        player.connection.teleport(base.getX() + .5, base.getY() + 1, base.getZ() - 3, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(), 3));
        player.inventoryMenu.broadcastChanges();
    }

    private void place(ServerPlayer player) {
        expect("侧面使用仍按来源返回成功", InteractionResult.SUCCESS, use(player, Direction.NORTH, InteractionHand.MAIN_HAND));
        expect("侧面使用不扣物品", 3, player.getMainHandItem().getCount());
        expect("侧面使用不产生实体", 0, lanterns(player).size());
        player.level().setBlock(base.above(), Blocks.STONE.defaultBlockState(), 3);
        expect("上方阻塞仍按来源返回成功", InteractionResult.SUCCESS, use(player, Direction.UP, InteractionHand.MAIN_HAND));
        expect("上方阻塞不扣物品", 3, player.getMainHandItem().getCount());
        expect("上方阻塞不产生实体", 0, lanterns(player).size());
        player.level().setBlock(base.above(), Blocks.AIR.defaultBlockState(), 3);
        use(player, Direction.UP, InteractionHand.MAIN_HAND);
        expect("生存顶面放置扣一件", 2, player.getMainHandItem().getCount());
        List<WishLantern> lanterns = lanterns(player);
        expect("生存顶面生成一盏", 1, lanterns.size());
        if (lanterns.size() != 1) {
            for (WishLantern entity : lanterns) spawned.add(entity.getUUID());
            cleanup(player);
            finish();
            return;
        }
        WishLantern lantern = lanterns.getFirst();
        spawned.add(lantern.getUUID());
        releasedAt = lantern.position();
        releasedYaw = lantern.getYRot();
        expect("生成位置为方块顶面中心", Vec3.atBottomCenterOf(base.above()), releasedAt);
        expect("来源生命上限", 4, lantern.getMaxHealth());
        expect("来源飞行速度", .1, lantern.getAttributeBaseValue(Attributes.FLYING_SPEED));
        player.setGameMode(GameType.CREATIVE);
        use(player, Direction.UP, InteractionHand.MAIN_HAND);
        expect("自然创造放置由原版恢复数量", 2, player.getMainHandItem().getCount());
        expect("创造放置仍产生实体", 2, lanterns(player).size());
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(item(), 2));
        use(player, Direction.UP, InteractionHand.OFF_HAND);
        expect("副手放置扣副手一件", 1, player.getOffhandItem().getCount());
        expect("副手放置不扣主手", 2, player.getMainHandItem().getCount());
        expect("副手放置产生第三盏", 3, lanterns(player).size());
        for (WishLantern entity : lanterns(player)) if (!spawned.contains(entity.getUUID())) spawned.add(entity.getUUID());
        player.inventoryMenu.broadcastChanges();
    }

    private void observeFlight(ServerPlayer player) {
        WishLantern lantern = (WishLantern) player.serverLevel().getEntity(spawned.getFirst());
        expectTrue("自然刻驱动祈愿灯上升（高度差=" + (lantern.getY() - releasedAt.y) + "）", lantern.getY() > releasedAt.y + 1);
        expectTrue("自然刻驱动祈愿灯转向", lantern.getYRot() != releasedYaw);
        expectTrue("自然刻启用固定无重力", lantern.isNoGravity());
        expectTrue("来源不把放出灯标记为永久实体", !lantern.isPersistenceRequired());
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.connection.teleport(lantern.getX(), lantern.getY() - .5, lantern.getZ() - 3, 0, 0);
    }

    private void serializationAndDamage(ServerPlayer player) {
        WishLantern lantern = FestivalSetup.wishLanternType().get().create(player.level());
        lantern.moveTo(base.getX(), base.getY(), base.getZ(), 0, 25);
        lantern.baseTick();
        expect("一刻转向一度", 1, lantern.getYRot());
        expect("一刻归零俯仰", 0, lantern.getXRot());
        expect("一刻设置向上速度", .1, lantern.getDeltaMovement().y);
        expectTrue("一刻水平速度为十分之一格", Math.abs(lantern.getDeltaMovement().horizontalDistance() - .1) < 1e-6);
        expect("前后帧朝向同步", lantern.getYRot(), lantern.yRotO);
        lantern.setNoGravity(false);
        expectTrue("来源固定无重力忽略关闭请求", lantern.isNoGravity());
        CompoundTag saved = new CompoundTag();
        lantern.save(saved);
        WishLantern restored = (WishLantern) EntityType.loadEntityRecursive(saved, player.level(), entity -> entity);
        expect("实体存储恢复朝向", 1, restored.getYRot());
        expect("实体存储恢复位置", lantern.position(), restored.position());
        expect("实体存储恢复速度", lantern.getDeltaMovement(), restored.getDeltaMovement());
        expectTrue("实体存储恢复无重力", restored.isNoGravity());
        restored.baseTick();
        expect("重读后下一刻继续转向", 2, restored.getYRot());
        restored.setHealth(restored.getMaxHealth());
        restored.invulnerableTime = 0;
        expectTrue("摔落伤害按来源免疫", !restored.hurt(player.damageSources().fall(), 10));
        expectTrue("仙人掌伤害按来源免疫", !restored.hurt(player.damageSources().cactus(), 10));
        expectTrue("普通伤害仍接受", restored.hurt(player.damageSources().generic(), 1));
        expect("普通伤害扣除生命", 3, restored.getHealth());
        restored.invulnerableTime = 0;
        restored.hurt(player.damageSources().generic(), 4);
        restored.baseTick();
        expectTrue("死亡第一刻尚未移除", !restored.isRemoved());
        restored.baseTick();
        expectTrue("死亡第二刻移除", restored.isRemoved());
    }

    private void cleanup(ServerPlayer player) {
        for (UUID uuid : spawned) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        blocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = originalSelected;
        player.setShiftKeyDown(originalShift);
        player.setGameMode(originalMode);
        player.getAbilities().flying = originalFlying;
        player.onUpdateAbilities();
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.inventoryMenu.broadcastChanges();
    }

    private List<WishLantern> lanterns(ServerPlayer player) {
        return player.level().getEntitiesOfClass(WishLantern.class, new AABB(base).inflate(2));
    }

    private InteractionResult use(ServerPlayer player, Direction face, InteractionHand hand) {
        return player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand,
                new BlockHitResult(Vec3.atCenterOf(base), face, base, false));
    }

    private static Item item() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "fuqi_03"));
    }
}
