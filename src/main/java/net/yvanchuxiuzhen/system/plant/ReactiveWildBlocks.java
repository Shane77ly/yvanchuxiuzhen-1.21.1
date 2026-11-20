package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.plant.api.WildPlantCreatureEvent;

/** 反应型野外方块按来源基类装配碰触、随机刻、计划刻、攻击和玩家破坏回调。 */
public final class ReactiveWildBlocks {
    private ReactiveWildBlocks() {
    }

    public static Block block(BlockTraits traits) {
        return ContentBlock.create(traits, ReactiveBlock::new);
    }

    public static Block flower(BlockTraits traits) {
        return new ReactiveFlower(traits);
    }

    public static Block doublePlant(BlockTraits traits) {
        return new ReactiveDoublePlant(traits);
    }

    public static Block waterlogged(BlockTraits traits) {
        return ContentBlock.create(traits, ReactiveWaterloggedBlock::new);
    }

    public static Block leaves(BlockTraits traits) {
        return new SandalwoodLeaves(traits);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(id(path));
    }

    private static void replace(Level level, BlockPos pos, String path) {
        level.setBlock(pos, block(path).defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void sound(Level level, BlockPos pos, SoundEvent sound) {
        if (level.isClientSide) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sound, SoundSource.NEUTRAL, 1, 1, false);
        } else {
            level.playSound(null, pos, sound, SoundSource.NEUTRAL, 1, 1);
        }
    }

    private static void opticalPrism(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(Particles.type("opticalprismlizi"), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 15, 0, 0, 0, 1);
        }
        sound(level, pos, BuiltInRegistries.SOUND_EVENT.get(id("ice1")));
        level.destroyBlock(pos, false);
    }

    private static void contact(String path, Level level, BlockPos pos, Entity entity) {
        switch (path) {
            case "huaguang" -> entity.setDeltaMovement(new Vec3(0, 0.2, 0));
            case "opticalprism" -> opticalPrism(level, pos);
            case "cannibalmoss" -> replace(level, pos, "cannibalmoss_02");
            case "cannibalmoss_02" -> {
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20, false, false));
                }
            }
            case "bitegrass_01" -> {
                entity.hurt(entity.damageSources().generic(), 5);
                replace(level, pos, "bitegrass_02");
            }
            case "barrenstonesting", "thistlesandthorns" -> entity.hurt(entity.damageSources().generic(), 4);
            case "lurker" -> {
                entity.hurt(entity.damageSources().generic(), 4);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 1));
                }
            }
            case "cloudgrass_01" -> {
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0, 0, 0, 0.4);
                }
                replace(level, pos, "cloudgrass_02");
            }
            case "soulgrass" -> {
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 1, 0.5, 1, 0);
                }
            }
            case "bladegrass", "tailbladegrass" -> {
                entity.hurt(entity.damageSources().generic(), path.equals("bladegrass") ? 8 : 24);
                level.destroyBlock(pos, false);
            }
            case "witheredbone_01", "witheredbone_02", "witheredbone_03", "witheredbone_04", "witheredbone_05", "witheredbone_06" ->
                    sound(level, pos, SoundEvents.SKELETON_AMBIENT);
            default -> {
            }
        }
    }

    private static void advance(String path, ServerLevel level, BlockPos pos) {
        switch (path) {
            case "cannibalmoss_02" -> replace(level, pos, "cannibalmoss");
            case "bitegrass_02" -> replace(level, pos, "bitegrass_01");
            case "cloudgrass_02" -> replace(level, pos, "cloudgrass_01");
            case "thundergrass" -> {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                lightning.moveTo(Vec3.atBottomCenterOf(pos.offset(new Random().nextInt(-3, 4), 0, new Random().nextInt(-3, 4))));
                lightning.setVisualOnly(false);
                level.addFreshEntity(lightning);
            }
            case "peachsaplingxve" -> {
                ResourceLocation dimension = level.dimension().location();
                if (!dimension.equals(id("fabao_009weidu")) && !dimension.equals(id("xvkong")) && Math.random() < 0.05) {
                    level.sendParticles(Particles.type("smokecrimson"), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 20, 1, 1, 1, 1);
                    NeoForge.EVENT_BUS.post(new WildPlantCreatureEvent(level, pos, WildPlantCreatureEvent.Kind.BLOOD_PEACH, null));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            default -> {
            }
        }
    }

    private static void snowball(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) {
            Snowball snowball = new Snowball(EntityType.SNOWBALL, server);
            snowball.setOwner(player);
            snowball.setPos(pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5);
            Vec3 look = player.getLookAngle();
            snowball.shoot(look.x, player.isShiftKeyDown() ? look.y : 0.1, look.z, 1, 1);
            server.addFreshEntity(snowball);
        }
    }

    /** 每个 else-if 单独抽样，保留来源最后仍可能没有动物的分支。 */
    private static void iceboundRock(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel server)) return;
        var silk = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silk, player.getMainHandItem()) != 0
                || EnchantmentHelper.getItemEnchantmentLevel(silk, player.getOffhandItem()) != 0) return;
        EntityType<? extends Mob> type;
        if (Math.random() <= 0.142) type = EntityType.BAT;
        else if (Math.random() <= 0.166) type = EntityType.BEE;
        else if (Math.random() <= 0.2) type = EntityType.CAT;
        else if (Math.random() <= 0.25) type = EntityType.CHICKEN;
        else if (Math.random() <= 0.333) type = EntityType.FOX;
        else if (Math.random() <= 0.5) type = EntityType.RABBIT;
        else return;
        Mob animal = type.create(server);
        animal.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        animal.setYBodyRot(0);
        animal.setYHeadRot(0);
        animal.finalizeSpawn(server, server.getCurrentDifficultyAt(animal.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(animal);
    }

    private static void drop(ServerLevel level, BlockPos pos, String item) {
        ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(BuiltInRegistries.ITEM.get(id(item))));
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }

    private static final class ReactiveFlower extends FallbackFlowerBlock {
        private final String path;

        private ReactiveFlower(BlockTraits traits) {
            super(traits);
            path = traits.row().id();
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            contact(path, level, pos, entity);
        }

        /** 保留1.18.2基类将随机刻委派给本方块计划刻回调的行为。 */
        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            tick(state, level, pos, random);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.tick(state, level, pos, random);
            advance(path, level, pos);
        }

        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            if (path.equals("opticalprism")) opticalPrism(level, pos);
            return removed;
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return path.equals("cloudgrass_02") ? new ItemStack(block("cloudgrass_01")) : super.getCloneItemStack(level, pos, state);
        }
    }

    private static final class ReactiveBlock extends ContentBlock {
        private final String path;

        private ReactiveBlock(BlockTraits traits) {
            super(traits);
            path = traits.row().id();
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            List<ItemStack> drops = super.getDrops(state, params);
            if (!drops.isEmpty()) return drops;
            return switch (path) {
                case "snowdrift" -> List.of(new ItemStack(Items.SNOWBALL, 16));
                case "barrenstonesting" -> List.of(new ItemStack(Blocks.STONE));
                default -> drops;
            };
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            contact(path, level, pos, entity);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!path.equals("snowdrift")) return super.useWithoutItem(state, level, pos, player, hit);
            super.useWithoutItem(state, level, pos, player, hit);
            snowball(level, pos, player);
            return InteractionResult.SUCCESS;
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!path.equals("snowdrift")) return super.useItemOn(stack, state, level, pos, player, hand, hit);
            super.useItemOn(stack, state, level, pos, player, hand, hit);
            snowball(level, pos, player);
            return ItemInteractionResult.SUCCESS;
        }

        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            if (path.equals("moeye") && level instanceof ServerLevel server) {
                NeoForge.EVENT_BUS.post(new WildPlantCreatureEvent(server, pos, WildPlantCreatureEvent.Kind.DEMON_EYE, player));
            }
            return removed;
        }
    }

    private static final class ReactiveDoublePlant extends FallbackDoublePlantBlock {
        private final String path;

        private ReactiveDoublePlant(BlockTraits traits) {
            super(traits);
            path = traits.row().id();
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            contact(path, level, pos, entity);
        }

        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            if (path.equals("iceboundrock")) iceboundRock(level, pos, player);
            return removed;
        }
    }

    private static final class ReactiveWaterloggedBlock extends WaterloggedContentBlock {
        private final String path;

        private ReactiveWaterloggedBlock(BlockTraits traits) {
            super(traits);
            path = traits.row().id();
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            List<ItemStack> drops = super.getDrops(state, params);
            if (!drops.isEmpty()) return drops;
            BlockRow.Drop fallback = traits().row().behavior().fallbackDrop().orElseThrow();
            if (fallback.item().equals("self")) return List.of(new ItemStack(this, fallback.count()));
            return List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(fallback.item())), fallback.count()));
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            contact(path, level, pos, entity);
        }

        @Override
        protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
            super.attack(state, level, pos, player);
            if (!path.equals("ironrope")) return;
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0, 0, 0, 0.1);
            }
            if (!player.getMainHandItem().isCorrectToolForDrops(state)) {
                player.getPersistentData().putBoolean("manaattackbegin", true);
                player.hurt(player.damageSources().inFire(), 5);
            }
        }
    }

    private static final class SandalwoodLeaves extends FruitTreeLeavesBlock {
        private SandalwoodLeaves(BlockTraits traits) {
            super(traits);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            List<ItemStack> drops = super.getDrops(state, params);
            return drops.isEmpty() ? List.of(new ItemStack(Blocks.AIR)) : drops;
        }

        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            if (level instanceof ServerLevel server) {
                if (Math.random() < 0.1) drop(server, pos, "sandalwoodleavesitem");
                else if (Math.random() < 0.1) drop(server, pos, "sandalwoodsapling");
                else if (Math.random() < 0.05) {
                    drop(server, pos, "sandalwoodsapling");
                    drop(server, pos, "sandalwoodsapling");
                }
            }
            return removed;
        }
    }
}
