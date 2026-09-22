// The dex-derived strings dragged in stray CR characters, which break literals.
// Remove CR/LF that appear *inside* a quoted literal (the raw-string block is
// left completely alone).
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const text = fs.readFileSync(SRC, 'utf8');
const lines = text.split(/\r?\n/);
const out = [];
let touched = 0;

for (const line of lines) {
  // Leave the raw-string block untouched.
  if (/^\s*private val \w+ = """/.test(line) || /^\s*"""\.trimIndent/.test(line)) {
    out.push(line);
    continue;
  }
  if (!/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/.test(line)) {
    out.push(line);
    continue;
  }
  const cleaned = line.replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/g, '');
  touched++;
  out.push(cleaned);
}

fs.writeFileSync(OUT, out.join('\n'), 'utf8');
console.log(`lines with control chars cleaned: ${touched}`);
