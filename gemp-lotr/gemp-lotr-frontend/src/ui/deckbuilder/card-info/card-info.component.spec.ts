import { TestBed } from '@angular/core/testing';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardRef } from '../../../core/cards/card-ref';
import { CardInfoComponent, ZOOM_COOKIE } from './card-info.component';

const CARD: CardRef = { kind: 'shown', blueprintId: '1_2', zone: 'COLLECTION', role: '' };
const META_SITE_MODIFIER: CardRef = { kind: 'shown', blueprintId: '90_1', zone: 'COLLECTION', role: '' };

describe('CardInfoComponent', () => {
  let info: CardInfoService;
  let cookies: string[];
  let spies: { mockRestore(): void }[];

  beforeEach(() => {
    cookies = [];
    spies = [
      vi.spyOn(document, 'cookie', 'get').mockReturnValue(`${ZOOM_COOKIE}=0.7`),
      vi.spyOn(document, 'cookie', 'set').mockImplementation((cookie: string) => void cookies.push(cookie)),
    ];
    info = TestBed.inject(CardInfoService);
  });

  afterEach(() => spies.forEach((spy) => spy.mockRestore()));

  async function render() {
    const fixture = TestBed.createComponent(CardInfoComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const dialog = el.querySelector<HTMLDialogElement>('dialog.gd-card-info')!;
    const click = async (selector: string) => {
      (el.querySelector(selector) as HTMLElement).dispatchEvent(new MouseEvent('click', { bubbles: true }));
      await fixture.whenStable();
    };
    return { fixture, el, dialog, click };
  }

  it('is a closed native dialog until a card is shown', async () => {
    const { fixture, dialog } = await render();
    expect(dialog.open).toBe(false);
    expect(dialog.children.length).toBe(0);
    info.show(CARD);
    await fixture.whenStable();
    expect(dialog.open).toBe(true);
    info.close();
    await fixture.whenStable();
    expect(dialog.open).toBe(false);
  });

  it('shows the card, its wiki link and the size slider (DOM contract)', async () => {
    const { fixture, dialog } = await render();
    info.show(CARD);
    await fixture.whenStable();
    expect(dialog.querySelector('.gd-card-info__header .gd-card-info__title')!.textContent).toBe('Card Information');
    expect(dialog.querySelector('button.gd-card-info__close')!.getAttribute('aria-label')).toBe('Close');
    const zoom = dialog.querySelector<HTMLInputElement>('input[type="range"].gd-card-info__zoom')!;
    expect([zoom.min, zoom.max, zoom.step, zoom.value]).toEqual(['0.4', '1', '0.1', '0.7']);
    expect(dialog.style.getPropertyValue('--gd-card-info-zoom')).toBe('0.7');
    const card = dialog.querySelector<HTMLElement>('.gd-card-info__card')!;
    expect(card.dataset['orientation']).toBe('vertical');
    expect(card.hasAttribute('data-flippable')).toBe(true);
    expect(card.querySelector<HTMLImageElement>('img.gd-card-info__image')!.src).toBe(
      'https://i.lotrtcgpc.net/decipher/LOTR01002.jpg',
    );
    expect(dialog.querySelector<HTMLAnchorElement>('a.gd-card-info__wiki')!.href).toBe(
      'http://wiki.lotrtcgpc.net/wiki/LOTR01002',
    );
  });

  it('flips the card when it is clicked, and starts a new card face up', async () => {
    const { fixture, dialog, click } = await render();
    info.show(CARD);
    await fixture.whenStable();
    await click('.gd-card-info__image');
    const card = dialog.querySelector<HTMLElement>('.gd-card-info__card')!;
    expect(card.dataset['flipped']).toBe('back');
    expect(card.querySelector('img')!.src).toBe('https://i.lotrtcgpc.net/decipher/LOTR00000.jpg');
    expect(info.isOpen()).toBe(true);
    info.show({ ...CARD, blueprintId: '1_3' });
    await fixture.whenStable();
    expect(dialog.querySelector('.gd-card-info__card')!.hasAttribute('data-flipped')).toBe(false);
  });

  it("closes on a click anywhere else, but not on the link or the slider, nor a meta-site modifier's card", async () => {
    const { fixture, click } = await render();
    info.show(META_SITE_MODIFIER);
    await fixture.whenStable();
    await click('.gd-card-info__zoom');
    expect(info.isOpen()).toBe(true);
    await click('.gd-card-info__card');
    expect(info.isOpen()).toBe(false);
    info.show(CARD);
    await fixture.whenStable();
    await click('dialog');
    expect(info.isOpen()).toBe(false);
  });

  it('closes with the close button and with Escape (the dialog closing)', async () => {
    const { fixture, dialog, click } = await render();
    info.show(CARD);
    await fixture.whenStable();
    await click('.gd-card-info__close');
    expect(info.isOpen()).toBe(false);
    info.show(CARD);
    await fixture.whenStable();
    dialog.dispatchEvent(new Event('close'));
    expect(info.isOpen()).toBe(false);
  });

  it('remembers the size, and a click ending a slide does not close it', async () => {
    const { fixture, dialog, click } = await render();
    info.show(CARD);
    await fixture.whenStable();
    const zoom = dialog.querySelector<HTMLInputElement>('.gd-card-info__zoom')!;
    zoom.dispatchEvent(new Event('pointerdown'));
    zoom.value = '0.9';
    zoom.dispatchEvent(new Event('input'));
    zoom.dispatchEvent(new Event('pointerup'));
    // The release ended outside the slider: the click lands on the dialog
    await click('dialog');
    expect(info.isOpen()).toBe(true);
    expect(cookies[0]).toMatch(new RegExp(`^${ZOOM_COOKIE}=0\\.9; expires=.*; path=/gemp-lotr$`));
    expect(dialog.style.getPropertyValue('--gd-card-info-zoom')).toBe('0.9');
    // The slide ends a moment later
    await new Promise((resolve) => setTimeout(resolve, 5));
    await click('dialog');
    expect(info.isOpen()).toBe(false);
  });
});
