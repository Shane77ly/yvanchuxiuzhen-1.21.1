package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;
import net.yvanchuxiuzhen.system.equipment.projectile.KailingProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 精血自然使用和物品保存，以及隐藏开灵松手、首刻冻结与两种土地工具消费。 */
final class EquipmentConsumersCheck extends ActionCheck {
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Entity> created = new ArrayList<>();
    private Vec3 position;
    private float yaw;
    private float pitch;
    private GameType mode;
    private double level;
    private BlockPos site;
    private KailingProjectile shot;
    private AABB area;

    EquipmentConsumersCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("精血认主与隐藏开灵", report, levelId);
        script(
                new Action(5, this::prepare),
                new Action(6, player -> {
                    binding(player);
                    minecraft.execute(() -> screenshots.take("equipment-blood-messages.png"));
                    player.setGameMode(GameType.CREATIVE);
                    fire(player, null);
                }),
                new Action(20, player -> {
                    expectTrue("隐藏物品主手松手后首刻空类别且关闭重力", shot.isNoGravity() && kind(shot).isEmpty());
                }),
                new Action(4, player -> {
                    expectTrue("空类别十六刻移除且不生成斧机或其他工具机", shot.isRemoved() && machines(player).isEmpty());
                    fire(player, Items.IRON_HOE.getDefaultInstance());
                }),
                new Action(20, player -> {
                    expectTrue("首刻Hoe冻结来源gongjvchu与开灵一级", kind(shot).equals("gongjvchu") && enchant(shot) == 1);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SHOVEL));
                }),
                new Action(4, player -> {
                    List<ToolMachineEntity> machines = machines(player);
                    expectTrue("Hoe弹体十六刻只生成gongjvchu且换手后等级仍为一", machines.size() == 1
                            && entityId(machines.getFirst()).equals("gongjvchu")
                            && machines.getFirst().getPersistentData().getDouble("fumodengji") == 1);
                    created.addAll(machines);
                    soil(player, machines.getFirst(), true);
                    clearMachines(player);
                    fire(player, new ItemStack(Items.IRON_SHOVEL));
                }),
                new Action(20, player -> {
                    expectTrue("首刻Shovel冻结来源gongjvchan", kind(shot).equals("gongjvchan") && enchant(shot) == 1);
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }),
                new Action(5, player -> {
                    List<ToolMachineEntity> machines = machines(player);
                    expectTrue("Shovel弹体十六刻生成gongjvchan", machines.size() == 1 && entityId(machines.getFirst()).equals("gongjvchan"));
                    created.addAll(machines);
                    soil(player, machines.getFirst(), false);
                }));
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.getInventory().clearContent();
        site = player.blockPosition().above(8);
        area = new AABB(site).inflate(80);
        setBlock(player, site, Blocks.AIR.defaultBlockState());
        setBlock(player, site.above(), Blocks.AIR.defaultBlockState());
        player.setGameMode(GameType.CREATIVE);
        player.connection.teleport(site.getX() - 3.5, site.getY() + 2, site.getZ() + .5, -90, -70);
    }

    private void binding(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 11);
        ItemStack blood = blood(player.getStringUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STONE));
        use(player);
        expectTrue("副手非武器法宝拒绝认主并保留精血", blood.getCount() == 1 && data(player.getOffhandItem()).getString("belongToMasterUUID").isEmpty());
        ItemStack instrument = item("weaponsword_01");
        CompoundTag original = new CompoundTag();
        original.putInt("smoke_marker", 27);
        instrument.set(DataComponents.CUSTOM_DATA, CustomData.of(original));
        player.setItemInHand(InteractionHand.OFF_HAND, instrument);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 10);
        use(player);
        expectTrue("境界十拒绝认主", blood.getCount() == 1 && data(instrument).getString("belongToMasterUUID").isEmpty());
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 11);
        player.setItemInHand(InteractionHand.MAIN_HAND, blood("other-owner"));
        use(player);
        expectTrue("他人精血拒绝认主且不扣件", player.getMainHandItem().getCount() == 1 && data(instrument).getString("belongToMasterUUID").isEmpty());
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        InteractionResult result = use(player);
        expectTrue("生存原生使用本人精血写副手主人并扣一件且返回PASS", result == InteractionResult.PASS
                && player.getMainHandItem().isEmpty() && data(instrument).getString("belongToMasterUUID").equals(player.getStringUUID())
                && data(instrument).getInt("smoke_marker") == 27);
        ItemStack restored = ItemStack.parse(player.registryAccess(), instrument.save(player.registryAccess())).orElseThrow();
        expectTrue("认主物品实际保存重读保留主人及原有字段", data(restored).equals(data(instrument)));
        player.setItemInHand(InteractionHand.MAIN_HAND, blood(player.getStringUUID()));
        use(player);
        expectTrue("重复认主不重复扣血", player.getMainHandItem().getCount() == 1);
        original.putString("belongToMasterUUID", "another-instrument-owner");
        instrument.set(DataComponents.CUSTOM_DATA, CustomData.of(original));
        use(player);
        expectTrue("他人已认主法宝保留原主人和精血", data(instrument).getString("belongToMasterUUID").equals("another-instrument-owner")
                && player.getMainHandItem().getCount() == 1);
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.OFF_HAND, item("fabao_001"));
        use(player);
        expectTrue("创造原生使用精血保留数量且fabao标签同样认主", player.getMainHandItem().getCount() == 1
                && data(player.getOffhandItem()).getString("belongToMasterUUID").equals(player.getStringUUID()));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    private void fire(ServerPlayer player, ItemStack firstTickTool) {
        player.connection.teleport(site.getX() - 3.5, site.getY() + 2, site.getZ() + .5, -90, -70);
        ItemStack hidden = item("gongjvshechu");
        player.setItemInHand(InteractionHand.MAIN_HAND, hidden);
        use(player);
        expectTrue("隐藏开灵原生右键开始72000刻无动画使用", player.isUsingItem() && hidden.getUseDuration(player) == 72000
                && hidden.getUseAnimation() == UseAnim.NONE);
        player.releaseUsingItem();
        List<KailingProjectile> projectiles = player.serverLevel().getEntitiesOfClass(KailingProjectile.class, area);
        expectTrue("松手生成唯一速度二零点一伤害不可拾取弹体且物品保留", projectiles.size() == 1 && hidden.getCount() == 1);
        shot = projectiles.getFirst();
        created.add(shot);
        expectTrue("隐藏开灵弹体发射数值", Math.abs(shot.getDeltaMovement().length() - 2) < .00001 && shot.getBaseDamage() == .1
                && shot.pickup == AbstractArrow.Pickup.DISALLOWED && shot.getOwner() == player);
        if (firstTickTool != null) {
            firstTickTool.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("kailing")), 1);
            player.setItemInHand(InteractionHand.MAIN_HAND, firstTickTool);
        }
    }

    private void soil(ServerPlayer player, ToolMachineEntity machine, boolean farming) {
        setBlock(player, site, Blocks.DIRT.defaultBlockState());
        setBlock(player, site.above(), Blocks.AIR.defaultBlockState());
        CompoundTag state = machine.getPersistentData();
        state.putString("jinxing", "mining");
        state.putDouble("xiaoshitick2", 4);
        state.putDouble("mubiaoX", site.getX());
        state.putDouble("mubiaoY", site.getY());
        state.putDouble("mubiaoZ", site.getZ());
        machine.tick();
        BlockState result = player.serverLevel().getBlockState(site);
        if (farming) {
            expectTrue("受控推进第五采掘刻gongjvchu把泥土设湿度七耕地", result.is(Blocks.FARMLAND) && result.getValue(BlockStateProperties.MOISTURE) == 7);
        } else {
            List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3),
                    entity -> entity.getItem().is(Items.DIRT));
            created.addAll(drops);
            expectTrue("受控推进第五采掘刻gongjvchan挖掉泥土并在主人位置产一件实物", result.isAir()
                    && drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() == 1);
        }
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos, player.serverLevel().getBlockState(pos));
        player.serverLevel().setBlock(pos, state, 3);
    }

    private List<ToolMachineEntity> machines(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ToolMachineEntity.class, area);
    }

    private void clearMachines(ServerPlayer player) {
        machines(player).forEach(Entity::discard);
    }

    private static String entityId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static String kind(KailingProjectile projectile) {
        CompoundTag tag = new CompoundTag();
        projectile.addAdditionalSaveData(tag);
        return tag.getString("kailing_kind");
    }

    private static int enchant(KailingProjectile projectile) {
        CompoundTag tag = new CompoundTag();
        projectile.addAdditionalSaveData(tag);
        return tag.getInt("kailing_enchant");
    }

    private static ItemStack blood(String owner) {
        ItemStack blood = item("jingxve");
        CompoundTag data = new CompoundTag();
        data.putString("belongToMasterUUID", owner);
        blood.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return blood;
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private static InteractionResult use(ServerPlayer player) {
        return player.gameMode.useItem(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.stopUsingItem();
        created.forEach(Entity::discard);
        blocks.forEach((pos, state) -> player.serverLevel().setBlock(pos, state, 3));
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, level);
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
    }
}
