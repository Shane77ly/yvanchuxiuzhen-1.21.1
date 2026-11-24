"""从锁定1.18.2版本提取经济价表、菜单几何与语言；只写经济输入文件。"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT.parent / 'yuanchuxiuzhen-1.21.1/reference/1.18.2yvanchuxitong'
if not SOURCE.exists():
    SOURCE = Path('D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1/reference/1.18.2yvanchuxitong')
JAVA = SOURCE / 'src/main/java/net/mcreator/yvanchuxitong'
OUT = ROOT / 'src/main/resources/economy'


def strings(text: str) -> list[str]:
    return [json.loads(value) for value in re.findall(r'"(?:[^"\\]|\\.)*"', text)]


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    bank_source = (JAVA / 'procedures/PlayerRightClickBlockProcedure.java').read_text(encoding='utf-8')
    bank_strings = strings(bank_source)
    basic_source = (JAVA / 'procedures/PutongshougoushuaxinProcedure.java').read_text(encoding='utf-8')
    catalog = {
        'bank_dialogue': next(value for value in bank_strings if value.startswith('打开通宝商城系统/')),
        'bank_products': json.loads(re.search(r'putString\("productlist",\s*("(?:[^"\\]|\\.)*")', bank_source)[1]),
        'bank_information': json.loads(re.search(r'putString\("shopinformation",\s*("(?:[^"\\]|\\.)*")', bank_source)[1]),
        'basic_prices': ''.join(value for value in strings(basic_source) if value.startswith(('minecraft:stone/', '/yvanchuxiuzhen:'))),
    }
    (OUT / 'commerce_catalog.json').write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    prices = {}
    for name in ('Gongfajiage', 'Lianqishoumaijizhi', 'Cailiaochushou'):
        text = (JAVA / f'procedures/{name}Procedure.java').read_text(encoding='utf-8')
        prices[name] = {level: float(value) for level, value in re.findall(r'(?:dengjilinshi|item_level_num) == (\d+)\) \{\s*jizhi = ([\d.]+);', text)}
    for name in ('Caoyaojiage', 'Shougouselljisuan'):
        text = (JAVA / f'procedures/{name}Procedure.java').read_text(encoding='utf-8')
        values = [value for value in strings(text) if value.startswith('yvanchuxiuzhen:') and ('%' in value or '/' in value)]
        prices['herbs' if name == 'Caoyaojiage' else 'talismans'] = values[0]
    material_shop = (JAVA / 'procedures/Cailiaoshop01Procedure.java').read_text(encoding='utf-8')
    prices['material_shop'] = {level: float(value) for level, value in re.findall(r'cailiaolevel == (\d+)\) \{\s*jiagejisuan = ([\d.]+);', material_shop)}
    accessory_values = (JAVA / 'procedures/ShougouselljisuanProcedure.java').read_text(encoding='utf-8')
    prices['accessories'] = {level: float(value) for level, value in re.findall(r'dengjilinshi == (\d+)\) \{\s*danjia = ([\d.]+);', accessory_values)}
    text = (JAVA / 'procedures/DanyaojiageProcedure.java').read_text(encoding='utf-8')
    prices['medicine_effects'] = {json.loads(effect): good == 'true' for effect, good in re.findall(r'\(danyaoxiaoguo\)\.equals\(("[^"\n]+")\)\) \{\s*jiajian = (true|false);', text)}
    (OUT / 'valuation.json').write_text(json.dumps(prices, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

    screens = {}
    assets = {}
    pages = ('ShopGUI', 'BankGUIshougou', 'BankGUIshougou2', 'ShopblockGui', 'ShopblockGuiset', 'Paimaifangkuaigui', 'Paimaihang')
    for page in pages:
        text = (JAVA / f'client/gui/{page}Screen.java').read_text(encoding='utf-8')
        layers = []
        field_textures = dict(re.findall(r'private static final ResourceLocation (\w+)\s*=\s*new ResourceLocation\("yvanchuxitong:textures/screens/([^"\n]+)"\);', text))
        for field_name, x, y, width, height, tw, th in re.findall(r'RenderSystem\.setShaderTexture\(0,\s*(\w+)\);\s*this\.blit\(ms,\s*([^,]+),\s*([^,]+),\s*0,\s*0,\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\);', text):
            if field_name not in field_textures:
                continue
            name = field_textures[field_name]
            dim_w = int(re.search(r'this\.imageWidth = (\d+);', text)[1])
            dim_h = int(re.search(r'this\.imageHeight = (\d+);', text)[1])
            lx = 0 if 'leftPos +' not in x else int(x.split('+')[1].strip())
            ly = 0 if 'topPos +' not in y else int(y.split('+')[1].strip())
            lw = dim_w if 'imageWidth' in width else int(width.strip())
            lh = dim_h if 'imageHeight' in height else int(height.strip())
            ltw = dim_w if 'imageWidth' in tw else int(tw.strip())
            lth = dim_h if 'imageHeight' in th else int(th.strip())
            layers.append({'texture': name, 'x': lx, 'y': ly, 'width': lw, 'height': lh, 'texture_width': ltw, 'texture_height': lth})
            assets[name] = name
        for name, x, y, width, height, tw, th in re.findall(r'new ResourceLocation\("yvanchuxitong:textures/screens/([^"\n]+)"\)\);\s*this.blit\(ms, this.leftPos \+ (-?\d+), this.topPos \+ (-?\d+), 0, 0, (\d+), (\d+), (\d+), (\d+)\);', text):
            layers.append({'texture': name, 'x': int(x), 'y': int(y), 'width': int(width), 'height': int(height), 'texture_width': int(tw), 'texture_height': int(th)})
            assets[name] = name
        buttons = []
        for match in re.finditer(r'new ImageButton\(this.leftPos \+ (-?\d+), this.topPos \+ (-?\d+), (\d+), (\d+), 0, 0, \d+, new ResourceLocation\("yvanchuxitong:textures/screens/([^"\n]+)"\), (\d+), (\d+), e -> \{([\s\S]*?)\}\)', text):
            x, y, width, height, name, tw, th, body = match.groups()
            action = re.search(r'new \w+ButtonMessage\((\d+),', body)
            buttons.append({'texture': name, 'x': int(x), 'y': int(y), 'width': int(width), 'height': int(height), 'texture_width': int(tw), 'texture_height': int(th), 'operation': int(action[1]) if action else -1})
            assets[name] = name
        plain_buttons = []
        for match in re.finditer(r'new Button\(this.leftPos \+ (-?\d+), this.topPos \+ (-?\d+), (\d+), (\d+), new TranslatableComponent\("([^"]+)"\), e -> \{([\s\S]*?)\}\)', text):
            x, y, width, height, key, body = match.groups()
            action = re.search(r'new \w+ButtonMessage\((\d+),', body)
            plain_buttons.append({'key': key.replace('yvanchuxitong', 'yvanchuxiuzhen'), 'x': int(x), 'y': int(y), 'width': int(width), 'height': int(height), 'operation': int(action[1]) if action else -1})
        labels = []
        for match in re.finditer(r'this.font.draw\((?:ms|poseStack),\s*([^;]*?), (-?\d+), (-?\d+), (-?\d+)\);', text):
            value, x, y, color = match.groups()
            # 每行表达式仅取最近一条draw调用，不跨越绘制语句。
            if ';' in value:
                continue
            procedure = re.search(r'(\w+)Procedure.execute', value)
            literal = re.search(r'new TranslatableComponent\("([^"]+)"\)', value)
            if procedure or literal:
                labels.append({'procedure': procedure[1] if procedure else '', 'key': literal[1].replace('yvanchuxitong', 'yvanchuxiuzhen') if literal else '', 'x': int(x), 'y': int(y), 'color': int(color)})
        menu = (JAVA / f'world/inventory/{page}Menu.java').read_text(encoding='utf-8')
        slots = [[int(i), int(x), int(y)] for i, x, y in re.findall(r'customSlots.put\((\d+), this.addSlot\(new SlotItemHandler\(internal, \d+, (-?\d+), (-?\d+)\)', menu)]
        player_inventory = re.search(r'new Slot\(inv, sj \+ \(si \+ 1\) \* 9, (\d+) \+ 8 \+ sj \* 18, (\d+) \+ 84 \+ si \* 18\)', menu)
        dimensions = [int(re.search(fr'this.image{axis} = (\d+);', text)[1]) for axis in ('Width', 'Height')]
        screens[page] = {'width': dimensions[0], 'height': dimensions[1], 'layers': layers, 'buttons': buttons, 'plain_buttons': plain_buttons, 'labels': labels, 'slots': slots,
                         'inventory_x': int(player_inventory[1]) + 8, 'inventory_y': int(player_inventory[2]) + 84}
    (OUT / 'screens.json').write_text(json.dumps(screens, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    language = json.loads((SOURCE / 'src/main/resources/assets/yvanchuxitong/lang/zh_cn.json').read_text(encoding='utf-8'))
    keys = {label['key'] for page in screens.values() for label in page['labels'] if label['key']}
    selected = {key.replace('yvanchuxitong', 'yvanchuxiuzhen'): value.replace('yvanchuxitong:', 'yvanchuxiuzhen:') for key, value in language.items() if key.replace('yvanchuxitong', 'yvanchuxiuzhen') in keys or key.startswith(('auction.itemdata.', 'txt.shoplist.', 'txt.shopdescribe.'))}
    selected.update({'gui.yvanchuxiuzhen.commerce.action': '操作', 'gui.yvanchuxiuzhen.commerce.buy': '购买商品',
                     'gui.yvanchuxiuzhen.commerce.refresh': '刷新价格', 'gui.yvanchuxiuzhen.commerce.sell': '出售',
                     'gui.yvanchuxiuzhen.commerce.close': '关闭', 'gui.yvanchuxiuzhen.commerce.explain': '说明',
                     'gui.yvanchuxiuzhen.commerce.next': '下一页', 'gui.yvanchuxiuzhen.commerce.previous': '上一页'})
    selected.update({'menu.yvanchuxiuzhen.' + identity: title for identity, title in {'shop_gui': '通宝商店', 'bank_gu_ishougou': '通宝商盟收购', 'bank_gu_ishougou_2': '基础物品收购', 'shopblock_gui': '交易方块', 'shopblock_guiset': '交易方块设置', 'paimaifangkuaigui': '拍卖行设置', 'paimaihang': '现场拍卖'}.items()})
    selected['gui.yvanchuxiuzhen.paimaihang.jiage'] = language['gui.yvanchuxitong.paimaihang.jiage'] or '在此输入价格'
    (OUT / 'language_zh_cn.json').write_text(json.dumps(selected, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (OUT / 'assets.json').write_text(json.dumps(sorted(assets), ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    descriptions = (JAVA / 'procedures/ShopblockGuisetshuomingProcedure.java').read_text(encoding='utf-8')
    (OUT / 'shop_explanation.json').write_text(json.dumps([json.loads(value) for value in re.findall(r'new TextComponent\(\s*("(?:[^"\\]|\\.)*")', descriptions)], ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    auction_lines = {}
    for name in ('Fangqijingjiaduihua', 'Beitiaoxinhuayv'):
        source_text = (JAVA / f'procedures/{name}Procedure.java').read_text(encoding='utf-8')
        auction_lines[name] = [json.loads(value) for value in re.findall(r'^\s*wenben = ("(?:[^"\\]|\\.)*")', source_text, re.M)]
    (OUT / 'auction_lines.json').write_text(json.dumps(auction_lines, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('economy:', len(screens), 'screens,', len(assets), 'textures,', len(selected), 'language entries')


if __name__ == '__main__':
    main()
