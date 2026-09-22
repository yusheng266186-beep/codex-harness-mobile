// Targeted fixes for the remaining compile errors.
// Patterns seen after the encoding repair:
//   * a literal lost its closing quote and the following ')' / '}' was eaten
//   * an inline '·' leftover marks where the closing quote used to be
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

let lines = fs.readFileSync(SRC, 'utf8').split('\n');
const log = [];

function setLine(n, newText) {
  log.push({ n, was: lines[n - 1], now: newText });
  lines[n - 1] = newText;
}

// L354: the ') }' that closed LiveRuntimeState(...) was swallowed.
setLine(354, '    var health by remember { mutableStateOf(LiveRuntimeState(checking = true, detail = "正在检测手机环境…")) }');

// L403: two literals lost their closing quote.
setLine(403, '                                    onSuccess = { if (wasRunning) "已发送停止指令" else "正在启动本地服务…" },');

// L418: the label literal lost its closing quote, leaving '"{' instead of '") {'.
setLine(418, '                    AnimatedContent(targetState = selected, label = "工作区切换") { page ->');

fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`fixed ${log.length} lines`);
for (const l of log) console.log(`  L${l.n}\n    was: ${l.was.trim()}\n    now: ${l.now.trim()}`);
