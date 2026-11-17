package net.yvanchuxiuzhen.system.creature;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenu;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou001Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou002Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 妖兽的灵兽袋收纳、释放与染色行为。 */
public final class SpiritBeastBagRuntime {
    private static final TagKey<Item> BAGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudai"));
    private static final TagKey<Item> DYES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ranliao"));
    private static final List<String> NUMERIC_FIELDS = List.of("LEVELnumber", "experience_Max", "experience", "health_Max", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def",
            "soul_Att", "soul_Def", "speed_number");
    private static final List<String> STRING_FIELDS = List.of("LEVELname", "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL",
            "power_Rec_LEVEL", "soul_Max_LEVEL", "soul_Rec_LEVEL", "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL",
            "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL");
    private static final List<Item> DYE_ITEMS = List.of(Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
            Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, Items.CYAN_DYE,
            Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE);

    private SpiritBeastBagRuntime() {
    }

    public static void interact(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isShiftKeyDown()
                || !(event.getTarget() instanceof TamableAnimal animal) || !animal.isTame()) return;
        ItemStack bag = player.getMainHandItem();
        if (!bag.is(BAGS)) return;
        CompoundTag data = read(bag);
        String bagUuid = data.getString("lingshoudaiuuid");
        String creatureUuid = event.getTarget().getPersistentData().getString("lingshoudaiuuid");
        if (!creatureUuid.isEmpty() && !bagUuid.equals(creatureUuid)) {
            player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag.wrong_bag"), false);
            return;
        }
        capture(player, animal, bag, data, bagUuid, creatureUuid);
    }

    private static void capture(Player player, TamableAnimal animal, ItemStack bag, CompoundTag data, String bagUuid, String creatureUuid) {
        if (!animal.isOwnedBy(player)) {
            player.displayClientMessage(red("not_owner"), false);
            return;
        }
        if (bagUuid.isEmpty()) {
            StringBuilder generated = new StringBuilder();
            String alphabet = "abcdefjhigklmnopqrstuvwxyz";
            for (int index = 0; index < 8; index++) generated.append(alphabet.charAt(Mth.nextInt(RandomSource.create(), 0, 24)));
            bagUuid = generated.toString();
            data.putString("lingshoudaiuuid", bagUuid);
            write(bag, data);
        }
        if (!data.getString("yaoshouid").isEmpty() && !bagUuid.equals(creatureUuid)) {
            player.displayClientMessage(red("occupied"), false);
            player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag.bag_uuid", bagUuid), false);
            player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag.creature_uuid", creatureUuid), false);
            player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag.creature_id", data.getString("yaoshouid")), false);
            return;
        }
        if (data.getBoolean("yaoshoushifang")) {
            data.putBoolean("yaoshoushifang", false);
            write(bag, data);
        }
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if ((animal instanceof Yaoshou001Entity || animal instanceof Yaoshou002Entity)
                && animal.getAttribute(YaoshouAttributes.SKIN).getBaseValue() >= 1) {
            data.putDouble("yaoshoupifuxianshi", animal.getAttribute(YaoshouAttributes.SKIN).getBaseValue());
            write(bag, data);
        }
        data.putDouble("entitydaxiao", animal.getAttribute(YaoshouAttributes.SIZE).getBaseValue());
        data.putString("yaoshouid", type.toString());
        // 来源玩家变量仅附着真实玩家，妖兽读取该能力时取得默认空团队。
        data.putString("team", "");
        data.putString("yaoshouname", cleanName(animal.getDisplayName().getString()));
        data.putBoolean("yaoshouxvnfu", true);
        data.putString("belongToMasterUUID", player.getStringUUID());
        for (String key : STRING_FIELDS) data.putString(key, animal.getPersistentData().getString(key));
        for (String key : NUMERIC_FIELDS) data.putDouble(key, animal.getPersistentData().getDouble(key));
        data.putDouble("health_Num", animal.getHealth());
        if (!animal.getPersistentData().getString("character").isEmpty()) {
            data.putString("character", animal.getPersistentData().getString("character"));
            for (String key : List.of("character_def", "character_att", "character_interval", "character_resist")) {
                data.putDouble(key, animal.getPersistentData().getDouble(key));
            }
        }
        write(bag, data);
        animal.discard();
    }

    public static void release(ServerLevel level, Player player, ItemStack bag) {
        CompoundTag data = read(bag);
        if (player.isShiftKeyDown() || !data.getString("belongToMasterUUID").equals(player.getStringUUID())
                || data.getBoolean("yaoshoushifang")) return;
        ResourceLocation id = ResourceLocation.parse(data.getString("yaoshouid"));
        Entity stored = BuiltInRegistries.ENTITY_TYPE.get(id).create(level);
        if (stored != null) {
            BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(1.0F),
                    player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(2)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            BlockPos position = hit.getBlockPos();
            stored.moveTo(position.getX(), position.getY(), position.getZ(), level.getRandom().nextFloat() * 360.0F, 0);
            stored.setDeltaMovement(player.getLookAngle().scale(0.5).add(0, 0.3, 0));
            stored.setYRot(player.getYRot());
            stored.setXRot(player.getXRot());
            stored.setYBodyRot(stored.getYRot());
            stored.setYHeadRot(stored.getYRot());
            stored.yRotO = stored.getYRot();
            stored.xRotO = stored.getXRot();
            if (stored instanceof LivingEntity living) {
                living.yBodyRotO = living.getYRot();
                living.yHeadRotO = living.getYRot();
            }
            LivingEntity living = (LivingEntity) stored;
            if (data.getDouble("yaoshoupifuxianshi") >= 1) living.getAttribute(YaoshouAttributes.SKIN).setBaseValue(data.getDouble("yaoshoupifuxianshi"));
            living.getAttribute(YaoshouAttributes.SIZE).setBaseValue(data.getDouble("entitydaxiao"));
            stored.getPersistentData().putString("lingshoudaiuuid", data.getString("lingshoudaiuuid"));
            for (String key : NUMERIC_FIELDS) stored.getPersistentData().putDouble(key, data.getDouble(key));
            for (String key : STRING_FIELDS) stored.getPersistentData().putString(key, data.getString(key));
            stored.setCustomName(Component.literal(cleanName(data.getString("yaoshouname"))));
            if (stored instanceof TamableAnimal animal) animal.tame(player);
            stored.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
            living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
            living.setHealth((float) data.getDouble("health_Num"));
            living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
            living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.getDouble("speed_number"));
            if (!data.getString("character").isEmpty()) {
                stored.getPersistentData().putString("character", data.getString("character"));
                for (String key : List.of("character_def", "character_att", "character_interval", "character_resist")) {
                    stored.getPersistentData().putDouble(key, data.getDouble(key));
                }
            }
            if (stored instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(stored.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            // 来源先恢复实体字段并执行初始化，再把当前主人的阵营与友方集合交给释放实体；
            // 否则编号妖兽的来源初始化会用自身物种队伍覆盖释放者阵营。
            Relations.shareEntityAllegiance(player, stored);
            level.addFreshEntity(stored);
        }
        CustomData.update(DataComponents.CUSTOM_DATA, bag, tag -> tag.putBoolean("yaoshoushifang", true));
    }

    public static void dye(Player player, SpiritBeastBagDyeMenu menu) {
        ItemStack bag = menu.getSlot(0).getItem();
        ItemStack dye = menu.getSlot(1).getItem();
        ItemStack output = menu.getSlot(2).getItem();
        Item blankBag = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudai_00"));
        if (!bag.is(BAGS) && bag.getItem() != blankBag) {
            tell(player, "dye_bag_required", true);
            return;
        }
        if (!dye.is(DYES)) {
            tell(player, "dye_required", true);
            return;
        }
        if (!output.isEmpty()) {
            tell(player, "dye_output_blocked", true);
            return;
        }
        int color = DYE_ITEMS.indexOf(dye.getItem());
        if (color >= 0) {
            ItemStack colored = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    "lingshoudai_%02d".formatted(color + 1))));
            colored.applyComponents(bag.getComponentsPatch());
            colored.setCount(1);
            menu.getSlot(2).set(colored);
        } else {
            menu.getSlot(2).set(ItemStack.EMPTY);
        }
        menu.getSlot(0).remove(1);
        menu.getSlot(1).remove(1);
        menu.broadcastChanges();
    }

    public static boolean isBag(ItemStack stack) {
        return stack.is(BAGS);
    }

    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    private static void tell(Player player, String key, boolean darkRed) {
        MutableComponent message = Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag." + key);
        player.displayClientMessage(darkRed ? message.withStyle(ChatFormatting.DARK_RED) : message, false);
    }

    private static Component red(String key, Object... arguments) {
        return Component.translatable("message." + ModId.VALUE + ".spirit_beast_bag." + key, arguments).withStyle(ChatFormatting.DARK_RED);
    }

    private static String cleanName(String name) {
        return name.replace("[", "").replace("]", "");
    }
}
