package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.projectile.Fuchen;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/** 弓与拂尘的五项主路径检查；弓条件分支显式注入来源无人写入的标记，不把它算作自然右键可用。 */
final class EquipmentWeaponCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private CultivationData cultivation;
    private SkillState skill;
    private CompoundTag persistent;
    private ItemStack hossu;
    private IronGolem enemy;
    private IronGolem ally;
    private int selectedSlot;
    private boolean control;
    private boolean alt;
    private boolean sneak;
    private boolean swingPassed;
    private boolean pausePassed;

    EquipmentWeaponCheck(List<String> report, String levelId) {
        super("弓与拂尘", report, levelId);
        script(
                new Action(12, p -> {
                    cultivation = p.getData(XiulianAttachments.CULTIVATION);
                    skill = Skills.state(p);
                    persistent = p.getPersistentData().copy();
                    selectedSlot = p.getInventory().selected;
                    control = ModifierKeys.ctrl(p);
                    alt = ModifierKeys.alt(p);
                    sneak = p.isShiftKeyDown();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.getInventory().clearContent();
                    p.setData(XiulianAttachments.CULTIVATION, new CultivationData());
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 5);
                    Cultivation.set(p, CultivationValue.EXPERIENCE_MAX, 5000);
                    Cultivation.set(p, CultivationValue.POWER, 100);
                    PlayerAttributes.setContribution(p, "smoke:equipment", new Contribution(Layer.BASE, Map.of(Stat.ATTACK, 8.0, Stat.POWER_MAX, 180.0)));
                    Arena.level(p);
                    p.getInventory().selected = 0;
                    p.connection.teleport(p.getX(), p.getY(), p.getZ(), -90, -60);
                    hossu = item("weaponhossu_09");
                    p.getInventory().setItem(0, hossu);
                    p.swing(InteractionHand.MAIN_HAND);
                    p.swing(InteractionHand.MAIN_HAND);
                    List<Fuchen> shots = p.serverLevel().getEntitiesOfClass(Fuchen.class, p.getBoundingBox().inflate(10));
                    swingPassed = shots.size() == 1 && Cultivation.get(p, CultivationValue.POWER) == 88
                            && Cultivation.tizhiDouble(p, "fuchenshiyongtick") == 20 && hossu.getDamageValue() == 1
                            && shots.getFirst().getPierceLevel() == 10 && Math.abs(shots.getFirst().getDeltaMovement().length() - .5) < .00001;
                    p.getInventory().setItem(0, ItemStack.EMPTY);
                }),
                new Action(5, p -> {
                    pausePassed = Cultivation.tizhiDouble(p, "fuchenshiyongtick") == 20;
                    p.getInventory().setItem(0, hossu);
                }),
                new Action(2, p -> {
                    double remaining = Cultivation.tizhiDouble(p, "fuchenshiyongtick");
                    expectTrue("挥动只生成一次、扣十二灵力与一点耐久；切走暂停，选中后冷却继续", swingPassed && pausePassed && remaining < 20 && remaining > 0);
                    clear(p);
                    p.getInventory().setItem(0, item("fuchen"));
                    ItemStack hidden = p.getMainHandItem();
                    double power = Cultivation.get(p, CultivationValue.POWER);
                    hidden.getItem().use(p.level(), p, InteractionHand.MAIN_HAND);
                    p.releaseUsingItem();
                    Fuchen projectile = p.serverLevel().getEntitiesOfClass(Fuchen.class, p.getBoundingBox().inflate(10)).getFirst();
                    boolean launch = Math.abs(projectile.getDeltaMovement().length() - 2) < .00001 && projectile.getPierceLevel() == 0
                            && hidden.getCount() == 1 && Cultivation.get(p, CultivationValue.POWER) == power;
                    enemy = golem(p, p.position().add(6, 0, 0));
                    ally = golem(p, p.position().add(6, 0, 1));
                    Relations.copyAllegiance(p, ally);
                    projectile.setPos(enemy.position().add(1.7, 1, 0));
                    projectile.setDeltaMovement(Vec3.ZERO);
                    projectile.tick();
                    float first = enemy.getHealth();
                    enemy.invulnerableTime = 0;
                    projectile.tick();
                    boolean once = first < 100 && enemy.getHealth() == first && ally.getHealth() == 100;
                    for (int i = 0; i < 8; i++) projectile.tick();
                    expectTrue("隐藏物品松开发射且不扣物品或灵力；范围只伤敌方一次，十刻消散", launch && once && projectile.isRemoved());
                    clear(p);
                    p.getInventory().setItem(0, item("weaponspeedbow_01"));
                    p.getInventory().setItem(1, new ItemStack(Items.ARROW, 3));
                    Skills.selectArrow(p, 2);
                    p.getPersistentData().remove("synchronizedata");
                    Cultivation.setTizhiDouble(p, "lagongtick", 0);
                    p.getMainHandItem().getItem().use(p.level(), p, InteractionHand.MAIN_HAND);
                }),
                new Action(2, p -> {
                    expectTrue("来源右键空过程不会产生 rightclicking、蓄力或弹体", !p.getPersistentData().getCompound("synchronizedata").getBoolean("rightclicking")
                            && Cultivation.tizhiDouble(p, "lagongtick") == 0 && arrows(p).isEmpty());
                    injectedBow(p);
                }),
                new Action(2, p -> {
                    clear(p);
                    p.setShiftKeyDown(false);
                    ModifierKeys.update(p, false, false);
                    p.getInventory().setItem(0, item("weaponlongbow_01"));
                    p.getInventory().setItem(1, item("skill_arrow_02"));
                    Cultivation.set(p, CultivationValue.POWER, 20);
                    Skills.setArrow(p, item("skill_arrow_02"), 3);
                    Cultivation.setTizhiDouble(p, "lagongtick", 60);
                    tickEquipment(p);
                    AbstractArrow arrow = arrows(p).getFirst();
                    expectTrue("人工蓄力：库存灵气箭允许余额不足时发射，扣至负五并只清箭选择物品",
                            BuiltInRegistries.ENTITY_TYPE.getKey(arrow.getType()).getPath().equals("projectile_skill_arrow_02")
                                    && arrow.isSilent() && Math.abs(arrow.getBaseDamage() - .1) < .00001
                                    && Cultivation.get(p, CultivationValue.POWER) == -5 && Skills.arrow(p).isEmpty()
                                    && Skills.arrowChoice(p) == 3 && p.getInventory().getItem(1).getCount() == 1);
                    clear(p);
                })
        );
    }

    private void injectedBow(ServerPlayer player) {
        CompoundTag input = new CompoundTag();
        input.putBoolean("rightclicking", true);
        player.getPersistentData().put("synchronizedata", input);
        player.setShiftKeyDown(true);
        ItemStack speed = player.getMainHandItem();
        speed.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.QUICK_CHARGE), 2);
        tickEquipment(player);
        boolean speedCharge = Cultivation.tizhiDouble(player, "lagongtick") == 6;
        ItemStack longbow = item("weaponlongbow_01");
        longbow.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.QUICK_CHARGE), 2);
        longbow.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("qingyv")), 2);
        player.getInventory().setItem(0, longbow);
        Cultivation.setTizhiDouble(player, "lagongtick", 0);
        tickEquipment(player);
        boolean longCharge = Cultivation.tizhiDouble(player, "lagongtick") == 3;
        Cultivation.setTizhiDouble(player, "lagongtick", 100);
        tickEquipment(player);
        input.putBoolean("rightclicking", false);
        ModifierKeys.update(player, true, true);
        tickEquipment(player);
        Arrow arrow = (Arrow) arrows(player).getFirst();
        CompoundTag saved = arrow.saveWithoutId(new CompoundTag());
        Arrow restored = EntityType.ARROW.create(player.serverLevel());
        restored.load(saved);
        expectTrue("人工标记：两弓蓄力、普通箭伤害与七格速度、库存耐久、固定击退和五级穿透存档", speedCharge && longCharge
                && Math.abs(arrow.getBaseDamage() - 4.5) < .00001 && Math.abs(arrow.getDeltaMovement().length() - 7) < .00001
                && player.getInventory().getItem(1).getCount() == 2 && longbow.getDamageValue() == 1
                && restored.getType() == EntityType.ARROW && restored.getPierceLevel() == 5
                && Enchants.level(player, restored.getWeaponItem(), Enchantments.PUNCH) == 1);
        clear(player);
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    private static void tickEquipment(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
    }

    private static List<AbstractArrow> arrows(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(30));
    }

    private static IronGolem golem(ServerPlayer player, Vec3 position) {
        IronGolem target = EntityType.IRON_GOLEM.create(player.serverLevel());
        target.setPos(position);
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        player.serverLevel().addFreshEntity(target);
        return target;
    }

    private void clear(ServerPlayer player) {
        arrows(player).forEach(Entity::discard);
        if (enemy != null) enemy.discard();
        if (ally != null) ally.discard();
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clear(player);
        player.stopUsingItem();
        PlayerAttributes.clearContribution(player, "smoke:equipment");
        player.setData(XiulianAttachments.CULTIVATION, cultivation);
        player.setData(SkillData.STATE, skill);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.getInventory().selected = selectedSlot;
        player.setShiftKeyDown(sneak);
        ModifierKeys.update(player, control, alt);
        for (String key : List.copyOf(player.getPersistentData().getAllKeys())) player.getPersistentData().remove(key);
        player.getPersistentData().merge(persistent);
    }
}
