import { Directive, HostListener, input, signal } from '@angular/core';

/**
 * Gives a <button> the classes and hover/active/focus states of a jQuery UI 1.8 button, so the legacy dark-hive theme
 * styles it as before. The template supplies the inner markup jQuery UI would have generated:
 *  - text button:  <span class="ui-button-text">...</span>
 *  - icon button:  <span class="ui-button-icon-primary ui-icon ui-icon-NAME"></span><span class="ui-button-text">label</span>
 */
@Directive({
  selector: 'button[gempUiButton]',
  host: {
    role: 'button',
    class: 'ui-button ui-widget ui-state-default ui-corner-all',
    '[class.ui-button-text-only]': "kind() === 'text'",
    '[class.ui-button-icon-only]': "kind() === 'icon'",
    '[class.ui-button-disabled]': 'disabled()',
    '[class.ui-state-disabled]': 'disabled()',
    '[class.ui-state-hover]': 'hovered() && !disabled()',
    '[class.ui-state-active]': 'pressed() && !disabled()',
    '[class.ui-state-focus]': 'focused()',
    '[attr.disabled]': "disabled() ? '' : null",
    '[attr.aria-disabled]': 'disabled()',
  },
})
export class UiButtonDirective {
  /** "text" (the default, also for a bare `gempUiButton` attribute) or "icon". */
  readonly kind = input<'text' | 'icon', '' | 'text' | 'icon'>('text', {
    alias: 'gempUiButton',
    transform: (kind) => kind || 'text',
  });
  readonly disabled = input(false);

  protected readonly hovered = signal(false);
  protected readonly pressed = signal(false);
  protected readonly focused = signal(false);
  /** jQuery UI re-activates a button the mouse returns to while still held down. */
  private mouseDownHere = false;

  @HostListener('mouseenter')
  protected onEnter(): void {
    this.hovered.set(true);
    if (this.mouseDownHere) {
      this.pressed.set(true);
    }
  }

  @HostListener('mouseleave')
  protected onLeave(): void {
    this.hovered.set(false);
    this.pressed.set(false);
  }

  @HostListener('mousedown')
  protected onDown(): void {
    if (!this.disabled()) {
      this.mouseDownHere = true;
      this.pressed.set(true);
    }
  }

  @HostListener('document:mouseup')
  protected onUp(): void {
    this.mouseDownHere = false;
    this.pressed.set(false);
  }

  @HostListener('focus')
  protected onFocus(): void {
    this.focused.set(true);
  }

  @HostListener('blur')
  protected onBlur(): void {
    this.focused.set(false);
  }
}
