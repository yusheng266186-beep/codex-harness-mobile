// Extract all strings from a DEX string table.
// Usage: node dexstrings.js <file.dex> <out.txt>
const fs = require('fs');
const path = process.argv[2];
const outPath = process.argv[3];
const b = fs.readFileSync(path);

const u32 = (o) => b.readUInt32LE(o);
const stringIdsSize = u32(0x38);
const stringIdsOff = u32(0x3c);

function uleb128(off) {
  let result = 0, shift = 0, pos = off, byte;
  do {
    byte = b[pos++];
    result |= (byte & 0x7f) << shift;
    shift += 7;
  } while (byte & 0x80);
  return [result >>> 0, pos];
}

const strings = [];
for (let i = 0; i < stringIdsSize; i++) {
  const dataOff = u32(stringIdsOff + i * 4);
  const [len, pos] = uleb128(dataOff);
  // MUTF-8; decode as utf8 with a manual NUL terminator (len is in UTF-16 units,
  // so scan to the terminating 0 byte instead of trusting len).
  let end = pos;
  while (b[end] !== 0) end++;
  strings.push(b.toString('utf8', pos, end));
}

fs.writeFileSync(outPath, strings.join('\n'), 'utf8');
console.log(`strings: ${strings.length} -> ${outPath}`);
const cjk = strings.filter((s) => /[\u4e00-\u9fff]/.test(s));
console.log(`containing CJK: ${cjk.length}`);
