package net.yvanchuxiuzhen;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 工程结构守卫，对应 docs/development/engineering.md“边界与自动检查”的六条规则；违反时 gradlew check 失败。
 * 所属单位按包划分：system.&lt;系统&gt;、family.&lt;内容族&gt;、panel、flow、platform 与根包中的两个 Mod 入口。
 */
class ArchitectureTest {
    private static final String ROOT = "net.yvanchuxiuzhen";
    private static final String COMMON_ENTRY = ROOT + ".YvanchuxiuzhenMod";
    private static final String CLIENT_ENTRY = ROOT + ".YvanchuxiuzhenClient";
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    /** 规则 1：系统之间只依赖对方的 api 包；flow、panel、family 依赖系统时同样只用 api。 */
    @Test
    void otherSystemsAreReachedOnlyThroughApi() {
        classes().that().resideInAnyPackage(ROOT + ".system..", ROOT + ".flow..", ROOT + ".panel..", ROOT + ".family..")
                .should(dependOnOtherSystemsOnlyThroughApi())
                .check(CLASSES);
    }

    /** 规则 2：client 包与客户端入口之外的类不依赖任何 client 包。 */
    @Test
    void clientCodeStaysInClientPackages() {
        noClasses().that().resideOutsideOfPackage(ROOT + "..client..")
                .and().doNotHaveFullyQualifiedName(CLIENT_ENTRY)
                .should().dependOnClassesThat()
                .resideInAnyPackage(ROOT + "..client..", "net.minecraft.client..", "net.neoforged.neoforge.client..")
                .check(CLASSES);
    }

    /** 规则 3：rule 包只放纯规则与公式，不依赖 Minecraft 与 NeoForge。 */
    @Test
    void rulePackagesArePure() {
        noClasses().that().resideInAPackage(ROOT + "..rule..")
                .should().dependOnClassesThat().resideInAnyPackage("net.minecraft..", "net.neoforged..")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    /** 规则 4：platform 只做平台适配，不依赖系统、流程、玩家面板与内容族。 */
    @Test
    void platformDoesNotDependOnGameplay() {
        noClasses().that().resideInAPackage(ROOT + ".platform..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(ROOT + ".system..", ROOT + ".flow..", ROOT + ".panel..", ROOT + ".family..")
                .check(CLASSES);
    }

    /** 规则 5：系统之间没有循环依赖。 */
    @Test
    void systemsAreFreeOfCycles() {
        slices().matching(ROOT + ".system.(*)..").should().beFreeOfCycles().check(CLASSES);
    }

    /** 规则 6：除两个 Mod 入口外，任何类不依赖其他内容族或玩家面板。 */
    @Test
    void familiesAndPanelAreNotDependedOn() {
        classes().that().doNotHaveFullyQualifiedName(COMMON_ENTRY).and().doNotHaveFullyQualifiedName(CLIENT_ENTRY)
                .should(notDependOnOtherFamiliesOrPanel())
                .check(CLASSES);
    }

    private static ArchCondition<JavaClass> dependOnOtherSystemsOnlyThroughApi() {
        return new ArchCondition<>("只通过 api 包依赖其他系统") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String from = unitOf(origin.getPackageName());
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    String targetPackage = dependency.getTargetClass().getPackageName();
                    String to = unitOf(targetPackage);
                    if (to != null && to.startsWith("system.") && !to.equals(from) && !isApiPackage(targetPackage, to)) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDependOnOtherFamiliesOrPanel() {
        return new ArchCondition<>("不依赖其他内容族或玩家面板") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String from = unitOf(origin.getPackageName());
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    String to = unitOf(dependency.getTargetClass().getPackageName());
                    if (to != null && (to.startsWith("family.") || to.equals("panel")) && !to.equals(from)) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static boolean isApiPackage(String packageName, String unit) {
        String api = ROOT + "." + unit + ".api";
        return packageName.equals(api) || packageName.startsWith(api + ".");
    }

    /** 返回包所属单位，如 system.skill、family.decor、panel，根包为空串；项目外的包返回 null。 */
    private static String unitOf(String packageName) {
        if (packageName.equals(ROOT)) {
            return "";
        }
        if (!packageName.startsWith(ROOT + ".")) {
            return null;
        }
        String[] parts = packageName.substring(ROOT.length() + 1).split("\\.");
        if ((parts[0].equals("system") || parts[0].equals("family")) && parts.length > 1) {
            return parts[0] + "." + parts[1];
        }
        return parts[0];
    }
}
