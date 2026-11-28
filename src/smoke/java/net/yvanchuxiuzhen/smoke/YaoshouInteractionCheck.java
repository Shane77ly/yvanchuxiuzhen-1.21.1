package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBagData;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;

/** 妖兽交互的来源外层门槛、物品与实体驯服度分离、喂食优先及普通反馈的必要检查。 */
final class YaoshouInteractionCheck extends SystemCheck {
    private static final TagKey<Item> FOOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "food"));
    private final List<Entity> samples = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private final Screenshots screenshots;
    private final Consumer<ClientChatReceivedEvent.System> chat = event -> {
        if (event.getMessage().getContents() instanceof TranslatableContents text
                && text.getKey().startsWith("message.yvanchuxiuzhen.yaoshou_interaction.")) {
            messages.add(event.getMessage().getString());
        }
    };
    private ItemStack mainHand;
    private ItemStack offHand;
    private GameType mode;
    private InteractionHand usedHand;
    private boolean shift;
    private boolean executed;
    private boolean visualPrepared;
    private YaoshouEntity visualSample;
    private float yaw;
    private float pitch;

    YaoshouInteractionCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("妖兽普通交互", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> {
                if (!executed) {
                    executed = true;
                    NeoForge.EVENT_BUS.addListener(chat);
                    onServer(this::check);
                }
                yield local >= 8;
            }
            case 1 -> {
                if (local < 20 || local < 100 && !feedbackComplete()) yield false;
                try {
                    expectTrue("客户端收到实际合并中文的恢复、听话与带名反馈", feedbackComplete());
                    expectTrue("普通交互消息不出现原始翻译键", messages.stream().noneMatch(text -> text.contains("message.yvanchuxiuzhen.")));
                    screenshots.take("yaoshou-interaction-messages.png");
                } finally {
                    NeoForge.EVENT_BUS.unregister(chat);
                }
                yield true;
            }
            case 2 -> {
                if (!visualPrepared) {
                    visualPrepared = true;
                    onServer(player -> {
                        visualSample = beast(player);
                        visualSample.moveTo(player.getX(), player.getY(), player.getZ() + 3);
                        player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                        selectUsedHand(player, InteractionHand.MAIN_HAND);
                        player.setShiftKeyDown(false);
                        player.setItemInHand(InteractionHand.MAIN_HAND, tamingItem(50, 2));
                    });
                }
                yield local >= 8;
            }
            case 3 -> {
                onServer(player -> player.interactOn(visualSample, InteractionHand.MAIN_HAND));
                yield true;
            }
            case 4 -> {
                if (local < 4) yield false;
                try {
                    screenshots.take("yaoshou-interaction-particles.png");
                } finally {
                    onServer(player -> {
                        samples.forEach(Entity::discard);
                        restorePlayer(player);
                    });
                }
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知妖兽交互检查步骤 " + step);
        };
    }

    private boolean feedbackComplete() {
        return messages.contains("恢复6点血量") && messages.contains("妖兽更听话了") && messages.contains("交互样本( ^ V ^ )");
    }

    private void check(ServerPlayer player) {
        mainHand = player.getMainHandItem().copy();
        offHand = player.getOffhandItem().copy();
        mode = player.gameMode.getGameModeForPlayer();
        usedHand = player.getUsedItemHand();
        shift = player.isShiftKeyDown();
        yaw = player.getYRot();
        pitch = player.getXRot();
        player.closeContainer();
        player.stopUsingItem();
        selectUsedHand(player, InteractionHand.MAIN_HAND);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        try {
            entryCases(player);
            YaoshouEntity beast = beast(player);
            beast.setOwnerUUID(UUID.randomUUID());
            tameCases(player, beast);
            foodCases(player, beast);
            handCases(player, beast);
            bagPriority(player, beast);
            nativeSave(player, beast);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.interactOn(beast, InteractionHand.MAIN_HAND);
        } finally {
            samples.forEach(Entity::discard);
            restorePlayer(player);
        }
    }

    private void restorePlayer(ServerPlayer player) {
        player.closeContainer();
        player.stopUsingItem();
        selectUsedHand(player, usedHand);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        player.setGameMode(mode);
        player.setShiftKeyDown(shift);
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private void entryCases(ServerPlayer player) {
        Wolf wolf = EntityType.WOLF.create(player.serverLevel());
        wolf.moveTo(player.getX() + 6, player.getY() + 12, player.getZ());
        wolf.tame(player);
        wolf.setNoAi(true);
        wolf.setNoGravity(true);
        samples.add(wolf);
        player.serverLevel().addFreshEntity(wolf);
        ItemStack bag = item("lingshoudai_01");
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        player.setShiftKeyDown(true);
        player.interactOn(wolf, InteractionHand.MAIN_HAND);
        expectTrue("Q226原版驯服狼被妖兽外层排除且袋无写入或体型异常", !wolf.isRemoved()
                && wolf.getAttribute(YaoshouAttributes.SIZE) == null && data(bag).isEmpty());
        wolf.discard();

        YaoshouEntity dead = beast(player);
        dead.setHealth(0);
        PlayerInteractEvent.EntityInteract deadEvent = post(player, dead, InteractionHand.MAIN_HAND);
        expectTrue("Q226死亡已驯服妖兽不进入活体袋收纳且事件未取消", !dead.isRemoved()
                && data(bag).isEmpty() && !deadEvent.isCanceled());
        dead.discard();

        player.setShiftKeyDown(false);
        YaoshouEntity wild = beast(player);
        wild.setTame(false, false);
        CreatureData.state(wild).setTameNum(7);
        ItemStack food = tamingItem(50, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, food);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        expectTrue("未驯服妖兽不进入普通喂养", CreatureData.state(wild).tameNum() == 7 && food.getCount() == 2);
        wild.discard();

        YaoshouEntity observed = beast(player);
        CreatureData.state(observed).setTameNum(7);
        ItemStack prior = item("fabao_015");
        prior.set(CreatureData.TAME_VALUE.get(), 50.0);
        player.setItemInHand(InteractionHand.MAIN_HAND, prior);
        post(player, observed, InteractionHand.MAIN_HAND);
        expect("来源前置物品分支不继续执行普通加驯服度", 7, CreatureData.state(observed).tameNum());
        observed.discard();
    }

    private void tameCases(ServerPlayer player, YaoshouEntity beast) {
        CreatureData.state(beast).setTameNum(99);
        beast.setHealth(10);
        for (double bound : new double[] {0, 100}) {
            ItemStack held = tamingItem(bound, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            player.interactOn(beast, InteractionHand.MAIN_HAND);
            expectTrue("物品驯服度严格边界" + bound + "只反馈不消费", held.getCount() == 2
                    && CreatureData.state(beast).tameNum() == 99 && beast.getHealth() == 10);
        }
        ItemStack plain = new ItemStack(Items.PAPER);
        player.setItemInHand(InteractionHand.MAIN_HAND, plain);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        expectTrue("读取无驯服度物品不写入多余自定义数据", !plain.has(DataComponents.CUSTOM_DATA)
                && !plain.has(CreatureData.TAME_VALUE.get()));
        ItemStack held = tamingItem(0.5, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        expectTrue("非主人以物品小数驯服度进入实体加五并越过100", held.getCount() == 1
                && CreatureData.state(beast).tameNum() == 104 && beast.getHealth() == 10
                && held.getOrDefault(CreatureData.TAME_VALUE.get(), 0.0) == 0.5 && !beast.isOwnedBy(player));
        held = tamingItem(99.5, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.setGameMode(GameType.CREATIVE);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        expectTrue("接近100的物品阈值仍有效且创造模式照样消耗", held.getCount() == 1
                && CreatureData.state(beast).tameNum() == 109);
        player.setGameMode(GameType.SURVIVAL);
    }

    private void foodCases(ServerPlayer player, YaoshouEntity beast) {
        Holder.Reference<Item> holder = Items.PAPER.builtInRegistryHolder();
        List<TagKey<Item>> original = holder.tags().toList();
        expectTrue("当前正式来源食物标签没有自然成员", BuiltInRegistries.ITEM.getTag(FOOD).map(tag -> tag.size() == 0).orElse(true)
                && !holder.is(FOOD));
        List<TagKey<Item>> temporary = new ArrayList<>(original);
        temporary.add(FOOD);
        try {
            Method bindTags = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
            bindTags.setAccessible(true);
            bindTags.invoke(holder, Set.copyOf(temporary));
            try {
                beast.setHealth(10);
                ItemStack held = tamingItem(50, 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
                player.interactOn(beast, InteractionHand.MAIN_HAND);
                expectTrue("临时食物成员优先恢复六点且不增加实体驯服度", beast.getHealth() == 16
                        && CreatureData.state(beast).tameNum() == 109 && held.getCount() == 1);
                beast.setHealth(97);
                held = tamingItem(50, 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
                player.interactOn(beast, InteractionHand.MAIN_HAND);
                expectTrue("六点恢复按原生最大生命裁切", beast.getHealth() == 100 && held.getCount() == 1);
                player.setGameMode(GameType.CREATIVE);
                held = tamingItem(50, 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
                player.interactOn(beast, InteractionHand.MAIN_HAND);
                expectTrue("满血和创造模式仍消费一件来源食物", beast.getHealth() == 100
                        && held.getCount() == 1 && CreatureData.state(beast).tameNum() == 109);
                player.setGameMode(GameType.SURVIVAL);
            } finally {
                bindTags.invoke(holder, Set.copyOf(original));
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法设置或恢复交互夹具的临时food成员", exception);
        }
        expectTrue("临时食物成员在同一服务端动作内完整恢复", Set.copyOf(original).equals(Set.copyOf(holder.tags().toList())));
    }

    private void handCases(ServerPlayer player, YaoshouEntity beast) {
        ItemStack held = tamingItem(50, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        post(player, beast, InteractionHand.OFF_HAND);
        expectTrue("与当前使用手不同的事件不消费主手物品", held.getCount() == 3
                && CreatureData.state(beast).tameNum() == 109);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BOW));
        player.startUsingItem(InteractionHand.OFF_HAND);
        post(player, beast, InteractionHand.MAIN_HAND);
        expectTrue("正在使用副手时主手事件被来源入口排除", player.getUsedItemHand() == InteractionHand.OFF_HAND && held.getCount() == 3);
        PlayerInteractEvent.EntityInteract accepted = post(player, beast, InteractionHand.OFF_HAND);
        expectTrue("匹配副手事件仍读取主手物品并保留原生后续交互", held.getCount() == 2
                && CreatureData.state(beast).tameNum() == 114 && player.getOffhandItem().is(Items.BOW)
                && !accepted.isCanceled());
        player.stopUsingItem();
        selectUsedHand(player, InteractionHand.MAIN_HAND);
    }

    private void bagPriority(ServerPlayer player, YaoshouEntity beast) {
        ItemStack bag = item("lingshoudai_02");
        SpiritBeastBagData stored = new SpiritBeastBagData(
                "different_bag", "", "", "", "", false, false, 0, 0, "", 0, 0, 0, 0, Map.of(), Map.of());
        bag.set(CreatureData.SPIRIT_BEAST_BAG.get(), stored);
        bag.set(CreatureData.TAME_VALUE.get(), 50.0);
        CreatureData.state(beast).setBagUuid("creature_bag");
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        player.setShiftKeyDown(true);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        expectTrue("潜行持袋先执行袋UUID分支且不落入普通加五", !beast.isRemoved() && bag.getCount() == 1
                && stored.equals(bag.get(CreatureData.SPIRIT_BEAST_BAG.get()))
                && CreatureData.state(beast).tameNum() == 114);
        player.setShiftKeyDown(false);
        player.interactOn(beast, InteractionHand.MAIN_HAND);
        expectTrue("不潜行持袋进入普通物品阈值分支", !beast.isRemoved() && bag.isEmpty()
                && CreatureData.state(beast).tameNum() == 119);
    }

    private void nativeSave(ServerPlayer player, YaoshouEntity beast) {
        CompoundTag saved = beast.saveWithoutId(new CompoundTag());
        YaoshouEntity restored = (YaoshouEntity) beast.getType().create(player.serverLevel());
        samples.add(restored);
        restored.load(saved);
        expectTrue("原生实体保存重读保留普通交互驯服度生命与原主人", CreatureData.state(restored).tameNum() == 119
                && restored.getHealth() == 100 && restored.getOwnerUUID().equals(beast.getOwnerUUID()) && restored.isTame());
    }

    private YaoshouEntity beast(ServerPlayer player) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "yaoshou_004"));
        YaoshouEntity beast = (YaoshouEntity) type.create(player.serverLevel());
        beast.moveTo(player.getX() + 6, player.getY() + 12, player.getZ());
        beast.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(beast.blockPosition()), MobSpawnType.COMMAND, null);
        beast.setNoAi(true);
        beast.setNoGravity(true);
        beast.tame(player);
        beast.setCustomName(Component.literal("交互样本"));
        beast.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        beast.setHealth(100);
        samples.add(beast);
        player.serverLevel().addFreshEntity(beast);
        return beast;
    }

    private static void selectUsedHand(ServerPlayer player, InteractionHand hand) {
        player.setItemInHand(hand, new ItemStack(Items.BOW));
        player.startUsingItem(hand);
        player.stopUsingItem();
    }

    private static PlayerInteractEvent.EntityInteract post(ServerPlayer player, Entity target, InteractionHand hand) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, hand, target));
    }

    private static ItemStack tamingItem(double itemTameness, int count) {
        ItemStack stack = new ItemStack(Items.PAPER, count);
        stack.set(CreatureData.TAME_VALUE.get(), itemTameness);
        return stack;
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
}
