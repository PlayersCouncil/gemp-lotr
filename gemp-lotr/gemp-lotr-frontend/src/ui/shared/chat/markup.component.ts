import { Component, effect, ElementRef, inject, input, Renderer2 } from '@angular/core';
import { MarkupNode } from '../../../core/chat/chat-markup';

/**
 * Server markup drawn from its MarkupNodes (core/chat parseServerMarkup): text nodes, `br`, the allowed formatting
 * elements and links, built one by one, never from an HTML string. Links open in a new tab.
 *
 * DOM contract: `gd-markup` (inline, no box of its own) holding the nodes as plain elements (`b`, `p`, `blockquote`,
 * ...) and `a.gd-markup__link` (`target=_blank`, `rel=noopener noreferrer`).
 */
@Component({
  selector: 'gd-markup',
  styleUrl: './markup.component.css',
  host: { class: 'gd-markup' },
  template: '',
})
export class MarkupComponent {
  readonly nodes = input.required<readonly MarkupNode[]>();

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
        this.append(element, node.children);
        return element;
      }
    }
  }
}
