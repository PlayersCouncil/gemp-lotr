import { parseServerMarkup } from './chat-markup';

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
