package net.yvanchuxiuzhen.system.world.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.world.WorldSetup;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 寻灵盘在人间界召出指引，Alt 储存物品实例坐标，Ctrl 按来源坐标和判定返回。 */
public final class SpiritCompassItem extends ContentItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderer;
    private boolean rendered;

    public SpiritCompassItem(Properties properties, List<Component> tooltip) { super(properties, tooltip); }
    public void setRenderProvider(GeoRenderProvider renderer) { this.renderer = renderer; }
    public void markRendered() { rendered = true; }
    @Override public Object getRenderProvider() { return renderer; }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idleController", 0,
                state -> rendered ? state.setAndContinue(RawAnimation.begin().thenLoop("animation.ling_14.idle")) : PlayState.STOP));
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return new ItemStack(this); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer owner)) return result;
        if (!level.dimension().location().toString().equals("yvanchuxiuzhen:dimensionrenjian")) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.compass_dimension"), false);
            return result;
        }
        ItemStack stack = result.getObject();
        if (ModifierKeys.alt(player)) {
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            data.putDouble("dangqianX", player.getX());
            data.putDouble("dangqianY", player.getY());
            data.putDouble("dangqianZ", player.getZ());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            owner.connection.teleport(0, 32, 0, player.getYRot(), player.getXRot());
        } else if (ModifierKeys.ctrl(player)) {
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (data.getDouble("dangqianX") + data.getDouble("dangqianY") + data.getDouble("dangqianZ") != 0) {
                owner.connection.teleport(data.getDouble("dangqianX"), data.getDouble("dangqianY"), data.getDouble("dangqianZ"), player.getYRot(), player.getXRot());
            } else {
                owner.connection.teleport(-70, 63, -275, player.getYRot(), player.getXRot());
            }
        } else {
            GuidanceHalo halo = WorldSetup.<GuidanceHalo>entityType("guanghuan").get().create(server);
            halo.moveTo(player.getX(), player.getY() + 1, player.getZ(), player.getYRot(), 0);
            halo.setYBodyRot(player.getYRot());
            halo.setYHeadRot(player.getYRot());
            halo.yRotO = player.getYRot();
            halo.xRotO = 0;
            halo.yBodyRotO = player.getYRot();
            halo.yHeadRotO = player.getYRot();
            Relations.shareEntityAllegiance(player, halo);
            Relations.setTeam(halo, "");
            halo.shareNavigation(player);
            halo.tame(player);
            halo.finalizeSpawn(server, server.getCurrentDifficultyAt(halo.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            server.addFreshEntity(halo);
            StructureSearch.search(server, new Vec3(player.getX(), player.getY() + 1, player.getZ()), player.getStringUUID());
            player.getCooldowns().addCooldown(this, 100);
        }
        return result;
    }
}
