// Last two literals. The second line lost an inline template expression
// ("${...}·" plus the "Codex 与 Harness 只监听 127.0.0.1" run), so it is rebuilt
// from the surrounding source plus the dex-confirmed tail.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

let text = fs.readFileSync(SRC, 'utf8');

const fixes = [
  [
    '· 思…?${client.settings.effort}"',
    '· 思考 ${client.settings.effort}"',
  ],
  [
    'Text("Codex Harness 移动端\\"7.0.0.1，不向局域网或互联网开放…?, color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)',
    'Text("Codex 与 Harness 只监听 127.0.0.1，不向局域网或互联网开放。", color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)',
  ],
];

let applied = 0;
for (const [from, to] of fixes) {
  if (text.includes(from)) { text = text.replace(from, to); applied++; }
  else console.log(`NOT FOUND: ${from}`);
}

fs.writeFileSync(OUT, text, 'utf8');
console.log(`applied ${applied}/${fixes.length}`);
console.log(`remaining "…?": ${(text.match(/…\?/g) || []).length}`);
