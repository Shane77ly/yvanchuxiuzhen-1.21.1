package net.yvanchuxiuzhen.system.creature.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * 生物系统的实体持久化运行时状态：包含主人与灵兽袋身份、性格加成、AI 仇恨与技能调度槽、
 * 技能位移目标坐标以及计数与状态开关，通过 {@link CreatureData#STATE} 随实体保存。
 */
public final class CreatureState {
    public record SkillSlot(String name, double timer, double weight) {
        public static final SkillSlot EMPTY = new SkillSlot("", 0, 0);
        public static final Codec<SkillSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("name", "").forGetter(SkillSlot::name),
                Codec.DOUBLE.optionalFieldOf("timer", 0.0).forGetter(SkillSlot::timer),
                Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(SkillSlot::weight)
        ).apply(instance, SkillSlot::new));

        public SkillSlot {
            name = name == null ? "" : name;
        }
    }

    private record IdentitySnapshot(
            String masterUuid, String bagUuid, double tameNum, double harvestCount, boolean beastCoreDropped,
            double additionalExperience, double recoveryTicks, double experienceStand, double experienceRate,
            double experienceHinder, String character, double characterDef, double characterAtt,
            double characterInterval, double characterResist
    ) {
        static final IdentitySnapshot EMPTY = new IdentitySnapshot("", "", 0, 0, false, 0, 0, 0, 0, 0, "", 0, 0, 0, 0);
        static final Codec<IdentitySnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("master_uuid", "").forGetter(IdentitySnapshot::masterUuid),
                Codec.STRING.optionalFieldOf("bag_uuid", "").forGetter(IdentitySnapshot::bagUuid),
                Codec.DOUBLE.optionalFieldOf("tame_num", 0.0).forGetter(IdentitySnapshot::tameNum),
                Codec.DOUBLE.optionalFieldOf("harvest_count", 0.0).forGetter(IdentitySnapshot::harvestCount),
                Codec.BOOL.optionalFieldOf("beast_core_dropped", false).forGetter(IdentitySnapshot::beastCoreDropped),
                Codec.DOUBLE.optionalFieldOf("additional_experience", 0.0).forGetter(IdentitySnapshot::additionalExperience),
                Codec.DOUBLE.optionalFieldOf("recovery_ticks", 0.0).forGetter(IdentitySnapshot::recoveryTicks),
                Codec.DOUBLE.optionalFieldOf("experience_stand", 0.0).forGetter(IdentitySnapshot::experienceStand),
                Codec.DOUBLE.optionalFieldOf("experience_rate", 0.0).forGetter(IdentitySnapshot::experienceRate),
                Codec.DOUBLE.optionalFieldOf("experience_hinder", 0.0).forGetter(IdentitySnapshot::experienceHinder),
                Codec.STRING.optionalFieldOf("character", "").forGetter(IdentitySnapshot::character),
                Codec.DOUBLE.optionalFieldOf("character_def", 0.0).forGetter(IdentitySnapshot::characterDef),
                Codec.DOUBLE.optionalFieldOf("character_att", 0.0).forGetter(IdentitySnapshot::characterAtt),
                Codec.DOUBLE.optionalFieldOf("character_interval", 0.0).forGetter(IdentitySnapshot::characterInterval),
                Codec.DOUBLE.optionalFieldOf("character_resist", 0.0).forGetter(IdentitySnapshot::characterResist)
        ).apply(instance, IdentitySnapshot::new));
    }

    private record AiSnapshot(
            String attackerUuid, String tempAttackerUuid, String skill, String phase, double tick, double tick2,
            String queuedSkill, double queuedTimer, double candidateCount, boolean interrupted,
            List<SkillSlot> candidates, List<String> skillChoices
    ) {
        static final AiSnapshot EMPTY = new AiSnapshot("", "", "", "", 0, 0, "", 0, 0, false, List.of(), List.of());
        static final Codec<AiSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("attacker_uuid", "").forGetter(AiSnapshot::attackerUuid),
                Codec.STRING.optionalFieldOf("temp_attacker_uuid", "").forGetter(AiSnapshot::tempAttackerUuid),
                Codec.STRING.optionalFieldOf("skill", "").forGetter(AiSnapshot::skill),
                Codec.STRING.optionalFieldOf("phase", "").forGetter(AiSnapshot::phase),
                Codec.DOUBLE.optionalFieldOf("tick", 0.0).forGetter(AiSnapshot::tick),
                Codec.DOUBLE.optionalFieldOf("tick2", 0.0).forGetter(AiSnapshot::tick2),
                Codec.STRING.optionalFieldOf("queued_skill", "").forGetter(AiSnapshot::queuedSkill),
                Codec.DOUBLE.optionalFieldOf("queued_timer", 0.0).forGetter(AiSnapshot::queuedTimer),
                Codec.DOUBLE.optionalFieldOf("candidate_count", 0.0).forGetter(AiSnapshot::candidateCount),
                Codec.BOOL.optionalFieldOf("interrupted", false).forGetter(AiSnapshot::interrupted),
                SkillSlot.CODEC.listOf().optionalFieldOf("candidates", List.of()).forGetter(AiSnapshot::candidates),
                Codec.STRING.listOf().optionalFieldOf("skill_choices", List.of()).forGetter(AiSnapshot::skillChoices)
        ).apply(instance, AiSnapshot::new));
    }

    private record MotionSnapshot(
            double destX, double destY, double destZ,
            double recordX, double recordY, double recordZ,
            double dropX, double dropY, double dropZ,
            double dirX, double dirY, double dirZ,
            double viewX, double viewZ
    ) {
        static final MotionSnapshot EMPTY = new MotionSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        static final Codec<MotionSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("dest_x", 0.0).forGetter(MotionSnapshot::destX),
                Codec.DOUBLE.optionalFieldOf("dest_y", 0.0).forGetter(MotionSnapshot::destY),
                Codec.DOUBLE.optionalFieldOf("dest_z", 0.0).forGetter(MotionSnapshot::destZ),
                Codec.DOUBLE.optionalFieldOf("record_x", 0.0).forGetter(MotionSnapshot::recordX),
                Codec.DOUBLE.optionalFieldOf("record_y", 0.0).forGetter(MotionSnapshot::recordY),
                Codec.DOUBLE.optionalFieldOf("record_z", 0.0).forGetter(MotionSnapshot::recordZ),
                Codec.DOUBLE.optionalFieldOf("drop_x", 0.0).forGetter(MotionSnapshot::dropX),
                Codec.DOUBLE.optionalFieldOf("drop_y", 0.0).forGetter(MotionSnapshot::dropY),
                Codec.DOUBLE.optionalFieldOf("drop_z", 0.0).forGetter(MotionSnapshot::dropZ),
                Codec.DOUBLE.optionalFieldOf("dir_x", 0.0).forGetter(MotionSnapshot::dirX),
                Codec.DOUBLE.optionalFieldOf("dir_y", 0.0).forGetter(MotionSnapshot::dirY),
                Codec.DOUBLE.optionalFieldOf("dir_z", 0.0).forGetter(MotionSnapshot::dirZ),
                Codec.DOUBLE.optionalFieldOf("view_x", 0.0).forGetter(MotionSnapshot::viewX),
                Codec.DOUBLE.optionalFieldOf("view_z", 0.0).forGetter(MotionSnapshot::viewZ)
        ).apply(instance, MotionSnapshot::new));
    }

    private record MiscSnapshot(
            double angle, double headAngle, double view, double countdown, double flyTick, double flyTick2,
            double summonCooldown, double talismanLevel, double startFlag, double randomCap, double skillCount,
            double xzX, double xzZ, boolean animated, boolean defensive, boolean frightened
    ) {
        static final MiscSnapshot EMPTY = new MiscSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false);
        static final Codec<MiscSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("angle", 0.0).forGetter(MiscSnapshot::angle),
                Codec.DOUBLE.optionalFieldOf("head_angle", 0.0).forGetter(MiscSnapshot::headAngle),
                Codec.DOUBLE.optionalFieldOf("view", 0.0).forGetter(MiscSnapshot::view),
                Codec.DOUBLE.optionalFieldOf("countdown", 0.0).forGetter(MiscSnapshot::countdown),
                Codec.DOUBLE.optionalFieldOf("fly_tick", 0.0).forGetter(MiscSnapshot::flyTick),
                Codec.DOUBLE.optionalFieldOf("fly_tick2", 0.0).forGetter(MiscSnapshot::flyTick2),
                Codec.DOUBLE.optionalFieldOf("summon_cooldown", 0.0).forGetter(MiscSnapshot::summonCooldown),
                Codec.DOUBLE.optionalFieldOf("talisman_level", 0.0).forGetter(MiscSnapshot::talismanLevel),
                Codec.DOUBLE.optionalFieldOf("start_flag", 0.0).forGetter(MiscSnapshot::startFlag),
                Codec.DOUBLE.optionalFieldOf("random_cap", 0.0).forGetter(MiscSnapshot::randomCap),
                Codec.DOUBLE.optionalFieldOf("skill_count", 0.0).forGetter(MiscSnapshot::skillCount),
                Codec.DOUBLE.optionalFieldOf("xz_x", 0.0).forGetter(MiscSnapshot::xzX),
                Codec.DOUBLE.optionalFieldOf("xz_z", 0.0).forGetter(MiscSnapshot::xzZ),
                Codec.BOOL.optionalFieldOf("animated", false).forGetter(MiscSnapshot::animated),
                Codec.BOOL.optionalFieldOf("defensive", false).forGetter(MiscSnapshot::defensive),
                Codec.BOOL.optionalFieldOf("frightened", false).forGetter(MiscSnapshot::frightened)
        ).apply(instance, MiscSnapshot::new));
    }

    public static final Codec<CreatureState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IdentitySnapshot.CODEC.optionalFieldOf("identity", IdentitySnapshot.EMPTY).forGetter(CreatureState::identitySnapshot),
            AiSnapshot.CODEC.optionalFieldOf("ai", AiSnapshot.EMPTY).forGetter(CreatureState::aiSnapshot),
            MotionSnapshot.CODEC.optionalFieldOf("motion", MotionSnapshot.EMPTY).forGetter(CreatureState::motionSnapshot),
            MiscSnapshot.CODEC.optionalFieldOf("misc", MiscSnapshot.EMPTY).forGetter(CreatureState::miscSnapshot)
    ).apply(instance, CreatureState::new));

    private String masterUuid = "";
    private String bagUuid = "";
    private double tameNum;
    private double harvestCount;
    private boolean beastCoreDropped;
    private double additionalExperience;
    private double recoveryTicks;
    private double experienceStand;
    private double experienceRate;
    private double experienceHinder;
    private String character = "";
    private double characterDef;
    private double characterAtt;
    private double characterInterval;
    private double characterResist;

    private String attackerUuid = "";
    private String tempAttackerUuid = "";
    private String skill = "";
    private String phase = "";
    private double tick;
    private double tick2;
    private String queuedSkill = "";
    private double queuedTimer;
    private double candidateCount;
    private boolean interrupted;
    private final List<SkillSlot> candidates = new ArrayList<>();
    private final List<String> skillChoices = new ArrayList<>();

    private double destX;
    private double destY;
    private double destZ;
    private double recordX;
    private double recordY;
    private double recordZ;
    private double dropX;
    private double dropY;
    private double dropZ;
    private double dirX;
    private double dirY;
    private double dirZ;
    private double viewX;
    private double viewZ;

    private double angle;
    private double headAngle;
    private double view;
    private double countdown;
    private double flyTick;
    private double flyTick2;
    private double summonCooldown;
    private double talismanLevel;
    private double startFlag;
    private double randomCap;
    private double skillCount;
    private double xzX;
    private double xzZ;
    private boolean animated;
    private boolean defensive;
    private boolean frightened;

    public CreatureState() {
    }

    private CreatureState(IdentitySnapshot id, AiSnapshot ai, MotionSnapshot motion, MiscSnapshot misc) {
        masterUuid = id.masterUuid();
        bagUuid = id.bagUuid();
        tameNum = id.tameNum();
        harvestCount = id.harvestCount();
        beastCoreDropped = id.beastCoreDropped();
        additionalExperience = id.additionalExperience();
        recoveryTicks = id.recoveryTicks();
        experienceStand = id.experienceStand();
        experienceRate = id.experienceRate();
        experienceHinder = id.experienceHinder();
        character = id.character();
        characterDef = id.characterDef();
        characterAtt = id.characterAtt();
        characterInterval = id.characterInterval();
        characterResist = id.characterResist();

        attackerUuid = ai.attackerUuid();
        tempAttackerUuid = ai.tempAttackerUuid();
        skill = ai.skill();
        phase = ai.phase();
        tick = ai.tick();
        tick2 = ai.tick2();
        queuedSkill = ai.queuedSkill();
        queuedTimer = ai.queuedTimer();
        candidateCount = ai.candidateCount();
        interrupted = ai.interrupted();
        candidates.addAll(ai.candidates());
        skillChoices.addAll(ai.skillChoices());

        destX = motion.destX();
        destY = motion.destY();
        destZ = motion.destZ();
        recordX = motion.recordX();
        recordY = motion.recordY();
        recordZ = motion.recordZ();
        dropX = motion.dropX();
        dropY = motion.dropY();
        dropZ = motion.dropZ();
        dirX = motion.dirX();
        dirY = motion.dirY();
        dirZ = motion.dirZ();
        viewX = motion.viewX();
        viewZ = motion.viewZ();

        angle = misc.angle();
        headAngle = misc.headAngle();
        view = misc.view();
        countdown = misc.countdown();
        flyTick = misc.flyTick();
        flyTick2 = misc.flyTick2();
        summonCooldown = misc.summonCooldown();
        talismanLevel = misc.talismanLevel();
        startFlag = misc.startFlag();
        randomCap = misc.randomCap();
        skillCount = misc.skillCount();
        xzX = misc.xzX();
        xzZ = misc.xzZ();
        animated = misc.animated();
        defensive = misc.defensive();
        frightened = misc.frightened();
    }

    private IdentitySnapshot identitySnapshot() {
        return new IdentitySnapshot(masterUuid, bagUuid, tameNum, harvestCount, beastCoreDropped,
                additionalExperience, recoveryTicks, experienceStand, experienceRate, experienceHinder,
                character, characterDef, characterAtt, characterInterval, characterResist);
    }

    private AiSnapshot aiSnapshot() {
        return new AiSnapshot(attackerUuid, tempAttackerUuid, skill, phase, tick, tick2,
                queuedSkill, queuedTimer, candidateCount, interrupted, candidates, skillChoices);
    }

    private MotionSnapshot motionSnapshot() {
        return new MotionSnapshot(destX, destY, destZ, recordX, recordY, recordZ,
                dropX, dropY, dropZ, dirX, dirY, dirZ, viewX, viewZ);
    }

    private MiscSnapshot miscSnapshot() {
        return new MiscSnapshot(angle, headAngle, view, countdown, flyTick, flyTick2,
                summonCooldown, talismanLevel, startFlag, randomCap, skillCount, xzX, xzZ,
                animated, defensive, frightened);
    }

    public String masterUuid() { return masterUuid; }
    public void setMasterUuid(String masterUuid) { this.masterUuid = nonNull(masterUuid); }
    public String bagUuid() { return bagUuid; }
    public void setBagUuid(String bagUuid) { this.bagUuid = nonNull(bagUuid); }
    public double tameNum() { return tameNum; }
    public void setTameNum(double tameNum) { this.tameNum = tameNum; }
    public double harvestCount() { return harvestCount; }
    public void setHarvestCount(double harvestCount) { this.harvestCount = harvestCount; }
    public boolean beastCoreDropped() { return beastCoreDropped; }
    public void setBeastCoreDropped(boolean beastCoreDropped) { this.beastCoreDropped = beastCoreDropped; }
    public double additionalExperience() { return additionalExperience; }
    public void setAdditionalExperience(double additionalExperience) { this.additionalExperience = additionalExperience; }
    public double recoveryTicks() { return recoveryTicks; }
    public void setRecoveryTicks(double recoveryTicks) { this.recoveryTicks = recoveryTicks; }
    public double experienceStand() { return experienceStand; }
    public void setExperienceStand(double experienceStand) { this.experienceStand = experienceStand; }
    public double experienceRate() { return experienceRate; }
    public void setExperienceRate(double experienceRate) { this.experienceRate = experienceRate; }
    public double experienceHinder() { return experienceHinder; }
    public void setExperienceHinder(double experienceHinder) { this.experienceHinder = experienceHinder; }
    public String character() { return character; }
    public void setCharacter(String character) { this.character = nonNull(character); }
    public double characterDef() { return characterDef; }
    public void setCharacterDef(double characterDef) { this.characterDef = characterDef; }
    public double characterAtt() { return characterAtt; }
    public void setCharacterAtt(double characterAtt) { this.characterAtt = characterAtt; }
    public double characterInterval() { return characterInterval; }
    public void setCharacterInterval(double characterInterval) { this.characterInterval = characterInterval; }
    public double characterResist() { return characterResist; }
    public void setCharacterResist(double characterResist) { this.characterResist = characterResist; }

    public String attackerUuid() { return attackerUuid; }
    public void setAttackerUuid(String attackerUuid) { this.attackerUuid = nonNull(attackerUuid); }
    public String tempAttackerUuid() { return tempAttackerUuid; }
    public void setTempAttackerUuid(String tempAttackerUuid) { this.tempAttackerUuid = nonNull(tempAttackerUuid); }
    public String skill() { return skill; }
    public void setSkill(String skill) { this.skill = nonNull(skill); }
    public String phase() { return phase; }
    public void setPhase(String phase) { this.phase = nonNull(phase); }
    public double tick() { return tick; }
    public void setTick(double tick) { this.tick = tick; }
    public double tick2() { return tick2; }
    public void setTick2(double tick2) { this.tick2 = tick2; }
    public String queuedSkill() { return queuedSkill; }
    public void setQueuedSkill(String queuedSkill) { this.queuedSkill = nonNull(queuedSkill); }
    public double queuedTimer() { return queuedTimer; }
    public void setQueuedTimer(double queuedTimer) { this.queuedTimer = queuedTimer; }
    public double candidateCount() { return candidateCount; }
    public void setCandidateCount(double candidateCount) { this.candidateCount = candidateCount; }
    public boolean interrupted() { return interrupted; }
    public void setInterrupted(boolean interrupted) { this.interrupted = interrupted; }

    /** 读取 1 起始索引的候选技能槽位；越界时返回空槽。 */
    public SkillSlot candidate(int index) {
        return index >= 1 && index <= candidates.size() ? candidates.get(index - 1) : SkillSlot.EMPTY;
    }

    public String candidateSkill(int index) { return candidate(index).name(); }
    public double candidateTimer(int index) { return candidate(index).timer(); }
    public double candidateWeight(int index) { return candidate(index).weight(); }

    public void setCandidateSkill(int index, String name) {
        if (index < 1) return;
        ensureCandidateCapacity(index);
        SkillSlot previous = candidates.get(index - 1);
        candidates.set(index - 1, new SkillSlot(name, previous.timer(), previous.weight()));
    }

    public void setCandidateTimer(int index, double timer) {
        if (index < 1) return;
        ensureCandidateCapacity(index);
        SkillSlot previous = candidates.get(index - 1);
        candidates.set(index - 1, new SkillSlot(previous.name(), timer, previous.weight()));
    }

    public void setCandidateWeight(int index, double weight) {
        if (index < 1) return;
        ensureCandidateCapacity(index);
        SkillSlot previous = candidates.get(index - 1);
        candidates.set(index - 1, new SkillSlot(previous.name(), previous.timer(), weight));
    }

    public void clearCandidate(int index) {
        if (index >= 1 && index <= candidates.size()) {
            candidates.set(index - 1, SkillSlot.EMPTY);
        }
    }

    /** 读取 1 起始索引的可选技能槽位；越界返回空串。 */
    public String skillChoice(int index) {
        return index >= 1 && index <= skillChoices.size() ? skillChoices.get(index - 1) : "";
    }

    public void setSkillChoice(int index, String value) {
        if (index < 1) return;
        while (skillChoices.size() < index) {
            skillChoices.add("");
        }
        skillChoices.set(index - 1, nonNull(value));
    }

    public double destX() { return destX; }
    public void setDestX(double destX) { this.destX = destX; }
    public double destY() { return destY; }
    public void setDestY(double destY) { this.destY = destY; }
    public double destZ() { return destZ; }
    public void setDestZ(double destZ) { this.destZ = destZ; }
    public double recordX() { return recordX; }
    public void setRecordX(double recordX) { this.recordX = recordX; }
    public double recordY() { return recordY; }
    public void setRecordY(double recordY) { this.recordY = recordY; }
    public double recordZ() { return recordZ; }
    public void setRecordZ(double recordZ) { this.recordZ = recordZ; }
    public double dropX() { return dropX; }
    public void setDropX(double dropX) { this.dropX = dropX; }
    public double dropY() { return dropY; }
    public void setDropY(double dropY) { this.dropY = dropY; }
    public double dropZ() { return dropZ; }
    public void setDropZ(double dropZ) { this.dropZ = dropZ; }
    public double dirX() { return dirX; }
    public void setDirX(double dirX) { this.dirX = dirX; }
    public double dirY() { return dirY; }
    public void setDirY(double dirY) { this.dirY = dirY; }
    public double dirZ() { return dirZ; }
    public void setDirZ(double dirZ) { this.dirZ = dirZ; }
    public double viewX() { return viewX; }
    public void setViewX(double viewX) { this.viewX = viewX; }
    public double viewZ() { return viewZ; }
    public void setViewZ(double viewZ) { this.viewZ = viewZ; }

    public double angle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    public double headAngle() { return headAngle; }
    public void setHeadAngle(double headAngle) { this.headAngle = headAngle; }
    public double view() { return view; }
    public void setView(double view) { this.view = view; }
    public double countdown() { return countdown; }
    public void setCountdown(double countdown) { this.countdown = countdown; }
    public double flyTick() { return flyTick; }
    public void setFlyTick(double flyTick) { this.flyTick = flyTick; }
    public double flyTick2() { return flyTick2; }
    public void setFlyTick2(double flyTick2) { this.flyTick2 = flyTick2; }
    public double summonCooldown() { return summonCooldown; }
    public void setSummonCooldown(double summonCooldown) { this.summonCooldown = summonCooldown; }
    public double talismanLevel() { return talismanLevel; }
    public void setTalismanLevel(double talismanLevel) { this.talismanLevel = talismanLevel; }
    public double startFlag() { return startFlag; }
    public void setStartFlag(double startFlag) { this.startFlag = startFlag; }
    public double randomCap() { return randomCap; }
    public void setRandomCap(double randomCap) { this.randomCap = randomCap; }
    public double skillCount() { return skillCount; }
    public void setSkillCount(double skillCount) { this.skillCount = skillCount; }
    public double xzX() { return xzX; }
    public void setXzX(double xzX) { this.xzX = xzX; }
    public double xzZ() { return xzZ; }
    public void setXzZ(double xzZ) { this.xzZ = xzZ; }
    public boolean animated() { return animated; }
    public void setAnimated(boolean animated) { this.animated = animated; }
    public boolean defensive() { return defensive; }
    public void setDefensive(boolean defensive) { this.defensive = defensive; }
    public boolean frightened() { return frightened; }
    public void setFrightened(boolean frightened) { this.frightened = frightened; }

    private void ensureCandidateCapacity(int index) {
        while (candidates.size() < index) {
            candidates.add(SkillSlot.EMPTY);
        }
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}
