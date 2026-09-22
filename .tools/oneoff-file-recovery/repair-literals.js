// Repair string literals left unterminated by the encoding round trip.
//
// Score-based: for each unterminated literal, score every CJK dex string by how
// much of the damaged text it explains when both are normalized (markers `…?`
// removed). Pick the best-scoring string, then rebuild the literal as
//   " <dex string> " <source code that followed the literal> .
const fs = require('fs');

const SRC = process.argv[2];
const STRINGS = process.argv[3];
const OUT = process.argv[4];

const norm = (s) => s.replace(/…\?/g, '');
const commonPrefix = (a, b) => {
  let i = 0;
  while (i < a.length && i < b.length && a[i] === b[i]) i++;
  return i;
};

const CJK = /[\u4e00-\u9fff]/;
const all = fs.readFileSync(STRINGS, 'utf8').split('\n').filter(Boolean);
const cjk = all.filter((s) => CJK.test(s) && norm(s) === s);

const lines = fs.readFileSync(SRC, 'utf8').split(/\r?\n/);
const report = [];
let done = 0, failed = 0;

lines.forEach((line, idx) => {
  let inStr = false, open = -1;
  for (let j = 0; j < line.length; j++) {
    const c = line[j];
    if (c === '\\') { j++; continue; }
    if (c === '"') { if (inStr) inStr = false; else { inStr = true; open = j; } }
  }
  if (!inStr || open < 0) return;

  const after = line.slice(open + 1);
  const na = norm(after);

  let best = null, bestScore = 0;
  for (const s of cjk) {
    let score = 0;
    // Characters of the damaged text explained, in order.
    let i = 0, j = 0;
    while (i < na.length && j < s.length) {
      if (na[i] === s[j]) { score++; i++; j++; }
      else break;
    }
    // The part of the damaged text that is clearly Kotlin code must agree with
    // where the dex string ends, so reward alignment there too.
    const tailAgreement = commonPrefix(na.slice(i), s.slice(j));
    score += tailAgreement * 0.5;
    if (score > bestScore) { bestScore = score; best = s; }
  }

  if (!best || bestScore < 4) { failed++; report.push({ idx: idx + 1, ok: false, after }); return; }

  // Cut the damaged text at `best.length` visible chars (markers don't count).
  let seen = 0, cut = 0;
  while (cut < after.length && seen < best.length) {
    if (after.startsWith('…?', cut)) { cut += 2; continue; }
    cut++; seen++;
  }

  lines[idx] = line.slice(0, open + 1) + best + '"' + after.slice(cut);
  done++;
  report.push({ idx: idx + 1, ok: true, best, score: bestScore });
});

fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`repaired: ${done}   failed: ${failed}`);
console.log('\n--- repaired ---');
for (const r of report) if (r.ok) console.log(`  L${r.idx} (score ${r.score}): "${r.best}"`);
if (failed) {
  console.log('\n--- FAILED ---');
  for (const r of report) if (!r.ok) console.log(`  L${r.idx}: "${r.after}"`);
}
