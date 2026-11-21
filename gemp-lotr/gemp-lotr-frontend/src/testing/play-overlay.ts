import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlayOverlay } from '../core/hall/play/play-overlay.service';
import { PlayOverlayComponent } from '../ui/hall/game-hall/play/play-overlay.component';
import { readServerResponse } from './files';
import { HallTestBed } from './hall';

/** The Play overlay drawn, opened on a flow, with what it asks for on opening answered. */
export interface OpenPlayOverlay {
  readonly fixture: ComponentFixture<PlayOverlayComponent>;
  readonly el: HTMLElement;
  /** Draws what changed. */
  update(): Promise<void>;
  /** The one element `selector` names (fails the test when there is none). */
  one<T extends Element = HTMLElement>(selector: string): T;
  /** Clicks the element `selector` names, then draws. */
  click(selector: string): Promise<void>;
  /** Sets a select's or field's value and sends its change (a select, a checkbox) or input (a text field), then draws. */
  set(selector: string, value: string): Promise<void>;
  /** The text of the element `selector` names, spaces collapsed ("" when there is none). */
  text(selector: string): string;
}

/**
 * Opens the Play overlay (as player "alice" of `setUpHall`) and answers what it asks for: the formats, the player's
 * decks, the library's, the tournament formats (or fails that with `tournamentFormatsStatus`); then opens `flow` (scenario, bot, casual, league, tournament) if given.
 */
export async function openPlayOverlay(
  hall: HallTestBed,
  flow?: string,
  options: { tournamentFormatsStatus?: number } = {},
): Promise<OpenPlayOverlay> {
  const fixture = TestBed.createComponent(PlayOverlayComponent);
  const el = fixture.nativeElement as HTMLElement;
  const update = async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  };
  const one = <T extends Element = HTMLElement>(selector: string): T => {
    const found = el.querySelector<T>(selector);
    if (!found) {
      throw new Error(`Nothing matches ${selector}`);
    }
    return found;
  };
  TestBed.inject(PlayOverlay).open();
  hall.request('/deck/formats').flush(readServerResponse('hall-formats.json'));
  hall.request('/deck/list', 'GET').flush(readServerResponse('deck-list.xml'));
  hall.request('/deck/libraryList', 'GET').flush('<decks><deck targetFormat="Fellowship Block">Starter</deck></decks>');
  const formats = hall.request('/tournament/tournamentFormats', 'GET');
  if (options.tournamentFormatsStatus) {
    formats.flush('', { status: options.tournamentFormatsStatus, statusText: 'Error' });
  } else {
    formats.flush(readServerResponse('tournament-formats.json'));
  }
  await update();
  const opened: OpenPlayOverlay = {
    fixture,
    el,
    update,
    one,
    click: async (selector) => {
      one(selector).click();
      await update();
    },
    set: async (selector, value) => {
      const field = one<HTMLInputElement | HTMLSelectElement>(selector);
      if (field instanceof HTMLInputElement && field.type === 'checkbox') {
        field.checked = value === 'true';
        field.dispatchEvent(new Event('change'));
      } else {
        field.value = value;
        field.dispatchEvent(new Event(field instanceof HTMLSelectElement ? 'change' : 'input'));
      }
      await update();
    },
    text: (selector) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim() ?? '',
  };
  if (flow) {
    await opened.click(`.gd-play-overlay__choice[data-flow=${flow}]`);
  }
  return opened;
}
