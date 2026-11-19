import { Directive, effect, ElementRef, inject, input } from '@angular/core';

/**
 * Plays the element's pulse (`gd-pulse`, its stylesheet's animation) again each time the number given grows: a clock
 * that calls for attention. 0 plays nothing.
 */
@Directive({
  selector: '[gdPulse]',
})
export class PulseDirective {
  readonly gdPulse = input(0);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  constructor() {
    effect(() => {
      if (this.gdPulse() === 0) return;
      // Taken off, then set again on the next frame: the animation starts over
      this.host.removeAttribute('data-pulse');
      this.host.getBoundingClientRect();
      this.host.setAttribute('data-pulse', '');
    });
  }
}
