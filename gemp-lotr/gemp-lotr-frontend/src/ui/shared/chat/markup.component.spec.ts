import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MarkupNode, parseServerMarkup } from '../../../core/chat/chat-markup';
import { MarkupComponent } from './markup.component';

/** The markup drawn, without the attributes Angular's style scoping adds to the elements. */
const markupOf = (el: Element) => el.innerHTML.replace(/ _ngcontent-[\w-]+=""/g, '');

@Component({
  imports: [MarkupComponent],
  template: `<gd-markup [nodes]="nodes()" />`,
})
class Host {
  readonly nodes = signal<readonly MarkupNode[]>([]);
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
});
