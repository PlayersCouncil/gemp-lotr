import { DOCUMENT } from '@angular/common';
import {
  afterNextRender,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  Injectable,
  Injector,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';

/** jQuery UI's dialog stacking: every dialog (and modal overlay) opened or clicked goes above the others. */
@Injectable({ providedIn: 'root' })
export class DialogStack {
  /** $.ui.dialog.maxZ, starting from the dialogs' zIndex option. */
  private maxZ = 1000;
  private titles = 0;

  nextZ(): number {
    return ++this.maxZ;
  }

  isTop(z: number): boolean {
    return z === this.maxZ;
  }

  /** A unique title id (ui-dialog-title-N). */
  nextTitleId(): string {
    return `ui-dialog-title-${++this.titles}`;
  }
}

/** The dialog's minWidth/minHeight options. */
const MIN_SIZE = 150;
const RESIZE_HANDLES = ['n', 'e', 's', 'w', 'se', 'sw', 'ne', 'nw'] as const;
type Handle = (typeof RESIZE_HANDLES)[number];

/** Elements focus can go to, for focusing the first of them when a dialog opens (jQuery UI's :tabbable). */
const TABBABLE = 'input, select, textarea, button, a[href], [tabindex]';

/**
 * A jQuery UI 1.8 dialog (markup, classes and behaviour), so the dark-hive theme styles it as before: centred in the
 * window when it opens, moved by its title bar, resized by its edges and corners, closed by the title bar's button or
 * Escape, and brought to the front when clicked. A modal dialog covers the page with an overlay.
 *
 * Render it only while it is open (e.g. inside @if); closing it emits `closed`. `width` and `height` are the jQuery UI
 * options: the dialog's width, and its height including the title bar ("auto" fits the content).
 */
@Component({
  selector: 'gemp-ui-dialog',
  host: { style: 'display: contents' },
  template: `
    <div
      #dialog
      class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-draggable"
      [class.ui-resizable]="resizable()"
      [class.ui-dialog-dragging]="dragging()"
      [class.ui-dialog-resizing]="resizing()"
      tabindex="-1"
      role="dialog"
      [attr.aria-labelledby]="titleId"
      (mousedown)="moveToTop()"
      (keydown)="onKeyDown($event)"
    >
      <div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix" (mousedown)="startDrag($event)">
        <span class="ui-dialog-title" [id]="titleId">{{ title() }}</span>
        <a
          href="#"
          class="ui-dialog-titlebar-close ui-corner-all"
          role="button"
          [class.ui-state-hover]="closeHovered()"
          [class.ui-state-focus]="closeFocused()"
          (mouseenter)="closeHovered.set(true)"
          (mouseleave)="closeHovered.set(false)"
          (focus)="closeFocused.set(true)"
          (blur)="closeFocused.set(false)"
          (mousedown)="$event.stopPropagation(); moveToTop()"
          (click)="$event.preventDefault(); close()"
          ><span class="ui-icon ui-icon-closethick">{{ closeText() }}</span></a
        >
      </div>
      <div
        #content
        class="ui-dialog-content ui-widget-content"
        [class]="contentClass()"
        (mousedown)="contentMouseDown.emit($event)"
      >
        <ng-content />
      </div>
      @if (resizable()) {
        @for (handle of handles; track handle; let i = $index) {
          <div
            class="ui-resizable-handle"
            [class]="'ui-resizable-' + handle"
            [class.ui-icon]="handle === 'se'"
            [class.ui-icon-gripsmall-diagonal-se]="handle === 'se'"
            [class.ui-icon-grip-diagonal-se]="handle === 'se'"
            [style.z-index]="i >= 4 ? 1001 + i - 4 : null"
            (mousedown)="startResize($event, handle)"
          ></div>
        }
      }
    </div>
    @if (modal()) {
      <div class="ui-widget-overlay" [style]="overlayStyle()"></div>
    }
  `,
})
export class UiDialogComponent {
  private readonly stack = inject(DialogStack);
  private readonly document = inject(DOCUMENT);
  private readonly window = this.document.defaultView!;
  private readonly dialogRef = viewChild.required<ElementRef<HTMLElement>>('dialog');
  private readonly contentRef = viewChild.required<ElementRef<HTMLElement>>('content');

  readonly title = input('');
  readonly width = input(300);
  readonly height = input<number | 'auto'>('auto');
  readonly modal = input(false);
  readonly resizable = input(true);
  readonly closeOnEscape = input(true);
  /** Text of the close button's icon ("close" by default; the card info and delivery dialogs use none). */
  readonly closeText = input('close');
  /** Extra classes of the content element (e.g. notesDialog). */
  readonly contentClass = input('');
  /**
   * Open as if it had no content yet: a legacy dialog opened on creation and filled after (the import dialog) kept the
   * position it got while empty, and the focus on itself rather than on its first field.
   */
  readonly openedEmpty = input(false);

  readonly closed = output<void>();
  /** The content's size (jQuery's .width()/.height() of the content) changed by opening or resizing. */
  readonly contentResized = output<{ width: number; height: number }>();
  /** A mouse button pressed over the content. */
  readonly contentMouseDown = output<MouseEvent>();
  /** The player resized the dialog: its new width and height options. */
  readonly sizeChanged = output<{ width: number; height: number }>();

  protected readonly handles = RESIZE_HANDLES;
  protected readonly titleId = this.stack.nextTitleId();
  protected readonly closeHovered = signal(false);
  protected readonly closeFocused = signal(false);
  protected readonly dragging = signal(false);
  protected readonly resizing = signal(false);
  protected readonly overlayStyle = signal<Record<string, string>>({});

  private z = 0;
  private overlayZ = 0;
  private nonContentHeight = 0;
  private currentWidth = 0;
  private currentHeight: number | 'auto' = 'auto';

  constructor() {
    const injector = inject(Injector);
    afterNextRender(() => this.open(), { injector });
    const onResize = () => this.updateOverlay();
    this.window.addEventListener('resize', onResize);
    inject(DestroyRef).onDestroy(() => this.window.removeEventListener('resize', onResize));
  }

  /** Applies new width/height options to the open dialog (jQuery UI's dialog({width, height}) on an open dialog). */
  setSize(width: number, height: number | 'auto'): void {
    this.currentWidth = width;
    this.currentHeight = height;
    this.size();
    this.emitContentSize();
  }

  /** Closing and opening it again, as the legacy code did to bring a dialog back on screen: centred, in front, focused. */
  reopen(): void {
    this.position();
    this.z = this.stack.nextZ();
    this.dialogRef().nativeElement.style.zIndex = `${this.z}`;
    this.focusFirst();
  }

  /** The content's box on the page (the element jQuery UI's dialog() was called on). */
  contentRect(): DOMRect {
    return this.contentRef().nativeElement.getBoundingClientRect();
  }

  close(): void {
    this.closed.emit();
  }

  protected moveToTop(): void {
    if (!this.stack.isTop(this.z)) {
      this.z = this.stack.nextZ();
      this.dialogRef().nativeElement.style.zIndex = `${this.z}`;
    }
  }

  protected onKeyDown(event: KeyboardEvent): void {
    if (this.closeOnEscape() && event.key === 'Escape') {
      event.preventDefault();
      this.close();
    }
  }

  protected startDrag(event: MouseEvent): void {
    if (event.button !== 0) {
      return;
    }
    event.preventDefault();
    const dialog = this.dialogRef().nativeElement;
    const startX = event.pageX;
    const startY = event.pageY;
    const startLeft = dialog.offsetLeft;
    const startTop = dialog.offsetTop;
    this.dragging.set(true);
    const move = (e: MouseEvent) => {
      // containment: "document"
      const maxLeft = this.documentWidth() - dialog.offsetWidth;
      const maxTop = this.documentHeight() - dialog.offsetHeight;
      dialog.style.left = `${Math.max(0, Math.min(maxLeft, startLeft + e.pageX - startX))}px`;
      dialog.style.top = `${Math.max(0, Math.min(maxTop, startTop + e.pageY - startY))}px`;
    };
    const up = () => {
      this.document.removeEventListener('mousemove', move);
      this.document.removeEventListener('mouseup', up);
      this.dragging.set(false);
    };
    this.document.addEventListener('mousemove', move);
    this.document.addEventListener('mouseup', up);
  }

  protected startResize(event: MouseEvent, handle: Handle): void {
    if (event.button !== 0) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    this.moveToTop();
    const dialog = this.dialogRef().nativeElement;
    const startX = event.pageX;
    const startY = event.pageY;
    const startLeft = dialog.offsetLeft;
    const startTop = dialog.offsetTop;
    const startWidth = this.currentWidth;
    const startHeight = this.jqHeight(dialog);
    this.resizing.set(true);
    const move = (e: MouseEvent) => {
      const dx = e.pageX - startX;
      const dy = e.pageY - startY;
      let width = startWidth;
      let height = startHeight;
      if (handle.includes('e')) width = startWidth + dx;
      if (handle.includes('w')) width = startWidth - dx;
      if (handle.includes('s')) height = startHeight + dy;
      if (handle.includes('n')) height = startHeight - dy;
      width = Math.max(MIN_SIZE, width);
      height = Math.max(MIN_SIZE, height);
      if (handle.includes('w')) dialog.style.left = `${startLeft + startWidth - width}px`;
      if (handle.includes('n')) dialog.style.top = `${startTop + startHeight - height}px`;
      this.currentWidth = width;
      this.currentHeight = height;
      this.size();
      this.emitContentSize();
      this.sizeChanged.emit({ width, height });
    };
    const up = () => {
      this.document.removeEventListener('mousemove', move);
      this.document.removeEventListener('mouseup', up);
      this.resizing.set(false);
    };
    this.document.addEventListener('mousemove', move);
    this.document.addEventListener('mouseup', up);
  }

  private open(): void {
    const dialog = this.dialogRef().nativeElement;
    dialog.style.visibility = 'hidden';
    if (this.modal()) {
      this.overlayZ = this.stack.nextZ();
      this.updateOverlay();
    }
    this.z = this.stack.nextZ();
    dialog.style.display = 'block';
    dialog.style.zIndex = `${this.z}`;
    dialog.style.outline = '0px';
    if (this.resizable()) {
      dialog.style.position = 'absolute';
    }
    this.currentWidth = Math.max(this.width(), MIN_SIZE);
    this.currentHeight = this.height();
    this.size();
    if (this.openedEmpty() && this.currentHeight === 'auto') {
      const content = this.contentRef().nativeElement;
      content.style.height = content.style.minHeight;
      this.position();
      content.style.height = 'auto';
    } else {
      this.position();
    }
    dialog.style.visibility = '';
    this.emitContentSize();
    if (this.openedEmpty()) {
      dialog.focus();
    } else {
      this.focusFirst();
    }
  }

  /** jQuery UI's _size: the content gets the height left after the title bar. */
  private size(): void {
    const dialog = this.dialogRef().nativeElement;
    const content = this.contentRef().nativeElement;
    content.style.width = 'auto';
    content.style.minHeight = '0px';
    content.style.height = '0px';
    dialog.style.height = 'auto';
    dialog.style.width = `${this.currentWidth}px`;
    this.nonContentHeight = this.jqHeight(dialog);
    if (this.currentHeight === 'auto') {
      content.style.minHeight = `${this.round(Math.max(0, MIN_SIZE - this.nonContentHeight))}px`;
      content.style.height = 'auto';
    } else {
      content.style.height = `${this.round(Math.max(this.currentHeight - this.nonContentHeight, 0))}px`;
    }
  }

  /** Centred in the window, kept inside it (position: center, collision: fit). */
  private position(): void {
    const dialog = this.dialogRef().nativeElement;
    const fit = (free: number, scroll: number) => Math.max(scroll, Math.round(free / 2) + scroll);
    dialog.style.top = `${fit(this.window.innerHeight - dialog.offsetHeight, this.window.scrollY)}px`;
    dialog.style.left = `${fit(this.window.innerWidth - dialog.offsetWidth, this.window.scrollX)}px`;
  }

  private focusFirst(): void {
    const content = this.contentRef().nativeElement;
    const first = Array.from(content.querySelectorAll<HTMLElement>(TABBABLE)).find(
      (el) => el.tabIndex >= 0 && !(el as HTMLButtonElement).disabled && (el.checkVisibility?.() ?? true),
    );
    (first ?? this.dialogRef().nativeElement).focus();
  }

  private emitContentSize(): void {
    const content = this.contentRef().nativeElement;
    this.contentResized.emit({ width: this.jqWidth(content), height: this.jqHeight(content) });
  }

  /** jQuery 1.6's .height(): the whole-pixel offsetHeight less the padding and border. */
  private jqHeight(element: HTMLElement): number {
    const style = this.window.getComputedStyle(element);
    const px = (value: string) => parseFloat(value) || 0;
    return (
      element.offsetHeight -
      px(style.paddingTop) -
      px(style.paddingBottom) -
      px(style.borderTopWidth) -
      px(style.borderBottomWidth)
    );
  }

  /** jQuery 1.6's .width(): the whole-pixel offsetWidth less the padding and border. */
  private jqWidth(element: HTMLElement): number {
    const style = this.window.getComputedStyle(element);
    const px = (value: string) => parseFloat(value) || 0;
    return (
      element.offsetWidth -
      px(style.paddingLeft) -
      px(style.paddingRight) -
      px(style.borderLeftWidth) -
      px(style.borderRightWidth)
    );
  }

  private updateOverlay(): void {
    if (this.modal()) {
      this.overlayStyle.set({
        width: `${this.documentWidth()}px`,
        height: `${this.documentHeight()}px`,
        'z-index': `${this.overlayZ}`,
      });
    }
  }

  private documentWidth(): number {
    return Math.max(this.document.documentElement.scrollWidth, this.window.innerWidth);
  }

  private documentHeight(): number {
    return Math.max(this.document.documentElement.scrollHeight, this.window.innerHeight);
  }

  /** jQuery wrote sizes with two decimals at most (e.g. 356.28px). */
  private round(value: number): number {
    return Math.round(value * 100) / 100;
  }
}
