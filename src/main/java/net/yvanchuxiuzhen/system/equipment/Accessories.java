package net.yvanchuxiuzhen.system.equipment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.FlightEnchantmentEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 饰品自然菜单、已保存槽位的飞行减耗与福气腰佩粒子。 */
public final class Accessories {
    private Accessories() {
    }

    public static void open(ServerPlayer player) {
        open(player, player.blockPosition().above());
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new AccessoryMenu(id, inventory, pos),
                Component.literal("PlayerGUI03")), pos);
    }

    static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.containerMenu instanceof AccessoryMenu menu) menu.initialize(player);
        ItemStack belt = Equipment.accessories(player).belt();
        if (!belt.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fuqi_01")))) return;
        long time = player.level().getDayTime();
        double timer = Cultivation.tizhiDouble(player, "shipintick");
        if (time - timer >= 10) {
            player.serverLevel().sendParticles(Particles.type("fuqi_0101"), player.getX(), player.getY() + 1, player.getZ(), 2, 1, 1, 1, 0);
            Cultivation.setTizhiDouble(player, "shipintick", time);
        } else if (time < timer) {
            Cultivation.setTizhiDouble(player, "shipintick", time);
        }
    }

    static void flight(FlightEnchantmentEvent event) {
        for (ItemStack stack : Equipment.accessories(event.player()).items()) {
            event.add(Enchants.level(event.player(), stack, Enchants.key("flying")));
        }
    }
}
