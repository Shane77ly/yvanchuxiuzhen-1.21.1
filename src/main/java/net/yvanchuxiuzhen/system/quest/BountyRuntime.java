package net.yvanchuxiuzhen.system.quest;

import java.text.DecimalFormat;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.api.CreatureBounties;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.EconomyCurrency;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTracking;
import net.yvanchuxiuzhen.system.quest.content.BountyBoardBlockEntity;
import net.yvanchuxiuzhen.system.quest.data.BountyTargetData;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 公共告示牌状态、追踪实例和按指定猎人结算的悬赏生命周期。 */
public final class BountyRuntime {
    private static final int[][] TARGETS = {{8,15,1},{11,15,1},{12,15,1},{14,10,1},{15,5,1},{24,10,1},{26,15,1},
            {32,15,1},{35,20,1},{36,20,1},{37,25,1},{38,25,1},{40,25,1},{43,20,1},{45,10,8},{47,25,1},
            {48,25,1},{50,20,1},{51,25,1},{53,25,1},{54,18,1}};

    private BountyRuntime() {
    }

    public static void initialize(BountyBoardBlockEntity board) {
        for (int index = 1; index <= 6; index++) generate(board, index, false);
    }

    private static void generate(BountyBoardBlockEntity board, int index, boolean refresh) {
        ServerLevel level = (ServerLevel) board.getLevel();
        BlockPos origin = board.getBlockPos();
        int[] target = TARGETS[level.random.nextInt(TARGETS.length)];
        String id = "yaoshou_" + String.format(Locale.ROOT, "%03d", target[0]);
        CompoundTag entry = new CompoundTag();
        entry.putString("entityname", "entity." + ModId.VALUE + "." + id);
        entry.putString("entityid", ModId.VALUE + ":" + id);
        entry.putInt("LEVELnumber", Mth.nextInt(level.random, target[1], 40));
        entry.putInt("entitynum", target[2]);
        entry.putInt("weizhiX", Mth.nextInt(level.random, -500, 500));
        entry.putInt("weizhiZ", Mth.nextInt(level.random, -500, 500));
        int x = origin.getX() + entry.getInt("weizhiX");
        int z = origin.getZ() + entry.getInt("weizhiZ");
        int y = origin.getY();
        while (!available(level, new BlockPos(x, y, z), refresh)) y++;
        entry.putInt("zuobiaoX", x);
        entry.putInt("zuobiaoY", y);
        entry.putInt("zuobiaoZ", z);
        entry.putInt("weizhiY", y - origin.getY());
        entry.putInt("zuobiaojvli", (int) Math.sqrt(origin.distSqr(new BlockPos(x, y, z))));
        entry.putInt("xvanshangmoney", entry.getInt("LEVELnumber") * 80);
        if (refresh) entry.putString("zhuizhongzheuuid", "");
        board.setItem(index - 1, new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xvanshang_0" + Mth.nextInt(level.random, 1, 6)))));
        board.entry(index, entry);
        if (refresh) {
            level.setBlock(origin.above(), Blocks.RED_WOOL.defaultBlockState(), 3);
            board.setRefreshClearAt(level.getGameTime() + 5);
            level.scheduleTick(origin, board.getBlockState().getBlock(), 5);
        }
    }

    private static boolean available(ServerLevel level, BlockPos pos, boolean refresh) {
        for (BlockPos sample : new BlockPos[]{pos.above(2), pos.below(2), pos.east(3), pos.west(3), pos.north(3), pos.south(3)}) {
            if (refresh ? !level.canSeeSkyFromBelowWater(sample) : !level.isEmptyBlock(sample)) return false;
        }
        return true;
    }

    public static void track(ServerPlayer player, BountyBoardBlockEntity board, int index) {
        CompoundTag entry = board.entry(index);
        if (entry.getInt("zuobiaojvli") <= 1) {
            player.displayClientMessage(Component.literal("§4请刷新悬赏。"), false);
            return;
        }
        BlockPos destination = new BlockPos(entry.getInt("zuobiaoX"), entry.getInt("zuobiaoY"), entry.getInt("zuobiaoZ"));
        if (!entry.getBoolean("zhuizhong")) {
            entry.putBoolean("zhuizhong", true);
            entry.putString("zhuizhongzheuuid", player.getStringUUID());
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry.getString("entityid"))).create(player.serverLevel());
            if (entity != null) {
                entity.moveTo(destination.getX(), destination.getY(), destination.getZ(), player.level().random.nextFloat() * 360, 0);
                entity.setCustomName(Component.literal(entity.getDisplayName().getString() + "(悬赏)"));
                CreatureBounties.setBounty(entity, true);
                entity.setData(QuestData.BOUNTY_TARGET, new BountyTargetData(true,
                        board.getBlockPos().getX(), board.getBlockPos().getY(), board.getBlockPos().getZ(),
                        index, player.getStringUUID()));
                if (entity instanceof Mob mob) {
                    CreatureBounties.prepare(mob, entry.getInt("LEVELnumber"));
                    mob.setPersistenceRequired();
                    EventHooks.finalizeMobSpawn(mob, player.serverLevel(), player.level().getCurrentDifficultyAt(destination), MobSpawnType.MOB_SUMMONED, null);
                }
                player.serverLevel().addFreshEntity(entity);
            }
            ItemHandlerHelper.giveItemToPlayer(player, GuchongTracking.createBound(player, destination));
            player.displayClientMessage(Component.literal("斥候：妖兽所在坐标为" + destination.getX() + "，" + destination.getY() + "，" + destination.getZ() + "。祝前辈旗开得胜！"), false);
            board.entry(index, entry);
        } else if (entry.getString("zhuizhongzheuuid").equals(player.getStringUUID())) {
            if (EconomyCurrency.payTrackingReissue(player)) {
                player.displayClientMessage(Component.literal("缴纳20文，再次领取荧光蛊。"), false);
                ItemHandlerHelper.giveItemToPlayer(player, GuchongTracking.createBound(player, destination));
            } else player.displayClientMessage(Component.literal("§4不够20文，无法再次领取荧光蛊。"), false);
        } else player.displayClientMessage(Component.literal("§4此悬赏已经开始追踪！"), false);
    }

    public static void death(LivingDeathEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (!(victim.level() instanceof ServerLevel level) || attacker == null) return;
        BountyTargetData attackerTarget = attacker.getData(QuestData.BOUNTY_TARGET);
        BountyTargetData victimTarget = victim.getData(QuestData.BOUNTY_TARGET);
        boolean failed = victim instanceof ServerPlayer && attackerTarget.active();
        boolean succeeded = !(victim instanceof ServerPlayer) && victim.getType().is(TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"))) && victimTarget.active();
        if (!failed && !succeeded) return;
        Entity bounty = failed ? attacker : victim;
        BountyTargetData target = failed ? attackerTarget : victimTarget;
        BlockPos pos = target.boardPos();
        BountyBoardBlockEntity board = (BountyBoardBlockEntity) level.getBlockEntity(pos);
        int index = target.slotIndex();
        CompoundTag entry = board.entry(index);
        entry.putBoolean("zhuizhong", false);
        board.entry(index, entry);
        if (failed) {
            ServerPlayer player = (ServerPlayer) victim;
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("失 败").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            player.displayClientMessage(Component.literal("§4悬赏任务失败"), false);
            generate(board, index, true);
            bounty.discard();
        } else {
            for (ServerPlayer player : level.players()) {
                if (player.getStringUUID().equals(target.hunterUuid())) {
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("成 功").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                    int reward = entry.getInt("xvanshangmoney");
                    player.displayClientMessage(Component.literal("§6" + new DecimalFormat("0").format(reward) + "灵石已入账！"), false);
                    BankAccounts.creditValue(player, reward);
                    break;
                }
            }
            generate(board, index, true);
            level.scheduleTick(pos, board.getBlockState().getBlock(), 2);
        }
    }

    public static void information(ServerPlayer player) {
        player.displayClientMessage(Component.literal("悬赏是获取高级材料与大量灵石的重要渠道，同时也非常具有挑战性。"), false);
        player.displayClientMessage(Component.literal("领取悬赏后，使用荧光蛊追踪妖兽位置并完成击杀，即可完成悬赏任务。"), false);
    }
}
