package net.yvanchuxiuzhen.flow;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.book.api.BookQuizzes;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.fulu.api.ReincarnationRequestedEvent;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.LifetimeExhaustedEvent;

/** 轮回 37 的实体处理顺序；玩家模组数据由所属系统写回。 */
public final class ReincarnationFlow {
    private static Consumer<ServerPlayer> panelReset;
    private ReincarnationFlow() {
    }

    public static void register(Consumer<ServerPlayer> resetPanel) {
        panelReset = resetPanel;
        NeoForge.EVENT_BUS.addListener(ReincarnationRequestedEvent.class,
                event -> reincarnate(event.entity(), event.sourceLevel(), resetPanel));
        NeoForge.EVENT_BUS.addListener(LifetimeExhaustedEvent.class,
                event -> reincarnate(event.player(), event.sourceLevel(), resetPanel));
    }

    static void reincarnate(Entity entity, ServerLevel sourceLevel) {
        reincarnate(entity, sourceLevel, panelReset);
    }

    private static void reincarnate(Entity entity, ServerLevel sourceLevel, Consumer<ServerPlayer> resetPanel) {
        if (!(entity instanceof ServerPlayer player) || entity instanceof FakePlayer) {
            reincarnateWithoutPlayerData(entity, sourceLevel);
            return;
        }
        double previousWalkSpeed = PlayerAttributes.value(player, Stat.MOVE_SPEED);
        double extraLocks = Cultivation.resetForReincarnation(player);
        Professions.reset(player);
        Skills.resetForReincarnation(player);
        Techniques.resetForReincarnation(player);
        Equipment.clearReincarnationAccessories(player);
        BookQuizzes.clearQuestion(player);
        Dungeons.clearPaintingTransit(player);
        resetPanel.accept(player);

        player.removeAllEffects();
        player.clearFire();
        player.fallDistance = 0;
        clearInventory(player);
        Equipment.refreshContributions(player);
        Relations.refreshEntityTeam(player);
        Cultivation.finishReincarnation(player, sourceLevel, extraLocks);
        PlayerAttributes.applyReincarnationBases(player, previousWalkSpeed);
    }

    /** 来源仅给真实玩家挂载玩家能力；其他实体保留原版属性和装备副作用。 */
    private static void reincarnateWithoutPlayerData(Entity entity, ServerLevel sourceLevel) {
        LivingEntity living = (LivingEntity) entity;
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
        living.removeAllEffects();
        entity.clearFire();
        entity.fallDistance = 0;
        if (entity instanceof ServerPlayer player) {
            clearInventory(player);
        } else {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
                    EquipmentSlot.HEAD, EquipmentSlot.OFFHAND}) {
                living.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        Cultivation.rollNonPlayerReincarnationRoots(sourceLevel);
        EntityAttributes.setLevel(entity, 0);
        Relations.setTeam(entity, "");
        if (entity instanceof ServerPlayer player) Cultivation.openBirthAfterReincarnation(player);
    }

    /** Forge 40.1.0 无方向玩家处理器依次读主库存、护甲、左手；每次用当前槽的物品与数量执行原版清理。 */
    private static void clearInventory(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        if (player.isAlive()) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;
                Item item = stack.getItem();
                inventory.clearOrCountMatchingItems(candidate -> candidate.is(item), stack.getCount(), player.inventoryMenu.getCraftSlots());
            }
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.OFFHAND}) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.inventoryMenu.broadcastChanges();
    }
}
