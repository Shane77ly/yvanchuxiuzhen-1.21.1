package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.system.combat.content.ColoredLightningBolt;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.entity.DamageArea;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 共享落雷的实际命令、客户端颜色同步和技能生成入口。 */
final class LightningCheck extends SystemCheck {
    private final Consumer<String> shot;
    private SkillState state;
    private SkillCooldowns cooldowns;
    private ItemStack hand;
    private double power;
    private Pig inside;
    private Pig outside;
    private float afterDamage;
    private boolean clientColor;
    private boolean captured;
    private final List<ColoredLightningBolt> castBolts = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> observeCast = event -> {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ColoredLightningBolt bolt) castBolts.add(bolt);
    };

    LightningCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("共享落雷", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                state = Skills.state(player);
                cooldowns = player.getData(SkillData.COOLDOWNS);
                hand = player.getMainHandItem().copy();
                power = Cultivation.get(player, CultivationValue.POWER);
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
            });
            return true;
        }
        if (step == 1) {
            if (local < 10) return false;
            onServer(player -> {
                Vec3 center = player.position().add(-2, 0.5, 5);
                inside = pig(player, center.add(0.8, -0.5, 0));
                outside = pig(player, center.add(2.8, -0.5, 0));
                inside.getRandom().setSeed(1);
                player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPosition(center),
                        "lightninglanding 4 无 1 占位 4 红色 独立");
                List<ColoredLightningBolt> bolts = bolts(player);
                afterDamage = inside.getHealth();
                expectTrue("命令先生成居中红色闪电，并只对直径四格范围内目标造成独立雷伤", bolts.size() == 1
                        && bolts.getFirst().color() == 0xffff0000
                        && bolts.getFirst().position().equals(Vec3.atBottomCenterOf(BlockPos.containing(center)))
                        && afterDamage < inside.getMaxHealth() && outside.getHealth() == outside.getMaxHealth());
            });
            return true;
        }
        if (step == 2) {
            if (!clientColor) {
                for (var entity : minecraft.level.entitiesForRendering()) {
                    if (entity instanceof ColoredLightningBolt bolt && bolt.color() == 0xffff0000) {
                        clientColor = minecraft.getEntityRenderDispatcher().getRenderer(bolt).getClass().getSimpleName().equals("ColoredLightningRenderer");
                        break;
                    }
                }
            } else if (!captured) {
                shot.accept("lightning-red.png");
                captured = true;
            }
            if (local < 8) return false;
            onServer(player -> {
                expectTrue("颜色实际同步并装配原生渲染，显示闪电没有再追加原生雷击伤害", clientColor && inside.getHealth() == afterDamage);
                inside.discard();
                outside.discard();
                bolts(player).forEach(ColoredLightningBolt::discard);
                checkTeamField(player, "");
                checkTeamField(player, "占位");
                for (String owner : List.of("", "无", "非法", "00000000-0000-0000-0000-000000000001", player.getStringUUID())) {
                    checkSavedField(player, owner);
                }
                checkTeamField(player, "原 4 金色 独立");
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                player.setData(SkillData.STATE, SkillState.EMPTY);
                Cultivation.set(player, CultivationValue.POWER, 1000);
                prepareCast(player, "五雷轰顶");
                NeoForge.EVENT_BUS.addListener(observeCast);
            });
            return true;
        }
        if (step == 3) {
            if (local < 10) return false;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            return true;
        }
        if (step == 4) {
            if (local < 10) return false;
            onServer(player -> {
                List<ColoredLightningBolt> five = List.copyOf(castBolts);
                castBolts.clear();
                Set<BlockPos> positions = five.stream().map(ColoredLightningBolt::blockPosition).collect(Collectors.toSet());
                boolean cross = five.size() == 5 && five.stream().allMatch(bolt -> bolt.color() == -1)
                        && positions.size() == 5 && positions.stream().anyMatch(center -> Set.of(center, center.east(3), center.west(3), center.south(3), center.north(3)).equals(positions));
                expectTrue("客户端自然右键经服务端入口放出十字五雷，生成=" + five.size() + "，落点=" + positions, cross);
                five.forEach(ColoredLightningBolt::discard);
                prepareCast(player, "天雷术");
            });
            return true;
        }
        if (step == 5) {
            if (local < 10) return false;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            return true;
        }
        if (step == 6) {
            if (local < 10) return false;
            onServer(player -> {
                NeoForge.EVENT_BUS.unregister(observeCast);
                expectTrue("客户端自然右键经服务端入口放出单道天雷", castBolts.size() == 1);
                bolts(player).forEach(ColoredLightningBolt::discard);
                Vec3 at = player.position().add(0, 0, 6);
                Skills.showDamageArea(player, at, "animation.shanghaifanwei.shiliuge3", "控雷阵", 20, 4);
                DamageArea area = player.serverLevel().getEntitiesOfClass(DamageArea.class, new AABB(at, at).inflate(1)).getFirst();
                area.setTimer(19);
                area.baseTick();
                expectTrue("控雷阵结束时落雷并移除提示", area.isRemoved() && bolts(player).size() == 1);
                bolts(player).forEach(ColoredLightningBolt::discard);
                player.setData(SkillData.STATE, state);
                player.setData(SkillData.COOLDOWNS, cooldowns);
                player.setItemInHand(InteractionHand.MAIN_HAND, hand);
                Cultivation.set(player, CultivationValue.POWER, power);
            });
            finish();
            return true;
        }
        return false;
    }

    private void checkTeamField(ServerPlayer player, String team) {
        Vec3 at = player.position().add(-2, .5, 5);
        for (boolean direct : List.of(false, true)) {
            Pig victim = pig(player, at.add(.8, -.5, 0));
            victim.getRandom().setSeed(1);
            if (direct) Lightning.cast(player.serverLevel(), at, player, 4, 1, team, 4, "红色", true);
            else player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPosition(at),
                    "lightninglanding 4 " + player.getStringUUID() + " 1 " + team + " 4 红色 独立");
            List<ColoredLightningBolt> spawned = bolts(player);
            int expected = team.isEmpty() ? -1 : team.equals("占位") ? 0xffff0000 : 0xffffcc66;
            expectTrue((direct ? "直接落雷" : "手动命令") + "队伍[" + team + "]的颜色与范围", spawned.size() == 1
                    && spawned.getFirst().color() == expected && (victim.getHealth() < victim.getMaxHealth()) == !team.isEmpty());
            spawned.forEach(ColoredLightningBolt::discard);
            victim.discard();
        }
    }

    private void checkSavedField(ServerPlayer player, String owner) {
        Vec3 at = player.position().add(-2, .5, 5);
        float referenceHealth = 0;
        for (boolean direct : List.of(false, true)) {
            Pig victim = pig(player, at.add(.8, -.5, 0));
            victim.getRandom().setSeed(1);
            if (direct) Lightning.castSaved(player.serverLevel(), at, owner, 4, 1, "占位", 4, "红色", true);
            else player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPosition(at),
                    "lightninglanding 4 " + owner + " 1 占位 4 红色 独立");
            if (!direct) referenceHealth = victim.getHealth();
            List<ColoredLightningBolt> spawned = bolts(player);
            expectTrue((direct ? "存档落雷" : "手动命令") + "主人[" + owner + "]的落点、颜色和伤害", spawned.size() == 1
                    && spawned.getFirst().position().equals(Vec3.atBottomCenterOf(BlockPos.containing(at)))
                    && spawned.getFirst().color() == (owner.isEmpty() ? -1 : 0xffff0000)
                    && victim.getHealth() == referenceHealth && (victim.getHealth() < victim.getMaxHealth()) == (owner.equals("无") || owner.equals(player.getStringUUID())));
            spawned.forEach(ColoredLightningBolt::discard);
            victim.discard();
        }
    }

    private static Pig pig(ServerPlayer player, Vec3 position) {
        Pig pig = EntityType.PIG.create(player.serverLevel());
        pig.setNoAi(true);
        pig.setPos(position);
        player.serverLevel().addFreshEntity(pig);
        return pig;
    }

    private static List<ColoredLightningBolt> bolts(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ColoredLightningBolt.class, player.getBoundingBox().inflate(32));
    }

    private static void prepareCast(ServerPlayer player, String name) {
        ItemStack book = Skills.createBook(SkillKind.SPELL, List.of(("yvanchuxiuzhen:book_003 " + name
                + " 1 1 火 x 无 3 1 0 0 1 0 0 0 0 0 100 0 0 0 0 0").split(" ")));
        book.set(SkillData.SKILL.get(), Skills.book(book).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
        SkillSelection.clear(player);
        Skills.setSpell(player, 1, book);
        SkillSelection.select(player, 1);
        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
    }
}
