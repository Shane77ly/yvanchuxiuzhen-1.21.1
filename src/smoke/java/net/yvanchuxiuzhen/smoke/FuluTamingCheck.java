package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.api.CreatureBounties;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.api.DungeonCreatures;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.fulu.TalismanControl;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.fulu.data.FuluCasterState;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 神傀符自然松手、跨端修饰键、来源概率、驯服状态和保存的必要单项。 */
final class FuluTamingCheck extends SystemCheck {
    private final Consumer<String> shot;
    private final List<Entity> created = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Consumer<EntityJoinLevelEvent> joined = this::joined;
    private ItemStack originalHand;
    private GameType mode;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private boolean noGravity;
    private boolean invulnerable;
    private boolean ctrl;
    private boolean alt;
    private double soul;
    private double fixed;
    private double percentage;
    private FuluCasterState casterSnapshot;
    private String controlOwnerSnapshot;
    private Pig target;
    private TalismanProjectile natural;
    private boolean releasedModifier;

    FuluTamingCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("神傀符", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            PacketDistributor.sendToServer(new ModifierKeys.StatePayload(false, false));
            return true;
        }
        if (step == 1 || step == 4) {
            if (local < 12) return false;
            minecraft.player.setYRot(0);
            minecraft.player.setXRot(0);
            minecraft.player.setYHeadRot(0);
            minecraft.player.setYBodyRot(0);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            return true;
        }
        if (step == 2 || step == 5) {
            if (local < 4) return false;
            minecraft.gameMode.releaseUsingItem(minecraft.player);
            return true;
        }
        if (step == 3) {
            if (local < 18) return false;
            onServer(player -> {
                expectTrue("自然松手生成18号弹体", natural != null);
                expectTrue("普通模式原生箭命中并给予四级缓慢", target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                        && target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 3);
                expect("命中先记录施放者显示名", player.getDisplayName().getString(), target.getData(FuluData.CONTROL_OWNER));
                expect("二阶普通发射扣40精力", 760, Cultivation.get(player, CultivationValue.SOUL));
                expect("二阶普通发射耗一张", 2, player.getMainHandItem().getCount());
                expect("首刻保留通用品阶", 2, player.getData(FuluData.CASTER).pendingLevel());
                target.discard();
                target = pig(player);
                natural = null;
            });
            PacketDistributor.sendToServer(new ModifierKeys.StatePayload(true, false));
            return true;
        }
        if (step == 6) {
            if (!releasedModifier && local >= 2) {
                boolean[] captured = {false};
                onServer(player -> captured[0] = natural != null && natural.isNoGravity());
                if (captured[0]) {
                    PacketDistributor.sendToServer(new ModifierKeys.StatePayload(false, false));
                    releasedModifier = true;
                }
            }
            if (local < 18) return false;
            onServer(player -> {
                expectTrue("客户端Ctrl松开载荷抵达服务端", releasedModifier && !ModifierKeys.ctrl(player));
                expectTrue("真实第二次命中进入控制模式", natural != null && target.hasEffect(effect("fulutexiao_18")));
                expectTrue("松开Ctrl后弹体仍保留首刻模式与主人名字", natural != null && natural.isNineEffects()
                        && natural.ownerName().equals(player.getDisplayName().getString())
                        && Relations.entityTeam(natural).equals(Relations.team(player)));
                expect("两次发射累计扣80精力", 720, Cultivation.get(player, CultivationValue.SOUL));
                expect("两次发射剩一张", 1, player.getMainHandItem().getCount());
                if (target.hasEffect(effect("fulutexiao_18"))) persistence(player);
                formula(player);
                tame(player);
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.taming.chance", 42.5), false);
            });
            return true;
        }
        if (step == 7) {
            if (local < 8) return false;
            expect("物品中文", "神傀符", minecraft.player.getMainHandItem().getHoverName().getString());
            expect("效果中文", "被控制", Component.translatable("effect.yvanchuxiuzhen.fulutexiao_18").getString());
            expectTrue("效果图标进入图集", !minecraft.getMobEffectTextures().get(effect("fulutexiao_18"))
                    .contents().name().getPath().equals("missingno"));
            shot.accept("fulu18-control");
            onServer(this::cleanup);
            PacketDistributor.sendToServer(new ModifierKeys.StatePayload(ctrl, alt));
            finish();
            return true;
        }
        return false;
    }

    private void prepare(ServerPlayer player) {
        originalHand = player.getMainHandItem().copy();
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        noGravity = player.isNoGravity();
        invulnerable = player.isInvulnerable();
        ctrl = ModifierKeys.ctrl(player);
        alt = ModifierKeys.alt(player);
        soul = Cultivation.get(player, CultivationValue.SOUL);
        fixed = Cultivation.tizhiDouble(player, "FuluConsumeJingli");
        percentage = Cultivation.tizhiDouble(player, "FuluConsumeJingliPercentage");
        casterSnapshot = player.getData(FuluData.CASTER);
        controlOwnerSnapshot = player.getData(FuluData.CONTROL_OWNER);
        player.setGameMode(GameType.SURVIVAL);
        player.setNoGravity(true);
        player.setInvulnerable(true);
        player.setDeltaMovement(Vec3.ZERO);
        BlockPos floor = player.blockPosition().above(6);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos pos = floor.offset(x, 0, z);
            blocks.put(pos, player.serverLevel().getBlockState(pos));
            player.serverLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
        player.connection.teleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 0);
        player.setYHeadRot(0);
        player.setYBodyRot(0);
        PlayerAttributes.setContribution(player, "smoke:fulu18", new Contribution(Layer.BASE, Map.of(Stat.SOUL_MAX, 1000.0)));
        Cultivation.editTizhi(player, data -> {
            data.setNumber("FuluConsumeJingli", 0);
            data.setNumber("FuluConsumeJingliPercentage", 0);
        });
        ItemStack talisman = new ItemStack(BuiltInRegistries.ITEM.get(id("itemfulu_18")), 3);
        Fulu.setTalisman(talisman, new TalismanInstance(2, "", 0, 0));
        player.setItemInHand(InteractionHand.MAIN_HAND, talisman);
        Cultivation.set(player, CultivationValue.SOUL, 39);
        talisman.getItem().releaseUsing(talisman, player.level(), player, 71990);
        expect("不足精力仍先写通用品阶", 2, player.getData(FuluData.CASTER).pendingLevel());
        expect("不足精力保留符箓", 3, talisman.getCount());
        expect("不足精力不扣费", 39, Cultivation.get(player, CultivationValue.SOUL));
        Cultivation.set(player, CultivationValue.SOUL, 800);
        target = pig(player);
        NeoForge.EVENT_BUS.addListener(joined);
        player.inventoryMenu.broadcastChanges();
    }

    private void joined(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof TalismanProjectile projectile
                && projectile.getType() == FuluSetup.projectile(Kind.TAMING).get()) natural = projectile;
    }

    private void persistence(ServerPlayer player) {
        CompoundTag saved = target.saveWithoutId(new CompoundTag());
        saved.putString("id", "minecraft:pig");
        Pig loaded = (Pig) EntityType.loadEntityRecursive(saved, player.serverLevel(), entity -> entity);
        created.add(loaded);
        expectTrue("控制效果和显示名保存重读", loaded.hasEffect(effect("fulutexiao_18"))
                && loaded.getData(FuluData.CONTROL_OWNER).equals(player.getDisplayName().getString()));
        loaded.removeEffect(effect("fulutexiao_18"));
        expectTrue("移除效果清控制显示名", loaded.getData(FuluData.CONTROL_OWNER).isEmpty());
        player.setShiftKeyDown(true);
        target.tick();
        player.setShiftKeyDown(false);
        expectTrue("潜行控制者令目标解除效果", !target.hasEffect(effect("fulutexiao_18"))
                && target.getData(FuluData.CONTROL_OWNER).isEmpty());
        TalismanProjectile probe = FuluSetup.projectile(Kind.TAMING).get().create(player.serverLevel());
        probe.setOwner(player);
        probe.setPos(player.position().add(0, 10, 0));
        player.setData(FuluData.CASTER, player.getData(FuluData.CASTER).withPendingLevel(4));
        ModifierKeys.update(player, false, true);
        probe.tick();
        ModifierKeys.update(player, false, false);
        CompoundTag arrow = probe.saveWithoutId(new CompoundTag());
        arrow.putString("id", id("projectile_itemfulu_18").toString());
        TalismanProjectile restored = (TalismanProjectile) EntityType.loadEntityRecursive(arrow, player.serverLevel(), entity -> entity);
        created.add(probe);
        created.add(restored);
        expectTrue("弹体保存首刻品阶模式团队名字与计数", arrow.getDouble("fululevel") == 4 && arrow.getInt("tick") == 1
                && restored.isNineEffects() && restored.isNoGravity()
                && restored.ownerName().equals(player.getDisplayName().getString())
                && Relations.entityTeam(restored).equals(Relations.team(player)));
        for (int index = 1; index < 9; index++) restored.tick();
        expectTrue("弹体第九刻仍存在", !restored.isRemoved());
        restored.tick();
        expectTrue("弹体第十刻移除", restored.isRemoved());
        TalismanControl.hit(player.serverLevel(), probe, player, player, 2);
        expectTrue("修饰键玩家目标只加控制不减精力", player.hasEffect(effect("fulutexiao_18"))
                && Cultivation.get(player, CultivationValue.SOUL) == 720);
        player.removeEffect(effect("fulutexiao_18"));
        probe.setNineEffects(false);
        TalismanControl.hit(player.serverLevel(), probe, player, player, 2);
        expect("普通玩家目标精力减少160", 560, Cultivation.get(player, CultivationValue.SOUL));
        TalismanControl.hit(player.serverLevel(), probe, player, player, 4);
        expect("普通玩家目标精力最低零", 0, Cultivation.get(player, CultivationValue.SOUL));
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private void formula(ServerPlayer player) {
        Pig victim = pig(player);
        Pig shooter = pig(player);
        victim.setHealth(victim.getMaxHealth() / 2);
        EntityAttributes.setLevel(victim, 10);
        expect("半血与正等级差概率20", 20, TamingProbe.chance(victim, shooter, 2));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 9));
        expect("缓慢十级分支加20", 40, TamingProbe.chance(victim, shooter, 2));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 10));
        expect("缓慢十一级分支加21", 41, TamingProbe.chance(victim, shooter, 2));
        victim.removeAllEffects();
        victim.addEffect(new MobEffectInstance(effect("effect_023"), 1200, 9));
        victim.addEffect(new MobEffectInstance(effect("effect_024"), 1200, 7));
        expect("023和024缺032时各加1", 22, TamingProbe.chance(victim, shooter, 2));
        victim.addEffect(new MobEffectInstance(effect("effect_032"), 1200, 4));
        expect("023和024连同032共读取三次五级", 35, TamingProbe.chance(victim, shooter, 2));
        victim.removeAllEffects();
        victim.addEffect(new MobEffectInstance(effect("duyao_01"), 1200, 2));
        victim.addEffect(new MobEffectInstance(effect("duyao_18"), 1200, 3));
        victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 1));
        shooter.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 2));
        shooter.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 1200, 1));
        expect("首与18号毒药凋零及施放者幸运厄运", 30, TamingProbe.chance(victim, shooter, 2));
        victim.removeAllEffects();
        shooter.removeAllEffects();
        EntityAttributes.setLevel(victim, 25);
        expect("负等级差平方扣分且不截断", -15, TamingProbe.chance(victim, shooter, 2));
        expect("高等级差概率超过100保留", 185, TamingProbe.chance(victim, shooter, 20));
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        victim.setHealth(17);
        EntityAttributes.setLevel(victim, 0);
        double boundary = TamingProbe.chance(victim, shooter, 7);
        expect("17/20生命先float运算的概率精度", Double.toHexString(72.99999952316284), Double.toHexString(boundary));
        expectTrue("来源随机73在此精度边界仍失败", boundary < 73 && boundary >= 72);
    }

    private void tame(ServerPlayer player) {
        TamableAnimal beast = (TamableAnimal) BuiltInRegistries.ENTITY_TYPE.get(id("yaoshou_001")).create(player.serverLevel());
        beast.setNoAi(true);
        beast.setNoGravity(true);
        beast.setPos(player.position().add(4, 0, 4));
        player.serverLevel().addFreshEntity(beast);
        created.add(beast);
        TalismanProjectile probe = FuluSetup.projectile(Kind.TAMING).get().create(player.serverLevel());
        created.add(probe);
        Relations.setTeam(beast, "former");
        Relations.addFriendlyTeam(beast, "former");
        Relations.addFriendlyTeam(beast, "unrelated");
        DungeonCreatures.setDungeonEntity(beast, true);
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 20);
        expectTrue("副本妖兽拒绝驯服但先写控制名", !beast.isTame()
                && beast.getData(FuluData.CONTROL_OWNER).equals(player.getDisplayName().getString()));
        DungeonCreatures.setDungeonEntity(beast, false);
        CreatureBounties.setBounty(beast, true);
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 20);
        expectTrue("悬赏妖兽拒绝驯服", !beast.isTame());
        CreatureBounties.setBounty(beast, false);
        Objective objective = player.serverLevel().getScoreboard().getObjective("zhaohuanwu");
        if (objective == null) objective = player.serverLevel().getScoreboard().addObjective("zhaohuanwu", ObjectiveCriteria.DUMMY,
                Component.literal("zhaohuanwu"), ObjectiveCriteria.RenderType.INTEGER, true, null);
        player.serverLevel().getScoreboard().getOrCreatePlayerScore(beast, objective).set(1);
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 20);
        expectTrue("召唤物分数1拒绝驯服", !beast.isTame());
        player.serverLevel().getScoreboard().getOrCreatePlayerScore(beast, objective).set(0);
        EntityAttributes.setLevel(beast, 100);
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 1);
        expectTrue("负概率真实失败且阵营保留", !beast.isTame() && Relations.entityTeam(beast).equals("former"));
        EntityAttributes.setLevel(beast, 0);
        CreatureData.state(beast).setAttackerUuid("prior");
        CreatureData.state(beast).setTempAttackerUuid("prior");
        beast.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 0));
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 20);
        expectTrue("大于100概率真实驯服并绑定原生主人", beast.isTame() && player.getUUID().equals(beast.getOwnerUUID()));
        expectTrue("驯服替换旧好友表并交接施放者团队", Relations.friendlyTeams(beast).isEmpty()
                && Relations.team(beast).equals(Relations.team(player)));
        expectTrue("成功清全效果再给002及033各100刻", beast.getActiveEffects().size() == 2
                && beast.getEffect(effect("effect_002")).getDuration() == 100 && beast.getEffect(effect("effect_033")).getDuration() == 100);
        expectTrue("成功清两个仇恨UUID", CreatureData.state(beast).attackerUuid().isEmpty()
                && CreatureData.state(beast).tempAttackerUuid().isEmpty());
        CompoundTag saved = beast.saveWithoutId(new CompoundTag());
        saved.putString("id", id("yaoshou_001").toString());
        TamableAnimal restored = (TamableAnimal) EntityType.loadEntityRecursive(saved, player.serverLevel(), entity -> entity);
        created.add(restored);
        expectTrue("驯服主人阵营效果保存重读", restored.isTame() && player.getUUID().equals(restored.getOwnerUUID())
                && Relations.team(restored).equals(Relations.team(player)) && Relations.friendlyTeams(restored).isEmpty()
                && restored.hasEffect(effect("effect_033")));
        TalismanControl.hit(player.serverLevel(), probe, beast, player, 0);
        expectTrue("已驯服妖兽不重新清理状态", beast.isTame() && beast.getActiveEffects().size() == 2);
    }

    private void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(joined);
        created.forEach(Entity::discard);
        blocks.forEach((pos, state) -> player.serverLevel().setBlock(pos, state, Block.UPDATE_ALL));
        player.setItemInHand(InteractionHand.MAIN_HAND, originalHand);
        player.setGameMode(mode);
        player.setNoGravity(noGravity);
        player.setInvulnerable(invulnerable);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        PlayerAttributes.clearContribution(player, "smoke:fulu18");
        Cultivation.set(player, CultivationValue.SOUL, soul);
        Cultivation.editTizhi(player, data -> {
            data.setNumber("FuluConsumeJingli", fixed);
            data.setNumber("FuluConsumeJingliPercentage", percentage);
        });
        player.setData(FuluData.CASTER, casterSnapshot);
        player.setData(FuluData.CONTROL_OWNER, controlOwnerSnapshot);
    }

    private Pig pig(ServerPlayer player) {
        Pig pig = EntityType.PIG.create(player.serverLevel());
        pig.setNoAi(true);
        pig.setNoGravity(true);
        pig.setPos(player.getEyePosition().add(player.getLookAngle().scale(5)).add(0, -0.3, 0));
        player.serverLevel().addFreshEntity(pig);
        pig.setHealth(pig.getMaxHealth());
        created.add(pig);
        return pig;
    }

    private static Holder<MobEffect> effect(String path) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(id(path)).orElseThrow();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
