import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardComponent, CardFace } from './card.component';
import { CardRect } from './card-group-layout';

const FACE: CardFace = {
  imageUrl: 'https://i.lotrtcgpc.net/decipher/LOTR02016.jpg',
  errata: false,
  foilImage: null,
  tokens: true,
  noBorder: false,
  count: 4,
};

@Component({
  imports: [CardComponent],
  template: `<div [gempCard]="face()" [rect]="rect()" class="cardInCollection"></div>`,
})
class Host {
  readonly face = signal(FACE);
  readonly rect = signal<CardRect>({ left: 3, top: 3, width: 86, height: 120, zIndex: 10 });
}

describe('CardComponent', () => {
  async function render(face: Partial<CardFace> = {}) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.face.set({ ...FACE, ...face });
    await fixture.whenStable();
    return (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.card')!;
  }

  /** Element children without Angular's comment anchors. */
  const children = (card: HTMLElement) => Array.from(card.children).map((c) => c.className || c.tagName);

  it('builds the legacy card markup, sized like layoutCardElem', async () => {
    const card = await render();
    expect(card.className).toBe('card cardInCollection');
    expect(card.style.cssText).toBe(
      'height: 120px; left: 3px; position: absolute; top: 3px; width: 86px; z-index: 10;',
    );
    expect(children(card)).toEqual(['IMG', 'tokenOverlay', 'borderOverlay actionArea', 'cardPileCount']);
    expect(card.querySelector('img')!.getAttribute('src')).toBe(FACE.imageUrl);

    const border = card.querySelector<HTMLElement>('.borderOverlay')!;
    expect(border.style.width).toBe('78px');
    expect(border.style.height).toBe('112px');
    expect(border.style.borderWidth).toBe('4px');

    const count = card.querySelector<HTMLElement>('.cardCount.token')!;
    expect(count.textContent).toBe('4');
    expect(count.style.left).toBe('33px');
    expect(count.style.top).toBe('51px');
  });

  it('adds the errata and foil layers, and drops the border for packs', async () => {
    const card = await render({
      errata: true,
      foilImage: '/gemp-lotr/images/holo.jpg',
      noBorder: true,
    });
    expect(children(card)).toEqual([
      'IMG',
      'errataOverlay',
      'foilOverlay',
      'tokenOverlay',
      'borderOverlay actionArea noBorder',
      'cardPileCount',
    ]);
    const border = card.querySelector<HTMLElement>('.borderOverlay')!;
    expect(border.style.borderWidth).toBe('0px');
    expect(border.style.width).toBe('86px');
  });

  it('can have no token layer', async () => {
    const card = await render({ tokens: false });
    expect(card.querySelector('.tokenOverlay')).toBeNull();
  });
});
