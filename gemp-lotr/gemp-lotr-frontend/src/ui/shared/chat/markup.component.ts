import { Component, effect, ElementRef, inject, input, output, Renderer2 } from '@angular/core';
import { Router } from '@angular/router';
import { MarkupNode } from '../../../core/chat/chat-markup';
import { hallRouteOf } from '../../../core/hall/hall-links';
import { hallPageAddress } from '../../../core/platform/hall-tab.service';

/**
 * Server markup drawn from its MarkupNodes (core/chat parseServerMarkup): text nodes, `br`, the allowed formatting
 * elements, links, and for rich markup (the patch notes') pictures, cards and links into the hall, built one by one,
 * never from an HTML string. Links open in a new tab. A link into the hall goes to the route the legacy link names
 * (hallRouteOf) and is reported (`hallLink`), for a page with something more to do; opened in a new tab by the
 * browser, it is that route's address. What a card does is the page's: it is reported (`card`).
 *
 * DOM contract: `gd-markup` (inline, no box of its own) holding the nodes as plain elements (`b`, `p`, `blockquote`,
 * `table`, ..., with `start`, `colspan` and `rowspan` where the server set them), `a.gd-markup__link`
 * (`target=_blank`, `rel=noopener noreferrer`), `img.gd-markup__image` (`data-icon` for a culture icon, drawn in the
 * line), `button.gd-markup__card` (`data-blueprint`; a card's name, which shows the card) and
 * `a.gd-markup__hall-link` (`href` = the address of the route the legacy `#fragment` names; `target=_blank` for
 * the legacy hall's Admin, which is a page of its own).
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
  /** A link into the hall was followed: the legacy `#fragment` it names. */
  readonly hallLink = output<string>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly renderer = inject(Renderer2);
  private readonly router = inject(Router);

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

  private withChildren(element: HTMLElement, children: readonly MarkupNode[]): HTMLElement {
    this.append(element, children);
    return element;
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
        const route = hallRouteOf(node.hash);
        this.renderer.setAttribute(link, 'href', hallPageAddress(node.hash));
        if (route === null) {
          // Admin (the legacy hall's page, which must not replace this one) or a link nothing knows: the hall
          this.renderer.setAttribute(link, 'target', '_blank');
          this.renderer.setAttribute(link, 'rel', 'noopener');
          return this.withChildren(link, node.children);
        }
        this.renderer.listen(link, 'click', (event: MouseEvent) => {
          if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
            return; // a new tab or window: the browser's, with the route's address
          }
          event.preventDefault();
          void this.router.navigate([route.path], {
            queryParams: route.queryParams,
            ...(route.fragment === null ? {} : { fragment: route.fragment }),
          });
          this.hallLink.emit(node.hash);
        });
        this.append(link, node.children);
        return link;
      }
    }
  }
}
