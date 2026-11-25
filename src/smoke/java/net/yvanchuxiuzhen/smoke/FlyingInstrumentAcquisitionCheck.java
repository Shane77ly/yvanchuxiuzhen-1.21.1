package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.content.FlyingInstrumentEntity;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 从结构商人的购买选项与传承灵物实际发奖，追踪死亡物品经原生碰撞进入背包后的使用。 */
final class FlyingInstrumentAcquisitionCheck extends SystemCheck {
    private static final Item LEAF = item("flyinginstrument_0101");
    private static final Item DISC = item("flyinginstrument_0201");
    private static final Item PRICE = item("cailiao_193");
    private final List<Entity> created = new ArrayList<>();
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Consumer<ItemEntityPickupEvent.Post> observer = this::picked;
    private UUID owner;
    private Entity merchant;
    private FlyingInstrumentEntity dead;
    private ItemEntity drop;
    private ItemStack expected;
    private Item current;
    private double power;
    private boolean witnessed;
    private boolean ready;
    private GameType mode;
    private int selected;
    private double level;
    private Vec3 origin;
    private boolean shifting;
    private CompoundTag journal, session, commerce;

    FlyingInstrumentAcquisitionCheck(List<String> report, String levelId) {
        super("飞行法器取得与自然拾取", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        boolean[] advance = {true};
        onServer(player -> {
            try {
                switch (step) {
                    case 0 -> prepare(player);
                    case 1, 2 -> {
                        if (!(player.containerMenu instanceof DialogueMenu) && local <= 100) advance[0] = false;
                        else purchase(player, step == 2);
                    }
                    case 3 -> deployAndKill(player, LEAF, "flyinginstrument_01", 26, 280);
                    case 4, 6 -> {
                        if (!witnessed && local < 160) {
                            if (drop.isAlive() && player.distanceToSqr(drop) > 0.8) {
                                player.connection.teleport(drop.getX(), drop.getY(), drop.getZ(), player.getYRot(), player.getXRot());
                            }
                            advance[0] = false;
                        } else recover(player);
                    }
                    case 5 -> {
                        inheritance(player);
                        deployAndKill(player, DISC, "flyinginstrument_02", 30, 260);
                    }
                    case 7 -> { cleanup(player); finish(); }
                    default -> throw new IllegalStateException("未知步骤 " + step);
                }
            } catch (RuntimeException ex) {
                fail("步骤 " + step + "：" + ex);
                if (ready) cleanup(player);
                finish();
            }
        });
        return advance[0];
    }

    private void prepare(ServerPlayer p) {
        require(p.containerMenu == p.inventoryMenu && !p.isPassenger(), "须使用空闲且未骑乘的专项玩家");
        owner = p.getUUID();
        mode = p.gameMode.getGameModeForPlayer();
        selected = p.getInventory().selected;
        level = Cultivation.get(p, CultivationValue.LEVEL_NUMBER);
        shifting = p.isShiftKeyDown();
        origin = p.position();
        journal = DialogueActions.journal(p);
        session = DialogueActions.snapshot(p);
        commerce = p.getData(CommerceData.SESSION).copy();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
        NeoForge.EVENT_BUS.addListener(observer);
        ready = true;
        p.setData(QuestData.JOURNAL, new CompoundTag());
        p.setData(QuestData.SESSION, new CompoundTag());
        p.setGameMode(GameType.SURVIVAL);
        p.setShiftKeyDown(false);
        p.getInventory().clearContent();
        p.getInventory().selected = 8;
        p.getInventory().setItem(0, new ItemStack(PRICE, 11));
        var template = p.server.getStructureManager().get(id("fanhuacheng11")).orElseThrow();
        var entries = template.save(new CompoundTag()).getList("entities", Tag.TAG_COMPOUND);
        var matches = entries.stream().map(tag -> ((CompoundTag) tag).getCompound("nbt"))
                .filter(nbt -> nbt.getString("id").equals(id("businessman_23").toString())
                        && nbt.getCompound("neoforge:attachments").getCompound("yvanchuxiuzhen:npc_profile")
                                .getList("scripts", Tag.TAG_STRING).stream()
                                .findFirst().map(Tag::getAsString).orElse("").startsWith("陈叶/chenyeduihua/")).toList();
        require(matches.size() == 1, "模板中没有唯一的陈叶任务居民");
        CompoundTag resident = matches.getFirst().copy();
        resident.remove("UUID");
        merchant = EntityType.create(resident, p.serverLevel()).orElseThrow();
        merchant.moveTo(p.getX() + 1, p.getY(), p.getZ(), 0, 0);
        require(p.serverLevel().addFreshEntity(merchant), "陈叶未进入世界");
        created.add(merchant);
        p.interactOn(merchant, InteractionHand.MAIN_HAND);
    }

    private void purchase(ServerPlayer p, boolean paid) {
        for (int i = 0; i < 6; i++) click(p, false);
        require(DialogueActions.snapshot(p).getString("daantxt1").equals("购买"), "首个选项不是购买");
        click(p, true);
        expect(paid ? "足额扣12枚三级灵石" : "11枚不足时不扣费", paid ? 0 : 11, p.getInventory().countItem(PRICE));
        expect(paid ? "陈叶交付一件飞叶" : "不足额不发飞叶", paid ? 1 : 0, p.getInventory().countItem(LEAF));
        if (!paid) {
            require(p.containerMenu == p.inventoryMenu, "拒购后任务未结束");
            p.getInventory().getItem(0).grow(1);
            p.interactOn(merchant, InteractionHand.MAIN_HAND);
        } else {
            click(p, false);
            click(p, false);
            require(DialogueActions.finished(p, "chenyeduihua"), "购买后任务未完成");
            merchant.discard();
        }
    }

    private static void click(ServerPlayer p, boolean choice) {
        require(p.containerMenu instanceof DialogueMenu menu && menu.choices == choice && menu.clickMenuButton(p, 0),
                "陈叶对话按钮或会话不符");
    }

    private void inheritance(ServerPlayer p) {
        Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 12);
        p.getInventory().selected = 8;
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("ling_18")));
        for (int i = 1; i <= 12; i++) {
            use(p);
            require(data(p.getMainHandItem()).getDouble("yikaiqi") == i, "传承灵物未推进到第" + i + "份");
            require(p.getInventory().countItem(DISC) == (i == 12 ? 1 : 0), "银盘领取份次或数量错误：" + i);
        }
        expect("新传承灵物第十二次实际交付银盘", 1, p.getInventory().countItem(DISC));
    }

    private void deployAndKill(ServerPlayer p, Item reward, String entityId, int health, int capacity) {
        current = reward;
        select(p, reward, null);
        dead = summon(p);
        require(dead.getType() == BuiltInRegistries.ENTITY_TYPE.get(id(entityId)) && dead.getHealth() == health
                && dead.power() == capacity, "首次召唤实体类型或初始生命、储灵不符");
        p.interactOn(dead, InteractionHand.MAIN_HAND);
        require(p.getVehicle() == dead, "空手交互未骑上奖励法器");
        p.stopRiding();
        power = capacity - 17.25;
        dead.setPower(power);
        var area = dead.getBoundingBox().inflate(4);
        Set<UUID> prior = p.serverLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .map(Entity::getUUID).collect(Collectors.toSet());
        dead.hurt(p.damageSources().generic(), 10000);
        require(!dead.isAlive(), "法器未死亡");
        var newDrops = p.serverLevel().getEntitiesOfClass(ItemEntity.class, area, e -> !prior.contains(e.getUUID()));
        created.addAll(newDrops);
        var matches = newDrops.stream().filter(e -> e.getItem().is(current)
                && Equipment.flyingInstrument(e.getItem()).health() == 1).toList();
        require(matches.size() == 1, "死亡没有唯一的一点生命专有掉落");
        drop = matches.getFirst();
        expected = drop.getItem().copy(); // 仅作组件比较，不作为背包或重召输入。
        require(expected.getCount() == 1 && Equipment.flyingInstrument(expected).power() == power && drop.hasPickUpDelay()
                && p.getInventory().countItem(current) == 0, "拾取前数量、储灵或延迟错误");
        witnessed = false;
    }

    private void picked(ItemEntityPickupEvent.Post event) {
        if (drop != null && drop.getUUID().equals(event.getItemEntity().getUUID()) && owner.equals(event.getPlayer().getUUID())) {
            witnessed = event.getCurrentStack().isEmpty() && ItemStack.matches(expected, event.getOriginalStack());
        }
    }

    private void recover(ServerPlayer p) {
        require(witnessed && p.serverLevel().getEntity(drop.getUUID()) == null, "未观察到该玩家自然拾取整件掉落并移除该UUID：" + drop.getUUID());
        select(p, current, expected);
        var replacement = summon(p);
        expectTrue("拾取 " + drop.getUUID() + " 后从背包重召 " + replacement.getUUID(), replacement.getType() == dead.getType()
                && !replacement.getUUID().equals(dead.getUUID()) && replacement.getHealth() == 1
                && replacement.power() == power);
        replacement.discard();
        drop = null;
    }

    private static void select(ServerPlayer p, Item kind, ItemStack reference) {
        var inv = p.getInventory();
        int found = -1;
        for (int i = 0; i < inv.items.size(); i++) {
            var stack = inv.getItem(i);
            if (!stack.is(kind) || reference != null && !ItemStack.matches(stack, reference)) continue;
            require(found == -1 && stack.getCount() == 1, "背包中候选不唯一");
            found = i;
        }
        require(found >= 0, "服务端背包中缺少实际法器");
        if (found < 9) inv.selected = found;
        else inv.pickSlot(found);
    }

    private FlyingInstrumentEntity summon(ServerPlayer p) {
        var area = p.getBoundingBox().inflate(8);
        Set<UUID> prior = p.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area).stream()
                .map(Entity::getUUID).collect(Collectors.toSet());
        use(p);
        var summoned = p.serverLevel().getEntitiesOfClass(FlyingInstrumentEntity.class, area, e -> !prior.contains(e.getUUID()));
        created.addAll(summoned);
        require(summoned.size() == 1 && p.getMainHandItem().isEmpty(), "主手施放未消耗一件并产生唯一实体");
        return summoned.getFirst();
    }

    private static void use(ServerPlayer p) {
        p.gameMode.useItem(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.get(id(path)); }
    private static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException(reason);
    }

    private void cleanup(ServerPlayer p) {
        NeoForge.EVENT_BUS.unregister(observer);
        ready = false;
        p.closeContainer();
        created.forEach(Entity::discard);
        p.getInventory().clearContent();
        for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        p.getInventory().selected = selected;
        p.setGameMode(mode);
        p.setShiftKeyDown(shifting);
        p.connection.teleport(origin.x, origin.y, origin.z, p.getYRot(), p.getXRot());
        Cultivation.set(p, CultivationValue.LEVEL_NUMBER, level);
        p.setData(QuestData.JOURNAL, journal);
        p.setData(QuestData.SESSION, session);
        p.setData(CommerceData.SESSION, commerce);
        p.inventoryMenu.broadcastChanges();
    }
}
