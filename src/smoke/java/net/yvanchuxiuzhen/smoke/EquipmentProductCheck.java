package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.cooking.data.CookingAttachments;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentRefinement;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.liandan.client.AlchemyScreen;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;
import net.yvanchuxiuzhen.system.liandan.client.MedicineSeparationScreen;
import net.yvanchuxiuzhen.system.liandan.content.MedicineSeparationBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.MedicineSeparationMenu;
import net.yvanchuxiuzhen.system.liandan.client.PharmacologyScreen;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyMenu;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.lianqi.client.TripodScreen;
import net.yvanchuxiuzhen.system.lianqi.client.BlueprintTableScreen;
import net.yvanchuxiuzhen.system.lianqi.client.RepairTableScreen;
import net.yvanchuxiuzhen.system.lianqi.client.SewingBasketScreen;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableMenu;
import net.yvanchuxiuzhen.system.lianqi.content.PoisonCauldronBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableMenu;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketMenu;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.TripodMenu;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.minecraft.network.chat.Component;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.skill.SkillGuard;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Preferences;
import net.yvanchuxiuzhen.system.storage.data.StorageData;
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;
import net.yvanchuxiuzhen.system.yihuo.api.Yihuo;

/** 核对成品、丹炉及炼器工作台的自然交互、库存消费、实例保存和双端同步。 */
final class EquipmentProductCheck extends ActionCheck {
    private static final String WEAPON = "minecraft:iron_sword 成品长剑 炼器说明 武器 上品 8.9 8.75 17.25 -3.4 -0.1 -0.2 0.3 6.9 4"
            + " minecraft unbreaking 2 minecraft mending 1 minecraft sharpness 5";
    private static final String ARMOR = "minecraft:leather_helmet 成品头盔 护甲说明 头盔 中品 4.6 10.9 4.9 5.9 3.7 -0.1 -0.2 1.9 2.8 -3.9 2 3"
            + " minecraft unbreaking 2 minecraft binding_curse 1 minecraft vanishing_curse 1";
    private List<ItemStack> inventory;
    private BlockPos chestPos;
    private ItemStack weapon;
    private ItemStack armor;
    private double oldGuard;
    private BlockPos alchemyPos;
    private final List<BlockState> alchemyBlocks = new ArrayList<>();
    private ProfessionProgress alchemyProfession;
    private MedicineInstance medicine;
    private int selected;
    private GameType gameMode;
    private final CompoundTag food = new CompoundTag();
    private double fastingTimer;
    private boolean hadFastingTimer;
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private boolean shiftKey;
    private int brewedCount;
    private BlockPos bottlePos;
    private BlockState bottleBlock;
    private ItemStack bottledMedicine;
    private int equippedTick;
    private Cow feedingTarget;
    private BlockPos separationPos;
    private BlockState separationBlock;
    private BlockState separationBase;
    private ItemStack separationMedicine;
    private BlockPos pharmacologyPos;
    private final List<BlockState> pharmacologyBlocks = new ArrayList<>();
    private BlockPos tripodPos;
    private final List<BlockState> tripodBlocks = new ArrayList<>();
    private ProfessionProgress refiningProfession;
    private MaterialWarehouse warehouse;
    private ItemStack refinedProduct;
    private BlockPos repairPos;
    private BlockPos sewingPos;
    private BlockPos blueprintPos;
    private BlockPos poisonPos;
    private final Map<BlockPos, BlockState> workbenchBlocks = new LinkedHashMap<>();
    private final Map<BlockPos, CompoundTag> workbenchEntities = new LinkedHashMap<>();
    private boolean sewingLastPage;
    private boolean poisonRejected;

    EquipmentProductCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("equipmentproduct", report, levelId);
        script(
                new Action(200, p -> {
                    inventory = new ArrayList<>();
                    gameMode = p.gameMode.getGameModeForPlayer();
                    p.getFoodData().addAdditionalSaveData(food);
                    hadFastingTimer = p.hasData(CookingAttachments.FASTING_TICKS);
                    fastingTimer = p.getData(CookingAttachments.FASTING_TICKS);
                    effects.addAll(p.getActiveEffects().stream().map(MobEffectInstance::new).toList());
                    shiftKey = minecraft.options.keyShift.isDown();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.getInventory().clearContent();
                    oldGuard = p.getData(SkillData.GUARD_COOLDOWN);
                    chestPos = p.blockPosition().offset(4, 0, 0);
                    run(p, WEAPON, 4);
                    boolean absent = drops(p).isEmpty();
                    p.serverLevel().setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
                    Container container = (Container) p.serverLevel().getBlockEntity(chestPos);
                    container.setItem(0, new ItemStack(Items.DIAMOND, 3));
                    run(p, WEAPON, 4);
                    boolean occupied = drops(p).isEmpty() && container.getItem(0).is(Items.DIAMOND) && container.getItem(0).getCount() == 3;
                    container.setItem(0, ItemStack.EMPTY);
                    run(p, WEAPON, 0);
                    expectTrue("无容器不产出，已有物品不覆盖，权限0不能生成", absent && occupied && drops(p).isEmpty() && container.getItem(0).isEmpty());
                    run(p, WEAPON, 1);
                    weapon = takeDrop(p);
                    EquipmentProduct product = Equipment.product(weapon).orElseThrow();
                    EquipmentStats stats = weapon.get(EquipmentData.STATS.get());
                    var enchantments = p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    expectTrue("空槽生成并清槽，武器取整、倍率和大于三组的附魔分支保留", container.getItem(0).isEmpty()
                            && product.level() == 8 && product.experience() == 890 && product.materials().physicalAttack() == 875
                            && product.materials().soulAttack() == -340 && product.weight() == 6
                            && stats.physicalAttack() == 8 && stats.spellAttack() == 17 && stats.soulAttack() == -3
                            && stats.attackSpeed() == -.1 && product.enchantmentCount() == 4
                            && product.enchantments().stream().map(EquipmentProduct.Enchantment::slot).toList().equals(List.of(1, 0))
                            && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SHARPNESS), weapon) == 0
                            && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.MENDING), weapon) == 1);
                    run(p, ARMOR, 1);
                    armor = takeDrop(p);
                    ItemStack restored = ItemStack.parse(p.registryAccess(), armor.save(p.registryAccess())).orElseThrow();
                    EquipmentProduct armorProduct = Equipment.product(restored).orElseThrow();
                    expectTrue("防具三组附魔及完整成品数据保存重读，原料在乘算后取整", ItemStack.isSameItemSameComponents(armor, restored)
                            && armorProduct.enchantments().stream().map(EquipmentProduct.Enchantment::slot).toList().equals(List.of(2, 1, 0))
                            && armorProduct.level() == 4 && armorProduct.experience() == 459
                            && restored.get(EquipmentData.STATS.get()).armor() == 3
                            && armorProduct.materials().health() == 218 && armorProduct.materials().armor() == 18
                            && armorProduct.materials().soulDefense() == -390 && armorProduct.materials().moveSpeed() == -200);
                    p.setItemSlot(EquipmentSlot.MAINHAND, weapon);
                    p.setItemSlot(EquipmentSlot.HEAD, restored);
                    p.inventoryMenu.broadcastChanges();
                    equippedTick = p.tickCount;
                }, () -> minecraft.getSingleplayerServer().submit(() -> minecraft.getSingleplayerServer().getPlayerList()
                        .getPlayers().getFirst().tickCount - equippedTick >= 2).join()),
                new Action(6, p -> {
                    expectTrue("成品覆盖默认修饰，生命和物攻各计一次，负移速只在武器生效（生命/护甲/攻速/移速："
                            + p.getMaxHealth() + "/" + p.getAttributeValue(Attributes.ARMOR) + "/"
                            + p.getAttributeValue(Attributes.ATTACK_SPEED) + "/" + p.getAttributeValue(Attributes.MOVEMENT_SPEED) + "）",
                            PlayerAttributes.value(p, Stat.ATTACK) == PlayerAttributes.baseValue(p, Stat.ATTACK) + 8
                            && PlayerAttributes.value(p, Stat.MANA_ATT) == PlayerAttributes.baseValue(p, Stat.MANA_ATT) + 17
                            && PlayerAttributes.value(p, Stat.SOUL_ATT) == PlayerAttributes.baseValue(p, Stat.SOUL_ATT) - 3
                            && p.getAttributeValue(Attributes.ATTACK_DAMAGE) == PlayerAttributes.value(p, Stat.ATTACK)
                            && p.getMaxHealth() == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX) + 10
                            && Math.abs(p.getAttributeValue(Attributes.ARMOR) - 3.7) < 1e-8
                            && Math.abs(p.getAttributeValue(Attributes.ATTACK_SPEED) - 1.26) < 1e-8
                            && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .08) < 1e-8
                            && weapon.getAttributeModifiers().modifiers().stream().noneMatch(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE)));
                    p.setData(SkillData.GUARD_COOLDOWN, 0.0);
                    SkillGuard.press(p);
                    boolean grade = Skills.guardRemaining(p) == 24;
                    WeaponStatEvent rootAttack = new WeaponStatEvent(weapon, "mana_Att");
                    NeoForge.EVENT_BUS.post(rootAttack);
                    ItemStack clientWeapon = minecraft.player.getMainHandItem();
                    var tooltip = clientWeapon.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL);
                    expectTrue("品阶进入格挡且不混用根法攻，组件同步客户端并显示品阶说明", grade && rootAttack.total() == 0
                            && Equipment.product(clientWeapon).orElseThrow().level() == 8
                            && tooltip.stream().anyMatch(line -> line.getString().contains("上品"))
                            && tooltip.stream().anyMatch(line -> line.getString().contains("炼器说明")));
                    p.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }),
                new Action(4, p -> {
                    expectTrue("移除成品后武器和护甲贡献撤除", PlayerAttributes.value(p, Stat.ATTACK) == PlayerAttributes.baseValue(p, Stat.ATTACK)
                            && PlayerAttributes.value(p, Stat.HEALTH_MAX) == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX));
                    alchemyProfession = p.getData(ProfessionAttachments.PROGRESS).get(Profession.ALCHEMY);
                    Professions.setLevel(p, Profession.ALCHEMY, 0);
                    Professions.setExperience(p, Profession.ALCHEMY, 0);
                    selected = p.getInventory().selected;
                    alchemyPos = p.blockPosition().offset(2, 1, 0);
                    for (int below = 0; below < 3; below++) alchemyBlocks.add(p.serverLevel().getBlockState(alchemyPos.below(below)));
                    p.serverLevel().setBlockAndUpdate(alchemyPos, Blocks.AIR.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(alchemyPos.below(), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(alchemyPos.below(2), Blocks.STONE.defaultBlockState());
                    p.getInventory().setItem(4, alchemyItem("alchemy_01", 1));
                    p.getInventory().setItem(5, Yihuo.createDanFire(p.getStringUUID(), 10));
                    p.getInventory().setItem(7, Yihuo.createDanFire(p.getStringUUID(), 20));
                    p.getInventory().setItem(6, ItemStack.EMPTY);
                    p.getInventory().setItem(9, alchemyItem("itemdanyaohulu_04", 2));
                    p.getInventory().setItem(10, alchemyItem("spiritgrass", 2));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, alchemyPos.below())),
                new Action(6, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    AlchemyBlockEntity clientFurnace = minecraft.level.getBlockEntity(alchemyPos) instanceof AlchemyBlockEntity client ? client : null;
                    expectTrue("自然放炉初始化本炉药性，双端同步黄铜炉参数", furnace.herbs().effectCount("yvanchuxiuzhen:spiritgrass") == 4
                            && furnace.alchemyLevel() == 1
                            && clientFurnace != null
                            && clientFurnace.saveWithoutMetadata(p.registryAccess()).equals(furnace.saveWithoutMetadata(p.registryAccess())));
                    furnace.setItem(9, new ItemStack(Items.DIAMOND));
                    useBlock(6, alchemyPos);
                }),
                new Action(6, p -> {
                    expectTrue("真实右键打开双方丹炉菜单与原生界面", p.containerMenu instanceof AlchemyMenu
                            && minecraft.player.containerMenu instanceof AlchemyMenu && minecraft.screen instanceof AlchemyScreen);
                    minecraft.execute(() -> {
                        int id = minecraft.player.containerMenu.containerId;
                        for (int slot = 0; slot < 2; slot++) {
                            minecraft.gameMode.handleInventoryMouseClick(id, 11 + slot, 0, ClickType.PICKUP, minecraft.player);
                            minecraft.gameMode.handleInventoryMouseClick(id, slot, 0, ClickType.PICKUP, minecraft.player);
                        }
                    });
                }),
                new Action(45, p -> minecraft.execute(() -> {
                    screenshots.take("liandan-loaded.png");
                    minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals("开始炼丹"))
                            .map(child -> (Button) child).findFirst().orElseThrow().onPress();
                })),
                new Action(5, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    medicine = Liandan.medicine(furnace.getItem(11));
                    expectTrue("开始按钮各消耗一件、保留旧输出，无热炉次暂停，四药性给出十五炉刻", furnace.brewing()
                            && furnace.getItem(0).getCount() == 1 && furnace.getItem(1).getCount() == 1 && furnace.getItem(9).is(Items.DIAMOND)
                            && (int) furnace.brewTickMax() == 15 && (int) furnace.brewTick() == 0 && medicine.effects().size() == 4
                            && medicine.effects().get(0).level() == 80 && medicine.effects().get(3).level() == -30
                            && ItemStack.matches(furnace.getItem(0), minecraft.player.containerMenu.getSlot(0).getItem()));
                    furnace.getItem(11).set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    AlchemyBlockEntity restored = (AlchemyBlockEntity) BlockEntity.loadStatic(alchemyPos, furnace.getBlockState(),
                            furnace.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("在炼炉次保存重读保留库存、隐藏成品全组件与装置状态",
                            restored.brewing() && restored.brewTickMax() == furnace.brewTickMax()
                                    && restored.brewTick() == furnace.brewTick()
                                    && restored.lianPower() == furnace.lianPower()
                                    && restored.herbs().equals(furnace.herbs())
                                    && ItemStack.matches(restored.getItem(0), furnace.getItem(0))
                                    && ItemStack.matches(restored.getItem(11), furnace.getItem(11)));
                    p.closeContainer();
                    p.serverLevel().removeBlock(alchemyPos.below(), false);
                }),
                new Action(6, p -> useBlock(5, alchemyPos.below(2))),
                new Action(45, p -> useBlock(7, alchemyPos.below())),
                new Action(320, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    AlchemyBlockEntity clientFurnace = (AlchemyBlockEntity) minecraft.level.getBlockEntity(alchemyPos);
                    double fuel = ((FireBlockEntity) p.serverLevel().getBlockEntity(alchemyPos.below())).power();
                    expectTrue("丹火自然供热推进炉次，已耗热量与火块剩余燃料守恒，客户端收到进度 实际燃料=" + fuel + "，进度=" + furnace.brewTick(),
                            furnace.brewTick() > 0 && fuel + furnace.lianPower() + furnace.brewTick() == 30
                                    && clientFurnace.brewTick() > 0);
                    useBlock(6, alchemyPos);
                }),
                new Action(6, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    ItemStack output = furnace.getItem(9);
                    expectTrue("自然成丹覆盖旧输出，04容器转0401并保留全部组件，隐藏槽清空且双端一致", output.is(alchemyItem("itemdanyaohulu_0401", 1).getItem())
                            && Liandan.medicine(output).equals(medicine)
                            && Boolean.TRUE.equals(output.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))
                            && output.getHoverName().getString().equals(medicine.name())
                            && furnace.getItem(11).isEmpty() && !furnace.brewing()
                            && ItemStack.matches(output, minecraft.player.containerMenu.getSlot(9).getItem()));
                    minecraft.execute(() -> {
                        screenshots.take("liandan-finished.png");
                        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 9, 0, ClickType.QUICK_MOVE, minecraft.player);
                    });
                }),
                new Action(4, p -> {
                    expectTrue("Shift取丹清空结果槽且经验只领取一次，并同步玩家", furnace(p).getItem(9).isEmpty()
                        && p.getInventory().items.stream().anyMatch(stack -> stack.is(alchemyItem("itemdanyaohulu_0401", 1).getItem()))
                        && Professions.experience(p, Profession.ALCHEMY) == 3 && Professions.experience(minecraft.player, Profession.ALCHEMY) == 3
                        && furnace(p).pendingExperience() == 0);
                    p.closeContainer();
                    p.setGameMode(GameType.SURVIVAL);
                    p.removeAllEffects();
                    ItemStack result = p.getInventory().items.stream().filter(stack -> stack.is(alchemyItem("itemdanyaohulu_0401", 1).getItem()))
                            .findFirst().orElseThrow();
                    brewedCount = Liandan.medicine(result).doses();
                    p.getInventory().removeItemNoUpdate(p.getInventory().findSlotMatchingItem(result));
                    p.getInventory().setItem(4, result);
                    p.inventoryMenu.broadcastChanges();
                    sneak(false);
                }),
                new Action(6, p -> useMedicine()),
                new Action(4, p -> {
                    ItemStack used = p.getMainHandItem();
                    expectTrue("自然成丹右键扣一颗，喝空换04空盒且双方持物一致", (brewedCount == 1
                            ? used.is(alchemyItem("itemdanyaohulu_04", 1).getItem()) && !Liandan.medicine(used).isPresent()
                            : used.is(alchemyItem("itemdanyaohulu_0401", 1).getItem()) && Liandan.medicine(used).doses() == brewedCount - 1)
                            && ItemStack.matches(used, minecraft.player.getMainHandItem()));
                    p.getInventory().clearContent();
                    ItemStack bottle = alchemyItem("itemdanyaohulu_01", 2);
                    String[] names = {"speed", "resistance", "yvanchuxiuzhen:effect_031", "glowing", "night_vision"};
                    int[] levels = {199, 900, 100, 0, 100};
                    List<MedicineInstance.Effect> bottleEffects = new ArrayList<>(names.length);
                    for (int i = 0; i < names.length; i++) bottleEffects.add(new MedicineInstance.Effect(names[i], levels[i], 201));
                    Liandan.setMedicine(bottle, new MedicineInstance("", "", "", 2, false, bottleEffects));
                    bottle.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    p.getInventory().setItem(4, bottle);
                    p.getFoodData().setFoodLevel(10);
                    p.getFoodData().setSaturation(0);
                    p.setData(CookingAttachments.FASTING_TICKS, 14.0);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useMedicine()),
                new Action(4, p -> {
                    expectTrue("多瓶首次右键只合并，保留组件并返一个同ID空瓶", p.getMainHandItem().getCount() == 1
                            && Liandan.medicine(p.getMainHandItem()).doses() == 4 && !p.hasEffect(MobEffects.MOVEMENT_SPEED)
                            && Boolean.TRUE.equals(p.getMainHandItem().get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))
                            && p.getInventory().items.stream().anyMatch(stack -> stack.is(alchemyItem("itemdanyaohulu_01", 1).getItem())
                                    && !Liandan.medicine(stack).isPresent())
                            && ItemStack.matches(p.getMainHandItem(), minecraft.player.getMainHandItem()));
                    useMedicine();
                }),
                new Action(6, p -> {
                    MobEffectInstance speed = p.getEffect(MobEffects.MOVEMENT_SPEED);
                    MobEffectInstance resistance = p.getEffect(MobEffects.DAMAGE_RESISTANCE);
                    expectTrue("实际服用按整数药效施加、抗性封顶三级，零等级截断，辟谷补饥饿且同步", Liandan.medicine(p.getMainHandItem()).doses() == 3
                            && speed != null && speed.getAmplifier() == 0 && speed.getDuration() > 180 && speed.getDuration() <= 200
                            && resistance != null && resistance.getAmplifier() == 2 && p.hasEffect(fasting())
                            && !p.hasEffect(MobEffects.NIGHT_VISION) && p.getFoodData().getFoodLevel() >= 11
                            && minecraft.player.hasEffect(fasting()) && ItemStack.matches(p.getMainHandItem(), minecraft.player.getMainHandItem()));
                    bottledMedicine = p.getMainHandItem().copy();
                    bottlePos = alchemyPos.below().offset(1, 0, 0);
                    bottleBlock = p.serverLevel().getBlockState(bottlePos);
                    p.serverLevel().setBlockAndUpdate(bottlePos, Blocks.AIR.defaultBlockState());
                    sneak(true);
                }),
                new Action(6, p -> useBlock(4, bottlePos.below())),
                new Action(6, p -> {
                    Container bottle = (Container) p.serverLevel().getBlockEntity(bottlePos);
                    Container restored = (Container) BlockEntity.loadStatic(bottlePos, p.serverLevel().getBlockState(bottlePos),
                            ((BlockEntity) bottle).saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("潜行真实放置扣瓶，单槽保存重读与客户端保留完整丹药组件", p.getMainHandItem().isEmpty()
                            && ItemStack.matches(bottledMedicine, bottle.getItem(0)) && ItemStack.matches(bottledMedicine, restored.getItem(0))
                            && ItemStack.matches(bottledMedicine, ((Container) minecraft.level.getBlockEntity(bottlePos)).getItem(0)));
                    sneak(false);
                }),
                new Action(6, p -> useBlock(4, bottlePos)),
                new Action(4, p -> {
                    expectTrue("空手右键拾回一瓶并移除方块，丹数与组件保持且同步", p.serverLevel().isEmptyBlock(bottlePos)
                        && p.getInventory().items.stream().anyMatch(stack -> ItemStack.matches(stack, bottledMedicine))
                        && minecraft.player.getInventory().items.stream().anyMatch(stack -> ItemStack.matches(stack, bottledMedicine)));
                    p.getInventory().setItem(4, bottledMedicine.copy());
                    p.inventoryMenu.broadcastChanges();
                    feedingTarget = EntityType.COW.create(p.serverLevel());
                    feedingTarget.setNoAi(true);
                    feedingTarget.setNoGravity(true);
                    feedingTarget.setPos(p.position().add(0, 0, 2));
                    p.serverLevel().addFreshEntity(feedingTarget);
                    sneak(true);
                }),
                new Action(6, p -> minecraft.execute(() -> minecraft.gameMode.interact(minecraft.player,
                        minecraft.level.getEntity(feedingTarget.getId()), InteractionHand.MAIN_HAND))),
                new Action(4, p -> {
                    expectTrue("潜行实际喂药作用于目标，抗性不套自服上限并只扣主手一颗", feedingTarget.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                        && feedingTarget.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 8
                        && Liandan.medicine(p.getMainHandItem()).doses() == 2
                        && ItemStack.matches(p.getMainHandItem(), minecraft.player.getMainHandItem()));
                    sneak(false);
                    separationPos = bottlePos;
                    separationBlock = p.serverLevel().getBlockState(separationPos);
                    separationBase = p.serverLevel().getBlockState(separationPos.below());
                    p.serverLevel().setBlockAndUpdate(separationPos.below(), Blocks.STONE.defaultBlockState());
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("fendantai", 1));
                    p.getInventory().setItem(9, alchemyItem("itemdanyaohulu_04", 1));
                    p.getInventory().setItem(10, alchemyItem("itemdanyaohulu_06", 1));
                    separationMedicine = bottledMedicine.copy();
                    MedicineInstance sepMed = Liandan.medicine(separationMedicine);
                    Liandan.setMedicine(separationMedicine, new MedicineInstance(sepMed.outputItemId(), "分合丹样本", sepMed.description(), 5,
                            sepMed.treasure(), sepMed.effects()));
                    separationMedicine.set(DataComponents.CUSTOM_NAME, Component.literal("分合丹样本"));
                    p.getInventory().setItem(11, separationMedicine.copy());
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, separationPos.below())),
                new Action(6, p -> useBlock(4, separationPos)),
                new Action(6, p -> {
                    expectTrue("自然放台开分丹界面，两端绑定三个单件槽且坐标一致", p.containerMenu instanceof MedicineSeparationMenu menu
                            && !menu.merging() && minecraft.screen instanceof MedicineSeparationScreen
                            && menu.slots.size() == 39 && menu.getSlot(0).getMaxStackSize() == 1
                            && menu.getSlot(0).y == 53 && menu.getSlot(2).y == 17);
                    minecraft.execute(() -> {
                        int id = minecraft.player.containerMenu.containerId;
                        for (int slot = 0; slot < 3; slot++) {
                            minecraft.gameMode.handleInventoryMouseClick(id, 3 + slot, 0, ClickType.PICKUP, minecraft.player);
                            minecraft.gameMode.handleInventoryMouseClick(id, slot, 0, ClickType.PICKUP, minecraft.player);
                        }
                    });
                }),
                new Action(6, p -> separationButton("分丹")),
                new Action(6, p -> {
                    MedicineSeparationBlockEntity table = separationTable(p);
                    expectTrue("分丹真实按钮将五颗分为左二右三，空盒转满盒并复制完整组件，原瓶清空", table.getItem(0).is(alchemyItem("itemdanyaohulu_0401", 1).getItem())
                            && Liandan.medicine(table.getItem(0)).doses() == 2 && Liandan.medicine(table.getItem(1)).doses() == 3
                            && table.getItem(0).getHoverName().getString().equals("分合丹样本")
                            && Boolean.TRUE.equals(table.getItem(1).get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))
                            && !Liandan.medicine(table.getItem(2)).isPresent()
                            && ItemStack.matches(table.getItem(0), minecraft.player.containerMenu.getSlot(0).getItem()));
                    minecraft.execute(() -> screenshots.take("liandan-split.png"));
                    separationButton("合丹");
                }),
                new Action(6, p -> {
                    expectTrue("切换合丹保留同台库存，两个菜单身份与上下槽位切换", p.containerMenu instanceof MedicineSeparationMenu menu
                            && menu.merging() && menu.getSlot(0).y == 17 && menu.getSlot(2).y == 53
                            && Liandan.medicine(menu.getSlot(0).getItem()).doses() == 2
                            && ((MedicineSeparationMenu) minecraft.player.containerMenu).merging());
                    separationButton("合丹");
                }),
                new Action(6, p -> {
                    MedicineSeparationBlockEntity table = separationTable(p);
                    MedicineSeparationBlockEntity restored = (MedicineSeparationBlockEntity) BlockEntity.loadStatic(separationPos, table.getBlockState(),
                            table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("合丹真实按钮汇总五颗并返空盒，保存重读和双端组件保持，六面只进无面可取", ItemStack.matches(table.getItem(2), separationMedicine)
                            && ItemStack.matches(restored.getItem(2), separationMedicine)
                            && ItemStack.matches(minecraft.player.containerMenu.getSlot(2).getItem(), separationMedicine)
                            && table.getItem(0).is(alchemyItem("itemdanyaohulu_04", 1).getItem())
                            && table.handler(Direction.DOWN).extractItem(2, 1, true).isEmpty()
                            && !table.handler(null).extractItem(2, 1, true).isEmpty());
                    minecraft.execute(() -> screenshots.take("liandan-merge.png"));
                    table.setItem(0, separationMedicine.copy());
                    ItemStack different = separationMedicine.copy();
                    MedicineInstance diffMed = Liandan.medicine(different);
                    List<MedicineInstance.Effect> diffEffects = new ArrayList<>(diffMed.effects());
                    diffEffects.set(0, new MedicineInstance.Effect(diffEffects.get(0).id(), 200, diffEffects.get(0).duration()));
                    Liandan.setMedicine(different, new MedicineInstance(diffMed.outputItemId(), diffMed.name(), diffMed.description(),
                            diffMed.doses(), diffMed.treasure(), diffEffects));
                    table.setItem(1, different);
                    table.setItem(2, alchemyItem("itemdanyaohulu_05", 1));
                    p.containerMenu.broadcastChanges();
                    separationButton("合丹");
                }),
                new Action(6, p -> {
                    MedicineSeparationBlockEntity table = separationTable(p);
                    expectTrue("同名但药效不同的合丹请求不消费三槽", ItemStack.matches(table.getItem(0), separationMedicine)
                            && Liandan.medicine(table.getItem(1)).doses() == 5 && !Liandan.medicine(table.getItem(2)).isPresent());
                    MedicineInstance prevMed = Liandan.medicine(table.getItem(1));
                    Liandan.setMedicine(table.getItem(1), new MedicineInstance(prevMed.outputItemId(), "另一名称", prevMed.description(),
                            prevMed.doses(), prevMed.treasure(), prevMed.effects()));
                    p.containerMenu.broadcastChanges();
                    separationButton("合丹");
                }),
                new Action(6, p -> {
                    MedicineSeparationBlockEntity table = separationTable(p);
                    MedicineInstance mergedMed = Liandan.medicine(table.getItem(2));
                    expectTrue("来源名称不同仅提示仍合并，保留左瓶药效并变满六边盒", table.getItem(2).is(alchemyItem("itemdanyaohulu_0501", 1).getItem())
                            && mergedMed.doses() == 10 && mergedMed.effects().get(0).level() == 199
                            && table.getItem(2).getHoverName().getString().equals("分合丹样本"));
                    p.closeContainer();
                    pharmacologyPos = bottlePos.offset(0, 0, 1);
                    for (int below = 0; below < 2; below++) pharmacologyBlocks.add(p.serverLevel().getBlockState(pharmacologyPos.below(below)));
                    p.serverLevel().setBlockAndUpdate(pharmacologyPos, Blocks.AIR.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(pharmacologyPos.below(), Blocks.STONE.defaultBlockState());
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("yaolitaiblock", 1));
                    ItemStack medium = new ItemStack(Items.BOOK);
                    medium.set(DataComponents.CUSTOM_NAME, Component.literal("旧媒介"));
                    medium.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    medium.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
                    p.getInventory().setItem(9, medium);
                    for (int slot = 10; slot <= 13; slot++) p.getInventory().setItem(slot, alchemyItem("spiritgrass", 1));
                    ItemStack recipeNote = new ItemStack(Items.PAPER);
                    recipeNote.set(DataComponents.CUSTOM_NAME, Component.literal("丹方说明"));
                    p.getInventory().setItem(14, recipeNote);
                    ItemStack medicineNote = new ItemStack(Items.PAPER);
                    medicineNote.set(DataComponents.CUSTOM_NAME, Component.literal("药效说明"));
                    p.getInventory().setItem(15, medicineNote);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, pharmacologyPos.below())),
                new Action(6, p -> useBlock(4, pharmacologyPos)),
                new Action(6, p -> {
                    PharmacologyBlockEntity table = pharmacologyTable(p);
                    expectTrue("自然放药理台初始化药性快照，真实开窗双方十一可见槽和隐藏预览隔离", p.containerMenu instanceof PharmacologyMenu
                            && minecraft.screen instanceof PharmacologyScreen && p.containerMenu.slots.size() == 47
                            && table.getContainerSize() == 12 && p.containerMenu.getSlot(0).getMaxStackSize() == 1
                            && table.herbs().effectCount("yvanchuxiuzhen:spiritgrass") == 4);
                    minecraft.execute(() -> {
                        int id = minecraft.player.containerMenu.containerId;
                        int[] destinations = {0, 1, 2, 3, 4, 9, 10};
                        for (int slot = 0; slot < destinations.length; slot++) {
                            minecraft.gameMode.handleInventoryMouseClick(id, 11 + slot, 0, ClickType.PICKUP, minecraft.player);
                            minecraft.gameMode.handleInventoryMouseClick(id, destinations[slot], 0, ClickType.PICKUP, minecraft.player);
                        }
                        minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast)
                                .findFirst().orElseThrow().setValue("药理样本");
                    });
                }),
                new Action(6, p -> separationButton("刷新药性")),
                new Action(6, p -> {
                    PharmacologyBlockEntity table = pharmacologyTable(p);
                    MedicineInstance preview = Liandan.medicine(table.getItem(11));
                    PharmacologyBlockEntity restored = (PharmacologyBlockEntity) BlockEntity.loadStatic(pharmacologyPos, table.getBlockState(),
                            table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("实际刷新保留八草和媒介，隐藏预览按顺序合药性，客户端显示及保存重读保持", table.getItem(11).is(Items.DIAMOND)
                            && preview.name().equals("药理样本") && !preview.effects().isEmpty()
                            && preview.effects().get(0).level() == 320 && preview.effects().get(0).duration() == 3200
                            && table.getItem(1).getCount() == 1 && table.getItem(0).has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
                            && ItemStack.matches(table.getItem(11), restored.getItem(11))
                            && ((PharmacologyMenu) minecraft.player.containerMenu).table().effectLine(0).contains("3级")
                            && table.handler(Direction.DOWN).extractItem(0, 1, true).isEmpty()
                            && !table.handler(Direction.DOWN).extractItem(9, 1, true).isEmpty()
                            && table.handler(Direction.UP).insertItem(11, new ItemStack(Items.STONE), true).is(Items.STONE));
                    minecraft.execute(() -> screenshots.take("liandan-pharmacology.png"));
                    separationButton("写入丹方");
                }),
                new Action(6, p -> {
                    PharmacologyBlockEntity table = pharmacologyTable(p);
                    ItemStack recipe = table.getItem(0);
                    MedicineRecipe contents = Liandan.recipe(recipe);
                    expectTrue("实际写丹方保留Item数量且替换全部旧组件，不消费材料说明、不复制预览药效并同步", recipe.is(Items.BOOK) && recipe.getCount() == 1
                            && recipe.getHoverName().getString().equals("药理样本") && !recipe.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
                            && contents.ingredient(1).id().equals("yvanchuxiuzhen:spiritgrass") && contents.ingredient(8).id().equals("minecraft:air")
                            && contents.description().equals("[丹方说明]") && contents.medicineDescription().equals("[药效说明]")
                            && !Liandan.medicine(recipe).isPresent() && table.getItem(4).getCount() == 1 && table.getItem(9).getCount() == 1
                            && ItemStack.matches(recipe, minecraft.player.containerMenu.getSlot(0).getItem()));
                    p.closeContainer();
                    tripodPos = alchemyPos.offset(0, 0, 3);
                    for (int below = 0; below < 3; below++) tripodBlocks.add(p.serverLevel().getBlockState(tripodPos.below(below)));
                    p.serverLevel().setBlockAndUpdate(tripodPos, Blocks.AIR.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(tripodPos.below(), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(tripodPos.below(2), Blocks.STONE.defaultBlockState());
                    refiningProfession = p.getData(ProfessionAttachments.PROGRESS).get(Profession.REFINING);
                    p.getData(ProfessionAttachments.PROGRESS).set(Profession.REFINING, new ProfessionProgress(9, 0, 100));
                    p.syncData(ProfessionAttachments.PROGRESS);
                    warehouse = MaterialStorage.snapshot(p);
                    p.setData(StorageData.WAREHOUSE, MaterialWarehouse.EMPTY);
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("tripod_01", 1));
                    p.getInventory().setItem(5, Yihuo.createDanFire(p.getStringUUID(), 10));
                    p.getInventory().setItem(7, Yihuo.createDanFire(p.getStringUUID(), 20));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, tripodPos.below())),
                new Action(6, p -> useBlock(6, tripodPos)),
                new Action(6, p -> {
                    TripodBlockEntity tripod = tripod(p);
                    expectTrue("自然放鼎开窗同步本炉参数，十一可见槽、隐藏槽与成品禁手取保持", minecraft.screen instanceof TripodScreen
                            && p.containerMenu instanceof TripodMenu && p.containerMenu.slots.size() == 47 && tripod.getContainerSize() == 12
                            && tripod.refinerLevel() == 9 && tripod.tripodLevel() == 1
                            && !p.containerMenu.getSlot(9).mayPickup(p) && !p.containerMenu.getSlot(9).mayPlace(new ItemStack(Items.STONE))
                            && tripod.handler(Direction.UP).insertItem(9, new ItemStack(Items.STONE), true).is(Items.STONE));
                    tripod.setItem(0, new ItemStack(Items.IRON_SWORD));
                    tripod.setItem(1, refiningMaterial(30, 2));
                    tripod.setItem(10, new ItemStack(Items.BOOK, 2));
                    p.containerMenu.broadcastChanges();
                    separationButton("保存图纸");
                }),
                new Action(6, p -> {
                    TripodBlockEntity tripod = tripod(p);
                    LianqiBlueprint bp = Lianqi.blueprint(tripod.getItem(10)).orElse(LianqiBlueprint.EMPTY);
                    expectTrue("真实保存图纸保留材料，媒介压一件且仅记录一至八槽", tripod.getItem(10).getCount() == 1
                            && bp.itemId().equals("minecraft:iron_sword")
                            && bp.ingredient(1).count() == 2 && bp.ingredient(0).id().isEmpty()
                            && tripod.getItem(1).getCount() == 2);
                    LianqiBlueprint translatedRecipe = new LianqiBlueprint("[Iron Sword]", bp.nameComponentJson(), bp.description(),
                            bp.itemId(), bp.itemDescription(), bp.itemDescriptionComponentJson(), bp.type(), bp.levelText(),
                            bp.level(), bp.sourceCarrierId(), List.of(
                                    bp.ingredient(0),
                                    new LianqiBlueprint.Ingredient(bp.ingredient(1).id(), "[Cailiao 1 7 1]",
                                            bp.ingredient(1).nameComponentJson(), bp.ingredient(1).count()),
                                    bp.ingredient(2), bp.ingredient(3), bp.ingredient(4), bp.ingredient(5),
                                    bp.ingredient(6), bp.ingredient(7), bp.ingredient(8)));
                    Lianqi.setBlueprint(tripod.getItem(10), translatedRecipe);
                    tripod.changed();
                    expectTrue("图纸保留可翻译名称，英文服务端根字符串不覆盖中文材料与成品名", translatedRecipe.ingredient(1).nameComponentJson().contains("translate")
                            && translatedRecipe.nameComponentJson().contains("item.minecraft.iron_sword")
                            && ((TripodMenu) p.containerMenu).recipeLine(1).contains(tripod.getItem(1).getHoverName().getString())
                            && Equipment.createRefinedProduct(p.registryAccess(), translatedRecipe.toRefinement(), EquipmentRefinement.EMPTY, ItemStack.EMPTY)
                                    .getHoverName().getString().equals(new ItemStack(Items.IRON_SWORD).getHoverName().getString()));
                    tripod.setItem(0, ItemStack.EMPTY);
                    tripod.setItem(1, ItemStack.EMPTY);
                    MaterialStorage.deposit(p, refiningMaterial(30, 1));
                    MaterialStorage.deposit(p, refiningMaterial(20, 1));
                    MaterialStorage.preferences(p, new Preferences(1, 0, 0, false, true));
                    separationButton("按图纸装料");
                }),
                new Action(6, p -> {
                    TripodBlockEntity tripod = tripod(p);
                    expectTrue("真实扣仓按高等级先扣且同槽逐桶覆盖，仓清空同步而鼎只留末桶", MaterialStorage.snapshot(p).total() == 0
                            && MaterialStorage.snapshot(minecraft.player).total() == 0 && tripod.getItem(1).getCount() == 1
                            && Materials.instance(tripod.getItem(1)).level() == 20);
                    MaterialStorage.preferences(p, Preferences.EMPTY);
                    tripod.setItem(1, ItemStack.EMPTY);
                    p.getInventory().setItem(9, refiningMaterial(30, 3));
                    p.containerMenu.broadcastChanges();
                    separationButton("按图纸装料");
                }),
                new Action(6, p -> {
                    expectTrue("仓关闭真实装料从足量整栈扣二件并同步组件", tripod(p).getItem(1).getCount() == 2
                            && p.getInventory().getItem(9).getCount() == 1
                            && ItemStack.matches(tripod(p).getItem(1), minecraft.player.containerMenu.getSlot(1).getItem()));
                    separationButton("开始炼器");
                }),
                new Action(45, p -> {}),
                new Action(6, p -> {
                    TripodBlockEntity tripod = tripod(p);
                    TripodBlockEntity restored = (TripodBlockEntity) BlockEntity.loadStatic(tripodPos, tripod.getBlockState(),
                            tripod.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    ItemStack planned = tripod.plannedItem();
                    expectTrue("图纸制作立即扣料且无热暂停，完整计划成品与炉次保存重读保持", tripod.refining()
                            && tripod.lianqiTick() == 0 && tripod.lianqiTickMax() == 9 && tripod.getItem(1).isEmpty()
                            && tripod.getItem(9).isEmpty() && tripod.getItem(11).isEmpty() && planned.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY).health() == 10
                            && Equipment.product(planned).orElseThrow().level() == 4
                            && restored.refining() == tripod.refining() && restored.lianqiTickMax() == tripod.lianqiTickMax()
                            && ItemStack.matches(restored.plannedItem(), tripod.plannedItem())
                            && ItemStack.matches(tripod.getItem(10), restored.getItem(10)));
                    p.closeContainer();
                    p.serverLevel().removeBlock(tripodPos.below(), false);
                }),
                new Action(240, p -> useBlock(5, tripodPos.below(2))),
                new Action(6, p -> useBlock(6, tripodPos)),
                new Action(6, p -> {
                    TripodBlockEntity tripod = tripod(p);
                    refinedProduct = tripod.getItem(9).copy();
                    expectTrue("丹火自然供热完成炼器，服务端成品属性与客户端完整组件一致", refinedProduct.is(Items.IRON_SWORD)
                            && !tripod.refining() && refinedProduct.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY).power() == 80
                            && ItemStack.matches(refinedProduct, minecraft.player.containerMenu.getSlot(9).getItem()));
                    minecraft.execute(() -> screenshots.take("lianqi-finished.png"));
                    separationButton("领取成品");
                }),
                new Action(6, p -> {
                    expectTrue("真实领取成品清槽且只结算四点炼器经验，玩家库存与职业同步", tripod(p).getItem(9).isEmpty()
                            && Professions.experience(p, Profession.REFINING) == 4 && Professions.experience(minecraft.player, Profession.REFINING) == 4
                            && p.getInventory().items.stream().anyMatch(stack -> ItemStack.matches(stack, refinedProduct)));
                    tripod(p).setItem(0, refinedProduct.copy());
                    tripod(p).setItem(1, refiningMaterial(30, 1));
                    tripod(p).setItem(10, ItemStack.EMPTY);
                    p.containerMenu.broadcastChanges();
                    separationButton("开始炼器");
                    useBlock(7, tripodPos.below());
                }),
                new Action(120, p -> {}),
                new Action(6, p -> {
                    ItemStack upgraded = tripod(p).getItem(9);
                    expectTrue("自然升级沿用完整小数材料累计，扣除旧装备和材料并同步新成品（实际原料生命："
                            + Equipment.product(upgraded).map(product -> product.materials().health()).orElse(0.0) + "）", upgraded.is(Items.IRON_SWORD)
                            && tripod(p).getItem(0).isEmpty() && tripod(p).getItem(1).isEmpty()
                            && Equipment.product(upgraded).orElseThrow().level() == 6 && upgraded.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY).health() == 20
                            && Math.abs(Equipment.product(upgraded).orElseThrow().materials().health() - 428.4) < .0001
                            && ItemStack.matches(upgraded, minecraft.player.containerMenu.getSlot(9).getItem()));
                    p.closeContainer();
                    repairPos = prepareWorkbench(p, alchemyPos.offset(-3, 0, 0));
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("xiulitai", 1));
                    p.getInventory().setItem(9, refiningMaterial(3, 1));
                    ItemStack quality = refiningMaterial(2, 1);
                    Materials.setInstance(quality, 2, 3);
                    p.getInventory().setItem(10, quality);
                    p.getInventory().setItem(11, refiningMaterial(2, 2));
                    ItemStack damaged = new ItemStack(Items.IRON_SWORD);
                    EquipmentRefinement.Mutable refinement = EquipmentRefinement.mutable();
                    refinement.putInt("item_level_num", 2);
                    damaged.set(EquipmentData.REFINEMENT.get(), refinement.toImmutable());
                    damaged.setDamageValue(200);
                    p.getInventory().setItem(12, damaged);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, repairPos.below())),
                new Action(6, p -> useBlock(6, repairPos)),
                new Action(6, p -> minecraft.execute(() -> {
                    int id = minecraft.player.containerMenu.containerId;
                    minecraft.gameMode.handleInventoryMouseClick(id, 4, 0, ClickType.PICKUP, minecraft.player);
                    minecraft.gameMode.handleInventoryMouseClick(id, 0, 0, ClickType.PICKUP, minecraft.player);
                })),
                new Action(6, p -> separationButton("修理")),
                new Action(6, p -> {
                    RepairTableBlockEntity table = repairTable(p);
                    expectTrue("自然放修理台开窗入槽，首次修理只刷新二级需求且不消费材料", p.containerMenu instanceof RepairTableMenu
                            && minecraft.screen instanceof RepairTableScreen && table.getItem(0).getDamageValue() == 200
                            && table.requiredLevel() == 2
                            && p.getInventory().getItem(9).getCount() == 1 && p.getInventory().getItem(10).getCount() == 1
                            && p.getInventory().getItem(11).getCount() == 2);
                    separationButton("修理");
                }),
                new Action(45, p -> {
                    RepairTableBlockEntity table = repairTable(p);
                    expectTrue("第二次真实修理优先低等级并跳过上品，扣一件修八十耐久且同步", table.getItem(0).getDamageValue() == 120
                            && p.getInventory().getItem(9).getCount() == 1 && p.getInventory().getItem(10).getCount() == 1
                            && p.getInventory().getItem(11).getCount() == 1
                            && table.requirement().equals("修理一次！")
                            && ItemStack.matches(table.getItem(0), minecraft.player.containerMenu.getSlot(0).getItem()));
                    minecraft.execute(() -> screenshots.take("lianqi-repair.png"));
                }),
                new Action(6, p -> {
                    RepairTableBlockEntity table = repairTable(p);
                    RepairTableBlockEntity restored = (RepairTableBlockEntity) BlockEntity.loadStatic(repairPos, table.getBlockState(),
                            table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("修理四十刻后提示恢复，需求与完整装备保存重读且侧面不能抽取", table.requirement().contains("2到6")
                            && restored.requirement().equals(table.requirement())
                            && restored.requiredLevel() == table.requiredLevel()
                            && restored.repairType().equals(table.repairType())
                            && ItemStack.matches(restored.getItem(0), table.getItem(0))
                            && table.handler(Direction.DOWN).extractItem(0, 1, true).isEmpty()
                            && !table.handler(null).extractItem(0, 1, true).isEmpty());
                    p.closeContainer();
                    sewingPos = prepareWorkbench(p, alchemyPos.offset(-3, 0, 1));
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("zhenxianpoluo", 1));
                    ItemStack first = alchemyItem("cailiao_171", 4);
                    ItemStack second = alchemyItem("cailiao_171", 4);
                    Materials.setInstance(first, 12, 3);
                    Materials.setInstance(second, 23, 4);
                    p.getInventory().setItem(9, first);
                    p.setItemSlot(EquipmentSlot.OFFHAND, second);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, sewingPos.below())),
                new Action(6, p -> useBlock(6, sewingPos)),
                new Action(6, p -> minecraft.execute(() -> minecraft.gameMode.handleInventoryMouseClick(
                        minecraft.player.containerMenu.containerId, 0, 0, ClickType.PICKUP, minecraft.player))),
                new Action(6, p -> {
                    SewingBasketBlockEntity basket = sewingBasket(p);
                    expectTrue("自然开针线笸箩延后填第一页，两端八槽只读且真实点击不能取预览", p.containerMenu instanceof SewingBasketMenu
                            && minecraft.screen instanceof SewingBasketScreen && basket.maxPage() == 14
                            && basket.getItem(0).is(alchemyItem("clothes_01_helmet", 1).getItem()) && basket.getItem(4).getCount() == 8
                            && p.containerMenu.getCarried().isEmpty() && !p.containerMenu.getSlot(4).mayPlace(alchemyItem("cailiao_171", 1))
                            && basket.handler(Direction.DOWN).extractItem(0, 1, true).isEmpty()
                            && ItemStack.matches(basket.getItem(0), minecraft.player.containerMenu.getSlot(0).getItem()));
                    separationButton("制作");
                }),
                new Action(6, p -> {
                    SewingBasketBlockEntity basket = sewingBasket(p);
                    SewingBasketBlockEntity restored = (SewingBasketBlockEntity) BlockEntity.loadStatic(sewingPos, basket.getBlockState(),
                            basket.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("真实缝纫按ID合计异组件主包与副手材料，扣八给四件并保留可保存预览", p.getInventory().getItem(9).isEmpty()
                            && p.getOffhandItem().isEmpty() && ownsClothing(p, "01") && ownsClothing(minecraft.player, "01")
                            && basket.getItem(4).getCount() == 8 && ItemStack.matches(basket.getItem(0), restored.getItem(0))
                            && restored.selectedPage() == basket.selectedPage() && restored.maxPage() == basket.maxPage());
                    p.getInventory().setItem(9, alchemyItem("cailiao_171", 4));
                    p.getInventory().setItem(10, new ItemStack(Items.LEATHER, 4));
                    p.containerMenu.broadcastChanges();
                    separationButton("<<");
                }),
                new Action(6, p -> {
                    SewingBasketBlockEntity basket = sewingBasket(p);
                    sewingLastPage = basket.selectedPage() == 14
                            && basket.getItem(0).is(alchemyItem("clothes_15_helmet", 1).getItem())
                            && basket.getItem(4).getCount() == 4 && basket.getItem(5).is(Items.LEATHER) && basket.getItem(5).getCount() == 4;
                    minecraft.execute(() -> screenshots.take("lianqi-sewing.png"));
                    separationButton("制作");
                }),
                new Action(6, p -> p.closeContainer()),
                new Action(6, p -> {
                    SewingBasketBlockEntity basket = sewingBasket(p);
                    expectTrue("上一页由零回绕十四并制作混合配方，关闭清空共享八槽且保留页码", sewingLastPage
                            && p.getInventory().getItem(9).isEmpty() && p.getInventory().getItem(10).isEmpty()
                            && ownsClothing(p, "15") && ownsClothing(minecraft.player, "15") && basket.isEmpty()
                            && basket.selectedPage() == 14
                            && ((SewingBasketBlockEntity) minecraft.level.getBlockEntity(sewingPos)).isEmpty());
                    blueprintPos = prepareWorkbench(p, alchemyPos.offset(-3, 0, 2));
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("tiandaotuzhitai", 1));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, blueprintPos.below())),
                new Action(6, p -> useBlock(6, blueprintPos)),
                new Action(6, p -> separationButton("刷新图纸预览")),
                new Action(6, p -> {
                    BlueprintTableBlockEntity table = blueprintTable(p);
                    int filled = 0;
                    for (int slot = 13; slot < 223; slot++) if (table.getItem(slot).getCount() == 64) filled++;
                    expectTrue("自然图纸台开窗填一百九十一栈目录，空料刷新保留来源四十一级超品", p.containerMenu instanceof BlueprintTableMenu
                            && minecraft.screen instanceof BlueprintTableScreen && p.containerMenu.slots.size() == 259 && filled == 191
                            && table.getItem(13).is(alchemyItem("cailiao_001", 1).getItem()) && table.getItem(204).isEmpty()
                            && table.previewData().integer("item_level_num") == 41
                            && table.previewData().text("item_level").equals("超品")
                            && !p.containerMenu.getSlot(13).mayPlace(table.getItem(13)) && p.containerMenu.getSlot(13).mayPickup(p));
                    table.setItem(0, refiningMaterial(1, 2));
                    ItemStack medium = new ItemStack(Items.PAPER, 3);
                    medium.set(DataComponents.CUSTOM_NAME, Component.literal("原载体"));
                    medium.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    table.setItem(9, medium);
                    table.setItem(10, new ItemStack(Items.IRON_SWORD));
                    ItemStack note = new ItemStack(Items.PAPER);
                    note.set(DataComponents.CUSTOM_NAME, Component.literal("图纸说明"));
                    table.setItem(11, note);
                    p.containerMenu.broadcastChanges();
                    minecraft.execute(() -> {
                        minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast)
                                .findFirst().orElseThrow().setValue("[天道]样本");
                        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 13, 0, ClickType.QUICK_MOVE, minecraft.player);
                    });
                }),
                new Action(6, p -> separationButton("保存图纸")),
                new Action(6, p -> {
                    BlueprintTableBlockEntity table = blueprintTable(p);
                    ItemStack recipe = table.getItem(9);
                    LianqiBlueprint contents = Lianqi.blueprint(recipe).orElse(LianqiBlueprint.EMPTY);
                    BlueprintTableBlockEntity restored = (BlueprintTableBlockEntity) BlockEntity.loadStatic(blueprintPos, table.getBlockState(),
                            table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("目录真实Shift可取，图纸保留组件与九槽配方，成品去名称括号并保留翻译样式", table.getItem(13).isEmpty()
                            && p.getInventory().items.stream().anyMatch(stack -> stack.is(alchemyItem("cailiao_001", 1).getItem()) && stack.getCount() == 64)
                            && recipe.is(Items.PAPER) && recipe.getCount() == 1 && recipe.getHoverName().getString().equals("原载体")
                            && Boolean.TRUE.equals(recipe.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) && contents.name().equals("[天道]样本")
                            && contents.itemId().equals("minecraft:iron_sword") && contents.type().equals("武器")
                            && contents.itemDescription().equals("[图纸说明]") && contents.ingredient(0).count() == 2
                            && Component.Serializer.fromJson(contents.nameComponentJson(), p.registryAccess()).getString().equals("天道样本")
                            && Component.Serializer.fromJson(contents.itemDescriptionComponentJson(), p.registryAccess()).getString().equals("[图纸说明]")
                            && contents.ingredient(8).id().equals("minecraft:air") && table.getItem(0).getCount() == 2
                            && ItemStack.matches(recipe, restored.getItem(9)) && ItemStack.matches(recipe, minecraft.player.containerMenu.getSlot(9).getItem())
                            && refinedNameParity(p, contents));
                    minecraft.execute(() -> screenshots.take("lianqi-blueprint.png"));
                    p.closeContainer();
                    poisonPos = prepareWorkbench(p, alchemyPos.offset(-3, 0, 3));
                    p.serverLevel().setBlockAndUpdate(poisonPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
                    p.getInventory().clearContent();
                    p.getInventory().setItem(4, alchemyItem("cailiao_141", 2));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, poisonPos)),
                new Action(6, p -> {
                    PoisonCauldronBlockEntity cauldron = (PoisonCauldronBlockEntity) p.serverLevel().getBlockEntity(poisonPos);
                    PoisonCauldronBlockEntity restored = (PoisonCauldronBlockEntity) BlockEntity.loadStatic(poisonPos, cauldron.getBlockState(),
                            cauldron.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    PoisonCauldronBlockEntity clientCauldron = (PoisonCauldronBlockEntity) minecraft.level.getBlockEntity(poisonPos);
                    expectTrue("材料真实右键水锅转毒锅扣一件，毒性命名空间与附魔保存并同步", p.getMainHandItem().getCount() == 1
                            && cauldron.poisonNamespace().equals(ModId.VALUE)
                            && cauldron.poisonId().equals("sinomeninevine")
                            && restored.poisonNamespace().equals(cauldron.poisonNamespace())
                            && restored.poisonId().equals(cauldron.poisonId())
                            && clientCauldron.poisonNamespace().equals(cauldron.poisonNamespace())
                            && clientCauldron.poisonId().equals(cauldron.poisonId()));
                    p.getInventory().setItem(4, new ItemStack(Items.IRON_SWORD));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, poisonPos)),
                new Action(6, p -> {
                    poisonRejected = p.serverLevel().getBlockEntity(poisonPos) instanceof PoisonCauldronBlockEntity
                            && EnchantmentHelper.getItemEnchantmentLevel(poisonEnchantment(p), p.getMainHandItem()) == 0;
                    p.getMainHandItem().enchant(poisonEnchantment(p), 3);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, poisonPos)),
                new Action(6, p -> {
                    expectTrue("毒藤锅保留已有附魔前置：未附魔不消费，三级剑淬毒后保三级并返空锅", poisonRejected
                            && p.serverLevel().getBlockState(poisonPos).is(Blocks.CAULDRON)
                            && EnchantmentHelper.getItemEnchantmentLevel(poisonEnchantment(p), p.getMainHandItem()) == 3
                            && ItemStack.matches(p.getMainHandItem(), minecraft.player.getMainHandItem()));
                    p.getInventory().setItem(4, alchemyItem("cailiao_141", 2));
                    p.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_SWORD));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useMedicine()),
                new Action(6, p -> {
                    ItemStack result = p.getOffhandItem();
                    EquipmentRefinement raw = result.getOrDefault(EquipmentData.REFINEMENT.get(), EquipmentRefinement.EMPTY);
                    expectTrue("毒材真实右键空气淬副手耗一件，一级附魔与先增总数后写fumo1完整同步", p.getMainHandItem().getCount() == 1
                            && EnchantmentHelper.getItemEnchantmentLevel(poisonEnchantment(p), result) == 1
                            && raw.integer("fumozongshu") == 1 && raw.text("fumo1").equals("sinomeninevine")
                            && raw.text("fumo1kongjian").equals(ModId.VALUE) && raw.integer("fumo1num") == 100
                            && raw.text("fumo0").isEmpty() && ItemStack.matches(result, minecraft.player.getOffhandItem()));
                }));
    }

    /** 在真实图纸记录副本上覆盖带括号模板，核对首次成品、升级及存档后的名称组件。 */
    private static boolean refinedNameParity(ServerPlayer player, LianqiBlueprint recipe) {
        Component templateName = Component.literal("[灵]").withStyle(ChatFormatting.GOLD)
                .append(Component.translatableWithFallback("item.minecraft.iron_sword", "[铁剑]"))
                .append(Component.translatableWithFallback("yvanchuxiuzhen.smoke.refined_name", "[%s][%s]",
                        Component.literal("[芯]").withStyle(ChatFormatting.BOLD), "[尾]"));
        ItemStack template = new ItemStack(Items.IRON_SWORD);
        template.set(DataComponents.CUSTOM_NAME, templateName);
        EquipmentRefinement namedRecipe = new LianqiBlueprint(template.getDisplayName().getString(),
                Component.Serializer.toJson(template.getHoverName(), player.registryAccess()),
                recipe.description(), recipe.itemId(), recipe.itemDescription(), recipe.itemDescriptionComponentJson(),
                recipe.type(), recipe.levelText(), recipe.level(), recipe.sourceCarrierId(), recipe.ingredients()).toRefinement();
        ItemStack product = Equipment.createRefinedProduct(player.registryAccess(), namedRecipe, EquipmentRefinement.EMPTY, ItemStack.EMPTY);
        ItemStack upgraded = Equipment.createRefinedProduct(player.registryAccess(), namedRecipe, EquipmentRefinement.EMPTY, product);
        ItemStack restored = ItemStack.parse(player.registryAccess(), product.save(player.registryAccess())).orElseThrow();
        Component expected = Component.literal("灵").withStyle(ChatFormatting.GOLD)
                .append(Component.translatableWithFallback("item.minecraft.iron_sword", "铁剑"))
                .append(Component.translatableWithFallback("yvanchuxiuzhen.smoke.refined_name", "%s%s",
                        Component.literal("芯").withStyle(ChatFormatting.BOLD), "尾"));
        return product.getHoverName().equals(expected) && upgraded.getHoverName().equals(expected)
                && restored.getHoverName().equals(expected) && template.getHoverName().getString().contains("[灵]");
    }

    private static ItemStack alchemyItem(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), count);
    }

    private AlchemyBlockEntity furnace(ServerPlayer player) {
        return (AlchemyBlockEntity) player.serverLevel().getBlockEntity(alchemyPos);
    }

    private MedicineSeparationBlockEntity separationTable(ServerPlayer player) {
        return (MedicineSeparationBlockEntity) player.serverLevel().getBlockEntity(separationPos);
    }

    private PharmacologyBlockEntity pharmacologyTable(ServerPlayer player) {
        return (PharmacologyBlockEntity) player.serverLevel().getBlockEntity(pharmacologyPos);
    }

    private TripodBlockEntity tripod(ServerPlayer player) {
        return (TripodBlockEntity) player.serverLevel().getBlockEntity(tripodPos);
    }

    private RepairTableBlockEntity repairTable(ServerPlayer player) {
        return (RepairTableBlockEntity) player.serverLevel().getBlockEntity(repairPos);
    }

    private SewingBasketBlockEntity sewingBasket(ServerPlayer player) {
        return (SewingBasketBlockEntity) player.serverLevel().getBlockEntity(sewingPos);
    }

    private BlueprintTableBlockEntity blueprintTable(ServerPlayer player) {
        return (BlueprintTableBlockEntity) player.serverLevel().getBlockEntity(blueprintPos);
    }

    private BlockPos prepareWorkbench(ServerPlayer player, BlockPos pos) {
        for (int below = 0; below < 2; below++) {
            BlockPos target = pos.below(below);
            workbenchBlocks.put(target, player.serverLevel().getBlockState(target));
            BlockEntity original = player.serverLevel().getBlockEntity(target);
            if (original != null) {
                workbenchEntities.put(target, original.saveWithFullMetadata(player.registryAccess()));
                if (original instanceof Container container) container.clearContent();
            }
        }
        player.serverLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        player.serverLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        return pos;
    }

    private static boolean ownsClothing(Player player, String page) {
        for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
            Item item = alchemyItem("clothes_" + page + "_" + piece, 1).getItem();
            if (player.getInventory().items.stream().noneMatch(stack -> stack.is(item) && stack.getCount() == 1)) return false;
        }
        return true;
    }

    private static Holder<Enchantment> poisonEnchantment(ServerPlayer player) {
        return player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(
                Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "sinomeninevine")));
    }

    private static ItemStack refiningMaterial(int level, int count) {
        ItemStack stack = alchemyItem("cailiao_001", count);
        Materials.setInstance(stack, level, 0);
        return stack;
    }

    private void separationButton(String name) {
        minecraft.execute(() -> minecraft.screen.children().stream()
                .filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow().onPress());
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static Holder<MobEffect> fasting() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_031")).orElseThrow();
    }

    private void useMedicine() {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = 4;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        });
    }

    private void sneak(boolean down) {
        minecraft.execute(() -> {
            if (minecraft.options.keyShift.isDown() != down) minecraft.options.keyShift.setDown(down || minecraft.options.toggleCrouch().get());
        });
    }

    private void useBlock(int hotbar, BlockPos pos) {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = hotbar;
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos).add(0, .5, 0), Direction.UP, pos, false));
        });
    }

    private void run(ServerPlayer player, String arguments, int permission) {
        Vec3 position = new Vec3(chestPos.getX() + .25, chestPos.getY() + .5, chestPos.getZ() + .75);
        CommandSourceStack source = player.createCommandSourceStack().withPosition(position).withPermission(permission).withSuppressedOutput();
        player.getServer().getCommands().performPrefixedCommand(source, "lianqi " + arguments);
    }

    private List<ItemEntity> drops(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(chestPos).inflate(2));
    }

    private ItemStack takeDrop(ServerPlayer player) {
        ItemEntity drop = drops(player).getFirst();
        ItemStack result = drop.getItem().copy();
        drop.discard();
        return result;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.setGameMode(gameMode);
        player.removeAllEffects();
        for (MobEffectInstance effect : effects) player.addEffect(effect);
        player.getFoodData().readAdditionalSaveData(food);
        if (hadFastingTimer) player.setData(CookingAttachments.FASTING_TICKS, fastingTimer);
        else player.removeData(CookingAttachments.FASTING_TICKS);
        sneak(shiftKey);
        for (Map.Entry<BlockPos, BlockState> entry : workbenchBlocks.entrySet()) {
            BlockEntity current = player.serverLevel().getBlockEntity(entry.getKey());
            if (current instanceof Container container) container.clearContent();
            player.serverLevel().setBlockAndUpdate(entry.getKey(), entry.getValue());
            CompoundTag original = workbenchEntities.get(entry.getKey());
            if (original != null) {
                BlockEntity restored = BlockEntity.loadStatic(entry.getKey(), entry.getValue(), original, player.registryAccess());
                player.serverLevel().setBlockEntity(restored);
                restored.setChanged();
                player.serverLevel().sendBlockUpdated(entry.getKey(), entry.getValue(), entry.getValue(), 3);
            }
        }
        if (tripodPos != null) {
            tripod(player).clearContent();
            for (int below = 0; below < 3; below++) player.serverLevel().setBlockAndUpdate(tripodPos.below(below), tripodBlocks.get(below));
            player.getData(ProfessionAttachments.PROGRESS).set(Profession.REFINING, refiningProfession);
            player.syncData(ProfessionAttachments.PROGRESS);
            player.setData(StorageData.WAREHOUSE, warehouse);
        }
        if (pharmacologyPos != null) {
            pharmacologyTable(player).clearContent();
            for (int below = 0; below < 2; below++) player.serverLevel().setBlockAndUpdate(pharmacologyPos.below(below), pharmacologyBlocks.get(below));
        }
        if (separationPos != null) {
            separationTable(player).clearContent();
            player.serverLevel().setBlockAndUpdate(separationPos, separationBlock);
            player.serverLevel().setBlockAndUpdate(separationPos.below(), separationBase);
        }
        if (bottlePos != null) player.serverLevel().setBlockAndUpdate(bottlePos, bottleBlock);
        if (feedingTarget != null) feedingTarget.discard();
        furnace(player).clearContent();
        for (int below = 0; below < 3; below++) player.serverLevel().setBlockAndUpdate(alchemyPos.below(below), alchemyBlocks.get(below));
        player.getData(ProfessionAttachments.PROGRESS).set(Profession.ALCHEMY, alchemyProfession);
        player.syncData(ProfessionAttachments.PROGRESS);
        player.getInventory().selected = selected;
        minecraft.execute(() -> minecraft.player.getInventory().selected = selected);
        player.serverLevel().removeBlock(chestPos, false);
        for (ItemEntity drop : drops(player)) drop.discard();
        player.setData(SkillData.GUARD_COOLDOWN, oldGuard);
        SkillGuard.release(player);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.inventoryMenu.broadcastChanges();
    }
}
