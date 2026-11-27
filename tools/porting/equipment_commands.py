"""成品指令在离线迁移时使用 1.21.1 已注册的物品名。"""
import re


def equipment_command(command: str) -> str:
    return re.sub(r'^(lianqi yvanchuxiuzhen:armor_0[1-8])_leggins(?= )', r'\1_leggings', command)
