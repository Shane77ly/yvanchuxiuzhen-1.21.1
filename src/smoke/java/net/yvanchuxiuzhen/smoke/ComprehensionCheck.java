package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionMenu;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlockEntity;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.gongfa.network.GongfaButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/**
 * 参悟台的实机检查：放置后的模式与说明，右键打开界面，参悟（消耗寿元 20、所需境界降一层、参悟次数 + 1），
 * 切换到继承模式，分别验证未升级和升级时消耗典籍；通过同一菜单按钮核对小数需求的减值与文字。
 */
final class ComprehensionCheck extends SystemCheck {
    private static final List<RequirementCase> REQUIREMENT_CASES = List.of(
            new RequirementCase("低于一层", 1.5, 0.5, ""),
            new RequirementCase("筑基小数", 12.75, 11.75, ""),
            new RequirementCase("化神下界小数", 41.75, 40.75, ""),
            new RequirementCase("化神小数", 42.25, 41.25, "化神"));
    private final Screenshots screenshots;
    private BlockPos pos;
    private double lifetime;
    private ItemStack consumed;
    private CultivationData originalCultivation;
    private TechniqueBook originalRequirementBook;

    private record RequirementCase(String label, double start, double result, String text) {
    }

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
                onServer(player -> table().setItem(0, consumed.copy()));
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
                    expectTrue("未升级时典籍也被消耗", table().getItem(0).isEmpty());
                    table().setItem(0, consumed.copy());
                });
                PacketDistributor.sendToServer(new GongfaButtonPayload(1));
            }
            case 4 -> {
                if (local < 10) {
                    return false;
                }
                onServer(player -> {
                    TechniqueBook equipped = Techniques.main(player).orElseThrow();
                    expect("继承第二本典籍后升到 2 层", 2.0, equipped.level());
                    expectTrue("升级后书被消耗", table().getItem(0).isEmpty());
                    player.closeContainer();
                    Techniques.setSlot(player, ItemStack.EMPTY);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                    player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                });
            }
            default -> {
                return requirementStep(step - 5, local);
            }
        }
        return true;
    }

    private boolean requirementStep(int phase, int local) {
        int index = phase / 3;
        if (index == REQUIREMENT_CASES.size()) {
            onServer(this::restoreRequirementState);
            finish();
            return true;
        }
        RequirementCase sample = REQUIREMENT_CASES.get(index);
        if (phase % 3 == 0) {
            onServer(player -> {
                if (index == 0) {
                    originalCultivation = player.getData(XiulianAttachments.CULTIVATION);
                    player.level().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canwutai_block"))
                            .defaultBlockState(), Block.UPDATE_ALL);
                }
                player.setData(XiulianAttachments.CULTIVATION, new CultivationData());
                Cultivation.set(player, CultivationValue.LIFETIME, 1000);
                TechniqueDefinition base = named("分金术");
                TechniqueDefinition definition = new TechniqueDefinition(base.item(), base.name(), base.gradeText(), base.grade(),
                        "原需求文字", sample.start(), base.body(), base.intro(), base.elements(), base.tiers());
                ItemStack input = Techniques.createBook(definition);
                originalRequirementBook = Techniques.book(input).orElseThrow().withOwner(player.getUUID(), player.getName().getString())
                        .withProgress(3, 7).withValues(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0));
                input.set(GongfaData.TECHNIQUE.get(), originalRequirementBook);
                input.setCount(3);
                table().setItem(0, input);
                player.level().getBlockState(pos).useWithoutItem(player.level(), player,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            });
            return true;
        }
        if (phase % 3 == 1) {
            if (local < 10 || !(minecraft.screen instanceof ComprehensionScreen)
                    || !(minecraft.player.containerMenu instanceof ComprehensionMenu menu)
                    || Techniques.book(menu.getSlot(0).getItem()).isEmpty()) {
                return requirementTimeout(sample, local, "没有同步输入书并打开参悟界面");
            }
            PacketDistributor.sendToServer(new GongfaButtonPayload(1));
            return true;
        }
        boolean[] serverProcessed = {false};
        onServer(player -> serverProcessed[0] = Techniques.book(table().getItem(0)).filter(book -> book.comprehensions() == 1).isPresent());
        Optional<TechniqueBook> clientBook = minecraft.player.containerMenu instanceof ComprehensionMenu menu
                ? Techniques.book(menu.getSlot(0).getItem()) : Optional.empty();
        if (local < 10 || !serverProcessed[0] || clientBook.filter(book -> book.comprehensions() == 1).isEmpty()) {
            return requirementTimeout(sample, local, "菜单按钮未完成参悟或结果未同步");
        }
        onServer(player -> {
            TechniqueBook book = Techniques.book(table().getItem(0)).orElseThrow();
            expect(sample.label() + " 所需层数减一", sample.result(), book.definition().level());
            expect(sample.label() + " 所需境界文字", sample.text(), book.definition().levelText());
            expect(sample.label() + " 寿元消耗", 980, Cultivation.get(player, CultivationValue.LIFETIME));
            expect(sample.label() + " 参悟者", Optional.of(player.getUUID()), book.comprehender());
            expect(sample.label() + " 参悟次数", 1, book.comprehensions());
            expect(sample.label() + " 槽内数量写为一", 1, table().getItem(0).getCount());
            expect(sample.label() + " 保留研习归属与成长", List.of(originalRequirementBook.owner(), originalRequirementBook.ownerName(),
                    originalRequirementBook.level(), originalRequirementBook.exp(), originalRequirementBook.values()),
                    List.of(book.owner(), book.ownerName(), book.level(), book.exp(), book.values()));
            expect(sample.label() + " 刷新说明使用新文字", "当前功法需要达到" + sample.text() + "才可修习。", table().text(0));
        });
        TechniqueBook book = clientBook.orElseThrow();
        expect(sample.label() + " 层数同步菜单", sample.result(), book.definition().level());
        expect(sample.label() + " 文字同步菜单", sample.text(), book.definition().levelText());
        expect(sample.label() + " 寿元同步本人", 980, Cultivation.get(minecraft.player, CultivationValue.LIFETIME));
        if (index == 1) {
            screenshots.take("gongfa_comprehension_fractional.png");
        }
        onServer(player -> {
            player.closeContainer();
            table().setItem(0, ItemStack.EMPTY);
        });
        return true;
    }

    private boolean requirementTimeout(RequirementCase sample, int local, String reason) {
        if (local > 200) {
            fail(sample.label() + reason);
            onServer(this::restoreRequirementState);
            finish();
        }
        return false;
    }

    private void restoreRequirementState(ServerPlayer player) {
        player.closeContainer();
        table().setItem(0, ItemStack.EMPTY);
        player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        player.setData(XiulianAttachments.CULTIVATION, originalCultivation);
        player.syncData(XiulianAttachments.CULTIVATION);
    }
}
