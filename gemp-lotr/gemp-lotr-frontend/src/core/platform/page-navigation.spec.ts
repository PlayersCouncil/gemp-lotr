import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { PageNavigation } from './page-navigation';

/** A window as far as PageNavigation uses it, recording what it was asked to do. */
class FakeWindow {
  readonly done: string[] = [];
  opened: FakeTab | null = new FakeTab();
  openThrows = false;
  readonly location = {
    assign: (url: string) => this.done.push(`assign ${url}`),
    reload: () => this.done.push('reload'),
  };
  open(url: string, target: string): FakeTab | null {
    this.done.push(`open ${url || '(blank)'} ${target}`);
    if (this.openThrows) throw new Error('blocked');
    return this.opened;
  }
}

class FakeTab {
  closed = false;
  focused = 0;
  focusThrows = false;
  closeThrows = false;
  readonly location = { href: '' };
  readonly document = { title: '', body: { style: { background: '' } } };
  focus(): void {
    if (this.focusThrows) throw new Error('no focus');
    this.focused++;
  }
  close(): void {
    if (this.closeThrows) throw new Error('gone');
    this.closed = true;
  }
}

describe('PageNavigation', () => {
  let window: FakeWindow;

  function navigation(view: FakeWindow | null = window): PageNavigation {
    TestBed.overrideProvider(DOCUMENT, { useValue: { defaultView: view } });
    return TestBed.inject(PageNavigation);
  }

  beforeEach(() => {
    window = new FakeWindow();
  });

  it('goes to another page in this tab, and loads this one again', () => {
    const pages = navigation();
    pages.go('/gemp-lotr/ng/hall');
    pages.reload();
    expect(window.done).toEqual(['assign /gemp-lotr/ng/hall', 'reload']);
  });

  it('opens a page in a new tab and focuses it (openOrOffer)', () => {
    expect(navigation().openInNewTab('/gemp-lotr/ng/game?gameId=7')).toBe(true);
    expect(window.done).toEqual(['open /gemp-lotr/ng/game?gameId=7 _blank']);
    expect(window.opened?.focused).toBe(1);
  });

  it('says so when the browser blocks the new tab, whichever way it does', () => {
    const pages = navigation();
    window.opened = null;
    expect(pages.openInNewTab('/a')).toBe(false);
    window.opened = new FakeTab();
    window.opened.closed = true;
    expect(pages.openInNewTab('/a')).toBe(false);
    window.openThrows = true;
    expect(pages.openInNewTab('/a')).toBe(false);
  });

  it('opens the tab even when it cannot be focused', () => {
    window.opened!.focusThrows = true;
    expect(navigation().openInNewTab('/a')).toBe(true);
  });

  it('does nothing without a window', () => {
    const pages = navigation(null);
    expect(() => pages.go('/a')).not.toThrow();
    expect(pages.openInNewTab('/a')).toBe(false);
    expect(pages.reserveTab()).toBeNull();
  });

  it('reserves a blank, dark tab at once and points it at a page later (PlayScenario.reserveWindow)', () => {
    const tab = navigation().reserveTab();
    const opened = window.opened!;
    expect(window.done).toEqual(['open (blank) _blank']);
    expect(opened.document.title).toBe('Gemp-LotR');
    expect(opened.document.body.style.background).toBe('#111');
    expect(tab?.go('/gemp-lotr/ng/game?gameId=8')).toBe(true);
    expect(opened.location.href).toBe('/gemp-lotr/ng/game?gameId=8');
    expect(opened.focused).toBe(1);
    tab?.close();
    expect(opened.closed).toBe(true);
    // the player closed it meanwhile
    expect(tab?.go('/elsewhere')).toBe(false);
    expect(opened.location.href).toBe('/gemp-lotr/ng/game?gameId=8');
  });

  it('gives no reserved tab when the browser refuses one, and keeps one it cannot dress, focus or close', () => {
    const pages = navigation();
    window.opened = null;
    expect(pages.reserveTab()).toBeNull();
    window.opened = new FakeTab();
    window.opened.closed = true;
    expect(pages.reserveTab()).toBeNull();
    window.openThrows = true;
    expect(pages.reserveTab()).toBeNull();
    window.openThrows = false;
    const stubborn = new FakeTab();
    Object.defineProperty(stubborn, 'document', {
      get: () => {
        throw new Error('cross-origin');
      },
    });
    stubborn.focusThrows = true;
    stubborn.closeThrows = true;
    window.opened = stubborn;
    const tab = pages.reserveTab();
    expect(tab?.go('/a')).toBe(true);
    expect(() => tab?.close()).not.toThrow();
  });
});
