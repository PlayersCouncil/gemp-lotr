import { Component, effect, ElementRef, inject, input, output, Renderer2 } from '@angular/core';
import { MarkupNode } from '../../../core/chat/chat-markup';

/**
 * Server markup drawn from its MarkupNodes (core/chat parseServerMarkup): text nodes, `br`, the allowed formatting
 * elements, links, and for rich markup (the patch notes') pictures, cards and links into the hall, built one by one,
 * never from an HTML string. Links open in a new tab. What a card or a hall link does is the page's: this reports them
 * (`card`, `hallLink`).
 *
 * DOM contract: `gd-markup` (inline, no box of its own) holding the nodes as plain elements (`b`, `p`, `blockquote`,
 * `table`, ..., with `start`, `colspan` and `rowspan` where the server set them), `a.gd-markup__link`
 * (`target=_blank`, `rel=noopener noreferrer`), `img.gd-markup__image` (`data-icon` for a culture icon, drawn in the
 * line), `button.gd-markup__card` (`data-blueprint`; a card's name, which shows the card) and
 * `a.gd-markup__hall-link` (`href` = the legacy `#fragment`; a click is reported, not followed).
 */
@Component({
  selector: 'gd-markup',
  styleUrl: './markup.component.css',
  host: { class: 'gd-markup' },
  template: '',
})
export class MarkupComponent {
  readonly nodes = input.required<readonly MarkupNode[]>();
  /** A card's name was used: its blueprint id. */
  readonly card = output<string>();
  /** A link into the hall was used: the legacy `#fragment` it names (core/hall hallRouteOf gives its route). */
  readonly hallLink = output<string>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly renderer = inject(Renderer2);

  constructor() {
    effect(() => {
      const nodes = this.nodes();
      for (const child of Array.from(this.host.childNodes)) {
        this.renderer.removeChild(this.host, child);
      }
      this.append(this.host, nodes);
    });
  }

  private append(parent: Node, nodes: readonly MarkupNode[]): void {
    for (const node of nodes) {
      this.renderer.appendChild(parent, this.create(node));
    }
  }

  private create(node: MarkupNode): Node {
    switch (node.kind) {
      case 'text':
        return this.renderer.createText(node.text) as Text;
      case 'break':
        return this.renderer.createElement('br') as HTMLBRElement;
      case 'link': {
        const link = this.renderer.createElement('a') as HTMLAnchorElement;
        this.renderer.addClass(link, 'gd-markup__link');
        this.renderer.setAttribute(link, 'href', node.href);
        this.renderer.setAttribute(link, 'target', '_blank');
        this.renderer.setAttribute(link, 'rel', 'noopener noreferrer');
        this.append(link, node.children);
        return link;
      }
      case 'element': {
        const element = this.renderer.createElement(node.tag) as HTMLElement;
        for (const [name, value] of Object.entries(node.attributes ?? {})) {
          this.renderer.setAttribute(element, name, value);
        }
        this.append(element, node.children);
        return element;
      }
      case 'image': {
        const image = this.renderer.createElement('img') as HTMLImageElement;
        this.renderer.addClass(image, 'gd-markup__image');
        this.renderer.setAttribute(image, 'src', node.src);
        this.renderer.setAttribute(image, 'alt', node.alt);
        if (node.title !== null) {
          this.renderer.setAttribute(image, 'title', node.title);
        }
        if (node.icon) {
          this.renderer.setAttribute(image, 'data-icon', '');
        }
        return image;
      }
      case 'card': {
        const card = this.renderer.createElement('button') as HTMLButtonElement;
        this.renderer.addClass(card, 'gd-markup__card');
        this.renderer.setAttribute(card, 'type', 'button');
        this.renderer.setAttribute(card, 'data-blueprint', node.blueprintId);
        if (node.title !== null) {
          this.renderer.setAttribute(card, 'title', node.title);
        }
        this.renderer.listen(card, 'click', () => this.card.emit(node.blueprintId));
        this.append(card, node.children);
        return card;
      }
      case 'hall-link': {
        const link = this.renderer.createElement('a') as HTMLAnchorElement;
        this.renderer.addClass(link, 'gd-markup__hall-link');
        this.renderer.setAttribute(link, 'href', node.hash);
        this.renderer.listen(link, 'click', (event: Event) => {
          event.preventDefault();
          this.hallLink.emit(node.hash);
        });
        this.append(link, node.children);
        return link;
      }
    }
  }
}
