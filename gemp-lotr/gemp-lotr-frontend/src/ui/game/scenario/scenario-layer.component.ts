import { DOCUMENT } from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  signal,
  untracked,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardViews } from '../../../core/cards/card-views.service';
import {
  around,
  arrowReach,
  gapPixels,
  overlap,
  placeBubble,
  Pointer,
  pointerFor,
  Rect,
} from '../../../core/game/scenario/bubble-placement';
import { HighlightStyle, ScenarioMessage } from '../../../core/game/scenario/scenario-events';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { cardShape, layoutZoom, ZoomLayout } from '../../../core/game/scenario/zoom-layout';
import { GameSession } from '../../../core/game/session/game-session.service';
import { DragHandlers, PointerDragDirective } from '../../shared/pointer-drag/pointer-drag.directive';
import { CardHintsDirective } from '../table/card-hints.directive';
import { AnchorContext, cardElement, rectAround, rectOf, resolveAnchor } from './scenario-anchors';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';

/** An outline round something on the page: what a message is about, or (`next`) what is to be clicked next. */
interface Outline extends Rect {
  look: HighlightStyle | 'next';
}

/** Everything the layer draws that depends on where things are on the page, measured each frame. */
interface Drawn {
  outlines: readonly Outline[];
  /** Where the bubble is; null until it has been measured (it is kept out of sight until then). */
  bubble: { left: number; top: number } | null;
  pointer: Pointer;
  hint: { left: number; top: number } | null;
  zoom: ZoomLayout | null;
}

const NOTHING: Drawn = { outlines: [], bubble: null, pointer: { kind: 'none' }, hint: null, zoom: null };
/** An outline stands this far off what it is round (px). */
const OUTLINE_PAD = 4;

const px = (value: number) => `${Math.round(value)}px`;

/**
 * What a scripted game (a scenario) draws over the game page while it is played: the bubble its messages are in,
 * joined to what each is about by an arrow or by lines; outlines round what a message is about and round what is to
 * be clicked next; what it says of a click the step doesn't take; and a card or picture shown large. Everything is
 * placed against what is on the page at that moment, found by the page's DOM contract (scenario-anchors.ts) and
 * measured every frame while anything is up, so it follows cards as they move. The layer lets clicks through; only
 * the bubble takes its own. With nothing to show, nothing is in the page.
 *
 * DOM contract: `gd-game-scenario` holding `.gd-scenario` while anything shows; in it `.gd-scenario-outline`
 * (`data-look`: gold, red, green, blue, next), `svg.gd-scenario-pointer`, `section.gd-scenario-bubble`
 * (`data-headed`, `data-collapsed`, `data-moved`, `data-placed` once it has its place) with
 * `.gd-scenario-bubble__arrow` (`data-edge`), `header.gd-scenario-bubble__head` (`.gd-scenario-bubble__title`,
 * `.gd-scenario-bubble__count`, `button.gd-scenario-bubble__fold`), `.gd-scenario-bubble__body` and
 * `footer.gd-scenario-bubble__foot` holding `button.gd-scenario-button` (`data-scenario`: next, or `data-choice`
 * with the choice's name); `.gd-scenario-hint`; `.gd-scenario-zoom` (`data-blocks` while it takes the pointer) with
 * `.gd-scenario-zoom__frame` > `img.gd-scenario-zoom__card`, `svg.gd-scenario-zoom__dim`, `.gd-scenario-zoom__mark`.
 */
@Component({
  selector: 'gd-game-scenario',
  styleUrl: './scenario-layer.component.css',
  // The messages are the server's HTML, which a scoped stylesheet wouldn't reach; every rule starts at .gd-scenario
  encapsulation: ViewEncapsulation.None,
  imports: [PointerDragDirective, CardHintsDirective],
  host: { style: 'display: contents' },
  template: `
    @if (showing()) {
      <div class="gd-scenario">
        @if (zoomPicture(); as picture) {
          @if (drawn().zoom; as zoom) {
            <div class="gd-scenario-zoom" [attr.data-blocks]="zoomBlocks() ? '' : null">
              <div
                class="gd-scenario-zoom__frame"
                [style.--gd-left]="px(zoom.frame.left)"
                [style.--gd-top]="px(zoom.frame.top)"
                [style.--gd-width]="px(zoom.frame.width)"
                [style.--gd-height]="px(zoom.frame.height)"
              >
                <img
                  class="gd-scenario-zoom__card"
                  alt=""
                  [src]="picture"
                  [style.--gd-left]="px(zoom.card.left)"
                  [style.--gd-top]="px(zoom.card.top)"
                  [style.--gd-width]="px(zoom.card.width)"
                  [style.--gd-height]="px(zoom.card.height)"
                  (load)="pictureLoaded($event)"
                />
                @if (zoom.dim; as dim) {
                  <svg class="gd-scenario-zoom__dim" aria-hidden="true">
                    <path fill-rule="evenodd" [attr.d]="dim" />
                  </svg>
                }
                @for (mark of zoom.marks; track $index) {
                  <span
                    class="gd-scenario-zoom__mark"
                    [style.--gd-left]="px(mark.left)"
                    [style.--gd-top]="px(mark.top)"
                    [style.--gd-width]="px(mark.width)"
                    [style.--gd-height]="px(mark.height)"
                  ></span>
                }
              </div>
            </div>
          }
        }
        @for (outline of drawn().outlines; track $index) {
          <span
            class="gd-scenario-outline"
            [attr.data-look]="outline.look"
            [style.--gd-left]="px(outline.left)"
            [style.--gd-top]="px(outline.top)"
            [style.--gd-width]="px(outline.width)"
            [style.--gd-height]="px(outline.height)"
          ></span>
        }
        @if (lines(); as lines) {
          <svg class="gd-scenario-pointer" aria-hidden="true">
            @for (line of lines; track $index) {
              <g>
                <line
                  class="gd-scenario-pointer__edge"
                  [attr.x1]="line.x1"
                  [attr.y1]="line.y1"
                  [attr.x2]="line.x2"
                  [attr.y2]="line.y2"
                />
                <line
                  class="gd-scenario-pointer__line"
                  [attr.x1]="line.x1"
                  [attr.y1]="line.y1"
                  [attr.x2]="line.x2"
                  [attr.y2]="line.y2"
                />
                <circle class="gd-scenario-pointer__end" r="4" [attr.cx]="line.x2" [attr.cy]="line.y2" />
              </g>
            }
          </svg>
        }
        @if (guide.message(); as message) {
          <section
            #bubble
            class="gd-scenario-bubble"
            aria-live="polite"
            [attr.data-headed]="message.header ? '' : null"
            [attr.data-collapsed]="collapsed() ? '' : null"
            [attr.data-moved]="moved() ? '' : null"
            [attr.data-placed]="drawn().bubble ? '' : null"
            [style.--gd-left]="px(drawn().bubble?.left ?? 0)"
            [style.--gd-top]="px(drawn().bubble?.top ?? 0)"
          >
            @if (arrow(); as arrow) {
              <span
                class="gd-scenario-bubble__arrow"
                [attr.data-edge]="arrow.edge"
                [style.--gd-along]="px(arrow.along)"
              ></span>
            }
            <header class="gd-scenario-bubble__head" [gdPointerDrag]="drag">
              <span class="gd-scenario-bubble__title">{{ message.header }}</span>
              @if (guide.count(); as count) {
                <span class="gd-scenario-bubble__count">{{ count.at }} / {{ count.of }}</span>
              }
              <button
                type="button"
                class="gd-scenario-bubble__fold"
                [attr.aria-expanded]="!collapsed()"
                [title]="collapsed() ? 'Show this again' : 'Fold this away'"
                (click)="collapsed.set(!collapsed())"
              >
                {{ collapsed() ? '+' : '–' }}
              </button>
            </header>
            <div class="gd-scenario-bubble__body" gdCardHints [innerHTML]="message.html"></div>
            @if (message.choices.length > 0 || guide.nextLabel()) {
              <footer class="gd-scenario-bubble__foot" [attr.data-nudge]="nudge()">
                @for (choice of message.choices; track choice.name) {
                  <button
                    type="button"
                    class="gd-scenario-button"
                    data-main
                    [attr.data-choice]="choice.name"
                    (click)="guide.choose(choice.name)"
                  >
                    {{ choice.label }}
                  </button>
                }
                @if (guide.nextLabel(); as label) {
                  <button
                    type="button"
                    class="gd-scenario-button"
                    data-main
                    data-scenario="next"
                    (click)="guide.next()"
                  >
                    {{ label }}
                  </button>
                }
              </footer>
            }
          </section>
        }
        @if (guide.hint(); as hint) {
          <div
            #hintBox
            class="gd-scenario-hint"
            role="status"
            [attr.data-placed]="drawn().hint ? '' : null"
            [style.--gd-left]="px(drawn().hint?.left ?? 0)"
            [style.--gd-top]="px(drawn().hint?.top ?? 0)"
            [innerHTML]="hint.html"
          ></div>
        }
      </div>
    }
  `,
})
export class ScenarioLayerComponent {
  protected readonly guide = inject(ScenarioGuide);
  private readonly session = inject(GameSession);
  private readonly room = inject(ScenarioRoom);
  private readonly cardInfo = inject(CardInfoService);
  private readonly cardViews = inject(CardViews);
  private readonly document = inject(DOCUMENT);
  protected readonly px = px;

  private readonly bubble = viewChild<ElementRef<HTMLElement>>('bubble');
  private readonly hintBox = viewChild<ElementRef<HTMLElement>>('hintBox');

  protected readonly drawn = signal<Drawn>(NOTHING);
  private drawnKey = '';
  protected readonly collapsed = signal(false);
  /** Where the player dragged the bubble to, until its message changes. */
  protected readonly moved = signal<{ left: number; top: number } | null>(null);
  /** The shape (width over height) of the picture shown large, once it has loaded. */
  private readonly pictureShape = signal<number | null>(null);

  /** Whether anything is up: the layer is in the page only then. */
  protected readonly showing = computed(() => {
    const clicks = this.guide.nextClicks();
    return (
      this.guide.message() !== null ||
      this.guide.hint() !== null ||
      clicks.cardIds.length > 0 ||
      clicks.button !== null ||
      clicks.options.length > 0
    );
  });

  private readonly zoom = computed(() => this.guide.message()?.zoom ?? null);
  /** The address of the picture shown large, and whether a card's lies on its side. */
  private readonly zoomCard = computed(() => {
    const zoom = this.zoom();
    if (!zoom) return null;
    if (zoom.image !== null) return { url: zoom.image, horizontal: false };
    if (zoom.blueprint === null) return null;
    const view = this.cardViews.create({ ref: { zoomed: true }, blueprintId: zoom.blueprint, zone: 'SPECIAL' });
    return { url: view.imageUrl, horizontal: view.horizontal };
  });
  protected readonly zoomPicture = computed(() => this.zoomCard()?.url ?? null);
  /**
   * A card shown large takes the pointer (nothing under it is hovered or clicked) while its message is read; one
   * that stays up while the decision is answered lets clicks through to the table.
   */
  protected readonly zoomBlocks = computed(() => {
    const message = this.guide.message();
    return message !== null && (message.advance === 'next' || message.choices.length > 0);
  });

  protected readonly arrow = computed(() => {
    const pointer = this.drawn().pointer;
    return pointer.kind === 'arrow' && !this.collapsed() ? pointer : null;
  });
  protected readonly lines = computed(() => {
    const pointer = this.drawn().pointer;
    return pointer.kind === 'lines' && !this.collapsed() ? pointer.lines : null;
  });

  /** Each time the player is to read on first, the buttons under the message shake: one of two alike animations. */
  protected readonly nudge = computed(() => (this.guide.nudges() === 0 ? null : this.guide.nudges() % 2));

  private frameRequest: number | null = null;

  constructor() {
    // A new message is whole again, and where the page puts it
    effect(() => {
      this.guide.message();
      untracked(() => {
        this.collapsed.set(false);
        this.moved.set(null);
        this.pictureShape.set(null);
        this.forget();
      });
    });

    // Measured every frame while anything is up (what it points at moves: cards are played, rows fill, dialogs open)
    effect(() => {
      if (this.showing()) untracked(() => this.loop());
      else untracked(() => this.forget());
    });

    // Space or Escape reads on, heard before the page does anything else with the key
    const keyed = (event: KeyboardEvent) => this.keyed(event);
    this.document.addEventListener('keydown', keyed, true);
    inject(DestroyRef).onDestroy(() => {
      this.document.removeEventListener('keydown', keyed, true);
      this.stop();
    });
  }

  private forget(): void {
    this.drawnKey = '';
    this.drawn.set(NOTHING);
  }

  private stop(): void {
    const view = this.document.defaultView;
    if (this.frameRequest !== null && view) view.cancelAnimationFrame(this.frameRequest);
    this.frameRequest = null;
  }

  private loop(): void {
    const view = this.document.defaultView;
    if (this.frameRequest !== null || !view) return;
    const frame = () => {
      this.frameRequest = null;
      if (!this.showing()) return;
      this.measure();
      this.frameRequest = view.requestAnimationFrame(frame);
    };
    this.frameRequest = view.requestAnimationFrame(frame);
  }

  private context(): AnchorContext {
    const model = this.session.model();
    const step = this.guide.step();
    const self = model.participantId === null ? -1 : model.players.indexOf(model.participantId);
    const opponent = model.players.findIndex((player) => player !== model.participantId);
    return {
      root: this.document,
      cards: step?.cards ?? {},
      parts: step?.parts ?? {},
      sites: Object.fromEntries(
        Object.values(model.cards).flatMap((card) =>
          card.zone === 'ADVENTURE_PATH' && card.index !== null ? [[card.index, card.id] as const] : [],
        ),
      ),
      self: self < 0 ? null : self,
      opponent: opponent < 0 ? null : opponent,
      humanSeat: this.guide.info()?.humanSeat ?? 'freeps',
    };
  }

  /** Where everything is now; drawn again only if anything has moved. */
  private measure(): void {
    const view = this.document.defaultView;
    if (!view) return;
    const context = this.context();
    const message = this.guide.message();
    const viewWidth = this.document.documentElement.clientWidth - this.room.reservedRight();
    const viewHeight = this.document.documentElement.clientHeight;
    const bubble = this.bubble()?.nativeElement;
    const zoomCard = this.zoomCard();
    const zoom = this.zoom();

    const zoomLayout =
      zoom && zoomCard && bubble
        ? layoutZoom(
            zoom,
            this.pictureShape() ?? cardShape(zoomCard.horizontal),
            bubble.offsetWidth,
            viewWidth,
            viewHeight,
          )
        : null;

    // Outlines: what the message is about, and what is to be clicked next (not under a card shown large)
    const outlines: Outline[] = [];
    if (!zoomLayout) {
      for (const anchor of message?.highlight ?? []) {
        const rect = resolveAnchor(context, anchor);
        if (rect) outlines.push({ ...pad(rect), look: message?.highlightStyle ?? 'gold' });
      }
      for (const rect of this.nextClickRects()) outlines.push({ ...pad(rect), look: 'next' });
    }

    let place: Drawn['bubble'] = null;
    let pointer: Pointer = { kind: 'none' };
    if (message && bubble) {
      const size = { width: bubble.offsetWidth, height: bubble.offsetHeight };
      let anchors = anchorRects(context, message);
      let targets = anchors;
      let side = message.side;
      let keepClear: Rect[];
      let cards: Rect[] = [];
      if (zoomLayout) {
        // Beside the large card, pointing at the part of it in focus; the table underneath doesn't matter
        anchors = [zoomLayout.frame];
        targets = zoomLayout.focus.slice(0, 1);
        side = 'right';
        keepClear = [zoomLayout.frame];
      } else {
        const anchor = around(anchors);
        keepClear = [...anchors, ...outlines];
        for (const avoid of message.avoid) {
          const rect = resolveAnchor(context, avoid);
          if (rect) keepClear.push(rect);
        }
        // The buttons that answer the decision; and a dialog that is open, which is either the decision itself or
        // something the player has just asked to see (a bubble about something in a dialog sits over part of it)
        const buttons = rectAround(this.document.querySelectorAll('gd-game-alert button[data-decision-button]'));
        if (buttons) keepClear.push(buttons);
        for (const dialog of this.document.querySelectorAll('dialog.gd-dialog[open], dialog.gd-card-info[open]')) {
          const rect = rectOf(dialog);
          if (rect && (!anchor || overlap(anchor, rect) === 0)) keepClear.push(rect);
        }
        cards = [...this.document.querySelectorAll('gemp-game [data-card-id]')].flatMap((card) => {
          const rect = rectOf(card);
          return rect ? [rect] : [];
        });
      }
      const moved = this.moved();
      const placed = moved
        ? { ...moved, side: 'none' as const }
        : placeBubble({
            ...size,
            viewWidth,
            viewHeight,
            anchors,
            gap: gapPixels([message.gap, this.guide.info()?.bubbleGap ?? null], viewWidth, viewHeight),
            side,
            keepClear,
            cards,
          });
      place = { left: Math.round(placed.left), top: Math.round(placed.top) };
      const besideOn =
        !moved && placed.side !== 'none' && targets.length === 1 && anchors.length === 1 && targets[0] === anchors[0]
          ? placed.side
          : null;
      pointer = pointerFor({ ...place, ...size }, targets, besideOn, arrowReach(viewWidth, viewHeight));
    }

    const next: Drawn = {
      outlines,
      bubble: place,
      pointer,
      hint: this.hintPlace(context, viewWidth, viewHeight),
      zoom: zoomLayout,
    };
    const key = JSON.stringify(next, (_, value: unknown) => (typeof value === 'number' ? Math.round(value) : value));
    if (key !== this.drawnKey) {
      this.drawnKey = key;
      this.drawn.set(next);
    }
  }

  /** What is to be clicked next, where it is on the page. */
  private nextClickRects(): Rect[] {
    const clicks = this.guide.nextClicks();
    const elements: (Element | null)[] = clicks.cardIds.map((cardId) => cardElement(this.document, cardId));
    if (clicks.button !== null) {
      elements.push(...this.document.querySelectorAll(`button[data-decision-button="${clicks.button}"]`));
    }
    for (const option of clicks.options) {
      elements.push(...this.document.querySelectorAll(`button[data-decision-choice="${option}"]`));
    }
    return elements.flatMap((element) => {
      const rect = rectOf(element);
      return rect ? [rect] : [];
    });
  }

  /** Beside what was clicked: above it, or below if there is no room above; in the window. */
  private hintPlace(context: AnchorContext, viewWidth: number, viewHeight: number): Drawn['hint'] {
    const hint = this.guide.hint();
    const box = this.hintBox()?.nativeElement;
    if (!hint || !box) return null;
    const clicked =
      hint.cardId !== null
        ? cardElement(context.root, hint.cardId)
        : hint.button !== null
          ? context.root.querySelector(`button[data-decision-button="${hint.button}"]`)
          : hint.option !== null
            ? context.root.querySelector(`button[data-decision-choice="${hint.option}"]`)
            : null;
    const at = rectOf(clicked);
    const width = box.offsetWidth;
    const height = box.offsetHeight;
    let left = at ? at.left + at.width / 2 - width / 2 : viewWidth / 2 - width / 2;
    let top = at ? at.top - height - 10 : 80;
    if (at && top < 6) top = at.top + at.height + 10;
    left = Math.max(6, Math.min(left, viewWidth - width - 6));
    top = Math.max(6, Math.min(top, viewHeight - height - 6));
    return { left, top };
  }

  /** The bubble dragged by its head stays where it is put (until its message changes). */
  protected readonly drag = (event: PointerEvent): DragHandlers | null => {
    const bubble = this.bubble()?.nativeElement;
    if (!bubble || (event.target instanceof Element && event.target.closest('button'))) return null;
    const box = bubble.getBoundingClientRect();
    const dx = event.clientX - box.left;
    const dy = event.clientY - box.top;
    const move = (moved: PointerEvent) => this.moved.set({ left: moved.clientX - dx, top: moved.clientY - dy });
    return { move, end: move };
  };

  protected pictureLoaded(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (image.naturalWidth > 0 && image.naturalHeight > 0)
      this.pictureShape.set(image.naturalWidth / image.naturalHeight);
  }

  /**
   * Space or Escape presses the message's button (whatever it says), unless the key is meant for something else:
   * typed into a field, or Escape for the card's information that is open.
   */
  private keyed(event: KeyboardEvent): void {
    if (
      (event.key !== ' ' && event.key !== 'Escape') ||
      event.repeat ||
      event.ctrlKey ||
      event.altKey ||
      event.metaKey
    ) {
      return;
    }
    if (!this.guide.playing() || this.guide.nextLabel() === null || this.collapsed()) return;
    const target = event.target instanceof HTMLElement ? event.target : null;
    if (target?.closest('input, textarea, select, [contenteditable], [data-own-keys]')) return;
    if (event.key === 'Escape' && this.cardInfo.isOpen()) return;
    // (a button that has the focus isn't pressed by the key as well)
    if (this.document.activeElement instanceof HTMLElement) this.document.activeElement.blur();
    event.preventDefault();
    event.stopPropagation();
    this.guide.next();
  }
}

function pad(rect: Rect): Rect {
  return {
    left: rect.left - OUTLINE_PAD,
    top: rect.top - OUTLINE_PAD,
    width: rect.width + OUTLINE_PAD * 2,
    height: rect.height + OUTLINE_PAD * 2,
  };
}

/** The things a message points at that are on the screen (a row of the table: where its cards are). */
function anchorRects(context: AnchorContext, message: ScenarioMessage): Rect[] {
  return message.anchors.flatMap((anchor) => {
    const rect = resolveAnchor(context, anchor, true);
    return rect ? [rect] : [];
  });
}
