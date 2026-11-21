import { DOCUMENT } from '@angular/common';
import {
  afterRenderEffect,
  Component,
  computed,
  ElementRef,
  inject,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { Router } from '@angular/router';
import { routeParts } from '../../../../core/hall/hall-links';
import { LeagueTableFlow } from '../../../../core/hall/play/league-table.service';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { BACK } from '../../../../core/hall/play/play-messages';
import { windowTooSmallForTutorial } from '../../../../core/hall/play/scenarios/scenario-list';
import { HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { DialogComponent } from '../../../shared/dialog/dialog.component';
import { BotFormComponent } from './bot-form.component';
import { CasualFormComponent } from './casual-form.component';
import { LeagueFormComponent } from './league-form.component';
import { ScenarioPanelComponent } from './scenario-panel.component';
import { TournamentFormComponent } from './tournament-form.component';

/**
 * The Play overlay (CreateTable, hall.html #create-table-popup): one dialog, "Create Table" or "Create Table ▸ <flow>",
 * the size of the legacy popup (900 x 800, kept inside the window), holding "< Back" (sticky at the top), the load
 * errors, and the choice of the five flows or the chosen flow's form. Back goes to the choice, and from the choice
 * closes. A flow that succeeds closes it; the format (i)'s link and the My Scenarios link close it and go there.
 *
 * Its stylesheet is not scoped: it draws the forms of every flow (`.gd-play-form…`, `gd-play-overlay …`) and is shared
 * by the Join dialog's form, which is in the hall's frame beside it.
 *
 * DOM contract: `gd-play-overlay` (no box) holding `gd-dialog[data-dialog=play]` with `div.gd-play-overlay` holding
 * `button.gd-play-overlay__back`, `div.gd-play-overlay__error[role=alert]` per load error, then
 * `div.gd-play-overlay__choices` (a `button.gd-play-overlay__choice` per flow: `data-flow`, `span.gd-play-overlay__title`
 * with `span.gd-play-overlay__icon[data-icon]`, and `span.gd-play-overlay__subtitle`) or the flow's form
 * (`gd-scenario-panel`, `gd-bot-form`, `gd-casual-form`, `gd-league-form`, `gd-tournament-form`).
 */
@Component({
  selector: 'gd-play-overlay',
  styleUrl: './play-overlay.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [
    BotFormComponent,
    CasualFormComponent,
    DialogComponent,
    LeagueFormComponent,
    ScenarioPanelComponent,
    TournamentFormComponent,
  ],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="play" [heading]="overlay.title()" [open]="overlay.isOpen()" (closed)="overlay.close()">
      @if (overlay.isOpen()) {
        <div class="gd-play-overlay">
          <button #backButton type="button" class="gd-play-overlay__back" autofocus (click)="overlay.back()">
            {{ back }}
          </button>
          @for (error of errors(); track $index) {
            <div class="gd-play-overlay__error" role="alert">{{ error }}</div>
          }
          @switch (overlay.screen()) {
            @case ('choice') {
              <div class="gd-play-overlay__choices">
                @for (choice of overlay.choices; track choice.flow) {
                  <button
                    type="button"
                    class="gd-play-overlay__choice"
                    [attr.data-flow]="choice.flow"
                    (click)="choose(choice.flow)"
                  >
                    <span class="gd-play-overlay__title"
                      ><span class="gd-play-overlay__icon" [attr.data-icon]="choice.icon" aria-hidden="true"></span
                      ><span>{{ choice.title }}</span></span
                    >
                    <span class="gd-play-overlay__subtitle">{{ choice.subtitle }}</span>
                  </button>
                }
              </div>
            }
            @case ('scenario') {
              <gd-scenario-panel [smallWindow]="smallWindow()" (started)="overlay.close()" (route)="go($event)" />
            }
            @case ('bot') {
              <gd-bot-form (created)="overlay.close()" (helpLink)="go($event)" />
            }
            @case ('casual') {
              <gd-casual-form (created)="overlay.close()" (helpLink)="go($event)" />
            }
            @case ('league') {
              <gd-league-form (created)="overlay.close()" />
            }
            @case ('tournament') {
              <gd-tournament-form (created)="overlay.close()" (helpLink)="go($event)" />
            }
          }
        </div>
      }
    </gd-dialog>
  `,
})
export class PlayOverlayComponent {
  protected readonly overlay = inject(PlayOverlay);
  private readonly tables = inject(HallTablesService);
  private readonly league = inject(LeagueTableFlow);
  private readonly router = inject(Router);
  private readonly window = inject(DOCUMENT).defaultView;
  protected readonly back = BACK;
  protected readonly smallWindow = signal(false);
  private readonly backButton = viewChild<ElementRef<HTMLButtonElement>>('backButton');

  constructor() {
    // each screen starts with Back focused (CreateTable.openFlow)
    afterRenderEffect(() => {
      this.overlay.screen();
      this.backButton()?.nativeElement.focus();
    });
  }

  /** The formats' and leagues' load errors (FormatManager.reportError, #play-load-error). */
  protected readonly errors = computed(() =>
    [this.tables.optionsError(), this.league.loadError()].filter((error): error is string => error !== null),
  );

  protected choose(flow: Parameters<PlayOverlay['choose']>[0]): void {
    if (flow === 'scenario') {
      // a tutorial's bubbles need room to point at things (the game screen checks again)
      this.smallWindow.set(windowTooSmallForTutorial(this.window?.innerWidth ?? 0, this.window?.innerHeight ?? 0));
    }
    this.overlay.choose(flow);
  }

  /** A link out of the overlay (Help › Format Definitions, My Scenarios): it closes and the hall goes there. */
  protected go(route: string): void {
    this.overlay.formatHelpOpened();
    const parts = routeParts(`/${route}`);
    void this.router.navigate([parts.path], { queryParams: parts.queryParams });
  }
}
