package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Field;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.NpcManagementMenu;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.client.NpcManagementScreen;
import net.yvanchuxiuzhen.system.npc.entity.NpcMob;

/** 管理工具真实开页、编辑保存、两类Mob右键效果与存档重读。 */
final class NpcManagementCheck extends SystemCheck {
    private static final String NAME = "管理界面烟测";
    private static final String TASK = "管理工具烟测任务";
    private static final String PRODUCTS = "yvanchuxiuzhen:iron_ingot%无%灵石%1%无";
    private static final String SHOP = "管理工具烟测商店%商品%来源数据重读";
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private CompletableFuture<Void> languageReload;
    private String oldLanguage;
    private ParticleStatus oldParticles;
    private GameType oldMode;
    private Difficulty oldDifficulty;
    private Vec3 oldPosition;
    private float oldYaw;
    private float oldPitch;
    private ListTag npcInventory;
    private ListTag cowInventory;
    private UUID npcId;
    private UUID cowId;
    private BlockPos npcBlock;
    private BlockPos cowBlock;
    private final Map<ChunkPos, Boolean> forcedChunks = new LinkedHashMap<>();
    private boolean prepared;
    private boolean savedToolChecked;
    private int selected;
    private int particleCount;
    private boolean aborting;
    private final Set<Particle> observedHappyParticles = Collections.newSetFromMap(new IdentityHashMap<>());

    NpcManagementCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("管理界面", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (aborting) {
            if (!languageReload.isDone()) return false;
            finish();
            return true;
        }
        try {
            return runStep(step, local);
        } catch (RuntimeException exception) {
            abort("烟测异常：" + exception);
            return true;
        }
    }

    private boolean runStep(int step, int local) {
        switch (step) {
            case 0 -> {
                oldLanguage = minecraft.getLanguageManager().getSelected();
                minecraft.getLanguageManager().setSelected("zh_cn");
                languageReload = minecraft.reloadResourcePacks();
            }
            case 1 -> {
                if (!languageReload.isDone()) return false;
                expect("管理界面使用中文资源", "天道NPC设置", I18n.get("menu.yvanchuxiuzhen.tiandao_np_cset"));
                oldParticles = minecraft.options.particles().get();
                minecraft.options.particles().set(ParticleStatus.ALL);
                onServer(this::prepare);
            }
            case 2 -> minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            case 3 -> {
                if (!(minecraft.screen instanceof NpcManagementScreen screen)
                        || !(minecraft.player.containerMenu instanceof NpcManagementMenu)) {
                    if (local > 200) abort("自然使用管理工具没有打开设置菜单");
                    return false;
                }
                expect("设置标题为中文", "天道NPC设置", screen.getTitle().getString());
                expect("保存按钮为中文", "保存", button("保存").getMessage().getString());
                expect("四个菜单页标签为中文", 4, List.of("基本", "任务", "商店", "资质").stream()
                        .filter(label -> screen.children().stream().anyMatch(child -> child instanceof Button widget
                                && widget.getMessage().getString().equals(label))).count());
                editBoxes().getFirst().setValue(NAME);
                button("境界降低").onPress();
                expect("来源等级下限循环值", "41.0", profileValue("npclevel"));
                button("境界增加").onPress();
                expect("来源等级上限循环值", "0.0", profileValue("npclevel"));
                button("境界增加").onPress();
                button("境界增加").onPress();
                expect("来源等级按钮按一层逐次增加", "2.0", profileValue("npclevel"));
                checkbox().onPress();
                screenshots.take("npc_management_profile.png");
                return true;
            }
            case 4 -> {
                onServer(player -> {
                    player.getInventory().setItem(9, new ItemStack(Items.IRON_SWORD));
                    player.containerMenu.broadcastChanges();
                });
                if (!(minecraft.player.containerMenu instanceof NpcManagementMenu menu) || menu.getSlot(6).getItem().isEmpty()) {
                    if (local > 100) abort("装备模板没有进入管理菜单的玩家背包");
                    return false;
                }
                minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 6, 0,
                        net.minecraft.world.inventory.ClickType.PICKUP, minecraft.player);
            }
            case 5 -> minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 0, 0,
                    net.minecraft.world.inventory.ClickType.PICKUP, minecraft.player);
            case 6 -> {
                if (!(minecraft.player.containerMenu instanceof NpcManagementMenu menu)
                        || menu.getSlot(0).getItem().isEmpty()) {
                    if (local > 100) abort("管理面板装备槽未接收单件装备");
                    return false;
                }
                button("任务").onPress();
                expect("任务页十项输入可见", 10, editBoxes().size());
                editBoxes().getFirst().setValue(TASK);
                screenshots.take("npc_management_tasks.png");
            }
            case 7 -> {
                button("商店").onPress();
                expect("商店页中文标签可解析", "商品列表", I18n.get("gui.yvanchuxiuzhen.npc.management.field.productlist"));
                editBoxes().get(0).setValue(PRODUCTS);
                editBoxes().get(1).setValue(SHOP);
                screenshots.take("npc_management_shop.png");
            }
            case 8 -> {
                button("资质").onPress();
                expect("资质页不接受来源五档之外的自由文本", 0, editBoxes().size());
                expect("资质档位封闭为来源五档", 5, List.of("贫弱", "勉强", "平庸", "强劲", "绝佳").stream()
                        .filter(label -> minecraft.screen.children().stream().anyMatch(child -> child instanceof Button widget
                                && widget.getMessage().getString().equals(label))).count());
                button("绝佳").onPress();
                for (int index = 0; index < 12; index++) button(">>").onPress();
                button("贫弱").onPress();
                button(">>").onPress();
                expect("十三项资质正向循环后回到气血上限", true, minecraft.screen.children().stream()
                        .anyMatch(child -> child instanceof Button widget && widget.getMessage().getString().equals("绝佳") && !widget.active));
                screenshots.take("npc_management_ratings.png");
            }
            case 9 -> button("保存").onPress();
            case 10 -> {
                if (minecraft.screen instanceof NpcManagementScreen) {
                    if (local > 200) abort("保存按钮没有提交并关闭管理菜单");
                    return false;
                }
                if (!savedToolChecked) {
                    onServer(this::checkSavedTool);
                    savedToolChecked = true;
                }
                Entity clientNpc = minecraft.level.getEntity(entityNetworkId(npcId));
                if (clientNpc == null) {
                    if (local > 200) abort("NPC目标未同步到客户端");
                    return false;
                }
                observedHappyParticles.clear();
                minecraft.gameMode.interact(minecraft.player, clientNpc, InteractionHand.MAIN_HAND);
            }
            case 11 -> {
                particleCount = nearbyHappyParticles(npcBlock);
                if (particleCount < 10 && local < 40) return false;
                onServer(this::checkManagedNpc);
                screenshots.take("npc_management_npc_apply.png");
                expect("NPC右键出现来源十枚村民祝福粒子", 10, particleCount);
                Entity clientCow = minecraft.level.getEntity(entityNetworkId(cowId));
                if (clientCow == null) {
                    if (local > 200) abort("原版Mob目标未同步到客户端");
                    return false;
                }
                observedHappyParticles.clear();
                minecraft.gameMode.interact(minecraft.player, clientCow, InteractionHand.MAIN_HAND);
            }
            case 12 -> {
                particleCount = nearbyHappyParticles(cowBlock);
                if (particleCount < 10 && local < 40) return false;
                onServer(this::checkVanillaMob);
                expect("原版Mob右键出现来源十枚村民祝福粒子", 10, particleCount);
                screenshots.take("npc_management_vanilla_mob.png");
                leaveWorld();
            }
            case 13 -> { return reopened(local); }
            case 14 -> { return inWorld(local); }
            case 15 -> {
                if (local < 10) return false;
                onServer(this::checkReloadedState);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            }
            case 16 -> {
                if (!(minecraft.screen instanceof NpcManagementScreen screen)
                        || !(minecraft.player.containerMenu instanceof NpcManagementMenu menu)) {
                    if (local > 200) abort("存档重进后无法重新打开管理工具");
                    return false;
                }
                expect("重读姓名字段", NAME, editBoxes().getFirst().getValue());
                expect("重读境界字段", "2.0", menu.fieldValue("npclevel"));
                expect("重读中文管理界面", "天道NPC设置", screen.getTitle().getString());
                screenshots.take("npc_management_reopened.png");
                onServer(ServerPlayer::closeContainer);
            }
            case 17 -> {
                if (local < 2) return false;
                onServer(this::cleanup);
                minecraft.options.particles().set(oldParticles);
                minecraft.getLanguageManager().setSelected(oldLanguage);
                languageReload = minecraft.reloadResourcePacks();
            }
            case 18 -> {
                if (!languageReload.isDone()) return false;
                finish();
            }
            default -> throw new IllegalStateException("未知管理界面烟测步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        oldMode = player.gameMode.getGameModeForPlayer();
        oldDifficulty = player.serverLevel().getDifficulty();
        oldPosition = player.position();
        oldYaw = player.getYRot();
        oldPitch = player.getXRot();
        BlockPos origin = player.blockPosition();
        npcBlock = origin.offset(0, 0, 3);
        cowBlock = origin.offset(4, 0, 3);
        for (ChunkPos chunk : List.of(new ChunkPos(npcBlock), new ChunkPos(cowBlock))) {
            forcedChunks.put(chunk, player.serverLevel().getForcedChunks().contains(chunk.toLong()));
        }
        prepared = true;
        Arena.level(player);
        forcedChunks.keySet().forEach(chunk -> player.serverLevel().setChunkForced(chunk.x, chunk.z, true));
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(BuiltInRegistries.ITEM.get(id("tiandao_np_csetitem"))));
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();

        NpcMob npc = NpcSetup.<NpcMob>entityType("businessman_01").get().spawn(player.serverLevel(), npcBlock, MobSpawnType.COMMAND);
        if (npc == null) throw new IllegalStateException("无法生成NPC管理目标");
        npc.setNoAi(true);
        npcId = npc.getUUID();
        Cow cow = net.minecraft.world.entity.EntityType.COW.create(player.serverLevel());
        if (cow == null) throw new IllegalStateException("无法生成原版牛管理目标");
        cow.setNoAi(true);
        cow.setPersistenceRequired();
        cow.setPos(cowBlock.getX() + .5, cowBlock.getY(), cowBlock.getZ() + .5);
        player.serverLevel().addFreshEntity(cow);
        cowId = cow.getUUID();
        player.teleportTo(player.serverLevel(), origin.getX() + .5, origin.getY(), origin.getZ() + .5, 0, 0);
    }

    private void checkSavedTool(ServerPlayer player) {
        ItemStack tool = player.getMainHandItem();
        CompoundTag data = tool.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        expect("工具写入中文姓名", NAME, data.getString("npcname"));
        expect("工具保存来源等级按钮值", 2, data.getDouble("npclevel"));
        expect("工具保存任务页文本", TASK, data.getString("renwulist1"));
        expect("工具保存商品表", PRODUCTS, data.getString("productlist"));
        expect("工具保存商店说明", SHOP, data.getString("shopinformation"));
        expect("工具保存封闭五档资质", "绝佳", data.getString("healthmaxtxt"));
        expect("工具保存末项资质", "贫弱", data.getString("speednumtxt"));
        expect("固定位置复选框只置true", true, data.getBoolean("npcset"));
        ListTag entries = data.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
        ItemStack configured = ItemStack.parseOptional(player.registryAccess(), entries.getCompound(0));
        expect("装备配置保留单件剑", 1, configured.getCount());
        expect("装备配置保持铁剑身份", Items.IRON_SWORD, configured.getItem());
    }

    private void checkManagedNpc(ServerPlayer player) {
        NpcMob target = (NpcMob) player.serverLevel().getEntity(npcId);
        if (target == null) throw new IllegalStateException("管理NPC未加载");
        CompoundTag data = target.getPersistentData();
        expect("NPC显示自定义中文姓名", NAME, target.getDisplayName().getString());
        expect("NPC等级持久数据来自管理工具", 2, data.getDouble("LEVELnumber"));
        expect("NPC持久化任务脚本", TASK, data.getString("renwulist1"));
        expect("NPC持久化商品与说明", PRODUCTS, data.getString("productlist"));
        expect("NPC资质由唯一所有者消费", "绝佳", data.getString("health_Max_LEVEL"));
        expect("NPC固定位置标记传播", true, data.getBoolean("npcset"));
        expect("主手装备的数量为一", 1, target.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).getCount());
        expect("主手装备身份", Items.IRON_SWORD, target.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).getItem());
        npcInventory = target.saveWithoutId(new CompoundTag()).getList("HandItems", Tag.TAG_COMPOUND);
    }

    private void checkVanillaMob(ServerPlayer player) {
        Entity target = player.serverLevel().getEntity(cowId);
        if (!(target instanceof Mob)) throw new IllegalStateException("原版牛未加载");
        expect("原版Mob显示自定义中文姓名", NAME, target.getDisplayName().getString());
        expect("原版Mob主手装备数量为一", 1, ((Mob) target).getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).getCount());
        expect("原版Mob主手装备身份", Items.IRON_SWORD, ((Mob) target).getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).getItem());
        expectTrue("未接NPC所有者的原版Mob未被报告为完整档案消费",
                !target.getPersistentData().contains("renwulist1") && !target.getPersistentData().contains("LEVELnumber"));
        cowInventory = target.saveWithoutId(new CompoundTag()).getList("HandItems", Tag.TAG_COMPOUND);
    }

    private void checkReloadedState(ServerPlayer player) {
        Entity npc = player.serverLevel().getEntity(npcId);
        Entity cow = player.serverLevel().getEntity(cowId);
        expectTrue("NPC存档重读保持原身份及管理任务", npc instanceof NpcMob
                && npc.getPersistentData().getString("renwulist1").equals(TASK));
        if (npc != null) expectTrue("NPC存档重读保持单件装备", npcInventory.equals(npc.saveWithoutId(new CompoundTag()).getList("HandItems", Tag.TAG_COMPOUND)));
        expectTrue("原版Mob存档重读保持管理姓名及装备", cow != null && cow.getDisplayName().getString().equals(NAME)
                && cowInventory.equals(cow.saveWithoutId(new CompoundTag()).getList("HandItems", Tag.TAG_COMPOUND)));
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        Entity npc = npcId == null ? null : player.serverLevel().getEntity(npcId);
        if (npc != null) npc.discard();
        Entity cow = cowId == null ? null : player.serverLevel().getEntity(cowId);
        if (cow != null) cow.discard();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(oldMode);
        player.server.setDifficulty(oldDifficulty, true);
        forcedChunks.forEach((chunk, wasForced) -> {
            if (!wasForced) {
                player.serverLevel().setChunkForced(chunk.x, chunk.z, false);
            }
        });
        player.teleportTo(player.serverLevel(), oldPosition.x, oldPosition.y, oldPosition.z, oldYaw, oldPitch);
        player.inventoryMenu.broadcastChanges();
    }

    private int nearbyHappyParticles(BlockPos center) {
        Vec3 expected = new Vec3(center.getX() + .5, center.getY() + 1, center.getZ() + .5);
        minecraft.particleEngine.iterateParticles(particle -> {
            if (particle.getClass().getName().equals("net.minecraft.client.particle.SuspendedTownParticle")
                    && particle.getPos().distanceTo(expected) < 2.6) observedHappyParticles.add(particle);
        });
        return observedHappyParticles.size();
    }

    private List<EditBox> editBoxes() {
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return List.of();
        return screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
    }

    private Button button(String message) {
        return minecraft.screen.children().stream().filter(child -> child instanceof Button widget
                        && widget.getMessage().getString().equals(message))
                .map(child -> (Button) child).findFirst().orElseThrow(() -> new IllegalStateException("找不到中文按钮 " + message));
    }

    private Checkbox checkbox() {
        return minecraft.screen.children().stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
    }

    private String profileValue(String key) {
        try {
            Field values = NpcManagementScreen.class.getDeclaredField("values");
            values.setAccessible(true);
            return ((Map<?, ?>) values.get(minecraft.screen)).get(key).toString();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法读取管理界面当前编辑值", exception);
        }
    }

    private int entityNetworkId(UUID uuid) {
        int[] id = {-1};
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) id[0] = entity.getId();
        });
        return id[0];
    }

    private void abort(String reason) {
        fail(reason);
        if (prepared) onServer(this::cleanup);
        if (oldParticles != null) minecraft.options.particles().set(oldParticles);
        if (oldLanguage != null) {
            minecraft.getLanguageManager().setSelected(oldLanguage);
            languageReload = minecraft.reloadResourcePacks();
        } else {
            languageReload = CompletableFuture.completedFuture(null);
        }
        aborting = true;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
