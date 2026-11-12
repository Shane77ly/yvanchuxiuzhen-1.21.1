package net.yvanchuxiuzhen.system.cooking;

import java.text.DecimalFormat;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.api.Alcohol;
import net.yvanchuxiuzhen.system.cooking.content.WineVesselBlockEntity;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 酒物品摆坛、陈酿、取回、饮用快照与八种结算，保留来源年份与共享快照语义。 */
public final class WineRuntime {
    private static final Set<String> BLOOD_TECHNIQUES = Set.of("御血术", "汲血功", "地血大法", "血魔祭魂秘法");

    private WineRuntime() {
    }

    public static Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    public static double age(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("agenum");
    }

    public static void place(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        Level level = context.getLevel();
        if (player == null || context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos)) return;
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            BlockState state = BuiltInRegistries.BLOCK.get(id(path + "01")).defaultBlockState();
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, player.getDirection().getOpposite()), Block.UPDATE_ALL);
            WineVesselBlockEntity jar = (WineVesselBlockEntity) level.getBlockEntity(pos);
            jar.setItem(0, stack.copyWithCount(1));
            jar.changed();
            stack.shrink(1);
        }
    }

    public static void age(ServerLevel level, BlockPos pos) {
        WineVesselBlockEntity jar = (WineVesselBlockEntity) level.getBlockEntity(pos);
        ItemStack wine = jar.getItem(0).copy();
        if (!wine.isEmpty()) CustomData.update(DataComponents.CUSTOM_DATA, wine, data -> data.putDouble("agenum", data.getDouble("agenum") + 1));
        wine.setCount(1);
        jar.setItem(0, wine);
        jar.getPersistentData().putDouble("agenum", 0);
        jar.changed();
    }

    public static void useJar(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        WineVesselBlockEntity jar = (WineVesselBlockEntity) level.getBlockEntity(pos);
        ItemStack wine = jar.getItem(0).copy();
        if (player.isShiftKeyDown()) {
            wine.setCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, wine);
            jar.setItem(0, ItemStack.EMPTY);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            double years = age(wine);
            player.displayClientMessage(years < 1 ? Component.translatable("message.yvanchuxiuzhen.cooking.wine.fresh")
                    : Component.translatable("message.yvanchuxiuzhen.cooking.wine.aged", new DecimalFormat("0").format(years)), false);
        }
    }

    public static void snapshot(ServerPlayer player, ItemStack stack) {
        Cultivation.editTizhi(player, data -> {
            data.putString("jiuniangid", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            data.putDouble("agenum", age(stack));
        });
    }

    public static void drink(ServerPlayer player) {
        double years = Cultivation.tizhiDouble(player, "agenum");
        double duration = 400 + 20 * years;
        int amplifier = 0;
        int[] thresholds = {2560, 1280, 640, 320, 160, 80, 40, 20, 10};
        int[] reductions = {12800, 6400, 3200, 1600, 800, 400, 200, 100, 50};
        for (int tier = 0; tier < thresholds.length; tier++) {
            if (years >= thresholds[tier]) {
                amplifier = 9 - tier;
                duration -= reductions[tier];
                break;
            }
        }
        String wine = Cultivation.tizhiString(player, "jiuniangid");
        switch (wine) {
            case "yvanchuxiuzhen:jiuniang_001" -> {
                Alcohol.add(player, 18);
                effect(player, effect("effect_010"), duration, amplifier);
                experience(player, 5);
            }
            case "yvanchuxiuzhen:jiuniang_002" -> {
                Alcohol.add(player, 50);
                effect(player, MobEffects.LUCK, duration, amplifier);
                effect(player, MobEffects.WATER_BREATHING, duration, amplifier);
                experience(player, 5);
            }
            case "yvanchuxiuzhen:jiuniang_003" -> {
                Alcohol.add(player, 70);
                effect(player, effect("effect_017"), duration, amplifier);
                experience(player, 5);
            }
            case "yvanchuxiuzhen:jiuniang_004" -> {
                Alcohol.add(player, 35);
                effect(player, MobEffects.REGENERATION, duration, amplifier);
                effect(player, effect("effect_010"), duration, amplifier);
                effect(player, effect("effect_017"), duration, amplifier);
                experience(player, 20);
            }
            case "yvanchuxiuzhen:jiuniang_005" -> {
                Alcohol.add(player, 50);
                effect(player, effect("effect_031"), duration, amplifier);
                experience(player, 5);
            }
            case "yvanchuxiuzhen:jiuniang_006" -> {
                Alcohol.add(player, 10);
                experience(player, 15);
            }
            case "yvanchuxiuzhen:jiuniang_007" -> {
                Alcohol.add(player, 20);
                effect(player, effect("effect_025"), duration, amplifier);
                experience(player, 5);
            }
            case "yvanchuxiuzhen:jiuniang_008" -> {
                String technique = Techniques.main(player).map(book -> book.definition().name()).orElse("");
                if (BLOOD_TECHNIQUES.contains(technique)) {
                    experience(player, 40);
                    effect(player, effect("effect_013"), duration, 0);
                    player.removeEffect(effect("effect_004"));
                } else {
                    effect(player, effect("duyao_08"), 2 * duration, amplifier);
                }
            }
            default -> {
            }
        }
        double tolerance = 100 + Cultivation.tizhiDouble(player, "AlcoholPatience");
        tolerance += tolerance * Cultivation.tizhiDouble(player, "AlcoholPatiencePercentage");
        if (Alcohol.amount(player) >= tolerance) {
            effect(player, MobEffects.CONFUSION, Math.floor(12 * Alcohol.amount(player)), 0);
        }
    }

    private static void experience(ServerPlayer player, double amount) {
        Cultivation.add(player, CultivationValue.EXPERIENCE_STAND, amount);
    }

    private static Holder<MobEffect> effect(String path) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(id(path)).orElseThrow();
    }

    private static void effect(ServerPlayer player, Holder<MobEffect> effect, double duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, (int) duration, amplifier));
    }

    /** 来源 BREAK 保留完整掉落，爆炸之后依 x/y/z 顺序覆盖七格立方范围的方块为火。 */
    public static void ignite(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel level)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.LAVA, x, y + 0.5, z, 20, 0, 0, 0, 1);
        Explosion explosion = new Explosion(level, null, x, y, z, 4, false, Explosion.BlockInteraction.DESTROY);
        if (!EventHooks.onExplosionStart(level, explosion)) {
            explosion.explode();
            explosion.finalizeExplosion(false);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(x, y, z) < 4096) {
                    player.connection.send(new ClientboundExplodePacket(x, y, z, 4, explosion.getToBlow(),
                            explosion.getHitPlayers().get(player), explosion.getBlockInteraction(), explosion.getSmallExplosionParticles(),
                            explosion.getLargeExplosionParticles(), explosion.getExplosionSound()));
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    level.setBlock(pos.offset(dx, dy, dz), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
