"""从 1.18.2 来源提取藏宝技能书与研习附赠技能（C skill）。

- 藏宝技能书：`Jinengget01`（随机数 1–10）与 `Jinengget02`（11–13）按随机数 `suijinum` 选一个分支，分支给出一本技能书：
  承载物品、名字、品阶、所需境界、灵根要求、简介、所需武器、攻击倍率、伤害倍率、消耗系数与冷却键。
  输出 src/main/resources/content/skill/treasure_books.json，一行一本，roll 为分支的随机数值；
  随机数由 `Jinenglistzong` 在 1–13 取，因此 13 个分支都能取到。
- 研习附赠技能：`BookRightClick` 在研习“行尸御火”“真言吒雷诀”后以权限 4 执行 `/jineng` 命令，参数原样保存在
  src/main/resources/content/skill/studied_bonuses.json，运行时经与命令相同的解析生成技能书。

工具可重复运行，输出只取决于来源。
"""
from __future__ import annotations

import json
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

PROCEDURES = SOURCES['yvanchuxiuzhen'][0] / 'src/main/java/net/mcreator/yvanchuxiuzhen/procedures'
OUTPUT = ROOT / 'src/main/resources/content/skill'
STRING_FIELDS = {'jinengName': 'name', 'belongTobody': 'body', 'belongTojianjie': 'intro', 'jinengneeditem': 'needed'}
NUMBER_FIELDS = {'belongTopinjie': 'grade', 'belongToLevel': 'level'}
NUMBER_KEYS = ('jinengmanaatt', 'jinengshanghai', 'jinengxiaohao', 'jinengjieyin')
COOLDOWN_SUFFIXES = ('colddown', 'coldignore')


def unescape(text: str) -> str:
    return re.sub(r'\\u([0-9A-Fa-f]{4})', lambda m: chr(int(m.group(1), 16)), text)


def number(text: str) -> float | int:
    value = float(text.replace('(', '').replace(')', ''))
    return int(value) if value.is_integer() else value


def parse_branch(roll: int, text: str) -> dict:
    carrier = re.search(r'new ItemStack\(YvanchuxiuzhenModItems\.(\w+)\.get\(\)\)', text).group(1).lower()
    strings = {key: unescape(value) for key, value in re.findall(r'putString\("(\w+)",\s*"((?:[^"\\]|\\.)*)"\)', text)}
    numbers = {key: number(value) for key, value in re.findall(r'putDouble\("(\w+)",\s*(\(?-?[\d.]+\)?)\)', text)}
    hover = unescape(re.search(r'setHoverName\(new TextComponent\("((?:[^"\\]|\\.)*)"\)\)', text).group(1))
    if hover != strings['jinengName']:
        raise ValueError(f'第 {roll} 支：显示名 {hover} 与技能名 {strings["jinengName"]} 不同')
    row: dict = {'roll': roll, 'item': f'{NAMESPACE}:{carrier}'}
    for source, target in STRING_FIELDS.items():
        row[target] = strings[source]
    for source, target in NUMBER_FIELDS.items():
        row[target] = numbers[source]
    row['numbers'] = {key: value for key, value in numbers.items()
                      if key in NUMBER_KEYS or key.lower().endswith(COOLDOWN_SUFFIXES)}
    return row


def main() -> None:
    rows: list[dict] = []
    for name in ('Jinengget01', 'Jinengget02'):
        text = (PROCEDURES / f'{name}Procedure.java').read_text(encoding='utf-8')
        parts = re.split(r'suijinum"\) == (\d+)\) \{', text)
        for index in range(1, len(parts), 2):
            rows.append(parse_branch(int(parts[index]), parts[index + 1]))
    rows.sort(key=lambda row: row['roll'])
    book = (PROCEDURES / 'BookRightClickProcedure.java').read_text(encoding='utf-8')
    bonuses = []
    for technique, arguments in re.findall(r'getString\("ExercisesName"\)\)\.equals\("([^"]+)"\)\) \{.*?"jineng ([^"]+)"', unescape(book), re.S):
        bonuses.append({'technique': technique, 'arguments': arguments})
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for file, data in (('treasure_books.json', rows), ('studied_bonuses.json', bonuses)):
        text = '[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in data) + '\n]\n'
        (OUTPUT / file).write_text(text, encoding='utf-8', newline='\n')
    print('treasure_books', len(rows), 'studied_bonuses', len(bonuses))


if __name__ == '__main__':
    main()
