// Generates legacy-vertical-bar-layouts.json: paste into the browser console on the legacy deckBuild.html (logged in)
// and save the copied JSON. It lays random sites out with the legacy VerticalBarGroup (the sites column,
// js/gemp-022/cards/CardGroup.js) and records where each ended up; card-group-layout.spec.ts checks the port.
// Per case: cards = horizontal flags (0/1), expected = [left, top, width, height, zIndex] per card.
(() => {
  let seed = 4242;
  const rnd = () => { seed = (seed * 1103515245 + 12345) % 2147483648; return seed / 2147483648; };
  const int = (a, b) => a + Math.floor(rnd() * (b - a + 1));
  const out = [];
  for (let c = 0; c < 60; c++) {
    const width = int(60, 260), height = int(100, 900);
    const cards = Array.from({ length: int(1, 12) }, () => (rnd() < 0.85 ? 1 : 0));
    const container = $('<div style="position:absolute;left:0;top:0"></div>').appendTo('body');
    const group = new VerticalBarGroup(container, () => true, false);
    const divs = cards.map((h, i) => {
      const card = new Card(h ? '1_319' : '1_1', null, null, 'deck', i, 'player');
      const div = Card.CreateCardDiv(card.imageUrl, null, null, false, false, false);
      div.data('card', card);
      container.append(div);
      return div;
    });
    group.setBounds(0, 0, width, height);
    const rect = (d) => [parseInt(d.css('left')), parseInt(d.css('top')), parseInt(d.css('width')), parseInt(d.css('height')), parseInt(d.css('z-index'))];
    out.push({ width, height, cards, expected: divs.map(rect) });
    container.remove();
  }
  if (typeof copy === 'function') {
    copy(JSON.stringify(out));
    console.log(`${out.length} cases copied to the clipboard`);
  }
  return out;
})();
