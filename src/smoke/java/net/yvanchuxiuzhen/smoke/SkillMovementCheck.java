package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.SkillFlight;
import net.yvanchuxiuzhen.system.skill.SkillMovement;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 身法输入到服务端和本人位移的主路径，以及飞行扣费、严格资源门槛与禁空；不重复旧神通。 */
final class SkillMovementCheck extends ActionCheck {
    private static final String[] TIMERS = {"shenfaanjianW", "shenfaanjianS", "shenfaanjianA", "shenfaanjianD", "shenfatiaoyve", "shenfatick"};
    private static final String[] TIZHI = {"flyxiaohaotick", "lvekongspeed", "superjet", "LevelNumberCorrect"};
    private final double[] oldTizhi = new double[TIZHI.length];
    private SkillState original;
    private CompoundTag originalPersistent;
    private ItemStack chest;
    private GameType gameMode;
    private double power;
    private double rank;
    private double startZ;
    private boolean flying;
    private boolean gliding;
    private boolean mayFly;
    private boolean abilityFlying;

    SkillMovementCheck(List<String> report, String levelId) {
        super("身法轻功", report, levelId);
        script(
                new Action(4, p -> {
                    original = Skills.state(p);
                    originalPersistent = p.getPersistentData().copy();
                    chest = p.getItemBySlot(EquipmentSlot.CHEST).copy();
                    gameMode = p.gameMode.getGameModeForPlayer();
                    power = Cultivation.get(p, CultivationValue.POWER);
                    rank = Cultivation.get(p, CultivationValue.LEVEL_NUMBER);
                    flying = Skills.flying(p);
                    gliding = Cultivation.tizhiBoolean(p, "lvekong");
                    mayFly = p.getAbilities().mayfly;
                    abilityFlying = p.getAbilities().flying;
                    for (int i = 0; i < TIZHI.length; i++) oldTizhi[i] = Cultivation.tizhiDouble(p, TIZHI[i]);
                    for (String key : TIMERS) p.getPersistentData().remove(key);
                    Arena.level(p);
                    p.connection.teleport(p.getX(), p.getY(), p.getZ(), 0, 0);
                    startZ = p.getZ();
                    Skills.setFlying(p, false);
                    Skills.setMovement(p, owned(p, SkillKind.MOVEMENT, "yvanchuxiuzhen:book_012 分形捉影 1 1 无 双击W。 30 60 20 60 1.5 0 0 0 1"));
                    tap("anjian_w");
                }),
                new Action(7, p -> tap("anjian_w")),
                new Action(8, p -> {
                    expectTrue("双击按键经网络使本人实际前移并累加身法冷却", p.getZ() > startZ + .3 && p.getPersistentData().getDouble("shenfatick") > 0 && p.hasEffect(MobEffects.MOVEMENT_SPEED));
                    ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
                    armor.enchant(p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("flyingskill")), 10);
                    p.setItemSlot(EquipmentSlot.CHEST, armor);
                    p.getPersistentData().putDouble("shenfatick", 5);
                    SkillMovement.tick(p);
                    expect("身法附魔十级让每刻冷却减少一点三", 3.7, p.getPersistentData().getDouble("shenfatick"));
                    p.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    p.setGameMode(GameType.SURVIVAL);
                    Skills.setFlight(p, ItemStack.EMPTY);
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 31);
                    Cultivation.set(p, CultivationValue.POWER, 200);
                    Cultivation.setTizhiDouble(p, "LevelNumberCorrect", 0);
                    tap("fanjian");
                }),
                new Action(8, p -> {
                    expectTrue("F 按下只切换一次，元婴无书起飞并同步本人能力", Skills.flying(p) && Skills.flying(minecraft.player) && p.getAbilities().flying && minecraft.player.getAbilities().mayfly);
                    Skills.setFlight(p, owned(p, SkillKind.FLIGHT, "yvanchuxiuzhen:book_012 御风 1 1 无 飞行。 4 1.5 云雾"));
                    ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
                    armor.enchant(p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("flying")), 1);
                    p.setItemSlot(EquipmentSlot.CHEST, armor);
                    Cultivation.set(p, CultivationValue.POWER, 100);
                    Cultivation.setTizhiDouble(p, "flyxiaohaotick", 19);
                    Cultivation.setTizhiDouble(p, "lvekongspeed", 2);
                    Cultivation.setTizhiDouble(p, "superjet", 1.2);
                    Cultivation.setTizhiBoolean(p, "lvekong", true);
                    SkillFlight.tick(p);
                    expect("每秒按附魔、掠空与档位扣费，喷射先清零不另扣", 84.4, Cultivation.get(p, CultivationValue.POWER));
                    expectTrue("掠空叠加档位和一次喷射速度", Math.abs(p.getDeltaMovement().length() - 3.2) < .001 && Cultivation.tizhiDouble(p, "superjet") == 0);
                    Cultivation.set(p, CultivationValue.POWER, 4);
                    SkillFlight.tick(p);
                    expectTrue("灵力等于基础消耗时停止飞行", !Skills.flying(p) && !p.getAbilities().mayfly && !p.getAbilities().flying);
                    Cultivation.set(p, CultivationValue.POWER, 200);
                    Skills.toggleFlight(p);
                    Cultivation.setTizhiBoolean(p, "lvekong", true);
                    Cultivation.setTizhiDouble(p, "lvekongspeed", 2);
                    StatusEffects.add(p, "effect_035", 40, 0);
                }),
                new Action(8, p -> expectTrue("禁空每刻关闭轻功并清空掠空速度且同步本人", !Skills.flying(p) && !Skills.flying(minecraft.player) && !p.getAbilities().mayfly
                        && !Cultivation.tizhiBoolean(p, "lvekong") && Cultivation.tizhiDouble(p, "lvekongspeed") == 0))
        );
    }

    private ItemStack owned(ServerPlayer player, SkillKind kind, String arguments) {
        ItemStack stack = Skills.createBook(kind, List.of(arguments.split(" ")));
        SkillBook book = Skills.book(stack).orElseThrow();
        stack.set(SkillData.SKILL.get(), book.withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    private void tap(String name) {
        minecraft.execute(() -> {
            KeyMapping key = Arrays.stream(minecraft.options.keyMappings).filter(k -> k.getName().equals("key.yvanchuxiuzhen." + name)).findFirst().orElseThrow();
            key.setDown(false);
            key.setDown(true);
            key.setDown(true);
            key.setDown(false);
        });
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        StatusEffects.remove(player, "effect_035");
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        player.setData(SkillData.STATE, original);
        Skills.setFlying(player, flying);
        Cultivation.setTizhiBoolean(player, "lvekong", gliding);
        for (int i = 0; i < TIZHI.length; i++) Cultivation.setTizhiDouble(player, TIZHI[i], oldTizhi[i]);
        Cultivation.set(player, CultivationValue.POWER, power);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, rank);
        for (String key : TIMERS) {
            if (originalPersistent.contains(key)) player.getPersistentData().put(key, originalPersistent.get(key).copy());
            else player.getPersistentData().remove(key);
        }
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setGameMode(gameMode);
        player.getAbilities().mayfly = mayFly;
        player.getAbilities().flying = abilityFlying;
        player.onUpdateAbilities();
    }
}
