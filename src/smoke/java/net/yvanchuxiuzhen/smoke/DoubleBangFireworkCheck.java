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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.festival.DoubleBangFirework;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.platform.ModId;

/** 二踢脚自然放置、交互点燃、传燃和两阶段实体存储的必要检查。 */
final class DoubleBangFireworkCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> spawned = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private Vec3 position;
    private float yaw;
    private float pitch;
    private GameType mode;
    private int selected;
    private BlockPos base;

    DoubleBangFireworkCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("二踢脚", report, levelId);
        script(new Action(2, this::prepare), new Action(2, this::place),
                new Action(2, player -> minecraft.execute(() -> screenshots.take("festival_double_bang.png"))),
                new Action(2, this::igniteAndStore));
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        base = player.blockPosition().above(12);
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, 0, -3), base.offset(2, 3, 2))) {
            blocks.put(pos.immutable(), player.level().getBlockState(pos));
            player.level().setBlock(pos, (pos.getY() == base.getY() ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
        }
        player.connection.teleport(base.getX() + .5, base.getY() + 1, base.getZ() - 2, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, item(3));
        player.inventoryMenu.broadcastChanges();
    }

    private void place(ServerPlayer player) {
        expect("侧面使用保留SUCCESS", InteractionResult.SUCCESS, use(player, Direction.NORTH, InteractionHand.MAIN_HAND));
        expect("侧面使用不扣件", 3, player.getMainHandItem().getCount());
        expect("侧面使用不生成实体", 0, nearby(player).size());
        player.level().setBlock(base.above(), Blocks.STONE.defaultBlockState(), 3);
        use(player, Direction.UP, InteractionHand.MAIN_HAND);
        expect("上方阻塞不扣件", 3, player.getMainHandItem().getCount());
        expect("上方阻塞不生成实体", 0, nearby(player).size());
        player.level().setBlock(base.above(), Blocks.AIR.defaultBlockState(), 3);
        use(player, Direction.UP, InteractionHand.MAIN_HAND);
        expect("生存顶面放置扣一件", 2, player.getMainHandItem().getCount());
        expect("生存顶面生成一件", 1, nearby(player).size());
        player.setGameMode(GameType.CREATIVE);
        use(player, Direction.UP, InteractionHand.MAIN_HAND);
        expect("创造放置由原生包装保留数量", 2, player.getMainHandItem().getCount());
        expect("创造放置仍生成一件", 2, nearby(player).size());
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, item(2));
        use(player, Direction.UP, InteractionHand.OFF_HAND);
        expect("副手放置扣副手", 1, player.getOffhandItem().getCount());
        expect("副手放置保持主手", 2, player.getMainHandItem().getCount());
        expect("副手放置第三件", 3, nearby(player).size());
        for (DoubleBangFirework entity : nearby(player)) {
            spawned.add(entity.getUUID());
            expect("放置位置为顶面中心", Vec3.atBottomCenterOf(base.above()), entity.position());
            expectTrue("放置时保持未点燃", !entity.getPersistentData().getBoolean("begin") && !entity.getPersistentData().getBoolean("begin2"));
            expectTrue("实体保持来源不受推挤", !entity.isPushable());
        }
        expect("客户端合并中文名称", "二踢脚", minecraft.player.getMainHandItem().getHoverName().getString());
        player.inventoryMenu.broadcastChanges();
    }

    private void igniteAndStore(ServerPlayer player) {
        List<DoubleBangFirework> group = nearby(player);
        if (group.size() != 3) {
            fail("自然放置未产生三件，跳过依赖的传燃与存储检查");
            return;
        }
        DoubleBangFirework first = group.getFirst();
        expect("自然实体右键在服务端返回CONSUME", InteractionResult.CONSUME, first.interact(player, InteractionHand.MAIN_HAND));
        expectTrue("自然实体右键置引燃状态", first.getPersistentData().getBoolean("begin"));
        expect("引燃不扣手中物品", 2, player.getMainHandItem().getCount());
        for (int tick = 0; tick < 19; tick++) first.baseTick();
        expectTrue("引燃第十九刻仍在第一阶段", first.getPersistentData().getBoolean("begin") && !first.getPersistentData().getBoolean("begin2"));
        CompoundTag saved = new CompoundTag();
        first.save(saved);
        DoubleBangFirework restored = (DoubleBangFirework) EntityType.loadEntityRecursive(saved, player.level(), entity -> entity);
        expect("实体重读保留第一阶段刻数", 19, restored.getPersistentData().getDouble("tick"));
        restored.baseTick();
        expectTrue("重读后第二十刻切换到二响阶段", !restored.getPersistentData().getBoolean("begin")
                && restored.getPersistentData().getBoolean("begin2") && restored.getPersistentData().getDouble("tick") == 0);
        expectTrue("第一响传燃一格盒范围内未点燃同类", group.stream().filter(entity -> entity != first)
                .allMatch(entity -> entity.getPersistentData().getBoolean("begin")));
        first.discard();
        for (int tick = 0; tick < 19; tick++) restored.baseTick();
        expect("二响阶段每刻设置上升速度", .8, restored.getDeltaMovement().y);
        expectTrue("二响第十九刻仍存活", !restored.isRemoved());
        restored.save(saved = new CompoundTag());
        restored = (DoubleBangFirework) EntityType.loadEntityRecursive(saved, player.level(), entity -> entity);
        restored.baseTick();
        expectTrue("二响阶段重读后第二十刻移除", restored.isRemoved());
        expectTrue("二响完成清除来源状态", !restored.getPersistentData().getBoolean("begin2") && restored.getPersistentData().getDouble("tick") == 0);

        DoubleBangFirework fixture = FestivalSetup.doubleBangType().get().create(player.level());
        fixture.moveTo(base.getX() + 8, base.getY() + 2, base.getZ(), 0, 0);
        fixture.getPersistentData().putBoolean("begin2", true);
        fixture.getPersistentData().putDouble("tick", 6);
        fixture.interact(player, InteractionHand.MAIN_HAND);
        expectTrue("重复右键保留二响标记并重开第一阶段", fixture.getPersistentData().getBoolean("begin") && fixture.getPersistentData().getBoolean("begin2"));
        expect("重复右键不重置共享计时", 6, fixture.getPersistentData().getDouble("tick"));
        expectTrue("玩家直接伤害按来源免疫", !fixture.hurt(player.damageSources().playerAttack(player), 1));
        expectTrue("摔落按来源免疫", !fixture.hurt(player.damageSources().fall(), 1));
        expectTrue("普通伤害仍接受", fixture.hurt(player.damageSources().generic(), 1));
    }

    private List<DoubleBangFirework> nearby(ServerPlayer player) {
        return player.level().getEntitiesOfClass(DoubleBangFirework.class, new AABB(base.above()).inflate(1));
    }

    private InteractionResult use(ServerPlayer player, Direction face, InteractionHand hand) {
        return player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand,
                new BlockHitResult(Vec3.atCenterOf(base), face, base, false));
    }

    private static ItemStack item(int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao_09")), count);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        for (UUID uuid : spawned) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        blocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
