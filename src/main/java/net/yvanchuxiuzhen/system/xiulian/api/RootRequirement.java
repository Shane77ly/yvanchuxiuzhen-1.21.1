package net.yvanchuxiuzhen.system.xiulian.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;

/**
 * 研习功法书与技能书的灵根要求（来源 {@code BookRightClick} 的研习分支）。要求文字为空或含“无”直接满足；含“五”须五行灵根都至少 1，
 * 否则按金水木火土阴阳功业罡煞逐字判定，任一满足即可，提示取最后一个不满足的字。
 */
public final class RootRequirement {
    /** 要求中的字与对应条件、不满足时的提示，按来源的判定次序。 */
    private static final List<Requirement> REQUIREMENTS = List.of(
            new Requirement("金", "§4需要金灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_GOLD) >= 1),
            new Requirement("水", "§4需要水灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_WATER) >= 1),
            new Requirement("木", "§4需要木灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_TREE) >= 1),
            new Requirement("火", "§4需要火灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_FIRE) >= 1),
            new Requirement("土", "§4需要土灵根！", player -> Cultivation.get(player, CultivationValue.ROOT_SOIL) >= 1),
            new Requirement("阴", "§4需要体质为阴！", player -> Cultivation.get(player, CultivationValue.YINYANG) <= -1),
            new Requirement("阳", "§4需要体质为阳！", player -> Cultivation.get(player, CultivationValue.YINYANG) >= 1),
            new Requirement("功", "§4需要功德！", player -> Cultivation.get(player, CultivationValue.MERITS) >= 1),
            new Requirement("业", "§4需要业力！", player -> Cultivation.get(player, CultivationValue.MERITS) <= -1),
            new Requirement("罡", "§4需要罡气！", player -> Cultivation.get(player, CultivationValue.VIGOROUS) >= 1),
            new Requirement("煞", "§4需要煞气！", player -> Cultivation.get(player, CultivationValue.VIGOROUS) <= -1));

    private record Requirement(String mark, String hint, Predicate<Player> met) {
    }

    private RootRequirement() {
    }

    /** 不满足要求时的提示（可能为空串，要求文字不含任何可判定的字时即如此）；满足时为空。 */
    public static Optional<String> unmet(Player player, String body) {
        if (body.isEmpty() || body.contains("无")) {
            return Optional.empty();
        }
        if (body.contains("五")) {
            return hasAllRoots(player) ? Optional.empty() : Optional.of("§4需要五行灵根！");
        }
        String hint = "";
        boolean allowed = false;
        for (Requirement requirement : REQUIREMENTS) {
            if (body.contains(requirement.mark())) {
                if (requirement.met().test(player)) {
                    allowed = true;
                } else {
                    hint = requirement.hint();
                }
            }
        }
        return allowed ? Optional.empty() : Optional.of(hint);
    }

    private static boolean hasAllRoots(Player player) {
        return Cultivation.get(player, CultivationValue.ROOT_GOLD) >= 1 && Cultivation.get(player, CultivationValue.ROOT_WATER) >= 1
                && Cultivation.get(player, CultivationValue.ROOT_TREE) >= 1 && Cultivation.get(player, CultivationValue.ROOT_FIRE) >= 1
                && Cultivation.get(player, CultivationValue.ROOT_SOIL) >= 1;
    }
}
