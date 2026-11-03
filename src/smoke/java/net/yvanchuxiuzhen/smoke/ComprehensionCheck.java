package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.gongfa.TechniqueLibrary;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.client.ComprehensionScreen;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlockEntity;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.gongfa.network.GongfaButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 参悟台的实机检查：放置后的模式与说明，右键打开界面，参悟（消耗寿元 20、所需境界降一层、参悟次数 + 1），
 * 切换到继承模式，继承经验未升级时书留在槽内（Q27），再次继承升级后书被消耗。
 */
final class ComprehensionCheck extends SystemCheck {
    private final Screenshots screenshots;
    private BlockPos pos;
    private double lifetime;
    private ItemStack consumed;

    ComprehensionCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("参悟台", report, levelId);
        this.screenshots = screenshots;
    }

    private static TechniqueDefinition named(String name) {
        return TechniqueLibrary.all().stream().filter(definition -> definition.name().equals(name)).findFirst().orElseThrow();
    }

    private ComprehensionTableBlockEntity table() {
        return (ComprehensionTableBlockEntity) minecraft.getSingleplayerServer().overworld().getBlockEntity(pos);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                pos = player.blockPosition().offset(2, 0, 0);
                player.level().setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                player.level().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canwutai_block"))
                        .defaultBlockState(), Block.UPDATE_ALL);
                expect("放置后为参悟模式", ComprehensionTableBlockEntity.COMPREHEND, table().mode());
                expect("放置后的第一行说明", "请放入你想要，但限于等级无法学习的功法。", table().text(0));
                table().setItem(0, Techniques.createBook(named("分金术")));
                // 研习会关闭当前界面，继承用的书在打开参悟界面之前准备。
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 5);
                ItemStack equipped = Techniques.createBook(named("五行基础"));
                Techniques.study(player, equipped);
                Techniques.setSlot(player, equipped);
                consumed = Techniques.createBook(named("五行基础"));
                Techniques.study(player, consumed);
                consumed.set(GongfaData.TECHNIQUE.get(), Techniques.book(consumed).orElseThrow().withProgress(2, 0));
                Cultivation.set(player, CultivationValue.LIFETIME, 1000);
                lifetime = Cultivation.get(player, CultivationValue.LIFETIME);
                player.level().getBlockState(pos).useWithoutItem(player.level(), player,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            });
            case 1 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("右键打开参悟界面", minecraft.screen instanceof ComprehensionScreen);
                PacketDistributor.sendToServer(new GongfaButtonPayload(2));
                PacketDistributor.sendToServer(new GongfaButtonPayload(1));
            }
            case 2 -> {
                if (local < 10) {
                    return false;
                }
                onServer(player -> {
                    TechniqueBook book = Techniques.book(table().getItem(0)).orElseThrow();
                    expect("参悟消耗寿元 0² × 3 + 20", lifetime - 20, Cultivation.get(player, CultivationValue.LIFETIME));
                    expect("参悟后所需境界降一层", 2.0, book.definition().level());
                    expect("参悟后所需境界文字", "练气二层", book.definition().levelText());
                    expect("参悟次数", 1.0, book.comprehensions());
                    expect("刷新后的第三行说明", "下次参悟需要消耗寿元:§323", table().text(2));
                });
                screenshots.take("gongfa_comprehension.png");
                onServer(player -> table().setItem(0, consumed));
                PacketDistributor.sendToServer(new GongfaButtonPayload(0));
                PacketDistributor.sendToServer(new GongfaButtonPayload(1));
            }
            case 3 -> {
                if (local < 10) {
                    return false;
                }
                onServer(player -> {
                    expect("切换为继承模式", ComprehensionTableBlockEntity.INHERIT, table().mode());
                    TechniqueBook equipped = Techniques.main(player).orElseThrow();
                    expect("继承 1² × 1 × 10 × 0.5 = 5 点经验", 5.0, equipped.exp());
                    expectTrue("未升级时书留在槽内（Q27）", !table().getItem(0).isEmpty());
                });
                PacketDistributor.sendToServer(new GongfaButtonPayload(1));
            }
            case 4 -> {
                if (local < 10) {
                    return false;
                }
                onServer(player -> {
                    TechniqueBook equipped = Techniques.main(player).orElseThrow();
                    expect("再次继承后升到 2 层", 2.0, equipped.level());
                    expectTrue("升级后书被消耗", table().getItem(0).isEmpty());
                    player.closeContainer();
                    Techniques.setSlot(player, ItemStack.EMPTY);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                    player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }
}
