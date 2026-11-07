package net.yvanchuxiuzhen.system.equipment;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ItemSwingEvent;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.projectile.Fuchen;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;
import net.yvanchuxiuzhen.system.equipment.projectile.KailingProjectile;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 弓的选中蓄力与发射、拂尘的选中冷却及挥动入口；玩法状态经所属系统公开操作写入。 */
final class EquipmentWeapons {
    private static final Set<String> BOWS = Set.of(
            "weaponspeedbow_01", "weaponspeedbow_02", "weaponspeedbow_03", "weaponspeedbow_04", "weaponspeedbow_05",
            "weaponspeedbow_06", "weaponspeedbow_07", "weaponspeedbow_08", "weaponspeedbow_09",
            "weaponlongbow_01", "weaponlongbow_02", "weaponlongbow_03", "weaponlongbow_04", "weaponlongbow_05",
            "weaponlongbow_06", "weaponlongbow_07", "weaponlongbow_08", "weaponlongbow_09");
    private static final Set<String> HOSSUS = Set.of(
            "weaponhossu_01", "weaponhossu_02", "weaponhossu_03", "weaponhossu_04", "weaponhossu_05",
            "weaponhossu_06", "weaponhossu_07", "weaponhossu_08", "weaponhossu_09");
    private static final TagKey<Item> SPEED_BOW = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "erspeedbow"));

    private EquipmentWeapons() {
    }

    static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ResourceLocation held = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        if (!held.getNamespace().equals(ModId.VALUE)) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer && HOSSUS.contains(held.getPath())) {
            double remaining = Cultivation.tizhiDouble(player, "fuchenshiyongtick");
            if (remaining >= 1) {
                Cultivation.setTizhiDouble(serverPlayer, "fuchenshiyongtick", remaining - 1);
            }
        } else if (BOWS.contains(held.getPath())) {
            ItemStack selected = Skills.arrow(player);
            if (!selected.isEmpty() && available(player, selected)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    bow(serverPlayer, selected);
                } else if (rightClicking(player)) {
                    chargeParticles(player);
                }
            }
        }
    }

    static void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()
                || Enchants.mainHand(player, Enchants.key("kailing")) == 0 || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            return;
        }
        ToolMachineEntity.Kind kind = toolKind(player.getMainHandItem());
        if (kind != null) {
            KailingProjectile.shoot(player.serverLevel(), player, kind);
            player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 100);
        }
    }

    static void swing(ItemSwingEvent event) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.stack().getItem());
        if (!(event.entity() instanceof ServerPlayer player) || !id.getNamespace().equals(ModId.VALUE) || !HOSSUS.contains(id.getPath())) {
            return;
        }
        double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 2 + 2;
        if (Cultivation.tizhiDouble(player, "fuchenshiyongtick") < 1 && Cultivation.get(player, CultivationValue.POWER) >= cost) {
            Fuchen.shoot(player, .5f, 10, .1f);
            Cultivation.add(player, CultivationValue.POWER, -cost);
            Cultivation.setTizhiDouble(player, "fuchenshiyongtick", 20);
            wear(player, event.stack());
        }
    }

    private static boolean rightClicking(Player player) {
        return player.getPersistentData().getCompound("synchronizedata").getBoolean("rightclicking");
    }

    private static ToolMachineEntity.Kind toolKind(ItemStack stack) {
        return stack.getItem() instanceof AxeItem ? ToolMachineEntity.Kind.AXE
                : stack.getItem() instanceof PickaxeItem ? ToolMachineEntity.Kind.PICKAXE
                : stack.getItem() instanceof HoeItem ? ToolMachineEntity.Kind.HOE
                : stack.getItem() instanceof ShovelItem ? ToolMachineEntity.Kind.SHOVEL : null;
    }

    private static boolean available(Player player, ItemStack selected) {
        return kind(selected) == 2 && Cultivation.get(player, CultivationValue.POWER) >= Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 5
                || player.getInventory().contains(selected);
    }

    private static int kind(ItemStack selected) {
        if (selected.is(Items.ARROW)) return 0;
        if (selected.is(Items.SPECTRAL_ARROW)) return 10;
        for (int kind = 1; kind <= 9; kind++) {
            if (selected.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_arrow_%02d".formatted(kind))))) {
                return kind;
            }
        }
        return -1;
    }

    private static void bow(ServerPlayer player, ItemStack selected) {
        int kind = kind(selected);
        double charge = Cultivation.tizhiDouble(player, "lagongtick");
        if (rightClicking(player)) {
            double speed = 3 + Enchants.mainHand(player, Enchantments.QUICK_CHARGE);
            if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) >= 5 && player.isShiftKeyDown()
                    && (kind == 0 || kind == 1 || kind == 2 || kind == 8 || kind == 9 || kind == 10)) {
                speed++;
            }
            if (charge < 100) {
                Cultivation.setTizhiDouble(player, "lagongtick", charge + speed * (player.getMainHandItem().is(SPEED_BOW) ? 1 : .5));
            } else if (!Cultivation.tizhiBoolean(player, "manxvli")) {
                player.serverLevel().sendParticles(Particles.type("xvliman"), player.getX(), player.getY() + 1.5, player.getZ(), 1, 0, 0, 0, 0);
                sound(player, ModId.VALUE + ":bowxvliman", 1, 1.5);
                Cultivation.setTizhiBoolean(player, "manxvli", true);
            }
        } else if (charge > 0) {
            double feather = Enchants.mainHand(player, Enchants.key("qingyv"));
            Cultivation.setTizhiBoolean(player, "arrowskill", true);
            wear(player, player.getMainHandItem());
            boolean infinity = Enchants.mainHand(player, Enchantments.INFINITY) > 0;
            if (kind >= 0) {
                sound(player, switch (kind) {
                    case 2 -> ModId.VALUE + ":bowshoot3";
                    case 3 -> ModId.VALUE + ":bowshoot4";
                    case 4 -> ModId.VALUE + ":bowshoot5";
                    case 5 -> ModId.VALUE + ":bowshoot6";
                    case 6, 7, 8, 9 -> ModId.VALUE + ":bowshoot1";
                    default -> "minecraft:entity.arrow.shoot";
                }, kind == 3 ? 1.2f : kind >= 6 && kind <= 9 ? .3f : 1, 1.3);
                if (kind == 4) sound(player, ModId.VALUE + ":bowshoot1", .3f, 1.3);
            }
            AbstractArrow arrow = switch (kind) {
                case 0 -> ordinaryArrow(player);
                case 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> Skills.createArrow(player, kind, kind == 3 || kind == 4 || kind == 6 ? 1 : 0, 0,
                        ModifierKeys.ctrl(player), ModifierKeys.alt(player), Cultivation.tizhiBoolean(player, "manxvli"), infinity);
                default -> null;
            };
            if (arrow != null) {
                double base = kind == 0 || kind == 6 ? PlayerAttributes.value(player, Stat.ATTACK) / 3 : kind == 1 ? 1 : .1;
                if (kind == 0 && ModifierKeys.alt(player)) base *= 1.5;
                arrow.setBaseDamage((float) base);
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                arrow.setPos(player.getX(), player.getEyeY() - .1, player.getZ());
                Vec3 direction = player.getLookAngle();
                float velocity = (float) ((player.getMainHandItem().is(SPEED_BOW) ? 3 : 6) * Math.min(charge, 100) * .01 + feather / 2);
                arrow.shoot(direction.x, direction.y, direction.z, velocity, 0);
                player.serverLevel().addFreshEntity(arrow);
            }
            Cultivation.setTizhiDouble(player, "lagongtick", 0);
            if (kind == 2) {
                double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 5;
                Cultivation.add(player, CultivationValue.POWER, -cost);
                if (Cultivation.get(player, CultivationValue.POWER) < cost) clearArrow(player);
            } else {
                if (!infinity) {
                    player.getInventory().clearOrCountMatchingItems(stack -> stack.is(selected.getItem()), 1, player.inventoryMenu.getCraftSlots());
                }
                if (!player.getInventory().contains(selected)) clearArrow(player);
            }
        }
    }

    /** 1.21.1 用发射物的武器快照承载固定击退与穿透；只放入这两项效果，箭仍使用原版注册与保存格式。 */
    private static Arrow ordinaryArrow(ServerPlayer player) {
        ItemStack effects = new ItemStack(Items.BOW);
        effects.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.PUNCH), 1);
        if (ModifierKeys.ctrl(player)) {
            effects.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.PIERCING), 5);
        }
        return new Arrow(player.serverLevel(), player, new ItemStack(Items.ARROW), effects);
    }

    private static void clearArrow(ServerPlayer player) {
        Skills.setArrow(player, ItemStack.EMPTY, Skills.arrowChoice(player));
    }

    /** 来源耐久入口未传入玩家，创造模式同样消耗；破损后清空伤害值。 */
    private static void wear(ServerPlayer player, ItemStack stack) {
        stack.hurtAndBreak(1, player.serverLevel(), (ServerPlayer) null, item -> stack.setDamageValue(0));
    }

    private static void sound(ServerPlayer player, String id, float volume, double lift) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
        player.serverLevel().playSound(null, BlockPos.containing(player.getX(), player.getY() + lift, player.getZ()), sound, SoundSource.NEUTRAL, volume, 1);
    }

    /** 来源的 addParticle 只在客户端生成本地蓄力粒子；不转为服务端广播。 */
    private static void chargeParticles(Player player) {
        for (int i = 0; i < Math.round(Cultivation.tizhiDouble(player, "lagongtick") / 20); i++) {
            Vec3 at = player.position().add(player.getRandom().nextDouble() * 4 - 2, 1 + player.getRandom().nextDouble() * 4 - 2,
                    player.getRandom().nextDouble() * 4 - 2);
            Vec3 motion = player.position().add(0, 1, 0).subtract(at).scale(.5);
            player.level().addParticle(Particles.type("xvlilizi"), at.x, at.y, at.z, motion.x, motion.y, motion.z);
        }
    }
}
