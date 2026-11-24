package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.system.economy.api.AuctionSeatRequestEvent;
import net.yvanchuxiuzhen.system.economy.client.CommerceScreen;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.town.TownRuntime;
import net.yvanchuxiuzhen.system.town.client.TownBuildScreen;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntity;
import net.yvanchuxiuzhen.system.town.data.TownBuildPlan;
import net.yvanchuxiuzhen.system.town.data.TownData;

/** Q69启动拒绝与Q70结构替换的针对性实机检查，使用可丢弃世界中的受控装置。 */
final class ConstructionCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<String> messages = new ArrayList<>();
    private final List<ItemStack> inventory = new ArrayList<>();
    private BlockPos auction;
    private BlockPos chair;
    private BlockPos plan;
    private BlockPos completedPlan;
    private BlockPos build03Marker;
    private Vec3 position;
    private int selected;

    ConstructionCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("拍卖与建筑修复", report, levelId);
        this.screenshots = screenshots;
        NeoForge.EVENT_BUS.addListener(this::chat);
        NeoForge.EVENT_BUS.addListener(this::freezeDrops);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 18) return reopened(local);
        if (step == 19) return inWorld(local);
        if (local < 15) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> use(chair);
            case 2 -> {
                if (!waitScreen(local, minecraft.screen instanceof CommerceScreen, "拍卖页")) return false;
                if (local < 140) return false;
                onServer(player -> {
                    EconomyBlockEntity block = (EconomyBlockEntity) player.level().getBlockEntity(auction);
                    expect("缺定义时拍卖进度保持零", 0, block.state().getInt("paimaijindu"));
                    expectTrue("缺定义拍卖标为本次已拒绝", block.auctionRejected());
                    expectTrue("自然右键座椅实际乘坐", player.isPassenger());
                });
                expect("连续现场刻只反馈一次", 1, messages.size());
                expectTrue("拒绝消息同时说明商品和低境界NPC缺失", messages.getFirst().contains("缺少拍卖商品和符合本场等级的NPC"));
                screenshots.take("construction_auction_rejected.png");
                minecraft.player.closeContainer();
            }
            case 3 -> onServer(player -> {
                player.stopRiding();
                player.connection.teleport(chair.getX() + 4, chair.getY(), chair.getZ(), 0, 0);
                plan = auction.offset(0, 24, 0);
                player.level().setBlock(plan.below(), Blocks.STONE.defaultBlockState(), 3);
                player.level().setBlock(plan.north(2).below(), Blocks.STONE.defaultBlockState(), 3);
                player.getInventory().setItem(8, TownRuntime.centerScroll());
                player.connection.teleport(plan.getX() + .5, plan.getY(), plan.getZ() - 1.5, 0, 15);
                player.inventoryMenu.broadcastChanges();
            });
            case 4 -> use(plan.below());
            case 5 -> onServer(player -> {
                expectTrue("卷轴真实放置十八槽规划核心", player.level().getBlockEntity(plan) instanceof TownBlockEntity town && town.getContainerSize() == 18);
                player.getInventory().setItem(8, ItemStack.EMPTY);
                player.inventoryMenu.broadcastChanges();
            });
            case 6 -> use(plan);
            case 7 -> {
                if (!waitScreen(local, minecraft.screen instanceof TownBuildScreen, "建筑物资页")) return false;
                onServer(player -> fill((TownBlockEntity) player.level().getBlockEntity(plan)));
            }
            case 8 -> {
                screenshots.take("construction_center_materials.png");
                buildButton();
            }
            case 9 -> {
                if (local < 80) return false;
                onServer(player -> {
                    TownBlockEntity center = (TownBlockEntity) player.level().getBlockEntity(plan);
                    expect("中心模板实际替换为一槽案台", 1, center.getContainerSize());
                    expectTrue("中心建造完成状态写回", center.getPersistentData().getBoolean("construction_completed"));
                    checkDrops(player);
                    for (ItemEntity entity : fixtureDrops(player)) entity.discard();
                    player.closeContainer();
                    plan = plan.east(80);
                    player.level().setBlock(plan.below(), Blocks.STONE.defaultBlockState(), 3);
                    player.level().setBlock(plan, BuiltInRegistries.BLOCK.get(id("chengzhen_buildblock")).defaultBlockState(), 3);
                    TownBlockEntity next = (TownBlockEntity) player.level().getBlockEntity(plan);
                    CompoundTag build = TownRuntime.centerScroll().getOrDefault(TownData.BUILD_PLAN.get(), TownBuildPlan.EMPTY).data();
                    build.putString("build_type", "工筑堂");
                    build.putString("build_direction", "坐北朝南");
                    next.getPersistentData().put("build_data", build);
                    var template = player.serverLevel().getStructureManager().getOrCreate(id("build02nouth"));
                    BlockPos core = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), next.getBlockState().getBlock()).getFirst().pos();
                    next.getPersistentData().putInt("shiftingX", core.getX());
                    next.getPersistentData().putInt("shiftingY", core.getY());
                    next.getPersistentData().putInt("shiftingZ", core.getZ());
                    fill(next);
                    player.level().setBlock(plan.north(2).below(), Blocks.STONE.defaultBlockState(), 3);
                    player.connection.teleport(plan.getX() + .5, plan.getY(), plan.getZ() - 1.5, 0, 15);
                });
            }
            case 10 -> {
                if (minecraft.level.getBlockState(plan).is(Blocks.VOID_AIR) || minecraft.player.distanceToSqr(Vec3.atCenterOf(plan)) >= 16) {
                    if (local > 200) {
                        fail("受控工筑堂客户端区块或传送未同步");
                        finish();
                    }
                    return false;
                }
                onServer(player -> {
                    expectTrue("受控工筑堂交互距离小于四格", player.distanceToSqr(Vec3.atCenterOf(plan)) < 16);
                    expectTrue("受控工筑堂服务端仍为规划核心", player.level().getBlockEntity(plan) instanceof TownBlockEntity town && town.getContainerSize() == 18);
                });
                expect("受控工筑堂客户端方块同步", id("chengzhen_buildblock"), BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(plan).getBlock()));
                use(plan);
            }
            case 11 -> {
                if (!waitScreen(local, minecraft.screen instanceof TownBuildScreen, "受控工筑堂物资页")) return false;
                buildButton();
            }
            case 12 -> {
                if (local < 80) return false;
                onServer(player -> {
                    checkPlan(player, plan);
                    completedPlan = plan;
                });
                screenshots.take("construction_planning_remainder.png");
                minecraft.player.closeContainer();
            }
            case 13 -> onServer(this::prepareBuild03);
            case 14 -> {
                if (minecraft.level.getBlockState(plan).is(Blocks.VOID_AIR) || minecraft.player.distanceToSqr(Vec3.atCenterOf(plan)) >= 16) {
                    if (local > 200) {
                        fail("灰瓦民居客户端区块或传送未同步");
                        finish();
                    }
                    return false;
                }
                use(plan);
            }
            case 15 -> {
                if (!waitScreen(local, minecraft.screen instanceof TownBuildScreen, "灰瓦民居物资页")) return false;
                buildButton();
            }
            case 16 -> {
                if (local < 80) return false;
                onServer(player -> checkBuild03(player));
                screenshots.take("construction_build03.png");
                minecraft.player.closeContainer();
            }
            case 17 -> leaveWorld();
            case 20 -> {
                onServer(player -> {
                    checkPlan(player, completedPlan);
                    checkBuild03(player);
                    player.getInventory().clearContent();
                    for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
                    player.getInventory().selected = selected;
                    player.connection.teleport(position.x, position.y, position.z, 0, 0);
                    player.inventoryMenu.broadcastChanges();
                });
                finish();
            }
            default -> throw new IllegalStateException("未知建设检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        selected = player.getInventory().selected;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        auction = player.blockPosition().above(6);
        chair = auction.east(2);
        player.level().setBlock(auction.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(chair.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(auction, BuiltInRegistries.BLOCK.get(id("paimaiblock")).defaultBlockState(), 3);
        player.level().setBlock(chair, BuiltInRegistries.BLOCK.get(id("paimaichair")).defaultBlockState(), 3);
        CompoundTag state = ((EconomyBlockEntity) player.level().getBlockEntity(auction)).state();
        state.putInt("paimaifangzhi", 2);
        state.putInt("paiblockX2", chair.getX());
        state.putInt("paiblockY2", chair.getY());
        state.putInt("paiblockZ2", chair.getZ());
        state.putString("paimaimoshi", "大量npc");
        state.putString("paimaidengji", "筑基期拍卖会");
        state.putInt("paimaiitemnumber", 9);
        NeoForge.EVENT_BUS.post(new AuctionSeatRequestEvent(player.serverLevel(), player, chair, AuctionSeatRequestEvent.Operation.CREATE));
        player.connection.teleport(chair.getX() + .5, chair.getY(), chair.getZ() - 1.5, 0, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private void fill(TownBlockEntity town) {
        town.setItem(0, stack(Items.OAK_LOG, 32, "wood"));
        town.setItem(1, stack(Items.OAK_LOG, 40, "wood"));
        town.setItem(2, stack(Items.COBBLESTONE, 32, "stone"));
        town.setItem(3, stack(Items.COBBLESTONE, 40, "stone"));
        town.setItem(4, stack(Items.WHITE_WOOL, 5, "wool"));
        town.setItem(5, stack(Items.DIAMOND, 7, "extra"));
        TownRuntime.countMaterials(town);
    }

    private void checkPlan(ServerPlayer player, BlockPos target) {
        TownBlockEntity town = (TownBlockEntity) player.level().getBlockEntity(target);
        expect("02模板保持十八槽规划核心", 18, town.getContainerSize());
        expectTrue("02模板写回完成状态", town.getPersistentData().getBoolean("construction_completed"));
        int[] remaining = {0, 12, 0, 12, 2, 7};
        for (int slot = 0; slot < remaining.length; slot++) expect("原槽余料" + slot, remaining[slot], town.getItem(slot).getCount());
        expect("十八槽回写未重复掉落夹具材料", 0, fixtureDrops(player, target).size());
    }

    private void prepareBuild03(ServerPlayer player) {
        plan = completedPlan.east(80);
        player.level().setBlock(plan.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(plan, BuiltInRegistries.BLOCK.get(id("chengzhen_buildblock")).defaultBlockState(), 3);
        TownBlockEntity next = (TownBlockEntity) player.level().getBlockEntity(plan);
        CompoundTag build = TownRuntime.centerScroll().getOrDefault(TownData.BUILD_PLAN.get(), TownBuildPlan.EMPTY).data();
        build.putString("build_type", "灰瓦民居");
        build.putString("build_direction", "坐南朝北");
        build.putInt("construction_need_log", 6);
        build.putInt("construction_need_stone", 8);
        build.putInt("construction_need_wool", 2);
        next.getPersistentData().put("build_data", build);
        var template = player.serverLevel().getStructureManager().getOrCreate(id("build03south"));
        BlockPos core = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), next.getBlockState().getBlock()).getFirst().pos();
        BlockPos marker = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), BuiltInRegistries.BLOCK.get(id("wallwhite"))).getFirst().pos();
        next.getPersistentData().putInt("shiftingX", core.getX());
        next.getPersistentData().putInt("shiftingY", core.getY());
        next.getPersistentData().putInt("shiftingZ", core.getZ());
        build03Marker = plan.offset(marker.getX() - core.getX(), marker.getY() - core.getY(), marker.getZ() - core.getZ());
        next.getPersistentData().putInt("lengthofX", 48);
        next.getPersistentData().putInt("lengthofY", 17);
        next.getPersistentData().putInt("lengthofZ", 48);
        next.getPersistentData().putString("team", "");
        next.setItem(0, stack(Items.OAK_LOG, 8, "build03-wood"));
        next.setItem(1, stack(Items.COBBLESTONE, 10, "build03-stone"));
        next.setItem(2, stack(Items.WHITE_WOOL, 3, "build03-wool"));
        next.setItem(3, stack(Items.DIAMOND, 4, "build03-extra"));
        TownRuntime.countMaterials(next);
        player.level().setBlock(plan.north(2).below(), Blocks.STONE.defaultBlockState(), 3);
        player.connection.teleport(plan.getX() + .5, plan.getY(), plan.getZ() - 1.5, 0, 15);
    }

    private void checkBuild03(ServerPlayer player) {
        TownBlockEntity town = (TownBlockEntity) player.level().getBlockEntity(plan);
        expect("03模板保持十八槽规划核心", 18, town.getContainerSize());
        expectTrue("03模板写回完成状态", town.getPersistentData().getBoolean("construction_completed"));
        expect("03模板写回类型", "灰瓦民居", town.getPersistentData().getCompound("build_data").getString("build_type"));
        expect("03模板写回方向", "坐南朝北", town.getPersistentData().getCompound("build_data").getString("build_direction"));
        expect("03原槽余木", 2, town.getItem(0).getCount());
        expect("03原槽余石", 2, town.getItem(1).getCount());
        expect("03原槽余羊毛", 1, town.getItem(2).getCount());
        expect("03原槽无关钻石完整保留", 4, town.getItem(3).getCount());
        expectTrue("03模板实际结构方块已放置", player.level().getBlockState(build03Marker).is(BuiltInRegistries.BLOCK.get(id("wallwhite"))));
        expect("03十八槽回写未重复掉落夹具材料", 0, fixtureDrops(player, plan).size());
    }

    private void checkDrops(ServerPlayer player) {
        expect("一槽模板余木12", 12, dropped(player, Items.OAK_LOG));
        expect("一槽模板余石12", 12, dropped(player, Items.COBBLESTONE));
        expect("一槽模板余羊毛2", 2, dropped(player, Items.WHITE_WOOL));
        expect("一槽模板无关钻石7完整保留", 7, dropped(player, Items.DIAMOND));
    }

    private int dropped(ServerPlayer player, Item item) {
        return fixtureDrops(player).stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private List<ItemEntity> fixtureDrops(ServerPlayer player) {
        return fixtureDrops(player, plan);
    }

    private List<ItemEntity> fixtureDrops(ServerPlayer player, BlockPos target) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(16), entity -> entity.getItem().getHoverName().getString().startsWith("r13-town-"));
    }

    private void freezeDrops(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity entity && entity.getItem().getHoverName().getString().startsWith("r13-town-")) {
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPickUpDelay(12000);
        }
    }

    private void chat(ClientChatReceivedEvent.System event) {
        String text = event.getMessage().getString();
        if (text.contains("本场拍卖未启动")) messages.add(text);
    }

    private static ItemStack stack(Item item, int count, String name) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("r13-town-" + name));
        return stack;
    }

    private void use(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private void buildButton() {
        minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).findFirst().orElseThrow().onPress();
    }

    private boolean waitScreen(int local, boolean ready, String label) {
        if (ready) return true;
        if (local > 200) { fail("未进入" + label); finish(); }
        return false;
    }

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name); }
}
