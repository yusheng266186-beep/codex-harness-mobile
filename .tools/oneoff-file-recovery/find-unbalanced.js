// Find lines where string-literal quotes are unbalanced, i.e. the earlier
// blanket replacement swallowed a closing quote.
const fs = require('fs');
const lines = fs.readFileSync(process.argv[2], 'utf8').split(/\r?\n/);
let n = 0;
lines.forEach((l, i) => {
  const q = (l.match(/"/g) || []).length;
  if (q % 2 === 1) {
    n++;
    console.log(`L${i + 1} (${q} quotes): ${l.trim()}`);
  }
});
console.log(`\ntotal unbalanced lines: ${n}`);
