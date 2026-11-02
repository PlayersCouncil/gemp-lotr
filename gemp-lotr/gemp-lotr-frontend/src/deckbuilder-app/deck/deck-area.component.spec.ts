import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { flushLegacyCardFiles } from '../../testing/legacy-cards';
import { LegacyCardDataService } from '../core/cards/legacy-card-data.service';
import { DeckService } from '../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../core/session/session.service';
import { PageLayoutService } from '../layout/page-layout.service';
import { DeckAreaComponent } from './deck-area.component';

@Component({
  imports: [DeckAreaComponent],
  template: `<div id="deckDiv" gempDeckArea></div>`,
})
class Host {}

describe('DeckAreaComponent', () => {
  let deck: DeckService;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    const loading = TestBed.inject(LegacyCardDataService).load();
    flushLegacyCardFiles(TestBed.inject(HttpTestingController));
    await loading;
    const layout = TestBed.inject(PageLayoutService);
    layout.viewportWidth.set(1280);
    layout.viewportHeight.set(800);
    deck = TestBed.inject(DeckService);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el };
  }

  it('lays the cards out in their slots and piles', async () => {
    deck.setupDeck(
      {
        targetFormat: null,
        notes: '',
        ringBearer: '1_290',
        ring: '1_2',
        map: null,
        sites: ['1_319', '1_327'],
        drawDeck: [
          { blueprintId: '1_12', side: 'FREE_PEOPLE' },
          { blueprintId: '1_12', side: 'FREE_PEOPLE' },
          { blueprintId: '1_131', side: 'SHADOW' },
        ],
      },
      'Mine',
    );
    const { el } = await render();
    const rb = el.querySelector<HTMLElement>('#ringBearerDiv .card')!;
    expect(rb.className).toBe('card cardInDeck');
    expect(rb.querySelector('.tokenOverlay')).toBeNull();
    // One card in the 104x138 area of the slot, as tall as fits
    expect([rb.style.left, rb.style.top, rb.style.width, rb.style.height]).toEqual(['3px', '3px', '98px', '137px']);

    const sites = el.querySelectorAll<HTMLElement>('#sitesDiv .card');
    expect(Array.from(sites).map((s) => s.style.top)).toEqual(['23px', '184px']);
    expect(Array.from(sites).map((s) => s.style.zIndex)).toEqual(['10', '11']);

    const draw = Array.from(el.querySelectorAll<HTMLElement>('#decksRegion .card'));
    // The extra copy is drawn behind the shown card (lower z-index), and can't be clicked away by itself
    expect(draw.map((c) => [c.className, c.style.zIndex])).toEqual([
      ['card', '10'],
      ['card cardInDeck', '11'],
      ['card cardInDeck', '10'],
    ]);
    // The Shadow pile is the lower half of the deck region
    expect(parseInt(draw[2].style.top)).toBeGreaterThan(345);
  });

  it('shows the deck name, orange with a "*" when modified', async () => {
    const { fixture, el } = await render();
    const label = el.querySelector('#editingDeck')!;
    expect(label.textContent).toBe('New deck');
    expect(label.querySelector('font')).toBeNull();
    deck.addFromCollection('1_12', 'FREE_PEOPLE');
    await fixture.whenStable();
    expect(label.querySelector('font')!.getAttribute('color')).toBe('orange');
    expect(label.textContent).toBe('*New deck - modified');
  });

  it('shows the server stats HTML as-is', async () => {
    deck.statsHtml.set("<b>Free People</b>: 0<br/><font color='red'>Deck doesn't have a Ring-bearer</font>");
    const { el } = await render();
    expect(el.querySelector('#deckStats')!.innerHTML).toBe(
      '<b>Free People</b>: 0<br><font color="red">Deck doesn\'t have a Ring-bearer</font>',
    );
  });

  it('hides the Map and Race slots outside their formats', async () => {
    const { fixture, el } = await render();
    expect(el.querySelector<HTMLElement>('#map-div')!.style.display).toBe('none');
    TestBed.inject(PageLayoutService).showMap.set(true);
    await fixture.whenStable();
    expect(el.querySelector<HTMLElement>('#map-div')!.style.display).toBe('');
  });
});
