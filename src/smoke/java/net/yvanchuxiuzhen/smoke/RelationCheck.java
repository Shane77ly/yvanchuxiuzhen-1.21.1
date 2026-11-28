package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.relation.FriendList;
import net.yvanchuxiuzhen.system.relation.TeamRequest;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.relation.client.FriendScreen;
import net.yvanchuxiuzhen.system.relation.client.TeamRequestScreen;
import net.yvanchuxiuzhen.system.relation.content.TeamRequestMenu;
import net.yvanchuxiuzhen.system.relation.data.PlayerRelation;
import net.yvanchuxiuzhen.system.relation.data.Allegiance;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;
import net.yvanchuxiuzhen.system.relation.network.RelationButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;

/**
 * 关系系统的实机检查（单人客户端，只有一名玩家）：登录得到的团队与非玩家实体的阵营判定；
 * 给本人写入 11 格好友（第 1 格是本人名字，用作“在线”的一行）后打开好友界面截图，经网络按钮选中、星标、上移、删除、翻页，
 * 核对普通行与星标行读取实际境界文本、刷新后跟随档案变化，再恢复原境界名；向已在列表中的本人申请时不发申请；放入一名不在线者的申请后打开申请界面，
 * 受控改写本体团队后等待服务端每秒恢复计时推进，核对本体团队恢复为能力团队；
 * 同意时因对方不在线不添加、拒绝时清除申请；死亡重生与退出重进后好友数据保持。
 * 两名玩家之间的申请与删除未覆盖。
 */
final class RelationCheck extends SystemCheck {
    private static final String REALM_NAME = "元婴七重";
    private final Screenshots screenshots;
    private String name;
    private String team;
    private String originalRealmName;
    private double recoveryTimer;

    RelationCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("关系", report, levelId);
        this.screenshots = screenshots;
    }

    private PlayerRelation client() {
        return minecraft.player.getData(RelationAttachments.PLAYER);
    }

    private static void press(int... buttons) {
        for (int button : buttons) {
            PacketDistributor.sendToServer(new RelationButtonPayload(button));
        }
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 20) {
                    return false;
                }
                boolean[] recovered = new boolean[1];
                onServer(player -> recovered[0] = Cultivation.tizhiDouble(player, "jishiqi") > recoveryTimer);
                if (!recovered[0] && local < 200) {
                    return false;
                }
                onServer(player -> {
                    expectTrue("服务端每秒恢复计时已推进", recovered[0]);
                    expect("每秒恢复将本体团队写回能力团队", team, Relations.entityTeam(player));
                    expect("每秒恢复保留能力团队", team, Relations.team(player));
                });
                expectTrue("好友界面已打开", minecraft.screen instanceof FriendScreen);
                expect("普通行读取当前境界", "(" + REALM_NAME + ")" + name, client().row(1).level());
                expectTrue("第 1 行 在线标记", client().row(1).online());
                expect("第 2 行 未上线", "(未上线)好友01", client().row(2).level());
                expect("显示行数", 10, client().rows().size());
                press(0);
            }
            case 2 -> {
                if (local < 10) {
                    return false;
                }
                expect("选中第 1 行", 1, client().selected());
                expectTrue("选中行 属性文本", client().row(1).health().startsWith("血量上限:20.0(恢复:0.0)"));
                screenshots.take("relation_friends.png");
                press(12, 2, 13);
            }
            case 3 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("第 1 格 星标", client().row(1).star());
                expect("星标行读取当前境界", "(" + REALM_NAME + ")" + name, client().row(1).level());
                expect("上移后 第 2 行", "(未上线)好友02", client().row(2).level());
                expect("上移后 选中跟随名字", 2, client().selected());
                onServer(player -> {
                    Cultivation.set(player, CultivationText.LEVEL_NAME, originalRealmName);
                    FriendList.refresh(player);
                });
                press(15);
            }
            case 4 -> {
                if (local < 10) {
                    return false;
                }
                expect("刷新后境界随档案更新", "(" + originalRealmName + ")" + name, client().row(1).level());
                expect("删除后 显示行数", 9, client().rows().size());
                expect("删除后 取消选中", 0, client().selected());
                expect("删除后 好友数不变", 11, client().friendCount());
                expectTrue("删除后 对方团队不再友好", !client().allegiance().isFriendly("team_02"));
                press(11);
            }
            case 5 -> {
                if (local < 10) {
                    return false;
                }
                expect("下一页 页码", 1, client().page());
                expect("下一页 第 1 行", "(未上线)好友10", client().row(1).level());
                press(11);
            }
            case 6 -> {
                if (local < 10) {
                    return false;
                }
                expect("再下一页回到首页", 0, client().page());
                onServer(player -> {
                    player.closeContainer();
                    TeamRequest.request(player, player);
                    expect("已是好友时不发申请", "", player.getData(RelationAttachments.PLAYER).requestName());
                    player.getData(RelationAttachments.PLAYER).setRequest("好友99", "team_99");
                    player.syncData(RelationAttachments.PLAYER);
                    openRequest(player);
                });
            }
            case 7 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("申请界面已打开", minecraft.screen instanceof TeamRequestScreen);
                expect("待处理申请", "好友99", client().requestName());
                screenshots.take("relation_request.png");
                press(0);
            }
            case 8 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("同意后界面关闭", minecraft.screen == null);
                expect("申请者不在线时不添加", 11, client().friendCount());
                expect("申请者不在线时保留申请", "好友99", client().requestName());
                onServer(RelationCheck::openRequest);
            }
            case 9 -> {
                if (local < 20) {
                    return false;
                }
                press(1);
            }
            case 10 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("拒绝后界面关闭", minecraft.screen == null);
                expect("拒绝后清除申请", "", client().requestName());
                kill();
            }
            case 11 -> {
                return respawned(local);
            }
            case 12 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> checkKept("重生后", player));
                leaveWorld();
            }
            case 13 -> {
                return reopened(local);
            }
            case 14 -> {
                if (!inWorld(local)) {
                    return false;
                }
                onServer(player -> {
                    checkKept("重进后", player);
                    PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
                    relation.setRequest(name, team);
                    TeamRequest.press(player, 0);
                    expectTrue("Q19 同意已存在好友不去重增格且清空请求", relation.friendCount() == 11 && relation.requestName().isEmpty());
                    FriendList.open(player);
                    FriendList.press(player, 0);
                    FriendList.press(player, 15);
                    expectTrue("Q19 删除在线本人行双向标记删除且保持好友总数", relation.friendCount() == 11 && relation.friend(1).black());
                    player.closeContainer();
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        name = player.getDisplayName().getString();
        team = Relations.team(player);
        expectTrue("登录得到 8 个小写字母的团队名 " + team, team.matches("[a-z]{8}"));
        expectTrue("本人视自己为友方", Relations.isFriendly(player, player));
        Pig summon = EntityType.PIG.create(player.serverLevel());
        Pig stranger = EntityType.PIG.create(player.serverLevel());
        Relations.copyAllegiance(player, summon);
        Relations.setTeam(stranger, "smoke_other");
        expectTrue("召唤物继承阵营后视主人为友方", Relations.isFriendly(summon, player));
        expectTrue("主人视召唤物为友方", Relations.isFriendly(player, summon));
        expectTrue("其他团队不是友方", !Relations.isFriendly(player, stranger));
        Relations.addFriendlyTeam(stranger, team);
        expectTrue("加入友方集合后视玩家为友方", Relations.isFriendly(stranger, player));
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        relation.allegiance().setFriendly("smoke_block_friend", true);
        List<String> jar = Relations.friendTeamsForBlock(player);
        Relations.shareBlockAllegiance(jar, summon, "smoke_insect");
        expect("放虫保留虫物品团队", "smoke_insect", Relations.team(summon));
        Set<String> sharedView = Relations.friendlyTeams(summon);
        expectTrue("主人友方列表经蛊罐快照传给虫和既有视图", jar.contains("smoke_block_friend") && sharedView.contains("smoke_block_friend"));
        expectTrue("未包含团队不算友方", !sharedView.contains("smoke_false_friend"));
        Allegiance restored = Allegiance.CODEC.parse(NbtOps.INSTANCE,
                Allegiance.CODEC.encodeStart(NbtOps.INSTANCE, summon.getData(RelationAttachments.ENTITY)).getOrThrow()).getOrThrow();
        relation.allegiance().setFriendly("smoke_block_friend", false);
        expectTrue("阵营存档重读保留虫友方并独立于主人", restored.isFriendly("smoke_block_friend") && !restored.isFriendly("smoke_false_friend"));
        relation.addFriend(name, team);
        for (int i = 1; i <= 10; i++) {
            String number = String.format("%02d", i);
            relation.addFriend("好友" + number, "team_" + number);
            relation.allegiance().setFriendly("team_" + number, true);
        }
        originalRealmName = Cultivation.get(player, CultivationText.LEVEL_NAME);
        Cultivation.set(player, CultivationText.LEVEL_NAME, REALM_NAME);
        recoveryTimer = Cultivation.tizhiDouble(player, "jishiqi");
        Relations.setTamingTeam(stranger, player);
        expect("非玩家驯服来源只清本体团队", "", Relations.entityTeam(player));
        expect("本体团队改写后能力团队保持", team, Relations.team(player));
        FriendList.open(player);
    }

    private static void openRequest(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new TeamRequestMenu(id, inventory), Component.literal("ZuduiteamGui")));
    }

    private void checkKept(String label, ServerPlayer player) {
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        expect(label + " 团队名", team, Relations.team(player));
        expect(label + " 本体实体团队恢复为能力团队", team, Relations.entityTeam(player));
        expect(label + " 好友数", 11, relation.friendCount());
        expectTrue(label + " 第 1 格星标", relation.friend(1).star());
        expectTrue(label + " 第 2 格已删除", relation.friend(2).black());
        expect(label + " 第 3 格", "好友01", relation.friend(3).name());
        expectTrue(label + " 本人团队友好", relation.allegiance().isFriendly(team));
    }
}
