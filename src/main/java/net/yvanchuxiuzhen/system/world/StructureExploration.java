package net.yvanchuxiuzhen.system.world;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.content.RiftBlock;
import net.yvanchuxiuzhen.system.world.content.RiftBlockEntity;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;

/** 寻灵盘、对话和管理命令共用来源的最近遗迹算法与立即登记次序。 */
public final class StructureExploration {
    private StructureExploration() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(StructureExploration::commands);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("searchingforstructure")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(StructureExploration::searchCommand))
                .executes(StructureExploration::searchCommand));
        event.getDispatcher().register(Commands.literal("renjiansealrelease").requires(source -> source.hasPermission(1))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Entity executor = source.getEntity() == null ? FakePlayerFactory.getMinecraft(source.getLevel()) : source.getEntity();
                    releaseAll(source.getLevel(), executor);
                    return 0;
                }));
    }

    private static int searchCommand(CommandContext<CommandSourceStack> context) {
        String[] words = context.getInput().split("\\s+");
        search(context.getSource().getLevel(), context.getSource().getPosition(), words.length > 1 ? words[1] : "");
        return 0;
    }

    public static void search(ServerLevel level, Vec3 origin, String playerUuid) {
        StructureExplorationData data = StructureExplorationData.get(level.getServer());
        List<String> released = data.released();
        StructureCatalog.Entry nearest = new StructureCatalog.Entry("", "", 0, 0, 0, "");
        double distance = 0;
        for (StructureCatalog.Entry entry : StructureCatalog.ENTRIES) {
            if (released.contains(entry.id())) continue;
            double candidate = origin.distanceTo(entry.position());
            // 来源用零表示尚未选择；真实零距离也会进入下一条记录的初始化分支。
            if (distance == 0 || distance > candidate) {
                nearest = entry;
                distance = candidate;
            }
        }
        List<Entity> nearby = level.getEntitiesOfClass(Entity.class, new AABB(origin, origin).inflate(2), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(origin))).toList();
        for (Entity entity : nearby) {
            if (entity instanceof GuidanceHalo halo) {
                if (distance == 0) halo.discard();
                else halo.target(nearest.position());
            }
            if (entity instanceof Player player && player.getStringUUID().equals(playerUuid)) {
                player.displayClientMessage(distance == 0
                        ? Component.translatable("message.yvanchuxiuzhen.structures_complete")
                        : Component.translatable("message.yvanchuxiuzhen.structure_distance", new DecimalFormat("0").format(distance)), true);
            }
        }
        BlockPos pos = BlockPos.containing(nearest.position());
        Block rift = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "liexi"));
        if (distance <= 16 && !level.getBlockState(pos).is(rift)) {
            level.setBlock(pos, rift.defaultBlockState(), 3);
            Direction direction = Math.random() < 0.25 ? Direction.NORTH : Math.random() < 0.333 ? Direction.SOUTH
                    : Math.random() < 0.5 ? Direction.WEST : Direction.EAST;
            level.setBlock(pos, level.getBlockState(pos).setValue(RiftBlock.FACING, direction), 3);
            RiftBlockEntity block = (RiftBlockEntity) level.getBlockEntity(pos);
            block.structure(nearest.name(), nearest.mobs());
            // 来源在放置裂隙后才解析 UUID，并直接使用查询结果的显示名。
            block.playerName(lookup(level, playerUuid).getDisplayName().getString());
            data.mark(nearest.id());
        }
    }

    private static Entity lookup(ServerLevel level, String uuid) {
        try {
            String dashed = uuid.replaceFirst("^(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})$", "$1-$2-$3-$4-$5");
            return level.getEntity(UUID.fromString(dashed));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void releaseAll(ServerLevel level, Entity executor) {
        StructureExplorationData data = StructureExplorationData.get(level.getServer());
        List<String> released = data.released();
        for (StructureCatalog.Entry entry : StructureCatalog.ENTRIES) {
            if (released.contains(entry.id())) continue;
            release(level, executor.getDisplayName().getString(), entry.name());
            data.mark(entry.id());
        }
    }

    /** 裂隙保留来源命令的当前维度在线玩家显示名门控。 */
    public static void release(ServerLevel level, String playerName, String structureName) {
        String target = playerName.trim();
        if (target.isEmpty() || target.matches("(?s).*\\s.*")) return;
        for (ServerPlayer player : level.players()) {
            if (player.getDisplayName().getString().equals(target)) {
                String[] words = structureName.trim().split("\\s+", 3);
                StructureReleases.release(level, player, words[0], words.length > 1 && words[1].equals("true"));
                return;
            }
        }
    }
}
