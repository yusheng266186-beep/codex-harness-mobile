// Final batch: the remaining literals where a legitimate '…' was damaged and
// took the closing quote with it. Every replacement below is the literal text
// confirmed present in the dex string table.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const fixes = [
  ['Text("基于成熟开…?GUI，手机上使用会话、文件、终端、审批和模型控制"',
   'Text("基于成熟开源 GUI，手机上使用会话、文件、终端、审批和模型控制"'],

  ['Text("对接手机…?Codex app-server"',
   'Text("对接手机内 Codex app-server"'],

  ['WorkflowButton("检查工…?) { draft = "请检查当前工作区的项目结构、关键配置和未提交改动，先给出简明报告· }',
   'WorkflowButton("检查工程") { draft = "请检查当前工作区的项目结构、关键配置和未提交改动，先给出简明报告。" }'],

  ['"思…?${client.settings.effort}"',
   '"思考 ${client.settings.effort}"'],

  ['else if (enabled) "告诉 Codex 你想完成什么· else "启动 Codex 后即可输…?, fontSize = 12.sp) },',
   'else if (enabled) "告诉 Codex 你想完成什么…" else "启动 Codex 后即可输入", fontSize = 12.sp) },'],

  ['title = if (health.harnessOnline) "Harness 服务运行· else "Harness 服务未启…?,',
   'title = if (health.harnessOnline) "Harness 服务运行中" else "Harness 服务未启动",'],

  ['else "点击按钮后会…?App 内自动打开"',
   'else "点击按钮后会在 App 内自动打开"'],

  ['StatusRow(Icons.Rounded.Terminal, "Termux", if (health.termuxInstalled) "已安· else "未安…?, health.termuxInstalled)',
   'StatusRow(Icons.Rounded.Terminal, "Termux", if (health.termuxInstalled) "已安装" else "未安装", health.termuxInstalled)'],

  ['StatusRow(Icons.Rounded.Code, "Codex app-server", if (health.codexOnline) "运行· else "已停…?, health.codexOnline)',
   'StatusRow(Icons.Rounded.Code, "Codex app-server", if (health.codexOnline) "运行中" else "已停止", health.codexOnline)'],

  ['StatusRow(Icons.Rounded.Language, "DeepSeek Harness", if (health.harnessOnline) "运行· else "已停…?, health.harnessOnline)',
   'StatusRow(Icons.Rounded.Language, "DeepSeek Harness", if (health.harnessOnline) "运行中" else "已停止", health.harnessOnline)'],

  ['Text("Codex Harness 移动端"7.0.0.1，不向局域网或互联网开放…?, color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)',
   'Text("Codex 与 Harness 只监听 127.0.0.1，不向局域网或互联网开放。", color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)'],
];

let text = fs.readFileSync(SRC, 'utf8');
let applied = 0;
const missed = [];

for (const [from, to] of fixes) {
  if (text.includes(from)) { text = text.replace(from, to); applied++; }
  else missed.push(from);
}

fs.writeFileSync(OUT, text, 'utf8');
console.log(`applied ${applied}/${fixes.length}`);
if (missed.length) { console.log('\n--- not found ---'); for (const m of missed) console.log(`  ${m}`); }
console.log(`remaining "…?" markers: ${(text.match(/…\?/g) || []).length}`);
