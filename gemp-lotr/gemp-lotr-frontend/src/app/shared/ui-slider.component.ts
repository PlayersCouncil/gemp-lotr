import { Component, computed, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';

/** jQuery UI's PAGE_UP / PAGE_DOWN step: a fifth of the range. */
const KEY_PAGES = 5;

/**
 * A horizontal jQuery UI 1.8 slider (markup, classes and behaviour), so the dark-hive theme styles it as before:
 * click the track to jump, drag the handle, or use the arrow, Home/End and Page Up/Down keys on the focused handle.
 * `slide` fires whenever the value changes while sliding, like jQuery UI's slide event.
 */
@Component({
  selector: 'div[gempUiSlider]',
  host: {
    class: 'ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all',
    '[class.ui-slider-disabled]': 'disabled()',
    '[class.ui-disabled]': 'disabled()',
    // jQuery UI wrote aria-disabled only when the option changed; a slider created disabled had none
    '[attr.aria-disabled]': "disabled() ? null : 'false'",
    '(mousedown)': 'onMouseDown($event)',
  },
  template: `
    <a
      #handle
      class="ui-slider-handle ui-state-default ui-corner-all"
      href="#"
      [class.ui-state-hover]="hovered() && !disabled()"
      [class.ui-state-focus]="focused() && !disabled()"
      [class.ui-state-active]="active()"
      [style.left]="percent() + '%'"
      (mouseenter)="hovered.set(true)"
      (mouseleave)="hovered.set(false)"
      (focus)="onFocus()"
      (blur)="focused.set(false)"
      (click)="$event.preventDefault()"
      (keydown)="onKeyDown($event)"
      (keyup)="onKeyUp()"
    ></a>
  `,
})
export class UiSliderComponent {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly handle = viewChild.required<ElementRef<HTMLAnchorElement>>('handle');

  readonly min = input(0);
  readonly max = input(100);
  readonly step = input(1);
  /** The starting value; afterwards the slider keeps its own value, as the jQuery UI widget did. */
  readonly initialValue = input(0, { alias: 'value' });
  readonly disabled = input(false);
  readonly slide = output<number>();

  private readonly current = signal<number | null>(null);
  readonly value = computed(() => this.current() ?? this.initialValue());

  protected readonly hovered = signal(false);
  protected readonly focused = signal(false);
  protected readonly active = signal(false);
  private keySliding = false;

  protected readonly percent = computed(() => ((this.value() - this.min()) / (this.max() - this.min())) * 100);

  protected onFocus(): void {
    if (this.disabled()) {
      this.handle().nativeElement.blur();
    } else {
      this.focused.set(true);
    }
  }

  protected onMouseDown(event: MouseEvent): void {
    if (this.disabled() || event.button !== 0) {
      return;
    }
    event.preventDefault();
    const handle = this.handle().nativeElement;
    const track = this.host.nativeElement.getBoundingClientRect();
    const onHandle = handle.contains(event.target as Node);
    // Grabbing the handle keeps the offset between the mouse and its centre, so it doesn't jump
    const handleBox = handle.getBoundingClientRect();
    const grabOffset = onHandle ? event.clientX - handleBox.left - handleBox.width / 2 : 0;
    const valueAt = (clientX: number) => {
      const fraction = Math.min(1, Math.max(0, (clientX - track.left - grabOffset) / track.width));
      return this.trimAlign(this.min() + fraction * (this.max() - this.min()));
    };

    this.active.set(true);
    handle.focus();
    if (!onHandle) {
      this.slideTo(valueAt(event.clientX));
    }
    const move = (e: MouseEvent) => this.slideTo(valueAt(e.clientX));
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      this.active.set(false);
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
  }

  protected onKeyDown(event: KeyboardEvent): void {
    if (this.disabled()) {
      return;
    }
    const value = this.value();
    const min = this.min();
    const max = this.max();
    const page = (max - min) / KEY_PAGES;
    let next: number;
    switch (event.key) {
      case 'Home':
        next = min;
        break;
      case 'End':
        next = max;
        break;
      case 'PageUp':
        next = this.trimAlign(value + page);
        break;
      case 'PageDown':
        next = this.trimAlign(value - page);
        break;
      case 'ArrowUp':
      case 'ArrowRight':
        next = value === max ? max : this.trimAlign(value + this.step());
        break;
      case 'ArrowDown':
      case 'ArrowLeft':
        next = value === min ? min : this.trimAlign(value - this.step());
        break;
      default:
        return;
    }
    event.preventDefault();
    if (!this.keySliding) {
      this.keySliding = true;
      this.active.set(true);
    }
    this.slideTo(next);
  }

  protected onKeyUp(): void {
    if (this.keySliding) {
      this.keySliding = false;
      this.active.set(false);
    }
  }

  private slideTo(value: number): void {
    if (value !== this.value()) {
      this.current.set(value);
      this.slide.emit(value);
    }
  }

  /** jQuery UI's _trimAlignValue: within the limits, on the nearest step. */
  private trimAlign(value: number): number {
    const min = this.min();
    const step = this.step() > 0 ? this.step() : 1;
    if (value <= min) {
      return min;
    }
    if (value >= this.max()) {
      return this.max();
    }
    const modStep = (value - min) % step;
    let aligned = value - modStep;
    if (Math.abs(modStep) * 2 >= step) {
      aligned += modStep > 0 ? step : -step;
    }
    return parseFloat(aligned.toFixed(5));
  }
}
