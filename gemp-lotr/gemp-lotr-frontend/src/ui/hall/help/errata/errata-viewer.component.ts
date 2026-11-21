import { DOCUMENT } from '@angular/common';
import {
  afterNextRender,
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  signal,
  untracked,
} from '@angular/core';
import { CARD_IMAGE_TABLES } from '../../../../core/cards/card-data';
import { CardImageService } from '../../../../core/cards/card-image.service';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { CardView } from '../../../../core/cards/card-view';
import { errataReadout, ErrataReadout } from '../../../../core/hall/help/errata/errata-diff';
import { errataImages, ErrataImages } from '../../../../core/hall/help/errata/errata-images';
import {
  CLOSE_ENLARGED,
  EMPTY_HINT,
  ENLARGE,
  FLIP_GROUP_LABEL,
  NEXT_LABEL,
  NEXT_MARK,
  NEXT_TITLE,
  ORIGINAL,
  ORIGINAL_TITLE,
  PC_ERRATA,
  PC_ERRATA_TITLE,
  PREVIOUS_LABEL,
  PREVIOUS_MARK,
  PREVIOUS_TITLE,
  SHARE_ARIA_LABEL,
  SHARE_LABEL,
  VIEWER_LABEL,
  VIEWER_ROLE,
} from '../../../../core/hall/help/errata/errata-messages';
import {
  BUMP_MS,
  DRAG_FOLLOW,
  DRAG_PX,
  isPreviewSwipe,
  SLIDE_MS,
  swipeStep,
  WheelStepper,
  wheelPixels,
} from '../../../../core/hall/help/errata/errata-skim';
import { ErrataShown, PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import {
  LINK_COPIED,
  SHARE_BUTTON_TITLE,
  SHARE_ICON,
  SHARE_LINK_FIELD_LABEL,
  ShareLinks,
} from '../../../../core/hall/share-links';
import { ErrataFrameComponent, ErrataPreviewRequest } from './errata-frame.component';
import { ErrataInfoToggleComponent } from './errata-info-toggle.component';
import { prefersReducedMotion } from './errata-motion';

/** A frame on the reel: the card it shows, and whether it is sliding out. */
interface ReelFrame {
  readonly shown: ErrataShown;
  readonly images: ErrataImages;
  readonly readout: ErrataReadout;
  readonly leaving: boolean;
}

/** How long a card takes to turn over (the squeeze before the other side shows). */
const TURN_MS = 90;

/** A swipe or tap on the reel, from the press (bindSwipe's drag). */
interface Drag {
  readonly x: number;
  readonly y: number;
  readonly at: number;
  readonly onCard: boolean;
  readonly side: 'before' | 'after' | null;
  readonly touch: boolean;
  moved: boolean;
}

/**
 * The PC Errata viewer (pc-errata.html .pc-errata-viewer; pcErrataUi.js show, the flip, setEnlarged, bindWheel,
 * bindSwipe, bindPreview, fitShare): a fixed window the list moves past, one card at a time. The bar: ▲, the counter,
 * ▼, Original | PC errata, the keys' (i), Share and Enlarge. A new card slides in from below (from above going back);
 * at an end the reel nudges. The wheel over it steps the list; a swipe steps it too; a tap on the card turns it over;
 * a right-click on a card (a swipe up on it by touch or pen) opens the card information on that side.
 *
 * DOM contract: `gd-errata-viewer.gd-errata-viewer` (`tabindex=0`, `aria-roledescription` "card viewer",
 * `aria-label`; `data-enlarged`; `data-bump` = start / end for a moment) holding `.gd-errata-viewer__bar`
 * (`data-share` = full / icon / hidden: how much room Share has) with `button.gd-errata-viewer__step` (`data-step` =
 * previous / next), `span.gd-errata-viewer__counter` (`aria-live`), `.gd-errata-viewer__flip-switch` (`role=group`)
 * > `button` (`data-original` = 1 / 0, `aria-pressed`), `gd-errata-info-toggle`, `button.gd-errata-viewer__share`
 * (`img`, `.gd-errata-viewer__share-label`) and its `.gd-errata-viewer__note` (`role=status`: "Link copied", or
 * `input` with the link), `button.gd-errata-viewer__enlarge` (`aria-pressed`); then `.gd-errata-viewer__keys` (the
 * keys' help, `hidden` until its (i) opens it) and `.gd-errata-viewer__reel` with a `gd-errata-frame` per frame
 * (`data-seq`; two while one slides out) or `.gd-errata-viewer__empty` (the status and the hint).
 */
@Component({
  selector: 'gd-errata-viewer',
  styleUrl: './errata-viewer.component.css',
  imports: [ErrataFrameComponent, ErrataInfoToggleComponent],
  host: {
    class: 'gd-errata-viewer',
    tabindex: '0',
    '[attr.aria-roledescription]': 'text.VIEWER_ROLE',
    '[attr.aria-label]': 'text.VIEWER_LABEL',
    '[attr.data-enlarged]': "errata.enlarged() ? '' : null",
    '[attr.data-bump]': 'bump()',
    '(wheel)': 'wheel($event)',
  },
  template: `
    <div class="gd-errata-viewer__bar" [attr.data-share]="shareRoom()">
      <button
        type="button"
        class="gd-errata-viewer__step"
        data-step="previous"
        [attr.aria-label]="text.PREVIOUS_LABEL"
        [title]="text.PREVIOUS_TITLE"
        [disabled]="!errata.canPrevious()"
        (click)="errata.step(-1)"
      >
        {{ text.PREVIOUS_MARK }}
      </button>
      <span class="gd-errata-viewer__counter" aria-live="polite">{{ errata.counter() }}</span>
      <button
        type="button"
        class="gd-errata-viewer__step"
        data-step="next"
        [attr.aria-label]="text.NEXT_LABEL"
        [title]="text.NEXT_TITLE"
        [disabled]="!errata.canNext()"
        (click)="errata.step(1)"
      >
        {{ text.NEXT_MARK }}
      </button>
      <div class="gd-errata-viewer__flip-switch" role="group" [attr.aria-label]="text.FLIP_GROUP_LABEL">
        <button
          type="button"
          data-original="1"
          [title]="text.ORIGINAL_TITLE"
          [attr.aria-pressed]="errata.showOriginal()"
          (click)="errata.setShowOriginal(true)"
        >
          {{ text.ORIGINAL }}</button
        ><button
          type="button"
          data-original="0"
          [title]="text.PC_ERRATA_TITLE"
          [attr.aria-pressed]="!errata.showOriginal()"
          (click)="errata.setShowOriginal(false)"
        >
          {{ text.PC_ERRATA }}
        </button>
      </div>
      <gd-errata-info-toggle [(open)]="keysOpen" controls="gd-errata-viewer-keys" />
      <button
        type="button"
        class="gd-errata-viewer__share"
        [title]="text.SHARE_BUTTON_TITLE"
        [attr.aria-label]="text.SHARE_ARIA_LABEL"
        [disabled]="shownId() === null"
        (click)="share($event)"
      >
        <img alt="" aria-hidden="true" [src]="shareIcon" /><span class="gd-errata-viewer__share-label">{{
          text.SHARE_LABEL
        }}</span>
      </button>
      @if (note(); as n) {
        <span class="gd-errata-viewer__note" role="status">
          @if (n.kind === 'copied') {
            {{ text.LINK_COPIED }}
          } @else {
            <input type="text" readonly [value]="n.url" [attr.aria-label]="text.SHARE_LINK_FIELD_LABEL" />
          }
        </span>
      }
      <button
        type="button"
        class="gd-errata-viewer__enlarge"
        [attr.aria-pressed]="errata.enlarged()"
        (click)="errata.setEnlarged(!errata.enlarged())"
      >
        {{ errata.enlarged() ? text.CLOSE_ENLARGED : text.ENLARGE }}
      </button>
    </div>
    <div id="gd-errata-viewer-keys" class="gd-errata-viewer__keys" [hidden]="!keysOpen()">
      Step through the list one card at a time: <b>↑ / ↓</b> or <b>k / j</b>, the mouse wheel over this viewer, or a
      swipe up / down on it; <b>Home / End</b> go to the first / last card. Scrolling the table skims: the selected card
      rides along with the rows, several cards per wheel notch. A click on a row shows that card. A click on the card
      image (or <b>O</b>, or the switch above) flips it between the PC errata and the original; a right-click on it (a
      swipe up on a touch screen) opens it large, with zoom. <b>Enlarge</b> shows both side by side (<b>Esc</b> closes
      it). The address bar links to the card on show.
    </div>
    <div
      class="gd-errata-viewer__reel"
      (pointerdown)="pointerDown($event)"
      (pointermove)="pointerMove($event)"
      (pointerup)="pointerEnd($event, false)"
      (pointercancel)="pointerEnd($event, true)"
    >
      @for (frame of frames(); track frame.shown.seq) {
        <gd-errata-frame
          [attr.data-seq]="frame.shown.seq"
          [entry]="frame.shown.entry"
          [images]="frame.images"
          [readout]="frame.readout"
          [full]="errata.enlarged()"
          [showOriginal]="!frame.leaving && shownOriginal()"
          [turning]="!frame.leaving && turning()"
          [leaving]="frame.leaving"
          (preview)="preview($event)"
        />
      } @empty {
        @if (errata.load().kind === 'ready') {
          <div class="gd-errata-viewer__empty">
            <p>{{ errata.status().text }}</p>
            <p class="gd-errata-viewer__hint">{{ text.EMPTY_HINT }}</p>
          </div>
        }
      }
    </div>
  `,
})
export class ErrataViewerComponent {
  protected readonly errata = inject(PcErrataService);
  private readonly shareLinks = inject(ShareLinks);
  private readonly cardInfo = inject(CardInfoService);
  private readonly cardImages = inject(CardImageService);
  private readonly tables = inject(CARD_IMAGE_TABLES);
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly text = {
    CLOSE_ENLARGED,
    EMPTY_HINT,
    ENLARGE,
    FLIP_GROUP_LABEL,
    LINK_COPIED,
    NEXT_LABEL,
    NEXT_MARK,
    NEXT_TITLE,
    ORIGINAL,
    ORIGINAL_TITLE,
    PC_ERRATA,
    PC_ERRATA_TITLE,
    PREVIOUS_LABEL,
    PREVIOUS_MARK,
    PREVIOUS_TITLE,
    SHARE_ARIA_LABEL,
    SHARE_BUTTON_TITLE,
    SHARE_LABEL,
    SHARE_LINK_FIELD_LABEL,
    VIEWER_LABEL,
    VIEWER_ROLE,
  };
  protected readonly shareIcon = SHARE_ICON;
  protected readonly keysOpen = signal(false);
  protected readonly frames = signal<readonly ReelFrame[]>([]);
  protected readonly bump = signal<'start' | 'end' | null>(null);
  /** The side the card shows: it follows showOriginal once the card has turned (90ms). */
  protected readonly shownOriginal = signal(false);
  protected readonly turning = signal(false);
  /** How much room Share has in the bar: its label, only its icon, or none (fitShare). */
  protected readonly shareRoom = signal<'full' | 'icon' | 'hidden'>('full');
  protected readonly shownId = computed(() => this.errata.shown()?.entry.id ?? null);
  protected readonly note = computed(() => {
    const id = this.shownId();
    return id === null ? null : this.shareLinks.noteOf('errata', id);
  });

  private readonly wheelStepper = new WheelStepper();
  private drag: Drag | null = null;
  private previewedAt = 0;
  private leaveTimer: ReturnType<typeof setTimeout> | null = null;
  private bumpTimer: ReturnType<typeof setTimeout> | null = null;
  private turnTimer: ReturnType<typeof setTimeout> | null = null;
  private lastShowSeq = -1;

  constructor() {
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => {
      for (const timer of [this.leaveTimer, this.bumpTimer, this.turnTimer]) {
        if (timer !== null) clearTimeout(timer);
      }
    });

    // a new card on show: onto the reel, sliding in from below (or above) while the last one slides out (show)
    effect(() => {
      const shown = this.errata.shown();
      untracked(() => this.showOnReel(shown));
    });

    // Original | PC errata: the card turns, then shows the other side (setShowOriginal)
    effect(() => {
      const original = this.errata.showOriginal();
      untracked(() => this.turnTo(original));
    });

    // at either end of the list the reel nudges and settles back (bump)
    effect(() => {
      const bump = this.errata.bump();
      if (!bump || this.reducedMotion()) {
        return;
      }
      untracked(() => {
        if (this.bumpTimer !== null) clearTimeout(this.bumpTimer);
        this.bump.set(null);
        // a frame without the attribute first, so the animation starts again
        requestAnimationFrame(() => {
          this.bump.set(bump.direction > 0 ? 'end' : 'start');
          this.bumpTimer = setTimeout(() => {
            this.bumpTimer = null;
            this.bump.set(null);
          }, BUMP_MS);
        });
      });
    });

    // Share gives way when the bar is too narrow: first to its icon, then it goes (fitShare)
    afterRenderEffect(() => {
      this.errata.enlarged();
      this.note();
      this.fitShare();
    });
    afterNextRender(() => {
      const Observer = this.document.defaultView?.ResizeObserver;
      if (!Observer) return;
      const observer = new Observer(() => this.fitShare());
      observer.observe(this.host);
      destroyRef.onDestroy(() => observer.disconnect());
    });

    // the viewer takes the focus (only from a tab or nowhere: never out of a field), so the keys browse at once
    effect(() => {
      if (this.errata.focusRequest() === 0) {
        return;
      }
      untracked(() => afterNextRender(() => this.takeFocus(), { injector: this.injector }));
    });
    effect(() => {
      if (this.errata.enlarged()) {
        untracked(() => this.host.focus({ preventScroll: true }));
      }
    });
  }

  private frameOf(shown: ErrataShown): ReelFrame {
    return {
      shown,
      images: errataImages(this.tables, shown.entry),
      readout: errataReadout(shown.entry),
      leaving: false,
    };
  }

  private reducedMotion(): boolean {
    return prefersReducedMotion(this.document);
  }

  private showOnReel(shown: ErrataShown | null): void {
    if (shown === null) {
      this.frames.set([]);
      return;
    }
    if (shown.seq === this.lastShowSeq) {
      return;
    }
    this.lastShowSeq = shown.seq;
    this.shownOriginal.set(false);
    this.turning.set(false);
    const current = this.frames().filter((frame) => !frame.leaving);
    if (!shown.direction || current.length === 0 || this.reducedMotion()) {
      this.frames.set([this.frameOf(shown)]);
      return;
    }
    this.frames.set([...current.map((frame) => ({ ...frame, leaving: true })), this.frameOf(shown)]);
    const direction = shown.direction;
    afterNextRender(
      () => {
        const reel = this.host.querySelector('.gd-errata-viewer__reel');
        const easing = 'cubic-bezier(0.2, 0.7, 0.2, 1)';
        for (const element of Array.from(reel?.querySelectorAll<HTMLElement>('gd-errata-frame') ?? [])) {
          if (typeof element.animate !== 'function') continue;
          if (element.hasAttribute('data-leaving')) {
            element.animate([{ transform: 'none' }, { transform: `translateY(${-direction * 100}%)` }], {
              duration: SLIDE_MS,
              easing,
              fill: 'forwards',
            });
          } else if (element.dataset['seq'] === String(shown.seq)) {
            element.animate([{ transform: `translateY(${direction * 100}%)` }, { transform: 'none' }], {
              duration: SLIDE_MS,
              easing,
            });
          }
        }
      },
      { injector: this.injector },
    );
    if (this.leaveTimer !== null) clearTimeout(this.leaveTimer);
    this.leaveTimer = setTimeout(() => {
      this.leaveTimer = null;
      this.frames.update((frames) => frames.filter((frame) => !frame.leaving));
    }, SLIDE_MS + 60);
  }

  private turnTo(original: boolean): void {
    if (original === this.shownOriginal()) {
      return;
    }
    if (this.turnTimer !== null) clearTimeout(this.turnTimer);
    if (this.reducedMotion() || this.frames().length === 0) {
      this.shownOriginal.set(original);
      return;
    }
    // a quick turn of the card: squeeze, swap, open
    this.turning.set(true);
    this.turnTimer = setTimeout(() => {
      this.turnTimer = null;
      this.shownOriginal.set(this.errata.showOriginal());
      this.turning.set(false);
    }, TURN_MS);
  }

  /** Opening the sub-tab leaves the focus on its tab: the viewer takes it, so ↑ / ↓ browse straight away. */
  private takeFocus(): void {
    const active = this.document.activeElement;
    if (!this.host.isConnected) {
      return;
    }
    if (active === null || active === this.document.body || active.matches('[role=tab]')) {
      this.host.focus({ preventScroll: true });
    }
  }

  /** The wheel over the viewer only ever steps through the list: a notch is one card (bindWheel). */
  wheel(event: WheelEvent): void {
    if (event.ctrlKey) {
      return; // pinch / Ctrl+wheel zoom
    }
    event.preventDefault();
    if (this.errata.load().kind !== 'ready') {
      return;
    }
    const step = this.wheelStepper.step(
      wheelPixels(event.deltaY, event.deltaMode, 400),
      this.document.defaultView?.performance.now() ?? Date.now(),
    );
    if (step) {
      this.errata.step(step);
    }
  }

  protected share(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    const id = this.shownId();
    if (id !== null) {
      void this.shareLinks.copy('errata', id);
    }
  }

  private fitShare(): void {
    const bar = this.host.querySelector<HTMLElement>('.gd-errata-viewer__bar');
    if (!bar) return;
    const overflows = () => bar.clientWidth > 0 && bar.scrollWidth > bar.clientWidth + 1;
    bar.dataset['share'] = 'full';
    let room: 'full' | 'icon' | 'hidden' = 'full';
    if (overflows()) {
      bar.dataset['share'] = 'icon';
      room = 'icon';
      if (overflows()) {
        room = 'hidden';
      }
    }
    bar.dataset['share'] = room;
    if (room !== this.shareRoom()) {
      this.shareRoom.set(room);
    }
  }

  // ---- the card information (right-click; a swipe up on touch) ----

  /** Opens the card information on one side of the card on show; nothing (the browser's menu) without an image. */
  protected preview(request: ErrataPreviewRequest): void {
    if (this.openPreview(request.side, request.event)) {
      request.event.preventDefault();
    }
  }

  private openPreview(side: 'before' | 'after', event: MouseEvent | null): boolean {
    const args = this.errata.preview(side);
    if (!args) {
      return false;
    }
    this.previewedAt = Date.now();
    const image = this.cardImages.resolve(args.blueprintId, 'SPECIAL');
    const view: CardView = {
      blueprintId: args.blueprintId,
      zone: 'SPECIAL',
      ref: null,
      role: 'card',
      imageUrl: args.imageUrl,
      horizontal: image.horizontal,
      errata: false,
      foilImage: null,
      border: !image.pack,
      count: null,
      overlayImage: null,
    };
    // after the press is over: the card information closes on a press outside it, this one included
    const open = () => setTimeout(() => this.cardInfo.show(view), 0);
    if (event?.type === 'contextmenu' && event.buttons) {
      this.document.addEventListener('mouseup', open, { once: true });
    } else {
      open();
    }
    return true;
  }

  // ---- swipes and taps on the reel ----

  private currentFrame(): HTMLElement | null {
    const frames = this.host.querySelectorAll<HTMLElement>('gd-errata-frame:not([data-leaving])');
    return frames.item(frames.length - 1);
  }

  protected pointerDown(event: PointerEvent): void {
    const target = event.target as Element;
    if (event.button > 0 || this.errata.load().kind !== 'ready' || target.closest('a, button')) {
      return;
    }
    const card = target.closest<HTMLElement>('gd-errata-card');
    const side = card?.dataset['side'];
    this.drag = {
      x: event.clientX,
      y: event.clientY,
      at: Date.now(),
      moved: false,
      onCard: target.closest('.gd-errata-frame__flip .gd-errata-frame__cardbox') !== null,
      side: side === 'before' || side === 'after' ? side : null,
      touch: event.pointerType === 'touch' || event.pointerType === 'pen',
    };
    const reel = event.currentTarget as HTMLElement;
    try {
      reel.setPointerCapture(event.pointerId);
    } catch {
      // the moves over the viewer still arrive
    }
  }

  protected pointerMove(event: PointerEvent): void {
    const drag = this.drag;
    if (!drag) return;
    const dy = event.clientY - drag.y;
    const dx = event.clientX - drag.x;
    if (Math.abs(dy) > DRAG_PX || Math.abs(dx) > DRAG_PX) {
      drag.moved = true;
    }
    if (
      drag.moved &&
      !this.reducedMotion() &&
      Math.abs(dy) >= Math.abs(dx) &&
      !(drag.touch && drag.side !== null && dy < 0)
    ) {
      this.currentFrame()?.style.setProperty('--gd-errata-drag', `${Math.round(dy * DRAG_FOLLOW)}px`);
    }
  }

  protected pointerEnd(event: PointerEvent, cancelled: boolean): void {
    const drag = this.drag;
    if (!drag) return;
    this.drag = null;
    this.currentFrame()?.style.removeProperty('--gd-errata-drag');
    // a long press opened the preview (contextmenu): not a tap as well
    if (cancelled || this.previewedAt >= drag.at) {
      return;
    }
    // a tap on the card (not a swipe) turns it over
    if (!drag.moved && drag.onCard) {
      this.errata.toggleOriginal();
      return;
    }
    const dy = event.clientY - drag.y;
    const dx = event.clientX - drag.x;
    if (isPreviewSwipe(drag.touch, drag.side !== null, dx, dy) && drag.side !== null) {
      this.openPreview(drag.side, null);
      return;
    }
    const step = swipeStep(dx, dy);
    if (step) {
      this.errata.step(step);
    }
  }
}
