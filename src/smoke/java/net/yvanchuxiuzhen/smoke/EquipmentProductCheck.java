package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.skill.SkillGuard;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;

/** 仅核对成品真实命令、容器与实例保存，以及新接入的属性和格挡消费边界。 */
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

    EquipmentProductCheck(List<String> report, String levelId) {
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
                new Action(4, p -> expectTrue("移除成品后武器和护甲贡献撤除", PlayerAttributes.value(p, Stat.ATTACK) == PlayerAttributes.baseValue(p, Stat.ATTACK)
                        && PlayerAttributes.value(p, Stat.HEALTH_MAX) == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX))));
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
        player.serverLevel().removeBlock(chestPos, false);
        for (ItemEntity drop : drops(player)) drop.discard();
        player.getPersistentData().putDouble("canjianjishicolddown", oldGuard);
        SkillGuard.release(player);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.inventoryMenu.broadcastChanges();
    }
}
