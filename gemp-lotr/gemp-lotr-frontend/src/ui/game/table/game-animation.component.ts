import { DOCUMENT } from '@angular/common';
import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  untracked,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { CardViews } from '../../../core/cards/card-views.service';
import { GameAnimation } from '../../../core/game/session/event-pacing';
import { GameSession } from '../../../core/game/session/game-session.service';

interface Box {
  left: number;
  top: number;
  width: number;
  height: number;
}

/** A card drawn only for an animation (a played event, a card affecting others), where it starts. */
interface Shown {
  image: string;
  from: Box;
}

const CARD_SCALE = 350 / 490;

/**
 * Plays the animation of the event that holds the page (gameAnimations.js), for as long as the session holds it:
 * a card put into play comes from the middle of the table to its place, removed cards fade, flipped cards turn over,
 * an activated card's frame flashes, a played event is shown in the middle of the table, a card affecting others
 * spreads out from each of them. Not scoped: it also styles the cards it moves (rules start at `gemp-game`).
 *
 * DOM contract: `gd-game-animation` holding `.gd-game-animation__card` (`img`) for each card drawn only for the
 * animation; a card flashing has `data-activated` (with `--gd-animation-duration`).
 */
@Component({
  selector: 'gd-game-animation',
  styleUrl: './game-animation.component.css',
  encapsulation: ViewEncapsulation.None,
  host: { style: 'display: contents' },
  template: `
    <div #layer class="gd-game-animation">
      @for (card of shown(); track $index) {
        <img class="gd-game-animation__card" [src]="card.image" alt="" draggable="false" />
      }
    </div>
  `,
})
export class GameAnimationComponent {
  private readonly session = inject(GameSession);
  private readonly views = inject(CardViews);
  private readonly document = inject(DOCUMENT);
  private readonly layer = viewChild.required<ElementRef<HTMLElement>>('layer');

  /** The cards the current animation draws by itself. */
  protected readonly shown = computed<Shown[]>(() => {
    const current = this.session.animation();
    const animation = current?.animation;
    if (animation?.kind === 'event') {
      const image = this.image(animation.blueprintId);
      return [{ image, from: { left: 0, top: 0, width: 0, height: 0 } }];
    }
    if (animation?.kind === 'affect') {
      return animation.cardIds.flatMap((id) => {
        const target = this.cardElement(id);
        return target ? [{ image: this.image(animation.blueprintId), from: this.box(target) }] : [];
      });
    }
    return [];
  });

  constructor() {
    effect(() => {
      const current = this.session.animation();
      // After the event's change is drawn (a card put into play is in its place)
      untracked(() => {
        const window = this.document.defaultView;
        // jsdom has no animations: the page is only held
        if (!current || !window || typeof window.Element.prototype.animate !== 'function') return;
        window.requestAnimationFrame(() => {
          // Over already (a very short one): nothing to play
          if (this.session.animation() === current) this.play(current.animation, current.duration);
        });
      });
    });
  }

  private play(animation: GameAnimation, duration: number): void {
    switch (animation.kind) {
      case 'put': {
        const card = this.cardElement(animation.cardId);
        if (card) this.flyIn(card, duration);
        break;
      }
      case 'remove':
        for (const id of animation.cardIds) {
          this.cardElement(id)?.animate([{ opacity: 1 }, { opacity: 0 }], {
            duration,
            easing: 'cubic-bezier(0.165, 0.84, 0.44, 1)',
            fill: 'forwards',
          });
        }
        break;
      case 'flip':
        for (const id of animation.cardIds) {
          // Each half ends by itself: a card is never left narrow if the other half isn't played
          const frames = [{ transform: 'scaleX(1)' }, { transform: 'scaleX(0)' }];
          this.cardElement(id)?.animate(animation.phase === 'out' ? frames : frames.reverse(), { duration });
        }
        break;
      case 'activate': {
        const card = this.cardElement(animation.cardId);
        if (card) {
          card.style.setProperty('--gd-animation-duration', `${duration}ms`);
          card.setAttribute('data-activated', '');
          setTimeout(() => card.removeAttribute('data-activated'), duration);
        }
        break;
      }
      case 'event':
        this.showEvent(duration);
        break;
      case 'affect':
        this.spread(duration);
        break;
      case 'decision':
        break;
    }
  }

  /** From the middle of the table at half size, to full size; a while there; then to its place. */
  private flyIn(card: HTMLElement, duration: number): void {
    const to = this.box(card);
    const { width, height } = this.table();
    const cardHeight = height / 2;
    const cardWidth = (cardHeight * to.width) / to.height;
    const half: Box = {
      left: width / 2 - cardWidth / 4,
      top: (height * 3) / 8,
      width: cardWidth / 2,
      height: cardHeight / 2,
    };
    const full: Box = { left: width / 2 - cardWidth / 2, top: height / 4, width: cardWidth, height: cardHeight };
    const at = (box: Box | null, opacity: number, offset: number): Keyframe => ({
      transform: box ? this.transform(to, box) : 'none',
      transformOrigin: 'top left',
      opacity,
      zIndex: 100,
      offset,
    });
    card.animate([at(half, 0, 0), at(full, 1, 1 / 8), at(full, 1, 6 / 8), at(null, 1, 1)], { duration });
  }

  /** The played event, half the table high in its middle: growing in, shown a while, fading out. */
  private showEvent(duration: number): void {
    const image = this.layer().nativeElement.querySelector<HTMLElement>('.gd-game-animation__card');
    if (!image) return;
    const { width, height } = this.table();
    const cardHeight = height / 2;
    const cardWidth = cardHeight * CARD_SCALE;
    const full: Box = { left: width / 2 - cardWidth / 2, top: height / 4, width: cardWidth, height: cardHeight };
    const half: Box = {
      left: width / 2 - cardWidth / 4,
      top: (height * 3) / 8,
      width: cardWidth / 2,
      height: cardHeight / 2,
    };
    this.place(image, full);
    image.animate(
      [
        { transform: this.transform(full, half), opacity: 0, offset: 0 },
        { transform: 'none', opacity: 1, offset: 1 / 8 },
        { transform: 'none', opacity: 1, offset: 6 / 8 },
        { transform: 'none', opacity: 0, offset: 1 },
      ],
      { duration, fill: 'forwards' },
    );
  }

  /** Over each affected card, growing to twice its size around it while fading out. */
  private spread(duration: number): void {
    const images = this.layer().nativeElement.querySelectorAll<HTMLElement>('.gd-game-animation__card');
    this.shown().forEach((shown, i) => {
      const image = images[i];
      if (!image) return;
      const from = shown.from;
      this.place(image, from);
      const to: Box = {
        left: from.left - from.width / 2,
        top: from.top - from.height / 2,
        width: from.width * 2,
        height: from.height * 2,
      };
      image.animate(
        [
          { transform: 'none', opacity: 1 },
          { transform: this.transform(from, to), opacity: 0 },
        ],
        { duration, easing: 'cubic-bezier(0.895, 0.03, 0.685, 0.22)', fill: 'forwards' },
      );
    });
  }

  private place(element: HTMLElement, box: Box): void {
    element.style.setProperty('--gd-animation-left', `${box.left}px`);
    element.style.setProperty('--gd-animation-top', `${box.top}px`);
    element.style.setProperty('--gd-animation-width', `${box.width}px`);
    element.style.setProperty('--gd-animation-height', `${box.height}px`);
  }

  /** The transform that draws an element placed at `from` at `to` (from its top left corner). */
  private transform(from: Box, to: Box): string {
    const dx = to.left - from.left;
    const dy = to.top - from.top;
    return `translate(${dx}px, ${dy}px) scale(${to.width / from.width}, ${to.height / from.height})`;
  }

  private table(): { width: number; height: number } {
    const window = this.document.defaultView;
    return { width: window?.innerWidth ?? 0, height: window?.innerHeight ?? 0 };
  }

  private box(element: Element): Box {
    const rect = element.getBoundingClientRect();
    return { left: rect.left, top: rect.top, width: rect.width, height: rect.height };
  }

  private cardElement(cardId: string): HTMLElement | null {
    const all = this.document.querySelectorAll<HTMLElement>(`gemp-game [data-card-id="${CSS.escape(cardId)}"]`);
    return (
      Array.from(all)
        .filter((element) => element.getBoundingClientRect().width > 0)
        .at(-1) ?? null
    );
  }

  private image(blueprintId: string): string {
    return this.views.create({ ref: null, blueprintId, zone: 'ANIMATION' }).imageUrl;
  }
}
