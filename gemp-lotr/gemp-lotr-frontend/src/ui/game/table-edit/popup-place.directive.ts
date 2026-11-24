import { afterRenderEffect, DestroyRef, Directive, ElementRef, inject, input } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Place, Size } from '../../../core/game/table-edit/popup-place';

/**
 * Places a popup of the edit controls in the window once it is drawn and whenever its size changes: `gdPopupPlace` is
 * where it goes for its size and the window's (core/game/table-edit/popup-place.ts), set on the host as `--gd-left`
 * and `--gd-top` (px), which its stylesheet uses (`position: fixed`). Until it is placed, it has `data-placing` (its
 * stylesheet keeps it unseen then).
 *
 * DOM contract: `--gd-left`, `--gd-top` and `data-placing` on the host.
 */
@Directive({ selector: '[gdPopupPlace]', host: { 'data-placing': '' } })
export class PopupPlaceDirective {
  readonly gdPopupPlace = input.required<(size: Size, room: Size) => Place>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly document = inject(DOCUMENT);

  constructor() {
    const ResizeObserverClass = this.document.defaultView?.ResizeObserver;
    const observer = ResizeObserverClass ? new ResizeObserverClass(() => this.place()) : null;
    observer?.observe(this.host);
    inject(DestroyRef).onDestroy(() => observer?.disconnect());
    afterRenderEffect({ write: () => this.place() });
  }

  private place(): void {
    const page = this.document.documentElement;
    const at = this.gdPopupPlace()(
      { width: this.host.offsetWidth, height: this.host.offsetHeight },
      { width: page.clientWidth, height: page.clientHeight },
    );
    this.host.style.setProperty('--gd-left', `${Math.round(at.left)}px`);
    this.host.style.setProperty('--gd-top', `${Math.round(at.top)}px`);
    this.host.removeAttribute('data-placing');
  }
}
