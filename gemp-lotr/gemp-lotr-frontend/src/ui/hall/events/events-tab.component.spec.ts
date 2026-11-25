import { PlayerInfo } from '../../../core/session/player-info.service';
import { signal } from '@angular/core';
import { EventsPage, setUpEventsPage } from '../../../testing/events-server';

/** The Events tab's "League operation" question: No, or closing it, joins nothing. */
describe('EventsTabComponent', () => {
  let events: EventsPage;

  beforeEach(async () => {
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    events = await setUpEventsPage([
      { provide: PlayerInfo, useValue: { player: signal({ name: 'alice', type: 'u' }) } },
    ]);
  });

  afterEach(() => vi.unstubAllGlobals());

  it('asks before joining a league; No and the close button send nothing', async () => {
    const el = await events.open('/events/leagues/1700000001');
    events.answer('/league/1700000001', 'league-detail.xml');
    await events.render();
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog=league-join]')!;
    const join = () => el.querySelector<HTMLButtonElement>('gd-event-row[data-key="1700000001"] [data-action=join]')!;

    join().click();
    await events.render();
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[data-action=no]')?.textContent?.trim()).toBe('No');
    dialog.querySelector<HTMLButtonElement>('[data-action=no]')!.click();
    await events.render();
    expect(dialog.open).toBe(false);

    join().click();
    await events.render();
    dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    await events.render();
    expect(dialog.open).toBe(false);
    events.backend.expectNone((request) => request.method === 'POST');
  });
});
