package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Moshou001Entity;

/** 金魔蛛通过原生喂食及致死入口生产的两套蛛丝。 */
final class Moshou001DropsCheck extends SystemCheck {
    private static final String PLAIN = "cailiao_17701";
    private static final String ENRICHED = "cailiao_17801";
    private final Consumer<EntityJoinLevelEvent> joinListener = this::onJoin;
    private final Consumer<LivingDeathEvent> deathListener = this::onDeath;
    private final List<Moshou001Entity> spiders = new ArrayList<>();
    private final List<ItemEntity> drops = new ArrayList<>();
    private ServerLevel level;
    private Moshou001Entity dying;
    private int deaths;

    Moshou001DropsCheck(List<String> report, String levelId) {
        super("金魔蛛001喂食与掉落", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            onServer(this::runChecks);
        } catch (RuntimeException error) {
            fail(error.getMessage());
        }
        finish();
        return true;
    }

    private void runChecks(ServerPlayer player) {
        level = player.serverLevel();
        Difficulty difficulty = level.getDifficulty();
        GameType mode = player.gameMode.getGameModeForPlayer();
        int selected = player.getInventory().selected;
        ItemStack main = player.getInventory().getItem(0).copy();
        ItemStack offhand = player.getOffhandItem().copy();
        NeoForge.EVENT_BUS.addListener(joinListener);
        NeoForge.EVENT_BUS.addListener(deathListener);
        try {
            player.server.setDifficulty(Difficulty.NORMAL, true);
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().selected = 0;
            Moshou001Entity ordinary = spawn(player.position().add(2, 0, 1));
            Moshou001Entity enriched = spawn(player.position().add(-2, 0, 1));
            Item coldVein = item("cailiao_132");
            if (coldVein == Items.AIR || item(PLAIN) == Items.AIR || item(ENRICHED) == Items.AIR) {
                throw new IllegalStateException("寒脉或蛛丝未注册");
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(coldVein));
            player.interactOn(ordinary, InteractionHand.OFF_HAND);
            expectTrue("仅副手寒脉不消费、不升格、不加速度", player.getOffhandItem().is(coldVein)
                    && player.getOffhandItem().getCount() == 1 && !ordinary.elevated()
                    && !ordinary.hasEffect(MobEffects.MOVEMENT_SPEED));

            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(coldVein, 2));
            player.interactOn(enriched, InteractionHand.MAIN_HAND);
            var speed = enriched.getEffect(MobEffects.MOVEMENT_SPEED);
            expectTrue("主手交互实际消耗一件并升格", player.getMainHandItem().is(coldVein)
                    && player.getMainHandItem().getCount() == 1 && enriched.elevated());
            expectTrue("喂食立即获得2400刻速度II", speed != null && speed.getDuration() == 2400 && speed.getAmplifier() == 1);

            checkDrops("未进食", ordinary, List.of(PLAIN, PLAIN, PLAIN, PLAIN));
            checkDrops("已进食", enriched, List.of(ENRICHED, ENRICHED, PLAIN));
            expect("两只样本分别触发一次原生死亡事件", 2, deaths);
        } finally {
            NeoForge.EVENT_BUS.unregister(joinListener);
            NeoForge.EVENT_BUS.unregister(deathListener);
            drops.forEach(Entity::discard);
            spiders.forEach(Entity::discard);
            player.getInventory().setItem(0, main);
            player.getInventory().selected = selected;
            player.setItemInHand(InteractionHand.OFF_HAND, offhand);
            player.setGameMode(mode);
            player.server.setDifficulty(difficulty, true);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private Moshou001Entity spawn(Vec3 origin) {
        Moshou001Entity spider = CreatureSetup.<Moshou001Entity>entityType("moshou_001").get().create(level);
        if (spider == null) throw new IllegalStateException("无法创建金魔蛛样本");
        spider.moveTo(origin);
        spider.finalizeSpawn(level, level.getCurrentDifficultyAt(spider.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        spider.setNoAi(true);
        if (!level.addFreshEntity(spider)) throw new IllegalStateException("金魔蛛样本未入场");
        spiders.add(spider);
        return spider;
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private void checkDrops(String label, Moshou001Entity spider, List<String> expected) {
        dying = spider;
        int start = drops.size();
        expectTrue(label + "走原生致死伤害", spider.hurt(spider.damageSources().genericKill(), Float.MAX_VALUE) && spider.isDeadOrDying());
        dying = null;
        List<ItemEntity> spawned = drops.subList(start, drops.size());
        expect(label + "逐件生成的蛛丝", expected, spawned.stream()
                .map(drop -> BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).getPath()).toList());
        expectTrue(label + "掉落实体实际入场、逐件计数1和10刻拾取延迟",
                !spawned.isEmpty() && spawned.stream().allMatch(drop -> level.getEntity(drop.getUUID()) == drop
                        && drop.getItem().getCount() == 1 && drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 10));
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() == level && dying != null && event.getEntity() instanceof ItemEntity drop
                && drop.position().distanceToSqr(dying.position().add(0, 1.5, 0)) < 1e-6) drops.add(drop);
    }

    private void onDeath(LivingDeathEvent event) {
        if (event.getEntity() == dying && dying != null) deaths++;
    }
}
