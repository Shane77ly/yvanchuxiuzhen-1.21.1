package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;

/** 黑风风刃的目标解析回归：使用服务端已加载玩家与未加入世界的探针，核对来源的暂停、恢复及保存重读。 */
final class WindBladeTargetCheck extends ActionCheck {
    private static final String ID = "skillwind_06";
    private static final UUID MISSING_TARGET = new UUID(0, 0);

    WindBladeTargetCheck(List<String> report, String levelId) {
        super("风刃目标", report, levelId);
        script(new Action(1, this::checkTargets));
    }

    private void checkTargets(ServerPlayer player) {
        if (player.serverLevel().getEntity(MISSING_TARGET) != null) {
            fail("无目标场景的零 UUID 已被实体占用，停止依赖检查");
            return;
        }
        SkillEntity standard = create(player);
        standard.baseTick();
        expectTrue("标准 UUID 首刻进入追踪且累计一刻", standard.getPersistentData().getDouble("tick") == 1
                && standard.getPersistentData().getString("jinengjieduan").equals("追踪"));
        standard.discard();

        SkillEntity undashed = create(player);
        undashed.getPersistentData().putString("mubiaoUUID", player.getStringUUID().replace("-", ""));
        undashed.baseTick();
        undashed.baseTick();
        expectTrue("无连字符 UUID 可解析到同一玩家并继续追踪", undashed.getPersistentData().getDouble("tick") == 2
                && undashed.getPersistentData().getString("jinengjieduan").equals("追踪")
                && undashed.getDeltaMovement().horizontalDistance() > 0.79);
        undashed.discard();

        SkillEntity paused = create(player);
        CompoundTag state = paused.getPersistentData();
        state.putDouble("tick", 7);
        state.putDouble("tick02", 3);
        state.putString("jinengjieduan", "追踪");
        Vec3 movement = new Vec3(0.1, 0.2, 0.3);
        paused.setDeltaMovement(movement);
        for (String target : List.of("", "not-a-uuid", MISSING_TARGET.toString())) {
            state.putString("mubiaoUUID", target);
            paused.baseTick();
            expectTrue("空值、无效值或未加载目标暂停技能计时与追踪：" + target, state.getDouble("tick") == 7
                    && state.getDouble("tick02") == 3 && state.getString("jinengjieduan").equals("追踪")
                    && paused.getDeltaMovement().equals(movement) && !paused.isRemoved());
        }
        state.putString("mubiaoUUID", "not-a-uuid");
        SkillEntity restored = SkillSetup.<SkillEntity>entityType(ID).get().create(player.serverLevel());
        restored.load(paused.saveWithoutId(new CompoundTag()));
        paused.discard();
        restored.baseTick();
        CompoundTag reloaded = restored.getPersistentData();
        expectTrue("NBT 重读后无效目标继续暂停且保留技能状态", reloaded.getDouble("tick") == 7
                && reloaded.getDouble("tick02") == 3 && reloaded.getString("jinengjieduan").equals("追踪")
                && reloaded.getString("mubiaoUUID").equals("not-a-uuid")
                && restored.getDeltaMovement().equals(movement) && !restored.isRemoved());
        reloaded.putString("mubiaoUUID", player.getStringUUID().replace("-", ""));
        restored.baseTick();
        expectTrue("目标恢复后续接既有计时并重新计算追踪速度", reloaded.getDouble("tick") == 8
                && reloaded.getDouble("tick02") == 4 && reloaded.getString("jinengjieduan").equals("追踪")
                && restored.getDeltaMovement().horizontalDistance() > 0.79);
        restored.discard();

        SkillEntity expiry = create(player);
        expiry.getPersistentData().putDouble("tick", 199);
        expiry.baseTick();
        expectTrue("有效目标下累计二百刻移除并清零", expiry.isRemoved()
                && expiry.getPersistentData().getDouble("tick") == 0);

        SkillEntity spin = create(player);
        spin.getPersistentData().putString("jinengjieduan", "自旋");
        spin.getPersistentData().putDouble("tick02", 12);
        spin.baseTick();
        expectTrue("自旋第十三刻仍保留", !spin.isRemoved() && spin.getPersistentData().getDouble("tick02") == 13);
        spin.baseTick();
        expectTrue("自旋第十四刻按来源移除并清零", spin.isRemoved() && spin.getPersistentData().getDouble("tick02") == 0);
    }

    private static SkillEntity create(ServerPlayer player) {
        SkillEntity blade = SkillEntities.summon(player.serverLevel(), player, ID, "风", 3, 1, "");
        blade.setPos(player.getX() + 20, player.getY() + 1, player.getZ());
        blade.getPersistentData().putString("mubiaoUUID", player.getStringUUID());
        blade.getPersistentData().putDouble("endtick", 200);
        return blade;
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        // 探针仅在单个服务端动作中使用，没有加入世界或改写玩家状态。
    }
}
