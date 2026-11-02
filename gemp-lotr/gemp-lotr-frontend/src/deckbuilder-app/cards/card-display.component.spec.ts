import { Component, signal, viewChild } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardDisplayComponent, CardDisplayData, cardDisplaySize } from './card-display.component';

const CARD: CardDisplayData = {
  image: 'https://i.lotrtcgpc.net/decipher/LOTR02016.jpg',
  back: 'https://i.lotrtcgpc.net/decipher/LOTR00000.jpg',
  horizontal: false,
  foil: false,
  overlayImage: null,
};

@Component({
  imports: [CardDisplayComponent],
  template: `<gemp-card-display [data]="data()" [maxWidth]="519.5" [maxHeight]="519.5" />`,
})
class Host {
  readonly data = signal<CardDisplayData | null>(CARD);
  readonly display = viewChild.required(CardDisplayComponent);
}

describe('CardDisplayComponent', () => {
  it('sizes the card like CardDisplay.resize (values measured on the legacy card info dialog)', () => {
    expect(cardDisplaySize(false, 519.5, 519.5)).toEqual({
      width: 372,
      height: 519,
      border: 18,
      testBorderWidth: 21,
      testBorderHeight: 18,
    });
    // Landscape cards use the long side for their width; never above the remastered size
    expect(cardDisplaySize(true, 2000, 2000)).toEqual(expect.objectContaining({ width: 1039, height: 745 }));
  });

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const base = (fixture.nativeElement as HTMLElement).querySelector('gemp-card-display > div') as HTMLElement;
    return { fixture, base };
  }

  it('builds the legacy layers', async () => {
    const { base } = await render();
    expect([base.style.position, base.style.display, base.style.width, base.style.height]).toEqual([
      'relative',
      'inline-block',
      '372px',
      '519px',
    ]);
    expect(Array.from(base.children).map((c) => c.className)).toEqual([
      'fullcard',
      'metaSiteOverlay',
      'foilOverlay',
      'testingTextOverlay',
      'borderOverlay',
    ]);
    const img = base.querySelector<HTMLImageElement>('.fullcard img')!;
    expect([img.getAttribute('width'), img.getAttribute('height'), img.src]).toEqual(['372', '519', CARD.image]);
    const border = base.querySelector<HTMLElement>('.borderOverlay')!;
    expect([border.style.borderWidth, border.style.width, border.style.height]).toEqual(['18px', '338px', '485px']);
  });

  it('flips to the back when clicked, and starts face up for the next card', async () => {
    const { fixture, base } = await render();
    base.click();
    await fixture.whenStable();
    expect(base.querySelector<HTMLImageElement>('.fullcard img')!.src).toBe(CARD.back);
    fixture.componentInstance.data.set({ ...CARD, image: 'next.jpg' });
    await fixture.whenStable();
    expect(base.querySelector('.fullcard img')!.getAttribute('src')).toBe('next.jpg');
  });

  it('turns a card without a back upside down, except when only flipping cards with a back', async () => {
    const { fixture, base } = await render();
    fixture.componentInstance.data.set({ ...CARD, back: null });
    await fixture.whenStable();
    fixture.componentInstance.display().flip(true);
    await fixture.whenStable();
    expect(base.querySelector<HTMLImageElement>('.fullcard img')!.style.transform).toBe('rotate(0deg)');
    fixture.componentInstance.display().flip();
    await fixture.whenStable();
    expect(base.querySelector<HTMLImageElement>('.fullcard img')!.style.transform).toBe('rotate(180deg)');
  });

  it('is hidden without a card', async () => {
    const { fixture, base } = await render();
    fixture.componentInstance.data.set(null);
    await fixture.whenStable();
    expect(base.style.display).toBe('none');
  });
});
