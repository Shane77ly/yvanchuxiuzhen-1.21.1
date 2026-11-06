package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 灵纹特效（来源 {@code Lingwentexiao2DangShiTiGengXinKeShi}，由主手武器的灵纹附魔命中时生成，见 {@link LingwenHit}）：
 * 一个跟随施放者的灵纹实体，{@code lingwennum} 记剩余的灵纹数（至多 6），按数量播放 {@code two}–{@code six} 动画；
 * 每 100 刻减一枚，每 20 刻检查 10 格内最近的非友方实体，找到时以主人的眼睛为起点、沿主人的视线射出一枚灵纹弹
 * （散布 20，不是朝目标）并减一枚，数量归零后消失。
 */
final class LingwenEntities {
    static final String ID = "lingwentexiao_2";
    private static final String[] NUMBERS = {"", "", "two", "three", "four", "five", "six"};
    private static final int DECAY = 100;
    private static final int SCAN = 20;
    private static final float SCATTER = 20;

    private LingwenEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put(ID, new Rune());
    }

    private static final class Rune implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag data = self.getPersistentData();
            int count = data.getInt("lingwennum");
            if (count >= 2 && count < NUMBERS.length) {
                self.setAnimation("animation.lingwen2." + NUMBERS[count]);
            }
            ServerLevel level = (ServerLevel) self.level();
            Entity owner = self.attackerEntity();
            if (owner != null) {
                self.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
            data.putDouble("tick", data.getDouble("tick") + 1);
            if (data.getDouble("tick") >= DECAY) {
                count--;
                data.putInt("lingwennum", count);
                if (count <= 0) {
                    self.discard();
                }
                data.putDouble("tick", 0);
            }
            data.putDouble("tick2", data.getDouble("tick2") + 1);
            if (data.getDouble("tick2") >= SCAN) {
                for (Entity other : ProjectileSupport.around(level, self.position(), 10)) {
                    if (!ProjectileSupport.friendly(self, other)) {
                        if (owner != null) {
                            ProjectileSupport.shoot(level, owner, "projectile_lingwentexiao", new Vec3(owner.getX(), owner.getEyeY() - 0.1, owner.getZ()),
                                    owner.getLookAngle(), 0.1, 0, 0, SCATTER);
                        }
                        count--;
                        data.putInt("lingwennum", count);
                        break;
                    }
                }
                if (count <= 0) {
                    self.discard();
                }
                data.putDouble("tick2", 0);
            }
        }
    }
}
