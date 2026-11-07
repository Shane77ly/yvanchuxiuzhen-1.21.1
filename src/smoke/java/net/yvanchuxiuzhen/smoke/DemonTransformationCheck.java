package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData.DemonTimer;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillScreen;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/** 妖化仅检查菜单条件、库存与数值、物品保存及分刻结束四条主路径。 */
final class DemonTransformationCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private CultivationData cultivation;
    private GameType gameMode;
    private ArmorStand display;
    private boolean rejected;
    private boolean paused;

    DemonTransformationCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("妖化", report, levelId);
        script(
                new Action(12, p -> {
                    cultivation = p.getData(XiulianAttachments.CULTIVATION);
                    gameMode = p.gameMode.getGameModeForPlayer();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.setData(XiulianAttachments.CULTIVATION, new CultivationData());
                    p.setGameMode(GameType.SURVIVAL);
                    Arena.level(p);
                    p.getInventory().clearContent();
                    Skills.openMenu(p);
                    p.containerMenu.clickMenuButton(p, 19);
                    rejected = p.containerMenu != p.inventoryMenu && p.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 21);
                    Cultivation.setTizhiBoolean(p, "tizhiopen_109", true);
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) p.getInventory().setItem(i, new ItemStack(Items.STONE));
                    p.containerMenu.clickMenuButton(p, 19);
                    rejected &= Cultivation.tizhiDouble(p, "yaohualengqve") == 0 && !(p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DemonArmorItem);
                    p.getInventory().clearContent();
                    p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                    p.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                    Skills.openMenu(p);
                }),
                new Action(12, p -> minecraft.execute(() -> {
                    if (minecraft.screen instanceof SkillScreen screen) {
                        screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                                .filter(button -> button.getMessage().getString().equals("妖化")).findFirst().ifPresent(Button::onPress);
                    }
                })),
                new Action(25, p -> {
                    expectTrue("体质与库存条件拒绝不改防具或冷却", rejected);
                    ItemStack helmet = p.getItemBySlot(EquipmentSlot.HEAD);
                    EquipmentStats stats = helmet.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
                    boolean armor = true;
                    for (ItemStack stack : p.getArmorSlots()) armor &= stack.getItem() instanceof DemonArmorItem;
                    expectTrue("真实菜单妖化搬移四件旧甲，数值与生命不重复，速度倍率生效", armor
                            && p.containerMenu == p.inventoryMenu && p.getInventory().contains(new ItemStack(Items.IRON_HELMET))
                            && p.getInventory().contains(new ItemStack(Items.IRON_CHESTPLATE)) && p.getInventory().contains(new ItemStack(Items.IRON_LEGGINGS))
                            && p.getInventory().contains(new ItemStack(Items.IRON_BOOTS)) && stats.health() == 21 && stats.power() == 19
                            && stats.soul() == 20 && stats.physicalDefense() == 15 && stats.soulDefense() == 15
                            && PlayerAttributes.value(p, Stat.HEALTH_MAX) == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX) + 84
                            && p.getAttributeValue(Attributes.MOVEMENT_SPEED) > .1
                            && Cultivation.tizhiDouble(p, "yaohualengqve") > 115 && p.hasEffect(MobEffects.INVISIBILITY));
                    display = new ArmorStand(p.level(), p.getX(), p.getY(), p.getZ() + 4);
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                        display.setItemSlot(slot, p.getItemBySlot(slot).copy());
                    }
                    p.serverLevel().addFreshEntity(display);
                    p.setYRot(0);
                    p.setXRot(0);
                    p.connection.teleport(p.getX(), p.getY(), p.getZ(), 0, 0);
                    helmet.set(EquipmentData.DEMON_TIMER.get(), new DemonTimer(7, 10, false));
                    ItemStack reloaded = ItemStack.parse(p.registryAccess(), helmet.save(p.registryAccess())).orElseThrow();
                    p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    p.getInventory().setItem(0, reloaded);
                }),
                new Action(10, p -> {
                    minecraft.execute(() -> screenshots.take("equipment-demon-armor.png"));
                    ItemStack helmet = p.getInventory().getItem(0);
                    paused = helmet.getOrDefault(EquipmentData.DEMON_TIMER.get(), DemonTimer.EMPTY).equals(new DemonTimer(7, 10, false));
                    p.getInventory().setItem(0, ItemStack.EMPTY);
                    p.setItemSlot(EquipmentSlot.HEAD, helmet);
                }),
                new Action(20, p -> {
                    ItemStack helmet = p.getItemBySlot(EquipmentSlot.HEAD);
                    DemonTimer timer = helmet.getOrDefault(EquipmentData.DEMON_TIMER.get(), DemonTimer.EMPTY);
                    var enchantments = p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    expectTrue("实例数值与倒计时保存重读，卸下暂停后恢复，双诅咒保留", paused
                            && helmet.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY).health() == 21
                            && timer.seconds() == 10 && timer.ticks() > 7
                            && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.BINDING_CURSE), helmet) == 1
                            && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), helmet) == 1);
                    helmet.set(EquipmentData.DEMON_TIMER.get(), new DemonTimer(19, 29, false));
                }),
                new Action(2, p -> {
                    expectTrue("三十秒到期依次清空护甲，旧甲仍在背包且属性贡献撤除", p.getInventory().armor.stream().allMatch(ItemStack::isEmpty)
                            && p.getInventory().contains(new ItemStack(Items.IRON_CHESTPLATE))
                            && PlayerAttributes.value(p, Stat.HEALTH_MAX) == PlayerAttributes.baseValue(p, Stat.HEALTH_MAX));
                }));
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        if (display != null) display.discard();
        player.closeContainer();
        player.removeEffect(MobEffects.INVISIBILITY);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.setData(XiulianAttachments.CULTIVATION, cultivation);
        player.setGameMode(gameMode);
        player.inventoryMenu.broadcastChanges();
    }
}
