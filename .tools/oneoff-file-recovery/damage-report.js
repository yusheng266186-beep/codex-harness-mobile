// List every string literal in the file that never closes, with the dex string
// it should become. This is the authoritative damage report.
const fs = require('fs');

const SRC = process.argv[2];
const STRINGS = process.argv[3];

const all = fs.readFileSync(STRINGS, 'utf8').split('\n').filter(Boolean);
const CJK = /[\u4e00-\u9fff]/;
const cjk = all.filter((s) => CJK.test(s)).sort((a, b) => b.length - a.length);

const src = fs.readFileSync(SRC, 'utf8');
const lines = src.split(/\r?\n/);

lines.forEach((line, i) => {
  // Simulate string scanning across the whole file (literals can span lines,
  // but the damaged ones here are single-line).
  let inStr = false, open = -1;
  for (let j = 0; j < line.length; j++) {
    const c = line[j];
    if (c === '\\') { j++; continue; }
    if (c === '"') {
      if (inStr) inStr = false;
      else { inStr = true; open = j; }
    }
  }
  if (!inStr) return;

  const after = line.slice(open + 1);
  // Which dex string does this appear to be a damaged version of?
  const best = cjk.find((s) => {
    const stem = s.slice(0, Math.max(0, s.length - 1));
    return (
      after.startsWith(s) ||
      after.startsWith(stem + '…') ||
      after.startsWith(stem) ||
      stem.startsWith(after.slice(0, Math.min(after.length, 12)))
    );
  });
  console.log(`L${i + 1}`);
  console.log(`  in file : "${after}"`);
  console.log(`  dex says: "${best || '?? NO MATCH ??'}"`);
});
