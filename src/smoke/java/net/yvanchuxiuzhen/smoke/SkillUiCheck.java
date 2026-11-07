package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillMenu;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 只验证本批界面到服务端的主路径、格挡显示同步和自爆的死亡后延迟，不重跑旧神通。 */
final class SkillUiCheck extends ActionCheck {
    private static final String CONTRIBUTION = "smoke:skill_ui";
    private static final String[] TIZHI = {"LevelNumberCorrect", "lagongtick", "zibaonumber", "zibaokouchu"};
    private static final CultivationValue[] VALUES = {CultivationValue.LEVEL_NUMBER, CultivationValue.POWER, CultivationValue.SOUL, CultivationValue.LIFETIME};
    private final double[] oldTizhi = new double[TIZHI.length];
    private final double[] oldValues = new double[VALUES.length];
    private final List<ItemStack> inventory = new ArrayList<>();
    private SkillState original;
    private SkillCooldowns cooldowns;
    private GameType gameMode;
    private SkillEntity guard;
    private BlockPos marker;

    SkillUiCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("技能界面", report, levelId);
        script(
                new Action(10, p -> {
                    original = Skills.state(p);
                    cooldowns = p.getData(SkillData.COOLDOWNS);
                    gameMode = p.gameMode.getGameModeForPlayer();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    for (int i = 0; i < TIZHI.length; i++) oldTizhi[i] = Cultivation.tizhiDouble(p, TIZHI[i]);
                    for (int i = 0; i < VALUES.length; i++) oldValues[i] = Cultivation.get(p, VALUES[i]);
                    p.getInventory().clearContent();
                    Arena.level(p);
                    p.setData(SkillData.STATE, SkillState.EMPTY);
                    p.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    PlayerAttributes.setContribution(p, CONTRIBUTION, new Contribution(Layer.BASE, Map.of(Stat.POWER_MAX, 1000.0, Stat.SOUL_MAX, 1000.0)));
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 11);
                    Cultivation.set(p, CultivationValue.POWER, 200);
                    Cultivation.set(p, CultivationValue.SOUL, 200);
                    Cultivation.setTizhiDouble(p, "LevelNumberCorrect", 0);
                    ItemStack spell = item("book_003");
                    spell.set(SkillData.SKILL.get(), new SkillBook(SkillKind.SPELL, "火球术", 1, 1, "无", "", Optional.of(p.getUUID()),
                            p.getName().getString(), Map.of("Firecolddown", 2.0), Map.of()));
                    Skills.setSpell(p, 1, spell);
                    p.getInventory().add(new ItemStack(Items.ARROW, 8));
                    p.getInventory().add(new ItemStack(Items.SPECTRAL_ARROW));
                    for (int i = 1; i <= 9; i++) p.getInventory().add(item("skill_arrow_%02d".formatted(i)));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(8, p -> {
                    // 首步可能立即结束，另起一步后再发按键，给网络和客户端界面留下完整等待时间。
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> {
                    expectTrue("G键经网络打开真实无槽技能界面", p.containerMenu instanceof SkillMenu && minecraft.screen instanceof SkillScreen && p.containerMenu.slots.isEmpty());
                    click("上一页");
                    click("连招");
                }),
                new Action(8, p -> {
                    expectTrue("翻页和连招按钮写入服务端并同步本人", Skills.state(p).page() == 2 && Skills.state(minecraft.player).page() == 2 && Skills.state(p).combo());
                    click("下一页");
                    click("技能 1");
                }),
                new Action(8, p -> {
                    expectTrue("技能按钮选择当前页并关闭菜单", Skills.state(p).selectedSlot() == 1 && Skills.state(minecraft.player).selectedSlot() == 1 && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> {
                    minecraft.execute(() -> screenshots.take("skill-menu"));
                    click("箭种 2");
                }),
                new Action(8, p -> {
                    expectTrue("轮盘按钮选择普通箭并同步本人", Skills.arrowChoice(p) == 2 && Skills.arrowChoice(minecraft.player) == 2 && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> click("召唤丹火")),
                new Action(8, p -> {
                    ItemStack fire = p.getInventory().items.stream().filter(s -> s.is(item("danhuo").getItem())).findFirst().orElseThrow();
                    expectTrue("丹火按钮扣费并给出本人和火力数据", Cultivation.get(p, CultivationValue.POWER) == 178
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getDouble("power") == 22
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getString("ManaAttackUUID").equals(p.getStringUUID()) && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> click("神识扫描")),
                new Action(8, p -> {
                    expectTrue("扫描按钮扣除神识并关闭菜单", Cultivation.get(p, CultivationValue.SOUL) == 178 && !(p.containerMenu instanceof SkillMenu));
                    p.setData(SkillData.COOLDOWNS, new SkillCooldowns(List.of(200.0, 400.0, 600.0, 800.0, 1000.0, 1200.0)));
                    Cultivation.setTizhiDouble(p, "lagongtick", 85);
                    p.getPersistentData().putDouble("canjianjishicolddown", 0);
                    key("canjian", true);
                    guard = SkillSetup.<SkillEntity>entityType("gedang").get().create(p.serverLevel());
                    guard.setPos(p.getX() + 2, p.getY(), p.getZ());
                    p.serverLevel().addFreshEntity(guard);
                }),
                new Action(12, p -> {
                    expectTrue("C按下的格挡计时同步给本人", Skills.guardRemaining(p) > 0 && Skills.guardRemaining(minecraft.player) > 0);
                    minecraft.execute(() -> screenshots.take("skill-hud"));
                    key("canjian", false);
                }),
                new Action(8, p -> {
                    expectTrue("C松开结束显示，格挡实体到八刻移除", Skills.guardRemaining(p) == 0 && Skills.guardRemaining(minecraft.player) == 0 && guard.isRemoved());
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 21);
                    Cultivation.set(p, CultivationValue.LIFETIME, 10000);
                    Cultivation.setTizhiDouble(p, "zibaonumber", 0);
                    Cultivation.setTizhiDouble(p, "zibaokouchu", 0);
                    p.setGameMode(GameType.SURVIVAL);
                    marker = p.blockPosition().offset(2, 0, 0);
                    p.serverLevel().setBlock(marker, Blocks.STONE.defaultBlockState(), 3);
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(50, p -> click("自爆")),
                new Action(100, p -> {
                    expectTrue("自爆先死亡并登记寿元代价，死亡后仍执行延迟爆炸", !p.isAlive() && Cultivation.tizhiDouble(p, "zibaonumber") == 1
                            && Cultivation.tizhiDouble(p, "zibaokouchu") == 3650 && Cultivation.get(p, CultivationValue.LIFETIME) <= 6350
                            && p.serverLevel().isEmptyBlock(marker));
                    minecraft.execute(() -> minecraft.player.respawn());
                })
        );
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    private void key(String name, boolean down) {
        minecraft.execute(() -> Arrays.stream(minecraft.options.keyMappings).filter(k -> k.getName().equals("key.yvanchuxiuzhen." + name))
                .findFirst().orElseThrow().setDown(down));
    }

    private void click(String name) {
        minecraft.execute(() -> minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow().onPress());
    }

    @Override
    protected void cleanup(ServerPlayer p) {
        p.closeContainer();
        p.setGameMode(gameMode);
        p.getInventory().clearContent();
        for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        p.getInventory().setChanged();
        p.setData(SkillData.STATE, original);
        p.setData(SkillData.COOLDOWNS, cooldowns);
        PlayerAttributes.clearContribution(p, CONTRIBUTION);
        for (int i = 0; i < VALUES.length; i++) Cultivation.set(p, VALUES[i], oldValues[i]);
        for (int i = 0; i < TIZHI.length; i++) Cultivation.setTizhiDouble(p, TIZHI[i], oldTizhi[i]);
        p.getPersistentData().remove("canjianjishi");
        p.getPersistentData().remove("canjianjishicolddown");
        if (guard != null && !guard.isRemoved()) guard.discard();
    }
}
