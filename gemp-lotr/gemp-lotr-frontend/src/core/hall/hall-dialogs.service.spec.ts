import { TestBed } from '@angular/core/testing';
import {
  escapeHtml,
  HallDialogs,
  plainTextMarkup,
  serverMessageHtml,
  serverMessageMarkup,
} from './hall-dialogs.service';

describe('the hall dialogs', () => {
  it('keeps only the bare formatting tags of server text', () => {
    expect(serverMessageHtml('<b>Hi</b> <script>x</script><BR/>&')).toBe(
      '<b>Hi</b> &lt;script&gt;x&lt;/script&gt;<br>&amp;',
    );
    expect(serverMessageMarkup('a<br>b')).toEqual([
      { kind: 'text', text: 'a' },
      { kind: 'break' },
      { kind: 'text', text: 'b' },
    ]);
    expect(serverMessageMarkup(null)).toEqual([]);
    expect(plainTextMarkup('<i>\nx')).toEqual([
      { kind: 'text', text: '<i>' },
      { kind: 'break' },
      { kind: 'text', text: 'x' },
    ]);
    expect(escapeHtml(`"'`)).toBe('&quot;&#39;');
  });

  it('shows and closes dialogs, newest last', () => {
    TestBed.configureTestingModule({ providers: [HallDialogs] });
    const dialogs = TestBed.inject(HallDialogs);
    const a = dialogs.showText('A', 'one');
    dialogs.showText('B', 'two');
    expect(dialogs.open().map((d) => d.title)).toEqual(['A', 'B']);
    dialogs.close(a.id);
    expect(dialogs.open().map((d) => d.title)).toEqual(['B']);
  });
});
