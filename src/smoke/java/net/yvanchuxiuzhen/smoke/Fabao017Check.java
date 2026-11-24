package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.content.Fabao01701Entity;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.client.TaoguanScreen;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;

/** 摄虫网的自然入口、服务端刻、捕获组件保存和蛊罐菜单消费；夹具固定实体位置以隔离落点与追击。 */
final class Fabao017Check extends ActionCheck {
    private final Screenshots screenshots;
    private BlockPos floor;
    private Difficulty originalDifficulty;
    private BlockState oldFloor;
    private Vec3 originalPosition;
    private GameType originalMode;
    private float originalYaw;
    private float originalPitch;
    private ItemStack tool;
    private ItemStack captured;
    private GuchongEntity wild;
    private GuchongEntity owned;
    private CompoundTag wildBefore;
    private Fabao01701Entity net;
    private Spider spider;
    private Cow cow;

    Fabao017Check(List<String> report, String levelId, Screenshots screenshots) {
        super("摄虫网", report, levelId);
        this.screenshots = screenshots;
        script(
                new Action(8, this::prepare),
                new Action(2, this::castCapture),
                new Action(55, this::reloadActiveNet),
                new Action(20, this::checkCaptureAndOpenJar),
                new Action(10, this::storeInJar),
                new Action(20, this::saveAndReopenJar),
                new Action(25, this::checkJarAndCastTrap),
                new Action(185, this::checkTrap),
                new Action(2, player -> expectTrue("困虫兜经过200服务端刻移除", net.isRemoved()))
        );
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalDifficulty = player.level().getDifficulty();
        player.server.setDifficulty(Difficulty.NORMAL, true);
        floor = player.blockPosition().above(4);
        oldFloor = player.level().getBlockState(floor);
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 0);
        tool = item("fabao_017");
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
    }

    private void castCapture(ServerPlayer player) {
        player.setShiftKeyDown(false);
        tool.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        net = currentNet(player);
        anchor(net);
        wild = insect(player.serverLevel(), "guchong_03", net.position());
        wild.setHealth(7);
        wild.setCustomName(Component.literal("[飞蝗样本]"));
        wild.getPersistentData().putDouble("experience", 37.5);
        wildBefore = wild.getPersistentData().copy();
        owned = insect(player.serverLevel(), "guchong_04", net.position());
        owned.getPersistentData().putBoolean("guchongxvnfu", true);
        expectTrue("普通右键生成摄虫网并进入50刻冷却", net.getPersistentData().getString("skill").equals("摄虫网")
                && player.getCooldowns().isOnCooldown(tool.getItem()));
    }

    private void reloadActiveNet(ServerPlayer player) {
        expectTrue("第36刻前尚未收纳蛊虫", wild.isAlive() && net.getPersistentData().getDouble("tick1") < 36);
        CompoundTag saved = net.saveWithoutId(new CompoundTag());
        double ticks = net.getPersistentData().getDouble("tick1");
        net.discard();
        net = EquipmentSetup.fabao017Type().get().create(player.serverLevel());
        net.load(saved);
        player.serverLevel().addFreshEntity(net);
        expectTrue("活动实体序列化重读保留主人、技能与计时", net.getPersistentData().getDouble("tick1") == ticks
                && net.getPersistentData().getString("belongToMasterUUID").equals(player.getStringUUID())
                && net.getPersistentData().getString("skill").equals("摄虫网"));
    }

    private void checkCaptureAndOpenJar(ServerPlayer player) {
        expectTrue("36刻收纳未认主蛊虫且40刻移除网，认主虫保留", wild.isRemoved() && net.isRemoved()
                && owned.isAlive() && count(player, "guchong_03item") == 1 && count(player, "guchong_04item") == 0);
        captured = player.getInventory().items.stream().filter(stack -> stack.is(item("guchong_03item").getItem())).findFirst().orElseThrow();
        CompoundTag data = GuchongJarData.read(captured);
        expectTrue("捕获物品保存当前生命、等级、经验和清理后的名称", data.getDouble("health_Num") == 7
                && data.getDouble("LEVELnumber") == wildBefore.getDouble("LEVELnumber")
                && data.getDouble("experience") == 37.5 && data.getString("guchongid").equals("yvanchuxiuzhen:guchong_03")
                && !data.getBoolean("guchongxvnfu") && captured.getHoverName().getString().equals("飞蝗样本"));
        boolean ratings = true;
        for (String key : List.of("health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
                "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"))
            ratings &= data.getString(key + "_LEVEL").equals(wildBefore.getString(key + "_LEVEL"));
        ItemStack restored = ItemStack.parse(player.registryAccess(), captured.save(player.registryAccess())).orElseThrow();
        expectTrue("十三项资质和物品全部组件经过序列化重读保持", ratings && ItemStack.isSameItemSameComponents(captured, restored));
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, item("taoguan_01item"));
        player.getInventory().setItem(9, restored);
        useJar(player);
    }

    private void storeInJar(ServerPlayer player) {
        boolean valid = player.containerMenu instanceof TaoguanMenu menu && menu.grade() == 1
                && minecraft.screen instanceof TaoguanScreen screen && screen.getTitle().getString().equals("小蛊罐");
        expectTrue("捕获结果进入中文小蛊罐菜单", valid);
        if (!valid) throw new IllegalStateException("摄虫网检查未打开一级蛊罐，停止依赖步骤");
        TaoguanMenu menu = (TaoguanMenu) player.containerMenu;
        menu.quickMoveStack(player, 1);
        expectTrue("真实捕获物品快捷入罐且原背包槽清空", menu.getSlot(0).getItem().is(captured.getItem()) && menu.getSlot(1).getItem().isEmpty());
        minecraft.execute(() -> screenshots.take("fabao017-captured-jar-chinese.png"));
    }

    private void saveAndReopenJar(ServerPlayer player) {
        player.closeContainer();
        ItemStack jar = player.getMainHandItem();
        ItemStack restored = ItemStack.parse(player.registryAccess(), jar.save(player.registryAccess())).orElseThrow();
        CompoundTag data = GuchongJarData.read(restored);
        expectTrue("关罐及罐物品序列化重读保存捕获个体", ItemStack.isSameItemSameComponents(jar, restored)
                && data.getString("guchongid0").equals("yvanchuxiuzhen:guchong_03") && data.getDouble("health_Num0") == 7
                && data.getDouble("experience0") == 37.5 && data.getString("guchongname0").contains("飞蝗样本"));
        player.setItemInHand(InteractionHand.MAIN_HAND, restored);
        useJar(player);
    }

    private void checkJarAndCastTrap(ServerPlayer player) {
        CompoundTag insect = GuchongJarData.read(player.containerMenu.getSlot(0).getItem());
        expectTrue("重开蛊罐恢复个体资质并保留来源不读当前生命的行为", insect.getDouble("experience") == 37.5
                && insect.getString("attack_Num_LEVEL").equals(wildBefore.getString("attack_Num_LEVEL")) && !insect.contains("health_Num"));
        player.closeContainer();
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        player.getCooldowns().removeCooldown(tool.getItem());
        player.setShiftKeyDown(true);
        tool.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        expectTrue("零品阶潜行施法被拒绝且不计冷却", !player.getCooldowns().isOnCooldown(tool.getItem()) && nets(player) == 0);
        player.setShiftKeyDown(false);
        StatusEffects.add(player, "effect_002", 200, 0);
        tool.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        expectTrue("沉默拒绝普通施法且不计冷却", !player.getCooldowns().isOnCooldown(tool.getItem()) && nets(player) == 0);
        StatusEffects.remove(player, "effect_002");
        Equipment.updateRootField(tool, "item_level_num", DoubleTag.valueOf(1));
        player.setShiftKeyDown(true);
        tool.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        net = currentNet(player);
        anchor(net);
        spider = EntityType.SPIDER.create(player.serverLevel());
        spider.moveTo(net.position());
        anchor(spider);
        player.serverLevel().addFreshEntity(spider);
        cow = EntityType.COW.create(player.serverLevel());
        cow.moveTo(net.position());
        anchor(cow);
        player.serverLevel().addFreshEntity(cow);
        expectTrue("黄阶一级潜行生成困虫兜并进入400刻冷却", net.getPersistentData().getString("skill").equals("困虫兜")
                && player.getCooldowns().isOnCooldown(tool.getItem()));
    }

    private void checkTrap(ServerPlayer player) {
        expectTrue("困虫兜每20刻使节肢生物减速20级且不影响牛", spider.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                && spider.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 20 && !cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                && !net.isRemoved());
        minecraft.execute(() -> screenshots.take("fabao017-trap.png"));
    }

    private static GuchongEntity insect(ServerLevel level, String id, Vec3 position) {
        GuchongEntity insect = (GuchongEntity) GuchongSetup.entityType(id).get().create(level);
        insect.moveTo(position);
        insect.finalizeSpawn(level, level.getCurrentDifficultyAt(insect.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        anchor(insect);
        level.addFreshEntity(insect);
        return insect;
    }

    private static void anchor(Mob entity) {
        entity.setNoAi(true);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
    }

    private static void useJar(ServerPlayer player) {
        player.setShiftKeyDown(true);
        player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    private static Fabao01701Entity currentNet(ServerPlayer player) {
        List<Fabao01701Entity> found = player.serverLevel().getEntitiesOfClass(Fabao01701Entity.class, player.getBoundingBox().inflate(8));
        if (found.size() != 1) throw new IllegalStateException("摄虫网生成前置未满足：附近实体=" + found.size()
                + "，手持=" + player.getMainHandItem() + "，物品类型=" + player.getMainHandItem().getItem().getClass().getSimpleName()
                + "，沉默=" + StatusEffects.has(player, "effect_002") + "，位置=" + player.position());
        return found.getFirst();
    }

    private static int nets(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Fabao01701Entity.class, player.getBoundingBox().inflate(8)).size();
    }

    private static int count(ServerPlayer player, String id) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item(id).getItem())).mapToInt(ItemStack::getCount).sum();
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        player.getCooldowns().removeCooldown(tool.getItem());
        player.setShiftKeyDown(false);
        StatusEffects.remove(player, "effect_002");
        for (Entity entity : List.of(wild, owned, net, spider, cow)) if (!entity.isRemoved()) entity.discard();
        player.level().setBlock(floor, oldFloor, 3);
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
        player.server.setDifficulty(originalDifficulty, true);
    }
}
