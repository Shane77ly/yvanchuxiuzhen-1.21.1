package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.liandan.client.AlchemyScreen;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.skill.SkillGuard;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;

/** 核对成品命令、容器与实例保存、属性消费，以及丹炉自然生产的库存和同步路径。 */
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
    private CompoundTag medicine;
    private int selected;

    EquipmentProductCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("equipmentproduct", report, levelId);
        script(
                new Action(4, p -> {
                    inventory = new ArrayList<>();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.getInventory().clearContent();
                    oldGuard = p.getPersistentData().getDouble("canjianjishicolddown");
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
                }),
                new Action(6, p -> {
                    expectTrue("成品覆盖默认修饰，生命和物攻各计一次，负移速只在武器生效", PlayerAttributes.value(p, Stat.ATTACK) == PlayerAttributes.baseValue(p, Stat.ATTACK) + 8
                            && PlayerAttributes.value(p, Stat.MANA_ATT) == PlayerAttributes.baseValue(p, Stat.MANA_ATT) + 17
                            && PlayerAttributes.value(p, Stat.SOUL_ATT) == PlayerAttributes.baseValue(p, Stat.SOUL_ATT) - 3
                            && p.getAttributeValue(Attributes.ATTACK_DAMAGE) == PlayerAttributes.value(p, Stat.ATTACK)
                            && p.getMaxHealth() == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX) + 10
                            && Math.abs(p.getAttributeValue(Attributes.ARMOR) - 3.7) < 1e-8
                            && Math.abs(p.getAttributeValue(Attributes.ATTACK_SPEED) - 1.26) < 1e-8
                            && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .08) < 1e-8
                            && weapon.getAttributeModifiers().modifiers().stream().noneMatch(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE)));
                    p.getPersistentData().putDouble("canjianjishicolddown", 0);
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
                    ItemStack fire = alchemyItem("danhuo", 1);
                    CompoundTag fuel = new CompoundTag();
                    fuel.putDouble("power", 10);
                    fuel.putString("ManaAttackUUID", p.getStringUUID());
                    fire.set(DataComponents.CUSTOM_DATA, CustomData.of(fuel));
                    p.getInventory().setItem(5, fire);
                    ItemStack supplement = fire.copy();
                    fuel.putDouble("power", 20);
                    supplement.set(DataComponents.CUSTOM_DATA, CustomData.of(fuel));
                    p.getInventory().setItem(7, supplement);
                    p.getInventory().setItem(6, ItemStack.EMPTY);
                    p.getInventory().setItem(9, alchemyItem("itemdanyaohulu_04", 2));
                    p.getInventory().setItem(10, alchemyItem("spiritgrass", 2));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(4, alchemyPos.below())),
                new Action(6, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    expectTrue("自然放炉初始化本炉药性，双端同步黄铜炉参数", furnace.getPersistentData().getCompound("zong").getInt("yvanchuxiuzhen:spiritgrassnumber") == 4
                            && furnace.getPersistentData().getDouble("alchemyLevel") == 1
                            && minecraft.level.getBlockEntity(alchemyPos).getPersistentData().equals(furnace.getPersistentData()));
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
                    CompoundTag state = furnace.getPersistentData();
                    medicine = furnace.getItem(11).get(DataComponents.CUSTOM_DATA).copyTag();
                    expectTrue("开始按钮各消耗一件、保留旧输出，无热炉次暂停，四药性给出十五炉刻", state.getBoolean("liandanBegin")
                            && furnace.getItem(0).getCount() == 1 && furnace.getItem(1).getCount() == 1 && furnace.getItem(9).is(Items.DIAMOND)
                            && state.getInt("lianqiTickMax") == 15 && state.getInt("lianqiTick") == 0 && medicine.getInt("shuliang") == 4
                            && medicine.getInt("danyaolevel0") == 80 && medicine.getInt("danyaolevel3") == -30
                            && ItemStack.matches(furnace.getItem(0), minecraft.player.containerMenu.getSlot(0).getItem()));
                    furnace.getItem(11).set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    AlchemyBlockEntity restored = (AlchemyBlockEntity) BlockEntity.loadStatic(alchemyPos, furnace.getBlockState(),
                            furnace.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("在炼炉次保存重读保留库存、隐藏成品全组件与装置状态", restored.getPersistentData().equals(state)
                            && ItemStack.matches(restored.getItem(0), furnace.getItem(0)) && ItemStack.matches(restored.getItem(11), furnace.getItem(11)));
                    p.closeContainer();
                    p.serverLevel().removeBlock(alchemyPos.below(), false);
                }),
                new Action(6, p -> useBlock(5, alchemyPos.below(2))),
                new Action(45, p -> useBlock(7, alchemyPos.below())),
                new Action(320, p -> {
                    CompoundTag state = furnace(p).getPersistentData();
                    double fuel = p.serverLevel().getBlockEntity(alchemyPos.below()).getPersistentData().getDouble("power");
                    expectTrue("丹火自然供热推进炉次，已耗热量与火块剩余燃料守恒，客户端收到进度 实际燃料=" + fuel + "，进度=" + state.getDouble("lianqiTick"),
                            state.getDouble("lianqiTick") > 0 && fuel + state.getDouble("lianpower") + state.getDouble("lianqiTick") == 30
                                    && minecraft.level.getBlockEntity(alchemyPos).getPersistentData().getDouble("lianqiTick") > 0);
                    useBlock(6, alchemyPos);
                }),
                new Action(6, p -> {
                    AlchemyBlockEntity furnace = furnace(p);
                    ItemStack output = furnace.getItem(9);
                    expectTrue("自然成丹覆盖旧输出，04容器转0401并保留全部组件，隐藏槽清空且双端一致", output.is(alchemyItem("itemdanyaohulu_0401", 1).getItem())
                            && output.get(DataComponents.CUSTOM_DATA).copyTag().equals(medicine)
                            && Boolean.TRUE.equals(output.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))
                            && output.getHoverName().getString().equals(medicine.getString("danyaoname"))
                            && furnace.getItem(11).isEmpty() && !furnace.getPersistentData().getBoolean("liandanBegin")
                            && ItemStack.matches(output, minecraft.player.containerMenu.getSlot(9).getItem()));
                    minecraft.execute(() -> {
                        screenshots.take("liandan-finished.png");
                        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 9, 0, ClickType.QUICK_MOVE, minecraft.player);
                    });
                }),
                new Action(4, p -> expectTrue("Shift取丹清空结果槽且经验只领取一次，并同步玩家", furnace(p).getItem(9).isEmpty()
                        && p.getInventory().items.stream().anyMatch(stack -> stack.is(alchemyItem("itemdanyaohulu_0401", 1).getItem()))
                        && Professions.experience(p, Profession.ALCHEMY) == 3 && Professions.experience(minecraft.player, Profession.ALCHEMY) == 3
                        && furnace(p).getPersistentData().getInt("jingyanhuoqv") == 0)));
    }

    private static ItemStack alchemyItem(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), count);
    }

    private AlchemyBlockEntity furnace(ServerPlayer player) {
        return (AlchemyBlockEntity) player.serverLevel().getBlockEntity(alchemyPos);
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
        furnace(player).clearContent();
        for (int below = 0; below < 3; below++) player.serverLevel().setBlockAndUpdate(alchemyPos.below(below), alchemyBlocks.get(below));
        player.getData(ProfessionAttachments.PROGRESS).set(Profession.ALCHEMY, alchemyProfession);
        player.syncData(ProfessionAttachments.PROGRESS);
        player.getInventory().selected = selected;
        minecraft.execute(() -> minecraft.player.getInventory().selected = selected);
        player.serverLevel().removeBlock(chestPos, false);
        for (ItemEntity drop : drops(player)) drop.discard();
        player.getPersistentData().putDouble("canjianjishicolddown", oldGuard);
        SkillGuard.release(player);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.inventoryMenu.broadcastChanges();
    }
}
