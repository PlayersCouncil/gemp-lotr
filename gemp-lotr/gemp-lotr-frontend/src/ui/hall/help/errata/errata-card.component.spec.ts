import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ErrataCardComponent } from './errata-card.component';

@Component({
  imports: [ErrataCardComponent],
  template: `<gd-errata-card
    side="before"
    [url]="url()"
    alt="Original"
    missing="No image"
    (sized)="sized = sized + 1"
  />`,
})
class Host {
  readonly url = signal<string | null>('a.jpg');
  sized = 0;
}

describe('ErrataCardComponent', () => {
  it('draws the image in its border, and reads its shape once it loads', () => {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    const card = fixture.nativeElement.querySelector('gd-errata-card') as HTMLElement;
    const image = card.querySelector('img.gd-errata-card__image') as HTMLImageElement;
    expect([image.getAttribute('src'), image.alt, image.getAttribute('draggable')]).toEqual([
      'a.jpg',
      'Original',
      'false',
    ]);
    expect(card.querySelector('.gd-errata-card__border')).not.toBeNull();
    Object.defineProperty(image, 'naturalWidth', { value: 357 });
    Object.defineProperty(image, 'naturalHeight', { value: 497 });
    image.dispatchEvent(new Event('load'));
    expect(card.dataset['aspect']).toBe(String(357 / 497));
    expect(card.dataset['naturalHeight']).toBe('497');
    expect(fixture.componentInstance.sized).toBe(1);
  });

  it('says so without an image, or when it fails, until another comes', () => {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    const card = fixture.nativeElement.querySelector('gd-errata-card') as HTMLElement;
    card.querySelector('img')?.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    expect(card.hasAttribute('data-missing')).toBe(true);
    expect(card.textContent?.trim()).toBe('No image');
    fixture.componentInstance.url.set('b.jpg');
    fixture.detectChanges();
    expect(card.querySelector('img')?.getAttribute('src')).toBe('b.jpg');
    fixture.componentInstance.url.set(null);
    fixture.detectChanges();
    expect(card.textContent?.trim()).toBe('No image');
  });
});
