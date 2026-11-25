import { TestBed } from '@angular/core/testing';
import { ClipboardWriter } from '../platform/clipboard.service';
import { PAGE_ORIGIN } from '../platform/page-location';
import { COPIED_NOTE_MS, MANUAL_NOTE_MS, ShareLinks, shareUrl } from './share-links';

describe('share links', () => {
  it('builds the share addresses (GempShareLinks.url)', () => {
    const origin = 'https://play.lotrtcgpc.net';
    expect(shareUrl(origin, 'patch-notes', '2026-09-27-hall-overhaul')).toBe(
      'https://play.lotrtcgpc.net/gemp-lotr/share/patch-notes/2026-09-27-hall-overhaul',
    );
    expect(shareUrl(origin, 'format', 'pc fotr')).toBe('https://play.lotrtcgpc.net/gemp-lotr/share/format/pc%20fotr');
    expect(shareUrl(origin, 'patch-notes', null)).toBe('https://play.lotrtcgpc.net/gemp-lotr/share/patch-notes');
  });

  describe('ShareLinks', () => {
    let copied: string[];
    let works: boolean;
    let links: ShareLinks;

    beforeEach(() => {
      vi.useFakeTimers();
      copied = [];
      works = true;
      TestBed.configureTestingModule({
        providers: [
          ShareLinks,
          { provide: PAGE_ORIGIN, useValue: 'http://localhost' },
          {
            provide: ClipboardWriter,
            useValue: {
              copy: (text: string) => {
                copied.push(text);
                return Promise.resolve(works);
              },
            },
          },
        ],
      });
      links = TestBed.inject(ShareLinks);
    });

    afterEach(() => vi.useRealTimers());

    it('copies the link and says so for two seconds', async () => {
      await links.copy('format', 'pc_movie');
      expect(copied).toEqual(['http://localhost/gemp-lotr/share/format/pc_movie']);
      expect(links.noteOf('format', 'pc_movie')).toEqual({
        kind: 'copied',
        url: 'http://localhost/gemp-lotr/share/format/pc_movie',
      });
      expect(links.noteOf('format', 'other')).toBeNull();
      vi.advanceTimersByTime(COPIED_NOTE_MS - 1);
      expect(links.noteOf('format', 'pc_movie')).not.toBeNull();
      vi.advanceTimersByTime(1);
      expect(links.noteOf('format', 'pc_movie')).toBeNull();
    });

    it('shows the link to copy by hand for fifteen seconds when copying fails; a new copy starts the time again', async () => {
      works = false;
      await links.copy('patch-notes', '2026-11-18');
      expect(links.noteOf('patch-notes', '2026-11-18')?.kind).toBe('manual');
      vi.advanceTimersByTime(MANUAL_NOTE_MS - 10);
      works = true;
      await links.copy('patch-notes', '2026-11-18');
      vi.advanceTimersByTime(COPIED_NOTE_MS - 1);
      expect(links.noteOf('patch-notes', '2026-11-18')?.kind).toBe('copied');
      vi.advanceTimersByTime(1);
      expect(links.notes().size).toBe(0);
    });

    it('does nothing without an id', async () => {
      await links.copy('errata', '');
      expect(copied).toEqual([]);
    });
  });
});
