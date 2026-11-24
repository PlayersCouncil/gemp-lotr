import { computed, inject, Injectable } from '@angular/core';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { displayName } from '../scenario/scenario-state';
import { GameSession } from '../session/game-session.service';
import { EditSeat } from './edit-ops';
import { editSeats, EditSeats } from './edit-seats';

/** A seat's words where its player is not known yet. */
export const SEAT_WORDS: Readonly<Record<EditSeat, string>> = { freeps: 'Free Peoples', shadow: 'Shadow' };

/**
 * Who sits in which of the scenario's seats on this page (the person in theirs, the other player in the other), and
 * what each seat is called in the edit controls: its player's name as the page shows it (a scripted opponent under
 * its scenario's name). The pile windows, the path's chooser and a site's menu name seats with it. Page-scoped.
 */
@Injectable()
export class EditSeating {
  private readonly session = inject(GameSession);
  private readonly guide = inject(ScenarioGuide);

  readonly seats = computed<EditSeats>(() => editSeats(this.session.model(), this.guide.info()?.humanSeat ?? 'freeps'));

  readonly names = computed<Readonly<Record<EditSeat, string>>>(() => {
    const { players } = this.seats();
    const scenario = this.session.model().scenario;
    const name = (seat: EditSeat) => (players[seat] === '' ? SEAT_WORDS[seat] : displayName(scenario, players[seat]));
    return { freeps: name('freeps'), shadow: name('shadow') };
  });
}
