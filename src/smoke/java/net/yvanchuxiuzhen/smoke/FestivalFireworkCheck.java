package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.collection.festival.AttachedFirecrackerBlock;
import net.yvanchuxiuzhen.collection.festival.BlockFireworkBlockEntity;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.collection.festival.Pao13Entity;
import net.yvanchuxiuzhen.platform.ModId;

/** Pao10–Pao13 的放置、延迟燃放、相邻传燃和弹体延迟队列必要检查。 */
final class FestivalFireworkCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<UUID> spawned = new ArrayList<>();
    private final List<UUID> rockets = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> joined = this::joined;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private GameType mode;
    private int selected;
    private BlockPos base;
    private BlockPos pao10;
    private BlockPos pao10Neighbor;
    private BlockPos pao11;
    private BlockPos pao12;
    private Pao13Entity projectile;
    private int rocketJoins;

    FestivalFireworkCheck(List<String> report, String levelId) {
        super("节日方块烟花", report, levelId);
        script(new Action(2, this::prepare), new Action(2, this::place), new Action(2, this::ignite),
                new Action(17, player -> { }), new Action(1, this::observeLaunch),
                new Action(8, player -> { }), new Action(1, this::observeFirstDelay),
                new Action(8, player -> { }), new Action(1, this::observeSecondDelay));
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        base = player.blockPosition().above(12);
        pao10 = base.above();
        pao10Neighbor = base.east().above();
        pao11 = base.east(3).above();
        pao12 = base.east(6).above();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-4, 0, -4), base.east(8).south(4).above(3))) {
            BlockPos key = pos.immutable();
            blocks.put(key, player.level().getBlockState(key));
            player.level().setBlock(key, pos.getY() == base.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        }
        player.connection.teleport(base.getX() + .5, base.getY() + 1, base.getZ() - 3, 0, 0);
        NeoForge.EVENT_BUS.addListener(joined);
    }

    private void place(ServerPlayer player) {
        place(player, "pao_10", base, pao10);
        place(player, "pao_10", base.east(), pao10Neighbor);
        place(player, "pao_11", base.east(3), pao11);
        place(player, "pao_12", base.east(6), pao12);
        BlockState state = player.level().getBlockState(pao10);
        expect("Pao10 顶面附着状态", AttachFace.FLOOR, state.getValue(AttachedFirecrackerBlock.FACE));
        expectTrue("Pao10 方块实体已创建", player.level().getBlockEntity(pao10) instanceof BlockFireworkBlockEntity);
        expectTrue("Pao11 方块实体已创建", player.level().getBlockEntity(pao11) instanceof BlockFireworkBlockEntity);
        expectTrue("Pao12 方块实体已创建", player.level().getBlockEntity(pao12) instanceof BlockFireworkBlockEntity);
    }

    private void place(ServerPlayer player, String id, BlockPos support, BlockPos target) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item(id, 2));
        InteractionResult result = player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false));
        expectTrue(id + " 顶面放置返回成功", accepted(result));
        expect(id + " 生存放置扣一件", 1, player.getMainHandItem().getCount());
        expect(id + " 放置后方块身份", id, BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(target).getBlock()).getPath());
    }

    private void ignite(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (Map.Entry<String, BlockPos> entry : Map.of("pao_10", pao10, "pao_11", pao11, "pao_12", pao12).entrySet()) {
            InteractionResult result = player.gameMode.useItemOn(player, player.level(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(entry.getValue()), Direction.UP, entry.getValue(), false));
            expectTrue(entry.getKey() + " 空手点燃入口返回成功", accepted(result));
            expectTrue(entry.getKey() + " 写入 begin", entity(player, entry.getValue()).begin());
        }

        BlockFireworkBlockEntity firecracker = entity(player, pao10);
        for (int tick = 0; tick < 20; tick++) firecracker.scheduledTick(player.serverLevel());
        expectTrue("Pao10 延迟二十刻进入第二阶段", firecracker.begin2() && !firecracker.begin());
        firecracker.setTick(599);
        firecracker.scheduledTick(player.serverLevel());
        expectTrue("Pao10 燃尽时传燃相邻爆竹", entity(player, pao10Neighbor).begin());
        expectTrue("Pao10 燃尽后清除方块", player.level().getBlockState(pao10).isAir());

        BlockFireworkBlockEntity fountain = entity(player, pao11);
        for (int tick = 0; tick < 20; tick++) fountain.scheduledTick(player.serverLevel());
        expectTrue("Pao11 延迟二十刻进入第二阶段", fountain.begin2() && !fountain.begin());

        BlockFireworkBlockEntity battery = entity(player, pao12);
        for (int tick = 0; tick < 2; tick++) battery.scheduledTick(player.serverLevel());
        expectTrue("Pao12 两次计划刻后进入第二阶段", battery.begin2() && !battery.begin());
        int before = pao13(player).size();
        battery.scheduledTick(player.serverLevel());
        List<Pao13Entity> launched = pao13(player);
        expect("Pao12 第二阶段每刻生成一枚 Pao13", before + 1, launched.size());
        launched.forEach(entity -> {
            spawned.add(entity.getUUID());
            entity.discard();
        });
        player.level().setBlock(pao12, Blocks.AIR.defaultBlockState(), 3);

        rocketJoins = 0;
        projectile = new Pao13Entity(FestivalSetup.pao13Type().get(), player.level());
        projectile.moveTo(base.east(3).north(3).getX() + .5, base.getY() + 3, base.getZ() + .5, 0, 0);
        projectile.setNoGravity(true);
        projectile.setFireType(5);
        spawned.add(projectile.getUUID());
        player.serverLevel().addFreshEntity(projectile);
    }

    private void observeLaunch(ServerPlayer player) {
        expectTrue("Pao13 飞行十六刻后移除弹体", projectile.isRemoved());
        expect("Pao13 第一阶段加入一枚烟花火箭", 1, rocketJoins);
    }

    private void observeFirstDelay(ServerPlayer player) {
        expect("Pao13 八刻延迟队列再加入一枚", 2, rocketJoins);
    }

    private void observeSecondDelay(ServerPlayer player) {
        expect("Pao13 十六刻延迟队列再加入一枚", 3, rocketJoins);
    }

    private void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof FireworkRocketEntity rocket)) return;
        rocketJoins++;
        rockets.add(rocket.getUUID());
    }

    private BlockFireworkBlockEntity entity(ServerPlayer player, BlockPos pos) {
        return (BlockFireworkBlockEntity) player.level().getBlockEntity(pos);
    }

    private List<Pao13Entity> pao13(ServerPlayer player) {
        return player.level().getEntitiesOfClass(Pao13Entity.class, player.getBoundingBox().inflate(32));
    }

    private static boolean accepted(InteractionResult result) {
        return result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME;
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), count);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        for (UUID uuid : spawned) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        for (UUID uuid : rockets) {
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
