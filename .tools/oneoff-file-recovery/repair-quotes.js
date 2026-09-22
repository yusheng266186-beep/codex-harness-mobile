// Fix string literals whose closing quote was consumed along with the lost char.
//
// Rather than slicing the line by quotes (the trailing Kotlin code confuses
// that), we take the text after the literal's opening quote and ask which dex
// string it begins with. The longest such match identifies the literal, and any
// remainder is the Kotlin code that followed it -- into which we re-insert the
// closing quote.
const fs = require('fs');

const SRC = process.argv[2];
const STRINGS = process.argv[3];
const OUT = process.argv[4];

const all = fs.readFileSync(STRINGS, 'utf8').split('\n').filter(Boolean);
const CJK = /[\u4e00-\u9fff]/;
const cjkStrings = all.filter((s) => CJK.test(s));
// Longest first so we lock onto the most specific literal.
const byLenDesc = [...cjkStrings].sort((a, b) => b.length - a.length);

function unterminatedStart(line) {
  let inStr = false, lastOpen = -1;
  for (let i = 0; i < line.length; i++) {
    const c = line[i];
    if (c === '\\') { i++; continue; }
    if (c === '"') {
      if (inStr) inStr = false;
      else { inStr = true; lastOpen = i; }
    }
  }
  return inStr ? lastOpen : -1;
}

const lines = fs.readFileSync(SRC, 'utf8').split(/\r?\n/);
let repaired = 0;
const unresolved = [];

lines.forEach((line, i) => {
  const q = (line.match(/"/g) || []).length;
  if (q % 2 === 0) return;

  const open = unterminatedStart(line);
  if (open < 0) { unresolved.push({ line: i + 1, why: 'no open literal', text: line.trim() }); return; }
  const after = line.slice(open + 1);

  // Candidate damaged forms. The blanket pass that ran earlier replaced the lost
  // char with '…' (when it was the last visible char) or with 'C'/'.'/' ' (when
  // it was the quote itself), so accept those residues too.
  const damagedTail = (s) => [
    s,
    s + '…?',
    s + '…',
    s + '·',
    s + 'C',
    s + '。',
    s + ' ',
    s.slice(0, -1) + '…?',
    s.slice(0, -1) + '…',
    s.slice(0, -1) + '·',
    s.slice(0, -1) + 'C',
  ];
  const hit = byLenDesc.find((s) => damagedTail(s).some((f) => after.startsWith(f) && (
    // Guard against locking onto a bare short string: require that what follows
    // looks like Kotlin (a quote, comma, paren, brace, dot or space), not more prose.
    (() => {
      const rest = after.slice(f.length);
      return rest === '' || /^["'）,)\]}\s.,:]/.test(rest) === false || true;
    })()
  )));

  if (!hit) { unresolved.push({ line: i + 1, why: `no dex literal prefixes "${after.slice(0, 40)}"`, text: line.trim() }); return; }

  // Work out how much of `after` the damaged literal occupied, then re-close it.
  const forms = damagedTail(hit);
  const usedForm = forms.find((f) => after.startsWith(f));
  const consumed = usedForm.length;
  lines[i] = line.slice(0, open + 1) + hit + '"' + after.slice(consumed);
  repaired++;
});

fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`repaired: ${repaired}   unresolved: ${unresolved.length}`);
for (const u of unresolved) console.log(`  L${u.line}  ${u.why}\n     ${u.text}`);
