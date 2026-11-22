package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/**
 * 穿界古镜（fabao_013）主路径：主世界右键传送到人间道并保存原坐标；人间道右键返回主世界并保存人间道坐标；
 * 同一物品两个方向各自使用上次落点；沉默时不生效也不计冷却；落点占用时按体质标记向上推出，空位只保留标记。
 * 来源以 {@code execute in … run teleport @p} 执行，这里用同义的跨维度传送并核对落点。
 */
final class Fabao013Check extends ActionCheck {
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "dimensionrenjian"));

    private ServerLevel originalLevel;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private boolean originalMark;
    private ItemStack tool;

    Fabao013Check(List<String> report, String levelId) {
        super("穿界古镜", report, levelId);
        script(
                new Action(12, player -> {
                    originalLevel = player.serverLevel();
                    originalPosition = player.position();
                    originalYaw = player.getYRot();
                    originalPitch = player.getXRot();
                    originalMark = Cultivation.tizhiBoolean(player, "kaqiangpanding");
                    Cultivation.setTizhiBoolean(player, "kaqiangpanding", false);
                    player.getInventory().clearContent();
                    tool = item("fabao_013");
                    player.setItemInHand(InteractionHand.MAIN_HAND, tool);
                    player.getCooldowns().removeCooldown(tool.getItem());
                    player.connection.teleport(7.5, 100, -3.5, -90, 0);
                }),
                new Action(4, player -> {
                    use(player);
                    expectTrue("主世界右键进入人间道并保存主世界坐标",
                            player.level().dimension() == RENJIAN
                                    && player.position().x() == 0 && player.position().y() == 120 && player.position().z() == 0
                                    && tag(player).getDouble("chuansongX") == 7.5
                                    && tag(player).getDouble("chuansongY") == 100
                                    && tag(player).getDouble("chuansongZ") == -3.5
                                    && player.getCooldowns().isOnCooldown(tool.getItem())
                                    && Cultivation.tizhiBoolean(player, "kaqiangpanding"));
                    reloadTool(player);
                }),
                new Action(2, player -> {
                    player.getCooldowns().removeCooldown(tool.getItem());
                    player.connection.teleport(21.5, 64, 18.5, 0, 0);
                    use(player);
                    expectTrue("人间道右键返回主世界并保存人间道坐标",
                            player.level().dimension() == Level.OVERWORLD
                                    && player.position().x() == 7.5 && player.position().y() == 100 && player.position().z() == -3.5
                                    && tag(player).getDouble("mubiaoX") == 21.5
                                    && tag(player).getDouble("mubiaoY") == 64
                                    && tag(player).getDouble("mubiaoZ") == 18.5);
                    reloadTool(player);
                }),
                new Action(2, player -> {
                    player.getCooldowns().removeCooldown(tool.getItem());
                    use(player);
                    expectTrue("再次进入使用人间道上次落点",
                            player.level().dimension() == RENJIAN
                                    && player.position().x() == 21.5 && player.position().y() == 64 && player.position().z() == 18.5);
                }),
                new Action(2, player -> {
                    player.getCooldowns().removeCooldown(tool.getItem());
                    StatusEffects.add(player, "effect_002", 600, 0);
                    player.connection.teleport(2.5, 80, 2.5, 0, 0);
                    use(player);
                    expectTrue("沉默时右键不传送也不计冷却",
                            player.level().dimension() == RENJIAN
                                    && !player.getCooldowns().isOnCooldown(tool.getItem()));
                    StatusEffects.remove(player, "effect_002");
                }),
                new Action(2, player -> {
                    Cultivation.setTizhiBoolean(player, "kaqiangpanding", true);
                    player.connection.teleport(1.5, 80, 1.5, 0, 0);
                    player.level().setBlock(player.blockPosition(), Blocks.STONE.defaultBlockState(), 2);
                    NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
                    expectTrue("落点被占用时向上推出并清除标记",
                            player.getY() >= 81 && !Cultivation.tizhiBoolean(player, "kaqiangpanding"));
                }),
                new Action(2, player -> {
                    Cultivation.setTizhiBoolean(player, "kaqiangpanding", true);
                    NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
                    expectTrue("空位不推出但保留标记", Cultivation.tizhiBoolean(player, "kaqiangpanding"));
                })
        );
    }

    private static CompoundTag tag(ServerPlayer player) {
        return player.getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void use(ServerPlayer player) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static void reloadTool(ServerPlayer player) {
        ItemStack restored = ItemStack.parse(player.registryAccess(), player.getMainHandItem().save(player.registryAccess())).orElseThrow();
        player.setItemInHand(InteractionHand.MAIN_HAND, restored);
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.getInventory().clearContent();
        player.getCooldowns().removeCooldown(item("fabao_013").getItem());
        Cultivation.setTizhiBoolean(player, "kaqiangpanding", originalMark);
        if (originalLevel != null) {
            player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        }
    }
}
