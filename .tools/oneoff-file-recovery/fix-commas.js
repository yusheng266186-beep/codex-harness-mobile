// The last three errors are missing commas after multi-line argument expressions.
const fs = require('fs');

const SRC = process.argv[2];
const OUT = process.argv[3];

const fixes = {
  602: '                detail = "首次启动会在 Debian/ARM64 中下载 cdesktop 组件，约 50 MB。",',
  887: '                    else "只读模式：可分析文件，但不会写入工程。",',
  1205: '                "首次进入 Harness 后，请在“设置 → 模型”中填写 DeepSeek API Key，并选择工作目录。密钥保存在手机本地的 Harness 配置中。",',
};

const lines = fs.readFileSync(SRC, 'utf8').split('\n');
for (const [n, text] of Object.entries(fixes)) lines[Number(n) - 1] = text;
fs.writeFileSync(OUT, lines.join('\n'), 'utf8');
console.log(`applied ${Object.keys(fixes).length} comma fixes`);
