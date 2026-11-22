package net.yvanchuxiuzhen.smoke;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;

/** 从归档目标门到已保存正厅的生存路线。 */
final class RenjianResidenceCheck extends SystemCheck {
    static final String SELECTOR = "renjianresidence";
    static final String SAVE_ID = "renjian-residence-replay";
    static final String REPLAY_MARKER = ".residence-replay-started";
    private static final String ARCHIVE_LEVEL_SHA256 = "a8497ab8ac88828eb6f023cf0457e1c43cec4c24e7d4804c01ba0845d1106488";
    private static final UUID PLAYER = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final long SEED = -6573343391607276249L;
    private static final BlockPos STAGING = new BlockPos(8, 160, 7);
    private static final BlockPos OVERWORLD_GATE = STAGING.south();
    private static final BlockPos EXIT = new BlockPos(1, -16, -2);
    private static final BlockPos HALL = new BlockPos(0, 34, -15);
    private static final BlockPos DAO_PROBE = new BlockPos(0, 36, -21);
    private static final BlockPos TABLE = new BlockPos(0, 35, -27);
    private static final BlockPos SHELL_LOW = new BlockPos(2, 31, 4);
    private static final BlockPos SHELL_HIGH = SHELL_LOW.above();
    private static final ResourceLocation PORTAL = ResourceLocation.parse("yvanchuxiuzhen:dimensionrenjian_portal");
    private static final ResourceLocation PORTAL_FRAME = ResourceLocation.parse("yvanchuxiuzhen:tiandaodimensionblock");
    private static final ResourceLocation TABLE_ID = ResourceLocation.parse("yvanchuxiuzhen:canwutai_block");
    private static final int PLANNED_BREAKS = 118;
    private static final int ROUTE_TICKS = 9000;
    private static final int WALK_TICKS = 160;
    private static final int MINE_TICKS = 120;

    private record Action(BlockPos target, ResourceLocation block) {
    }

    private final List<Action> route = new ArrayList<>();
    private final Field teleportSequence = findTeleportSequence();
    private final Screenshots screenshots;
    private final Runnable checkpoint;
    private int actionIndex;
    private int actionStart;
    private int breaks;
    private int shellBreaks;
    private boolean digging;
    private Direction face;
    private boolean staged;
    private boolean sourceContact;
    private BlockPos lastSourceContact;
    private int stagingTeleport;
    private int portalTeleport;
    private int portalFrameStart = -1;
    private BlockPos stand;
    private Vec3 priorPosition;

    RenjianResidenceCheck(List<String> report, String levelId, Screenshots screenshots, Runnable checkpoint) {
        super("人间道归档门至居所", report, levelId);
        this.screenshots = screenshots;
        this.checkpoint = checkpoint;
    }

    static Path replaySave(Minecraft minecraft) throws IOException {
        Path gameDir = minecraft.gameDirectory.toPath().toAbsolutePath().normalize();
        Path save = gameDir.resolve("saves").resolve(SAVE_ID);
        if (!gameDir.endsWith(Path.of("run", "smoke")) || !save.toRealPath().equals(save)
                || !Files.isRegularFile(save.resolve("level.dat")) || Files.exists(save.resolve("session.lock"))
                || Files.exists(save.resolve(REPLAY_MARKER))) {
            throw new IllegalStateException("居所重放需要未运行的专用归档副本 " + save);
        }
        if (minecraft.options.renderDistance().get() != 2 || minecraft.options.simulationDistance().get() != 5
                || !"zh_cn".equals(minecraft.options.languageCode) || minecraft.options.tutorialStep != TutorialSteps.NONE
                || minecraft.options.autoJump().get() || minecraft.options.toggleCrouch().get()) {
            throw new IllegalStateException("居所重放需要视距 2/5、中文、无教程，并关闭自动跳跃和切换式潜行");
        }
        try {
            byte[] levelData = Files.readAllBytes(save.resolve("level.dat"));
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(levelData));
            if (!ARCHIVE_LEVEL_SHA256.equals(hash)) throw new IllegalStateException("居所归档 level.dat 哈希不符 " + save);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
        return save;
    }

    @Override
    protected boolean step(int step, int local) {
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isAlive()) return stop("玩家缺失或死亡");
        return switch (step) {
            case 0 -> {
                route.addAll(readRoute());
                if (minecraft.screen != null || !serverHas(this::prepare)) yield stop("归档玩家、源门或目标门预检不符");
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                expectTrue("PREP 主世界旧门旁一格支撑及铁镐为夹具，置生存玩家于主世界门外", true);
                checkpoint.run();
                yield true;
            }
            case 1 -> crossPortal(player, local);
            case 2 -> followRoute(player, local);
            case 3 -> recordArrival(player, local);
            default -> throw new IllegalStateException("居所重放步骤无效 " + step);
        };
    }

    private boolean crossPortal(LocalPlayer player, int local) {
        if (local > 600) return stop("目标门等待超时，客户端维度=" + minecraft.level.dimension().location()
                + "，站位=" + player.position() + "，源门取证=" + sourceContact + "，最后接触格=" + lastSourceContact);
        if (minecraft.level.dimension() == Level.OVERWORLD) {
            if (!staged) {
                staged = minecraft.screen == null && at(player.position(), STAGING) && player.onGround() && toolDamage(player, 0)
                        && serverHas(p -> p.serverLevel().dimension() == Level.OVERWORLD && p.onGround()
                                && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                                && at(p.position(), STAGING) && toolDamage(p, 0));
            }
            if (staged) {
                if (!sourceContact && serverHas(this::sampleSourceContact)) {
                    sourceContact = true;
                    expectTrue("SOURCE_CONTACT 服务端接触格=" + lastSourceContact + "，传送序号=" + stagingTeleport
                            + "，客户端站位=" + player.position() + "，前进键=" + minecraft.options.keyUp.isDown()
                            + "，前进量=" + player.input.forwardImpulse, true);
                    checkpoint.run();
                }
                drive(player, OVERWORLD_GATE, false);
            }
            return false;
        }
        release();
        if (minecraft.screen != null || minecraft.getOverlay() != null) return false;
        if (!minecraft.level.dimension().equals(DimensionPortalSpec.RENJIAN.dimension())) {
            return stop("落入非预期维度 " + minecraft.level.dimension().location());
        }
        if (!staged || !sourceContact) {
            return stop("已进入人间道但缺少归档源门输入取证，站位=" + player.position() + "，最后接触格=" + lastSourceContact);
        }
        BlockPos arrived = player.blockPosition();
        boolean matching = (arrived.equals(EXIT) || arrived.equals(EXIT.east())) && toolDamage(player, 0)
                && blockId(minecraft.level, arrived).equals(PORTAL)
                && serverHas(p -> survival(p) && teleportSequence(p) == portalTeleport
                        && p.blockPosition().equals(arrived) && p.isOnPortalCooldown()
                        && blockId(p.serverLevel(), arrived).equals(PORTAL) && toolDamage(p, 0));
        if (!matching) {
            portalFrameStart = -1;
            return false;
        }
        if (portalFrameStart < 0) {
            portalFrameStart = local;
            player.setYRot(arrived.equals(EXIT) ? 90 : -90);
            player.setXRot(0);
        }
        if (local - portalFrameStart < 8) return false;
        stand = arrived;
        priorPosition = player.position();
        if (arrived.equals(EXIT.east())) route.addFirst(new Action(EXIT.east().north(), null));
        screenshots.take("renjian-residence-portal-arrived.png");
        expectTrue("PORTAL 原生碰撞落入已有门洞 " + arrived + "，服务端传送序号=" + portalTeleport, true);
        checkpoint.run();
        return true;
    }

    private boolean sampleSourceContact(ServerPlayer player) {
        if (player.serverLevel().dimension() != Level.OVERWORLD || teleportSequence(player) != stagingTeleport) return false;
        PortalProcessor processor = player.portalProcess;
        lastSourceContact = processor == null ? null : processor.getEntryPosition();
        BlockPos contact = lastSourceContact;
        // 原版按玩家包围盒逐格触发门户，接触格可位于脚上方，不等同于门洞底角。
        if (contact == null || contact.getZ() != OVERWORLD_GATE.getZ() || contact.getX() < OVERWORLD_GATE.getX()
                || contact.getX() > OVERWORLD_GATE.getX() + 1 || contact.getY() < OVERWORLD_GATE.getY()
                || contact.getY() > OVERWORLD_GATE.getY() + 2) return false;
        BlockState state = player.serverLevel().getBlockState(contact);
        return isPortalCell(state) && state.getBlock() instanceof NetherPortalBlock portal && processor.isSamePortal(portal);
    }

    private boolean followRoute(LocalPlayer player, int local) {
        if (local > ROUTE_TICKS) return stop("路线总时限，站位=" + player.position());
        if (!minecraft.level.dimension().equals(DimensionPortalSpec.RENJIAN.dimension()) || minecraft.screen != null
                || player.isPassenger() || player.getAbilities().flying || !player.getMainHandItem().is(Items.IRON_PICKAXE)) {
            return stop("开路时玩家维度、界面、移动方式或铁镐变化");
        }
        if (priorPosition.distanceToSqr(player.position()) > 1.25 * 1.25) return stop("开路时发生异常位移 " + player.position());
        priorPosition = player.position();
        if (actionIndex == route.size()) {
            release();
            player.setYRot(180);
            player.setXRot(0);
            return true;
        }
        Action action = route.get(actionIndex);
        boolean walk = action.block() == null;
        BlockPos end = walk ? action.target() : stand;
        if (!inStep(player.position(), stand, end, actionIndex == 0)) return stop("偏离路线或跌落，站位=" + player.position());
        if (local - actionStart > (walk ? WALK_TICKS : MINE_TICKS)) {
            return stop("动作超时，站位=" + player.position() + " 下一操作=" + action);
        }
        boolean done = walk ? drive(player, end, true) && toolDamage(player, breaks)
                && serverHas(p -> onRoute(p) && p.onGround() && at(p.position(), end) && toolDamage(p, breaks)) : mine(player, action);
        if (done) {
            stand = end;
            actionIndex++;
            actionStart = local;
        }
        return false;
    }

    private boolean recordArrival(LocalPlayer player, int local) {
        if (local < 20) return false;
        if (minecraft.screen != null || !player.onGround() || breaks != PLANNED_BREAKS || shellBreaks != 2
                || !at(player.position(), HALL) || !toolDamage(player, breaks)
                || !serverHas(p -> onRoute(p) && at(p.position(), HALL) && p.onGround() && toolDamage(p, breaks)
                        && p.serverLevel().getBlockState(DAO_PROBE).is(Blocks.EMERALD_BLOCK)
                        && blockId(p.serverLevel(), TABLE).equals(TABLE_ID))) {
            return stop("原正厅落点、原摆件、防重放探针或破块计数不符");
        }
        screenshots.take("renjian-residence-arrived.png");
        expectTrue("ARRIVED 双端玩家生存到达保存的原正厅 " + HALL + "，总破块=" + breaks + "，其中模板外壳=" + shellBreaks, true);
        checkpoint.run();
        finish();
        return false;
    }

    private boolean prepare(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        ServerLevel renjian = player.server.getLevel(DimensionPortalSpec.RENJIAN.dimension());
        if (!PLAYER.equals(player.getUUID()) || player.serverLevel() != overworld || overworld.getSeed() != SEED
                || player.position().distanceToSqr(-4.5, 87, -7.5) > 0.25 || !player.getInventory().getItem(0).isEmpty()
                || !hasArchivedGate(overworld, OVERWORLD_GATE) || renjian == null || !hasArchivedGate(renjian, EXIT)
                || !overworld.isEmptyBlock(STAGING.below()) || !overworld.isEmptyBlock(STAGING) || !overworld.isEmptyBlock(STAGING.above())) {
            return false;
        }
        overworld.setBlockAndUpdate(STAGING.below(), Blocks.STONE.defaultBlockState());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE));
        player.teleportTo(overworld, STAGING.getX() + 0.5, STAGING.getY(), STAGING.getZ() + 0.5, 0, 0);
        stagingTeleport = teleportSequence(player);
        portalTeleport = stagingTeleport == Integer.MAX_VALUE - 1 ? 0 : stagingTeleport + 1;
        return true;
    }

    private boolean drive(LocalPlayer player, BlockPos target, boolean slow) {
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        boolean advance = dx * dx + dz * dz > 0.16 * 0.16;
        if (advance) player.setYRot((float) Math.toDegrees(Mth.atan2(-dx, dz)));
        player.setXRot(0);
        boolean jump = slow && advance && target.getY() - player.getY() > 0.55;
        minecraft.options.keyShift.setDown(slow);
        minecraft.options.keyUp.setDown(advance);
        minecraft.options.keyJump.setDown(jump);
        if (advance || !at(player.position(), target) || !player.onGround()) return false;
        release();
        return true;
    }

    private boolean mine(LocalPlayer player, Action action) {
        BlockPos target = action.target();
        release();
        if (digging && minecraft.level.isEmptyBlock(target)) {
            if (!toolDamage(player, breaks + 1) || !serverHas(p -> onRoute(p) && p.onGround() && at(p.position(), stand)
                    && p.serverLevel().isEmptyBlock(target) && toolDamage(p, breaks + 1))) return false;
            minecraft.gameMode.stopDestroyBlock();
            digging = false;
            SmokeMod.residenceDigging = false;
            breaks++;
            if (target.equals(SHELL_LOW) || target.equals(SHELL_HIGH)) shellBreaks++;
            expectTrue("MINE " + breaks + " 双端确认原生挖除 " + action.block() + " " + target
                    + (target.equals(SHELL_LOW) || target.equals(SHELL_HIGH) ? " 居所外壳" : ""), true);
            checkpoint.run();
            return true;
        }
        if (!blockId(minecraft.level, target).equals(action.block())) return stop("预期矿块不符 " + action);
        if (!digging) {
            if (!at(player.position(), stand) || !player.onGround() || player.getDeltaMovement().horizontalDistanceSqr() > 0.001
                    || player.isShiftKeyDown() || !toolDamage(player, breaks)) return false;
            if (!serverHas(p -> onRoute(p) && p.onGround() && at(p.position(), stand) && toolDamage(p, breaks)
                    && blockId(p.serverLevel(), target).equals(action.block()))) return false;
            BlockHitResult hit = visibleFace(player, target);
            if (hit == null) return false;
            Vec3 aim = hit.getLocation().subtract(player.getEyePosition());
            player.setYRot((float) Math.toDegrees(Mth.atan2(-aim.x, aim.z)));
            player.setXRot((float) -Math.toDegrees(Mth.atan2(aim.y, aim.horizontalDistance())));
            face = hit.getDirection();
            digging = minecraft.gameMode.startDestroyBlock(target, face);
            SmokeMod.residenceDigging = digging;
            if (digging && breaks == 0) {
                expectTrue("WALK_PREFIX 已生存离开旧门并走至第一处障碍，累计破块 0，此后进入开路段", true);
                checkpoint.run();
            }
        } else {
            minecraft.gameMode.continueDestroyBlock(target, face);
        }
        return false;
    }

    private BlockHitResult visibleFace(LocalPlayer player, BlockPos target) {
        Vec3 eye = player.getEyePosition();
        for (Direction face : Direction.values()) {
            Vec3 end = Vec3.atCenterOf(target).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.499));
            if (eye.distanceToSqr(end) > 16) continue;
            BlockHitResult hit = minecraft.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(target)) return hit;
        }
        return null;
    }

    private static List<Action> readRoute() {
        List<Action> result = new ArrayList<>();
        String resource = "/assets/yvanchuxiuzhen_smoke/renjian_residence.route";
        try (InputStream stream = RenjianResidenceCheck.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("缺少 " + resource);
            try (BufferedReader lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.US_ASCII))) {
                for (String line; (line = lines.readLine()) != null;) {
                    String[] fields = line.split(" ");
                    if (!(fields.length == 4 && fields[0].equals("M")) && !(fields.length == 5 && fields[0].equals("B"))) {
                        throw new IOException("归档路线格式无效 " + line);
                    }
                    result.add(new Action(new BlockPos(Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3])),
                            fields.length == 5 ? ResourceLocation.parse(fields[4]) : null));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("读取归档路线失败", exception);
        }
        if (result.stream().filter(action -> action.block() != null).count() != PLANNED_BREAKS
                || !result.getLast().target().equals(HALL)) throw new IllegalStateException("归档路线长度或终点不符");
        return result;
    }

    private boolean serverHas(Predicate<ServerPlayer> check) {
        boolean[] result = new boolean[1];
        onServer(player -> result[0] = check.test(player));
        return result[0];
    }

    /** 普通移动不改变位置同步包序号。门后任何额外传送或服务端位置修正都会使后续断言失效。 */
    private boolean onRoute(ServerPlayer player) {
        return survival(player) && teleportSequence(player) == portalTeleport;
    }

    private static Field findTeleportSequence() {
        try {
            Field field = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingTeleport");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法检查服务端位置同步序号", exception);
        }
    }

    private int teleportSequence(ServerPlayer player) {
        try {
            return teleportSequence.getInt(player.connection);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean survival(ServerPlayer player) {
        return PLAYER.equals(player.getUUID()) && player.isAlive() && !player.isPassenger() && !player.getAbilities().flying
                && player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                && player.serverLevel().dimension().equals(DimensionPortalSpec.RENJIAN.dimension());
    }

    private static boolean toolDamage(Player player, int damage) {
        ItemStack tool = player.getMainHandItem();
        return tool.is(Items.IRON_PICKAXE) && tool.getDamageValue() == damage;
    }

    private static boolean inStep(Vec3 position, BlockPos from, BlockPos to, boolean leavingPortal) {
        double margin = leavingPortal ? 0 : 0.10;
        return position.x >= Math.min(from.getX(), to.getX()) + margin && position.x <= Math.max(from.getX(), to.getX()) + 1 - margin
                && position.z >= Math.min(from.getZ(), to.getZ()) + margin && position.z <= Math.max(from.getZ(), to.getZ()) + 1 - margin
                && position.y >= from.getY() - 0.20 && position.y <= Math.max(from.getY(), to.getY()) + 0.45;
    }

    private static boolean at(Vec3 position, BlockPos target) {
        return Math.abs(position.y - target.getY()) <= 0.51
                && Math.abs(position.x - target.getX() - 0.5) <= 0.20 && Math.abs(position.z - target.getZ() - 0.5) <= 0.20;
    }

    private static ResourceLocation blockId(Level level, BlockPos pos) {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
    }

    private static boolean hasArchivedGate(Level level, BlockPos lowerCorner) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                BlockPos pos = lowerCorner.offset(dx, dy, 0);
                boolean inside = dx >= 0 && dx <= 1 && dy >= 0 && dy <= 2;
                if (inside ? !isPortalCell(level.getBlockState(pos)) : !PORTAL_FRAME.equals(blockId(level, pos))) return false;
            }
        }
        return true;
    }

    private static boolean isPortalCell(BlockState state) {
        return PORTAL.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))
                && state.getOptionalValue(NetherPortalBlock.AXIS).orElse(null) == Direction.Axis.X;
    }

    private void release() {
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyShift.setDown(false);
    }

    private boolean stop(String reason) {
        release();
        SmokeMod.residenceDigging = false;
        if (digging && minecraft.gameMode != null && minecraft.player != null && minecraft.level != null) {
            minecraft.gameMode.stopDestroyBlock();
        }
        fail(reason + "，路线操作=" + actionIndex + "，已破=" + breaks);
        checkpoint.run();
        finish();
        return false;
    }
}
