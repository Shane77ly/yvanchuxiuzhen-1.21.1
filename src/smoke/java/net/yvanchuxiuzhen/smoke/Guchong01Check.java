package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.guchong.api.GuchongCapture;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarRuntime;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong01Entity;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 编号01新增路径：自然挑衅与跟随、七级认主、百刻回罐、击杀成长及真实存档重进。 */
final class Guchong01Check extends SystemCheck {
    private final Screenshots screenshots;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Entity> actors = new ArrayList<>();
    private int executed = -1;
    private BlockPos origin;
    private BlockPos home;
    private Vec3 oldPosition;
    private float oldYaw;
    private float oldPitch;
    private GameType oldMode;
    private Difficulty oldDifficulty;
    private Guchong01Entity active;
    private UUID activeUuid;
    private Pig pig;
    private Zombie zombie;
    private long releasedAt;
    private double followDistance;
    private ItemStack captured;
    private boolean checkedTarget;

    Guchong01Check(List<String> report, String levelId, Screenshots screenshots) {
        super("鼓上蚤", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 10, this::prepare);
            case 1 -> {
                boolean complete = action(step, local, 70, this::wildAndProvoke);
                if (complete) screenshots.take("guchong01-natural-provocation.png");
                yield complete;
            }
            case 2 -> action(step, local, 10, this::checkProvokeAndCapture);
            case 3 -> action(step, local, 5, this::placeAndRecognize);
            case 4 -> released(local);
            case 5 -> returned(local);
            case 6 -> action(step, local, 65, this::handheldAndFollow);
            case 7 -> action(step, local, 10, this::growthAndSave);
            case 8 -> { leaveWorld(); yield true; }
            case 9 -> reopened(local);
            case 10 -> inWorld(local);
            case 11 -> action(step, local, 8, this::loadedAndKill);
            case 12 -> action(step, local, 2, this::deathAndCleanup);
            case 13 -> { finish(); yield true; }
            default -> throw new IllegalStateException("未知鼓上蚤检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, int delay, Consumer<ServerPlayer> action) {
        if (executed != step) {
            executed = step;
            onServer(action);
        }
        return local >= delay;
    }

    private void prepare(ServerPlayer player) {
        oldPosition = player.position();
        oldYaw = player.getYRot();
        oldPitch = player.getXRot();
        oldMode = player.gameMode.getGameModeForPlayer();
        oldDifficulty = player.level().getDifficulty();
        player.closeContainer();
        player.getInventory().clearContent();
        player.setGameMode(GameType.CREATIVE);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        origin = player.blockPosition().above(16);
        home = origin.offset(0, 1, 0);
        for (int x = -5; x <= 17; x++) for (int z = -5; z <= 5; z++) {
            setBlock(player, origin.offset(x, 0, z), Blocks.STONE.defaultBlockState());
        }
        setBlock(player, origin, Blocks.GRASS_BLOCK.defaultBlockState());
        setBlock(player, origin.above(3), Blocks.STONE.defaultBlockState());
        blocks.put(home, player.level().getBlockState(home));
        player.connection.teleport(origin.getX() - 2, origin.getY() + 1, origin.getZ() + .5, -90, 15);
    }

    private void wildAndProvoke(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("guchong_01item"));
        useOn(player, origin, Direction.NORTH);
        expectTrue("单虫非顶面拒绝且不消耗", player.getMainHandItem().getCount() == 1 && insects(player).isEmpty());
        useOn(player, origin, Direction.UP);
        active = onlyInsect(player);
        player.setGameMode(GameType.CREATIVE);
        actors.add(active);
        EntityStats stats = EntityAttributes.get(active);
        expectTrue("单虫自然初始化为二阶低级且保持来源十三资质", player.getMainHandItem().isEmpty()
                && stats.level() == 7 && stats.realmName().equals("二阶低级")
                && stats.rating(Stat.HEALTH_MAX).equals("平庸") && stats.rating(Stat.ATTACK_DEF).equals("勉强")
                && stats.rating(Stat.MOVE_SPEED).equals("贫弱") && !active.isTame());
        expect("七级来源生命", 148, active.getMaxHealth());
        expect("七级来源经验上限", 1570, stats.experienceMax());
        expect("七级来源攻击", 4, active.getAttributeValue(Attributes.ATTACK_DAMAGE));
        expect("七级来源移速", .135, active.getAttributeValue(Attributes.MOVEMENT_SPEED));
        pig = EntityType.PIG.create(player.serverLevel());
        pig.moveTo(origin.getX() + 1.5, origin.getY() + 1, origin.getZ() + .5);
        pig.setNoAi(true);
        player.serverLevel().addFreshEntity(pig);
        actors.add(pig);
        zombie = EntityType.ZOMBIE.create(player.serverLevel());
        zombie.moveTo(origin.getX() + 3.5, origin.getY() + 1, origin.getZ() + .5);
        zombie.setPersistenceRequired();
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        player.serverLevel().addFreshEntity(zombie);
        actors.add(zombie);
    }

    private void checkProvokeAndCapture(ServerPlayer player) {
        expectTrue("自然十九刻挑衅驱动原生AI攻击前一只生物", zombie.getData(GuchongData.PROVOKE_TARGET).equals(pig.getStringUUID())
                && (pig.getHealth() < pig.getMaxHealth() || !pig.isAlive()));
        expectTrue("鼓上蚤本身没有近战或主动选敌", active.getTarget() == null && active.getHealth() == active.getMaxHealth());
        String marker = zombie.getData(GuchongData.PROVOKE_TARGET);
        expectTrue("挑衅标记随原生实体完整保存", zombie.saveWithoutId(new CompoundTag())
                .getCompound("neoforge:attachments").getString("yvanchuxiuzhen:guchong_provoke_target").equals(marker));
        zombie.discard();
        pig.discard();
        GuchongCapture.captureWild(player.serverLevel(), active, player.getStringUUID());
        captured = player.getInventory().items.stream().filter(stack -> stack.is(item("guchong_01item").getItem())).findFirst().orElseThrow().copy();
        expectTrue("捕获编号01保留七级个体与实体身份", GuchongJarData.read(captured).getString("guchongid").equals("yvanchuxiuzhen:guchong_01")
                && GuchongJarData.read(captured).getDouble("LEVELnumber") == 7);
        player.getInventory().clearContent();
        ItemStack owned = captured.copy();
        CompoundTag saved = GuchongJarData.read(owned);
        saved.putBoolean("guchongxvnfu", true);
        saved.putDouble("health_Num", 7);
        saved.putString("belongToMasterUUID", "source_does_not_copy");
        GuchongJarData.write(owned, saved);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, owned);
        useOn(player, origin, Direction.UP);
        Guchong01Entity restored = onlyInsect(player);
        actors.add(restored);
        expectTrue("认主单虫恢复七级资质和当前生命并认当前使用者", restored.isOwnedBy(player) && restored.getHealth() == 7
                && EntityAttributes.level(restored) == 7
                && Relations.entityTeam(restored).equals(Relations.team(player))
                && restored.getData(GuchongData.RUNTIME).ownerUuid().isEmpty());
        restored.discard();
        player.setGameMode(GameType.CREATIVE);
    }

    private void placeAndRecognize(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        ItemStack jar = item("taoguan_01item");
        CompoundTag saved = new CompoundTag();
        saved.putString("guguanuuid", "r10_01");
        GuchongJarData.pack(captured, saved, 0, false);
        GuchongJarData.write(jar, saved);
        player.setItemInHand(InteractionHand.MAIN_HAND, jar);
        useOn(player, origin, Direction.UP);
        ItemStack blood = item("jingxve");
        CompoundTag bloodData = new CompoundTag();
        bloodData.putInt("playerlevelnum", 6);
        GuchongJarData.write(blood, bloodData);
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        useOn(player, home, Direction.UP);
        expectTrue("七级鼓上蚤拒绝六级精血", !GuchongJarData.read(jar(player).getItem(0)).getBoolean("guchongxvnfu"));
        blood = item("jingxve");
        bloodData.putInt("playerlevelnum", 7);
        GuchongJarData.write(blood, bloodData);
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        useOn(player, home, Direction.UP);
        expectTrue("七级精血认主写主人", GuchongJarData.read(jar(player).getItem(0)).getString("belongToMasterUUID").equals(player.getStringUUID()));
        player.setGameMode(GameType.CREATIVE);
        pig = EntityType.PIG.create(player.serverLevel());
        pig.moveTo(origin.getX() + 4.5, origin.getY() + 1, origin.getZ() + .5);
        pig.setNoAi(true);
        player.serverLevel().addFreshEntity(pig);
        actors.add(pig);
    }

    private boolean released(int local) {
        boolean[] found = {false};
        onServer(player -> {
            List<Guchong01Entity> insects = insects(player);
            if (insects.isEmpty()) return;
            active = insects.getFirst();
            actors.add(active);
            releasedAt = player.level().getGameTime();
            expectTrue("地上罐自然放出认主编号01", active.isOwnedBy(player) && active.getData(GuchongData.RUNTIME).guarding());
            pig.moveTo(origin.getX() + 15.5, origin.getY() + 1, origin.getZ() + .5);
            found[0] = true;
        });
        if (!found[0] && local > 100) throw new IllegalStateException("编号01自然放虫超时");
        return found[0];
    }

    private boolean returned(int local) {
        boolean[] done = {false};
        onServer(player -> {
            long elapsed = player.level().getGameTime() - releasedAt;
            if (!checkedTarget && !active.isRemoved() && elapsed > 45 && elapsed < 75) {
                expectTrue("编号01保持目标时仍等待百刻回罐", active.getTarget() == pig && pig.isAlive());
                checkedTarget = true;
            }
            if (!active.isRemoved()) return;
            expectTrue("编号01有存活目标仍自然百刻回罐", pig.isAlive() && elapsed >= 80 && elapsed <= 130
                    && !jar(player).jarData().getBoolean("shifangguchong") && !jar(player).getItem(0).isEmpty());
            pig.discard();
            done[0] = true;
        });
        if (!done[0] && local > 180) throw new IllegalStateException("编号01百刻回罐超时");
        return done[0];
    }

    private void handheldAndFollow(ServerPlayer player) {
        ModifierKeys.update(player, true, false);
        useOn(player, home, Direction.UP);
        ModifierKeys.update(player, false, false);
        ItemStack jar = player.getInventory().items.stream().filter(stack -> stack.is(item("taoguan_01item").getItem())).findFirst().orElseThrow();
        player.setItemInHand(InteractionHand.MAIN_HAND, jar);
        useJar(player);
        active = onlyInsect(player);
        actors.add(active);
        active.setDeltaMovement(Vec3.ZERO);
        active.moveTo(origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5);
        player.connection.teleport(origin.getX() + 13.5, origin.getY() + 1, origin.getZ() + .5, 90, 15);
        followDistance = active.distanceToSqr(player);
    }

    private void growthAndSave(ServerPlayer player) {
        expectTrue("手持释放的认主鼓上蚤自然跟随主人", active.isOwnedBy(player) && active.distanceToSqr(player) < followDistance / 2);
        EntityAttributes.setExperience(active, 1560);
        active.setData(GuchongData.PROVOKE_TARGET, "provocation_marker");
        Guchong01Entity victim = (Guchong01Entity) GuchongJarRuntime.create(player.serverLevel(), "yvanchuxiuzhen:guchong_01");
        victim.moveTo(active.position());
        GuchongJarRuntime.finishSpawn(player.serverLevel(), victim);
        actors.add(victim);
        double profession = Professions.experience(player, Profession.INSECT);
        victim.hurt(player.damageSources().mobAttack(active), 10000);
        EntityStats stats = EntityAttributes.get(active);
        expectTrue("同种击杀按经验上限四分之一结算并升级", stats.level() == 8
                && stats.experience() == 382.5 && stats.experienceMax() == 2220
                && stats.realmName().equals("二阶中级") && active.getData(GuchongData.PROVOKE_TARGET).isEmpty());
        expect("升级回满来源生命", 156, active.getHealth());
        expect("养蛊经验通过公开接口按新等级增加", profession + 80, Professions.experience(player, Profession.INSECT));
        Pig ordinary = EntityType.PIG.create(player.serverLevel());
        ordinary.moveTo(active.position());
        player.serverLevel().addFreshEntity(ordinary);
        actors.add(ordinary);
        ordinary.hurt(player.damageSources().mobAttack(active), 10000);
        expect("非同种普通生物按原生生命除五结算", 384.5, EntityAttributes.experience(active));
        for (String id : List.of("guchong_03", "guchong_04")) {
            GuchongEntity other = (GuchongEntity) GuchongJarRuntime.create(player.serverLevel(), "yvanchuxiuzhen:" + id);
            other.moveTo(active.position());
            GuchongJarRuntime.finishSpawn(player.serverLevel(), other);
            actors.add(other);
            Pig ordinaryVictim = EntityType.PIG.create(player.serverLevel());
            ordinaryVictim.moveTo(active.position());
            player.serverLevel().addFreshEntity(ordinaryVictim);
            actors.add(ordinaryVictim);
            ordinaryVictim.hurt(player.damageSources().mobAttack(other), 10000);
            expect(id + "实际实体标签接入同一新增击杀消费者", 2, EntityAttributes.experience(other));
            other.discard();
        }
        checkBeastVictimBranch(player);
        activeUuid = active.getUUID();
        player.getInventory().setItem(9, player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        minecraft.execute(() -> screenshots.take("guchong01-owned-follow.png"));
    }

    private void checkBeastVictimBranch(ServerPlayer player) {
        GuchongEntity killer = (GuchongEntity) GuchongJarRuntime.create(player.serverLevel(), "yvanchuxiuzhen:guchong_03");
        killer.moveTo(active.position());
        GuchongJarRuntime.finishSpawn(player.serverLevel(), killer);
        actors.add(killer);
        EntityAttributes.setExperience(killer, 119);
        Mob beast = (Mob) GuchongJarRuntime.create(player.serverLevel(), "yvanchuxiuzhen:yaoshou_004");
        beast.moveTo(active.position());
        GuchongJarRuntime.finishSpawn(player.serverLevel(), beast);
        actors.add(beast);
        EntityAttributes.setLevel(beast, 1);
        CreatureData.state(beast).setAdditionalExperience(0);
        EntityAttributes.setStat(beast, Stat.HEALTH_MAX, -1);
        beast.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1);
        beast.setHealth(1);
        beast.hurt(player.damageSources().mobAttack(killer), 10000);
        expectTrue("不同种等级公式通过Creature查询并触发升级", EntityAttributes.level(killer) == 2
                && EntityAttributes.experience(killer) == 9);
        expectTrue("来源死者妖兽分支只重算死者且不重算升级虫", EntityAttributes.stat(beast, Stat.HEALTH_MAX) == 122
                && beast.getMaxHealth() == 122 && killer.getMaxHealth() == 121);
        killer.discard();
        beast.discard();
    }

    private void loadedAndKill(ServerPlayer player) {
        active = (Guchong01Entity) player.serverLevel().getEntity(activeUuid);
        if (active == null) throw new IllegalStateException("真实重进未恢复编号01");
        actors.add(active);
        expectTrue("真实退出重进保存编号01主人等级经验属性和罐关联", active.isOwnedBy(player)
                && EntityAttributes.level(active) == 8 && EntityAttributes.experience(active) == 384.5
                && active.getMaxHealth() == 156 && active.getData(GuchongData.RUNTIME).jarUuid().equals("r10_01"));
        active.hurt(player.damageSources().generic(), 10000);
        expectTrue("编号01死亡清理背包罐对应槽", GuchongJarData.read(player.getInventory().getItem(9)).getString("guchongid0").isEmpty());
    }

    private void deathAndCleanup(ServerPlayer player) {
        expectTrue("编号01按来源两刻死亡移除", active.isRemoved() && active.deathTime == 2);
        for (Entity actor : actors) if (!actor.isRemoved()) actor.discard();
        blocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.getInventory().clearContent();
        player.connection.teleport(oldPosition.x, oldPosition.y, oldPosition.z, oldYaw, oldPitch);
        player.setGameMode(oldMode);
        player.server.setDifficulty(oldDifficulty, true);
    }

    private List<Guchong01Entity> insects(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Guchong01Entity.class, player.getBoundingBox().inflate(24), Entity::isAlive);
    }

    private Guchong01Entity onlyInsect(ServerPlayer player) {
        List<Guchong01Entity> values = insects(player);
        if (values.size() != 1) throw new IllegalStateException("预期一只鼓上蚤，实际 " + values.size());
        return values.getFirst();
    }

    private TaoguanBlockEntity jar(ServerPlayer player) {
        return (TaoguanBlockEntity) player.level().getBlockEntity(home);
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path)));
    }

    private static void useOn(ServerPlayer player, BlockPos pos, Direction face) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
    }

    private static void useJar(ServerPlayer player) {
        player.setShiftKeyDown(false);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos, player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 3);
    }
}
