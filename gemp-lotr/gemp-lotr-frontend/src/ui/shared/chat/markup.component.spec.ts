import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MarkupNode, parseServerMarkup, RICH_MARKUP } from '../../../core/chat/chat-markup';
import { MarkupComponent } from './markup.component';

/** The markup drawn, without the attributes Angular's style scoping adds to the elements. */
const markupOf = (el: Element) => el.innerHTML.replace(/ _ngcontent-[\w-]+=""/g, '');

@Component({
  imports: [MarkupComponent],
  template: `<gd-markup [nodes]="nodes()" (card)="cards.push($event)" (hallLink)="links.push($event)" />`,
})
class Host {
  readonly nodes = signal<readonly MarkupNode[]>([]);
  readonly cards: string[] = [];
  readonly links: string[] = [];
}

describe('MarkupComponent', () => {
  it('draws the nodes element by element, links opening in a new tab', async () => {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.nodes.set(
      parseServerMarkup('<p>hi <b>@bob</b>, see <a href="https://x.test">x</a><br/>&lt;img src=x&gt;</p>'),
    );
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-markup') as HTMLElement;
    expect(markupOf(el)).toBe(
      '<p>hi <b>@bob</b>, see <a class="gd-markup__link" href="https://x.test" target="_blank" ' +
        'rel="noopener noreferrer">x</a><br>&lt;img src=x&gt;</p>',
    );
    expect(el.querySelector('img')).toBeNull();

    fixture.componentInstance.nodes.set(parseServerMarkup('plain'));
    await fixture.whenStable();
    expect(markupOf(el)).toBe('plain');
  });

  it("draws rich markup's pictures, cards and hall links, and reports the last two when used", async () => {
    const fixture = TestBed.createComponent(Host);
    const nodes: MarkupNode[] = [
      { kind: 'image', src: '/gemp-lotr/images/cultures/gondor.png', alt: 'Gondor', title: 'Gondor', icon: true },
      { kind: 'image', src: '/gemp-lotr/patchnotes/img/x.png', alt: 'A screenshot', title: null, icon: false },
      { kind: 'card', blueprintId: '1_5', title: null, children: [{ kind: 'text', text: 'Gimli' }] },
      { kind: 'hall-link', hash: '#format-pc_movie', children: [{ kind: 'text', text: 'the formats' }] },
      {
        kind: 'element',
        tag: 'ol',
        attributes: { start: '3' },
        children: [{ kind: 'element', tag: 'li', children: [{ kind: 'text', text: 'third' }] }],
      },
    ];
    fixture.componentInstance.nodes.set(nodes);
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-markup') as HTMLElement;

    const [icon, picture] = Array.from(el.querySelectorAll<HTMLImageElement>('img.gd-markup__image'));
    expect(icon?.getAttribute('src')).toBe('/gemp-lotr/images/cultures/gondor.png');
    expect(icon?.getAttribute('alt')).toBe('Gondor');
    expect(icon?.hasAttribute('data-icon')).toBe(true);
    expect(picture?.hasAttribute('data-icon')).toBe(false);
    expect(picture?.hasAttribute('title')).toBe(false);
    expect(el.querySelector('ol')?.getAttribute('start')).toBe('3');

    const card = el.querySelector<HTMLButtonElement>('button.gd-markup__card')!;
    expect(card.textContent).toBe('Gimli');
    expect(card.getAttribute('data-blueprint')).toBe('1_5');
    card.click();
    expect(fixture.componentInstance.cards).toEqual(['1_5']);

    const link = el.querySelector<HTMLAnchorElement>('a.gd-markup__hall-link')!;
    expect(link.getAttribute('href')).toBe('#format-pc_movie');
    const click = new MouseEvent('click', { bubbles: true, cancelable: true });
    link.dispatchEvent(click);
    // reported to the page, not followed
    expect(click.defaultPrevented).toBe(true);
    expect(fixture.componentInstance.links).toEqual(['#format-pc_movie']);
  });

  it('draws what the rich parser reads of a patch note', async () => {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.nodes.set(
      parseServerMarkup('<table><tr><td colspan="2">a</td></tr></table><a href="#patch-notes">notes</a>', RICH_MARKUP),
    );
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-markup') as HTMLElement;
    expect(el.querySelector('td')?.getAttribute('colspan')).toBe('2');
    expect(el.querySelector('a.gd-markup__hall-link')?.textContent).toBe('notes');
  });
});
