package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou020Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 使用已选分身技能的跑参，分开观察击杀与限时移除的死亡事件和收益。 */
final class TemporaryBeastCheck extends ActionCheck {
    private Yaoshou020Entity parent;
    private List<Yaoshou020Entity> children = List.of();
    private int deaths;
    private final Consumer<LivingDeathEvent> listener = event -> {
        if (children.contains(event.getEntity())) deaths++;
    };

    TemporaryBeastCheck(List<String> report, String levelId) {
        super("跑参分身", report, levelId);
        script(new Action(15, this::prepare), new Action(8, this::compare), new Action(1, p -> {
            if (children.size() != 2) return;
            expectTrue("自然倒计时结束不留尸体", children.get(1).isRemoved());
            expect("两种移除只有击杀触发死亡事件", 1, deaths);
        }));
    }

    private void prepare(ServerPlayer p) {
        var level = p.serverLevel();
        BlockPos center = p.blockPosition().above(12);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5,-1,-5), center.offset(5,-1,5))) level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        p.connection.teleport(center.getX()+0.5, center.getY(), center.getZ()+0.5, 0, 0);
        parent = CreatureSetup.<Yaoshou020Entity>entityType("yaoshou_020").get().create(level);
        parent.moveTo(p.getX()+2, p.getY(), p.getZ(), 0, 0);
        parent.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(parent);
        var data = parent.getPersistentData();
        data.putString("jineng", "分身");
        data.putString("jinengjinxing", "准备");
        data.putDouble("entitytick", 39);
        data.putString("gongjizhe", p.getStringUUID().replace("-", ""));
        NeoForge.EVENT_BUS.addListener(listener);
    }

    private void compare(ServerPlayer p) {
        children = p.serverLevel().getEntitiesOfClass(Yaoshou020Entity.class, new AABB(parent.blockPosition()).inflate(32),
                child -> child != parent && child.getPersistentData().getDouble("daojishi") > 0);
        expect("无连字符目标驱动正式技能生成两只分身", 2, children.size());
        if (children.size() != 2) return;
        Relations.addFriendlyTeam(parent, "smoke:after_split");
        for (var child : children) {
            expectTrue("分身可见出生后增加的好友", Relations.friendlyTeams(child).contains("smoke:after_split"));
            expect("分身等级", 3, child.getPersistentData().getDouble("LEVELnumber"));
        }
        double before = Cultivation.get(p, CultivationValue.EXPERIENCE_STAND);
        var killed = children.getFirst();
        killed.hurt(p.damageSources().playerAttack(p), Float.MAX_VALUE);
        expectTrue("玩家击杀立即移除分身并增加待吸收修为", killed.isRemoved() && Cultivation.get(p, CultivationValue.EXPERIENCE_STAND) > before);
        children.get(1).getPersistentData().putDouble("daojishi", 1);
    }

    @Override protected void cleanup(ServerPlayer p) {
        NeoForge.EVENT_BUS.unregister(listener);
        children.forEach(Yaoshou020Entity::discard);
        parent.discard();
    }
}
