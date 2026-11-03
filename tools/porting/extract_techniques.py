"""从 1.18.2 来源的 Gongfalist01–08 过程提取功法定义（C gongfa）。

每个过程按 Mth.nextInt(1, N) 的随机数选一个分支，分支给出一本功法书：物品 ID、名字、品阶与文字、所需境界与文字、
灵根要求、简介、五行值与十三项属性档位。输出：
- src/main/resources/content/gongfa/techniques.json：一行一本，list 为过程序号（藏宝等级），roll 为分支的随机数值
- src/main/resources/content/gongfa/technique_lists.json：每个过程的随机数上限 max

随机数取不到的分支与取到却没有分支的数值按原样保留（行的 roll 大于 max，或 1–max 中有缺号），由运行时规则处理。
工具可重复运行，输出只取决于来源。
"""
from __future__ import annotations

import json
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

PROCEDURES = SOURCES['yvanchuxiuzhen'][0] / 'src/main/java/net/mcreator/yvanchuxiuzhen/procedures'
OUTPUT = ROOT / 'src/main/resources/content/gongfa'
STRING_FIELDS = {'ExercisesName': 'name', 'belongTopinjietext': 'grade_text', 'belongToLeveltext': 'level_text',
                 'belongTobody': 'body', 'belongTojianjie': 'intro'}
NUMBER_FIELDS = {'belongTopinjie': 'grade', 'belongToLevel': 'level'}
ELEMENTS = {'ExercisesGold': 'gold', 'ExercisesWater': 'water', 'ExercisesTree': 'tree', 'ExercisesFire': 'fire', 'ExercisesSoil': 'soil'}
TIERS = ['TechniqueHealth', 'TechniqueHeathRec', 'Techniquepower', 'TechniquepowerRec', 'TechniqueSoul', 'TechniqueSoulRec',
         'TechniqueAttack', 'TechniqueAttackDef', 'TechniqueMana', 'TechniqueManaDef', 'TechniqueSouatt', 'TechniqueSouDef', 'TechniqueSpeed']


def unescape(text: str) -> str:
    return re.sub(r'\\u([0-9A-Fa-f]{4})', lambda m: chr(int(m.group(1), 16)), text)


def number(text: str) -> float | int:
    value = float(text)
    return int(value) if value.is_integer() else value


def parse_branch(roll: int, text: str, list_number: int) -> dict:
    item = re.search(r'new ItemStack\(YvanchuxiuzhenModItems\.(\w+)\.get\(\)\)', text).group(1).lower()
    row: dict = {'list': list_number, 'roll': roll, 'item': f'{NAMESPACE}:{item}'}
    strings = {key: unescape(value) for key, value in re.findall(r'putString\("(\w+)",\s*"((?:[^"\\]|\\.)*)"\)', text)}
    numbers = {key: number(value) for key, value in re.findall(r'putDouble\("(\w+)",\s*([\d.]+)\)', text)}
    hover = unescape(re.search(r'setHoverName\(new TextComponent\("((?:[^"\\]|\\.)*)"\)\)', text).group(1))
    if hover != strings['ExercisesName']:
        raise ValueError(f'第 {list_number} 表第 {roll} 支：显示名 {hover} 与功法名 {strings["ExercisesName"]} 不同')
    for source, target in STRING_FIELDS.items():
        row[target] = strings[source]
    for source, target in NUMBER_FIELDS.items():
        row[target] = numbers[source]
    elements = {target: numbers[source] for source, target in ELEMENTS.items() if source in numbers}
    if elements:
        row['elements'] = elements
    row['tiers'] = [strings[key] for key in TIERS]
    return row


def main() -> None:
    rows: list[dict] = []
    lists: list[dict] = []
    for list_number in range(1, 9):
        text = (PROCEDURES / f'Gongfalist0{list_number}Procedure.java').read_text(encoding='utf-8')
        low, high = map(int, re.search(r'Mth\.nextInt\(new Random\(\), (\d+), (\d+)\)', text).groups())
        if low != 1:
            raise ValueError(f'第 {list_number} 表的随机数下限不是 1：{low}')
        lists.append({'list': list_number, 'max': high})
        parts = re.split(r'suijishu == (\d+)\) \{', text)
        for index in range(1, len(parts), 2):
            rows.append(parse_branch(int(parts[index]), parts[index + 1], list_number))
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name, data in (('techniques.json', rows), ('technique_lists.json', lists)):
        text = '[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in data) + '\n]\n'
        (OUTPUT / name).write_text(text, encoding='utf-8', newline='\n')
    print('techniques', len(rows), 'lists', lists)


if __name__ == '__main__':
    main()
