"""将源码索引整理为架构审阅台账；目标归属为提案，运行证据单列。"""
from __future__ import annotations
from collections import Counter, defaultdict
import csv,json,re
from pathlib import Path
from investigate_legacy import csv_text

ROOT=Path(__file__).resolve().parents[2]
DEST=ROOT/'docs/reference/legacy/inventory'

FOLDERS={
 'yvanchuxiuzhen':{
  '~/systemset/gongfa':'gongfa','~/systemset/levelup':'cultivation','~/systemset/leijie':'cultivation',
  '~/systemset/damage':'combat','~/systemset/zhiling/shanghai':'combat','~/systemset/tiandao':'world',
  '~/systemset':'gameplay','~/gui/dantian':'cultivation','~/gui/playeruplevel':'cultivation','~/gui':'presentation',
  '~/skill/jineng/jinenggui/yvanyingchuqiao':'cultivation','~/skill/zhenfa':'formations','~/skill':'abilities',
  '~/tool/canwutai':'gongfa','~/tool/liandanlu':'alchemy','~/tool/yinhang':'economy','~/tool/xiulitai':'refining',
  '~/tool/xvnlong':'creatures','~/tool/murenzhuang':'combat','~/tool':'life',
  '~/item/zawu/ling15':'combat','~/item/zawu/lingshoudai':'creatures','~/item/zawu/qiankundai':'storage',
  '~/item/zawu/taoguan':'storage','~/item/book':'knowledge','~/item/huafu':'talismans',
  '~/item/lingshi':'economy','~/item/fire':'abilities','~/item/food':'life','~/item/jieri':'life','~/item':'materials',
  '~/entity/npc':'npc','~/entity':'creatures','~/weapon':'equipment','~/plant':'life','~/food':'life',
  '~/fuben':'world','~/dimension':'world','~/biome':'world','~/decorate/jiegou':'world','~/decorate':'life',
  '~/baoxiang':'world','~/chengjiu':'quests','~/enchanting':'combat','~/effect':'combat','~/lizi':'presentation',
  '~/tag/zhenfatag':'formations','~/tag/baoxiang':'world','~/tag/foodxiangguan':'life','~/tag':'gameplay'},
 'yvanchuxitong':{
  '~/yvanchulianqi':'refining','~/yvanchuliandan':'alchemy','~/yvanchuzhenfa':'formations',
  '~/yvanchupengren':'life','~/mujiangtai':'life','~/huafu':'talismans','~/gongfa':'gongfa',
  '~/shop':'economy','~/xvanshang':'quests','~/duihua':'quests','~/chengzhen':'world','~/fuben':'world',
  '~/xvnzhanyiji':'world','~/baoxiang':'world','~/guchong':'creatures','~/zawu/cailiaodai':'storage',
  '~/zawu/taolun':'life','~/zawu/makebook':'life','~/zawu':'gameplay','~/zhuangshiwu':'life',
  '~/fumo':'combat','~/zhiling':'gameplay','~/asd':'gameplay'}
}
OVERRIDES={
 'cultivation':'LEVELup LevelupCorrect RealmupCorrect Kaishixiulian Playerhuifu PlayerJoin02 Jinruyouxichushenxvanze Linggenlevel EntityThunder ThunderBegin Suijikaijv HealthMaxUp PowerMaxUp SoulMaxUp AttackUp ManaUp SoulUp Fulutexiao37',
 'gongfa':'Kaishigongfa Gongfashuaxin Gongfashuaxin2 Gongfashuxingjisuan Gongfashuxingjiyv BookGiveGongfa Gongfaxvexi GongfaxvexiGengXinYouXiKe GongfaxvexiZaiFangKuaiShangYouJian PlayerGUI04 PlayerGUI04DangZheGeJieMianDaKaiQiJianMeiKeFaSheng PlayerGUI04DangZheGeJieMianBeiGuanBi',
 'attributes':'ShuxingjisuanZong Shuxingjisuan Shuxingjisuan2 Playershuxingjisuan Shujvtongbu Attributexianshi Weaponshuxing',
 'combat':'EntityBeforeHurt EntityBeforeHurt2 EntityHurt EntityHurt2 EntityHurt3 DamageIntegration DamageDefense DamageXiaoguo FindTheEnemy',
 'equipment':'PlayerGUI03 PlayerGUI03DangZheGeJieMianDaKaiQiJianMeiKeFaSheng PlayerGUI03DangZheGeJieMianBeiGuanBi Shipintick Dantianbenmingfabao',
 'formations':'Luopan LuopanFangZhiFangKuaiShi Flagmother Flagson Flagmotheritem Flagsonitem FlagmotherZaiFangKuaiShangYouJian Flagmotherhecheng Flagsonhecheng FlagmotheritemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhi FlagmotheritemDangYouJianDianJiKongQiShiShiTiDeWeiZhi',
 'quests':'Setrenwu RenwufangkuaiGui Blockrenwuchufa PlayerTickxitong',
 'materials':'Cailiaopinjie Yihuoshujvfuyv','world':'Worldbegin Worldclose Worldload WorldTick Yijichushishengcheng',
 'creatures':'Entityxvnfu Entitylevelup Entityhuifu','economy':'Jiagejizhijisuan',
 'presentation':'ItemLore Lore Lore02 Tiankongxvanran',
}


def read(name):
    return list(csv.DictReader((DEST/(name+'.csv')).open(encoding='utf-8',newline='')))


def element_owner(row):
    for owner,names in OVERRIDES.items():
        if row['element'] in names.split():return owner,'explicit_element_proposal'
    path=row['folder'].lower()
    rules=FOLDERS[row['source_mod']]
    matches=[p for p in rules if path==p or path.startswith(p+'/')]
    if row['type'] in ('biome','dimension','feature','structure','fluid'):return 'world','element_type_proposal'
    if row['type'] in ('particle','overlay'):return 'presentation','element_type_proposal'
    if row['type'] in ('enchantment','potioneffect'):return 'combat','element_type_proposal'
    if row['type']=='attribute':return 'attributes','element_type_proposal'
    if matches:return rules[max(matches,key=len)],'source_folder_proposal'
    return 'gameplay','source_root_cross_system_review'


def state_owner(row):
    name,scope,mod=row['name'],row['scope'],row['source_mod']
    if scope!='PlayerVariables':
        special={'dailyexperience':'cultivation','lianqicailiaolist':'materials','caoyaolist':'alchemy','yiwuliebiao':'life','jiagetiaozheng':'economy','xvanshanglist':'quests'}
        if name=='worlddata':return 'world / npc / economy','混合载体；拆分维度实例、NPC 定义与价格数据'
        return special.get(name,'world'),'维度或主世界数据；区分固定定义、实例及管理设置'
    if mod=='yvanchuxitong':
        if name=='renwuitem':return 'quests / economy / storage','隐藏铁锭混合载体；任务持久态、材料仓与交互会话分拆'
        if name in ('renwuliebiao','renwulist','renwutag'):return 'quests','玩家任务记录；无消费字段不自动新增玩法'
        if name.startswith('bank'):return 'economy','账户持久化；bankceshi 字段独立审查'
        if name.startswith('huafu'):return 'talismans','玩家符箓知识'
        if name=='playercaoyaojiexi':return 'alchemy','以药材 ID 为键的辨识进度'
        if name=='playercailiaojiexi':return 'materials','以材料 ID 为键的辨识进度'
        if name in ('gongfa','gongfalinshi','xvexigongfa','suijishu'):return 'gongfa','声明与生效分离；suijishu 是学习小游戏的序列游标'
        return 'gameplay','无外部消费时保留调查记录，不创建目标存档字段'
    if name=='tizhiitem':return 'cultivation / abilities / world / attributes','混合载体；体质、修炼、技能、副本与属性贡献分拆'
    if name.endswith('_display'):return 'attributes','派生展示值；保留实际读取语义，不复制为第二份真值'
    if name in ('gongfa','gongfalinshi','gongfaxvexi'):return 'gongfa','装备书籍与比较缓存；书籍成长随实例'
    if re.fullmatch(r'jineng\d+',name) or name.startswith(('colddown','jineng')) or name in ('flyjineng','qinggong','wuqiskill','gongjian','gongjianjineng','flybegin','lshift','lctrl','lalt','arrowlinshi','feibiaolinshi'):
        return 'abilities','区分物品槽、施法状态、冷却及输入会话'
    if name.endswith('ornaments') or name in ('benmingfabao','wuqilinshi') or name.startswith('kuijia'):return 'equipment','装备槽/本命关系与比较缓存分离'
    if name.startswith('dati'):return 'knowledge','答题会话；读取与答题回调一起迁移'
    if name in ('team','teamwork','friendlist'):return 'combat','队伍与关系数据；好友显示分页独立'
    if name.startswith('Alchemy_'):return 'alchemy','职业等级、经验与门槛'
    if name.startswith('Refiner_'):return 'refining','职业等级、经验与门槛'
    if name.startswith('Rune_'):return 'talismans','职业等级、经验与门槛'
    if name.startswith('collection_'):return 'materials','采集职业成长'
    if name.startswith('cooking_') or name=='jiujing':return 'life','烹饪职业或饮酒状态'
    if name.startswith(('puppet_','breed_','insect_','ghost_','blood_')):return 'creatures / cultivation','职业声明；逐入口判断所属玩法，不能按字段补造独立系统'
    if name in ('health_Max','health_Rec','power_Max','power_Rec','soul_Max','soul_Rec','attack_Num','attack_Def','mana_Att','mana_Def','soul_Att','soul_Def','Cooling_reduction','walkspeed'):
        return 'attributes','基础值与派生值依实际计算/消费者拆分'
    if name in ('dangqianGUI','linshi','renwulistlinshi'):return 'gameplay','界面会话或未见外部消费的声明'
    if name=='chuansongitem':return 'world','传送暂存；普通死亡与非死亡复制规则不同'
    return 'cultivation','成长/资源/身份；数值、单位和生命周期按来源对应'


def main():
    elements=read('source_elements')
    element_map={}
    for row in elements:
        owner,evidence=element_owner(row)
        row.update(proposed_owner=owner,ownership_basis=evidence,status='架构提案；尚未批准；目录归组不等于行为验收')
        element_map[(row['source_mod'],row['element'])]=row
    file_owners=defaultdict(set)
    for row in read('element_files'):
        file_owners[(row['source_mod'],row['generated_file'])].add(element_map[(row['source_mod'],row['element'])]['proposed_owner'])
    classes=[];class_map={}
    for row in read('java_files'):
        owners=file_owners[(row['source_mod'],row['source_file'])]
        if not owners:owners={'presentation' if '/client/' in row['source_file'] or '/entity/model/' in row['source_file'] else 'platform'}
        row={k:row[k] for k in ('source_mod','class','source_file','package_area')}
        row['proposed_owner']=' / '.join(sorted(owners))
        row['basis']='source_element_metadata' if file_owners[(row['source_mod'],row['source_file'])] else 'infrastructure_package_review'
        classes.append(row);class_map[(row['source_mod'],row['class'])]=row
    dependencies=defaultdict(list)
    for row in read('method_calls'):
        a=class_map[(row['source_mod'],row['class'])]['proposed_owner']
        b=class_map.get((row['target_mod'],row['target_class']),{}).get('proposed_owner','unresolved_target')
        if a!=b:dependencies[(a,b)].append(row)
    edges=[{'caller_owner':a,'callee_owner':b,'call_sites':len(sites),
            'examples':'; '.join(f"{r['source_mod']}/{r['source_file']}:{r['line']}" for r in sites[:3]),
            'evidence':'当前源码调用 + 提案归组；不是目标架构允许的全部依赖'} for (a,b),sites in sorted(dependencies.items())]
    accesses=defaultdict(list)
    for row in read('typed_field_accesses'):
        if row['owner_mod']:accesses[(row['owner_mod'],row['scope'],row['field'])].append(row)
    clone={(r['source_mod'],r['field']):r for r in read('clone_policy')}
    states=[]
    for row in read('variables_detailed'):
        owner,role=state_owner(row)
        sites=accesses[(row['source_mod'],row['scope'],row['name'])]
        policy=clone.get((row['source_mod'],row['name'])) if row['scope']=='PlayerVariables' else None
        states.append({'source_mod':row['source_mod'],'scope':row['scope'],'field':row['name'],'type':row['java_type'],
            'source':f"{row['source_file']}:{row['source_line']}",'proposed_owner':owner,'proposed_role':role,
            'read_sites':sum(r['access']=='read_or_access' for r in sites),'write_sites':sum(r['access']=='write' for r in sites),
            'clone_policy':policy['copy_condition'] if policy else 'not_player_clone',
            'clone_source':f"{policy['source_file']}:{policy['line']}" if policy else '',
            'evidence':'显式接收类型/能力 lambda 已匹配；对象内部修改计为访问，另查键索引',
            'external_consumption':'有静态外部访问' if sites else '仅声明/序列化/复制；未找到外部成员访问',
            'accessing_systems':' / '.join(sorted({class_map[(r['source_mod'],r['class'])]['proposed_owner'] for r in sites})),
            'examples':'; '.join(f"{r['source_mod']}/{r['source_file']}:{r['line']}" for r in sites[:3]),'identity':row['identity']})
    keys=defaultdict(list)
    for row in read('dynamic_state_keys'):
        keys[(row['source_mod'],row['carrier'],row['resolved_key_expression'])].append(row)
    dynamic=[{'source_mod':mod,'carrier':carrier,'key_expression':expr,'sites':len(sites),
        'operations':'; '.join(sorted({r['operation'] for r in sites})),
        'examples':'; '.join(f"{r['source_file']}:{r['line']}" for r in sites[:3]),
        'proposed_systems':' / '.join(sorted({class_map[(r['source_mod'],r['class'])]['proposed_owner'] for r in sites})),
        'evidence':'保留表达式与载体类型；所属实例、取值域以系统正文人工核对为准'} for (mod,carrier,expr),sites in sorted(keys.items())]
    counts=Counter(r['proposed_owner'] for r in elements)
    summaries=[{'system':owner,'source_elements':n,'java_classes':sum(r['proposed_owner']==owner for r in classes),
        'source_folders':'; '.join(sorted({r['source_mod']+':'+r['folder'] for r in elements if r['proposed_owner']==owner})),
        'status':'全量索引已归组；运行/行为验收未执行'} for owner,n in sorted(counts.items())]
    for name,rows in [('ownership_proposal',elements),('class_ownership',classes),('module_dependencies',edges),('state_lifecycle',states),('dynamic_key_catalog',dynamic),('system_coverage',summaries)]:
        (DEST/(name+'.csv')).write_text(csv_text(rows),encoding='utf-8',newline='\n')
    payload={'states':states,'dynamic_keys':dynamic,'systems':summaries,'dependencies':edges,
             'source_design_summary':json.loads((DEST/'source_design_summary.json').read_text(encoding='utf-8')),
             'architecture_summary':json.loads((DEST/'architecture_summary.json').read_text(encoding='utf-8'))}
    (DEST/'architecture_workbook.json').write_text(json.dumps(payload,ensure_ascii=False),encoding='utf-8')
    print(json.dumps({'elements':len(elements),'java_classes':len(classes),'states':len(states),'dynamic_groups':len(dynamic),'dependency_pairs':len(edges),'systems':dict(counts)},ensure_ascii=False))


if __name__=='__main__':main()
