import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { parseErrataCatalog } from '../../../../core/hall/help/errata/errata-catalog';
import { errataReadout } from '../../../../core/hall/help/errata/errata-diff';
import { ErrataEntry } from '../../../../core/hall/help/errata/errata-models';
import { readServerResponse } from '../../../../testing/files';
import { ErrataFrameComponent, ErrataPreviewRequest } from './errata-frame.component';

const { entries } = parseErrataCatalog(JSON.parse(readServerResponse('errata.json')));

@Component({
  imports: [ErrataFrameComponent],
  template: `<gd-errata-frame
    [entry]="entry()"
    [images]="{ before: before(), after: 'after.jpg' }"
    [readout]="readout()"
    [full]="full()"
    [showOriginal]="original()"
    (preview)="previews.push($event)"
  />`,
})
class Host {
  readonly entry = signal<ErrataEntry>(entries[0] as ErrataEntry);
  readonly before = signal<string | null>('before.jpg');
  readonly full = signal(false);
  readonly original = signal(false);
  readonly readout = signal(errataReadout(entries[0] as ErrataEntry));
  readonly previews: ErrataPreviewRequest[] = [];
  show(entry: ErrataEntry): void {
    this.entry.set(entry);
    this.readout.set(errataReadout(entry));
  }
}

describe('ErrataFrameComponent', () => {
  function render() {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    return { fixture, frame: fixture.nativeElement.querySelector('gd-errata-frame') as HTMLElement };
  }
  const texts = (root: Element, selector: string) =>
    Array.from(root.querySelectorAll(selector)).map((element) => element.textContent?.trim().replace(/\s+/g, ' '));

  it('heads the card with its name, number, culture and type, and its badges (head)', () => {
    const { frame } = render();
    expect(frame.dataset['id']).toBe('51_45');
    expect(frame.dataset['layout']).toBe('compact');
    const head = frame.querySelector('.gd-errata-frame__stage .gd-errata-frame__head') as HTMLElement;
    expect(texts(head, 'span')).toEqual([
      'Gandalf, The Grey Wizard',
      '1R45 · Gandalf Companion',
      'New',
      'PC revision 1',
    ]);
    expect(head.querySelector('.gd-errata-frame__meta')?.getAttribute('title')).toBe(
      'The Fellowship of the Ring (set 1), card 45',
    );
    expect(head.querySelector('[data-badge=new]')?.getAttribute('title')).toBe('In the latest batch of errata');
  });

  it('reads out what changed: the stats, then Old and New with the additions marked (note)', () => {
    const { frame } = render();
    expect(texts(frame, '.gd-errata-frame__stats li')).toEqual(['Strength7→6']);
    expect(frame.querySelector('.gd-errata-frame__stat-arrow')?.getAttribute('aria-label')).toBe('to');
    expect(texts(frame, '.gd-errata-frame__texts .gd-errata-frame__side')).toEqual([
      'OldWise. Fellowship: Exert Gandalf twice to play a [gandalf] card from your draw deck.',
      'NewWise. Fellowship: Exert Gandalf to play a [gandalf] card from your discard pile.',
    ]);
    expect(texts(frame, '[data-side=new] ins')).toEqual(['discard pile']);
    expect(frame.querySelector('[data-side=new] .gd-errata-frame__gametext')?.textContent).toBe(
      'NewWise. Fellowship: Exert Gandalf to play a [gandalf] card from your discard pile.',
    );
  });

  it('puts the hint before an unchanged text', () => {
    const { fixture, frame } = render();
    fixture.componentInstance.show(entries[3] as ErrataEntry);
    fixture.detectChanges();
    const note = frame.querySelector('.gd-errata-frame__note') as HTMLElement;
    expect(Array.from(note.children).map((child) => child.className)).toEqual([
      'gd-errata-frame__head',
      'gd-errata-frame__hint',
      'gd-errata-frame__texts',
      'gd-errata-frame__more',
    ]);
    expect(texts(note, '[data-side=same]')).toEqual(['Game text (unchanged)Underground. Each minion is strength +1.']);
  });

  it('shows the side asked for, with "No image" for a missing one', () => {
    const { fixture, frame } = render();
    const box = frame.querySelector('.gd-errata-frame__cardbox') as HTMLElement;
    expect(box.dataset['showing']).toBe('after');
    expect(box.title).toBe('Click (or press O) to see the other version; right-click to zoom');
    fixture.componentInstance.original.set(true);
    fixture.componentInstance.before.set(null);
    fixture.detectChanges();
    expect(box.dataset['showing']).toBe('before');
    expect(texts(frame, 'gd-errata-card[data-side=before]')).toEqual(['No image of the original']);
    expect(frame.querySelector('gd-errata-card[data-side=after] img')?.getAttribute('alt')).toBe('PC errata');
  });

  it('asks for the card preview on a right-click, with its side', () => {
    const { fixture, frame } = render();
    const card = frame.querySelector('gd-errata-card[data-side=after]') as HTMLElement;
    card.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true }));
    expect(fixture.componentInstance.previews.map((request) => request.side)).toEqual(['after']);
  });

  it('enlarged, shows both cards captioned around the arrow, the head above', () => {
    const { fixture, frame } = render();
    fixture.componentInstance.full.set(true);
    fixture.detectChanges();
    expect(frame.dataset['layout']).toBe('full');
    expect(frame.firstElementChild?.className).toBe('gd-errata-frame__head');
    expect(texts(frame, 'figcaption')).toEqual(['Original', 'PC errata']);
    expect(texts(frame, '.gd-errata-frame__arrow span')).toEqual(['→', '↓']);
    expect(frame.querySelector('.gd-errata-frame__figure .gd-errata-frame__cardbox')?.getAttribute('title')).toBe(
      'Right-click to zoom',
    );
  });
});
