package net.yvanchuxiuzhen.system.equipment.content;

import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 开灵附魔生成的工具机；工具类别决定来源状态机、采集范围与方块处理方式。 */
public final class ToolMachineEntity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final String EMPTY = "";
    private static final String FIND = "find";
    private static final String PATH = "path";
    private static final String COLLECT = "collect";
    private static final String CONTACT = "contact";
    private static final String NONE = "none";
    private static final String CLEAR_LEAVES = "clear_leaves";
    private static final String CLEARING = "clearing";
    private static final String MOVE = "move";
    private static final String PREPARE = "prepare";
    private static final String MINE = "mine";
    private static final String MINING = "mining";
    private static final String FINISH = "finish";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String UP = "up";
    private static final String DOWN = "down";

    public enum Kind {
        AXE("gongjvfu", "animation.gongjvfu.attack", "animation.gongjvfu.attack02"),
        PICKAXE("gongjvgao", "animation.gongjvgao.attack", "animation.gongjvgao.attackup"),
        HOE("gongjvchan", "animation.gongjvchan.attack", "animation.gongjvchan.attack"),
        SHOVEL("gongjvchu", "animation.gongjvchu.attack", "animation.gongjvchu.attack");

        private final String id;
        private final String attack;
        private final String alternate;

        Kind(String id, String attack, String alternate) {
            this.id = id;
            this.attack = attack;
            this.alternate = alternate;
        }

        public String id() {
            return id;
        }
    }

    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(ToolMachineEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(ToolMachineEntity.class, EntityDataSerializers.STRING);

    private final Kind kind;
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedure = EMPTY;

    public ToolMachineEntity(EntityType<ToolMachineEntity> type, Level level, EntityModelRow model, Kind kind) {
        super(type, level);
        this.model = model;
        this.kind = kind;
        navigation = new FlyingPathNavigation(this, level);
        moveControl = new FlyingMoveControl(this, 10, true);
        setNoAi(false);
        setNoGravity(true);
        setPersistenceRequired();
        xpReward = 0;
        setTexture(model.texture());
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, .3).add(NeoForgeMod.SWIM_SPEED, .3);
    }

    /** 按来源的上下、东西、南北优先级确定工具机附着方向。 */
    public static Direction face(Level level, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above())) return Direction.UP;
        if (!level.isEmptyBlock(pos.below())) return Direction.DOWN;
        if (!level.isEmptyBlock(pos.east())) return Direction.EAST;
        if (!level.isEmptyBlock(pos.west())) return Direction.WEST;
        if (!level.isEmptyBlock(pos.south())) return Direction.SOUTH;
        if (!level.isEmptyBlock(pos.north())) return Direction.NORTH;
        return null;
    }

    public static void spawn(ServerLevel level, Entity source, Kind kind, Vec3 position, Direction face) {
        int enchantLevel = source instanceof Player player
                ? Enchants.mainHand(player, Enchants.key("kailing")) : 1;
        spawn(level, source, kind, enchantLevel, position, face);
    }

    /** 由弹体首刻保存的附魔等级创建工具机，避免主人换手后改变已发出的实体。 */
    public static void spawn(ServerLevel level, Entity source, Kind kind, int enchantLevel, Vec3 position, Direction face) {
        ToolMachineEntity machine = EquipmentSetup.toolMachineType(kind).get().create(level);
        if (machine == null) return;
        machine.configure(source, enchantLevel, position, face);
        machine.moveTo(position.x, position.y, position.z, level.random.nextFloat() * 360, 0);
        machine.finalizeSpawn(level, level.getCurrentDifficultyAt(machine.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(machine);
    }

    private void configure(Entity source, int enchantLevel, Vec3 position, Direction face) {
        CompoundTag data = getPersistentData();
        data.putString("zhurenuuid", source == null ? EMPTY : source.getStringUUID());
        data.putString("fangxiang", directionName(face));
        data.putDouble("fumodengji", enchantLevel);
        data.putDouble("xiaoshitickMax", enchantLevel * 2400d);
        data.putDouble("zhongxinX", position.x);
        data.putDouble("zhongxinY", position.y);
        data.putDouble("zhongxinZ", position.z);
    }

    private static String directionName(Direction direction) {
        if (direction == null) return EMPTY;
        return switch (direction) {
            case UP -> UP;
            case DOWN -> DOWN;
            case EAST -> "east";
            case WEST -> "west";
            case SOUTH -> "south";
            case NORTH -> "north";
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "kong");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    private void setTexture(String value) {
        entityData.set(TEXTURE, value);
    }

    private void setAnimation(String value) {
        entityData.set(ANIMATION, value);
    }

    @Override
    public void tick() {
        String requested = entityData.get(ANIMATION);
        if (!requested.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedure = requested;
        }
        super.tick();
        setNoGravity(true);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) process();
        refreshDimensions();
    }

    private void process() {
        if (!isAlive()) return;
        CompoundTag data = getPersistentData();
        double enchant = data.getDouble("fumodengji");
        if (data.getDouble("xiaoshitickMax") == 0 && (kind == Kind.AXE || kind == Kind.PICKAXE)) {
            data.putDouble("xiaoshitickMax", enchant * 400);
        }
        data.putDouble("xiaoshitick", data.getDouble("xiaoshitick") + 1);
        if (data.getDouble("xiaoshitick") >= data.getDouble("xiaoshitickMax")) {
            expire();
            return;
        }
        switch (kind) {
            case AXE -> processAxe(data, (int) enchant);
            case PICKAXE -> processPickaxe(data);
            case HOE -> processSoilTool(data, false);
            case SHOVEL -> processSoilTool(data, true);
        }
        if (getHealth() < getMaxHealth()) setHealth(getMaxHealth());
    }

    private void expire() {
        ServerLevel server = (ServerLevel) level();
        server.sendParticles(Particles.type("cailiao_229particle"), getX(), getY() + .5, getZ(), 20, .5, .5, .5, .1);
        server.sendParticles(kind == Kind.AXE ? ParticleTypes.FLAME : ParticleTypes.FLASH, getX(), getY() + .5, getZ(), 1, 0, 0, 0, 0);
        discard();
    }

    private void expireWithoutTarget() {
        ServerLevel server = (ServerLevel) level();
        server.sendParticles(Particles.type("cailiao_229particle"), getX(), getY() + .5, getZ(), 20, .5, .5, .5, .1);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY() + .5, getZ(), 1, 0, 0, 0, 0);
        discard();
    }

    private void processAxe(CompoundTag data, int enchant) {
        String phase = data.getString("jinxing");
        if (phase.equals(EMPTY)) data.putString("jinxing", FIND);
        else if (phase.equals(FIND)) findLog(data, enchant);
        else if (phase.equals(PATH)) followAxeTarget(data);
        else if (phase.equals(COLLECT)) {
            lookAtTarget(data);
            setDeltaMovement(Vec3.ZERO);
            setAnimation(kind.attack);
            data.putString("jinxing", CONTACT);
        } else if (phase.equals(CONTACT)) {
            lookAtTarget(data);
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 11) {
                BlockPos hit = rayBlock();
                if (level().getBlockState(hit).getDestroySpeed(level(), hit) >= 0 && level().getBlockState(hit).is(BlockTags.LOGS)) {
                    ServerPlayer owner = ownerPlayer(data);
                    if (owner != null) ItemHandlerHelper.giveItemToPlayer(owner, new ItemStack(level().getBlockState(hit).getBlock().asItem()));
                    level().destroyBlock(hit, false);
                }
                data.putString("jinxing", FIND);
                data.putDouble("xiaoshitick2", 0);
            }
        } else if (phase.equals(NONE)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 20) expireWithoutTarget();
        } else if (phase.equals(CLEAR_LEAVES)) {
            setAnimation(kind.alternate);
            clearLeaves();
            data.putString("jinxing", CLEARING);
        } else if (phase.equals(CLEARING)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 20) {
                data.putString("jinxing", PATH);
                data.putDouble("xiaoshitick2", 0);
            }
        }
    }

    private void findLog(CompoundTag data, int enchant) {
        int range = 8 * enchant;
        int start = -Math.round(range / 2f);
        int misses = 0;
        for (int x = start; x < start + range; x++) {
            for (int y = start; y < start + range; y++) {
                for (int z = start; z < start + range; z++) {
                    BlockPos pos = BlockPos.containing(getX() + x, getY() + y, getZ() + z);
                    if (level().getBlockState(pos).is(BlockTags.LOGS)) {
                        data.putDouble("mubiaoX", getX() + x);
                        data.putDouble("mubiaoY", getY() + y);
                        data.putDouble("mubiaoZ", getZ() + z);
                        lookAtTarget(data);
                        data.putString("jinxing", PATH);
                    } else misses++;
                }
            }
        }
        if (misses >= Math.pow(range, 3)) data.putString("jinxing", NONE);
    }

    private void followAxeTarget(CompoundTag data) {
        lookAtTarget(data);
        setDeltaMovement(new Vec3(data.getDouble("mubiaoX") - getX(), data.getDouble("mubiaoY") - getY(), data.getDouble("mubiaoZ") - getZ()));
        BlockPos hit = rayBlock();
        BlockState state = level().getBlockState(hit);
        if (state.getDestroySpeed(level(), hit) >= 0 && state.is(BlockTags.LOGS)) {
            data.putDouble("xiaoshitick2", 0);
            data.putString("jinxing", COLLECT);
        } else if (hasLeavesAround()) {
            data.putDouble("xiaoshitick2", 0);
            data.putString("jinxing", CLEAR_LEAVES);
        } else {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 10) {
                double delta = data.getDouble("mubiaoY") - getY();
                if (delta > 0) setDeltaMovement(0, 1, 0);
                else if (delta < 0) setDeltaMovement(0, -1, 0);
                else data.putString("jinxing", FIND);
                data.putDouble("xiaoshitick2", 0);
            }
        }
    }

    private boolean hasLeavesAround() {
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (level().getBlockState(pos).is(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    private void clearLeaves() {
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            BlockState state = level().getBlockState(pos);
            if (state.getDestroySpeed(level(), pos) >= 0 && state.is(BlockTags.LEAVES)) level().destroyBlock(pos, false);
        }
    }

    private void processPickaxe(CompoundTag data) {
        String phase = data.getString("jinxing");
        if (phase.equals(EMPTY)) {
            data.putString("fangxianglinshi", LEFT);
            data.putDouble("zongbushu", 0);
            if (data.getDouble("zongwajvenum") >= 25) {
                setMiningTarget(data);
                data.putDouble("zongwajvenum", 0);
                data.putString("jinxing", EMPTY);
            } else {
                setInitialPickaxeTarget(data);
                data.putString("jinxing", PREPARE);
            }
        } else if (phase.equals(FIND)) {
            advanceSearch(data);
            data.putString("jinxing", MOVE);
        } else if (phase.equals(MOVE)) {
            moveToMiningTarget(data);
            data.putString("jinxing", PREPARE);
        } else if (phase.equals(PREPARE)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 1) {
                BlockPos target = target(data);
                if (isPickaxeBlock(target)) data.putString("jinxing", MINE);
                else {
                    data.putDouble("zongwajvenum", data.getDouble("zongwajvenum") + 1);
                    data.putString("jinxing", FINISH);
                }
                data.putDouble("xiaoshitick2", 0);
            }
        } else if (phase.equals(MINE)) {
            setAnimation(pickaxeAnimation(data));
            data.putString("jinxing", MINING);
        } else if (phase.equals(MINING)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 5) {
                destroyForOwner(data, target(data));
                data.putDouble("zongwajvenum", data.getDouble("zongwajvenum") + 1);
                data.putDouble("xiaoshitick2", 0);
                data.putString("jinxing", FINISH);
            }
        } else if (phase.equals(FINISH)) data.putString("jinxing", data.getDouble("zongwajvenum") >= 25 ? EMPTY : FIND);
    }

    private void processSoilTool(CompoundTag data, boolean shovel) {
        String phase = data.getString("jinxing");
        if (phase.equals(EMPTY)) {
            initializeMining(data);
            data.putString("jinxing", PREPARE);
        } else if (phase.equals(FIND)) {
            advanceSearch(data);
            data.putString("jinxing", MOVE);
        } else if (phase.equals(MOVE)) {
            moveToMiningTarget(data);
            data.putString("jinxing", PREPARE);
        } else if (phase.equals(PREPARE)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 1) {
                BlockPos target = target(data);
                if (isSoilBlock(target, shovel)) data.putString("jinxing", MINE);
                else {
                    data.putDouble("zongwajvenum", data.getDouble("zongwajvenum") + 1);
                    data.putString("jinxing", FINISH);
                }
                data.putDouble("xiaoshitick2", 0);
            }
        } else if (phase.equals(MINE)) {
            setAnimation(kind.attack);
            data.putString("jinxing", MINING);
        } else if (phase.equals(MINING)) {
            data.putDouble("xiaoshitick2", data.getDouble("xiaoshitick2") + 1);
            if (data.getDouble("xiaoshitick2") >= 5) {
                till(target(data));
                data.putDouble("xiaoshitick2", 0);
                data.putString("jinxing", FINISH);
            }
        } else if (phase.equals(FINISH)) data.putString("jinxing", FIND);
    }

    private void initializeMining(CompoundTag data) {
        data.putString("fangxianglinshi", LEFT);
        data.putDouble("zongbushu", 0);
        data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
        data.putDouble("mubiaoY", data.getDouble("zhongxinY") - 1);
        data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
    }

    private void setInitialPickaxeTarget(CompoundTag data) {
        String facing = data.getString("fangxiang");
        if (facing.equals(DOWN)) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY") - 1);
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else if (facing.equals("south")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ") + 1);
        } else if (facing.equals("north")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ") - 1);
        } else if (facing.equals("east")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX") + 1);
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else if (facing.equals("west")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX") - 1);
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY") + 1);
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        }
    }

    private void advanceSearch(CompoundTag data) {
        String local = data.getString("fangxianglinshi");
        data.putDouble("dangqianbushu", data.getDouble("dangqianbushu") + 1);
        if (data.getDouble("dangqianbushu") >= data.getDouble("zongbushu")) {
            local = switch (local) {
                case RIGHT -> UP;
                case UP -> LEFT;
                case DOWN -> RIGHT;
                default -> DOWN;
            };
            data.putString("fangxianglinshi", local);
            if (data.getDouble("zongbushu") == 0) {
                data.putDouble("zongbushu", 1);
                data.putString("bushujieduan", EMPTY);
            } else if (data.getString("bushujieduan").equals(EMPTY)) data.putString("bushujieduan", "one");
            else {
                data.putDouble("zongbushu", data.getDouble("zongbushu") + 1);
                data.putString("bushujieduan", EMPTY);
            }
            data.putDouble("dangqianbushu", 0);
        }
        moveSearchTarget(data, local);
    }

    private void moveSearchTarget(CompoundTag data, String local) {
        String facing = data.getString("fangxiang");
        if (facing.equals(UP) || facing.equals(DOWN)) {
            if (local.equals(DOWN)) data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") + 1);
            else if (local.equals(UP)) data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") - 1);
            else if (local.equals(RIGHT)) data.putDouble("mubiaoX", data.getDouble("mubiaoX") + 1);
            else if (local.equals(LEFT)) data.putDouble("mubiaoX", data.getDouble("mubiaoX") - 1);
        } else if (facing.equals("south") || facing.equals("north")) {
            if (local.equals(DOWN)) data.putDouble("mubiaoY", data.getDouble("mubiaoY") - 1);
            else if (local.equals(UP)) data.putDouble("mubiaoY", data.getDouble("mubiaoY") + 1);
            else if (local.equals(RIGHT)) data.putDouble("mubiaoX", data.getDouble("mubiaoX") + 1);
            else if (local.equals(LEFT)) data.putDouble("mubiaoX", data.getDouble("mubiaoX") - 1);
        } else {
            if (local.equals(DOWN)) data.putDouble("mubiaoY", data.getDouble("mubiaoY") - 1);
            else if (local.equals(UP)) data.putDouble("mubiaoY", data.getDouble("mubiaoY") + 1);
            else if (local.equals(RIGHT)) data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") - 1);
            else if (local.equals(LEFT)) data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") + 1);
        }
    }

    private void setMiningTarget(CompoundTag data) {
        String facing = data.getString("fangxiang");
        if (facing.equals(DOWN)) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("mubiaoY") - 1);
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else if (facing.equals("south")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") + 1);
        } else if (facing.equals("north")) {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("mubiaoZ") - 1);
        } else if (facing.equals("east")) {
            data.putDouble("mubiaoX", data.getDouble("mubiaoX") + 1);
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else if (facing.equals("west")) {
            data.putDouble("mubiaoX", data.getDouble("mubiaoX") - 1);
            data.putDouble("mubiaoY", data.getDouble("zhongxinY"));
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        } else {
            data.putDouble("mubiaoX", data.getDouble("zhongxinX"));
            data.putDouble("mubiaoY", data.getDouble("mubiaoY") + 1);
            data.putDouble("mubiaoZ", data.getDouble("zhongxinZ"));
        }
    }

    private void moveToMiningTarget(CompoundTag data) {
        lookAtTarget(data);
        double x = data.getDouble("mubiaoX");
        double y = data.getDouble("mubiaoY");
        double z = data.getDouble("mubiaoZ");
        if (kind == Kind.HOE || kind == Kind.SHOVEL) {
            teleportTo(x, y + 1, z);
            return;
        }
        switch (data.getString("fangxiang")) {
            case DOWN -> teleportTo(x, y + 1, z);
            case "south" -> teleportTo(x, y, z - 1);
            case "north" -> teleportTo(x, y, z + 1);
            case "east" -> teleportTo(x - 1, y, z);
            case "west" -> teleportTo(x + 1, y, z);
            default -> teleportTo(x, y - 1, z);
        }
    }

    private String pickaxeAnimation(CompoundTag data) {
        return switch (data.getString("fangxiang")) {
            case DOWN -> "animation.gongjvgao.attackup";
            case UP -> "animation.gongjvgao.attackdown";
            default -> kind.attack;
        };
    }

    private boolean isPickaxeBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.getDestroySpeed(level(), pos) >= 0
                && (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.ICE) || state.is(Blocks.CLAY));
    }

    private boolean isSoilBlock(BlockPos pos, boolean shovel) {
        BlockState state = level().getBlockState(pos);
        if (state.getDestroySpeed(level(), pos) < 0) return false;
        if (shovel) return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK);
        return state.is(BlockTags.SNOW) || state.is(Blocks.SNOW) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.SAND);
    }

    private void till(BlockPos pos) {
        if (!isSoilBlock(pos, kind == Kind.SHOVEL)) return;
        BlockState above = level().getBlockState(pos.above());
        if (above.is(BlockTags.REPLACEABLE) || above.getBlock() instanceof BushBlock) level().setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        level().setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
        BlockState farmland = level().getBlockState(pos);
        if (farmland.hasProperty(BlockStateProperties.MOISTURE)) level().setBlock(pos, farmland.setValue(BlockStateProperties.MOISTURE, 7), 3);
    }

    private void destroyForOwner(CompoundTag data, BlockPos pos) {
        String ownerId = data.getString("zhurenuuid");
        if (!ownerId.isEmpty()) {
            ServerPlayer owner = ownerPlayer(data);
            if (owner == null) return;
            Block.dropResources(level().getBlockState(pos), level(), owner.blockPosition());
        } else Block.dropResources(level().getBlockState(pos), level(), pos);
        level().destroyBlock(pos, false);
    }

    private ServerPlayer ownerPlayer(CompoundTag data) {
        if (!(level() instanceof ServerLevel server)) return null;
        String ownerId = data.getString("zhurenuuid");
        if (ownerId.isEmpty()) return null;
        try {
            Player player = server.getPlayerByUUID(UUID.fromString(ownerId));
            return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private BlockPos target(CompoundTag data) {
        return BlockPos.containing(data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ"));
    }

    private void lookAtTarget(CompoundTag data) {
        lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ")));
    }

    private BlockPos rayBlock() {
        Vec3 eye = getEyePosition(1f);
        BlockHitResult hit = level().clip(new ClipContext(eye, eye.add(getViewVector(1f).scale(1)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
        return hit.getBlockPos();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Player || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)
                || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("texture", texture());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedure.isEmpty()) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.loop(onGround())));
        }));
        controllers.add(new AnimationController<>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<ToolMachineEntity> controller = state.getController();
            if (!procedure.isEmpty() && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedure, Animation.LoopType.PLAY_ONCE));
                procedure = EMPTY;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
