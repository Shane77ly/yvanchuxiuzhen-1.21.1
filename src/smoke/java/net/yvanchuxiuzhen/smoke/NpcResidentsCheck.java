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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.npc.NpcData;
import net.yvanchuxiuzhen.system.npc.NpcDialogueProfile;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.NpcStructureReleases;
import net.yvanchuxiuzhen.system.npc.entity.NpcResident;
import net.yvanchuxiuzhen.system.npc.entity.NpcResidentAnimated;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/** 居民十蛋、默认生命周期、真实模板数据、结构任务后处理与原生对话保存重进的必要单项。 */
final class NpcResidentsCheck extends SystemCheck {
    private static final Vec3 CHILD_POSITION = new Vec3(-98.5, 63, -277.5);
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> spawned = new ArrayList<>();
    private final List<BlockPos> floors = new ArrayList<>();
    private final List<BlockState> floorStates = new ArrayList<>();
    private final Map<Long, Boolean> forced = new LinkedHashMap<>();
    private CompoundTag journal;
    private CompoundTag session;
    private NpcDialogueProfile childProfile;
    private UUID child;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;

    NpcResidentsCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("21至30号居民", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        if (step == 7) return reopened(local);
        if (step == 8) return inWorld(local);
        if (local < 10) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                boolean[] ready = {false};
                onServer(player -> ready[0] = player.serverLevel().isPositionEntityTicking(BlockPos.containing(CHILD_POSITION))
                        && player.serverLevel().isPositionEntityTicking(new BlockPos(-75, 63, -360)));
                if (!ready[0]) {
                    if (local > 400) { fail("模板居民与商人区块未达到实体更新状态"); finish(); }
                    return false;
                }
                onServer(player -> { eggs(player); lifecycle(player); template(player); });
            }
            case 2 -> {
                if (!clientChild(local)) return false;
                screenshots.take("npc_resident_template.png");
                minecraft.gameMode.interact(minecraft.player, minecraft.level.getEntity(serverChildId()), InteractionHand.MAIN_HAND);
            }
            case 3 -> {
                if (!dialogue(local, "小孩哥")) return false;
                screenshots.take("npc_resident_first_dialogue.png");
                onServer(player -> {
                    expectTrue("模板首任务经原生右键进入会话", DialogueActions.snapshot(player).getString("renwulist").contains("/yiqizhuatuzi/"));
                    expect("原生居民回调转向玩家反面", player.getYRot() - 180, player.serverLevel().getEntity(child).getYRot());
                    player.closeContainer();
                    CompoundTag completed = DialogueActions.journal(player);
                    completed.putString("yiqizhuatuzizhuangtai", "完成");
                    player.setData(QuestData.JOURNAL, completed);
                });
            }
            case 4 -> minecraft.gameMode.interact(minecraft.player, minecraft.level.getEntity(serverChildId()), InteractionHand.MAIN_HAND);
            case 5 -> {
                if (!dialogue(local, "唉")) return false;
                screenshots.take("npc_resident_second_dialogue.png");
                onServer(player -> {
                    expectTrue("首任务完成后自然选择结构追加第二任务", DialogueActions.snapshot(player).getString("renwulist").contains("/yiqizhuatuzier/"));
                    childProfile = player.serverLevel().getEntity(child).getData(NpcData.PROFILE);
                    player.closeContainer();
                });
            }
            case 6 -> leaveWorld();
            case 9 -> {
                if (!clientChild(local)) return false;
                onServer(player -> {
                    Entity loaded = player.serverLevel().getEntity(child);
                    expectTrue("正常重进保留真实模板居民身份", loaded instanceof NpcResidentAnimated);
                    NpcDialogueProfile reloaded = loaded.getData(NpcData.PROFILE);
                    expect("正常重进保留首项任务", childProfile.script(0), reloaded.script(0));
                    expect("正常重进保留结构追加任务", childProfile.script(1), reloaded.script(1));
                    expect("正常重进保持模板居民生命上限", 100, ((NpcResident) loaded).getMaxHealth());
                    expect("正常重进保留模板姓名", "小孩哥", loaded.getDisplayName().getString());
                    expectTrue("居民模型已注册", minecraft.getEntityRenderDispatcher().getRenderer(minecraft.level.getEntity(loaded.getId())) != null);
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知居民检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        journal = DialogueActions.journal(player);
        session = DialogueActions.snapshot(player);
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setData(QuestData.SESSION, new CompoundTag());
        player.setData(QuestData.JOURNAL, new CompoundTag());
        for (ChunkPos chunk : List.of(new ChunkPos(BlockPos.containing(CHILD_POSITION)), new ChunkPos(new BlockPos(-75, 63, -360)))) {
            forced.put(chunk.toLong(), player.serverLevel().getForcedChunks().contains(chunk.toLong()));
            player.serverLevel().setChunkForced(chunk.x, chunk.z, true);
        }
    }

    private void eggs(ServerPlayer player) {
        BlockPos base = player.blockPosition().above(4);
        for (int number = 21; number <= 30; number++) {
            String name = "businessman_" + number;
            BlockPos floor = base.east((number - 21) * 2);
            floor(player, floor);
            player.getInventory().setItem(8, new ItemStack(BuiltInRegistries.ITEM.get(id(name + "_spawn_egg"))));
            player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0, .5, 0), Direction.UP, floor, false));
            List<NpcResident> residents = player.serverLevel().getEntitiesOfClass(NpcResident.class, new AABB(floor.above()).inflate(.5),
                    resident -> BuiltInRegistries.ENTITY_TYPE.getKey(resident.getType()).equals(id(name)));
            expect("原生刷怪蛋实际产生居民" + number, 1, residents.size());
            NpcResident resident = residents.getFirst();
            expect("新生居民来源默认生命" + number, number <= 22 ? 20 : 100, resident.getMaxHealth());
            expect("新生居民来源默认攻击" + number, 1, resident.getAttributeValue(Attributes.ATTACK_DAMAGE));
            expectTrue("新生居民未经过商人档案初始化" + number, EntityAttributes.realmName(resident).isEmpty()
                    && resident.getData(NpcData.PROFILE).script(0).isEmpty());
            if (resident instanceof NpcResidentAnimated) {
                expect("首刻更新前仍为注册碰撞宽度" + number, .5F, resident.getBbWidth());
                expect("首刻更新前仍为注册碰撞高度" + number, 1.5F, resident.getBbHeight());
                resident.baseTick();
            }
            expect("居民碰撞宽度" + number, number <= 22 ? .4F : .6F, resident.getBbWidth());
            expect("居民碰撞高度" + number, number <= 22 ? 1.2F : 1.8F, resident.getBbHeight());
            expectTrue("居民火焰免疫" + number, resident.fireImmune());
            expectTrue("居民玩家伤害免疫" + number, !resident.hurt(player.damageSources().playerAttack(player), 4));
            expectTrue("居民渲染器注册" + number, minecraft.getEntityRenderDispatcher().getRenderer(resident) != null);
            resident.discard();
        }
        player.getInventory().clearContent();
    }

    private void lifecycle(ServerPlayer player) {
        for (int number : new int[]{21, 22, 23}) {
            NpcResident resident = NpcSetup.<NpcResident>entityType("businessman_" + number).get().create(player.level());
            resident.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.COMMAND, null);
            if (resident instanceof NpcResidentAnimated animated) {
                animated.setSmileTick(99);
                resident.setHealth(resident.getMaxHealth() * .89F);
                resident.baseTick();
                expect("低于九成血仍在百刻清计数" + number, 0, animated.smileTick());
                expect("低于九成血不请求微笑" + number, "undefined", animated.getSyncedAnimation());
                animated.setSmileTick(98);
                resident.setHealth(resident.getMaxHealth());
                resident.baseTick();
                expect("百刻前保留累计" + number, 99, animated.smileTick());
                resident.baseTick();
                expect("满血百刻清计数" + number, 0, animated.smileTick());
                expect("满血百刻产生来源微笑同步请求" + number, "animation.businessman" + number + ".smile", animated.getSyncedAnimation());
                resident.tick();
                expect("下一实体刻先消费上一刻同步动画" + number, "undefined", animated.getSyncedAnimation());
            }
            int deathTicks = number <= 22 ? 79 : 20;
            resident.setHealth(0);
            for (int tick = 1; tick < deathTicks; tick++) resident.baseTick();
            expectTrue("来源死亡移除前一刻仍存在" + number, !resident.isRemoved());
            resident.baseTick();
            expectTrue("来源死亡指定刻移除" + number, resident.isRemoved());
        }
    }

    private void template(ServerPlayer player) {
        CompoundTag saved = player.serverLevel().getStructureManager().getOrCreate(id("fanhuacheng03")).save(new CompoundTag());
        ListTag entities = saved.getList("entities", Tag.TAG_COMPOUND);
        CompoundTag childNbt = null;
        for (int index = 0; index < entities.size(); index++) {
            CompoundTag entity = entities.getCompound(index).getCompound("nbt");
            if (entity.getString("id").equals(id("businessman_21").toString())) childNbt = entity;
        }
        expectTrue("真实花繁城03模板含21号居民", childNbt != null);
        NpcResident resident = (NpcResident) EntityType.loadEntityRecursive(childNbt.copy(), player.serverLevel(), entity -> entity);
        resident.setUUID(UUID.randomUUID());
        resident.moveTo(CHILD_POSITION.x, CHILD_POSITION.y, CHILD_POSITION.z, 0, 0);
        floor(player, resident.blockPosition().below());
        floor(player, resident.blockPosition().south(2).below());
        player.serverLevel().addFreshEntity(resident);
        child = resident.getUUID();
        spawned.add(child);
        expect("模板姓名按中文保存", "小孩哥", resident.getDisplayName().getString());
        expect("模板实例生命上限覆盖默认值", 100, resident.getMaxHealth());
        expect("模板实例攻击覆盖默认值", 2, resident.getAttributeValue(Attributes.ATTACK_DAMAGE));
        expectTrue("结构组件读取到首任务", resident.getData(NpcData.PROFILE).script(0).contains("/yiqizhuatuzi/"));
        expectTrue("结构首任务物品已统一命名空间", !resident.getData(NpcData.PROFILE).script(0).contains("yvanchuxitong:"));
        AABB merchants = new AABB(-77, 61, -366, -62, 67, -353);
        List<UUID> previous = player.serverLevel().getEntitiesOfClass(Entity.class, merchants).stream().map(Entity::getUUID).toList();
        NpcStructureReleases.structureReleased(new StructureReleasedEvent(player.serverLevel(), "花繁城"));
        expectTrue("结构消费者为真实21号追加第二任务", resident.getData(NpcData.PROFILE).script(1).contains("/yiqizhuatuzier/"));
        expectTrue("结构消费者保留第一任务", resident.getData(NpcData.PROFILE).script(0).contains("/yiqizhuatuzi/"));
        for (Entity entity : player.serverLevel().getEntitiesOfClass(Entity.class, merchants)) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
            if (!previous.contains(entity.getUUID()) && (name.equals("businessman_09") || name.equals("businessman_10") || name.equals("businessman_11"))) {
                spawned.add(entity.getUUID());
            }
        }
        player.connection.teleport(CHILD_POSITION.x, CHILD_POSITION.y, CHILD_POSITION.z + 2, 180, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private void floor(ServerPlayer player, BlockPos pos) {
        floors.add(pos);
        floorStates.add(player.level().getBlockState(pos));
        player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
    }

    private int serverChildId() {
        int[] result = {-1};
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(child);
            if (entity != null) result[0] = entity.getId();
        });
        return result[0];
    }

    private boolean clientChild(int local) {
        if (minecraft.level.getEntity(serverChildId()) != null) return true;
        if (local > 200) { fail("居民未同步到客户端"); finish(); }
        return false;
    }

    private boolean dialogue(int local, String text) {
        if (minecraft.screen instanceof DialogueScreen && DialogueActions.snapshot(minecraft.player).getString("TXTxianshi").contains(text)) return true;
        if (local > 200) { fail("居民自然右键未打开预期中文对话：" + text); finish(); }
        return false;
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        for (UUID uuid : spawned) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        for (int index = 0; index < floors.size(); index++) player.level().setBlock(floors.get(index), floorStates.get(index), 3);
        for (Map.Entry<Long, Boolean> entry : forced.entrySet()) {
            ChunkPos chunk = new ChunkPos(entry.getKey());
            player.serverLevel().setChunkForced(chunk.x, chunk.z, entry.getValue());
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setData(QuestData.JOURNAL, journal);
        player.setData(QuestData.SESSION, session);
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name); }
}
