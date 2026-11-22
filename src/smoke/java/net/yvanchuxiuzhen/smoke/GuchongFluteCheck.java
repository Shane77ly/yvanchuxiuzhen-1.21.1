package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.guchong.api.GuchongCapture;
import net.yvanchuxiuzhen.system.guchong.client.GuchongReleaseScreen;
import net.yvanchuxiuzhen.system.guchong.content.GuchongFluteItem;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarRuntime;
import net.yvanchuxiuzhen.system.guchong.content.GuchongReleaseMenu;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.guchong.network.GuchongReleaseButtonPayload;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 蛊笛真实右键与客户端按钮；分页、空UUID和部分收回各使用独立背包样本。 */
final class GuchongFluteCheck extends SystemCheck {
    private static final String MAIN_UUID = "r10_flute_main";
    private static final String PARTIAL_UUID = "r10_flute_partial";
    private static final String COMBAT_UUID = "r10_flute_combat";
    private final Screenshots screenshots;
    private final List<String> diagnostics;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> untouched = new ArrayList<>();
    private final List<GuchongEntity> specimens = new ArrayList<>();
    private final Map<BlockPos, BlockState> combatArenaBlocks = new LinkedHashMap<>();
    private List<GuchongEntity> active = List.of();
    private int selected;
    private GameType mode;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private BlockPos floor;
    private BlockState floorState;
    private CompoundTag savedFlute;
    private CompoundTag savedJar;
    private CompoundTag partialBefore;
    private Pig combatTarget;
    private ServerPlayer combatOwner;
    private GuchongEntity combatInsect;
    private boolean insectKilledTarget;
    private boolean observingCombat;
    private boolean combatArmed;
    private double oldInsectExperience;
    private double experienceAfterDeath;
    private long combatStartTime;
    private long lastCombatSample;
    private int combatStartAge;
    private int incomingEarly;
    private int incomingLate;
    private int damagePre;
    private int damagePost;
    private int deathEvents;
    private boolean meleeObserved;
    private double closestDistance = Double.POSITIVE_INFINITY;
    private final Consumer<LivingIncomingDamageEvent> incomingBefore = event -> {
        if (event.getEntity() != combatTarget) return;
        incomingEarly++;
        if (incomingEarly <= 4) diagnostic("INFO 蛊笛战斗 入伤初始#" + incomingEarly + " " + incomingState(event));
    };
    private final Consumer<LivingIncomingDamageEvent> incomingAfter = event -> {
        if (event.getEntity() != combatTarget) return;
        incomingLate++;
        if (incomingLate <= 4) diagnostic("INFO 蛊笛战斗 入伤末尾#" + incomingLate + " " + incomingState(event));
    };
    private final Consumer<LivingDamageEvent.Pre> beforeDamage = event -> {
        if (event.getEntity() != combatTarget) return;
        damagePre++;
        if (damagePre <= 4) diagnostic("INFO 蛊笛战斗 减伤前#" + damagePre + " amount=" + event.getNewDamage());
    };
    private final Consumer<LivingDamageEvent.Post> afterDamage = event -> {
        if (event.getEntity() != combatTarget) return;
        damagePost++;
        if (damagePost <= 4) diagnostic("INFO 蛊笛战斗 扣血后#" + damagePost + " original=" + event.getOriginalDamage()
                + " final=" + event.getNewDamage() + " 猪生命=" + combatTarget.getHealth());
    };
    private final Consumer<LevelTickEvent.Post> combatTick = event -> {
        if (combatArmed && event.getLevel() == combatInsect.level()) sampleCombat();
    };
    private final Consumer<LivingDeathEvent> combatDeath = event -> {
        if (event.getEntity() != combatTarget) return;
        deathEvents++;
        diagnostic("INFO 蛊笛战斗 死亡事件#" + deathEvents + " canceled=" + event.isCanceled()
                + " mobAttack=" + event.getSource().is(DamageTypes.MOB_ATTACK)
                + " 攻击者是虫=" + (event.getSource().getEntity() == combatInsect)
                + " 直接实体是虫=" + (event.getSource().getDirectEntity() == combatInsect));
        if (event.getSource().is(DamageTypes.MOB_ATTACK)
                && event.getSource().getEntity() == combatInsect && event.getSource().getDirectEntity() == combatInsect
                && !event.isCanceled()) {
            insectKilledTarget = true;
            experienceAfterDeath = Professions.experience(combatOwner, Profession.INSECT);
        }
    };

    GuchongFluteCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("蛊笛分页放收", report, levelId);
        this.screenshots = screenshots;
        this.diagnostics = report;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(local, 8, this::prepare);
            case 1 -> action(local, 4, player -> {
                StatusEffects.addHidden(player, "effect_002", 200, 0);
                use(player, false);
                expectTrue("沉默阻断普通右键开菜单", player.containerMenu == player.inventoryMenu);
                StatusEffects.remove(player, "effect_002");
            });
            case 2 -> action(local, 4, player -> use(player, false));
            case 3 -> firstPage(local);
            case 4 -> click(local, 0);
            case 5 -> action(local, 8, player -> page(player, 1, 0, 6));
            case 6 -> click(local, 1);
            case 7 -> lastPage(local);
            case 8 -> click(local, 0);
            case 9 -> action(local, 8, player -> page(player, 1, 0, 6));
            case 10 -> action(local, 4, this::displayAndMove);
            case 11 -> click(local, 2);
            case 12 -> action(local, 6, this::releasedMain);
            case 13 -> click(local, 2);
            case 14 -> action(local, 6, player -> {
                CompoundTag data = GuchongJarData.read(player.getInventory().getItem(22));
                expectTrue("全部收回后真实罐保存两虫当前生命经验并清计数", active.stream().allMatch(GuchongEntity::isRemoved)
                        && !data.getBoolean("shifangguchong") && data.getDouble("shifangnum") == 0
                        && data.getDouble("health_Num0") == 6.5 && data.getDouble("experience0") == 15
                        && data.getDouble("health_Num3") == 8.5 && data.getDouble("experience3") == 25);
                expect("收回展示同步真实罐", data, GuchongJarData.read(menu(player).getSlot(0).getItem()));
                StatusEffects.addHidden(player, "effect_002", 200, 0);
            });
            case 15 -> click(local, 2);
            case 16 -> action(local, 6, player -> {
                active = capture(player, MAIN_UUID, 2);
                expect("来源按钮在已开界面时不检查沉默", 2.0,
                        GuchongJarData.read(player.getInventory().getItem(22)).getDouble("shifangnum"));
                active.get(1).getPersistentData().putString("belongToMasterUUID", UUID.randomUUID().toString());
                player.closeContainer();
            });
            case 17 -> action(local, 4, player -> {
                use(player, true);
                expectTrue("沉默阻断潜行收回且不修改真实计数", active.stream().noneMatch(GuchongEntity::isRemoved)
                        && GuchongJarData.read(player.getInventory().getItem(22)).getDouble("shifangnum") == 2);
                StatusEffects.remove(player, "effect_002");
            });
            case 18 -> action(local, 4, player -> {
                use(player, true);
                CompoundTag data = GuchongJarData.read(player.getInventory().getItem(22));
                expectTrue("潜行仅收本人虫并逐只写真实罐", active.get(0).isRemoved() && !active.get(1).isRemoved()
                        && data.getBoolean("shifangguchong") && data.getDouble("shifangnum") == 1);
            });
            case 19 -> action(local, 4, player -> {
                active.get(1).getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
                use(player, true);
                CompoundTag data = GuchongJarData.read(player.getInventory().getItem(22));
                expectTrue("潜行收齐最后一只后清释放标记", active.get(1).isRemoved()
                        && !data.getBoolean("shifangguchong") && data.getDouble("shifangnum") == 0);
                checkUntouched(player);
                use(player, false);
                player.getInventory().selected = 1;
            });
            case 20 -> action(local, 8, player -> {
                expectTrue("换下主手蛊笛后原生刻关闭菜单", player.containerMenu == player.inventoryMenu);
                player.getInventory().selected = 0;
                savedFlute = GuchongJarData.read(player.getMainHandItem());
                savedJar = GuchongJarData.read(player.getInventory().getItem(22));
                expectTrue("蛊笛无消耗和冷却", player.getMainHandItem().getCount() == 1
                        && !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()));
            });
            case 21 -> { leaveWorld(); yield true; }
            case 22 -> reopened(local);
            case 23 -> inWorld(local);
            case 24 -> action(local, 4, player -> {
                expect("真实保存重进保留蛊笛页码和六行操作状态", savedFlute, GuchongJarData.read(player.getInventory().getItem(0)));
                expect("真实保存重进保留移动后背包罐完整组件", savedJar, GuchongJarData.read(player.getInventory().getItem(22)));
                checkUntouched(player);
                stage(player, "", 0);
            });
            case 25 -> click(local, 3);
            case 26 -> action(local, 6, player -> {
                active = capture(player, "", 1);
                expectTrue("空展示UUID按来源匹配独立空UUID真实罐", menu(player).getSlot(1).getItem().isEmpty()
                        && GuchongJarData.read(player.getInventory().getItem(9)).getDouble("shifangnum") == 1);
                player.closeContainer();
                use(player, true);
                expectTrue("空UUID本人虫可经潜行回罐", active.get(0).isRemoved()
                        && !GuchongJarData.read(player.getInventory().getItem(9)).getBoolean("shifangguchong"));
                stage(player, PARTIAL_UUID, 0, 1);
            });
            case 27 -> click(local, 2);
            case 28 -> action(local, 6, player -> {
                active = capture(player, PARTIAL_UUID, 2);
                partialBefore = GuchongJarData.read(player.getInventory().getItem(9));
                active.get(0).getPersistentData().putString("belongToMasterUUID", UUID.randomUUID().toString());
                active.get(0).setHealth(4.5F);
                active.get(1).moveTo(player.getX() + 20, player.getY() + 1, player.getZ());
            });
            case 29 -> click(local, 2);
            case 30 -> action(local, 6, player -> {
                expectTrue("按钮收回只比罐UUID，范围内他人虫同样被移除", active.get(0).isRemoved() && !active.get(1).isRemoved());
                expect("部分收回按来源不写真实罐", partialBefore, GuchongJarData.read(player.getInventory().getItem(9)));
                expect("部分收回按来源不更新展示副本", partialBefore, GuchongJarData.read(menu(player).getSlot(0).getItem()));
            });
            case 31 -> {
                if (!screen(local)) yield false;
                screenshots.take("guchong_flute_partial_recall.png");
                onServer(this::prepareCombat);
                yield true;
            }
            case 32 -> click(local, 2);
            case 33 -> action(local, 6, this::startCombat);
            case 34 -> settleCombat(local);
            case 35 -> combatFinished(local);
            case 36 -> click(local, 2);
            case 37 -> action(local, 6, this::checkCombatRecall);
            case 38 -> click(local, 2);
            case 39 -> action(local, 6, this::checkCombatRelease);
            case 40 -> {
                onServer(this::cleanup);
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知蛊笛检查步骤 " + step);
        };
    }

    private boolean action(int local, int wait, Consumer<ServerPlayer> action) {
        if (local < wait) return false;
        onServer(action);
        return true;
    }

    private boolean firstPage(int local) {
        if (!screen(local)) return false;
        expect("中文菜单标题", "蛊虫释放", minecraft.screen.getTitle().getString());
        expect("客户端首开页码保持来源0", 0.0, GuchongJarData.read(minecraft.player.getMainHandItem()).getDouble("pagenum"));
        onServer(player -> page(player, 0, 0, 6));
        screenshots.take("guchong_flute_first_page.png");
        return true;
    }

    private boolean lastPage(int local) {
        if (!screen(local)) return false;
        onServer(player -> page(player, 2, 6, 1));
        expect("客户端第二页同步第七只真实罐", "r10_flute_6",
                GuchongJarData.read(minecraft.player.containerMenu.getSlot(0).getItem()).getString("guguanuuid"));
        screenshots.take("guchong_flute_last_page.png");
        int id = minecraft.player.containerMenu.containerId;
        PacketDistributor.sendToServer(new GuchongReleaseButtonPayload(id + 100, 0));
        PacketDistributor.sendToServer(new GuchongReleaseButtonPayload(id, -1));
        PacketDistributor.sendToServer(new GuchongReleaseButtonPayload(id, 8));
        return true;
    }

    private boolean click(int local, int index) {
        if (!screen(local)) return false;
        List<Button> buttons = minecraft.screen.children().stream().filter(child -> child instanceof Button)
                .map(child -> (Button) child).toList();
        buttons.get(index).onPress();
        return true;
    }

    private boolean screen(int local) {
        if (local < 8) return false;
        if (minecraft.screen instanceof GuchongReleaseScreen) return true;
        if (local > 120) {
            fail("蛊笛菜单未出现：" + (minecraft.screen == null ? "无" : minecraft.screen.getClass().getSimpleName()));
            onServer(this::cleanup);
            finish();
        }
        return false;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) inventory.add(player.getInventory().getItem(index).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        floor = player.blockPosition().above(12);
        floorState = player.level().getBlockState(floor);
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 0);
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        ItemStack flute = item("fabao_018");
        if (!(flute.getItem() instanceof GuchongFluteItem)) throw new IllegalStateException("蛊笛运行实现未实际装配");
        player.getInventory().setItem(0, flute);
        player.getInventory().setItem(9, jar(player, MAIN_UUID, 0, 3));
        for (int index = 1; index < 7; index++) {
            ItemStack filler = jar(player, "r10_flute_" + index);
            player.getInventory().setItem(9 + index, filler);
            untouched.add(filler.copy());
        }
        player.inventoryMenu.broadcastChanges();
    }

    private void page(ServerPlayer player, double expectedPage, int start, int count) {
        GuchongReleaseMenu menu = menu(player);
        expect("来源分页实际页码", expectedPage, GuchongJarData.read(player.getMainHandItem()).getDouble("pagenum"));
        List<String> actual = new ArrayList<>();
        for (int index = 0; index < 6; index++) {
            ItemStack display = menu.getSlot(index).getItem();
            actual.add(display.isEmpty() ? "" : GuchongJarData.read(display).getString("guguanuuid"));
        }
        List<String> expected = new ArrayList<>();
        for (int index = 0; index < 6; index++) expected.add(index < count ? (start + index == 0 ? MAIN_UUID : "r10_flute_" + (start + index)) : "");
        expect("六展示槽来自当前分页真实库存", expected, actual);
    }

    private void displayAndMove(ServerPlayer player) {
        GuchongReleaseMenu menu = menu(player);
        ItemStack physical = player.getInventory().getItem(9);
        CompoundTag before = GuchongJarData.read(physical);
        expectTrue("展示罐与真实罐对象分离", menu.getSlot(0).getItem() != physical);
        expectTrue("六展示槽均禁止放入和拿取", menu.slots.subList(0, 6).stream()
                .allMatch(slot -> !slot.mayPickup(player) && !slot.mayPlace(physical)));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        expectTrue("真实点击和快速移动无法取出展示罐", menu.getCarried().isEmpty() && menu.quickMoveStack(player, 0).isEmpty());
        CompoundTag display = GuchongJarData.read(menu.getSlot(0).getItem());
        display.putString("fixture_copy_probe", "display_only");
        GuchongJarData.write(menu.getSlot(0).getItem(), display);
        expect("修改展示副本不反写真实库存", before, GuchongJarData.read(physical));
        menu.populate(player);
        player.getInventory().setItem(22, physical);
        player.getInventory().setItem(9, ItemStack.EMPTY);
        menu.broadcastChanges();
    }

    private void releasedMain(ServerPlayer player) {
        active = capture(player, MAIN_UUID, 2);
        CompoundTag data = GuchongJarData.read(player.getInventory().getItem(22));
        expectTrue("按钮按缓存UUID找到移动后的真实槽位释放", player.getInventory().getItem(9).isEmpty()
                && data.getBoolean("shifangguchong") && data.getDouble("shifangnum") == 2);
        expect("释放展示同步移动后真实罐", data, GuchongJarData.read(menu(player).getSlot(0).getItem()));
        expect("来源编号槽0和3随实体保存", List.of(0.0, 3.0), active.stream()
                .map(insect -> insect.getPersistentData().getDouble("guguanweizhi")).toList());
        active.get(0).setHealth(6.5F);
        active.get(0).getPersistentData().putDouble("experience", 15);
        active.get(1).setHealth(8.5F);
        active.get(1).getPersistentData().putDouble("experience", 25);
        checkUntouched(player);
    }

    private List<GuchongEntity> capture(ServerPlayer player, String uuid, int count) {
        List<GuchongEntity> insects = player.serverLevel().getEntitiesOfClass(GuchongEntity.class,
                player.getBoundingBox().inflate(40), insect -> !insect.isRemoved()
                        && insect.getPersistentData().getString("guguanuuid").equals(uuid)).stream()
                .sorted(Comparator.comparingDouble(insect -> insect.getPersistentData().getDouble("guguanweizhi"))).toList();
        if (insects.size() != count) throw new IllegalStateException("预期 " + count + " 只来源罐蛊虫，实际 " + insects.size() + "，UUID=" + uuid);
        expectTrue("释放真实蛊虫保留主人与罐UUID", insects.stream().allMatch(insect -> insect.isOwnedBy(player)
                && insect.getPersistentData().getString("belongToMasterUUID").equals(player.getStringUUID())));
        for (int index = 0; index < insects.size(); index++) {
            GuchongEntity insect = insects.get(index);
            insect.setNoAi(true);
            insect.setNoGravity(true);
            insect.setDeltaMovement(Vec3.ZERO);
            insect.moveTo(player.getX() + .5 + index, player.getY() + 1, player.getZ());
            specimens.add(insect);
        }
        return insects;
    }

    private void prepareCombat(ServerPlayer player) {
        player.closeContainer();
        discardSpecimens();
        if (!createCombatArena(player)) {
            fail("战斗场地被占用，无法安全建立受控平地");
            cleanup(player);
            finish();
            return;
        }
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 0);
        player.setDeltaMovement(Vec3.ZERO);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, item("fabao_018"));
        GuchongEntity wild = (GuchongEntity) GuchongJarRuntime.create(player.serverLevel(), "yvanchuxiuzhen:guchong_03");
        wild.moveTo(player.position());
        GuchongJarRuntime.finishSpawn(player.serverLevel(), wild);
        wild.getPersistentData().putDouble("experience", 119);
        expect("受控飞蝗出生经验上限", 120, wild.getPersistentData().getDouble("experience_Max"));
        expect("摄虫网收纳操作捕获野生飞蝗", GuchongCapture.Result.CAPTURED,
                GuchongCapture.captureWild(player.serverLevel(), wild, player.getStringUUID()));
        ItemStack captured = player.getInventory().items.stream().filter(stack -> stack.is(item("guchong_03item").getItem()))
                .findFirst().orElseThrow().copy();
        player.getInventory().clearContent();
        player.getInventory().setItem(0, item("fabao_018"));
        CompoundTag insectData = GuchongJarData.read(captured);
        insectData.putBoolean("guchongxvnfu", true);
        insectData.putString("belongToMasterUUID", player.getStringUUID());
        GuchongJarData.write(captured, insectData);
        ItemStack jar = item("taoguan_01item");
        CompoundTag data = new CompoundTag();
        data.putString("guguanuuid", COMBAT_UUID);
        GuchongJarData.pack(captured, data, 0, false);
        GuchongJarData.write(jar, data);
        player.getInventory().setItem(9, jar);
        combatOwner = player;
        oldInsectExperience = Professions.experience(player, Profession.INSECT);
        Professions.setExperience(player, Profession.INSECT, 0);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, incomingBefore);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, incomingAfter);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, beforeDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, afterDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, combatDeath);
        NeoForge.EVENT_BUS.addListener(combatTick);
        observingCombat = true;
        use(player, false);
    }

    private boolean createCombatArena(ServerPlayer player) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = 0; y <= 2; y++) {
                    if (y == 0 && x == 0 && z == 0) continue;
                    BlockPos pos = floor.offset(x, y, z);
                    if (!player.level().getBlockState(pos).isAir()) return false;
                }
            }
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos pos = floor.offset(x, 0, z);
                if (!(x == 0 && z == 0)) {
                    combatArenaBlocks.put(pos, player.level().getBlockState(pos));
                    player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                }
                if (Math.abs(x) < 5 && Math.abs(z) < 5) continue;
                for (int y = 1; y <= 2; y++) {
                    pos = floor.offset(x, y, z);
                    combatArenaBlocks.put(pos, player.level().getBlockState(pos));
                    player.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                }
            }
        }
        return true;
    }

    private void startCombat(ServerPlayer player) {
        active = capture(player, COMBAT_UUID, 1);
        combatInsect = active.getFirst();
        combatInsect.setNoGravity(false);
        combatInsect.setDeltaMovement(Vec3.ZERO);
        combatInsect.moveTo(floor.getX() + 1.5, floor.getY() + 1, floor.getZ() + .5);
        combatInsect.setOnGround(true);
        combatTarget = EntityType.PIG.create(player.serverLevel());
        combatTarget.moveTo(floor.getX() + 3.5, floor.getY() + 1, floor.getZ() + .5);
        combatTarget.setNoAi(true);
        combatTarget.setNoGravity(false);
        combatTarget.setDeltaMovement(Vec3.ZERO);
        combatTarget.setOnGround(true);
        combatTarget.setHealth(1);
        player.serverLevel().addFreshEntity(combatTarget);
        expect("受控猪的原生生命上限", 10, combatTarget.getMaxHealth());
        expectTrue("战斗前蛊笛实体保留同一捕获虫的等级与经验",
                combatInsect.getPersistentData().getDouble("LEVELnumber") == 1
                        && combatInsect.getPersistentData().getDouble("experience") == 119
                        && combatInsect.getPersistentData().getDouble("experience_Max") == 120);
    }

    private boolean settleCombat(int local) {
        boolean[] ready = {false};
        onServer(player -> {
            if (!combatInsect.onGround() || !combatTarget.onGround()) return;
            Path path = combatInsect.getNavigation().createPath(combatTarget, 0);
            boolean reachable = path != null && path.canReach();
            double ownerDistance = combatInsect.distanceToSqr(player);
            diagnostic("INFO 蛊笛战斗 平地前置 虫着地=" + combatInsect.onGround() + " 猪着地=" + combatTarget.onGround()
                    + " 虫重力=" + !combatInsect.isNoGravity() + " 猪重力=" + !combatTarget.isNoGravity()
                    + " 虫猪距离平方=" + combatInsect.distanceToSqr(combatTarget) + " 主人距离平方=" + ownerDistance
                    + " 路径=" + (path == null ? "null" : "reachable=" + path.canReach() + " target=" + path.getTarget()));
            expectTrue("战斗前双方着地且启用重力", !combatInsect.isNoGravity() && !combatTarget.isNoGravity());
            expectTrue("战斗前地面路径可到达受控猪", reachable);
            expectTrue("战斗前主人距离小于跟随门槛", ownerDistance < 100);
            if (!reachable || ownerDistance >= 100) {
                cleanup(player);
                finish();
                return;
            }
            combatInsect.setNoAi(false);
            combatInsect.setTarget(combatTarget);
            beginCombat(player);
            ready[0] = true;
        });
        if (!ready[0] && local > 120 && observingCombat) {
            onServer(player -> {
                fail("战斗平地前置未在120客户端刻内着地：虫=" + combatInsect.position() + " 虫着地=" + combatInsect.onGround()
                        + " 猪=" + combatTarget.position() + " 猪着地=" + combatTarget.onGround());
                cleanup(player);
                finish();
            });
        }
        return ready[0];
    }

    private void beginCombat(ServerPlayer player) {
        combatStartTime = player.serverLevel().getGameTime();
        lastCombatSample = combatStartTime - 20;
        combatStartAge = combatInsect.tickCount;
        combatArmed = true;
        diagnostic("INFO 蛊笛战斗 初始关系 猪队伍=" + Relations.team(combatTarget)
                + " 虫队伍=" + Relations.team(combatInsect) + " 猪视虫为友=" + Relations.isFriendly(combatTarget, combatInsect)
                + " 虫原生攻击=" + combatInsect.getAttributeValue(Attributes.ATTACK_DAMAGE)
                + " 猪原生免疫虫攻击=" + combatTarget.isInvulnerableTo(player.damageSources().mobAttack(combatInsect)));
        sampleCombat();
    }

    private void diagnostic(String line) {
        diagnostics.add(line);
    }

    private String incomingState(LivingIncomingDamageEvent event) {
        return "canceled=" + event.isCanceled() + " original=" + event.getOriginalAmount() + " amount=" + event.getAmount()
                + " mobAttack=" + event.getSource().is(DamageTypes.MOB_ATTACK)
                + " 攻击者是虫=" + (event.getSource().getEntity() == combatInsect)
                + " 直接实体是虫=" + (event.getSource().getDirectEntity() == combatInsect);
    }

    private void sampleCombat() {
        long now = combatInsect.level().getGameTime();
        double distance = combatInsect.distanceToSqr(combatTarget);
        closestDistance = Math.min(closestDistance, distance);
        boolean melee = combatInsect.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.isRunning() && goal.getGoal() instanceof MeleeAttackGoal);
        meleeObserved |= melee;
        if (now - lastCombatSample < 20) return;
        lastCombatSample = now;
        diagnostic("INFO 蛊笛战斗 采样 gameTicks=" + (now - combatStartTime)
                + " 虫实体刻=" + (combatInsect.tickCount - combatStartAge)
                + " 虫=" + combatInsect.position() + " 猪=" + combatTarget.position()
                + " 距离平方=" + distance + " 最小采样距离平方=" + closestDistance
                + " 原生近战范围=" + combatInsect.isWithinMeleeAttackRange(combatTarget)
                + " 自定义近战判距=" + (distance <= combatInsect.getBbWidth() * combatInsect.getBbWidth() + combatTarget.getBbWidth())
                + " 视线=" + combatInsect.getSensing().hasLineOfSight(combatTarget)
                + " 虫NoAI=" + combatInsect.isNoAi() + " 虫NoGravity=" + combatInsect.isNoGravity()
                + " 虫着地=" + combatInsect.onGround() + " 猪着地=" + combatTarget.onGround()
                + " 猪NoGravity=" + combatTarget.isNoGravity() + " 寻路结束=" + combatInsect.getNavigation().isDone()
                + " 攻击状态=" + combatInsect.isAggressive() + " 近战运行=" + melee + " 曾采到近战=" + meleeObserved
                + " 运行目标=" + combatInsect.goalSelector.getAvailableGoals().stream().filter(goal -> goal.isRunning())
                        .map(goal -> goal.getGoal().getClass().getName()).toList()
                + " 主人距离平方=" + combatInsect.distanceToSqr(combatOwner)
                + " 跟随主人运行=" + combatInsect.goalSelector.getAvailableGoals().stream()
                        .anyMatch(goal -> goal.isRunning() && goal.getGoal().getClass().getSimpleName().equals("GuchongFollowOwnerGoal"))
                + " 目标是猪=" + (combatInsect.getTarget() == combatTarget) + " 猪生命=" + combatTarget.getHealth()
                + " 入伤初始/末尾=" + incomingEarly + "/" + incomingLate + " 减伤前/扣血后=" + damagePre + "/" + damagePost
                + " 死亡=" + deathEvents);
    }

    private boolean combatFinished(int local) {
        boolean[] finished = {false};
        onServer(player -> {
            if (!insectKilledTarget || combatTarget.isAlive()) return;
            CompoundTag insect = combatInsect.getPersistentData();
            expectTrue("蛊笛放出的同一只飞蝗原生近战击杀受控猪", combatTarget.isDeadOrDying());
            expectTrue("普通猪按生命上限除五升级并保留剩余经验", insect.getDouble("LEVELnumber") == 2
                    && insect.getDouble("experience") == 1 && insect.getDouble("experience_Max") == 170
                    && insect.getString("LEVELname").equals("一阶低级"));
            expect("同次死亡事件后主人养蛊经验增加", 20, experienceAfterDeath);
            expect("升级后按贫弱生命资质回满", 122, combatInsect.getHealth());
            finished[0] = true;
        });
        boolean[] timedOut = {false};
        onServer(player -> timedOut[0] = player.serverLevel().getGameTime() - combatStartTime >= 240 || local > 1200);
        if (!finished[0] && timedOut[0]) {
            onServer(player -> fail("蛊笛飞蝗未在观察预算内原生击杀受控猪：客户端刻=" + local
                    + " 服务端刻=" + (player.serverLevel().getGameTime() - combatStartTime)
                    + " 虫实体刻=" + (combatInsect.tickCount - combatStartAge) + " 合格死亡事件=" + insectKilledTarget
                    + " 死亡事件总数=" + deathEvents + " 猪生命=" + combatTarget.getHealth()
                    + " 蛊虫存活=" + combatInsect.isAlive() + " 蛊虫目标=" + (combatInsect.getTarget() == combatTarget)
                    + " 曾采到近战=" + meleeObserved + " 最小采样距离平方=" + closestDistance
                    + " 入伤初始/末尾=" + incomingEarly + "/" + incomingLate + " 减伤前/扣血后=" + damagePre + "/" + damagePost));
            onServer(this::cleanup);
            finish();
        }
        return finished[0];
    }

    private void checkCombatRecall(ServerPlayer player) {
        CompoundTag data = GuchongJarData.read(player.getInventory().getItem(9));
        expectTrue("蛊笛按钮收回战斗后的同一只飞蝗并保存等级经验与生命", combatInsect.isRemoved()
                && !data.getBoolean("shifangguchong") && data.getDouble("shifangnum") == 0
                && data.getDouble("LEVELnumber0") == 2 && data.getDouble("experience0") == 1
                && data.getDouble("experience_Max0") == 170 && data.getDouble("health_Num0") == 122);
    }

    private void checkCombatRelease(ServerPlayer player) {
        active = capture(player, COMBAT_UUID, 1);
        CompoundTag data = active.getFirst().getPersistentData();
        expectTrue("再次通过蛊笛放出保留战斗成长与主人", data.getDouble("LEVELnumber") == 2
                && data.getDouble("experience") == 1 && data.getDouble("experience_Max") == 170
                && active.getFirst().getHealth() == 122 && active.getFirst().isOwnedBy(player));
    }

    private void stage(ServerPlayer player, String uuid, int... slots) {
        player.closeContainer();
        discardSpecimens();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, item("fabao_018"));
        player.getInventory().setItem(9, jar(player, uuid, slots));
        use(player, false);
    }

    private static ItemStack jar(ServerPlayer player, String uuid, int... slots) {
        ItemStack jar = item(slots.length > 1 ? "taoguan_02item" : "taoguan_01item");
        CompoundTag data = new CompoundTag();
        data.putString("guguanuuid", uuid);
        for (int slot : slots) {
            ItemStack insect = item("guchong_03item");
            insect.set(DataComponents.CUSTOM_NAME, Component.literal("蛊笛检查虫" + slot));
            CompoundTag insectData = new CompoundTag();
            insectData.putString("guchongid", "yvanchuxiuzhen:guchong_03");
            insectData.putString("belongToMasterUUID", player.getStringUUID());
            insectData.putBoolean("guchongxvnfu", true);
            insectData.putString("LEVELname", "炼气");
            insectData.putDouble("LEVELnumber", 1);
            insectData.putDouble("health_Max", 10);
            insectData.putDouble("health_Num", 9);
            insectData.putDouble("attack_Num", 1);
            insectData.putDouble("speed_number", .3);
            GuchongJarData.write(insect, insectData);
            GuchongJarData.pack(insect, data, slot, false);
        }
        GuchongJarData.write(jar, data);
        return jar;
    }

    private void checkUntouched(ServerPlayer player) {
        expectTrue("操作不改六只无关真实蛊罐", IntStream.range(0, untouched.size())
                .allMatch(index -> ItemStack.isSameItemSameComponents(untouched.get(index), player.getInventory().getItem(10 + index))));
    }

    private static GuchongReleaseMenu menu(ServerPlayer player) {
        if (player.containerMenu instanceof GuchongReleaseMenu menu) return menu;
        throw new IllegalStateException("服务端未打开蛊笛菜单");
    }

    private static void use(ServerPlayer player, boolean sneaking) {
        player.setShiftKeyDown(sneaking);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private void discardSpecimens() {
        for (GuchongEntity insect : specimens) if (!insect.isRemoved()) insect.discard();
        specimens.clear();
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        if (observingCombat) {
            combatArmed = false;
            NeoForge.EVENT_BUS.unregister(incomingBefore);
            NeoForge.EVENT_BUS.unregister(incomingAfter);
            NeoForge.EVENT_BUS.unregister(beforeDamage);
            NeoForge.EVENT_BUS.unregister(afterDamage);
            NeoForge.EVENT_BUS.unregister(combatDeath);
            NeoForge.EVENT_BUS.unregister(combatTick);
            Professions.setExperience(player, Profession.INSECT, oldInsectExperience);
            observingCombat = false;
        }
        if (combatTarget != null && !combatTarget.isRemoved()) combatTarget.discard();
        discardSpecimens();
        StatusEffects.remove(player, "effect_002");
        player.getInventory().clearContent();
        for (int index = 0; index < inventory.size(); index++) player.getInventory().setItem(index, inventory.get(index));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        combatArenaBlocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        combatArenaBlocks.clear();
        player.level().setBlock(floor, floorState, 3);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
