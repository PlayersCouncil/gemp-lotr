import { parseServerMarkup, RICH_MARKUP } from './chat-markup';

describe('parseServerMarkup', () => {
  it('reads text, line breaks and formatting', () => {
    expect(parseServerMarkup('Welcome <b>back</b>!<br />Hi')).toEqual([
      { kind: 'text', text: 'Welcome ' },
      { kind: 'element', tag: 'b', children: [{ kind: 'text', text: 'back' }] },
      { kind: 'text', text: '!' },
      { kind: 'break' },
      { kind: 'text', text: 'Hi' },
    ]);
    expect(parseServerMarkup('')).toEqual([]);
  });

  it("reads a player's rendered markdown, its escaped text staying text", () => {
    expect(parseServerMarkup('<p>a &lt;script&gt; <em>b</em></p><blockquote>c</blockquote>')).toEqual([
      {
        kind: 'element',
        tag: 'p',
        children: [
          { kind: 'text', text: 'a <script> ' },
          { kind: 'element', tag: 'em', children: [{ kind: 'text', text: 'b' }] },
        ],
      },
      { kind: 'element', tag: 'blockquote', children: [{ kind: 'text', text: 'c' }] },
    ]);
  });

  it('keeps links to web addresses only, without their attributes', () => {
    expect(
      parseServerMarkup(
        '<a href="https://wiki.lotrtcgpc.net" target="_blank" onclick="x()">wiki</a>' +
          '<a href="javascript:alert(1)">bad</a><a>none</a>',
      ),
    ).toEqual([
      { kind: 'link', href: 'https://wiki.lotrtcgpc.net', children: [{ kind: 'text', text: 'wiki' }] },
      { kind: 'text', text: 'bad' },
      { kind: 'text', text: 'none' },
    ]);
  });

  it('drops scripts and styles, and replaces other elements by their content', () => {
    expect(
      parseServerMarkup(
        '<script>alert(1)</script><style>p{}</style><img src="x" onerror="y()">' +
          '<span class="x" style="color:red">s<i>t</i></span><div>d</div>',
      ),
    ).toEqual([
      { kind: 'text', text: 's' },
      { kind: 'element', tag: 'i', children: [{ kind: 'text', text: 't' }] },
      { kind: 'text', text: 'd' },
    ]);
  });
});

describe('parseServerMarkup with RICH_MARKUP (the patch notes)', () => {
  it('reads as before without the option', () => {
    const html =
      '<table><tr><td colspan="2">a</td></tr></table><hr><img src="images/x.png">' +
      '<span class="cardHint patchnote-card" value="1_5">c</span><a href="#format-pc_movie">f</a>';
    expect(parseServerMarkup(html)).toEqual([
      { kind: 'text', text: 'a' },
      { kind: 'text', text: 'c' },
      { kind: 'text', text: 'f' },
    ]);
  });

  it("keeps PatchNoteRenderer's further elements, with a list's start and a cell's spans", () => {
    expect(
      parseServerMarkup(
        '<ol start="3" type="a"><li>x</li></ol><table><tbody><tr><th rowspan="2" style="color:red">h</th>' +
          '<td colspan="x">d</td></tr></tbody></table><hr><details><summary>s</summary><p>b<sup>2</sup></p></details>',
        RICH_MARKUP,
      ),
    ).toEqual([
      {
        kind: 'element',
        tag: 'ol',
        attributes: { start: '3' },
        children: [{ kind: 'element', tag: 'li', children: [{ kind: 'text', text: 'x' }] }],
      },
      {
        kind: 'element',
        tag: 'table',
        children: [
          {
            kind: 'element',
            tag: 'tbody',
            children: [
              {
                kind: 'element',
                tag: 'tr',
                children: [
                  { kind: 'element', tag: 'th', attributes: { rowspan: '2' }, children: [{ kind: 'text', text: 'h' }] },
                  { kind: 'element', tag: 'td', children: [{ kind: 'text', text: 'd' }] },
                ],
              },
            ],
          },
        ],
      },
      { kind: 'element', tag: 'hr', children: [] },
      {
        kind: 'element',
        tag: 'details',
        children: [
          { kind: 'element', tag: 'summary', children: [{ kind: 'text', text: 's' }] },
          {
            kind: 'element',
            tag: 'p',
            children: [
              { kind: 'text', text: 'b' },
              { kind: 'element', tag: 'sup', children: [{ kind: 'text', text: '2' }] },
            ],
          },
        ],
      },
    ]);
  });

  it('reads pictures: culture icons and screenshots, relative ones made the legacy pages', () => {
    expect(
      parseServerMarkup(
        '<img class="patchnote-culture" src="images/cultures/gondor.png" alt="Gondor" title="Gondor">' +
          '<img src="patchnotes/2026/img/a.png" alt="A" loading="lazy"><img src="https://x.org/b.png">' +
          '<img src="/gemp-lotr/images/c.png"><img src="javascript:alert(1)"><img src="data:image/png;base64,AA">' +
          '<img src="//evil.example/d.png"><img>',
        RICH_MARKUP,
      ),
    ).toEqual([
      { kind: 'image', src: '/gemp-lotr/images/cultures/gondor.png', alt: 'Gondor', title: 'Gondor', icon: true },
      { kind: 'image', src: '/gemp-lotr/patchnotes/2026/img/a.png', alt: 'A', title: null, icon: false },
      { kind: 'image', src: 'https://x.org/b.png', alt: '', title: null, icon: false },
      { kind: 'image', src: '/gemp-lotr/images/c.png', alt: '', title: null, icon: false },
    ]);
  });

  it('reads card links: a cardHint span naming a blueprint', () => {
    expect(
      parseServerMarkup(
        '<span class="cardHint patchnote-card" value="1_5" title="Cleaving Blow">the <b>Blow</b></span>' +
          '<span class="cardHint" value="1_5; x">bad</span><span title="t">plain</span>',
        RICH_MARKUP,
      ),
    ).toEqual([
      {
        kind: 'card',
        blueprintId: '1_5',
        title: 'Cleaving Blow',
        children: [
          { kind: 'text', text: 'the ' },
          { kind: 'element', tag: 'b', children: [{ kind: 'text', text: 'Blow' }] },
        ],
      },
      { kind: 'text', text: 'bad' },
      { kind: 'text', text: 'plain' },
    ]);
  });

  it("reads links into the hall and to the site's own pages; others give way to their text", () => {
    expect(
      parseServerMarkup(
        '<a href="#format-pc_movie">a</a><a href="#">b</a><a href="patchnotes/2026/notes.txt">c</a>' +
          '<a href="/gemp-lotr/deckBuild.html">d</a><a href="https://x.org" target="_blank">e</a>' +
          '<a href="javascript:x()">f</a><a href="mailto:a@b.c">g</a>',
        RICH_MARKUP,
      ),
    ).toEqual([
      { kind: 'hall-link', hash: '#format-pc_movie', children: [{ kind: 'text', text: 'a' }] },
      { kind: 'text', text: 'b' },
      { kind: 'link', href: '/gemp-lotr/patchnotes/2026/notes.txt', children: [{ kind: 'text', text: 'c' }] },
      { kind: 'link', href: '/gemp-lotr/deckBuild.html', children: [{ kind: 'text', text: 'd' }] },
      { kind: 'link', href: 'https://x.org', children: [{ kind: 'text', text: 'e' }] },
      { kind: 'text', text: 'f' },
      { kind: 'text', text: 'g' },
    ]);
  });
});
