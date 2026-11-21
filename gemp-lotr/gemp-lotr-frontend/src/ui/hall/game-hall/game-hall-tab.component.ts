import { Component, computed, inject } from '@angular/core';
import { parseServerMarkup, RICH_MARKUP } from '../../../core/chat/chat-markup';
import { HallComms } from '../../../core/hall/hall-comms.service';
import { MarkupComponent } from '../../shared/chat/markup.component';
import { HALL_INFO_LINKS } from '../hall-links';
import { GameHallSectionsComponent } from './game-hall-sections.component';

/**
 * The Game Hall tab (hall.html #gameHallMain): at the top (#serverInfo) the link buttons, each opening in a new tab,
 * and the message of the day (`<b>MOTD:</b> ` and the server's markup, drawn element by element: GempLotrHallUI's
 * motd handling); then the sections of tables, queues and tournaments (`gd-game-hall-sections`). The hall's dialogs
 * are the frame's (they show over every tab).
 *
 * DOM contract: `gd-game-hall-tab.gd-game-hall` holding `div.gd-game-hall__info` (`nav.gd-game-hall__links` of
 * `a.gd-game-hall__link` (`data-link` = bug, report, discord, wiki, merchant, for its icon; `target=_blank`; the label
 * in `span.gd-game-hall__link-label`, in `s` when struck through) and `p.gd-game-hall__motd`, holding
 * `b.gd-game-hall__motd-label` "MOTD:" and `gd-markup` when there is one, empty otherwise), then
 * `gd-game-hall-sections`.
 */
@Component({
  selector: 'gd-game-hall-tab',
  styleUrl: './game-hall-tab.component.css',
  imports: [MarkupComponent, GameHallSectionsComponent],
  host: { class: 'gd-game-hall' },
  template: `
    <div class="gd-game-hall__info">
      <nav class="gd-game-hall__links" aria-label="Links">
        @for (link of links; track link.link) {
          <a class="gd-game-hall__link" [attr.data-link]="link.link" [href]="link.href" target="_blank">
            @if (link.struck) {
              <span class="gd-game-hall__link-label"
                ><s>{{ link.label }}</s></span
              >
            } @else {
              <span class="gd-game-hall__link-label">{{ link.label }}</span>
            }
          </a>
        }
      </nav>
      <p class="gd-game-hall__motd">
        @if (motd(); as motd) {
          <b class="gd-game-hall__motd-label">MOTD:</b>&ngsp;<gd-markup [nodes]="motd" />
        }
      </p>
    </div>
    <gd-game-hall-sections />
  `,
})
export class GameHallTabComponent {
  private readonly comms = inject(HallComms);
  protected readonly links = HALL_INFO_LINKS;
  protected readonly motd = computed(() => {
    const motd = this.comms.motd();
    return motd ? parseServerMarkup(motd, RICH_MARKUP) : null;
  });
}
