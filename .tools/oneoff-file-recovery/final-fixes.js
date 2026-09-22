// Final targeted fixes for the last 12 damaged literals.
// Each replacement below is the literal text confirmed from the dex string
// table of the pre-corruption APK build.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const fixes = [
  ['confirmButton = { Button(onClick = { codex.respondToApproval(true) }) { Text("允许一…?) } },',
   'confirmButton = { Button(onClick = { codex.respondToApproval(true) }) { Text("允许一次") } },' ],

  ['else Icon(Icons.Rounded.Refresh, "刷新状…?, tint = TextSecondary)',
   'else Icon(Icons.Rounded.Refresh, "刷新状态", tint = TextSecondary)'],

  ['Text("…?多会话与历史记录\\n…?模型和思考强度选择\\n…?文件浏览、编辑、生成和差异查看\\n…?终端、Git、审批和中断\\n…?手机端响应式布局与附件上…?, color = TextSecondary, fontSize = 12.sp, lineHeight = 19.sp)',
   'Text("· 多会话与历史记录\\n· 模型和思考强度选择\\n· 文件浏览、编辑、生成和差异查看\\n· 终端、Git、审批和中断\\n· 手机端响应式布局与附件上传", color = TextSecondary, fontSize = 12.sp, lineHeight = 19.sp)'],

  ['title = if (client.connected) "Codex 已连· else "Codex 尚未连接",',
   'title = if (client.connected) "Codex 已连接" else "Codex 尚未连接",'],

  ['Text("新对…?, fontSize = 11.sp)',
   'Text("新对话", fontSize = 11.sp)'],

  ['"gpt-5.6-luna" to "快速响…?,',
   '"gpt-5.6-luna" to "快速响应",'],

  ['val efforts = listOf("low" to "低：更快", "medium" to "中：均衡", "high" to "高：更深…?, "xhigh" to "极高：最充分")',
   'val efforts = listOf("low" to "低：更快", "medium" to "中：均衡", "high" to "高：更深入", "xhigh" to "极高：最充分")'],

  ['Text("思考强…?, color = TextSecondary, fontSize = 12.sp)',
   'Text("思考强度", color = TextSecondary, fontSize = 12.sp)'],

  ['if (user) "· else if (system) "系统" else "Codex",',
   'if (user) "你" else if (system) "系统" else "Codex",'],

  ['label = "发送按…?)',
   'label = "发送按钮")'],

  ['else Icon(Icons.AutoMirrored.Rounded.Send, "发…?, tint = if (enabled && value.isNotBlank()) Ink else TextSecondary)',
   'else Icon(Icons.AutoMirrored.Rounded.Send, "发送", tint = if (enabled && value.isNotBlank()) Ink else TextSecondary)'],

  ['label = "状态颜…?)',
   'label = "状态颜色")'],
];

let text = fs.readFileSync(SRC, 'utf8');
let applied = 0;
const missed = [];

for (const [from, to] of fixes) {
  if (text.includes(from)) {
    text = text.replace(from, to);
    applied++;
  } else {
    missed.push(from);
  }
}

fs.writeFileSync(OUT, text, 'utf8');
console.log(`applied ${applied}/${fixes.length}`);
if (missed.length) {
  console.log('\n--- not found (check escaping) ---');
  for (const m of missed) console.log(`  ${m}`);
}
