import { Component, computed, ElementRef, inject, input } from '@angular/core';
import { PathPopups } from './path-popups';

/**
 * The adventure path's open spot: a site-shaped + glowing blue (the open spot's own look) under the sites laid, for
 * the next site not laid yet. Pressing it opens the site chooser (Search…, From adventure deck…) by it. The path shows
 * it only while TableEdit allows `sites`.
 *
 * DOM contract: `gd-site-spot` holding `button.gd-open-spot[data-shape=site][data-site]` (`aria-label` "Add site …",
 * `aria-expanded` while its chooser is open) with `.gd-open-spot__plus` and `.gd-open-spot__number` (the site number).
 */
@Component({
  selector: 'gd-site-spot',
  // (the open spot's own stylesheet, so the two look and glow alike)
  styleUrl: './open-spot.component.css',
  template: `
    <button
      type="button"
      class="gd-open-spot"
      data-shape="site"
      [attr.data-site]="number()"
      [attr.aria-label]="'Add site ' + number()"
      [attr.aria-expanded]="open()"
      [title]="'Lay site ' + number()"
      (click)="press()"
    >
      <span class="gd-open-spot__plus" aria-hidden="true">+</span>
      <span class="gd-open-spot__number" aria-hidden="true">{{ number() }}</span>
    </button>
  `,
})
export class SiteSpotComponent {
  /** The site number a site chosen here is laid at. */
  readonly number = input.required<number>();

  private readonly popups = inject(PathPopups);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly open = computed(() => {
    const chooser = this.popups.chooser();
    return chooser !== null && !chooser.replacing && chooser.number === this.number();
  });

  protected press(): void {
    if (this.open()) {
      this.popups.closeChooser();
    } else {
      this.popups.openChooser(this.number(), this.host.querySelector('button') ?? this.host);
    }
  }
}
