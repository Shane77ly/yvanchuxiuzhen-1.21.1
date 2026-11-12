package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryInventory;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.client.AccessoryScreen;
import net.yvanchuxiuzhen.system.equipment.client.JewelryTableScreen;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableBlockEntity;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableMenu;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.skill.SkillFlight;
import net.yvanchuxiuzhen.system.skill.api.FlightEnchantmentEvent;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.DantianMenu;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/** 饰品生产、佩戴与制作台的自然菜单、库存保存和本人同步，以及饰品飞行附魔的实机检查。 */
final class AccessoryCheck extends SystemCheck {
    private static final String CONTRIBUTION = "smoke:accessories";
    private static final CultivationValue[] VALUES = {CultivationValue.POWER, CultivationValue.SOUL, CultivationValue.POWER_DISPLAY, CultivationValue.SOUL_DISPLAY};
    private static final String[] TIZHI = {"flyxiaohaotick", "lvekongspeed", "superjet"};
    private final double[] oldValues = new double[VALUES.length];
    private final double[] oldTizhi = new double[TIZHI.length];
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> products = new ArrayList<>();
    private final Screenshots screenshots;
    private AccessoryInventory original;
    private SkillState skill;
    private GameType gameMode;
    private int page;
    private int entered = -1;
    private boolean gliding;
    private BlockPos chest;
    private BlockPos jewelryPos;
    private BlockState jewelryState;
    private long firstJewelryCraft;

    AccessoryCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("accessories", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 10, this::prepare);
            case 1 -> action(step, local, 10, p -> key());
            case 2 -> action(step, local, 10, p -> {
                expectTrue("K键经网络打开默认属性页与36格背包", p.containerMenu instanceof PlayerPanelMenu
                        && minecraft.screen instanceof AttributeScreen && p.containerMenu.slots.size() == 36 && p.getData(PanelNavigation.PAGE) == 1);
                minecraft.execute(() -> screenshots.take("panel-attributes.png"));
                click("饰品装备");
            });
            case 3 -> action(step, local, 10, p -> {
                AccessoryMenu menu = (AccessoryMenu) p.containerMenu;
                expectTrue("饰品页40槽与腰手标签错位保留，客户端真实界面已打开", minecraft.screen instanceof AccessoryScreen && menu.slots.size() == 40
                        && menu.slots.get(2).mayPlace(products.get(2)) && !menu.slots.get(2).mayPlace(products.get(3))
                        && menu.slots.get(3).mayPlace(products.get(3)) && !menu.slots.get(3).mayPlace(products.get(2)));
                minecraft.execute(() -> {
                    for (int slot = 4; slot < 8; slot++) minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
                });
            });
            case 4 -> action(step, local, 10, p -> {
                expectTrue("实际Shift搬入四个单件饰品，关闭前持久槽与飞行读取保持原值", p.containerMenu.slots.get(0).getItem().getCount() == 1
                        && p.containerMenu.slots.get(2).getItem().is(products.get(2).getItem()) && p.containerMenu.slots.get(3).getItem().is(products.get(3).getItem())
                        && Equipment.accessories(p).items().stream().allMatch(ItemStack::isEmpty)
                        && NeoForge.EVENT_BUS.post(new FlightEnchantmentEvent(p)).levels() == 0);
                minecraft.execute(() -> {
                    screenshots.take("equipment-accessories.png");
                    minecraft.player.closeContainer();
                });
            });
            case 5 -> action(step, local, 10, p -> {
                AccessoryInventory saved = Equipment.accessories(p);
                RegistryAccess registries = p.registryAccess();
                var ops = RegistryOps.create(NbtOps.INSTANCE, registries);
                AccessoryInventory restored = AccessoryInventory.CODEC.parse(ops, AccessoryInventory.CODEC.encodeStart(ops, saved).getOrThrow()).getOrThrow();
                WeaponStatEvent root = NeoForge.EVENT_BUS.post(new WeaponStatEvent(saved.necklace(), "mana_Att"));
                expectTrue("关闭保存单件饰品和腰手字段，Codec与本人同步完整，根法攻17.5可被消费", saved.head().getCount() == 1
                        && saved.belt().is(products.get(2).getItem()) && saved.hand().is(products.get(3).getItem())
                        && ItemStack.matches(saved.head(), restored.head()) && ItemStack.matches(saved.head(), Equipment.accessories(minecraft.player).head()) && root.total() == 17.5);
                expectTrue("饰品按槽计一次，四甲附魔和饰品冷却共同提交", near(PlayerAttributes.value(p, Stat.HEALTH_MAX), PlayerAttributes.baseValue(p, Stat.HEALTH_MAX) + 5.5)
                        && near(PlayerAttributes.value(p, Stat.POWER_MAX), PlayerAttributes.baseValue(p, Stat.POWER_MAX) + 6.25)
                        && near(PlayerAttributes.value(p, Stat.SOUL_DEF), PlayerAttributes.baseValue(p, Stat.SOUL_DEF) - 2.5)
                        && near(PlayerAttributes.value(p, Stat.HEALTH_REC), PlayerAttributes.baseValue(p, Stat.HEALTH_REC) + 2.25)
                        && near(PlayerAttributes.value(p, Stat.POWER_REC), PlayerAttributes.baseValue(p, Stat.POWER_REC) + 3)
                        && near(PlayerAttributes.value(p, Stat.ATTACK_DEF), PlayerAttributes.baseValue(p, Stat.ATTACK_DEF) + 4)
                        && near(PlayerAttributes.value(p, Stat.MANA_DEF), PlayerAttributes.baseValue(p, Stat.MANA_DEF) + 5)
                        && near(PlayerAttributes.value(p, Stat.COOLDOWN_REDUCTION), .06));
                p.setGameMode(GameType.SURVIVAL);
                Cultivation.set(p, CultivationValue.POWER, 100);
                Cultivation.setTizhiDouble(p, "flyxiaohaotick", 19);
                Skills.setFlying(p, true);
                SkillFlight.tick(p);
                expectTrue("已保存四槽合计飞行7级，单次12灵力扣费降为7.8", NeoForge.EVENT_BUS.post(new FlightEnchantmentEvent(p)).levels() == 7
                        && near(Cultivation.get(p, CultivationValue.POWER), 92.2));
                Skills.setFlying(p, false);
                p.setGameMode(gameMode);
                chest = BlockPos.containing(p.getX(), p.getY() + 1, p.getZ());
                p.serverLevel().setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
                Container container = (Container) p.serverLevel().getBlockEntity(chest);
                container.setItem(0, new ItemStack(Items.DIAMOND));
                container.setItem(4, new ItemStack(Items.EMERALD));
                key();
            });
            case 6 -> action(step, local, 10, p -> {
                Container container = (Container) p.serverLevel().getBlockEntity(chest);
                expectTrue("K记住饰品页，首刻装入单件饰品且绑定头顶容器的前四槽", p.containerMenu instanceof AccessoryMenu
                        && minecraft.screen instanceof AccessoryScreen && p.getData(PanelNavigation.PAGE) == 3
                        && p.containerMenu.slots.get(0).getItem().getCount() == 1 && Equipment.accessories(p).head().getCount() == 1
                        && container.getItem(0).is(products.getFirst().getItem()) && container.getItem(4).is(Items.EMERALD));
                click("属性显示");
            });
            case 7 -> action(step, local, 10, p -> {
                clearChest(p);
                expectTrue("返回属性页同时保存饰品，丹田按钮可经当前菜单进入", p.containerMenu instanceof PlayerPanelMenu && Equipment.accessories(p).head().getCount() == 1);
                click("丹田");
            });
            case 8 -> action(step, local, 10, p -> {
                expectTrue("丹田从属性页打开", p.containerMenu instanceof DantianMenu);
                minecraft.execute(() -> PacketDistributor.sendToServer(new XiulianButtonPayload(12)));
            });
            case 9 -> action(step, local, 10, p -> {
                expectTrue("丹田返回按钮恢复已记住的属性页", p.containerMenu instanceof PlayerPanelMenu && minecraft.screen instanceof AttributeScreen);
                click("饰品装备");
            });
            case 10 -> action(step, local, 5, p -> {
                p.closeContainer();
                p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                p.setGameMode(GameType.SURVIVAL);
                p.kill();
            });
            case 11 -> respawned(local);
            case 12 -> {
                if (!inWorld(local)) yield false;
                onServer(p -> expectTrue("死亡复制保留四槽与当前分页", p.getData(PanelNavigation.PAGE) == 3 && Equipment.accessories(p).head().getCount() == 1
                        && ItemStack.isSameItemSameComponents(Equipment.accessories(p).necklace(), products.get(1))));
                leaveWorld();
                yield true;
            }
            case 13 -> reopened(local);
            case 14 -> {
                if (minecraft.screen instanceof BackupConfirmScreen) {
                    click(Component.translatable("selectWorld.backupJoinSkipButton").getString());
                }
                yield inWorld(local);
            }
            case 15 -> action(step, local, 10, p -> {
                expectTrue("退出重登后四槽组件、未知动态键与分页仍在", p.getData(PanelNavigation.PAGE) == 3
                        && Equipment.properties(Equipment.accessories(p).necklace()).text("custom_key").equals("保留")
                        && ItemStack.isSameItemSameComponents(Equipment.accessories(p).hand(), products.get(3)));
                key();
            });
            case 16 -> action(step, local, 10, p -> {
                expectTrue("重登后K仍打开饰品页，已保存四槽实际装入客户端", p.containerMenu instanceof AccessoryMenu && minecraft.screen instanceof AccessoryScreen
                        && ItemStack.isSameItemSameComponents(minecraft.player.containerMenu.slots.get(2).getItem(), products.get(2)));
                p.closeContainer();
                jewelryPos = p.blockPosition().offset(2, 1, 0);
                jewelryState = p.serverLevel().getBlockState(jewelryPos);
                p.serverLevel().setBlockAndUpdate(jewelryPos,
                        BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shipintai")).defaultBlockState());
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                p.getInventory().setItem(9, jewelryItem("cailiao_088", 2));
                p.getInventory().setItem(10, jewelryItem("cailiao_191", 2));
                p.getInventory().setItem(11, jewelryItem("cailiao_161", 2));
                p.getInventory().setItem(12, new ItemStack(Items.EMERALD));
                p.inventoryMenu.broadcastChanges();
            });
            case 17 -> action(step, local, 10, p -> openJewelry());
            case 18 -> action(step, local, 10, p -> {
                expectTrue("真实方块右键经网络打开饰品制作台与头簪类别", p.containerMenu instanceof JewelryTableMenu
                        && minecraft.screen instanceof JewelryTableScreen && p.containerMenu.slots.size() == 40
                        && ((JewelryTableMenu) p.containerMenu).category().equals("头簪"));
                minecraft.execute(() -> fillJewelry(true));
            });
            case 19 -> action(step, local, 10, p -> {
                JewelryTableBlockEntity table = (JewelryTableBlockEntity) p.serverLevel().getBlockEntity(jewelryPos);
                expectTrue("真实槽点击每个输入只放一件并保留背包余件，结果槽拒绝放入", table.getItem(0).getCount() == 1
                        && table.getItem(1).getCount() == 1 && table.getItem(2).getCount() == 1 && table.getItem(3).isEmpty()
                        && p.containerMenu.slots.get(0).getMaxStackSize() == 1 && !p.containerMenu.slots.get(3).mayPlace(jewelryItem("cailiao_088", 1))
                        && p.getInventory().getItem(9).getCount() == 1 && p.getInventory().getItem(10).getCount() == 1
                        && p.getInventory().getItem(11).getCount() == 1 && p.containerMenu.getCarried().isEmpty());
                firstJewelryCraft = p.serverLevel().getGameTime();
                minecraft.execute(() -> pressJewelry(0));
            });
            case 20 -> action(step, local, 10, p -> {
                JewelryTableBlockEntity table = (JewelryTableBlockEntity) p.serverLevel().getBlockEntity(jewelryPos);
                ItemStack result = table.getItem(3);
                EquipmentProperties properties = Equipment.properties(result);
                ItemStack clientResult = minecraft.player.containerMenu.slots.get(3).getItem();
                expectTrue("屏幕合成按钮消费三个输入，头簪根字段小数与冷却附魔完整同步", result.is(jewelryItem("headornaments_01", 1).getItem())
                        && table.getItem(0).isEmpty() && table.getItem(1).isEmpty() && table.getItem(2).isEmpty()
                        && properties.number("item_level_num") == 4 && properties.text("item_level").equals("黄阶四级")
                        && properties.fields().get("item_level_num").kind() == EquipmentProperties.Kind.DOUBLE
                        && properties.text("fabaotype").equals("头饰") && properties.text("tuzhiname").equals(result.getDisplayName().getString())
                        && properties.text("fabaoid").equals("yvanchuxiuzhen:headornaments_01")
                        && Enchants.level(p, result, Enchants.key("colddown")) == 1 && ItemStack.matches(result, clientResult)
                        && Enchants.level(minecraft.player, clientResult, Enchants.key("colddown")) == 1
                        && minecraft.player.containerMenu.slots.get(0).getItem().isEmpty()
                        && minecraft.player.containerMenu.slots.get(1).getItem().isEmpty()
                        && minecraft.player.containerMenu.slots.get(2).getItem().isEmpty());
                p.getInventory().setItem(9, jewelryItem("cailiao_202", 1));
                p.getInventory().setItem(10, jewelryItem("cailiao_192", 1));
                p.getInventory().setItem(11, jewelryItem("cailiao_162", 1));
                p.containerMenu.broadcastChanges();
            });
            case 21 -> action(step, local, 10, p -> minecraft.execute(() -> {
                fillJewelry(false);
                pressJewelry(0);
            }));
            case 22 -> action(step, local, 10, p -> {
                JewelryTableBlockEntity table = (JewelryTableBlockEntity) p.serverLevel().getBlockEntity(jewelryPos);
                ItemStack result = table.getItem(3);
                expectTrue("四秒内可再次合成并覆盖未取出的头簪，二档成品与消费同步", p.serverLevel().getGameTime() - firstJewelryCraft < 80
                        && result.is(jewelryItem("headornaments_02", 1).getItem()) && result.getCount() == 1
                        && Equipment.properties(result).number("item_level_num") == 9 && Enchants.level(p, result, Enchants.key("colddown")) == 2
                        && table.getItem(0).isEmpty() && table.getItem(1).isEmpty() && table.getItem(2).isEmpty()
                        && ItemStack.matches(result, minecraft.player.containerMenu.slots.get(3).getItem()));
                minecraft.execute(() -> pressJewelry(1));
            });
            case 23 -> action(step, local, 10, p -> {
                expectTrue("下翻按钮由头簪进入项坠且类别同步到客户端", ((JewelryTableMenu) p.containerMenu).category().equals("项坠")
                        && ((JewelryTableMenu) minecraft.player.containerMenu).category().equals("项坠"));
                minecraft.execute(() -> {
                    for (int i = 0; i < 3; i++) pressJewelry(1);
                    pressJewelry(2);
                });
            });
            case 24 -> action(step, local, 10, p -> {
                expectTrue("下翻经过手镯腰佩回到头簪，上翻回到腰佩且双端一致", ((JewelryTableMenu) p.containerMenu).category().equals("腰佩")
                        && ((JewelryTableMenu) minecraft.player.containerMenu).category().equals("腰佩"));
                minecraft.execute(() -> {
                    int id = minecraft.player.containerMenu.containerId;
                    minecraft.gameMode.handleInventoryMouseClick(id, 7, 0, ClickType.PICKUP, minecraft.player);
                    minecraft.gameMode.handleInventoryMouseClick(id, 0, 0, ClickType.PICKUP, minecraft.player);
                    minecraft.player.closeContainer();
                });
            });
            case 25 -> action(step, local, 10, p -> openJewelry());
            case 26 -> action(step, local, 10, p -> {
                JewelryTableBlockEntity table = (JewelryTableBlockEntity) p.serverLevel().getBlockEntity(jewelryPos);
                expectTrue("关闭后自然右键重开保留未消费输入、覆盖后成品和类别，客户端库存一致", p.containerMenu instanceof JewelryTableMenu
                        && minecraft.screen instanceof JewelryTableScreen && table.category().equals("腰佩")
                        && table.getItem(0).is(Items.EMERALD) && table.getItem(0).getCount() == 1
                        && table.getItem(3).is(jewelryItem("headornaments_02", 1).getItem())
                        && ItemStack.matches(table.getItem(0), minecraft.player.containerMenu.slots.get(0).getItem())
                        && ItemStack.matches(table.getItem(3), minecraft.player.containerMenu.slots.get(3).getItem())
                        && ((JewelryTableMenu) minecraft.player.containerMenu).category().equals("腰佩"));
                cleanup(p);
            });
            default -> {
                finish();
                yield true;
            }
        };
    }

    private boolean action(int step, int local, int wait, Consumer<ServerPlayer> action) {
        if (entered != step) {
            entered = step;
            onServer(action);
        }
        return local >= wait;
    }

    private void prepare(ServerPlayer p) {
        p.closeContainer();
        original = Equipment.accessories(p);
        page = p.getData(PanelNavigation.PAGE);
        skill = Skills.state(p);
        gameMode = p.gameMode.getGameModeForPlayer();
        for (int i = 0; i < VALUES.length; i++) oldValues[i] = Cultivation.get(p, VALUES[i]);
        for (int i = 0; i < TIZHI.length; i++) oldTizhi[i] = Cultivation.tizhiDouble(p, TIZHI[i]);
        gliding = Cultivation.tizhiBoolean(p, "lvekong");
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
        p.getInventory().clearContent();
        p.setData(EquipmentData.ACCESSORIES, new AccessoryData());
        p.setData(PanelNavigation.PAGE, 0);
        p.setData(SkillData.STATE, SkillState.EMPTY);
        PlayerAttributes.setContribution(p, CONTRIBUTION, new Contribution(Layer.BASE, Map.of(Stat.POWER_MAX, 1000.0)));
        for (String key : TIZHI) Cultivation.setTizhiDouble(p, key, 0);
        Cultivation.setTizhiBoolean(p, "lvekong", false);
        products.add(product(p, "YVANCHUXIUZHEN:HEADORNAMENTS_01 无 无 头饰 上品 2.8 health_Max double 5.5 yvanchuxiuzhen flying 2 yvanchuxiuzhen colddown 3"));
        products.add(product(p, "yvanchuxiuzhen:necklaceornaments_01 项坠 无 项坠 中品 3 power_Max double 6.25 mana_Att double 17.5 custom_key char 保留"));
        products.add(product(p, "yvanchuxiuzhen:handornaments_01 手饰 无 手饰 下品 1 health_Rec double .25 yvanchuxiuzhen flying 4 yvanchuxiuzhen colddown 1"));
        products.add(product(p, "yvanchuxiuzhen:beltornaments_01 腰佩 无 腰饰 下品 1 soul_Def double -2.5 yvanchuxiuzhen flying 1"));
        EquipmentProperties overrides = Equipment.properties(product(p, "minecraft:stick 无 无 头饰 旧 4.8 item_level str 新 item_level_num double 8.75 custom_key char 保留"));
        expectTrue("权限0的真实/shipin产出上限1的饰品并保留三组类型参数、同名覆盖与未知字段", products.stream().allMatch(stack -> stack.getCount() == 1 && stack.getMaxStackSize() == 1)
                && !products.getFirst().has(DataComponents.CUSTOM_NAME)
                && Equipment.properties(products.getFirst()).number("item_level_num") == 2 && overrides.text("item_level").equals("新")
                && overrides.number("item_level_num") == 8.75 && overrides.text("custom_key").equals("保留"));
        for (int i = 0; i < 4; i++) p.getInventory().setItem(9 + i, products.get(i).copy());
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        String[] enchants = {"dawn", "powerup", "attdef", "manadef", "colddown"};
        int[] levels = {2, 3, 4, 5, 2};
        for (int i = 0; i < enchants.length; i++) helmet.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchants.key(enchants[i])), levels[i]);
        p.setItemSlot(EquipmentSlot.HEAD, helmet);
        p.inventoryMenu.broadcastChanges();
    }

    private ItemStack product(ServerPlayer p, String arguments) {
        Vec3 pos = p.position().add(4, 0, 0);
        p.getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack().withPosition(pos).withPermission(0).withSuppressedOutput(), "shipin " + arguments);
        ItemEntity drop = p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.add(1, 1, 1)).inflate(1)).getFirst();
        ItemStack stack = drop.getItem().copy();
        drop.discard();
        return stack;
    }

    private void key() {
        minecraft.execute(() -> {
            var key = Arrays.stream(minecraft.options.keyMappings).filter(mapping -> mapping.getName().equals("key.yvanchuxiuzhen.guicaidan")).findFirst().orElseThrow();
            key.setDown(true);
            key.setDown(false);
        });
    }

    private void click(String name) {
        minecraft.execute(() -> minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow().onPress());
    }

    private static ItemStack jewelryItem(String name, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name)), count);
    }

    private void openJewelry() {
        minecraft.execute(() -> minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(jewelryPos), Direction.UP, jewelryPos, false)));
    }

    /** 通过玩家背包槽搬运三种材料，并在持有余件时实际点击结果槽。仅在客户端线程调用。 */
    private void fillJewelry(boolean checkResult) {
        int id = minecraft.player.containerMenu.containerId;
        for (int slot = 0; slot < 3; slot++) {
            minecraft.gameMode.handleInventoryMouseClick(id, 4 + slot, 0, ClickType.PICKUP, minecraft.player);
            minecraft.gameMode.handleInventoryMouseClick(id, slot, 0, ClickType.PICKUP, minecraft.player);
            if (slot == 0 && checkResult) {
                minecraft.gameMode.handleInventoryMouseClick(id, 3, 0, ClickType.PICKUP, minecraft.player);
                expectTrue("客户端真实点击结果槽拒绝材料且保留鼠标余件", minecraft.player.containerMenu.slots.get(3).getItem().isEmpty()
                        && minecraft.player.containerMenu.getCarried().is(jewelryItem("cailiao_088", 1).getItem())
                        && minecraft.player.containerMenu.getCarried().getCount() == 1);
            }
            minecraft.gameMode.handleInventoryMouseClick(id, 4 + slot, 0, ClickType.PICKUP, minecraft.player);
        }
    }

    /** 按来源按钮坐标点击真实屏幕，经过控件发送按钮消息。仅在客户端线程调用。 */
    private void pressJewelry(int button) {
        JewelryTableScreen screen = (JewelryTableScreen) minecraft.screen;
        int x = screen.getGuiLeft() + switch (button) {
            case 0 -> 105;
            case 1 -> 64;
            case 2 -> 110;
            default -> throw new IllegalArgumentException("饰品台检查按钮：" + button);
        };
        int y = screen.getGuiTop() + (button == 0 ? 74 : 57);
        screen.mouseClicked(x, y, 0);
        screen.mouseReleased(x, y, 0);
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-7;
    }

    private void clearChest(ServerPlayer p) {
        ((Container) p.serverLevel().getBlockEntity(chest)).clearContent();
        p.serverLevel().removeBlock(chest, false);
    }

    private void cleanup(ServerPlayer p) {
        p.closeContainer();
        ((Container) p.serverLevel().getBlockEntity(jewelryPos)).clearContent();
        p.serverLevel().setBlockAndUpdate(jewelryPos, jewelryState);
        p.setGameMode(gameMode);
        p.setData(EquipmentData.ACCESSORIES, new AccessoryData(original));
        p.setData(PanelNavigation.PAGE, page);
        p.setData(SkillData.STATE, skill);
        p.getInventory().clearContent();
        for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        for (int i = 0; i < VALUES.length; i++) Cultivation.set(p, VALUES[i], oldValues[i]);
        for (int i = 0; i < TIZHI.length; i++) Cultivation.setTizhiDouble(p, TIZHI[i], oldTizhi[i]);
        Cultivation.setTizhiBoolean(p, "lvekong", gliding);
        PlayerAttributes.clearContribution(p, CONTRIBUTION);
        p.inventoryMenu.broadcastChanges();
    }
}
