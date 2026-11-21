"""Offline reader for one recorded FULL chunk and its local single-player owner."""
import argparse
from collections import Counter
import hashlib
from io import BytesIO
import json
from pathlib import Path
import uuid
import zlib

import nbtlib

HERE = Path(__file__).resolve().parent
SOURCE = json.loads((HERE / 'saved-sample.json').read_text(encoding='utf-8-sig'))
DIM = 'yvanchuxiuzhen:dimensionrenjian'
NS = 'yvanchuxiuzhen:'
LOG, LEAF, PLANK, BOX = (NS + s for s in ('camphorlog', 'mulberryleaves', 'camphorplanks', 'canhe'))
TABLE = 'minecraft:crafting_table'
BENCH_POS, BOX_POS = (4298, 66, 4413), (4297, 66, 4413)
START = [4298.5, 66., 4414.5]
LOGS = [(4299, y, 4414) for y in (66, 67, 68)]
LEAVES = SOURCE['blocks']['mulberryleaves']['positions']
NAME = 'Renjian - mulberry survival 268 275'
ORIGINAL_OWNER = '380df991-f603-344c-a090-369bad2a924a'


def require(condition, message):
    if not condition:
        raise ValueError(message)


def chunk(save):
    with (save / 'dimensions/yvanchuxiuzhen/dimensionrenjian/region/r.8.8.mca').open('rb') as f:
        f.seek(4 * (268 % 32 + 32 * (275 % 32)))
        offset = int.from_bytes(f.read(3), 'big')
        require(offset > 0, 'Recorded chunk absent')
        f.seek(offset * 4096)
        size = int.from_bytes(f.read(4), 'big')
        require(f.read(1) == b'\x02', 'Expected a zlib region')
        data = zlib.decompress(f.read(size - 1))
    root = nbtlib.File.parse(BytesIO(data))
    require((root['xPos'], root['zPos'], root['Status']) == (268, 275, 'minecraft:full'), 'Wrong FULL chunk')
    return root, hashlib.sha256(data).hexdigest()


def block(root, pos):
    x, y, z = pos
    require((x // 16, z // 16) == (268, 275), 'Outside sample')
    states = next(s['block_states'] for s in root['sections'] if s['Y'] == y // 16)
    palette = states['palette']
    i = (y % 16) * 256 + (z % 16) * 16 + x % 16
    bits = max(4, (len(palette) - 1).bit_length())
    q, r = divmod(i, 64 // bits)
    idx = (int(states['data'][q]) >> (bits * r)) & ((1 << bits) - 1) if len(palette) > 1 else 0
    return str(palette[idx]['Name'])


def inventory(player):
    items = Counter()
    for entry in player['Inventory']:
        items[str(entry['id'])] += int(entry.get('count', 1))
    return items


def inspect(save, ready):
    world = nbtlib.load(save / 'level.dat')['Data']
    root, digest = chunk(save)
    require(world['WorldGenSettings']['seed'] == -5586163949850800998 and world['LevelName'] == NAME,
            'Wrong sample world')
    require(world['DataVersion'] == 3955 and not world['allowCommands'] and world['GameType'] == 0,
            'Expected 1.21.1 survival with commands disabled')
    player = world['Player']
    owner = str(uuid.UUID(bytes=b''.join((int(v) & 0xffffffff).to_bytes(4, 'big') for v in player['UUID'])))
    disk = nbtlib.load(save / 'playerdata' / (owner + '.dat'))
    for p in (player, disk):
        require(p['Dimension'] == DIM and p['playerGameType'] == 0 and not p['Invulnerable'] and
                not any(p['abilities'][k] for k in ('invulnerable', 'mayfly', 'flying', 'instabuild')),
                'Local player is not in Renjian survival')
    require(inventory(player) == inventory(disk) and list(player['Pos']) == list(disk['Pos']),
            'Local and per-player saves disagree')
    stats = json.loads((save / 'stats' / (owner + '.json')).read_text(encoding='utf-8'))['stats']

    def stat(group, key):
        return stats.get('minecraft:' + group, {}).get(key, 0)

    leave = stat('custom', 'minecraft:leave_game')
    ticks = stat('custom', 'minecraft:play_time')
    inv = inventory(player)
    if ready:
        require(digest == SOURCE['chunk_nbt_sha256'], 'Sample chunk changed')
        require(owner == ORIGINAL_OWNER and list(player['Pos']) == START and not inv and leave == 1 and ticks == 112,
                'Not the untouched prepared host')
        require(not any(stats.get('minecraft:' + g) for g in ('mined', 'picked_up', 'crafted', 'used')),
                'Unexpected initial action stats')
        require(all(block(root, pos) == LOG for pos in LOGS) and all(block(root, pos) == LEAF for pos in LEAVES),
                'Missing source blocks')
        require([block(root, (4298, y, 4414)) for y in (65, 66, 67)] ==
                ['minecraft:grass_block', 'minecraft:air', 'minecraft:air'], 'Unsafe start')
        for x, y, z in (BENCH_POS, BOX_POS):
            require(block(root, (x, y, z)) == 'minecraft:short_grass' and
                    block(root, (x, y - 1, z)) == 'minecraft:grass_block', 'Occupied work area')
        return {'stage': 'ready', 'chunk_sha256': digest, 'player': owner, 'mode': 'survival',
                'inventory': dict(inv), 'start': START, 'recorded_leaves': len(LEAVES), 'leave_game': leave}

    mined = stat('mined', LEAF)
    missing = [pos for pos in LEAVES if block(root, pos) != LEAF]
    counts = {g: {k: stat(g, k) for k in keys} for g, keys in
              [('mined', (LOG, LEAF)), ('picked_up', (LOG, LEAF)), ('crafted', (PLANK, TABLE, BOX)), ('used', (TABLE, BOX))]}
    require(stat('mined', LOG) == stat('picked_up', LOG) == 3 and all(block(root, p) == 'minecraft:air' for p in LOGS),
            'Three original logs not yet mined and picked up')
    require(1 <= mined <= len(missing) and all(pos in missing for pos in LEAVES[:mined]) and
            stat('picked_up', LEAF) == 1, 'No matching ordered leaf harvest and pickup')
    require([stat('crafted', k) for k in (PLANK, TABLE, BOX)] == [12, 1, 1], 'Crafting totals differ from 12/1/1')
    require(all(stat('used', k) == 1 for k in (TABLE, BOX)), 'Missing single normal placement for table/box')
    require(inv[PLANK] == 3 and not any(inv[k] for k in (LOG, LEAF, TABLE, BOX)), 'Wrong unspent materials')
    require((block(root, BENCH_POS), block(root, BOX_POS)) == (TABLE, BOX), 'Workstations not in recorded positions')
    require(any(b['id'] == BOX and tuple(int(b[k]) for k in 'xyz') == BOX_POS for b in root['block_entities']),
            'Silkworm box block entity absent')
    require(leave > (1 if owner == ORIGINAL_OWNER else 0), 'No new clean exit recorded')
    return {'save': str(save.resolve()), 'player': owner, 'counts': counts, 'missing_leaves': missing,
            'inventory': dict(inv), 'leave_game': leave, 'play_time': ticks, 'world_time': int(world['Time'])}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('stage', choices=['ready', 'checkpoint', 'reentry'])
    parser.add_argument('save', type=Path)
    parser.add_argument('record', nargs='?', type=Path, help='External checkpoint JSON (required after play)')
    args = parser.parse_args()
    if args.stage != 'ready' and (not args.record or args.record.resolve().is_relative_to(args.save.resolve())):
        parser.error('Provide a checkpoint path outside the save')
    result = inspect(args.save, args.stage == 'ready')
    if args.stage == 'checkpoint':
        with args.record.open('x', encoding='utf-8') as out:
            json.dump(result, out, indent=2)
    if args.stage == 'reentry':
        before = json.loads(args.record.read_text(encoding='utf-8'))
        for k in ('save', 'player', 'counts', 'inventory'):
            require(before[k] == result[k], 'Changed since checkpoint: ' + k)
        require(all(pos in result['missing_leaves'] for pos in before['missing_leaves']), 'Missing leaves reappeared')
        require(result['leave_game'] == before['leave_game'] + 1 and result['play_time'] > before['play_time'] and
                result['world_time'] > before['world_time'], 'No distinct second session/exit on the checkpoint save')
    print(json.dumps(result, ensure_ascii=False, indent=2))
    print('PASS', args.stage)


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, KeyError) as error:
        raise SystemExit('NOT VERIFIED: ' + str(error))
