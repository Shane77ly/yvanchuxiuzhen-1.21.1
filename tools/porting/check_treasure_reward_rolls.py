"""编译六个锁定来源过程并指定抽签值，逐签对比目标奖励、权重与顺序。"""

import json
import subprocess
from pathlib import Path
from tempfile import TemporaryDirectory

from extract_treasure_rewards import OUTPUT, SOURCE, parse_reward_string


POOLS = ('Fulu', 'Hujia', 'Jineng', 'Liandantuzhi', 'Yaoshoucailiao', 'Zuowu')
STUBS = {
    'LevelAccessor': 'package net.minecraft.world.level; public interface LevelAccessor {}',
    'YvanchuxitongModVariables': '''package net.mcreator.yvanchuxitong.network;
        public class YvanchuxitongModVariables { public static class MapVariables {
            public static final MapVariables world = new MapVariables(); public double baoxianglevellinshi;
            public static MapVariables get(net.minecraft.world.level.LevelAccessor ignored) { return world; }
        } }''',
    'Mth': '''package net.minecraft.util;
        public class Mth { public static int calls, roll, high;
            public static int nextInt(java.util.Random ignored, int lo, int hi) {
                if (calls++ != 0) return lo;
                if (lo != 1 || roll < lo || roll > hi) throw new AssertionError();
                high = hi; return roll;
            }
        }''',
}


def source_rolls():
    calls = '\n'.join(f'case "{name}" -> Baoxiang{name}Procedure.execute({"" if name == "Zuowu" else "null"});'
                      for name in POOLS)
    runner = '''import net.mcreator.yvanchuxitong.procedures.*;
        import net.mcreator.yvanchuxitong.network.YvanchuxitongModVariables.MapVariables;
        import net.minecraft.util.Mth;
        public class RollAudit { public static void main(String[] names) {
            for (String name : names) for (double level : new double[]{0,1,2,3,4,5,6,7,8,8.5,9}) {
                if (name.equals("Zuowu") && level != 1) continue;
                MapVariables.world.baoxianglevellinshi = level;
                for (Mth.roll = 1; ; Mth.roll++) {
                    Mth.calls = 0; Mth.high = 0;
                    String reward = switch(name) { CALLS default -> throw new AssertionError(); };
                    System.out.println(name + "\\t" + level + "\\t" + Mth.roll + "\\t" + Mth.high + "\\t" + reward);
                    if (Mth.roll >= Mth.high) break;
                }
            }
        }}'''.replace('CALLS', calls)
    with TemporaryDirectory() as tmp:
        files = []
        for name, code in {**STUBS, 'RollAudit': runner}.items():
            path = Path(tmp) / (name + '.java')
            path.write_text(code, encoding='utf-8')
            files.append(path)
        originals = [SOURCE / f'Baoxiang{name}Procedure.java' for name in POOLS]
        subprocess.run(['javac', '-encoding', 'UTF-8', '-d', tmp, *files, *originals,
                        SOURCE / 'SetcailiaopinjieProcedure.java'], check=True)
        return subprocess.check_output(['java', '-Dstdout.encoding=UTF-8', '-cp', tmp, 'RollAudit', *POOLS],
                                       encoding='utf-8').splitlines()


def main():
    pools = {pool['id']: pool for pool in json.loads(OUTPUT.read_text(encoding='utf-8'))}
    counts = dict.fromkeys(POOLS, 0)
    for line in source_rolls():
        name, level, roll, high, raw = line.split('\t', 4)
        level, roll, high = float(level), int(roll), int(high)
        tier = next((t for t in pools[name.lower()]['tiers']
                     if t.get('min_level', -float('inf')) <= level <= t.get('max_level', float('inf'))), None)
        entries = [] if tier is None else [e['reward'] for e in tier['entries'] for _ in range(e['weight'])]
        label = f'{name} 等级 {level} 签 {roll}/{high}'
        if len(entries) != high:
            raise ValueError(f'{label}: 目标共 {len(entries)} 签')
        reward = parse_reward_string(raw.replace('yvanchuxitong:', 'yvanchuxiuzhen:'))
        if name == 'Yaoshoucailiao' and reward['kind'] == 'item':
            reward['kind'] = 'material'
        if entries[roll - 1] != reward:
            raise ValueError(f'{label}: 来源 {reward}，目标 {entries[roll - 1]}')
        if name == 'Fulu' and reward['kind'] == 'fulu':
            ctx = tier['context']
            suffix = f"%fululevel/int/{ctx['fulu_level']}%fululeveltxt/char/{ctx['fulu_tier_text']}"
            if not raw.endswith(suffix):
                raise ValueError(f'{label}: 符箓品阶不一致')
        counts[name] += 1
    print('六池逐签通过：', counts, '共', sum(counts.values()))


if __name__ == '__main__':
    main()
