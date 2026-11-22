"""按锁定的 1.18.2 原初系统源码生成完整宝箱奖励表。

只改写纯奖励过程的版本接口：传入等级和随机源、合并命名空间。
原有 if/else 顺序、随机上下限、空结果和字段拼写均保留，不将不可达分支补入随机池。
输出 Java 表由运行层解释来源奖励串；不运行资源转换或 Gradle。
"""
from __future__ import annotations

import hashlib
import json
import re

from build_ledger import ROOT
from convert_content import SOURCES

SOURCE = SOURCES['yvanchuxitong'][0] / 'src/main/java/net/mcreator/yvanchuxitong/procedures'
OUTPUT = ROOT / 'src/main/java/net/yvanchuxiuzhen/system/treasure/TreasureRewardTables.java'
EVIDENCE = ROOT / 'docs/reference/conversion/treasure_reward_tables.json'
CATEGORIES = {
    '书架': {'书籍堆': '0101', '书桶': '0102', '符箓堆': 'Fulu', '丹方': 'Liandantuzhi', '功法书': 'Gongfa', '技能书': 'Jineng'},
    '药架': {'草药堆': 'Caoyao', '丹药堆': 'Danyao', '恢复类丹药堆': 'Huifudanyao', '状态类丹药堆': 'Zhuangtaidanyao', '精进修为丹药堆': 'Xiuweidanyao'},
    '武器架': {'武器架': 'Wuqi', '法宝架': 'Fabao', '盔甲架': 'Hujia', '饰品箱': 'Shipin', '装备': 'Zhuangbei'},
    '宝箱': {'妖兽材料': 'Yaoshoucailiao', '异火': 'Yihuocailiao', '金属材料': 'Jinshucailiao', '灵石': 'Lingshi', '宝石': 'Baoshicailiao',
           '妖丹': 'Yaodancailiao', '布匹': 'Bupi', '综合': 'Cailiao'},
}
LITERAL = re.compile(r'"(?:[^"\\]|\\.)*"')


def method(name: str) -> str:
    return 'table' + name.removeprefix('Baoxiang').removesuffix('Procedure')


def translate(name: str, text: str) -> str:
    match = re.search(r'public static (String|double) execute\([^)]*\) \{', text)
    if match is None:
        raise ValueError(f'{name}: 不是纯奖励过程')
    body = text[match.end():text.rfind('}')].rstrip()
    body = body[:body.rfind('}')]
    literals: list[str] = []

    def keep(found: re.Match[str]) -> str:
        literal = found.group().replace('yvanchuxitong:', 'yvanchuxiuzhen:')
        literal = re.sub(r'\\u([0-9A-Fa-f]{4})', lambda value: chr(int(value.group(1), 16)), literal)
        literals.append(literal)
        return f'__TEXT_{len(literals) - 1}__'

    body = LITERAL.sub(keep, body)
    body = body.replace('YvanchuxitongModVariables.MapVariables.get(world).baoxianglevellinshi', 'level')
    body = body.replace('Mth.nextInt(new Random(),', 'Mth.nextInt(random,')
    body = re.sub(r'(Baoxiang\w+Procedure)\.execute\((?:world)?\)', lambda call: method(call.group(1)) + '(level, random)', body)
    body = body.replace('SetcailiaopinjieProcedure.execute()', 'materialQuality(level, random)')
    body = body.replace('new java.text.DecimalFormat(', 'new DecimalFormat(')
    if 'Procedure.' in body or 'world' in body or 'Variables' in body or 'new Random' in body:
        raise ValueError(f'{name}: 存在未适配的源码接口')
    body = re.sub(r'__TEXT_(\d+)__', lambda found: literals[int(found.group(1))], body)
    body = '\n'.join(line.replace('\t', '    ').rstrip() for line in body.splitlines())
    return f'    /** 来源 B:{name}，保留分支及随机边界。 */\n    private static {match.group(1)} {method(name) if name.startswith("Baoxiang") else "materialQuality"}(double level, RandomSource random) {{{body}\n    }}\n'


def main() -> None:
    names = {f'Baoxiang{suffix}Procedure' for categories in CATEGORIES.values() for suffix in categories.values()}
    pending = list(names)
    texts: dict[str, str] = {}
    while pending:
        name = pending.pop()
        if name in texts:
            continue
        text = (SOURCE / f'{name}.java').read_text(encoding='utf-8')
        texts[name] = text
        calls = re.findall(r'(Baoxiang\w+Procedure|SetcailiaopinjieProcedure)\.execute\(', text)
        pending.extend(call for call in calls if call not in texts)
    selection = []
    for category, subtypes in CATEGORIES.items():
        cases = '\n'.join(f'                case "{subtype}" -> {method("Baoxiang" + suffix + "Procedure")}(level, random);' for subtype, suffix in subtypes.items())
        selection.append(f'            case "{category}" -> switch (subtype) {{\n{cases}\n                default -> "";\n            }};')
    header = '''package net.yvanchuxiuzhen.system.treasure;

import java.text.DecimalFormat;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** 1.18.2 版本的纯奖励表，由 tools/porting/extract_treasure_rewards.py 确定性生成。 */
final class TreasureRewardTables {
    private TreasureRewardTables() {
    }

    static String select(String category, String subtype, double level, RandomSource random) {
        return switch (category) {
'''
    body = header + '\n'.join(selection) + '\n            default -> "";\n        };\n    }\n\n'
    body += '\n'.join(translate(name, texts[name]) for name in sorted(texts)) + '}\n'
    OUTPUT.write_text(body, encoding='utf-8', newline='\n')
    records = [{'source': f'B:{name}', 'sha256': hashlib.sha256(texts[name].encode('utf-8')).hexdigest()} for name in sorted(texts)]
    EVIDENCE.write_text(json.dumps({'inputs': records, 'output': str(OUTPUT.relative_to(ROOT)).replace('\\', '/'),
                                   'output_sha256': hashlib.sha256(body.encode('utf-8')).hexdigest()}, ensure_ascii=False, indent=2) + '\n',
                        encoding='utf-8', newline='\n')
    print(f'treasure reward tables: {len(texts)} procedures, {sum(len(value) for value in CATEGORIES.values())} dispatch entries')


if __name__ == '__main__':
    main()
