"""将源码索引整理为架构审阅台账。

元素归属取自人工维护的 ownership_assignment.csv：“已读”行按系统设计的归属判定读过来源规则，
“候选”行是批量归组结果，不作为实现依据。类、状态与依赖的归组由元素归属推导，运行证据单列。
"""
from __future__ import annotations
from collections import Counter, defaultdict
import csv,json,re
from pathlib import Path
from investigate_legacy import csv_text

ROOT=Path(__file__).resolve().parents[2]
DEST=ROOT/'docs/reference/legacy/inventory'
STATUS_TEXT={'已读':'归属已读来源规则；行为验收另计','候选':'候选归属；所属系统开工时按归属判定确认'}


def read(name):
    return list(csv.DictReader((DEST/(name+'.csv')).open(encoding='utf-8',newline='')))


def state_owner(row):
    name,scope,mod=row['name'],row['scope'],row['source_mod']
    if scope!='PlayerVariables':
        special={'dailyexperience':'xiulian','lianqicailiaolist':'material','caoyaolist':'liandan','yiwuliebiao':'lianqi','jiagetiaozheng':'economy',
                 'xvanshanglist':'quest','baoxianglevellinshi':'treasure','fubenweizhi':'dungeon'}
        if name=='worlddata':return 'world / npc / economy / town','混合载体；拆分维度实例、NPC 定义、价格数据与城镇实例'
        return special.get(name,'world'),'维度或主世界数据；区分固定定义、实例及管理设置'
    if mod=='yvanchuxitong':
        if name=='renwuitem':return 'quest / economy / storage','隐藏铁锭混合载体；任务持久态、材料仓与交互会话分拆'
        if name in ('renwuliebiao','renwulist','renwutag'):return 'quest','玩家任务记录；无消费字段不自动新增玩法'
        if name.startswith('bank'):return 'economy','账户持久化；bankceshi 字段独立审查'
        if name.startswith('huafu'):return 'fulu','玩家符箓知识'
        if name=='playercaoyaojiexi':return 'liandan','以药材 ID 为键的辨识进度'
        if name=='playercailiaojiexi':return 'material','以材料 ID 为键的辨识进度'
        if name in ('gongfa','gongfalinshi','xvexigongfa','suijishu'):return 'gongfa','声明与生效分离；suijishu 是学习小游戏的序列游标'
        return '待读','无外部消费时保留调查记录，不创建目标存档字段'
    if name=='tizhiitem':return 'xiulian / skill / dungeon / attribute','混合载体；体质、修炼、技能、副本与属性贡献分拆'
    if name.endswith('_display'):return 'attribute','派生展示值；保留实际读取语义，不复制为第二份真值'
    if name in ('gongfa','gongfalinshi','gongfaxvexi'):return 'gongfa','装备书籍与比较缓存；书籍成长随实例'
    if re.fullmatch(r'jineng\d+',name) or name.startswith(('colddown','jineng')) or name in ('flyjineng','qinggong','wuqiskill','gongjian','gongjianjineng','flybegin','lshift','lctrl','lalt','arrowlinshi','feibiaolinshi'):
        return 'skill','区分物品槽、施法状态、冷却及输入会话'
    if name=='benmingfabao':return 'xiulian','丹田本命槽位；槽内物品的本命培养数据归 equipment'
    if name.endswith('ornaments') or name=='wuqilinshi' or name.startswith('kuijia'):return 'equipment','装备槽与比较缓存分离'
    if name.startswith('dati'):return 'book','答题会话；读取与答题回调一起迁移'
    if name in ('team','teamwork','friendlist'):return 'relation','队伍与关系数据；好友显示分页独立'
    if name.endswith(('_LEVEL','_experience','_experience_Max')):return 'profession','副职业等级与经验；逐入口判断所属玩法，不能按字段补造独立系统'
    if name=='jiujing':return 'cooking','饮酒状态'
    if name in ('health_Max','health_Rec','power_Max','power_Rec','soul_Max','soul_Rec','attack_Num','attack_Def','mana_Att','mana_Def','soul_Att','soul_Def','Cooling_reduction','walkspeed'):
        return 'attribute','基础值与派生值依实际计算/消费者拆分'
    if name in ('dangqianGUI','linshi','renwulistlinshi'):return 'panel','界面会话或未见外部消费的声明'
    if name=='chuansongitem':return 'world','传送暂存；普通死亡与非死亡复制规则不同'
    return 'xiulian','成长/资源/身份；数值、单位和生命周期按来源对应'


def main():
    elements=read('source_elements')
    assignment={(r['source_mod'],r['element']):r for r in read('ownership_assignment')}
    element_map={}
    for row in elements:
        assigned=assignment[(row['source_mod'],row['element'])]
        row.update(proposed_owner=assigned['owner'],ownership_basis=assigned['evidence'],status=STATUS_TEXT[assigned['status']])
        element_map[(row['source_mod'],row['element'])]=row
    file_owners=defaultdict(set)
    for row in read('element_files'):
        file_owners[(row['source_mod'],row['generated_file'])].add(element_map[(row['source_mod'],row['element'])]['proposed_owner'])
    classes=[];class_map={}
    for row in read('java_files'):
        owners=file_owners[(row['source_mod'],row['source_file'])]
        if not owners:owners={'panel' if '/client/' in row['source_file'] or '/entity/model/' in row['source_file'] else 'platform'}
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
