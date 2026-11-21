import { readServerResponse } from '../../../testing/files';
import { parsePatchNotesMonths, parsePatchNotesPage, parsePatchNotesSlug } from './patch-notes';

describe('patch notes (GET /patchnotes, /patchnotes/months)', () => {
  const page = parsePatchNotesPage(JSON.parse(readServerResponse('patch-notes-page.json')));

  it('reads a page: its place in the feed, the filters and the updates', () => {
    expect(page).toMatchObject({ total: 7, start: 5, count: 5, tag: null, all: 7 });
    expect(page.filters).toEqual([
      { tag: 'Card Fixes', count: 3 },
      { tag: 'PC Updates', count: 2 },
      { tag: 'User Interface', count: 2 },
      { tag: 'Announcement', count: 1 },
    ]);
    const [note, announcement] = page.notes;
    expect(note).toMatchObject({
      slug: '2026-09-27-hall-overhaul',
      kind: 'note',
      date: '2026-09-27',
      title: 'The hall overhaul',
      tags: ['User Interface', 'Card Fixes'],
    });
    expect(announcement).toMatchObject({ kind: 'announcement', title: null, summary: null, summaryMarkup: null });
  });

  it("draws a summary's culture icons, and a body's cards, hall links, screenshots and web links as data", () => {
    const [note, announcement] = page.notes;
    expect(note?.summaryMarkup).toEqual([
      { kind: 'text', text: 'New tabs and ' },
      { kind: 'image', src: '/gemp-lotr/images/cultures/gondor.png', alt: 'Gondor', title: 'Gondor', icon: true },
      { kind: 'text', text: ' fixes.' },
    ]);
    const body = JSON.stringify(note?.body);
    expect(body).toContain('{"kind":"element","tag":"h2","children":[{"kind":"text","text":"What changed"}]}');
    expect(body).toContain('{"kind":"card","blueprintId":"1_5","title":"Cleaving Blow"');
    expect(body).toContain('{"kind":"hall-link","hash":"#format-pc_movie"');
    expect(body).toContain('"src":"/gemp-lotr/patchnotes/2026/img/hall.png","alt":"The new hall"');
    expect(body).toContain('{"kind":"link","href":"https://lotrtcgpc.net/"');
    expect(announcement?.body).toEqual([
      {
        kind: 'element',
        tag: 'p',
        children: [
          { kind: 'text', text: 'The server restarts at noon.' },
          { kind: 'break' },
          { kind: 'text', text: 'Back soon.' },
        ],
      },
    ]);
    const plain = parsePatchNotesPage({ notes: [{ slug: 'a', summary: 'Plain :x: text' }] });
    expect(plain.notes[0]?.summaryMarkup).toEqual([{ kind: 'text', text: 'Plain :x: text' }]);
  });

  it('skips an update without a name', () => {
    expect(
      parsePatchNotesPage({ notes: [{ slug: '' }, { title: 'x' }, { slug: 'a' }] }).notes.map((n) => n.slug),
    ).toEqual(['a']);
  });

  it('reads a damaged page as an empty one', () => {
    expect(parsePatchNotesPage(null)).toEqual({
      total: 0,
      start: 0,
      count: 5,
      tag: null,
      all: 0,
      filters: [],
      notes: [],
    });
  });

  it('reads the months, newest first', () => {
    expect(parsePatchNotesMonths(JSON.parse(readServerResponse('patch-notes-months.json')))).toEqual([
      { month: '2026-11', first: '2026-11-18', count: 1 },
      { month: '2026-09', first: '2026-09-30', count: 4 },
      { month: '2023-12', first: 'announcement-12', count: 2 },
    ]);
    expect(parsePatchNotesMonths({ months: [{ month: '2026-01' }] })).toEqual([]);
  });

  it("reads a link's update name in lower case", () => {
    expect(parsePatchNotesSlug('2026-09-27-Hall-Overhaul')).toBe('2026-09-27-hall-overhaul');
    expect(parsePatchNotesSlug('')).toBeNull();
    expect(parsePatchNotesSlug(null)).toBeNull();
    expect(parsePatchNotesSlug('../x')).toBeNull();
  });
});
