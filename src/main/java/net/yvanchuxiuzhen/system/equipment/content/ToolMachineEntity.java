package net.yvanchuxiuzhen.system.equipment.content;

import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
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
    private static final TagKey<Block> PICKAXE_MATERIALS = materialTag("pickaxe");
    private static final TagKey<Block> SHOVEL_MATERIALS = materialTag("shovel");
    private static final TagKey<Block> PLANT_MATERIALS = materialTag("plant");
    private static final TagKey<Block> LEAVES_MATERIAL = materialTag("leaves");

    /** 材质成员来自锁定的1.18.2版本目录，独立于目标版本采掘与可替换标签。 */
    private static TagKey<Block> materialTag(String family) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "equipment_kailing_" + family));
    }

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
    private String ownerUuid = EMPTY;
    private String facing = EMPTY;
    private int enchantLevel;
    private double maxLifeTicks;
    private double centerX;
    private double centerY;
    private double centerZ;
    private double lifeTicks;
    private double actionTicks;
    private String phase = EMPTY;
    private double targetX;
    private double targetY;
    private double targetZ;
    private String searchDirection = EMPTY;
    private double searchSteps;
    private double searchLimit;
    private String stepStage = EMPTY;
    private double dugCount;

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

    public String phase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public int enchantLevel() { return enchantLevel; }
    public void setEnchantLevel(int enchantLevel) { this.enchantLevel = enchantLevel; }
    public double maxLifeTicks() { return maxLifeTicks; }
    public void setMaxLifeTicks(double maxLifeTicks) { this.maxLifeTicks = maxLifeTicks; }
    public double actionTicks() { return actionTicks; }
    public void setActionTicks(double actionTicks) { this.actionTicks = actionTicks; }
    public void setOwnerUuid(String ownerUuid) { this.ownerUuid = ownerUuid; }
    public void setTargetX(double targetX) { this.targetX = targetX; }
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
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
        ownerUuid = source == null ? EMPTY : source.getStringUUID();
        facing = directionName(face);
        this.enchantLevel = enchantLevel;
        maxLifeTicks = enchantLevel * 2400d;
        centerX = position.x;
        centerY = position.y;
        centerZ = position.z;
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
        if (maxLifeTicks == 0 && (kind == Kind.AXE || kind == Kind.PICKAXE)) {
            maxLifeTicks = enchantLevel * 400d;
        }
        lifeTicks += 1;
        if (lifeTicks >= maxLifeTicks) {
            expire();
            return;
        }
        switch (kind) {
            case AXE -> processAxe(enchantLevel);
            case PICKAXE -> processPickaxe();
            case HOE -> processSoilTool(false);
            case SHOVEL -> processSoilTool(true);
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

    private void processAxe(int enchant) {
        if (phase.equals(EMPTY)) phase = FIND;
        else if (phase.equals(FIND)) findLog(enchant);
        else if (phase.equals(PATH)) followAxeTarget();
        else if (phase.equals(COLLECT)) {
            lookAtTarget();
            setDeltaMovement(Vec3.ZERO);
            setAnimation(kind.attack);
            phase = CONTACT;
        } else if (phase.equals(CONTACT)) {
            lookAtTarget();
            actionTicks += 1;
            if (actionTicks >= 11) {
                BlockPos hit = rayBlock();
                if (level().getBlockState(hit).getDestroySpeed(level(), hit) >= 0 && level().getBlockState(hit).is(BlockTags.LOGS)) {
                    ServerPlayer owner = ownerPlayer();
                    if (owner != null) ItemHandlerHelper.giveItemToPlayer(owner, new ItemStack(level().getBlockState(hit).getBlock().asItem()));
                    level().destroyBlock(hit, false);
                }
                phase = FIND;
                actionTicks = 0;
            }
        } else if (phase.equals(NONE)) {
            actionTicks += 1;
            if (actionTicks >= 20) expireWithoutTarget();
        } else if (phase.equals(CLEAR_LEAVES)) {
            setAnimation(kind.alternate);
            clearLeaves();
            phase = CLEARING;
        } else if (phase.equals(CLEARING)) {
            actionTicks += 1;
            if (actionTicks >= 20) {
                phase = PATH;
                actionTicks = 0;
            }
        }
    }

    private void findLog(int enchant) {
        int range = 8 * enchant;
        int start = -Math.round(range / 2f);
        int misses = 0;
        for (int x = start; x < start + range; x++) {
            for (int y = start; y < start + range; y++) {
                for (int z = start; z < start + range; z++) {
                    BlockPos pos = BlockPos.containing(getX() + x, getY() + y, getZ() + z);
                    if (level().getBlockState(pos).is(BlockTags.LOGS)) {
                        targetX = getX() + x;
                        targetY = getY() + y;
                        targetZ = getZ() + z;
                        lookAtTarget();
                        phase = PATH;
                    } else misses++;
                }
            }
        }
        if (misses >= Math.pow(range, 3)) phase = NONE;
    }

    private void followAxeTarget() {
        lookAtTarget();
        setDeltaMovement(new Vec3(targetX - getX(), targetY - getY(), targetZ - getZ()));
        BlockPos hit = rayBlock();
        BlockState state = level().getBlockState(hit);
        if (state.getDestroySpeed(level(), hit) >= 0 && state.is(BlockTags.LOGS)) {
            actionTicks = 0;
            phase = COLLECT;
        } else if (hasLeavesAround()) {
            actionTicks = 0;
            phase = CLEAR_LEAVES;
        } else {
            actionTicks += 1;
            if (actionTicks >= 10) {
                double delta = targetY - getY();
                if (delta > 0) setDeltaMovement(0, 1, 0);
                else if (delta < 0) setDeltaMovement(0, -1, 0);
                else phase = FIND;
                actionTicks = 0;
            }
        }
    }

    private boolean hasLeavesAround() {
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (level().getBlockState(pos).is(LEAVES_MATERIAL)) return true;
        }
        return false;
    }

    private void clearLeaves() {
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            BlockState state = level().getBlockState(pos);
            if (state.getDestroySpeed(level(), pos) >= 0 && state.is(LEAVES_MATERIAL)) level().destroyBlock(pos, false);
        }
    }

    private void processPickaxe() {
        if (phase.equals(EMPTY)) {
            searchDirection = LEFT;
            searchLimit = 0;
            if (dugCount >= 25) {
                setMiningTarget();
                dugCount = 0;
                phase = EMPTY;
            } else {
                setInitialPickaxeTarget();
                phase = PREPARE;
            }
        } else if (phase.equals(FIND)) {
            advanceSearch();
            phase = MOVE;
        } else if (phase.equals(MOVE)) {
            moveToMiningTarget();
            phase = PREPARE;
        } else if (phase.equals(PREPARE)) {
            actionTicks += 1;
            if (actionTicks >= 1) {
                BlockPos target = target();
                if (isPickaxeBlock(target)) phase = MINE;
                else {
                    dugCount += 1;
                    phase = FINISH;
                }
                actionTicks = 0;
            }
        } else if (phase.equals(MINE)) {
            setAnimation(pickaxeAnimation());
            phase = MINING;
        } else if (phase.equals(MINING)) {
            actionTicks += 1;
            if (actionTicks >= 5) {
                destroyForOwner(target());
                dugCount += 1;
                actionTicks = 0;
                phase = FINISH;
            }
        } else if (phase.equals(FINISH)) phase = dugCount >= 25 ? EMPTY : FIND;
    }

    private void processSoilTool(boolean shovel) {
        if (phase.equals(EMPTY)) {
            initializeMining();
            phase = PREPARE;
        } else if (phase.equals(FIND)) {
            advanceSearch();
            phase = MOVE;
        } else if (phase.equals(MOVE)) {
            moveToMiningTarget();
            phase = PREPARE;
        } else if (phase.equals(PREPARE)) {
            actionTicks += 1;
            if (actionTicks >= 1) {
                BlockPos target = target();
                if (isSoilBlock(target, shovel)) phase = MINE;
                else {
                    dugCount += 1;
                    phase = FINISH;
                }
                actionTicks = 0;
            }
        } else if (phase.equals(MINE)) {
            setAnimation(kind.attack);
            phase = MINING;
        } else if (phase.equals(MINING)) {
            actionTicks += 1;
            if (actionTicks >= 5) {
                if (shovel) till(target());
                else destroyForOwner(target());
                actionTicks = 0;
                phase = FINISH;
            }
        } else if (phase.equals(FINISH)) phase = FIND;
    }

    private void initializeMining() {
        searchDirection = LEFT;
        searchLimit = 0;
        targetX = centerX;
        targetY = centerY - 1;
        targetZ = centerZ;
    }

    private void setInitialPickaxeTarget() {
        if (facing.equals(DOWN)) {
            targetX = centerX;
            targetY = centerY - 1;
            targetZ = centerZ;
        } else if (facing.equals("south")) {
            targetX = centerX;
            targetY = centerY;
            targetZ = centerZ + 1;
        } else if (facing.equals("north")) {
            targetX = centerX;
            targetY = centerY;
            targetZ = centerZ - 1;
        } else if (facing.equals("east")) {
            targetX = centerX + 1;
            targetY = centerY;
            targetZ = centerZ;
        } else if (facing.equals("west")) {
            targetX = centerX - 1;
            targetY = centerY;
            targetZ = centerZ;
        } else {
            targetX = centerX;
            targetY = centerY + 1;
            targetZ = centerZ;
        }
    }

    private void advanceSearch() {
        String local = searchDirection;
        searchSteps += 1;
        if (searchSteps >= searchLimit) {
            local = switch (local) {
                case RIGHT -> UP;
                case UP -> LEFT;
                case DOWN -> RIGHT;
                default -> DOWN;
            };
            searchDirection = local;
            if (searchLimit == 0) {
                searchLimit = 1;
                stepStage = EMPTY;
            } else if (stepStage.equals(EMPTY)) stepStage = "one";
            else {
                searchLimit += 1;
                stepStage = EMPTY;
            }
            searchSteps = 0;
        }
        moveSearchTarget(local);
    }

    private void moveSearchTarget(String local) {
        if (facing.equals(UP) || facing.equals(DOWN)) {
            if (local.equals(DOWN)) targetZ += 1;
            else if (local.equals(UP)) targetZ -= 1;
            else if (local.equals(RIGHT)) targetX += 1;
            else if (local.equals(LEFT)) targetX -= 1;
        } else if (facing.equals("south") || facing.equals("north")) {
            if (local.equals(DOWN)) targetY -= 1;
            else if (local.equals(UP)) targetY += 1;
            else if (local.equals(RIGHT)) targetX += 1;
            else if (local.equals(LEFT)) targetX -= 1;
        } else {
            if (local.equals(DOWN)) targetY -= 1;
            else if (local.equals(UP)) targetY += 1;
            else if (local.equals(RIGHT)) targetZ -= 1;
            else if (local.equals(LEFT)) targetZ += 1;
        }
    }

    private void setMiningTarget() {
        if (facing.equals(DOWN)) {
            targetX = centerX;
            targetY -= 1;
            targetZ = centerZ;
        } else if (facing.equals("south")) {
            targetX = centerX;
            targetY = centerY;
            targetZ += 1;
        } else if (facing.equals("north")) {
            targetX = centerX;
            targetY = centerY;
            targetZ -= 1;
        } else if (facing.equals("east")) {
            targetX += 1;
            targetY = centerY;
            targetZ = centerZ;
        } else if (facing.equals("west")) {
            targetX -= 1;
            targetY = centerY;
            targetZ = centerZ;
        } else {
            targetX = centerX;
            targetY += 1;
            targetZ = centerZ;
        }
    }

    private void moveToMiningTarget() {
        lookAtTarget();
        if (kind == Kind.HOE || kind == Kind.SHOVEL) {
            teleportTo(targetX, targetY + 1, targetZ);
            return;
        }
        switch (facing) {
            case DOWN -> teleportTo(targetX, targetY + 1, targetZ);
            case "south" -> teleportTo(targetX, targetY, targetZ - 1);
            case "north" -> teleportTo(targetX, targetY, targetZ + 1);
            case "east" -> teleportTo(targetX - 1, targetY, targetZ);
            case "west" -> teleportTo(targetX + 1, targetY, targetZ);
            default -> teleportTo(targetX, targetY - 1, targetZ);
        }
    }

    private String pickaxeAnimation() {
        return switch (facing) {
            case DOWN -> "animation.gongjvgao.attackup";
            case UP -> "animation.gongjvgao.attackdown";
            default -> kind.attack;
        };
    }

    private boolean isPickaxeBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.getDestroySpeed(level(), pos) >= 0 && state.is(PICKAXE_MATERIALS);
    }

    private boolean isSoilBlock(BlockPos pos, boolean shovel) {
        BlockState state = level().getBlockState(pos);
        if (state.getDestroySpeed(level(), pos) < 0) return false;
        if (shovel) return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT);
        return state.is(SHOVEL_MATERIALS);
    }

    private void till(BlockPos pos) {
        if (!isSoilBlock(pos, kind == Kind.SHOVEL)) return;
        BlockState above = level().getBlockState(pos.above());
        if (above.is(PLANT_MATERIALS)) level().setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        level().setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
        BlockState farmland = level().getBlockState(pos);
        if (farmland.hasProperty(BlockStateProperties.MOISTURE)) level().setBlock(pos, farmland.setValue(BlockStateProperties.MOISTURE, 7), 3);
    }

    private void destroyForOwner(BlockPos pos) {
        if (!ownerUuid.isEmpty()) {
            ServerPlayer owner = ownerPlayer();
            if (owner == null) return;
            Block.dropResources(level().getBlockState(pos), level(), owner.blockPosition());
        } else Block.dropResources(level().getBlockState(pos), level(), pos);
        level().destroyBlock(pos, false);
    }

    private ServerPlayer ownerPlayer() {
        if (!(level() instanceof ServerLevel server)) return null;
        if (ownerUuid.isEmpty()) return null;
        try {
            Player player = server.getPlayerByUUID(UUID.fromString(ownerUuid));
            return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private BlockPos target() {
        return BlockPos.containing(targetX, targetY, targetZ);
    }

    private void lookAtTarget() {
        lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(targetX, targetY, targetZ));
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
        tag.putString("ownerUuid", ownerUuid);
        tag.putString("facing", facing);
        tag.putInt("enchantLevel", enchantLevel);
        tag.putDouble("maxLifeTicks", maxLifeTicks);
        tag.putDouble("centerX", centerX);
        tag.putDouble("centerY", centerY);
        tag.putDouble("centerZ", centerZ);
        tag.putDouble("lifeTicks", lifeTicks);
        tag.putDouble("actionTicks", actionTicks);
        tag.putString("phase", phase);
        tag.putDouble("targetX", targetX);
        tag.putDouble("targetY", targetY);
        tag.putDouble("targetZ", targetZ);
        tag.putString("searchDirection", searchDirection);
        tag.putDouble("searchSteps", searchSteps);
        tag.putDouble("searchLimit", searchLimit);
        tag.putString("stepStage", stepStage);
        tag.putDouble("dugCount", dugCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
        ownerUuid = tag.getString("ownerUuid");
        facing = tag.getString("facing");
        enchantLevel = tag.getInt("enchantLevel");
        maxLifeTicks = tag.getDouble("maxLifeTicks");
        centerX = tag.getDouble("centerX");
        centerY = tag.getDouble("centerY");
        centerZ = tag.getDouble("centerZ");
        lifeTicks = tag.getDouble("lifeTicks");
        actionTicks = tag.getDouble("actionTicks");
        phase = tag.getString("phase");
        targetX = tag.getDouble("targetX");
        targetY = tag.getDouble("targetY");
        targetZ = tag.getDouble("targetZ");
        searchDirection = tag.getString("searchDirection");
        searchSteps = tag.getDouble("searchSteps");
        searchLimit = tag.getDouble("searchLimit");
        stepStage = tag.getString("stepStage");
        dugCount = tag.getDouble("dugCount");
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
