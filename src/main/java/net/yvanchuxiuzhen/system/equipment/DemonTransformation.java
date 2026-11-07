package net.yvanchuxiuzhen.system.equipment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData.DemonTimer;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.DemonTransformationEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 妖化的背包搬移、四部位数值、头盔倒计时与分刻消散；冷却由 xiulian 的每秒入口递减。 */
public final class DemonTransformation {
    private static final TagKey<Item> ARMOR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaohuaarmor"));
    private static final List<Ending> ENDINGS = new ArrayList<>();

    private static final class Ending {
        private final ServerLevel level;
        private final ServerPlayer player;
        private int ticks;

        private Ending(ServerLevel level, ServerPlayer player) {
            this.level = level;
            this.player = player;
        }
    }

    private DemonTransformation() {
    }

    static void request(DemonTransformationEvent event) {
        ServerPlayer player = event.player();
        if (!eligible(player)) {
            message(player, "§4需要境界达到金丹期以上，并且身具妖化类金丹！");
            return;
        }
        double cooldown = Cultivation.tizhiDouble(player, "yaohualengqve");
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ARMOR) && cooldown <= 0) {
            player.serverLevel().playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaohua")), SoundSource.NEUTRAL, 1, 1);
            begin(player);
        } else {
            message(player, "§4妖化冷却剩余§b" + new DecimalFormat("0").format(cooldown) + "§4秒");
        }
        player.closeContainer();
    }

    private static boolean eligible(ServerPlayer player) {
        for (String key : new String[]{"tizhiopen_203", "tizhiopen_106", "tizhiopen_107", "tizhiopen_108", "tizhiopen_109"}) {
            if (Cultivation.tizhiBoolean(player, key)) return true;
        }
        return false;
    }

    private static void begin(ServerPlayer player) {
        PlayerInvWrapper inventory = new PlayerInvWrapper(player.getInventory());
        int empty = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) empty++;
        }
        if (empty < 4) {
            message(player, "§4背包中必须留下四个空位，才能进行妖化。");
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack previous = player.getItemBySlot(slot).copyWithCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, previous);
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            player.setItemSlot(slot, armor(player, slot));
        }
        Cultivation.setTizhiDouble(player, "yaohualengqve", 120);
        double y = player.getY() + player.getBbHeight() * .5;
        for (int angle = 0; angle < 360; angle += 18) {
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            player.serverLevel().sendParticles(Particles.type("smokecrimson"), player.getX() + 2 * x, y, player.getZ() + 2 * z, 0, .5 * x, 0, .5 * z, .5);
        }
        player.serverLevel().sendParticles(Particles.type("starred"), player.getX(), y, player.getZ(), 30, 0, 0, 0, .5);
        player.serverLevel().sendParticles(Particles.type("smokedeeppurple"), player.getX(), y, player.getZ(), 20, 0, 0, 0, .4);
        player.serverLevel().sendParticles(Particles.type("smokecrimson"), player.getX(), y, player.getZ(), 20, 0, 0, 0, .3);
    }

    private static ItemStack armor(ServerPlayer player, EquipmentSlot slot) {
        double adjustment = Cultivation.tizhiBoolean(player, "tizhiopen_109") ? .5
                : Cultivation.tizhiBoolean(player, "tizhiopen_108") ? .2
                : Cultivation.tizhiBoolean(player, "tizhiopen_107") ? 0
                : Cultivation.tizhiBoolean(player, "tizhiopen_106") ? -.5 : 0;
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        double resistance = slot == EquipmentSlot.CHEST ? adjustment : .85;
        double speed = slot == EquipmentSlot.CHEST ? adjustment : .7;
        String part = switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> throw new IllegalArgumentException("妖化部位：" + slot);
        };
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaohua_0101_" + part)));
        stack.set(EquipmentData.STATS.get(), new EquipmentStats(
                (int) Math.round(level * .7 * (.95 + adjustment)),
                (int) Math.round((level * .55 + .4) * (1.05 + adjustment)),
                (int) Math.round((level * .55 + .4) * (1.15 + adjustment)),
                (int) Math.round((level * .42 + 1.2) * (.95 + adjustment)),
                (int) Math.round((level * .42 + 1.2) * (.95 + adjustment)),
                (int) Math.round((level * .42 + 1.2) * (1 + adjustment)),
                ((level * .1 + .4) * resistance) / 100,
                ((level * .1 + .4) * speed) / 100,
                (level * 1.25 * speed) / 100));
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        stack.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        stack.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        return stack;
    }

    static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof DemonArmorItem)) return;
        DemonTimer timer = helmet.getOrDefault(EquipmentData.DEMON_TIMER.get(), DemonTimer.EMPTY);
        double ticks = timer.ticks() + 1;
        double seconds = timer.seconds();
        if (ticks >= 20) {
            seconds++;
            ticks = 0;
            if (seconds <= 30) player.displayClientMessage(Component.literal("§6变身剩余§b" + new DecimalFormat("0").format(30 - seconds) + "§6秒"), true);
        }
        boolean ending = timer.ending();
        if (seconds >= 30) {
            if (!ending) {
                ending = true;
                finish(player);
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, false, false));
        }
        helmet.set(EquipmentData.DEMON_TIMER.get(), new DemonTimer(ticks, seconds, ending));
    }

    private static void finish(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        remove(level, player, EquipmentSlot.FEET, 4);
        ENDINGS.add(new Ending(level, player));
    }

    /** 结束回调捕获原玩家及世界；五、十、十五刻时移除当时槽内的物品，不补自动穿回。 */
    static void endTick(ServerTickEvent.Post event) {
        ENDINGS.removeIf(ending -> {
            ending.ticks++;
            if (ending.ticks == 5) remove(ending.level, ending.player, EquipmentSlot.LEGS, 8);
            if (ending.ticks == 10) remove(ending.level, ending.player, EquipmentSlot.CHEST, 12);
            if (ending.ticks == 15) remove(ending.level, ending.player, EquipmentSlot.HEAD, 16);
            return ending.ticks >= 15;
        });
    }

    static void stopped(ServerStoppedEvent event) {
        ENDINGS.clear();
    }

    private static void remove(ServerLevel level, ServerPlayer player, EquipmentSlot slot, int height) {
        level.sendParticles(Particles.type("brokencrimson"), player.getX(), player.getY() + height, player.getZ(), 20, 0, 0, 0, .5);
        player.setItemSlot(slot, ItemStack.EMPTY);
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
