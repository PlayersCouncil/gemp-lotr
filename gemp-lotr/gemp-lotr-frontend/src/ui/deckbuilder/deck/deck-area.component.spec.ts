import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CollectionService } from '../../../core/collection/collection.service';
import { parseDeckStats } from '../../../core/deck/deck-stats';
import { DeckService } from '../../../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { DeckAreaComponent } from './deck-area.component';

describe('DeckAreaComponent', () => {
  let deck: DeckService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    deck = TestBed.inject(DeckService);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(DeckAreaComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const slot = (name: string) => el.querySelector<HTMLElement>(`.gd-slot[data-slot="${name}"]`)!;
    const pile = (side: string) => el.querySelector<HTMLElement>(`.gd-pile[data-side="${side}"]`)!;
    return { fixture, el, slot, pile };
  }

  function setUp() {
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
          { blueprintId: '1_13', side: 'FREE_PEOPLE' },
          { blueprintId: '1_12', side: 'FREE_PEOPLE' },
          { blueprintId: '1_12', side: 'FREE_PEOPLE' },
          { blueprintId: '1_131', side: 'SHADOW' },
        ],
      },
      'Mine',
    );
  }

  it('has the toolbar, the slots, the piles and the stats (DOM contract)', async () => {
    const { el } = await render();
    expect(el.classList).toContain('gd-deck');
    expect(Array.from(el.children).map((child) => child.className)).toEqual([
      'gd-toolbar',
      'gd-slot',
      'gd-slot',
      'gd-slot',
      'gd-slot',
      'gd-slot',
      'gd-pile',
      'gd-pile',
      'gd-stats',
    ]);
    expect(Array.from(el.querySelectorAll<HTMLElement>('.gd-slot')).map((s) => s.dataset['slot'])).toEqual([
      'ring-bearer',
      'ring',
      'map',
      'race',
      'sites',
    ]);
    expect(Array.from(el.querySelectorAll<HTMLElement>('.gd-pile')).map((p) => p.dataset['side'])).toEqual([
      'free-peoples',
      'shadow',
    ]);
    const ringBearer = el.querySelector('.gd-slot[data-slot="ring-bearer"]')!;
    expect(ringBearer.querySelector('button.gd-slot__label > .gd-slot__title')!.textContent).toBe('Ring-bearer');
    expect(ringBearer.querySelector('.gd-slot__hint')!.textContent).toBe('(Click here to select your Ring-bearer.)');
    expect(el.querySelector('.gd-slot[data-slot="race"] div.gd-slot__label')).not.toBeNull();
    expect(el.querySelector('.gd-slot[data-slot="map"] a.gd-slot__link')!.getAttribute('href')).toBe(
      'https://wiki.lotrtcgpc.net/wiki/Map',
    );
  });

  it('shows the Map and Race slots only in their formats', async () => {
    const { fixture, slot } = await render();
    expect(slot('map').hidden).toBe(true);
    expect(slot('race').hidden).toBe(true);
    deck.usesMap.set(true);
    await fixture.whenStable();
    expect(slot('map').hidden).toBe(false);
    expect(slot('ring').hidden).toBe(false);
  });

  it('shows the cards in their slots, the sites and the piles, with the copies behind their card', async () => {
    setUp();
    const { slot, pile } = await render();
    expect(slot('ring-bearer').dataset['count']).toBe('1');
    expect(slot('ring-bearer').querySelector('.gd-slot__cards > button.gd-card[data-role="card"]')).not.toBeNull();
    const sites = slot('sites').querySelector<HTMLElement>('.gd-slot__cards')!;
    expect(sites.style.getPropertyValue('--gd-slot-count')).toBe('2');
    expect(Array.from(sites.querySelectorAll<HTMLElement>('.gd-card')).map((c) => c.dataset['orientation'])).toEqual([
      'horizontal',
      'horizontal',
    ]);
    const free = pile('free-peoples');
    expect(free.dataset['count']).toBe('4');
    const stacks = Array.from(free.querySelectorAll<HTMLElement>('.gd-pile__cards > .gd-stack'));
    expect(stacks.map((stack) => Array.from(stack.children).map((c) => (c as HTMLElement).dataset['role']))).toEqual([
      ['copy', 'copy', 'card'],
      ['card'],
    ]);
    expect(pile('shadow').dataset['count']).toBe('1');
  });

  it('removes a card when it is used, and selects for an empty slot when the slot is clicked', async () => {
    setUp();
    const { fixture, slot, pile } = await render();
    const collection = TestBed.inject(CollectionService);
    const override = vi.spyOn(collection, 'setFilterOverride');

    // The Ring-bearer's own card: removed, without selecting for the slot (it wasn't empty when clicked)
    slot('ring-bearer').querySelector<HTMLButtonElement>('.gd-card')!.click();
    await fixture.whenStable();
    expect(deck.deck().ringBearer).toEqual([]);
    expect(override).not.toHaveBeenCalled();

    slot('ring-bearer').querySelector<HTMLButtonElement>('.gd-slot__label')!.click();
    expect(override).toHaveBeenCalledWith('canStartWithRing:true product:card');

    // One copy of a pile card goes at a time
    pile('free-peoples').querySelector<HTMLButtonElement>('.gd-stack .gd-card[data-role="card"]')!.click();
    await fixture.whenStable();
    expect(pile('free-peoples').dataset['count']).toBe('3');
  });

  it('shows the sites in the collection when the sites or a site is clicked', async () => {
    setUp();
    const { fixture, slot } = await render();
    const collection = TestBed.inject(CollectionService);
    slot('sites').querySelector<HTMLButtonElement>('.gd-card')!.click();
    await fixture.whenStable();
    expect(deck.deck().sites.length).toBe(1);
    expect(collection.fields().cardType).toBe('SITE');
    collection.setField('cardType', '');
    slot('sites').querySelector<HTMLButtonElement>('.gd-slot__label')!.click();
    expect(collection.fields().cardType).toBe('SITE');
  });

  it('renders the stats from data, toned', async () => {
    deck.stats.set(parseDeckStats("<b>Free People</b>: 0<br/><font color='red'>No <script>x</script></font><br/><br/>"));
    const { el } = await render();
    const lines = Array.from(el.querySelectorAll('.gd-stats > .gd-stats__line'));
    expect(lines.map((line) => line.textContent)).toEqual(['Free People: 0', 'No <script>x</script>', '']);
    expect(lines[0].querySelector('strong')!.textContent).toBe('Free People');
    expect(lines[1].querySelector('span')!.dataset['tone']).toBe('error');
    expect(el.querySelector('.gd-stats script')).toBeNull();
  });
});
