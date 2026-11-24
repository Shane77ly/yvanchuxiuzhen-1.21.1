package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.world.WorldSetup;
import net.yvanchuxiuzhen.system.world.content.CompassAnchor;
import net.yvanchuxiuzhen.system.world.content.RiftBlockEntity;
import net.yvanchuxiuzhen.system.world.content.RiftItem;
import net.yvanchuxiuzhen.system.world.content.SpiritCompassItem;
import net.yvanchuxiuzhen.system.world.data.WorldData;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;

/** 在独立冒烟存档内，以真实玩家主手验证遗迹搜索、裂隙计划刻、指引和保存重进。 */
final class WorldExplorationCheck extends SystemCheck {
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian"));
    private static final BlockPos RIFT = new BlockPos(28, 64, 22);
    private static final BlockPos BOAT = new BlockPos(22, 62, 15);
    private static final Vec3 STAND = new Vec3(44, 63, 22);
    private static final Vec3 MIDPOINT = new Vec3(120, 65.5, 18);
    private static final Vec3 SCROLL = new Vec3(212, 67, 14);
    private static final String NAVIGATION = "structure_navigation";
    private static final Field RELEASED = field("net.yvanchuxiuzhen.system.world.StructureExplorationData", "released");
    private static final Method WORLD_DATA = method("net.yvanchuxiuzhen.system.world.StructureExplorationData", "get", MinecraftServer.class);
    private static final Field CATALOG = field("net.yvanchuxiuzhen.system.world.StructureCatalog", "ENTRIES");

    private final Screenshots screenshots;
    private final List<String> diagnostics;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<UUID> halos = new ArrayList<>();
    private final Map<Long, Boolean> forced = new LinkedHashMap<>();
    private final Map<Long, String> riftTicks = new LinkedHashMap<>();
    private final Set<UUID> previousBoatEntities = new HashSet<>();
    private ResourceKey<Level> originalDimension;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private int originalSelected;
    private CompoundTag originalAbilities;
    private CompoundTag originalNavigation;
    private boolean hadNavigation;
    private String originalProgress;
    private String stagedProgress;
    private String persistedProgress;
    private CompassAnchor persistedCompass;
    private StructureTemplate previousBoat;
    private Vec3i boatSize;
    private AABB boatArea;
    private BlockPos boatSample;
    private BlockState expectedBoat;
    private BlockState previousRift;
    private int haloEntityId;
    private long creationTime;
    private long firstScheduledTime;
    private boolean observing;
    private boolean prepared;
    private boolean originalOverworldForced;

    WorldExplorationCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("遗迹寻路与裂隙", report, levelId);
        this.screenshots = screenshots;
        diagnostics = report;
        NeoForge.EVENT_BUS.addListener(this::observe);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            abort("步骤" + step + "异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                return true;
            }
            case 1 -> {
                if (!waitFor(local, "人间界、寻灵盘与两维度观察区块就绪", minecraft.screen == null && minecraft.level != null && minecraft.level.dimension().equals(RENJIAN)
                        && minecraft.player.getMainHandItem().is(compass())
                        && read(player -> player.server.getLevel(RENJIAN).getChunkSource().isPositionTicking(ChunkPos.asLong(RIFT))
                                && player.server.getLevel(RENJIAN).isPositionEntityTicking(RIFT)
                                && player.server.getLevel(RENJIAN).isPositionEntityTicking(BlockPos.containing(MIDPOINT))
                                && player.server.overworld().isPositionEntityTicking(BlockPos.containing(STAND))), 400)) return false;
                onServer(player -> {
                    verifyNearest(player, player.serverLevel(), MIDPOINT, new Vec3(RIFT.getX(), RIFT.getY(), RIFT.getZ()), "平距保留目录靠前的孤舟");
                    verifyNearest(player, player.serverLevel(), MIDPOINT.add(1, 0, 0), SCROLL, "严格更近时替换为残卷");
                    expect("远距离搜索不登记遗迹", stagedProgress, progress(player.server));
                    ModifierKeys.update(player, true, false);
                    use(player);
                    expect("未记录坐标的Ctrl回程", new Vec3(-70, 63, -275), player.position());
                    expectTrue("Ctrl回程没有普通使用冷却", !player.getCooldowns().isOnCooldown(compass()));
                    move(player, STAND);
                    ModifierKeys.update(player, false, false);
                    creationTime = player.serverLevel().getGameTime();
                    use(player);
                    firstScheduledTime = ((LevelChunkTicks<Block>) player.serverLevel().getChunkAt(RIFT).getBlockTicks()).getAll()
                            .filter(tick -> tick.pos().equals(RIFT) && tick.type() == block("liexi")).findFirst().orElseThrow().triggerTick();
                    expect("放置时原生计划刻延迟为2", 2, firstScheduledTime - creationTime);
                    RiftBlockEntity rift = rift(player.serverLevel());
                    expectTrue("恰好16格的原生主手使用生成裂隙并立即登记", rift != null
                            && progress(player.server).equals(stagedProgress + "%shuishangguzhou"));
                    expect("裂隙保存实际结构名", "水上孤舟", rift.getPersistentData().getString("jiegouname"));
                    expect("裂隙保存当前玩家显示名", player.getDisplayName().getString(), rift.getPersistentData().getString("playername"));
                    expectTrue("正常使用添加100刻冷却", player.getCooldowns().isOnCooldown(compass())
                            && Math.abs(player.getCooldowns().getCooldownPercent(compass(), 0) - 1) < 1e-6);
                    List<GuidanceHalo> active = player.serverLevel().getEntitiesOfClass(GuidanceHalo.class, new AABB(STAND, STAND).inflate(4));
                    expect("正常使用只召出一个光环", 1, active.size());
                    GuidanceHalo halo = active.getFirst();
                    halos.add(halo.getUUID());
                    haloEntityId = halo.getId();
                    CompoundTag navigation = halo.getPersistentData().getCompound(NAVIGATION);
                    expectTrue("召唤光环保留原生主人与导航UUID", halo.isOwnedBy(player)
                            && navigation.getString("belongToMasterUUID").equals(player.getStringUUID()));
                    expect("光环写入孤舟目标", new Vec3(28, 64, 22), target(halo));
                    use(player);
                    expect("原生玩家冷却阻断重复召唤", 1,
                            player.serverLevel().getEntitiesOfClass(GuidanceHalo.class, new AABB(STAND, STAND).inflate(4)).size());
                    observing = true;
                });
                return true;
            }
            case 2 -> {
                if (!waitFor(local, "光环实体与裂隙动画同步到客户端", local >= 6
                        && minecraft.level.getEntity(haloEntityId) instanceof GuidanceHalo
                        && minecraft.level.getBlockEntity(RIFT) instanceof RiftBlockEntity, 60)) return false;
                GuidanceHalo halo = (GuidanceHalo) minecraft.level.getEntity(haloEntityId);
                expectTrue("光环已注册专属渲染器", minecraft.getEntityRenderDispatcher().getRenderer(halo).getClass().getSimpleName().equals("GuidanceHaloRenderer"));
                expectTrue("裂隙已注册方块渲染器", minecraft.getBlockEntityRenderDispatcher().getRenderer(minecraft.level.getBlockEntity(RIFT))
                        .getClass().getSimpleName().equals("RiftRenderer"));
                expectTrue("两件物品已安装Geo渲染提供者", ((SpiritCompassItem) compass()).getRenderProvider() != null
                        && ((RiftItem) item("liexi")).getRenderProvider() != null);
                expect("寻灵盘中文名称", "寻灵盘", minecraft.player.getMainHandItem().getHoverName().getString());
                expect("裂隙中文名称", "裂隙", new ItemStack(item("liexi")).getHoverName().getString());
                expect("距离动态中文", "距离：16", I18n.get("message.yvanchuxiuzhen.structure_distance", "16"));
                if (minecraft.screen == null) screenshots.take("world_exploration_compass_rift.png");
                return true;
            }
            case 3 -> {
                if (!waitFor(local, "82刻真实结构释放完成", read(player -> player.server.getLevel(RENJIAN).getGameTime() - creationTime >= 86), 240)) return false;
                onServer(player -> {
                    observing = false;
                    diagnostics.add("TRACE 裂隙逐刻诊断 creation=" + creationTime + " firstDue=" + firstScheduledTime + " trace=" + riftTicks);
                    expect("第1刻尚未推进计划计数", "true:0.0", riftTicks.get(1L));
                    expect("第2刻首次推进计数", "true:1.0", riftTicks.get(2L));
                    expect("第4刻第二次推进计数", "true:2.0", riftTicks.get(4L));
                    expect("第80刻结束四十次等待但尚未释放", "false:0.0", riftTicks.get(80L));
                    expect("第82刻移除裂隙", "released", riftTicks.get(82L));
                    expect("裂隙实际调用管理命令放置来源模板", expectedBoat, player.serverLevel().getBlockState(boatSample));
                    verifyNearest(player, player.server.overworld(), new Vec3(44, 64, 22), SCROLL,
                            "另一维度搜索读取同一份已登记孤舟进度");
                    expectTrue("主世界未重复生成已登记的孤舟裂隙", !player.server.overworld().getBlockState(RIFT).is(block("liexi")));
                    discardHalos(player.serverLevel());
                    player.getCooldowns().removeCooldown(compass());
                    move(player, STAND);
                    ModifierKeys.update(player, true, true);
                    use(player);
                    expect("Alt与Ctrl同时按住时优先Alt", new Vec3(0, 32, 0), player.position());
                    persistedCompass = anchor(player.getMainHandItem());
                    expectTrue("Alt把精确回程坐标写入实际手持栈", persistedCompass.present() && persistedCompass.x() == STAND.x
                            && persistedCompass.y() == STAND.y && persistedCompass.z() == STAND.z);
                    expectTrue("Alt不添加普通冷却", !player.getCooldowns().isOnCooldown(compass()));
                    ModifierKeys.update(player, true, false);
                    use(player);
                    expect("Ctrl从同一物品实例返回保存坐标", STAND, player.position());
                    expect("Ctrl保留回程记录", persistedCompass, anchor(player.getMainHandItem()));
                    expectTrue("寻灵盘不消耗数量或耐久", player.getMainHandItem().getCount() == 1 && player.getMainHandItem().getDamageValue() == 0);
                    ModifierKeys.update(player, false, false);
                    persistedProgress = progress(player.server);
                    player.teleportTo(player.server.overworld(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
                });
                return true;
            }
            case 4 -> {
                if (local < 20) return false;
                leaveWorld();
                return true;
            }
            case 5 -> { return reopened(local); }
            case 6 -> { return inWorld(local); }
            case 7 -> {
                onServer(player -> {
                    expect("正常保存重进保留全服遗迹进度", persistedProgress, progress(player.server));
                    expect("正常保存重进保留寻灵盘实例回程", persistedCompass, anchor(player.getInventory().getItem(0)));
                    cleanup(player);
                });
                finish();
                return true;
            }
            default -> throw new IllegalStateException("未知遗迹检查步骤 " + step);
        }
    }

    private void prepare(ServerPlayer player) {
        originalDimension = player.level().dimension();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalSelected = player.getInventory().selected;
        originalAbilities = new CompoundTag();
        player.getAbilities().addSaveData(originalAbilities);
        hadNavigation = player.getPersistentData().contains(NAVIGATION, Tag.TAG_COMPOUND);
        originalNavigation = player.getPersistentData().getCompound(NAVIGATION).copy();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        originalProgress = progress(player.server);
        ChunkPos overworldChunk = new ChunkPos(BlockPos.containing(STAND));
        originalOverworldForced = player.server.overworld().getForcedChunks().contains(overworldChunk.toLong());
        player.server.overworld().setChunkForced(overworldChunk.x, overworldChunk.z, true);
        player.server.overworld().getChunk(overworldChunk.x, overworldChunk.z);
        prepared = true;
        List<?> catalog;
        try {
            catalog = (List<?>) CATALOG.get(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
        expect("实际有效目录有28处遗迹", 28, catalog.size());
        List<String> ids = new ArrayList<>();
        for (Object entry : catalog) ids.add(entryValue(entry, "id"));
        expect("目录身份没有重复", 28, new HashSet<>(ids).size());
        stagedProgress = "wu%kong";
        for (String id : ids) if (!id.equals("shuishangguzhou") && !id.equals("konggucanjvanqiyi")) stagedProgress += "%" + id;
        setProgress(player.server, stagedProgress);
        ServerLevel level = player.server.getLevel(RENJIAN);
        StructureTemplate template = level.getStructureManager().getOrCreate(id("shoponwater01"));
        boatSize = template.getSize();
        if (boatSize.getX() == 0 || boatSize.getY() == 0 || boatSize.getZ() == 0) throw new IllegalStateException("孤舟模板为空");
        boatArea = new AABB(Vec3.atLowerCornerOf(BOAT), Vec3.atLowerCornerOf(BOAT.offset(boatSize))).inflate(1);
        for (int x = BOAT.getX() >> 4; x <= (BOAT.getX() + boatSize.getX()) >> 4; x++) {
            for (int z = BOAT.getZ() >> 4; z <= (BOAT.getZ() + boatSize.getZ()) >> 4; z++) force(level, x, z);
        }
        force(level, BlockPos.containing(STAND).getX() >> 4, BlockPos.containing(STAND).getZ() >> 4);
        force(level, BlockPos.containing(MIDPOINT).getX() >> 4, BlockPos.containing(MIDPOINT).getZ() >> 4);
        previousBoat = new StructureTemplate();
        previousBoat.fillFromWorld(level, BOAT, boatSize, false, null);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, boatArea)) previousBoatEntities.add(entity.getUUID());
        previousRift = level.getBlockState(RIFT);
        selectSample(player, template);
        level.setBlock(boatSample, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(RIFT, Blocks.AIR.defaultBlockState(), 3);
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(compass()));
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(level, STAND.x, STAND.y, STAND.z, 90, 20);
        player.inventoryMenu.broadcastChanges();
    }

    private void selectSample(ServerPlayer player, StructureTemplate template) {
        CompoundTag tag = template.save(new CompoundTag());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        for (Tag raw : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = (CompoundTag) raw;
            BlockState state = NbtUtils.readBlockState(player.registryAccess().lookupOrThrow(Registries.BLOCK), palette.getCompound(block.getInt("state")));
            ListTag offset = block.getList("pos", Tag.TAG_INT);
            BlockPos pos = BOAT.offset(offset.getInt(0), offset.getInt(1), offset.getInt(2));
            if (!state.isAir() && !state.hasBlockEntity() && state.isCollisionShapeFullBlock(player.serverLevel(), pos)
                    && !state.is(Blocks.STRUCTURE_BLOCK) && !pos.equals(RIFT)) {
                boatSample = pos;
                expectedBoat = state;
                return;
            }
        }
        throw new IllegalStateException("孤舟模板中没有可观察的实方块");
    }

    private void verifyNearest(ServerPlayer player, ServerLevel level, Vec3 position, Vec3 expected, String label) {
        GuidanceHalo halo = WorldSetup.<GuidanceHalo>entityType("guanghuan").get().create(level);
        GuidanceHalo other = WorldSetup.<GuidanceHalo>entityType("guanghuan").get().create(level);
        for (GuidanceHalo specimen : List.of(halo, other)) {
            specimen.moveTo(position.x, position.y, position.z);
            specimen.getPersistentData().put(NAVIGATION, new CompoundTag());
            level.addFreshEntity(specimen);
        }
        level.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withLevel(level)
                .withPosition(position).withPermission(4).withSuppressedOutput(), "searchingforstructure " + player.getStringUUID() + " 未使用团队");
        expect(label, expected, target(halo));
        expect("搜索同时写入附近其他光环目标", expected, target(other));
        halo.discard();
        other.discard();
    }

    private void observe(LevelTickEvent.Post event) {
        if (!observing || !(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(RENJIAN)) return;
        long elapsed = level.getGameTime() - creationTime;
        if (elapsed < 0 || elapsed > 85) return;
        RiftBlockEntity block = rift(level);
        riftTicks.put(elapsed, block == null ? "released" : block.getPersistentData().getBoolean("kaishi")
                + ":" + block.getPersistentData().getDouble("MobcomeTick"));
    }

    private void force(ServerLevel level, int x, int z) {
        long key = ChunkPos.asLong(x, z);
        forced.putIfAbsent(key, level.getForcedChunks().contains(key));
        level.setChunkForced(x, z, true);
        level.getChunk(x, z);
    }

    private void cleanup(ServerPlayer player) {
        if (!prepared) return;
        observing = false;
        ServerLevel level = player.server.getLevel(RENJIAN);
        discardHalos(level);
        if (boatArea != null) {
            for (Entity entity : level.getEntitiesOfClass(Entity.class, boatArea)) {
                if (!(entity instanceof ServerPlayer) && !previousBoatEntities.contains(entity.getUUID())) entity.discard();
            }
        }
        if (previousBoat != null) previousBoat.placeInWorld(level, BOAT, BOAT, new StructurePlaceSettings().setIgnoreEntities(true), level.random, 3);
        if (previousRift != null) level.setBlock(RIFT, previousRift, 3);
        for (Map.Entry<Long, Boolean> entry : forced.entrySet()) {
            ChunkPos chunk = new ChunkPos(entry.getKey());
            level.setChunkForced(chunk.x, chunk.z, entry.getValue());
        }
        ChunkPos overworldChunk = new ChunkPos(BlockPos.containing(STAND));
        player.server.overworld().setChunkForced(overworldChunk.x, overworldChunk.z, originalOverworldForced);
        setProgress(player.server, originalProgress);
        ModifierKeys.update(player, false, false);
        if (hadNavigation) player.getPersistentData().put(NAVIGATION, originalNavigation);
        else player.getPersistentData().remove(NAVIGATION);
        player.getCooldowns().removeCooldown(compass());
        player.teleportTo(player.server.getLevel(originalDimension), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = originalSelected;
        player.setGameMode(originalMode);
        player.getAbilities().loadSaveData(originalAbilities);
        player.onUpdateAbilities();
        player.inventoryMenu.broadcastChanges();
        prepared = false;
    }

    private void discardHalos(ServerLevel level) {
        for (UUID uuid : halos) {
            Entity halo = level.getEntity(uuid);
            if (halo != null) halo.discard();
        }
    }

    private boolean waitFor(int local, String label, boolean ready, int timeout) {
        if (ready) return true;
        if (local > timeout) abort(label + "超时");
        return false;
    }

    private void abort(String message) {
        fail(message);
        onServer(this::cleanup);
        finish();
    }

    private <T> T read(Function<ServerPlayer, T> action) {
        return minecraft.getSingleplayerServer().submit(() -> action.apply(minecraft.getSingleplayerServer().getPlayerList().getPlayers().getFirst())).join();
    }

    private static void move(ServerPlayer player, Vec3 position) {
        player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void use(ServerPlayer player) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static Vec3 target(GuidanceHalo halo) {
        CompoundTag navigation = halo.getPersistentData().getCompound(NAVIGATION);
        return new Vec3(navigation.getDouble("mubiaoX"), navigation.getDouble("mubiaoY"), navigation.getDouble("mubiaoZ"));
    }

    private static RiftBlockEntity rift(ServerLevel level) {
        return level.getBlockEntity(RIFT) instanceof RiftBlockEntity block ? block : null;
    }
    private static CompassAnchor anchor(ItemStack stack) { return stack.getOrDefault(WorldData.COMPASS_ANCHOR.get(), CompassAnchor.EMPTY); }
    private static Item compass() { return item("ling_14item"); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.get(id(path)); }
    private static Block block(String path) { return BuiltInRegistries.BLOCK.get(id(path)); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }

    private static SavedData data(MinecraftServer server) {
        try {
            return (SavedData) WORLD_DATA.invoke(null, server);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static String progress(MinecraftServer server) {
        try {
            return (String) RELEASED.get(data(server));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static void setProgress(MinecraftServer server, String progress) {
        try {
            SavedData data = data(server);
            RELEASED.set(data, progress);
            data.setDirty();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static String entryValue(Object entry, String name) {
        try {
            Method method = entry.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (String) method.invoke(entry);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static Field field(String owner, String name) {
        try {
            Field field = Class.forName(owner).getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static Method method(String owner, String name, Class<?> parameter) {
        try {
            Method method = Class.forName(owner).getDeclaredMethod(name, parameter);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
