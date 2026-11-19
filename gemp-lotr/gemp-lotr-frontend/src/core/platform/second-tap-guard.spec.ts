import { TestBed } from '@angular/core/testing';
import { PAGE_QUERY_STRING } from './page-location';
import { DOUBLE_TAP_MS, SecondTapGuard } from './second-tap-guard';
import { ThemeService } from './theme.service';

describe('SecondTapGuard', () => {
  let page: HTMLElement;
  let pressed: string[];

  /** A page with two buttons and a card, and the guard started in the theme. */
  function start(theme: 'classic' | 'modern') {
    TestBed.configureTestingModule({ providers: [{ provide: PAGE_QUERY_STRING, useValue: `?theme=${theme}` }] });
    vi.spyOn(document, 'cookie', 'set').mockImplementation(() => undefined);
    TestBed.inject(ThemeService).init();
    TestBed.inject(SecondTapGuard).start();
    page = document.createElement('div');
    page.innerHTML = `<button id="menu-item"><span>Rename</span></button><button id="slot"></button>
      <div class="gd-card" id="card"><img alt=""></div>
      <div class="gd-card" id="pack" data-tap-once><img alt=""></div>
      <label id="culture"><input type="checkbox" id="culture-box"> Elven</label>`;
    document.body.append(page);
    pressed = [];
    page.addEventListener('click', (event) => pressed.push((event.target as Element).closest('[id]')!.id));
  }

  afterEach(() => {
    page.remove();
    TestBed.resetTestingModule();
    vi.restoreAllMocks();
  });

  /** A click on `id`'s element (or the one inside it), by `pointerType`, at x, y, `time` ms into the page's life. */
  function click(selector: string, pointerType: string, time: number, x = 10, y = 10) {
    const event = new MouseEvent('click', { bubbles: true, cancelable: true, clientX: x, clientY: y });
    Object.defineProperty(event, 'pointerType', { value: pointerType });
    Object.defineProperty(event, 'timeStamp', { value: time });
    page.querySelector(selector)!.dispatchEvent(event);
    return event;
  }

  it('drops the second tap of a double tap that lands on another control than the first', () => {
    start('modern');
    click('#menu-item span', 'touch', 1000);
    const second = click('#slot', 'touch', 1120);
    expect(pressed).toEqual(['menu-item']);
    expect(second.defaultPrevented).toBe(true);
  });

  it('lets a tap on a label through, and the click it passes on to the box in it (one control)', () => {
    start('modern');
    click('#culture', 'touch', 1000);
    // As Chrome passes it on: the same tap, at the same moment and place
    const passedOn = click('#culture-box', 'touch', 1000);
    expect(passedOn.defaultPrevented).toBe(false);
    expect(pressed.filter((id) => id === 'culture-box').length).toBeGreaterThan(0);
  });

  it('lets a double tap on one control press it twice, from any part of it', () => {
    start('modern');
    click('#card img', 'touch', 1000);
    click('#card', 'touch', 1120);
    expect(pressed).toEqual(['card', 'card']);
  });

  it('lets a double or triple tap on a control marked data-tap-once (a pack) press it once', () => {
    start('modern');
    click('#pack img', 'touch', 1000);
    click('#pack', 'touch', 1120);
    click('#pack img', 'touch', 1240);
    click('#pack', 'touch', 1240 + DOUBLE_TAP_MS + 1);
    expect(pressed).toEqual(['pack', 'pack']);
  });

  it('lets a tap on another control through once the double tap is over, or far from the first', () => {
    start('modern');
    click('#menu-item', 'touch', 1000);
    click('#slot', 'touch', 1000 + DOUBLE_TAP_MS + 1);
    click('#menu-item', 'touch', 3000, 10, 10);
    click('#slot', 'touch', 3100, 100, 10);
    expect(pressed).toEqual(['menu-item', 'slot', 'menu-item', 'slot']);
  });

  it('leaves the mouse alone, and the classic theme (as the legacy page)', () => {
    start('modern');
    click('#menu-item', 'mouse', 1000);
    click('#slot', 'mouse', 1120);
    expect(pressed).toEqual(['menu-item', 'slot']);
    page.remove();
    TestBed.resetTestingModule();
    start('classic');
    click('#menu-item', 'touch', 1000);
    click('#slot', 'touch', 1120);
    expect(pressed).toEqual(['menu-item', 'slot']);
  });
});
