package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.SealedCorpse;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 尸体收纳符的事件入口、原生顶面施用和真实保存重进必要单项。 */
final class FuluCorpseCheck extends SystemCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> entities = new ArrayList<>();
    private GameType mode;
    private int selected;
    private boolean sneaking;
    private BlockPos floor;
    private BlockState floorState;
    private BlockState aboveState;
    private CompoundTag payload;
    private UUID retainedCorpse;
    private double defaultAttack;
    private double defaultSpeed;
    private int settledTicks;

    FuluCorpseCheck(List<String> report, String levelId) {
        super("尸体收纳符", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            return true;
        }
        if (step == 1) {
            if (local < 8) return false;
            int[] pending = new int[1];
            onServer(player -> pending[0] = player.server.overworld().getChunkSource().getPendingTasksCount());
            settledTicks = pending[0] == 0 ? settledTicks + 1 : 0;
            if (settledTicks < 40) return false;
            expect("封存符中文名称", "收纳符", minecraft.player.getMainHandItem().getHoverName().getString());
            onServer(player -> {
                // 新世界区块准备可超过来源尸体的600刻寿命，存档样本在区块稳定后重新释放。
                Entity previous = player.serverLevel().getEntity(retainedCorpse);
                if (previous != null) previous.discard();
                ItemStack sealed = player.getMainHandItem().copy();
                use(player, Direction.UP);
                YaoshouEntity fresh = released(player);
                retainedCorpse = fresh.getUUID();
                expectTrue("保存前尸体仍在来源六百刻寿命内", fresh.deathTime < 600);
                player.setItemInHand(InteractionHand.MAIN_HAND, sealed);
                player.inventoryMenu.broadcastChanges();
            });
            leaveWorld();
            return true;
        }
        if (step == 2) return reopened(local);
        if (step == 3) {
            if (!inWorld(local)) return false;
            onServer(player -> {
                expect("真实重进保存封存载体", payload, data(player.getMainHandItem()));
                Entity corpse = player.serverLevel().getEntity(retainedCorpse);
                expectTrue("真实重进保留已释放尸体", corpse instanceof YaoshouEntity);
                if (corpse instanceof YaoshouEntity beast) verify(beast, "重进");
                player.setGameMode(GameType.CREATIVE);
                ItemStack sealed = player.getMainHandItem();
                use(player, Direction.UP);
                expect("创造模式原生入口恢复封存符数量", 1, sealed.getCount());
                expectTrue("创造模式仍释放尸体", !released(player).isAlive());
                cleanup(player);
            });
            finish();
            return true;
        }
        return false;
    }

    private void prepare(ServerPlayer player) {
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        sneaking = player.isShiftKeyDown();
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) inventory.add(player.getInventory().getItem(index).copy());
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        floor = player.blockPosition().offset(5, 4, 0);
        floorState = player.serverLevel().getBlockState(floor);
        aboveState = player.serverLevel().getBlockState(floor.above());
        player.serverLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        player.serverLevel().setBlock(floor.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        conditions(player);
        overflow(player);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
        player.getInventory().setItem(8, item("itemfulu_50", 2));
        YaoshouEntity corpse = beast(player, "yaoshou_001");
        corpse.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(77);
        corpse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.77);
        corpse.getAttribute(YaoshouAttributes.SKIN).setBaseValue(3);
        corpse.getAttribute(YaoshouAttributes.SIZE).setBaseValue(1.7);
        corpse.setCustomName(Component.literal("[收纳样本]"));
        corpse.tame(player);
        corpse.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        Relations.setTeam(corpse, "sealed_team");
        CompoundTag state = corpse.getPersistentData();
        state.putDouble("caijinumber", 4.25);
        state.putDouble("experience", 17.5);
        state.putDouble("health_Max", 1234);
        state.putDouble("attack_Num", 90);
        state.putDouble("speed_number", 0.9);
        state.putString("character", "谨慎");
        state.putDouble("character_def", 1.5);
        state.putDouble("character_att", 0.5);
        state.putDouble("character_interval", 1.5);
        state.putDouble("character_resist", 1.3);
        state.putBoolean("yaodandiaoluo", true);
        state.putString("jineng", "不应封存");
        corpse.setHealth(0);
        player.setShiftKeyDown(true);
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("携带空符且主手石头仍可收纳", corpse.isRemoved() && player.getMainHandItem().is(Items.STONE));
        expect("生存收纳只扣一张空符", 1, player.getInventory().getItem(8).getCount());
        ItemStack sealed = sealed(player);
        payload = data(sealed);
        expect("收纳去除显示名方括号", "收纳样本", payload.getString("yaoshouname"));
        expect("来源妖兽玩家能力团队为空", "", payload.getString("team"));
        expectTrue("载体不复制采丹标志和技能", !payload.contains("yaodandiaoluo") && !payload.contains("jineng"));
        expect("封存符组件保存重读", payload, data(ItemStack.parseOptional(player.registryAccess(), (CompoundTag) sealed.save(player.registryAccess()))));
        player.setItemInHand(InteractionHand.MAIN_HAND, sealed.copy());
        player.setShiftKeyDown(false);
        expect("侧面仍返回成功", InteractionResult.SUCCESS, use(player, Direction.EAST));
        expect("侧面不消耗", 1, player.getMainHandItem().getCount());
        expect("顶面返回成功", InteractionResult.SUCCESS, use(player, Direction.UP));
        expectTrue("生存顶面消耗封存符", player.getMainHandItem().isEmpty());
        YaoshouEntity released = released(player);
        verify(released, "释放");
        expect("顶面释放位置", Vec3.atBottomCenterOf(floor.above()), released.position());
        expectTrue("顶面存在方块仍生成尸体", player.serverLevel().getBlockState(released.blockPosition()).is(Blocks.STONE));
        retainedCorpse = released.getUUID();
        player.setItemInHand(InteractionHand.MAIN_HAND, sealed.copy());
        player.inventoryMenu.broadcastChanges();
    }

    private void conditions(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
        player.getInventory().setItem(8, item("itemfulu_50", 2));
        YaoshouEntity corpse = beast(player, "yaoshou_002");
        player.setShiftKeyDown(true);
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("存活妖兽不收纳", !corpse.isRemoved());
        corpse.setHealth(0);
        player.setShiftKeyDown(false);
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("未潜行不收纳", !corpse.isRemoved());
        player.setShiftKeyDown(true);
        interact(player, corpse, InteractionHand.OFF_HAND);
        expectTrue("非当前使用手不收纳", !corpse.isRemoved());
        player.getInventory().setItem(8, ItemStack.EMPTY);
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("没有空符不收纳", !corpse.isRemoved());
        player.getInventory().setItem(8, item("itemfulu_50", 2));
        player.setItemInHand(InteractionHand.MAIN_HAND, item("tiandao_np_csetitem", 1));
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("来源优先物品阻断收纳", !corpse.isRemoved());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
        player.setGameMode(GameType.CREATIVE);
        Fulu.setTalisman(player.getInventory().getItem(8), new TalismanInstance(2, "", 0, 0));
        corpse.getAttribute(YaoshouAttributes.SKIN).setBaseValue(5);
        interact(player, corpse, InteractionHand.MAIN_HAND);
        expectTrue("创造携带有品阶空符仍收纳", corpse.isRemoved());
        expect("创造收纳仍扣一张空符", 1, player.getInventory().getItem(8).getCount());
        expect("002皮肤写入载体", 5, data(sealed(player)).getDouble("yaoshoupifuxianshi"));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
    }

    private void overflow(ServerPlayer player) {
        for (int index = 0; index < player.getInventory().items.size(); index++) player.getInventory().setItem(index, new ItemStack(Items.STONE, 64));
        player.getInventory().setItem(8, item("itemfulu_50", 2));
        YaoshouEntity corpse = beast(player, "yaoshou_001");
        corpse.setHealth(0);
        Set<UUID> before = new HashSet<>();
        player.serverLevel().getAllEntities().forEach(entity -> before.add(entity.getUUID()));
        interact(player, corpse, InteractionHand.MAIN_HAND);
        List<ItemEntity> dropped = player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3),
                entity -> !before.contains(entity.getUUID()) && entity.getItem().is(item("itemfulu_5001", 1).getItem()));
        expect("满背包封存符交原生掉落", 1, dropped.size());
        dropped.forEach(Entity::discard);
    }

    private YaoshouEntity beast(ServerPlayer player, String id) {
        YaoshouEntity beast = (YaoshouEntity) CreatureSetup.entityType(id).get().create(player.serverLevel());
        if (id.equals("yaoshou_001")) {
            defaultAttack = beast.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
            defaultSpeed = beast.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        }
        beast.moveTo(player.getX() + 2, player.getY(), player.getZ());
        beast.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(beast.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        beast.setNoAi(true);
        beast.setNoGravity(true);
        player.serverLevel().addFreshEntity(beast);
        entities.add(beast.getUUID());
        return beast;
    }

    private void verify(YaoshouEntity corpse, String phase) {
        CompoundTag state = corpse.getPersistentData();
        expectTrue(phase + "保留零生命与境界", corpse.getHealth() == 0 && !state.getString("LEVELname").isEmpty());
        expect(phase + "保留采集次数", 4.25, state.getDouble("caijinumber"));
        expect(phase + "保留经验", 17.5, state.getDouble("experience"));
        expect(phase + "恢复最大生命属性", 1234, corpse.getAttributeBaseValue(Attributes.MAX_HEALTH));
        expect(phase + "恢复皮肤和尺寸", 3, corpse.getAttributeBaseValue(YaoshouAttributes.SKIN));
        expect(phase + "恢复尺寸", 1.7, corpse.getAttributeBaseValue(YaoshouAttributes.SIZE));
        expect(phase + "恢复性格参数", 1.3, state.getDouble("character_resist"));
        expect(phase + "攻击数值仍只存自由字段", 90, state.getDouble("attack_Num"));
        expect(phase + "不从自由字段恢复攻击属性", defaultAttack, corpse.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
        expect(phase + "不从自由字段恢复速度属性", defaultSpeed, corpse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
        expect(phase + "团队为空", "", Relations.entityTeam(corpse));
        expectTrue(phase + "不带回驯服、装备、采丹和技能状态", !corpse.isTame() && corpse.getMainHandItem().isEmpty()
                && !state.getBoolean("yaodandiaoluo") && state.getString("jineng").isEmpty());
    }

    private YaoshouEntity released(ServerPlayer player) {
        YaoshouEntity corpse = player.serverLevel().getEntitiesOfClass(YaoshouEntity.class, new AABB(floor).inflate(3)).stream()
                .filter(entity -> !entities.contains(entity.getUUID())).findFirst().orElseThrow();
        entities.add(corpse.getUUID());
        return corpse;
    }

    private InteractionResult use(ServerPlayer player, Direction face) {
        return player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor), face, floor, false));
    }

    private static void interact(ServerPlayer player, Entity corpse, InteractionHand hand) {
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, hand, corpse));
    }

    private static ItemStack sealed(ServerPlayer player) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item("itemfulu_5001", 1).getItem())).findFirst().orElseThrow().copy();
    }

    private static ItemStack item(String path, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)), count);
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(FuluData.SEALED_CORPSE.get(), SealedCorpse.EMPTY).corpse();
    }

    private void cleanup(ServerPlayer player) {
        for (UUID id : entities) {
            Entity entity = player.serverLevel().getEntity(id);
            if (entity != null) entity.discard();
        }
        player.getInventory().clearContent();
        for (int index = 0; index < inventory.size(); index++) player.getInventory().setItem(index, inventory.get(index));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(sneaking);
        player.serverLevel().setBlock(floor, floorState, Block.UPDATE_ALL);
        player.serverLevel().setBlock(floor.above(), aboveState, Block.UPDATE_ALL);
        player.inventoryMenu.broadcastChanges();
    }
}
