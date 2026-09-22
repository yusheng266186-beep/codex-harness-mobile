// Final compile fixes: the repair re-inserted closing quotes but the separator
// (`,` or `)`) that used to follow them was consumed at the same time.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const fixes = {
  475: '            title = { Text("需要你的确认") },',
  506: '                    Text("Codex 移动工作台", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)',
  603: '                action = if (!health.commandPermission) "授予权限" else "启动并打开",',
  610: '                    Text("已接入功能", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)',
  623: '                Text(if (!health.commandPermission) "授予 Termux 权限" else "启动 Codex 工作台")',
  642: '            TextButton(onClick = onClose) { Text("返回控制台") }',
  888: '                    color = if (draft.sandboxMode == "workspaceWrite") Green else Amber,',
  919: '                Text("暂无已保存对话。完成一次 Codex 对话后，这里会显示可继续的任务。", color = TextSecondary)',
  985: '                Text("正在生成…", color = TextSecondary, fontSize = 10.sp)',
  999: '        Text("Codex 正在思考和执行…", color = TextSecondary, fontSize = 11.sp)',
  1069: '                TextButton(onClick = onCloseWeb) { Text("返回控制台") }',
  1179: '                        Text("仅本机访问", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)',
};

const lines = fs.readFileSync(SRC, 'utf8').split('\n');
let applied = 0;
for (const [n, text] of Object.entries(fixes)) {
  const i = Number(n) - 1;
  if (lines[i] === undefined) { console.log(`L${n}: MISSING`); continue; }
  lines[i] = text;
  applied++;
}
fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`applied ${applied}/${Object.keys(fixes).length}`);
