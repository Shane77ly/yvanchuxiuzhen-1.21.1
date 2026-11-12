package net.yvanchuxiuzhen.system.guchong.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 手持蛊罐的放置、放虫、收虫和潜行开罐入口。 */
public final class TaoguanItem extends ContentItem {
    public static final TagKey<Item> INSECTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong"));
    private static final TagKey<Item> JARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan"));
    private final int grade;

    public TaoguanItem(Properties properties, List<Component> tooltip, int grade) {
        super(properties, tooltip);
        this.grade = grade;
    }

    public static int grade(ItemStack stack) {
        if (!(stack.getItem() instanceof TaoguanItem item)) return 0;
        return item.grade;
    }

    public static int capacity(int grade) {
        return switch (grade) { case 1 -> 1; case 2 -> 4; case 3 -> 9; default -> 0; };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        ItemStack stack = context.getItemInHand();
        CompoundTag data = GuchongJarData.read(stack);
        if (context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        if (data.getBoolean("shifangguchong")) {
            player.displayClientMessage(Component.literal("§4还有蛊虫未回到蛊罐里，无法放置蛊罐！"), false);
            return InteractionResult.SUCCESS;
        }
        level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan_0" + grade)).defaultBlockState(), 3);
        TaoguanBlockEntity jar = (TaoguanBlockEntity) level.getBlockEntity(pos);
        Relations.shareAllegianceToBlock(player, jar.jarData());
        jar.jarData().putString("guguanuuid", data.getString("guguanuuid"));
        jar.jarData().putString("team", Relations.team(player));
        for (int slot = 0; slot < capacity(grade); slot++) {
            ItemStack insect = GuchongJarData.unpack(data, slot, false);
            if (!insect.isEmpty()) jar.setItem(slot, insect);
        }
        jar.sync();
        if (player.getMainHandItem().is(JARS)) player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        else if (player.getOffhandItem().is(JARS)) player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.getInventory().setChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(level instanceof ServerLevel server)) return result;
        ItemStack stack = result.getObject();
        CompoundTag data = GuchongJarData.read(stack);
        if (player.isShiftKeyDown()) {
            if (data.getBoolean("shifangguchong")) {
                player.displayClientMessage(Component.literal("§4还有蛊虫未回到蛊罐中，无法打开蛊罐！"), false);
            } else {
                if (data.getString("guguanuuid").isEmpty()) {
                    StringBuilder uuid = new StringBuilder();
                    for (int letter = 0; letter < 8; letter++) uuid.append((char) ('a' + level.random.nextInt(25)));
                    data.putString("guguanuuid", uuid.toString());
                    GuchongJarData.write(stack, data);
                }
                if (player instanceof ServerPlayer owner) {
                    Component title = Component.literal(switch (grade) {
                        case 1 -> "小蛊罐";
                        case 2 -> "蛊罐";
                        default -> "大蛊罐";
                    });
                    owner.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new TaoguanMenu(id, inventory, grade, player.blockPosition()), title),
                            buffer -> buffer.writeBlockPos(player.blockPosition()));
                }
            }
        } else if (!data.getBoolean("shifangguchong")) {
            int released = 0;
            for (int slot = 0; slot < capacity(grade); slot++) {
                String suffix = Integer.toString(slot);
                if (data.getString("guchongid" + suffix).isEmpty()) continue;
                if (!data.getString("belongToMasterUUID" + suffix).equals(player.getStringUUID())) {
                    player.displayClientMessage(Component.literal("§4您不是" + data.getString("guchongname" + suffix) + "的主人，无法操控此虫。"), false);
                    continue;
                }
                Entity insect = GuchongJarRuntime.create(server, data.getString("guchongid" + suffix));
                if (insect != null) {
                    insect.moveTo(player.getX(), player.getY(), player.getZ(), level.random.nextFloat() * 360F, 0);
                    Relations.shareEntityAllegiance(player, insect);
                    insect.setYRot(player.getYRot());
                    insect.setXRot(0);
                    insect.setYBodyRot(insect.getYRot());
                    insect.setYHeadRot(insect.getYRot());
                    insect.yRotO = insect.getYRot();
                    insect.xRotO = insect.getXRot();
                    if (insect instanceof LivingEntity living) {
                        living.yBodyRotO = living.getYRot();
                        living.yHeadRotO = living.getYRot();
                    }
                    Vec3 look = player.getLookAngle();
                    insect.setDeltaMovement(look.x, look.y + 1, look.z);
                    CompoundTag insectData = insect.getPersistentData();
                    GuchongJarData.copy(data, suffix, insectData, "", false);
                    insectData.remove("team");
                    insectData.putString("guguanuuid", data.getString("guguanuuid"));
                    insectData.putDouble("guguanweizhi", slot);
                    insectData.putBoolean("guchongxvnfu", true);
                    insect.setCustomName(Component.literal(data.getString("guchongname" + suffix).replace("[", "").replace("]", "")));
                    if (insect instanceof TamableAnimal tamable) tamable.tame(player);
                    GuchongJarRuntime.applyAttributes(insect, insectData);
                    GuchongJarRuntime.finishSpawn(server, insect);
                }
                released++;
            }
            data.putDouble("shifangnum", released);
            if (released >= 1) data.putBoolean("shifangguchong", true);
            GuchongJarData.write(stack, data);
        } else {
            Vec3 center = player.position();
            List<Entity> insects = server.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), entity -> true)
                    .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
            for (Entity entity : insects) {
                if (!(entity instanceof GuchongEntity insect)) continue;
                if (!insect.getPersistentData().getString("guguanuuid").equals(data.getString("guguanuuid"))) continue;
                GuchongJarData.capture(insect, data, Relations.team(player));
                insect.discard();
            }
            if (data.getDouble("shifangnum") < 1) data.putBoolean("shifangguchong", false);
            GuchongJarData.write(stack, data);
        }
        return result;
    }
}
