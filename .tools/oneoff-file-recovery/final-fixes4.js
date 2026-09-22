// Last literal: the line lost an inline template expression along with the
// closing of that template, leaving a stray backslash-quote. Rebuild the whole
// Text(...) call from the dex-confirmed string.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

let lines = fs.readFileSync(SRC, 'utf8').split(/\r?\n/);
let fixed = 0;

for (let i = 0; i < lines.length; i++) {
  if (!lines[i].includes('…?') || !lines[i].includes('不向局域网或互联网开放')) continue;
  const indent = lines[i].match(/^\s*/)[0];
  lines[i] =
    indent +
    'Text("Codex 与 Harness 只监听 127.0.0.1，不向局域网或互联网开放。", ' +
    'color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)';
  fixed++;
}

fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`fixed ${fixed} line(s)`);
const text = fs.readFileSync(OUT, 'utf8');
console.log(`remaining "…?": ${(text.match(/…\?/g) || []).length}`);
