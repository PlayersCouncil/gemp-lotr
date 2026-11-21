import { afterNextRender, Component, effect, ElementRef, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { MANUAL_FRAGMENT } from '../../../core/hall/help/help-routes';
import { HallArticleComponent } from './hall-article.component';

/**
 * Help › How Do I Play? (help.html #howtoplay): the PC's new-player link, the three YouTube videos and the Manual.
 * `help/how-to-play#manual` (the legacy `#help/manual`) scrolls to the Manual.
 *
 * DOM contract: `gd-how-to-play` holding `gd-hall-article`, its `iframe.gd-how-to-play__video`s (420 by 315) and
 * `h1#manual.gd-how-to-play__manual`.
 */
@Component({
  selector: 'gd-how-to-play',
  styleUrl: './how-to-play.component.css',
  imports: [HallArticleComponent],
  host: { class: 'gd-how-to-play' },
  templateUrl: './how-to-play.component.html',
})
export class HowToPlayComponent {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly fragment = toSignal(inject(ActivatedRoute).fragment, { initialValue: null });

  constructor() {
    let rendered = false;
    afterNextRender(() => {
      rendered = true;
      this.scrollToManual(this.fragment());
    });
    effect(() => {
      const fragment = this.fragment();
      if (rendered) this.scrollToManual(fragment);
    });
  }

  private scrollToManual(fragment: string | null): void {
    if (fragment === MANUAL_FRAGMENT) {
      this.host.querySelector('.gd-how-to-play__manual')?.scrollIntoView({ block: 'start' });
    }
  }
}
