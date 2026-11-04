package net.yvanchuxiuzhen.system.combat.enchant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;

/**
 * 穿石（来源 {@code LeftClickBlock} 中带 {@code chuanshi} 的镐分支）：主手镐有穿石时左键方块给自己急迫 II（100 刻）；
 * 点到的是基岩且其下方不是虚空空气（最底层不可挖）时，把基岩换成 {@code bedrock_01} 并让镐损耗 10 点。
 * 之后三个阶段的方块每次被破坏降一级，见 {@code CrackedBedrockBlock}。
 */
public final class BedrockCracking {
    private BedrockCracking() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BedrockCracking::onLeftClick);
    }

    private static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide() || !(player.getMainHandItem().getItem() instanceof PickaxeItem) || Enchants.mainHand(player, Enchants.key("chuanshi")) == 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 1));
        BlockPos pos = event.getPos();
        if (level.getBlockState(pos.below()).getBlock() != Blocks.VOID_AIR && level.getBlockState(pos).is(Blocks.BEDROCK)) {
            level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "bedrock_01")).defaultBlockState(), 3);
            // 来源的 hurt(10, random, null) 不带玩家，创造模式也损耗。
            player.getMainHandItem().hurtAndBreak(10, (ServerLevel) level, (LivingEntity) null, item -> {
            });
        }
    }
}
