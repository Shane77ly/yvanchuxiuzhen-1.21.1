"""记录 1.18.2 MCreator 元素归属、生成文件和连接/游离过程节点。"""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
import csv
import json
from pathlib import Path
import xml.etree.ElementTree as ET

from inventory_legacy import SOURCES
from investigate_legacy import csv_text

ROOT=Path(__file__).resolve().parents[2]
DEST=ROOT/'docs/reference/legacy/inventory'


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-workspace',type=Path,default=ROOT.parent/'yuanchuxiuzhen-1.21.1')
    args=parser.parse_args()
    rows=defaultdict(list)
    java_calls=defaultdict(set)
    for row in csv.DictReader((DEST/'method_calls.csv').open(encoding='utf-8')):
        java_calls[(row['source_mod'],row['class'])].add((row['target_mod'],row['target_class']))
    for mod,source in SOURCES.items():
        root=args.source_workspace/source
        data=json.loads((root/(mod+'.mcreator')).read_text(encoding='utf-8-sig'))
        for el in data['mod_elements']:
            name=el['name']; path=root/'elements'/(name+'.mod.json')
            definition=json.loads(path.read_text(encoding='utf-8-sig'))['definition'] if path.exists() else {}
            base={'source_mod':mod,'element':name,'type':el['type'],'folder':el.get('path',''),
                  'registry_name':el.get('registry_name',''),'locked_code':el.get('locked_code',False),
                  'definition_file':path.relative_to(root).as_posix() if path.exists() else ''}
            rows['source_elements'].append({**base,'generated_files':'; '.join(el.get('metadata',{}).get('files',[]))})
            for file in el.get('metadata',{}).get('files',[]):
                rows['element_files'].append({**base,'generated_file':file,'file_exists':(root/file).is_file()})
            if el['type']=='procedure' and 'procedurexml' in definition:
                xml=ET.fromstring(definition['procedurexml'])
                connected,detached=set(),set()
                for block in xml:
                    if block.tag.rsplit('}',1)[-1]!='block':continue
                    active=block.get('type')=='event_trigger'
                    for node in block.iter():
                        if node.tag.rsplit('}',1)[-1]=='field' and node.get('name')=='procedure':
                            target=node.text or ''
                            (connected if active else detached).add(target)
                            rows['blockly_procedure_calls'].append({**base,'root_type':block.get('type'),
                                'connected_to_trigger':active,'target_procedure':target})
                generated={cls.removesuffix('Procedure') for m,cls in java_calls[(mod,name+'Procedure')] if m==mod}
                rows['procedure_design_comparison'].append({**base,'connected_calls':'; '.join(sorted(connected)),
                    'detached_calls':'; '.join(sorted(detached)),'java_calls':'; '.join(sorted(generated)),
                    'connected_only':'; '.join(sorted(connected-generated)),
                    'java_only':'; '.join(sorted(generated-connected)),
                    'evidence':'连接状态按顶层 event_trigger 子树；名称不同需人工对应，锁定代码可与图不同'})
            # 只记录真实定义字段，不据编辑器标签推定运行生效。
            for key,value in definition.items():
                if key in ('procedurexml','code','aiBase'):continue
                if isinstance(value,dict) and value.get('name'):
                    rows['element_triggers'].append({**base,'property':key,'value':json.dumps(value,ensure_ascii=False)})
            if el['type'] in ('block','animatedblock','livingentity','animatedentity','gui','dimension','biome','structure','feature','keybind','overlay'):
                selected={k:v for k,v in definition.items() if k not in ('procedurexml','code') and
                          any(token in k.lower() for token in ('inventory','slot','tick','gui','spawn','ai','dimension','trigger','procedure','key','shape','model','texture'))}
                rows['element_architecture'].append({**base,'definition':json.dumps(selected,ensure_ascii=False)})
    for name,items in rows.items():
        (DEST/(name+'.csv')).write_text(csv_text(items),encoding='utf-8',newline='\n')
    summary={'counts':{name:len(items) for name,items in rows.items()},
             'types':dict(Counter(r['type'] for r in rows['source_elements'])),
             'locked_elements':sum(r['locked_code'] for r in rows['source_elements']),
             'procedures_with_detached_calls':sum(bool(r['detached_calls']) for r in rows['procedure_design_comparison']),
             'comparison_differences':sum(bool(r['connected_only'] or r['java_only']) for r in rows['procedure_design_comparison'])}
    (DEST/'source_design_summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(summary,ensure_ascii=False))


if __name__=='__main__':main()
