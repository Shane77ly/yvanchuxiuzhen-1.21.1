package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 本批箭种的权威选择、附件编解码与同步、实际弹道命中、箭雨归属、掉落和内爆主路径。 */
final class SkillArrowCheck extends ActionCheck {
    private SkillState originalState;
    private final List<ItemStack> inventory = new ArrayList<>();
    private double originalPower;
    private double originalRank;
    private boolean originalArrowSkill;
    private boolean originalFullCharge;
    private boolean originalSneak;
    private IronGolem target;
    private AbstractArrow arrow;
    private long rainLaunchedAt;

    SkillArrowCheck(List<String> report, String levelId) {
        super("技能箭", report, levelId);
        script(
                new Action(8, p -> {
                    originalState = Skills.state(p);
                    originalPower = Cultivation.get(p, CultivationValue.POWER);
                    originalRank = Cultivation.get(p, CultivationValue.LEVEL_NUMBER);
                    originalArrowSkill = Cultivation.tizhiBoolean(p, "arrowskill");
                    originalFullCharge = Cultivation.tizhiBoolean(p, "manxvli");
                    originalSneak = p.isShiftKeyDown();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.getInventory().clearContent();
                    p.setShiftKeyDown(false);
                    Arena.level(p);
                    // 箭雨从命中点上方16格落下，清空通用地面场地之外的空中路径。
                    BlockPos feet = p.blockPosition();
                    for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-8, 7, -8), feet.offset(14, 18, 8))) {
                        p.serverLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                    Skills.setArrow(p, ItemStack.EMPTY, 0);
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 20);
                    Cultivation.set(p, CultivationValue.POWER, 0);
                    expectTrue("库存或灵力不足的箭种请求被拒绝", !Skills.selectArrow(p, 2) && !Skills.selectArrow(p, 3) && Skills.arrowChoice(p) == 0);
                    p.getInventory().setItem(0, new ItemStack(Items.ARROW, 3));
                    expectTrue("选择已有箭种不扣库存", Skills.selectArrow(p, 2) && Skills.arrow(p).is(Items.ARROW) && p.getInventory().getItem(0).getCount() == 3);
                    Cultivation.set(p, CultivationValue.POWER, 100);
                    expectTrue("灵力恰好达到境界乘五可选灵气箭且不扣灵力", Skills.selectArrow(p, 3) && Cultivation.get(p, CultivationValue.POWER) == 100);
                    SkillState changed = Skills.state(p).withPage(2).withCombo(true);
                    var ops = p.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                    SkillState restored = SkillState.CODEC.parse(ops, SkillState.CODEC.encodeStart(ops, changed).getOrThrow()).getOrThrow();
                    p.setData(SkillData.STATE, restored);
                    expectTrue("技能页修改及附件存档编解码保留箭种", restored.arrowChoice() == 3 && restored.arrow().is(item("skill_arrow_02").getItem()));
                    arrow = shot(p, 2, false, false, p.position().add(1, 4, 0), new Vec3(1, 0, 0), .1);
                }),
                new Action(20, p -> {
                    expectTrue("本人客户端收到箭种附件", Skills.arrowChoice(minecraft.player) == 3 && Skills.arrow(minecraft.player).is(item("skill_arrow_02").getItem()));
                }),
                new Action(12, p -> {
                    expectTrue("灵气箭在二十刻后消失", arrow.isRemoved());
                    target(p);
                    shot(p, 5, false, false, p.position().add(1, 1, 0), new Vec3(1, 0, 0), .1);
                }),
                new Action(12, p -> {
                    expectTrue("火箭沿真实弹道命中并附加燃烧状态", target.getHealth() < 100 && StatusEffects.has(target, "effect_023"));
                    clear(p);
                    target(p);
                    arrow = shot(p, 1, true, false, p.position().add(1, 1, 0), new Vec3(1, 0, 0), 1);
                    rainLaunchedAt = p.serverLevel().getGameTime();
                }),
                new Action(8, p -> {
                    List<SkillProjectile> rain = arrows(p).stream().filter(a -> a.getOwner() == target).toList();
                    expectTrue("命中实体的箭雨产生二十支新箭，射手为受击者且未继承箭雨模式；实际数量=" + rain.size()
                                    + "，主箭模式=" + arrow.getPersistentData().getString("jinengName") + "，主箭位置=" + arrow.position()
                                    + "，主箭移除=" + arrow.isRemoved() + "，服务端经过刻数=" + (p.serverLevel().getGameTime() - rainLaunchedAt),
                            rain.size() == 20 && rain.stream().allMatch(a -> a.getPersistentData().getString("jinengName").isEmpty()));
                    clear(p);
                    shot(p, 10, false, false, p.position().add(3, 3, 0), new Vec3(0, -1, 0), .1);
                }),
                new Action(12, p -> {
                    expectTrue("光灵箭命中方块掉落原版光灵箭", p.serverLevel().getEntitiesOfClass(ItemEntity.class, area(p)).stream().anyMatch(e -> e.getItem().is(Items.SPECTRAL_ARROW)));
                    clear(p);
                    target(p);
                    shot(p, 6, true, false, p.position().add(1, 1, 0), new Vec3(1, 0, 0), .1);
                }),
                new Action(8, p -> {
                    float before = target.getHealth();
                    boolean present = StatusEffects.has(target, "skill_arrow_0601");
                    target.invulnerableTime = 0;
                    StatusEffects.remove(target, "skill_arrow_0601");
                    expectTrue("内爆实际命中后加上效果，移除结算二点虚空伤害", present && Math.abs(before - target.getHealth() - 2) < .001);
                    before = target.getHealth();
                    StatusEffects.add(target, "skill_arrow_0601", 200, 0);
                    target.invulnerableTime = 0;
                    StatusEffects.add(target, "skill_arrow_0601", 100, 1);
                    target.invulnerableTime = 0;
                    StatusEffects.remove(target, "skill_arrow_0601");
                    expect("内爆刷新现读新等级造成四点伤害，单独移除仍结算二点", 6, before - target.getHealth());
                    clear(p);
                    arrow = Skills.createArrow(p, 7, 0, 2, false, true, true, false);
                    arrow.setPos(p.position().add(1, 4, 0));
                    arrow.shoot(1, 0, 0, .1f, 0);
                    p.serverLevel().addFreshEntity(arrow);
                }),
                new Action(8, p -> {
                    CompoundTag saved = arrow.saveWithoutId(new CompoundTag());
                    AbstractArrow restored = (AbstractArrow) arrow.getType().create(p.serverLevel());
                    restored.load(saved);
                    expectTrue("震灵在已有穿透上加六、保持无重力并随箭保存", restored.getPierceLevel() == 8 && restored.isNoGravity()
                            && restored.getPersistentData().getString("jinengName").equals("震灵"));
                })
        );
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    private static AABB area(ServerPlayer player) {
        return new AABB(player.blockPosition()).inflate(40);
    }

    private static List<SkillProjectile> arrows(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(SkillProjectile.class, area(player));
    }

    private void clear(ServerPlayer player) {
        arrows(player).forEach(SkillProjectile::discard);
        player.serverLevel().getEntitiesOfClass(ItemEntity.class, area(player)).forEach(ItemEntity::discard);
        if (target != null) target.discard();
    }

    private void target(ServerPlayer player) {
        target = EntityType.IRON_GOLEM.create(player.serverLevel());
        target.setPos(player.position().add(6, 0, 0));
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        player.serverLevel().addFreshEntity(target);
    }

    private static AbstractArrow shot(ServerPlayer player, int kind, boolean control, boolean alt, Vec3 origin, Vec3 direction, double damage) {
        AbstractArrow arrow = Skills.createArrow(player, kind, 0, 0, control, alt, true, false);
        arrow.setBaseDamage(damage);
        arrow.setPos(origin);
        arrow.shoot(direction.x, direction.y, direction.z, 1, 0);
        player.serverLevel().addFreshEntity(arrow);
        return arrow;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clear(player);
        player.setData(SkillData.STATE, originalState);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.setShiftKeyDown(originalSneak);
        Cultivation.set(player, CultivationValue.POWER, originalPower);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, originalRank);
        Cultivation.setTizhiBoolean(player, "arrowskill", originalArrowSkill);
        Cultivation.setTizhiBoolean(player, "manxvli", originalFullCharge);
    }
}
