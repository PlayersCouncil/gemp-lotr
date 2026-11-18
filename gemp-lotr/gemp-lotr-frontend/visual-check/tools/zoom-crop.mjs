// Cuts the same area out of several screenshots of a report and puts them side by side, enlarged pixel for pixel, to
// show the owner what a difference is (e.g. a legacy and an Angular screenshot, and their diff).
//
//   node tools/zoom-crop.mjs --area <x>,<y>,<width>,<height> [--scale 3] --out <file.png> <screenshot.png>...

import { readFileSync, writeFileSync } from 'node:fs';
import { parseArgs } from 'node:util';
import { PNG } from 'pngjs';

const { values: options, positionals: files } = parseArgs({
  options: {
    area: { type: 'string' },
    scale: { type: 'string', default: '3' },
    gap: { type: 'string', default: '8' },
    out: { type: 'string' },
  },
  allowPositionals: true,
});
if (!options.area || !options.out || files.length === 0) {
  console.error('usage: node tools/zoom-crop.mjs --area x,y,w,h [--scale 3] --out out.png a.png b.png ...');
  process.exit(2);
}
const [x, y, width, height] = options.area.split(',').map(Number);
const scale = Number(options.scale);
const gap = Number(options.gap);

const out = new PNG({ width: files.length * width * scale + (files.length - 1) * gap, height: height * scale });
out.data.fill(255);
files.forEach((file, index) => {
  const source = PNG.sync.read(readFileSync(file));
  const left = index * (width * scale + gap);
  for (let row = 0; row < height * scale; row++) {
    for (let column = 0; column < width * scale; column++) {
      const sx = x + Math.floor(column / scale);
      const sy = y + Math.floor(row / scale);
      if (sx >= source.width || sy >= source.height) continue;
      const from = (sy * source.width + sx) * 4;
      const to = (row * out.width + left + column) * 4;
      source.data.copy(out.data, to, from, from + 4);
    }
  }
});
writeFileSync(options.out, PNG.sync.write(out));
console.log(`${options.out}: ${out.width}x${out.height}`);
