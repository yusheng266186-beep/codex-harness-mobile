// Repair MainActivity.kt after an encoding round-trip damaged it.
//
// The file was read as UTF-8 but written back as GBK, which merged a trailing
// CJK character with the following byte into one unmappable char; that char was
// then replaced with U+2026 + '?'. So each damage site lost exactly ONE
// character, immediately before a closing quote. The exact original text still
// exists in a previously built APK's dex string table.
//
// Strategy: for each site, take the CJK run that precedes it, find dex strings
// starting with that prefix, and read back the single character that follows.
// Replace only the marker -- the surrounding source, including its quotes, is
// left untouched.
const fs = require('fs');

const SRC = process.argv[2];
const STRINGS = process.argv[3];
const OUT = process.argv[4];
const MARK = '…?';

const CJK = /[\u4e00-\u9fff]/;
const cjkStrings = fs
  .readFileSync(STRINGS, 'utf8')
  .split('\n')
  .filter((s) => s && CJK.test(s));

let text = fs.readFileSync(SRC, 'utf8');
const markers = (text.match(/…\?/g) || []).length;
const report = [];
let unresolved = 0;

for (let guard = 0; guard < 500; guard++) {
  const idx = text.indexOf(MARK);
  if (idx === -1) break;

  // The CJK run immediately before the marker.
  let start = idx;
  while (start > 0 && CJK.test(text[start - 1])) start--;
  const prefix = text.slice(start, idx);

  const tally = (chars) => {
    const m = new Map();
    for (const ch of chars) m.set(ch, (m.get(ch) || 0) + 1);
    return [...m.entries()].sort((a, b) => b[1] - a[1]);
  };

  // Preferred: the damage site sits mid-string, so a dex string continues the prefix.
  const asPrefix = [];
  for (const s of cjkStrings) {
    if (s.length > prefix.length && s.startsWith(prefix)) asPrefix.push(s[prefix.length]);
  }

  // Fallback: the prefix ends the string, so look for it inside a dex string.
  const asSuffix = [];
  if (asPrefix.length === 0) {
    for (const s of cjkStrings) {
      const at = s.indexOf(prefix);
      if (at >= 0 && at + prefix.length < s.length) asSuffix.push(s[at + prefix.length]);
    }
  }

  const ranked = tally(asPrefix.length ? asPrefix : asSuffix);

  if (ranked.length === 0) {
    unresolved++;
    report.push({ prefix, chosen: null, via: '-', options: [] });
    text = text.slice(0, idx) + '\u0000UNRESOLVED\u0000' + text.slice(idx + MARK.length);
    continue;
  }

  const chosen = ranked[0][0];
  report.push({
    prefix,
    chosen,
    via: asPrefix.length ? 'prefix' : 'contains',
    options: ranked,
  });

  text = text.slice(0, idx) + chosen + text.slice(idx + MARK.length);
}

fs.writeFileSync(OUT, text, 'utf8');
console.log(`markers found : ${markers}`);
console.log(`unresolved    : ${unresolved}`);
console.log(`markers left  : ${(text.match(/…\?/g) || []).length}`);
console.log('\n--- resolved ---');
for (const r of report) {
  if (r.chosen) {
    const opts = r.options.map((o) => o[0] + ':' + o[1]).join(' ');
    console.log(`  [${r.via}] "${r.prefix}" + "${r.chosen}"   [${opts}]`);
  }
}
if (unresolved) {
  console.log('\n--- UNRESOLVED (need manual look) ---');
  for (const r of report) if (!r.chosen) console.log(`  "${r.prefix}"`);
}
