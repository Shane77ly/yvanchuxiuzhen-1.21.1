package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;

/**
 * 蓄放型远程符的松手过程（来源各 {@code Itemfulu<编号>Item.releaseUsing} 与 {@code Itemfulu10KeYiShiYongYuanChengWuPinDeTiaoJian}）：
 * 先把符箓品阶写入使用者的持久数据 {@code fululevel}（弹体第一刻读取），再付精力；付清后按符箓取弹体种类，
 * 以速度 2、伤害 0.1 从眼前射出并播放释放声。35 号符不查找弹药，其一张消耗由弹体第一刻从主手扣除；其余符消耗持有或背包中的一张，
 * 创造模式不消耗。
 */
public final class FuluRanged {
    private FuluRanged() {
    }

    public static void release(ServerPlayer player, ItemStack used, int number) {
        player.getPersistentData().putDouble("fululevel", FuluUse.grade(used));
        if (!FuluUse.pay(player, used)) {
            return;
        }
        Kind kind = switch (number) {
            case 10 -> Kind.DEBUFF;
            case 20 -> Kind.FURNACE;
            case 28 -> Kind.GLOW;
            case 33 -> Kind.BIND;
            case 35 -> Kind.BLOOD;
            default -> throw new IllegalArgumentException("not a ranged talisman: " + number);
        };
        ItemStack ammunition = ItemStack.EMPTY;
        if (number != 35) {
            ammunition = ammunition(player, used);
            if (!player.getAbilities().instabuild && ammunition.isEmpty()) {
                return;
            }
        }
        ServerLevel level = player.serverLevel();
        TalismanProjectile.launch(level, FuluSetup.projectile(kind).get(), player, new Vec3(player.getX(), player.getEyeY() - 0.1, player.getZ()), player.getLookAngle(), 2, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulushifang")),
                SoundSource.PLAYERS, 1, 1f / (level.random.nextFloat() * 0.5f + 1) + 0.5f);
        if (!player.getAbilities().instabuild && !ammunition.isEmpty()) {
            ammunition.shrink(1);
        }
    }

    /** 优先取手持的同种符，否则取背包中第一张。 */
    private static ItemStack ammunition(ServerPlayer player, ItemStack used) {
        ItemStack held = ProjectileWeaponItem.getHeldProjectile(player, stack -> stack.is(used.getItem()));
        if (!held.isEmpty()) {
            return held;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(used.getItem())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
