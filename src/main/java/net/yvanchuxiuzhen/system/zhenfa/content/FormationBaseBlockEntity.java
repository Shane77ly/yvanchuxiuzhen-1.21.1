package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 阵基独占布局参数、灵力、触发计数和控制坐标；所有字段直接保存在方块实体根标签，二刻计划刻按来源顺序推进。 */
public final class FormationBaseBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] handlers = new IItemHandler[6];

    private boolean kun;
    private boolean zhen;
    private boolean li;
    private boolean dui;
    // 来源中八卦“乾”和阵前跟随共用 NBT 键 zhenqian，保留单整型以维持位语义和现有结构兼容。
    private int zhenQian;
    private boolean xvn;
    private boolean kan;
    private boolean gen;

    private int limit;
    private int range;
    private int triggerMode;
    private int effectFlag;
    private int sightScale;
    private int targetMode;
    private int controlFlag;
    private double triggerX;
    private double triggerY;
    private double triggerZ;

    private boolean absoluteCoords;
    private boolean randomOffset;
    private boolean inspectFlag;

    private int gold;
    private int water;
    private int tree;
    private int fire;
    private int soil;
    private int yinYang;

    private int rank;
    private int power;
    private int powerNeed;
    private boolean valid;
    private String skillName = "";
    private String skillCategory = "";
    private boolean redstonePowered;
    private boolean begin;
    private int tick;
    private double yaw;
    private double pitch;
    private String manaAttackUuid = "";
    private double playerX;
    private double playerY;
    private double playerZ;

    public FormationBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ZhenfaBlockEntities.BASE.get(), pos, state);
        for (Direction direction : Direction.values()) handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : handlers[direction.ordinal()]; }

    public int power() { return power; }
    public void setPower(int value) { power = value; }
    public boolean kun() { return kun; }
    public boolean zhen() { return zhen; }
    public boolean li() { return li; }
    public boolean dui() { return dui; }
    public boolean qian() { return (byte) zhenQian != 0; }
    public boolean xvn() { return xvn; }
    public boolean kan() { return kan; }
    public boolean gen() { return gen; }
    public int limit() { return limit; }
    public int range() { return range; }
    public void setRange(int value) { range = value; }
    public int triggerMode() { return triggerMode; }
    public int effectFlag() { return effectFlag; }
    public int followCaster() { return zhenQian; }
    public int sightScale() { return sightScale; }
    public int targetMode() { return targetMode; }
    public int controlFlag() { return controlFlag; }
    public double triggerX() { return triggerX; }
    public double triggerY() { return triggerY; }
    public double triggerZ() { return triggerZ; }
    public boolean absoluteCoords() { return absoluteCoords; }
    public boolean randomOffset() { return randomOffset; }
    public boolean inspectFlag() { return inspectFlag; }
    public int gold() { return gold; }
    public int water() { return water; }
    public int tree() { return tree; }
    public int fire() { return fire; }
    public int soil() { return soil; }
    public int yinYang() { return yinYang; }
    public int rank() { return rank; }
    public int powerNeed() { return powerNeed; }
    public boolean valid() { return valid; }
    public String skillName() { return skillName; }
    public String skillCategory() { return skillCategory; }
    public boolean redstonePowered() { return redstonePowered; }
    public boolean begin() { return begin; }
    public void setBegin(boolean value) { begin = value; }
    public int tick() { return tick; }
    public double yaw() { return yaw; }
    public double pitch() { return pitch; }
    public String manaAttackUuid() { return manaAttackUuid; }
    public void setManaAttackUuid(String value) { manaAttackUuid = value; }
    public double playerX() { return playerX; }
    public double playerY() { return playerY; }
    public double playerZ() { return playerZ; }

    public FormationSnapshot formationSnapshot() {
        return new FormationSnapshot(
                kun, zhen, li, dui, qian(), xvn, kan, gen,
                limit, range, triggerMode, effectFlag, zhenQian, sightScale, targetMode, controlFlag,
                triggerX, triggerY, triggerZ, absoluteCoords, randomOffset, inspectFlag,
                gold, water, tree, fire, soil, yinYang,
                rank, power, powerNeed, valid, skillName, skillCategory,
                redstonePowered, begin, tick, yaw, pitch, manaAttackUuid, playerX, playerY, playerZ);
    }

    public void setTargetPlayerPos(double x, double y, double z) {
        playerX = x;
        playerY = y;
        playerZ = z;
    }

    public void setTrigrams(boolean kun, boolean zhen, boolean li, boolean dui, boolean qian, boolean xvn, boolean kan, boolean gen) {
        this.kun = kun;
        this.zhen = zhen;
        this.li = li;
        this.dui = dui;
        this.zhenQian = qian ? 1 : 0;
        this.xvn = xvn;
        this.kan = kan;
        this.gen = gen;
    }

    public void configureEffect(String category, String name, int rank, int cost, BlockPos target) {
        this.valid = true;
        this.begin = true;
        this.absoluteCoords = true;
        this.randomOffset = false;
        this.skillCategory = category;
        this.skillName = name;
        this.rank = rank;
        this.powerNeed = cost;
        this.power = cost + 1;
        this.range = 2;
        this.triggerX = target.getX();
        this.triggerY = target.getY();
        this.triggerZ = target.getZ();
        this.triggerMode = 0;
        this.targetMode = 0;
        this.tick = 0;
    }

    void resetLayout() {
        kun = false; zhen = false; li = false; dui = false;
        zhenQian = 0; xvn = false; kan = false; gen = false;
        limit = 0; range = 0; triggerMode = 0; effectFlag = 0;
        sightScale = 0; targetMode = 0; controlFlag = 0;
        triggerX = 0; triggerY = 0; triggerZ = 0;
        absoluteCoords = false; randomOffset = false; inspectFlag = false;
        gold = 0; water = 0; tree = 0; fire = 0; soil = 0; yinYang = 0;
    }

    void setTrigram(int index, boolean value) {
        switch (index) {
            case 0 -> kun = value;
            case 1 -> zhen = value;
            case 2 -> li = value;
            case 3 -> dui = value;
            case 4 -> zhenQian = value ? 1 : 0;
            case 5 -> xvn = value;
            case 6 -> kan = value;
            case 7 -> gen = value;
            default -> { }
        }
    }

    void addLimit(int delta) { limit += delta; }
    void addRange(int delta) { range += delta; }
    void addTriggerMode(int delta) { triggerMode += delta; }
    void setAbsoluteCoords(boolean value) { absoluteCoords = value; }
    void addEffectFlag(int delta) { effectFlag += delta; }
    void setRandomOffset(boolean value) { randomOffset = value; }
    void addFollowCaster(int delta) { zhenQian += delta; }
    void addSightScale(int delta) { sightScale += delta; }
    void addTargetMode(int delta) { targetMode += delta; }
    void setInspectFlag(boolean value) { inspectFlag = value; }
    void addControlFlag(int delta) { controlFlag += delta; }
    void setTriggerX(double value) { triggerX = value; }
    void addTriggerY(double delta) { triggerY += delta; }
    void setTriggerZ(double value) { triggerZ = value; }

    int elementValue(int index) {
        return switch (index) {
            case 0 -> gold;
            case 1 -> water;
            case 2 -> tree;
            case 3 -> fire;
            case 4 -> soil;
            case 5 -> yinYang;
            default -> 0;
        };
    }

    void addElement(int index, int delta) {
        switch (index) {
            case 0 -> gold += delta;
            case 1 -> water += delta;
            case 2 -> tree += delta;
            case 3 -> fire += delta;
            case 4 -> soil += delta;
            case 5 -> yinYang += delta;
            default -> { }
        }
    }

    void applyLayoutSummary(int rank, int basePowerNeed, boolean valid) {
        this.rank = rank;
        this.powerNeed = basePowerNeed;
        this.valid = valid;
    }

    void applyRecipeResult(String name, String category, int finalCost) {
        this.skillName = name;
        this.skillCategory = category;
        this.powerNeed = finalCost;
    }

    public void detect() {
        FormationLayout.detect(level, worldPosition, this);
        changed();
    }

    public void redstone(boolean powered) {
        if (!powered || triggerMode == 1) redstonePowered = powered;
        setChanged();
    }

    public void fuel(Player player) {
        ItemStack held = player.getMainHandItem();
        if (!held.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi")))) return;
        String id = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
        int value = 0;
        for (int rankIndex = 0; rankIndex < 9; rankIndex++) {
            if (id.equals("cailiao_" + (191 + rankIndex))) value = 1 << (rankIndex * 2);
        }
        power += value;
        held.shrink(1);
        player.getInventory().setChanged();
        ((ServerLevel) level).sendParticles(Particles.type("lingqi"), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.1);
        changed();
    }

    /** 旗物品只提交控制者及射线结果；参数仍由本阵基读取，坐标维持来源的 double 写入。 */
    public void control(Player player) {
        yaw = player.getYRot();
        pitch = player.getXRot();
        manaAttackUuid = player.getStringUUID();
        playerX = player.getX();
        playerY = player.getY();
        playerZ = player.getZ();
        if (sightScale > 0) {
            double distance = 1 + sightScale * 0.5 * range;
            Vec3 eye = player.getEyePosition(1);
            BlockPos hit = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(distance)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos();
            triggerX = hit.getX();
            triggerY = hit.getY();
            triggerZ = hit.getZ();
        } else if (followCaster() == 3) {
            triggerX = player.getX();
            triggerY = player.getY();
            triggerZ = player.getZ();
        }
        begin = true;
        changed();
    }

    public void scheduledTick(ServerLevel level) {
        if (valid) {
            if (powerNeed > 0 && power > powerNeed) {
                tick++;
                if (tick >= 10) {
                    if (triggerMode == 3) {
                        Vec3 center = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
                        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(range / 2d))
                                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                            if (matchesTeam(entity)) FormationEffects.execute(this);
                            power -= powerNeed;
                        }
                    } else if (redstonePowered) {
                        FormationEffects.execute(this);
                        power -= powerNeed;
                    }
                    tick = 0;
                }
                if (begin || triggerMode == 2) {
                    FormationEffects.execute(this);
                    power -= powerNeed;
                    begin = false;
                }
            } else {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, 10, 5, 5, 5, 0.1);
            }
        }
        if (!(level.getBlockEntity(worldPosition.above()) instanceof FormationMarkerBlockEntity marker) || marker.compass()) {
            valid = false;
        }
        setChanged();
    }

    boolean matchesTeam(Entity entity) {
        String team = level.getBlockEntity(worldPosition.above()) instanceof FormationMarkerBlockEntity marker ? marker.team() : "";
        return targetMode == 1 ? team.equals(Relations.entityTeam(entity)) : targetMode != 2 || !team.equals(Relations.entityTeam(entity));
    }

    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.zhenji"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(0, ItemStack.EMPTY);
        kun = tag.getBoolean("zhenkun");
        zhen = tag.getBoolean("zhenzhen");
        li = tag.getBoolean("zhenli");
        dui = tag.getBoolean("zhendui");
        zhenQian = tag.getInt("zhenqian");
        xvn = tag.getBoolean("zhenxvn");
        kan = tag.getBoolean("zhenkan");
        gen = tag.getBoolean("zhengen");
        limit = tag.getInt("zhenshangxian");
        range = tag.getInt("zhenjvli");
        triggerMode = tag.getInt("zhenchufa");
        effectFlag = tag.getInt("zhentexiao");
        sightScale = tag.getInt("zhenshijiao");
        targetMode = tag.getInt("zhenmubiao");
        controlFlag = tag.getInt("zhenkongzhi");
        triggerX = tag.getDouble("zhenchufaX");
        triggerY = tag.getDouble("zhenchufaY");
        triggerZ = tag.getDouble("zhenchufaZ");
        absoluteCoords = tag.getBoolean("zhenzuobiao");
        randomOffset = tag.getBoolean("zhensuiji");
        inspectFlag = tag.getBoolean("zhenjiance");
        gold = tag.getInt("zhengold");
        water = tag.getInt("zhenwater");
        tree = tag.getInt("zhentree");
        fire = tag.getInt("zhenfire");
        soil = tag.getInt("zhensoil");
        yinYang = tag.getInt("zhenyinyang");
        rank = tag.getInt("zhenLEVEL");
        power = tag.getInt("power");
        powerNeed = tag.getInt("powerneed");
        valid = tag.getBoolean("zhenfazhengqve");
        skillName = tag.getString("jinengname");
        skillCategory = tag.getString("jinengleibie");
        redstonePowered = tag.getBoolean("zhenhongshi");
        begin = tag.getBoolean("zhenbegin");
        tick = tag.getInt("tick");
        yaw = tag.getDouble("zhenjiaodu");
        pitch = tag.getDouble("zhentaitou");
        manaAttackUuid = tag.getString("ManaAttackUUID");
        playerX = tag.getDouble("playerX");
        playerY = tag.getDouble("playerY");
        playerZ = tag.getDouble("playerZ");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (kun) tag.putBoolean("zhenkun", true);
        if (zhen) tag.putBoolean("zhenzhen", true);
        if (li) tag.putBoolean("zhenli", true);
        if (dui) tag.putBoolean("zhendui", true);
        if (zhenQian != 0) tag.putInt("zhenqian", zhenQian);
        if (xvn) tag.putBoolean("zhenxvn", true);
        if (kan) tag.putBoolean("zhenkan", true);
        if (gen) tag.putBoolean("zhengen", true);
        if (limit != 0) tag.putInt("zhenshangxian", limit);
        if (range != 0) tag.putInt("zhenjvli", range);
        if (triggerMode != 0) tag.putInt("zhenchufa", triggerMode);
        if (effectFlag != 0) tag.putInt("zhentexiao", effectFlag);
        if (sightScale != 0) tag.putInt("zhenshijiao", sightScale);
        if (targetMode != 0) tag.putInt("zhenmubiao", targetMode);
        if (controlFlag != 0) tag.putInt("zhenkongzhi", controlFlag);
        if (triggerX != 0) tag.putDouble("zhenchufaX", triggerX);
        if (triggerY != 0) tag.putDouble("zhenchufaY", triggerY);
        if (triggerZ != 0) tag.putDouble("zhenchufaZ", triggerZ);
        if (absoluteCoords) tag.putBoolean("zhenzuobiao", true);
        if (randomOffset) tag.putBoolean("zhensuiji", true);
        if (inspectFlag) tag.putBoolean("zhenjiance", true);
        if (gold != 0) tag.putInt("zhengold", gold);
        if (water != 0) tag.putInt("zhenwater", water);
        if (tree != 0) tag.putInt("zhentree", tree);
        if (fire != 0) tag.putInt("zhenfire", fire);
        if (soil != 0) tag.putInt("zhensoil", soil);
        if (yinYang != 0) tag.putInt("zhenyinyang", yinYang);
        if (rank != 0) tag.putInt("zhenLEVEL", rank);
        if (power != 0) tag.putInt("power", power);
        if (powerNeed != 0) tag.putInt("powerneed", powerNeed);
        if (valid) tag.putBoolean("zhenfazhengqve", true);
        if (!skillName.isEmpty()) tag.putString("jinengname", skillName);
        if (!skillCategory.isEmpty()) tag.putString("jinengleibie", skillCategory);
        if (redstonePowered) tag.putBoolean("zhenhongshi", true);
        if (begin) tag.putBoolean("zhenbegin", true);
        if (tick != 0) tag.putInt("tick", tick);
        if (yaw != 0) tag.putDouble("zhenjiaodu", yaw);
        if (pitch != 0) tag.putDouble("zhentaitou", pitch);
        if (!manaAttackUuid.isEmpty()) tag.putString("ManaAttackUUID", manaAttackUuid);
        if (playerX != 0) tag.putDouble("playerX", playerX);
        if (playerY != 0) tag.putDouble("playerY", playerY);
        if (playerZ != 0) tag.putDouble("playerZ", playerZ);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
