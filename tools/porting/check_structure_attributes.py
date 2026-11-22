"""在内存样本中回归结构 Mod 实体属性丢失及改写边界，不生成结构资源。"""
from __future__ import annotations

import io
import struct
from pathlib import Path

from convert_structures import Rewriter, select_structures


def string(value: str) -> bytes:
    encoded = value.encode('utf-8')
    return struct.pack('>H', len(encoded)) + encoded


def payload(kind: int, value) -> bytes:
    if kind in (3, 6):
        return struct.pack({3: '>i', 6: '>d'}[kind], value)
    if kind == 8:
        return string(value)
    if kind == 11:
        return struct.pack('>i', len(value)) + b''.join(struct.pack('>i', v) for v in value)
    if kind == 9:
        element, values = value
        return bytes((element,)) + struct.pack('>i', len(values)) + b''.join(payload(element, v) for v in values)
    if kind == 10:
        return b''.join(bytes((k,)) + string(n) + payload(k, v) for k, n, v in value) + b'\0'
    raise ValueError(kind)


def decode(data: bytes) -> dict:
    stream = io.BytesIO(data)

    def number(format: str):
        return struct.unpack(format, stream.read(struct.calcsize(format)))[0]

    def text():
        return stream.read(number('>H')).decode('utf-8')

    def read(kind):
        if kind in (3, 6):
            return number({3: '>i', 6: '>d'}[kind])
        if kind == 8:
            return text()
        if kind == 11:
            return [number('>i') for _ in range(number('>i'))]
        if kind == 9:
            element, count = number('>B'), number('>i')
            return [read(element) for _ in range(count)]
        if kind == 10:
            result = {}
            while (child := number('>B')) != 0:
                name = text()
                result[name] = read(child)
            return result
        raise ValueError(kind)

    assert number('>B') == 10
    text()
    result = read(10)
    assert stream.read() == b''
    return result


def attribute(identity, base, modifiers=None):
    fields = [(6, 'Base', base), (8, 'Name', identity)]
    if modifiers is not None:
        fields.insert(1, (9, 'Modifiers', (10, modifiers)))
    return fields


def main():
    # 复现居民结构的保存值覆盖注册默认值：max_health=100、attack_damage=2。
    modifier = [(3, 'Operation', 1), (11, 'UUID', [1, 2, 3, 4]),
                (6, 'Amount', 0.125), (8, 'Name', 'Random spawn bonus')]
    attributes = (10, [attribute('minecraft:generic.max_health', 100),
                       attribute('minecraft:generic.attack_damage', 2),
                       attribute('minecraft:generic.follow_range', 16, [modifier]),
                       attribute('forge:entity_gravity', 0.08),
                       attribute('forge:swim_speed', 1),
                       attribute('forge:nametag_distance', 64),
                       attribute('forge:step_height_addition', 0),
                       attribute('yvanchuxiuzhen:entitydaxiao', 1)])
    business = [(9, 'Attributes', attributes), (8, 'id', 'yvanchuxiuzhen:businessman_21'),
                (10, 'ForgeData', [(9, 'Attributes', attributes)])]
    passenger = [(8, 'id', 'yvanchuxitong:shitiyizi'), (9, 'Attributes', attributes)]
    vanilla = [(9, 'Attributes', attributes), (8, 'id', 'minecraft:pig'),
               (9, 'Passengers', (10, [passenger]))]
    block = [(10, 'nbt', [(9, 'Attributes', attributes), (8, 'id', 'yvanchuxiuzhen:businessman_21')])]
    inheritance = [(10, 'nbt', [(10, 'ForgeData', [(8, 'weizhi', '风来神王')]),
                               (8, 'id', 'yvanchuxiuzhen:chuancheng')])]
    root = [(3, 'DataVersion', 2975), (9, 'blocks', (10, [block, inheritance])),
            (9, 'entities', (10, [[(10, 'nbt', business)], [(10, 'nbt', vanilla)]])),
            (9, 'Attributes', attributes)]
    original = bytes((10,)) + string('') + payload(10, root)
    reader = Rewriter(original)
    output = reader.run()
    before, after = decode(original), decode(output)
    changed = after['entities'][0]['nbt']
    assert 'Attributes' not in changed
    actual = {a['id']: a for a in changed['attributes']}
    assert actual['minecraft:generic.max_health'] == {'base': 100, 'id': 'minecraft:generic.max_health'}
    assert actual['minecraft:generic.attack_damage']['base'] == 2
    assert actual['minecraft:generic.follow_range']['modifiers'] == [
        {'operation': 'add_multiplied_base', 'amount': 0.125, 'id': 'minecraft:random_spawn_bonus'}]
    assert actual['minecraft:generic.gravity']['base'] == 0.08
    assert actual['neoforge:swim_speed']['base'] == 1
    assert actual['neoforge:nametag_distance']['base'] == 64
    assert actual['yvanchuxiuzhen:entitydaxiao']['base'] == 1
    assert actual['forge:step_height_addition']['base'] == 0
    assert 'minecraft:generic.step_height' not in actual
    assert changed['NeoForgeData']['Attributes'] == before['entities'][0]['nbt']['ForgeData']['Attributes']
    assert after['Attributes'] == before['Attributes']
    assert after['blocks'][0] == before['blocks'][0]
    assert after['blocks'][1]['nbt']['NeoForgeData']['weizhi'] == 1
    pig = after['entities'][1]['nbt']
    assert pig['Attributes'] == before['entities'][1]['nbt']['Attributes']
    assert pig['Passengers'][0]['id'] == 'yvanchuxiuzhen:shitiyizi'
    assert pig['Passengers'][0]['attributes'] == changed['attributes']
    assert (reader.attribute_entities, reader.attribute_entries, reader.attribute_modifiers) == (2, 16, 2)
    assert reader.attribute_id_mappings == 6
    assert reader.unmapped_attribute_ids == {'forge:step_height_addition': 2}
    assert Rewriter(output).run() == output
    files = [('source', Path('.'), Path('one.nbt')), ('source', Path('.'), Path('two.nbt'))]
    assert select_structures(files, ['two.nbt']) == [files[1]]
    assert select_structures(files, ['one', 'two']) == files
    try:
        select_structures(files, ['missing'])
    except ValueError as error:
        assert 'missing' in str(error)
    else:
        raise AssertionError('未知模板必须在写入前报错')
    print('structure attribute regression: PASS (in-memory only; no resource writes)')


if __name__ == '__main__':
    main()
