// Generates legacy-card-group-layouts.json: paste into the browser console on the legacy deckBuild.html (logged in),
// then save the printed JSON. It lays random cards out with the legacy NormalCardGroup (js/gemp-022/cards/CardGroup.js)
// and records where each card ended up. card-group-layout.spec.ts checks the port against these results.
// Format per case: cards = [horizontal(0/1), [attached horizontal(0/1)...]?], expected = [[left, top, width, height,
// zIndex], [attached rects...]?] per card.
(() => {
  let seed = 12345;
  const rnd = () => { seed = (seed * 1103515245 + 12345) % 2147483648; return seed / 2147483648; };
  const int = (a, b) => a + Math.floor(rnd() * (b - a + 1));
  const out = [];
  for (let c = 0; c < 150; c++) {
    const width = int(40, 1000), height = int(20, 900);
    const maxCardHeight = [200, 497, null][int(0, 2)];
    const n = int(1, 40);
    const withAttachments = rnd() < 0.4;
    const cards = [];
    for (let i = 0; i < n; i++) {
      const horizontal = rnd() < 0.25;
      const attached = withAttachments ? Array.from({ length: int(0, 3) }, () => ({ horizontal: rnd() < 0.15 })) : [];
      cards.push(withAttachments ? [horizontal ? 1 : 0, attached.map((a) => a.horizontal ? 1 : 0)] : [horizontal ? 1 : 0]);
    }
    const container = $('<div style="position:absolute;left:0;top:0"></div>').appendTo('body');
    const group = new NormalCardGroup(container, (card) => card.zone !== 'attached', false);
    group.maxCardHeight = maxCardHeight;
    const divs = cards.map((spec, i) => {
      const specH = spec[0] === 1; const specA = (spec[1] || []).map((x) => ({ horizontal: x === 1 }));
      const make = (h, zone) => { const card = new Card(h ? '1_319' : '1_1', null, null, zone, i, 'player'); const div = Card.CreateCardDiv(card.imageUrl, null, null, false, false, false); div.data('card', card); container.append(div); return div; };
      const div = make(specH, 'FREE_PEOPLE');
      const atts = specA.map((a) => make(a.horizontal, 'attached'));
      div.data('card').attachedCards = atts;
      return { div, atts };
    });
    group.setBounds(0, 0, width, height);
    const rect = (d) => [parseInt(d.css("left")), parseInt(d.css("top")), parseInt(d.css("width")), parseInt(d.css("height")), parseInt(d.css("z-index"))];
    out.push({ width, height, maxCardHeight, cards, expected: divs.map(({ div, atts }) => atts.length ? [rect(div), atts.map(rect)] : [rect(div)]) });
    container.remove();
  }
  copy(JSON.stringify(out));
  console.log(`${out.length} cases copied to the clipboard`);
})();
