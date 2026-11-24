package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.DungeonController;
import net.yvanchuxiuzhen.system.dungeon.DungeonPlayers;
import net.yvanchuxiuzhen.system.dungeon.DungeonSourceData;
import net.yvanchuxiuzhen.system.dungeon.PaintingRuntime;
import net.yvanchuxiuzhen.system.dungeon.client.DungeonScreen;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenu;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonWorldData;
import net.yvanchuxiuzhen.system.dungeon.data.PaintingState;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/**
 * 副本必要路径：客户端真实放置、四页菜单和按钮、三波受控清敌、十件奖励、成功与超时回程、画卷双击往返和收回。
 * 奖励样本通过控制器公开初始化入口解析；失败样本只把已运行实例的剩余时间设为一秒。
 * 不验证保存重进、多人、镇妖塔自然调用、地灵与无归回程及1.18.2版本实机，不重跑藏宝开箱场景。
 */
final class DungeonCheck extends SystemCheck {
    private static final BlockPos FLOOR = new BlockPos(4096, 220, 4096);
    private static final BlockPos ENTRANCE = FLOOR.above();
    private static final BlockPos SAMPLE = ENTRANCE.east(4);
    private static final BlockPos MARKER = ENTRANCE.west(4);
    private static final BlockPos SUPPORT = FLOOR.offset(0, 2, -5);
    private static final BlockPos PAINTING = SUPPORT.south();
    private static final Vec3 ENTRANCE_STAND = Vec3.atBottomCenterOf(ENTRANCE).add(0, 0, -3);
    private static final Vec3 PAINTING_STAND = Vec3.atBottomCenterOf(FLOOR.above()).add(0, 0, -1.5);
    private static final Field AWAITING_TELEPORT = awaitingTeleportField();
    private static final List<List<String>> WAVES = List.of(
            List.of("yaoshou_001:1", "yaoshou_001:1", "yaoshou_001:2"),
            List.of("yaoshou_001:1", "yaoshou_001:2", "yaoshou_002:3"),
            List.of("yaoshou_001:2", "yaoshou_001:2", "yaoshou_002:3", "yaoshou_002:3", "yaoshou_002:5"));
    private final Screenshots screenshots;
    private final List<String> report;
    private final List<ItemStack> inventory = new ArrayList<>();
    private ServerLevel originalLevel;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private Difficulty originalDifficulty;
    private boolean originalInvulnerable;
    private boolean originalFlying;
    private boolean originalShift;
    private boolean originalKeyShift;
    private MobEffectInstance originalSlowFalling;
    private int originalSelected;
    private CompoundTag originalDungeon;
    private CompoundTag originalPersistent;
    private ItemStack originalTransit;
    private String originalTeam;
    private String originalEntityTeam;
    private BlockPos controllerPosition;
    private BlockPos privateExit;
    private Vec3 privateStand;
    private int clearedWaves;
    private int visibleMenuId = -1;
    private int visibleMenuTicks;
    private boolean initialized;

    DungeonCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("天道副本与画卷", report, levelId);
        this.screenshots = screenshots;
        this.report = report;
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
                onServer(this::initialize);
                minecraft.player.getInventory().selected = 0;
                return true;
            }
            case 1 -> {
                if (!waitFor(local, "入口支撑块与主手同步到客户端", minecraft.level.getBlockState(FLOOR).is(Blocks.STONE)
                        && minecraft.player.getMainHandItem().is(item("tiandaofuben").getItem()), 240)) return false;
                if (!settled(local, "入口放置站位", ENTRANCE_STAND)) return false;
                entranceDiagnostic("点击前");
                report.add("INFO 天道副本与画卷 入口点击返回=" + use(FLOOR, Direction.UP));
                return true;
            }
            case 2 -> {
                if (local == 1 || local == 160) entranceDiagnostic("等待放置第" + local + "刻");
                if (!waitFor(local, "客户端收到自然放置的入口", read(p -> isBlock(p.server.overworld(), ENTRANCE, "tiandaofuben"))
                        && minecraft.level.getBlockState(ENTRANCE).is(DungeonPlayers.block("tiandaofuben")), 160)) return false;
                onServer(p -> {
                    CompoundTag data = block(p.server.overworld(), ENTRANCE).getPersistentData();
                    expect("自然入口默认名称", "花妖连战", data.getString("fubenname"));
                    expect("默认定义完整保存", DungeonSourceData.DEFAULT, data.getString("fubenshujv"));
                    expectTrue("默认0–40级、10人、预览5、冷却240秒", data.getDouble("fubenminlevel") == 0
                            && data.getDouble("fubenmaxlevel") == 40 && data.getDouble("fubenpeoplemax") == 10
                            && data.getDouble("fubenyvlan") == 5 && data.getDouble("fubenjieshujishiMax") == 240);
                    checkEntranceCapability(p.server.overworld());
                    p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 4));
                    p.inventoryMenu.broadcastChanges();
                });
                return true;
            }
            case 3 -> {
                if (local < 8) return false;
                use(ENTRANCE, Direction.UP);
                return true;
            }
            case 4 -> {
                if (!menu(local, DungeonMenu.MANAGE, "天道副本设置")) return false;
                List<EditBox> inputs = inputs();
                if (!require("管理页十项输入均为空", inputs.size() == 10 && inputs.stream().allMatch(input -> input.getValue().isEmpty()))) return false;
                screenshots.take("dungeon_manage.png");
                inputs.get(2).setValue("甲".repeat(16) + "乙".repeat(16) + "尾");
                pressSet();
                return true;
            }
            case 5 -> {
                if (!waitFor(local, "管理按钮写回简介并按十六字拆行", read(p -> {
                    CompoundTag data = block(p.server.overworld(), ENTRANCE).getPersistentData();
                    return data.getString("text01").equals("甲".repeat(16)) && data.getString("text02").equals("乙".repeat(16))
                            && data.getString("text03").equals("尾") && data.getString("fubenshujv").equals(DungeonSourceData.DEFAULT);
                }), 100)) return false;
                clickSlot(28, 0);
                clickSlot(0, 1);
                clickSlot(0, 1);
                clickSlot(28, 0);
                return true;
            }
            case 6 -> {
                if (!waitFor(local, "真实槽操作放入两枚门票并保留两枚余件", read(p -> block(p.server.overworld(), ENTRANCE).getItem(0).getCount() == 2
                        && p.getInventory().countItem(Items.IRON_INGOT) == 2), 100)) return false;
                minecraft.player.closeContainer();
                if (!read(this::createSamples)) return false;
                onServer(p -> move(p, Vec3.atBottomCenterOf(SAMPLE).add(0, 0, -3)));
                return true;
            }
            case 7 -> {
                if (!settled(local, "控制器站位", Vec3.atBottomCenterOf(SAMPLE).add(0, 0, -3))) return false;
                use(SAMPLE, Direction.UP);
                return true;
            }
            case 8 -> {
                if (!menu(local, DungeonMenu.CONTROLLER, "副本控制方块")) return false;
                onServer(p -> {
                    DungeonMenu serverMenu = (DungeonMenu) p.containerMenu;
                    DungeonMenu clientMenu = ((DungeonScreen) minecraft.screen).getMenu();
                    expectTrue("控制器45格奖励只读", serverMenu.slots.size() == 81
                            && serverMenu.slots.subList(0, 45).stream().noneMatch(slot -> slot.mayPickup(p) || slot.mayPlace(new ItemStack(Items.STONE))));
                    expectTrue("超过物品上限的32枚珍珠在服务端与客户端均保留", serverMenu.getSlot(5).getItem().getCount() == 32
                            && clientMenu.getSlot(5).getItem().getCount() == 32);
                    for (int slot = 0; slot < 5; slot++) expectTrue("奖励样本双端组件一致" + slot,
                            ItemStack.matches(serverMenu.getSlot(slot).getItem(), clientMenu.getSlot(slot).getItem()));
                });
                screenshots.take("dungeon_controller.png");
                minecraft.player.closeContainer();
                onServer(p -> move(p, Vec3.atBottomCenterOf(MARKER).add(0, 0, -3)));
                return true;
            }
            case 9 -> {
                if (!settled(local, "占位符站位", Vec3.atBottomCenterOf(MARKER).add(0, 0, -3))) return false;
                use(MARKER, Direction.UP);
                return true;
            }
            case 10 -> {
                if (!menu(local, DungeonMenu.MARKER, "副本实体占位")) return false;
                if (!require("占位页无物品槽且攻击框初始未选", minecraft.player.containerMenu.slots.isEmpty()
                        && minecraft.screen.children().stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).noneMatch(Checkbox::selected))) return false;
                inputs().getFirst().setValue("zhanweiSmoke");
                Checkbox checkbox = minecraft.screen.children().stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
                checkbox.onPress();
                pressSet();
                screenshots.take("dungeon_marker.png");
                return true;
            }
            case 11 -> {
                if (!waitFor(local, "占位页真实按钮保存ID与攻击位", read(p -> {
                    CompoundTag data = block(p.server.overworld(), MARKER).getPersistentData();
                    return data.getString("zhanweifuid").equals("zhanweiSmoke") && data.getBoolean("entityshengchenggongji");
                }), 100)) return false;
                minecraft.player.closeContainer();
                onServer(p -> {
                    p.setGameMode(GameType.SURVIVAL);
                    move(p, ENTRANCE_STAND);
                });
                return true;
            }
            case 12 -> {
                if (!settled(local, "普通入口站位", ENTRANCE_STAND)) return false;
                use(ENTRANCE, Direction.UP);
                return true;
            }
            case 13 -> {
                if (!menu(local, DungeonMenu.OPEN, "天道副本")) return false;
                onServer(p -> {
                    DungeonMenu menu = (DungeonMenu) p.containerMenu;
                    expectTrue("普通入口只展示门票与预览5", menu.data().getDouble("fubenyvlan") == 5
                            && !menu.getSlot(0).mayPickup(p) && !menu.getSlot(0).mayPlace(new ItemStack(Items.STONE)));
                });
                screenshots.take("dungeon_open.png");
                pressEnter();
                return true;
            }
            case 14 -> {
                if (!waitFor(local, "真实入场按钮进入画卷维度候场", read(p -> p.level().dimension().equals(DungeonPlayers.PAINTING)
                        && p.getData(DungeonData.PLAYER).getBoolean("fubenstate")) && minecraft.level.dimension().equals(DungeonPlayers.PAINTING), 240)) return false;
                onServer(p -> {
                    CompoundTag data = p.getData(DungeonData.PLAYER);
                    controllerPosition = position(data, "fubenzuobiao");
                    expectTrue("候场六秒倒计时与大厅自然生成", data.getDouble("daojishi") > 0 && data.getDouble("daojishi") <= 6
                            && DungeonWorldData.get(p.server).lobbyGenerated());
                    expect("进入时切换冒险模式", GameType.ADVENTURE, p.gameMode.getGameModeForPlayer());
                    expectTrue("真实门票只扣两枚并标记入口冷却", p.getInventory().countItem(Items.IRON_INGOT) == 0
                            && block(p.server.overworld(), ENTRANCE).getPersistentData().getBoolean("fubenbegin")
                            && block(p.server.overworld(), ENTRANCE).getItem(1).is(Items.FLINT));
                    expectTrue("保存入口回程坐标", position(data, "chuansong").equals(ENTRANCE)
                            && data.getString("chuansongid").equals("minecraft:overworld"));
                });
                screenshots.take("dungeon_lobby.png");
                return true;
            }
            case 15 -> {
                if (!waitFor(local, "六秒后原生实例控制器进入准备阶段", read(p -> {
                    if (!(p.serverLevel().getBlockEntity(controllerPosition) instanceof DungeonBlockEntity controller)) return false;
                    return controller.getPersistentData().getBoolean("fubenstate") && controller.getPersistentData().getString("fubenjindu").equals("准备");
                }), 240)) return false;
                onServer(p -> {
                    DungeonBlockEntity controller = block(p.serverLevel(), controllerPosition);
                    CompoundTag data = controller.getPersistentData();
                    expectTrue("默认三波3/3/5及区域边界有效", data.getInt("fubenbociMax") == 3 && data.getInt("fybenboci1num") == 3
                            && data.getInt("fybenboci2num") == 3 && data.getInt("fybenboci3num") == 5 && area(controller).contains(p.position()));
                    for (int slot = 0; slot < 5; slot++) expectTrue("默认奖励槽" + slot + "为100%两件特殊皮革", controller.getItem(slot).is(item("cailiao_001").getItem())
                            && controller.getItem(slot).getCount() == 2 && controller.getItem(slot).getHoverName().getString().equals("特殊皮革")
                            && data.getInt("gift" + slot + "qvanzhong") == 100);
                });
                return true;
            }
            case 16 -> {
                return waves(local);
            }
            case 17 -> {
                if (!returned(local, "成功十秒后回程并恢复生存模式")) return false;
                onServer(p -> {
                    expect("成功总奖励十件", 10, leather(p));
                    p.getInventory().selected = 8;
                    p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));
                    p.inventoryMenu.broadcastChanges();
                    move(p, ENTRANCE_STAND);
                });
                minecraft.player.getInventory().selected = 8;
                return true;
            }
            case 18 -> {
                if (!settled(local, "失败样本入口站位", ENTRANCE_STAND)) return false;
                use(ENTRANCE, Direction.UP);
                return true;
            }
            case 19 -> {
                if (!menu(local, DungeonMenu.OPEN, "天道副本")) return false;
                pressEnter();
                return true;
            }
            case 20 -> {
                if (local == 1 || local == 300) onServer(p -> {
                    CompoundTag state = p.getData(DungeonData.PLAYER);
                    DungeonBlockEntity controller = block(p.server.getLevel(DungeonPlayers.PAINTING), controllerPosition);
                    CompoundTag data = controller.getPersistentData();
                    report.add("INFO 天道副本与画卷 二次入场第" + local + "刻：维度=" + p.level().dimension().location()
                            + "，位置=" + p.position() + "，玩家候场=" + state.getBoolean("fubenstate") + "/" + state.getDouble("daojishi")
                            + "，控制点=" + position(state, "fubenzuobiao") + "，入口冷却=" + block(p.server.overworld(), ENTRANCE).getPersistentData().getBoolean("fubenbegin")
                            + "，铁锭=" + p.getInventory().countItem(Items.IRON_INGOT) + "，控制器移除=" + controller.isRemoved()
                            + "，开关=" + data.getBoolean("fubenopen") + "/" + data.getBoolean("fubenstate") + "，阶段=" + data.getString("fubenjindu"));
                });
                if (!waitFor(local, "第二次自然入场并初始化原控制器", read(p -> p.level().dimension().equals(DungeonPlayers.PAINTING)
                        && !p.getData(DungeonData.PLAYER).getBoolean("fubenstate")
                        && block(p.serverLevel(), controllerPosition).getPersistentData().getBoolean("fubenstate")), 360)) return false;
                onServer(p -> {
                    DungeonBlockEntity controller = block(p.serverLevel(), controllerPosition);
                    expect("失败样本仍处于原生准备阶段", "准备", controller.getPersistentData().getString("fubenjindu"));
                    controller.getPersistentData().putInt("fubenjieshutime", 1);
                    controller.changed();
                });
                return true;
            }
            case 21 -> {
                if (!returned(local, "受控超时失败经延后任务回程并复位入口")) return false;
                onServer(p -> {
                    expect("失败不新增成功奖励", 10, leather(p));
                    expectTrue("失败控制器停止且清空倒计时", !block(p.server.getLevel(DungeonPlayers.PAINTING), controllerPosition).getPersistentData().getBoolean("fubenstate")
                            && block(p.server.getLevel(DungeonPlayers.PAINTING), controllerPosition).getPersistentData().getInt("fubenjieshutime") == 0);
                    p.serverLevel().setBlock(SUPPORT, Blocks.STONE.defaultBlockState(), 3);
                    p.setItemInHand(InteractionHand.MAIN_HAND, item("fabao_00901"));
                    p.inventoryMenu.broadcastChanges();
                    move(p, PAINTING_STAND, 180);
                });
                return true;
            }
            case 22 -> {
                if (!settled(local, "画卷自然放置站位", PAINTING_STAND)) return false;
                use(SUPPORT, Direction.SOUTH);
                return true;
            }
            case 23 -> {
                if (!waitFor(local, "画卷真实放置并清空主手", read(p -> isBlock(p.server.overworld(), PAINTING, "fabao_009")
                        && p.getMainHandItem().isEmpty()) && minecraft.player.getMainHandItem().isEmpty()
                        && minecraft.level.getBlockState(PAINTING).is(DungeonPlayers.block("fabao_009")), 120)) return false;
                onServer(p -> {
                    DungeonBlockEntity painting = block(p.server.overworld(), PAINTING);
                    PaintingState state = PaintingRuntime.state(painting.getItem(0));
                    expectTrue("画卷单槽一件并记录主人和入口坐标", painting.getContainerSize() == 1 && painting.getItem(0).getCount() == 1
                            && state.ownerUuid().equals(p.getStringUUID())
                            && state.rukouPos().equals(PAINTING));
                    checkClosedSides(p.server.overworld(), PAINTING, 1);
                });
                screenshots.take("dungeon_painting_placed.png");
                use(PAINTING, Direction.SOUTH);
                return true;
            }
            case 24 -> {
                if (local < 8) return false;
                if (!require("画卷首次只启动动画1及八秒窗口", read(p -> p.level().dimension().equals(Level.OVERWORLD)
                        && block(p.server.overworld(), PAINTING).getPersistentData().getBoolean("chuanyve")
                        && block(p.server.overworld(), PAINTING).animation() == 1
                        && !PaintingRuntime.state(block(p.server.overworld(), PAINTING).getItem(0)).activated()))) return false;
                use(PAINTING, Direction.SOUTH);
                return true;
            }
            case 25 -> {
                if (!waitFor(local, "画卷第二次右键自然生成私人出口", read(p -> p.level().dimension().equals(DungeonPlayers.PAINTING)
                        && PaintingRuntime.state(p.getData(DungeonData.PAINTING_TRANSIT)).placed())
                        && minecraft.level.dimension().equals(DungeonPlayers.PAINTING), 300)) return false;
                onServer(p -> {
                    PaintingState transit = PaintingRuntime.state(p.getData(DungeonData.PAINTING_TRANSIT));
                    privateExit = transit.chukouPos();
                    expectTrue("私人结构出口保存激活、主人和主世界回程", transit.activated()
                            && transit.ownerUuid().equals(p.getStringUUID())
                            && transit.returnDimension().equals("主世界")
                            && isBlock(p.serverLevel(), privateExit, "fabao_009")
                            && PaintingRuntime.state(block(p.serverLevel(), privateExit).getItem(0)).placed());
                    privateStand = Vec3.atBottomCenterOf(privateExit).add(0, 0, 3);
                    platform(p.serverLevel(), privateStand, 1);
                    move(p, privateStand, 180);
                });
                return true;
            }
            case 26 -> {
                if (!settled(local, "私人画卷出口站位", privateStand)) return false;
                screenshots.take("dungeon_painting_private.png");
                use(privateExit, Direction.SOUTH);
                return true;
            }
            case 27 -> {
                if (local < 8) return false;
                if (!require("私人出口首次右键仍只启动窗口", read(p -> p.level().dimension().equals(DungeonPlayers.PAINTING)
                        && block(p.serverLevel(), privateExit).getPersistentData().getBoolean("chuanyve")))) return false;
                use(privateExit, Direction.SOUTH);
                return true;
            }
            case 28 -> {
                if (!waitFor(local, "画卷第二次右键返回主世界并完成客户端确认", read(p -> p.level().dimension().equals(Level.OVERWORLD)
                        && acknowledged(p)) && minecraft.level.dimension().equals(Level.OVERWORLD), 300)) return false;
                onServer(p -> {
                    PaintingState stored = PaintingRuntime.state(block(p.server.overworld(), PAINTING).getItem(0));
                    expectTrue("回程更新原画卷且清除瞬时返回标志", stored.activated() && !stored.returning()
                            && p.position().distanceToSqr(Vec3.atLowerCornerOf(PAINTING)) < 25);
                    p.setShiftKeyDown(true);
                    move(p, PAINTING_STAND, 180);
                });
                minecraft.options.keyShift.setDown(true);
                return true;
            }
            case 29 -> {
                if (!settled(local, "画卷回收站位", PAINTING_STAND)) return false;
                use(PAINTING, Direction.SOUTH);
                return true;
            }
            case 30 -> {
                if (!waitFor(local, "主人潜行真实右键回收画卷", read(p -> !isBlock(p.server.overworld(), PAINTING, "fabao_009")
                        && p.getInventory().countItem(item("fabao_00901").getItem()) == 1), 120)) return false;
                onServer(p -> {
                    ItemStack painting = p.getInventory().items.stream().filter(stack -> stack.is(item("fabao_00901").getItem())).findFirst().orElseThrow();
                    PaintingState recovered = PaintingRuntime.state(painting);
                    expectTrue("回收保留主人、激活和私人出口坐标", painting.getCount() == 1 && recovered.activated()
                            && recovered.ownerUuid().equals(p.getStringUUID())
                            && recovered.chukouPos().equals(privateExit));
                });
                return true;
            }
            default -> {
                onServer(this::cleanup);
                finish();
                return true;
            }
        }
    }

    private void initialize(ServerPlayer player) {
        originalLevel = player.serverLevel();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalDifficulty = player.server.getWorldData().getDifficulty();
        originalInvulnerable = player.isInvulnerable();
        originalFlying = player.getAbilities().flying;
        originalShift = player.isShiftKeyDown();
        originalKeyShift = minecraft.options.keyShift.isDown();
        MobEffectInstance slowFalling = player.getEffect(MobEffects.SLOW_FALLING);
        originalSlowFalling = slowFalling == null ? null : new MobEffectInstance(slowFalling);
        originalSelected = player.getInventory().selected;
        originalDungeon = player.getData(DungeonData.PLAYER).copy();
        originalPersistent = player.getPersistentData().copy();
        originalTransit = player.getData(DungeonData.PAINTING_TRANSIT).copy();
        originalTeam = Relations.temporaryTeam(player);
        originalEntityTeam = Relations.entityTemporaryTeam(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        initialized = true;
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setData(DungeonData.PLAYER, new CompoundTag());
        player.setData(DungeonData.PAINTING_TRANSIT, ItemStack.EMPTY);
        for (String key : List.of("openfubengui", "fubenfanhui", "gamemoshi")) player.getPersistentData().remove(key);
        Relations.setTemporaryTeam(player, "");
        Relations.setEntityTemporaryTeam(player, "");
        player.setInvulnerable(true);
        player.setShiftKeyDown(false);
        minecraft.options.keyShift.setDown(false);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(player.server.overworld(), ENTRANCE_STAND.x, ENTRANCE_STAND.y, ENTRANCE_STAND.z, 0, 15);
        platform(player.server.overworld(), Vec3.atBottomCenterOf(FLOOR.above()), 7);
        move(player, ENTRANCE_STAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("tiandaofuben"));
        player.inventoryMenu.broadcastChanges();
        expectTrue("单人准入前本人属于友方", Relations.isFriendly(player, player));
    }

    private boolean createSamples(ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        level.setBlock(SAMPLE, DungeonPlayers.block("fubenblock").defaultBlockState(), 3);
        level.setBlock(MARKER, DungeonPlayers.block("fubenentity").defaultBlockState(), 3);
        DungeonBlockEntity controller = block(level, SAMPLE);
        CompoundTag state = controller.getPersistentData();
        state.putString("fubenshujv", "字段样本%0%240%" + sampleRewards());
        state.putBoolean("fubenopen", true);
        DungeonController.tick(controller);
        state.putBoolean("fubenstate", false);
        if (!require("五类奖励载体均为有效注册物品", controller.getItem(0).is(item("cailiao_001").getItem())
                && controller.getItem(1).is(item("book_006").getItem()) && controller.getItem(2).is(item("book_031").getItem())
                && controller.getItem(3).is(item("book_021").getItem()) && controller.getItem(4).is(item("treasure_chest_03").getItem())
                && IntStream.range(0, 5).noneMatch(slot -> controller.getItem(slot).isEmpty()))) return false;
        controller.setItem(5, new ItemStack(Items.ENDER_PEARL, 32));
        controller.changed();
        checkClosedSides(level, SAMPLE, 45);
        expect("材料奖励组件等级品阶", new MaterialInstance(17, 3), Materials.instance(controller.getItem(0)));
        if (!require("技能奖励具有所有者组件", Skills.book(controller.getItem(1)).isPresent())) return false;
        SkillBook skill = Skills.book(controller.getItem(1)).orElseThrow();
        expectTrue("技能所有者组件保留", skill.kind() == SkillKind.SPELL && skill.name().equals("字段法术")
                && skill.grade() == 12 && skill.level() == 11 && skill.damageMultiplier() == 1.75);
        LianqiBlueprint blueprint = Lianqi.blueprint(controller.getItem(2)).orElse(LianqiBlueprint.EMPTY);
        expectTrue("图纸所有者转换ID", blueprint.itemId().equals("yvanchuxiuzhen:fabao_001"));
        for (int slot = 0; slot < 9; slot++) expectTrue("图纸九材料" + slot,
                blueprint.ingredient(slot).id().equals("yvanchuxiuzhen:cailiao_001")
                        && blueprint.ingredient(slot).name().equals("材料" + slot)
                        && blueprint.ingredient(slot).count() == slot + 1);
        MedicineRecipe recipe = Liandan.recipe(controller.getItem(3));
        expectTrue("丹方所有者信息保留", recipe.name().equals("字段丹方"));
        for (int slot = 1; slot <= 8; slot++) expectTrue("丹方八材料" + slot,
                recipe.ingredient(slot).id().equals("yvanchuxiuzhen:cailiao_001")
                        && recipe.ingredient(slot).name().equals("药草" + slot));
        TreasureContainerConfig chest = TreasureContainers.config(controller.getItem(4));
        expectTrue("宝箱所有者五字段完整", chest.category().equals("材料") && chest.subtype().equals("全部")
                && chest.level() == 11 && chest.minimum() == 2 && chest.maximum() == 4);
        for (int slot = 0; slot < 5; slot++) {
            expectTrue("不写回未注册CustomData " + slot, !controller.getItem(slot).has(DataComponents.CUSTOM_DATA));
        }
        return true;
    }

    private static String sampleRewards() {
        String extra = "/smokeLogic/logic/真/smokeText/str/额外字段/smokeInt/int/7/smokeDouble/double/1.25/smokeEmpty/int/ ";
        List<String> gifts = new ArrayList<>();
        gifts.add("yvanchuxiuzhen:cailiao_001/无/100/2/cailiaolevel/int/17/cailiaopinjie/int/3" + extra);
        gifts.add("yvanchuxiuzhen:book_006/无/100/1/jinengName/char/字段法术/belongTopinjie/int/12/belongToLevel/int/11/belongTobody/char/火"
                + "/belongTojianjie/str/字段说明/jinengshanghai/doube/1.75/belongToItemID/char/yvanchuxiuzhen:book_006" + extra);
        StringBuilder blueprint = new StringBuilder("yvanchuxiuzhen:book_031/无/100/1/tuzhiname/str/字段图纸/tuzhishuoming/str/图纸说明"
                + "/fabaoid/char/yvanchuxitong:fabao_001/fabaoshuoming/str/成品说明/fabaotype/str/武器/item_level/str/下品/item_level_num/int/11");
        for (int slot = 0; slot < 9; slot++) blueprint.append("/cailiao0").append(slot).append("/char/yvanchuxitong:cailiao_001/cailiao0")
                .append(slot).append("name/str/材料").append(slot).append("/cailiao0").append(slot).append("num/int/").append(slot + 1);
        gifts.add(blueprint + extra);
        StringBuilder recipe = new StringBuilder("yvanchuxiuzhen:book_021/无/100/1/danyaoname/str/字段丹方/danfangshuoming/str/丹方说明"
                + "/danyaoshuoming/str/丹药说明/item_level/str/下品/item_level_num/int/11/danfangid/str/独立丹方标记");
        for (int slot = 1; slot <= 8; slot++) recipe.append("/cailiao0").append(slot).append("/char/yvanchuxitong:cailiao_001/cailiao0")
                .append(slot).append("name/str/药草").append(slot);
        gifts.add(recipe + extra);
        gifts.add("yvanchuxiuzhen:treasure_chest_03/无/100/1/TreasureTypeBig/str/材料/TreasureTypeSmall/str/全部/TreasureLEVEL/double/11"
                + "/minimumvalue/double/2/Maximumvalue/double/4" + extra);
        return String.join("&", gifts);
    }

    private boolean waves(int local) {
        if (local % 3 != 0) return waitFor(local, "默认三波自然推进", false, 1200);
        return read(player -> {
            DungeonBlockEntity controller = block(player.serverLevel(), controllerPosition);
            CompoundTag state = controller.getPersistentData();
            if (state.getBoolean("fubenclose")) {
                if (!require("三波均实际出现后成功", clearedWaves == 3)) return false;
                expectTrue("成功在原维度保留十秒退出倒计时", player.level().dimension().equals(DungeonPlayers.PAINTING)
                        && state.getInt("fubenjieshutime") > 0 && state.getInt("fubenjieshutime") <= 10);
                expect("成功立即发放十件特殊皮革", 10, leather(player));
                minecraft.execute(() -> screenshots.take("dungeon_success.png"));
                return true;
            }
            if (state.getString("fubenjindu").equals("监测敌人是否死亡") && state.getInt("fubenbocinum") == clearedWaves + 1) {
                List<Entity> enemies = enemies(player.serverLevel());
                List<String> actual = enemies.stream().map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath()
                        + ":" + entity.getPersistentData().getInt("LEVELnumber")).sorted().toList();
                if (!require("第" + (clearedWaves + 1) + "波实体种类和等级，实际" + actual, actual.equals(WAVES.get(clearedWaves)))) return false;
                expectTrue("本波刷怪均在控制器区域", enemies.stream().allMatch(entity -> area(controller).contains(entity.position())));
                for (Entity enemy : enemies) {
                    if (enemy instanceof Mob mob) mob.setNoAi(true);
                    enemy.setDeltaMovement(Vec3.ZERO);
                    enemy.discard();
                }
                clearedWaves++;
            }
            if (local > 1200) abort("默认三波推进超时，阶段=" + state.getString("fubenjindu") + "，已清波数=" + clearedWaves);
            return false;
        });
    }

    private boolean returned(int local, String label) {
        return waitFor(local, label, read(p -> p.level().dimension().equals(Level.OVERWORLD) && acknowledged(p)
                && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                && !block(p.server.overworld(), ENTRANCE).getPersistentData().getBoolean("fubenbegin")
                && !p.getData(DungeonData.PLAYER).getBoolean("fubenfanhui"))
                && minecraft.level.dimension().equals(Level.OVERWORLD), 400);
    }

    private boolean menu(int local, int kind, String title) {
        boolean opened = minecraft.screen instanceof DungeonScreen screen && screen.getMenu().kind() == kind
                && read(p -> p.containerMenu instanceof DungeonMenu menu && menu.kind() == kind);
        if (!waitFor(local, "打开" + title, opened, 160)) return false;
        int containerId = minecraft.player.containerMenu.containerId;
        if (visibleMenuId != containerId) {
            visibleMenuId = containerId;
            visibleMenuTicks = 0;
        }
        if (++visibleMenuTicks < 8) return false;
        expect("菜单中文标题", title, minecraft.screen.getTitle().getString());
        return true;
    }

    private boolean settled(int local, String label, Vec3 stand) {
        return waitFor(local, label + "传送确认且双端稳定落地", local >= 10 && minecraft.player.onGround() && read(p -> acknowledged(p) && p.onGround()
                && p.position().distanceToSqr(stand) < .04 && minecraft.player.position().distanceToSqr(p.position()) < .04), 200);
    }

    private boolean acknowledged(ServerPlayer player) {
        try {
            return AWAITING_TELEPORT.get(player.connection) == null && minecraft.level != null
                    && minecraft.level.dimension().equals(player.level().dimension());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Field awaitingTeleportField() {
        try {
            Field field = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingPositionFromClient");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private boolean waitFor(int local, String label, boolean ready, int timeout) {
        if (ready) {
            expectTrue(label, ready);
            return true;
        }
        if (local > timeout) abort(label + "超时");
        return false;
    }

    private boolean require(String label, boolean passed) {
        expectTrue(label, passed);
        if (!passed) abort("前置条件失败，停止依赖步骤：" + label);
        return passed;
    }

    private void abort(String message) {
        fail(message);
        if (minecraft.getSingleplayerServer().isSameThread()) cleanup(minecraft.getSingleplayerServer().getPlayerList().getPlayers().getFirst());
        else onServer(this::cleanup);
        finish();
    }

    private <T> T read(Function<ServerPlayer, T> action) {
        return minecraft.getSingleplayerServer().submit(() -> action.apply(minecraft.getSingleplayerServer().getPlayerList().getPlayers().getFirst())).join();
    }

    private InteractionResult use(BlockPos position, Direction face) {
        Vec3 point = Vec3.atCenterOf(position).add(face.getStepX() * .5, face.getStepY() * .5, face.getStepZ() * .5);
        return minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND, new BlockHitResult(point, face, position, false));
    }

    private void entranceDiagnostic(String stage) {
        String client = "客户端chunk=" + minecraft.level.getChunkSource().hasChunk(FLOOR.getX() >> 4, FLOOR.getZ() >> 4)
                + "，支撑=" + minecraft.level.getBlockState(FLOOR) + "，入口=" + minecraft.level.getBlockState(ENTRANCE)
                + "，主手=" + minecraft.player.getMainHandItem() + "，位置=" + minecraft.player.position();
        String server = read(player -> "服务端支撑=" + player.server.overworld().getBlockState(FLOOR)
                + "，入口=" + player.server.overworld().getBlockState(ENTRANCE) + "，主手=" + player.getMainHandItem()
                + "，位置=" + player.position() + "，传送确认=" + acknowledged(player));
        report.add("INFO 天道副本与画卷 " + stage + " " + client + "；" + server);
    }

    private List<EditBox> inputs() {
        return minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
    }

    private void pressSet() {
        Button button = minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(candidate -> candidate.getMessage().equals(Component.translatable("screen.yvanchuxiuzhen.dungeon.set"))).findFirst().orElseThrow();
        minecraft.screen.mouseClicked(button.getX() + 2, button.getY() + 2, 0);
        minecraft.screen.mouseReleased(button.getX() + 2, button.getY() + 2, 0);
    }

    private void pressEnter() {
        DungeonScreen screen = (DungeonScreen) minecraft.screen;
        screen.mouseClicked(screen.getGuiLeft() + 175, screen.getGuiTop() + 44, 0);
        screen.mouseReleased(screen.getGuiLeft() + 175, screen.getGuiTop() + 44, 0);
    }

    private void clickSlot(int slot, int button) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, button, ClickType.PICKUP, minecraft.player);
    }

    private void checkEntranceCapability(ServerLevel level) {
        IItemHandler unsided = level.getCapability(Capabilities.ItemHandler.BLOCK, ENTRANCE, null);
        expectTrue("入口无方向能力保留两槽", unsided != null && unsided.getSlots() == 2);
        for (Direction side : Direction.values()) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, ENTRANCE, side);
            expectTrue("入口六侧可放不可取" + side, handler != null && handler.getSlots() == 2
                    && handler.insertItem(0, new ItemStack(Items.IRON_INGOT), true).isEmpty());
        }
        block(level, ENTRANCE).setItem(0, new ItemStack(Items.IRON_INGOT));
        for (Direction side : Direction.values()) expectTrue("入口六侧提取被拒" + side,
                level.getCapability(Capabilities.ItemHandler.BLOCK, ENTRANCE, side).extractItem(0, 1, true).isEmpty());
        expectTrue("入口无方向能力按原生规则可取", unsided != null && unsided.extractItem(0, 1, true).getCount() == 1);
        block(level, ENTRANCE).setItem(0, ItemStack.EMPTY);
    }

    private void checkClosedSides(ServerLevel level, BlockPos pos, int slots) {
        IItemHandler unsided = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        expectTrue("无方向库存保留" + pos, unsided != null && unsided.getSlots() == slots);
        for (Direction side : Direction.values()) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
            expectTrue("六侧禁止放取" + pos + side, handler != null && handler.getSlots() == slots
                    && handler.insertItem(0, new ItemStack(Items.STONE), true).getCount() == 1
                    && handler.extractItem(0, 1, true).isEmpty());
        }
    }

    private static void platform(ServerLevel level, Vec3 stand, int radius) {
        BlockPos feet = BlockPos.containing(stand);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(feet.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
                for (int y = 0; y <= 4; y++) level.setBlock(feet.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void move(ServerPlayer player, Vec3 position) {
        move(player, position, 0);
    }

    private static void move(ServerPlayer player, Vec3 position, float yaw) {
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        player.connection.teleport(position.x, position.y, position.z, yaw, 15);
    }

    private static DungeonBlockEntity block(ServerLevel level, BlockPos position) {
        if (level.getBlockEntity(position) instanceof DungeonBlockEntity block) return block;
        throw new IllegalStateException("缺少副本方块实体 " + level.dimension().location() + " " + position);
    }

    private static boolean isBlock(ServerLevel level, BlockPos position, String id) {
        return level.getBlockState(position).is(DungeonPlayers.block(id));
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    private static BlockPos position(CompoundTag tag, String prefix) {
        return BlockPos.containing(tag.getDouble(prefix + "X"), tag.getDouble(prefix + "Y"), tag.getDouble(prefix + "Z"));
    }

    private static AABB area(DungeonBlockEntity controller) {
        CompoundTag data = controller.getPersistentData();
        Vec3 center = Vec3.atLowerCornerOf(controller.getBlockPos()).add(data.getInt("fubenzhongxinX"), data.getInt("fubenzhongxinY"), data.getInt("fubenzhongxinZ"));
        return new AABB(center, center).inflate(data.getInt("fubenbianchang") / 2.0);
    }

    private List<Entity> enemies(ServerLevel level) {
        return level.getEntitiesOfClass(Entity.class, new AABB(controllerPosition).inflate(128), entity -> entity.getPersistentData().getBoolean("fubenentity")
                && position(entity.getPersistentData(), "fubenzuobiao").equals(controllerPosition));
    }

    private static int leather(ServerPlayer player) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item("cailiao_001").getItem())
                && stack.getHoverName().getString().equals("特殊皮革")).mapToInt(ItemStack::getCount).sum();
    }

    private void cleanup(ServerPlayer player) {
        if (!initialized) return;
        player.closeContainer();
        DungeonController.clearDelayedReturns();
        ServerLevel paintingLevel = player.server.getLevel(DungeonPlayers.PAINTING);
        if (controllerPosition != null && paintingLevel != null) {
            enemies(paintingLevel).forEach(Entity::discard);
            if (paintingLevel.getBlockEntity(controllerPosition) instanceof DungeonBlockEntity controller) {
                controller.getPersistentData().putBoolean("fubenstate", false);
                controller.getPersistentData().putBoolean("fubenclose", false);
                controller.getPersistentData().putBoolean("fubenopen", false);
            }
        }
        for (BlockPos pos : List.of(ENTRANCE, SAMPLE, MARKER, PAINTING, SUPPORT)) {
            if (player.server.overworld().getBlockEntity(pos) instanceof DungeonBlockEntity block) block.clearContent();
            player.server.overworld().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        player.setData(DungeonData.PLAYER, new CompoundTag());
        player.setData(DungeonData.PAINTING_TRANSIT, ItemStack.EMPTY);
        player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setData(DungeonData.PLAYER, originalDungeon.copy());
        player.setData(DungeonData.PAINTING_TRANSIT, originalTransit.copy());
        for (String key : List.of("openfubengui", "fubenfanhui", "gamemoshi")) {
            if (originalPersistent.contains(key)) player.getPersistentData().put(key, originalPersistent.get(key).copy());
            else player.getPersistentData().remove(key);
        }
        Relations.setTemporaryTeam(player, originalTeam);
        Relations.setEntityTemporaryTeam(player, originalEntityTeam);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot).copy());
        player.getInventory().selected = originalSelected;
        player.inventoryMenu.broadcastChanges();
        player.setGameMode(originalMode);
        player.setInvulnerable(originalInvulnerable);
        player.removeEffect(MobEffects.SLOW_FALLING);
        if (originalSlowFalling != null) player.addEffect(new MobEffectInstance(originalSlowFalling));
        player.getAbilities().flying = originalFlying;
        player.onUpdateAbilities();
        player.setShiftKeyDown(originalShift);
        player.server.setDifficulty(originalDifficulty, true);
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = originalSelected;
            minecraft.options.keyShift.setDown(originalKeyShift);
        });
        initialized = false;
    }
}
