package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.system.zhenfa.data.FormationAttachments;
import net.yvanchuxiuzhen.system.zhenfa.data.FormationFlagData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.content.FlyingInstrumentEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillFlight;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.zhenfa.api.FormationProtection;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationBaseBlockEntity;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationElementBlockEntity;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationMarkerBlockEntity;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationArrow;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationFalling;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationLauncher;

/** 阵基布局、供能、阴系效果、阵旗绑定控制、保存和掉落必要单项。 */
final class FormationCheck extends SystemCheck {
    private final boolean spaceOnly;
    private final boolean yangOnly;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> oldBlocks = new HashMap<>();
    private final Map<BlockPos, CompoundTag> oldBlockEntities = new HashMap<>();
    private final Set<UUID> oldDrops = new HashSet<>();
    private final Set<UUID> createdEntities = new HashSet<>();
    private BlockPos floor;
    private BlockPos support;
    private BlockPos basePos;
    private BlockPos elementPos;
    private BlockPos flagPos;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType gameType;
    private boolean sneaking;
    private boolean shift;
    private boolean noGravity;

    FormationCheck(List<String> report, String levelId) {
        this(report, levelId, false);
    }

    FormationCheck(List<String> report, String levelId, boolean spaceOnly) {
        this(report, levelId, spaceOnly, false);
    }

    FormationCheck(List<String> report, String levelId, boolean spaceOnly, boolean yangOnly) {
        super("阵法", report, levelId);
        this.spaceOnly = spaceOnly;
        this.yangOnly = yangOnly;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 10) return false;
                if (yangOnly) {
                    onServer(this::checkYangEffects);
                    onServer(this::cleanup);
                    finish();
                    return true;
                }
                if (spaceOnly) {
                    onServer(this::checkSpaceEffects);
                    onServer(this::cleanup);
                    finish();
                    return true;
                }
                onServer(this::checkLayoutFlagsAndFuel);
            }
            case 2 -> {
                if (local < 1) return false;
                onServer(this::checkEffectsAndSerialization);
                leaveWorld();
            }
            case 3 -> { return reopened(local); }
            case 4 -> {
                if (!inWorld(local)) return false;
                onServer(this::checkReloadAndDrops);
                finish();
            }
            default -> throw new IllegalStateException("未知阵法检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        selected = player.getInventory().selected;
        gameType = player.gameMode.getGameModeForPlayer();
        sneaking = player.isShiftKeyDown();
        shift = minecraft.options.keyShift.isDown();
        noGravity = player.isNoGravity();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();

        floor = player.blockPosition().above(4);
        support = floor.east(2);
        basePos = floor.above();
        elementPos = basePos.offset(0, 0, -1);
        flagPos = basePos.above();
        for (BlockPos pos : List.of(floor, support, basePos, elementPos, flagPos)) {
            oldBlocks.put(pos, player.level().getBlockState(pos));
            BlockEntity original = player.level().getBlockEntity(pos);
            if (original != null) oldBlockEntities.put(pos, original.saveWithFullMetadata(player.registryAccess()));
        }
        player.level().getEntitiesOfClass(ItemEntity.class, new AABB(basePos).inflate(8))
                .forEach(drop -> oldDrops.add(drop.getUUID()));

        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(support, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(basePos, block("zhenji").defaultBlockState(), 3);
        player.level().setBlock(elementPos, block("zhenyvan").defaultBlockState(), 3);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        player.connection.teleport(support.getX() + 0.5, support.getY() + 1, support.getZ() + 0.5, yaw, pitch);
    }

    private void checkLayoutFlagsAndFuel(ServerPlayer player) {
        FormationBaseBlockEntity base = base(player);
        FormationElementBlockEntity element = element(player);
        expectTrue("阵基与阵元方块实体已创建", base != null && element != null);
        if (base == null || element == null) {
            cleanup(player);
            finish();
            return;
        }

        ItemStack material = new ItemStack(item("cailiao_135"));
        expectTrue("测试材料属于布阵与阴系来源标签", material.is(tag("buzhencailiao")) && material.is(tag("buzhenyin")));
        IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, elementPos, null);
        expectTrue("阵元无方向能力可访问", handler != null);
        if (handler == null) {
            cleanup(player);
            finish();
            return;
        }
        expectTrue("阵元接受来源布阵材料", handler.insertItem(0, material, false).isEmpty());
        base.detect();
        CompoundTag layout = base.formationSnapshot();
        int rank = materialRank(material);
        expectTrue("来源索引零解析八卦与上限", layout.getBoolean("zhenkun") && layout.getInt("zhenshangxian") == 5);
        expect("布局按材料等级计算阵阶", rank, layout.getInt("zhenLEVEL"));
        expect("无匹配配方时费用乘数保持零", 0, layout.getInt("powerneed"));
        expectTrue("单个阴材料满足当前空缺平衡", layout.getBoolean("zhenfazhengqve"));
        expectTrue("阵元回写阵基坐标与来源角度", element.formationOrigin().equals(basePos) && element.displayAngle() == -90F);

        ItemStack son = new ItemStack(item("flagsonitem"));
        player.setItemInHand(InteractionHand.MAIN_HAND, son);
        InteractionResult placed = son.getItem().useOn(context(player, basePos, Direction.UP));
        FormationMarkerBlockEntity placedMarker = player.level().getBlockEntity(flagPos) instanceof FormationMarkerBlockEntity value ? value : null;
        expectTrue("子旗物品放置成旗实体并保存团队", placed.consumesAction() && placedMarker != null
                && placedMarker.team().equals(Relations.team(player)));
        if (placedMarker == null) {
            cleanup(player);
            finish();
            return;
        }

        ItemStack mother = new ItemStack(item("flagmotheritem"));
        player.setItemInHand(InteractionHand.MAIN_HAND, mother);
        InteractionResult bound = mother.getItem().useOn(context(player, flagPos, Direction.NORTH));
        FormationFlagData binding = mother.getOrDefault(FormationAttachments.FLAG.get(), FormationFlagData.EMPTY);
        expectTrue("母旗绑定同队子旗且只记录目标坐标", bound.consumesAction() && placedMarker.childBound()
                && binding.children().size() == 1 && binding.children().getFirst().equals(flagPos));
        player.setItemInHand(InteractionHand.MAIN_HAND, mother);
        mother.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        CompoundTag control = base.formationSnapshot();
        expectTrue("母旗右键按绑定位置控制阵基", control.getBoolean("zhenbegin")
                && control.getString("ManaAttackUUID").equals(player.getStringUUID())
                && control.getDouble("playerX") == player.getX());

        ItemStack fuel = new ItemStack(item("cailiao_191"));
        player.setItemInHand(InteractionHand.MAIN_HAND, fuel);
        base.fuel(player);
        expect("下品灵石供能并消耗一份", 1, base.power());
        expectTrue("供能仅从主手取走一份", fuel.isEmpty());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private void checkSpaceEffects(ServerPlayer player) {
        FormationBaseBlockEntity base = base(player);
        if (base == null) throw new IllegalStateException("空间阵法检查前阵基不存在");
        BlockPos targetPos = basePos.offset(-3, 2, 0);
        Zombie target = spawn(player, targetPos);
        target.setNoAi(true);
        target.getPersistentData().putInt("LEVELnumber", 3);
        configureSpace(base, "空间1", 2, targetPos);
        base.scheduledTick(player.serverLevel());
        expectTrue("空间一不减速高于阵阶的目标", !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
        configureSpace(base, "空间1", 3, targetPos);
        base.scheduledTick(player.serverLevel());
        expectTrue("空间一给同阶目标施加20刻20级缓慢", target.getEffect(MobEffects.MOVEMENT_SLOWDOWN) != null
                && target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() == 20
                && target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 20);
        configureSpace(base, "空间2", 3, targetPos);
        base.scheduledTick(player.serverLevel());
        expectTrue("空间二向实体写入禁空标记", target.getPersistentData().getBoolean("jinkong"));

        boolean flying = Skills.flying(player);
        boolean airborne = Cultivation.tizhiBoolean(player, "takong");
        player.setGameMode(GameType.CREATIVE);
        Skills.setFlying(player, true);
        Cultivation.setTizhiBoolean(player, "takong", true);
        configureSpace(base, "空间2", 3, player.blockPosition());
        base.scheduledTick(player.serverLevel());
        SkillFlight.tick(player);
        expectTrue("空间二标记经技能推进关闭轻功与踏空并消费一次", !Skills.flying(player)
                && !Cultivation.tizhiBoolean(player, "takong") && !player.getPersistentData().getBoolean("jinkong"));
        Skills.setFlying(player, flying);
        Cultivation.setTizhiBoolean(player, "takong", airborne);
        player.setGameMode(GameType.SURVIVAL);

        FlyingInstrumentEntity instrument = EquipmentSetup.flyingInstrumentType("flyinginstrument_01").get().create(player.serverLevel());
        if (instrument == null) throw new IllegalStateException("空间阵法检查未能创建飞行法器");
        instrument.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0, 0);
        player.serverLevel().addFreshEntity(instrument);
        createdEntities.add(instrument.getUUID());
        player.startRiding(instrument, true);
        instrument.setNoGravity(true);
        configureSpace(base, "空间2", 3, instrument.blockPosition());
        base.scheduledTick(player.serverLevel());
        instrument.baseTick();
        expectTrue("空间二标记经骑乘法器推进恢复重力并消费一次", !instrument.isNoGravity()
                && !instrument.getPersistentData().getBoolean("jinkong"));
        player.stopRiding();
        instrument.discard();

        Zombie teleported = spawn(player, basePos.south());
        BlockPos destination = basePos.offset(10, 3, 10);
        configureSpace(base, "空间4", 3, destination);
        base.getPersistentData().putInt("zhenjvli", 4);
        base.scheduledTick(player.serverLevel());
        expectTrue("空间四将阵基范围目标传送到绝对坐标", teleported.position().distanceToSqr(Vec3.atLowerCornerOf(destination)) < 1e-6);

        player.connection.teleport(basePos.getX() + .25, basePos.getY(), basePos.getZ() + .25, yaw, pitch);
        configureSpace(base, "空间5", 3, destination);
        for (String key : List.of("zhenkun", "zhenli", "zhenqian", "zhenkan")) base.getPersistentData().putBoolean(key, false);
        for (String key : List.of("zhenzhen", "zhendui", "zhenxvn", "zhengen")) base.getPersistentData().putBoolean(key, true);
        base.scheduledTick(player.serverLevel());
        expectTrue("空间五主世界阵位映射经来源显示名命令传送玩家", player.level() == player.server.overworld()
                && player.position().distanceToSqr(Vec3.atLowerCornerOf(destination)) < 1e-6);
        expectTrue("空间三检测反馈命令已接入真实分派器", player.server.getCommands().getDispatcher().getRoot().getChild("zizhijiancefankui") != null);
    }

    private void configureSpace(FormationBaseBlockEntity base, String name, int rank, BlockPos target) {
        configureEffect(base, name, rank, 1, target);
        base.getPersistentData().putString("jinengleibie", "空间");
    }

    private void checkEffectsAndSerialization(ServerPlayer player) {
        FormationBaseBlockEntity base = base(player);
        if (base == null || !(player.level().getBlockEntity(flagPos) instanceof FormationMarkerBlockEntity marker)) {
            expectTrue("效果检查前阵基与子旗仍存在", false);
            return;
        }

        BlockPos soulPos = basePos.offset(-3, 2, 0);
        Zombie soulTarget = spawn(player, soulPos);
        configureEffect(base, "阴1", 3, 2, soulPos);
        base.getPersistentData().putInt("power", 2);
        base.scheduledTick(player.serverLevel());
        expectTrue("余额等于费用时不执行且不扣费", soulTarget.getPersistentData().getInt("soul") == 0 && base.power() == 2);
        base.getPersistentData().putInt("power", 3);
        base.getPersistentData().putBoolean("zhenbegin", true);
        base.scheduledTick(player.serverLevel());
        expect("阴一按阵阶增加实体 soul", 6, soulTarget.getPersistentData().getInt("soul"));
        expect("阴一执行扣除来源费用", 1, base.power());

        BlockPos attackPos = basePos.offset(0, 2, -3);
        Zombie attackTarget = spawn(player, attackPos);
        configureEffect(base, "阴2", 3, 1, attackPos);
        base.scheduledTick(player.serverLevel());
        CompoundTag attack = attackTarget.getPersistentData();
        expectTrue("阴二仅写入来源待消费伤害字段", attack.getInt("SoulAttackDamage") == 12
                && attack.getString("SoulAttackUUID").equals("wu") && attack.getString("damagetype").equals("阴阳")
                && attack.getDouble("Damage_multiplier") == 2.0);

        BlockPos discardPos = basePos.offset(3, 2, -3);
        Zombie discardTarget = spawn(player, discardPos);
        configureEffect(base, "阴3", 3, 1, discardPos);
        base.scheduledTick(player.serverLevel());
        expectTrue("阴三移除目标实体", discardTarget.isRemoved());

        BlockPos noAiPos = basePos.offset(-3, 2, 3);
        Zombie noAiTarget = spawn(player, noAiPos);
        configureEffect(base, "阴4", 3, 1, noAiPos);
        base.scheduledTick(player.serverLevel());
        expectTrue("阴四仅写入来源 NoAI 持久标记", noAiTarget.getPersistentData().getBoolean("NoAI"));

        CompoundTag baseSaved = base.saveWithFullMetadata(player.registryAccess());
        FormationBaseBlockEntity restoredBase = new FormationBaseBlockEntity(basePos, base.getBlockState());
        restoredBase.loadWithComponents(baseSaved, player.registryAccess());
        expectTrue("阵基能量与布局状态可序列化", restoredBase.power() == base.power()
                && restoredBase.formationSnapshot().getString("jinengname").equals("阴4")
                && restoredBase.formationSnapshot().getInt("zhenLEVEL") == 3);
        CompoundTag markerSaved = marker.saveWithFullMetadata(player.registryAccess());
        FormationMarkerBlockEntity restoredMarker = new FormationMarkerBlockEntity(flagPos, marker.getBlockState());
        restoredMarker.loadWithComponents(markerSaved, player.registryAccess());
        expectTrue("子旗团队、绑定态与存放物品可序列化", restoredMarker.team().equals(marker.team())
                && restoredMarker.childBound() && ItemStack.matches(restoredMarker.getItem(0), marker.getItem(0)));
    }

    private void checkYangEffects(ServerPlayer player) {
        FormationBaseBlockEntity base = base(player);
        FormationElementBlockEntity element = element(player);
        player.level().setBlock(flagPos, block("flagson").defaultBlockState(), 3);
        FormationMarkerBlockEntity marker = (FormationMarkerBlockEntity) player.level().getBlockEntity(flagPos);
        expectTrue("阳系入口前阵基阵元与子旗均绑定专属方块实体", base != null && element != null && marker != null);
        if (base == null || element == null || marker == null) return;

        configureEffect(base, "阳2", 3, 1, elementPos);
        base.getPersistentData().putString("jinengleibie", "阳");
        base.scheduledTick(player.serverLevel());
        expect("阳二经阵基入口向目标方块累加三点罡气", 3, FormationProtection.amount(element));
        base.getPersistentData().putBoolean("zhenbegin", true);
        base.getPersistentData().putInt("power", 2);
        base.scheduledTick(player.serverLevel());
        expect("阳二重复执行累加原罡气", 6, FormationProtection.amount(element));
        FormationElementBlockEntity restored = new FormationElementBlockEntity(elementPos, element.getBlockState());
        restored.loadWithComponents(element.saveWithFullMetadata(player.registryAccess()), player.registryAccess());
        expect("阳二共享附件经方块实体序列化保留", 6, FormationProtection.amount(restored));

        BlockPos target = basePos.offset(-3, 4, -3);
        configureEffect(base, "阳1", 3, 1, target);
        base.getPersistentData().putString("jinengleibie", "阳");
        base.getPersistentData().putString("ManaAttackUUID", player.getStringUUID());
        base.scheduledTick(player.serverLevel());
        List<FormationLauncher> launchers = player.level().getEntitiesOfClass(FormationLauncher.class, new AABB(target).inflate(4));
        expect("阳一真实阵基入口只生成一个发射体", 1, launchers.size());
        if (launchers.size() == 1) {
            FormationLauncher launcher = launchers.getFirst();
            createdEntities.add(launcher.getUUID());
            expectTrue("阳一复制阵阶、队伍与攻击者", launcher.getPersistentData().getInt("zhenLEVEL") == 3
                    && Relations.entityTeam(launcher).equals(marker.team())
                    && launcher.getPersistentData().getString("ManaAttackUUID").equals(player.getStringUUID()));
            launcher.baseTick();
            List<FormationArrow> arrows = player.level().getEntitiesOfClass(FormationArrow.class, launcher.getBoundingBox().inflate(4));
            expect("阳一受控首刻生成一个原生箭弹体", 1, arrows.size());
            if (arrows.size() == 1) {
                FormationArrow arrow = arrows.getFirst();
                createdEntities.add(arrow.getUUID());
                arrow.tick();
                expectTrue("箭首刻复制阵阶倍率且保持来源漏写攻击者和筛选模式", arrow.isNoGravity()
                        && arrow.getPersistentData().getInt("ManaAttackDamage") == 3
                        && arrow.getPersistentData().getDouble("Damage_multiplier") == 2
                        && arrow.getPersistentData().getString("ManaAttackUUID").isEmpty()
                        && arrow.getPersistentData().getInt("zhenmubiao") == 0
                        && arrow.getPierceLevel() == 5 && arrow.getBaseDamage() == .1);
                arrow.getPersistentData().putInt("tick", 19);
                arrow.tick();
                expectTrue("箭有所有者时第二十刻执行来源移除", arrow.isRemoved());
            }
            for (int tick = 1; tick < 10; tick++) launcher.baseTick();
            expectTrue("阳一发射体受控第十刻移除", launcher.isRemoved());
        }

        configureEffect(base, "阳3", 4, 1, target);
        base.getPersistentData().putString("jinengleibie", "阳");
        base.scheduledTick(player.serverLevel());
        Vec3 high = Vec3.atLowerCornerOf(target).add(0, 32, 0);
        List<FormationFalling> falling = player.level().getEntitiesOfClass(FormationFalling.class, new AABB(high, high).inflate(1));
        expect("阳三真实命令在指定点上方三十二格生成落实体", 1, falling.size());
        if (falling.size() == 1) {
            FormationFalling entity = falling.getFirst();
            createdEntities.add(entity.getUUID());
            expectTrue("阳三复制来源字段与旗队伍", entity.getPersistentData().getInt("zhenLEVEL") == 4
                    && Relations.entityTeam(entity).equals(marker.team()));
            entity.baseTick();
            expectTrue("阳三下方空气时受控首刻即移除", entity.isRemoved());
        }

        Zombie unchanged = spawn(player, target);
        for (String category : List.of("金", "水", "木", "火", "土")) {
            configureEffect(base, category + "1", 3, 1, target);
            base.getPersistentData().putString("jinengleibie", category);
            base.scheduledTick(player.serverLevel());
            expectTrue(category + "系保持来源阴名分支错配并扣来源费用",
                    unchanged.getPersistentData().getInt("soul") == 0 && base.power() == 1);
        }
        configureEffect(base, "阳2", 3, 0, elementPos);
        base.getPersistentData().putString("jinengleibie", "阳");
        base.scheduledTick(player.serverLevel());
        expectTrue("零费用来源门槛保持不执行也不扣费", FormationProtection.amount(element) == 6 && base.power() == 1);
    }

    private void checkReloadAndDrops(ServerPlayer player) {
        FormationBaseBlockEntity base = base(player);
        FormationElementBlockEntity element = element(player);
        FormationMarkerBlockEntity marker = player.level().getBlockEntity(flagPos) instanceof FormationMarkerBlockEntity value ? value : null;
        expectTrue("保存重进恢复阵基、阵元与子旗", base != null && element != null && marker != null);
        if (base == null || element == null || marker == null) {
            cleanup(player);
            return;
        }
        expectTrue("保存重进保留布局、费用与灵力", base.power() == 1
                && base.formationSnapshot().getString("jinengname").equals("阴4")
                && base.formationSnapshot().getInt("zhenLEVEL") == 3);
        expectTrue("保存重进保留阵旗团队与绑定", marker.childBound()
                && marker.team().equals(Relations.team(player))
                && ItemStack.matches(marker.getItem(0), new ItemStack(item("flagsonitem"))));
        expectTrue("保存重进保留阵元材料与坐标", element.material().is(item("cailiao_135"))
                && element.formationOrigin().equals(basePos) && element.displayAngle() == -90F);

        player.getInventory().selected = 0;
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        player.gameMode.destroyBlock(flagPos);
        List<ItemEntity> drops = newDrops(player);
        expect("拆除子旗收回存放的旗物品", 1, countDrops(drops, item("flagsonitem")));
        expect("拆除旗方块不额外掉落方块物品", 0, countDrops(drops, item("flagson")));
        drops.forEach(ItemEntity::discard);
        cleanup(player);
    }

    private void configureEffect(FormationBaseBlockEntity base, String name, int rank, int cost, BlockPos target) {
        CompoundTag data = base.getPersistentData();
        data.putBoolean("zhenfazhengqve", true);
        data.putBoolean("zhenbegin", true);
        data.putBoolean("zhenzuobiao", true);
        data.putBoolean("zhensuiji", false);
        data.putString("jinengleibie", "阴");
        data.putString("jinengname", name);
        data.putInt("zhenLEVEL", rank);
        data.putInt("powerneed", cost);
        data.putInt("power", cost + 1);
        data.putInt("tick", 0);
        data.putInt("zhenchufa", 0);
        data.putInt("zhenmubiao", 0);
        data.putInt("zhenjvli", 2);
        data.putInt("zhenchufaX", target.getX());
        data.putInt("zhenchufaY", target.getY());
        data.putInt("zhenchufaZ", target.getZ());
    }

    private Zombie spawn(ServerPlayer player, BlockPos pos) {
        Zombie zombie = EntityType.ZOMBIE.create(player.serverLevel());
        if (zombie == null) throw new IllegalStateException("阵法检查未能创建目标僵尸");
        zombie.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        if (!player.serverLevel().addFreshEntity(zombie)) throw new IllegalStateException("阵法检查目标实体未能加入世界");
        createdEntities.add(zombie.getUUID());
        return zombie;
    }

    private FormationBaseBlockEntity base(ServerPlayer player) {
        return player.level().getBlockEntity(basePos) instanceof FormationBaseBlockEntity base ? base : null;
    }

    private FormationElementBlockEntity element(ServerPlayer player) {
        return player.level().getBlockEntity(elementPos) instanceof FormationElementBlockEntity element ? element : null;
    }

    private static UseOnContext context(ServerPlayer player, BlockPos clicked, Direction face) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), face, clicked, false);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(resource(path));
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(resource(path));
    }

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(resource(path));
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static int materialRank(ItemStack material) {
        for (int rank = 9; rank >= 2; rank--) {
            if (material.is(tag("zhenitemlevel_" + rank))) return rank;
        }
        return 1;
    }

    private List<ItemEntity> newDrops(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(basePos).inflate(8)).stream()
                .filter(drop -> !oldDrops.contains(drop.getUUID())).toList();
    }

    private static int countDrops(List<ItemEntity> drops, Item item) {
        return drops.stream().filter(drop -> drop.getItem().is(item)).mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private void cleanup(ServerPlayer player) {
        for (UUID uuid : createdEntities) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        newDrops(player).forEach(ItemEntity::discard);
        for (Map.Entry<BlockPos, BlockState> entry : oldBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            player.level().setBlock(pos, entry.getValue(), 3);
            if (oldBlockEntities.containsKey(pos)) {
                BlockEntity restored = player.level().getBlockEntity(pos);
                if (restored != null) {
                    restored.loadWithComponents(oldBlockEntities.get(pos), player.registryAccess());
                    restored.setChanged();
                    player.level().sendBlockUpdated(pos, entry.getValue(), entry.getValue(), 3);
                }
            }
        }
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(gameType);
        player.setShiftKeyDown(sneaking);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.options.keyShift.setDown(shift));
    }
}
