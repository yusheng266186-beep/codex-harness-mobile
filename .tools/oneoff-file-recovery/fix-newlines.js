// Fix literals that a dex string broke across lines: the dex table's raw string
// contains a real newline where the source needs the two-character escape.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const lines = fs.readFileSync(SRC, 'utf8').split(/\r?\n/);
const fixed = [];

for (let i = 0; i < lines.length; i++) {
  const q = (lines[i].match(/"/g) || []).length;
  if (q % 2 === 0) continue;
  if (/^\s*private val \w+ = """/.test(lines[i])) continue;   // raw string open
  if (/^\s*"""\.trimIndent/.test(lines[i])) continue;          // raw string close

  // Join the following line(s) until quotes balance, inserting \n escapes.
  let joined = lines[i];
  let j = i;
  while ((joined.match(/"/g) || []).length % 2 === 1 && j + 1 < lines.length) {
    j++;
    joined = joined.replace(/\s*$/, '\\n') + lines[j].trim();
  }
  const before = lines[i];
  lines[i] = joined;
  for (let k = i + 1; k <= j; k++) lines[k] = null;
  fixed.push({ line: i + 1, before, after: joined });
}

const out = lines.filter((l) => l !== null).join('\n');
fs.writeFileSync(OUT, out, 'utf8');
console.log(`joined ${fixed.length} literal(s)`);
for (const f of fixed) console.log(`  L${f.line}\n    was: ${f.before.trim()}\n    now: ${f.after.trim()}`);
