"""提取003–018编号妖兽的来源身份、模型、资产与过程闭包，不代替行为验收。"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from convert_content import ROOT
from convert_yaoshou_019_036 import build


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/yaoshou-003-018-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build(tuple(range(3, 19)))
    report['verification'] = {
        'static_source': '来源身份、注册、模型及过程闭包已提取；各编号实际行为由实现与证据台账登记。',
        'build': '本工具未执行', 'client': '本工具未执行', 'server': '本工具未执行'}
    report.pop('death_contract', None)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    if args.write_resources:
        resources = ROOT / 'src/main/resources'
        for suffix, rows in (('models', report['models']), ('eggs', report['eggs'])):
            path = resources / f'content/creature/yaoshou_003_018_{suffix}.json'
            path.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        for relative, data in report['generated_data'].items():
            path = resources / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        for relative, entry in report['copies'].items():
            path = resources / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(Path(entry['source']).read_bytes())
    print(json.dumps({'entities': len(report['models']), 'assets': len(report['copies']),
                     'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
