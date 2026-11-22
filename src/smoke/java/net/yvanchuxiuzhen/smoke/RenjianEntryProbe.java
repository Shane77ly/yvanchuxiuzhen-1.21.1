package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 固定场景证据附着在主世界点门、真实碰撞、保存与再次入门的流程上。 */
final class RenjianEntryProbe {
    private static final BlockPos RESIDENCE = new BlockPos(0, 36, -21);
    private static final BlockPos TOMB = new BlockPos(391, 1, 356);
    private static final BlockPos TABLE = new BlockPos(0, 35, -27);
    private static final BlockPos SECOND_RESPAWN = new BlockPos(8, 160, 3);
    private Path savedData;
    private int enteredTick;

    void prepare(ServerPlayer player) {
        savedData = player.server.getWorldPath(LevelResource.ROOT).resolve("dimensions")
                .resolve(ModId.VALUE).resolve("dimensionrenjian/data/yvanchuxiuzhen_renjian_generation.dat");
        if (player.serverLevel().dimension() != Level.OVERWORLD || Files.exists(savedData)) {
            throw new IllegalStateException("人间首入检查要求从主世界全新存档开始");
        }
        if (Cultivation.tizhiBoolean(player, "chuanjiegujing") || Cultivation.tizhiBoolean(player, "shengxianling")) {
            throw new IllegalStateException("人间首入检查要求全新玩家入场标记");
        }
    }

    void useAscensionToken(SystemCheck check, ServerPlayer player) {
        int slot = player.getInventory().selected;
        ItemStack original = player.getMainHandItem();
        ItemStack token = BuiltInRegistries.ITEM.get(id("ling_09")).getDefaultInstance();
        player.getInventory().setItem(slot, token);
        player.gameMode.useItem(player, player.level(), token, InteractionHand.MAIN_HAND);
        player.getInventory().setItem(slot, original);
        check.expectTrue("实际使用升仙令留下标记与两种效果", Cultivation.tizhiBoolean(player, "shengxianling")
                && player.hasEffect(MobEffects.LEVITATION) && player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
    }

    void entered(SystemCheck check, ServerPlayer player, boolean reopened) {
        if (reopened) {
            check.expect("重进跨维不改重生维度", Level.OVERWORLD, player.getRespawnDimension());
            check.expect("重进跨维不改重生点", SECOND_RESPAWN, player.getRespawnPosition());
            check.expectTrue("重进不重放居所探针", player.serverLevel().getBlockState(RESIDENCE).is(Blocks.EMERALD_BLOCK));
            check.expectTrue("重进不重放小尸王场景探针", player.serverLevel().getBlockState(TOMB).is(Blocks.GOLD_BLOCK));
            gifts(check, player);
            return;
        }
        enteredTick = player.server.getTickCount();
        ServerLevel level = player.serverLevel();
        check.expect("首次入场绑定人间道强制重生维度", DimensionPortalSpec.RENJIAN.dimension(), player.getRespawnDimension());
        BlockPos respawn = player.getRespawnPosition();
        check.expectTrue("首次重生点在真实传送落点而非固定居所", player.isRespawnForced() && respawn != null
                && respawn.distSqr(player.blockPosition()) < 9);
        check.expectTrue("居所核心方块来自实际模板", matches(level, RESIDENCE, "qiankundaoren"));
        check.expectTrue("小尸王场景来自实际模板", matches(level, TOMB, "tiandaoguding"));
        check.expect("固定居所生成一名大管家", 1, level.getEntitiesOfClass(Mob.class,
                new AABB(-4, 28, -6, 5, 40, 3), mob -> mob.getType() == BuiltInRegistries.ENTITY_TYPE.get(id("daguanjia"))).size());
        gifts(check, player);
    }

    void afterDelays(SystemCheck check, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockState table = level.getBlockState(TABLE);
        check.expectTrue("60 刻后来源摆件、参悟台及南向", matches(level, TABLE.below(), "beechfurniture_1701")
                && table.is(BuiltInRegistries.BLOCK.get(id("canwutai_block")))
                && table.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH);
        check.expectTrue("升仙令入场后清标记并在 80 刻后解除效果", !Cultivation.tizhiBoolean(player, "shengxianling")
                && !player.hasEffect(MobEffects.LEVITATION) && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
        level.setBlock(RESIDENCE, Blocks.EMERALD_BLOCK.defaultBlockState(), 3);
        level.setBlock(TOMB, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
    }

    boolean delaysElapsed(ServerPlayer player) {
        return player.server.getTickCount() - enteredTick >= 85;
    }

    void beforeSave(ServerPlayer player) {
        player.setRespawnPosition(Level.OVERWORLD, SECOND_RESPAWN, 0, true, false);
    }

    void saved(SystemCheck check) {
        try {
            check.expectTrue("人间道专属存档实际写入 qiankundaochang", Files.isRegularFile(savedData)
                    && NbtIo.readCompressed(savedData, NbtAccounter.unlimitedHeap())
                    .getCompound("data").getBoolean("qiankundaochang"));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void gifts(SystemCheck check, ServerPlayer player) {
        check.expectTrue("玩家已保存传界赠礼标记", Cultivation.tizhiBoolean(player, "chuanjiegujing"));
        for (String item : new String[]{"fabao_013", "qiankundai_06"}) {
            check.expect("一次性赠礼 " + item, 1, player.getInventory().countItem(BuiltInRegistries.ITEM.get(id(item))));
        }
    }

    private static boolean matches(ServerLevel level, BlockPos pos, String block) {
        return level.getBlockState(pos).is(BuiltInRegistries.BLOCK.get(id(block)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
