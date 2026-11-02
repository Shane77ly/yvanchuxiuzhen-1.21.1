import fs from "node:fs/promises";
import { fileURLToPath } from "node:url";
import path from "node:path";
import { FileBlob, SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const projectRoot = fileURLToPath(new URL("../../", import.meta.url));
const inventoryDir = path.join(projectRoot, "docs/reference/legacy/inventory");
const outputDir = path.join(projectRoot, "outputs/01a19713-78e6-72b2-844d-c8e8d0ce7ded");
const previewDir = path.join(projectRoot, "tmp/porting-workbook-preview");
const outputPath = path.join(outputDir, "1.18.2-content-register.xlsx");
const data = JSON.parse(await fs.readFile(path.join(inventoryDir, "workbook_data.json"), "utf8"));
const architecture = JSON.parse(await fs.readFile(path.join(inventoryDir, "architecture_workbook.json"), "utf8"));
const workbook = Workbook.create();
const reviewIndex = process.argv.indexOf("--review-workbook");
const reviewed = reviewIndex < 0 ? null : await SpreadsheetFile.importXlsx(await FileBlob.load(process.argv[reviewIndex + 1]));

// 人工列以稳定来源身份匹配；更新静态证据时保留已填写的映射和评审意见。
function retainReview(sheetName, rows, keyColumn, reviewColumns) {
  if (!reviewed) return rows;
  const values = reviewed.worksheets.getItem(sheetName).getUsedRange().values;
  const byIdentity = new Map(values.slice(1).map((row) => [row[keyColumn], row]));
  for (const row of rows) {
    const previous = byIdentity.get(row[keyColumn]);
    if (previous) for (const column of reviewColumns) row[column] = previous[column] ?? "";
  }
  return rows;
}

function columnName(index) {
  let result = "";
  for (let n = index + 1; n > 0; n = Math.floor((n - 1) / 26)) result = String.fromCharCode(65 + ((n - 1) % 26)) + result;
  return result;
}

function tableSheet(name, tableName, headers, rows, widths, editable = []) {
  const sheet = workbook.worksheets.add(name);
  sheet.showGridLines = false;
  const all = sheet.getRangeByIndexes(0, 0, rows.length + 1, headers.length);
  all.values = [headers, ...rows];
  all.format.font = { name: "Microsoft YaHei", size: 10, color: "#263445" };
  all.format.rowHeight = 22;
  all.format.verticalAlignment = "center";
  all.format.wrapText = true;
  const last = columnName(headers.length - 1);
  const table = sheet.tables.add(`A1:${last}${rows.length + 1}`, true, tableName);
  table.showFilterButton = true;
  sheet.getRange(`A1:${last}1`).format = { fill: "#244763", font: { name: "Microsoft YaHei", size: 10, bold: true, color: "#FFFFFF" }, rowHeight: 30, horizontalAlignment: "center" };
  widths.forEach((width, i) => sheet.getRangeByIndexes(0, i, rows.length + 1, 1).format.columnWidth = width);
  for (const i of editable) sheet.getRangeByIndexes(1, i, rows.length, 1).format.fill = "#FFF2CC";
  sheet.getRangeByIndexes(1, 0, rows.length, headers.length).format.autofitRows();
  sheet.freezePanes.freezeRows(1);
  sheet.freezePanes.freezeColumns(2);
  return sheet;
}

const overview = workbook.worksheets.add("阅读说明");
overview.showGridLines = false;
overview.getRange("A2").values = [["原初修真 · 1.18.2 编号与变量台账"]];
overview.getRange("A4:B11").values = [
  ["目标", "完整移植到 Minecraft 1.21.1 / NeoForge / Java 21，合为一个 Mod"],
  ["方案状态", "调查与架构草案；未批准，未开始游戏实现"],
  ["原有编号", "数字标记按源码原样保存，如 002；不把行号或声明顺序当作内容编号"],
  ["黄色列", "人工填写：目标名称、目标模块、评审意见；空白表示尚未决定"],
  ["读写证据", "状态变量表保留按名候选；状态生命周期表按来源 Mod 和作用域匹配显式访问"],
  ["初值摘录", "超过 180 字符仅展示开头；完整值见 variables_detailed.csv 和对应源码"],
  ["存档范围", "PlayerVariables：玩家；WorldVariables：维度；MapVariables：主世界数据存储"],
  ["刷新方式", "构建脚本使用 --review-workbook 指定本文件，按来源身份保留人工列"],
];
overview.getRange("A13:D16").values = [
  ["来源 Mod", "Java 文件", "状态字段", "资源文件"],
  ...Object.entries(data.summary.sources).map(([mod, s]) => [mod, s.java_files, s.states, s.resources]),
  ["合计", data.summary.java_count, data.summary.state_count, Object.values(data.summary.sources).reduce((sum, s) => sum + s.resources, 0)],
];
overview.getRange("A18:B21").values = [["注册声明", data.content.length], ["Procedure", data.procedures.length], ["菜单类", data.menus.length], ["独立存档常量", data.constants.length]];
overview.getRange("A23:B24").values = [["证据范围", "全量 Java 与编辑器元素已索引；连接调用集合已对照。逐项行为与实机验收未完成"], ["共享字段名", data.summary.shared_state_names.join("、")]];
overview.getRange("A26:B27").values = Object.entries(data.summary.sources).map(([mod, s]) => [mod, `来源工作区/` + s.source_root]);
overview.getRange("A28:B28").values = [["来源工作区", path.resolve(projectRoot, "../yuanchuxiuzhen-1.21.1")]];
overview.getRange("A30:B34").values = [
  ["新增架构页", "状态生命周期、动态表达式、系统归组、跨系统调用；均为生成证据或待批准提案"],
  ["动态表达式", "630 组包含还原后的 helper 实参，不是 630 个唯一存档字段；具体范围见系统正文"],
  ["复制策略", "所有 Clone 复制、仅非死亡复制、非玩家数据分别记录；轮回主动重置另行核对"],
  ["系统归组", "7,465 个来源元素的候选归属；来源调用方向不代表目标架构允许依赖"],
  ["正文入口", "docs/README.md；系统事实、数据归属和待裁定项以对应正文为准"],
];
overview.getRange("A1:D34").format.font = { name: "Microsoft YaHei", size: 10, color: "#263445" };
overview.getRange("A1:D34").format.rowHeight = 24;
overview.getRange("A1:D34").format.verticalAlignment = "center";
overview.getRange("A2").format.font = { name: "Microsoft YaHei", size: 14, bold: true, color: "#244763" };
overview.getRange("A1:A34").format.columnWidth = 24;
overview.getRange("B1:B34").format.columnWidth = 104;
overview.getRange("C1:D34").format.columnWidth = 16;
overview.getRange("B30:B34").format.wrapText = true;
overview.getRange("A30:D34").format.autofitRows();
overview.getRange("A13:D13").format = { fill: "#244763", font: { name: "Microsoft YaHei", size: 10, bold: true, color: "#FFFFFF" } };
overview.getRange("B14:D16").setNumberFormat("#,##0");
overview.getRange("B18:B21").setNumberFormat("#,##0");
overview.tabColor = "#244763";

const contents = data.content.map((r) => [r.display_name, r.number_markers, r.legacy_id, r.registry, r.source_mod, r.symbol, r.family, r.translation_key, r.declaration_kind, `${r.source_file}:${r.line}`, "", "", "", `${r.registry}/${r.legacy_id}`]);
const contentSheet = tableSheet("内容编号", "ContentRegister", ["中文名称", "原有数字标记", "1.18.2 注册 ID", "注册表", "来源 Mod", "Java 注册符号", "编号族", "语言键", "注册形式", "源码位置", "目标 ID（人工）", "目标模块（人工）", "评审备注（人工）", "来源身份"], retainReview("内容编号", contents, 13, [10, 11, 12]), [26, 16, 48, 22, 23, 36, 28, 58, 26, 100, 48, 24, 68, 62], [10, 11, 12]);
contentSheet.getRange(`B2:B${contents.length + 1}`).setNumberFormat("@");

const scopes = { PlayerVariables: "玩家", WorldVariables: "维度", MapVariables: "主世界数据存储" };
const variables = data.variables.map((r) => [r.name, r.number_suffix, r.source_mod, scopes[r.scope], r.java_type, r.nbt_key, r.initial_value.length > 180 ? r.initial_value.slice(0, 180) + "…（摘录）" : r.initial_value, r.read_candidate_sites, r.write_candidate_sites, `${r.source_file}:${r.source_line}`, r.read_examples, r.write_examples, "", "", r.review_status, "", r.identity]);
const stateSheet = tableSheet("状态变量", "StateRegister", ["1.18.2 字段", "原有数字后缀", "来源 Mod", "存储范围", "Java 类型", "NBT 键", "初值（长文本摘录）", "读取候选数", "写入候选数", "声明位置", "读取候选示例", "写入候选示例", "目标名称（人工）", "目标所有者（人工）", "核对状态（人工）", "评审备注（人工）", "来源身份"], retainReview("状态变量", variables, 16, [12, 13, 14, 15]), [30, 16, 23, 20, 16, 30, 60, 16, 16, 100, 100, 100, 32, 25, 42, 68, 72], [12, 13, 14, 15]);
stateSheet.getRange(`B2:B${variables.length + 1}`).setNumberFormat("@");
stateSheet.getRange(`H2:I${variables.length + 1}`).setNumberFormat("#,##0");

tableSheet("存档常量", "StorageConstants", ["来源 Mod", "作用范围", "常量名", "存档名称", "源码位置"], data.constants.map((r) => [r.source_mod, scopes[r.scope], r.name, r.initial_value, `${r.source_file}:${r.source_line}`]), [23, 22, 20, 40, 105]);

const procedures = data.procedures.map((r) => [r.class, r.source_mod, r.event_entries, r.incoming_sites, r.source_file, "", "", `${r.source_mod}/${r.class}`]);
tableSheet("过程入口", "ProcedureRegister", ["Procedure 类", "来源 Mod", "事件入口", "静态调用处数", "源码位置", "目标模块（人工）", "核对备注（人工）", "来源身份"], retainReview("过程入口", procedures, 7, [5, 6]), [78, 23, 90, 18, 115, 28, 80, 100], [5, 6]);
const menus = data.menus.map((r) => [r.class, r.source_mod, r.add_slot_statements, r.source_file, r.note, "", "", `${r.source_mod}/${r.class}`]);
tableSheet("菜单入口", "MenuRegister", ["菜单类", "来源 Mod", "addSlot 语句数", "源码位置", "计数说明", "目标模块（人工）", "核对备注（人工）", "来源身份"], retainReview("菜单入口", menus, 7, [5, 6]), [42, 23, 20, 110, 85, 28, 80, 75], [5, 6]);

const keys = data.literal_keys.map((r) => [r.key, r.source_mod, r.operations, r.read_sites, r.write_sites, r.source_example, r.evidence, "", "", r.identity]);
tableSheet("字典键候选", "LiteralKeys", ["字面量键", "来源 Mod", "访问方法", "读取处数", "写入处数", "源码示例", "证据边界", "所属对象（人工）", "评审备注（人工）", "来源身份"], retainReview("字典键候选", keys, 9, [7, 8]), [42, 23, 42, 15, 15, 115, 100, 36, 80, 75], [7, 8]);

const cloneLabels = { all_clone_events: "所有 Clone 复制", non_death_only: "仅非死亡复制", not_player_clone: "非玩家数据" };
const lifecycle = architecture.states.map((r) => [r.field, r.source_mod, scopes[r.scope], r.proposed_owner, r.proposed_role, r.read_sites, r.write_sites, cloneLabels[r.clone_policy], r.external_consumption, r.source, r.clone_source, r.examples, r.evidence, r.identity]);
tableSheet("状态生命周期", "StateLifecycle", ["1.18.2 字段", "来源 Mod", "存储范围", "目标所有者提案", "职责提案", "读取或对象访问数", "字段赋值数", "1.18.2 复制策略", "显式外部访问", "声明位置", "复制位置", "访问示例", "证据范围", "来源身份"], lifecycle, [30, 23, 20, 30, 64, 20, 16, 24, 55, 100, 100, 115, 88, 75]);
workbook.worksheets.getItem("状态生命周期").getRange(`F2:G${lifecycle.length + 1}`).setNumberFormat("#,##0");

const carrierLabels = { entity_persistent_data: "实体持久数据", item_tag_expression: "物品标签表达式", block_entity_tile_data: "方块实体数据", declared_compound_tag: "局部 CompoundTag", variables_field_expression: "Variables 字段表达式" };
const dynamicKeys = architecture.dynamic_keys.map((r) => [r.source_mod, carrierLabels[r.carrier], r.key_expression, r.sites, r.operations, r.proposed_systems, r.examples, r.evidence]);
tableSheet("动态表达式", "DynamicExpressions", ["来源 Mod", "载体形状", "已还原的键表达式", "调用处数", "操作", "系统归属提案", "源码位置", "证据范围"], dynamicKeys, [23, 26, 100, 16, 35, 55, 115, 88]);

const systemNames = { platform: "平台", particle: "粒子库", flow: "流程", panel: "玩家面板", decor: "装饰", festival: "节日", tiandao: "天道", attribute: "属性", relation: "关系", material: "材料", storage: "储物", profession: "副职业", gongfa: "功法", xiulian: "修炼", combat: "战斗", skill: "技能", yihuo: "异火", equipment: "装备", plant: "植物", cooking: "烹饪", liandan: "炼丹", lianqi: "炼器", fulu: "符箓", craft: "工艺", world: "世界", treasure: "宝箱", town: "城镇", dungeon: "副本", creature: "生物", guchong: "蛊虫", npc: "修士", zhenfa: "阵法", economy: "经济", quest: "任务", book: "书籍" };
const systems = architecture.systems.map((r, i) => [systemNames[r.system], r.system, r.source_elements, r.java_classes, r.status, r.source_folders.split("; ").length, `docs/reference/legacy/inventory/system_coverage.csv:${i + 2}`]);
tableSheet("系统归组", "SystemOwnership", ["系统", "目录标识", "来源元素数", "单一归属 Java 类数", "状态", "来源目录数", "完整目录清单位置"], systems, [26, 23, 18, 24, 65, 18, 80]);
workbook.worksheets.getItem("系统归组").getRange(`C2:D${systems.length + 1}`).setNumberFormat("#,##0");

const dependencies = architecture.dependencies.map((r) => [r.caller_owner, r.callee_owner, r.call_sites, r.examples, r.evidence]);
tableSheet("跨系统调用", "SourceDependencies", ["调用方候选归属", "被调用方候选归属", "调用处数", "源码示例", "证据范围"], dependencies, [30, 30, 16, 120, 90]);

workbook.recalculate();
await fs.mkdir(outputDir, { recursive: true });
await fs.mkdir(previewDir, { recursive: true });
const viewRanges = { "阅读说明": "A2:D24", "内容编号": "A1:F12", "状态变量": "A1:F12", "存档常量": "A1:D5", "过程入口": "A1:D10", "菜单入口": "A1:C12", "字典键候选": "A1:E12" };
const architectureViews = { "状态生命周期": "A1:E8", "动态表达式": "A1:D7", "系统归组": "A1:E12", "跨系统调用": "A1:C12" };
const renderRanges = reviewed ? { "阅读说明": "A23:D34", ...architectureViews } : { ...viewRanges, ...architectureViews };
for (const [sheetName, range] of Object.entries(renderRanges)) {
  const preview = await workbook.render({ sheetName, range, scale: 1, format: "png" });
  await fs.writeFile(path.join(previewDir, `${sheetName}.png`), new Uint8Array(await preview.arrayBuffer()));
}
const inspection = await workbook.inspect({ kind: "sheet,table", maxChars: 7000, tableMaxRows: 2, tableMaxCols: 4 });
await fs.writeFile(path.join(previewDir, "register-inspection.ndjson"), inspection.ndjson);
const errors = await workbook.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!", options: { useRegex: true, maxResults: 100 }, maxChars: 3000 });
await fs.writeFile(path.join(previewDir, "formula-errors.ndjson"), errors.ndjson);
const stagedExport = path.join(previewDir, "export.xlsx");
await (await SpreadsheetFile.exportXlsx(workbook)).save(stagedExport);
await fs.copyFile(stagedExport, outputPath);
console.log(JSON.stringify({ outputPath, contents: contents.length, variables: variables.length, procedures: procedures.length, menus: menus.length, lifecycle: lifecycle.length, dynamicGroups: dynamicKeys.length, systemGroups: systems.length, dependencyPairs: dependencies.length }));
