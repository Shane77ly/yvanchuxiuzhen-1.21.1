package net.yvanchuxiuzhen.system.liandan;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlock;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyBlockEntity;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.yihuo.api.FireExhaustedEvent;
import net.yvanchuxiuzhen.system.yihuo.api.FireHeatEvent;

/** 丹炉每二十刻消费热量并推进炉次；库存、药性快照与进度均由丹炉方块实体持久化。 */
public final class AlchemyRuntime {
    private static final String[] NAMES = {"黄铜炉", "红蛇炉", "海渊炉", "地肺炉", "紫云炉", "天青炉", "锦纹炉", "八卦金炉"};

    private AlchemyRuntime() {
    }

    public static void initialize(AlchemyBlockEntity furnace) {
        int tier = furnace.tier();
        CompoundTag data = furnace.getPersistentData();
        data.putString("alchemyName", NAMES[tier - 1]);
        data.putDouble("alchemyLevel", tier);
        data.putDouble("alchemyFire", tier);
        data.putDouble("alchemyDannum", 1 + (tier - 1) / 3);
        furnace.changed();
    }

    /** 来源药性表只在实体放置事件中初始化，结构恢复与指令放置不补写快照。 */
    public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (!event.getLevel().isClientSide() && event.getLevel().getBlockEntity(event.getPos()) instanceof AlchemyBlockEntity furnace) {
            furnace.getPersistentData().put("zong", LiandanHerbs.snapshot());
            furnace.changed();
        } else if (!event.getLevel().isClientSide() && event.getLevel().getBlockEntity(event.getPos()) instanceof PharmacologyBlockEntity table) {
            table.getPersistentData().put("zong", LiandanHerbs.snapshot());
            table.changed();
        }
    }

    /** 来源全局右键先开窗，方块 use 随后仅刷新空闲炉的职业快照与开盖动画。 */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() == player.getUsedItemHand() && player instanceof ServerPlayer
                && event.getLevel().getBlockEntity(event.getPos()) instanceof AlchemyBlockEntity furnace) {
            player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new AlchemyMenu(id, inventory, furnace),
                    Component.literal("LiandanGUI")), event.getPos());
        }
    }

    public static void use(Level level, BlockPos pos, Player player, AlchemyBlockEntity furnace) {
        if (!furnace.getPersistentData().getBoolean("liandanBegin")) {
            level.setBlock(pos, furnace.getBlockState().setValue(AlchemyBlock.ANIMATION, 3), Block.UPDATE_ALL);
            if (!level.isClientSide()) {
                furnace.getPersistentData().putDouble("Alchemy_LEVEL", Professions.level(player, Profession.ALCHEMY));
                furnace.changed();
            }
        }
    }

    public static void heat(FireHeatEvent event) {
        if (event.level().getBlockEntity(event.pos()) instanceof AlchemyBlockEntity furnace) {
            CompoundTag data = furnace.getPersistentData();
            data.putDouble("lianpower", data.getDouble("lianpower") + event.amount());
            furnace.changed();
        }
    }

    public static void exhausted(FireExhaustedEvent event) {
        if (event.level().getBlockEntity(event.pos()) instanceof AlchemyBlockEntity furnace) {
            CompoundTag data = furnace.getPersistentData();
            if (data.getDouble("lianpower") >= 1) {
                data.putBoolean("gengxin", !data.getBoolean("gengxin"));
                furnace.changed();
                event.retain();
            }
        }
    }

    public static void tick(ServerLevel level, AlchemyBlockEntity furnace) {
        CompoundTag data = furnace.getPersistentData();
        BlockPos pos = furnace.getBlockPos();
        if (data.getDouble("firepower") >= 1) {
            level.sendParticles(ParticleTypes.LAVA, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 5, 0, 0, 0, 1);
            data.putDouble("firepowertick", data.getDouble("firepowertick") + 1);
            if (data.getDouble("firepowertick") >= 12000) {
                data.putDouble("firepower", 0);
                data.putDouble("firepowertick", 0);
            }
        }
        if (data.getBoolean("liandanBegin")) {
            if (data.getDouble("lianpower") >= data.getDouble("alchemyLevel")) {
                data.putDouble("lianpower", data.getDouble("lianpower") - data.getDouble("alchemyLevel"));
                data.putDouble("tick", data.getDouble("tick") + 1);
                if (data.getDouble("tick") >= 1) {
                    animate(level, furnace, data.getBoolean("zhalu") ? 2 : 1);
                    data.putDouble("tick", 0);
                }
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 5, .5, .5, .5, .1);
                data.putDouble("lianqiTick", data.getDouble("lianqiTick") + 1 + data.getDouble("firepower"));
                if (data.getDouble("lianqiTick") >= data.getDouble("lianqiTickMax")) {
                    boolean exploded = data.getBoolean("zhalu") && Math.random() > data.getDouble("zhalugailv");
                    if (exploded) {
                        furnace.setItem(9, new ItemStack(Items.GUNPOWDER));
                    } else {
                        animate(level, furnace, 4);
                        ItemStack intermediate = furnace.getItem(11);
                        CompoundTag medicine = intermediate.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(medicine.getString("danyaoid").toLowerCase(Locale.ENGLISH))));
                        result.applyComponents(intermediate.getComponentsPatch());
                        result.set(DataComponents.CUSTOM_NAME, Component.literal(medicine.getString("danyaoname")));
                        furnace.setItem(9, result);
                    }
                    furnace.setItem(11, ItemStack.EMPTY);
                    data.putDouble("lianqiTick", 0);
                    data.putBoolean("liandanBegin", false);
                    data.putBoolean("zhalu", false);
                    if (exploded) {
                        explode(level, pos, (float) (2 + 2 * data.getDouble("alchemyLevel")));
                        if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity remaining) {
                            remaining.getPersistentData().putBoolean("zhenzhale", true);
                        }
                    }
                }
                if (level.getBlockEntity(pos) instanceof AlchemyBlockEntity remaining) {
                    CompoundTag current = remaining.getPersistentData();
                    current.putDouble("lianzhijindu", current.getDouble("lianqiTick") / current.getDouble("lianqiTickMax") * 100);
                }
            } else {
                level.sendParticles(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 10, .5, .5, .5, .1);
            }
        } else {
            animate(level, furnace, 0);
        }
        if (!furnace.isRemoved()) furnace.changed();
    }

    private static void animate(Level level, AlchemyBlockEntity furnace, int animation) {
        level.setBlock(furnace.getBlockPos(), furnace.getBlockState().setValue(AlchemyBlock.ANIMATION, animation), Block.UPDATE_ALL);
    }

    /** 来源 BREAK 对应完整方块掉落；不让新增的爆炸掉落衰减规则改变炸炉结果。 */
    private static void explode(ServerLevel level, BlockPos pos, float radius) {
        Explosion.BlockInteraction interaction = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        Explosion explosion = new Explosion(level, null, pos.getX(), pos.getY(), pos.getZ(), radius, false, interaction);
        if (EventHooks.onExplosionStart(level, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(false);
        if (!explosion.interactsWithBlocks()) explosion.clearToBlow();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(pos.getX(), pos.getY(), pos.getZ(), radius,
                        explosion.getToBlow(), explosion.getHitPlayers().get(player), explosion.getBlockInteraction(),
                        explosion.getSmallExplosionParticles(), explosion.getLargeExplosionParticles(), explosion.getExplosionSound()));
            }
        }
    }
}
