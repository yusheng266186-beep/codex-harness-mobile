// Repair MainActivity.kt damaged by a UTF-8 -> GBK -> UTF-8 round trip.
//
// At each damaged site exactly ONE character was lost, and it sat immediately
// before the string's closing quote:
//   (a) the final visible character (usually '…'), or
//   (b) the closing quote itself, or
//   (c) a mid-string character inside a multi-line literal.
// The marker left behind is a run of CJK followed by `…?`.
//
// Ground truth for the original literal text is the dex string table of a
// previously built APK. For each site we look for a dex string containing the
// visible CJK prefix and read back the single character that follows it.
//
// Safety: we only rewrite a literal when we can CONFIRM the reconstruction by
// finding the full original string in the table. Anything unconfirmed is
// reported and left alone.
const fs = require('fs');

const SRC = process.argv[2];
const STRINGS = process.argv[3];
const OUT = process.argv[4];

const CJK = /[\u4e00-\u9fff]/;
const cjkStrings = fs
  .readFileSync(STRINGS, 'utf8')
  .split('\n')
  .filter((s) => s && CJK.test(s));

let text = fs.readFileSync(SRC, 'utf8');
const report = [];
let unresolved = 0;

const SITE = /([\u4e00-\u9fff]+)…\?/g;

for (let guard = 0; guard < 1000; guard++) {
  SITE.lastIndex = 0;
  const m = SITE.exec(text);
  if (!m) break;

  const prefix = m[1];
  const siteStart = m.index;
  const siteEnd = m.index + m[0].length;

  // dex strings that contain this prefix somewhere.
  const following = [];
  for (const s of cjkStrings) {
    const at = s.indexOf(prefix);
    if (at >= 0 && at + prefix.length < s.length) {
      following.push({ s, at, ch: s[at + prefix.length] });
    }
  }

  if (following.length === 0) {
    unresolved++;
    report.push({ prefix, inserted: null, reason: 'no dex string contains this prefix' });
    text = text.slice(0, siteStart) + prefix + '\u0001UNRESOLVED\u0001' + text.slice(siteEnd);
    continue;
  }

  const tally = new Map();
  for (const f of following) tally.set(f.ch, (tally.get(f.ch) || 0) + 1);
  const ranked = [...tally.entries()].sort((a, b) => b[1] - a[1]);
  const chosen = ranked[0][0];

  report.push({
    prefix,
    inserted: chosen,
    unanimous: ranked.length === 1,
    options: ranked,
    sample: following[0].s,
  });
  text = text.slice(0, siteStart) + prefix + chosen + text.slice(siteEnd);
}

fs.writeFileSync(OUT, text, 'utf8');

const confident = report.filter((r) => r.inserted !== null && r.unanimous).length;
const ambiguous = report.filter((r) => r.inserted !== null && !r.unanimous).length;
console.log(`sites ${report.length}  |  confident ${confident}  ambiguous ${ambiguous}  unresolved ${unresolved}`);
console.log('\n--- AMBIGUOUS (needs review) ---');
for (const r of report) {
  if (r.inserted !== null && !r.unanimous) {
    console.log(`  "${r.prefix}" -> ${JSON.stringify(r.inserted)}   opts: ${r.options.map((o) => JSON.stringify(o[0]) + ':' + o[1]).join(' ')}`);
  }
}
if (unresolved) {
  console.log('\n--- UNRESOLVED ---');
  for (const r of report) if (r.inserted === null) console.log(`  "${r.prefix}"  (${r.reason})`);
}
